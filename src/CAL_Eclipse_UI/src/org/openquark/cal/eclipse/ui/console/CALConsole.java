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
 * CALConsole.java
 * Creation date: Jul 24, 2007.
 * By: Edward Lam
 */
package org.openquark.cal.eclipse.ui.console;


import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.logging.Formatter;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;
import java.util.logging.StreamHandler;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.debug.ui.IDebugUIConstants;
import org.eclipse.debug.ui.console.ConsoleColorProvider;
import org.eclipse.debug.ui.console.IConsoleColorProvider;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.swt.graphics.Font;
import org.eclipse.ui.console.ConsolePlugin;
import org.eclipse.ui.console.IConsole;
import org.eclipse.ui.console.IConsoleManager;
import org.eclipse.ui.console.IOConsole;
import org.eclipse.ui.console.IOConsoleInputStream;
import org.eclipse.ui.console.IOConsoleOutputStream;
import org.openquark.cal.ConsoleRunner;
import org.openquark.cal.compiler.AdjunctSource;
import org.openquark.cal.compiler.CompilerMessage;
import org.openquark.cal.compiler.CompilerMessageLogger;
import org.openquark.cal.compiler.MessageLogger;
import org.openquark.cal.compiler.ModuleName;
import org.openquark.cal.compiler.ModuleNameResolver;
import org.openquark.cal.compiler.ScopedEntityNamingPolicy;
import org.openquark.cal.compiler.TypeExpr;
import org.openquark.cal.eclipse.core.CALModelManager;
import org.openquark.cal.eclipse.core.util.Util;
import org.openquark.cal.eclipse.ui.CALEclipseUIPlugin;
import org.openquark.cal.services.ProgramModelManager;

/**
 * A console which can be used for interactive evaluation of CAL expressions.
 * @author Edward Lam
 */
public class CALConsole extends IOConsole {

    public final static String CONSOLE_TYPE = "calConsole"; //$NON-NLS-1$

    // IDebugPreferenceConstants.CONSOLE_FONT / Eclipse 3.3: IDebugUIConstants.PREF_CONSOLE_FONT
    public static final String CONSOLE_FONT= "org.eclipse.debug.ui.consoleFont";

    /** The namespace for CAL log messages. */
    private static final String calLoggerNamespace = "org.openquark.cal.eclipse.ui.console";

    /** An instance of a Logger for cal messages. */
    private Logger calLogger;

    /** The ConsoleRunner instance for this console. */
    private final CALConsoleRunner calConsoleRunner;
    
    /** The name of the current module, with respect to which the evaluation will take place. */
    private ModuleName currentModuleName = ModuleName.make("Cal.Core.Prelude");

    /** The background job to read input from the user and take appropriate actions in response. */
    private InputReadJob readJob;
    
    /** Remembered previous commands. */
    private List<String> commandHistory = new ArrayList<String>();

    /** Listener for changes in the console font. */
    private final IPropertyChangeListener propertyListener = new IPropertyChangeListener() {
        public void propertyChange(PropertyChangeEvent event) {
            String property = event.getProperty();
            if (property.equals(CONSOLE_FONT)) {
                setFont(JFaceResources.getFont(CONSOLE_FONT));
            }
        }
    };

    /**
     * A StreamHandler which simply outputs log records to the given output stream.
     * @author Edward Lam
     */
    private static class OutputStreamStreamHandler extends StreamHandler {
        
        /**
         * Constructor for an OutputStreamStreamHandler
         */
        public OutputStreamStreamHandler(OutputStream outputStream) {
            super(outputStream, new ConsoleFormatter());
        }
        
        /** Override this to always flush the stream. */
        @Override
        public void publish(LogRecord record) {
            super.publish(record);
            flush();
        }

        /** Override to just flush the stream, we don't want to close System.out. */
        @Override
        public void close() {
            flush();
        }
    }

    /**
     * A log message formatter that simply outputs the message of the log record, 
     *   plus the text of any throwable.
     * Used to print messages to the console.
     */
    private static class ConsoleFormatter extends Formatter {

