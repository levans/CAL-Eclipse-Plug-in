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
 * OpenInConsoleAction.java
 * Created: Aug 22, 2007
 * By: Edward Lam
 */

package org.openquark.cal.eclipse.ui.console;

import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.IObjectActionDelegate;
import org.eclipse.ui.IViewActionDelegate;
import org.eclipse.ui.IViewPart;
import org.eclipse.ui.IWorkbenchPart;
import org.openquark.cal.compiler.ModuleName;
import org.openquark.cal.eclipse.core.CALModelManager;
import org.openquark.cal.eclipse.core.util.Util;


/**
 * The action to open the CAL Console and change to a module which is selected in the workspace or outline tree.
 * @author Edward Lam
 */
public class OpenInConsoleAction implements IViewActionDelegate, IObjectActionDelegate {

    private IWorkbenchPart activePart;

    /**
     * {@inheritDoc}
     */
    public void init(IViewPart view) {
        activePart = view;
    }

    /**
     * {@inheritDoc}
     */
    public void setActivePart(IAction action, IWorkbenchPart targetPart) {
        activePart = targetPart;
    }

    /**
     * {@inheritDoc}
     */
    public void run(IAction action) {
        try {
            ISelection selection = activePart.getSite().getSelectionProvider().getSelection();
            ModuleName moduleName = getSelectedModuleName(selection);
            if (moduleName != null) {
                openConsoleOnModule(moduleName);
            }
        } catch (Exception e) {
            Util.log(e, "Exception opening console on module.");
        }
    }
    
    /**
     * @param selection a selection to analyze
     * @return the name of the module representing the first element in the selection, or null if none.
     */
    private ModuleName getSelectedModuleName(ISelection selection) {
        if (selection instanceof IStructuredSelection) {
            IStructuredSelection structuredSelection = (IStructuredSelection)selection;

            Object selectedObject = structuredSelection.getFirstElement();

            if (selectedObject instanceof ModuleName) {
                return (ModuleName)selectedObject;
            }
        }
        return null;
    }

    /**
     * Open a CAL Console on the given module.
     * @param moduleName the name of the module to open in the console
     */
    private void openConsoleOnModule(ModuleName moduleName) {

        CALConsole existingCALConsole = CALConsole.showCALConsole();
        existingCALConsole.handleRequest_setModule(moduleName.toSourceText());
    }

    /**
     * {@inheritDoc}
     */
    public void selectionChanged(IAction action, ISelection selection) {
        // Can only change to a module which exists in the program.
        ModuleName moduleName = getSelectedModuleName(selection);
        boolean shouldEnable = moduleName != null && CALModelManager.getCALModelManager().getModuleTypeInfo(moduleName) != null;
        action.setEnabled(shouldEnable);
    }

}
