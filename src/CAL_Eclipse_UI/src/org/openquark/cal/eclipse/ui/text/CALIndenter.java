/*******************************************************************************
 * Copyright (c) 2006 Business Objects Software Limited and others.
 * All rights reserved. 
 * This file is made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Business Objects Software Limited - initial API and implementation based on Eclipse 3.1.2 code for
 *                             /org.eclipse.jdt.ui/ui/org/eclipse/jdt/internal/ui/text/JavaIndenter.java
 *                           Eclipse source is available at: http://www.eclipse.org/downloads/
 *******************************************************************************/

/*
 * CALIndenter.java
 * Creation date: Feb 10, 2006.
 * By: Edward Lam
 */
package org.openquark.cal.eclipse.ui.text;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.Assert;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IRegion;
import org.openquark.cal.eclipse.core.CALEclipseCorePlugin;
import org.openquark.cal.eclipse.core.CoreOptionIDs;
import org.openquark.cal.eclipse.core.formatter.DefaultCodeFormatterConstants;
import org.openquark.cal.eclipse.ui.util.CodeFormatterUtil;


/**
 * Uses the {@link org.openquark.cal.eclipse.ui.text.CALHeuristicScanner} to
 * get the indentation level for a certain position in a document.
 *
 * <p>
 * An instance holds some internal position in the document and is therefore
 * not threadsafe.
 * </p>
 *
 * @author Edward Lam
 */
public class CALIndenter {
    
    /** The document being scanned. */
    private final IDocument fDocument;
    
    /** The indentation accumulated by <code>findReferencePosition</code>. */
    private int fIndent;
    
    /** The absolute (character-counted) indentation offset for special cases (method defs, array initializers) */
    private int fAlign;
    
    /** The stateful scanposition for the indentation methods.  nextToken() will read the token at this position. */
    private int fPosition;
    
    /** The previous position. */
    private int fPreviousPos;
    
    /** The most recent token. */
    private int fToken;

    /**
     * The scanner we will use to scan the document. It has to be installed
     * on the same document as the one we get.
     */
    private final CALHeuristicScanner fScanner;
    private final IProject fProject;            // Should be a cal project

    /**
     * Creates a new instance.
     *
     * @param document the document to scan
     * @param scanner the {@link CALHeuristicScanner} to be used for scanning
     * the document. It must be installed on the same <code>IDocument</code>.
     */
    public CALIndenter(IDocument document, CALHeuristicScanner scanner) {
        this(document, scanner, null);
    }
    
    /**
     * Creates a new instance.
     *
     * @param document the document to scan
     * @param scanner the {@link CALHeuristicScanner}to be used for scanning
     *        the document. It must be installed on the same
     *        <code>IDocument</code>.
     * @param project the java project to get the formatter preferences from, or
     *        <code>null</code> to use the workspace settings
     * @since 3.1
     */
    public CALIndenter(IDocument document, CALHeuristicScanner scanner, IProject project) {
        Assert.isNotNull(document);
        Assert.isNotNull(scanner);
        fDocument = document;
        fScanner = scanner;
        fProject = project;
    }
    
    /**
     * Computes the indentation at the reference point of <code>position</code>.
     *
     * @param offset the offset in the document
     * @return a String which reflects the indentation at the line in which the
     *         reference position to <code>offset</code> resides, or <code>null</code>
     *         if it cannot be determined
     */
    public StringBuilder getReferenceIndentation(int offset) {
        return getReferenceIndentation(offset, Symbols.TokenOTHER, 0);
    }
    
    /**
     * Computes the indentation at the reference point of <code>position</code>.
     *
     * @param offset the offset in the document
     * @param nextToken the next token to assume in the document
     * @param nDanglingSemicolons if nextToken is a semicolon, the number of semicolons following the current position on the same line.
     * @return a String which reflects the indentation at the line in which the
     *         reference position to <code>offset</code> resides, or <code>null</code>
     *         if it cannot be determined
     */
    private StringBuilder getReferenceIndentation(int offset, int nextToken, int nDanglingSemicolons) {
        
        int unit;
        if (nextToken != Symbols.TokenOTHER) {
            unit = findReferencePosition(offset, nextToken, nDanglingSemicolons);
        
        } else {
            unit = findReferencePosition(offset);
        }

        // if we were unable to find anything, return null
        if (unit == CALHeuristicScanner.NOT_FOUND) {
            return null;
        }
        
        return getLeadingWhitespace(unit);
        
    }
    
    /**
     * Computes the indentation at <code>offset</code>.
     *
     * @param offset the offset in the document
     * @return a String which reflects the correct indentation for the line in
     *         which offset resides, or <code>null</code> if it cannot be determined
     */
    public StringBuilder computeIndentation(int offset) {
        return computeIndentation(offset, Symbols.TokenOTHER, 0);
    }
    
    /**
     * Computes the indentation at <code>offset</code>.
     *
     * @param offset the offset in the document
     * @param nextToken the next token to assume in the document, or Symbols.TokenOTHER for none.
     * @param nDanglingSemicolons if nextToken is a semicolon, the number of semicolons following the current position on the same line.
     * @return a String which reflects the correct indentation for the line in
     *         which offset resides, or <code>null</code> if it cannot be determined
     */
    public StringBuilder computeIndentation(int offset, int nextToken, int nDanglingSemicolons) {
        
        StringBuilder reference = getReferenceIndentation(offset, nextToken, nDanglingSemicolons);
        
        // handle special alignment
        if (fAlign != CALHeuristicScanner.NOT_FOUND) {
            try {
                // a special case has been detected.
                IRegion line = fDocument.getLineInformationOfOffset(fAlign);
                int lineOffset = line.getOffset();
                return createIndent(lineOffset, fAlign, false);
            } catch (BadLocationException e) {
                return null;
            }
        }

        if (reference == null) {
            return null;
        }
        
        // add additional indent
        return createReusingIndent(reference, fIndent);
    }
    
    /**
     * Computes the length of a <code>CharacterSequence</code>, counting
     * a tab character as the size until the next tab stop and every other
     * character as one.
     *
     * @param indent the string to measure
     * @return the visual length in characters
     */
    private int computeVisualLength(CharSequence indent) {
        final int tabSize = prefTabSize();
        int length = 0;
        for (int i = 0; i < indent.length(); i++) {
            char ch = indent.charAt(i);
            switch (ch) {
                case '\t':
                    if (tabSize > 0) {
                        int reminder = length % tabSize;
                        length += tabSize - reminder;
                    }
                    break;
                case ' ':
                    length++;
                    break;
            }
        }
        return length;
    }
    
    /**
     * Strips any characters off the end of <code>reference</code> that exceed
     * <code>indentLength</code>.
     *
     * @param reference the string to measure
     * @param indentLength the maximum visual indentation length
     * @return the stripped <code>reference</code>
     */
    private StringBuilder stripExceedingChars(StringBuilder reference, int indentLength) {
        final int tabSize = prefTabSize();
        int measured = 0;
        int chars = reference.length();
        int i = 0;
        for (; measured < indentLength && i < chars; i++) {
            char ch = reference.charAt(i);
            switch (ch) {
                case '\t':
                    if (tabSize > 0) {
                        int reminder = measured % tabSize;
                        measured += tabSize - reminder;
                    }
                    break;
                case ' ':
                    measured++;
                    break;
            }
        }
        int deleteFrom = measured > indentLength ? i - 1 : i;
        
        return reference.delete(deleteFrom, chars);
    }
    
    /**
     * Returns the indentation of the line at <code>offset</code> as a
     * <code>StringBuilder</code>. If the offset is not valid, the empty string
     * is returned.
     *
     * @param offset the offset in the document
     * @return the indentation (leading whitespace) of the line in which
     *                 <code>offset</code> is located
     */
    private StringBuilder getLeadingWhitespace(int offset) {
        StringBuilder indent = new StringBuilder();
        try {
            IRegion line = fDocument.getLineInformationOfOffset(offset);
            int lineOffset = line.getOffset();
            int nonWS = fScanner.findNonWhitespaceForwardInAnyPartition(lineOffset, lineOffset + line.getLength());
            indent.append(fDocument.get(lineOffset, nonWS - lineOffset));
            return indent;
        } catch (BadLocationException e) {
            return indent;
        }
    }
    
