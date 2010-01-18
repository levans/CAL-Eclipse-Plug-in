/*******************************************************************************
 * Copyright (c) 2005 Business Objects Software Limited and others.
 * All rights reserved. 
 * This file is made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Business Objects Software Limited - initial API and implementation
 *******************************************************************************/

/*
 * EclipseProgramResourcePathRepository.java
 * Creation date: Dec 13, 2005.
 * By: Edward Lam
 */
package org.openquark.cal.eclipse.core;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Set;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IStorage;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.IWorkspaceRunnable;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IPackageFragment;
import org.eclipse.jdt.internal.core.JarEntryFile;
import org.openquark.cal.compiler.ModuleName;
import org.openquark.cal.eclipse.core.builder.ICALResourceContainer;
import org.openquark.cal.eclipse.core.util.Util;
import org.openquark.cal.machine.AsynchronousFileWriter;
import org.openquark.cal.machine.ProgramResourceLocator;
import org.openquark.cal.machine.ProgramResourceRepository;
import org.openquark.cal.runtime.MachineType;


/*
 * For now, this is just the same as a BasicProgramResourcePathRepository.
 * This will have to change if there is the possibility that runtime files are included precompiled.
 * 
 * One problem with a potential change would be that Eclipse really assumes that the files in the plugin are static.
 * For performance reasons, Eclipse's classloader caches class-finding info between calls to findClass().
 * This means that any changes to the files in the nullary workspace might cause classloader 
 *   finding of files to return incorrect results.
 * Trace through org.eclipse.osgi.framework.internal.core.BundleLoader.findClass() for details.
 */


/**
 * A repository of program resources in the eclipse environment.
 * 
 * This class grabs resources out of the lecc_runtime directory
 * 
 * @author Edward Lam
 */
public class EclipseProgramResourcePathRepository implements ProgramResourceRepository {
    
    /*
     * TODOEL: we might want the program resource output to go into a user-specified folder, 
     *   just as .java files can be specified as having .class file output to a bin/ folder.
     */
    
    
    /**
     * @return a provider for a program resource finder associated with the workspace.
     */
    public static ProgramResourceRepository.Provider getResourceRepositoryProvider() {
        return new ProgramResourceRepository.Provider() {
            public ProgramResourceRepository getRepository(MachineType machineType) {
                return new EclipseProgramResourcePathRepository();
            }
        };
    }
    
    /**
     * Constructor for a EclipseProgramResourcePathRepository.
     * Private -- obtain via the repository provider. 
     */
    private EclipseProgramResourcePathRepository() {  }

    /**
     * @return the workspace.
     */
    private static IWorkspace getWorkspace() {
        return ResourcesPlugin.getWorkspace();
    }
    
    /**
     * {@inheritDoc}
     */
    public InputStream getContents(ProgramResourceLocator.File fileLocator) throws IOException {
        IStorage inputFile = getProgramResource(fileLocator);
        if (inputFile == null) {
            return null;
        }
        try {
            return inputFile.getContents();
        } catch (CoreException e) {
            throw (IOException)(new IOException()).initCause(e);
        }
    }

    /**
     * {@inheritDoc}
     * Only sets the contents if this is a file, not a Jar entry
     */
    public void setContents(ProgramResourceLocator.File fileLocator, InputStream source) throws IOException {
        
        IStorage output = getProgramResource(fileLocator);
        if (output instanceof IFile) {
            // Handle null source.
            if (source == null) {
                source = new ByteArrayInputStream(new byte[0]);
            }
            IFile outputFile = (IFile) output;
            try {
                if (outputFile.exists()) {
                    // source, force, keepHistory, progressMonitor
                    outputFile.setContents(source, true, false, null);
                } else {
                    // walk up the parent folders and ensure they all exist
                    IFolder folder = (IFolder) outputFile.getParent();
                    EclipseFileResourceHelper.ensureFolderExists(folder);

                    // source, force, monitor
                    outputFile.create(source, true, null);
                }
            
            } catch (CoreException e) {
                throw (IOException)(new IOException()).initCause(e);
            }
        } else {
            Util.log(new Exception(), "Attempt to write to an unwritable resource: " + output, IStatus.WARNING);
        }
    }

