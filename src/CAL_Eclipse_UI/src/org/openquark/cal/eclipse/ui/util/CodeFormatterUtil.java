/*******************************************************************************
 * Copyright (c) 2006 Business Objects Software Limited and others.
 * All rights reserved. 
 * This file is made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Business Objects Software Limited - initial API and implementation based on Eclipse 3.1.2 code for
 *                             /org.eclipse.jdt.ui/core extension/org/eclipse/jdt/internal/corext/util/CodeFormatterUtil.java
 *                           Eclipse source is available at: http://www.eclipse.org/downloads/
 *******************************************************************************/

/*
 * CodeFormatterUtil.java
 * Creation date: Feb 9, 2006.
 * By: Edward Lam
 */
package org.openquark.cal.eclipse.ui.util;

import org.eclipse.core.resources.IProject;
import org.openquark.cal.eclipse.core.CALEclipseCorePlugin;
import org.openquark.cal.eclipse.core.CoreOptionIDs;
import org.openquark.cal.eclipse.core.formatter.DefaultCodeFormatterConstants;




/**
 * @author Edward Lam
 */
public class CodeFormatterUtil {
    
    /**
     * Creates a string that represents the given number of indentation units.
     * The returned string can contain tabs and/or spaces depending on the core
     * formatter preferences.
     * 
     * @param indentationUnits the number of indentation units to generate
     * @param project the project from which to get the formatter settings,
     *        <code>null</code> if the workspace default should be used
     * @return the indent string
     */
    public static String createIndentString(int indentationUnits, IProject project) {
        final String tabChar = getCoreOption(project, DefaultCodeFormatterConstants.FORMATTER_TAB_CHAR);
        final int tabs, spaces;
        if (CoreOptionIDs.SPACE.equals(tabChar)) {
            tabs = 0;
            spaces = indentationUnits * getIndentWidth(project);
        } else if (CoreOptionIDs.TAB.equals(tabChar)) {
            // indentWidth == tabWidth
            tabs = indentationUnits;
            spaces = 0;
        } else if (DefaultCodeFormatterConstants.MIXED.equals(tabChar)) {
            int tabWidth = getTabWidth(project);
            int spaceEquivalents = indentationUnits * getIndentWidth(project);
            if (tabWidth > 0) {
                tabs = spaceEquivalents / tabWidth;
                spaces = spaceEquivalents % tabWidth;
            } else {
                tabs = 0;
                spaces = spaceEquivalents;
            }
        } else {
            // new indent type not yet handled
            assert (false);
            return null;
        }

        StringBuilder buffer = new StringBuilder(tabs + spaces);
        for (int i = 0; i < tabs; i++) {
            buffer.append('\t');
        }
        for (int i = 0; i < spaces; i++) {
            buffer.append(' ');
        }
        return buffer.toString();
    } 
    
    /**
     * Gets the current tab width.
     * 
     * @param project The project where the source is used, used for project
     *        specific options or <code>null</code> if the project is unknown
     *        and the workspace default should be used
     * @return The tab width
     */
    public static int getTabWidth(IProject project) {
        /*
         * If the tab-char is SPACE, FORMATTER_INDENTATION_SIZE is not used by the core formatter. We piggy back the
         * visual tab length setting in that preference in that case.
         */
        String key;
        if (CoreOptionIDs.SPACE.equals(getCoreOption(project, DefaultCodeFormatterConstants.FORMATTER_TAB_CHAR))) {
            key = DefaultCodeFormatterConstants.FORMATTER_INDENTATION_SIZE;
        } else {
            key = DefaultCodeFormatterConstants.FORMATTER_TAB_SIZE;
        }

        return getCoreOption(project, key, 4);
    }
    
    /**
     * Returns the current indent width.
     * 
     * @param project the project where the source is used or <code>null</code>
     *        if the project is unknown and the workspace default should be used
     * @return the indent width
     * @since 3.1
     */
    public static int getIndentWidth(IProject project) {
        String key;
        if (DefaultCodeFormatterConstants.MIXED.equals(getCoreOption(project, DefaultCodeFormatterConstants.FORMATTER_TAB_CHAR))) {
            key = DefaultCodeFormatterConstants.FORMATTER_INDENTATION_SIZE;
        } else {
            key = DefaultCodeFormatterConstants.FORMATTER_TAB_SIZE;
        }

        return getCoreOption(project, key, 4);
    }
   
    /**
     * Returns the possibly <code>project</code>-specific core preference
     * defined under <code>key</code>.
     * 
     * @param project the project to get the preference from, or
     *        <code>null</code> to get the global preference
     * @param key the key of the preference
     * @return the value of the preference
     * @since 3.1
     */
    private static String getCoreOption(IProject project, String key) {
        // TODOEL: TEMP
//        if (project == null) {
            return CALEclipseCorePlugin.getOption(key);
//        }
//        return project.getOption(key, true);
    }
    
    /**
     * Returns the possibly <code>project</code>-specific core preference
     * defined under <code>key</code>, or <code>def</code> if the value is
     * not a integer.
     * 
     * @param project the project to get the preference from, or
     *        <code>null</code> to get the global preference
     * @param key the key of the preference
     * @param def the default value
     * @return the value of the preference
     * @since 3.1
     */
    private static int getCoreOption(IProject project, String key, int def) {
        try {
            return Integer.parseInt(getCoreOption(project, key));
        } catch (NumberFormatException e) {
            return def;
        }
    }
    

}
