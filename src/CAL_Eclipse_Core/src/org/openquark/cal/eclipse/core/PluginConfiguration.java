/*******************************************************************************
 * Copyright (c) 2006 Business Objects Software Limited and others.
 * All rights reserved. 
 * This file is made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Business Objects Software Limited - initial API and implementation based on Eclipse 3.1.2 code for
 *                             /org.eclipse.jdt.core/model/org/eclipse/jdt/internal/core/JavaModelManager.java
 *                           Eclipse source is available at: http://www.eclipse.org/downloads/
 *******************************************************************************/

/*
 * PluginConfiguration.java
 * Creation date: Jan 30, 2006.
 * By: Edward Lam
 */
package org.openquark.cal.eclipse.core;

import java.util.HashSet;
import java.util.Hashtable;
import org.eclipse.core.runtime.PerformanceStats;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Preferences;
import org.eclipse.core.runtime.preferences.DefaultScope;
import org.eclipse.core.runtime.preferences.IEclipsePreferences;
import org.eclipse.core.runtime.preferences.IPreferencesService;
import org.eclipse.core.runtime.preferences.InstanceScope;


/**
 * This class holds configuration information for the CAL Eclipse Core plugin.
 * @author Edward Lam
 */
public class PluginConfiguration {
    /*
     * TODOEL: Currently pretty much only a placeholder.
     * Copied from JavaModelManager.
     */
    
    /*
     * Debug keys.
     * Configured using Platform.getDebugOption(key);
     */
    private static final String BUFFER_MANAGER_DEBUG = CALEclipseCorePlugin.PLUGIN_ID + "/debug/buffermanager" ; //$NON-NLS-1$
//    private static final String INDEX_MANAGER_DEBUG = CALEclipseCorePlugin.PLUGIN_ID + "/debug/indexmanager" ; //$NON-NLS-1$
//    private static final String COMPILER_DEBUG = CALEclipseCorePlugin.PLUGIN_ID + "/debug/compiler" ; //$NON-NLS-1$
    private static final String JAVAMODEL_DEBUG = CALEclipseCorePlugin.PLUGIN_ID + "/debug/javamodel" ; //$NON-NLS-1$
    private static final String CP_RESOLVE_DEBUG = CALEclipseCorePlugin.PLUGIN_ID + "/debug/cpresolution" ; //$NON-NLS-1$
    private static final String ZIP_ACCESS_DEBUG = CALEclipseCorePlugin.PLUGIN_ID + "/debug/zipaccess" ; //$NON-NLS-1$
//    private static final String DELTA_DEBUG =CALEclipseCorePlugin.PLUGIN_ID + "/debug/javadelta" ; //$NON-NLS-1$
//    private static final String DELTA_DEBUG_VERBOSE =CALEclipseCorePlugin.PLUGIN_ID + "/debug/javadelta/verbose" ; //$NON-NLS-1$
//    private static final String HIERARCHY_DEBUG = CALEclipseCorePlugin.PLUGIN_ID + "/debug/hierarchy" ; //$NON-NLS-1$
//    private static final String POST_ACTION_DEBUG = CALEclipseCorePlugin.PLUGIN_ID + "/debug/postaction" ; //$NON-NLS-1$
//    private static final String BUILDER_DEBUG = CALEclipseCorePlugin.PLUGIN_ID + "/debug/builder" ; //$NON-NLS-1$
//    private static final String COMPLETION_DEBUG = CALEclipseCorePlugin.PLUGIN_ID + "/debug/completion" ; //$NON-NLS-1$
//    private static final String RESOLUTION_DEBUG = CALEclipseCorePlugin.PLUGIN_ID + "/debug/resolution" ; //$NON-NLS-1$
//    private static final String SELECTION_DEBUG = CALEclipseCorePlugin.PLUGIN_ID + "/debug/selection" ; //$NON-NLS-1$
//    private static final String SEARCH_DEBUG = CALEclipseCorePlugin.PLUGIN_ID + "/debug/search" ; //$NON-NLS-1$
//    private static final String SOURCE_MAPPER_DEBUG_VERBOSE = CALEclipseCorePlugin.PLUGIN_ID + "/debug/sourcemapper" ; //$NON-NLS-1$
    
