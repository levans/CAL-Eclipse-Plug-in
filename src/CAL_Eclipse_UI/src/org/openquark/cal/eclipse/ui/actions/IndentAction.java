/*******************************************************************************
 * Copyright (c) 2006 Business Objects Software Limited and others.
 * All rights reserved. 
 * This file is made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Business Objects Software Limited - initial API and implementation based on Eclipse 3.1.2 code for
 *                             /org.eclipse.jdt.ui/ui/org/eclipse/jdt/internal/ui/actions/IndentAction.java
 *                           Eclipse source is available at: http://www.eclipse.org/downloads/
 *******************************************************************************/

/*
 * IndentAction.java
 * Creation date: Feb 15, 2006.
 * By: Edward Lam
 */
package org.openquark.cal.eclipse.ui.actions;

import java.util.ResourceBundle;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.Assert;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.IRewriteTarget;
import org.eclipse.jface.text.ITextSelection;
import org.eclipse.jface.text.ITypedRegion;
import org.eclipse.jface.text.Position;
import org.eclipse.jface.text.Region;
import org.eclipse.jface.text.TextUtilities;
import org.eclipse.jface.text.source.ISourceViewer;
import org.eclipse.swt.custom.BusyIndicator;
import org.eclipse.swt.widgets.Display;
import org.eclipse.text.edits.MalformedTreeException;
import org.eclipse.text.edits.ReplaceEdit;
import org.eclipse.text.edits.TextEdit;
import org.eclipse.ui.texteditor.ITextEditor;
import org.eclipse.ui.texteditor.ITextEditorExtension3;
import org.eclipse.ui.texteditor.TextEditorAction;
import org.openquark.cal.eclipse.core.CALEclipseCorePlugin;
import org.openquark.cal.eclipse.core.CoreOptionIDs;
import org.openquark.cal.eclipse.core.formatter.DefaultCodeFormatterConstants;
import org.openquark.cal.eclipse.ui.CALEclipseUIPlugin;
import org.openquark.cal.eclipse.ui.caleditor.CALEditor;
import org.openquark.cal.eclipse.ui.preferences.PreferenceConstants;
import org.openquark.cal.eclipse.ui.text.CALHeuristicScanner;
import org.openquark.cal.eclipse.ui.text.CALIndenter;
import org.openquark.cal.eclipse.ui.text.CALPartitions;
import org.openquark.cal.eclipse.ui.text.SmartBackspaceManager;
import org.openquark.cal.eclipse.ui.text.SmartBackspaceManager.UndoSpec;
import org.openquark.util.Pair;



/**
 * Indents a line or range of lines in a Java document to its correct position. No complete
 * AST must be present, the indentation is computed using heuristics. The algorithm used is fast for
 * single lines, but does not store any information and therefore not so efficient for large line
 * ranges.
 * 
 * @see org.openquark.cal.eclipse.ui.text.CALHeuristicScanner
 * @see org.openquark.cal.eclipse.ui.text.CALIndenter
 * @author Edward Lam
 */
public class IndentAction extends TextEditorAction {
    
    /** 
     * Whether this is the action invoked by TAB. When <code>true</code>, indentation behaves 
     * differently to accomodate normal TAB operation.
     */
    private final boolean fIsTabAction;
    
    /**
     * Creates a new instance.
     * 
     * @param bundle the resource bundle
     * @param prefix the prefix to use for keys in <code>bundle</code>
     * @param editor the text editor
     * @param isTabAction whether the action should insert tabs if over the indentation
     */
    public IndentAction(ResourceBundle bundle, String prefix, ITextEditor editor, boolean isTabAction) {
        super(bundle, prefix, editor);
        fIsTabAction = isTabAction;
    }
    
