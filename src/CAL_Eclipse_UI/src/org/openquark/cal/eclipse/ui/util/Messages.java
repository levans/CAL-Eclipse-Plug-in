/*******************************************************************************
 * Copyright (c) 2006 Business Objects Software Limited and others.
 * All rights reserved. 
 * This file is made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Business Objects Software Limited - initial API and implementation based on Eclipse 3.1.2 code for
 *                             /org.eclipse.jdt.ui/core extension/org/eclipse/jdt/internal/corext/util/Messages.java
 *                           Eclipse source is available at: http://www.eclipse.org/downloads/
 *******************************************************************************/

/*
 * Messages.java
 * Creation date: Feb 14, 2006.
 * By: Edward Lam
 */
package org.openquark.cal.eclipse.ui.util;

import java.text.MessageFormat;


/**
 * Helper class to format message strings.
 * @author Edward Lam
 */
public final class Messages {
    
    public static String format(String message, Object object) {
        return MessageFormat.format(message, new Object[]{object});
    }
    
    public static String format(String message, Object[] objects) {
        return MessageFormat.format(message, objects);
    }
    
    private Messages() {
        // Not for instantiation
    }
}
