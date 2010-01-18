/*******************************************************************************
 * Copyright (c) 2006 Business Objects Software Limited and others.
 * All rights reserved. 
 * This file is made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Business Objects Software Limited - initial API and implementation based on Eclipse 3.1.2 code for
 *                             /org.eclipse.jdt.ui/ui/org/eclipse/jdt/internal/ui/text/java/JavaAutoIndentStrategy.java
 *                           Eclipse source is available at: http://www.eclipse.org/downloads/
 *******************************************************************************/

/*
 * CALAutoIndentStrategy.java
 * Creation date: Feb 10, 2006.
 * By: Edward Lam
 */
package org.openquark.cal.eclipse.ui.text.cal;

import org.eclipse.core.resources.IProject;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.DefaultIndentLineAutoEditStrategy;
import org.eclipse.jface.text.Document;
import org.eclipse.jface.text.DocumentCommand;
import org.eclipse.jface.text.DocumentRewriteSession;
import org.eclipse.jface.text.DocumentRewriteSessionType;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.TextUtilities;
import org.eclipse.jface.text.rules.FastPartitioner;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.texteditor.ITextEditorExtension3;
import org.openquark.cal.compiler.LanguageInfo;
import org.openquark.cal.eclipse.ui.CALEclipseUIPlugin;
import org.openquark.cal.eclipse.ui.preferences.PreferenceConstants;
import org.openquark.cal.eclipse.ui.text.CALHeuristicScanner;
import org.openquark.cal.eclipse.ui.text.CALIndenter;
import org.openquark.cal.eclipse.ui.text.CALPartitions;
import org.openquark.cal.eclipse.ui.text.FastCALPartitionScanner;
import org.openquark.cal.eclipse.ui.text.Symbols;
import org.openquark.cal.eclipse.ui.util.CodeFormatterUtil;


/**
 * Auto indent strategy sensitive to brackets.
 * @author Edward Lam
 */
public class CALAutoIndentStrategy extends DefaultIndentLineAutoEditStrategy {

    /** The line comment introducer. Value is "{@value}" */
    private static final String LINE_COMMENT = "//"; //$NON-NLS-1$

    // ADE unused!
//    private static class CompilationUnitInfo {
//
//        char[] buffer;
//        int delta;
//
//        CompilationUnitInfo(char[] buffer, int delta) {
//            this.buffer = buffer;
//            this.delta = delta;
//        }
//    }

//    private boolean fCloseBrace;
    private boolean fIsSmartMode;

    // ADE unused
//    private String fPartitioning;
    private final IProject fProject; // should be a cal project

    /**
     * Creates a new Java auto indent strategy for the given document partitioning.
     *
     * @param partitioning the document partitioning
     * @param project the project to get formatting preferences from, or null to use default preferences
     */
    public CALAutoIndentStrategy(String partitioning, IProject project) {
//        fPartitioning = partitioning;
        fProject = project;
    }

    // ADE unused!
//    private int getBracketCount(IDocument d, int startOffset, int endOffset, boolean ignoreCloseBrackets) throws BadLocationException {
//
//        int bracketCount = 0;
//        while (startOffset < endOffset) {
//            char curr = d.getChar(startOffset);
//            startOffset++;
//            switch (curr) {
//                case '/':
//                    if (startOffset < endOffset) {
//                        char next = d.getChar(startOffset);
//                        if (next == '*') {
//                            // a comment starts, advance to the comment end
//                            startOffset = getCommentEnd(d, startOffset + 1, endOffset);
//                        } else if (next == '/') {
//                            // '//'-comment: nothing to do anymore on this line
//                            startOffset = endOffset;
//                        }
//                    }
//                    break;
//                case '*':
//                    if (startOffset < endOffset) {
//                        char next = d.getChar(startOffset);
//                        if (next == '/') {
//                            // we have been in a comment: forget what we read before
//                            bracketCount = 0;
//                            startOffset++;
//                        }
//                    }
//                    break;
//                case '{':
//                    bracketCount++;
//                    ignoreCloseBrackets = false;
//                    break;
//                case '}':
//                    if (!ignoreCloseBrackets) {
//                        bracketCount--;
//                    }
//                    break;
//                case '"':
//                case '\'':
//                    startOffset = getStringEnd(d, startOffset, endOffset, curr);
//                    break;
//                default:
//            }
//        }
//        return bracketCount;
//    }

    // ----------- bracket counting ------------------------------------------------------

