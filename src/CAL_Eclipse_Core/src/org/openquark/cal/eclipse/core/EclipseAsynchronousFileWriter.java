/*******************************************************************************
 * Copyright (c) 2006 Business Objects Software Limited and others.
 * All rights reserved. 
 * This file is made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Business Objects Software Limited - initial API and implementation
 *******************************************************************************/

/*
 * EclipseAsynchronousFileWriter.java
 * Creation date: Jan 17, 2006.
 * By: Edward Lam
 */
package org.openquark.cal.eclipse.core;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.LinkedList;

import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.IWorkspaceRunnable;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.resources.WorkspaceJob;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.core.runtime.Status;
import org.openquark.cal.compiler.CompilerMessageLogger;
import org.openquark.cal.machine.AsynchronousFileWriter;
import org.openquark.cal.machine.ProgramResourceLocator;
import org.openquark.cal.machine.ProgramResourceRepository;



/**
 * An AsynchronousFileWriter which writes files within an Eclipse workspace.
 * Based on the BasicAsynchronousFileWriter by Bo Ilic.
 * 
 * @author Edward Lam
 */
public class EclipseAsynchronousFileWriter extends AsynchronousFileWriter {
    
    /** The workspace job which will actually do the writing of the files. */
    private final WriterJob writerJob = new WriterJob();

    /** The list of files to write out to disk */
    private final LinkedList<FileData> filesToWrite;              // TODOEL: Probably better to use LinkedQueue from concurrent.
    
    /** whether this ClassFileWriter will continue to accept new class files to write */
    volatile private boolean acceptFiles;
    
    /** 
     * total number of bytes that this AsynchronousFileWriter has outstanding to commit to disk. 
     * This field is designed to limit the amount of memory that the AsynchronousFileWriter can use.    
     */
    private long bytesPending; 
    
    /**
     * the maximum number of bytes that this AsynchronousFileWriter will allow to have pending for
     * asynchronous saving. Beyond this, the call to addFilesToWrite will block.
     */
    private static final long MAX_BYTES_FOR_ASYNCHRONOUS_SAVING = 3000000L; //3MB max
    
    /** If true, then calculate some statistics for BasicAsynchronousFileWriter and dump to the console. */
    private static final boolean DEBUG = false;
    
    /** the maximimum number of files held by the AsynchronousFileWriter. Only computed if DEBUG = true. */
    private int maxFilesPending;
    
    /** the maximum number of bytes actually held by this AsynchronousFileWriter. Only computed if DEBUG = true. */
    private long maxBytesPending;
    
    /** Whether the file writer thread has been started. */
    private boolean started = false;

    /** Whether the file writer thread has been stopped.
     * Set to false by a call to waitForFilesToBeWritten. */
    private volatile boolean jobStopped = false;
    
    /** The repository for program resources. */
    private final ProgramResourceRepository resourceRepository;
    
    /**
     * A class to actually write the files.
     * Note that using a workspace job allows resource change notification batching to happen
     *   for work which happens on a separate thread.
     *   
     * A WriterJob continuously writes files from the queue (filesToWrite) until it finds that the queue is empty,
     * at which time it goes into a wait state.
     * It is roused from its wait state by calling unWait(), at which time it either:
     * 1) resumes continuously writing files from the queue, if acceptFiles is true.  Or,
     * 2) exits, if acceptFiles is false.
     *   
     * @author Edward Lam
     */
    private class WriterJob extends WorkspaceJob {

        /**
         * Constructor for a WriterJob.
         */
        WriterJob() {
            super("Writing Files");       // TODOEL: localize.
        }
        
        /**
         * {@inheritDoc}
         */
        @Override
        public IStatus runInWorkspace(IProgressMonitor monitor) throws CoreException {
            // We don't know ahead of time how much work it will take.
            // TODOEL: localize.
            if (monitor != null) {
                monitor.beginTask("Writing files", IProgressMonitor.UNKNOWN);
            }
            
            while (acceptFiles) {
                writeFiles(monitor);
            }
            
            //one last time to make sure all files get written out.
            writeFiles(monitor);
            
            if (DEBUG) {
                System.out.println("AsynchronousFileWriter.maxFilesPending = " + maxFilesPending);
                System.out.println("AsynchronousFileWriter.maxBytesPending = " + maxBytesPending);
            }
            
            if (monitor != null) {
                monitor.done();
            }
            return Status.OK_STATUS;
        }
        
        /**
         * Write files until canceled.
         * @param monitor the monitor, or null for no monitor.
         */
        private void writeFiles(IProgressMonitor monitor) {
            
            AsynchronousFileWriter.FileData fileToWrite;
            while (!jobStopped && (fileToWrite = getFileToWrite(monitor)) != null) {
                if (monitor != null && monitor.isCanceled()) {
                    // We can safely bail out here.  Handled by the Job manager.
                    throw new OperationCanceledException();
                }
                
                writeFile(fileToWrite, monitor);
            }
        }
        
        /**
         * If at any time this job runs out of files to write, it will go into a waiting state.
         * Calling this method rouses it.
         */
        public synchronized void unWait() {
            // Use a synchronized instance method so that the calling thread becomes the owner of this job's lock.
            notify();
        }
    }
    
    /**
     * Helper interface for writeUntilDone().
     * The WriteController specifies whether the caller to the method should continue writing.
     * @author Edward Lam
     */
    private interface WriteController {
        /**
         * @return whether the caller should continue writing files.
         */
        public boolean shouldContinueWriting();
    }
    
