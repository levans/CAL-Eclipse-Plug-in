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
 * CleanImportsAction.java
 * Created: 21-Feb-07
 * By: Greg McClement
 */
package org.openquark.cal.eclipse.ui.caleditor;

import java.util.ResourceBundle;

import org.eclipse.core.resources.IStorage;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.jface.text.IDocument;
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


public class CleanImportsAction extends TextEditorAction {

    protected CleanImportsAction(ResourceBundle bundle, String prefix, ITextEditor editor) {
        super(bundle, prefix, editor);
    }

    @Override
    public void run() {
        // update has been called by the framework
        if (!isEnabled()){
            return;
        }
        
        if (!CoreUtility.builderEnabledCheck(ActionMessages.CleanImportsAction_error_title)){
            return;
        }

        // make sure the file is writable
        if (!validateEditorInputState()){
            return;
        }

        final CALEditor textEditor = (CALEditor) getTextEditor();
        final IDocument document = ActionUtilities.getDocument(textEditor);

        if (document != null) {
            CALModelManager cmm = CALModelManager.getCALModelManager();
            final IStorage storage = textEditor.getStorage();
            
            ModuleName moduleName;
            try{
                moduleName = cmm.getModuleName(storage);
            }
            catch(IllegalArgumentException ex){
                // CAL File is not in the correct spot in the hierarchy so there
                // is no type information available.
                CoreUtility.showMessage(ActionMessages.CleanImportsAction_error_title, ActionMessages.error_calFileNotInCorrectLocation_message, IStatus.ERROR);
                return;
            }
            
            if (moduleName == null){
                final String errorMessage = Messages.format(ActionMessages.error_invalidFileName_message, storage.getName());
                CoreUtility.showMessage(ActionMessages.CleanImportsAction_error_title, errorMessage, IStatus.ERROR);
                return;
            }

            Refactorer refactorer = new Refactorer.CleanImports(
                    CALModelManager.getCALModelManager().getModuleContainer(textEditor.getSourceManagerFactory(true)),
                    moduleName,
                    true);

            CompilerMessageLogger messageLogger = new MessageLogger();
            refactorer.calculateModifications(messageLogger);
            if (CoreUtility.showErrors(ActionMessages.CleanImportsAction_error_title, ActionMessages.CleanImportsAction_failed, messageLogger)){
                return;
            }
            refactorer.apply(messageLogger);
            if (CoreUtility.showErrors(ActionMessages.CleanImportsAction_error_title, ActionMessages.CleanImportsAction_failed, messageLogger)){
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
