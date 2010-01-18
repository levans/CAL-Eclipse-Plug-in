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
 * RenameAction.java
 * Creation date: Feb 23 2007.
 * By: Greg McClement
 */
package org.openquark.cal.eclipse.ui.actions;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;
import java.util.SortedSet;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IStorage;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.ITextSelection;
import org.eclipse.jface.window.Window;
import org.eclipse.ltk.core.refactoring.Change;
import org.eclipse.ltk.core.refactoring.CompositeChange;
import org.eclipse.ltk.core.refactoring.PerformChangeOperation;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.texteditor.ITextEditor;
import org.eclipse.ui.texteditor.TextEditorAction;
import org.openquark.cal.compiler.CompilerMessageLogger;
import org.openquark.cal.compiler.FieldName;
import org.openquark.cal.compiler.IdentifierInfo;
import org.openquark.cal.compiler.IdentifierOccurrence;
import org.openquark.cal.compiler.LanguageInfo;
import org.openquark.cal.compiler.MessageLogger;
import org.openquark.cal.compiler.ModuleContainer;
import org.openquark.cal.compiler.ModuleName;
import org.openquark.cal.compiler.ModuleSourceDefinition;
import org.openquark.cal.compiler.Name;
import org.openquark.cal.compiler.QualifiedName;
import org.openquark.cal.compiler.Refactorer;
import org.openquark.cal.compiler.SearchManager;
import org.openquark.cal.compiler.SourceIdentifier;
import org.openquark.cal.compiler.SourcePosition;
import org.openquark.cal.compiler.IdentifierInfo.TopLevel;
import org.openquark.cal.compiler.IdentifierOccurrence.Binding;
import org.openquark.cal.eclipse.core.CALModelManager;
import org.openquark.cal.eclipse.ui.CALEclipseUIPlugin;
import org.openquark.cal.eclipse.ui.CALUIMessages;
import org.openquark.cal.eclipse.ui.caleditor.CALEditor;
import org.openquark.cal.eclipse.ui.caleditor.CALHyperlinkDetector;
import org.openquark.cal.eclipse.ui.caleditor.PartiallySynchronizedDocument;
import org.openquark.cal.eclipse.ui.util.CoreUtility;
import org.openquark.cal.eclipse.ui.util.CreateFolderChange;
import org.openquark.cal.eclipse.ui.util.MoveResourceChange;
import org.openquark.cal.eclipse.ui.util.RenameResourceChange;
import org.openquark.util.Pair;

/**
 * Provides access the the Rename refactorer throught the Eclipse UI.
 * 
 * @author Greg McClement
 */
public class RenameAction extends TextEditorAction{
    private final SearchManager searchManager;
    private final static String errorTitle = ActionMessages.RenameAction_windowTitle;
    
    public RenameAction(ResourceBundle bundle, String prefix, ITextEditor editor) {
        super(bundle, prefix, editor);
        CALModelManager modelManager = CALModelManager.getCALModelManager();
        searchManager = modelManager.getSearchManager();
    }