    /**
     * Creates an indentation string of the length indent - start, consisting of
     * the content in <code>fDocument</code> in the range [start, indent),
     * with every character replaced by a space except for tabs, which are kept
     * as such.
     * <p>
     * If <code>convertSpaceRunsToTabs</code> is <code>true</code>, every
     * run of the number of spaces that make up a tab are replaced by a tab
     * character. If it is not set, no conversion takes place, but tabs in the
     * original range are still copied verbatim.
     * </p>
     *
     * @param start the start of the document region to copy the indent from
     * @param indent the exclusive end of the document region to copy the indent
     *        from
     * @param convertSpaceRunsToTabs whether to convert consecutive runs of
     *        spaces to tabs
     * @return the indentation corresponding to the document content specified
     *         by <code>start</code> and <code>indent</code>
     */
    private StringBuilder createIndent(int start, final int indent, final boolean convertSpaceRunsToTabs) {
        final boolean convertTabs = prefUseTabs() && convertSpaceRunsToTabs;
        final int tabLen = prefTabSize();
        final StringBuilder ret = new StringBuilder();
        try {
            int spaces = 0;
            while (start < indent) {

                char ch = fDocument.getChar(start);
                if (ch == '\t') {
                    ret.append('\t');
                    spaces = 0;
                } else if (convertTabs) {
                    spaces++;
                    if (spaces == tabLen) {
                        ret.append('\t');
                        spaces = 0;
                    }
                } else {
                    ret.append(' ');
                }

                start++;
            }
            // remainder
            while (spaces-- > 0) {
                ret.append(' ');
            }
            
        } catch (BadLocationException e) {
        }
        
        return ret;
    }
    
    /**
     * Creates a string with a visual length of the given
     * <code>indentationSize</code>.
     *
     * @param buffer the original indent to reuse if possible
     * @param additional the additional indentation units to add or subtract to
     *        reference
     * @return the modified <code>buffer</code> reflecting the indentation
     *         adapted to <code>additional</code>
     */
    private StringBuilder createReusingIndent(StringBuilder buffer, int additional) {
        int refLength = computeVisualLength(buffer);
        int addLength = prefIndentationSize() * additional; // may be < 0
        int totalLength = Math.max(0, refLength + addLength);

        // copy the reference indentation for the indent up to the last tab
        // stop within the maxCopy area
        int minLength = Math.min(totalLength, refLength);
        int tabSize = prefTabSize();
        int maxCopyLength = tabSize > 0 ? minLength - minLength % tabSize : minLength; // maximum indent to copy
        stripExceedingChars(buffer, maxCopyLength);

        // add additional indent
        int missing = totalLength - maxCopyLength;
        final int tabs, spaces;
        if (CoreOptionIDs.SPACE.equals(getCoreFormatterOption(DefaultCodeFormatterConstants.FORMATTER_TAB_CHAR))) {
            tabs = 0;
            spaces = missing;
        } else if (CoreOptionIDs.TAB.equals(getCoreFormatterOption(DefaultCodeFormatterConstants.FORMATTER_TAB_CHAR))) {
            tabs = tabSize > 0 ? missing / tabSize : 0;
            spaces = tabSize > 0 ? missing % tabSize : missing;
        } else if (DefaultCodeFormatterConstants.MIXED.equals(getCoreFormatterOption(DefaultCodeFormatterConstants.FORMATTER_TAB_CHAR))) {
            tabs = tabSize > 0 ? missing / tabSize : 0;
            spaces = tabSize > 0 ? missing % tabSize : missing;
        } else {
            Assert.isTrue(false);
            return null;
        }
        for (int i = 0; i < tabs; i++) {
            buffer.append('\t');
        }
        for (int i = 0; i < spaces; i++) {
            buffer.append(' ');
        }
        return buffer;
    }
    
    /**
     * Returns the reference position regarding to indentation for <code>offset</code>,
     * or <code>NOT_FOUND</code>. This method calls
     * {@link #findReferencePosition(int, int, int) findReferencePosition(offset, nextChar, nDanglingSemicolons)} where
     * <code>nextChar</code> is the next character after <code>offset</code>.
     *
     * @param offset the offset for which the reference is computed
     * @return the reference statement relative to which <code>offset</code>
     *         should be indented, or {@link CALHeuristicScanner#NOT_FOUND}
     */
    public int findReferencePosition(int offset) {
        int peekChar = peekChar(offset);
        int nDanglingSemicolons = 0;
        
        if (peekChar == Symbols.TokenSEMICOLON) {
            nDanglingSemicolons = peekSemicolons(offset);
        }
        
        return findReferencePosition(offset, peekChar, nDanglingSemicolons);
    }
    
    /**
     * Peeks the next char in the document that comes after <code>offset</code>
     * on the same line as <code>offset</code>.
     *
     * @param offset the offset into document
     * @return the token symbol of the next element, or TokenEOF if there is none
     */
    private int peekChar(int offset) {
        if (offset < fDocument.getLength()) {
            try {
                IRegion line = fDocument.getLineInformationOfOffset(offset);
                int lineOffset = line.getOffset();
                int next = fScanner.nextToken(offset, lineOffset + line.getLength());
                return next;
            } catch (BadLocationException e) {
            }
        }
        return Symbols.TokenEOF;
    }
    
    /**
     * Find the number of semicolons in the document that comes after <code>offset</code>
     * on the same line as <code>offset</code>.
     *
     * @param offset the offset into document
     * @return the number of semicolons following the current offset on the same line.
     */
    private int peekSemicolons(int offset) {
        int nSemicolons = 0;
        while (offset < fDocument.getLength()) {
            try {
                IRegion line = fDocument.getLineInformationOfOffset(offset);
                int lineOffset = line.getOffset();
                int next = fScanner.nextToken(offset, lineOffset + line.getLength());
                
                // Update the offset for the next iteration of the loop.
                offset = fScanner.getPosition();
                
                if (next == Symbols.TokenSEMICOLON) {
                    nSemicolons++;
                } else {
                    return nSemicolons;
                }
            } catch (BadLocationException e) {
                return nSemicolons;
            }
        }
        return nSemicolons;
    }
    
    /**
     * Returns the reference position regarding to indentation for <code>position</code>,
     * or <code>NOT_FOUND</code>.
     *
     * Currently, if the next token is the first token on the line (i.e. only preceded by whitespace), 
     * the following tokens are specially handled:
     * <ul>
     *      <li><code>switch</code> labels are indented relative to the switch block</li>
     *      <li>opening curly braces are aligned correctly with the introducing code</li>
     *      <li>closing curly braces are aligned properly with the introducing code of
     *              the matching opening brace</li>
     *      <li>closing parenthesis' are aligned with their opening peer</li>
     *      <li>the <code>else</code> keyword is aligned with its <code>if</code>, anything
     *              else is aligned normally (i.e. with the base of any introducing statements).</li>
     *  <li>if there is no token on the same line after <code>offset</code>, the indentation
     *              is the same as for an <code>else</code> keyword</li>
     * </ul>
     *
     * @param offset the offset for which the reference is computed
     * @param nextToken the next token to assume in the document
     * @param nDanglingSemicolons if nextToken is a semicolon, the number of semicolons following the current position on the same line.
     * @return the reference statement relative to which <code>offset</code>
     *         should be indented, or {@link CALHeuristicScanner#NOT_FOUND}
     */
    public int findReferencePosition(int offset, int nextToken, int nDanglingSemicolons) {
        int danglingToken = Symbols.TokenOTHER;
        boolean unindent = false;
        boolean indent = false;
        boolean matchBrace = false;
        boolean matchParen = false;
        boolean matchBracket = false;

        // account for unindentation characters already typed in, but after position
        // if they are on a line by themselves, the indentation gets adjusted
        // accordingly
        //
        // also account for a dangling else
        if (offset < fDocument.getLength()) {
            try {
                IRegion line = fDocument.getLineInformationOfOffset(offset);
                int lineOffset = line.getOffset();
                int prevPos = Math.max(offset - 1, 0);
                boolean isFirstTokenOnLine = fDocument.get(lineOffset, prevPos + 1 - lineOffset).trim().length() == 0;
                fScanner.previousToken(prevPos, CALHeuristicScanner.UNBOUND);

                switch (nextToken) {
                    case Symbols.TokenIN:
                    case Symbols.TokenOF:
                    case Symbols.TokenDEFAULT:
                    case Symbols.TokenDERIVING:
                    case Symbols.TokenTHEN:
                    case Symbols.TokenELSE:
                        danglingToken = nextToken;
                        break;
                    case Symbols.TokenLBRACE:
                        // TODOEL: Do we want this?
                        if (prefIndentBracesForBlocks()) {
                            indent = true;
                        }
                        break;
                    case Symbols.TokenRBRACE: // closing braces get unindented
                        if (isFirstTokenOnLine) {
                            matchBrace = true;
                        }
                        break;
                    case Symbols.TokenRPAREN:
                        if (isFirstTokenOnLine) {
                            matchParen = true;
                        }
                        break;
                    case Symbols.TokenRBRACKET:
                        if (isFirstTokenOnLine) {
                            matchBracket = true;
                        }
                        break;
                    default:
                        // fall through
                        break;
                }
            } catch (BadLocationException e) {
            }
        }

        int ref = findReferencePosition(offset, danglingToken, matchBrace, matchParen, matchBracket, nDanglingSemicolons);
        if (unindent) {
            fIndent--;
        }
        if (indent) {
            fIndent++;
        }
        return ref;
    }
    