    // ADE unused!
//    private int getCommentEnd(IDocument d, int offset, int endOffset) throws BadLocationException {
//        while (offset < endOffset) {
//            char curr = d.getChar(offset);
//            offset++;
//            if (curr == '*') {
//                if (offset < endOffset && d.getChar(offset) == '/') {
//                    return offset + 1;
//                }
//            }
//        }
//        return endOffset;
//    }
//
//    private String getIndentOfLine(IDocument d, int line) throws BadLocationException {
//        if (line > -1) {
//            int start = d.getLineOffset(line);
//            int end = start + d.getLineLength(line) - 1;
//            int whiteEnd = findEndOfWhiteSpace(d, start, end);
//            return d.get(start, whiteEnd - start);
//        } else {
//            return ""; //$NON-NLS-1$
//        }
//    }
//
//    private int getStringEnd(IDocument d, int offset, int endOffset, char ch) throws BadLocationException {
//        while (offset < endOffset) {
//            char curr = d.getChar(offset);
//            offset++;
//            if (curr == '\\') {
//                // ignore escaped characters
//                offset++;
//            } else if (curr == ch) {
//                return offset;
//            }
//        }
//        return endOffset;
//    }

    private void smartIndentAfterNewLine(IDocument d, DocumentCommand c) {
        CALHeuristicScanner scanner = new CALHeuristicScanner(d);
        int docLength = d.getLength();
        if (c.offset == -1 || docLength == 0) {
            return;
        }

        CALIndenter indenter = new CALIndenter(d, scanner, fProject);
        StringBuilder indent = indenter.computeIndentation(c.offset);            // What the correct indentation should be.
        if (indent == null) {
            indent = new StringBuilder(); 
        }
                
        try {
            int p = c.offset;
            int line = d.getLineOfOffset(p);

            StringBuilder buf = new StringBuilder(c.text + indent);

            IRegion reg = d.getLineInformation(line);
            int lineEnd = reg.getOffset() + reg.getLength();

            int contentStart = findEndOfWhiteSpace(d, c.offset, lineEnd);
            c.length = Math.max(contentStart - c.offset, 0);

            c.text = buf.toString();

        } catch (BadLocationException e) {
            CALEclipseUIPlugin.log(e);
        }
    }