        /**
         * {@inheritDoc}
         */
        @Override
        public String format(LogRecord record) {

            StringBuilder sb = new StringBuilder();

            // Append the log message
            sb.append(record.getMessage() + "\n");

            // Append the throwable if there is one
            if (record.getThrown() != null) {
                try {
                    StringWriter sw = new StringWriter();
                    PrintWriter pw = new PrintWriter(sw);
                    record.getThrown().printStackTrace(pw);
                    pw.close();
                    sb.append(sw.toString());
                } catch (Exception ex) {
                    sb.append("Failed to generate a stack trace for the throwable");
                }
            }
            
            return sb.toString();
        }
    }

    /**
     * Console runner for the CAL Console.
     * @author Edward Lam
     */
    private class CALConsoleRunner extends ConsoleRunner {

        /**
         * Constructor for a CALConsoleRunner
         * @param calLogger
         */
        public CALConsoleRunner(Logger calLogger) {
            super(calLogger);
        }

        /**
         * {@inheritDoc}
         */
        public ProgramModelManager getProgramModelManager() {
            return CALConsole.this.getProgramModelManager();
        }
        
        /**
         * {@inheritDoc}
         */
        public boolean hasModuleSource(ModuleName moduleName) {
            return CALModelManager.getCALModelManager().getInputSourceFile(moduleName) != null;
        }
    }

    /**
     * The Job which hangs around reading input from the console and responding to it with output.
     * @author Edward Lam
     */
    private class InputReadJob extends Job {

        private final IOConsoleOutputStream outputStream;
        private final IOConsoleInputStream inputStream;

        private final StreamHandler consoleHandler;
        
        /** Set to true by finishUp, indicating that this Job should exit. */
        private volatile boolean shouldDie = false;  

        /**
         * Constructor for an InputReadJob
         */
        InputReadJob() {
            super("Input Job for CAL"); //$NON-NLS-1$
            
            IConsoleColorProvider colorProvider = new ConsoleColorProvider();
            
            this.inputStream = CALConsole.this.getInputStream();
            this.inputStream.setColor(colorProvider.getColor(IDebugUIConstants.ID_STANDARD_INPUT_STREAM));

            this.outputStream = CALConsole.this.newOutputStream();
            this.outputStream.setColor(colorProvider.getColor(IDebugUIConstants.ID_STANDARD_OUTPUT_STREAM));

            consoleHandler = new OutputStreamStreamHandler(outputStream);
            
            // Note that we can add other handlers which log more about what's happening.
            calLogger.addHandler(consoleHandler);
        }

        /**
         * Cause this job to wrap up whatever it's doing and exit.
         * Called by CALConsole.dispose()
         */
        protected void finishUp() {
            this.shouldDie = true;
            
            try {
                calLogger.removeHandler(consoleHandler);
                outputStream.close();
            } catch (IOException e) {
                Util.log(e, "Exception closing stream.");
            }
            
            // This is somewhat of a hack -- we know that inputStream returns from its wait after interruption
            synchronized (runThreadAccessLock) {
                if (runThread != null) {
                    runThread.interrupt();
                }
            }
        }

        private Thread runThread = null;
        private final byte[] runThreadAccessLock = new byte[0];     // used to synchronize access to the runThread field.
        
        @Override
        protected IStatus run(IProgressMonitor monitor) {
            synchronized (runThreadAccessLock) {
                runThread = Thread.currentThread();
            }
            try {
                outputStream.write("Enter an expression to evaluate.  :h for help.\n");
                displayPrompt();
                
                byte[] b = new byte[1024];
                int read = 0;
                while (true) {
                    read = inputStream.read(b);
                    
                    if (shouldDie) {
                        return Status.CANCEL_STATUS;
                    }
                    
                    if (read > 0) {
                        String enteredText = new String(b, 0, read);
                        handleRequest_enteredText(enteredText);
                    }
                    
                    displayPrompt();
                }
            } catch (IOException e) {
                CALEclipseUIPlugin.log(e);
            
            } finally {
                synchronized (runThreadAccessLock) {
                    runThread = null;
                }
            }
            
            // unreachable..
            return Status.OK_STATUS;
        }
        
