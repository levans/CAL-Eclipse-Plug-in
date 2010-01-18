/*******************************************************************************
 * Copyright (c) 2006 Business Objects Software Limited and others.
 * All rights reserved. 
 * This file is made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Business Objects Software Limited - initial API and implementation based on Eclipse 3.1.2 code for
 *                             /org.eclipse.jdt.ui/ui/org/eclipse/jdt/internal/ui/text/java/JavaStringDoubleClickSelector.java
 *                           Eclipse source is available at: http://www.eclipse.org/downloads/
 *******************************************************************************/

/*
 * CALStringDoubleClickSelector.java
 * Creation date: Feb 8, 2006.
 * By: Edward Lam
 */
package org.openquark.cal.eclipse.ui.text.cal;

import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.ITextViewer;
import org.eclipse.jface.text.TextUtilities;

/**
 * Double click strategy aware of CAL string and character syntax rules.
 * @author Edward Lam
 */
public class CALStringDoubleClickSelector extends CALDoubleClickSelector {
    
    private final String fPartitioning;
    
    /**
     * Creates a new Java string double click selector for the given document partitioning.
     *
     * @param partitioning the document partitioning
     */
    public CALStringDoubleClickSelector(String partitioning) {
        super();
        fPartitioning = partitioning;
    }
    
    /*
     * @see ITextDoubleClickStrategy#doubleClicked(ITextViewer)
     */
    @Override
    public void doubleClicked(ITextViewer textViewer) {
        
        int offset = textViewer.getSelectedRange().x;

        if (offset < 0) {
            return;
        }

        IDocument document = textViewer.getDocument();

        IRegion region = match(document, offset);
        if (region != null && region.getLength() >= 2) {
            textViewer.setSelectedRange(region.getOffset() + 1, region.getLength() - 2);
        } else {
            region = selectWord(document, offset);
            textViewer.setSelectedRange(region.getOffset(), region.getLength());
        }
    }
    
    private IRegion match(IDocument document, int offset) {
        try {
            if ((document.getChar(offset) == '"') || (document.getChar(offset) == '\'') ||
                    (document.getChar(offset - 1) == '"') || (document.getChar(offset - 1) == '\''))
            {
                return TextUtilities.getPartition(document, fPartitioning, offset, true);
            }
        } catch (BadLocationException e) {
        }
        
        return null;
    }
}
