/*******************************************************************************
 * Copyright (c) 2006 Business Objects Software Limited and others.
 * All rights reserved. 
 * This file is made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Business Objects Software Limited - initial API and implementation based on Eclipse 3.1.2 code for
 *                             /org.eclipse.jdt.ui/ui/org/eclipse/jdt/ui/actions/JdtActionConstants.java
 *                           Eclipse source is available at: http://www.eclipse.org/downloads/
 *******************************************************************************/

/*
 * CALEclipseActionConstants.java
 * Creation date: Feb 15, 2006.
 * By: Edward Lam
 */
package org.openquark.cal.eclipse.ui.actions;

/**
 * Action ids for standard actions, for groups in the menu bar, and
 * for actions in context menus of JDT views.
 * 
 * <p>
 * This class may be instantiated; it is not intended to be subclassed.
 * </p>
 * 
 * @author Edward Lam
 */
public class CALEclipseActionConstants {

    // Navigate menu

    /**
     * Navigate menu: name of standard Goto Type global action
     * (value <code>"org.openquark.cal.eclipse.ui.actions.GoToType"</code>).
     */
    public static final String GOTO_TYPE = "org.openquark.cal.eclipse.ui.actions.GoToType"; //$NON-NLS-1$

    /**
     * Navigate menu: name of standard Goto Package global action
     * (value <code>"org.openquark.cal.eclipse.ui.actions.GoToPackage"</code>).
     */
    public static final String GOTO_PACKAGE = "org.openquark.cal.eclipse.ui.actions.GoToPackage"; //$NON-NLS-1$

    /**
     * Navigate menu: name of standard Open global action
     * (value <code>"org.openquark.cal.eclipse.ui.actions.Open"</code>).
     */
    public static final String OPEN = "org.openquark.cal.eclipse.ui.actions.Open"; //$NON-NLS-1$

    /**
     * Navigate menu: name of standard Open Super Implementation global action
     * (value <code>"org.openquark.cal.eclipse.ui.actions.OpenSuperImplementation"</code>).
     */
    public static final String OPEN_SUPER_IMPLEMENTATION = "org.openquark.cal.eclipse.ui.actions.OpenSuperImplementation"; //$NON-NLS-1$

    /**
     * Navigate menu: name of standard Open Type Hierarchy global action
     * (value <code>"org.openquark.cal.eclipse.ui.actions.OpenTypeHierarchy"</code>).
     */
    public static final String OPEN_TYPE_HIERARCHY = "org.openquark.cal.eclipse.ui.actions.OpenTypeHierarchy"; //$NON-NLS-1$

    /**
     * Navigate menu: name of standard Open Call Hierarchy global action
     * (value <code>"org.openquark.cal.eclipse.ui.actions.OpenCallHierarchy"</code>).
     * @since 3.0
     */
    public static final String OPEN_CALL_HIERARCHY = "org.openquark.cal.eclipse.ui.actions.OpenCallHierarchy"; //$NON-NLS-1$

    /**
     * Navigate menu: name of standard Open External Javadoc global action
     * (value <code>"org.openquark.cal.eclipse.ui.actions.OpenExternalJavaDoc"</code>).
     */
    public static final String OPEN_EXTERNAL_JAVA_DOC = "org.openquark.cal.eclipse.ui.actions.OpenExternalJavaDoc"; //$NON-NLS-1$

    /**
     * Navigate menu: name of standard Show in Packages View global action
     * (value <code>"org.openquark.cal.eclipse.ui.actions.ShowInPackagesView"</code>).
     */
    public static final String SHOW_IN_PACKAGE_VIEW = "org.openquark.cal.eclipse.ui.actions.ShowInPackagesView"; //$NON-NLS-1$

    /**
     * Navigate menu: name of standard Show in Navigator View global action
     * (value <code>"org.openquark.cal.eclipse.ui.actions.ShowInNaviagtorView"</code>).
     */
    public static final String SHOW_IN_NAVIGATOR_VIEW = "org.openquark.cal.eclipse.ui.actions.ShowInNaviagtorView"; //$NON-NLS-1$