        /**
         * Display the input prompt.
         */
        void displayPrompt() {
            try {
                outputStream.write(getPrompt());
            } catch (IOException e) {
                CALEclipseUIPlugin.log(e);
            }
        }
    }
    
    /**
     * Constructor for a CAL Console
     */
    public CALConsole() {
        super("CAL Console", CONSOLE_TYPE, null, true);
        Font font = JFaceResources.getFont(CONSOLE_FONT);
        setFont(font);
        
        calLogger = Logger.getLogger(calLoggerNamespace);

        calLogger.setLevel(Level.FINEST);
        calLogger.setUseParentHandlers(false);
        
        calConsoleRunner = new CALConsoleRunner(calLogger);
    }

    /**
     * Show the CAL Console in the Console view.
     * Note that this isn't thread safe as it internally iterates through the consoles in the console manager 
     * to find any existing CALConsole instance.  This should be fine though if consoles are only show as a result of
     * UI actions (which don't tend to happen concurrently).
     * 
     * @return the CALConsole instance being shown.  There will only be one of these in the Console manager.
     */
    public static CALConsole showCALConsole() {
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

        return existingCALConsole;
    }

    @Override
    protected void init() {
        JFaceResources.getFontRegistry().addListener(propertyListener);
        readJob = new InputReadJob();
        readJob.setSystem(true);
        readJob.schedule();
    }

    @Override
    protected void dispose() {
        JFaceResources.getFontRegistry().removeListener(propertyListener);
        readJob.finishUp();
        readJob = null;
        calLogger = null;
        super.dispose();
    }
    
    @Override
    public void clearConsole() {
        // TODOEL: display prompt when clearing the console (if not running).
        // TODOEL: Handle calls while something is running.
        super.clearConsole();
    }
    
    /**
     * Set the document's initial contents.
     * Called by CALConsoleFactory.
     */
    void initializeDocument() {
        getDocument().set("");
    }
    
    /**
     * @return the program model manager from the CALModelManager
     */
    private ProgramModelManager getProgramModelManager() {
        return CALModelManager.getCALModelManager().getProgramModelManager();
    }

    /**
     * @return the name of the current module, with respect to which the evaluation will take place.
     */
    private ModuleName getCurrentModuleName() {
        return currentModuleName;
    }
    
    /**
     * @return the string to display as the prompt
     */
    private String getPrompt() {
        return getCurrentModuleName() + ">";
    }

    /**
     * Handle the given text as entered by the user.
     * This may cause an expression to be run.
     * @param enteredText the text which was entered
     * @return whether execution terminated normally.
     */
    private boolean handleRequest_enteredText(String enteredText) {
        
        enteredText = enteredText.trim();

        if (handleConsoleCommand(enteredText)) {
            return true;
        }
        if (enteredText.length() < 1) {
            return true;
        }
        
        return calConsoleRunner.runExpression(getCurrentModuleName(), enteredText, true);
    }
    
    /** 
     * Helper to dump the console help to the console.
     */
    private void showHelp() {
        calLogger.info(":h[elp]                     Show this help.");
        calLogger.info(":sm <target_module_name>    Set the module in which to evaluate.");
        calLogger.info(":t <expression>             Display the type of the given expression.");
        calLogger.info(":rs [module name]           Reset any cached CAFs in the named module, or all modules if none specified.");

        calLogger.info("");

        calLogger.info(":spc                        Show previously executed commands in a numbered list");
        calLogger.info(":pc <command number>        Execute a previous command indicated by the command number");
    }

