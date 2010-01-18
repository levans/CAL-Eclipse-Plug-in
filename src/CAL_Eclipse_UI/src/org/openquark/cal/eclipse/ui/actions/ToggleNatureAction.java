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
 * ToggleNatureAction.java
 * Creation date: Nov 1, 2005.
 * By: Edward Lam
 */
package org.openquark.cal.eclipse.ui.actions;

import java.util.Iterator;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.IObjectActionDelegate;
import org.eclipse.ui.IWorkbenchPart;
import org.openquark.cal.eclipse.ui.util.CoreUtility;
import org.openquark.util.UnsafeCast;


/**
 * The action to toggle the cal nature on a selection.
 * @author Edward Lam
 */
public class ToggleNatureAction implements IObjectActionDelegate {

    /** The currently selected element. */
    private ISelection selection;

    /**
     * {@inheritDoc}
     */
    public void run(IAction action) {
        if (selection instanceof IStructuredSelection) {
            for (Iterator<Object> it = UnsafeCast.unsafeCast(((IStructuredSelection) selection).iterator()); it.hasNext();) {
                Object element = it.next();
                IProject project = null;
                if (element instanceof IProject) {
                    project = (IProject) element;
                } else if (element instanceof IAdaptable) {
                    project = (IProject) ((IAdaptable) element).getAdapter(IProject.class);
                }
                if (project != null) {
                    CoreUtility.toggleNature(project);
                }
            }
        }
    }

    /**
     * {@inheritDoc}
     */
    public void selectionChanged(IAction action, ISelection selection) {
        this.selection = selection;
    }

    /**
     * {@inheritDoc}
     */
    public void setActivePart(IAction action, IWorkbenchPart targetPart) {
    }
}
