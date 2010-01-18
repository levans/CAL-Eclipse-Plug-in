/*******************************************************************************
 * Copyright (c) 2006 Business Objects Software Limited and others.
 * All rights reserved. 
 * This file is made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Business Objects Software Limited - initial API and implementation based on Eclipse 3.1.2 code for
 *                             /org.eclipse.jdt.core/model/org/eclipse/jdt/core/JavaCore.java
 *                           Eclipse source is available at: http://www.eclipse.org/downloads/
 *******************************************************************************/

/*
 * CoreOptionIDs.java
 * Creation date: Feb 9, 2006.
 * By: Edward Lam
 */
package org.openquark.cal.eclipse.core;


/**
 * @author Edward Lam
 */
public class CoreOptionIDs {
    
    /**
     * The plug-in identifier of the CAL core support
     */
    public static final String PLUGIN_ID = CALEclipseCorePlugin.PLUGIN_ID;
    
    /**
     * Possible  configurable option ID.
     * Enables the CAL builder.
     * @see CALEclipseCorePreferenceInitializer#initializeDefaultPreferences()
     */
    public static final String CORE_CAL_BUILD_ENABLE = PLUGIN_ID + ".builder.enable"; //$NON-NLS-1$

    
    /**
     * The identifier for the Java builder
     * (value <code>"org.eclipse.jdt.core.javabuilder"</code>).
     */
    public static final String BUILDER_ID = CALEclipseCorePlugin.BUILDER_ID;
    
    /**
     * The identifier for the Java model
     * (value <code>"org.eclipse.jdt.core.javamodel"</code>).
     */
    public static final String MODEL_ID = PLUGIN_ID + ".javamodel" ; //$NON-NLS-1$
    
    /**
     * The identifier for the Java nature
     * (value <code>"org.eclipse.jdt.core.javanature"</code>).
     * The presence of this nature on a project indicates that it is 
     * Java-capable.
     *
     * @see org.eclipse.core.resources.IProject#hasNature(java.lang.String)
     */
    public static final String NATURE_ID = CALEclipseCorePlugin.BUILDER_ID;
    
    /**
     * Name of the handle id attribute in a Java marker.
     */
    protected static final String ATT_HANDLE_ID =
        "org.openquark.cal.eclipse.core.CALModelManager.handleId" ; //$NON-NLS-1$
    
    /**
     * Name of the User Library Container id.
     * @since 3.0
     */
    public static final String USER_LIBRARY_CONTAINER_ID= "org.openquark.cal.eclipse.core.USER_LIBRARY"; //$NON-NLS-1$
    
    // *************** Possible IDs for configurable options. ********************
    
