/*******************************************************************************
 * Copyright (c) 2006 Business Objects Software Limited and others.
 * All rights reserved. 
 * This file is made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Business Objects Software Limited - initial API and implementation
 *******************************************************************************/

/*
 * EclipseFileResourceHelper.java
 * Creation date: Dec 14, 2005.
 * By: Edward Lam
 */
package org.openquark.cal.eclipse.core;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.ListIterator;

import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.openquark.cal.eclipse.core.util.Util;
import org.openquark.cal.services.ResourceName;
import org.openquark.cal.services.ResourcePath;
import org.openquark.cal.services.ResourcePathMapper;



/**
 * A static helper class which contains various utility methods used in the manipulation of files
 * with respect to the Eclipse workspace.
 * @author Edward Lam
 */
public final class EclipseFileResourceHelper {
    
    /*
     * Not intended to be instantiated.
     */
    private EclipseFileResourceHelper() {
    }
 
    /**
     * Convert a ResourcePath to an IPath
     * @param resourcePath the resource path to convert.
     * @return the corresponding IPath.
     */
    public static IPath getPath(ResourcePath resourcePath) {
        // Get the path string
        String pathString = resourcePath.getPathString();
        if (resourcePath instanceof ResourcePath.Folder) {
            pathString += "/";
        }

        return new Path(pathString);
    }

    // ADE DELETEME?
    /**
     * @param resourcePath the path to a resource.
     * @return the resource indicated by the locator.
     */
//    public static IResource getIResource(ResourcePath resourcePath, IContainer rootContainer) {
//        return getIResource(resourcePath, rootContainer, false);
//    }
    
    
    // ADE DELETEME?
    /**
     * Get the file which corresponds to the given path
     * @param resourcePath the resource path.
     * @param rootContainer the root container for resources using resource paths.
     * @param createFolderIfAbsent whether to create the folder to the file if it does not exist.
     * @return the file which corresponds to that path.
     */
//    public static IResource getIResource(ResourcePath resourcePath, IContainer rootContainer, boolean createFolderIfAbsent) {
//
//        IPath rootFolderPath = rootContainer.getFullPath();
//        IPath resourceIPath = getPath(resourcePath);
//        IPath resourceFileIPath = rootFolderPath.append(resourceIPath);
//        IResource resource = resourcePath instanceof ResourcePath.FilePath ? 
//                (IResource)ResourcesPlugin.getWorkspace().getRoot().getFile(resourceFileIPath) :
//                    ResourcesPlugin.getWorkspace().getRoot().getFolder(resourceFileIPath);
//
//        // Create the folder if that's what we need to do.
//        if (createFolderIfAbsent && !resource.exists()) {
//            IFolder fileFolder;
//            if (resourcePath instanceof ResourcePath.Folder) {
//                fileFolder = (IFolder)resource;
//                
//            } else if (resourcePath instanceof ResourcePath.FilePath) {
//                
//                IResource parentResource = ((IFile)resource).getParent();
//                
//                if (parentResource instanceof IFolder) {
//                    fileFolder = (IFolder)parentResource;
//                } else if (parentResource instanceof IProject) {
//                    fileFolder = null;
//                } else {
//                    throw new IllegalStateException("Unknown parent resource type: " + parentResource.getClass());
//                }
//                                                                
//
//            } else {
//                throw new IllegalArgumentException("Unknown ResourcePath type: " + resourcePath.getClass().getName());
//            }
//            
//            if (fileFolder != null && !fileFolder.exists()) {
//                try {
//                    ensureFolderExists(fileFolder);
//                } catch (IOException e) {
//                    Util.log(e, "Could not create folder: " + fileFolder, IStatus.WARNING);
//                }
//            }
//            
//        }
//        return resource;
//    }
    
