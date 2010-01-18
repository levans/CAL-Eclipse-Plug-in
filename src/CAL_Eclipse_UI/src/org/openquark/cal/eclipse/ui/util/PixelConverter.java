/*******************************************************************************
 * Copyright (c) 2006 Business Objects Software Limited and others.
 * All rights reserved. 
 * This file is made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Business Objects Software Limited - initial API and implementation based on Eclipse 3.1.2 code for
 *                             /org.eclipse.jdt.ui/ui/org/eclipse/jdt/internal/ui/util/PixelConverter.java
 *                           Eclipse source is available at: http://www.eclipse.org/downloads/
 *******************************************************************************/

/*
 * PixelConverter.java
 * Creation date: Jan 20, 2006.
 * By: Edward Lam
 */
package org.openquark.cal.eclipse.ui.util;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.DialogPage;
import org.eclipse.swt.graphics.FontMetrics;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.widgets.Control;

/**
 * Helper class to convert various unit measurements to pixel counts.
 * @author Edward Lam
 */
public class PixelConverter {

    private final FontMetrics fFontMetrics;

    public PixelConverter(Control control) {
        GC gc = new GC(control);
        gc.setFont(control.getFont());
        fFontMetrics = gc.getFontMetrics();
        gc.dispose();
    }

    /**
     * @see DialogPage#convertHeightInCharsToPixels
     */
    public int convertHeightInCharsToPixels(int chars) {
        return Dialog.convertHeightInCharsToPixels(fFontMetrics, chars);
    }

    /**
     * @see DialogPage#convertHorizontalDLUsToPixels
     */
    public int convertHorizontalDLUsToPixels(int dlus) {
        return Dialog.convertHorizontalDLUsToPixels(fFontMetrics, dlus);
    }

    /**
     * @see DialogPage#convertVerticalDLUsToPixels
     */
    public int convertVerticalDLUsToPixels(int dlus) {
        return Dialog.convertVerticalDLUsToPixels(fFontMetrics, dlus);
    }

    /**
     * @see DialogPage#convertWidthInCharsToPixels
     */
    public int convertWidthInCharsToPixels(int chars) {
        return Dialog.convertWidthInCharsToPixels(fFontMetrics, chars);
    }

}