    /**
     * Returns the reference position regarding to indentation for <code>position</code>,
     * or <code>NOT_FOUND</code>.<code>fIndent</code> will contain the
     * relative indentation (in indentation units, not characters) after the
     * call. If there is a special alignment (e.g. for a method declaration
     * where parameters should be aligned), <code>fAlign</code> will contain
     * the absolute position of the alignment reference in <code>fDocument</code>,
     * otherwise <code>fAlign</code> is set to <code>CALHeuristicScanner.NOT_FOUND</code>.
     *
     * @param offset the offset for which the reference is computed
     * @param danglingToken a token which follows a partner token, to assume dangling at <code>position</code>
     *   for instance, a dangling "else" follows a partner "if".
     *   Current dangling tokens: in, deriving, then, else.
     *   Pass in Symbols.TokenOTHER if no dangler.
     *   
     * @param matchBrace whether the position of the matching brace should be
     *            returned instead of doing code analysis
     * @param matchParen whether the position of the matching parenthesis
     *            should be returned instead of doing code analysis
     * @param matchBracket whether the position of the matching bracket should be
     *            returned instead of doing code analysis
     * @param nDanglingSemicolons if nextToken is a semicolon, the number of semicolons following the current position on the same line.
     * @return the reference statement relative to which <code>position</code>
     *         should be indented, or {@link CALHeuristicScanner#NOT_FOUND}
     */
    public int findReferencePosition(int offset, int danglingToken, boolean matchBrace, boolean matchParen, boolean matchBracket, int nDanglingSemicolons) {
        fIndent = 0; // the indentation modification
        fAlign = CALHeuristicScanner.NOT_FOUND;
        fPosition = offset;

        // forward cases
        // an unindentation happens sometimes if the next token is special, 
        // namely on braces, brackets, and parens, but only if they are the first token on the line.
        if (matchBrace) {
            if (skipScope(Symbols.TokenLBRACE, Symbols.TokenRBRACE)) {
                setFirstElementAlignment(fPreviousPos - 1, fPreviousPos + 1);
                return fPosition;
            } else {
                // if we can't find the matching brace, the heuristic is to unindent
                // by one against the normal position
                int pos = findReferencePosition(offset, danglingToken, false, matchParen, matchBracket, nDanglingSemicolons);
                fIndent--;
                return pos;
            }
        }

        // align parentheses
        if (matchParen) {
            if (skipScope(Symbols.TokenLPAREN, Symbols.TokenRPAREN)) {
                return fPosition;
            } else {
                // if we can't find the matching paren, the heuristic is to unindent
                // by one against the normal position
                int pos = findReferencePosition(offset, danglingToken, matchBrace, false, matchBracket, nDanglingSemicolons);
                fIndent--;
                return pos;
            }
        }
        
        // align brackets
        if (matchBracket) {
            if (skipScope(Symbols.TokenLBRACKET, Symbols.TokenRBRACKET)) {
                return fPosition;
            } else {
                // if we can't find the matching paren, the heuristic is to unindent
                // by one against the normal position
                int pos = findReferencePosition(offset, danglingToken, matchBrace, matchParen, false, nDanglingSemicolons);
                fIndent--;
                return pos;
            }
        }
        
        // Handle dangling semicolons.
        if (nDanglingSemicolons > 0) {
            for (int i = 0; i < nDanglingSemicolons - 1; i++) {
                skipToSemicolonInitiator(false);
            }
            
            return skipToSemicolonInitiator(true);
        }
        
        // Skip over the next token which matches a provided dangling token. 
        // No effect if the current token when calling this method is not a partnered dangling token. 
        int pos = fPosition;
        switch (danglingToken) {
            case Symbols.TokenIN:
                // align with let.
                fToken = danglingToken;
                if (skipNextLET()) {
                    return fPosition;
                }
                break;
                
            case Symbols.TokenDERIVING:
                // indent wrt data.
                fToken = danglingToken;
                if (skipScope(Symbols.TokenDATA, Symbols.TokenDERIVING)) {
                    fIndent++;
                    return fPosition;
                }
                break;
                
            case Symbols.TokenDEFAULT:
                // indent wrt class method declaration.
                fToken = danglingToken;
                if (skipToSemicolonInitiator(false) != pos) {
                    fIndent++;
                    return fPosition;
                }
                break;
                
            case Symbols.TokenTHEN:
            case Symbols.TokenELSE:

                fToken = danglingToken;
                if (skipNextIF()) {
                    // align with "if".
                    // align with "else" if "else-if".

                    // The current fPosition is the position of the if.
                    // To handle else - if:
                    //   If the if is preceded by an else on the same line, align with the else.
                    
                    int ifPosition = fPosition;
                    int ifLine = getLine(fPosition);
                    
                    nextToken();
                    int nextTokenLine = getLine(fPosition);
                    
                    if (!(fToken == Symbols.TokenELSE && nextTokenLine == ifLine)) {
                        fPosition = ifPosition;
                    
                    } else {
                        // the case of an else - if
                        // fPosition is the already position of the else
                    }
                    
                    // align.
                    return setFirstElementAlignment(fPosition - 1, fPosition + 1);
                }
                break;

            default:
                // fall through.
        }
        fPosition = pos;
        
        // Something self-contained, like an expression or a type.
        // eg. stuff between parens, a cons or var identifier.  Not a single operator (like a barbar).
        boolean skippedAtomic = false;

        nextToken();
        switch (fToken) {
            
            /*
             * Tokens after which it is not customary to have a newline
             */

            // module header
            case Symbols.TokenMODULE:
            case Symbols.TokenFRIEND:
            case Symbols.TokenIMPORT:
                
            case Symbols.TokenDATA:
            case Symbols.TokenDERIVING:
            case Symbols.TokenPRIMITIVE:

                // using clause
            case Symbols.TokenTYPECLASS:
            case Symbols.TokenFUNCTION:
            case Symbols.TokenDATACONSTRUCTOR:
            case Symbols.TokenTYPECONSTRUCTOR:
                
                // instance defn
            case Symbols.TokenCLASS:
            case Symbols.TokenINSTANCE:
                
                // access modifiers
            case Symbols.TokenPUBLIC:
            case Symbols.TokenPROTECTED:
            case Symbols.TokenPRIVATE:
                fIndent = 1;
                return fPosition;
                
            /*
             * Tokens after which a newline is not unexpected.
             */
            
            // Foreign import
            case Symbols.TokenFOREIGN:
            case Symbols.TokenUNSAFE:
            case Symbols.TokenJVM:
                fIndent = 1;
                return fPosition;
                
            case Symbols.TokenSEMICOLON:
                return skipToSemicolonInitiator(false);

            // scope introduction: special treat who special is
            case Symbols.TokenLPAREN:
            case Symbols.TokenLBRACE:
            case Symbols.TokenLBRACKET:
                return handleScopeIntroduction(offset + 1);

            case Symbols.TokenEOF:
                // trap when hitting start of document
                return CALHeuristicScanner.NOT_FOUND;

            case Symbols.TokenEQUAL:
                // indent assignments
                fIndent = prefAssignmentIndent();
                return fPosition;

            // indentation for blockless introducers:
            case Symbols.TokenLET:
            case Symbols.TokenIN:
            case Symbols.TokenIF:
            case Symbols.TokenTHEN:
            case Symbols.TokenELSE:
            case Symbols.TokenCASE:
            case Symbols.TokenWHERE:
            case Symbols.TokenUSING:
                fIndent = prefSimpleIndent();
                return fPosition;
            
            case Symbols.TokenOF:
                fIndent = 0;            // Normally no indent.  Preference?
                return fPosition;

            case Symbols.TokenCOLON:
                pos = fPosition;
                nextToken();
                if (fToken == Symbols.TokenCOLON && (fPosition == pos - 1)) {
                    // colon colon.  Not customary to have a newline.
                    fIndent = 1;
                    return fPosition;
                
                } else {
                    // List.
                    fPosition = pos;
                    break;
                }
                
            /*
             * Other stuff
             */
            case Symbols.TokenCONSIDENT:
                pos = fPosition;
                int previousPos = fPreviousPos;
                if (looksLikeDataOrTypeConstructorDeclaration()) {
                    // Indent 1 wrt a data or type constructor declaration
                    fIndent = 1;
                    return pos;
                }
                
                fPosition = previousPos;
                skippedAtomic = true;
                skipMaybeQualifiedCons();
                
                break;
                
            case Symbols.TokenBAR:
                // One of:
                //   record         {r | ...}
                //   data           data Foo = Foo | Bar;
                //   pat group      case foo of (Foo | Bar) -> Baz;
                //   barbar logic   True || False
                
                pos = fPosition;
                nextToken();
                if (fToken == Symbols.TokenBAR && (fPosition == pos - 1)) {
                    // bar-bar, ie. logical or.

                    // fall through to default.
                    break;
                }
                
                fPosition = pos;
                return skipToSingleBarIntroducer();
                
            case Symbols.TokenGREATERTHAN:      // '>'
                // look for "->"
                pos = fPosition;
                nextToken();
                if (fToken == Symbols.TokenDASH && (fPosition == pos - 1)) {
                    // Right arrow.
                    
                    // check for instance or type expr arrow.
                    pos = fPosition;
                    if (looksLikeTypeArrow()) {
                        fPosition = pos;
                        break;
                    }
                    
                    // Not a type arrow.  Maybe a case alt or a lambda.
                    // Indent by one.
                    fIndent = 1;
                    return pos;
                
                } else if (fToken == Symbols.TokenEQUAL) {
                    // => implies
                    // Class or type context.
                    fIndent = 1;
                    return fPosition;

                } else {
                    // "Greater than" operator.
                    break;
                }

            case Symbols.TokenRBRACE:           // record or dc arg bindings.
            case Symbols.TokenRBRACKET:         // list
            case Symbols.TokenRPAREN:
                // skip the scope closed by this token.
                if (!skipScope()) {
                    return fPosition;
                }
                
                // fall through to default.
                skippedAtomic = true;
                break;
                
            case Symbols.TokenCOMMA:
                // inside a list of some type
                // easy if there is already a list item before with its own indentation - we just align
                // if not: take the start of the list ( LPAREN, LBRACE, LBRACKET ) and either align or
                // indent by list-indent
                return skipToFirstListItemOrScopeOpenerOnPreviousLine(false);
                
            default: {
                // inside something else.  Among other possibilities, could be a function call (eg. "foo bar (newline) baz")
                    
                // Align with any scope introducer on the previous line.
                // Otherwise just align with the line itself
                break;
            }
        }
        
        // The default case.
        
        // Check for something with an atomic type.
        if (skippedAtomic) {
            pos = fPosition;
            nextToken();
            
            if (fToken == Symbols.TokenCOLON) {
                int colonPos = fPosition;
                nextToken();
                
                if (fToken == Symbols.TokenCOLON && (fPosition == colonPos - 1)) {
                    // colon-colon
                    
                    // Two possibilities:
                    //    1) type signature
                    //          fname :: (type -> type);
                    //    2) data constructor arg
                    //          DC arg :: (type -> type);
                    
                    // Attempt to detect a type signature by reading back two more tokens, and looking for a semicolon.
                    nextToken();
                    nextToken();
                    
                    if (fToken == Symbols.TokenSEMICOLON) {
                        // align with the item after the colon-colon.
                        setFirstElementAlignment(pos - 1, pos + 1);
                        return pos;
                    }
                    
                    // align with the line
                    fPosition = colonPos;
                    return skipToFirstListItemOrScopeOpenerOnPreviousLine(true);
                }
                // fall through
            }
            
            fPosition = pos;
            // fall through
        }
        
        return skipToFirstListItemOrScopeOpenerOnPreviousLine(false);
    }
    