    /*
     * Performance debug keys.
     * Configured using PerformanceStats.isEnabled(COMPLETION_PERF);
     */
    public static final String COMPLETION_PERF = CALEclipseCorePlugin.PLUGIN_ID + "/perf/completion" ; //$NON-NLS-1$
    public static final String SELECTION_PERF = CALEclipseCorePlugin.PLUGIN_ID + "/perf/selection" ; //$NON-NLS-1$
    public static final String DELTA_LISTENER_PERF = CALEclipseCorePlugin.PLUGIN_ID + "/perf/javadeltalistener" ; //$NON-NLS-1$
    public static final String VARIABLE_INITIALIZER_PERF = CALEclipseCorePlugin.PLUGIN_ID + "/perf/variableinitializer" ; //$NON-NLS-1$
    public static final String CONTAINER_INITIALIZER_PERF = CALEclipseCorePlugin.PLUGIN_ID + "/perf/containerinitializer" ; //$NON-NLS-1$
    public static final String RECONCILE_PERF = CALEclipseCorePlugin.PLUGIN_ID + "/perf/reconcile" ; //$NON-NLS-1$
    
    /*
     * Performance debug values.
     */
    public static boolean PERF_VARIABLE_INITIALIZER = false;
    public static boolean PERF_CONTAINER_INITIALIZER = false;
    
    // Preferences
    private final HashSet<String> optionNames = new HashSet<String>(20);
    private Hashtable<String, String> optionsCache;
    
    public final IEclipsePreferences[] preferencesLookup = new IEclipsePreferences[2];
    static final int PREF_INSTANCE = 0;
    static final int PREF_DEFAULT = 1;
    
    public static boolean VERBOSE = false;
    public static boolean CP_RESOLVE_VERBOSE = false;
    public static boolean ZIP_ACCESS_VERBOSE = false;
    
    /**
     * Update variables associated with preferences.
     */
    public static class EclipsePreferencesListener implements IEclipsePreferences.IPreferenceChangeListener {
        /**
         * @see org.eclipse.core.runtime.preferences.IEclipsePreferences.IPreferenceChangeListener#preferenceChange(org.eclipse.core.runtime.preferences.IEclipsePreferences.PreferenceChangeEvent)
         */
        public void preferenceChange(IEclipsePreferences.PreferenceChangeEvent event) {
//            String propertyName = event.getKey();
//            if (propertyName.startsWith(CP_VARIABLE_PREFERENCES_PREFIX)) {
//                String varName = propertyName.substring(CP_VARIABLE_PREFERENCES_PREFIX.length());
//                String newValue = (String)event.getNewValue();
//                if (newValue != null && !(newValue = newValue.trim()).equals(CP_ENTRY_IGNORE)) {
//                    getCALModelManager().variables.put(varName, new Path(newValue));
//                } else {
//                    getCALModelManager().variables.remove(varName);
//                }
//            }
//            if (propertyName.startsWith(CP_CONTAINER_PREFERENCES_PREFIX)) {
//                recreatePersistedContainer(propertyName, (String)event.getNewValue(), false);
//            }
        }
    }
    
    PluginConfiguration() {
    }
    
    /** Nulls out the options cache when a property changes. */
    private Preferences.IPropertyChangeListener propertyListener = null;
    
    /**
     * Called by CALModelManager.startup().
     */
    public void startup() {
        configurePluginDebugOptions();
        
        // request state folder creation (workaround 19885)
        CALEclipseCorePlugin.getDefault().getStateLocation();
        
        // Initialize eclipse preferences
        initializePreferences();
        
        // Listen to preference changes
        propertyListener = new Preferences.IPropertyChangeListener() {

            public void propertyChange(Preferences.PropertyChangeEvent event) {
                PluginConfiguration.this.optionsCache = null;
            }
        };
        
        CALEclipseCorePlugin.getDefault().getPluginPreferences().addPropertyChangeListener(propertyListener);
    }
    
    /**
     * Called by CALModelManager.shutdown().
     */
    public void shutdown() {
        CALEclipseCorePlugin.getDefault().getPluginPreferences().removePropertyChangeListener(propertyListener);
    }
    
