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

import java.util.HashSet;
import java.util.Set;

import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IStorage;
import org.eclipse.core.runtime.IPath;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IPackageFragment;
import org.eclipse.jdt.core.IPackageFragmentRoot;
import org.eclipse.jdt.core.JavaModelException;
import org.openquark.cal.eclipse.core.util.Util;
import org.openquark.cal.machine.ProgramResourceLocator;
import org.openquark.cal.services.CALSourcePathMapper;
import org.openquark.cal.services.ProgramResourcePathMapper;

/**
 * A CAL Resource container that is inside of a jar file
 * It is read-only
 * @author aeisenberg
 *
 */
class JarCALResourceContainer implements ICALResourceContainer {
    private static final String CAL_EXTENSION = "." + CALSourcePathMapper.CAL_FILE_EXTENSION;

    
    // more efficient if we turn into a hashtable keyed by the path
    private Set<IStorage> cachedCalSources = null;
    
    /** 
     * Path mapper for getting lecc resources
     */
    private final ProgramResourcePathMapper pathMapper;
    
    private final IPackageFragmentRoot root;
    
    JarCALResourceContainer(IPackageFragmentRoot root, ProgramResourcePathMapper pathMapper) {
        this.pathMapper = pathMapper;
        this.root = root;
    }

    
    public Set<IStorage> getCALSources() {
        if (cachedCalSources == null) {
            initCache();
        }
        return cachedCalSources;
    }


    private void initCache() {
        cachedCalSources = new HashSet<IStorage>();
        try {
            for (final IJavaElement child : root.getChildren()) {
                if (child instanceof IPackageFragment) {
                    IPackageFragment frag = (IPackageFragment) child;
                    if (frag != null
                            && frag.exists()
                            && frag.getElementName().startsWith(
                                    CALSourcePathMapper.SCRIPTS_BASE_FOLDER)) {
                        internalGetSources(frag);
                    }
                }
            }
        } catch (JavaModelException e) {
            Util.log(e, "Error getting CAL sources for " + root);
        }
    }

    /**
     * Recursively traverses all children of fragment.
     * Looks for files that end in the cal extension (.cal).
     * If a file is found, it is added to the cached set.
     * @param fragment
     */
    private void internalGetSources(IPackageFragment fragment) {
        try {
            Object[] nonJavaChildren = fragment.getNonJavaResources();
            for (final Object child : nonJavaChildren) {
                if (child instanceof IStorage) {
                    if (((IStorage) child).getName().endsWith(CAL_EXTENSION)) {
                        cachedCalSources.add((IStorage) child);
                    }
                }
            }
        } catch (JavaModelException e) {
            Util.log(e, "Error getting CAL Sources");
        }
    }
    
    public IPackageFragmentRoot getPackageRoot() {
        return root;
    }

    public boolean isWritable() {
        return false;
    }

    public IPath getPath() {
        return root.getPath();
    }


    /**
     * Gets the storage (in this case, it is an IJarEntryResource) the createIfPossible 
     * argument is ignored.
     * {@inheritDoc}
     */
    public IStorage getProgramResource(ProgramResourceLocator.File filePath, boolean createIfPossible) {
        String[] folderSegments = pathMapper.getModuleResourcePath(filePath.getModuleName()).getPathElements();
        StringBuilder sb = new StringBuilder();
        for (final String folderSegment : folderSegments) {
            sb.append("." + folderSegment);
        }
        sb.replace(0, 1, "");
        IPackageFragment frag = root.getPackageFragment(sb.toString());
        
        try {
            for (final Object child : frag.getNonJavaResources()) {
                if (child instanceof IStorage) {
                    IStorage storage = (IStorage) child;
                    if (storage.getName().equals(filePath.getName())) {
                        return storage;
                    }
                }
            }
        } catch (JavaModelException e) {
            Util.log(e, "Error retrieving file from jar: " + filePath);
        }
        return null;
    }


    /**
     * ignores createIfPossible
     * {@inheritDoc}
     */
    public IPackageFragment getProgramFolder(ProgramResourceLocator.Folder folderPath, boolean createIfPossible) {
        String path = pathMapper.getModuleResourcePath(folderPath.getModuleName()).getPathString();
        path = path.replace('/', '.').substring(1);
        IPackageFragment frag = root.getPackageFragment(path);
        return frag.exists() ? frag : null;
    }    

    /**
     * return null since the container is not writable
     * {@inheritDoc}
     */
    public IFolder getOutputFolder() {
        return null;
    }
    
    public long getTimeStamp() {
        IResource resource = root.getParent().getResource();
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
            JarCALResourceContainer otherContainer = (JarCALResourceContainer)obj;
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