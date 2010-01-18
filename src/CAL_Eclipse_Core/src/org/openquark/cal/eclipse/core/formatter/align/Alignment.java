/*******************************************************************************
 * Copyright (c) 2006 Business Objects Software Limited and others.
 * All rights reserved. 
 * This file is made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Business Objects Software Limited - initial API and implementation based on Eclipse 3.1.2 code for
 *                             /org.eclipse.jdt.core/formatter/org/eclipse/jdt/internal/formatter/align/Alignment.java
 *                           Eclipse source is available at: http://www.eclipse.org/downloads/
 *******************************************************************************/

/*
 * Alignment.java
 * Creation date: Feb 9, 2006.
 * By: Edward Lam
 */
package org.openquark.cal.eclipse.core.formatter.align;

/**
 * Alignment management
 * 
 * @author Edward Lam
 */
public class Alignment {
    /*
     * TODOEL: Just contains constants for now.
     */
    
    /* 
     * Alignment modes
     */
    public static final int M_FORCE = 1; // if bit set, then alignment will be non-optional (default is optional)
    public static final int M_INDENT_ON_COLUMN = 2; // if bit set, broken fragments will be aligned on current location column (default is to break at current indentation level)
    public static final int M_INDENT_BY_ONE = 4; // if bit set, broken fragments will be indented one level below current (not using continuation indentation)
    
    // split modes can be combined either with M_FORCE or M_INDENT_ON_COLUMN
    
    /** foobar(#fragment1, #fragment2, <ul>
     *  <li>    #fragment3, #fragment4 </li>
     * </ul>
     */
    public static final int M_COMPACT_SPLIT = 16; // fill each line with all possible fragments
    
    /** foobar(<ul>
     * <li>    #fragment1, #fragment2,  </li>
     * <li>     #fragment5, #fragment4, </li>
     * </ul>
     */
    public static final int M_COMPACT_FIRST_BREAK_SPLIT = 32; //  compact mode, but will first try to break before first fragment
    
    /** foobar(<ul>
     * <li>     #fragment1,  </li>
     * <li>     #fragment2,  </li>
     * <li>     #fragment3 </li>
     * <li>     #fragment4,  </li>
     * </ul>
     */
    public static final int M_ONE_PER_LINE_SPLIT = 32+16; // one fragment per line
    
    /** 
     * foobar(<ul>
     * <li>     #fragment1,  </li>
     * <li>        #fragment2,  </li>
     * <li>        #fragment3 </li>
     * <li>        #fragment4,  </li>
     * </ul>
     */ 
    public static final int M_NEXT_SHIFTED_SPLIT = 64; // one fragment per line, subsequent are indented further
    
    /** foobar(#fragment1, <ul>
     * <li>      #fragment2,  </li>
     * <li>      #fragment3 </li>
     * <li>      #fragment4,  </li>
     * </ul>
     */
    public static final int M_NEXT_PER_LINE_SPLIT = 64+16; // one per line, except first fragment (if possible)
    
    //64+32
    //64+32+16
    
    // mode controlling column alignments
    /** 
     * <table BORDER COLS=4 WIDTH="100%" >
     * <tr><td>#fragment1A</td>            <td>#fragment2A</td>       <td>#fragment3A</td>  <td>#very-long-fragment4A</td></tr>
     * <tr><td>#fragment1B</td>            <td>#long-fragment2B</td>  <td>#fragment3B</td>  <td>#fragment4B</td></tr>
     * <tr><td>#very-long-fragment1C</td>  <td>#fragment2C</td>       <td>#fragment3C</td>  <td>#fragment4C</td></tr>
     * </table>
     */
    public static final int M_MULTICOLUMN = 256; // fragments are on same line, but multiple line of fragments will be aligned vertically
    
    public static final int M_NO_ALIGNMENT = 0;
    
//    public int mode;
    
    public static final int SPLIT_MASK = M_ONE_PER_LINE_SPLIT | M_NEXT_SHIFTED_SPLIT | M_COMPACT_SPLIT | M_COMPACT_FIRST_BREAK_SPLIT | M_NEXT_PER_LINE_SPLIT;
    
    // alignment tie-break rules - when split is needed, will decide whether innermost/outermost alignment is to be chosen
    public static final int R_OUTERMOST = 1;
    public static final int R_INNERMOST = 2;
//    public int tieBreakRule;
    
    // alignment effects on a per fragment basis
    public static int NONE = 0;
    public static int BREAK = 1;
    
    // chunk kind
    public static final int CHUNK_FIELD = 1;
    public static final int CHUNK_METHOD = 2;
    public static final int CHUNK_TYPE = 3;
    public static final int CHUNK_ENUM = 4;
    
}
