/*******************************************************************************
 * Copyright (c) 2006 Business Objects Software Limited and others.
 * All rights reserved. 
 * This file is made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Business Objects Software Limited - initial API and implementation based on Eclipse 3.1.2 code for
 *                             /org.eclipse.jdt.ui/ui/org/eclipse/jdt/internal/ui/wizards/dialogfields/IDialogFieldListener.java
 *                           Eclipse source is available at: http://www.eclipse.org/downloads/
 *******************************************************************************/

/*
 * IDialogFieldListener.java
 * Creation date: Feb 16, 2006.
 * By: Edward Lam
 */
package org.openquark.cal.eclipse.ui.wizards.dialogfields;

/**
 * Change listener used by <code>DialogField</code>
 * @author Edward Lam
 */
public interface IDialogFieldListener {

    /**
     * The dialog field has changed.
     */
    void dialogFieldChanged(DialogField field);

}
