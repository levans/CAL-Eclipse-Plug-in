/*******************************************************************************
 * Copyright (c) 2006 Business Objects Software Limited and others.
 * All rights reserved. 
 * This file is made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Business Objects Software Limited - initial API and implementation based on Eclipse 3.1.2 code for
 *                             /org.eclipse.jdt.ui/ui/org/eclipse/jdt/internal/ui/text/javadoc/JavaDocAutoIndentStrategy.java
 *                           Eclipse source is available at: http://www.eclipse.org/downloads/
 *******************************************************************************/

/*
 * CALDocAutoIndentStrategy.java
 * Creation date: Feb 10, 2006.
 * By: Edward Lam
 */
package org.openquark.cal.eclipse.ui.text.caldoc;

import org.eclipse.core.runtime.Assert;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.DefaultIndentLineAutoEditStrategy;
import org.eclipse.jface.text.DocumentCommand;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.ITypedRegion;
import org.eclipse.jface.text.Region;
import org.eclipse.jface.text.TextUtilities;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.texteditor.ITextEditorExtension3;
import org.openquark.cal.eclipse.ui.CALEclipseUIPlugin;
import org.openquark.cal.eclipse.ui.preferences.PreferenceConstants;



/**
 * Auto indent strategy for CALDoc comments.
 * 
 * @author Edward Lam
 */
public class CALDocAutoIndentStrategy extends DefaultIndentLineAutoEditStrategy {
    
    /** The partitioning that this strategy operates on. */
    private final String fPartitioning;
    
    /**
     * Creates a new CALDoc auto indent strategy for the given document partitioning.
     *
     * @param partitioning the document partitioning
     */
    public CALDocAutoIndentStrategy(String partitioning) {
        fPartitioning = partitioning;
    }
    
    /**
     * Copies the indentation of the previous line and adds a star.
     * If the javadoc just started on this line add standard method tags
     * and close the javadoc.
     *
     * @param d the document to work on
     * @param c the command to deal with
     */
    private void indentAfterNewLine(IDocument d, DocumentCommand c) {

        int offset = c.offset;
        if (offset == -1 || d.getLength() == 0) {
            return;
        }

        try {
            int p = (offset == d.getLength() ? offset - 1 : offset);
            IRegion line = d.getLineInformationOfOffset(p);

            int lineOffset = line.getOffset();
            int firstNonWS = findEndOfWhiteSpace(d, lineOffset, offset);
            Assert.isTrue(firstNonWS >= lineOffset, "indentation must not be negative"); //$NON-NLS-1$

            StringBuilder buf = new StringBuilder(c.text);
            IRegion prefix = findPrefixRange(d, line);
            String indentation = d.get(prefix.getOffset(), prefix.getLength());
            int lengthToAdd = Math.min(offset - prefix.getOffset(), prefix.getLength());

            buf.append(indentation.substring(0, lengthToAdd));

            if (firstNonWS < offset) {
                if (d.getChar(firstNonWS) == '/') {
                    // javadoc started on this line
                    buf.append(" * "); //$NON-NLS-1$

                    if (isPreferenceTrue(PreferenceConstants.EDITOR_CLOSE_JAVADOCS) && isNewComment(d, offset)) {
                        c.shiftsCaret = false;
                        c.caretOffset = c.offset + buf.length();
                        String lineDelimiter = TextUtilities.getDefaultLineDelimiter(d);

                        String endTag = lineDelimiter + indentation + " */"; //$NON-NLS-1$

//                        if (isPreferenceTrue(PreferenceConstants.EDITOR_ADD_JAVADOC_TAGS)) {
//                            // we need to close the comment before computing
//                            // the correct tags in order to get the method
//                            d.replace(offset, 0, endTag);
//
//                            // evaluate method signature
//                            ICompilationUnit unit = getCompilationUnit();
//
//                            if (unit != null) {
//                                try {
//                                    JavaModelUtil.reconcile(unit);
//                                    String string = createJavaDocTags(d, c, indentation, lineDelimiter, unit);
//                                    if (string != null) {
//                                        buf.append(string);
//                                    }
//                                } catch (CoreException e) {
//                                    // ignore
//                                }
//                            }
//                        } else {
                            buf.append(endTag);
//                        }
                    }

                }
            }

            // move the caret behind the prefix, even if we do not have to insert it.
            if (lengthToAdd < prefix.getLength()) {
                c.caretOffset = offset + prefix.getLength() - lengthToAdd;
            }
            c.text = buf.toString();

        } catch (BadLocationException excp) {
            // stop work
        }
    }
    
