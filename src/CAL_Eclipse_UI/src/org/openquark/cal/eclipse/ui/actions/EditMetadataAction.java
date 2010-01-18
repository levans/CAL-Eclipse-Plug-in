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
 * EditMetadataAction.java
 * Created: 19-Feb-07
 * By: Rick Cameron
 */


package org.openquark.cal.eclipse.ui.actions;

import org.eclipse.jface.action.IAction;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IObjectActionDelegate;
import org.eclipse.ui.IViewActionDelegate;
import org.eclipse.ui.IViewPart;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.openquark.cal.compiler.ModuleName;
import org.openquark.cal.compiler.ScopedEntity;
import org.openquark.cal.eclipse.ui.metadataeditor.CALMetadataEditor;



/**
 * @author rcameron
 *
 */
public final class EditMetadataAction implements IViewActionDelegate, IObjectActionDelegate {

    private IWorkbenchPart activePart;

    /* (non-Javadoc)
     * @see org.eclipse.ui.IViewActionDelegate#init(org.eclipse.ui.IViewPart)
     */
    public void init (IViewPart view) {
        activePart = view;
    }

    /* (non-Javadoc)
     * @see org.eclipse.ui.IObjectActionDelegate#setActivePart(org.eclipse.jface.action.IAction, org.eclipse.ui.IWorkbenchPart)
     */
    public void setActivePart (IAction action, IWorkbenchPart targetPart) {
        activePart = targetPart;
    }

    /* (non-Javadoc)
     * @see org.eclipse.ui.IActionDelegate#run(org.eclipse.jface.action.IAction)
     */
    public void run (IAction action) {
        try {
            ISelection selection = activePart.getSite ().getSelectionProvider ().getSelection ();

            if (selection instanceof IStructuredSelection) {
                IStructuredSelection structuredSelection = (IStructuredSelection)selection;

                Object selectedObject = structuredSelection.getFirstElement ();

                openMetadataEditor (selectedObject);
            }
        } catch (Exception e) {
            e.printStackTrace ();
        }
    }

    /**
     * Method openMetadataEditor
     *
     * @param object
     */
    private void openMetadataEditor (Object object) {
        if (object instanceof ModuleName) {
            ModuleName moduleName = (ModuleName)object;
            
            openMetadataEditor (CALMetadataEditor.Input.getInstance (moduleName));
        } else if (object instanceof ScopedEntity) {
            ScopedEntity scopedEntity = (ScopedEntity)object;
            
            openMetadataEditor (CALMetadataEditor.Input.getInstance (scopedEntity));
        }
    }
    
    private void openMetadataEditor (IEditorInput editorInput) {
        try {
            getActivePage ().openEditor (editorInput, CALMetadataEditor.ID);
        } catch (PartInitException e) {
            MessageDialog.openError (null, "CAL Editor", "Error opening editor: " + e.getLocalizedMessage ());
        }

    }

    /* (non-Javadoc)
     * @see org.eclipse.ui.IActionDelegate#selectionChanged(org.eclipse.jface.action.IAction, org.eclipse.jface.viewers.ISelection)
     */
    public void selectionChanged (IAction action, ISelection selection) {
//        if (selection == null)
//            System.out.println ("Readme view action delegate - selection is null");
//        else
//        {
//            System.out.println ("Readme view action delegate - selection is a <" + selection.getClass() + ">");
//
//            if (selection instanceof TreeSelection) {
//                TreeSelection treeSelection = (TreeSelection) selection;
//
//                Object firstElement = treeSelection.getFirstElement();
//                
//                if (firstElement == null)
//                    System.out.println ("  No selected object");
//                else
//                    System.out.println ("  Selected object is a <" + firstElement.getClass() + ">");
//
//            }
//        }
    }

    /**
     * Method getActivePage
     * 
     * @return Returns the active {@link IWorkbenchPage}
     */
    private IWorkbenchPage getActivePage () {
        return PlatformUI.getWorkbench ().getActiveWorkbenchWindow ().getActivePage ();
    }
    
}