    /*
     * @see org.eclipse.jface.action.Action#run()
     */
    @Override
    public void run() {
        // update has been called by the framework
        if (!isEnabled() || !validateEditorInputState()) {
            return;
        }

        ITextSelection selection = ActionUtilities.getSelection(getTextEditor());
        final IDocument document = ActionUtilities.getDocument(getTextEditor());

        if (document != null) {

            final int offset = selection.getOffset();
            final int length = selection.getLength();
            final Position end = new Position(offset + length);
            final int firstLine, nLines;
            try {
                document.addPosition(end);
                firstLine = document.getLineOfOffset(offset);
                // check for marginal (zero-length) lines
                int minusOne = length == 0 ? 0 : 1;
                nLines = document.getLineOfOffset(offset + length - minusOne) - firstLine + 1;
            } catch (BadLocationException e) {
                // will only happen on concurrent modification
                CALEclipseUIPlugin.log(new Status(IStatus.ERROR, CALEclipseUIPlugin.PLUGIN_ID, IStatus.OK, "", e)); //$NON-NLS-1$
                return;
            }

            Runnable runnable = new Runnable() {
                public void run() {
                    IRewriteTarget target = (IRewriteTarget)getTextEditor().getAdapter(IRewriteTarget.class);
                    if (target != null) {
                        target.beginCompoundChange();
                        target.setRedraw(false);
                    }

                    try {
                        CALHeuristicScanner scanner = new CALHeuristicScanner(document);
                        CALIndenter indenter = new CALIndenter(document, scanner, getJavaProject());
                        boolean hasChanged = false;
                        int fCaretOffset = -1;
                        for (int i = 0; i < nLines; i++) {
                            Pair<Boolean, Integer> result = indentLine(document, firstLine + i, offset, indenter, scanner, fIsTabAction, fCaretOffset, getTextEditor());
                            hasChanged |= result.fst();
                            fCaretOffset = result.snd();
                        }

                        // update caret position: move to new position when indenting just one line
                        // keep selection when indenting multiple
                        int newOffset, newLength;
                        if (fIsTabAction) {
                            newOffset = fCaretOffset;
                            newLength = 0;
                        } else if (nLines > 1) {
                            newOffset = offset;
                            newLength = end.getOffset() - offset;
                        } else {
                            newOffset = fCaretOffset;
                            newLength = 0;
                        }

                        // always reset the selection if anything was replaced
                        // but not when we had a singleline nontab invocation
                        if (newOffset != -1 && (hasChanged || newOffset != offset || newLength != length)) {
                            selectAndReveal(newOffset, newLength);
                        }

                        document.removePosition(end);
                    } catch (BadLocationException e) {
                        // will only happen on concurrent modification
                        CALEclipseUIPlugin.log(new Status(IStatus.ERROR, CALEclipseUIPlugin.PLUGIN_ID, IStatus.OK, "ConcurrentModification in IndentAction", e)); //$NON-NLS-1$

                    } finally {

                        if (target != null) {
                            target.endCompoundChange();
                            target.setRedraw(true);
                        }
                    }
                }
            };

            if (nLines > 50) {
                Display display = getTextEditor().getEditorSite().getWorkbenchWindow().getShell().getDisplay();
                BusyIndicator.showWhile(display, runnable);
            } else {
                runnable.run();
            }

        }
    }
    
    /**
     * Selects the given range on the editor.
     * 
     * @param newOffset the selection offset
     * @param newLength the selection range
     */
    private void selectAndReveal(int newOffset, int newLength) {
        Assert.isTrue(newOffset >= 0);
        Assert.isTrue(newLength >= 0);
        ITextEditor editor = getTextEditor();
        if (editor instanceof CALEditor) {
            ISourceViewer viewer = ((CALEditor)editor).getViewer();
            if (viewer != null) {
                viewer.setSelectedRange(newOffset, newLength);
            }
        } else {
            // this is too intrusive, but will never get called anyway
            getTextEditor().selectAndReveal(newOffset, newLength);
        }

    }
    
