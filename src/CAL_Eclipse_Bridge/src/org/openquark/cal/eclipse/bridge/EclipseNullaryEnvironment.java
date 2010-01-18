/*
 * Copyright (c) 2007 BUSINESS OBJECTS SOFTWARE LIMITED
 * All rights reserved.
 * 
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 * 
 *     * Redistributions of source code must retain the above copyright notice,
 *       this list of conditions and the following disclaimer.
 *  
 *     * Redistributions in binary form must reproduce the above copyright
 *       notice, this list of conditions and the following disclaimer in the
 *       documentation and/or other materials provided with the distribution.
 *  
 *     * Neither the name of Business Objects nor the names of its contributors
 *       may be used to endorse or promote products derived from this software
 *       without specific prior written permission.
 *  
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 */


/*
 * EclipseNullaryEnvironment.java
 * Created: Dec 28, 2005
 * By: Edward Lam
 */
package org.openquark.cal.eclipse.bridge;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import org.eclipse.core.runtime.FileLocator;
import org.openquark.cal.services.CALSourcePathMapper;
import org.openquark.cal.services.CarBuilder;
import org.openquark.cal.services.FileSystemResourceHelper;
import org.openquark.cal.services.NullaryEnvironment;
import org.openquark.cal.services.ResourceName;
import org.openquark.cal.services.ResourcePath;
import org.openquark.cal.services.ResourcePathMapper;
import org.openquark.cal.services.WorkspaceLoader;
import org.openquark.cal.services.ResourceName.Filter;
import org.openquark.cal.services.ResourcePath.Folder;
import org.openquark.util.ClassInfo;
import org.openquark.util.FileSystemHelper;
import org.openquark.util.Pair;



/**
 * The nullary environment which exists when executing code within the context of an Eclipse plugin.
 * @author Edward Lam
 */
class EclipseNullaryEnvironment extends NullaryEnvironment {

    /** Singleton instance. */
    public static final EclipseNullaryEnvironment INSTANCE = new EclipseNullaryEnvironment();
    