    @Override
    public void run() {
        // update has been called by the framework
        if (!isEnabled()){
            return;
        }
        
        if (!CoreUtility.builderEnabledCheck(errorTitle)){
            return;
        }

        final CALEditor textEditor = (CALEditor) getTextEditor();
        final ITextSelection selection = ActionUtilities.getSelection(textEditor);
        final IDocument document = ActionUtilities.getDocument(textEditor);

        if (document != null) {
            final int offset = selection.getOffset();
            try {
                final PartiallySynchronizedDocument psd = (PartiallySynchronizedDocument) document;
                
                CALModelManager cmm = CALModelManager.getCALModelManager();
                final IStorage storage = textEditor.getStorage();
                
                ModuleName moduleName;
                try{
                    moduleName = cmm.getModuleName(storage);
                }
                catch(IllegalArgumentException ex){
                    // CAL File is not in the correct spot in the hierarchy so there
                    // is no type information available.
                    CoreUtility.showMessage(errorTitle, ActionMessages.error_calFileNotInCorrectLocation_message, IStatus.ERROR);
                    return;
                }

                final Pair<SourcePosition, SourcePosition> posAndPosToTheLeft = CALHyperlinkDetector.getCurrentPositionAndPositionToTheLeft(psd, offset, moduleName);
                if (posAndPosToTheLeft == null){
                    CoreUtility.showErrorOnStatusLine(textEditor, ActionMessages.OpenAction_error_noSourceCodeMetrics);
                    return;
                }
                
                CoreUtility.initializeCALBuilder(null, 100, 100);

                ModuleSourceDefinition msd = cmm.getModuleSourceDefinition(moduleName);
                if (msd == null){
                    CoreUtility.showErrorOnStatusLine(textEditor, ActionMessages.error_messageBadSelection_noTypeInformation_CAL);
                    return;
                }

                if (!cmm.getProgramModelManager().hasModuleInProgram(moduleName)){
                    CoreUtility.showErrorOnStatusLine(textEditor, ActionMessages.OpenAction_error_noSourceCodeMetrics);
                    return;
                }

                CompilerMessageLogger messageLogger = new MessageLogger();
                
                IdentifierOccurrence<?> occurrence = searchManager.findSymbolAt(moduleName, posAndPosToTheLeft.fst(), posAndPosToTheLeft.snd(), messageLogger);
                
                // we only activate renaming if the occurrence is not an operator...
                if (occurrence != null && !(occurrence instanceof IdentifierOccurrence.Reference.Operator<?>)) {
                    final IdentifierInfo identifierInfo = occurrence.getIdentifierInfo();
                    if (identifierInfo instanceof IdentifierInfo.DataConsFieldName) {
                        final IdentifierInfo.DataConsFieldName oldName = (IdentifierInfo.DataConsFieldName)identifierInfo;
                        performDataConsFieldNameRename(textEditor.getSite().getShell(), cmm, messageLogger, oldName, oldName.getFieldName().getCalSourceForm());
                        CoreUtility.showErrors(ActionMessages.RenameAction_windowTitle, ActionMessages.RenameAction_failed, messageLogger);
                        
                    } else if (identifierInfo instanceof IdentifierInfo.TypeVariable) {
                        final IdentifierInfo.TypeVariable oldName = (IdentifierInfo.TypeVariable)identifierInfo;
                        performTypeVarRename(textEditor.getSite().getShell(), cmm, messageLogger, moduleName, oldName, oldName.getTypeVarName());
                        CoreUtility.showErrors(ActionMessages.RenameAction_windowTitle, ActionMessages.RenameAction_failed, messageLogger);
                        
                    } else if (identifierInfo instanceof IdentifierInfo.Local) {
                        
                        final IdentifierInfo.Local oldLocalName = (IdentifierInfo.Local)identifierInfo;
                        
                        if (occurrence instanceof Binding.PunnedTextualDataConsFieldName<?>) {
                            final List<Binding<IdentifierInfo.TopLevel.DataCons>> dataConsBindings = ((Binding.PunnedTextualDataConsFieldName<?>)occurrence).getDataConsNameBindings();
                            final List<IdentifierInfo.TopLevel.DataCons> dataConsInfos = new ArrayList<IdentifierInfo.TopLevel.DataCons>();
                            
                            for (final Binding<IdentifierInfo.TopLevel.DataCons> dataConsBinding : dataConsBindings) {
                                dataConsInfos.add(dataConsBinding.getIdentifierInfo());
                            }
                            
                            final IdentifierInfo.DataConsFieldName oldName = new IdentifierInfo.DataConsFieldName(FieldName.make(oldLocalName.getVarName()), dataConsInfos);
                            final MessageDialog dialog = new MessageDialog(
                                textEditor.getSite().getShell(),
                                ActionMessages.RenameAction_punnedDataConsField_title,
                                null,
                                ActionMessages.RenameAction_punnedDataConsField_message,
                                MessageDialog.QUESTION,
                                new String[] {
                                    ActionMessages.RenameAction_punnedDataConsField_renameDataConsField,
                                    ActionMessages.RenameAction_punnedDataConsField_renameLocalVar,
                                    IDialogConstants.CANCEL_LABEL },
                                0); // data cons field name is the default
                            
                            final int userChoice = dialog.open();
                            
                            switch (userChoice) {
                            case 0:
                                performDataConsFieldNameRename(textEditor.getSite().getShell(), cmm, messageLogger, oldName, oldName.getFieldName().getCalSourceForm());
                                break;
                            case 1:
                                performLocalNameRename(textEditor.getSite().getShell(), cmm, messageLogger, moduleName, oldLocalName, oldLocalName.getVarName());
                                break;
                            case 2:
                                // the user cancelled the operation, so do nothing
                                break;
                            }
                            
                        } else {
                            performLocalNameRename(textEditor.getSite().getShell(), cmm, messageLogger, moduleName, oldLocalName, oldLocalName.getVarName());
                        }
                        CoreUtility.showErrors(ActionMessages.RenameAction_windowTitle, ActionMessages.RenameAction_failed, messageLogger);
                        
                    } else if (identifierInfo instanceof IdentifierInfo.TopLevel) {
                        final IdentifierInfo.TopLevel topLevelIdentifierInfo = (IdentifierInfo.TopLevel)identifierInfo;
                        final QualifiedName oldName = topLevelIdentifierInfo.getResolvedName();
                        final SourceIdentifier.Category category = getCategoryFromTopLevelIdentifierInfo(topLevelIdentifierInfo); 
                        performRename(textEditor.getSite().getShell(), cmm, messageLogger, oldName, oldName.getUnqualifiedName(), category);
                        CoreUtility.showErrors(ActionMessages.RenameAction_windowTitle, ActionMessages.RenameAction_failed, messageLogger);
                        
                    } else if (identifierInfo instanceof IdentifierInfo.Module) {
                        final ModuleName oldName = ((IdentifierInfo.Module)identifierInfo).getResolvedName();
                        final SourceIdentifier.Category category = SourceIdentifier.Category.MODULE_NAME; 
                        performRename(textEditor.getSite().getShell(), cmm, messageLogger, oldName, oldName.toSourceText(), category);
                        CoreUtility.showErrors(ActionMessages.RenameAction_windowTitle, ActionMessages.RenameAction_failed, messageLogger);
                    }
                }
                else{
                    CoreUtility.showMessage(ActionMessages.RenameAction_windowTitle, ActionMessages.RenameAction_noSymbolSelectedError, IStatus.WARNING);
                }
            } catch (BadLocationException e) {
                // will only happen on concurrent modification
                CALEclipseUIPlugin.log(new Status(IStatus.ERROR, CALEclipseUIPlugin.PLUGIN_ID, IStatus.OK, "", e)); //$NON-NLS-1$
                return;                
            }
        }
    }
    