    // Edit menu

    /**
     * Edit menu: name of standard Show Javadoc global action
     * (value <code>"org.openquark.cal.eclipse.ui.actions.ShowJavaDoc"</code>).
     */
    public static final String SHOW_JAVA_DOC = "org.openquark.cal.eclipse.ui.actions.ShowJavaDoc"; //$NON-NLS-1$

    /**
     * Edit menu: name of standard Code Assist global action
     * (value <code>"org.openquark.cal.eclipse.ui.actions.ContentAssist"</code>).
     */
    public static final String CONTENT_ASSIST = "org.openquark.cal.eclipse.ui.actions.ContentAssist"; //$NON-NLS-1$

    // Source menu  

    /**
     * Source menu: name of standard Comment global action
     * (value <code>"org.openquark.cal.eclipse.ui.actions.Comment"</code>).
     */
    public static final String COMMENT = "org.openquark.cal.eclipse.ui.actions.Comment"; //$NON-NLS-1$

    /**
     * Source menu: name of standard Uncomment global action
     * (value <code>"org.openquark.cal.eclipse.ui.actions.Uncomment"</code>).
     */
    public static final String UNCOMMENT = "org.openquark.cal.eclipse.ui.actions.Uncomment"; //$NON-NLS-1$

    /**
     * Source menu: name of standard ToggleComment global action
     * (value <code>"org.openquark.cal.eclipse.ui.actions.ToggleComment"</code>).
     * @since 3.0
     */
    public static final String TOGGLE_COMMENT = "org.openquark.cal.eclipse.ui.actions.ToggleComment"; //$NON-NLS-1$

    /**
     * Source menu: name of standard GenerateElementComment global action
     * (value <code>"org.openquark.cal.eclipse.ui.actions.GenerateElementComment"</code>).
     * @since 3.0
     */
    public static final String GENERATE_ELEMENT_COMMENT = "org.openquark.cal.eclipse.ui.actions.GenerateElementComment"; //$NON-NLS-1$
    
    /**
     * Source menu: name of standard Block Comment global action
     * (value <code>"org.openquark.cal.eclipse.ui.actions.AddBlockComment"</code>).
     * 
     * @since 3.0
     */
    public static final String ADD_BLOCK_COMMENT = "org.openquark.cal.eclipse.ui.actions.AddBlockComment"; //$NON-NLS-1$

    /**
     * Source menu: name of standard Block Uncomment global action
     * (value <code>"org.openquark.cal.eclipse.ui.actions.RemoveBlockComment"</code>).
     * 
     * @since 3.0
     */
    public static final String REMOVE_BLOCK_COMMENT = "org.openquark.cal.eclipse.ui.actions.RemoveBlockComment"; //$NON-NLS-1$

    /**
     * Source menu: name of standard Indent global action
     * (value <code>"org.openquark.cal.eclipse.ui.actions.Indent"</code>).
     * 
     * @since 3.0
     */
    public static final String INDENT = "org.openquark.cal.eclipse.ui.actions.Indent"; //$NON-NLS-1$

    /**
     * Source menu: name of standard Shift Right action
     * (value <code>"org.openquark.cal.eclipse.ui.actions.ShiftRight"</code>).
     */
    public static final String SHIFT_RIGHT = "org.openquark.cal.eclipse.ui.actions.ShiftRight"; //$NON-NLS-1$

    /**
     * Source menu: name of standard Shift Left global action
     * (value <code>"org.openquark.cal.eclipse.ui.actions.ShiftLeft"</code>).
     */
    public static final String SHIFT_LEFT = "org.openquark.cal.eclipse.ui.actions.ShiftLeft"; //$NON-NLS-1$

    /**
     * Source menu: name of standard Format global action
     * (value <code>"org.openquark.cal.eclipse.ui.actions.Format"</code>).
     */
    public static final String FORMAT = "org.openquark.cal.eclipse.ui.actions.Format"; //$NON-NLS-1$