    /**
     * Call this when a right-arrow has just been read to determine using heuristics whether the
     * arrow is a type arrow, or some other arrow (eg. the arrow for a case alt or lambda).
     * @return true if the right-arrow seems to be a type arrow.  False otherwise.
     */
    private boolean looksLikeTypeArrow() {
        /*
         * Case alt    Foo -> expr;
         * lambda      \x y -> expr;
         * type expr   expr :: a -> b;
         * instance    (.. , ..) => Foo (a -> b);
         */
        
        // Look for a semicolon or colon colon.
        while (true) {
            nextToken();
            switch (fToken) {
                // scopes: skip them
                case Symbols.TokenRPAREN:
                case Symbols.TokenRBRACKET:
                case Symbols.TokenRBRACE:
                    skipScope();
                    break;
                    
                case Symbols.TokenLPAREN:
                    return true;
                    
                case Symbols.TokenCOLON:
                    int pos = fPosition;
                    nextToken();
                    if (fToken == Symbols.TokenCOLON && (fPosition == pos - 1)) {
                        return true;
                    }

                case Symbols.TokenOF:
                case Symbols.TokenBACKSLASH:
                case Symbols.TokenSEMICOLON:
                case Symbols.TokenEOF:
                    return false;
            }
        }
    }

    /**
     * Call this when a cons identifier has just been read to determine using heuristics whether we seem 
     * to be in a data type declaration, and the identifier is a data or type constructor being defined.
     * @return true if the cons identifier seems to be a data or type constructor being defined.  False otherwise.
     */
    private boolean looksLikeDataOrTypeConstructorDeclaration() {
        /*
         * data public FooType =              <--- want indent after
         *    protected DC
         *        argName :: Foo              <--- don't want indent after
         *        argName2 :: [Foo] |
         *    protected DC2 
         *        argName :: Foo              <--- don't want indent after
         *        argName2 :: [Foo] |
         *    ...
         */
        
        // Conditions when scanning backwards:
        // 1) If we reach a colon-colon, must have already seen a bar.
        //    Never see a bar-bar or single colon.
        // 2) See a "data" before a semicolon or eof.

        boolean sawBar = false;
        while (true) {
            nextToken();
            switch (fToken) {
                // scopes: skip them
                case Symbols.TokenRPAREN:
                case Symbols.TokenRBRACKET:
                case Symbols.TokenRBRACE:
                    skipScope();
                    break;
                    
                case Symbols.TokenLPAREN:
                case Symbols.TokenLBRACKET:
                case Symbols.TokenLBRACE:
                case Symbols.TokenSEMICOLON:
                case Symbols.TokenEOF:
                    return false;
                
                case Symbols.TokenCOLON:
                    int pos = fPosition;
                    nextToken();
                    if (fToken != Symbols.TokenCOLON && (fPosition == pos - 1)) {
                        // single colon (list constructor).
                        return false;
                    }
                    // colon-colon
                    
                    if (!sawBar) {
                        // reached a colon-colon before a bar.
                        return false;
                    }
                    break;
                    
                case Symbols.TokenBAR:
                    pos = fPosition;
                    nextToken();
                    if (fToken == Symbols.TokenBAR && (fPosition == pos - 1)) {
                        // bar-bar
                        return false;
                    }
                    
                    // Single bar.  Necessary but not sufficient condition.
                    fPosition = pos;
                    sawBar = true;
                    break;
                
                case Symbols.TokenDATA:
                    return true;
            }
        }
    }

    /**
     * Call this when an equals has just been read to determine using heuristics whether the
     * equals seems to exist with a field assignment.
     * @return true if the equals sign seems to be part of a field assignment.  False otherwise.
     */
    private boolean looksLikeFieldAssignment() {
        Assert.isTrue(fToken == Symbols.TokenEQUAL);

        // Look for an opening brace or a semicolon.
        while (true) {
            nextToken();
            switch (fToken) {
                // scopes: skip them
                case Symbols.TokenRPAREN:
                case Symbols.TokenRBRACKET:
                case Symbols.TokenRBRACE:
                    skipScope();
                    break;
                    
                case Symbols.TokenLBRACE:
                    return true;
                    
                case Symbols.TokenSEMICOLON:
                case Symbols.TokenEOF:
                    return false;
            }
        }
    }

