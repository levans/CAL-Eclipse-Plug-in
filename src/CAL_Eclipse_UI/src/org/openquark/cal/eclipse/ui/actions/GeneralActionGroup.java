/*******************************************************************************
 * Copyright (c) 2007 Business Objects Software Limited and others.
 * All rights reserved. 
 * This file is made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Business Objects Software Limited - initial API and implementation based on Eclipse 3.1.2 code for
 *                             /org.eclipse.jdt.ui/ui/org/eclipse/jdt/ui/actions/GenerateActionGroup.java
 *                           Eclipse source is available at: http://www.eclipse.org/downloads/
 *******************************************************************************/

/*
 * GeneralActionGroup.java
 * Creation date: July 11 2007.
 * By: Greg McClement
 */

package org.openquark.cal.eclipse.ui.actions;

import java.util.List;

import org.eclipse.jface.action.IAction;
import org.eclipse.jface.action.IContributionManager;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.core.runtime.Assert;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.ui.IActionBars;
import org.eclipse.ui.IKeyBindingService;
import org.eclipse.ui.IWorkbenchSite;
import org.eclipse.ui.actions.ActionGroup;
import org.eclipse.ui.actions.AddBookmarkAction;
import org.eclipse.ui.ide.IDEActionFactory;
import org.eclipse.ui.texteditor.IUpdate;
import org.openquark.cal.eclipse.ui.IContextMenuConstants;
import org.openquark.cal.eclipse.ui.caleditor.CALEditor;

/**
 * Used to implement the action groups. This is easily done by overriding a 
 * couple of members. Search for 
 * uses of the class to see examples.
 * 
 * @author Greg McClement
 */
public abstract class GeneralActionGroup extends ActionGroup {

    protected CALEditor fEditor;
    protected IWorkbenchSite fSite;
    protected String fGroupName = IContextMenuConstants.GROUP_REORGANIZE;
    protected final String MENU_ID;
    protected final String menuText;
    protected List<ISelectionChangedListener> fRegisteredSelectionListeners;
    protected AddBookmarkAction fAddBookmark;
    protected RefactorQuickAccessAction fQuickAccessAction;
    protected IKeyBindingService fKeyBindingService;
    private final String quickMenuId;    


    public GeneralActionGroup(CALEditor editor, String groupName, String menuId, String quickMenuId, String menuText) {
        fSite = editor.getSite();
        fEditor = editor;
        fGroupName = groupName;
        MENU_ID = menuId;
        this.menuText = menuText;
        this.quickMenuId = quickMenuId;

        if (quickMenuId != null){
            fQuickAccessAction = new RefactorQuickAccessAction(editor);
            fKeyBindingService = editor.getEditorSite().getKeyBindingService();
            fKeyBindingService.registerAction(fQuickAccessAction);
        }
    }

    /* (non-Javadoc)
     * Method declared in ActionGroup
     */
    @Override
    public void fillContextMenu(IMenuManager menu) {
        super.fillContextMenu(menu);
        fillContextMenuExtra(menu);
        String menuTextToShow = menuText;
        if (fQuickAccessAction != null) {
            menuTextToShow = fQuickAccessAction.addShortcut(menuTextToShow); 
        }

        {
            IMenuManager subMenu = new MenuManager(menuTextToShow, MENU_ID);
            int added = 0;
            if (isEditorOwner()) {
                added = fillSubMenu(subMenu);
            } else {
                added = fillViewSubMenu(subMenu);
            }
            if (added > 0) {
                menu.appendToGroup(fGroupName, subMenu);
            }
        }
    }

    /*
     * TODOEL - HACK: (see plugin.xml for this plugin).
     * Registering the quick menu action with the same keys as the Java quick menu causes a conflict, 
     * resulting in both quick menus being disabled.
     * For now, to get around this we just assume the java quick menu id.
     */
    private class RefactorQuickAccessAction extends CALEclipseQuickMenuAction {
        public RefactorQuickAccessAction(CALEditor editor) {
            super(editor, quickMenuId); 
        }
        @Override
        protected void fillMenu(IMenuManager menu) {
            fillQuickMenu(menu);
        }
    }

    private boolean isEditorOwner() {
        return fEditor != null;
    }    

    protected void addEditorAction(IContributionManager menu, String groupName, String actionID) {
        if (fEditor == null) {
            return;
        }
        IAction action = fEditor.getAction(actionID);
        if (action == null) {
            return;
        }
        if (action instanceof IUpdate) {
            ((IUpdate)action).update();
        }
        if (action.isEnabled()) {
            menu.insertBefore(groupName, action);
            return;
        }
        return;
    }

    protected int addEditorAction(IContributionManager menu, String actionID) {
        if (fEditor == null) {
            return 0;
        }
        IAction action = fEditor.getAction(actionID);
        if (action == null) {
            return 0;
        }
        if (action instanceof IUpdate) {
            ((IUpdate)action).update();
        }
        if (action.isEnabled()) {
            menu.add(action);
            return 1;
        }
        return 0;
    }
    

    private void setGlobalActionHandlers(IActionBars actionBar) {
        if (!isEditorOwner()) {
            // editor provides its own implementation of these actions.
            actionBar.setGlobalActionHandler(IDEActionFactory.BOOKMARK.getId(), fAddBookmark);
        }
    }
    /* (non-Javadoc)
     * Method declared in ActionGroup
     */
    @Override
    public void dispose() {
        if (fRegisteredSelectionListeners != null) {
            ISelectionProvider provider = fSite.getSelectionProvider();
            for (final ISelectionChangedListener listener : fRegisteredSelectionListeners) {
                provider.removeSelectionChangedListener(listener);
            }
        }
        if (fQuickAccessAction != null && fKeyBindingService != null) {
            fKeyBindingService.unregisterAction(fQuickAccessAction);
        }
        fEditor = null;
        super.dispose();
    }
    
    /* (non-Javadoc)
     * Method declared in ActionGroup
     */
    @Override
    public void fillActionBars(IActionBars actionBar) {
        super.fillActionBars(actionBar);
        setGlobalActionHandlers(actionBar);
    }


    /*
     * The state of the editor owning this action group has changed. 
     * This method does nothing if the group's owner isn't an
     * editor.
     */
    /**
     * Note: This method is for internal use only. Clients should not call this method.
     */
    public void editorStateChanged() {
        Assert.isTrue(isEditorOwner());
    }
    private void fillQuickMenu(IMenuManager menu) {
        if (isEditorOwner()) {
            fillSubMenu(menu);
        } else {
            fillViewSubMenu(menu);
        }
    }

    protected int fillSubMenu(IMenuManager source){
        return 0;
    }
    
    protected int fillViewSubMenu(IMenuManager source){
        return 0;
    }
    
    protected void fillContextMenuExtra(IMenuManager menu){        
    }
}