    // ADE unused!
//    /**
//     * Checks whether the content of <code>document</code> in the range (<code>offset</code>, <code>length</code>)
//     * contains the <code>new</code> keyword.
//     *
//     * @param document the document being modified
//     * @param offset the first character position in <code>document</code> to be considered
//     * @param length the length of the character range to be considered
//     * @param partitioning the document partitioning
//     * @return <code>true</code> if the specified character range contains a <code>new</code> keyword, <code>false</code> otherwise.
//     */
//    private static boolean isNewMatch(IDocument document, int offset, int length, String partitioning) {
//        assert (length >= 0);
//        assert (offset >= 0);
//        assert (offset + length < document.getLength() + 1);
//
//        try {
//            String text = document.get(offset, length);
//            int pos = text.indexOf("new"); //$NON-NLS-1$
//
//            while (pos != -1 && !isDefaultPartition(document, pos + offset, partitioning))
//                pos = text.indexOf("new", pos + 2); //$NON-NLS-1$
//
//            if (pos < 0)
//                return false;
//
//            if (pos != 0 && Character.isJavaIdentifierPart(text.charAt(pos - 1)))
//                return false;
//
//            if (pos + 3 < length && Character.isJavaIdentifierPart(text.charAt(pos + 3)))
//                return false;
//
//            return true;
//
//        } catch (BadLocationException e) {
//        }
//        return false;
//    }
//
//    /**
//     * Checks whether the content of <code>document</code> at <code>position</code> looks like an
//     * anonymous class definition. <code>position</code> must be to the left of the opening
//     * parenthesis of the definition's parameter list.
//     *
//     * @param document the document being modified
//     * @param position the first character position in <code>document</code> to be considered
//     * @param partitioning the document partitioning
//     * @return <code>true</code> if the content of <code>document</code> looks like an anonymous class definition, <code>false</code> otherwise
//     */
//    private static boolean looksLikeAnonymousClassDef(IDocument document, String partitioning, CALHeuristicScanner scanner, int position) {
//        int previousCommaOrParen = scanner.scanBackward(position - 1, CALHeuristicScanner.UNBOUND, new char[]{',', '('});
//        if (previousCommaOrParen == -1 || position < previousCommaOrParen + 5) // 2 for borders, 3 for "new"
//            return false;
//
//        if (isNewMatch(document, previousCommaOrParen + 1, position - previousCommaOrParen - 2, partitioning))
//            return true;
//
//        return false;
//    }
//    
//    /**
//     * Checks whether <code>position</code> resides in a default (Java) partition of <code>document</code>.
//     *
//     * @param document the document being modified
//     * @param position the position to be checked
//     * @param partitioning the document partitioning
//     * @return <code>true</code> if <code>position</code> is in the default partition of <code>document</code>, <code>false</code> otherwise
//     */
//    private static boolean isDefaultPartition(IDocument document, int position, String partitioning) {
//        assert (position >= 0);
//        assert (position <= document.getLength());
//
//        try {
//            ITypedRegion region = TextUtilities.getPartition(document, partitioning, position, false);
//            return region.getType().equals(IDocument.DEFAULT_CONTENT_TYPE);
//
//        } catch (BadLocationException e) {
//        }
//
//        return false;
//    }
//
//    private boolean isClosed(IDocument document, int offset, int length) {
//
//        CompilationUnitInfo info = getCompilationUnitForMethod(document, offset, fPartitioning);
//        if (info == null)
//            return false;
//        
//        CompilationUnit compilationUnit = null;
//        try {
//            ASTParser parser = ASTParser.newParser(AST.JLS3);
//            parser.setSource(info.buffer);
//            compilationUnit = (CompilationUnit)parser.createAST(null);
//        } catch (ArrayIndexOutOfBoundsException x) {
//            // work around for parser problem
//            return false;
//        }
//        
//        IProblem[] problems = compilationUnit.getProblems();
//        for (int i = 0; i != problems.length; ++i) {
//            if (problems[i].getID() == IProblem.UnmatchedBracket)
//                return true;
//        }
//        
//        final int relativeOffset = offset - info.delta;
//        
//        ASTNode node = NodeFinder.perform(compilationUnit, relativeOffset, length);
//        
//        if (length == 0) {
//            while (node != null && (relativeOffset == node.getStartPosition() || relativeOffset == node.getStartPosition() + node.getLength()))
//                node = node.getParent();
//        }
//        
//        if (node == null)
//            return false;
//        
//        switch (node.getNodeType()) {
//            case ASTNode.BLOCK:
//                return getBlockBalance(document, offset, fPartitioning) <= 0;
//                
//            case ASTNode.IF_STATEMENT: {
//                IfStatement ifStatement = (IfStatement)node;
//                Expression expression = ifStatement.getExpression();
//                IRegion expressionRegion = createRegion(expression, info.delta);
//                Statement thenStatement = ifStatement.getThenStatement();
//                IRegion thenRegion = createRegion(thenStatement, info.delta);
//                
//                // between expression and then statement
//                if (expressionRegion.getOffset() + expressionRegion.getLength() <= offset && offset + length <= thenRegion.getOffset())
//                    return thenStatement != null;
//                
//                Statement elseStatement = ifStatement.getElseStatement();
//                IRegion elseRegion = createRegion(elseStatement, info.delta);
//                
//                if (elseStatement != null) {
//                    int sourceOffset = thenRegion.getOffset() + thenRegion.getLength();
//                    int sourceLength = elseRegion.getOffset() - sourceOffset;
//                    IRegion elseToken = getToken(document, new Region(sourceOffset, sourceLength), ITerminalSymbols.TokenNameelse);
//                    return elseToken != null && elseToken.getOffset() + elseToken.getLength() <= offset && offset + length < elseRegion.getOffset();
//                }
//            }
//            break;
//            
//            case ASTNode.WHILE_STATEMENT:
//            case ASTNode.FOR_STATEMENT: {
//                Expression expression = node.getNodeType() == ASTNode.WHILE_STATEMENT ? ((WhileStatement)node).getExpression() : ((ForStatement)node).getExpression();
//                IRegion expressionRegion = createRegion(expression, info.delta);
//                Statement body = node.getNodeType() == ASTNode.WHILE_STATEMENT ? ((WhileStatement)node).getBody() : ((ForStatement)node).getBody();
//                IRegion bodyRegion = createRegion(body, info.delta);
//                
//                // between expression and body statement
//                if (expressionRegion.getOffset() + expressionRegion.getLength() <= offset && offset + length <= bodyRegion.getOffset())
//                    return body != null;
//            }
//            break;
//            
//            case ASTNode.DO_STATEMENT: {
//                DoStatement doStatement = (DoStatement)node;
//                IRegion doRegion = createRegion(doStatement, info.delta);
//                Statement body = doStatement.getBody();
//                IRegion bodyRegion = createRegion(body, info.delta);
//                
//                if (doRegion.getOffset() + doRegion.getLength() <= offset && offset + length <= bodyRegion.getOffset())
//                    return body != null;
//            }
//            break;
//        }
//
//        return true;
//    }

    /**
     * Installs a java partitioner with <code>document</code>.
     *
     * @param document the document
     */
    private static void installCALStuff(Document document) {
        String[] types= new String[] {
                CALPartitions.CAL_DOC,
                CALPartitions.CAL_MULTI_LINE_COMMENT,
                CALPartitions.CAL_SINGLE_LINE_COMMENT,
                CALPartitions.CAL_STRING,
                CALPartitions.CAL_CHARACTER,
                IDocument.DEFAULT_CONTENT_TYPE
        };
        FastPartitioner partitioner = new FastPartitioner(new FastCALPartitionScanner(), types);
        partitioner.connect(document);
        document.setDocumentPartitioner(CALPartitions.CAL_PARTITIONING, partitioner);
    }
    