    /**
     * Source menu: name of standard Format Element global action
     * (value <code>"org.openquark.cal.eclipse.ui.actions.FormatElement"</code>).
     * @since 3.0
     */
    public static final String FORMAT_ELEMENT = "org.openquark.cal.eclipse.ui.actions.FormatElement"; //$NON-NLS-1$

    /**
     * Source menu: name of standard Add Import global action
     * (value <code>"org.openquark.cal.eclipse.ui.actions.AddImport"</code>).
     */
    public static final String ADD_IMPORT = "org.openquark.cal.eclipse.ui.actions.AddImport"; //$NON-NLS-1$

    /**
     * Source menu: name of standard Organize Imports global action
     * (value <code>"org.openquark.cal.eclipse.ui.actions.OrganizeImports"</code>).
     */
    public static final String ORGANIZE_IMPORTS = "org.openquark.cal.eclipse.ui.actions.OrganizeImports"; //$NON-NLS-1$

    /**
     * Source menu: name of standard Sort Members global action (value
     * <code>"org.openquark.cal.eclipse.ui.actions.SortMembers"</code>).
     * @since 2.1
     */
    public static final String SORT_MEMBERS = "org.openquark.cal.eclipse.ui.actions.SortMembers"; //$NON-NLS-1$

    /**
     * Source menu: name of standard Surround with try/catch block global action
     * (value <code>"org.openquark.cal.eclipse.ui.actions.SurroundWithTryCatch"</code>).
     */
    public static final String SURROUND_WITH_TRY_CATCH = "org.openquark.cal.eclipse.ui.actions.SurroundWithTryCatch"; //$NON-NLS-1$

    /**
     * Source menu: name of standard Override Methods global action
     * (value <code>"org.openquark.cal.eclipse.ui.actions.OverrideMethods"</code>).
     */
    public static final String OVERRIDE_METHODS = "org.openquark.cal.eclipse.ui.actions.OverrideMethods"; //$NON-NLS-1$

    /**
     * Source menu: name of standard Generate Getter and Setter global action
     * (value <code>"org.openquark.cal.eclipse.ui.actions.GenerateGetterSetter"</code>).
     */
    public static final String GENERATE_GETTER_SETTER = "org.openquark.cal.eclipse.ui.actions.GenerateGetterSetter"; //$NON-NLS-1$

    /**
     * Source menu: name of standard delegate methods global action (value
     * <code>"org.openquark.cal.eclipse.ui.actions.GenerateDelegateMethods"</code>).
     * @since 2.1
     */
    public static final String GENERATE_DELEGATE_METHODS = "org.openquark.cal.eclipse.ui.actions.GenerateDelegateMethods"; //$NON-NLS-1$

    /**
     * Source menu: name of standard Add Constructor From Superclass global action
     * (value <code>"org.openquark.cal.eclipse.ui.actions.AddConstructorFromSuperclass"</code>).
     */
    public static final String ADD_CONSTRUCTOR_FROM_SUPERCLASS = "org.openquark.cal.eclipse.ui.actions.AddConstructorFromSuperclass"; //$NON-NLS-1$

    /**
     * Source menu: name of standard Generate Constructor using Fields global action
     * (value <code>"org.openquark.cal.eclipse.ui.actions.GenerateConstructorUsingFields"</code>).
     */
    public static final String GENERATE_CONSTRUCTOR_USING_FIELDS = "org.openquark.cal.eclipse.ui.actions.GenerateConstructorUsingFields"; //$NON-NLS-1$

    /**
     * Source menu: name of standard Add Javadoc Comment global action
     * (value <code>"org.openquark.cal.eclipse.ui.actions.AddJavaDocComment"</code>).
     */
    public static final String ADD_JAVA_DOC_COMMENT = "org.openquark.cal.eclipse.ui.actions.AddJavaDocComment"; //$NON-NLS-1$