    public static boolean indentLine(IDocument document, int firstLine, int nLines, int offset) throws BadLocationException {
        CALHeuristicScanner scanner = new CALHeuristicScanner(document);
        CALIndenter indenter = new CALIndenter(document, scanner, null);
        boolean hasChanged = false;
        for (int i = 0; i < nLines; i++) {
            hasChanged |= indentLine(document, firstLine + i, offset, indenter, scanner, false, -1, null).fst();
        }
        return hasChanged;
    }
    
    /**
     * Indents a single line using the cal heuristic scanner. CALDoc and multiline comments are 
     * indented as specified by the <code>CALDocAutoIndentStrategy</code>.
     * 
     * @param document the document
     * @param line the line to be indented
     * @param caret the caret position
     * @param indenter the java indenter
     * @param scanner the heuristic scanner
     * @return a pair consisting of 
     * <code>true</code> if <code>document</code> was modified, <code>false</code> otherwise and
     * the new caret offset
     * @throws BadLocationException if the document got changed concurrently 
     */
    private static Pair<Boolean, Integer> indentLine(IDocument document, int line, int caret, CALIndenter indenter, CALHeuristicScanner scanner, final boolean fIsTabAction, int fCaretOffset, ITextEditor editor) throws BadLocationException {
        IRegion currentLine = document.getLineInformation(line);
        int offset = currentLine.getOffset();
        int wsStart = offset; // where we start searching for non-WS; after the "//" in single line comments

        String indent = null;
        if (offset < document.getLength()) {
            ITypedRegion partition = TextUtilities.getPartition(document, CALPartitions.CAL_PARTITIONING, offset, true);
            ITypedRegion startingPartition = TextUtilities.getPartition(document, CALPartitions.CAL_PARTITIONING, offset, false);
            String type = partition.getType();
            if (type.equals(CALPartitions.CAL_DOC) || type.equals(CALPartitions.CAL_MULTI_LINE_COMMENT)) {
                indent = computeJavadocIndent(document, line, scanner);
            } else if (!fIsTabAction && startingPartition.getOffset() == offset && startingPartition.getType().equals(CALPartitions.CAL_SINGLE_LINE_COMMENT)) {

                // line comment starting at position 0 -> indent inside
                int max = document.getLength() - offset;
                int slashes = 2;
                while (slashes < max - 1 && document.get(offset + slashes, 2).equals("//")) {
                    slashes += 2;
                }

                wsStart = offset + slashes;

                StringBuilder computed = indenter.computeIndentation(offset);
                if (computed == null) {
                    computed = new StringBuilder(0);
                }
                int tabSize = getTabSize();
                while (slashes > 0 && computed.length() > 0) {
                    char c = computed.charAt(0);
                    if (c == '\t') {
                        if (slashes > tabSize) {
                            slashes -= tabSize;
                        } else {
                            break;
                        }
                    } else if (c == ' ') {
                        slashes--;
                    } else {
                        break;
                    }

                    computed.deleteCharAt(0);
                }

                indent = document.get(offset, wsStart - offset) + computed;

            }
        }

        // standard java indentation
        if (indent == null) {
            StringBuilder computed = indenter.computeIndentation(offset);
            if (computed != null) {
                indent = computed.toString();
            } else {
                indent = "";
            }
        }

        // change document:
        // get current white space
        int lineLength = currentLine.getLength();
        int end = scanner.findNonWhitespaceForwardInAnyPartition(wsStart, offset + lineLength);
        if (end == CALHeuristicScanner.NOT_FOUND) {
            end = offset + lineLength;
        }
        int length = end - offset;
        String currentIndent = document.get(offset, length);

        // if we are right before the text start / line end, and already after the insertion point
        // then just insert a tab.
        if (fIsTabAction && caret == end && whiteSpaceLength(currentIndent) >= whiteSpaceLength(indent)) {
            String tab = getTabEquivalent();
            document.replace(caret, 0, tab);
            fCaretOffset = caret + tab.length();
            return new Pair<Boolean, Integer>(true, fCaretOffset);
        }

        // set the caret offset so it can be used when setting the selection
        if (caret >= offset && caret <= end) {
            fCaretOffset = offset + indent.length();
        } else {
            fCaretOffset = -1;
        }

        // only change the document if it is a real change
        if (!indent.equals(currentIndent)) {
            String deletedText = document.get(offset, length);
            document.replace(offset, length, indent);

            if (fIsTabAction && indent.length() > currentIndent.length()
                    && CALEclipseUIPlugin.getDefault().getPreferenceStore().getBoolean(PreferenceConstants.EDITOR_SMART_BACKSPACE)) {
                if (editor != null) {
                    final SmartBackspaceManager manager = (SmartBackspaceManager)editor.getAdapter(SmartBackspaceManager.class);
                    if (manager != null) {
                        try {
                            // restore smart portion
                            ReplaceEdit smart = new ReplaceEdit(offset, indent.length(), deletedText);

                            final UndoSpec spec = new UndoSpec(offset + indent.length(), new Region(caret, 0), new TextEdit[]{smart}, 2, null);
                            manager.register(spec);
                        
                        } catch (MalformedTreeException e) {
                            // log & ignore
                            CALEclipseUIPlugin.log(new Status(IStatus.ERROR, CALEclipseUIPlugin.PLUGIN_ID, IStatus.OK, "Illegal smart backspace action", e)); //$NON-NLS-1$
                        }
                    }
                }
            }

            return new Pair<Boolean, Integer>(true, fCaretOffset);
        } else {
            return new Pair<Boolean, Integer>(false, fCaretOffset);
        }
    }
    