    /**
     * Installs a cal partitioner with <code>document</code>.
     *
     * @param document the document
     */
    private static void removeCALStuff(Document document) {
        document.setDocumentPartitioner(CALPartitions.CAL_PARTITIONING, null);
    }

    private void smartPaste(IDocument document, DocumentCommand command) {
        int newOffset = command.offset;
        int newLength = command.length;
        String newText = command.text;

        try {
            CALHeuristicScanner scanner = new CALHeuristicScanner(document);
            CALIndenter indenter = new CALIndenter(document, scanner, fProject);
            int offset = newOffset;

            // reference position to get the indent from
            int refOffset = indenter.findReferencePosition(offset);
            if (refOffset == CALHeuristicScanner.NOT_FOUND) {
                return;
            }
            int peerOffset = getPeerPosition(document, command);
            peerOffset = indenter.findReferencePosition(peerOffset);
            refOffset = Math.min(refOffset, peerOffset);

            // eat any WS before the insertion to the beginning of the line
            int firstLine = 1; // don't format the first line per default, as it has other content before it
            IRegion line = document.getLineInformationOfOffset(offset);
            String notSelected = document.get(line.getOffset(), offset - line.getOffset());
            if (notSelected.trim().length() == 0) {
                newLength += notSelected.length();
                newOffset = line.getOffset();
                firstLine = 0;
            }

            // prefix: the part we need for formatting but won't paste
            IRegion refLine = document.getLineInformationOfOffset(refOffset);
            String prefix = document.get(refLine.getOffset(), newOffset - refLine.getOffset());

            // handle the indentation computation inside a temporary document
            Document temp = new Document(prefix + newText);
            DocumentRewriteSession session = temp.startRewriteSession(DocumentRewriteSessionType.STRICTLY_SEQUENTIAL);
            scanner = new CALHeuristicScanner(temp);
            indenter = new CALIndenter(temp, scanner, fProject);
            installCALStuff(temp);

            // indent the first and second line
            // compute the relative indentation difference from the second line
            // (as the first might be partially selected) and use the value to
            // indent all other lines.
            boolean isIndentDetected = false;
            StringBuilder addition = new StringBuilder();
            int insertLength = 0;
            int first = document.computeNumberOfLines(prefix) + firstLine; // don't format first line
            int lines = temp.getNumberOfLines();
            boolean changed = false;
            for (int l = first; l < lines; l++) { // we don't change the number of lines while adding indents

                IRegion r = temp.getLineInformation(l);
                int lineOffset = r.getOffset();
                int lineLength = r.getLength();

                if (lineLength == 0) {
                    continue;
                }

                if (!isIndentDetected) {

                    // indent the first pasted line
                    String current = getCurrentIndent(temp, l);
                    StringBuilder correct = indenter.computeIndentation(lineOffset);
                    if (correct == null) {
                        return; // bail out
                    }

                    insertLength = subtractIndent(correct, current, addition);
                    if (l != first && temp.get(lineOffset, lineLength).trim().length() != 0) {
                        isIndentDetected = true;
                        if (insertLength == 0) {
                            // no adjustment needed, bail out
                            if (firstLine == 0) {
                                // but we still need to adjust the first line
                                command.offset = newOffset;
                                command.length = newLength;
                                if (changed) {
                                    break; // still need to get the leading indent of the first line
                                }
                            }
                            return;
                        }
                        removeCALStuff(temp);
                    } else {
                        changed = insertLength != 0;
                    }
                }

                // relatively indent all pasted lines
                if (insertLength > 0) {
                    addIndent(temp, l, addition);
                } else if (insertLength < 0) {
                    cutIndent(temp, l, -insertLength);
                }

            }

            temp.stopRewriteSession(session);
            newText = temp.get(prefix.length(), temp.getLength() - prefix.length());

            command.offset = newOffset;
            command.length = newLength;
            command.text = newText;

        } catch (BadLocationException e) {
            CALEclipseUIPlugin.log(e);
        }

    }

    /**
     * Returns the indentation of the line <code>line</code> in <code>document</code>.
     * The returned string may contain pairs of leading slashes that are considered
     * part of the indentation. The space before the asterix in a javadoc-like
     * comment is not considered part of the indentation.
     *
     * @param document the document
     * @param line the line
     * @return the indentation of <code>line</code> in <code>document</code>
     * @throws BadLocationException if the document is changed concurrently
     */
    private static String getCurrentIndent(Document document, int line) throws BadLocationException {
        IRegion region = document.getLineInformation(line);
        int from = region.getOffset();
        int endOffset = region.getOffset() + region.getLength();

        // go behind line comments
        int to = from;
        while (to < endOffset - 2 && document.get(to, 2).equals(LINE_COMMENT)) {
            to += 2;
        }

        while (to < endOffset) {
            char ch = document.getChar(to);
            if (!Character.isWhitespace(ch)) {
                break;
            }
            to++;
        }

        // don't count the space before javadoc like, asterix-style comment lines
        if (to > from && to < endOffset - 1 && document.get(to - 1, 2).equals(" *")) { //$NON-NLS-1$
            String type = TextUtilities.getContentType(document, CALPartitions.CAL_PARTITIONING, to, true);
            if (type.equals(CALPartitions.CAL_DOC) || type.equals(CALPartitions.CAL_MULTI_LINE_COMMENT)) {
                to--;
            }
        }

        return document.get(from, to - from);
    }