    /**
     * Possible  configurable option ID.
     * @see CALEclipseCorePreferenceInitializer#initializeDefaultPreferences()
     */
    public static final String COMPILER_LOCAL_VARIABLE_ATTR = PLUGIN_ID + ".compiler.debug.localVariable"; //$NON-NLS-1$
    /**
     * Possible  configurable option ID.
     * @see CALEclipseCorePreferenceInitializer#initializeDefaultPreferences()
     */
    public static final String COMPILER_LINE_NUMBER_ATTR = PLUGIN_ID + ".compiler.debug.lineNumber"; //$NON-NLS-1$
    /**
     * Possible  configurable option ID.
     * @see CALEclipseCorePreferenceInitializer#initializeDefaultPreferences()
     */
    public static final String COMPILER_SOURCE_FILE_ATTR = PLUGIN_ID + ".compiler.debug.sourceFile"; //$NON-NLS-1$
    /**
     * Possible  configurable option ID.
     * @see CALEclipseCorePreferenceInitializer#initializeDefaultPreferences()
     */
    public static final String COMPILER_CODEGEN_UNUSED_LOCAL = PLUGIN_ID + ".compiler.codegen.unusedLocal"; //$NON-NLS-1$
    /**
     * Possible  configurable option ID.
     * @see CALEclipseCorePreferenceInitializer#initializeDefaultPreferences()
     */
    public static final String COMPILER_CODEGEN_TARGET_PLATFORM = PLUGIN_ID + ".compiler.codegen.targetPlatform"; //$NON-NLS-1$
    /**
     * Possible  configurable option ID.
     * @see CALEclipseCorePreferenceInitializer#initializeDefaultPreferences()
     * @since 3.0
     */
    public static final String COMPILER_CODEGEN_INLINE_JSR_BYTECODE = PLUGIN_ID + ".compiler.codegen.inlineJsrBytecode"; //$NON-NLS-1$
    /**
     * Possible  configurable option ID.
     * @see CALEclipseCorePreferenceInitializer#initializeDefaultPreferences()
     * @since 3.0
     */
    public static final String COMPILER_DOC_COMMENT_SUPPORT = PLUGIN_ID + ".compiler.doc.comment.support"; //$NON-NLS-1$
    /**
     * Possible  configurable option ID.
     * @see CALEclipseCorePreferenceInitializer#initializeDefaultPreferences()
     * @deprecated - discontinued since turning off would violate language specs
     */
    @Deprecated
    public static final String COMPILER_PB_UNREACHABLE_CODE = PLUGIN_ID + ".compiler.problem.unreachableCode"; //$NON-NLS-1$
    /**
     * Possible  configurable option ID.
     * @see CALEclipseCorePreferenceInitializer#initializeDefaultPreferences()
     * @deprecated - discontinued since turning off would violate language specs
     */
    @Deprecated
    public static final String COMPILER_PB_INVALID_IMPORT = PLUGIN_ID + ".compiler.problem.invalidImport"; //$NON-NLS-1$
    /**
     * Possible  configurable option ID.
     * @see CALEclipseCorePreferenceInitializer#initializeDefaultPreferences()
     */
    public static final String COMPILER_PB_OVERRIDING_PACKAGE_DEFAULT_METHOD = PLUGIN_ID + ".compiler.problem.overridingPackageDefaultMethod"; //$NON-NLS-1$
    /**
     * Possible  configurable option ID.
     * @see CALEclipseCorePreferenceInitializer#initializeDefaultPreferences()
     */
    public static final String COMPILER_PB_METHOD_WITH_CONSTRUCTOR_NAME = PLUGIN_ID + ".compiler.problem.methodWithConstructorName"; //$NON-NLS-1$
    /**
     * Possible  configurable option ID.
     * @see CALEclipseCorePreferenceInitializer#initializeDefaultPreferences()
     */
    public static final String COMPILER_PB_DEPRECATION = PLUGIN_ID + ".compiler.problem.deprecation"; //$NON-NLS-1$
    /**
     * Possible  configurable option ID.
     * @see CALEclipseCorePreferenceInitializer#initializeDefaultPreferences()
     * @since 2.1
     */
    public static final String COMPILER_PB_DEPRECATION_IN_DEPRECATED_CODE = PLUGIN_ID + ".compiler.problem.deprecationInDeprecatedCode"; //$NON-NLS-1$
    /**
     * Possible  configurable option ID.
     * @see CALEclipseCorePreferenceInitializer#initializeDefaultPreferences()
     * @since 3.0
     */
    public static final String COMPILER_PB_DEPRECATION_WHEN_OVERRIDING_DEPRECATED_METHOD = "org.eclipse.jdt.core.compiler.problem.deprecationWhenOverridingDeprecatedMethod"; //$NON-NLS-1$
    /**
     * Possible  configurable option ID.
     * @see CALEclipseCorePreferenceInitializer#initializeDefaultPreferences()
     */
    public static final String COMPILER_PB_HIDDEN_CATCH_BLOCK = PLUGIN_ID + ".compiler.problem.hiddenCatchBlock"; //$NON-NLS-1$
    /**
     * Possible  configurable option ID.
     * @see CALEclipseCorePreferenceInitializer#initializeDefaultPreferences()
     */
    public static final String COMPILER_PB_UNUSED_LOCAL = PLUGIN_ID + ".compiler.problem.unusedLocal"; //$NON-NLS-1$
    /**
     * Possible  configurable option ID.
     * @see CALEclipseCorePreferenceInitializer#initializeDefaultPreferences()
     */
    public static final String COMPILER_PB_UNUSED_PARAMETER = PLUGIN_ID + ".compiler.problem.unusedParameter"; //$NON-NLS-1$
    /**
     * Possible  configurable option ID.
     * @see CALEclipseCorePreferenceInitializer#initializeDefaultPreferences()
     * @since 2.1
     */
    public static final String COMPILER_PB_UNUSED_PARAMETER_WHEN_IMPLEMENTING_ABSTRACT = PLUGIN_ID + ".compiler.problem.unusedParameterWhenImplementingAbstract"; //$NON-NLS-1$
    /**
     * Possible  configurable option ID.
     * @see CALEclipseCorePreferenceInitializer#initializeDefaultPreferences()
     * @since 2.1
     */
    public static final String COMPILER_PB_UNUSED_PARAMETER_WHEN_OVERRIDING_CONCRETE = PLUGIN_ID + ".compiler.problem.unusedParameterWhenOverridingConcrete"; //$NON-NLS-1$
    /**
     * Possible  configurable option ID.
     * @see CALEclipseCorePreferenceInitializer#initializeDefaultPreferences()
     * @since 2.0
     */
    public static final String COMPILER_PB_UNUSED_IMPORT = PLUGIN_ID + ".compiler.problem.unusedImport"; //$NON-NLS-1$
    /**
     * Possible  configurable option ID.
     * @see CALEclipseCorePreferenceInitializer#initializeDefaultPreferences()
     */
    public static final String COMPILER_PB_SYNTHETIC_ACCESS_EMULATION = PLUGIN_ID + ".compiler.problem.syntheticAccessEmulation"; //$NON-NLS-1$
    /**
     * Possible  configurable option ID.
     * @see CALEclipseCorePreferenceInitializer#initializeDefaultPreferences()
     * @since 2.0
     */
    public static final String COMPILER_PB_NON_NLS_STRING_LITERAL = PLUGIN_ID + ".compiler.problem.nonExternalizedStringLiteral"; //$NON-NLS-1$
    /**
     * Possible  configurable option ID.
     * @see CALEclipseCorePreferenceInitializer#initializeDefaultPreferences()
     * @since 2.0
     */
    public static final String COMPILER_PB_ASSERT_IDENTIFIER = PLUGIN_ID + ".compiler.problem.assertIdentifier"; //$NON-NLS-1$
    /**
     * Possible  configurable option ID.
     * @see CALEclipseCorePreferenceInitializer#initializeDefaultPreferences()
     * @since 3.1
     */
    public static final String COMPILER_PB_ENUM_IDENTIFIER = PLUGIN_ID + ".compiler.problem.enumIdentifier"; //$NON-NLS-1$
    /**
     * Possible  configurable option ID.
     * @see CALEclipseCorePreferenceInitializer#initializeDefaultPreferences()
     * @since 2.1
     */
    public static final String COMPILER_PB_STATIC_ACCESS_RECEIVER = PLUGIN_ID + ".compiler.problem.staticAccessReceiver"; //$NON-NLS-1$
    /**
     * Possible  configurable option ID.
     * @see CALEclipseCorePreferenceInitializer#initializeDefaultPreferences()
     * @since 3.0
     */
    public static final String COMPILER_PB_INDIRECT_STATIC_ACCESS = PLUGIN_ID + ".compiler.problem.indirectStaticAccess"; //$NON-NLS-1$
    /**
     * Possible  configurable option ID.
     * @see CALEclipseCorePreferenceInitializer#initializeDefaultPreferences()
     * @since 2.1
     */
    public static final String COMPILER_PB_NO_EFFECT_ASSIGNMENT = PLUGIN_ID + ".compiler.problem.noEffectAssignment"; //$NON-NLS-1$
    /**
     * Possible  configurable option ID.
     * @see CALEclipseCorePreferenceInitializer#initializeDefaultPreferences()
     * @since 2.1
     */
    public static final String COMPILER_PB_INCOMPATIBLE_NON_INHERITED_INTERFACE_METHOD = PLUGIN_ID + ".compiler.problem.incompatibleNonInheritedInterfaceMethod"; //$NON-NLS-1$
    /**
     * Possible  configurable option ID.
     * @see CALEclipseCorePreferenceInitializer#initializeDefaultPreferences()
     * @since 2.1
     */
    public static final String COMPILER_PB_UNUSED_PRIVATE_MEMBER = PLUGIN_ID + ".compiler.problem.unusedPrivateMember"; //$NON-NLS-1$
    /**
     * Possible  configurable option ID.
     * @see CALEclipseCorePreferenceInitializer#initializeDefaultPreferences()
     * @since 3.0
     */
    public static final String COMPILER_PB_LOCAL_VARIABLE_HIDING = PLUGIN_ID + ".compiler.problem.localVariableHiding"; //$NON-NLS-1$
    /**
     * Possible  configurable option ID.
     * @see CALEclipseCorePreferenceInitializer#initializeDefaultPreferences()
     * @since 3.0
     */
    public static final String COMPILER_PB_SPECIAL_PARAMETER_HIDING_FIELD = PLUGIN_ID + ".compiler.problem.specialParameterHidingField"; //$NON-NLS-1$
    /**
     * Possible  configurable option ID.
     * @see CALEclipseCorePreferenceInitializer#initializeDefaultPreferences()
     * @since 3.0
     */
    public static final String COMPILER_PB_FIELD_HIDING = PLUGIN_ID + ".compiler.problem.fieldHiding"; //$NON-NLS-1$
    /**
     * Possible  configurable option ID.
     * @see CALEclipseCorePreferenceInitializer#initializeDefaultPreferences()
     * @since 3.1
     */
    public static final String COMPILER_PB_TYPE_PARAMETER_HIDING = PLUGIN_ID + ".compiler.problem.typeParameterHiding"; //$NON-NLS-1$
    /**
     * Possible  configurable option ID.
     * @see CALEclipseCorePreferenceInitializer#initializeDefaultPreferences()
     * @since 3.0
     */
    public static final String COMPILER_PB_POSSIBLE_ACCIDENTAL_BOOLEAN_ASSIGNMENT = PLUGIN_ID + ".compiler.problem.possibleAccidentalBooleanAssignment"; //$NON-NLS-1$
    /**
     * Possible  configurable option ID.
     * @see CALEclipseCorePreferenceInitializer#initializeDefaultPreferences()
     * @since 3.0
     */
    public static final String COMPILER_PB_EMPTY_STATEMENT = PLUGIN_ID + ".compiler.problem.emptyStatement"; //$NON-NLS-1$
    /**
     * Possible  configurable option ID.
     * @see CALEclipseCorePreferenceInitializer#initializeDefaultPreferences()
     * @since 3.0
     */
    public static final String COMPILER_PB_BOOLEAN_METHOD_THROWING_EXCEPTION = PLUGIN_ID + ".compiler.problem.booleanMethodThrowingException"; //$NON-NLS-1$
    /**
     * Possible  configurable option ID.
     * @see CALEclipseCorePreferenceInitializer#initializeDefaultPreferences()
     * @since 3.0
     */
    public static final String COMPILER_PB_UNNECESSARY_TYPE_CHECK = PLUGIN_ID + ".compiler.problem.unnecessaryTypeCheck"; //$NON-NLS-1$
    /**
     * Possible  configurable option ID.
     * @see CALEclipseCorePreferenceInitializer#initializeDefaultPreferences()
     * @since 3.0
     */
    public static final String COMPILER_PB_UNNECESSARY_ELSE = PLUGIN_ID + ".compiler.problem.unnecessaryElse"; //$NON-NLS-1$
    /**
     * Possible  configurable option ID.
     * @see CALEclipseCorePreferenceInitializer#initializeDefaultPreferences()
     * @since 3.0
     */
    public static final String COMPILER_PB_UNDOCUMENTED_EMPTY_BLOCK = PLUGIN_ID + ".compiler.problem.undocumentedEmptyBlock"; //$NON-NLS-1$
    /**
     * Possible  configurable option ID.
     * @see CALEclipseCorePreferenceInitializer#initializeDefaultPreferences()
     * @since 3.0
     */
    public static final String COMPILER_PB_FINALLY_BLOCK_NOT_COMPLETING = PLUGIN_ID + ".compiler.problem.finallyBlockNotCompletingNormally"; //$NON-NLS-1$
    /**
     * Possible  configurable option ID.
     * @see CALEclipseCorePreferenceInitializer#initializeDefaultPreferences()
     * @since 3.0
     */
    public static final String COMPILER_PB_UNUSED_DECLARED_THROWN_EXCEPTION = PLUGIN_ID + ".compiler.problem.unusedDeclaredThrownException"; //$NON-NLS-1$
    /**
     * Possible  configurable option ID.
     * @see CALEclipseCorePreferenceInitializer#initializeDefaultPreferences()
     * @since 3.0
     */
    public static final String COMPILER_PB_UNUSED_DECLARED_THROWN_EXCEPTION_WHEN_OVERRIDING = PLUGIN_ID + ".compiler.problem.unusedDeclaredThrownExceptionWhenOverriding"; //$NON-NLS-1$
    /**
     * Possible  configurable option ID.
     * @see CALEclipseCorePreferenceInitializer#initializeDefaultPreferences()
     * @since 3.0
     */
    public static final String COMPILER_PB_UNQUALIFIED_FIELD_ACCESS = PLUGIN_ID + ".compiler.problem.unqualifiedFieldAccess"; //$NON-NLS-1$
    /**
     * Possible  configurable option ID.
     * @see CALEclipseCorePreferenceInitializer#initializeDefaultPreferences()
     * @deprecated - got renamed into {@link #COMPILER_PB_UNCHECKED_TYPE_OPERATION}
     * @since 3.1
     */
    @Deprecated
    public static final String COMPILER_PB_UNSAFE_TYPE_OPERATION = PLUGIN_ID + ".compiler.problem.uncheckedTypeOperation"; //$NON-NLS-1$
    /**
     * Possible  configurable option ID.
     * @see CALEclipseCorePreferenceInitializer#initializeDefaultPreferences()
     * @since 3.1
     */
    public static final String COMPILER_PB_UNCHECKED_TYPE_OPERATION = PLUGIN_ID + ".compiler.problem.uncheckedTypeOperation"; //$NON-NLS-1$
    /**
     * Possible  configurable option ID.
     * @see CALEclipseCorePreferenceInitializer#initializeDefaultPreferences()
     * @since 3.1
     */
    public static final String COMPILER_PB_FINAL_PARAMETER_BOUND = PLUGIN_ID + ".compiler.problem.finalParameterBound"; //$NON-NLS-1$
    /**
     * Possible  configurable option ID.
     * @see CALEclipseCorePreferenceInitializer#initializeDefaultPreferences()
     * @since 3.1
     */
    public static final String COMPILER_PB_MISSING_SERIAL_VERSION = PLUGIN_ID + ".compiler.problem.missingSerialVersion"; //$NON-NLS-1$
    /**
     * Possible  configurable option ID.
     * @see CALEclipseCorePreferenceInitializer#initializeDefaultPreferences()
     * @since 3.1
     */
    public static final String COMPILER_PB_VARARGS_ARGUMENT_NEED_CAST = PLUGIN_ID + ".compiler.problem.varargsArgumentNeedCast"; //$NON-NLS-1$
    /**
     * Possible  configurable option ID.
     * @see CALEclipseCorePreferenceInitializer#initializeDefaultPreferences()
     * @since 3.1
     */
    public static final String COMPILER_PB_AUTOBOXING = PLUGIN_ID + ".compiler.problem.autoboxing"; //$NON-NLS-1$
    /**
     * Possible  configurable option ID.
     * @see CALEclipseCorePreferenceInitializer#initializeDefaultPreferences()
     * @since 3.1
     */
    public static final String COMPILER_PB_ANNOTATION_SUPER_INTERFACE = PLUGIN_ID + ".compiler.problem.annotationSuperInterface"; //$NON-NLS-1$
    /**
     * Possible  configurable option ID.
     * @see CALEclipseCorePreferenceInitializer#initializeDefaultPreferences()
     * @since 3.1
     */
    public static final String COMPILER_PB_MISSING_OVERRIDE_ANNOTATION = PLUGIN_ID + ".compiler.problem.missingOverrideAnnotation"; //$NON-NLS-1$
    /**
     * Possible  configurable option ID.
     * @see CALEclipseCorePreferenceInitializer#initializeDefaultPreferences()
     * @since 3.1
     */
    public static final String COMPILER_PB_MISSING_DEPRECATED_ANNOTATION = PLUGIN_ID + ".compiler.problem.missingDeprecatedAnnotation"; //$NON-NLS-1$
    /**
     * Possible  configurable option ID.
     * @see CALEclipseCorePreferenceInitializer#initializeDefaultPreferences()
     * @since 3.1
     */
    public static final String COMPILER_PB_INCOMPLETE_ENUM_SWITCH = PLUGIN_ID + ".compiler.problem.incompleteEnumSwitch"; //$NON-NLS-1$
    /**
     * Possible  configurable option ID.
     * @see CALEclipseCorePreferenceInitializer#initializeDefaultPreferences()
     * @since 3.1
     */
    public static final String COMPILER_PB_INCONSISTENT_NULL_CHECK = PLUGIN_ID + ".compiler.problem.inconsistentNullCheck"; //$NON-NLS-1$
    /**
     * Possible  configurable option ID.
     * @see CALEclipseCorePreferenceInitializer#initializeDefaultPreferences()
     * @since 3.0
     */
    public static final String COMPILER_PB_INVALID_JAVADOC = PLUGIN_ID + ".compiler.problem.invalidJavadoc"; //$NON-NLS-1$
    /**
     * Possible  configurable option ID.
     * @see CALEclipseCorePreferenceInitializer#initializeDefaultPreferences()
     * @since 3.0
     */
    public static final String COMPILER_PB_INVALID_JAVADOC_TAGS = PLUGIN_ID + ".compiler.problem.invalidJavadocTags"; //$NON-NLS-1$
    /**
     * Possible  configurable option ID.
     * @see CALEclipseCorePreferenceInitializer#initializeDefaultPreferences()
     * @since 3.1
     */
    public static final String COMPILER_PB_INVALID_JAVADOC_TAGS__DEPRECATED_REF = PLUGIN_ID + ".compiler.problem.invalidJavadocTagsDeprecatedRef"; //$NON-NLS-1$
    /**
     * Possible  configurable option ID.
     * @see CALEclipseCorePreferenceInitializer#initializeDefaultPreferences()
     * @since 3.1
     */
    public static final String COMPILER_PB_INVALID_JAVADOC_TAGS__NOT_VISIBLE_REF = PLUGIN_ID + ".compiler.problem.invalidJavadocTagsNotVisibleRef"; //$NON-NLS-1$
    /**
     * Possible  configurable option ID.
     * @see CALEclipseCorePreferenceInitializer#initializeDefaultPreferences()
     * @since 3.0
     */
    public static final String COMPILER_PB_INVALID_JAVADOC_TAGS_VISIBILITY = PLUGIN_ID + ".compiler.problem.invalidJavadocTagsVisibility"; //$NON-NLS-1$
    /**
     * Possible  configurable option ID.
     * @see CALEclipseCorePreferenceInitializer#initializeDefaultPreferences()
     * @since 3.0
     */
    public static final String COMPILER_PB_MISSING_JAVADOC_TAGS = PLUGIN_ID + ".compiler.problem.missingJavadocTags"; //$NON-NLS-1$
    /**
     * Possible  configurable option ID.
     * @see CALEclipseCorePreferenceInitializer#initializeDefaultPreferences()
     * @since 3.0
     */
    public static final String COMPILER_PB_MISSING_JAVADOC_TAGS_VISIBILITY = PLUGIN_ID + ".compiler.problem.missingJavadocTagsVisibility"; //$NON-NLS-1$
    /**
     * Possible  configurable option ID.
     * @see CALEclipseCorePreferenceInitializer#initializeDefaultPreferences()
     * @since 3.0
     */
    public static final String COMPILER_PB_MISSING_JAVADOC_TAGS_OVERRIDING = PLUGIN_ID + ".compiler.problem.missingJavadocTagsOverriding"; //$NON-NLS-1$
    /**
     * Possible  configurable option ID.
     * @see CALEclipseCorePreferenceInitializer#initializeDefaultPreferences()
     * @since 3.0
     */
    public static final String COMPILER_PB_MISSING_JAVADOC_COMMENTS = PLUGIN_ID + ".compiler.problem.missingJavadocComments"; //$NON-NLS-1$
    /**
     * Possible  configurable option ID.
     * @see CALEclipseCorePreferenceInitializer#initializeDefaultPreferences()
     * @since 3.0
     */
    public static final String COMPILER_PB_MISSING_JAVADOC_COMMENTS_VISIBILITY = PLUGIN_ID + ".compiler.problem.missingJavadocCommentsVisibility"; //$NON-NLS-1$
    /**
     * Possible  configurable option ID.
     * @see CALEclipseCorePreferenceInitializer#initializeDefaultPreferences()
     * @since 3.0
     */
    public static final String COMPILER_PB_MISSING_JAVADOC_COMMENTS_OVERRIDING = PLUGIN_ID + ".compiler.problem.missingJavadocCommentsOverriding"; //$NON-NLS-1$
    /**
     * Possible  configurable option ID.
     * @see CALEclipseCorePreferenceInitializer#initializeDefaultPreferences()
     * @since 2.1
     */
    public static final String COMPILER_PB_CHAR_ARRAY_IN_STRING_CONCATENATION = PLUGIN_ID + ".compiler.problem.noImplicitStringConversion"; //$NON-NLS-1$
    /**
     * Possible  configurable option ID.
     * @see CALEclipseCorePreferenceInitializer#initializeDefaultPreferences()
     * @since 2.0
     */
    public static final String COMPILER_PB_MAX_PER_UNIT = PLUGIN_ID + ".compiler.maxProblemPerUnit"; //$NON-NLS-1$
    /**
     * Possible  configurable option ID.
     * @see CALEclipseCorePreferenceInitializer#initializeDefaultPreferences()
     * @since 2.0
     */
    public static final String COMPILER_SOURCE = PLUGIN_ID + ".compiler.source"; //$NON-NLS-1$
    /**
     * Possible  configurable option ID.
     * @see CALEclipseCorePreferenceInitializer#initializeDefaultPreferences()
     * @since 2.0
     */
    public static final String COMPILER_COMPLIANCE = PLUGIN_ID + ".compiler.compliance"; //$NON-NLS-1$
    /**
     * Possible  configurable option ID.
     * @see CALEclipseCorePreferenceInitializer#initializeDefaultPreferences()
     * @since 2.1
     */
    public static final String COMPILER_TASK_PRIORITIES = PLUGIN_ID + ".compiler.taskPriorities"; //$NON-NLS-1$
    /**
     * Possible  configurable option value for COMPILER_TASK_PRIORITIES.
     * @see CALEclipseCorePreferenceInitializer#initializeDefaultPreferences()
     * @since 2.1
     */
    public static final String COMPILER_TASK_PRIORITY_HIGH = "HIGH"; //$NON-NLS-1$
    /**
     * Possible  configurable option value for COMPILER_TASK_PRIORITIES.
     * @see CALEclipseCorePreferenceInitializer#initializeDefaultPreferences()
     * @since 2.1
     */
    public static final String COMPILER_TASK_PRIORITY_LOW = "LOW"; //$NON-NLS-1$
    /**
     * Possible  configurable option value for COMPILER_TASK_PRIORITIES.
     * @see CALEclipseCorePreferenceInitializer#initializeDefaultPreferences()
     * @since 2.1
     */
    public static final String COMPILER_TASK_PRIORITY_NORMAL = "NORMAL"; //$NON-NLS-1$
    /**
     * Possible  configurable option ID.
     * @see CALEclipseCorePreferenceInitializer#initializeDefaultPreferences()
     * @since 2.1
     */
    public static final String COMPILER_TASK_TAGS = PLUGIN_ID + ".compiler.taskTags"; //$NON-NLS-1$
    /**
     * Possible  configurable option ID.
     * @see CALEclipseCorePreferenceInitializer#initializeDefaultPreferences()
     * @since 3.0
     */
    public static final String COMPILER_TASK_CASE_SENSITIVE = PLUGIN_ID + ".compiler.taskCaseSensitive"; //$NON-NLS-1$      
    /**
     * Possible  configurable option ID.
     * @see CALEclipseCorePreferenceInitializer#initializeDefaultPreferences()
     * @since 3.1
     */
    public static final String COMPILER_PB_FORBIDDEN_REFERENCE = PLUGIN_ID + ".compiler.problem.forbiddenReference"; //$NON-NLS-1$
    /**
     * Possible  configurable option ID.
     * @see CALEclipseCorePreferenceInitializer#initializeDefaultPreferences()
     * @since 3.1
     */
    public static final String COMPILER_PB_DISCOURAGED_REFERENCE = PLUGIN_ID + ".compiler.problem.discouragedReference"; //$NON-NLS-1$
    /**
     * Possible  configurable option ID.
     * @see CALEclipseCorePreferenceInitializer#initializeDefaultPreferences()
     * @since 3.1
     */
    public static final String COMPILER_PB_SUPPRESS_WARNINGS = PLUGIN_ID + ".compiler.problem.suppressWarnings"; //$NON-NLS-1$
    /**
     * Possible  configurable option ID.
     * @see CALEclipseCorePreferenceInitializer#initializeDefaultPreferences()
     * @since 3.1
     */
    public static final String COMPILER_PB_UNHANDLED_WARNING_TOKEN = PLUGIN_ID + ".compiler.problem.unhandledWarningToken"; //$NON-NLS-1$
    /**
     * Possible  configurable option ID.
     * @see CALEclipseCorePreferenceInitializer#initializeDefaultPreferences()
     */
    public static final String CORE_JAVA_BUILD_ORDER = PLUGIN_ID + ".computeJavaBuildOrder"; //$NON-NLS-1$
    /**
     * Possible  configurable option ID.
     * @see CALEclipseCorePreferenceInitializer#initializeDefaultPreferences()
     * @since 2.0
     */
    public static final String CORE_JAVA_BUILD_RESOURCE_COPY_FILTER = PLUGIN_ID + ".builder.resourceCopyExclusionFilter"; //$NON-NLS-1$
    /**
     * Possible  configurable option ID.
     * @see CALEclipseCorePreferenceInitializer#initializeDefaultPreferences()
     * @since 2.1
     */
    public static final String CORE_JAVA_BUILD_DUPLICATE_RESOURCE = PLUGIN_ID + ".builder.duplicateResourceTask"; //$NON-NLS-1$
    /**
     * Possible  configurable option ID.
     * @see CALEclipseCorePreferenceInitializer#initializeDefaultPreferences()
     * @since 2.1
     */
    public static final String CORE_JAVA_BUILD_CLEAN_OUTPUT_FOLDER = PLUGIN_ID + ".builder.cleanOutputFolder"; //$NON-NLS-1$                
    /**
     * Possible  configurable option ID.
     * @see CALEclipseCorePreferenceInitializer#initializeDefaultPreferences()
     * @since 2.1
     */
    public static final String CORE_INCOMPLETE_CLASSPATH = PLUGIN_ID + ".incompleteClasspath"; //$NON-NLS-1$
    /**
     * Possible  configurable option ID.
     * @see CALEclipseCorePreferenceInitializer#initializeDefaultPreferences()
     * @since 2.1
     */
    public static final String CORE_CIRCULAR_CLASSPATH = PLUGIN_ID + ".circularClasspath"; //$NON-NLS-1$
    /**
     * Possible  configurable option ID.
     * @see CALEclipseCorePreferenceInitializer#initializeDefaultPreferences()
     * @since 3.0
     */
    public static final String CORE_INCOMPATIBLE_JDK_LEVEL = PLUGIN_ID + ".incompatibleJDKLevel"; //$NON-NLS-1$
    /**
     * Possible  configurable option ID.
     * @see CALEclipseCorePreferenceInitializer#initializeDefaultPreferences()
     * @since 2.0
     */
    public static final String CORE_JAVA_BUILD_INVALID_CLASSPATH = PLUGIN_ID + ".builder.invalidClasspath"; //$NON-NLS-1$
    /**
     * Possible  configurable option ID.
     * @see CALEclipseCorePreferenceInitializer#initializeDefaultPreferences()
     * @since 2.0
     */
    public static final String CORE_ENCODING = PLUGIN_ID + ".encoding"; //$NON-NLS-1$
    /**
     * Possible  configurable option ID.
     * @see CALEclipseCorePreferenceInitializer#initializeDefaultPreferences()
     * @since 2.1 
     */
    public static final String CORE_ENABLE_CLASSPATH_EXCLUSION_PATTERNS = PLUGIN_ID + ".classpath.exclusionPatterns"; //$NON-NLS-1$
    /**
     * Possible  configurable option ID.
     * @see CALEclipseCorePreferenceInitializer#initializeDefaultPreferences()
     * @since 2.1
     */
    public static final String CORE_ENABLE_CLASSPATH_MULTIPLE_OUTPUT_LOCATIONS = PLUGIN_ID + ".classpath.multipleOutputLocations"; //$NON-NLS-1$
    /**
     * Default task tag
     * @deprecated - should use #DEFAULT_TASK_TAGS instead 
     * @since 2.1
     */
    @Deprecated
    public static final String DEFAULT_TASK_TAG = "TODO"; //$NON-NLS-1$
    /**
     * Default task priority
     * @deprecated - should use #DEFAULT_TASK_PRIORITIES instead 
     * @since 2.1
     */
    @Deprecated
    public static final String DEFAULT_TASK_PRIORITY = "NORMAL"; //$NON-NLS-1$
    /**
     * Default task tag
     * @since 3.0
     */
    public static final String DEFAULT_TASK_TAGS = "TODO,FIXME,XXX"; //$NON-NLS-1$
    /**
     * Default task priority
     * @since 3.0
     */
    public static final String DEFAULT_TASK_PRIORITIES = "NORMAL,HIGH,NORMAL"; //$NON-NLS-1$
    /**
     * Possible  configurable option ID.
     * @see CALEclipseCorePreferenceInitializer#initializeDefaultPreferences()
     * @since 2.0
     * @deprecated Use {@link org.openquark.cal.eclipse.core.formatter.DefaultCodeFormatterConstants#FORMATTER_BRACE_POSITION_FOR_ANONYMOUS_TYPE_DECLARATION},
     * {@link org.openquark.cal.eclipse.core.formatter.DefaultCodeFormatterConstants#FORMATTER_BRACE_POSITION_FOR_BLOCK} ,
     * {@link org.openquark.cal.eclipse.core.formatter.DefaultCodeFormatterConstants#FORMATTER_BRACE_POSITION_FOR_CONSTRUCTOR_DECLARATION},
     * {@link org.openquark.cal.eclipse.core.formatter.DefaultCodeFormatterConstants#FORMATTER_BRACE_POSITION_FOR_METHOD_DECLARATION},
     * {@link org.openquark.cal.eclipse.core.formatter.DefaultCodeFormatterConstants#FORMATTER_BRACE_POSITION_FOR_SWITCH},
     * {@link org.openquark.cal.eclipse.core.formatter.DefaultCodeFormatterConstants#FORMATTER_BRACE_POSITION_FOR_TYPE_DECLARATION} instead
     */
    @Deprecated
    public static final String FORMATTER_NEWLINE_OPENING_BRACE = PLUGIN_ID + ".formatter.newline.openingBrace"; //$NON-NLS-1$
    /**
     * Possible  configurable option ID.
     * @see CALEclipseCorePreferenceInitializer#initializeDefaultPreferences()
     * @since 2.0
     * @deprecated Use {@link org.openquark.cal.eclipse.core.formatter.DefaultCodeFormatterConstants#FORMATTER_INSERT_NEW_LINE_BEFORE_CATCH_IN_TRY_STATEMENT},
     *  {@link org.openquark.cal.eclipse.core.formatter.DefaultCodeFormatterConstants#FORMATTER_INSERT_NEW_LINE_BEFORE_ELSE_IN_IF_STATEMENT},
     *  {@link org.openquark.cal.eclipse.core.formatter.DefaultCodeFormatterConstants#FORMATTER_INSERT_NEW_LINE_BEFORE_FINALLY_IN_TRY_STATEMENT},
     *  {@link org.openquark.cal.eclipse.core.formatter.DefaultCodeFormatterConstants#FORMATTER_INSERT_NEW_LINE_BEFORE_WHILE_IN_DO_STATEMENT} instead.
     */
    @Deprecated
    public static final String FORMATTER_NEWLINE_CONTROL = PLUGIN_ID + ".formatter.newline.controlStatement"; //$NON-NLS-1$
    /**
     * Possible  configurable option ID.
     * @see CALEclipseCorePreferenceInitializer#initializeDefaultPreferences()
     * @since 2.0
     * @deprecated Use {@link org.openquark.cal.eclipse.core.formatter.DefaultCodeFormatterConstants#FORMATTER_COMPACT_ELSE_IF} instead
     */
    @Deprecated
    public static final String FORMATTER_NEWLINE_ELSE_IF = PLUGIN_ID + ".formatter.newline.elseIf"; //$NON-NLS-1$
    /**
     * Possible  configurable option ID.
     * @see CALEclipseCorePreferenceInitializer#initializeDefaultPreferences()
     * @since 2.0
     * @deprecated Use {@link org.openquark.cal.eclipse.core.formatter.DefaultCodeFormatterConstants#FORMATTER_INSERT_NEW_LINE_IN_EMPTY_BLOCK} instead
     */
    @Deprecated
    public static final String FORMATTER_NEWLINE_EMPTY_BLOCK = PLUGIN_ID + ".formatter.newline.emptyBlock"; //$NON-NLS-1$
    /**
     * Possible  configurable option ID.
     * @see CALEclipseCorePreferenceInitializer#initializeDefaultPreferences()
     * @since 2.0
     * @deprecated Use {@link org.openquark.cal.eclipse.core.formatter.DefaultCodeFormatterConstants#FORMATTER_NUMBER_OF_EMPTY_LINES_TO_PRESERVE} instead
     */
    @Deprecated
    public static final String FORMATTER_CLEAR_BLANK_LINES = PLUGIN_ID + ".formatter.newline.clearAll"; //$NON-NLS-1$
    /**
     * Possible  configurable option ID.
     * @see CALEclipseCorePreferenceInitializer#initializeDefaultPreferences()
     * @since 2.0
     * @deprecated Use {@link org.openquark.cal.eclipse.core.formatter.DefaultCodeFormatterConstants#FORMATTER_LINE_SPLIT} instead
     */
    @Deprecated
    public static final String FORMATTER_LINE_SPLIT = PLUGIN_ID + ".formatter.lineSplit"; //$NON-NLS-1$
    /**
     * Possible  configurable option ID.
     * @see CALEclipseCorePreferenceInitializer#initializeDefaultPreferences()
     * @since 2.0
     * @deprecated Use {@link org.openquark.cal.eclipse.core.formatter.DefaultCodeFormatterConstants#FORMATTER_INSERT_SPACE_BEFORE_ASSIGNMENT_OPERATOR} instead
     */
    @Deprecated
    public static final String FORMATTER_COMPACT_ASSIGNMENT = PLUGIN_ID + ".formatter.style.assignment"; //$NON-NLS-1$
//    /**
//     * Possible  configurable option ID.
//     * @see CALEclipseCorePreferenceInitializer#initializeDefaultPreferences()
//     * @since 2.0
//     * @deprecated Use {@link org.openquark.cal.eclipse.core.formatter.DefaultCodeFormatterConstants#FORMATTER_TAB_CHAR}} instead
//     */
//    public static final String FORMATTER_TAB_CHAR = PLUGIN_ID + ".formatter.tabulation.char"; //$NON-NLS-1$
//    /**
//     * Possible  configurable option ID.
//     * @see CALEclipseCorePreferenceInitializer#initializeDefaultPreferences()
//     * @since 2.0
//     * @deprecated Use {@link org.openquark.cal.eclipse.core.formatter.DefaultCodeFormatterConstants#FORMATTER_TAB_SIZE} instead
//     */
//    public static final String FORMATTER_TAB_SIZE = PLUGIN_ID + ".formatter.tabulation.size"; //$NON-NLS-1$
    /**
     * Possible configurable option ID
     * @see CALEclipseCorePreferenceInitializer#initializeDefaultPreferences()
     * @since 2.1
     * @deprecated Use {@link org.openquark.cal.eclipse.core.formatter.DefaultCodeFormatterConstants#FORMATTER_INSERT_SPACE_AFTER_CLOSING_PAREN_IN_CAST} instead
     */
    @Deprecated
    public static final String FORMATTER_SPACE_CASTEXPRESSION = PLUGIN_ID + ".formatter.space.castexpression"; //$NON-NLS-1$
    /**
     * Possible  configurable option ID.
     * @see CALEclipseCorePreferenceInitializer#initializeDefaultPreferences()
     * @since 2.0
     */
    public static final String CODEASSIST_VISIBILITY_CHECK = PLUGIN_ID + ".codeComplete.visibilityCheck"; //$NON-NLS-1$
    /**
     * Possible  configurable option ID.
     * @see CALEclipseCorePreferenceInitializer#initializeDefaultPreferences()
     * @since 2.0
     */
    public static final String CODEASSIST_IMPLICIT_QUALIFICATION = PLUGIN_ID + ".codeComplete.forceImplicitQualification"; //$NON-NLS-1$
    /**
     * Possible  configurable option ID.
     * @see CALEclipseCorePreferenceInitializer#initializeDefaultPreferences()
     * @since 2.1
     */
    public static final String CODEASSIST_FIELD_PREFIXES = PLUGIN_ID + ".codeComplete.fieldPrefixes"; //$NON-NLS-1$
    /**
     * Possible  configurable option ID.
     * @see CALEclipseCorePreferenceInitializer#initializeDefaultPreferences()
     * @since 2.1
     */
    public static final String CODEASSIST_STATIC_FIELD_PREFIXES = PLUGIN_ID + ".codeComplete.staticFieldPrefixes"; //$NON-NLS-1$
    /**
     * Possible  configurable option ID.
     * @see CALEclipseCorePreferenceInitializer#initializeDefaultPreferences()
     * @since 2.1
     */
    public static final String CODEASSIST_LOCAL_PREFIXES = PLUGIN_ID + ".codeComplete.localPrefixes"; //$NON-NLS-1$
    /**
     * Possible  configurable option ID.
     * @see CALEclipseCorePreferenceInitializer#initializeDefaultPreferences()
     * @since 2.1
     */
    public static final String CODEASSIST_ARGUMENT_PREFIXES = PLUGIN_ID + ".codeComplete.argumentPrefixes"; //$NON-NLS-1$
    /**
     * Possible  configurable option ID.
     * @see CALEclipseCorePreferenceInitializer#initializeDefaultPreferences()
     * @since 2.1
     */
    public static final String CODEASSIST_FIELD_SUFFIXES = PLUGIN_ID + ".codeComplete.fieldSuffixes"; //$NON-NLS-1$
    /**
     * Possible  configurable option ID.
     * @see CALEclipseCorePreferenceInitializer#initializeDefaultPreferences()
     * @since 2.1
     */
    public static final String CODEASSIST_STATIC_FIELD_SUFFIXES = PLUGIN_ID + ".codeComplete.staticFieldSuffixes"; //$NON-NLS-1$
    /**
     * Possible  configurable option ID.
     * @see CALEclipseCorePreferenceInitializer#initializeDefaultPreferences()
     * @since 2.1
     */
    public static final String CODEASSIST_LOCAL_SUFFIXES = PLUGIN_ID + ".codeComplete.localSuffixes"; //$NON-NLS-1$
    /**
     * Possible  configurable option ID.
     * @see CALEclipseCorePreferenceInitializer#initializeDefaultPreferences()
     * @since 2.1
     */
    public static final String CODEASSIST_ARGUMENT_SUFFIXES = PLUGIN_ID + ".codeComplete.argumentSuffixes"; //$NON-NLS-1$
    /**
     * Possible  configurable option ID.
     * @see CALEclipseCorePreferenceInitializer#initializeDefaultPreferences()
     * @since 3.1
     */
    public static final String CODEASSIST_FORBIDDEN_REFERENCE_CHECK= PLUGIN_ID + ".codeComplete.forbiddenReferenceCheck"; //$NON-NLS-1$
    /**
     * Possible  configurable option ID.
     * @see CALEclipseCorePreferenceInitializer#initializeDefaultPreferences()
     * @since 3.1
     */
    public static final String CODEASSIST_DISCOURAGED_REFERENCE_CHECK= PLUGIN_ID + ".codeComplete.discouragedReferenceCheck"; //$NON-NLS-1$
    