    /**
     * @param identifierInfo the top level identifier's info
     * @return the corresponding category.
     */
    // todo-jowong when all renaming functionality moves to the new framework, this will no longer be necessary
    private static SourceIdentifier.Category getCategoryFromTopLevelIdentifierInfo(final IdentifierInfo.TopLevel identifierInfo) {
        if (identifierInfo instanceof TopLevel.FunctionOrClassMethod) {
            return SourceIdentifier.Category.TOP_LEVEL_FUNCTION_OR_CLASS_METHOD;
        } else if (identifierInfo instanceof TopLevel.TypeCons) {
            return SourceIdentifier.Category.TYPE_CONSTRUCTOR;
        } else if (identifierInfo instanceof TopLevel.DataCons) {
            return SourceIdentifier.Category.DATA_CONSTRUCTOR;
        } else if (identifierInfo instanceof TopLevel.TypeClass) {
            return SourceIdentifier.Category.TYPE_CLASS;
        } else {
            throw new IllegalArgumentException("Unknown identifierInfo: " + identifierInfo);
        }
    }

    /**
     * Performs a local variable renaming.
     * @param shell the shell
     * @param cmm the CAL model manager
     * @param messageLogger the message logger
     * @param moduleName the current module name
     * @param oldName the original name to be renamed
     * @param initValue the initial value for the dialog
     */
    static void performLocalNameRename(Shell shell, CALModelManager cmm, CompilerMessageLogger messageLogger, ModuleName moduleName, IdentifierInfo.Local oldName, String initValue) {
        
        CoreUtility.SaveAllDirtyEditors sade = new CoreUtility.SaveAllDirtyEditors(shell);
        
        // Only rename if the module is compiled
        if (cmm.getModuleTypeInfo(moduleName) == null){
            CoreUtility.showMessage(errorTitle, ActionMessages.OpenAction_error_noSourceCodeMetrics, IStatus.ERROR);
            return;
        }

        sade.open();
        // only rename if all the dirty editors were saved.
        if (sade.getReturnCode() == Window.OK){

            // Ask the user what the new name for the symbol is going to be.
            RenameDialog rd = new RenameDialog(shell, initValue) {
                @Override
                String getDialogTitle() {
                    return ActionMessages.RenameAction_renameLocalVariable_title;
                }
                @Override
                boolean isValidNewName(final String identifier) {
                    return LanguageInfo.isValidFunctionName(identifier);
                }
            };
            
            rd.open();
            if (rd.getReturnCode() == Window.OK){

                // Run the refactorer
                ModuleContainer moduleContainer = CALModelManager.getCALModelManager().getModuleContainer(CALEditor.getSourceManagerFactory(false, shell, null));
                Refactorer refactorer = new Refactorer.RenameLocalName(moduleContainer, moduleName, oldName, rd.getNewName());

                refactorer.calculateModifications(messageLogger);
                if (CoreUtility.showErrors(ActionMessages.RenameAction_windowTitle, ActionMessages.RenameAction_failed, messageLogger)){
                    return;
                }

                refactorer.apply(messageLogger);
                if (CoreUtility.showErrors(ActionMessages.RenameAction_windowTitle, ActionMessages.RenameAction_failed, messageLogger)){
                    return;
                }
            }
        }
    }

