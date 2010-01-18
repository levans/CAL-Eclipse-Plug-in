/*******************************************************************************
 * Copyright (c) 2006 Business Objects Software Limited and others.
 * All rights reserved. 
 * This file is made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Business Objects Software Limited - initial API and implementation based on Eclipse 3.1.2 code for
 *                             /org.eclipse.jdt.ui/ui/org/eclipse/jdt/internal/ui/text/SubstitutionTextReader.java
 *                           Eclipse source is available at: http://www.eclipse.org/downloads/
 *******************************************************************************/

/*
 * SubstitutionTextReader.java
 * Creation date: Feb 9, 2006.
 * By: Edward Lam
 */
package org.openquark.cal.eclipse.ui.text;

import java.io.IOException;
import java.io.Reader;

/**
 * Reads the text contents from a reader and computes for each character
 * a potential substitution. The substitution may eat more characters than
 * only the one passed into the computation routine.
 * @author Edward Lam
 */
public abstract class SubstitutionTextReader extends SingleCharReader {

    protected static final String LINE_DELIM = System.getProperty("line.separator", "\n"); //$NON-NLS-1$ //$NON-NLS-2$

    private final Reader fReader;
    protected boolean fWasWhiteSpace;
    private int fCharAfterWhiteSpace;

    /**
     * Tells whether white space characters are skipped.
     */
    private boolean fSkipWhiteSpace = true;

    private boolean fReadFromBuffer;
    private final StringBuilder fBuffer;
    private int fIndex;

    protected SubstitutionTextReader(Reader reader) {
        fReader = reader;
        fBuffer = new StringBuilder();
        fIndex = 0;
        fReadFromBuffer = false;
        fCharAfterWhiteSpace = -1;
        fWasWhiteSpace = true;
    }

    /**
     * Implement to compute the substitution for the given character and
     * if necessary subsequent characters. Use <code>nextChar</code>
     * to read subsequent characters.
     */
    protected abstract String computeSubstitution(int c) throws IOException;

    /**
     * Returns the internal reader.
     */
    protected Reader getReader() {
        return fReader;
    }

    /**
     * Returns the next character.
     */
    protected int nextChar() throws IOException {
        fReadFromBuffer = (fBuffer.length() > 0);
        if (fReadFromBuffer) {
            char ch = fBuffer.charAt(fIndex++);
            if (fIndex >= fBuffer.length()) {
                fBuffer.setLength(0);
                fIndex = 0;
            }
            return ch;
        } else {
            int ch = fCharAfterWhiteSpace;
            if (ch == -1) {
                ch = fReader.read();
            }
            if (fSkipWhiteSpace && Character.isWhitespace((char)ch)) {
                do {
                    ch = fReader.read();
                } while (Character.isWhitespace((char)ch));
                if (ch != -1) {
                    fCharAfterWhiteSpace = ch;
                    return ' ';
                }
            } else {
                fCharAfterWhiteSpace = -1;
            }
            return ch;
        }
    }

    /**
     * @see Reader#read()
     */
    @Override
    public int read() throws IOException {
        int c;
        do {

            c = nextChar();
            while (!fReadFromBuffer) {
                String s = computeSubstitution(c);
                if (s == null) {
                    break;
                }
                if (s.length() > 0) {
                    fBuffer.insert(0, s);
                }
                c = nextChar();
            }

        } while (fSkipWhiteSpace && fWasWhiteSpace && (c == ' '));
        fWasWhiteSpace = (c == ' ' || c == '\r' || c == '\n');
        return c;
    }

    /**
     * @see Reader#ready()
     */
    @Override
    public boolean ready() throws IOException {
        return fReader.ready();
    }

    /**
     * @see Reader#close()
     */
    @Override
    public void close() throws IOException {
        fReader.close();
    }

    /**
     * @see Reader#reset()
     */
    @Override
    public void reset() throws IOException {
        fReader.reset();
        fWasWhiteSpace = true;
        fCharAfterWhiteSpace = -1;
        fBuffer.setLength(0);
        fIndex = 0;
    }

    protected final void setSkipWhitespace(boolean state) {
        fSkipWhiteSpace = state;
    }

    protected final boolean isSkippingWhitespace() {
        return fSkipWhiteSpace;
    }
}