    /**
     * Call this when a single bar (not bar-bar) has just been read.
     * Skips to the token which introduces the cal construct (eg. data declaration ) allowing a single bar.
     * In a record, the introducer is considered to be the first token *inside* the brace.
     * The cursor (<code>fPosition</code>) is set to the offset of the  token.
     * @return the position of the introducer.
     */
    private int skipToSingleBarIntroducer() {
        // One of:
        //   record         {r | ...}
        //   data           data Foo = Foo | Bar;
        //   pat group      case foo of (Foo | Bar) -> Baz;
        //   barbar logic   True || False

        // Look for a scope opener or equals sign.
        while (true) {
            nextToken();
            switch (fToken) {
                // scopes: skip them
                case Symbols.TokenRPAREN:
                case Symbols.TokenRBRACKET:
                case Symbols.TokenRBRACE:
                    skipScope();
                    break;
                    
                case Symbols.TokenEQUAL:
                case Symbols.TokenLPAREN:
                case Symbols.TokenLBRACKET:
                case Symbols.TokenLBRACE:
                    return fPreviousPos;
                    
                case Symbols.TokenEOF:
                    // bail out with current position
                    return fPosition;
            }
        }
    }

    /**
     * @param position the position in the current document (fDocument)
     * @return the line in which the position occurs, or -1 if it does not correspond to a position in the document.
     */
    private int getLine(int position) {
        try {
            return fDocument.getLineOfOffset(fPosition);
        } catch (BadLocationException e) {
            return -1;
        }
    }
    
    /**
     * Returns the reference position for a list element or scope opener. The algorithm
     * tries to match any previous indentation on the same list. If there is none,
     * the reference position returned is determined depending on the type of list.
     *
     * @return the reference position for a list item: either a previous list item
     * that has its own indentation, or the list introduction start.
     */
    private int skipToFirstListItemOrScopeOpenerOnPreviousLine(boolean matchColonColon) {
        
        int startLine = getLine(fPosition);
        int startPosition = fPosition;
        
        boolean detectedFieldAssignment = false;
        
        while (true) {
            nextToken();
            int line = getLine(fPosition);

            // if any line item comes with its own indentation, adapt to it
            if (line < startLine) {
                try {
                    int lineOffset = fDocument.getLineOffset(startLine);
                    int bound = Math.min(fDocument.getLength(), startPosition + 1);
                    fAlign = fScanner.findNonWhitespaceForwardInAnyPartition(lineOffset, bound);
                } catch (BadLocationException e) {
                    // ignore and return just the position
                }
                return startPosition;
            }

            switch (fToken) {
                // scopes: skip them
                case Symbols.TokenRPAREN:
                case Symbols.TokenRBRACKET:
                case Symbols.TokenRBRACE:
                    skipScope();
                    break;

                    // record type, eg. {r | #1 :: foo, #2 :: bar}
                    // Note also:
                    //  data decl:   data Foo = Foo | Bar;
                    //  pat group:   (Foo | Bar | ...) -> altexpr;
                    //  logical:     True || bar;
                    
//                case Symbols.TokenBAR:
                    
                // scope introduction: align with the first token after the scope introducer.
                case Symbols.TokenLPAREN:
                case Symbols.TokenLBRACE:
                case Symbols.TokenLBRACKET:
                    
                    // Same for deriving / using clauses.
                case Symbols.TokenDERIVING:
                case Symbols.TokenUSING:
                    setFirstElementAlignment(fPreviousPos - 1, fPreviousPos + 1);
                    return fPosition;

                case Symbols.TokenCOLON:
                    if (!matchColonColon) {
                        break;
                    }
                    int pos = fPosition;
                    int previousPos = fPreviousPos;
                    
                    nextToken();
                    
                    if (fToken == Symbols.TokenCOLON && (fPosition == pos - 1)) {
                        setFirstElementAlignment(previousPos - 1, previousPos + 1);
                        return pos;
                    }
                    
                    // Single colon (list constructor).  Keep going.
                    fPosition = pos;
                    break;
                    
                case Symbols.TokenSEMICOLON:
                    return fPosition;

                    // Records:   { foo = bar, 
                    //              baz = cuz }
                    // Lists:     foo : 
                    //            bar : baz : cuz;
                    // Tuples:    (foo, 
                    //             bar, baz, cuz)
                    // Functions: foo (bar 
                    //                 (baz (cuz)))
                    //            foo
                    //              bar
                    //              (baz cuz)
                case Symbols.TokenGREATERTHAN:
                    // Handle implies "=>".
                    // special case to skip equals if it's followed by a greater-than (implies: "=>")
                    pos = fPosition;
                    previousPos = fPreviousPos;
                    nextToken();
                    if (fToken == Symbols.TokenEQUAL && (fPosition == pos - 1)) {
                        setFirstElementAlignment(previousPos - 1, previousPos + 1);
                        return fPosition;
                    }
                    fPosition = pos;
                    break;
                    
                case Symbols.TokenEQUAL:
                    // If it looks like a field assignment, keep going.
                    // eg. something like {foo = bar, baz = qux}.
                    pos = fPosition;
                    previousPos = fPreviousPos;
                    
                    // See if it looks like we are in a field assignment.
                    detectedFieldAssignment |= looksLikeFieldAssignment();
                    fPosition = pos;
                    fPreviousPos = previousPos;
                    
                    if (detectedFieldAssignment) {
                        break;
                    }
                    
                    // Probably some kind of declaration.  Align with the equals.
                    setFirstElementAlignment(fPreviousPos - 1, fPreviousPos + 1);
                    return fPosition;
                    
                case Symbols.TokenEOF:
                    return 0;

            }
        }
    }
    

    /**
     * If the current token is an unqualified cons, read it.
     * If the current token is the last component in a qualified cons, read the unqualified cons, the dot, and the qualification.
     */
    private void skipMaybeQualifiedCons() {
        
        // DOT:
        //   Qualified cons.
        //   Qualified var.
        //   field selection
        //   decimal
        int pos1 = fPosition;
        nextToken();
        if (fToken != Symbols.TokenCONSIDENT) {
            fPosition = pos1;
            return;
        }
        
        int pos2 = fPosition;
        nextToken();
        if (fToken != Symbols.TokenPERIOD) {
            fPosition = pos2;
            return;
        }

        // (Not a cons) - dot - (cons ident)
        nextToken();
        if (fToken != Symbols.TokenCONSIDENT) {
            fPosition = pos1;
            return;
        }
        
    }

    /**
     * Call this within a foreign declaration to attempt to skip to the beginning of the declaration.
     * @return the position of the introducer.
     */
    private int skipToForeignDeclarationStart() {
        
        int pos = fPosition;
        while (true) {
            nextToken();
            switch (fToken) {
                case Symbols.TokenEOF:
                    // Invalid.  Bail out with current position.
                    return fPosition;
                    
                case Symbols.TokenUNSAFE:
                case Symbols.TokenJVM:
                case Symbols.TokenIMPORT:
                    break;
                
                case Symbols.TokenFOREIGN:
                    nextToken();
                    
                    // If the preceding token is "data", return that.  Otherwise return "foreign".
                    if (fToken == Symbols.TokenDATA) {
                        return fPosition;
                    }
                    
                    return fPreviousPos;
                
                default:
                    return pos;
            }
        }
    }
    
