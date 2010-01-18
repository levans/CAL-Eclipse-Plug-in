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
 * CleanAction.java
 * Creation date: Jul 24, 2007.
 * By: Edward Lam
 */
package org.openquark.cal.eclipse.ui.actions;

import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.IWorkbenchWindowActionDelegate;
import org.openquark.cal.eclipse.ui.util.CoreUtility;


/**
 * The action to run the Job to clean all CAL build artifacts.
 * @author Edward Lam
 */
public class CleanAllCALAction implements IWorkbenchWindowActionDelegate {

    /**
     * {@inheritDoc}
     */
    public void init(IWorkbenchWindow window) {
    }

    /**
     * {@inheritDoc}
     */
    public void dispose() {
    }

    /**
     * {@inheritDoc}
     */
    public void selectionChanged(IAction action, ISelection selection) {
    }

    /**
     * {@inheritDoc}
     */
    public void run(IAction action) {
        CoreUtility.getCleanJob(null, true).schedule();
    }

}