    /**
     * Source menu: name of standard Find Strings to Externalize global action
     * (value <code>"org.openquark.cal.eclipse.ui.actions.FindStringsToExternalize"</code>).
     */
    public static final String FIND_STRINGS_TO_EXTERNALIZE = "org.openquark.cal.eclipse.ui.actions.FindStringsToExternalize"; //$NON-NLS-1$

    /**
     * Source menu: name of standard Externalize Strings global action
     * (value <code>"org.openquark.cal.eclipse.ui.actions.ExternalizeStrings"</code>).
     */
    public static final String EXTERNALIZE_STRINGS = "org.openquark.cal.eclipse.ui.actions.ExternalizeStrings"; //$NON-NLS-1$

    /**
     * Source menu: name of standard Convert Line Delimiters To Windows global action
     * (value <code>"org.openquark.cal.eclipse.ui.actions.ConvertLineDelimitersToWindows"</code>).
     */
    public static final String CONVERT_LINE_DELIMITERS_TO_WINDOWS = "org.openquark.cal.eclipse.ui.actions.ConvertLineDelimitersToWindows"; //$NON-NLS-1$

    /**
     * Source menu: name of standard Convert Line Delimiters To UNIX global action
     * (value <code>"org.openquark.cal.eclipse.ui.actions.ConvertLineDelimitersToUNIX"</code>).
     */
    public static final String CONVERT_LINE_DELIMITERS_TO_UNIX = "org.openquark.cal.eclipse.ui.actions.ConvertLineDelimitersToUNIX"; //$NON-NLS-1$

    /**
     * Source menu: name of standardConvert Line Delimiters To Mac global action
     * (value <code>"org.openquark.cal.eclipse.ui.actions.ConvertLineDelimitersToMac"</code>).
     */
    public static final String CONVERT_LINE_DELIMITERS_TO_MAC = "org.openquark.cal.eclipse.ui.actions.ConvertLineDelimitersToMac"; //$NON-NLS-1$

    // Refactor menu

    /**
     * Refactor menu: name of standard Self Encapsulate Field global action
     * (value <code>"org.openquark.cal.eclipse.ui.actions.SelfEncapsulateField"</code>).
     */
    public static final String SELF_ENCAPSULATE_FIELD = "org.openquark.cal.eclipse.ui.actions.SelfEncapsulateField"; //$NON-NLS-1$

    /**
     * Refactor menu: name of standard Modify Parameters global action
     * (value <code>"org.openquark.cal.eclipse.ui.actions.ModifyParameters"</code>).
     */
    public static final String MODIFY_PARAMETERS = "org.openquark.cal.eclipse.ui.actions.ModifyParameters"; //$NON-NLS-1$

    /**
     * Refactor menu: name of standard Pull Up global action
     * (value <code>"org.openquark.cal.eclipse.ui.actions.PullUp"</code>).
     */
    public static final String PULL_UP = "org.openquark.cal.eclipse.ui.actions.PullUp"; //$NON-NLS-1$

    /**
     * Refactor menu: name of standard Push Down global action
     * (value <code>"org.openquark.cal.eclipse.ui.actions.PushDown"</code>).
     * 
     * @since 2.1
     */
    public static final String PUSH_DOWN = "org.openquark.cal.eclipse.ui.actions.PushDown"; //$NON-NLS-1$

    /**
     * Refactor menu: name of standard Move Element global action
     * (value <code>"org.openquark.cal.eclipse.ui.actions.Move"</code>).
     */
    public static final String MOVE = "org.openquark.cal.eclipse.ui.actions.Move"; //$NON-NLS-1$

    /**
     * Refactor menu: name of standard Rename Element global action
     * (value <code>"org.openquark.cal.eclipse.ui.actions.Rename"</code>).
     */
    public static final String RENAME = "org.openquark.cal.eclipse.ui.actions.Rename"; //$NON-NLS-1$

