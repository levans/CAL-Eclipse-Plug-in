/*******************************************************************************
 * Copyright (c) 2006 Business Objects Software Limited and others.
 * All rights reserved. 
 * This file is made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Business Objects Software Limited - initial API and implementation based on Eclipse 3.1.2 code for
 *                             /org.eclipse.jdt.ui/ui/org/eclipse/jdt/internal/ui/wizards/IStatusChangeListener.java
 *                           Eclipse source is available at: http://www.eclipse.org/downloads/
 *******************************************************************************/

/*
 * IStatusChangeListener.java
 * Creation date: Feb 15, 2006.
 * By: Edward Lam
 */
package org.openquark.cal.eclipse.ui.wizards;

import org.eclipse.core.runtime.IStatus;


/**
 * A listener which listens for changes in IStatus.
 * @author Edward Lam
 */
public interface IStatusChangeListener {
    
    /**
     * Notifies this listener that the given status has changed.
     * 
     * @param       status  the new status
     */
    void statusChanged(IStatus status);
}
