/*******************************************************************************
 * Copyright (c) 2006 Business Objects Software Limited and others.
 * All rights reserved. 
 * This file is made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Business Objects Software Limited - initial API and implementation based on Eclipse 3.1.2 code for
 *                             /org.eclipse.jdt.ui/ui/org/eclipse/jdt/internal/ui/text/java/JavaCodeScanner.java
 *                           Eclipse source is available at: http://www.eclipse.org/downloads/
 *******************************************************************************/

/*
 * CALCodeScanner.java
 * Creation date: Feb 8, 2006.
 * By: Edward Lam
 */
package org.openquark.cal.eclipse.ui.text.cal;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.text.rules.ICharacterScanner;
import org.eclipse.jface.text.rules.IRule;
import org.eclipse.jface.text.rules.IToken;
import org.eclipse.jface.text.rules.IWordDetector;
import org.eclipse.jface.text.rules.SingleLineRule;
import org.eclipse.jface.text.rules.Token;
import org.eclipse.jface.text.rules.WhitespaceRule;
import org.eclipse.jface.text.rules.WordRule;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.openquark.cal.compiler.LanguageInfo;
import org.openquark.cal.eclipse.core.CoreOptionIDs;
import org.openquark.cal.eclipse.ui.text.AbstractCALScanner;
import org.openquark.cal.eclipse.ui.text.CALColorConstants;
import org.openquark.cal.eclipse.ui.text.CALWhitespaceDetector;
import org.openquark.cal.eclipse.ui.text.ColorManager;
import org.openquark.cal.eclipse.ui.text.CombinedWordRule;



/**
 * A CAL code scanner.
 * @author Edward Lam
 */
public final class CALCodeScanner extends AbstractCALScanner {
    
    
    /*
     * What Luke had in the JEdit rules:
     * 
     * keyword3:        => -> ::
     * operator:        <= >= ++ && != || ==
     * 
     * keyword1:        ;
     * keyword3:        | = \
     * operator:        < > + - * / 
     * function:        $ #
     * markup:          !
     * label:           ( ) [ ] { } :
     */
    
    /**
     * Rule to detect java operators.
     */
    protected class OperatorRule implements IRule {
        
        /** Java operators */
        private final char[] CAL_OPERATORS= { ';', '(', ')', '{', '}', '.', '=', '/', '\\', '+', '-', '*', '[', ']', '<', '>', ':', '?', '!', ',', '|', '&', '^', '%', '~'};
        /** Token to return for this rule */
        private final IToken fToken;
        
        /**
         * Creates a new operator rule.
         *
         * @param token Token to use for this rule
         */
        public OperatorRule(IToken token) {
            fToken = token;
        }
        
        /**
         * Is this character an operator character?
         *
         * @param character Character to determine whether it is an operator character
         * @return <code>true</code> iff the character is an operator, <code>false</code> otherwise.
         */
        public boolean isOperator(char character) {
            for (final char ithChar : CAL_OPERATORS) {
                if (ithChar == character) {
                    return true;
                }
            }
            return false;
        }
        
        /*
         * @see org.eclipse.jface.text.rules.IRule#evaluate(org.eclipse.jface.text.rules.ICharacterScanner)
         */
        public IToken evaluate(ICharacterScanner scanner) {
            
            int character = scanner.read();
            if (isOperator((char)character)) {
                do {
                    character = scanner.read();
                } while (isOperator((char)character));
                scanner.unread();
                return fToken;
            } else {
                scanner.unread();
                return Token.UNDEFINED;
            }
        }
    }
    
    /*
     * TODOEL: It would be better to use LanguageInfo.
     */
    private static final String[] fgKeywords = {
        "primitive", "data", "class", "where", "default", "instance", "deriving", 
        "public", "protected", "private", 
        "if", "then", "else", 
        "let", "in", 
        "case", "of", 
        "_"
    };
    
    // According to Luke these are more "importy" keywords
    // It would probably be better to merge this with fgKeywords1
    private static final String[] fgKeywords2 = {
        "module", "friend", "import", "using", "typeConstructor", "dataConstructor", "typeClass", "function", 
        "foreign", "unsafe", "jvm"
    };
    private static final String[] functionKeywords = {
        "error", "seq", "deepSeq", "strict", "deepStrict", "eager", "undefined", "unsafeCoerce", "assert", "typeOf"
    };

    private static final String SOURCE_VERSION = CoreOptionIDs.COMPILER_SOURCE;
    
    
    private static String[] fgConstants = {"_"}; //$NON-NLS-1$
    
    
    private static String[] fgTokenProperties = {
        CALColorConstants.CAL_KEYWORD,
        CALColorConstants.CAL_STRING,
        CALColorConstants.CAL_DEFAULT,
        CALColorConstants.CAL_KEYWORD_RETURN,
        CALColorConstants.CAL_OPERATOR,
        CALColorConstants.CAL_ANNOTATION,
    };
  
    // ADE unused
//    private List fVersionDependentRules = new ArrayList(3);
    
    /**
     * Creates a Java code scanner
     *
     * @param manager       the color manager
     * @param store         the preference store
     */
    public CALCodeScanner(ColorManager manager, IPreferenceStore store) {
        super(manager, store);
        initialize();
    }
    
    /*
     * @see AbstractJavaScanner#getTokenProperties()
     */
    @Override
    protected String[] getTokenProperties() {
        return fgTokenProperties;
    }
    
