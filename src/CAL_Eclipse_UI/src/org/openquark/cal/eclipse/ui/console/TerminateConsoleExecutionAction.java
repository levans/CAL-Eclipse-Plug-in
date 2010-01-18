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
 * TerminateConsoleExecutionAction.java
 * Created: Aug 14, 2007
 * By: Edward Lam
 */

package org.openquark.cal.eclipse.ui.console;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.ui.IViewActionDelegate;
import org.eclipse.ui.IViewPart;
import org.eclipse.ui.console.IConsole;
import org.eclipse.ui.console.IConsoleView;
import org.openquark.cal.eclipse.ui.CALEclipseUIPlugin;


/**
 * The action to terminate execution in the CAL Console.
 * @author Edward Lam
 */
public class TerminateConsoleExecutionAction extends Action implements IViewActionDelegate {
    
    /** The associated console view. */
    private IConsoleView iConsoleView;

    /** The associated CAL Console.*/
    private CALConsole calConsole;

    /**
     * No-arg constructor for this class.
     */
    public TerminateConsoleExecutionAction() {
        super("Terminate execution");
        setToolTipText("Terminate execution");   // ConsoleMessages.TerminateExecution_Tooltip
        ImageDescriptor imageDescriptor = CALEclipseUIPlugin.getImageDescriptor("/icons/stop.gif");

        setImageDescriptor(imageDescriptor);
        setHoverImageDescriptor(imageDescriptor);
//        PlatformUI.getWorkbench().getHelpSystem().setHelp(this, CALDebugHelpContextIds.CONSOLE_TERMINATE_EXECUTION_ACTION);
    }
    
    /**
     * Default constructor for this class.
     * @param calConsole
     */
    public TerminateConsoleExecutionAction(CALConsole calConsole) {
        this();
        this.calConsole = calConsole;
    }
    
    public void run() {
        calConsole.terminateExecution();
    }

    /**
     * {@inheritDoc}
     */
    public void init(IViewPart view) {
        if (view instanceof IConsoleView) {
            iConsoleView = (IConsoleView)view;
        }
    }

    /**
     * {@inheritDoc}
     */
    public void run(IAction action) {
        if (calConsole == null && iConsoleView != null) {
            IConsole console = iConsoleView.getConsole();
            if (console instanceof CALConsole) {
                calConsole = (CALConsole)console;
            }
        }

        if (calConsole != null) {
            calConsole.terminateExecution();
        }
    }

    /**
     * {@inheritDoc}
     */
    public void selectionChanged(IAction action, ISelection selection) {
    }      

}
