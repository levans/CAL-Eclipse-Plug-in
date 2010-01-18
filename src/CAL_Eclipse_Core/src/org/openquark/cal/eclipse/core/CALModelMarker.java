/*******************************************************************************
 * Copyright (c) 2005 Business Objects Software Limited and others.
 * All rights reserved. 
 * This file is made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Business Objects Software Limited - initial API and implementation based on Eclipse 3.1.2 code for
 *                             /org.eclipse.jdt.core/model/org/eclipse/jdt/core/IJavaModelMarker.java
 *                           Eclipse source is available at: http://www.eclipse.org/downloads/
 *******************************************************************************/

/*
 * CALModelMarker.java
 * Creation date: Nov 2, 2005.
 * By: Edward Lam
 */
package org.openquark.cal.eclipse.core;

/**
 * Markers used by the CAL model.
 * 
 * This class declares constants only; it is not intended to be implemented or extended.
 * 
 * @author Edward Lam
 */
public final class CALModelMarker {

    /*
     * Note: 
     *   Based on org.eclipse.jdt.core.IJavaModelMarker.
     *   The marker strings are bound in the plugin.xml file for this project to the extension ids for CAL Problem and associated markers.
     */
    
    /*
     * Not intended to be instantiated.
     */
    private CALModelMarker() {
    }
    
    /**
     * CAL model problem marker type (value <code>"org.openquark.cal.eclipse.core.problem"</code>).
     * This can be used to recognize those markers in the workspace that flag problems 
     * detected by the CAL tooling during compilation.
     */
    public static final String CAL_MODEL_PROBLEM_MARKER = CALEclipseCorePlugin.PLUGIN_ID + ".problem"; //$NON-NLS-1$
    
    /**
     * CAL model transient problem marker type (value <code>"org.openquark.cal.eclipse.core.transient_problem"</code>).
     * This can be used to recognize those markers in the workspace that flag transient
     * problems detected by the CAL tooling (such as a problem
     * detected by the outliner, or a problem detected during a code completion)
     */
    public static final String TRANSIENT_PROBLEM = CALEclipseCorePlugin.PLUGIN_ID + ".transient_problem"; //$NON-NLS-1$
    
    /**
     * CAL model task marker type (value <code>"org.openquark.cal.eclipse.core.task"</code>).
     * This can be used to recognize task markers in the workspace that correspond to tasks
     * specified in CAL source comments and detected during compilation (for example, 'TO-DO: ...').
     * Tasks are identified by a task tag, which can be customized through <code>CALEclipseCorePlugin</code>
     * option <code>"org.openquark.cal.eclipse.core.compiler.taskTag"</code>.
     */
    public static final String TASK_MARKER = CALEclipseCorePlugin.PLUGIN_ID + ".task"; //$NON-NLS-1$
    
    /** 
     * Id marker attribute (value <code>"arguments"</code>).
     * Arguments are concatenated into one String, prefixed with an argument count (followed with colon
     * separator) and separated with '#' characters. For example:
     *     { "foo", "bar" } is encoded as "2:foo#bar",     
     *     {  } is encoded as "0: "
     */
    public static final String ARGUMENTS = "arguments"; //$NON-NLS-1$
    
    /** 
     * Id marker attribute (value <code>"id"</code>).
     */
    public static final String ID = "id"; //$NON-NLS-1$
    
    /** 
     * Flags marker attribute (value <code>"flags"</code>).
     * Reserved for future use.
     */
    public static final String FLAGS = "flags"; //$NON-NLS-1$
    
    /** 
     * Cycle detected marker attribute (value <code>"cycleDetected"</code>).
     * Used only on buildpath problem markers.
     * The value of this attribute is either "true" or "false".
     */
    public static final String CYCLE_DETECTED = "cycleDetected"; //$NON-NLS-1$
    
    /**
     * Build path problem marker type (value <code>"org.openquark.cal.eclipse.core.buildpath_problem"</code>).
     * This can be used to recognize those markers in the workspace that flag problems 
     * detected by the CAL tooling during classpath setting.
     */
    public static final String BUILDPATH_PROBLEM_MARKER = CALEclipseCorePlugin.PLUGIN_ID + ".buildpath_problem"; //$NON-NLS-1$
    
    /** 
     * Classpath file format marker attribute (value <code>"classpathFileFormat"</code>).
     * Used only on buildpath problem markers.
     * The value of this attribute is either "true" or "false".
     */
    public static final String CLASSPATH_FILE_FORMAT = "classpathFileFormat"; //$NON-NLS-1$
    
}