    /**
     * Computes and returns the indentation for a javadoc line. The line
     * must be inside a javadoc comment.
     * 
     * @param document the document
     * @param line the line in document
     * @param scanner the scanner
     * @return the indent, or <code>null</code> if not computable
     * @throws BadLocationException
     * @since 3.1
     */
    private static String computeJavadocIndent(IDocument document, int line, CALHeuristicScanner scanner) throws BadLocationException {
        if (line == 0) {
            return null;
        }

        IRegion previousLine = document.getLineInformation(line - 1);
        int start = previousLine.getOffset();
        int end = start + previousLine.getLength();

        int firstNonWS = scanner.findNonWhitespaceForwardInAnyPartition(start, end);
        if (firstNonWS == CALHeuristicScanner.NOT_FOUND) {
            return document.get(start, end - start);
        }
        StringBuilder buf = new StringBuilder();
        String indentation = document.get(start, firstNonWS - start);
        buf.append(indentation);
        if (document.getChar(firstNonWS) == '/') {
            // javadoc started on the previous line
            buf.append(' ');
        }
        return buf.toString();
    }
    
    /**
     * Returns the size in characters of a string. All characters count one, tabs count the editor's
     * preference for the tab display 
     * 
     * @param indent the string to be measured.
     * @return the size in characters of a string
     */
    private static int whiteSpaceLength(String indent) {
        if (indent == null) {
            return 0;
        } else {
            int size = 0;
            int l = indent.length();
            int tabSize = getTabSize();

            for (int i = 0; i < l; i++) {
                size += indent.charAt(i) == '\t' ? tabSize : 1;
            }
            return size;
        }
    }
    
    /**
     * Returns a tab equivalent, either as a tab character or as spaces, depending on the editor and
     * formatter preferences.
     * 
     * @return a string representing one tab in the editor, never <code>null</code>
     */
    private static String getTabEquivalent() {
        String tab;
        if (CoreOptionIDs.SPACE.equals(getCoreFormatterOption(DefaultCodeFormatterConstants.FORMATTER_TAB_CHAR))) {
            int size = getTabSize();
            StringBuilder buf = new StringBuilder();
            for (int i = 0; i < size; i++) {
                buf.append(' ');
            }
            tab = buf.toString();
        } else {
            tab= "\t"; //$NON-NLS-1$
        }
        
        return tab;
    }
    