    /**
     * Refactor menu: name of standard Inline Temp global action
     * (value <code>"org.openquark.cal.eclipse.ui.actions.InlineTemp"</code>).
     * @deprecated Use INLINE
     */
    @Deprecated
    public static final String INLINE_TEMP = "org.openquark.cal.eclipse.ui.actions.InlineTemp"; //$NON-NLS-1$

    /**
     * Refactor menu: name of standard Extract Temp global action
     * (value <code>"org.openquark.cal.eclipse.ui.actions.ExtractTemp"</code>).
     */
    public static final String EXTRACT_TEMP = "org.openquark.cal.eclipse.ui.actions.ExtractTemp"; //$NON-NLS-1$

    /**
     * Refactor menu: name of standard Extract Constant global action
     * (value <code>"org.openquark.cal.eclipse.ui.actions.ExtractConstant"</code>).
     * 
     * @since 2.1
     */
    public static final String EXTRACT_CONSTANT = "org.openquark.cal.eclipse.ui.actions.ExtractConstant"; //$NON-NLS-1$

    /**
     * Refactor menu: name of standard Introduce Parameter global action
     * (value <code>"org.openquark.cal.eclipse.ui.actions.IntroduceParameter"</code>).
     * 
     * @since 3.0
     */
    public static final String INTRODUCE_PARAMETER = "org.openquark.cal.eclipse.ui.actions.IntroduceParameter"; //$NON-NLS-1$

    /**
     * Refactor menu: name of standard Introduce Factory global action
     * (value <code>"org.openquark.cal.eclipse.ui.actions.IntroduceFactory"</code>).
     * 
     * @since 3.0
     */
    public static final String INTRODUCE_FACTORY = "org.openquark.cal.eclipse.ui.actions.IntroduceFactory"; //$NON-NLS-1$

    /**
     * Refactor menu: name of standard Extract Method global action
     * (value <code>"org.openquark.cal.eclipse.ui.actions.ExtractMethod"</code>).
     */
    public static final String EXTRACT_METHOD = "org.openquark.cal.eclipse.ui.actions.ExtractMethod"; //$NON-NLS-1$

    /**
     * Refactor menu: name of standard Inline global action 
     * (value <code>"org.openquark.cal.eclipse.ui.actions.Inline"</code>).
     *
     * @since 2.1
     */
    public static final String INLINE = "org.openquark.cal.eclipse.ui.actions.Inline"; //$NON-NLS-1$

    /**
     * Refactor menu: name of standard Extract Interface global action
     * (value <code>"org.openquark.cal.eclipse.ui.actions.ExtractInterface"</code>).
     * 
     * @since 2.1
     */
    public static final String EXTRACT_INTERFACE = "org.openquark.cal.eclipse.ui.actions.ExtractInterface"; //$NON-NLS-1$

    /**
     * Refactor menu: name of standard Generalize Type global action
     * (value <code>"org.openquark.cal.eclipse.ui.actions.ChangeType"</code>).
     * 
     * @since 3.0
     */
    public static final String CHANGE_TYPE = "org.openquark.cal.eclipse.ui.actions.ChangeType"; //$NON-NLS-1$

    /**
     * Refactor menu: name of standard global action to convert a nested type to a top level type
     * (value <code>"org.openquark.cal.eclipse.ui.actions.MoveInnerToTop"</code>).
     * 
     * @since 2.1
     */
    public static final String CONVERT_NESTED_TO_TOP = "org.openquark.cal.eclipse.ui.actions.ConvertNestedToTop"; //$NON-NLS-1$

    /**
     * Refactor menu: name of standard Use Supertype global action
     * (value <code>"org.openquark.cal.eclipse.ui.actions.UseSupertype"</code>).
     * 
     * @since 2.1
     */
    public static final String USE_SUPERTYPE = "org.openquark.cal.eclipse.ui.actions.UseSupertype"; //$NON-NLS-1$

    /**
     * Refactor menu: name of standard Infer Generic Type Arguments global action
     * (value <code>"org.openquark.cal.eclipse.ui.actions.InferTypeArguments"</code>).
     * 
     * @since 3.1
     */
    public static final String INFER_TYPE_ARGUMENTS = "org.openquark.cal.eclipse.ui.actions.InferTypeArguments"; //$NON-NLS-1$

