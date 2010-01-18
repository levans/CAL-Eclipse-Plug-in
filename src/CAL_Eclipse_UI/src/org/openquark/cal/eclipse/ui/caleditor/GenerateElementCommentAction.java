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
 * GenerateElementCommentAction.java
 * Creation date: Oct 10, 2007
 * By: Greg McClement
 */
package org.openquark.cal.eclipse.ui.caleditor;

import java.util.ResourceBundle;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.DocumentRewriteSessionType;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.ITextSelection;
import org.eclipse.ui.part.FileEditorInput;
import org.eclipse.ui.texteditor.ITextEditor;
import org.eclipse.ui.texteditor.ResourceAction;
import org.eclipse.ui.texteditor.TextEditorAction;
import org.openquark.cal.caldoc.CALDocToJavaDocUtilities;
import org.openquark.cal.compiler.CompilerMessageLogger;
import org.openquark.cal.compiler.DataConstructor;
import org.openquark.cal.compiler.FunctionalAgent;
import org.openquark.cal.compiler.MessageLogger;
import org.openquark.cal.compiler.ModuleName;
import org.openquark.cal.compiler.ModuleTypeInfo;
import org.openquark.cal.compiler.Refactorer;
import org.openquark.cal.compiler.SourceMetricsManager;
import org.openquark.cal.compiler.SourceModel;
import org.openquark.cal.compiler.SourcePosition;
import org.openquark.cal.compiler.SourceRange;
import org.openquark.cal.compiler.TypeClass;
import org.openquark.cal.compiler.TypeConstructor;
import org.openquark.cal.compiler.SourceModel.SourceElement;
import org.openquark.cal.compiler.SourceModel.TypeConstructorDefn;
import org.openquark.cal.eclipse.core.CALModelManager;
import org.openquark.cal.eclipse.core.CALModelManager.SourceManagerFactory;
import org.openquark.cal.eclipse.ui.CALEclipseUIPlugin;
import org.openquark.cal.eclipse.ui.actions.ActionMessages;
import org.openquark.cal.eclipse.ui.actions.ActionUtilities;
import org.openquark.cal.eclipse.ui.actions.IndentAction;
import org.openquark.cal.eclipse.ui.templates.CALTemplateVariableResolver;
import org.openquark.cal.eclipse.ui.util.CoreUtility;
import org.openquark.util.Pair;

/**
 * Generates a CAL Doc comment initialized with argument names if possible. 
 * 
 * @author Greg McClement
 */
public class GenerateElementCommentAction extends TextEditorAction {

    final String errorTitle = ActionMessages.GenerateElementComment_error_title;

    private final SourceMetricsManager sourceMetrics;

    /**
     * Creates and initializes the action for the given text editor. The action
     * configures its visual representation from the given resource bundle.
     *
     * @param bundle the resource bundle
     * @param prefix a prefix to be prepended to the various resource keys
     *   (described in <code>ResourceAction</code> constructor), or
     *   <code>null</code> if none
     * @param editor the text editor
     * @see ResourceAction#ResourceAction(ResourceBundle, String, int)
     */
    public GenerateElementCommentAction(ResourceBundle bundle, String prefix, ITextEditor editor) {
        super(bundle, prefix, editor);
        CALModelManager modelManager = CALModelManager.getCALModelManager();
        sourceMetrics = modelManager.getSourceMetrics();
    }

