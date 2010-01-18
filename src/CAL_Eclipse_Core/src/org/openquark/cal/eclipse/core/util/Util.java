/*******************************************************************************
 * Copyright (c) 2005 Business Objects Software Limited and others.
 * All rights reserved. 
 * This file is made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Business Objects Software Limited - initial API and implementation
 *******************************************************************************/

/*
 * Util.java
 * Creation date: Nov 3, 2005.
 * By: Edward Lam
 */
package org.openquark.cal.eclipse.core.util;

import java.util.Date;

import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IStorage;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.SubProgressMonitor;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaCore;
import org.openquark.cal.compiler.ModuleName;
import org.openquark.cal.eclipse.core.CALEclipseCorePlugin;
import org.openquark.cal.services.CALSourcePathMapper;
import org.openquark.cal.services.FileSystemResourceHelper;


/**
 * Provides convenient utility methods to other types in this package.
 * 
 * Based on org.eclipse.jdt.internal.core.util.Util.
 * @author Edward Lam
 */
public class Util {
    
    // uninstantiable
    private Util() { }
    
    
    public static void log(String message) {
        log(new Exception(), message);
    }
    
    /**
     * Add a log entry for an exception.
     * @param e the exception to log.
     * @param message the associated message.
     */
    public static void log(Throwable e, String message) {
        log(e, message, IStatus.ERROR);
    }       
 
    /**
     * Add a log entry for an exception.
     * @param e the exception to log.
     * @param message the associated message.
     * @param severity one of the severity constants in IStatus.
     */
    public static void log(Throwable e, String message, int severity) {
        IStatus status = new Status(severity, CALEclipseCorePlugin.PLUGIN_ID, severity, message, e); 
        CALEclipseCorePlugin.getDefault().getLog().log(status);
    }
    
    /**
     * Dump a message to the console, with the current time and date.
     * @param message the message to print.
     */
    public static void printlnWithDate(String message) {
        System.out.println(message + " @ " + new Date(System.currentTimeMillis())); //$NON-NLS-1$
    }
    
    /*
     * Copied from org.eclipse.core.internal.utils.Policy.
     */
    
    /**
     * @param monitor the monitor to check.
     * @throws OperationCanceledException if the monitor is non-null and canceled.
     */
    public static void checkCanceled(IProgressMonitor monitor) {
        if (monitor != null && monitor.isCanceled()) {
            throw new OperationCanceledException();
        }
    }
    
    /**
     * Get a non-null monitor based on the given input monitor.
     * @param monitor a monitor, or null.
     * @return the monitor, or a new null progress monitor if the input was null.
     */
    public static IProgressMonitor monitorFor(IProgressMonitor monitor) {
        return monitor == null ? new NullProgressMonitor() : monitor;
    }
    
    /**
     * Get a subprogress monitor of a given monitor.
     * @param monitor the monitor for which to get a subprogress monitor.
     * @param ticks the number of ticks from the monitor to allocate.
     * @return a subprogress monitor with the given attributes.
     */
    public static IProgressMonitor subMonitorFor(IProgressMonitor monitor, int ticks) {
        if (monitor == null) {
            return new NullProgressMonitor();
        }
        if (monitor instanceof NullProgressMonitor) {
            return monitor;
        }
        return new SubProgressMonitor(monitor, ticks);
    }
    
    /**
     * Get a subprogress monitor of a given monitor.
     * @param monitor the monitor for which to get a subprogress monitor.
     * @param ticks the number of ticks from the monitor to allocate.
     * @param style the style of the sub monitor.
     * @return a subprogress monitor with the given attributes.
     */
    public static IProgressMonitor subMonitorFor(IProgressMonitor monitor, int ticks, int style) {
        if (monitor == null) {
            return new NullProgressMonitor();
        }
        if (monitor instanceof NullProgressMonitor) {
            return monitor;
        }
        return new SubProgressMonitor(monitor, ticks, style);
    }
    
