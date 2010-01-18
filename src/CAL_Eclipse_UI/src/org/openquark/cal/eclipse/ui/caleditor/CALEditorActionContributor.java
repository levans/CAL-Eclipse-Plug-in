/*******************************************************************************
 * Copyright (c) 2006 Business Objects Software Limited and others.
 * All rights reserved. 
 * This file is made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Business Objects Software Limited - initial API and implementation based on Eclipse 3.1.2 code for
 *                             /org.eclipse.jdt.ui/ui/org/eclipse/jdt/internal/ui/javaeditor/BasicCompilationUnitEditorActionContributor.java
 *                             /org.eclipse.jdt.ui/ui/org/eclipse/jdt/internal/ui/javaeditor/BasicJavaEditorActionContributor.java
 *                             /org.eclipse.jdt.ui/ui/org/eclipse/jdt/internal/ui/javaeditor/CompilationUnitEditorActionContributor.java
 *                           Eclipse source is available at: http://www.eclipse.org/downloads/
 *******************************************************************************/

/*
 * CALEditorActionContributor.java
 * Creation date: Jan 27, 2006.
 * By: Edward Lam
 */
package org.openquark.cal.eclipse.ui.caleditor;

import java.util.ResourceBundle;

import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.IStatusLineManager;
import org.eclipse.ui.IActionBars;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IWorkbenchActionConstants;
import org.eclipse.ui.ide.IDEActionFactory;
import org.eclipse.ui.texteditor.BasicTextEditorActionContributor;
import org.eclipse.ui.texteditor.ITextEditor;
import org.eclipse.ui.texteditor.RetargetTextEditorAction;
import org.openquark.cal.eclipse.ui.actions.CALEclipseActionConstants;
import org.openquark.cal.eclipse.ui.actions.CALEditorActionDefinitionIds;



/**
 * Contributes interesting CAL actions to the desktop's Edit menu and the toolbar.
 * @author Edward Lam
 */
public class CALEditorActionContributor extends BasicTextEditorActionContributor {
    
    private final RetargetTextEditorAction fShowOutline;
    private final RetargetTextEditorAction fOpenStructure;
    
    public CALEditorActionContributor() {
        ResourceBundle bundleForConstructedKeys = CALEditorMessages.getBundleForConstructedKeys();
        
        fShowOutline= new RetargetTextEditorAction(bundleForConstructedKeys, "ShowOutline."); //$NON-NLS-1$
        fShowOutline.setActionDefinitionId(CALEditorActionDefinitionIds.SHOW_OUTLINE);
        
        fOpenStructure= new RetargetTextEditorAction(bundleForConstructedKeys, "OpenStructure."); //$NON-NLS-1$
        fOpenStructure.setActionDefinitionId(CALEditorActionDefinitionIds.OPEN_STRUCTURE);
    }
    
    /*
     * @see org.eclipse.jdt.internal.ui.javaeditor.BasicEditorActionContributor#contributeToMenu(org.eclipse.jface.action.IMenuManager)
     */
//    public void contributeToMenu(IMenuManager menu) {
//        super.contributeToMenu(menu);
//        
//        IMenuManager editMenu= menu.findMenuUsingPath(IWorkbenchActionConstants.M_EDIT);
//        if (editMenu != null) {
//            editMenu.appendToGroup(IContextMenuConstants.GROUP_ADDITIONS, fToggleInsertModeAction);
//        }
//    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public void setActiveEditor(IEditorPart part) {
        super.setActiveEditor(part);

        /*
         * BasicJavaEditorActionContributor
         */
        IActionBars actionBars = getActionBars();
        IStatusLineManager manager = actionBars.getStatusLineManager();
        manager.setMessage(null);
        manager.setErrorMessage(null);

