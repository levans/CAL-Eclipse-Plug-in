/*******************************************************************************
 * Copyright (c) 2006 Business Objects Software Limited and others.
 * All rights reserved. 
 * This file is made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Business Objects Software Limited - initial API and implementation based on Eclipse 3.1.2 code for
 *                             /org.eclipse.jdt.ui/ui/org/eclipse/jdt/internal/ui/preferences/ScrolledPageContent.java
 *                           Eclipse source is available at: http://www.eclipse.org/downloads/
 *******************************************************************************/

/*
 * ScrolledPageContent.java
 * Creation date: Feb 14, 2006.
 * By: Edward Lam
 */
package org.openquark.cal.eclipse.ui.preferences;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;

import org.eclipse.ui.forms.FormColors;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.ui.forms.widgets.SharedScrolledComposite;

/**
 * A SharedScrollComposite suited for use in a form.
 * @author Edward Lam
 */
public class ScrolledPageContent extends SharedScrolledComposite {

    private final FormToolkit fToolkit;

    public ScrolledPageContent(Composite parent) {
        this(parent, SWT.V_SCROLL | SWT.H_SCROLL);
    }

    public ScrolledPageContent(Composite parent, int style) {
        super(parent, style);

        setFont(parent.getFont());

        FormColors colors = new FormColors(parent.getDisplay());
        colors.setBackground(null);
        colors.setForeground(null);

        fToolkit = new FormToolkit(colors);

        setExpandHorizontal(true);
        setExpandVertical(true);

        Composite body = new Composite(this, SWT.NONE);
        body.setFont(parent.getFont());
        setContent(body);
    }

    /* (non-Javadoc)
     * @see org.eclipse.swt.widgets.Widget#dispose()
     */
    @Override
    public void dispose() {
        fToolkit.dispose();
        super.dispose();
    }

    public void adaptChild(Control childControl) {
        fToolkit.adapt(childControl, true, true);
    }

    public Composite getBody() {
        return (Composite)getContent();
    }

}