    /**
     * Performs a type variable renaming.
     * @param shell the shell
     * @param cmm the CAL model manager
     * @param messageLogger the message logger
     * @param moduleName the current module name
     * @param oldName the original name to be renamed
     * @param initValue the initial value for the dialog
     */
    static void performTypeVarRename(Shell shell, CALModelManager cmm, CompilerMessageLogger messageLogger, ModuleName moduleName, IdentifierInfo.TypeVariable oldName, String initValue) {
        
        CoreUtility.SaveAllDirtyEditors sade = new CoreUtility.SaveAllDirtyEditors(shell);
        
        // Only rename if the module is compiled
        if (cmm.getModuleTypeInfo(moduleName) == null){
            CoreUtility.showMessage(errorTitle, ActionMessages.OpenAction_error_noSourceCodeMetrics, IStatus.ERROR);
            return;
        }

        sade.open();
        // only rename if all the dirty editors were saved.
        if (sade.getReturnCode() == Window.OK){

            // Ask the user what the new name for the symbol is going to be.
            RenameDialog rd = new RenameDialog(shell, initValue) {
                @Override
                String getDialogTitle() {
                    return ActionMessages.RenameAction_renameTypeVariable_title;
                }
                @Override
                boolean isValidNewName(final String identifier) {
                    return LanguageInfo.isValidTypeVariableName(identifier);
                }
            };
            
            rd.open();
            if (rd.getReturnCode() == Window.OK){

                // Run the refactorer
                ModuleContainer moduleContainer = CALModelManager.getCALModelManager().getModuleContainer(CALEditor.getSourceManagerFactory(false, shell, null));
                Refactorer refactorer = new Refactorer.RenameTypeVariable(moduleContainer, moduleName, oldName, rd.getNewName());

                refactorer.calculateModifications(messageLogger);
                if (CoreUtility.showErrors(ActionMessages.RenameAction_windowTitle, ActionMessages.RenameAction_failed, messageLogger)){
                    return;
                }

                refactorer.apply(messageLogger);
                if (CoreUtility.showErrors(ActionMessages.RenameAction_windowTitle, ActionMessages.RenameAction_failed, messageLogger)){
                    return;
                }
            }
        }
    }

