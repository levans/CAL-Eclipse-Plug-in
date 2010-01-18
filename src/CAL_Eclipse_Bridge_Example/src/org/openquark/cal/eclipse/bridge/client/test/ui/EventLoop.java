/*
 * Copyright (c) 2006 BUSINESS OBJECTS SOFTWARE LIMITED
 * All rights reserved.
 * 
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 * 
 *     * Redistributions of source code must retain the above copyright notice,
 *       this list of conditions and the following disclaimer.
 *  
 *     * Redistributions in binary form must reproduce the above copyright
 *       notice, this list of conditions and the following disclaimer in the
 *       documentation and/or other materials provided with the distribution.
 *  
 *     * Neither the name of Business Objects nor the names of its contributors
 *       may be used to endorse or promote products derived from this software
 *       without specific prior written permission.
 *  
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 */


/*
 * EventLoop.java
 * Creation date: Jan 9, 2006.
 * By: Edward Lam
 */
package org.openquark.cal.eclipse.bridge.client.test.ui;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.console.ConsolePlugin;
import org.eclipse.ui.console.IConsole;
import org.eclipse.ui.console.IConsoleConstants;
import org.eclipse.ui.console.IConsoleManager;
import org.eclipse.ui.console.IConsoleView;
import org.eclipse.ui.console.MessageConsole;
import org.eclipse.ui.console.MessageConsoleStream;
import org.openquark.cal.CALPlatformTestModuleNames;
import org.openquark.cal.compiler.CompilerMessageLogger;
import org.openquark.cal.compiler.MessageLogger;
import org.openquark.cal.compiler.QualifiedName;
import org.openquark.cal.compiler.io.EntryPoint;
import org.openquark.cal.compiler.io.EntryPointSpec;
import org.openquark.cal.compiler.io.OutputPolicy;
import org.openquark.cal.eclipse.bridge.client.test.ExamplePlugin;
import org.openquark.cal.machine.CALExecutor;
import org.openquark.cal.runtime.CALExecutorException;
import org.openquark.cal.runtime.ExecutionContext;
import org.openquark.cal.services.BasicCALServices;
import org.openquark.cal.services.WorkspaceManager;


/**
 * Some CAL code being called from Java and interacting with the Eclipse environment.
 * When instantiated, an entry point is created for a CAL value
 * representing the infinite list of prime numbers.
 * 
 * The caller can call a method to evaluate the next few of these, and the result
 * will be dumped to the Eclipse console.
 * 
 * Simplified from the primesIterator example in the EventLoop.java in CAL_Samples 
 * (which contains many excellent examples of how to use CAL from Java).
 * 
 * @author Bo Ilic
 * @author Edward Lam
 */
final class EventLoop {
    
    private final MessageConsole messageConsole;
    private final MessageConsoleStream out;

    private final EntryPoint entryPoint;
    private final CALExecutor executor;
    
    private static final String WORKSPACE_FILE_NAME = "cal.platform.test.cws";  
        
    /**
     * Private constructor for this class.
     * Use factory method to obtain instances.
     * 
     * @param executor
     * @param entryPoint
     * @param messageConsole
     * @param out
     */
    private EventLoop(CALExecutor executor, EntryPoint entryPoint, MessageConsole messageConsole, MessageConsoleStream out) {
        this.executor = executor;
        this.entryPoint = entryPoint;
        this.messageConsole = messageConsole;
        this.out = out;
    }
    
    /**
     * Factory method for this class.
     * @return a new EventLoop instance.
     */
    public static EventLoop makeEventLoop() {
        // Create the console first since we will use it to print out error messages if construction fails.
        String consoleName = "EventLoop";
        ConsolePlugin plugin = ConsolePlugin.getDefault();
        IConsoleManager conMan = plugin.getConsoleManager();

        MessageConsole messageConsole = new MessageConsole(consoleName, null);
        conMan.addConsoles(new IConsole[]{messageConsole});
        
        MessageConsoleStream out = messageConsole.newMessageStream();


        // Create the CALServices.
        BasicCALServices calServices = BasicCALServices.make(WORKSPACE_FILE_NAME);

        // Compile the workspace.
        CompilerMessageLogger messageLogger = new MessageLogger();        
        if (!calServices.compileWorkspace(null, messageLogger)) {
            writeToConsole(messageLogger.toString(), messageConsole, out);
            return null;
        }

        // Create the entry point.
        EntryPointSpec allPrimesEntryPointSpec =
            EntryPointSpec.make(QualifiedName.make(CALPlatformTestModuleNames.M1, "allPrimes"), null, OutputPolicy.ITERATOR_OUTPUT_POLICY);        
        
        EntryPoint entryPoint =
            calServices.getCompiler().getEntryPoint(allPrimesEntryPointSpec, CALPlatformTestModuleNames.M1, messageLogger);

        if (messageLogger.getNMessages() > 0) {
            writeToConsole(messageLogger.toString(), messageConsole, out);
            return null;
        }

        // Create the executor and execution context.
        WorkspaceManager workspaceManager = calServices.getWorkspaceManager();
        ExecutionContext executionContext = workspaceManager.makeExecutionContextWithDefaultProperties();        
        CALExecutor executor = workspaceManager.makeExecutor(executionContext);

        return new EventLoop(executor, entryPoint, messageConsole, out);
    }
        
    /**
     * Evaluates M1.allPrimes and gets the next primes.
     * Note the reuse of the EntryPoint over multiple executions.
     * @param nPrimesMore number of times to call the function in a row.
     */    
    void getNextNPrimes(int nPrimesMore) {
        
        try {
            Iterator<?> primesIterator = (Iterator<?>)executor.exec(entryPoint, null);
            List<Object> nextNPrimes = new ArrayList<Object>(nPrimesMore);
            for (int i = 0; i < nPrimesMore; ++i) {
                nextNPrimes.add(primesIterator.next());
            }
            writeToConsole("the next " + nPrimesMore + " primes are " + nextNPrimes);
        
        } catch (CALExecutorException executorException) {
            writeToConsole(executorException.toString());
        }                                  
    }

    /**
     * Write a message to the Eclipse console using a message console.
     * Ensures the console is visible when the message is displayed..
     * @param message the message to write.
     */
    private void writeToConsole(String message) {
        writeToConsole(message, messageConsole, out);
    }

    /**
     * Write a message to the Eclipse console using a message console.
     * Ensures the console is visible when the message is displayed..
     * @param message the message to write.
     * @param messageConsole the console to which to write.
     * @param out the output stream to which to write.
     */
    private static void writeToConsole(String message, MessageConsole messageConsole, MessageConsoleStream out) {
        String id = IConsoleConstants.ID_CONSOLE_VIEW;
        IWorkbenchPage activePage = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage();

        // Show the console view.
        IConsoleView view;
        try {
            view = (IConsoleView)activePage.showView(id);
        } catch (PartInitException e) {
            IStatus status = new Status(Status.ERROR, ExamplePlugin.PLUGIN_ID, Status.ERROR, message, e); 
            ExamplePlugin.getDefault().getLog().log(status);
            return;
        }
        
        // Display the message console in the console view.
        view.display(messageConsole);
        
        // write the message to the message console.
        out.println(message);
    }

}

