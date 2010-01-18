/*******************************************************************************
 * Copyright (c) 2006 Business Objects Software Limited and others.
 * All rights reserved. 
 * This file is made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Business Objects Software Limited - initial API and implementation based on Eclipse 3.1.2 code for
 *                             /org.eclipse.jdt.ui/ui/org/eclipse/jdt/internal/ui/text/JavaPairMatcher.java
 *                           Eclipse source is available at: http://www.eclipse.org/downloads/
 *******************************************************************************/

/*
 * CALPairMatcher.java
 * Creation date: Feb 8, 2006.
 * By: Edward Lam
 */
package org.openquark.cal.eclipse.ui.text;


import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.Region;
import org.eclipse.jface.text.TextUtilities;
import org.eclipse.jface.text.source.ICharacterPairMatcher;

/**
 * Helper class for match pairs of characters.
 * @author Edward Lam
 */
public class CALPairMatcher implements ICharacterPairMatcher {
    
    protected char[] fPairs;
    protected IDocument fDocument;
    protected int fOffset;
    
    protected int fStartPos;
    protected int fEndPos;
    protected int fAnchor;
    
    private boolean fHighlightAngularBrackets= false;
    
    
    public CALPairMatcher(char[] pairs) {
        fPairs = pairs;
    }
    
    /* (non-Javadoc)
     * @see org.eclipse.jface.text.source.ICharacterPairMatcher#match(org.eclipse.jface.text.IDocument, int)
     */
    public IRegion match(IDocument document, int offset) {
        
        fOffset = offset;

        if (fOffset < 0) {
            return null;
        }

        fDocument = document;

        if (fDocument != null && matchPairsAt() && fStartPos != fEndPos) {
            return new Region(fStartPos, fEndPos - fStartPos + 1);
        }
        
        return null;
    }
    
    /* (non-Javadoc)
     * @see org.eclipse.jface.text.source.ICharacterPairMatcher#getAnchor()
     */
    public int getAnchor() {
        return fAnchor;
    }
    
    /* (non-Javadoc)
     * @see org.eclipse.jface.text.source.ICharacterPairMatcher#dispose()
     */
    public void dispose() {
        clear();
        fDocument = null;
    }
    
    /*
     * @see org.eclipse.jface.text.source.ICharacterPairMatcher#clear()
     */
    public void clear() {
    }
    
    protected boolean matchPairsAt() {
        
        int i;
        int pairIndex1 = fPairs.length;
        int pairIndex2 = fPairs.length;

        fStartPos = -1;
        fEndPos = -1;

        // get the char preceding the start position
        try {

            char prevChar = fDocument.getChar(Math.max(fOffset - 1, 0));
            // search for opening peer character next to the activation point
            for (i = 0; i < fPairs.length; i = i + 2) {
                if (prevChar == fPairs[i]) {
                    fStartPos = fOffset - 1;
                    pairIndex1 = i;
                }
            }

            // search for closing peer character next to the activation point
            for (i = 1; i < fPairs.length; i = i + 2) {
                if (prevChar == fPairs[i]) {
                    fEndPos = fOffset - 1;
                    pairIndex2 = i;
                }
            }

            if (fEndPos > -1) {
                fAnchor = RIGHT;
                fStartPos = searchForOpeningPeer(fEndPos, fPairs[pairIndex2 - 1], fPairs[pairIndex2], fDocument);
                if (fStartPos > -1) {
                    return true;
                } else {
                    fEndPos = -1;
                }
            } else if (fStartPos > -1) {
                fAnchor = LEFT;
                fEndPos = searchForClosingPeer(fStartPos, fPairs[pairIndex1], fPairs[pairIndex1 + 1], fDocument);
                if (fEndPos > -1) {
                    return true;
                } else {
                    fStartPos = -1;
                }
            }
            
        } catch (BadLocationException x) {
        }
        
        return false;
    }
    
