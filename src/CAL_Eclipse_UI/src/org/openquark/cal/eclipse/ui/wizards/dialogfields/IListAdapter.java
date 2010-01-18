/*******************************************************************************
 * Copyright (c) 2006 Business Objects Software Limited and others.
 * All rights reserved. 
 * This file is made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Business Objects Software Limited - initial API and implementation based on Eclipse 3.1.2 code for
 *                             /org.eclipse.jdt.ui/ui/org/eclipse/jdt/internal/ui/wizards/dialogfields/IListAdapter.java
 *                           Eclipse source is available at: http://www.eclipse.org/downloads/
 *******************************************************************************/

/*
 * IListAdapter.java
 * Creation date: Jun 27, 2006.
 * By: Edward Lam
 */
package org.openquark.cal.eclipse.ui.wizards.dialogfields;

/**
 * Change listener used by <code>ListDialogField</code> and <code>CheckedListDialogField</code>
 * @author Edward Lam
 */
public interface IListAdapter {

    /**
     * A button from the button bar has been pressed.
     */
    void customButtonPressed(ListDialogField field, int index);

    /**
     * The selection of the list has changed.
     */
    void selectionChanged(ListDialogField field);

    /**
     * En entry in the list has been double clicked
     */
    void doubleClicked(ListDialogField field);

}