    /**
     * A word detector which can detect ordinal field references.
     * @author Edward Lam
     */
    public static class FieldIndexDetector implements IWordDetector {
        public boolean isWordStart(char c) {
            return c == '#';
        }
        public boolean isWordPart(char c) {
            // TODOEL: Not quite right.
            return Character.isDigit(c);
        }
    }
    /**
     * A word detector which can detect constructors.
     * @author Edward Lam
     */
    public static class ConstructorDetector implements IWordDetector {
        public boolean isWordStart(char c) {
            return LanguageInfo.isCALConsStart(c);
        }
        public boolean isWordPart(char c) {
            return LanguageInfo.isCALConsPart(c);
        }
    }
    /**
     * A word detector which can detect numeric literals.
     * @author Edward Lam
     */
    public static class NumberDetector implements IWordDetector {
        /*
         * TODOEL: Not quite right.
         * Also, allow '-' to prefix the number.
         * 
         * (Just adding it to the or statement in isWordStart() would clash with the operator rule).
         */
        public boolean isWordStart(char c) {
            return Character.isDigit(c);
        }
        public boolean isWordPart(char c) {
            return Character.isDigit(c) || c == 'e' || c == '.';
        }
    }
    
    /**
     * A word detector which detects "words" which look like cal vars.
     * @author Edward Lam
     */
    public static class CALVarLikeWordDetector implements IWordDetector {
        
        /*
         * @see IWordDetector#isWordStart
         */
        public boolean isWordStart(char c) {
            return LanguageInfo.isCALVarStart(c);
        }
        
        /*
         * @see IWordDetector#isWordPart
         */
        public boolean isWordPart(char c) {
            return LanguageInfo.isCALVarPart(c);
        }
    }


    /*
     * @see AbstractJavaScanner#createRules()
     */
    @Override
    protected List<IRule> createRules() {
        
        List<IRule> rules = new ArrayList<IRule>();

        // Add rule for character constants.
        Token token = getToken(CALColorConstants.CAL_STRING);
        rules.add(new SingleLineRule("'", "'", token, '\\')); //$NON-NLS-2$ //$NON-NLS-1$

        // Add generic whitespace rule.
        {
            rules.add(new WhitespaceRule(new CALWhitespaceDetector()));
        }

        // Add word rule for numbers
        {
            token = getToken(CALColorConstants.CAL_STRING);
            rules.add(new WordRule(new NumberDetector(), token));
        }

        // Add rule for operators and brackets
        {
            token = getToken(CALColorConstants.CAL_OPERATOR);
            rules.add(new OperatorRule(token));
        }
        
        // Add word rule for Constructors
        {
            token = getToken(CALColorConstants.CAL_ANNOTATION);
            rules.add(new WordRule(new ConstructorDetector(), token));
        }

        // Add word rules for keywords
        {
            CALVarLikeWordDetector wordDetector = new CALVarLikeWordDetector();
            token = getToken(CALColorConstants.CAL_DEFAULT);
            CombinedWordRule combinedWordRule = new CombinedWordRule(wordDetector, token);
            
            // Add word rule for keywords1.
            {
                CombinedWordRule.WordMatcher wordRule = new CombinedWordRule.WordMatcher();
                token = getToken(CALColorConstants.CAL_KEYWORD);
                for (final String keyword : fgKeywords) {
                    wordRule.addWord(keyword, token);
                }
                for (final String constant : fgConstants) {
                    wordRule.addWord(constant, token);
                }
                
                combinedWordRule.addWordMatcher(wordRule);
            }
            
            // Add word rule for keywords2.
            {
                CombinedWordRule.WordMatcher wordRule = new CombinedWordRule.WordMatcher();
                token = getToken(CALColorConstants.CAL_KEYWORD_RETURN);
                for (final String keyword : fgKeywords2) {
                    wordRule.addWord(keyword, token);
                }
                combinedWordRule.addWordMatcher(wordRule);
            }
            
            // Add word rule for functions.
            {
                CombinedWordRule.WordMatcher wordRule = new CombinedWordRule.WordMatcher();
                token = getToken(CALColorConstants.CAL_ANNOTATION);
                for (final String keyword : functionKeywords) {
                    wordRule.addWord(keyword, token);
                }
                combinedWordRule.addWordMatcher(wordRule);
                
                // Infix function application
                rules.add(new SingleLineRule("`", "`", token, '\\')); //$NON-NLS-2$ //$NON-NLS-1$

            }
            
            rules.add(combinedWordRule);
        }
        
        // Add word rule for record field indices 
        {
            token = getToken(CALColorConstants.CAL_ANNOTATION);
            rules.add(new WordRule(new FieldIndexDetector(), token));
        }
        
        setDefaultReturnToken(getToken(CALColorConstants.CAL_DEFAULT));
        return rules;
    }
    
    /*
     * @see AbstractJavaScanner#affectsBehavior(PropertyChangeEvent)
     */
    @Override
    public boolean affectsBehavior(PropertyChangeEvent event) {
        return event.getProperty().equals(SOURCE_VERSION) || super.affectsBehavior(event);
    }
    
    /*
     * @see AbstractJavaScanner#adaptToPreferenceChange(PropertyChangeEvent)
     */
    @Override
    public void adaptToPreferenceChange(PropertyChangeEvent event) {
        
        if (super.affectsBehavior(event)) {
            super.adaptToPreferenceChange(event);
        }
    }
}