    /**
     * Configure the plugin with respect to option settings defined in ".options" file
     */
    private void configurePluginDebugOptions(){
        if (CALEclipseCorePlugin.getDefault().isDebugging()){
            String option = Platform.getDebugOption(BUFFER_MANAGER_DEBUG);
//            if (option != null) BufferManager.VERBOSE = option.equalsIgnoreCase("true") ; //$NON-NLS-1$
//            
//            option = Platform.getDebugOption(BUILDER_DEBUG);
//            if (option != null) JavaBuilder.DEBUG = option.equalsIgnoreCase("true") ; //$NON-NLS-1$
//            
//            option = Platform.getDebugOption(COMPILER_DEBUG);
//            if (option != null) Compiler.DEBUG = option.equalsIgnoreCase("true") ; //$NON-NLS-1$
//            
//            option = Platform.getDebugOption(COMPLETION_DEBUG);
//            if (option != null) CompletionEngine.DEBUG = option.equalsIgnoreCase("true") ; //$NON-NLS-1$
            
            option = Platform.getDebugOption(CP_RESOLVE_DEBUG);
            if (option != null) {
                CP_RESOLVE_VERBOSE = option.equalsIgnoreCase("true") ; //$NON-NLS-1$
            }
            
//            option = Platform.getDebugOption(DELTA_DEBUG);
//            if (option != null) DeltaProcessor.DEBUG = option.equalsIgnoreCase("true") ; //$NON-NLS-1$
//            
//            option = Platform.getDebugOption(DELTA_DEBUG_VERBOSE);
//            if (option != null) DeltaProcessor.VERBOSE = option.equalsIgnoreCase("true") ; //$NON-NLS-1$
//            
//            option = Platform.getDebugOption(HIERARCHY_DEBUG);
//            if (option != null) TypeHierarchy.DEBUG = option.equalsIgnoreCase("true") ; //$NON-NLS-1$
//            
//            option = Platform.getDebugOption(INDEX_MANAGER_DEBUG);
//            if (option != null) JobManager.VERBOSE = option.equalsIgnoreCase("true") ; //$NON-NLS-1$
            
            option = Platform.getDebugOption(JAVAMODEL_DEBUG);
            if (option != null) {
                VERBOSE = option.equalsIgnoreCase("true") ; //$NON-NLS-1$
            }
            
//            option = Platform.getDebugOption(POST_ACTION_DEBUG);
//            if (option != null) JavaModelOperation.POST_ACTION_VERBOSE = option.equalsIgnoreCase("true") ; //$NON-NLS-1$
//            
//            option = Platform.getDebugOption(RESOLUTION_DEBUG);
//            if (option != null) NameLookup.VERBOSE = option.equalsIgnoreCase("true") ; //$NON-NLS-1$
//            
//            option = Platform.getDebugOption(SEARCH_DEBUG);
//            if (option != null) BasicSearchEngine.VERBOSE = option.equalsIgnoreCase("true") ; //$NON-NLS-1$
//            
//            option = Platform.getDebugOption(SELECTION_DEBUG);
//            if (option != null) SelectionEngine.DEBUG = option.equalsIgnoreCase("true") ; //$NON-NLS-1$
            
            option = Platform.getDebugOption(ZIP_ACCESS_DEBUG);
            if (option != null) {
                ZIP_ACCESS_VERBOSE = option.equalsIgnoreCase("true") ; //$NON-NLS-1$
            }
            
//            option = Platform.getDebugOption(SOURCE_MAPPER_DEBUG_VERBOSE);
//            if (option != null) SourceMapper.VERBOSE = option.equalsIgnoreCase("true") ; //$NON-NLS-1$
        }
        
        // configure performance options
        if (PerformanceStats.ENABLED) {
//            CompletionEngine.PERF = PerformanceStats.isEnabled(COMPLETION_PERF);
//            SelectionEngine.PERF = PerformanceStats.isEnabled(SELECTION_PERF);
//            DeltaProcessor.PERF = PerformanceStats.isEnabled(DELTA_LISTENER_PERF);
            PERF_VARIABLE_INITIALIZER = PerformanceStats.isEnabled(VARIABLE_INITIALIZER_PERF);
            PERF_CONTAINER_INITIALIZER = PerformanceStats.isEnabled(CONTAINER_INITIALIZER_PERF);
//            ReconcileWorkingCopyOperation.PERF = PerformanceStats.isEnabled(RECONCILE_PERF);
        }
    }
    
