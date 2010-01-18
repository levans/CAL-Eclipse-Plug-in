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
 * PrettyPrinterAction.java
 * Created: June 26 2007
 * By: Greg McClement
 */
package org.openquark.cal.eclipse.ui.caleditor;

import java.util.ResourceBundle;

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


/**
 * Invoke the Pretty Printer on the current file.
 * 
 * @author Greg McClement
 */
public class PrettyPrinterAction extends TextEditorAction {

    protected PrettyPrinterAction(ResourceBundle bundle, String prefix, ITextEditor editor) {
        super(bundle, prefix, editor);
    }

    private final String errorTitle = ActionMessages.PrettyPrinterAction_error_title;
    private final String actionFailedMessage = ActionMessages.PrettyPrinterAction_failed;
    
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
            ModuleName moduleName;
            try{
                moduleName = CALModelManager.getCALModelManager().getModuleName(textEditor.getStorage());
            }
            catch(IllegalArgumentException ex){
                // CAL File is not in the correct spot in the hierarchy so there
                // is no type information available.
                CoreUtility.showMessage(errorTitle, ActionMessages.error_calFileNotInCorrectLocation_message, IStatus.ERROR);
                return;
            }
                        
            if (moduleName == null){
                final String errorMessage = Messages.format(ActionMessages.error_invalidFileName_message, textEditor.getStorage().getName());
                CoreUtility.showMessage(errorTitle, errorMessage, IStatus.ERROR);
                return;
            }
            
            Refactorer.PrettyPrint refactorer = new Refactorer.PrettyPrint(
                    CALModelManager.getCALModelManager().getModuleContainer(textEditor.getSourceManagerFactory(true)),
                    moduleName);

            CompilerMessageLogger messageLogger = new MessageLogger();
            refactorer.calculateModifications(messageLogger);           
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