    /**
     * Constructor for a EclipseAsynchronousFileWriter.
     * @param resourceRepository the program resource repository into which to write the files.
     */
    EclipseAsynchronousFileWriter(ProgramResourceRepository resourceRepository) {
        this.resourceRepository = resourceRepository;
        if (resourceRepository == null) {
            throw new NullPointerException("Argument 'resourceRepository' must not be null.");
        }
        
        acceptFiles = true;
        filesToWrite = new LinkedList<FileData>();        
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public void stopAcceptingFiles () {
        acceptFiles = false;           
        writerJob.unWait();
    }      
    
    /**
     * {@inheritDoc}
     */
    @Override
    public void addFileToWrite(AsynchronousFileWriter.FileData fileData, CompilerMessageLogger logger) {
       
        if (fileData == null) {
            throw new NullPointerException();
        }
               
        if (!acceptFiles) {
            throw new IllegalStateException();
        }
        
        synchronized (filesToWrite) {
            bytesPending += fileData.getSize();    
            filesToWrite.add(fileData);
            
            if (DEBUG) {
                if (bytesPending > maxBytesPending) {
                    maxBytesPending = bytesPending;
                }
                int nFiles = filesToWrite.size();
                if (nFiles > maxFilesPending) {
                    maxFilesPending = nFiles;
                }
            }
        }

        if (!started) {
            // TODOEL: We should limit the rule to those files which can actually be written (ie. files in the project).
            // writerJob.setRule(modifyRule());
            
            // Note: 
            //   Setting to a system job hides it.
            //   Setting to a user job makes it too prominent.
            
            // Start the job.
            writerJob.schedule();
            started = true;
        }

        // Note: this only works because the job's progress monitor doesn't know how much work it will be doing in the end.
        // If it did, then removing the file from the queue outside the thread would screw up any increments 
        // pre-calculated per written file from inside the writer thread.
        
        // Don't overload the file-writing cache.
        writeUntilDone(new WriteController() {
            public boolean shouldContinueWriting() {
                return bytesPending > MAX_BYTES_FOR_ASYNCHRONOUS_SAVING;
            }
        });

        writerJob.unWait();
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public void waitForFilesToBeWritten(CompilerMessageLogger logger) throws InterruptedException {
        // We don't want the builder to return before all the files have been written.
        // Cancel the job and perform the writing on this thread.
        // Note: if the builder could return before writing all files, we could just return here and ignore this thread.
        //  Any errors reported while writing files will be reported by Eclipse rather than as a compiler error.
        //  If the builder thread runs out of things to do, the UI will show that the writer thread is still writing.
        jobStopped = true;
        
        // Assume responsibility for writing from the job.
        writeUntilDone(new WriteController() {
            public boolean shouldContinueWriting() {
                synchronized (filesToWrite) {
                    return !filesToWrite.isEmpty();
                }
            }
        });
    }

    /**
     * While allowed by the write controller, take the first file from the write queue and write it to the program
     *  resource repository, and repeat.
     * @param writeController the write controller which controls whether writing should continue.
     */
    private void writeUntilDone(final WriteController writeController) {
        IWorkspace workspace = ResourcesPlugin.getWorkspace();
        
        // Wrap in a workspace runnable so that resource change events are batched.
        try {
            workspace.run(new IWorkspaceRunnable() {
                public void run(IProgressMonitor monitor) throws CoreException {
                    while (writeController.shouldContinueWriting()) {
                        synchronized (filesToWrite) {
                            AsynchronousFileWriter.FileData fileToWrite = filesToWrite.removeFirst();
                            bytesPending -= fileToWrite.getSize();
                            writeFile(fileToWrite, null);
                        }
                    }
                }
            }, workspace.getRoot(), IResource.NONE, null);
        
        } catch (CoreException e) {
            // The operation failed for some reason.
            e.printStackTrace();
        }
    }
    
    private AsynchronousFileWriter.FileData getFileToWrite(IProgressMonitor monitor) {
        // Block until a file is available and we're still accepting files.  When woken up the code will
        // continue only if a new object has been added to the list or acceptFiles has been set to false.
        boolean filesToWriteIsEmpty;
        synchronized (filesToWrite) {
            filesToWriteIsEmpty = filesToWrite.isEmpty();
        }
        while (filesToWriteIsEmpty && acceptFiles) {
            
            if (monitor != null && monitor.isCanceled()) {
                // We can safely bail out here.  Handled by the Job manager.
                throw new OperationCanceledException();
            }
            
            try {
                wait();
            } catch (InterruptedException e) {
                // This is unexpected, but won't cause any problems.
                // We'll just loop around and check the list and acceptFiles flag again.
            }
            synchronized (filesToWrite) {
                filesToWriteIsEmpty = filesToWrite.isEmpty();
            }
        }
        
        // Return the first file if one is available
        synchronized (filesToWrite) {
            if (!filesToWrite.isEmpty()) {
                AsynchronousFileWriter.FileData fileToWrite = filesToWrite.removeFirst();
                bytesPending -= fileToWrite.getSize();
                return fileToWrite;
            }
        }
        
        return null;
    }
    
    private void writeFile(AsynchronousFileWriter.FileData fileData, IProgressMonitor monitor) {
        
        ProgramResourceLocator.File fileLocator = fileData.getFileLocator();
        
        try {
            resourceRepository.setContents(fileLocator, fileData.getInputStream());
            
        } catch (FileNotFoundException e) {
            throw new IllegalArgumentException ("Unable to find file: " + fileLocator.toString());
        
        } catch (IOException e) {
            throw new IllegalStateException ("Error writing to file: " + fileLocator.toString());
        }

        if (monitor != null) {
            // Notify the monitor that we did something.
            monitor.worked(1);
        }
    }
    

}