    /**
     * Refactor menu: name of standard global action to convert a local
     * variable to a field (value <code>"org.openquark.cal.eclipse.ui.actions.ConvertLocalToField"</code>).
     * 
     * @since 2.1
     */
    public static final String CONVERT_LOCAL_TO_FIELD = "org.openquark.cal.eclipse.ui.actions.ConvertLocalToField"; //$NON-NLS-1$

    /**
     * Refactor menu: name of standard Covert Anonymous to Nested global action
     * (value <code>"org.openquark.cal.eclipse.ui.actions.ConvertAnonymousToNested"</code>).
     * 
     * @since 2.1
     */
    public static final String CONVERT_ANONYMOUS_TO_NESTED = "org.openquark.cal.eclipse.ui.actions.ConvertAnonymousToNested"; //$NON-NLS-1$

    // Search Menu

    /**
     * Search menu: name of standard Find References in Workspace global action
     * (value <code>"org.openquark.cal.eclipse.ui.actions.ReferencesInWorkspace"</code>).
     */
    public static final String FIND_REFERENCES_IN_WORKSPACE = "org.openquark.cal.eclipse.ui.actions.SearchReferencesInWorkspace"; //$NON-NLS-1$

    /**
     * Search menu: name of standard Find References in Project global action
     * (value <code>"org.openquark.cal.eclipse.ui.actions.ReferencesInProject"</code>).
     */
    public static final String FIND_REFERENCES_IN_PROJECT = "org.openquark.cal.eclipse.ui.actions.SearchReferencesInProject"; //$NON-NLS-1$

    /**
     * Search menu: name of standard Find References in Hierarchy global action
     * (value <code>"org.openquark.cal.eclipse.ui.actions.ReferencesInHierarchy"</code>).
     */
    public static final String FIND_REFERENCES_IN_HIERARCHY = "org.openquark.cal.eclipse.ui.actions.SearchReferencesInHierarchy"; //$NON-NLS-1$

    /**
     * Search menu: name of standard Find References in Working Set global action
     * (value <code>"org.openquark.cal.eclipse.ui.actions.ReferencesInWorkingSet"</code>).
     */
    public static final String FIND_REFERENCES_IN_WORKING_SET = "org.openquark.cal.eclipse.ui.actions.SearchReferencesInWorkingSet"; //$NON-NLS-1$

    /**
     * Search menu: name of standard Find Declarations in Workspace global action
     * (value <code>"org.openquark.cal.eclipse.ui.actions.DeclarationsInWorkspace"</code>).
     */
    public static final String FIND_DECLARATIONS_IN_WORKSPACE = "org.openquark.cal.eclipse.ui.actions.SearchDeclarationsInWorkspace"; //$NON-NLS-1$

    /**
     * Search menu: name of standard Find Declarations in Project global action
     * (value <code>"org.openquark.cal.eclipse.ui.actions.DeclarationsInProject"</code>).
     */
    public static final String FIND_DECLARATIONS_IN_PROJECT = "org.openquark.cal.eclipse.ui.actions.SearchDeclarationsInProject"; //$NON-NLS-1$

    /**
     * Search menu: name of standard Find Declarations in Hierarchy global action
     * (value <code>"org.openquark.cal.eclipse.ui.actions.DeclarationsInHierarchy"</code>).
     */
    public static final String FIND_DECLARATIONS_IN_HIERARCHY = "org.openquark.cal.eclipse.ui.actions.SearchDeclarationsInHierarchy"; //$NON-NLS-1$

    /**
     * Search menu: name of standard Find Declarations in Working Set global action
     * (value <code>"org.openquark.cal.eclipse.ui.actions.DeclarationsInWorkingSet"</code>).
     */
    public static final String FIND_DECLARATIONS_IN_WORKING_SET = "org.openquark.cal.eclipse.ui.actions.SearchDeclarationsInWorkingSet"; //$NON-NLS-1$

