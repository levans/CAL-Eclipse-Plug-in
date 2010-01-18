/*******************************************************************************
 * Copyright (c) 2006 Business Objects Software Limited and others.
 * All rights reserved. 
 * This file is made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Business Objects Software Limited - initial API and implementation based on Eclipse 3.1.2 code for
 *                             /org.eclipse.jdt.ui/ui/org/eclipse/jdt/internal/ui/text/JavaWordFinder.java
 *                           Eclipse source is available at: http://www.eclipse.org/downloads/
 *******************************************************************************/

/*
 * CALWordFinder.java
 * Creation date: Feb 23, 2006.
 * By: Edward Lam
 */
package org.openquark.cal.eclipse.ui.text;


import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.Region;
import org.openquark.cal.compiler.LanguageInfo;


/**
 * A class with a static helper method to find a cal word in a document.
 * @author Edward Lam
 */
public class CALWordFinder {

    /**
     * Constructor for a CALWordFinder.
     * Not intended to be instantiated.
     */
    private CALWordFinder() {
    }
    
    public static IRegion findWord(IDocument document, int offset) {

        int start = -1;
        int end = -1;

        try {

            int pos = offset;
            char c;

            while (pos >= 0) {
                c = document.getChar(pos);
                if (!LanguageInfo.isCALVarPart(c)) {
                    break;
                }
                --pos;
            }

            start = pos;

            pos = offset;
            int length = document.getLength();

            while (pos < length) {
                c = document.getChar(pos);
                if (!LanguageInfo.isCALVarPart(c)) {
                    break;
                }
                ++pos;
            }

            end = pos;

        } catch (BadLocationException x) {
        }

        if (start > -1 && end > -1) {
            if (start == offset && end == offset) {
                return new Region(offset, 0);
            } else if (start == offset) {
                return new Region(start, end - start);
            } else {
                return new Region(start + 1, end - start - 1);
            }
        }

        return null;
    }
}
