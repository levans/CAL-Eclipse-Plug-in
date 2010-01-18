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
 * RunInConsoleAction.java
 * Created: Aug 20, 2007
 * By: Edward Lam
 */

package org.openquark.cal.eclipse.ui.console;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.IObjectActionDelegate;
import org.eclipse.ui.IViewActionDelegate;
import org.eclipse.ui.IViewPart;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.console.ConsolePlugin;
import org.eclipse.ui.console.IConsole;
import org.eclipse.ui.console.IConsoleManager;
import org.openquark.cal.compiler.FunctionalAgent;
import org.openquark.cal.compiler.QualifiedName;
import org.openquark.cal.eclipse.core.util.Util;


/**
 * The action to execute in the CAL Console a selected node in the workspace or outline tree.
 * @author Edward Lam
 */
public class RunInConsoleAction implements IViewActionDelegate, IObjectActionDelegate {

    private IWorkbenchPart activePart;

    /**
     * {@inheritDoc}
     */
    public void init(IViewPart view) {
        activePart = view;
    }

    /**
     * {@inheritDoc}
     */
    public void setActivePart(IAction action, IWorkbenchPart targetPart) {
        activePart = targetPart;
    }

    /**
     * {@inheritDoc}
     */
    public void run(IAction action) {
        try {
            ISelection selection = activePart.getSite().getSelectionProvider().getSelection();
            FunctionalAgent functionalAgent = getSelectedFunctionalAgent(selection);
            if (functionalAgent != null) {
                openConsoleAndRun(functionalAgent);
            }
        } catch (Exception e) {
            Util.log(e, "Exception running selection in console.");
        }
    }
    
    /**
     * @param selection a selection to analyze
     * @return the functional agent representing the first element in the selection, or null if none.
     */
    private FunctionalAgent getSelectedFunctionalAgent(ISelection selection) {
        if (selection instanceof IStructuredSelection) {
            IStructuredSelection structuredSelection = (IStructuredSelection)selection;

            Object selectedObject = structuredSelection.getFirstElement ();

            if (selectedObject instanceof FunctionalAgent) {
                return (FunctionalAgent)selectedObject;
            }
        }
        return null;
    }

    /**
     * Open a CAL Console and run the given entity.
     * @param functionalAgent the entity to run in the console
     */
    private void openConsoleAndRun(final FunctionalAgent functionalAgent) {
        // This isn't thread safe, but creation of new consoles will probably only happen as a 
        // result of UI actions (which don't tend to happen concurrently).
        
        IConsoleManager consoleManager = ConsolePlugin.getDefault().getConsoleManager();
        IConsole[] consoles = consoleManager.getConsoles();
        
        CALConsole existingCALConsole = null;
        for (IConsole console : consoles) {
            if (console instanceof CALConsole) {
                existingCALConsole = (CALConsole)console;
                consoleManager.showConsoleView(console);
                break;
            }
        }
        
        if (existingCALConsole == null) {
            CALConsoleFactory calConsoleFactory = new CALConsoleFactory();
            calConsoleFactory.openConsole();  // This shows the console and adds it to the console manager.
            existingCALConsole = calConsoleFactory.getConsole();
        }
        
        // existingCALConsole should be non-null.
        
        QualifiedName functionalAgentName = functionalAgent.getName();
        final String moduleName = functionalAgentName.getModuleName().toSourceText();
        
        final CALConsole calConsole = existingCALConsole;
        
        // Run in a new thread to avoid blocking UI.
        Job job = new Job("CAL Console run job") {
            /**
             * {@inheritDoc}
             */
            @Override
            protected IStatus run(IProgressMonitor monitor) {
                calConsole.handleRequest_setModuleAndRunExpression(moduleName, functionalAgent.getName().getUnqualifiedName());
                return Status.OK_STATUS;
            }
        };
        job.setSystem(true);
        job.schedule();
    }

    /**
     * {@inheritDoc}
     */
    public void selectionChanged(IAction action, ISelection selection) {
        // Only functional agents which don't take arguments are currently runnable.
        FunctionalAgent functionalAgent = getSelectedFunctionalAgent(selection);
        if (functionalAgent != null) {
            boolean isFunction = functionalAgent.getTypeExpr().getArity() != 0;
            action.setEnabled(!isFunction);
        } else {
            action.setEnabled(false);
        }
    }

}