    /**
     * Skips to the start of a declaration or expression which is terminated by the semicolon at the current position.
     * @param setIndent whether indent should be set according to any cal construct which is detected.
     * @return the position of the semicolon initiator.
     */
    private int skipToSemicolonInitiator(boolean setIndent) {
        boolean firstLoopIteration = true;
        boolean followedByEquals = false;  // true if we encountered an equals.
        
        /*
         * true until we don't encounter a semicolon or "of" or "where" keyword.
         * Used in handling multiple semicolons.
         * example:
         *     case x of
         *     y -> z;
         *     ;
         * When called after the last semicolon, the call to skip..() for the alt-skipping semicolon will set fPosition when it encounters the "of" token.
         * This flag is used to tell the call for the outer semicolon to ignore the "of" token when that call returns.
         */
        boolean skipNextSemicolonOfOrWhere = true;
        
        // true if encountered an arrow, and it hasn't been proven to not be an alt arrow.
        // If we encounter a semicolon and this is true, we assume we are in a case alt and indent one.
        // right arrow:
        //   case alt      Foo -> expr;
        //   lambda        \ args -> expr                  - preceded by \
        //   type appl     exprOrFn :: (type with arrow)   - preceded by ::
        //   instance      ( , ) => Foo (type with arrow)  - always inside parens
        boolean couldBeFollowedByAltArrow = false;  
        
        while (true) {
            nextToken();
            boolean sawSemicolon = false;
            
            switch(fToken) {
                /*
                 * Symbols which always start something ending in a semicolon.
                 */

                // module declaration
                case Symbols.TokenMODULE:
                    
                // friend declaration
                case Symbols.TokenFRIEND:
                    
                // primitive declaration
                case Symbols.TokenPRIMITIVE:
                    
                // using clause
                case Symbols.TokenTYPECLASS:
                case Symbols.TokenFUNCTION:
                case Symbols.TokenDATACONSTRUCTOR:
                case Symbols.TokenTYPECONSTRUCTOR:
                
                // if expression
//                case Symbols.TokenIF:
                    return fPosition;

                case Symbols.TokenTHEN:
                case Symbols.TokenELSE:
                    int pos = fPosition;
                    
                    if (!skipNextIF()) {
                        fPosition = pos;
                    }
                    
                    break;
                    
                // data declaration
                case Symbols.TokenDATA:
                
                // instance defn
                case Symbols.TokenINSTANCE:
                    if (setIndent) {
                        fIndent = 1;
                    }
                    return fPosition;
                    

                /*
                 * Tokens which start semicolon-delimited lists.
                 */
                case Symbols.TokenLET:
                    if (setIndent) {
                        fIndent = 1;
                    }
                    return fPreviousPos;
                    
                /*
                 * Other tokens
                 */

                case Symbols.TokenFOREIGN:
                case Symbols.TokenUNSAFE:
                case Symbols.TokenJVM:
                    // Must be a foreign import.
                    return skipToForeignDeclarationStart();
                    
                case Symbols.TokenIMPORT:
                    // Either a foreign import or a module import.
                    pos = fPosition;
                    nextToken();
                    
                    if (fToken == Symbols.TokenUNSAFE) {
                        return skipToForeignDeclarationStart();
                    }
                    
                    if (setIndent) {
                        fIndent = 1;
                    }
                    
                    // Assume a module import.
                    fPosition = pos;
                    return pos;
                
                case Symbols.TokenCLASS:
                    if (setIndent) {
                        fIndent = 1;
                    }
                    
                    // return this token or access modifier.
                    pos = fPosition;
                    nextToken();
                    switch (fToken) {
                        case Symbols.TokenPUBLIC:
                        case Symbols.TokenPROTECTED:
                        case Symbols.TokenPRIVATE:
                            return fPosition;

                        default:
                            return pos;
                    }
                    
                case Symbols.TokenWHERE:  
                    // matches "(public) class" or "instance"
                    if (!skipNextSemicolonOfOrWhere) {
                        return fPreviousPos;
                    }
                    break;
//                case Symbols.TokenCASE:
//                    if (setIndent) {
//                        setFirstElementAlignment(fPosition - 1, fPosition + 1);
//                        return fPosition;
//                    }
//                    break;
                    
                case Symbols.TokenOF:
                    // Precedes the first alt (other alts are preceded by another semicolon).
                    if (!skipNextSemicolonOfOrWhere) {
                        if (setIndent) {
                            fIndent = 1;
                        }
                        return fPreviousPos;
                    }
                    break;
                
                case Symbols.TokenIN:
                    pos = fPreviousPos;
                    if (!skipNextLET()) {
                        // give up
                        return pos;
                    }
                    
                    // keep looking.
                    break;
                    
//                case Symbols.TokenUSING:
//                    pos = fPreviousPos;
//                    if (skipScope(Symbols.TokenIMPORT, Symbols.TokenUSING)) {
//                        return fPosition;
//                    }
//                    return pos;
                case Symbols.TokenDERIVING:
                    // Keep looking for the "data" token.

                case Symbols.TokenPUBLIC:
                case Symbols.TokenPROTECTED:
                case Symbols.TokenPRIVATE:
                    // These can occur in many places.  For now, treat like TokenIDENT.  ie. keep searching.
                    break;
                
                /*
                 * Other symbols..
                 */
                // => :: ->
                //  ! \ | && || ++ $ # ` _ ; : , = < >
                case Symbols.TokenRBRACE:
                case Symbols.TokenRBRACKET:
                case Symbols.TokenRPAREN:
                    pos = fPreviousPos;
                    if (skipScope()) {
                        break;
                    } else {
                        // Unterminated -- give up.
                        return pos;
                    }
                case Symbols.TokenEQUAL:
                    followedByEquals = true;
                    break;
                    
                case Symbols.TokenGREATERTHAN:      // '>'
                    // look for "->"
                    pos = fPosition;
                    nextToken();
                    
                    if (fToken == Symbols.TokenDASH && (fPosition == pos - 1)) {
                        // Right arrow.
                        couldBeFollowedByAltArrow = true;
                    
                    } else if (fToken == Symbols.TokenEQUAL && (fPosition == pos - 1)) {
                        // Don't reset fPosition - consume the token.

                    } else {
                        // "Greater than" operator.
                        fPosition = pos;
                    }
                    break;
                    
                case Symbols.TokenLBRACE:       // record or dc arg binding
                case Symbols.TokenLBRACKET:     // list
                case Symbols.TokenLPAREN:       // something parenthesized.
                case Symbols.TokenEOF:
                    followedByEquals = false;
                    return fPreviousPos;

                case Symbols.TokenBACKSLASH:
                    couldBeFollowedByAltArrow = false;
                    break;
                
                case Symbols.TokenSEMICOLON:
                    sawSemicolon = true;
                    
                    if (firstLoopIteration || skipNextSemicolonOfOrWhere) {
                        fPosition = skipToSemicolonInitiator(false);
                        break;
                    }
                    
                    if (setIndent && (couldBeFollowedByAltArrow || followedByEquals)) {
                        fIndent = 1;
                    }
                    
                    return fPreviousPos;
                    
                
                case Symbols.TokenCOLON:
                    pos = fPosition;
                    nextToken();
                    if (fToken == Symbols.TokenCOLON && (fPosition == pos - 1)) {
                        couldBeFollowedByAltArrow = false;
                    } else {
                        fPosition = pos;
                    }
                    break;
                    // todo
                    
                    // If colon but not coloncolon, it's a list or case list extractor pattern.
                    // If coloncolon, could be:
                    //  1) type declaration
                    //  2) data cons arg declaration
                    //  3) class method declaration
                    //  4) type signature after an expression
                    //  5) record type declaration within a type declaration
                    
                    // So:
                    //  Look at next token.
                    //    If not identifier, 4
                    //  Look at next token.
                    //    If visibility thing, 3.
                    //    If '#', 2 or 5.
                    //      If next token is ';', 2.
                    //      If next token is ',', 5.
                    //    If ',', 5
                    //    If ';', 1-4 (doesn't matter).
                    //    Else 4.
                    
                default:
                    // keep searching
                    break;
            }
            
            firstLoopIteration = false;
            skipNextSemicolonOfOrWhere &= sawSemicolon;
        }
    }

    /**
     * Skips a scope and positions the cursor (<code>fPosition</code>) on the token that opens the scope. Returns
     * <code>true</code> if a matching peer could be found, <code>false</code> otherwise. The current token when
     * calling must be one out of <code>Symbols.TokenRPAREN</code>, <code>Symbols.TokenRBRACE</code>, and
     * <code>Symbols.TokenRBRACKET</code>.
     * 
     * @return <code>true</code> if a matching peer was found, <code>false</code> otherwise
     */
    private boolean skipScope() {
        switch (fToken) {
            case Symbols.TokenRPAREN:
                return skipScope(Symbols.TokenLPAREN, Symbols.TokenRPAREN);
            case Symbols.TokenRBRACKET:
                return skipScope(Symbols.TokenLBRACKET, Symbols.TokenRBRACKET);
            case Symbols.TokenRBRACE:
                return skipScope(Symbols.TokenLBRACE, Symbols.TokenRBRACE);
            default:
                Assert.isTrue(false);
            return false;
        }
    }
    
