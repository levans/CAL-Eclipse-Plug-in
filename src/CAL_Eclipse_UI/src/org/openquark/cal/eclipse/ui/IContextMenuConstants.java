/*******************************************************************************
 * Copyright (c) 2005 Business Objects Software Limited and others.
 * All rights reserved. 
 * This file is made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Business Objects Software Limited - initial API and implementation based on Eclipse 3.1.2 code for
 *                             /org.eclipse.jdt.ui/ui/org/eclipse/jdt/internal/ui/JavaUIMessages.java
 *                           Eclipse source is available at: http://www.eclipse.org/downloads/
 *******************************************************************************/

/*
 * IContextMenuConstants.java
 * Creation date: Nov 2, 2005.
 * By: Edward Lam
 */
package org.openquark.cal.eclipse.ui;

import org.eclipse.ui.IWorkbenchActionConstants;

/**
 * Constants for menu groups used in context menus for CAL views and editors.
 * 
 * @author Edward Lam
 */
public final class IContextMenuConstants {

    /**
     * Pop-up menu: name of group for goto actions (value <code>"group.open"</code>).
     * <p>
     * Examples for open actions are:
     * <ul>
     *  <li>Go Into</li>
     *  <li>Go To</li>
     * </ul>
     * </p>
     */
    public static final String GROUP_GOTO = "group.goto"; //$NON-NLS-1$
    
    /**
     * Pop-up menu: name of group for open actions (value <code>"group.open"</code>).
     * <p>
     * Examples for open actions are:
     * <ul>
     *  <li>Open To</li>
     *  <li>Open With</li>
     * </ul>
     * </p>
     */
    public static final String GROUP_OPEN = "group.open"; //$NON-NLS-1$
    
    /**
     * Pop-up menu: name of group for show actions (value <code>"group.show"</code>).
     * <p>
     * Examples for show actions are:
     * <ul>
     *  <li>Show in Navigator</li>
     *  <li>Show in Type Hierarchy</li>
     * </ul>
     * </p>
     */
    public static final String GROUP_SHOW = "group.show"; //$NON-NLS-1$
    
    /**
     * Pop-up menu: name of group for new actions (value <code>"group.new"</code>).
     * <p>
     * Examples for new actions are:
     * <ul>
     *  <li>Create new class</li>
     *  <li>Create new interface</li>
     * </ul>
     * </p>
     */
    public static final String GROUP_NEW = "group.new"; //$NON-NLS-1$
    
    /**
     * Pop-up menu: name of group for build actions (value <code>"group.build"</code>).
     */
    public static final String GROUP_BUILD = "group.build"; //$NON-NLS-1$
    
    /**
     * Pop-up menu: name of group for reorganize actions (value <code>"group.reorganize"</code>).
     */
    public static final String GROUP_REORGANIZE = IWorkbenchActionConstants.GROUP_REORGANIZE;
    
    /**
     * Pop-up menu: name of group for code generation or refactoring actions (
     * value <code>"group.generate"</code>).
     */
    public static final String GROUP_GENERATE = "group.generate"; //$NON-NLS-1$
    
    /**
     * Pop-up menu: name of group for search actions (value <code>"group.search"</code>).
     */
    public static final String GROUP_SEARCH = "group.search"; //$NON-NLS-1$
    
    /**
     * Pop-up menu: name of group for additional actions (value <code>"group.additions"</code>).
     */
    public static final String GROUP_ADDITIONS = "additions"; //$NON-NLS-1$
    
    /**
     * Pop-up menu: name of group for viewer setup actions (value <code>"group.viewerSetup"</code>).
     */
    public static final String GROUP_VIEWER_SETUP = "group.viewerSetup"; //$NON-NLS-1$
    
    /**
     * Pop-up menu: name of group for properties actions (value <code>"group.properties"</code>).
     */
    public static final String GROUP_PROPERTIES = "group.properties"; //$NON-NLS-1$
    
    /**
     * Pop-up menu: name of group for remove match actions (value <code>"group.removeMatches"</code>).
     * @since 2.1
     */
    public static final String GROUP_REMOVE_MATCHES = "group.removeMatches"; //$NON-NLS-1$
    
}
