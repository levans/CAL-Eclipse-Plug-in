/*******************************************************************************
 * Copyright (c) 2006 Business Objects Software Limited and others.
 * All rights reserved. 
 * This file is made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Business Objects Software Limited - initial API and implementation based on Eclipse 3.1.2 code for
 *                             /org.eclipse.jdt.ui/ui/org/eclipse/jdt/internal/ui/text/JavaCommentScanner.java
 *                           Eclipse source is available at: http://www.eclipse.org/downloads/
 *******************************************************************************/

/*
 * CALCommentScanner.java
 * Creation date: Feb 8, 2006.
 * By: Edward Lam
 */
package org.openquark.cal.eclipse.ui.text;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;

import org.eclipse.core.runtime.Assert;
import org.eclipse.core.runtime.Preferences;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.text.rules.ICharacterScanner;
import org.eclipse.jface.text.rules.IRule;
import org.eclipse.jface.text.rules.IToken;
import org.eclipse.jface.text.rules.IWordDetector;
import org.eclipse.jface.text.rules.Token;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.openquark.cal.eclipse.core.CoreOptionIDs;
import org.openquark.cal.eclipse.ui.text.CombinedWordRule.CharacterBuffer;


/**
 * CALCommentScanner.java
 * @author Edward Lam
 */
public class CALCommentScanner extends AbstractCALScanner {
    
    private static class AtJavaIdentifierDetector implements IWordDetector {
        
        public boolean isWordStart(char c) {
            return c == '@' || Character.isJavaIdentifierStart(c);
        }
        
        public boolean isWordPart(char c) {
            return Character.isJavaIdentifierPart(c);
        }
    }
    
    private class TaskTagMatcher extends CombinedWordRule.WordMatcher {
        
        private final IToken fToken;
        /**
         * Uppercase words
         */
        private final Map<CharacterBuffer, IToken> fUppercaseWords = new HashMap<CharacterBuffer, IToken>();
        /**
         * <code>true</code> if task tag detection is case-sensitive.
         */
        private boolean fCaseSensitive = true;
        /**
         * Buffer for uppercase word
         */
        private final CombinedWordRule.CharacterBuffer fBuffer = new CombinedWordRule.CharacterBuffer(16);

        public TaskTagMatcher(IToken token) {
            fToken = token;
        }
        
        /*
         * @see org.eclipse.jdt.internal.ui.text.CombinedWordRule.WordMatcher#clearWords()
         */
        @Override
        public synchronized void clearWords() {
            super.clearWords();
            fUppercaseWords.clear();
        }
        
        public synchronized void addTaskTags(String value) {
            String[] tasks = split(value, ","); //$NON-NLS-1$
            for (final String task : tasks) {
                if (task.length() > 0) {
                    addWord(task, fToken);
                }
            }
        }
        
        private String[] split(String value, String delimiters) {
            StringTokenizer tokenizer = new StringTokenizer(value, delimiters);
            int size = tokenizer.countTokens();
            String[] tokens = new String[size];
            int i = 0;
            while (i < size) {
                tokens[i++] = tokenizer.nextToken();
            }
            return tokens;
        }
        
        /*
         * @see org.eclipse.jdt.internal.ui.text.CombinedWordRule.WordMatcher#addWord(java.lang.String, org.eclipse.jface.text.rules.IToken)
         */
        @Override
        public synchronized void addWord(String word, IToken token) {
            Assert.isNotNull(word);
            Assert.isNotNull(token);
            
            super.addWord(word, token);
            fUppercaseWords.put(new CombinedWordRule.CharacterBuffer(word.toUpperCase()), token);
        }
        
        /*
         * @see org.eclipse.jdt.internal.ui.text.CombinedWordRule.WordMatcher#evaluate(org.eclipse.jface.text.rules.ICharacterScanner, org.eclipse.jdt.internal.ui.text.CombinedWordRule.CharacterBuffer)
         */
        @Override
        public synchronized IToken evaluate(ICharacterScanner scanner, CombinedWordRule.CharacterBuffer word) {
            if (fCaseSensitive) {
                return super.evaluate(scanner, word);
            }

            fBuffer.clear();
            for (int i = 0, n = word.length(); i < n; i++) {
                fBuffer.append(Character.toUpperCase(word.charAt(i)));
            }

            IToken token = fUppercaseWords.get(fBuffer);
            if (token != null) {
                return token;
            }
            return Token.UNDEFINED;
        }
        
        /**
         * Is task tag detection case-senstive?
         *
         * @return <code>true</code> iff task tag detection is case-sensitive
         */
        public boolean isCaseSensitive() {
            return fCaseSensitive;
        }
        
        /**
         * Enables/disables the case-sensitivity of the task tag detection.
         *
         * @param caseSensitive <code>true</code> iff case-sensitivity should be enabled
         */
        public void setCaseSensitive(boolean caseSensitive) {
            fCaseSensitive = caseSensitive;
        }
    }
    
    private static final String COMPILER_TASK_TAGS = CoreOptionIDs.COMPILER_TASK_TAGS;
    protected static final String TASK_TAG = CALColorConstants.TASK_TAG;
    /**
     * Preference key of a string preference, specifying if task tag detection is case-sensitive.
     */
    private static final String COMPILER_TASK_CASE_SENSITIVE = CoreOptionIDs.COMPILER_TASK_CASE_SENSITIVE;
    /**
     * Preference value of enabled preferences.
     */
    private static final String ENABLED = CoreOptionIDs.ENABLED;
    