    /**
     * Scans tokens for the matching opening peer. The internal cursor
     * (<code>fPosition</code>) is set to the offset of the opening peer if found.
     *
     * @param openToken the opening peer token
     * @param closeToken the closing peer token
     * @return <code>true</code> if a matching token was found, <code>false</code>
     *         otherwise
     */
    private boolean skipScope(int openToken, int closeToken) {
        
        int depth = 1;

        while (true) {
            nextToken();

            if (fToken == closeToken) {
                depth++;
            } else if (fToken == openToken) {
                depth--;
                if (depth == 0) {
                    return true;
                }
            } else if (fToken == Symbols.TokenEOF) {
                return false;
            }
        }
    }
    
    /**
     * Handles the introduction of a new scope. The current token must be one out
     * of <code>Symbols.TokenLPAREN</code>, <code>Symbols.TokenLBRACE</code>,
     * and <code>Symbols.TokenLBRACKET</code>. Returns as the reference position
     * either the token introducing the scope or - if available - the first
     * java token after that.
     *
     * <code>fIndent</code> will be set to the number of indentation units.
     *
     * @param bound the bound for the search for the first token after the scope introduction.
     * @return the indent
     */
    private int handleScopeIntroduction(int bound) {
        switch (fToken) {
            // scope introduction: special treat who special is
            case Symbols.TokenLPAREN:
                // return the parenthesis as reference
                int pos = fPosition; // store
                fIndent = prefParenthesisIndent();
                return pos;

            case Symbols.TokenLBRACE:
                // return the brace as reference
                pos = fPosition; // store

                fIndent = prefBlockIndent();
                return pos;

            case Symbols.TokenLBRACKET:
                // return the bracket as reference
                pos = fPosition; // store
                fIndent = prefBracketIndent();
                return pos; // restore
                
            default:
                Assert.isTrue(false);
            return -1; // dummy
        }  
    }
    
    /**
     * Sets the deep indent offset (<code>fAlign</code>) to either the offset right after
     * <code>scopeIntroducerOffset</code> or - if available - the first CAL token after
     * <code>scopeIntroducerOffset</code>, but before <code>bound</code>.
     * 
     * @param scopeIntroducerOffset the offset of the scope introducer
     * @param bound the bound for the search for another element
     * @return the reference position
     */
    private int setFirstElementAlignment(int scopeIntroducerOffset, int bound) {
        int firstPossible = scopeIntroducerOffset + 1; // align with the first position after the scope intro
        fAlign = fScanner.findNonWhitespaceForwardInAnyPartition(firstPossible, bound);
        if (fAlign == CALHeuristicScanner.NOT_FOUND) {
            fAlign = firstPossible;
        }
        return fAlign;
    }
    
    
    /**
     * Skips over the next <code>if</code> keyword. The current token when calling
     * this method must be a <code>then</code> or <code>else</code> keyword. Returns <code>true</code>
     * if a matching <code>if</code> could be found, <code>false</code> otherwise.
     * The cursor (<code>fPosition</code>) is set to the offset of the <code>if</code>
     * token.
     *
     * @return <code>true</code> if a matching <code>if</code> token was found, <code>false</code> otherwise
     */
    private boolean skipNextIF() {
        Assert.isTrue(fToken == Symbols.TokenTHEN || fToken == Symbols.TokenELSE);
        
        while (true) {
            nextToken();
            switch (fToken) {
                // scopes: skip them
                case Symbols.TokenRPAREN:
                case Symbols.TokenRBRACKET:
                case Symbols.TokenRBRACE:
                    skipScope();
                    break;
                    
                case Symbols.TokenIF:
                    // found it, return
                    return true;

                case Symbols.TokenELSE:
                    // recursively skip (then/else)-if blocks
                    skipNextIF();
                    break;
                    
                    // shortcut scope starts
                case Symbols.TokenLPAREN:
                case Symbols.TokenLBRACE:
                case Symbols.TokenLBRACKET:
                case Symbols.TokenEOF:
                    return false;
            }
        }
    }

    /**
     * Skips over the next <code>let</code> keyword. The current token when calling
     * this method must be an <code>in</code> keyword. Returns <code>true</code>
     * if a matching <code>case</code> could be found, <code>false</code> otherwise.
     * The cursor (<code>fPosition</code>) is set to the offset of the <code>case</code>
     * token.
     *
     * @return <code>true</code> if a matching <code>let</code> token was found, <code>false</code> otherwise
     */
    private boolean skipNextLET() {
        Assert.isTrue(fToken == Symbols.TokenIN);
        
        while (true) {
            nextToken();
            switch (fToken) {
                // scopes: skip them
                case Symbols.TokenRPAREN:
                case Symbols.TokenRBRACKET:
                case Symbols.TokenRBRACE:
                    skipScope();
                    break;
                    
                case Symbols.TokenLET:
                    // found it, return
                    return true;
                case Symbols.TokenIN:
                    // recursively skip let-in blocks
                    skipNextLET();
                    break;
                    
                    // shortcut scope starts
                case Symbols.TokenLPAREN:
                case Symbols.TokenLBRACE:
                case Symbols.TokenLBRACKET:
                case Symbols.TokenEOF:
                    return false;
            }
        }
    }

    /**
     * Reads the next token in backward direction from the heuristic scanner
     * and sets the fields <code>fToken, fPreviousPosition</code> and <code>fPosition</code>
     * accordingly.
     */
    private void nextToken() {
        nextToken(fPosition);
    }
    
    /**
     * Reads the next token in backward direction of <code>start</code> from
     * the heuristic scanner and sets the fields <code>fToken, fPreviousPosition</code>
     * and <code>fPosition</code> accordingly.
     *
     * @param start the start offset from which to scan backwards
     */
    private void nextToken(int start) {
        fToken = fScanner.previousToken(start - 1, CALHeuristicScanner.UNBOUND);
        fPreviousPos = start;
        fPosition = fScanner.getPosition() + 1;
    }
    
    /**
     * Returns the possibly project-specific core preference defined under <code>key</code>.
     *
     * @param key the key of the preference
     * @return the value of the preference
     * @since 3.1
     */
    private String getCoreFormatterOption(String key) {
//        if (fProject == null)
            return CALEclipseCorePlugin.getOption(key);
//        return fProject.getOption(key, true);
    }
    
    private boolean prefUseTabs() {
        boolean useTabs;
        if (!isStandalone()) {
            useTabs = !CoreOptionIDs.SPACE.equals(getCoreFormatterOption(DefaultCodeFormatterConstants.FORMATTER_TAB_CHAR));
        } else {
            useTabs = true; // sensible default for testing
        }
        
        return useTabs;
    }
    
    private int prefTabSize() {
        return CodeFormatterUtil.getTabWidth(fProject);
    }
    
    private int prefIndentationSize() {
        return CodeFormatterUtil.getIndentWidth(fProject);
    }
    
    private int prefAssignmentIndent() {
        return prefBlockIndent();
    }
    
    private int prefSimpleIndent() {
        if (prefIndentBracesForBlocks() && prefBlockIndent() == 0) {
            return 1;
        } else {
            return prefBlockIndent();
        }
    }
    
    private int prefBracketIndent() {
        return prefBlockIndent();
    }
    
    
    private int prefParenthesisIndent() {
        return prefContinuationIndent();
    }
    
    /**
     * Returns <code>true</code> if the class is used outside the workbench,
     * <code>false</code> in normal mode
     *
     * @return <code>true</code> if the plug-ins are not available
     */
    private boolean isStandalone() {
        return CALEclipseCorePlugin.getDefault() == null;
    }
    private int prefBlockIndent() {
        if (!isStandalone()) {
            String option = getCoreFormatterOption(DefaultCodeFormatterConstants.FORMATTER_INDENT_STATEMENTS_COMPARE_TO_BLOCK);
            if (DefaultCodeFormatterConstants.FALSE.equals(option)) {
                return 0;
            }
        }
        
        return 1; // sensible default
    }
    
    private boolean prefIndentBracesForBlocks() {
        if (!isStandalone()) {
            return DefaultCodeFormatterConstants.NEXT_LINE_SHIFTED.equals(getCoreFormatterOption(DefaultCodeFormatterConstants.FORMATTER_BRACE_POSITION_FOR_BLOCK));
        }
        
        return false; // sensible default
    }
    
    private int prefContinuationIndent() {
        if (!isStandalone()) {
            try {
                return Integer.parseInt(getCoreFormatterOption(DefaultCodeFormatterConstants.FORMATTER_CONTINUATION_INDENTATION));
            } catch (NumberFormatException e) {
                // ignore and return default
            }
        }
        
        return 1; // sensible default
    }
    

