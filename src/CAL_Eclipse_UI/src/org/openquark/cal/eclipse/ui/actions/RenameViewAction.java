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
 * RenameViewAction.java
 * Created: May 9 2007
 * By: Greg McClement
 */

package org.openquark.cal.eclipse.ui.actions;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IStorage;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.IObjectActionDelegate;
import org.eclipse.ui.IViewActionDelegate;
import org.eclipse.ui.IViewPart;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.IWorkbenchPartSite;
import org.openquark.cal.compiler.CompilerMessageLogger;
import org.openquark.cal.compiler.DataConstructor;
import org.openquark.cal.compiler.Function;
import org.openquark.cal.compiler.MessageLogger;
import org.openquark.cal.compiler.ModuleName;
import org.openquark.cal.compiler.Name;
import org.openquark.cal.compiler.QualifiedName;
import org.openquark.cal.compiler.ScopedEntity;
import org.openquark.cal.compiler.SourceIdentifier;
import org.openquark.cal.compiler.TypeClass;
import org.openquark.cal.compiler.TypeConstructor;
import org.openquark.cal.eclipse.core.CALModelManager;
import org.openquark.cal.eclipse.ui.CALEclipseUIPlugin;
import org.openquark.cal.eclipse.ui.util.CoreUtility;

/**
 * The Rename action for the Outline and Workspace view.
 * 
 * @author Greg McClement
 */
public final class RenameViewAction implements IViewActionDelegate, IObjectActionDelegate {

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
            final IWorkbenchPartSite site = activePart.getSite();
            final ISelection selection = site.getSelectionProvider().getSelection();            

            if (selection instanceof IStructuredSelection) {
                IStructuredSelection structuredSelection = (IStructuredSelection)selection;

                Object selectedObject = structuredSelection.getFirstElement();
                Name oldName = null;
                String initValue = null;
                SourceIdentifier.Category category;
                if (selectedObject instanceof ScopedEntity){
                    ScopedEntity scopedEntity = (ScopedEntity) selectedObject;
                    oldName = scopedEntity.getName();                    
                    initValue = scopedEntity.getName().getUnqualifiedName();
                }
                
                CALModelManager cmm = CALModelManager.getCALModelManager();
				if (selectedObject instanceof Function){
                    category = SourceIdentifier.Category.TOP_LEVEL_FUNCTION_OR_CLASS_METHOD;
                }
                else if (selectedObject instanceof TypeClass){
                    category = SourceIdentifier.Category.TYPE_CLASS;
                }
                else if (selectedObject instanceof TypeConstructor){
                    category = SourceIdentifier.Category.TYPE_CONSTRUCTOR;
                }
                else if (selectedObject instanceof DataConstructor){
                    category = SourceIdentifier.Category.DATA_CONSTRUCTOR;
                }
                else if (selectedObject instanceof ModuleName){
                    ModuleName moduleName = (ModuleName) selectedObject;
                    oldName = moduleName;
                    category = SourceIdentifier.Category.MODULE_NAME;
                    initValue = moduleName.toSourceText();
                }
                else if (selectedObject instanceof IFile){
                	ModuleName moduleName = cmm.getModuleName((IFile) selectedObject);
                    oldName = moduleName;
                    category = SourceIdentifier.Category.MODULE_NAME;
                    initValue = moduleName.toSourceText();
                }
                else{
                    oldName = null;
                    category = null;
                    assert false;
                    return;
                }
                CompilerMessageLogger messageLogger = new MessageLogger();
                RenameAction.performRename(site.getShell(), cmm, messageLogger, oldName, initValue, category);
                CoreUtility.showErrors(ActionMessages.RenameAction_windowTitle, ActionMessages.RenameAction_failed, messageLogger);
            }
        } catch (Exception e) {
            CALEclipseUIPlugin.log(new Status(IStatus.ERROR, CALEclipseUIPlugin.PLUGIN_ID, IStatus.OK, "", e)); //$NON-NLS-1$
        }
    }

    /* (non-Javadoc)
     * @see org.eclipse.ui.IActionDelegate#selectionChanged(org.eclipse.jface.action.IAction, org.eclipse.jface.viewers.ISelection)
     */
    public void selectionChanged (IAction action, ISelection selection) {
    }
}