    /**
     * Performs a data cons field name renaming.
     * @param shell the shell
     * @param cmm the CAL model manager
     * @param messageLogger the message logger
     * @param oldName the original name to be renamed
     * @param initValue the initial value for the dialog
     */
    static void performDataConsFieldNameRename(final Shell shell, CALModelManager cmm, CompilerMessageLogger messageLogger, IdentifierInfo.DataConsFieldName oldName, String initValue) {
        
        CoreUtility.SaveAllDirtyEditors sade = new CoreUtility.SaveAllDirtyEditors(shell);
        
        // Only rename if the module is compiled
        if (cmm.getModuleTypeInfo(oldName.getFirstAssociatedDataConstructor().getResolvedName().getModuleName()) == null){
            CoreUtility.showMessage(errorTitle, ActionMessages.OpenAction_error_noSourceCodeMetrics, IStatus.ERROR);
            return;
        }

        sade.open();
        // only rename if all the dirty editors were saved.
        if (sade.getReturnCode() == Window.OK){

            // Ask the user what the new name for the symbol is going to be.
            RenameDialog rd = new RenameDialog(shell, initValue) {
                @Override
                String getDialogTitle() {
                    return ActionMessages.RenameAction_renameDataConsFieldName_title;
                }
                @Override
                boolean isValidNewName(final String identifier) {
                    return LanguageInfo.isValidFieldName(identifier);
                }
            };
            
            rd.open();
            if (rd.getReturnCode() == Window.OK){

                // Run the refactorer
                final Refactorer.RenameDataConsFieldName.UserChoiceConfirmer confirmer =
                    new Refactorer.RenameDataConsFieldName.UserChoiceConfirmer() {
                        public boolean shouldRenameMultipleDataConsField(FieldName oldName, SortedSet<QualifiedName> dataConsSet) {
                            final StringBuilder dataConsList = new StringBuilder();
                            for (final QualifiedName dataConsName : dataConsSet) {
                                dataConsList.append(dataConsName).append("\n");
                            }
                            final String message = MessageFormat.format(ActionMessages.RenameAction_fieldNameAssociatedWithMultipleDataCons, oldName, dataConsList.toString());
                            MessageDialog dialog = new MessageDialog(
                                shell,
                                ActionMessages.RenameAction_renameDataConsFieldName_title,
                                null,
                                message,
                                MessageDialog.QUESTION,
                                new String[] { IDialogConstants.OK_LABEL, IDialogConstants.CANCEL_LABEL },
                                0); // yes is the default
                            return dialog.open() == 0;
                        }

                        public void notifyCollisionOfNewName(FieldName newName) {
                            CoreUtility.showMessage(errorTitle, MessageFormat.format(ActionMessages.RenameAction_fieldNameAlreadyDefinedInDataCons, newName), IStatus.ERROR);
                        }
                };
                
                ModuleContainer moduleContainer = CALModelManager.getCALModelManager().getModuleContainer(CALEditor.getSourceManagerFactory(false, shell, null));
                Refactorer refactorer = new Refactorer.RenameDataConsFieldName(moduleContainer, oldName, FieldName.make(rd.getNewName()), confirmer);

                refactorer.calculateModifications(messageLogger);
                if (CoreUtility.showErrors(ActionMessages.RenameAction_windowTitle, ActionMessages.RenameAction_failed, messageLogger)){
                    return;
                }

                refactorer.apply(messageLogger);
                if (CoreUtility.showErrors(ActionMessages.RenameAction_windowTitle, ActionMessages.RenameAction_failed, messageLogger)){
                    return;
                }
            }
        }
    }