    // ADE unused code!  commented out for deletion later
    
//    private int prefCaseBlockIndent() {
//        if (true)
//            return prefBlockIndent();
//        
//        if (!isStandalone()) {
//            if (DefaultCodeFormatterConstants.TRUE.equals(getCoreFormatterOption(DefaultCodeFormatterConstants.FORMATTER_INDENT_SWITCHSTATEMENTS_COMPARE_TO_CASES)))
//                return prefBlockIndent();
//            else
//                return 0;
//        }
//        return prefBlockIndent(); // sun standard
//    }
//    
//    private boolean prefMethodDeclDeepIndent() {
//        if (!isStandalone()) {
//            String option = getCoreFormatterOption(DefaultCodeFormatterConstants.FORMATTER_ALIGNMENT_FOR_PARAMETERS_IN_METHOD_DECLARATION);
//            try {
//                return DefaultCodeFormatterConstants.getIndentStyle(option) == DefaultCodeFormatterConstants.INDENT_ON_COLUMN;
//            } catch (IllegalArgumentException e) {
//                // ignore and return default
//            }
//        }
//        
//        return true;
//    }
//    
//    private int prefMethodDeclIndent() {
//        if (!isStandalone()) {
//            String option = getCoreFormatterOption(DefaultCodeFormatterConstants.FORMATTER_ALIGNMENT_FOR_PARAMETERS_IN_METHOD_DECLARATION);
//            try {
//                if (DefaultCodeFormatterConstants.getIndentStyle(option) == DefaultCodeFormatterConstants.INDENT_BY_ONE)
//                    return 1;
//                else
//                    return prefContinuationIndent();
//            } catch (IllegalArgumentException e) {
//                // ignore and return default
//            }
//        }
//        return 1;
//    }
//    
//    private boolean prefMethodCallDeepIndent() {
//        if (!isStandalone()) {
//            String option = getCoreFormatterOption(DefaultCodeFormatterConstants.FORMATTER_ALIGNMENT_FOR_ARGUMENTS_IN_METHOD_INVOCATION);
//            try {
//                return DefaultCodeFormatterConstants.getIndentStyle(option) == DefaultCodeFormatterConstants.INDENT_ON_COLUMN;
//            } catch (IllegalArgumentException e) {
//                // ignore and return default
//            }
//        }
//        return false; // sensible default
//    }
//    
//    private int prefMethodCallIndent() {
//        if (!isStandalone()) {
//            String option = getCoreFormatterOption(DefaultCodeFormatterConstants.FORMATTER_ALIGNMENT_FOR_ARGUMENTS_IN_METHOD_INVOCATION);
//            try {
//                if (DefaultCodeFormatterConstants.getIndentStyle(option) == DefaultCodeFormatterConstants.INDENT_BY_ONE)
//                    return 1;
//                else
//                    return prefContinuationIndent();
//            } catch (IllegalArgumentException e) {
//                // ignore and return default
//            }
//        }
//        
//        return 1; // sensible default
//    }
//    
//    private boolean prefParenthesisDeepIndent() {
//        
//        if (true) // don't do parenthesis deep indentation
//            return false;
//        
//        if (!isStandalone()) {
//            String option = getCoreFormatterOption(DefaultCodeFormatterConstants.FORMATTER_CONTINUATION_INDENTATION);
//            try {
//                return DefaultCodeFormatterConstants.getIndentStyle(option) == DefaultCodeFormatterConstants.INDENT_ON_COLUMN;
//            } catch (IllegalArgumentException e) {
//                // ignore and return default
//            }
//        }
//        
//        return false; // sensible default
//    }
//
//    private boolean prefTernaryDeepAlign() {
//        if (!isStandalone()) {
//            String option = getCoreFormatterOption(DefaultCodeFormatterConstants.FORMATTER_ALIGNMENT_FOR_CONDITIONAL_EXPRESSION);
//            try {
//                return DefaultCodeFormatterConstants.getIndentStyle(option) == DefaultCodeFormatterConstants.INDENT_ON_COLUMN;
//            } catch (IllegalArgumentException e) {
//                // ignore and return default
//            }
//        }
//        return false;
//    }
//
//    private int prefTypeIndent() {
//        if (!isStandalone()) {
//            String option = getCoreFormatterOption(DefaultCodeFormatterConstants.FORMATTER_INDENT_BODY_DECLARATIONS_COMPARE_TO_TYPE_HEADER);
//            if (DefaultCodeFormatterConstants.FALSE.equals(option))
//                return 0;
//        }
//        
//        return 1; // sensible default
//    }
//
//    private boolean prefIndentBracesForTypes() {
//        if (!isStandalone()) {
//            return DefaultCodeFormatterConstants.NEXT_LINE_SHIFTED.equals(getCoreFormatterOption(DefaultCodeFormatterConstants.FORMATTER_BRACE_POSITION_FOR_TYPE_DECLARATION));
//        }
//        
//        return false; // sensible default
//    }
//
//    private int prefArrayIndent() {
//        if (!isStandalone()) {
//            String option = getCoreFormatterOption(DefaultCodeFormatterConstants.FORMATTER_ALIGNMENT_FOR_EXPRESSIONS_IN_ARRAY_INITIALIZER);
//            try {
//                if (DefaultCodeFormatterConstants.getIndentStyle(option) == DefaultCodeFormatterConstants.INDENT_BY_ONE)
//                    return 1;
//            } catch (IllegalArgumentException e) {
//                // ignore and return default
//            }
//        }
//        
//        return prefContinuationIndent(); // default
//    }
//
//    private boolean prefArrayDeepIndent() {
//        if (!isStandalone()) {
//            String option = getCoreFormatterOption(DefaultCodeFormatterConstants.FORMATTER_ALIGNMENT_FOR_EXPRESSIONS_IN_ARRAY_INITIALIZER);
//            try {
//                return DefaultCodeFormatterConstants.getIndentStyle(option) == DefaultCodeFormatterConstants.INDENT_ON_COLUMN;
//            } catch (IllegalArgumentException e) {
//                // ignore and return default
//            }
//        }
//        
//        return true;
//    }
//
//    private int prefTernaryIndent() {
//        if (!isStandalone()) {
//            String option = getCoreFormatterOption(DefaultCodeFormatterConstants.FORMATTER_ALIGNMENT_FOR_CONDITIONAL_EXPRESSION);
//            try {
//                if (DefaultCodeFormatterConstants.getIndentStyle(option) == DefaultCodeFormatterConstants.INDENT_BY_ONE)
//                    return 1;
//                else
//                    return prefContinuationIndent();
//            } catch (IllegalArgumentException e) {
//                // ignore and return default
//            }
//        }
//        
//        return prefContinuationIndent();
//    }
//
//    private int prefCaseIndent() {
//        if (!isStandalone()) {
//            if (DefaultCodeFormatterConstants.TRUE.equals(getCoreFormatterOption(DefaultCodeFormatterConstants.FORMATTER_INDENT_SWITCHSTATEMENTS_COMPARE_TO_SWITCH)))
//                return prefBlockIndent();
//            else
//                return 0;
//        }
//        
//        return 0; // sun standard
//    }
//
//    private int prefMethodBodyIndent() {
//        if (!isStandalone()) {
//            if (DefaultCodeFormatterConstants.FALSE.equals(getCoreFormatterOption(DefaultCodeFormatterConstants.FORMATTER_INDENT_STATEMENTS_COMPARE_TO_BODY)))
//                return 0;
//        }
//        
//        return 1; // sensible default
//    }
//
//    private boolean prefIndentBracesForArrays() {
//        if (!isStandalone()) {
//            return DefaultCodeFormatterConstants.NEXT_LINE_SHIFTED.equals(getCoreFormatterOption(DefaultCodeFormatterConstants.FORMATTER_BRACE_POSITION_FOR_ARRAY_INITIALIZER));
//        }
//        
//        return false; // sensible default
//    }
//
//    private boolean prefIndentBracesForMethods() {
//        if (!isStandalone()) {
//            return DefaultCodeFormatterConstants.NEXT_LINE_SHIFTED.equals(getCoreFormatterOption(DefaultCodeFormatterConstants.FORMATTER_BRACE_POSITION_FOR_METHOD_DECLARATION));
//        }
//        
//        return false; // sensible default
//    }
//    
}