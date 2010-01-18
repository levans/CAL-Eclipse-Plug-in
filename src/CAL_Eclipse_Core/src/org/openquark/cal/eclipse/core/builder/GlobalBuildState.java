/*******************************************************************************
 * Copyright (c) 2005 Business Objects Software Limited and others.
 * All rights reserved. 
 * This file is made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Business Objects Software Limited - initial API and implementation based on Eclipse 3.1.2 code for
 *                             /org.eclipse.jdt.core/model/org/eclipse/jdt/internal/core/builder/BuildNotifier.java
 *                           Eclipse source is available at: http://www.eclipse.org/downloads/
 *******************************************************************************/

/*
 * BuildNotifier.java
 * Creation date: Nov 2, 2005.
 * By: Edward Lam
 */
package org.openquark.cal.eclipse.core.builder;

import org.eclipse.core.resources.IMarker;
import org.eclipse.core.runtime.IProgressMonitor;
import org.openquark.cal.eclipse.core.compiler.IProblem;
import org.openquark.cal.eclipse.core.util.Messages;

/**
 * The global build state -- based on BuildNotifier from JDT Core.
 * This is intended to be used to track state across a complete build iteration.
 * 
 * @author Edward Lam
 */
public class GlobalBuildState {
    
    /*
     * TODOEL:
     * Not currently used.
     * 
     * This is intended to be used for tracking errors, and displaying problems found/fixed messages, 
     *  during and after compilation.
     */

    /*
     * State modified while a build is in progress.
     */
    protected int newErrorCount;
    protected int fixedErrorCount;
    protected int newWarningCount;
    protected int fixedWarningCount;
    
    /*
     * State between build iterations.
     */
    public static int NewErrorCount = 0;
    public static int FixedErrorCount = 0;
    public static int NewWarningCount = 0;
    public static int FixedWarningCount = 0;

    public static void resetProblemCounters() {
        NewErrorCount = 0;
        FixedErrorCount = 0;
        NewWarningCount = 0;
        FixedWarningCount = 0;
    }

    public GlobalBuildState() {
        this.newErrorCount = NewErrorCount;
        this.fixedErrorCount = FixedErrorCount;
        this.newWarningCount = NewWarningCount;
        this.fixedWarningCount = FixedWarningCount;
    }

    /**
     * Notification before a compile that a unit is about to be compiled.
     * @param unitName the name of the object being compiled
     * @param monitor progress monitor for the task
     */
    public void aboutToCompile(String unitName, IProgressMonitor monitor) {
        String message = Messages.bind(Messages.build_compiling, unitName);
        subTask(message, monitor);
    }

    
    public void begin(IProgressMonitor monitor) {  }


    public void done(IProgressMonitor monitor) {
        NewErrorCount = this.newErrorCount;
        FixedErrorCount = this.fixedErrorCount;
        NewWarningCount = this.newWarningCount;
        FixedWarningCount = this.fixedWarningCount;
//        updateProgress(1.0f);
        subTask(Messages.build_done, monitor);
//        if (monitor != null) {
//            monitor.done();
//        }
    }

