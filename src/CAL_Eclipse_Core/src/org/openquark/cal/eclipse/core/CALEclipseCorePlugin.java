/*******************************************************************************
 * Copyright (c) 2005 Business Objects Software Limited and others.
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
 * CALEclipseCorePlugin.java
 * Creation date: Nov 1, 2005.
 * By: Edward Lam
 */
package org.openquark.cal.eclipse.core;

import java.util.Hashtable;

import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.Plugin;
import org.osgi.framework.BundleContext;

/**
 * The main plugin class to be used in the desktop.
 * 
 * @author Edward Lam
 */
public class CALEclipseCorePlugin extends Plugin {

    /**
     * The plug-in identifier of the CAL core support
     * (value <code>"org.openquark.cal.eclipse.core"</code>).
     */
    public static final String PLUGIN_ID = "org.openquark.cal.eclipse.core" ; //$NON-NLS-1$
    
    /**
     * The identifier for the CAL builder
     * (value <code>"org.openquark.cal.eclipse.core.calBuilder"</code>).
     */
    public static final String BUILDER_ID = PLUGIN_ID + ".calBuilder" ; //$NON-NLS-1$
    
    /**
     * The identifier for the CAL nature
     * (value <code>"org.openquark.cal.eclipse.core.calNature"</code>).
     * The presence of this nature on a project indicates that it is 
     * CAL-capable.
     *
     * @see org.eclipse.core.resources.IProject#hasNature(java.lang.String)
     */
    public static final String NATURE_ID = PLUGIN_ID + ".calNature" ; //$NON-NLS-1$
    
    /**
     * Possible configurable option ID.
     */
    public static final String CORE_ENCODING = PLUGIN_ID + ".encoding"; //$NON-NLS-1$

    
    // The shared instance.
    private static CALEclipseCorePlugin plugin;

    /**
     * The constructor.
     */
    public CALEclipseCorePlugin() {
        plugin = this;
        
        // Set the nullary environment so that resource path lookups can succeed.
        System.setProperty ("org.openquark.cal.services.CALWorkspaceEnvironmentProvider.Factory", 
            "org.openquark.cal.eclipse.bridge.EclipseWorkspaceEnvironmentProvider$Factory");
        
        // Set the system property which forces foreign entity resolution to happen strictly when loading a .cmi file.
        // Otherwise, if the cal builder is disabled and reenabled after a foreign entity change, any errors will not be picked
        // up if the module is simply loaded.
        // HACK/FIXME: We rely on this being set before the MachineConfiguration class is loaded.
        System.setProperty("org.openquark.cal.machine.lecc.strict_foreign_entity_loading", "true");
    }

    /**
     * {@inheritDoc}
     * 
     * <p> Registers the CALModelManager as a resource changed listener and save participant.
     * Starts the background indexing, and restore saved classpath variable values. <p> 
     * @throws Exception
     */
    @Override
    public void start(BundleContext context) throws Exception {
        super.start(context);
        CALModelManager.getCALModelManager().startup();
        
        /* 
         * identifying the os
         *
         *     http://developer.apple.com/technotes/tn2002/tn2110.html
         *     
         * the problem description
         * 
         *     http://developer.apple.com/technotes/tn2005/tn2147.html#TNTAG24
         *     
         * Basically get the toolkit initialized on the main thread.
         * 
         */
        {
            final String lcOSName = System.getProperty ("os.name").toLowerCase();
            if (lcOSName.startsWith("mac os x")){ 
                try{
                    System.setProperty("apple.awt.usingSWT", "true");
                    Class.forName("java.awt.Color"); 
                }
                catch(ClassNotFoundException e){
                }
            }
        }
    }

    /**
     * {@inheritDoc}
     * 
     * <p> De-registers the JavaModelManager as a resource changed listener and save participant.
     * <p>
     */
    @Override
    public void stop(BundleContext context) throws Exception {
        try {
            CALModelManager.getCALModelManager().shutdown();
        } finally {
            // ensure we call super.stop as the last thing
            super.stop(context);
        }
    }

    /**
     * @return the shared instance.
     */
    public static CALEclipseCorePlugin getDefault() {
        return plugin;
    }
    
    /**
     * Returns true if the given project is accessible and it has
     * a cal nature, otherwise false.
     * @param project IProject
     * @return boolean
     */
    public static boolean hasCALNature(IProject project) { 
        try {
            return project.hasNature(NATURE_ID);
        } catch (CoreException e) {
            // project does not exist or is not open
        }
        return false;
    }
    
    /**
     * Returns the workspace root default charset encoding.
     * 
     * @return the name of the default charset encoding for workspace root.
     * @see IContainer#getDefaultCharset()
     * @see ResourcesPlugin#getEncoding()
     * @since 3.0
     */
    public static String getEncoding() {
        // Verify that workspace is not shutting down (see bug https://bugs.eclipse.org/bugs/show_bug.cgi?id=60687)
        IWorkspace workspace = ResourcesPlugin.getWorkspace();
        if (workspace != null) {
            try {
                return workspace.getRoot().getDefaultCharset();
            } catch (CoreException e) {
                // fails silently and return plugin global encoding if core exception occurs
            }
        }
        return ResourcesPlugin.getEncoding();
    }
    
    /**
     * Helper method for returning one option value only. Equivalent to <code>(String)JavaCore.getOptions().get(optionName)</code>
     * Note that it may answer <code>null</code> if this option does not exist.
     * <p>
     * For a complete description of the configurable options, see <code>getDefaultOptions</code>.
     * </p>
     * 
     * @param optionName the name of an option
     * @return the String value of a given option
     * @see CALEclipseCorePreferenceInitializer for changing default settings
     * @since 2.0
     */
    public static String getOption(String optionName) {
        return CALModelManager.getCALModelManager().getOption(optionName);
    }
    
    /**
     * Returns the table of the current options. Initially, all options have their default values,
     * and this method returns a table that includes all known options.
     * <p>
     * For a complete description of the configurable options, see <code>getDefaultOptions</code>.
     * </p>
     * 
     * @return table of current settings of all options 
     *   (key type: <code>String</code>; value type: <code>String</code>)
     * @see CALEclipseCorePreferenceInitializer for changing default settings
     */
    public static Hashtable<String, String> getOptions() {
        return CALModelManager.getCALModelManager().getOptions();
    }

}
