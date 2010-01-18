/*******************************************************************************
 * Copyright (c) 2006 Business Objects Software Limited and others.
 * All rights reserved. 
 * This file is made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Business Objects Software Limited - initial API and implementation based on Eclipse 3.1.2 code for
 *                             /org.eclipse.jdt.ui/ui/org/eclipse/jdt/internal/ui/text/javadoc/JavaDocScanner.java
 *                           Eclipse source is available at: http://www.eclipse.org/downloads/
 *******************************************************************************/

/*
 * CALDocScanner.java
 * Creation date: Feb 8, 2006.
 * By: Edward Lam
 */
package org.openquark.cal.eclipse.ui.text.caldoc;


import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.runtime.Preferences;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.rules.IRule;
import org.eclipse.jface.text.rules.IWordDetector;
import org.eclipse.jface.text.rules.Token;
import org.eclipse.jface.text.rules.WhitespaceRule;
import org.openquark.cal.eclipse.ui.text.CALColorConstants;
import org.openquark.cal.eclipse.ui.text.CALCommentScanner;
import org.openquark.cal.eclipse.ui.text.CALWhitespaceDetector;
import org.openquark.cal.eclipse.ui.text.ColorManager;
import org.openquark.cal.eclipse.ui.text.CombinedWordRule;


/**
 * A rule based JavaDoc scanner.
 * @author Edward Lam
 */
public final class CALDocScanner extends CALCommentScanner {
    
    
    private static final String[] fgTokenProperties = {
        CALColorConstants.CALDOC_KEYWORD,
        CALColorConstants.CALDOC_TAG,
        CALColorConstants.CALDOC_LINK,
        CALColorConstants.CALDOC_DEFAULT,
        TASK_TAG
    };
    
    
    public CALDocScanner(ColorManager manager, IPreferenceStore store, Preferences coreStore) {
        super(manager, store, coreStore, CALColorConstants.CALDOC_DEFAULT, fgTokenProperties);
    }
    
    /**
     * Initialize with the given arguments
     * @param manager       Color manager
     * @param store Preference store
     *
     * @since 3.0
     */
    public CALDocScanner(ColorManager manager, IPreferenceStore store) {
        this(manager, store, null);
    }
    
    public IDocument getDocument() {
        return fDocument;
    }
    
    /**
     * A CAL-aware word detector.
     * @author Edward Lam
     */
    public class BlockTagWordDetector implements IWordDetector {
        /*
         * @see IWordDetector#isWordStart
         */
        public boolean isWordStart(char c) {
            return c == '@';
        }
        
        /*
         * @see IWordDetector#isWordPart
         */
        public boolean isWordPart(char c) {
            return Character.isJavaIdentifierPart(c) || c == '}';
        }
    }
    /**
     * A CAL-aware word detector.
     * @author Edward Lam
     */
    public class InlineStartTagWordDetector implements IWordDetector {
        public boolean isWordStart(char c) {
            return c == '{';
        }
        
        public boolean isWordPart(char c) {
            return c == '@' || Character.isJavaIdentifierPart(c);
        }
    }
    private static final String[] blockTags = {
        "@arg", //$NON-NLS-1$
        "@return", //$NON-NLS-1$
        "@see", //$NON-NLS-1$
        "@author", //$NON-NLS-1$
        "@version", //$NON-NLS-1$
        "@deprecated" //$NON-NLS-1$
    };
    private static final String[] inlineTags = {
        "@em", //$NON-NLS-1$
        "@strong", //$NON-NLS-1$
        "@sup", //$NON-NLS-1$
        "@sub", //$NON-NLS-1$
        "@url", //$NON-NLS-1$
        "@code", //$NON-NLS-1$
        "@orderedList", //$NON-NLS-1$
        "@unorderedList", //$NON-NLS-1$
        "@item", //$NON-NLS-1$
        "@summary", //$NON-NLS-1$
        "@link" //$NON-NLS-1$
    };
    
    /*
     * @see AbstractJavaScanner#createRules()
     */
    @Override
    protected List<IRule> createRules() {
        
        List<IRule> list = new ArrayList<IRule>();


        /*
         * Rules for block tags.
         * Also for the end inline tag.  "@}".
         */
        {
            Token token = getToken(CALColorConstants.CALDOC_DEFAULT);
            CombinedWordRule combinedWordRule = new CombinedWordRule(new BlockTagWordDetector(), token);
            CombinedWordRule.WordMatcher wordMatcher = new CombinedWordRule.WordMatcher();
            
            token = getToken(CALColorConstants.CALDOC_TAG);
            for (final String blockTag : blockTags) {
                wordMatcher.addWord(blockTag, token);
            }
            
            token = getToken(CALColorConstants.CALDOC_TAG);
            wordMatcher.addWord("@}", token);
            
            combinedWordRule.addWordMatcher(wordMatcher);
            
            list.add(combinedWordRule);
        }

        /*
         * TODOEL:
         * - Support for nested tags.
         *   @link behaves differently from the other tags (embedded text is ignored..) ??
         *   
         * - Support for multi-line rules (which should exclude the intervening *'s).
         *   Doesn't work if we just make SingleLineRule into MultiLineRule.
         *   
         * - @see keywords (typeConstructor, dataConstructor, function, ...)
         *   
         * See: AnnotationRule in JavaCodeScanner.
         */
        
        /*
         * Rules for starting inline tags.
         */
        {
            Token token = getToken(CALColorConstants.CALDOC_DEFAULT);
            CombinedWordRule combinedWordRule = new CombinedWordRule(new InlineStartTagWordDetector(), token);
            CombinedWordRule.WordMatcher wordMatcher = new CombinedWordRule.WordMatcher();
            
            token = getToken(CALColorConstants.CALDOC_TAG);
            for (final String inlineTag : inlineTags) {
                String startSequence = "{" + inlineTag;
                wordMatcher.addWord(startSequence, token);
            }
            combinedWordRule.addWordMatcher(wordMatcher);
            
            list.add(combinedWordRule);
        }

        // Add generic whitespace rule.
        list.add(new WhitespaceRule(new CALWhitespaceDetector()));

        list.addAll(super.createRules());
        return list;
    }
    
}