    private TaskTagMatcher fTaskTagMatcher;
    private final Preferences fCorePreferenceStore;
    private final String fDefaultTokenProperty;
    private final String[] fTokenProperties;
    
    public CALCommentScanner(ColorManager manager, IPreferenceStore store, Preferences coreStore, String defaultTokenProperty) {
        this(manager, store, coreStore, defaultTokenProperty, new String[] { defaultTokenProperty, TASK_TAG });
    }
    
    public CALCommentScanner(ColorManager manager, IPreferenceStore store, Preferences coreStore, String defaultTokenProperty, String[] tokenProperties) {
        super(manager, store);

        fCorePreferenceStore = coreStore;
        fDefaultTokenProperty = defaultTokenProperty;
        fTokenProperties = tokenProperties;

        initialize();
    }
    
    /**
     * Initialize with the given arguments.
     *
     * @param manager Color manager
     * @param store Preference store
     * @param defaultTokenProperty Default token property
     */
    public CALCommentScanner(ColorManager manager, IPreferenceStore store, String defaultTokenProperty) {
        this(manager, store, null, defaultTokenProperty, new String[] { defaultTokenProperty, TASK_TAG });
    }
    
    /**
     * Initialize with the given arguments.
     *
     * @param manager Color manager
     * @param store Preference store
     * @param defaultTokenProperty Default token property
     * @param tokenProperties Token properties
     */
    public CALCommentScanner(ColorManager manager, IPreferenceStore store, String defaultTokenProperty, String[] tokenProperties) {
        this(manager, store, null, defaultTokenProperty, tokenProperties);
    }
    
    /*
     * @see AbstractJavaScanner#createRules()
     */
    @Override
    protected List<IRule> createRules() {
        List<IRule> list = new ArrayList<IRule>();
        Token defaultToken = getToken(fDefaultTokenProperty);

        List<TaskTagMatcher> matchers = createMatchers();
        if (!matchers.isEmpty()) {
            CombinedWordRule combinedWordRule = new CombinedWordRule(new AtJavaIdentifierDetector(), defaultToken);
            for (int i = 0, n = matchers.size(); i < n; i++) {
                combinedWordRule.addWordMatcher(matchers.get(i));
            }
            list.add(combinedWordRule);
        }

        setDefaultReturnToken(defaultToken);

        return list;
    }
    
    /**
     * Creates a list of word matchers.
     * 
     * @return the list of word matchers
     */
    protected List<TaskTagMatcher> createMatchers() {
        List<TaskTagMatcher> list = new ArrayList<TaskTagMatcher>();

        // Add rule for Task Tags.
        boolean isCaseSensitive = true;
        String tasks = null;
        if (getPreferenceStore().contains(COMPILER_TASK_TAGS)) {
            tasks = getPreferenceStore().getString(COMPILER_TASK_TAGS);
            isCaseSensitive = ENABLED.equals(getPreferenceStore().getString(COMPILER_TASK_CASE_SENSITIVE));
        } else if (fCorePreferenceStore != null) {
            tasks = fCorePreferenceStore.getString(COMPILER_TASK_TAGS);
            isCaseSensitive = ENABLED.equals(fCorePreferenceStore.getString(COMPILER_TASK_CASE_SENSITIVE));
        }
        if (tasks != null) {
            fTaskTagMatcher = new TaskTagMatcher(getToken(TASK_TAG));
            fTaskTagMatcher.addTaskTags(tasks);
            fTaskTagMatcher.setCaseSensitive(isCaseSensitive);
            list.add(fTaskTagMatcher);
        }

        return list;
    }
    
    /*
     * @see org.eclipse.jdt.internal.ui.text.AbstractJavaScanner#affectsBehavior(org.eclipse.jface.util.PropertyChangeEvent)
     */
    @Override
    public boolean affectsBehavior(PropertyChangeEvent event) {
        return event.getProperty().equals(COMPILER_TASK_TAGS) || event.getProperty().equals(COMPILER_TASK_CASE_SENSITIVE) || super.affectsBehavior(event);
    }
    
    /*
     * @see org.eclipse.jdt.internal.ui.text.AbstractJavaScanner#adaptToPreferenceChange(org.eclipse.jface.util.PropertyChangeEvent)
     */
    @Override
    public void adaptToPreferenceChange(PropertyChangeEvent event) {
        if (fTaskTagMatcher != null && event.getProperty().equals(COMPILER_TASK_TAGS)) {
            Object value = event.getNewValue();
            if (value instanceof String) {
                synchronized (fTaskTagMatcher) {
                    fTaskTagMatcher.clearWords();
                    fTaskTagMatcher.addTaskTags((String)value);
                }
            }
        } else if (fTaskTagMatcher != null && event.getProperty().equals(COMPILER_TASK_CASE_SENSITIVE)) {
            Object value = event.getNewValue();
            if (value instanceof String) {
                fTaskTagMatcher.setCaseSensitive(ENABLED.equals(value));
            }
        } else if (super.affectsBehavior(event)) {
            super.adaptToPreferenceChange(event);
        }
    }
    
    /*
     * @see org.eclipse.jdt.internal.ui.text.AbstractJavaScanner#getTokenProperties()
     */
    @Override
    protected String[] getTokenProperties() {
        return fTokenProperties;
    }
    
}

