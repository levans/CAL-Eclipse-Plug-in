/*******************************************************************************
 * Copyright (c) 2006 Business Objects Software Limited and others.
 * All rights reserved. 
 * This file is made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Business Objects Software Limited - initial API and implementation based on Eclipse 3.1.2 code for
 *                             /org.eclipse.jdt.ui/ui/org/eclipse/jdt/internal/ui/text/ITypingRunListener.java
 *                           Eclipse source is available at: http://www.eclipse.org/downloads/
 *******************************************************************************/

/*
 * ITypingRunListener.java
 * Creation date: Apr 24, 2006.
 * By: Edward Lam
 */
package org.openquark.cal.eclipse.ui.text;

import org.openquark.cal.eclipse.ui.text.TypingRun.ChangeType;

/**
 * Listener for <code>TypingRun</code> events.
 *
 * @author Edward Lam
 */
public interface ITypingRunListener {

    /**
     * Called when a new <code>TypingRun</code> is started.
     *
     * @param run the newly started run
     */
    void typingRunStarted(TypingRun run);

    /**
     * Called whenever a <code>TypingRun</code> is ended.
     *
     * @param run the ended run
     * @param reason the type of change that caused the end of the run
     */
    void typingRunEnded(TypingRun run, ChangeType reason);
}