    /**
     * Constructor for a EclipseNullaryEnvironment.
     * Private - access via the singleton instance.
     */
    EclipseNullaryEnvironment() {
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public List<ResourceName> getFolderResourceNames(ResourcePath.Folder folder, ResourcePathMapper pathMapper) {
        return getFilteredFolderResourceNames(folder, pathMapper, null);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<ResourceName> getResourceNamesInSubtreeRootedAtFolder(Folder folder, ResourcePathMapper pathMapper) {
        
        Set<ResourceName> filteredSet = new TreeSet<ResourceName>();        // sort alphabetically..
        for (final Pair<ResourcePath.Folder, String> pair : getFileFolderNamePairsInSubtreeRootedAtFolder(folder, false)) {
            ResourcePath.Folder fileFolder = pair.fst();
            String fileName = pair.snd();
            
            ResourceName resourceName = pathMapper.getResourceNameFromFolderAndFileName(fileFolder, fileName);
            
            // It passes if the path mapper returns a non-null resource name (e.g. it has the right file extension).
            if (resourceName != null) {
                filteredSet.add(resourceName);
            }
        }
        
        return new ArrayList<ResourceName>(filteredSet);
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public List<ResourceName> getFilteredFolderResourceNames(Folder folder, ResourcePathMapper pathMapper, Filter filter) {
        
        Set<ResourceName> filteredSet = new TreeSet<ResourceName>();        // sort alphabetically..
        for (final String fileName : getFileNamesInFolder(folder)) {
            ResourceName resourceName = pathMapper.getResourceNameFromFolderAndFileName(folder, fileName);
            
            // It passes if the path mapper returns a non-null resource name (e.g. it has the right file extension).
            if (resourceName != null) {
                // If the filter is not null, check the path to make sure it's acceptable first
                if (filter == null || filter.accept(resourceName)) {
                    filteredSet.add(resourceName);
                }
            }
        }
        
        return new ArrayList<ResourceName>(filteredSet);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public File getFile(ResourcePath resourcePath, boolean createDirectoryIfAbsent) {
        if (resourcePath instanceof ResourcePath.FilePath) {
            return getFile((ResourcePath.FilePath)resourcePath, createDirectoryIfAbsent);
        
        } else if (resourcePath instanceof ResourcePath.Folder) {
            return getFile((ResourcePath.Folder)resourcePath, createDirectoryIfAbsent);
        
        } else {
            throw new IllegalArgumentException("Unknown ResourcePath type: " + resourcePath.getClass().getName());
        }
    }

    /**
     * @param folder the folder for which to return a file.
     * @param createDirectoryIfAbsent whether to create the directory to the file if it does not exist.
     * @return the file corresponding to this resource path.  If createDirectoryIfAbsent is false, the file does not exist, 
     * and the directory for this file does not exist, null will be returned.
     */
    private static File getFile(ResourcePath.Folder folder, boolean createDirectoryIfAbsent) {
        // Check whether the resource already exists.
        File existingFolderFile = getExistingFolderFile(folder);
        if (existingFolderFile != null) {
            return existingFolderFile;
        }
        
        if (!createDirectoryIfAbsent) {
            return null;
        }
        
        return ensurePathExists(folder);
    }
    
    /**
     * @param filePath the FilePath for which to return a file.
     * @param createDirectoryIfAbsent whether to create the directory to the file if it does not exist.
     * @return the file corresponding to this resource path.  If createDirectoryIfAbsent is false, the file does not exist, 
     * and the directory for this file does not exist, null will be returned.
     */
    private static File getFile(ResourcePath.FilePath filePath, boolean createDirectoryIfAbsent) {
        
        String pathString = filePath.getPathString();

        // If the file exists, return it.
        File pathFile = getExistingFile(pathString);
        if (pathFile != null && !pathFile.isDirectory()) {
            return pathFile;
        }
    
        // If here, the file does not exist.  Determine the appropriate folder.
        File folderFile = getFile(filePath.getFolder(), createDirectoryIfAbsent);
        if (folderFile == null) {
            return null;
        }
    
        // Return a file object in the folder.
        String[] pathElements = filePath.getPathElements();
        String fileName = pathElements[pathElements.length - 1];
        
        return new File(folderFile, fileName);
    }
    
    /**
     * Ensure that the directory corresponding to a folder exists on the file system.
     * @param folder the folder in question.
     * @return the corresponding directory, or null if a corresponding directory does not exist and could not be created.
     *   If this is a file, the path to the directory will be created (if non-existent) but the file will not be created.
     */
    private static File ensurePathExists(ResourcePath.Folder folder) {
        
        String[] pathElements = folder.getPathElements();
        
        // Find the maximal sub path which already exists.
        for (int i = pathElements.length - 1; i >= -1; i--) {
            
            int nSubPathElements = i + 1;
            
            // Construct /pathElem1/pathElem2/.../pathElemi/
            String[] subPathElements = new String[nSubPathElements];
            System.arraycopy(pathElements, 0, subPathElements, 0, nSubPathElements);
            
            ResourcePath.Folder subPath = new ResourcePath.Folder(subPathElements);
            
            File subpathFolderFile = getExistingFolderFile(subPath);
            if (subpathFolderFile != null) {
                // If here, subpathFolderFile corresponds to the maximal sub path which already exists.
                StringBuilder remainingPathStringBuilder = new StringBuilder("/");
                for (int j = nSubPathElements; j < pathElements.length; j++) {
                    remainingPathStringBuilder.append(pathElements[j] + "/");
                }
                
                File wholePathDir = new File(subpathFolderFile, remainingPathStringBuilder.toString());
                boolean directoryExists = FileSystemHelper.ensureDirectoryExists(wholePathDir);
    
                if (!directoryExists) {
                    // For some reason, the directory doesn't exist, and couldn't be created.
                    break;
                }
    
                return wholePathDir;
            }
        }
        
        // If here, we couldn't even get the resource for "/".
        return null;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Set<String> getFolderNamesWithFileExtension(ResourcePath.Folder parentFolder, ResourcePathMapper pathMapper, boolean convertFromFileSystemName) {
        Set<String> subFolderNameSet = new HashSet<String>();
        
        List<String> subFolderNames = getFileNamesInFolder(parentFolder);
        
      subFolderName:
        
        for (final String subFolderName : subFolderNames) {
            String moduleName = convertFromFileSystemName ? FileSystemResourceHelper.fromFileSystemName(subFolderName) : subFolderName;
            ResourcePath.Folder subFolder = parentFolder.extendFolder(subFolderName);
            
            // Check whether the module folder has a file with the given extension.
            if (!getFolderResourceNames(subFolder, pathMapper).isEmpty()) {
                subFolderNameSet.add(moduleName);
                continue;
            }
            
            // Check whether any of the files in the folder tree have a file with the given extension.
            List<String> subFolderResourceNames = getFileNamesInFolder(subFolder);
            for (final String string : subFolderResourceNames) {
                ResourcePath.Folder subFolderResourceFolder = subFolder.extendFolder(string);
                File subFolderResourceDirectory = getFile(subFolderResourceFolder, false);
                
                if (subFolderResourceDirectory != null) {
                    Set<String> filesInModuleTree = new HashSet<String>();
                    FileSystemResourceHelper.getFilesInDirectoryTree(subFolderResourceDirectory, filesInModuleTree);
                    
                    for (String fileName : filesInModuleTree) {
                        if (convertFromFileSystemName) {
                            fileName = FileSystemResourceHelper.fromFileSystemName(fileName);
                        }
                        if (fileName.endsWith(CALSourcePathMapper.CAL_FILE_EXTENSION)) {
                            subFolderNameSet.add(moduleName);
                            continue subFolderName;
                        }
                    }
                }
            }
        }
        
        return subFolderNameSet;
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public Set<ResourcePath.Folder> getResourcePathsOfAllNonEmptySubfolders(ResourcePath.Folder parentFolder, ResourcePathMapper pathMapper) {
        
        Set<ResourcePath.Folder> result = new HashSet<Folder>(); 
        
        List<Pair<ResourcePath.Folder, String>> subfolderParentNamePairs =
            getFileFolderNamePairsInSubtreeRootedAtFolder(parentFolder, true);
        
        for (final Pair<ResourcePath.Folder, String> pair : subfolderParentNamePairs) {
            ResourcePath.Folder subfolderParent = pair.fst();
            String subfolderName = pair.snd();
            
            ResourcePath.Folder subfolder = subfolderParent.extendFolder(subfolderName);
            
            if (!getFolderResourceNames(subfolder, pathMapper).isEmpty()) {
                result.add(subfolder);
            }
        }
        
        return result;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Set<File> getCarJarFilesInEnvironment() {
        
        // The jar files in the environment are those on the classpath. We require
        // that the jar files be well-formed, with a manifest file in the appropriate location.
        
        // We look for jars by finding all the locatable manifest files, and then deconstructing the
        // resource URIs to obtain the underlying jar files.
        
        Enumeration<URL> platformURLs;
        try {
            /*
             * Can't' use:
             *   platformURLs = ClassInfo.getResources(pathString);
             * See comment in getExistingFile()
             */

            platformURLs = getCallingClass().getClassLoader().getResources(CarBuilder.CAR_MARKER_NAME);
        
        } catch (IOException e) {
            platformURLs = null;
        }
        
        Set<File> carJarFilesOnClasspath = new HashSet<File>();
        
        if (platformURLs != null) {
            while (platformURLs.hasMoreElements()) {
                URL platformURL = platformURLs.nextElement();
                
                URL resolvedURL;
                try {
                    resolvedURL = FileLocator.resolve(platformURL);
                } catch (IOException e) {
                    continue;
                }
                
                URI uri = WorkspaceLoader.urlToUri(resolvedURL);
                
                if (uri.isOpaque() && uri.getScheme().equals("jar")) {
                    String schemeSpecificPart = uri.getRawSchemeSpecificPart();
                    String jarFilePart = schemeSpecificPart.substring(0, schemeSpecificPart.indexOf("!/"));

                    // The resolvedURL may be of the form "jar:file:c:\Program Files\foo",
                    // where it is missing a leading slash after the "file:" and where the space
                    // is not escaped. Luckily the URI constructor handles the space character, but
                    // the File constructor does not like the opaque url (opaque because of the missing slash).
                    
                    if (jarFilePart.startsWith("file:") && !jarFilePart.startsWith("file:/")) {
                        jarFilePart = "file:/" + jarFilePart.substring(5);
                    }
                    
                    try {
                        File jarFile = new File(new URI(jarFilePart));
                        
                        if (jarFile.getName().endsWith(CarBuilder.DOT_CAR_DOT_JAR)) {
                            carJarFilesOnClasspath.add(jarFile);
                        }
                        
                    } catch (URISyntaxException e) {
                        // skip this bad entry and continue
                    }
                }
            }
        }
        
        return carJarFilesOnClasspath;
    }
    
    /**
     * Get the file names of resources which reside in a folder.
     * This will be sorted.
     * 
     * @param folder the folder to search.
     * @return the file names of the resources.
     */
    private static List<String> getFileNamesInFolder(ResourcePath.Folder folder) {
        
        // The list to return.
        List<String> fileNamesList = new ArrayList<String>();
        
        // Get the existing folder files.
        File[] existingFolderFiles = getExistingFolderFiles(folder);
        
        // Iterate over the members.  Add names for files.
        for (final File folderFile : existingFolderFiles) {
            // Add the matching files from the directory.
            File[] directoryFiles = folderFile.listFiles();
            if (directoryFiles != null) {
                for (final File directoryFile : directoryFiles) {
                    fileNamesList.add(directoryFile.getName());
                }
            }
        }
        
        Collections.sort(fileNamesList);
        return fileNamesList;
    }
    
    /**
     * Get the file names of resources which reside in the subtree rooted at the folder.
     * This will not be sorted.
     * 
     * @param folder the folder to search.
     * @param addSubfolderNames whether the returned list should contain entries for subfolders as well as files.
     * @return the pairs of (file folder, file name) of the resources.
     */
    private static List<Pair<ResourcePath.Folder, String>> getFileFolderNamePairsInSubtreeRootedAtFolder(ResourcePath.Folder folder, boolean addSubfolderNames) {
        
        // The list to return.
        List<Pair<Folder, String>> fileNamesList = new ArrayList<Pair<Folder, String>>();
        
        // Get the existing folder files.
        File[] existingFolderFiles = getExistingFolderFiles(folder);
        
        // Iterate over the members.  Add names for files.
        for (final File folderFile : existingFolderFiles) {
            // Add the matching files from the directory.
            FileSystemResourceHelper.getFileFolderNamePairsInDirectoryTree(folderFile, folder, fileNamesList, addSubfolderNames);
        }
        
        return fileNamesList;
    }
    
    /**
     * @param folderPath the path to a folder
     * @return the existing files in the file system which correspond to that folder.
     */
    private static File[] getExistingFolderFiles(ResourcePath.Folder folderPath) {
        String pathString = folderPath.getPathString();
        
        List<File> existingFolderFileList = new ArrayList<File>();
        
        // Look for a resource with the given name.
        File[] existingFiles = getExistingFiles(pathString);
        if (existingFiles != null) {
            existingFolderFileList.addAll(Arrays.asList(existingFiles));
        }
        
        // Add a slash to the end and try again.
        // Unslashed will find the folder with respect to the current project.
        // Slashed will find the folder with respect to dependee projects.
        pathString += "/";
        existingFiles = getExistingFiles(pathString);
        if (existingFiles != null) {
            existingFolderFileList.addAll(Arrays.asList(existingFiles));
        }
        
        return existingFolderFileList.toArray(new File[existingFolderFileList.size()]);
    }

    /**
     * @param folderPath the path to a folder
     * @return an existing file in the file system which corresponds to that folder.
     */
    private static File getExistingFolderFile(ResourcePath.Folder folderPath) {
        String pathString = folderPath.getPathString();
        
        // Look for a resource with the given name.
        File pathFile = getExistingFile(pathString);
        if (pathFile != null) {
            return pathFile;
        }
        
        // Add a slash to the end and try again.
        // Unslashed will find the folder with respect to the current project.
        // Slashed will find the folder with respect to dependee projects.
        pathString += "/";
        pathFile = getExistingFile(pathString);
        if (pathFile != null) {
            return pathFile;
        }
        
        return null;
    }
    
    /**
     * @return the class currently calling a method in EclipseNullaryEnvironment.
     * Null if all classes in the call stack are this class.
     */
    private static Class<?> getCallingClass() {
        Class<?>[] classContext = ClassInfo.getClassContext();
        for (int i = 1; i < classContext.length; i++) {
            Class<?> ithClass = classContext[i];
            if (ithClass != ClassInfo.class && ithClass != EclipseNullaryEnvironment.class) {
                return ithClass;
            }
        }
        
        // All classes in the call stack are this class.
        return null;
    }
    
    /**
     * @param pathString the path to a file.
     * @return the existing file in the file system corresponding to that path, or null if there isn't any.
     */
    private static File getExistingFile(String pathString) {
        /*
         * Note that we can't use: 
         *   URL platformURL = ClassInfo.getResource(pathString);
         *   
         * ClassInfo uses the classloader of the calling class to resolve resources.
         * However, the Eclipse classloader uses a similar trick to figure out the context in which to perform lookups.
         * Eventually, it figures out that the closest class on the stack which isn't part of its Eclipse plugins is the ClassInfo class itself.
         * So it will perform lookups within the context of the ClassInfo class - ie. wrt the Utilities project.
         * 
         * What we have below is to perform lookups within the context of the class calling methods in EclipseNullaryEnvironment.
         */
        
        // remove any leading slash.
        if (pathString.startsWith("/")) {
            pathString = pathString.substring(1);
        }
        URL platformURL = getCallingClass().getClassLoader().getResource(pathString);
        if (platformURL == null) {
            return null;
        }
        URL resolvedURL;
        try {
            resolvedURL = FileLocator.resolve(platformURL);
        } catch (IOException e) {
            return null;
        }
        if ("file".equals(resolvedURL.getProtocol())) {
            return new File(WorkspaceLoader.urlToUri(resolvedURL));
        }
        
        return null;
    }
    
    /**
     * @param pathString the path to a file.
     * @return the existing file in the file system corresponding to that path.
     */
    private static File[] getExistingFiles(String pathString) {
        // remove any leading slash.
        if (pathString.startsWith("/")) {
            pathString = pathString.substring(1);
        }

        Enumeration<URL> platformURLs;
        try {
            /*
             * Can't' use:
             *   platformURLs = ClassInfo.getResources(pathString);
             * See comment in getExistingFile()
             */

            platformURLs = getCallingClass().getClassLoader().getResources(pathString);
        
        } catch (IOException e) {
            // Can't get the resources.
            return new File[] {};
        }
        
        List<File> existingFileList = new ArrayList<File>();
        
        while (platformURLs.hasMoreElements()) {
            URL platformURL = platformURLs.nextElement();
            
            URL resolvedURL;
            try {
                resolvedURL = FileLocator.resolve(platformURL);
            } catch (IOException e) {
                continue;
            }
            if ("file".equals(resolvedURL.getProtocol())) {
                existingFileList.add(new File(WorkspaceLoader.urlToUri(resolvedURL)));
            }
        }
        
        return existingFileList.toArray(new File[existingFileList.size()]);
    }

}