    /**
     * Search menu: name of standard Find Implementors in Workspace global action
     * (value <code>"org.openquark.cal.eclipse.ui.actions.ImplementorsInWorkspace"</code>).
     */
    public static final String FIND_IMPLEMENTORS_IN_WORKSPACE = "org.openquark.cal.eclipse.ui.actions.ImplementorsInWorkspace"; //$NON-NLS-1$

    /**
     * Search menu: name of standard Find Implementors in Project global action
     * (value <code>"org.openquark.cal.eclipse.ui.actions.ImplementorsInProject"</code>).
     */
    public static final String FIND_IMPLEMENTORS_IN_PROJECT = "org.openquark.cal.eclipse.ui.actions.ImplementorsInProject"; //$NON-NLS-1$

    /**
     * Search menu: name of standard Find Implementors in Working Set global action
     * (value <code>"org.openquark.cal.eclipse.ui.actions.ImplementorsInWorkingSet"</code>).
     */
    public static final String FIND_IMPLEMENTORS_IN_WORKING_SET = "org.openquark.cal.eclipse.ui.actions.ImplementorsInWorkingSet"; //$NON-NLS-1$

    /**
     * Search menu: name of standard Find Read Access in Workspace global action
     * (value <code>"org.openquark.cal.eclipse.ui.actions.ReadAccessInWorkspace"</code>).
     */
    public static final String FIND_READ_ACCESS_IN_WORKSPACE = "org.openquark.cal.eclipse.ui.actions.ReadAccessInWorkspace"; //$NON-NLS-1$

    /**
     * Search menu: name of standard Find Read Access in Project global action
     * (value <code>"org.openquark.cal.eclipse.ui.actions.ReadAccessInProject"</code>).
     */
    public static final String FIND_READ_ACCESS_IN_PROJECT = "org.openquark.cal.eclipse.ui.actions.ReadAccessInProject"; //$NON-NLS-1$

    /**
     * Search menu: name of standard Find Read Access in Hierarchy global action
     * (value <code>"org.openquark.cal.eclipse.ui.actions.ReadAccessInHierarchy"</code>).
     */
    public static final String FIND_READ_ACCESS_IN_HIERARCHY = "org.openquark.cal.eclipse.ui.actions.ReadAccessInHierarchy"; //$NON-NLS-1$

    /**
     * Search menu: name of standard Find Read Access in Working Set global action
     * (value <code>"org.openquark.cal.eclipse.ui.actions.ReadAccessInWorkingSet"</code>).
     */
    public static final String FIND_READ_ACCESS_IN_WORKING_SET = "org.openquark.cal.eclipse.ui.actions.ReadAccessInWorkingSet"; //$NON-NLS-1$

    /**
     * Search menu: name of standard Find Write Access in Workspace global action
     * (value <code>"org.openquark.cal.eclipse.ui.actions.WriteAccessInWorkspace"</code>).
     */
    public static final String FIND_WRITE_ACCESS_IN_WORKSPACE = "org.openquark.cal.eclipse.ui.actions.WriteAccessInWorkspace"; //$NON-NLS-1$

    /**
     * Search menu: name of standard Find Write Access in Project global action
     * (value <code>"org.openquark.cal.eclipse.ui.actions.WriteAccessInProject"</code>).
     */
    public static final String FIND_WRITE_ACCESS_IN_PROJECT = "org.openquark.cal.eclipse.ui.actions.WriteAccessInProject"; //$NON-NLS-1$

    /**
     * Search menu: name of standard Find Read Access in Hierarchy global action
     * (value <code>"org.openquark.cal.eclipse.ui.actions.WriteAccessInHierarchy"</code>).
     */
    public static final String FIND_WRITE_ACCESS_IN_HIERARCHY = "org.openquark.cal.eclipse.ui.actions.WriteAccessInHierarchy"; //$NON-NLS-1$

