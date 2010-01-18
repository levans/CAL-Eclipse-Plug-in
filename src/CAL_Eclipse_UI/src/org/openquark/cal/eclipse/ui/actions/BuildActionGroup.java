/*******************************************************************************
 * Copyright (c) 2005 Business Objects Software Limited and others.
 * All rights reserved. 
 * This file is made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Business Objects Software Limited - initial API and implementation based on Eclipse 3.1.2 code for
 *                             /org.eclipse.jdt.ui/ui/org/eclipse/jdt/internal/ui/actions/BlockCommentAction.java
 *                           Eclipse source is available at: http://www.eclipse.org/downloads/
 *******************************************************************************/

/*
 * BuildActionGroup.java
 * Creation date: Nov 1, 2005.
 * By: Edward Lam
 */
package org.openquark.cal.eclipse.ui.actions;

import org.eclipse.core.resources.IncrementalProjectBuilder;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IActionBars;
import org.eclipse.ui.IViewPart;
import org.eclipse.ui.IWorkbenchSite;
import org.eclipse.ui.actions.ActionGroup;
import org.eclipse.ui.actions.BuildAction;
import org.eclipse.ui.ide.IDEActionFactory;
import org.openquark.cal.eclipse.ui.IContextMenuConstants;

/**
 * Contributes all build related actions to the context menu and installs handlers for the 
 * corresponding global menu actions.
 * 
 * <p>
 * This class may be instantiated; it is not intended to be subclassed.
 * </p>
 * 
 * @author Edward Lam
 */
public class BuildActionGroup extends ActionGroup {

    private final IWorkbenchSite fSite;
    private final BuildAction fBuildAction;
//    private RefreshAction fRefreshAction;

    /**
     * Creates a new <code>BuildActionGroup</code>. The group requires that
     * the selection provided by the view part's selection provider is of type
     * <code>org.eclipse.jface.viewers.IStructuredSelection</code>.
     * 
     * @param part the view part that owns this action group
     */
    public BuildActionGroup(IViewPart part) {
        fSite = part.getSite();
        Shell shell = fSite.getShell();
        ISelectionProvider provider = fSite.getSelectionProvider();
        fBuildAction = new BuildAction(shell, IncrementalProjectBuilder.INCREMENTAL_BUILD);
        fBuildAction.setText(ActionMessages.BuildAction_label); 
        fBuildAction.setActionDefinitionId("org.eclipse.ui.project.buildProject"); //$NON-NLS-1$
//        fRefreshAction = new RefreshAction(fSite);
//        fRefreshAction.setActionDefinitionId("org.eclipse.ui.file.refresh"); //$NON-NLS-1$
        provider.addSelectionChangedListener(fBuildAction);
//        provider.addSelectionChangedListener(fRefreshAction);
    }

//    /**
//     * Returns the refresh action managed by this group.
//     * 
//     * @return the refresh action. If this group doesn't manage a refresh action
//     *      <code>null</code> is returned
//     */
//    public IAction getRefreshAction() {
//        return fRefreshAction;
//    }

    /* (non-Javadoc)
     * Method declared in ActionGroup
     */
    @Override
    public void fillActionBars(IActionBars actionBar) {
        super.fillActionBars(actionBar);
        setGlobalActionHandlers(actionBar);
    }

    /* (non-Javadoc)
     * Method declared in ActionGroup
     */
    @Override
    public void fillContextMenu(IMenuManager menu) {
        ISelection selection = getContext().getSelection();
        if (!ResourcesPlugin.getWorkspace().isAutoBuilding() && isBuildTarget(selection)) {
            appendToGroup(menu, fBuildAction);
        }
//        appendToGroup(menu, fRefreshAction);
        super.fillContextMenu(menu);
    }

    /* (non-Javadoc)
     * Method declared in ActionGroup
     */
    @Override
    public void dispose() {
        ISelectionProvider provider = fSite.getSelectionProvider();
        provider.removeSelectionChangedListener(fBuildAction);
//        provider.removeSelectionChangedListener(fRefreshAction);
        super.dispose();
    }

    private void setGlobalActionHandlers(IActionBars actionBar) {
        actionBar.setGlobalActionHandler(IDEActionFactory.BUILD_PROJECT.getId(), fBuildAction);
//        actionBar.setGlobalActionHandler(ActionFactory.REFRESH.getId(), fRefreshAction);
    }

    /**
     * Append the action to the menu if the action is enabled.
     * @param menu the menu to which to add the action
     * @param action the action to add.
     */
    private void appendToGroup(IMenuManager menu, IAction action) {
        if (action.isEnabled()) {
            menu.appendToGroup(IContextMenuConstants.GROUP_BUILD, action);
        }
    }

    /**
     * @param s the selection within the viewer
     * @return whether we can build whatever is selected.
     */
    private boolean isBuildTarget(ISelection s) {
        if (!(s instanceof IStructuredSelection)) {
            return false;
        }
        IStructuredSelection selection = (IStructuredSelection) s;
        if (selection.size() != 1) {
            return false;
        }
        
        // TODOEL
//        return selection.getFirstElement() instanceof ICALProject;
        return true;
    }
}