    /**
     * @param enteredText
     * @return whether the entered text corresponds to a console command.
     */
    private boolean handleConsoleCommand(String enteredText) {
        if (!enteredText.startsWith(":")) {
            commandHistory.add(enteredText);
            return false;
        }
        
        String[] split = enteredText.split(" ");
        List<String> tokens = new ArrayList<String>();
        for (final String s : split) {
            if (s.length() > 0) {
                tokens.add(s);
            }
        }
        
        String command = tokens.get(0).toLowerCase();
        
        if (command.equals(":h") || command.equals(":help")) {
            showHelp();
        
        } else if (command.equals(":sm")) {
            // set the module
            if (checkNArgs(tokens, 1)) {
                setModule(tokens.get(1));
            }
        
            commandHistory.add(enteredText);

        } else if (command.equals(":t")) {
            // display type
            String argString = getTrimmedArgString(enteredText, ":t");

            String qualifiedCodeExpression = calConsoleRunner.qualifyCodeExpression(currentModuleName, argString);
            if (qualifiedCodeExpression != null) {
                // pick a name which doesn't exist in the module.
                String targetName = "target";
                int index = 1;
                while (getProgramModelManager().getModuleTypeInfo(currentModuleName).getFunctionalAgent(targetName) != null) {
                    targetName = "target" + index;
                    index++;
                }
                CompilerMessageLogger logger = new MessageLogger();
                String scDef = targetName + " = \n" + qualifiedCodeExpression + "\n;";

                TypeExpr te = getProgramModelManager().getTypeChecker().checkFunction(new AdjunctSource.FromText(scDef), currentModuleName, logger);
                if (te == null) {
                    calLogger.info("Attempt to type expression has failed because of errors: ");
                    dumpCompilerMessages(logger);
                
                } else {
                    //we want to display the type using fully qualified type constructor names, but also making use of 
                    //preferred names of type are record variables
                    calLogger.info("  " + te.toString(true, ScopedEntityNamingPolicy.FULLY_QUALIFIED) + "\n");
                }
            }

            commandHistory.add(enteredText);

        } else if (command.equals(":rs")) {
            // reset cached CAFs
            if (tokens.size() == 1) {
                calConsoleRunner.command_resetCachedResults(null);
            
            } else {
                String trimmedArgString = getTrimmedArgString(enteredText, ":rs");
                ModuleName moduleName = resolveModuleNameInProgram(trimmedArgString);
                if (moduleName != null) {
                    calConsoleRunner.command_resetCachedResults(moduleName);
                }
            }
            
            commandHistory.add(enteredText);
        
        } else if (command.equals(":spc")) {
            // show previous commands
            if (checkNArgs(tokens, 0)) {
                int i = 1;
                for (String commandString : commandHistory) {
                    calLogger.info(i + "> " + commandString);
                    i++;
                }
            }

        } else if (command.equals(":pc")) {
            // execute a previous command
            if (checkNArgs(tokens, 1)) {
                String argString = getTrimmedArgString(enteredText, ":pc");
                try {
                    Integer n = Integer.decode(argString);
                    if (n.intValue() <= 0 || n.intValue() > commandHistory.size()) {
                        calLogger.info("\"" + argString + "\" is not a valid command number.");
                    } else {
                        handleRequest_enteredText(commandHistory.get(n.intValue() - 1));
                    }
                
                } catch (NumberFormatException e) {
                    calLogger.info("\"" + argString + "\" is not a valid command number.");
                }
            }
        } else {
            calLogger.severe("Unknown command.  :h for help.");
        }
        
        return true;
    }
    
    /**
     * Helper method for handleConsoleCommand.
     * 
     * @param tokens the tokens from the entered command
     * @param nExpectedArgs the number of arguments expected for the command
     * @return if the number of arguments is not the expected number (according to the size of the tokens list)
     * then an error message is logged and false is returned.  Otherwise true is returned.
     */
    private boolean checkNArgs(List<String> tokens, int nExpectedArgs) {
        if (tokens.size() != (nExpectedArgs + 1)) {
            calLogger.severe("Invalid number of arguments.");
            return false;
        }
        return true;
    }
    
    /**
     * Helper method for handleConsoleCommand.
     * @param enteredText the text which was entered
     * @param commandString the string form of the token representing the command eg. ":h"
     * @return the part of the entered text representing the arguments to the command, ie. enteredText minus the commandString.
     * This will be a trimmed string.
     */
    private static String getTrimmedArgString(String enteredText, String commandString) {
        return enteredText.substring(enteredText.indexOf(commandString) + commandString.length()).trim();
    }
    
