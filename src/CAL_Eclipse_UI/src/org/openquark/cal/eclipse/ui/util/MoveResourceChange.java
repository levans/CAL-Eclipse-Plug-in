/*******************************************************************************
 * Copyright (c) 2007 Business Objects Software Limited and others.
 * All rights reserved. 
 * This file is made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Business Objects Software Limited - initial API and implementation based on Eclipse 3.1.2 code for
 *                             org.eclipse.jdt.internal.corext.refactoring.changes.MoveResourceChange
 *                           Eclipse source is available at: http://www.eclipse.org/downloads/
 *******************************************************************************/
/*
 * MoveResourceChange.java
 * Created: Sept 7, 2007
 * By: Greg McClement
 */
package org.openquark.cal.eclipse.ui.util;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;

import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IResource;

import org.eclipse.ltk.core.refactoring.Change;
import org.eclipse.ltk.core.refactoring.RefactoringStatus;
import org.openquark.cal.eclipse.ui.CALUIMessages;

/**
 * A change that moves a resource. There is no support for undo.
 * @author Greg McClement
 */
public class MoveResourceChange extends ResourceReorgChange {
    
    public MoveResourceChange(IResource res, IContainer dest){
        super(res, dest);
    }
    
    public RefactoringStatus isValid(IProgressMonitor pm) throws CoreException {
        // We already present a dialog to the user if he
        // moves read-only resources. Since moving a resource
        // doesn't do a validate edit (it actually doesn't
        // change the content we can't check for READ only
        // here.
        return super.isValid(pm, DIRTY);
    }
    
    /* non java-doc
     * @see ResourceReorgChange#doPerform(IPath, IProgressMonitor)
     */
    protected Change doPerformReorg(IPath path, IProgressMonitor pm) throws CoreException{
        getResource().move(path, getReorgFlags(), pm);
        return null;
    }
    
    public String getName() {
        return Messages.format(CALUIMessages.MoveResourceChange_move, 
            new String[]{getResource().getFullPath().toString(), getDestination().getName()});
    }
}