    static void performRename(Shell shell, CALModelManager cmm, CompilerMessageLogger messageLogger, Name oldName, String initValue, final SourceIdentifier.Category category) {
    	
        CoreUtility.SaveAllDirtyEditors sade = new CoreUtility.SaveAllDirtyEditors(shell);
        
        // Only rename if the module is compiled
        if (cmm.getModuleTypeInfo(oldName.getModuleName()) == null){
        	CoreUtility.showMessage(errorTitle, ActionMessages.OpenAction_error_noSourceCodeMetrics, IStatus.ERROR);
        	return;
        }
        
        sade.open();
        // only rename if all the dirty editors were saved.
        if (sade.getReturnCode() == Window.OK){
            if (
                    category != null &&
                    // rename does not support these types of symbols
                    category != SourceIdentifier.Category.LOCAL_VARIABLE &&
                    category != SourceIdentifier.Category.LOCAL_VARIABLE_DEFINITION 
            ){
                // Ask the user what the new name for the symbol is going to be.
                RenameDialog rd = new RenameDialog(shell, initValue) {
                    @Override
                    String getDialogTitle() {
                        if (category == SourceIdentifier.Category.TOP_LEVEL_FUNCTION_OR_CLASS_METHOD) {
                            return ActionMessages.RenameAction_renameFunctionOrClassMethod_title;
                        } else if (category == SourceIdentifier.Category.DATA_CONSTRUCTOR) {
                            return ActionMessages.RenameAction_renameDataCons_title;
                        } else if (category == SourceIdentifier.Category.TYPE_CONSTRUCTOR) {
                            return ActionMessages.RenameAction_renameTypeCons_title;
                        } else if (category == SourceIdentifier.Category.TYPE_CLASS) {
                            return ActionMessages.RenameAction_renameTypeClass_title;
                        } else if (category == SourceIdentifier.Category.MODULE_NAME) {
                            return ActionMessages.RenameAction_renameModule_title;
                        } else {
                            return super.getDialogTitle();
                        }
                    }
                    @Override
                    boolean isValidNewName(String identifier) {
                        if (category == SourceIdentifier.Category.TOP_LEVEL_FUNCTION_OR_CLASS_METHOD) {
                            return LanguageInfo.isValidFunctionName(identifier);
                        } else if (category == SourceIdentifier.Category.DATA_CONSTRUCTOR) {
                            return LanguageInfo.isValidDataConstructorName(identifier);
                        } else if (category == SourceIdentifier.Category.TYPE_CONSTRUCTOR) {
                            return LanguageInfo.isValidTypeConstructorName(identifier);
                        } else if (category == SourceIdentifier.Category.TYPE_CLASS) {
                            return LanguageInfo.isValidTypeClassName(identifier);
                        } else if (category == SourceIdentifier.Category.MODULE_NAME) {
                            return LanguageInfo.isValidModuleName(identifier);
                        } else {
                            return false;
                        }
                    }
                };
                rd.open();
                if (rd.getReturnCode() == Window.OK){

                    // Run the refactorer
                    ModuleContainer moduleContainer = CALModelManager.getCALModelManager().getModuleContainer(CALEditor.getSourceManagerFactory(false, shell, null));
                    QualifiedName oldQN;
                    QualifiedName newQN;
                    if (oldName instanceof QualifiedName){
                        oldQN = (QualifiedName) oldName;
                        newQN = QualifiedName.make(oldQN.getModuleName(), rd.getNewName());
                    }
                    else if (oldName instanceof ModuleName){
                        ModuleName oldModuleName = (ModuleName) oldName;
                        try{
                            ModuleName newModuleName = ModuleName.make(rd.getNewName());
                            oldQN = QualifiedName.make(oldModuleName, Refactorer.Rename.UNQUALIFIED_NAME_FOR_MODULE_RENAMING);
                            newQN = QualifiedName.make(newModuleName, Refactorer.Rename.UNQUALIFIED_NAME_FOR_MODULE_RENAMING);
                        }
                        catch(IllegalArgumentException ex){                           
                            CoreUtility.showMessage(errorTitle, CALUIMessages.CALNewModuleWizard_error_invalidModuleName, IStatus.ERROR);
                            return;
                        }
                    }
                    else{
                        throw new IllegalArgumentException();
                    }
                    Refactorer refactorer = new Refactorer.Rename(moduleContainer, cmm.getTypeChecker(), oldQN, newQN, category);

                    refactorer.calculateModifications(messageLogger);
                    if (CoreUtility.showErrors(ActionMessages.RenameAction_windowTitle, ActionMessages.RenameAction_failed, messageLogger)){
                        return;
                    }

                    refactorer.apply(messageLogger);
                    if (CoreUtility.showErrors(ActionMessages.RenameAction_windowTitle, ActionMessages.RenameAction_failed, messageLogger)){
                        return;
                    }
                    
                    if (oldName instanceof ModuleName){
                        ModuleName oldModuleName = oldQN.getModuleName();
                        ModuleName newModuleName = newQN.getModuleName();
                        if (!oldModuleName.equals(newModuleName)){
                            IStorage definitionStorage = cmm.getInputSourceFile(oldModuleName);
                            if (definitionStorage instanceof IFile){
                                IFile definitionFile = (IFile) definitionStorage;
                                final String newName = newModuleName.getLastComponent() + ".cal";

                                Change renameChange = null;

                                // if the file name would change then create 
                                // a Change object for the rename
                                if (!newModuleName.getLastComponent().equals(oldModuleName.getLastComponent())){
                                    RenameResourceChange changeFileName = new RenameResourceChange(null, definitionFile, newName, null);
                                    changeFileName.initializeValidationData(new NullProgressMonitor());
                                    renameChange = changeFileName;
                                }

                                CompositeChange moveChanges = new CompositeChange("");
                                
                                // If the hierarchical part of the name changed 
                                // then we have to move the file
                                // to a new location
                                if ( 
                                    (newModuleName.getNComponents() != oldModuleName.getNComponents()) || 
                                    
                                    (
                                        newModuleName.getNComponents() > 1 && 
                                        (!newModuleName.getImmediatePrefix().equals(oldModuleName.getImmediatePrefix()))
                                    )){
                                    IPath sourcePath = definitionFile.getLocation();
                                    IPath rootPath = sourcePath.removeLastSegments(oldModuleName.getNComponents());
                                    IPath destPath = rootPath;
                                    String[] components = newModuleName.getComponents();
                                    for (int i = 0; i < components.length - 1; i++) {
                                        destPath = destPath.append(components[i]);
                                    }
                                    IFolder lastComponent = null;
                                    {
                                        IFolder rootFolder = (IFolder) ResourcesPlugin.getWorkspace().getRoot().getContainerForLocation(rootPath);
                                        lastComponent = rootFolder;
                                        final String[] newModuleNameComponents = newModuleName.getComponents();
                                        final int justBeforeLastComponent = newModuleNameComponents.length - 1;

                                        for(int i = 0; i < justBeforeLastComponent; ++i){
                                            final String componentName = newModuleNameComponents[i];
                                            lastComponent = lastComponent.getFolder(componentName);
                                            if (!lastComponent.exists()){
                                                moveChanges.add(new CreateFolderChange(lastComponent));
                                            }
                                        }                                        
                                    }
                                    
                                    // have to move the file after the name is changed
                                    IFolder sourceFolder = (IFolder) ResourcesPlugin.getWorkspace().getRoot().getContainerForLocation(sourcePath.removeLastSegments(1));
                                    final IFile sourceFile = sourceFolder.getFile(newModuleName.getLastComponent() + ".cal");
                                    MoveResourceChange changeLocation = new MoveResourceChange(sourceFile, lastComponent);                                
                                    changeLocation.initializeValidationData(new NullProgressMonitor());
                                    moveChanges.add(changeLocation);
                                }

                                {
                                    // The rename has to run first so that the move in perform2 is 
                                    // accessing an existing file.
                                    try {
                                        if (renameChange != null){
                                            PerformChangeOperation perform1= new PerformChangeOperation(renameChange);
                                            ResourcesPlugin.getWorkspace().run(perform1, new NullProgressMonitor());
                                        }
                                        if (moveChanges != null){
                                            PerformChangeOperation perform2= new PerformChangeOperation(moveChanges);
                                            ResourcesPlugin.getWorkspace().run(perform2, new NullProgressMonitor());
                                        }
                                    } catch (CoreException e) {
                                        CALEclipseUIPlugin.log(new Status(IStatus.ERROR, CALEclipseUIPlugin.PLUGIN_ID, IStatus.OK, "", e)); //$NON-NLS-1$                            }
                                    }
//                                  perform.getUndoChange();
                                }
                            }
                            else{
                                assert false;
                            }
                        }
                    }

                }
            }
            else{
                CoreUtility.showMessage(ActionMessages.RenameAction_windowTitle, ActionMessages.RenameAction_noSymbolSelectedError, IStatus.WARNING);
            }
        }
    }