    /**
     * {@inheritDoc}
     * If the specified folder is in a jar file, then this request is ignored 
     */
    public void ensureFolderExists(ProgramResourceLocator.Folder folderLocator) throws IOException {
        IFolder folder = getIFolder(folderLocator);
        if (folder != null) {
            EclipseFileResourceHelper.ensureFolderExists(folder);
        } else {
            Util.log(new Exception(), "Attempt to create a folder in a jar file: " + folderLocator, IStatus.WARNING);
        }
    }
    
    /**
     * {@inheritDoc}
     */
    public void delete(ProgramResourceLocator resourceLocator) throws IOException {
        IResource resourceToDelete = getIResource(resourceLocator);
        
        if (resourceToDelete != null) {
            try {
                // force, progressMonitor
                resourceToDelete.delete(true, null);
            } catch (CoreException e) {
                // Couldn't delete for some reason.
                Util.log(new IOException().initCause(e), "Couldn't delete " + resourceLocator);
            }
        }
    }

    /**
     * may return null if resourceLocator points to somewhere in a jar
     * @param resourceLocator
     * @return the corresponding IResource if any
     */
    private IResource getIResource(ProgramResourceLocator resourceLocator) {
        if (resourceLocator instanceof ProgramResourceLocator.File) {
            IStorage storage = getProgramResource((ProgramResourceLocator.File) resourceLocator);
            if (storage instanceof IFile) {
                return (IFile) storage;
            } else {
                return null;
            }
        } else {
            return getIFolder((ProgramResourceLocator.Folder) resourceLocator);
        }
    }
    
    /**
     * {@inheritDoc}
     */
    public void delete(final ProgramResourceLocator[] resourceLocators) throws IOException {
        try {
            // Batch together in a workspace runnable so that only one update is sent.
            getWorkspace().run(new IWorkspaceRunnable() {

                public void run(IProgressMonitor monitor) throws CoreException {
                    for (final ProgramResourceLocator resourceLocator : resourceLocators) {
                        IResource resource = getIResource(resourceLocator);
                        if (resource != null) {
                            resource.delete(true, null);
                        }
                    }
                }
            }, null);
            
        } catch (CoreException e) {
            // The operation failed for some reason.
            throw (IOException)(new IOException()).initCause(e);
        }
    }

    /**
     * {@inheritDoc}
     */
    public boolean exists(ProgramResourceLocator resourceLocator) {
        
        if (resourceLocator instanceof ProgramResourceLocator.File) {
            ProgramResourceLocator.File fileLocator = (ProgramResourceLocator.File) resourceLocator;
            IStorage storage = getProgramResource(fileLocator);
            if (storage == null) {
                return false;
            } else {
                return storage instanceof JarEntryFile || ((IFile) storage).exists();
            }
        } else {
            ProgramResourceLocator.Folder folderLocator = (ProgramResourceLocator.Folder) resourceLocator;
            IFolder folder = getIFolder(folderLocator);
            return folder != null && folder.exists();
        }
    }

    /**
     * {@inheritDoc}
     */
    public long lastModified(ProgramResourceLocator resourceLocator) {
        IResource resource = getIResource(resourceLocator);
        if (resource != null) {
            return resource.getLocalTimeStamp();
        } else {
            // ADE warning...slow!
            ICALResourceContainer inputContainer = 
                CALModelManager.getCALModelManager().
                getInputSourceFileContainer(resourceLocator.getModuleName());
            
            return inputContainer.getTimeStamp();
        }
    }

    /**
     * {@inheritDoc}
     */
    public ProgramResourceLocator[] getMembers(ProgramResourceLocator.Folder folder) {
        
        IResource[] resources;
        try {
            // TODOEL: There are three functions named members().  Is this the right one?
            resources = getIFolder(folder).members();
        
        } catch (CoreException e) {
            // Doesn't exist, or project isn't open.
            return null;
        }
        
        // The locators to return.
        ProgramResourceLocator[] folderFileLocators = new ProgramResourceLocator[resources.length];
        
        // Iterate over the resource members.
        for (int i = 0; i < resources.length; i++) {
            IResource ithResource = resources[i];
            String nthFileName = ithResource.getName();
            
            int resourceType = ithResource.getType();
            if (resourceType == IResource.FOLDER) {
                folderFileLocators[i] = folder.extendFolder(nthFileName);
                
            } else if (resourceType == IResource.FILE) {
                folderFileLocators[i] = folder.extendFile(nthFileName);
                
            } else {
                throw new IllegalStateException();
            }
        }
        
        return folderFileLocators;
    }