        ITextEditor textEditor = null;
        if (part instanceof ITextEditor) {
            textEditor = (ITextEditor)part;
        }

//        fTogglePresentation.setEditor(textEditor);
//        fToggleMarkOccurrencesAction.setEditor(textEditor);
//        fPreviousAnnotation.setEditor(textEditor);
//        fNextAnnotation.setEditor(textEditor);
//
//        fGotoMatchingBracket.setAction(getAction(textEditor, GotoMatchingBracketAction.GOTO_MATCHING_BRACKET));
//        fShowJavaDoc.setAction(getAction(textEditor, "ShowJavaDoc")); //$NON-NLS-1$
//        fShowOutline.setAction(getAction(textEditor, IJavaEditorActionDefinitionIds.SHOW_OUTLINE));
//        fOpenHierarchy.setAction(getAction(textEditor, IJavaEditorActionDefinitionIds.OPEN_HIERARCHY));
//        fOpenStructure.setAction(getAction(textEditor, IJavaEditorActionDefinitionIds.OPEN_STRUCTURE));
//
//        fStructureSelectEnclosingAction.setAction(getAction(textEditor, StructureSelectionAction.ENCLOSING));
//        fStructureSelectNextAction.setAction(getAction(textEditor, StructureSelectionAction.NEXT));
//        fStructureSelectPreviousAction.setAction(getAction(textEditor, StructureSelectionAction.PREVIOUS));
//        fStructureSelectHistoryAction.setAction(getAction(textEditor, StructureSelectionAction.HISTORY));
//
//        fGotoNextMemberAction.setAction(getAction(textEditor, GoToNextPreviousMemberAction.NEXT_MEMBER));
//        fGotoPreviousMemberAction.setAction(getAction(textEditor, GoToNextPreviousMemberAction.PREVIOUS_MEMBER));
//
//        fRemoveOccurrenceAnnotationsAction.setAction(getAction(textEditor, "RemoveOccurrenceAnnotations")); //$NON-NLS-1$

//        if (part instanceof CALEditor) {
//            CALEditor calEditor = (CALEditor)part;
//            calEditor.getActionGroup().fillActionBars(getActionBars());
//            FoldingActionGroup foldingActions = calEditor.getFoldingActionGroup();
//            if (foldingActions != null)
//                foldingActions.updateActionBars();
//        }

        /*
         * BasicCompilationUnitEditorActionContributor
         */

//        fContentAssist.setAction(getAction(textEditor, "ContentAssistProposal")); //$NON-NLS-1$
//        fContextInformation.setAction(getAction(textEditor, "ContentAssistContextInformation")); //$NON-NLS-1$
//        fCorrectionAssist.setAction(getAction(textEditor, "CorrectionAssistProposal")); //$NON-NLS-1$

//        fChangeEncodingAction.setAction(getAction(textEditor, ITextEditorActionConstants.CHANGE_ENCODING));

        actionBars.setGlobalActionHandler(CALEclipseActionConstants.SHIFT_RIGHT, getAction(textEditor, "ShiftRight")); //$NON-NLS-1$
        actionBars.setGlobalActionHandler(CALEclipseActionConstants.SHIFT_LEFT, getAction(textEditor, "ShiftLeft")); //$NON-NLS-1$

        actionBars.setGlobalActionHandler(IDEActionFactory.ADD_TASK.getId(), getAction(textEditor, IDEActionFactory.ADD_TASK.getId()));
        actionBars.setGlobalActionHandler(IDEActionFactory.BOOKMARK.getId(), getAction(textEditor, IDEActionFactory.BOOKMARK.getId()));

        fShowOutline.setAction(getAction(textEditor, CALEditorActionDefinitionIds.SHOW_OUTLINE));
        fOpenStructure.setAction(getAction(textEditor, CALEditorActionDefinitionIds.OPEN_STRUCTURE));
        
        /*
         * CompilationUnitEditorActionContributor
         */