    /**
     * Computes the difference of two indentations and returns the difference in
     * length of current and correct. If the return value is positive, <code>addition</code>
     * is initialized with a substring of that length of <code>correct</code>.
     *
     * @param correct the correct indentation
     * @param current the current indentation (migth contain non-whitespace)
     * @param difference a string buffer - if the return value is positive, it will be cleared and set to the substring of <code>current</code> of that length
     * @return the difference in lenght of <code>correct</code> and <code>current</code>
     */
    private int subtractIndent(CharSequence correct, CharSequence current, StringBuilder difference) {
        int c1 = computeVisualLength(correct);
        int c2 = computeVisualLength(current);
        int diff = c1 - c2;
        if (diff <= 0) {
            return diff;
        }

        difference.setLength(0);
        int len = 0, i = 0;
        while (len < diff) {
            char c = correct.charAt(i++);
            difference.append(c);
            len += computeVisualLength(c);
        }

        return diff;
    }

    /**
     * Indents line <code>line</code> in <code>document</code> with <code>indent</code>.
     * Leaves leading comment signs alone.
     *
     * @param document the document
     * @param line the line
     * @param indent the indentation to insert
     * @throws BadLocationException on concurrent document modification
     */
    private static void addIndent(Document document, int line, CharSequence indent) throws BadLocationException {
        IRegion region = document.getLineInformation(line);
        int insert = region.getOffset();
        int endOffset = region.getOffset() + region.getLength();

        // go behind line comments
        while (insert < endOffset - 2 && document.get(insert, 2).equals(LINE_COMMENT)) {
            insert += 2;
        }

        // insert indent
        document.replace(insert, 0, indent.toString());
    }

    /**
     * Cuts the visual equivalent of <code>toDelete</code> characters out of the
     * indentation of line <code>line</code> in <code>document</code>. Leaves
     * leading comment signs alone.
     *
     * @param document the document
     * @param line the line
     * @param toDelete the number of space equivalents to delete.
     * @throws BadLocationException on concurrent document modification
     */
    private void cutIndent(Document document, int line, int toDelete) throws BadLocationException {
        IRegion region = document.getLineInformation(line);
        int from = region.getOffset();
        int endOffset = region.getOffset() + region.getLength();

        // go behind line comments
        while (from < endOffset - 2 && document.get(from, 2).equals(LINE_COMMENT)) {
            from += 2;
        }

        int to = from;
        while (toDelete > 0 && to < endOffset) {
            char ch = document.getChar(to);
            if (!Character.isWhitespace(ch)) {
                break;
            }
            toDelete -= computeVisualLength(ch);
            if (toDelete >= 0) {
                to++;
            } else {
                break;
            }
        }

        document.replace(from, to - from, null);
    }

    /**
     * Returns the visual length of a given <code>CharSequence</code> taking into
     * account the visual tabulator length.
     *
     * @param seq the string to measure
     * @return the visual length of <code>seq</code>
     */
    private int computeVisualLength(CharSequence seq) {
        int size = 0;
        int tablen = getVisualTabLengthPreference();

        for (int i = 0; i < seq.length(); i++) {
            char ch = seq.charAt(i);
            if (ch == '\t') {
                if (tablen != 0) {
                    size += tablen - size % tablen;
                // else: size stays the same
                }
            } else {
                size++;
            }
        }
        return size;
    }

    /**
     * Returns the visual length of a given character taking into
     * account the visual tabulator length.
     *
     * @param ch the character to measure
     * @return the visual length of <code>ch</code>
     */
    private int computeVisualLength(char ch) {
        if (ch == '\t') {
            return getVisualTabLengthPreference();
        } else {
            return 1;
        }
    }

    /**
     * The preference setting for the visual tabulator display.
     *
     * @return the number of spaces displayed for a tabulator in the editor
     */
    private int getVisualTabLengthPreference() {
        return CodeFormatterUtil.getTabWidth(fProject);
    }

