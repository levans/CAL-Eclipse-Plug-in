/*******************************************************************************
 * Copyright (c) 2005 Business Objects Software Limited and others.
 * All rights reserved. 
 * This file is made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Business Objects Software Limited - initial API and implementation based on Eclipse 3.1.2 code for
 *                             /org.eclipse.jdt.ui/ui/org/eclipse/jdt/ui/IContextMenuConstants.java
 *                           Eclipse source is available at: http://www.eclipse.org/downloads/
 *******************************************************************************/

/*
 * CALUIMessages.java
 * Creation date: Feb 15, 2006.
 * By: Edward Lam
 */
package org.openquark.cal.eclipse.ui;

import org.eclipse.osgi.util.NLS;

/**
 * @author Edward Lam
 */
public final class CALUIMessages extends NLS {

    private static final String BUNDLE_NAME = "org.openquark.cal.eclipse.ui.CALUIMessages";//$NON-NLS-1$

    private CALUIMessages() {
        // Do not instantiate
    }

    public static String JavaPlugin_internal_error;
    public static String JavaElementProperties_name;

    public static String AddMethodStubAction_label;
    public static String AddMethodStubAction_detailed_implement;
    public static String AddMethodStubAction_detailed_override;
    public static String AddMethodStubAction_description;
    public static String AddMethodStubAction_tooltip;
    public static String AddMethodStubAction_error_title;
    public static String AddMethodStubAction_QueryDialog_title;
    public static String AddMethodStubAction_OverridesFinalDialog_message;
    public static String AddMethodStubAction_OverridesPrivateDialog_message;
    public static String AddMethodStubAction_ReplaceExistingDialog_message;

    public static String OpenTypeAction_description;
    public static String OpenTypeAction_tooltip;
    public static String OpenTypeAction_errorMessage;
    public static String OpenTypeAction_errorTitle;
    public static String OpenTypeAction_label;
    public static String OpenTypeAction_dialogTitle;
    public static String OpenTypeAction_dialogMessage;

    public static String JavaUI_defaultDialogMessage;

    public static String MultiElementListSelectionDialog_pageInfoMessage;

    public static String MultiTypeSelectionDialog_dialogMessage;
    public static String MultiTypeSelectionDialog_dialogTitle;
    public static String MultiTypeSelectionDialog_errorMessage;
    public static String MultiTypeSelectionDialog_errorTitle;
    public static String MultiTypeSelectionDialog_error2Message;
    public static String MultiTypeSelectionDialog_error2Title;

    public static String TypeSelectionDialog_errorMessage;
    public static String TypeSelectionDialog_dialogMessage;
    public static String TypeSelectionDialog_errorTitle;
    public static String TypeSelectionDialog_lowerLabel;
    public static String TypeSelectionDialog_upperLabel;
    public static String TypeSelectionDialog_notypes_title;
    public static String TypeSelectionDialog_notypes_message;
    public static String TypeSelectionDialog_error3Message;
    public static String TypeSelectionDialog_error3Title;
    public static String TypeSelectionDialog_progress_consistency;

    public static String ExceptionDialog_seeErrorLogMessage;

    public static String MainTypeSelectionDialog_errorTitle;
    public static String MultiMainTypeSelectionDialog_errorTitle;

    public static String PackageSelectionDialog_error_title;
    public static String PackageSelectionDialog_nopackages_title;
    public static String PackageSelectionDialog_nopackages_message;

    public static String BuildPathDialog_title;

    public static String OverrideMethodDialog_groupMethodsByTypes;
    public static String OverrideMethodDialog_dialog_title;
    public static String OverrideMethodDialog_dialog_description;
    public static String OverrideMethodDialog_selectioninfo_more;
    public static String OverrideMethodDialog_link_tooltip;
    public static String OverrideMethodDialog_link_message;

    public static String GetterSetterMethodDialog_link_tooltip;
    public static String GetterSetterMethodDialog_link_message;

    public static String GenerateConstructorDialog_link_tooltip;
    public static String GenerateConstructorDialog_link_message;

