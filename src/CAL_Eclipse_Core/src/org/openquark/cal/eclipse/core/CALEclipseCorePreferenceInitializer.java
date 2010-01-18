/*******************************************************************************
 * Copyright (c) 2006 Business Objects Software Limited and others.
 * All rights reserved. 
 * This file is made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Business Objects Software Limited - initial API and implementation based on Eclipse 3.1.2 code for
 *                             /org.eclipse.jdt.core/model/org/eclipse/jdt/internal/core/JavaCorePreferenceInitializer.java
 *                           Eclipse source is available at: http://www.eclipse.org/downloads/
 *******************************************************************************/

/*
 * CALEclipseCorePreferenceInitializer.java
 * Creation date: Feb 14, 2006.
 * By: Edward Lam
 */
package org.openquark.cal.eclipse.core;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.core.runtime.preferences.AbstractPreferenceInitializer;
import org.eclipse.core.runtime.preferences.DefaultScope;
import org.eclipse.core.runtime.preferences.IEclipsePreferences;
import org.openquark.cal.eclipse.core.formatter.DefaultCodeFormatterConstants;


/**
 * CALEclipseCore eclipse preferences initializer.
 * Initially done in JavaCore.initializeDefaultPreferences which was deprecated
 * with new eclipse preferences mechanism.
 * @author Edward Lam
 */
public class CALEclipseCorePreferenceInitializer extends AbstractPreferenceInitializer {
    /*
     * Note: many options are set by CompilerOptions.
     * Many of the string constants contained in CompilerOptions are identical to those in JavaCore/CoreOptionIDs.
     * eg. CoreOptionIDs.COMPILER_PB_MAX_PER_UNIT is the same as CompilerOptions.OPTION_MaxProblemPerUnit.
     */
    