    private int getPeerPosition(IDocument document, DocumentCommand command) {
        if (document.getLength() == 0) {
            return 0;
        }
        /*
         * Search for scope closers in the pasted text and find their opening peers
         * in the document.
         */
        Document pasted = new Document(command.text);
        installCALStuff(pasted);
        int firstPeer = command.offset;

        CALHeuristicScanner pScanner = new CALHeuristicScanner(pasted);
        CALHeuristicScanner dScanner = new CALHeuristicScanner(document);

        // add scope relevant after context to peer search
        int afterToken = dScanner.nextToken(command.offset + command.length, CALHeuristicScanner.UNBOUND);
        try {
            switch (afterToken) {
                case Symbols.TokenRBRACE:
                    pasted.replace(pasted.getLength(), 0, "}"); //$NON-NLS-1$
                    break;
                case Symbols.TokenRPAREN:
                    pasted.replace(pasted.getLength(), 0, ")"); //$NON-NLS-1$
                    break;
                case Symbols.TokenRBRACKET:
                    pasted.replace(pasted.getLength(), 0, "]"); //$NON-NLS-1$
                    break;
            }
        } catch (BadLocationException e) {
            // cannot happen
            assert (false);
        }

        int pPos = 0; // paste text position (increasing from 0)
        int dPos = Math.max(0, command.offset - 1); // document position (decreasing from paste offset)
        while (true) {
            int token = pScanner.nextToken(pPos, CALHeuristicScanner.UNBOUND);
            pPos = pScanner.getPosition();
            switch (token) {
                case Symbols.TokenLBRACE:
                case Symbols.TokenLBRACKET:
                case Symbols.TokenLPAREN:
                    pPos = skipScope(pScanner, pPos, token);
                    if (pPos == CALHeuristicScanner.NOT_FOUND) {
                        return firstPeer;
                    }
                    break; // closed scope -> keep searching
                case Symbols.TokenRBRACE:
                    int peer = dScanner.findOpeningPeer(dPos, '{', '}');
                    dPos = peer - 1;
                    if (peer == CALHeuristicScanner.NOT_FOUND) {
                        return firstPeer;
                    }
                    firstPeer = peer;
                    break; // keep searching
                case Symbols.TokenRBRACKET:
                    peer = dScanner.findOpeningPeer(dPos, '[', ']');
                    dPos = peer - 1;
                    if (peer == CALHeuristicScanner.NOT_FOUND) {
                        return firstPeer;
                    }
                    firstPeer = peer;
                    break; // keep searching
                case Symbols.TokenRPAREN:
                    peer = dScanner.findOpeningPeer(dPos, '(', ')');
                    dPos = peer - 1;
                    if (peer == CALHeuristicScanner.NOT_FOUND) {
                        return firstPeer;
                    }
                    firstPeer = peer;
                    break; // keep searching
//                case Symbols.TokenCASE:
//                case Symbols.TokenDEFAULT:
//                    CALIndenter indenter = new CALIndenter(document, dScanner, fProject);
//                    peer = indenter.findReferencePosition(dPos, Symbols.TokenOTHER, false, false, false);
//                    if (peer == CALHeuristicScanner.NOT_FOUND)
//                        return firstPeer;
//                    firstPeer = peer;
//                    break; // keep searching

                case Symbols.TokenEOF:
                    return firstPeer;
                default:
            // keep searching
            }
        }
    }

    /**
     * Skips the scope opened by <code>token</code> in <code>document</code>,
     * returns either the position of the
     * @param pos
     * @param token
     * @return the position after the scope
     */
    private static int skipScope(CALHeuristicScanner scanner, int pos, int token) {
        int openToken = token;
        int closeToken;
        switch (token) {
            case Symbols.TokenLPAREN:
                closeToken = Symbols.TokenRPAREN;
                break;
            case Symbols.TokenLBRACKET:
                closeToken = Symbols.TokenRBRACKET;
                break;
            case Symbols.TokenLBRACE:
                closeToken = Symbols.TokenRBRACE;
                break;
            default:
                assert (false);
                return -1; // dummy
        }

        int depth = 1;
        int p = pos;

        while (true) {
            int tok = scanner.nextToken(p, CALHeuristicScanner.UNBOUND);
            p = scanner.getPosition();

            if (tok == openToken) {
                depth++;
            } else if (tok == closeToken) {
                depth--;
                if (depth == 0) {
                    return p + 1;
                }
            } else if (tok == Symbols.TokenEOF) {
                return CALHeuristicScanner.NOT_FOUND;
            }
        }
    }

    private boolean isLineDelimiter(IDocument document, String text) {
        String[] delimiters = document.getLegalLineDelimiters();
        if (delimiters != null) {
            return TextUtilities.equals(delimiters, text) > -1;
        }
        return false;
    }

