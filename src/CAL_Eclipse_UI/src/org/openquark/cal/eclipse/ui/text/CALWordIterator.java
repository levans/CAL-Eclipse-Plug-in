/*******************************************************************************
 * Copyright (c) 2006 Business Objects Software Limited and others.
 * All rights reserved. 
 * This file is made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Business Objects Software Limited - initial API and implementation based on Eclipse 3.1.2 code for
 *                             /org.eclipse.jdt.ui/ui/org/eclipse/jdt/internal/ui/text/JavaWordIterator.java
 *                           Eclipse source is available at: http://www.eclipse.org/downloads/
 *******************************************************************************/

/*
 * CALWordIterator.java
 * Creation date: Feb 24, 2006.
 * By: Edward Lam
 */
package org.openquark.cal.eclipse.ui.text;

import java.text.BreakIterator;
import java.text.CharacterIterator;

import org.eclipse.core.runtime.Assert;
import org.openquark.cal.compiler.LanguageInfo;


/**
 * Breaks cal text into word starts, also stops at line start and end. No direction dependency.
 */
public class CALWordIterator extends BreakIterator {

    /**
     * The underlying cal break iterator. It returns all breaks, including before and after every whitespace.
     */
    private final CALBreakIterator fIterator;
    /** The current index for the stateful operations. */
    private int fIndex;

    /**
     * Creates a new word iterator.
     */
    public CALWordIterator() {
        fIterator = new CALBreakIterator();
        first();
    }

    /*
     * @see java.text.BreakIterator#first()
     */
    @Override
    public int first() {
        fIndex = fIterator.first();
        return fIndex;
    }

    /*
     * @see java.text.BreakIterator#last()
     */
    @Override
    public int last() {
        fIndex = fIterator.last();
        return fIndex;
    }

    /*
     * @see java.text.BreakIterator#next(int)
     */
    @Override
    public int next(int n) {
        int next = 0;
        while (--n > 0 && next != DONE) {
            next = next();
        }
        return next;
    }

    /*
     * @see java.text.BreakIterator#next()
     */
    @Override
    public int next() {
        fIndex = following(fIndex);
        return fIndex;
    }

    /*
     * @see java.text.BreakIterator#previous()
     */
    @Override
    public int previous() {
        fIndex = preceding(fIndex);
        return fIndex;
    }

    /*
     * @see java.text.BreakIterator#preceding(int)
     */
    @Override
    public int preceding(int offset) {
        int first = fIterator.preceding(offset);
        if (isWhitespace(first, offset)) {
            int second = fIterator.preceding(first);
            if (second != DONE && !isDelimiter(second, first)) {
                return second;
            }
        }
        return first;
    }

    /*
     * @see java.text.BreakIterator#following(int)
     */
    @Override
    public int following(int offset) {
        int first = fIterator.following(offset);
        if (eatFollowingWhitespace(offset, first)) {
            int second = fIterator.following(first);
            if (isWhitespace(first, second)) {
                return second;
            }
        }
        return first;
    }

    private boolean eatFollowingWhitespace(int offset, int exclusiveEnd) {
        if (exclusiveEnd == DONE || offset == DONE) {
            return false;
        }

        if (isWhitespace(offset, exclusiveEnd)) {
            return false;
        }
        if (isDelimiter(offset, exclusiveEnd)) {
            return false;
        }

        return true;
    }

    /**
     * Returns <code>true</code> if the given sequence into the underlying text represents a delimiter,
     * <code>false</code> otherwise.
     * 
     * @param offset the offset
     * @param exclusiveEnd the end offset
     * @return <code>true</code> if the given range is a delimiter
     */
    private boolean isDelimiter(int offset, int exclusiveEnd) {
        if (exclusiveEnd == DONE || offset == DONE) {
            return false;
        }

        Assert.isTrue(offset >= 0);
        Assert.isTrue(exclusiveEnd <= getText().getEndIndex());
        Assert.isTrue(exclusiveEnd > offset);

        CharSequence seq = fIterator.fText;

        while (offset < exclusiveEnd) {
            char ch = seq.charAt(offset);
            if (ch != '\n' && ch != '\r') {
                return false;
            }
            offset++;
        }

        return true;
    }

    /**
     * Returns <code>true</code> if the given sequence into the underlying text represents whitespace, but not a
     * delimiter, <code>false</code> otherwise.
     * 
     * @param offset the offset
     * @param exclusiveEnd the end offset
     * @return <code>true</code> if the given range is whitespace
     */
    private boolean isWhitespace(int offset, int exclusiveEnd) {
        if (exclusiveEnd == DONE || offset == DONE) {
            return false;
        }

        Assert.isTrue(offset >= 0);
        Assert.isTrue(exclusiveEnd <= getText().getEndIndex());
        Assert.isTrue(exclusiveEnd > offset);

        CharSequence seq = fIterator.fText;

        while (offset < exclusiveEnd) {
            char ch = seq.charAt(offset);
            if (!LanguageInfo.isCALWhitespace(ch)) {
                return false;
            }
            if (ch == '\n' || ch == '\r') {
                return false;
            }
            offset++;
        }

        return true;
    }

    /*
     * @see java.text.BreakIterator#current()
     */
    @Override
    public int current() {
        return fIndex;
    }

    /*
     * @see java.text.BreakIterator#getText()
     */
    @Override
    public CharacterIterator getText() {
        return fIterator.getText();
    }

    /**
     * Sets the text as <code>CharSequence</code>.
     * @param newText the new text
     */
    public void setText(CharSequence newText) {
        fIterator.setText(newText);
        first();
    }

    /*
     * @see java.text.BreakIterator#setText(java.text.CharacterIterator)
     */
    @Override
    public void setText(CharacterIterator newText) {
        fIterator.setText(newText);
        first();
    }

    /*
     * @see java.text.BreakIterator#setText(java.lang.String)
     */
    @Override
    public void setText(String newText) {
        setText((CharSequence)newText);
    }

}