    /**
     * Create a folder if it does not already exist.
     * @param iFolder the handle to the folder.
     * @throws IOException if there was a problem creating the folder.
     */
    public static void ensureFolderExists(IFolder iFolder) throws IOException {
        
        // Gather the folder and ancestors which don't exist.
        List<IFolder> parentsToCreate = new ArrayList<IFolder>();
        for (IFolder parentFolder = iFolder; !parentFolder.exists(); ) {
            parentsToCreate.add(parentFolder);
            
            // Update parentFolder for the next iteration.
            IContainer parent = parentFolder.getParent();
            if (parent instanceof IFolder) {
                parentFolder = (IFolder)parent;
            } else {
                // Should be a project.
                if (!(parent instanceof IProject)) {
                    throw new IllegalStateException();
                }
                // Can't go any higher than the project.
                break;
            }
        }
        
        // Check whether the folder already existed.
        if (parentsToCreate.isEmpty()) {
            return;
        }
        
        // Iterate over the ancestors in reverse order, creating the folders.
        try {
            for (ListIterator<IFolder> revIt = parentsToCreate.listIterator(parentsToCreate.size()); revIt.hasPrevious(); ) {
                IFolder prevFolder = revIt.previous();
                
                // force, local, progressMonitor.
                prevFolder.create(true, true, null);
            }
        } catch (CoreException e) {
            Util.log((new IOException()).initCause(e), "Error creating folder " + iFolder);
        }
    }

    
    /**
     * Get the names representing the resources in a folder.  This will be sorted.
     * 
     * @param resourceFolder the folder to search.
     * @param fileExtension the file extension used to represent resources of the desired type.
     *   If null, any resource will match.
     * @param folder the resource folder corresponding to the folder searched.
     * @param pathMapper the path mapper to use to map paths back to resource names
     * @return the names representing the resources in the given folder.
     *   Never null, but may be empty if the given file is not a folder.
     */
    public static List<ResourceName> getFolderResourceNames(IFolder resourceFolder, final String fileExtension, ResourcePath.Folder folder, ResourcePathMapper pathMapper) {
        return getFilteredFolderResourceNames(resourceFolder, fileExtension, folder, pathMapper, null);
    }
    
    /**
     * Get a filtered list of the names representing the resources in a folder.  This will be sorted.
     * 
     * @param resourceFolder the folder to search.
     * @param fileExtension the file extension used to represent resources of the desired type.
     *   If null, any resource will match.
     * @param folder the resource folder corresponding to the folder searched.
     * @param pathMapper the path mapper to use to map paths back to resource names
     * @param filter the filter to use for determining which resource names to keep in the returned list.
     *   Null for no filter.
     * @return the names representing the resources in the given folder.
     *   Never null, but may be empty if the given file is not a folder.
     */
    public static List<ResourceName> getFilteredFolderResourceNames(IFolder resourceFolder, final String fileExtension, ResourcePath.Folder folder, ResourcePathMapper pathMapper, ResourceName.Filter filter) {

        List<IFile> matchingFileList = members(resourceFolder, fileExtension);
        if (matchingFileList == null) {
            return Collections.emptyList();
        }
        
        List<ResourceName> folderResourceNames = new ArrayList<ResourceName>();
        for (final IFile iFile : matchingFileList) {
            IFile matchingFile = iFile;
            String fileName = matchingFile.getName();
            
            // If the filter is not null, check the path to make sure it's acceptable first
            ResourceName folderResourceName = pathMapper.getResourceNameFromFolderAndFileName(folder, fileName);
            
            // It passes if the path mapper returns a non-null resource name (e.g. it has the right file extension).
            if (folderResourceName != null) {
                // If the filter is not null, check the path to make sure it's acceptable first
                if (filter == null || filter.accept(folderResourceName)) {
                    folderResourceNames.add(folderResourceName);
                }
            }
        }
        
        Collections.sort(folderResourceNames);
        return folderResourceNames;
    }
    
    /**
     * Get the file members of a container, optionally with a given extension.
     * @param parentContainer the container to check.
     * @param fileExtension the file extension to check for.  If null, all member files will be returned.
     * @return the matching members.
     */
    public static List<IFile> members(IContainer parentContainer, String fileExtension) {
        
        // Get the folder members.
        IResource[] resources;
        try {
            resources = parentContainer.members();
        } catch (CoreException e) {
            return null;
        }
        
        // Iterate over the members, getting the matching files.
        List<IFile> memberList = new ArrayList<IFile>();
        for (final IResource ithResource : resources) {
            if (ithResource instanceof IFile) {
                // second check also handles null ithResource file extension
                if (fileExtension == null || fileExtension.equals(((IFile)ithResource).getFileExtension())) {  // getFileExtension() can be null
                    memberList.add((IFile)ithResource);
                }
            }
        }
        return memberList;
    }
    
}