    /**
     * Initialize preferences lookups for plugin.
     */
    private void initializePreferences() {
        
        // Create lookups
        preferencesLookup[PREF_INSTANCE] = new InstanceScope().getNode(CALEclipseCorePlugin.PLUGIN_ID);
        preferencesLookup[PREF_DEFAULT] = new DefaultScope().getNode(CALEclipseCorePlugin.PLUGIN_ID);
        
        //
        // Listen to instance preferences node removal from parent in order to refresh stored one
        //
        IEclipsePreferences.INodeChangeListener listener = new IEclipsePreferences.INodeChangeListener() {
            public void added(IEclipsePreferences.NodeChangeEvent event) {
                // do nothing
            }
            public void removed(IEclipsePreferences.NodeChangeEvent event) {
                if (event.getChild() == preferencesLookup[PREF_INSTANCE]) {
                    preferencesLookup[PREF_INSTANCE] = new InstanceScope().getNode(CALEclipseCorePlugin.PLUGIN_ID);
                    preferencesLookup[PREF_INSTANCE].addPreferenceChangeListener(new EclipsePreferencesListener());
                }
            }
        };
        ((IEclipsePreferences) preferencesLookup[PREF_INSTANCE].parent()).addNodeChangeListener(listener);
        preferencesLookup[PREF_INSTANCE].addPreferenceChangeListener(new EclipsePreferencesListener());
        
        //
        // Listen to default preferences node removal from parent in order to refresh stored one
        //
        listener = new IEclipsePreferences.INodeChangeListener() {
            public void added(IEclipsePreferences.NodeChangeEvent event) {
                // do nothing
            }
            public void removed(IEclipsePreferences.NodeChangeEvent event) {
                if (event.getChild() == preferencesLookup[PREF_DEFAULT]) {
                    preferencesLookup[PREF_DEFAULT] = new DefaultScope().getNode(CALEclipseCorePlugin.PLUGIN_ID);
                }
            }
        };
        ((IEclipsePreferences) preferencesLookup[PREF_DEFAULT].parent()).addNodeChangeListener(listener);
    }
    
    /**
     * @param optionName the option name to register.
     */
    public void addOptionName(String optionName) {
        optionNames.add(optionName);
    }
    
    /**
     * Get the value of an option.
     * @param optionName the name of the option
     * @return the value of the option, or null if the option is not defined.
     */
    public String getOption(String optionName) {

        if (CoreOptionIDs.CORE_ENCODING.equals(optionName)) {
            return CALEclipseCorePlugin.getEncoding();
        }
        String propertyName = optionName;
        if (this.optionNames.contains(propertyName)) {
            IPreferencesService service = Platform.getPreferencesService();
            String value = service.get(optionName, null, this.preferencesLookup);
            return value == null ? null : value.trim();
        }
        return null;
    }
    
    /**
     * Get the options defined by this manager.
     * @return Map from option name to options.
     */
    public Hashtable<String, String> getOptions() {

        // return cached options if already computed
        if (this.optionsCache != null) {
            return new Hashtable<String, String>(this.optionsCache);
        }

        // init
        Hashtable<String, String> options = new Hashtable<String, String>(10);
        IPreferencesService service = Platform.getPreferencesService();

        // set options using preferences service lookup
        for (final String propertyName : optionNames) {
            String propertyValue = service.get(propertyName, null, this.preferencesLookup);
            if (propertyValue != null) {
                options.put(propertyName, propertyValue);
            }
        }

        // get encoding through resource plugin
        options.put(CoreOptionIDs.CORE_ENCODING, CALEclipseCorePlugin.getEncoding());

        // store built map in cache
        this.optionsCache = new Hashtable<String, String>(options);

        // return built map
        return options;
    }

}