    public static String DelegateMethodDialog_link_tooltip;
    public static String DelegateMethodDialog_link_message;

    public static String JavaImageLabelprovider_assert_wrongImage;

    public static String JavaElementLabels_default_package;
    public static String JavaElementLabels_anonym_type;
    public static String JavaElementLabels_anonym;
    public static String JavaElementLabels_import_container;
    public static String JavaElementLabels_initializer;
    public static String JavaElementLabels_concat_string;
    public static String JavaElementLabels_comma_string;
    public static String JavaElementLabels_declseparator_string;

    public static String StatusBarUpdater_num_elements_selected;

    public static String OpenTypeHierarchyUtil_error_open_view;
    public static String OpenTypeHierarchyUtil_error_open_perspective;
    public static String OpenTypeHierarchyUtil_error_open_editor;
    public static String OpenTypeHierarchyUtil_selectionDialog_title;
    public static String OpenTypeHierarchyUtil_selectionDialog_message;

    public static String TypeInfoLabelProvider_default_package;

    public static String JavaUIHelp_link_label;
    public static String JavaUIHelpContext_javaHelpCategory_label;

    public static String ResourceTransferDragAdapter_cannot_delete_resource;
    public static String ResourceTransferDragAdapter_moving_resource;
    public static String ResourceTransferDragAdapter_cannot_delete_files;

    public static String Spelling_dictionary_file_extension;
    public static String Spelling_error_label;
    public static String Spelling_correct_label;
    public static String Spelling_add_info;
    public static String Spelling_add_label;
    public static String Spelling_ignore_info;
    public static String Spelling_ignore_label;
    public static String Spelling_case_label;
    public static String Spelling_error_case_label;

    public static String JavaAnnotationHover_multipleMarkersAtThisLine;
    public static String JavaEditor_codeassist_noCompletions;

    public static String HTMLTextPresenter_ellipsis;
    public static String HTML2TextReader_listItemPrefix;

    public static String OptionalMessageDialog_dontShowAgain;
    public static String ElementValidator_cannotPerform;
    public static String SelectionListenerWithASTManager_job_title;

    public static String JavaOutlineControl_statusFieldText_hideInheritedMembers;
    public static String JavaOutlineControl_statusFieldText_showInheritedMembers;
    public static String GetHoverFocus;
    public static String GetCodeHoverAffordance;
    
    public static String RenameSupport_not_available;
    public static String RenameSupport_dialog_title;

    public static String CoreUtility_job_title;
    public static String CoreUtility_buildall_taskname;
    public static String CoreUtility_buildproject_taskname;

    public static String CoreUtility_clean_job_title;
    public static String CoreUtility_cleanall_taskname;
    public static String CoreUtility_cleanproject_taskname;

    public static String TypeSelectionDialog2_title_format;

    public static String TypeSelectionComponent_label;
    public static String TypeSelectionComponent_menu;
    public static String TypeSelectionComponent_show_status_line_label;
    public static String TypeSelectionComponent_fully_qualify_duplicates_label;

    public static String TypeInfoViewer_job_label;
    public static String TypeInfoViewer_job_error;
    public static String TypeInfoViewer_job_cancel;
    public static String TypeInfoViewer_default_package;
    public static String TypeInfoViewer_progress_label;
    public static String TypeInfoViewer_searchJob_taskName;
    public static String TypeInfoViewer_syncJob_label;
    public static String TypeInfoViewer_syncJob_taskName;
    public static String TypeInfoViewer_progressJob_label;
    public static String TypeInfoViewer_remove_from_history;
    public static String TypeInfoViewer_separator_message;

    public static String InitializeAfterLoadJob_starter_job_name;
    public static String InitializeAfterLoadJob_real_job_name;

    static {
        NLS.initializeMessages(BUNDLE_NAME, CALUIMessages.class);
    }