    /**
     * Dump the messages contained in logger to the console
     * @param logger A CompilerMessageLogger
     */
    private void dumpCompilerMessages(CompilerMessageLogger logger) {
        for (final CompilerMessage message : logger.getCompilerMessages()) {
            calLogger.info("  " + message.toString());
        }
    }

    /**
     * Set the current module.
     * @param moduleNameString the name of the module to be set as the current module.
     * This may be unqualified or partially-qualified.
     */
    private void setModule(String moduleNameString) {
        // Do some work to resolve partially-qualified module names.
        ModuleName resolvedModuleName = resolveModuleNameInProgram(moduleNameString);
        if (resolvedModuleName != null) {
            currentModuleName = resolvedModuleName;
        }
    }
    
    /**
     * Resolves the given module name in the context of the current program. If the given name cannot be unambiguously resolved, null is returned.
     * @param moduleNameString the module name to resolve.
     * @return the corresponding fully qualified module name, or null if the given name cannot be unambiguously resolved.
     */
    private ModuleName resolveModuleNameInProgram(String moduleNameString) {
        ModuleName[] moduleNamesInProgram = getProgramModelManager().getModuleNamesInProgram();
        
        ModuleNameResolver moduleNameResolver = ModuleNameResolver.make(new HashSet<ModuleName>(Arrays.asList(moduleNamesInProgram)));

        ModuleName moduleName = ModuleName.maybeMake(moduleNameString);

        if (moduleName == null) {
            calLogger.log(Level.INFO, moduleNameString + " is not a valid module.");
            
        } else {
            ModuleNameResolver.ResolutionResult resolution = moduleNameResolver.resolve(moduleName);

            if (resolution.isKnownUnambiguous()) {
                ModuleName resolvedModuleName = resolution.getResolvedModuleName();
                
                // The module name can be resolved. 
                // Check that it is compiled.
                if (getProgramModelManager().getModule(resolvedModuleName) == null) {
                    calLogger.log(Level.SEVERE, "Module \"" + resolvedModuleName + "\" has compilation errors (or is currently in the process of being compiled).");

                } else {
                    return resolvedModuleName;
                }

            } else {
                if (resolution.isAmbiguous()) {
                    // The partially qualified name is ambiguous, so show the potential matches
                    calLogger.log(Level.INFO, "The module name " + moduleName + " is ambiguous. Do you mean one of:");
                    ModuleName[] potentialMatches = resolution.getPotentialMatches();
                    for (final ModuleName element : potentialMatches) {
                        calLogger.log(Level.INFO, "    " + element);
                    }

                } else {
                    calLogger.log(Level.INFO, moduleName + " is not a valid module.");
                }
            }
        }
        
        return null;
    }

    /**
     * External call to set the current module to the given module and run the given expression.
     * @param moduleNameString the text of the module name.
     * @param expressionText the text of the expression to run
     * @return whether execution terminated normally.
     */
    boolean handleRequest_setModuleAndRunExpression(String moduleNameString, String expressionText) {
        // Add a newline so that subsequent output doesn't appear after the display prompt.
        calLogger.info("");
        
        setModule(moduleNameString);
        boolean retVal = calConsoleRunner.runExpression(getCurrentModuleName(), expressionText, true);
        
        // display the prompt.
        readJob.displayPrompt();

        return retVal;
    }

    /**
     * External call to set the current module to the given module
     * @param moduleNameString the text of the module name.
     */
    void handleRequest_setModule(String moduleNameString) {
        // Add a newline so that subsequent output doesn't appear after the display prompt.
        calLogger.info("");
        
        setModule(moduleNameString);
        
        // display the prompt.
        readJob.displayPrompt();
    }

    /**
     * Terminate execution of the currently running expression if any.
     */
    public void terminateExecution() {
        calConsoleRunner.terminateExecution();
    }

}
