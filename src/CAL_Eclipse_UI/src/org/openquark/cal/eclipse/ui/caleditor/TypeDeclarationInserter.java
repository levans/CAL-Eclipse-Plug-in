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
 * TypeDeclarationInserter.java
 * Created: Apr 19 2007
 * By: Greg McClement
 */
package org.openquark.cal.eclipse.ui.caleditor;

import java.util.ResourceBundle;

import org.eclipse.core.resources.IStorage;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.TextSelection;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.ui.texteditor.ITextEditor;
import org.eclipse.ui.texteditor.TextEditorAction;
import org.openquark.cal.compiler.CompilerMessageLogger;
import org.openquark.cal.compiler.MessageLogger;
import org.openquark.cal.compiler.ModuleName;
import org.openquark.cal.compiler.Refactorer;
import org.openquark.cal.eclipse.core.CALModelManager;
import org.openquark.cal.eclipse.ui.actions.ActionMessages;
import org.openquark.cal.eclipse.ui.actions.ActionUtilities;
import org.openquark.cal.eclipse.ui.util.CoreUtility;
import org.openquark.cal.eclipse.ui.util.Messages;

/**
 * Invoke the TypeDeclarationInserter on the current file.
 * 
 * @author Greg McClement
 */
public class TypeDeclarationInserter extends TextEditorAction {

    protected TypeDeclarationInserter(ResourceBundle bundle, String prefix, ITextEditor editor) {
        super(bundle, prefix, editor);
    }

    private final String errorTitle = ActionMessages.TypeDeclarationInserterAction_error_title;
    private final String actionFailedMessage = ActionMessages.TypeDeclarationInserterAction_failed;
    
    @Override
    public void run() {
        // update has been called by the framework
        if (!isEnabled()){
            return;
        }
        
        if (!CoreUtility.builderEnabledCheck(errorTitle)){
            return;
        }

        // make sure the file is writable
        if (!validateEditorInputState()){
            return;
        }

        final CALEditor textEditor = (CALEditor) getTextEditor();
        final IDocument document = ActionUtilities.getDocument(textEditor);

        if (document != null) {
            final IStorage storage = textEditor.getStorage();
            
            ModuleName moduleName;
            try{
                moduleName = CALModelManager.getCALModelManager().getModuleName(storage);
            }
            catch(IllegalArgumentException ex){
                // CAL File is not in the correct spot in the hierarchy so there
                // is no type information available.
                CoreUtility.showMessage(errorTitle, ActionMessages.error_calFileNotInCorrectLocation_message, IStatus.ERROR);
                return;
            }

            int startLine = -1;
            int startColumn = -1;
            int endLine = -1;
            int endColumn = -1;
            int length = 0;
            
            // Initialize the source code position
            {
                final ISelection selection = textEditor.getSelectionProvider().getSelection();
                if (selection instanceof TextSelection){
                    final TextSelection textSelection = (TextSelection) selection;
                    try {
                        final int startOffset = textSelection.getOffset();
                        startLine = document.getLineOfOffset(startOffset);
                        startColumn = CoreUtility.getColumn(startLine, startOffset, document);

                        length = textSelection.getLength();
                        final int endOffset = startOffset + length;
                        endLine = document.getLineOfOffset(endOffset);
                        endColumn = CoreUtility.getColumn(endLine, endOffset, document);
                    } catch (BadLocationException e) {
                        startLine = -1;
                        startColumn = -1;
                        endLine = -1;
                        endColumn = -1;
                        length = 0;
                    }
                }
            }
            
            if (moduleName == null){
                final String errorMessage = Messages.format(ActionMessages.error_invalidFileName_message, storage.getName());
                CoreUtility.showMessage(errorTitle, errorMessage, IStatus.ERROR);
                return;
            }
            
            Refactorer.InsertTypeDeclarations refactorer = new Refactorer.InsertTypeDeclarations(
                    CALModelManager.getCALModelManager().getModuleContainer(textEditor.getSourceManagerFactory(true)),
                    moduleName,
                    // plus one since the offsets are all from 1.
                    startLine + 1, startColumn + 1, endLine + 1, endColumn + 1); 

            CompilerMessageLogger messageLogger = new MessageLogger();
            refactorer.calculateModifications(messageLogger);
            
            // No function were found under the current position so do the whole file
            if ( 
                    refactorer.getTopLevelTypeDeclarationsAddedWithClassConstraints() == 0 &&
                    refactorer.getTopLevelTypeDeclarationsAddedWithoutClassConstraints() == 0 &&
                    refactorer.getLocalTypeDeclarationsAddedWithClassConstraints() == 0 &&
                    refactorer.getLocalTypeDeclarationsAddedWithoutClassConstraints() == 0){
                refactorer = new Refactorer.InsertTypeDeclarations(
                        CALModelManager.getCALModelManager().getModuleContainer(textEditor.getSourceManagerFactory(true)),
                        moduleName, -1, -1, -1, -1); 

                refactorer.calculateModifications(messageLogger);
            }
            if (CoreUtility.showErrors(errorTitle, actionFailedMessage, messageLogger)){
                return;
            }
            refactorer.apply(messageLogger);
            if (CoreUtility.showErrors(errorTitle, actionFailedMessage, messageLogger)){
                return;
            }
        }
    }
    
    @Override
    public void update() {
        super.update();
        setEnabled(true);
    }


}
