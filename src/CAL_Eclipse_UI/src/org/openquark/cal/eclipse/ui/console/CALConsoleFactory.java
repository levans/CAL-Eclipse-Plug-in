/*******************************************************************************
 * Copyright (c) 2007 Business Objects Software Limited and others.
 * All rights reserved. 
 * This file is made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Business Objects Software Limited - initial API and implementation
 *******************************************************************************/

/*
 * CALConsoleFactory.java
 * Creation date: Jul 24, 2007.
 * By: Edward Lam
 */

package org.openquark.cal.eclipse.ui.console;

import java.util.Arrays;

import org.eclipse.ui.console.ConsolePlugin;
import org.eclipse.ui.console.IConsole;
import org.eclipse.ui.console.IConsoleFactory;
import org.eclipse.ui.console.IConsoleListener;
import org.eclipse.ui.console.IConsoleManager;

/**
 * Creates a CAL new console.
 * 
 * @author Edward Lam
 */
public class CALConsoleFactory implements IConsoleFactory {
    
    /** The console created by this factory if any. */
    private CALConsole openConsole = null;

    /**
     * Constructor for a CALConsoleFactory.
     * Called via extension point.
     */
    public CALConsoleFactory() {
        
        ConsolePlugin.getDefault().getConsoleManager().addConsoleListener(new IConsoleListener() {

            /**
             * {@inheritDoc}
             */
            public void consolesAdded(IConsole[] consoles) {
            }

            /**
             * {@inheritDoc}
             */
            public void consolesRemoved(IConsole[] consoles) {
                if (Arrays.asList(consoles).contains(openConsole)) {
                    openConsole = null;
                }
            }
        });
    }

    /**
     * {@inheritDoc}
     */
    public void openConsole() {

        IConsoleManager consoleManager = ConsolePlugin.getDefault().getConsoleManager();
        
        if (openConsole == null) {
            openConsole = new CALConsole();
            openConsole.initializeDocument();
            
            consoleManager.addConsoles(new IConsole[] {openConsole});
        }
        
        consoleManager.showConsoleView(openConsole);
    }
    
    /**
     * @return the CALConsole instance which has been returned by this factory if any, or null if none.
     */
    public CALConsole getConsole() {
        return openConsole;
    }
}