    private void smartIndentOnKeypress(IDocument document, DocumentCommand command) {
        switch (command.text.charAt(0)) {
            case ';':
                smartIndentAfterTokenChar(document, command, ';', Symbols.TokenSEMICOLON);
                break;
            case ']':
                smartIndentAfterTokenChar(document, command, ']', Symbols.TokenRBRACKET);
                break;
            case ')':
                smartIndentAfterTokenChar(document, command, ')', Symbols.TokenRPAREN);
                break;
            case '}':
                smartIndentAfterTokenChar(document, command, '}', Symbols.TokenRBRACE);
                break;
            case 'e':
                smartIndentTokenHelper(document, command, "else", Symbols.TokenELSE);
                break;
            case 'g':
                smartIndentTokenHelper(document, command, "deriving", Symbols.TokenDERIVING);
                break;
            case 'n':
                smartIndentTokenHelper(document, command, "in", Symbols.TokenIN);
                smartIndentTokenHelper(document, command, "then", Symbols.TokenTHEN);
                break;
            case 't':
                smartIndentTokenHelper(document, command, "default", Symbols.TokenDEFAULT);
                break;
        }
    }

    private static boolean isRunOfWhitespaceOrSemicolons(String s) {
        int len = s.length();
        for (int i = 0; i < len; i++) {
            char ithChar = s.charAt(i);
            if (!(ithChar == ';' || LanguageInfo.isCALWhitespace(ithChar))) {
                return false;
            }
        }
        return true;
    }
    
    private void smartIndentAfterTokenChar(IDocument d, DocumentCommand c, char ch, int symbol) {
        if (c.offset == -1 || d.getLength() == 0) {
            return;
        }

        try {
            CALHeuristicScanner scanner = new CALHeuristicScanner(d);
            int p = c.offset;
            
            // current line
            int line = d.getLineOfOffset(p);
            int lineOffset = d.getLineOffset(line);
            
            // Look for a run of whitespace and semicolons, without leading comments, etc.
            String trimmedLine = d.get(lineOffset, p - lineOffset).trim();
            if (!isRunOfWhitespaceOrSemicolons(trimmedLine)) {
                return;
            }
            
            // line of last cal code
            int pos = scanner.findNonWhitespaceBackward(lineOffset - 1, CALHeuristicScanner.UNBOUND);
            if (pos == -1) {
                return;
            }
            int lastLine = d.getLineOfOffset(pos);
            
            // only shift if the last cal line is further up.
            if (lastLine < line) {
                
                CALIndenter indenter = new CALIndenter(d, scanner, fProject);
                StringBuilder indent = indenter.computeIndentation(p, symbol, 1);
                
                if (indent != null) {
                    c.text = indent.toString() + trimmedLine + ch;
                    c.length += c.offset - lineOffset;
                    c.offset = lineOffset;
                }
            }
            
            return;
            
        } catch (BadLocationException e) {
            CALEclipseUIPlugin.log(e);
        }
    }
    
    private void smartIndentTokenHelper(IDocument d, DocumentCommand c, String tokenContent, int tokenSymbol) {
        int tokenLen = tokenContent.length();
        
        // Check that the typed character is the last letter of the token content.
        char typedChar = c.text.charAt(0);
        if (typedChar != tokenContent.charAt(tokenContent.length() - 1)) {
            return;
        }
        
        if (c.offset < tokenLen || d.getLength() == 0) {
            return;
        }


        try {
            int p = c.offset - (tokenLen - 1);
            String content = d.get(p, tokenLen - 1);
            if (content.equals(tokenContent.substring(0, tokenLen - 1))) { 
                CALHeuristicScanner scanner = new CALHeuristicScanner(d);

                // current line
                int line = d.getLineOfOffset(p);
                int lineOffset = d.getLineOffset(line);

                // make sure we don't have any leading comments etc.
                if (d.get(lineOffset, p - lineOffset).trim().length() != 0) {
                    return;
                }

                // line of last cal code
                int pos = scanner.findNonWhitespaceBackward(p - 1, CALHeuristicScanner.UNBOUND);
                if (pos == -1) {
                    return;
                }
                int lastLine = d.getLineOfOffset(pos);

                // only shift if the last cal line is further up.
                if (lastLine < line) {

                    CALIndenter indenter = new CALIndenter(d, scanner, fProject);
                    int ref = indenter.findReferencePosition(p, tokenSymbol, false, false, false, 0);
                    if (ref == CALHeuristicScanner.NOT_FOUND) {
                        return;
                    }

                    StringBuilder indent = indenter.computeIndentation(p, tokenSymbol, 0);            // What the correct indentation should be.
                    if (indent != null) {
                        c.text = indent.toString() + tokenContent;
                        c.length += c.offset - lineOffset;
                        c.offset = lineOffset;
                    }
                }

                return;
            }

        } catch (BadLocationException e) {
            CALEclipseUIPlugin.log(e);
        }
    }