    protected int searchForClosingPeer(int offset, char openingPeer, char closingPeer, IDocument document) throws BadLocationException {
        boolean useGenericsHeuristic = openingPeer == '<';
        if (useGenericsHeuristic && !fHighlightAngularBrackets) {
            return -1;
        }
        CALHeuristicScanner scanner = new CALHeuristicScanner(document, CALPartitions.CAL_PARTITIONING, TextUtilities.getContentType(document, CALPartitions.CAL_PARTITIONING, offset, false));
        if (useGenericsHeuristic && !isTypeParameterBracket(offset, document, scanner)) {
            return -1;
        }

        return scanner.findClosingPeer(offset + 1, openingPeer, closingPeer);
    }
    
    
    protected int searchForOpeningPeer(int offset, char openingPeer, char closingPeer, IDocument document) throws BadLocationException {
        boolean useGenericsHeuristic = openingPeer == '<';
        if (useGenericsHeuristic && !fHighlightAngularBrackets) {
            return -1;
        }

        CALHeuristicScanner scanner = new CALHeuristicScanner(document, CALPartitions.CAL_PARTITIONING, TextUtilities.getContentType(document, CALPartitions.CAL_PARTITIONING, offset, false));
        int peer = scanner.findOpeningPeer(offset - 1, openingPeer, closingPeer);
        if (peer == CALHeuristicScanner.NOT_FOUND) {
            return -1;
        }
        if (useGenericsHeuristic && !isTypeParameterBracket(peer, document, scanner)) {
            return -1;
        }
        return peer;
    }
    
    /**
     * Checks if the angular bracket at <code>offset</code> is a type
     * parameter bracket.
     *
     * @param offset the offset of the opening bracket
     * @param document the document
     * @param scanner a java heuristic scanner on <code>document</code>
     * @return <code>true</code> if the bracket is part of a type parameter,
     *         <code>false</code> otherwise
     * @since 3.1
     */
    private boolean isTypeParameterBracket(int offset, IDocument document, CALHeuristicScanner scanner) {
        /*
         * type parameter come after braces (closing or opening), semicolons, or after
         * a Type name (heuristic: starts with capital character, or after a modifier
         * keyword in a method declaration (visibility, static, synchronized, final)
         */
        
        try {
            IRegion line = document.getLineInformationOfOffset(offset);

            int prevToken = scanner.previousToken(offset - 1, line.getOffset());
            int prevTokenOffset = scanner.getPosition() + 1;
            String previous = prevToken == Symbols.TokenEOF ? null : document.get(prevTokenOffset, offset - prevTokenOffset).trim();
            
            if (       prevToken == Symbols.TokenLBRACE
                    || prevToken == Symbols.TokenRBRACE
                    || prevToken == Symbols.TokenSEMICOLON
                    || prevToken == Symbols.TokenSYNCHRONIZED
                    || prevToken == Symbols.TokenSTATIC
                    || (prevToken == Symbols.TokenCONSIDENT && isTypeParameterIntroducer(previous))
                    || prevToken == Symbols.TokenEOF) {
                return true;
            }
        } catch (BadLocationException e) {
            return false;
        }
        
        return false;
    }
    
    /**
     * Returns <code>true</code> if <code>identifier</code> is an identifier
     * that could come right before a type parameter list. It uses a heuristic:
     * if the identifier starts with an upper case, it is assumed a type name.
     * Also, if <code>identifier</code> is a method modifier, it is assumed
     * that the angular bracket is part of the generic type parameter of a
     * method.
     *
     * @param identifier the identifier to check
     * @return <code>true</code> if the identifier could introduce a type
     *         parameter list
     * @since 3.1
     */
    private boolean isTypeParameterIntroducer(String identifier) {
        return identifier.length() > 0
        && (Character.isUpperCase(identifier.charAt(0))
                || identifier.startsWith("final") //$NON-NLS-1$
                || identifier.startsWith("public") //$NON-NLS-1$
                || identifier.startsWith("public") //$NON-NLS-1$
                || identifier.startsWith("protected") //$NON-NLS-1$
                || identifier.startsWith("private")); //$NON-NLS-1$
    }
    
}
