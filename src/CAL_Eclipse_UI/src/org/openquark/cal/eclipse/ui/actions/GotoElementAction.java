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
 * GotoElementAction.java
 * Creation date: Sept 12, 2007
 * By: Greg McClement
 */
package org.openquark.cal.eclipse.ui.actions;

import java.util.ResourceBundle;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IStorage;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.ITextSelection;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.part.FileEditorInput;
import org.eclipse.ui.texteditor.ITextEditor;
import org.eclipse.ui.texteditor.TextEditorAction;
import org.openquark.cal.compiler.CompilerMessageLogger;
import org.openquark.cal.compiler.MessageLogger;
import org.openquark.cal.compiler.ModuleName;
import org.openquark.cal.compiler.SourceMetricsManager;
import org.openquark.cal.compiler.SourceRange;
import org.openquark.cal.eclipse.core.CALModelManager;
import org.openquark.cal.eclipse.ui.CALEclipseUIPlugin;
import org.openquark.cal.eclipse.ui.actions.ActionMessages;
import org.openquark.cal.eclipse.ui.actions.ActionUtilities;
import org.openquark.cal.eclipse.ui.caleditor.CALEditor;
import org.openquark.cal.eclipse.ui.util.CoreUtility;
import org.openquark.cal.eclipse.ui.util.Messages;

/**
 * This class implements to goto next/previous element action that 
 * will move the cursor to the name of the next/previous element 
 * defined in the current module.
 * 
 * @author Greg McClement
 */
public class GotoElementAction extends TextEditorAction{
    private final SourceMetricsManager sourceMetrics;
	private final Direction direction;

    public enum Direction {Previous, Next};
    private final String errorTitle = ActionMessages.OpenDeclarationAction_error_title;
    
    public GotoElementAction(ResourceBundle bundle, String prefix, ITextEditor editor, Direction direction) {
        super(bundle, prefix, editor);
		this.direction = direction;
        CALModelManager modelManager = CALModelManager.getCALModelManager();
        sourceMetrics = modelManager.getSourceMetrics();
    }

    /*
     * @see org.eclipse.jface.action.Action#run()
     */
    @Override
	public void run() {
        // update has been called by the framework
        if (!isEnabled())
            return;

        if (!CoreUtility.builderEnabledCheck(errorTitle)){
            return;
        }

        final CALEditor textEditor = (CALEditor) getTextEditor();
        final ITextSelection selection = ActionUtilities.getSelection(textEditor);
        final IDocument document = ActionUtilities.getDocument(textEditor);

        if (document != null) {
            final int offset = selection.getOffset();
            try {
                CoreUtility.initializeCALBuilder(null, 100, 100);

                final int firstLine = document.getLineOfOffset(offset);
                final int column = offset - document.getLineOffset(firstLine);
                CALModelManager cmm = CALModelManager.getCALModelManager();
                FileEditorInput ei = (FileEditorInput) textEditor.getEditorInput();
                final IFile memberFile = ei.getFile();
                ModuleName moduleName = cmm.getModuleName(memberFile);
                if (moduleName == null){
                    final String errorMessage = Messages.format(ActionMessages.error_invalidFileName_message, textEditor.getStorage().getName());
                    CoreUtility.showMessage(errorTitle, errorMessage, IStatus.ERROR);
                    return;
                }                
                CompilerMessageLogger messageLogger = new MessageLogger();
                final SourceRange range;
                if (direction == Direction.Previous){
                	range = sourceMetrics.findPreviousTopLevelElement(moduleName, firstLine+1, column+1, messageLogger);
                }
                else if (direction == Direction.Next){
                	range = sourceMetrics.findNextTopLevelElement(moduleName, firstLine+1, column+1, messageLogger);
                }
                else{
                	throw new IllegalArgumentException();
                }
                if (range != null){
                    IStorage definitionFile = cmm.getInputSourceFile(moduleName);
                    IEditorPart editorPart = CoreUtility.openInEditor(definitionFile, true);
                    CoreUtility.showPosition(editorPart, definitionFile, range, true);
                }
            } catch (BadLocationException e) {
                // will only happen on concurrent modification
                CALEclipseUIPlugin.log(new Status(IStatus.ERROR, CALEclipseUIPlugin.PLUGIN_ID, IStatus.OK, "", e)); //$NON-NLS-1$
                return;                
            } catch (PartInitException e) {
                CALEclipseUIPlugin.log(new Status(IStatus.ERROR, CALEclipseUIPlugin.PLUGIN_ID, IStatus.OK, "", e)); //$NON-NLS-1$
                return;
            }
        }
    }

}
