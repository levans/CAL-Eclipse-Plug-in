/*******************************************************************************
 * Copyright (c) 2006 Business Objects Software Limited and others.
 * All rights reserved. 
 * This file is made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Business Objects Software Limited - initial API and implementation based on Eclipse 3.1.2 code for
 *                             /org.eclipse.jdt.ui/ui/org/eclipse/jdt/ui/text/IJavaColorConstants.java
 *                           Eclipse source is available at: http://www.eclipse.org/downloads/
 *******************************************************************************/

/*
 * CALColorConstants.java
 * Creation date: Feb 7, 2006.
 * By: Edward Lam
 */
package org.openquark.cal.eclipse.ui.text;

/**
 * Color keys used for syntax highlighting CAL code and CALDoc compliant comments.
 * A <code>IColorManager</code> is responsible for mapping concrete colors to these keys.
 * <p>
 * This interface declares static final fields only; it is not intended to be implemented.
 * </p>
 *
 * @author Edward Lam
 */
public final class CALColorConstants {

    /*
     * Based on IJavaColorConstants
     */

    /**
     * Note: This constant is for internal use only. Clients should not use this constant.
     * The prefix all color constants start with
     * (value <code>"cal_"</code>).
     */
    public static final String PREFIX = "cal_"; //$NON-NLS-1$

    /** The color key for multi-line comments in CAL code
     * (value <code>"cal_multi_line_comment"</code>).
     */
    public static final String CAL_MULTI_LINE_COMMENT = "cal_multi_line_comment"; //$NON-NLS-1$

    /** The color key for single-line comments in CAL code
     * (value <code>"cal_single_line_comment"</code>).
     */
    public static final String CAL_SINGLE_LINE_COMMENT = "cal_single_line_comment"; //$NON-NLS-1$

    /** The color key for CAL keywords in CAL code
     * (value <code>"cal_keyword"</code>).
     */
    public static final String CAL_KEYWORD = "cal_keyword"; //$NON-NLS-1$

    /** The color key for string and character literals in CAL code
     * (value <code>"cal_string"</code>).
     */
    public static final String CAL_STRING = "cal_string"; //$NON-NLS-1$

    /** The color key for method names in CAL code
     * (value <code>"cal_method_name"</code>).
     *
     * @since 3.0
     * @deprecated replaced as of 3.1 by an equivalent semantic highlighting
     */
    @Deprecated
    public static final String CAL_METHOD_NAME = "cal_method_name"; //$NON-NLS-1$

    /** The color key for keyword 'return' in CAL code
     * (value <code>"cal_keyword_return"</code>).
     *
     * @since 3.0
     */
    public static final String CAL_KEYWORD_RETURN = "cal_keyword_return"; //$NON-NLS-1$

    /** The color key for operators and brackets in CAL code
     * (value <code>"cal_operator"</code>).
     *
     * @since 3.0
     */
    public static final String CAL_OPERATOR = "cal_operator"; //$NON-NLS-1$

    /**
     * The color key for everything in CAL code for which no other color is specified
     * (value <code>"cal_default"</code>).
     */
    public static final String CAL_DEFAULT = "cal_default"; //$NON-NLS-1$

    /**
     * The color key for annotations
     * (value <code>"cal_annotation"</code>).
     *
     * @since 3.1
     */
    public static final String CAL_ANNOTATION = "cal_annotation"; //$NON-NLS-1$

    /**
     * The color key for task tags in cal comments
     * (value <code>"cal_comment_task_tag"</code>).
     *
     * @since 2.1
     */
    public static final String TASK_TAG = "cal_comment_task_tag"; //$NON-NLS-1$

    /**
     * The color key for CALDoc keywords (<code>@foo</code>) in CALDoc comments
     * (value <code>"cal_doc_keyword"</code>).
     */
    public static final String CALDOC_KEYWORD = "cal_doc_keyword"; //$NON-NLS-1$

    /**
     * The color key for HTML tags (<code>&lt;foo&gt;</code>) in CALDoc comments
     * (value <code>"cal_doc_tag"</code>).
     */
    public static final String CALDOC_TAG = "cal_doc_tag"; //$NON-NLS-1$

    /**
     * The color key for CALDoc links (<code>{foo}</code>) in CALDoc comments
     * (value <code>"cal_doc_link"</code>).
     */
    public static final String CALDOC_LINK = "cal_doc_link"; //$NON-NLS-1$

    /**
     * The color key for everything in CALDoc comments for which no other color is specified
     * (value <code>"cal_doc_default"</code>).
     */
    public static final String CALDOC_DEFAULT = "cal_doc_default"; //$NON-NLS-1$

    /*
     * Not intended to be instantiated.
     */
    private CALColorConstants() {
    }

}