    /**
     * A dialog for asking the user for the new name of the symbol.
     */
    private static abstract class RenameDialog extends Dialog{

        private Text newNameText;
        private final String originalName;
        
        private String newName;
        
        protected RenameDialog(Shell parentShell, String originalName) {
            super(parentShell);
            this.newName = originalName;
            this.originalName = originalName;
        }
        
        public String getNewName(){
            return newName;
        }
        
        @Override
        protected void okPressed(){
            newName = newNameText.getText();
            super.okPressed();
        }
        
        @Override
        protected Button createButton(Composite parent, int id, String label, boolean defaultButton) {
            Button button = super.createButton(parent, id, label, defaultButton);
            if (id == IDialogConstants.OK_ID){
                button.setEnabled(false);
            }
            return button;
        }

        @Override
        public Control createDialogArea(Composite parent){
            Composite composite = (Composite) super.createDialogArea(parent);
            getShell().setText(getDialogTitle());
            GridLayout layout = new GridLayout();
            layout.verticalSpacing = 20;
            layout.numColumns = 2;
            composite.setLayout(layout);
            Label label = new Label(composite, SWT.CENTER);
            label.setText(ActionMessages.RenameAction_newNameTextLabel);
            {
                newNameText = new Text(composite, SWT.LEFT | SWT.BORDER);
                newNameText.setText(newName);
                newNameText.selectAll();
                newNameText.addModifyListener(
                        new ModifyListener(){

                            private boolean identifierIsOkay(String identifier){
                                if (identifier.length() == 0){
                                    return false;
                                }

                                if (!isValidNewName(identifier)){
                                    return false;                                
                                }

                                if (identifier.equals(originalName)){
                                    return false;
                                }

                                return true;
                            }

                            public void modifyText(ModifyEvent arg0) {
                                getButton(IDialogConstants.OK_ID).setEnabled(identifierIsOkay(newNameText.getText()));
                            }                            
                        }
                );

                GridData gridData = new GridData();
                gridData.horizontalAlignment = GridData.FILL;
                gridData.grabExcessHorizontalSpace = true;
                gridData.widthHint = 250;
                newNameText.setLayoutData(gridData);                
            }

            getInitialSize();
            return composite;
        }

        /**
         * @return the title for the dialog.
         */
        String getDialogTitle() {
            return ActionMessages.RenameAction_windowTitle;
        }

        /**
         * @param identifier an identifier to check.
         * @return whether the name is valid as the new name in a renaming.
         */
        abstract boolean isValidNewName(String identifier);
    }
}