    /**
     * Print a debug message to the console. 
     * Pre-pend the message with the current date and the name of the current thread.
     * @param message the message to debug
     */
    public static void debug(String message) {
        System.out.println(new Date(System.currentTimeMillis()) + " - [" + Thread.currentThread().getName() + "] " + message);
    }

    /**
     * Creates a folder and recursively creates all parent folders if not existing.
     * Project must exist.
     * This method is a wrapper for IFolder.create
     * 
     * <code> org.eclipse.ui.dialogs.ContainerGenerator</code> is too heavy
     * (creates a runnable)
     * @param folder the folder to create
     * @param force force creation even if parent is read-only
     * @param local is this a local folder?
     * @param monitor progress monitor for the task.
     * @throws CoreException thrown if there is an error in creating the folder.
     */
    public static void createFolder(IFolder folder, boolean force, boolean local, 
            IProgressMonitor monitor) throws CoreException {
        if (!folder.exists()) {
            IContainer parent = folder.getParent();
            if (parent instanceof IFolder) {
                createFolder((IFolder)parent, force, local, null);
            }
            folder.create(force, local, monitor);
        }
    }
    
    public static boolean isCalResource(Object obj) {
        if (obj instanceof IStorage) {
            IStorage storage = (IStorage) obj;
                
            String name = storage.getName();
            return name.endsWith(
                    CALSourcePathMapper.CAL_FILE_EXTENSION);
        } else {
            return false;
        }
    }
    
    public static boolean isCalResourceOfModule(Object obj, ModuleName moduleName) {
        if (isCalResource(obj)) {
            IStorage storage = (IStorage) obj;
            return storage.getName().equals(moduleName.getModuleName().getLastComponent() + 
                    "." + CALSourcePathMapper.CAL_FILE_EXTENSION);
        } else {
            return false;
        }
    }
    
    /**
     * @param project to check for CAL nature
     * @return true if this project has a CAL nature
     */
    public static boolean isCalProject(IProject project) {
        try {
            return project.hasNature(CALEclipseCorePlugin.NATURE_ID)
                && project.hasNature(JavaCore.NATURE_ID);
        } catch (CoreException e) {
            // if project does not exist or is not open
            return false;
        }
    }
    
    /**
     * Returns a JavaProject for the given project if this project exists,
     * is open, is a java project AND has a CAL nature.
     * @param project the project
     * @return java project corresponding to the project passed in
     */
    public static IJavaProject getCalProject(IProject project) {
        if (project.isAccessible() && isCalProject(project)) {
            return JavaCore.create(project);
        } else {
            return null;
        }
    }
    
    /**
     * This returns a ModuleName given a storage object (either a file or a jar entry.  
     * This storage object's full path should contain the "CAL" segment.  The assumption is
     * that everything after the last CAL segment is part of the module path. Everything before
     * is discarded.
     * 
     * The module name is the file name and module path with the extension removed.
     * <p>
     * If the CAL segment is not found or the extension is not correct, then the file path is not 
     * valid and we return null.
     * 
     * @param storage the file or jar entry
     * @return the module name of this storage object or null if the storage doesn't correspond to a valid module name.
     */
    public static ModuleName getModuleNameFromStorage(IStorage storage) {
        String fileName = storage.getName();
        String simpleName = FileSystemResourceHelper.stripFileExtension(fileName, CALSourcePathMapper.CAL_FILE_EXTENSION, false);
        if (simpleName == null) {
            // extension was not ".cal"
            return null;
        }
        StringBuilder sb = new StringBuilder(simpleName);
        
        IPath fullPath = storage.getFullPath();
        String[] segments = fullPath.segments();
        for (int i = segments.length - 2 /* ignore the last segment since it has simpleName */; i >= 0; i--) {
            if (segments[i].equals(CALSourcePathMapper.SCRIPTS_BASE_FOLDER)) {
                break;
            }
            sb.insert(0, segments[i] + ".");
        }
        
        return ModuleName.maybeMake(sb.toString());
    }
}