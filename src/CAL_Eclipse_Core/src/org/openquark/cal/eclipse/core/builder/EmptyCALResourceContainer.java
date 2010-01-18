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

import java.util.Collections;
import java.util.Set;

import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IStorage;
import org.eclipse.core.runtime.IPath;
import org.eclipse.jdt.core.IPackageFragment;
import org.eclipse.jdt.core.IPackageFragmentRoot;
import org.openquark.cal.machine.ProgramResourceLocator;

/**
 * This represents an empty container.  It should only be used if there is an error
 * 
 * @author aeisenberg
 */
class EmptyCALResourceContainer implements ICALResourceContainer {
    
    private final IPackageFragmentRoot root;
    
    EmptyCALResourceContainer(IPackageFragmentRoot root) {
        this.root = root;
    }
    
    public Set<IStorage> getCALSources() {
        return Collections.emptySet();
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

    public IStorage getProgramResource(ProgramResourceLocator.File resourcePath,
            boolean createIfPossible) {
        return null;
    }

    public IPackageFragment getProgramFolder(ProgramResourceLocator.Folder folderPath,
            boolean createIfPossible) {
        return null;
    }

    /**
     * return null since the container is not writable
     */
    public IFolder getOutputFolder() {
        return null;
    }    
    
    public long getTimeStamp() {
        return IResource.NULL_STAMP;
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
            EmptyCALResourceContainer otherContainer = (EmptyCALResourceContainer)obj;
            return root.equals(otherContainer.root);
        }
        return false;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int hashCode() {
        int result = 17;
        result = 37 * root.hashCode() + result;
        return result;
    }


}
