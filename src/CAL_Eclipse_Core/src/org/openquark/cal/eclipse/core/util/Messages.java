/*******************************************************************************
 * Copyright (c) 2005 Business Objects Software Limited and others.
 * All rights reserved. 
 * This file is made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Business Objects Software Limited - initial API and implementation based on Eclipse 3.1.2 code for
 *                             /org.eclipse.jdt.core/model/org/eclipse/jdt/internal/core/util/Messages.java
 *                           Eclipse source is available at: http://www.eclipse.org/downloads/
 *******************************************************************************/

/*
 * Messages.java
 * Creation date: Nov 2, 2005.
 * By: Edward Lam
 */
package org.openquark.cal.eclipse.core.util;

import java.text.MessageFormat;
import org.eclipse.osgi.util.NLS;

/**
 * Shared Messages class for the CALEclipse core plugin.
 * @author Edward Lam
 */
public final class Messages extends NLS {
    /*
     * TODOEL: prune.
     */

    private static final String BUNDLE_NAME = "org.openquark.cal.eclipse.core.util.messages";//$NON-NLS-1$

    private Messages() {
        // Do not instantiate
    }

    public static String workingCopy_commit;
    public static String build_preparingBuild;
    public static String build_readStateProgress;
    public static String build_saveStateProgress;
    public static String build_saveStateComplete;
    public static String build_readingDelta;
    public static String build_analyzingDeltas;
    public static String build_analyzingSources;
    public static String build_cleaningOutput;
    public static String build_copyingResources;
    public static String build_compiling;
    public static String build_foundHeader;
    public static String build_fixedHeader;
    public static String build_oneError;
    public static String build_oneWarning;
    public static String build_multipleErrors;
    public static String build_multipleWarnings;
    public static String build_done;
    public static String build_wrongFileFormat;
    public static String build_cannotSaveState;
    public static String build_cannotSaveStates;
    public static String build_initializationError;
    public static String build_serializationError;
    public static String build_classFileCollision;
    public static String build_duplicateClassFile;
    public static String build_duplicateResource;
    public static String build_inconsistentClassFile;
    public static String build_inconsistentProject;
    public static String build_incompleteClassPath;
    public static String build_missingSourceFile;
    public static String build_prereqProjectHasClasspathProblems;
    public static String build_prereqProjectMustBeRebuilt;
    public static String build_abortDueToClasspathProblems;
    public static String error_hint_UnsupportedClassVersionError;
    public static String status_cannotUseDeviceOnPath;
    public static String status_coreException;
    public static String status_defaultPackageReadOnly;
    public static String status_evaluationError;
    public static String status_JDOMError;
    public static String status_IOException;
    public static String status_indexOutOfBounds;
    public static String status_invalidContents;
    public static String status_invalidDestination;
    public static String status_invalidName;
    public static String status_invalidPackage;
    public static String status_invalidPath;
    public static String status_invalidProject;
    public static String status_invalidResource;
    public static String status_invalidResourceType;
    public static String status_invalidSibling;
    public static String status_nameCollision;
    public static String status_noLocalContents;
    public static String status_OK;
    public static String status_readOnly;
    public static String status_targetException;
    public static String status_updateConflict;
    public static String file_notFound;
    public static String file_badFormat;
    public static String path_nullPath;
    public static String path_mustBeAbsolute;
    public static String cache_invalidLoadFactor;
    public static String savedState_jobName;
    public static String javamodel_initialization;
    public static String quickFix_importThisModule;
    public static String quickFix_fullyQualifyName;
    
    static {
        NLS.initializeMessages(BUNDLE_NAME, Messages.class);
    }
    
    /**
     * Bind the given message's substitution locations with the given string values.
     * 
     * @param message the message to be manipulated
     * @return the manipulated String
     */
    public static String bind(String message) {
        return bind(message, null);
    }

    /**
     * Bind the given message's substitution locations with the given string values.
     * 
     * @param message the message to be manipulated
     * @param binding the object to be inserted into the message
     * @return the manipulated String
     */
    public static String bind(String message, Object binding) {
        return bind(message, new Object[]{binding});
    }

    /**
     * Bind the given message's substitution locations with the given string values.
     * 
     * @param message the message to be manipulated
     * @param binding1 An object to be inserted into the message
     * @param binding2 A second object to be inserted into the message
     * @return the manipulated String
     */
    public static String bind(String message, Object binding1, Object binding2) {
        return bind(message, new Object[]{binding1, binding2});
    }

    /**
     * Bind the given message's substitution locations with the given string values.
     * 
     * @param message the message to be manipulated
     * @param bindings An array of objects to be inserted into the message
     * @return the manipulated String
     */
    public static String bind(String message, Object[] bindings) {
        return MessageFormat.format(message, bindings);
    }
}