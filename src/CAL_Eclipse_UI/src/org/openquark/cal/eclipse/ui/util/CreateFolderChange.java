/*******************************************************************************
 * Copyright (c) 2007 Business Objects SA and others.
 * All rights reserved. 
 * This file is made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Business Objects Software Limited
 *******************************************************************************/
/*
 * CreateFolderChange.java
 * Created: Sept 7, 2007
 * By: Greg McClement
 */
package org.openquark.cal.eclipse.ui.util;

import org.eclipse.core.resources.IFolder;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.ltk.core.refactoring.Change;
import org.eclipse.ltk.core.refactoring.RefactoringStatus;

/**
 * A change that creates a new folder. No undo support.
 * @author Greg McClement
 */
public class CreateFolderChange extends CALChange {

    private final IFolder folder;

    public CreateFolderChange(IFolder folder){
        this.folder = folder;        
    }
    
    @Override
    public Object getModifiedElement() {
        return null;
    }

    @Override
    public String getName() {
        return null;
    }

    @Override
    public RefactoringStatus isValid(IProgressMonitor pm) throws CoreException,
            OperationCanceledException {
        return null;
    }

    @Override
    public Change perform(IProgressMonitor pm) throws CoreException {
        folder.create(false, true, null);
        return null;
    }

}
