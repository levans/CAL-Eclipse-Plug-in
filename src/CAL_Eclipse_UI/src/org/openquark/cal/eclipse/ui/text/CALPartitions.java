/*******************************************************************************
 * Copyright (c) 2006 Business Objects Software Limited and others.
 * All rights reserved. 
 * This file is made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Business Objects Software Limited - initial API and implementation based on Eclipse 3.1.2 code for
 *                             /org.eclipse.jdt.ui/ui/org/eclipse/jdt/ui/text/IJavaPartitions.java
 *                           Eclipse source is available at: http://www.eclipse.org/downloads/
 *******************************************************************************/

/*
 * CALPartitions.cal
 * Creation date: Feb 7, 2006.
 * By: Edward Lam
 */
package org.openquark.cal.eclipse.ui.text;

/**
 * Definition of CAL partitioning and its partitions.
 *
 * @author Edward Lam
 */
public final class CALPartitions {

    /*
     * Not intended to be instantiated.
     */
    private CALPartitions() {
    }

    /**
     * The identifier of the CAL partitioning.
     */
    public static final String CAL_PARTITIONING = "___cal_partitioning"; //$NON-NLS-1$

    /**
     * The identifier of the single-line end comment partition content type.
     */
    public static final String CAL_SINGLE_LINE_COMMENT = "__cal_singleline_comment"; //$NON-NLS-1$

    /**
     * The identifier multi-line comment partition content type.
     */
    public static final String CAL_MULTI_LINE_COMMENT = "__cal_multiline_comment"; //$NON-NLS-1$

    /**
     * The identifier of the CALDoc partition content type.
     */
    public static final String CAL_DOC = "__cal_caldoc"; //$NON-NLS-1$

    /**
     * The identifier of the CAL string partition content type.
     */
    public static final String CAL_STRING = "__cal_string"; //$NON-NLS-1$

    /**
     * The identifier of the CAL character partition content type.
     */
    public static final String CAL_CHARACTER = "__cal_character"; //$NON-NLS-1$
    
    /**
     * @param partitionName the name of the partitiion
     * @return true if the given partition is a kind of comment
     */
    public static boolean isComment(String partitionName){
        return partitionName.equals(CAL_SINGLE_LINE_COMMENT) ||
            partitionName.equals(CAL_MULTI_LINE_COMMENT) ||
            partitionName.equals(CAL_DOC);
    }
}