    /**
     * Search menu: name of standard Find Read Access in Working Set global action
     * (value <code>"org.openquark.cal.eclipse.ui.actions.WriteAccessInWorkingSet"</code>).
     */
    public static final String FIND_WRITE_ACCESS_IN_WORKING_SET = "org.openquark.cal.eclipse.ui.actions.WriteAccessInWorkingSet"; //$NON-NLS-1$

    /**
     * Search menu: name of standard Occurrences in File global action (value
     * <code>"org.openquark.cal.eclipse.ui.actions.OccurrencesInFile"</code>).
     * 
     * @since 2.1
     */
    public static final String FIND_OCCURRENCES_IN_FILE = "org.openquark.cal.eclipse.ui.actions.OccurrencesInFile"; //$NON-NLS-1$

    /**
     * Search menu: name of standard Find exception occurrences global action (value
     * <code>"org.openquark.cal.eclipse.ui.actions.ExceptionOccurrences"</code>).
     * 
     * @since 3.0
     */
    public static final String FIND_EXCEPTION_OCCURRENCES = "org.openquark.cal.eclipse.ui.actions.ExceptionOccurrences"; //$NON-NLS-1$

    /**
     * Search menu: name of standard Find implement occurrences global action (value
     * <code>"org.openquark.cal.eclipse.ui.actions.ImplementOccurrences"</code>).
     * 
     * @since 3.1
     */
    public static final String FIND_IMPLEMENT_OCCURRENCES = "org.openquark.cal.eclipse.ui.actions.ImplementOccurrences"; //$NON-NLS-1$
    
    /**
     * Source menu: name of standard Open Declaration global action
     * (value <code>"org.openquark.cal.eclipse.ui.actions.OpenDeclaration"</code>).
     * 
     * @since 3.0
     */
    public static final String OPEN_DECLARATION_ACTION = "org.openquark.cal.eclipse.ui.actions.OpenDeclaration"; //$NON-NLS-1$

    /**
     * Source menu: name of standard Open Declaration global action
     * (value <code>"org.openquark.cal.eclipse.ui.actions.CleanImports"</code>).
     * 
     * @since 3.0
     */
    public static final String CLEAN_IMPORTS = "org.openquark.cal.eclipse.ui.actions.CleanImports"; //$NON-NLS-1$

    /**
     * Source menu: name of standard Open Declaration global action
     * 
     * @since 3.0
     */
    public static final String GOTO_NEXT_ELEMENT_ACTION = "org.openquark.cal.eclipse.ui.actions.GotoNextElement"; //$NON-NLS-1$

    /**
     * Source menu: name of standard Open Declaration global action
     * 
     * @since 3.0
     */
    public static final String GOTO_PREVIOUS_ELEMENT_ACTION = "org.openquark.cal.eclipse.ui.actions.GotoPreviousElement"; //$NON-NLS-1$
    
    /**
     * Source menu: name of standard Open Declaration global action
     * (value <code>"org.openquark.cal.eclipse.ui.actions.TypeDeclarationInserter"</code>).
     * 
     * @since 3.0
     */
    public static final String TYPE_DECLARATION_INSERTER = "org.openquark.cal.eclipse.ui.actions.TypeDeclarationInserter"; //$NON-NLS-1$

    /**
     * Source menu: name of standard Open Declaration global action
     * (value <code>"org.openquark.cal.eclipse.ui.actions.PrettyPrinter"</code>).
     * 
     * @since 3.0
     */
    public static final String PRETTY_PRINTER = "org.openquark.cal.eclipse.ui.actions.PrettyPrinter"; //$NON-NLS-1$

    /**
     * Source menu: name of standard Show Tooltip Description global action
     * (value <code>"org.openquark.cal.eclipse.ui.actions.ShowTooltipDescription"</code>).
     * 
     * @since 3.0
     */
    public static final String SHOW_TOOLTIP_DESCRIPTION = "org.openquark.cal.eclipse.ui.actions.ShowTooltipDescription"; //$NON-NLS-1$
}
