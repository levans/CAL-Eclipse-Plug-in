/*******************************************************************************
 * Copyright (c) 2006 Business Objects Software Limited and others.
 * All rights reserved. 
 * This file is made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Business Objects Software Limited - initial API and implementation based on Eclipse 3.1.2 code for
 *                             /org.eclipse.jdt.ui/ui/org/eclipse/jdt/internal/ui/dialogs/StatusUtil.java
 *                           Eclipse source is available at: http://www.eclipse.org/downloads/
 *******************************************************************************/

/*
 * StatusUtil.java
 * Creation date: Feb 14, 2006.
 * By: Edward Lam
 */
package org.openquark.cal.eclipse.ui.dialogs;

import org.eclipse.core.runtime.IStatus;

import org.eclipse.jface.dialogs.DialogPage;
import org.eclipse.jface.dialogs.IMessageProvider;

/**
 * A utility class to work with IStatus.
 * 
 * @author Edward Lam
 */
public class StatusUtil {

    /**
     * Compares two instances of <code>IStatus</code>. The more severe is returned:
     * An error is more severe than a warning, and a warning is more severe
     * than ok. If the two stati have the same severity, the second is returned.
     */
    public static IStatus getMoreSevere(IStatus s1, IStatus s2) {
        if (s1.getSeverity() > s2.getSeverity()) {
            return s1;
        } else {
            return s2;
        }
    }

    /**
     * Finds the most severe status from a array of stati.
     * An error is more severe than a warning, and a warning is more severe
     * than ok.
     */
    public static IStatus getMostSevere(IStatus[] status) {
        IStatus max = null;
        for (final IStatus curr : status) {
            if (curr.matches(IStatus.ERROR)) {
                return curr;
            }
            if (max == null || curr.getSeverity() > max.getSeverity()) {
                max = curr;
            }
        }
        return max;
    }

    /**
     * Applies the status to the status line of a dialog page.
     */
    public static void applyToStatusLine(DialogPage page, IStatus status) {
        String message = status.getMessage();
        switch (status.getSeverity()) {
            case IStatus.OK:
                page.setMessage(message, IMessageProvider.NONE);
                page.setErrorMessage(null);
                break;
            case IStatus.WARNING:
                page.setMessage(message, IMessageProvider.WARNING);
                page.setErrorMessage(null);
                break;
            case IStatus.INFO:
                page.setMessage(message, IMessageProvider.INFORMATION);
                page.setErrorMessage(null);
                break;
            default:
                if (message.length() == 0) {
                    message = null;
                }
                page.setMessage(null);
                page.setErrorMessage(message);
                break;
        }
    }
}
