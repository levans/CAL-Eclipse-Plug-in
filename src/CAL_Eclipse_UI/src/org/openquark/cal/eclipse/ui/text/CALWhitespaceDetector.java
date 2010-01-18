/*******************************************************************************
 * Copyright (c) 2006 Business Objects Software Limited and others.
 * All rights reserved. 
 * This file is made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Business Objects Software Limited - initial API and implementation
 *******************************************************************************/

/*
 * CALWhitespaceDetector.java
 * Creation date: Feb 8, 2006.
 * By: Edward Lam
 */
package org.openquark.cal.eclipse.ui.text;

import org.eclipse.jface.text.rules.IWhitespaceDetector;
import org.openquark.cal.compiler.LanguageInfo;


/**
 * A CAL-aware white space detector.
 * @author Edward Lam
 */
public class CALWhitespaceDetector implements IWhitespaceDetector {
    
    /**
     * {@inheritDoc}
     */
    public boolean isWhitespace(char c) {
        return LanguageInfo.isCALWhitespace(c);
    }
}
