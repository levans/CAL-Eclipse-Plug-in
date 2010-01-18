/*******************************************************************************
 * Copyright (c) 2007 Business Objects Software Limited and others.
 * All rights reserved. 
 * This file is made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Business Objects Software Limited - initial API and implementation
 *******************************************************************************/


/*
 * FolderCALResourceContainer.java
 * Created: Jul 25, 2007
 * By: Andrew Eisenberg
 */
package org.openquark.cal.eclipse.core.builder;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashSet;
import java.util.Set;

import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IStorage;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IPackageFragment;
import org.eclipse.jdt.core.IPackageFragmentRoot;
import org.eclipse.jdt.core.JavaModelException;
import org.openquark.cal.eclipse.core.util.Util;
import org.openquark.cal.machine.ProgramResourceLocator;
import org.openquark.cal.services.CALSourcePathMapper;
import org.openquark.cal.services.ProgramResourcePathMapper;

/**
 * A CAL Resource Container that is a source folder.
 * It is writable
 * @author aeisenberg
 */
class FolderCALResourceContainer implements ICALResourceContainer {

    /** 
     * Path mapper for getting lecc resources
     */
    private final ProgramResourcePathMapper pathMapper;
    

    private static final String CAL_EXTENSION = "." + CALSourcePathMapper.CAL_FILE_EXTENSION;
    private final IPackageFragmentRoot root;
    
    FolderCALResourceContainer(IPackageFragmentRoot root, ProgramResourcePathMapper pathMapper) {
        this.pathMapper = pathMapper;
        this.root = root;
    }
    
    /**
     * {@inheritDoc}
     */
    public Set<IStorage> getCALSources() {
        Set<IStorage> calSources = new HashSet<IStorage>();
        try {
            for (final IJavaElement child : root.getChildren()) {
                if (child instanceof IPackageFragment) {
                    IPackageFragment frag = (IPackageFragment) child;
                    if (frag != null 
                            && 
                            (
                                frag.getElementName().startsWith(CALSourcePathMapper.SCRIPTS_BASE_FOLDER + ".") ||
                                frag.getElementName().equals(CALSourcePathMapper.SCRIPTS_BASE_FOLDER)
                            )
                            && frag.exists()) {         // slow
                        calSources.addAll(internalGetSources(frag));
                    }
                }
            }
        } catch (JavaModelException e) {
            Util.log(e, "Error getting CAL sources for " + root);
        }

        return calSources;
    }

    /**
     * Recursively traverses all children of fragment.
     * Looks for files that end in the cal extension (.cal).
     * If a file is found, it is added to a set and returned.
     * @param fragment
     * @return the set of cal files below this package fragment
     */
    private Set<IStorage> internalGetSources(IPackageFragment fragment) {
        Set<IStorage> calSources = new HashSet<IStorage>();
        
        try {
            Object[] nonJavaChildren = fragment.getNonJavaResources();
            for (final Object child : nonJavaChildren) {
                if (child instanceof IStorage &&
                        ((IStorage) child).getName().endsWith(CAL_EXTENSION)) {
                    calSources.add((IStorage) child);
                }
            }
            
            IJavaElement[] children = fragment.getChildren();
            for (final IJavaElement child : children) {
                if (child.getElementType() == IJavaElement.PACKAGE_FRAGMENT) {
                    calSources.addAll(internalGetSources((IPackageFragment) child));
                }
            }
        } catch (JavaModelException e) {
            Util.log(e, "Error getting CAL Sources");
        }
        
        return calSources;
    }

    /**
     * {@inheritDoc}
     */
    public IPackageFragmentRoot getPackageRoot() {
        return root;
    }

    /**
     * {@inheritDoc}
     */
    public boolean isWritable() {
        return true;
    }

    /**
     * {@inheritDoc}
     */
    public IPath getPath() {
        return root.getPath().append(CALSourcePathMapper.SCRIPTS_BASE_FOLDER);
    }

    /**
     * {@inheritDoc}
     */
    public IStorage getProgramResource(ProgramResourceLocator.File resourcePath, boolean createIfPossible) {
        IPath path = new Path(
                pathMapper.getModuleResourcePath(resourcePath.getModuleName())
                .getPathString()).append(resourcePath.getName());
        IFile resourceFile = ((IContainer)root.getResource()).getFile(path);
        if (createIfPossible && !resourceFile.exists()) {
            try {
                resourceFile.create(new InputStream() {
                    @Override
                    public int read() throws IOException {
                        return -1;
                    }
                }, true, null);
            } catch (CoreException e) {
                Util.log(e, "Error creating file " + resourcePath);
            }
        }
        return resourceFile;
    }
    
    /**
     * {@inheritDoc}
     */
    public IPackageFragment getProgramFolder(ProgramResourceLocator.Folder folderPath, boolean createIfPossible) {
        
        String path = pathMapper.getModuleResourcePath(folderPath.getModuleName()).getPathString();
        path = path.replace('/', '.').substring(1);
        IPackageFragment frag = root.getPackageFragment(path);
        if (createIfPossible && !frag.exists()) {
            try {
                frag = root.createPackageFragment(path, true, null);
            } catch (CoreException e) {
                Util.log(e, "Error creating fragment " + folderPath);
            }
        }
        return frag;
    }    

    /**
     * {@inheritDoc}
     */
    public IFolder getOutputFolder() {
        IPath outputPath = 
            root.getPath().append(pathMapper.getBaseResourceFolder().getPathString());
        return root.getJavaModel().getWorkspace().getRoot().getFolder(outputPath);
    }    

    /**
     * {@inheritDoc}
     */
    public long getTimeStamp() {
        IResource resource = root.getResource();
        if (resource != null && resource.exists()) {
            return resource.getLocalTimeStamp();
        } else {
            return IResource.NULL_STAMP;
        }
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (obj.getClass() == getClass()) {
            FolderCALResourceContainer otherContainer = (FolderCALResourceContainer)obj;
            return pathMapper == otherContainer.pathMapper && root.equals(otherContainer.root);
        }
        return false;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int hashCode() {
        int result = 17;
        result = 37 * pathMapper.hashCode() + result;
        result = 37 * root.hashCode() + result;
        return result;
    }

}