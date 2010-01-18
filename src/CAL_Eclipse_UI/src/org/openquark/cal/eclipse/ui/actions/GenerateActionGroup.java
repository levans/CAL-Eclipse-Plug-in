/*******************************************************************************
 * Copyright (c) 2006 Business Objects Software Limited and others.
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
 * GenerateActionGroup.java
 * Creation date: Feb 23, 2006.
 * By: Edward Lam
 */
package org.openquark.cal.eclipse.ui.actions;

import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.Separator;
import org.openquark.cal.eclipse.ui.caleditor.CALEditor;


/**
 * Action group that adds the source and generate actions to a part's context
 * menu and installs handlers for the corresponding global menu actions.
 * 
 * <p>
 * This class may be instantiated; it is not intended to be subclassed.
 * </p>
 */
public class GenerateActionGroup extends GeneralActionGroup {
    
    /**
     * Pop-up menu: id of the import group of the source sub menu (value <code>importGroup</code>).
     */
    private static final String GROUP_IMPORT = "importGroup"; //$NON-NLS-1$

    /**
     * Note: This constructor is for internal use only. Clients should not call this constructor.
     * @param editor the compilation unit editor
     * @param groupName the group name to add the action to
     */
    public GenerateActionGroup(CALEditor editor, String groupName) {
        super(editor, groupName, "org.eclipse.jdt.ui.source.menu", "org.eclipse.jdt.ui.edit.text.java.source.quickMenu", ActionMessages.SourceMenu_label);
    }
    
    @Override
    protected void fillContextMenuExtra(IMenuManager menu){
        addEditorAction(menu, "group.show", "OpenDeclaration");
        addEditorAction(menu, "group.show", "ShowOutline");
    }
    
    @Override
    protected int fillSubMenu(IMenuManager source) {
        int added = 0;
        added += addEditorAction(source, "ToggleComment"); //$NON-NLS-1$
        added += addEditorAction(source, "AddBlockComment"); //$NON-NLS-1$
        added += addEditorAction(source, "RemoveBlockComment"); //$NON-NLS-1$
        added += addEditorAction(source, "GenerateElementComment"); //$NON-NLS-1$
        added += addEditorAction(source, "Indent"); //$NON-NLS-1$
        source.add(new Separator(GROUP_IMPORT));
        added += addEditorAction(source, "CleanImports");
        added += addEditorAction(source, "TypeDeclarationInserter");
        added += addEditorAction(source, "PrettyPrinter");
        return added;
    }
    
//    private void setGlobalActionHandlers(IActionBars actionBar) {
////        actionBar.setGlobalActionHandler(CALEclipseActionConstants.ADD_IMPORT, fAddImport);
//        if (!isEditorOwner()) {
//            // editor provides its own implementation of these actions.
//            actionBar.setGlobalActionHandler(IDEActionFactory.BOOKMARK.getId(), fAddBookmark);
//        }
//    }
    
}