    /**
     * Returns a string describing the problems.
     */
    protected String problemsMessage() {
        int numNew = newErrorCount + newWarningCount;
        int numFixed = fixedErrorCount + fixedWarningCount;
        
        if (numNew == 0 && numFixed == 0) {
            return ""; //$NON-NLS-1$
        }
        
        boolean displayBoth = numNew > 0 && numFixed > 0;
        StringBuilder buffer = new StringBuilder();
        buffer.append('(');
        
        if (numNew > 0) {
            // (Found x errors + y warnings)
            buffer.append(Messages.build_foundHeader);
            buffer.append(' ');
            
            if (displayBoth || newErrorCount > 0) {
                if (newErrorCount == 1) {
                    buffer.append(Messages.build_oneError);
                } else {
                    buffer.append(Messages.bind(Messages.build_multipleErrors, String.valueOf(newErrorCount)));
                }
                
                if (displayBoth || newWarningCount > 0) {
                    buffer.append(" + "); //$NON-NLS-1$
                }
            }
            
            if (displayBoth || newWarningCount > 0) {
                if (newWarningCount == 1) {
                    buffer.append(Messages.build_oneWarning);
                } else {
                    buffer.append(Messages.bind(Messages.build_multipleWarnings, String.valueOf(newWarningCount)));
                }
            }

            if (numFixed > 0) {
                buffer.append(", "); //$NON-NLS-1$
            }
        }
        if (numFixed > 0) {
            // (Fixed x errors + y warnings) or (Found x errors + y warnings, Fixed x + y)
            buffer.append(Messages.build_fixedHeader);
            buffer.append(' ');
            
            if (displayBoth) {
                buffer.append(String.valueOf(fixedErrorCount));
                buffer.append(" + "); //$NON-NLS-1$
                buffer.append(String.valueOf(fixedWarningCount));
            } else {
                if (fixedErrorCount > 0) {
                    if (fixedErrorCount == 1) {
                        buffer.append(Messages.build_oneError);
                    } else {
                        buffer.append(Messages.bind(Messages.build_multipleErrors, String.valueOf(fixedErrorCount)));
                    }
                    
                    if (fixedWarningCount > 0) {
                        buffer.append(" + "); //$NON-NLS-1$
                    }
                }
                if (fixedWarningCount > 0) {
                    if (fixedWarningCount == 1) {
                        buffer.append(Messages.build_oneWarning);
                    } else {
                        buffer.append(Messages.bind(Messages.build_multipleWarnings, String.valueOf(fixedWarningCount)));
                    }
                }
            }
        }
        
        buffer.append(')');
        
        return buffer.toString();
    }

    public void subTask(String message, IProgressMonitor monitor) {
        String pm = problemsMessage();
        String msg = pm.length() == 0 ? message : pm + " " + message; //$NON-NLS-1$
        //if (JavaBuilder.DEBUG) System.out.println(msg);
        if (monitor != null) {
            monitor.subTask(msg);
        }
    }

    protected void updateProblemCounts(IProblem[] newProblems) {
        for (final IProblem newProblem : newProblems) {
            if (newProblem.isError()) {
                newErrorCount++;
            } else {
                newWarningCount++;
            }
        }
    }

    /**
     * Update the problem counts from one compilation result given the old and new problems,
     * either of which may be null.
     */
    protected void updateProblemCounts(IMarker[] oldProblems, IProblem[] newProblems) {
        if (newProblems != null) {
            
            next: for (int i = 0, l = newProblems.length; i < l; i++) {
                IProblem newProblem = newProblems[i];

//                if (newProblem.getID() == IProblem.Task) {
//                    continue; // skip task
//                }
                
                boolean isError = newProblem.isError();
                
                String message = newProblem.getMessage();
                if (oldProblems != null) {
                    
                    for (int j = 0, m = oldProblems.length; j < m; j++) {
                        IMarker pb = oldProblems[j];
                        
                        if (pb == null) {
                            continue; // already matched up with a new problem
                        }
                        
                        boolean wasError = IMarker.SEVERITY_ERROR == pb.getAttribute(IMarker.SEVERITY, IMarker.SEVERITY_ERROR);
                        if (isError == wasError && message.equals(pb.getAttribute(IMarker.MESSAGE, ""))) { //$NON-NLS-1$
                            oldProblems[j] = null;
                            continue next;
                        }
                    }
                }
                if (isError) {
                    newErrorCount++;
                } else {
                    newWarningCount++;
                }
            }
        }
        
        if (oldProblems != null) {
            
            next: for (int i = 0, l = oldProblems.length; i < l; i++) {
                IMarker oldProblem = oldProblems[i];
                
                if (oldProblem == null) {
                    continue next; // already matched up with a new problem
                }
                
                boolean wasError = IMarker.SEVERITY_ERROR == oldProblem.getAttribute(IMarker.SEVERITY, IMarker.SEVERITY_ERROR);
                String message = oldProblem.getAttribute(IMarker.MESSAGE, ""); //$NON-NLS-1$
                
                if (newProblems != null) {
                    
                    for (int j = 0, m = newProblems.length; j < m; j++) {
                        IProblem pb = newProblems[j];
                        
//                        if (pb.getID() == IProblem.Task) {
//                            continue; // skip task
//                        }
                        
                        if (wasError == pb.isError() && message.equals(pb.getMessage())) {
                            continue next;
                        }
                    }
                }
                
                if (wasError) {
                    fixedErrorCount++;
                } else {
                    fixedWarningCount++;
                }
            }
        }
    }

}