    /**
     * {@inheritDoc}
     */
    public File getFile(ProgramResourceLocator resourceLocator) {
        IResource resource = getIResource(resourceLocator);
        if (resource != null) {
            return new File(resource.getLocation().toOSString());
        } else {
            return null;
        }
    }
    

    /**
     * This will return null for resourceLocators in car-jars
     * @param resourceLocator the locator for a folder resource.
     * @return the folder resource indicated by the locator.
     */
    private IFolder getIFolder(ProgramResourceLocator.Folder resourceLocator) {
        ICALResourceContainer inputFolder = CALModelManager.getCALModelManager().getInputSourceFileContainer(resourceLocator.getModuleName());
        if (inputFolder != null) {
            IPackageFragment frag = inputFolder.getProgramFolder(resourceLocator, false);
            IResource resource = frag.getResource();
            if (resource instanceof IFolder) {
                return (IFolder)resource;
            }
        }
        return null;
    }
    
    /**
     * @param resourceLocator the locator for a resource.
     * @return the resource indicated by the locator.
     * Null if the resource does not exist.
     */
    private IStorage getProgramResource(ProgramResourceLocator.File resourceLocator) {
        // Get the path to the module base folder, relative to the workspace root.
        ICALResourceContainer inputFolder = 
            CALModelManager.getCALModelManager().
            getInputSourceFileContainer(resourceLocator.getModuleName());
        
        if (inputFolder == null) {
            // TODOEL should we default to some folder?  
            // We probably should not be asking for a resource from a module for which we don't have source.
            return null;
        }

        return inputFolder.getProgramResource(resourceLocator, false);
    }
    
    /**
     * {@inheritDoc}
     */
    public boolean isEmpty() {

        // Iterate over the projects in the workspace.
        IProject[] projects = getWorkspace().getRoot().getProjects();
        for (final IProject project : projects) {
            // only look at CAL projects
            IJavaProject javaProject = Util.getCalProject(project);
            if (javaProject != null) {
                
                // Iterate over the input folders in the project.
                ICALResourceContainer[] inputFolders = CALModelManager.getCALModelManager().getInputFolders(javaProject);
                for (final ICALResourceContainer inputFolder : inputFolders) {
                    if (!inputFolder.getCALSources().isEmpty()) {
                        return false;
                    }
                }
            }
        }
            
        
        // No projects have an input folder parent with a non-empty base resource folder.
        return true;
    }

    /**
     * {@inheritDoc}
     */
    public long getSize(ProgramResourceLocator.File fileLocator) {
        /*
         * IFile doesn't provide a direct way to find the length of a file.
         * So first convert to a File, and get the length of that.
         * This only works if there is a corresponding java.io.File, so it doesn't work when a file is derived or not real in some other sense.
         * 
         * IFile.length() returns 0L when there is no corresponding java.io.File -- this is ok.
         */
        File file = getFile(fileLocator);
        if (file != null) {
            return file.length();
        } else {
            return 0;
        }
    }
    
    /**
     * {@inheritDoc}
     */
    public ModuleName[] getModules() {
        Set<ModuleName> moduleNames = CALModelManager.getCALModelManager().getModuleNames();
        return moduleNames.toArray(new ModuleName[moduleNames.size()]);
        
    }

    /**
     * {@inheritDoc}
     */
    public String getLocationString() {
        // Return the path to the root of the workspace.  This is probably the best we can do..
        return getWorkspace().getRoot().getFullPath().toOSString();
    }

    /**
     * {@inheritDoc}
     */
    public String getDebugInfo(org.openquark.cal.machine.ProgramResourceLocator.File fileLocator) {
        IStorage inputFile = getProgramResource(fileLocator);
        if (inputFile == null) {
            return null;
        } else {
            return "from Eclipse file: " + inputFile.getName();
        }
    }

    /**
     * {@inheritDoc}
     */
    public AsynchronousFileWriter getAsynchronousFileWriter() {
        return new EclipseAsynchronousFileWriter(this);
    }
}
