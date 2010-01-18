/*******************************************************************************
 * Copyright (c) 2007 Business Objects Software Limited and others.
 * All rights reserved. 
 * This file is made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Business Objects Software Limited - initial API and implementation based on Eclipse 3.1.2 code for
 *                             org.eclipse.jdt.internal.corext.refactoring.changes.ResourceReorgChange
 *                           Eclipse source is available at: http://www.eclipse.org/downloads/
 *******************************************************************************/
/*
 * ResourceReorgChange.java
 * Created: Sept 7, 2007
 * By: Greg McClement
 */
package org.openquark.cal.eclipse.ui.util;

import java.net.URI;

import org.eclipse.core.runtime.Assert;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.core.runtime.SubProgressMonitor;

import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;

import org.eclipse.ltk.core.refactoring.Change;
import org.eclipse.ltk.core.refactoring.participants.ReorgExecutionLog;

/**
 * Class for abstracting some functionality out of reorg type changes. 
 * 
 * @author Greg McClement
 */
abstract class ResourceReorgChange extends CALChange {
    
    private final IResource fSource;
    private final IContainer fTarget;

    ResourceReorgChange(IResource res, IContainer dest){
        Assert.isTrue(res instanceof IFile || res instanceof IFolder);  
        Assert.isTrue(dest instanceof IProject || dest instanceof IFolder);
        
        fSource= res;
        fTarget= dest;
    }
    
    protected abstract Change doPerformReorg(IPath path, IProgressMonitor pm) throws CoreException;
    
    /* non java-doc
     * @see IChange#perform(ChangeContext, IProgressMonitor)
     */
    public final Change perform(IProgressMonitor pm) throws CoreException, OperationCanceledException {
        try{
            pm.beginTask(getName(), 2);
            
            String newName= getNewResourceName();
            IResource resource= getResource();
            boolean performReorg= deleteIfAlreadyExists(new SubProgressMonitor(pm, 1), newName);
            if (!performReorg)
                return null;
            final Change result= doPerformReorg(getDestinationPath(newName), new SubProgressMonitor(pm, 1));
            markAsExecuted(resource);
            return result;
        } finally {
            pm.done();
        }
    }

    protected IPath getDestinationPath(String newName) {
        return getDestination().getFullPath().append(newName);
    }

    /**
     * returns false if source and destination are the same (in workspace or on disk)
     * in such case, no action should be performed
     */
    private boolean deleteIfAlreadyExists(IProgressMonitor pm, String newName) throws CoreException {
        pm.beginTask("", 1); //$NON-NLS-1$
        IResource current= getDestination().findMember(newName);
        if (current == null)
            return true;
        if (! current.exists())
            return true;

        IResource resource= getResource();
        Assert.isNotNull(resource);
            
        if (areEqualInWorkspaceOrOnDisk(resource, current))
            return false;
        
        if (current instanceof IFile)
            ((IFile)current).delete(false, true, new SubProgressMonitor(pm, 1));
        else if (current instanceof IFolder)
            ((IFolder)current).delete(false, true, new SubProgressMonitor(pm, 1));
        else 
            Assert.isTrue(false);
            
        return true;    
    }
    
    public static boolean areEqualInWorkspaceOrOnDisk(IResource r1, IResource r2){
        if (r1 == null || r2 == null)
            return false;
        if (r1.equals(r2))
            return true;
        URI r1Location= r1.getLocationURI();
        URI r2Location= r2.getLocationURI();
        if (r1Location == null || r2Location == null)
            return false;
        return r1Location.equals(r2Location);
    }
    

    private String getNewResourceName() throws OperationCanceledException {
        return getResource().getName();
    }
    
    /* non java-doc
     * @see IChange#getModifiedLanguageElement()
     */
    public Object getModifiedElement() {
        return getResource();
    }
    
    protected IResource getResource(){
        return fSource;
    }
    
    IContainer getDestination(){
        return fTarget; 
    }

    protected int getReorgFlags() {
        return IResource.KEEP_HISTORY | IResource.SHALLOW;
    }
    
    private void markAsExecuted(IResource resource) {
        ReorgExecutionLog log= (ReorgExecutionLog)getAdapter(ReorgExecutionLog.class);
        if (log != null) {
            log.markAsProcessed(resource);
        }
    }
}