    /**
     * Returns the tab size used by the java editor, which is deduced from the
     * formatter preferences.
     * 
     * @return the tab size as defined in the current formatter preferences
     */
    private static int getTabSize() {
        return getCoreFormatterOption(DefaultCodeFormatterConstants.FORMATTER_TAB_SIZE, 4);
    }
    
    /**
     * Returns the possibly project-specific core preference defined under <code>key</code>.
     * 
     * @param key the key of the preference
     * @return the value of the preference
     * @since 3.1
     */
    private static String getCoreFormatterOption(String key) {
//        IJavaProject project= getJavaProject();
//        if (project == null)
            return CALEclipseCorePlugin.getOption(key);
//        return project.getOption(key, true);
    }
    
    /**
     * Returns the possibly project-specific core preference defined under <code>key</code>, or
     * def if the value is not a integer.
     * 
     * @param key the key of the preference
     * @param def the default value
     * @return the value of the preference
     * @since 3.1
     */
    private static int getCoreFormatterOption(String key, int def) {
        try {
            return Integer.parseInt(getCoreFormatterOption(key));
        } catch (NumberFormatException e) {
            return def;
        }
    }
    
    /**
     * Returns the <code>IJavaProject</code> of the current editor input, or
     * <code>null</code> if it cannot be found.
     * 
     * @return the <code>IJavaProject</code> of the current editor input, or
     *         <code>null</code> if it cannot be found
     * @since 3.1
     */
    private IProject getJavaProject() {
//        ITextEditor editor= getTextEditor();
//        if (editor == null)
//            return null;
//        
//        ICompilationUnit cu= CALEclipseUIPlugin.getDefault().getWorkingCopyManager().getWorkingCopy(editor.getEditorInput());
//        if (cu == null)
//            return null;
//        return cu.getJavaProject();
        return null;
    }
    
    /*
     * @see org.eclipse.ui.texteditor.IUpdate#update()
     */
    @Override
    public void update() {
        super.update();

        if (isEnabled()) {
            if (fIsTabAction) {
                setEnabled(canModifyEditor() && isSmartMode() && isValidSelection());
            } else {
                setEnabled(canModifyEditor() && !ActionUtilities.getSelection(getTextEditor()).isEmpty());
            }
        }
    }
    
    /**
     * Returns if the current selection is valid, i.e. whether it is empty and the caret in the 
     * whitespace at the start of a line, or covers multiple lines.
     * 
     * @return <code>true</code> if the selection is valid for an indent operation
     */
    private boolean isValidSelection() {
        ITextSelection selection = ActionUtilities.getSelection(getTextEditor());
        if (selection.isEmpty()) {
            return false;
        }

        int offset = selection.getOffset();
        int length = selection.getLength();

        IDocument document = ActionUtilities.getDocument(getTextEditor());
        if (document == null) {
            return false;
        }

        try {
            IRegion firstLine = document.getLineInformationOfOffset(offset);
            int lineOffset = firstLine.getOffset();

            // either the selection has to be empty and the caret in the WS at the line start
            // or the selection has to extend over multiple lines
            if (length == 0) {
                return document.get(lineOffset, offset - lineOffset).trim().length() == 0;
            } else {
                //              return lineOffset + firstLine.getLength() < offset + length;
                return false; // only enable for empty selections for now
            }
            
        } catch (BadLocationException e) {
        }
        
        return false;
    }
    
    /**
     * Returns the smart preference state.
     * 
     * @return <code>true</code> if smart mode is on, <code>false</code> otherwise
     */
    private boolean isSmartMode() {
        ITextEditor editor = getTextEditor();

        if (editor instanceof ITextEditorExtension3) {
            return ((ITextEditorExtension3)editor).getInsertMode() == ITextEditorExtension3.SMART_INSERT;
        }

        return false;
    }
    
}