    /**
     * Returns the value of the given boolean-typed preference.
     * 
     * @param preference the preference to look up
     * @return the value of the given preference in the Java plug-in's default preference store
     */
    private boolean isPreferenceTrue(String preference) {
        return CALEclipseUIPlugin.getDefault().getPreferenceStore().getBoolean(preference);
    }
    
    /**
     * Returns the range of the Javadoc prefix on the given line in
     * <code>document</code>. The prefix greedily matches the following regex
     * pattern: <code>\w*\*\w*</code>, that is, any number of whitespace
     * characters, followed by an asterix ('*'), followed by any number of
     * whitespace characters.
     *
     * @param document the document to which <code>line</code> refers
     * @param line the line from which to extract the prefix range
     * @return an <code>IRegion</code> describing the range of the prefix on
     *         the given line
     * @throws BadLocationException if accessing the document fails
     */
    private IRegion findPrefixRange(IDocument document, IRegion line) throws BadLocationException {
        int lineOffset = line.getOffset();
        int lineEnd = lineOffset + line.getLength();
        int indentEnd = findEndOfWhiteSpace(document, lineOffset, lineEnd);
        if (indentEnd < lineEnd && document.getChar(indentEnd) == '*') {
            indentEnd++;
            while (indentEnd < lineEnd && document.getChar(indentEnd) == ' ') {
                indentEnd++;
            }
        }
        return new Region(lineOffset, indentEnd - lineOffset);
    }
    
    /**
     * Unindents a typed slash ('/') if it forms the end of a comment.
     *
     * @param d the document
     * @param c the command
     */
    private void indentAfterCommentEnd(IDocument d, DocumentCommand c) {
        if (c.offset < 2 || d.getLength() == 0) {
            return;
        }
        try {
            if ("* ".equals(d.get(c.offset - 2, 2))) { //$NON-NLS-1$
                // modify document command
                c.length++;
                c.offset--;
            }
        } catch (BadLocationException excp) {
            // stop work
        }
    }
    
    /**
     * Guesses if the command operates within a newly created javadoc comment or not.
     * If in doubt, it will assume that the javadoc is new.
     *
     * @param document the document
     * @param commandOffset the command offset
     * @return <code>true</code> if the comment should be closed, <code>false</code> if not
     */
    private boolean isNewComment(IDocument document, int commandOffset) {

        try {
            int lineIndex = document.getLineOfOffset(commandOffset) + 1;
            if (lineIndex >= document.getNumberOfLines()) {
                return true;
            }

            IRegion line = document.getLineInformation(lineIndex);
            ITypedRegion partition = TextUtilities.getPartition(document, fPartitioning, commandOffset, false);
            int partitionEnd = partition.getOffset() + partition.getLength();
            if (line.getOffset() >= partitionEnd) {
                return false;
            }

            if (document.getLength() == partitionEnd) {
                return true; // partition goes to end of document - probably a new comment
            }

            String comment = document.get(partition.getOffset(), partition.getLength());
            if (comment.indexOf("/*", 2) != -1) {
                return true; // enclosed another comment -> probably a new comment
            }

            return false;

        } catch (BadLocationException e) {
            return false;
        }
    }

    private boolean isSmartMode() {

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
    
    /**
     * {@inheritDoc}
     */
    @Override
    public void customizeDocumentCommand(IDocument document, DocumentCommand command) {

        if (!isSmartMode()) {
            return;
        }

        if (command.text != null) {
            if (command.length == 0) {
                String[] lineDelimiters = document.getLegalLineDelimiters();
                int index = TextUtilities.endsWith(lineDelimiters, command.text);
                if (index > -1) {
                    // ends with line delimiter
                    if (lineDelimiters[index].equals(command.text)) {
                        // just the line delimiter
                        indentAfterNewLine(document, command);
                    }
                    return;
                }
            }

            if (command.text.equals("/")) { //$NON-NLS-1$
                indentAfterCommentEnd(document, command);
                return;
            }
        }
    }
}