    public static String TypeInfoViewer_library_name_format;
    public static String CALWorkspace_Title;
    public static String CALWorkspace_showHierarchyOfModules;
    public static String CALWorkspace_linkWithEditor;
    public static String CALWorkspace_showHierarchyOfModules_tooltip;
    public static String CALWorkspace_showClassOfCALElements;
    public static String CALWorkspace_showClassOfCALElements_tooltip;
    public static String CALWorkspace_refactorMenu;
    public static String CALWorkspace_renameAction;
    public static String CALWorkspace_rename_tooltip;
    public static String CALWorkspace_showPrivateElements;
    public static String CALWorkspace_showPrivateElements_tooltip;
    public static String CALWorkspace_CALElementType_ClassInstances;
    public static String CALWorkspace_CALElementType_FriendModules;
    public static String CALWorkspace_CALElementType_Functions;
    public static String CALWorkspace_CALElementType_ImportedModules;
    public static String CALWorkspace_CALElementType_TypeClasses;
    public static String CALWorkspace_CALElementType_TypeConstructors;
    public static String CALWorkspace_CompileFailed;
    public static String CALWorkspace_ModuleName; 

    public static String CoreUtilities_SaveAllModifiedResource_Title;
    public static String CoreUtilities_SaveAllModifiedResource_Message;
    public static String CoreUtilities_SaveAllModifiedResource_SavingFilesProgress;
    
    public static String CALNewModuleWizard_title;
    public static String CALNewModuleWizard_pageTitle;
    public static String CALNewModuleWizard_description;
    public static String CALNewModuleWizard_errorTitle;
    
    public static String CALNewQuarkBinariesProjectWizard_title;
    public static String CALNewQuarkBinariesProjectWizard_pageTitle;
    public static String CALNewQuarkBinariesProjectWizard_description;
    public static String CALNewQuarkBinariesProjectWizard_error_projectNameMustNotBeNull;
    public static String CALNewQuarkBinariesProjectWizard_error_projectNameMustNotExist;
    public static String CALNewQuarkBinariesProjectWizard_error_quarkBinariesLocationMustNotBeNull;
    public static String CALNewQuarkBinariesProjectWizard_error_workspaceShouldNotBeUnderQuarkBinariesDirectory;
    public static String CALNewQuarkBinariesProjectWizard_error_quarkBinariesShouldNotBeUnderDirectoryWorkspace;
    public static String CALNewQuarkBinariesProjectWizard_error_quarkBinariesLocationMustExist;
    public static String CALNewQuarkBinariesProjectWizard_error_missingCALPlatformFile;
    public static String CALNewModuleWizard_browseButtonLabel;
    public static String CALNewModuleWizard_updateCompilerOptionsButtonLabel;
    public static String CALNewModuleWizard_selectLocationOfOpenQuarkFiles;
    public static String CALNewModuleWizard_containerNameLabel;
    public static String CALNewModuleWizard_missingQuarkBinariesProject;
    public static String CALNewModuleWizard_moduleNameLabel;
    public static String CALNewModuleWizard_projectNameLabel;
    public static String CALNewModuleWizard_quarkBinariesLocationLabel;
    public static String CALNewModuleWizard_newFileLabel;
    public static String CALNewModuleWizard_error_invalidModuleName;
    public static String CALNewModuleWizard_error_moduleAlreadyExists;
    public static String CALNewModuleWizard_error_containerMustHaveProjectName;
    public static String CALNewModuleWizard_error_invalidContainerComponent;
    public static String CALNewModuleWizard_error_noTrailingDot;
    public static String CALNewModuleWizard_error_containerNameMustNotBeNull;
    public static String CALNewModuleWizard_error_sourceFolderMustExist;
    public static String CALNewModuleWizard_error_sourceFolderMustBeSpecified;
    public static String CALNewModuleWizard_error_alreadyHasQuarkBinaries;
    
    public static String RenameResourceChange_does_not_exist;
    public static String RenameResourceChange_rename_resource;
    public static String RenameResourceChange_name;
    
    public static String Change_is_unsaved;
    public static String Change_is_read_only;
    public static String Change_has_modifications;
    public static String Change_does_not_exist;
    public static String Change_same_read_only;

    public static String DynamicValidationStateChange_workspace_changed;
    public static String DynamicValidationRefactoringChange_fatal_error;
    
    public static String MoveResourceChange_move;
}