    /* (non-Javadoc)
     * @see org.eclipse.core.runtime.preferences.AbstractPreferenceInitializer#initializeDefaultPreferences()
     */
    @Override
    public void initializeDefaultPreferences() {
        
        // Get options names set
        CALModelManager calModelManager = CALModelManager.getCALModelManager();
        
        // Compiler settings
//        Map defaultOptionsMap = new CompilerOptions().getMap(); // compiler defaults
        Map<String, String> defaultOptionsMap = new HashMap<String, String>();
        
        /*
         * TEMP
         */
        
        // Enable the cal builder
        defaultOptionsMap.put(CoreOptionIDs.CORE_CAL_BUILD_ENABLE, CoreOptionIDs.ENABLED); 
        
        // Should be set by CompilerOptions.
        final int maxProblemsPerUnit = 100;
        defaultOptionsMap.put(CoreOptionIDs.COMPILER_PB_MAX_PER_UNIT, String.valueOf(maxProblemsPerUnit)); 
        
        /*
         * Copied
         */
        // Override some compiler defaults
        defaultOptionsMap.put(CoreOptionIDs.COMPILER_LOCAL_VARIABLE_ATTR, CoreOptionIDs.GENERATE);
        defaultOptionsMap.put(CoreOptionIDs.COMPILER_CODEGEN_UNUSED_LOCAL, CoreOptionIDs.PRESERVE);
        defaultOptionsMap.put(CoreOptionIDs.COMPILER_TASK_TAGS, CoreOptionIDs.DEFAULT_TASK_TAGS);
        defaultOptionsMap.put(CoreOptionIDs.COMPILER_TASK_PRIORITIES, CoreOptionIDs.DEFAULT_TASK_PRIORITIES);
        defaultOptionsMap.put(CoreOptionIDs.COMPILER_TASK_CASE_SENSITIVE, CoreOptionIDs.ENABLED);
        defaultOptionsMap.put(CoreOptionIDs.COMPILER_DOC_COMMENT_SUPPORT, CoreOptionIDs.ENABLED);
        defaultOptionsMap.put(CoreOptionIDs.COMPILER_PB_FORBIDDEN_REFERENCE, CoreOptionIDs.ERROR);
        
        // Builder settings
        defaultOptionsMap.put(CoreOptionIDs.CORE_JAVA_BUILD_RESOURCE_COPY_FILTER, ""); //$NON-NLS-1$
        defaultOptionsMap.put(CoreOptionIDs.CORE_JAVA_BUILD_INVALID_CLASSPATH, CoreOptionIDs.ABORT); 
        defaultOptionsMap.put(CoreOptionIDs.CORE_JAVA_BUILD_DUPLICATE_RESOURCE, CoreOptionIDs.WARNING); 
        defaultOptionsMap.put(CoreOptionIDs.CORE_JAVA_BUILD_CLEAN_OUTPUT_FOLDER, CoreOptionIDs.CLEAN); 
        
        // CoreOptionIDs settings
        defaultOptionsMap.put(CoreOptionIDs.CORE_JAVA_BUILD_ORDER, CoreOptionIDs.IGNORE); 
        defaultOptionsMap.put(CoreOptionIDs.CORE_INCOMPLETE_CLASSPATH, CoreOptionIDs.ERROR); 
        defaultOptionsMap.put(CoreOptionIDs.CORE_CIRCULAR_CLASSPATH, CoreOptionIDs.ERROR); 
        defaultOptionsMap.put(CoreOptionIDs.CORE_INCOMPATIBLE_JDK_LEVEL, CoreOptionIDs.IGNORE); 
        defaultOptionsMap.put(CoreOptionIDs.CORE_ENABLE_CLASSPATH_EXCLUSION_PATTERNS, CoreOptionIDs.ENABLED); 
        defaultOptionsMap.put(CoreOptionIDs.CORE_ENABLE_CLASSPATH_MULTIPLE_OUTPUT_LOCATIONS, CoreOptionIDs.ENABLED); 
        
        // encoding setting comes from resource plug-in
        calModelManager.addOptionName(CoreOptionIDs.CORE_ENCODING);
        
        // Formatter settings
        Map<String, String> codeFormatterOptionsMap = DefaultCodeFormatterConstants.getDefaultSettings(); // code formatter defaults
        for (final Map.Entry<String, String> entry : codeFormatterOptionsMap.entrySet()) {
            String optionName = entry.getKey();
            defaultOptionsMap.put(optionName, entry.getValue());
            calModelManager.addOptionName(optionName);
        }
        
        // CodeAssist settings
        defaultOptionsMap.put(CoreOptionIDs.CODEASSIST_VISIBILITY_CHECK, CoreOptionIDs.DISABLED);
        defaultOptionsMap.put(CoreOptionIDs.CODEASSIST_IMPLICIT_QUALIFICATION, CoreOptionIDs.DISABLED);
        defaultOptionsMap.put(CoreOptionIDs.CODEASSIST_FIELD_PREFIXES, ""); //$NON-NLS-1$
        defaultOptionsMap.put(CoreOptionIDs.CODEASSIST_STATIC_FIELD_PREFIXES, ""); //$NON-NLS-1$
        defaultOptionsMap.put(CoreOptionIDs.CODEASSIST_LOCAL_PREFIXES, ""); //$NON-NLS-1$
        defaultOptionsMap.put(CoreOptionIDs.CODEASSIST_ARGUMENT_PREFIXES, ""); //$NON-NLS-1$
        defaultOptionsMap.put(CoreOptionIDs.CODEASSIST_FIELD_SUFFIXES, ""); //$NON-NLS-1$
        defaultOptionsMap.put(CoreOptionIDs.CODEASSIST_STATIC_FIELD_SUFFIXES, ""); //$NON-NLS-1$
        defaultOptionsMap.put(CoreOptionIDs.CODEASSIST_LOCAL_SUFFIXES, ""); //$NON-NLS-1$
        defaultOptionsMap.put(CoreOptionIDs.CODEASSIST_ARGUMENT_SUFFIXES, ""); //$NON-NLS-1$
        defaultOptionsMap.put(CoreOptionIDs.CODEASSIST_FORBIDDEN_REFERENCE_CHECK, CoreOptionIDs.ENABLED);
        defaultOptionsMap.put(CoreOptionIDs.CODEASSIST_DISCOURAGED_REFERENCE_CHECK, CoreOptionIDs.DISABLED);
        
        // Store default values to default preferences
        IEclipsePreferences defaultPreferences = new DefaultScope().getNode(CoreOptionIDs.PLUGIN_ID);
        for (final Map.Entry<String, String> entry : defaultOptionsMap.entrySet()) {
            String optionName = entry.getKey();
            defaultPreferences.put(optionName, entry.getValue());
            calModelManager.addOptionName(optionName);
        }
    }
}