    /**
     * Implementation of the <code>IAction</code> prototype. Checks if the selected
     * lines are all commented or not and uncomments/comments them respectively.
     */
    @Override
    public void run() {
        // update has been called by the framework
        if (!isEnabled()) {
            return;
        }

        if (!CoreUtility.builderEnabledCheck(ActionMessages.OpenDeclarationAction_error_title)){
            return;
        }

        final CALEditor textEditor = (CALEditor) getTextEditor();
        final ITextSelection selection = ActionUtilities.getSelection(textEditor);
        final PartiallySynchronizedDocument currentDocument = (PartiallySynchronizedDocument) ActionUtilities.getDocument(textEditor);

        if (currentDocument != null) {
            final int currentOffset = selection.getOffset();
            try {
                CoreUtility.initializeCALBuilder(null, 100, 100);

                final IDocument originalDocument = currentDocument.getOriginalDocument();
                final int originalOffset = currentDocument.getOriginalOffset(currentOffset);

                final int originalFirstLine = originalDocument.getLineOfOffset(originalOffset);
                final int originalColumn = CoreUtility.getColumn(originalFirstLine, originalOffset, originalDocument);
                CALModelManager cmm = CALModelManager.getCALModelManager();
                FileEditorInput ei = (FileEditorInput) textEditor.getEditorInput();
                final IFile memberFile = ei.getFile();
                ModuleName moduleName = cmm.getModuleName(memberFile);
                final ModuleTypeInfo mti = cmm.getModuleTypeInfo(moduleName);
                if (mti == null){
                    // show message
                    CoreUtility.showMessage(errorTitle, ActionMessages.error_messageBadSelection_noTypeInformation_CAL, IStatus.ERROR);
                    return; // bail
                }
                CompilerMessageLogger messageLogger = new MessageLogger();

                Pair<SourceElement, SourceRange> searchResult = sourceMetrics.findContainingSourceElement(moduleName, originalFirstLine+1, originalColumn+1, messageLogger);
                if (searchResult == null){
                    // show message
                    CoreUtility.showMessage(errorTitle, ActionMessages.GenerateElementComment_error_noSelectedElement, IStatus.ERROR);
                    return; // bail
                }
                SourceElement element = searchResult.fst();

                // Find the argument names
                String[] argumentNames = null;
                if (element instanceof SourceModel.FunctionDefn){
                    SourceModel.FunctionDefn functionDefn = (SourceModel.FunctionDefn) element;
                    FunctionalAgent scopedEntity = mti.getFunctionOrClassMethod(functionDefn.getName());
                    if (scopedEntity != null){
                        argumentNames = CALDocToJavaDocUtilities.getArgumentNamesFromCALDocComment(scopedEntity.getCALDocComment(), scopedEntity);
                    }
                }
                else if (element instanceof SourceModel.LocalDefn.Function.Definition){
//                    SourceModel.LocalDefn.Function.Definition localFunctionDefn = (SourceModel.LocalDefn.Function.Definition) element;
//                    FunctionalAgent scopedEntity = mti.getFunctionOrClassMethod(functionDefn.getName());
//                    localFunctionDefn.
//                    final LocalFunctionIdentifier localFunctionIdentifier = localFunctionDefn.getLocalFunctionIdentifier();
//                    Function topLevelFunction = mti.getFunction(localFunctionIdentifier.getToplevelFunctionName().getUnqualifiedName());
//                    if (topLevelFunction != null){
//                        Function localFunction = topLevelFunction.getLocalFunction(localFunctionIdentifier);
//                        if (localFunction != null){
//                            argumentNames = CALDocToJavaDocUtilities.getArgumentNamesFromCALDocComment(scopedEntity.getCALDocComment(), scopedEntity);
//                        }
//                    }
                }
                else if (element instanceof SourceModel.LocalDefn.Function.TypeDeclaration){                    
                }
                else if (element instanceof SourceModel.FunctionTypeDeclaration){
                    SourceModel.FunctionTypeDeclaration functionTypeDeclaration = (SourceModel.FunctionTypeDeclaration) element;
                    FunctionalAgent scopedEntity = mti.getFunctionOrClassMethod(functionTypeDeclaration.getFunctionName());
                    if (scopedEntity != null){
                        argumentNames = CALDocToJavaDocUtilities.getArgumentNamesFromCALDocComment(scopedEntity.getCALDocComment(), scopedEntity);
                    }
                }
                else if (element instanceof SourceModel.TypeConstructorDefn){
                    SourceModel.TypeConstructorDefn tcd = (TypeConstructorDefn) element;
                    TypeConstructor scopedEntity = mti.getTypeConstructor(tcd.getTypeConsName());
                    if (scopedEntity != null){
                        argumentNames = CALDocToJavaDocUtilities.getArgumentNamesFromCALDocComment(scopedEntity.getCALDocComment());
                    }
                }
                else if (element instanceof SourceModel.TypeConstructorDefn.AlgebraicType.DataConsDefn){
                    SourceModel.TypeConstructorDefn.AlgebraicType.DataConsDefn dataConsDefn = (SourceModel.TypeConstructorDefn.AlgebraicType.DataConsDefn) element;
                    DataConstructor scopedEntity = mti.getDataConstructor(dataConsDefn.getDataConsName());
                    if (scopedEntity != null){
                        argumentNames = CALTemplateVariableResolver.getValues(scopedEntity);
                    }
//                    argumentNames = CALDocToJavaDocUtilities.getArgumentNamesFromCALDocComment(scopedEntity.getCALDocComment(), scopedEntity);
                }
                else if (element instanceof SourceModel.TypeClassDefn){
                    SourceModel.TypeClassDefn tcd = (SourceModel.TypeClassDefn) element;
                    TypeClass scopedEntity = mti.getTypeClass(tcd.getTypeClassName());
                    if (scopedEntity != null){
                        argumentNames = CALDocToJavaDocUtilities.getArgumentNamesFromCALDocComment(scopedEntity.getCALDocComment());
                    }
                }
                else if (element instanceof SourceModel.TypeClassDefn.ClassMethodDefn){
                    SourceModel.TypeClassDefn.ClassMethodDefn tcd = (SourceModel.TypeClassDefn.ClassMethodDefn) element;
                    FunctionalAgent scopedEntity = mti.getFunctionOrClassMethod(tcd.getMethodName());
                    if (scopedEntity != null){
                        argumentNames = CALDocToJavaDocUtilities.getArgumentNamesFromCALDocComment(scopedEntity.getCALDocComment(), scopedEntity);
                    }
                }
                else if (element instanceof SourceModel.ModuleDefn){
                    argumentNames = new String[0];
                }
                else{
                    // show message
                    CoreUtility.showMessage(errorTitle, ActionMessages.GenerateElementComment_error_noSelectedElement, IStatus.ERROR);
                    return; // bail
                }
                
                // build the cal doc
                StringBuilder calDocText = new StringBuilder();
                final SourceRange originalRange = searchResult.snd();
                if (originalRange == null){
                    // show message
                    CoreUtility.showMessage(errorTitle, ActionMessages.GenerateElementComment_error_noSelectedElement, IStatus.ERROR);
                    return; // bail
                }
                
                if (argumentNames == null){
                    // doing this is safer and eliminates a bunch of if's
                    argumentNames = new String[0]; 
                }
                
                calDocText.append("/**\n");
                for(String argumentName : argumentNames){
                    calDocText.append(" * @arg ");
                    calDocText.append(argumentName);
                    calDocText.append("\n");
                }
                if (argumentNames.length == 0){                    
                    calDocText.append(" *\n");
                }
                calDocText.append(" */\n");
                final int nLines = 3 + argumentNames.length;

                SourceManagerFactory smf = textEditor.getSourceManagerFactory(true);
                final int originalRangeStartOffset = CoreUtility.toOffset(originalRange.getStartSourcePosition(), originalDocument);
                final int currentRangeStartOffset = currentDocument.fromOriginalOffset(originalRangeStartOffset);
                // line number staring at one like CAL likes
                final int currentRangeStartLine = currentDocument.getLineOfOffset(currentRangeStartOffset) + 1;
                currentDocument.startRewriteSession(DocumentRewriteSessionType.UNRESTRICTED);
                try{
                    Refactorer.ReplaceText refactorer = new Refactorer.ReplaceText(
                            CALModelManager.getCALModelManager().getModuleContainer(smf),
                            moduleName,
                            "",
                            calDocText.toString(),
                            currentRangeStartLine,
                            1);
                    final int currentFirstLine = originalDocument.getLineOfOffset(currentOffset);
                    final int currentColumn = CoreUtility.getColumn(currentFirstLine, currentOffset, currentDocument);                
                    refactorer.setCursorPosition(currentFirstLine+1, currentColumn+1);

                    refactorer.calculateModifications(messageLogger);
                    if (messageLogger.getNErrors() == 0){
                        refactorer.apply(messageLogger);
                    }

                    IndentAction.indentLine(currentDocument, currentRangeStartLine - 1, nLines, originalOffset);

                    SourcePosition newSourcePosition = refactorer.getNewCursorPosition();
                    final int currentStart = CoreUtility.toOffset(newSourcePosition, currentDocument);

                    textEditor.selectAndReveal(currentStart, 0);
                }
                finally{
                    currentDocument.stopRewriteSession(currentDocument.getActiveRewriteSession());
                }
                
            } catch (BadLocationException e) {
                // will only happen on concurrent modification
                CALEclipseUIPlugin.log(new Status(IStatus.ERROR, CALEclipseUIPlugin.PLUGIN_ID, IStatus.OK, "", e)); //$NON-NLS-1$
                return;
            }
        }
    }
}