        // Source menu.
        IActionBars bars = getActionBars();
        bars.setGlobalActionHandler(CALEclipseActionConstants.COMMENT, getAction(textEditor, "Comment")); //$NON-NLS-1$
        bars.setGlobalActionHandler(CALEclipseActionConstants.UNCOMMENT, getAction(textEditor, "Uncomment")); //$NON-NLS-1$
        bars.setGlobalActionHandler(CALEclipseActionConstants.TOGGLE_COMMENT, getAction(textEditor, "ToggleComment")); //$NON-NLS-1$
        bars.setGlobalActionHandler(CALEclipseActionConstants.GENERATE_ELEMENT_COMMENT, getAction(textEditor, "GenerateElementComment")); //$NON-NLS-1$
        bars.setGlobalActionHandler(CALEclipseActionConstants.FORMAT, getAction(textEditor, "Format")); //$NON-NLS-1$
        bars.setGlobalActionHandler(CALEclipseActionConstants.FORMAT_ELEMENT, getAction(textEditor, "QuickFormat")); //$NON-NLS-1$
        bars.setGlobalActionHandler(CALEclipseActionConstants.ADD_BLOCK_COMMENT, getAction(textEditor, "AddBlockComment")); //$NON-NLS-1$
        bars.setGlobalActionHandler(CALEclipseActionConstants.REMOVE_BLOCK_COMMENT, getAction(textEditor, "RemoveBlockComment")); //$NON-NLS-1$
        bars.setGlobalActionHandler(CALEclipseActionConstants.INDENT, getAction(textEditor, "Indent")); //$NON-NLS-1$ 
        bars.setGlobalActionHandler(CALEclipseActionConstants.OPEN_DECLARATION_ACTION, getAction(textEditor, "OpenDeclaration")); //$NON-NLS-1$ 
        bars.setGlobalActionHandler(CALEclipseActionConstants.RENAME, getAction(textEditor, "Rename")); //$NON-NLS-1$ 
        bars.setGlobalActionHandler(CALEclipseActionConstants.FIND_REFERENCES_IN_WORKSPACE, getAction(textEditor, "SearchReferencesInWorkspace")); //$NON-NLS-1$ 
        bars.setGlobalActionHandler(CALEclipseActionConstants.FIND_REFERENCES_IN_PROJECT, getAction(textEditor, "SearchReferencesInProject")); //$NON-NLS-1$ 
        bars.setGlobalActionHandler(CALEclipseActionConstants.FIND_DECLARATIONS_IN_WORKSPACE, getAction(textEditor, "SearchDeclarationsInWorkspace")); //$NON-NLS-1$ 
        bars.setGlobalActionHandler(CALEclipseActionConstants.FIND_DECLARATIONS_IN_PROJECT, getAction(textEditor, "SearchDeclarationsInProject")); //$NON-NLS-1$ 
        bars.setGlobalActionHandler(CALEclipseActionConstants.CLEAN_IMPORTS, getAction(textEditor, "CleanImports")); //$NON-NLS-1$ 
        bars.setGlobalActionHandler(CALEclipseActionConstants.GOTO_NEXT_ELEMENT_ACTION, getAction(textEditor, "GotoNextFunction")); //$NON-NLS-1$ 
        bars.setGlobalActionHandler(CALEclipseActionConstants.GOTO_PREVIOUS_ELEMENT_ACTION, getAction(textEditor, "GotoPreviousFunction")); //$NON-NLS-1$         
        bars.setGlobalActionHandler(CALEclipseActionConstants.TYPE_DECLARATION_INSERTER, getAction(textEditor, "TypeDeclarationInserter")); //$NON-NLS-1$ 
        bars.setGlobalActionHandler(CALEclipseActionConstants.PRETTY_PRINTER, getAction(textEditor, "PrettyPrinter")); //$NON-NLS-1$ 
        bars.setGlobalActionHandler(CALEclipseActionConstants.SHOW_TOOLTIP_DESCRIPTION, getAction(textEditor, "ShowTooltipDescription")); //$NON-NLS-1$ 

//        fToggleInsertModeAction.setAction(getAction(textEditor, ITextEditorActionConstants.TOGGLE_INSERT_MODE));

    }

    @Override
    public void contributeToMenu(IMenuManager menu) {
        super.contributeToMenu(menu);
        
        IMenuManager navigateMenu= menu.findMenuUsingPath(IWorkbenchActionConstants.M_NAVIGATE);
        if (navigateMenu != null) {
            navigateMenu.appendToGroup(IWorkbenchActionConstants.SHOW_EXT, fShowOutline);
//            navigateMenu.appendToGroup(IWorkbenchActionConstants.SHOW_EXT, fOpenHierarchy);
        }        
    }
    
}
