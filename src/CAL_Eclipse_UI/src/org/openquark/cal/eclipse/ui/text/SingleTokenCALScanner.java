/*******************************************************************************
 * Copyright (c) 2006 Business Objects Software Limited and others.
 * All rights reserved. 
 * This file is made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Business Objects Software Limited - initial API and implementation based on Eclipse 3.1.2 code for
 *                             /org.eclipse.jdt.ui/ui/org/eclipse/jdt/internal/ui/text/SingleTokenJavaScanner.java
 *                           Eclipse source is available at: http://www.eclipse.org/downloads/
 *******************************************************************************/

/*
 * SingleTokenCALScanner.java
 * Creation date: Feb 8, 2006.
 * By: Edward Lam
 */
package org.openquark.cal.eclipse.ui.text;

import java.util.List;

import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.text.rules.IRule;


/**
 *
 * @author Edward Lam
 */
public final class SingleTokenCALScanner extends AbstractCALScanner {

    private final String[] fProperty;

    public SingleTokenCALScanner(ColorManager manager, IPreferenceStore store, String property) {
        super(manager, store);
        fProperty = new String[] {property};
        initialize();
    }

    /*
     * @see AbstractJavaScanner#getTokenProperties()
     */
    @Override
    protected String[] getTokenProperties() {
        return fProperty;
    }

    /*
     * @see AbstractJavaScanner#createRules()
     */
    @Override
    protected List<IRule> createRules() {
        setDefaultReturnToken(getToken(fProperty[0]));
        return null;
    }
}
