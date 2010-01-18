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
 * QuickSearchInWorkspace.java
 * Creation date: July 11 2007.
 * By: Greg McClement
 */
package org.openquark.cal.eclipse.ui.actions;

import java.util.ResourceBundle;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.search.ui.ISearchPageContainer;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.texteditor.ITextEditor;
import org.eclipse.ui.texteditor.TextEditorAction;
import org.openquark.cal.eclipse.ui.search.CALSearchPage;
import org.openquark.cal.eclipse.ui.util.CoreUtility;

/**
 * Implements searches from the context menu for references and declarations of objects.
 * 
 * @author Greg McClement
 */
public class QuickSearchInWorkspace extends TextEditorAction{
    public static final int DEFINITIONS = CALSearchPage.SCOPE_DEFINITIONS;
    public static final int REFERENCES = CALSearchPage.SCOPE_REFERENCES;

    // DEFINITIONS or REFERENCES
    private final int searchKind;
    
    // ISearchPageContainer.WORKSPACE_SCOPE, SELECTION_SCOPE, SELECTED_PROJECTS_SCOPE
    private final int scope; 
    
    public QuickSearchInWorkspace(ResourceBundle bundle, String prefix, ITextEditor editor, int scope, int searchKind) {
        super(bundle, prefix, editor);

        switch(scope){
        case ISearchPageContainer.WORKSPACE_SCOPE:
        case ISearchPageContainer.SELECTION_SCOPE:
        case ISearchPageContainer.SELECTED_PROJECTS_SCOPE:
            break;
        default:
            throw new IllegalArgumentException();
        }
        this.scope = scope;

        // validate scope
        {
            switch(searchKind){
            case DEFINITIONS:
            case REFERENCES:
                // ok
                break;
            default:
                throw new IllegalArgumentException();
            }
        }

        this.searchKind = searchKind;
    }

    @Override
    public void run() {
        // update has been called by the framework
        if (!isEnabled()){
            return;
        }
        
        if (!CoreUtility.builderEnabledCheck(ActionMessages.RenameAction_windowTitle)){
            return;
        }

        ISelection selection = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getSelectionService().getSelection();
        if (!CALSearchPage.performSearch(scope, searchKind, selection)){
            CoreUtility.showMessage(ActionMessages.QuickSearch_error_title, ActionMessages.QuickSearch_error_missingSelection, IStatus.ERROR);
        }
    }
}