    // *************** Possible values for configurable options. ********************
    
    /**
     * Possible  configurable option value.
     * @see CALEclipseCorePreferenceInitializer#initializeDefaultPreferences()
     */
    public static final String GENERATE = "generate"; //$NON-NLS-1$
    /**
     * Possible  configurable option value.
     * @see CALEclipseCorePreferenceInitializer#initializeDefaultPreferences()
     */
    public static final String DO_NOT_GENERATE = "do not generate"; //$NON-NLS-1$
    /**
     * Possible  configurable option value.
     * @see CALEclipseCorePreferenceInitializer#initializeDefaultPreferences()
     */
    public static final String PRESERVE = "preserve"; //$NON-NLS-1$
    /**
     * Possible  configurable option value.
     * @see CALEclipseCorePreferenceInitializer#initializeDefaultPreferences()
     */
    public static final String OPTIMIZE_OUT = "optimize out"; //$NON-NLS-1$
    /**
     * Possible  configurable option value.
     * @see CALEclipseCorePreferenceInitializer#initializeDefaultPreferences()
     */
    public static final String VERSION_1_1 = "1.1"; //$NON-NLS-1$
    /**
     * Possible  configurable option value.
     * @see CALEclipseCorePreferenceInitializer#initializeDefaultPreferences()
     */
    public static final String VERSION_1_2 = "1.2"; //$NON-NLS-1$
    /**
     * Possible  configurable option value.
     * @see CALEclipseCorePreferenceInitializer#initializeDefaultPreferences()
     * @since 2.0
     */
    public static final String VERSION_1_3 = "1.3"; //$NON-NLS-1$
    /**
     * Possible  configurable option value.
     * @see CALEclipseCorePreferenceInitializer#initializeDefaultPreferences()
     * @since 2.0
     */
    public static final String VERSION_1_4 = "1.4"; //$NON-NLS-1$
    /**
     * Possible  configurable option value.
     * @see CALEclipseCorePreferenceInitializer#initializeDefaultPreferences()
     * @since 3.0
     */
    public static final String VERSION_1_5 = "1.5"; //$NON-NLS-1$
    /**
     * Possible  configurable option value.
     * @see CALEclipseCorePreferenceInitializer#initializeDefaultPreferences()
     * @since 2.0
     */
    public static final String ABORT = "abort"; //$NON-NLS-1$
    /**
     * Possible  configurable option value.
     * @see CALEclipseCorePreferenceInitializer#initializeDefaultPreferences()
     */
    public static final String ERROR = "error"; //$NON-NLS-1$
    /**
     * Possible  configurable option value.
     * @see CALEclipseCorePreferenceInitializer#initializeDefaultPreferences()
     */
    public static final String WARNING = "warning"; //$NON-NLS-1$
    /**
     * Possible  configurable option value.
     * @see CALEclipseCorePreferenceInitializer#initializeDefaultPreferences()
     */
    public static final String IGNORE = "ignore"; //$NON-NLS-1$
    /**
     * Possible  configurable option value.
     * @see CALEclipseCorePreferenceInitializer#initializeDefaultPreferences()
     */
    public static final String COMPUTE = "compute"; //$NON-NLS-1$
    /**
     * Possible  configurable option value.
     * @see CALEclipseCorePreferenceInitializer#initializeDefaultPreferences()
     * @since 2.0
     */
    public static final String INSERT = "insert"; //$NON-NLS-1$
    /**
     * Possible  configurable option value.
     * @see CALEclipseCorePreferenceInitializer#initializeDefaultPreferences()
     * @since 2.0
     */
    public static final String DO_NOT_INSERT = "do not insert"; //$NON-NLS-1$
    /**
     * Possible  configurable option value.
     * @see CALEclipseCorePreferenceInitializer#initializeDefaultPreferences()
     * @since 2.0
     */
    public static final String PRESERVE_ONE = "preserve one"; //$NON-NLS-1$
    /**
     * Possible  configurable option value.
     * @see CALEclipseCorePreferenceInitializer#initializeDefaultPreferences()
     * @since 2.0
     */
    public static final String CLEAR_ALL = "clear all"; //$NON-NLS-1$
    /**
     * Possible  configurable option value.
     * @see CALEclipseCorePreferenceInitializer#initializeDefaultPreferences()
     * @since 2.0
     */
    public static final String NORMAL = "normal"; //$NON-NLS-1$
    /**
     * Possible  configurable option value.
     * @see CALEclipseCorePreferenceInitializer#initializeDefaultPreferences()
     * @since 2.0
     */
    public static final String COMPACT = "compact"; //$NON-NLS-1$
    /**
     * Possible  configurable option value.
     * @see CALEclipseCorePreferenceInitializer#initializeDefaultPreferences()
     * @since 2.0
     */
    public static final String TAB = "tab"; //$NON-NLS-1$
    /**
     * Possible  configurable option value.
     * @see CALEclipseCorePreferenceInitializer#initializeDefaultPreferences()
     * @since 2.0
     */
    public static final String SPACE = "space"; //$NON-NLS-1$
    /**
     * Possible  configurable option value.
     * @see CALEclipseCorePreferenceInitializer#initializeDefaultPreferences()
     * @since 2.0
     */
    public static final String ENABLED = "enabled"; //$NON-NLS-1$
    /**
     * Possible  configurable option value.
     * @see CALEclipseCorePreferenceInitializer#initializeDefaultPreferences()
     * @since 2.0
     */
    public static final String DISABLED = "disabled"; //$NON-NLS-1$
    /**
     * Possible  configurable option value.
     * @see CALEclipseCorePreferenceInitializer#initializeDefaultPreferences()
     * @since 2.1
     */
    public static final String CLEAN = "clean"; //$NON-NLS-1$
    /**
     * Possible  configurable option value.
     * @see CALEclipseCorePreferenceInitializer#initializeDefaultPreferences()
     * @since 3.0
     */
    public static final String PUBLIC = "public"; //$NON-NLS-1$
    /**
     * Possible  configurable option value.
     * @see CALEclipseCorePreferenceInitializer#initializeDefaultPreferences()
     * @since 3.0
     */
    public static final String PROTECTED = "protected"; //$NON-NLS-1$
    /**
     * Possible  configurable option value.
     * @see CALEclipseCorePreferenceInitializer#initializeDefaultPreferences()
     * @since 3.0
     */
    public static final String DEFAULT = "default"; //$NON-NLS-1$
    /**
     * Possible  configurable option value.
     * @see CALEclipseCorePreferenceInitializer#initializeDefaultPreferences()
     * @since 3.0
     */
    public static final String PRIVATE = "private"; //$NON-NLS-1$
    /**
     * Possible  configurable option value.
     * @see CALEclipseCorePreferenceInitializer#initializeDefaultPreferences()
     * @since 3.1
     */
    public static final String NEVER = "never"; //$NON-NLS-1$
    
}