    /*
     * @see org.eclipse.jface.text.IAutoIndentStrategy#customizeDocumentCommand(org.eclipse.jface.text.IDocument, org.eclipse.jface.text.DocumentCommand)
     */
    @Override
    public void customizeDocumentCommand(IDocument d, DocumentCommand c) {

        if (c.doit == false) {
            return;
        }

        clearCachedValues();
        if (!fIsSmartMode) {
            super.customizeDocumentCommand(d, c);
            return;
        }

        if (c.length == 0 && c.text != null && isLineDelimiter(d, c.text)) {
            smartIndentAfterNewLine(d, c);
        
        } else if (c.text.length() == 1) {
            smartIndentOnKeypress(d, c);
        
        } else if (c.text.length() > 1 && getPreferenceStore().getBoolean(PreferenceConstants.EDITOR_SMART_PASTE)) {
            smartPaste(d, c); // no smart backspace for paste
        }
    }

    private static IPreferenceStore getPreferenceStore() {
        return CALEclipseUIPlugin.getDefault().getCombinedPreferenceStore();
    }

//    private boolean closeBrace() {
//        return fCloseBrace;
//    }

    private void clearCachedValues() {
//        IPreferenceStore preferenceStore = getPreferenceStore();
//        fCloseBrace = preferenceStore.getBoolean(PreferenceConstants.EDITOR_CLOSE_BRACES);
        fIsSmartMode = computeSmartMode();
    }

    private boolean computeSmartMode() {
        IWorkbenchPage page = CALEclipseUIPlugin.getActivePage();
        if (page != null) {
            IEditorPart part = page.getActiveEditor();
            if (part instanceof ITextEditorExtension3) {
                ITextEditorExtension3 extension = (ITextEditorExtension3)part;
                return extension.getInsertMode() == ITextEditorExtension3.SMART_INSERT;
            }
        }
        return false;
    }

    // ADE unused!
//    private static CompilationUnitInfo getCompilationUnitForMethod(IDocument document, int offset, String partitioning) {
//        try {
//            CALHeuristicScanner scanner = new CALHeuristicScanner(document);
//
//            IRegion sourceRange = scanner.findSurroundingBlock(offset);
//            if (sourceRange == null)
//                return null;
//            String source = document.get(sourceRange.getOffset(), sourceRange.getLength());
//
//            StringBuilder contents = new StringBuilder();
//            contents.append("class ____C{void ____m()"); //$NON-NLS-1$
//            final int methodOffset = contents.length();
//            contents.append(source);
//            contents.append('}');
//
//            char[] buffer = contents.toString().toCharArray();
//
//            return new CompilationUnitInfo(buffer, sourceRange.getOffset() - methodOffset);
//
//        } catch (BadLocationException e) {
//            CALEclipseUIPlugin.log(e);
//        }
//
//        return null;
//    }
//
//
//    /**
//     * Returns the block balance, i.e. zero if the blocks are balanced at
//     * <code>offset</code>, a negative number if there are more closing than opening
//     * braces, and a positive number if there are more opening than closing braces.
//     *
//     * @param document
//     * @param offset
//     * @param partitioning
//     * @return the block balance
//     */
//    private static int getBlockBalance(IDocument document, int offset, String partitioning) {
//        if (offset < 1)
//            return -1;
//        if (offset >= document.getLength())
//            return 1;
//
//        int begin = offset;
//        int end = offset - 1;
//
//        CALHeuristicScanner scanner = new CALHeuristicScanner(document);
//
//        while (true) {
//            begin = scanner.findOpeningPeer(begin - 1, '{', '}');
//            end = scanner.findClosingPeer(end + 1, '{', '}');
//            if (begin == -1 && end == -1)
//                return 0;
//            if (begin == -1)
//                return -1;
//            if (end == -1)
//                return 1;
//        }
//    }
//
//    private static IRegion createRegion(ASTNode node, int delta) {
//        return node == null ? null : new Region(node.getStartPosition() + delta, node.getLength());
//    }
//
//    private static IRegion getToken(IDocument document, IRegion scanRegion, int tokenId) {
//
//        try {
//
//            final String source = document.get(scanRegion.getOffset(), scanRegion.getLength());
//
//            IScanner scanner = ToolFactory.createScanner(false, false, false, false);
//            scanner.setSource(source.toCharArray());
//
//            int id = scanner.getNextToken();
//            while (id != ITerminalSymbols.TokenNameEOF && id != tokenId)
//                id = scanner.getNextToken();
//
//            if (id == ITerminalSymbols.TokenNameEOF)
//                return null;
//
//            int tokenOffset = scanner.getCurrentTokenStartPosition();
//            int tokenLength = scanner.getCurrentTokenEndPosition() + 1 - tokenOffset; // inclusive end
//            return new Region(tokenOffset + scanRegion.getOffset(), tokenLength);
//
//        } catch (InvalidInputException x) {
//            return null;
//        } catch (BadLocationException x) {
//            return null;
//        }
//    }
}
