/*******************************************************************************
 * Copyright (c) 2006 Business Objects Software Limited and others.
 * All rights reserved. 
 * This file is made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Business Objects Software Limited - initial API and implementation based on Eclipse 3.1.2 code for
 *                             /org.eclipse.jdt.ui/ui/org/eclipse/jdt/internal/ui/util/CoreUtility.java, and
 *                             org.eclipse.jdt.internal.ui.javaeditor.EditorUtility
 *                           Eclipse source is available at: http://www.eclipse.org/downloads/
 *******************************************************************************/

/*
 * CoreUtility.java
 * Creation date: Feb 15, 2006.
 * By: Edward Lam
 */
package org.openquark.cal.eclipse.ui.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.nio.channels.FileChannel;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IProjectDescription;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IStorage;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.IWorkspaceDescription;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.IncrementalProjectBuilder;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.resources.IWorkspace.ProjectOrder;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.SubProgressMonitor;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jdt.core.IClasspathEntry;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.ISourceRange;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.formatter.DefaultCodeFormatterConstants;
import org.eclipse.jdt.internal.core.JarEntryFile;
import org.eclipse.jdt.internal.ui.javaeditor.JarEntryEditorInput;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.dialogs.ErrorDialog;
import org.eclipse.jface.dialogs.ProgressMonitorDialog;
import org.eclipse.jface.operation.IRunnableContext;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.custom.BusyIndicator;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IEditorDescriptor;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IEditorRegistry;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.actions.WorkspaceModifyOperation;
import org.eclipse.ui.dialogs.ListDialog;
import org.eclipse.ui.ide.IDE;
import org.eclipse.ui.part.FileEditorInput;
import org.eclipse.ui.texteditor.IEditorStatusLine;
import org.eclipse.ui.texteditor.ITextEditor;
import org.eclipse.ui.texteditor.ITextEditorActionDefinitionIds;
import org.eclipse.ui.texteditor.TextEditorAction;
import org.openquark.cal.compiler.CALDocComment;
import org.openquark.cal.compiler.ClassInstance;
import org.openquark.cal.compiler.ClassMethod;
import org.openquark.cal.compiler.CompilerMessage;
import org.openquark.cal.compiler.CompilerMessageLogger;
import org.openquark.cal.compiler.DataConstructor;
import org.openquark.cal.compiler.Function;
import org.openquark.cal.compiler.FunctionalAgent;
import org.openquark.cal.compiler.MessageLogger;
import org.openquark.cal.compiler.ModuleName;
import org.openquark.cal.compiler.ScopedEntity;
import org.openquark.cal.compiler.SearchResult;
import org.openquark.cal.compiler.SourceIdentifier;
import org.openquark.cal.compiler.SourceMetricsManager;
import org.openquark.cal.compiler.SourcePosition;
import org.openquark.cal.compiler.SourceRange;
import org.openquark.cal.compiler.TypeClass;
import org.openquark.cal.compiler.TypeConstructor;
import org.openquark.cal.compiler.SearchResult.Precise;
import org.openquark.cal.eclipse.core.CALEclipseCorePlugin;
import org.openquark.cal.eclipse.core.CALModelManager;
import org.openquark.cal.eclipse.core.CALModelMarker;
import org.openquark.cal.eclipse.core.builder.CALBuilder;
import org.openquark.cal.eclipse.core.util.Util;
import org.openquark.cal.eclipse.ui.CALEclipseUIPlugin;
import org.openquark.cal.eclipse.ui.CALUIMessages;
import org.openquark.cal.eclipse.ui.actions.ActionMessages;
import org.openquark.cal.eclipse.ui.caleditor.CALEditor;
import org.openquark.cal.eclipse.ui.caleditor.PartiallySynchronizedDocument;
import org.openquark.cal.eclipse.ui.search.SearchMessages;
import org.openquark.cal.module.Cal.Core.CAL_Prelude;
import org.osgi.framework.Bundle;


/**
 * Helper utility functions for this plugin.
 * @author Edward Lam
 */
public final class CoreUtility {
    
    /** The lecc_runtime folder path - used by addLeccFolderSrcExclude. */
    private static final IPath leccRuntimeFolderPath = new Path("lecc_runtime/");
    
    /**
     * Private constructor - do not instantiate.
     */
    private CoreUtility() {
    }
    
    /**
     * Creates an extension.  If the extension plugin has not
     * been loaded a busy cursor will be activated during the duration of
     * the load.
     *
     * @param element the config element defining the extension
     * @param classAttribute the name of the attribute carrying the class
     * @return the extension object
     */
    public static Object createExtension(final IConfigurationElement element, final String classAttribute) throws CoreException {
        // If plugin has been loaded create extension.
        // Otherwise, show busy cursor then create extension.
        String pluginId = element.getNamespace();
        Bundle bundle = Platform.getBundle(pluginId);
        if (bundle != null && bundle.getState() == Bundle.ACTIVE) {
            return element.createExecutableExtension(classAttribute);
        
        } else {
            final Object[] ret = new Object[1];
            final CoreException[] exc = new CoreException[1];
            BusyIndicator.showWhile(null, new Runnable() {
                public void run() {
                    try {
                        ret[0] = element.createExecutableExtension(classAttribute);
                    } catch (CoreException e) {
                        exc[0] = e;
                    }
                }
            });
            if (exc[0] != null) {
                throw exc[0];
            } else {
                return ret[0];
            }
        }
    }       
    
    
    /**
     * Starts a build in the background.
     * @param project The project to build or <code>null</code> to build the workspace.
     * @param forCalOnly if true, full build will only apply for the cal portion of the project.
     */
    public static void startBuildInBackground(final IProject project, boolean forCalOnly) {
        getBuildJob(project, forCalOnly).schedule();
    }
    
    /**
     * A job to build a project or workspace.
     * @author Edward Lam
     */
    private static final class BuildJob extends Job {

        private final IProject fProject;
        private final boolean clean;
        private final boolean forCalOnly;

        /**
         * @param name the name of the job
         * @param clean if true, this is a clean job.  if false, this is a build job.
         * @param project The project to build or null if all projects are to be built.
         * @param forCalOnly
         */
        private BuildJob(String name, IProject project, boolean clean, boolean forCalOnly) {
            super(name);
            fProject = project;
            this.clean = clean;
            this.forCalOnly = forCalOnly;
        }

        public boolean isCoveredBy(BuildJob other) {
            if (other.fProject == null) {
                return true;
            }
            return fProject != null && fProject.equals(other.fProject);
        }

        /*
         * (non-Javadoc)
         * 
         * @see org.eclipse.core.runtime.jobs.Job#run(org.eclipse.core.runtime.IProgressMonitor)
         */
        @Override
        protected IStatus run(IProgressMonitor monitor) {
            synchronized (getClass()) {
                if (monitor.isCanceled()) {
                    return Status.CANCEL_STATUS;
                }
                Job[] buildJobs = Platform.getJobManager().find(ResourcesPlugin.FAMILY_MANUAL_BUILD);
                for (final Job curr : buildJobs) {
                    if (curr != this && curr instanceof BuildJob) {
                        BuildJob job = (BuildJob)curr;
                        if (job.isCoveredBy(this)) {
                            curr.cancel(); // cancel all other build jobs of our kind
                        }
                    }
                }
            }
            try {
                if (fProject != null) {
                    // Build a project and its dependents.
                    String taskName = clean ? CALUIMessages.CoreUtility_cleanproject_taskname : CALUIMessages.CoreUtility_buildproject_taskname;
                    monitor.beginTask(Messages.format(taskName, fProject.getName()), 4);

                    if (clean) {
                        fProject.build(IncrementalProjectBuilder.CLEAN_BUILD, CALEclipseCorePlugin.BUILDER_ID, null, new SubProgressMonitor(monitor, 4));

                    } else {
                        // First build the specific project.
                        if (forCalOnly && fProject.hasNature(JavaCore.NATURE_ID)) {
                            // incremental build for java.
                            // full build for cal.
                            fProject.build(IncrementalProjectBuilder.INCREMENTAL_BUILD, JavaCore.BUILDER_ID, null, new SubProgressMonitor(monitor, 1));
                            fProject.build(IncrementalProjectBuilder.FULL_BUILD, CALEclipseCorePlugin.BUILDER_ID, null, new SubProgressMonitor(monitor, 1));

                        } else {
                            // Build everything.
                            fProject.build(IncrementalProjectBuilder.FULL_BUILD, new SubProgressMonitor(monitor, 2));
                        }

                        // Now invoke the incremental builder to build dependents.
                        CALEclipseUIPlugin.getWorkspace().build(IncrementalProjectBuilder.INCREMENTAL_BUILD, new SubProgressMonitor(monitor, 2));
                    }
                
                } else {
                    // Build all.
                    String taskName = clean ? CALUIMessages.CoreUtility_cleanall_taskname : CALUIMessages.CoreUtility_buildall_taskname;
                    if (forCalOnly) {

                        // TODOEL: this does not handle cycles (knots) in the project order.
                        ProjectOrder projectOrder = ResourcesPlugin.getWorkspace().computeProjectOrder(CALEclipseUIPlugin.getWorkspace().getRoot().getProjects());
                        IProject[] projects = projectOrder.projects;
                        monitor.beginTask(taskName, projects.length*2);
                        for (final IProject project : projects) {
                            if (clean) {
                                // clean build.
                                project.deleteMarkers(CALModelMarker.CAL_MODEL_PROBLEM_MARKER, true, IResource.DEPTH_INFINITE);
                                project.build(IncrementalProjectBuilder.CLEAN_BUILD, CALEclipseCorePlugin.BUILDER_ID, null, new SubProgressMonitor(monitor, 2));
                            } else {
                                // incremental build for java.
                                if (project.hasNature(JavaCore.NATURE_ID)) {
                                    // Don't need to delete the markers, but if a Java file is being edited it must be saved before this compilation is invoked
                                    //  if the user wants the Java build to remove existing Java build problem markers.
                                    project.build(IncrementalProjectBuilder.INCREMENTAL_BUILD, JavaCore.BUILDER_ID, null, new SubProgressMonitor(monitor, 1));
                                }

                                // full build for cal.
                                project.deleteMarkers(CALModelMarker.CAL_MODEL_PROBLEM_MARKER, true, IResource.DEPTH_INFINITE);
                                project.build(IncrementalProjectBuilder.FULL_BUILD, CALEclipseCorePlugin.BUILDER_ID, null, new SubProgressMonitor(monitor, 1));
                            }
                        }

                    } else {
                        monitor.beginTask(taskName, 2);
                        if (clean) {
                            CALEclipseUIPlugin.getWorkspace().build(IncrementalProjectBuilder.CLEAN_BUILD, new SubProgressMonitor(monitor, 2));
                        } else {
                            CALEclipseUIPlugin.getWorkspace().build(IncrementalProjectBuilder.FULL_BUILD, new SubProgressMonitor(monitor, 2));
                        }
                    }
                }
            } catch (CoreException e) {
                return e.getStatus();
            } catch (OperationCanceledException e) {
                return Status.CANCEL_STATUS;
            } finally {
                monitor.done();
            }
            initializationIsComplete = true;
            return Status.OK_STATUS;
        }

        @Override
        public boolean belongsTo(Object family) {
            return ResourcesPlugin.FAMILY_MANUAL_BUILD == family;
        }
    }
    
    /**
     * This is a temporary workaround. The Program object is not initialized correctly until
     * the compiler is run the first time. This cannot be done during startup because the 
     * startup must be fast. This flag is used by the search to determine if the compiler
     * will be run before the search is started. This needs to be done only once. This
     * is not thread safe but I don't think that multiple version of this can run at the same
     * time because of the way the Eclipse UI works. After this check-in. I will fix the problem 
     * the correct way and remove this hack.
     * 
     * TODO Remove this hack after the CALBuilder is initializing correctly.
     */
    private static boolean initializationIsComplete = false;    
 
    /**
     * @return true if the type information was loaded by the CALBuilder. The CALBuilder can be enabled
     * but not have the type information loaded because of a configuration bug. This will be fixed in the 
     * future
     * TODO GJM: remove this hack
     */
    public static boolean calBuilderWasInitialized(){
        return initializationIsComplete;
    }

    /**
     * Whether or not the initializeCALBuilderJob job was run.
     */
    private static boolean initializeJobWasStarted = false;
    private static Job initializeCALBuilderJob = CoreUtility.getBuildJob(null, true);
    
    /**
     * Initializes the CALBuilder. This is a temporary hack until the CALBuilder is initialized properly.
     * @param monitor
     * @return true iff the CALBuilder needed to be initialized.
     */
    public static boolean initializeCALBuilder(IProgressMonitor monitor, int totalTicks, int ticks){
        if (initializationIsComplete){
            return false;
        }
        else{
            // If the monitor is null then pop up a dialog.
            if (monitor == null){
                final ProgressMonitorDialog progressMonitorDialog = new ProgressMonitorDialog(null);
                try {
                    progressMonitorDialog.run(false, false, new IRunnableWithProgress() {                       
                        public void run(IProgressMonitor monitor) {
                            CoreUtility.initializeCALBuilder(progressMonitorDialog.getProgressMonitor(), 100, 100);
                            monitor.done();
                        }});
                    return true;
                } catch (InvocationTargetException e) {
                    CALEclipseUIPlugin.log(new Status(IStatus.ERROR, CALEclipseUIPlugin.PLUGIN_ID, IStatus.OK, "", e)); //$NON-NLS-1$
                    return false;
                } catch (InterruptedException e) {
                    CALEclipseUIPlugin.log(new Status(IStatus.ERROR, CALEclipseUIPlugin.PLUGIN_ID, IStatus.OK, "", e)); //$NON-NLS-1$
                    return false;
                }
            }
            else{
                // This is called from the UI thread so cannot be called more than once at the same
                // time so I am not syncing this.
                if (initializeJobWasStarted){
                    // job is or might be still running so just wait for it to complete.
                    try {
                        initializeCALBuilderJob.join();
                    } catch (InterruptedException e) {
                    }
                }
                else{
                    // The builder has not been initialize so just run it now.
                    monitor.beginTask(SearchMessages.SearchPage_initializingTypeInfo, totalTicks);
                    // Ensure that the CAL module information is loaded
                    initializeCALBuilderJob.setUser(true);
                    CoreUtility.runJob(initializeCALBuilderJob, new SubProgressMonitor(monitor, ticks));
                }
                return true;
            }
        }
    }

    /**
     * Initializes the CALBuilder. This will load the CAL information in the background
     */
    public static void initializeCALBuilderInBackground(){
        // This sync is to make sure that the build job is run only once. 
        synchronized(initializeCALBuilderJob){
            if (!initializeJobWasStarted){
                initializeCALBuilderJob.setUser(false);
                initializeCALBuilderJob.schedule();
                initializeJobWasStarted = true;
            }
        }
    }
    
    /**
     * Helper function for running the build job in the current thread. 
     * @param job The job to run.
     * @param monitor The progress monitor for the job.
     */    
    private static void runJob(Job job, IProgressMonitor monitor){
        BuildJob buildJob = (BuildJob) job;
        buildJob.run(monitor);
    }
    
    /**
     * Returns a build job
     * 
     * @param project The project to build or <code>null</code> to build the workspace.
     * @param forCalOnly if true, full build will only apply for the cal portion of the project.
     * If false, full build will apply for the whole project.
     */
    public static Job getBuildJob(final IProject project, boolean forCalOnly) {
        Job buildJob = new BuildJob(CALUIMessages.CoreUtility_job_title, project, false, forCalOnly);
        buildJob.setRule(ResourcesPlugin.getWorkspace().getRuleFactory().buildRule());
        buildJob.setUser(true);
        return buildJob;
    }
    
    /**
     * Returns a clean job
     * 
     * @param project The project to clean or <code>null</code> to clean the workspace.
     * @param forCalOnly if true, clean will only apply for the cal portion of the project.
     * If false, clean will apply for the whole project.
     */
    public static Job getCleanJob(final IProject project, boolean forCalOnly) {
        Job buildJob = new BuildJob(CALUIMessages.CoreUtility_clean_job_title, project, true, forCalOnly);
        buildJob.setRule(ResourcesPlugin.getWorkspace().getRuleFactory().buildRule());
        buildJob.setUser(true);
        return buildJob;
    }
    
    /**
     * Set the autobuild to the value of the parameter and
     * return the old one.
     * 
     * @param state the value to be set for autobuilding.
     * @return the old value of the autobuild state
     */
    public static boolean enableAutoBuild(boolean state) throws CoreException {
        IWorkspace workspace = ResourcesPlugin.getWorkspace();
        IWorkspaceDescription desc = workspace.getDescription();
        boolean isAutoBuilding = desc.isAutoBuilding();
        if (isAutoBuilding != state) {
            desc.setAutoBuilding(state);
            workspace.setDescription(desc);
        }
        return isAutoBuilding;
    }

    /**
     * Convert the given source position to an offset into the document.
     * @param sp The source position to convert
     * @param document The document to get the offset to the source position
     * @return The offset in the document that corresponds to the given source position. This is a zero based index.
     * @throws BadLocationException
     */
    public static int toOffset(SourcePosition sp, IDocument document) throws BadLocationException{        
        return CoreUtility.convertToCharacterPosition(sp.getLine(), sp.getColumn(), document);
    }

    /**
     * Convert the given source range to a length in the document.
     * @param sourceRange The source range to convert
     * @param document The document to get the offset to the source range
     * @return the length of the source range in the given document.
     * @throws BadLocationException
     */
    public static int toLength(SourceRange sourceRange, IDocument document) throws BadLocationException{
        final int start = toOffset(sourceRange.getStartSourcePosition(), document); 
        final int end = toOffset(sourceRange.getEndSourcePosition(), document);
        return end - start;
    }
    
    /**
     * Convert the given source range to an IRegion in the document. Source ranges have positions
     * in terms of editor columns. The IRegion has it in terms of offsets in the document. This is
     * an issue when the documents contain tabs.
     * @param sourceRangeInOrg The source range to convert
     * @param psd The document to get the offset to the source range
     * @return the IRegion of the source range in the given document.
     * @throws BadLocationException
     */
    public static IRegion toRegion(SourceRange sourceRangeInOrg, PartiallySynchronizedDocument psd) throws BadLocationException{
        // Org suffix means that the co-ordinates are in the original document that the source position is based on
        // Cur suffix means that the co-ordinates are in the current document's space 
        final int startInOrg = toOffset(sourceRangeInOrg.getStartSourcePosition(), psd.getOriginalDocument());
        final int startInCur = psd.fromOriginalOffset(startInOrg);
        int lineOffsetInOrg = psd.getOriginalDocument().getLineOffset(sourceRangeInOrg.getEndLine() - 1);

        final int tabSize = getTabSize();
        int columnInEditor = 1;
        // source range goes one column past the end. The last position may be on 
        // a non-existant space character on the end of the line
        final int columnToFind = sourceRangeInOrg.getEndColumn() - 1;
        int i = 0;
        final int currentLength = psd.getLength();
        while (columnInEditor < columnToFind){
            final int pos = i + lineOffsetInOrg;
            final char ch = (pos >= currentLength) ? ' ' : psd.getChar(pos);
            if (ch != '\t') {
                columnInEditor++;
            } else { 
                //tabs can consume from 1 to tabSize columns (a tab character moves the column to the next tab stop)
                final int jump = (((columnInEditor-1)/tabSize) + 1) * tabSize + 1 - columnInEditor;
                columnInEditor += jump;
            }
            ++i;
        }
        final int endInOrg = i + lineOffsetInOrg;        
        final int endInCur = psd.fromOriginalOffset(endInOrg);
        
        final int length = endInCur - startInCur + 1;
        return new IRegion(){

            public int getLength() {
                return length;
            }

            public int getOffset() {
                return startInCur;
            }            
        };
    }

    public static void showPosition(IEditorPart editorPart, IStorage sourceStorage, SourceRange range){
        showPosition(editorPart, sourceStorage, null, range, false);
    }

    public static void showPosition(IEditorPart editorPart, IStorage sourceStorage, SourceRange range, boolean startOnly){
        showPosition(editorPart, sourceStorage, null, range, startOnly);
    }
    
    public static void showPosition(IEditorPart editorPart, IStorage sourceStorage, PartiallySynchronizedDocument psd, SourceRange range, boolean startOnly){
        if (editorPart instanceof ITextEditor) {
            ITextEditor textEditor = (ITextEditor) editorPart;
            
            if (sourceStorage instanceof IFile) {
                IFile sourceFile = (IFile) sourceStorage;
                
                if (sourceFile == null || !sourceFile.exists()){
                    return;
                }
            }
            
            IDocument doc = textEditor.getDocumentProvider().getDocument(textEditor.getEditorInput());
            try {
                int start; 
                int end;
                if (psd != null){
                    start = toOffset(range.getStartSourcePosition(), psd.getOriginalDocument());
                    start = psd.fromOriginalOffset(start);
                    end = toOffset(range.getEndSourcePosition(), psd.getOriginalDocument());
                    end = psd.fromOriginalOffset(end);
                }
                else{
                    start = toOffset(range.getStartSourcePosition(), doc); 
                    end = toOffset(range.getEndSourcePosition(), doc);
                }
                final int length = startOnly ? 0 : end - start;
                
                textEditor.selectAndReveal(start, length);
            } catch (BadLocationException e) {
                // will only happen on concurrent modification
                CALEclipseUIPlugin.log(new Status(IStatus.ERROR, CALEclipseUIPlugin.PLUGIN_ID, IStatus.OK, "", e)); //$NON-NLS-1$
            }
        } 
    }

    /**
     * Opens an editor on the given file resource.
     * 
     * @param storage The storage to open an editor for.
     * @param activate if <code>true</code> the editor will be activated
     * @return an open editor or <code>null</code> if an external editor was opened
     * @throws PartInitException
     */
    @SuppressWarnings("restriction")
    public static IEditorPart openInEditor(IStorage storage, boolean activate) throws PartInitException {
        IEditorPart editorPart = null;
        if (storage != null) {
            IWorkbenchPage ap = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage();
            if (ap != null) {
                
                if (storage instanceof IFile) {
                    IFile file = (IFile) storage;
                    editorPart = IDE.openEditor(ap, file, activate);
                    initializeHighlightRange(editorPart);
                    
                } else if (storage instanceof JarEntryFile) {
                    JarEntryFile jarEntry = (JarEntryFile) storage;
                    editorPart = IDE.openEditor(ap, new JarEntryEditorInput(jarEntry), 
                            CALEclipseUIPlugin.EDITOR_ID);
                }
            }
        }
        return editorPart;
    }

    private static IEditorInput getEditorInput(IJavaElement element){
        while (element != null) {
            if (element instanceof ICompilationUnit) {
                ICompilationUnit unit = (ICompilationUnit) element;
                    IResource resource = unit.getResource();
                    if (resource instanceof IFile) {
                        return new FileEditorInput((IFile) resource);
                    }
            }

            element= element.getParent();
        }

        return null;
    }
    
    public static IEditorPart openJavaElementInEditor(IJavaElement inputElement, ISourceRange sourceRange) throws PartInitException{
        final boolean activate = true;

        IEditorInput input= getEditorInput(inputElement);
        if (input != null){
            final IEditorPart editorPart = openInEditor(input, getEditorID(input, inputElement), activate);
            if (editorPart instanceof ITextEditor) {
                ITextEditor textEditor = (ITextEditor) editorPart;
                textEditor.selectAndReveal(sourceRange.getOffset(), sourceRange.getLength());
            }
            return editorPart;
        }

        return null;
    }

    private static IEditorPart openInEditor(IEditorInput input, String editorID, boolean activate) throws PartInitException {
        if (input != null) {
            IWorkbenchPage ap = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage();
            if (ap != null) {
                IEditorPart editorPart= ap.openEditor(input, editorID, activate);
                initializeHighlightRange(editorPart);
                return editorPart;
            }
        }
        return null;
    }
    
    public static String getEditorID(IEditorInput input, Object inputObject) {
        IEditorRegistry registry= PlatformUI.getWorkbench().getEditorRegistry();
        IEditorDescriptor descriptor= registry.getDefaultEditor(input.getName());
        if (descriptor != null) {
            return descriptor.getId();
        }
        return null;
    }
    
    public static void initializeHighlightRange(IEditorPart editorPart) {
        if (editorPart instanceof ITextEditor) {
            IAction toggleAction = editorPart
                    .getEditorSite()
                    .getActionBars()
                    .getGlobalActionHandler(
                            ITextEditorActionDefinitionIds.TOGGLE_SHOW_SELECTED_ELEMENT_ONLY);
            boolean enable = toggleAction != null;
            enable = enable && toggleAction.isEnabled() && toggleAction.isChecked();
            if (enable) {
                if (toggleAction instanceof TextEditorAction) {
                    // Reset the action
                    ((TextEditorAction) toggleAction).setEditor(null);
                    // Restore the action
                    ((TextEditorAction) toggleAction)
                            .setEditor((ITextEditor) editorPart);
                } else {
                    // Un-check
                    toggleAction.run();
                    // Check
                    toggleAction.run();
                }
            }
        }
    }

    /**
     * Shows the given error on the status line that appears to the bottom left side of the editor.
     * @param textEditor the text editor that the status message is shown under.
     * @param message the error message to show.
     */
    public static void showErrorOnStatusLine(ITextEditor textEditor, String message){
        showErrorOnStatusLine(textEditor, message, true);
    }
    
    /**
     * Shows the given error on the status line that appears to the bottom left side of the editor.
     * @param textEditor the text editor that the status message is shown under.
     * @param message the error message to show.
     */
    public static void showErrorOnStatusLine(ITextEditor textEditor, String message, boolean performBeep){
        IEditorStatusLine statusLine = (IEditorStatusLine) textEditor.getAdapter(IEditorStatusLine.class);
        if (statusLine != null){
            statusLine.setMessage(true, message, null);
        }
        if (performBeep){
            textEditor.getSite().getShell().getDisplay().beep();
        }
    }

    /**
     * Convert to given line and column position into an offset in the document.
     * @param line the line number of selected position. Starts at one.
     * @param columnToFind the column number of the selected position. Starts at one.
     * @param document the document to find the selected position in.
     * @return the offset in characters into the current file that corresponds to the given line and column position. This is a zero based index.
     * @throws BadLocationException
     */
    public static int convertToCharacterPosition(int line, int columnToFind, IDocument document) throws BadLocationException {
        int lineOffsetInDocument = document.getLineOffset(line - 1);

        final int tabSize = getTabSize();
        // Start the columns from zero instead of one so the jump math works properly
        int currentColumn = 1;
        int i = 0;
        while (currentColumn < columnToFind){
            char ch = document.getChar(i + lineOffsetInDocument);
            if (ch != '\t') {
                currentColumn++;
            } else { 
                //tabs can consume from 1 to tabSize columns (a tab character moves the column to the next tab stop)
                int jump = (((currentColumn-1)/tabSize) + 1) * tabSize + 1 - currentColumn;
                currentColumn += jump;
            }
            ++i;
        }
        return i + lineOffsetInDocument;
    }
    
    /**
     * @param line
     * @param offsetInDocument
     * @param document
     * @return the column corresponding to the given offset. The column starts at zero.
     * @throws BadLocationException
     */
    public static int getColumn(int line, int offsetInDocument, IDocument document) throws BadLocationException{
        int lineOffsetInDocument = document.getLineOffset(line);
        int characterInDocument = offsetInDocument - lineOffsetInDocument;

        int tabSize = getTabSize();
        int columnInEditor = 0;
        for (int i = 0; i < characterInDocument; i ++) {
            char ch = document.getChar(i + lineOffsetInDocument);
            if (ch != '\t') {
                columnInEditor++;
            } else { 
                //tabs can consume from 1 to tabSize columns (a tab character moves the column to the next tab stop)
                int jump = (((columnInEditor)/tabSize) + 1) * tabSize - columnInEditor;
                columnInEditor += jump;
            }
        }
        return columnInEditor;
    }

    private static int getTabSize() {
        return getCoreFormatterOption(DefaultCodeFormatterConstants.FORMATTER_TAB_SIZE, 4);
    }
    
    private static int getCoreFormatterOption(String key, int def) {
        try {
            return Integer.parseInt(getCoreFormatterOption(key));
        } catch (NumberFormatException e) {
            return def;
        }
    }
    
    private static String getCoreFormatterOption(String key) {
          return CALEclipseCorePlugin.getOption(key);
    }
    
    /**
     * This class is passed into an error dialog to show one error message.
     * 
     * @author Greg McClement
     */
    private static class DialogStatus implements IStatus{

        private final int severity;
        private final String message;
        
        public DialogStatus(int severity, String message){
            this.severity = severity;
            this.message = message;
        }
        
        public String getMessage(){
            return message;
        }
        
        public IStatus[] getChildren() { 
            return new IStatus[0];
        }

        public int getCode() {
            return severity;
        }

        public Throwable getException() {
            return null;
        }

        public String getPlugin() {
            return null;
        }

        public int getSeverity() {
            return severity;
        }

        public boolean isMultiStatus() {
            return false;
        }

        public boolean isOK() {
            return false;
        }

        public boolean matches(int severityMask) {
            return (severityMask & severity) != 0;
        }                        
    }

    /**
     * This class is passed into an error dialog to show only the Severity.ERROR messages of
     * a compiler message logger.
     *  
     * @author Greg McClement
     */
    private static class CompilerMessagesStatus implements IStatus{

        private final String message;
        private final IStatus[] children;
        
        public CompilerMessagesStatus(final String message, final CompilerMessageLogger messageLogger){
            this.message = message;
            
            // Build a list of error messages to remove duplicates
            final HashSet<String> errorMessages = new HashSet<String>();
            {
                List<CompilerMessage> messages = messageLogger.getCompilerMessages(CompilerMessage.Severity.ERROR);
                for (final CompilerMessage compilerMessage : messages) {
                    errorMessages.add(compilerMessage.toString());
                }
            }
            
            // build a status object for each error message
            {
                children = new IStatus[errorMessages.size()];
                final Iterator<String> iter = errorMessages.iterator();
                for (int i = 0; i < children.length; ++i) {
                    final String errorMessage = iter.next();
                    children[i] = new DialogStatus(ERROR, errorMessage);
                }
            }
        }
        
        public IStatus[] getChildren() { 
            return children;
        }

        public int getCode() {
            return ERROR;
        }

        public Throwable getException() {
            return null;
        }

        public String getPlugin() {
            return null;
        }

        public int getSeverity() {
            return ERROR;
        }

        public boolean isMultiStatus() {
            return true;
        }

        public boolean isOK() {
            return false;
        }

        public boolean matches(int severityMask) {
            return (severityMask & ERROR) != 0;
        }

        public String getMessage() {
            return message;
        }                        
    }
    
    public static void showMessage(final String title, final String message, final int severity){
        showMessage(title, new DialogStatus(severity, message));
    }
    
    public static void showMessage(final String title, Exception exception){
        showMessage(title, exception.toString(), IStatus.ERROR);
    }
    
    public static void showMessage(final String title, IStatus status){
        ErrorDialog.openError( 
                null, 
                title,
                null,
                status
                );
    }
    
    /**
     * This function will return true if the CAL builder is enabled. If the builder is 
     * not enabled then an error message will be shown in the status line.
     * @param textEditor The text editor to use to update the status line on
     * @return True if the builder was enabled.
     */
    
    public static boolean builderEnabledCheck(CALEditor textEditor){
        if (!CALBuilder.isEnabled()){
            showErrorOnStatusLine(textEditor, ActionMessages.error_calBuilderNotEnabled_message);
            return false; // Cannot run action since the build is not enabled.
        }
        return true;
    }

    /**
     * @param title Title for the error dialog if the build is not enabled.
     * @return True if the builder was enabled.
     */
    
    public static boolean builderEnabledCheck(final String title){
        /**
         * This feature only works if the CALBuilder is enabled. I was going to disable
         * it but I think having it selectable and showing a message that says how to make
         * it work is more friendly than just being disabled and having the user guess how
         * to turn it on.
         */
        if (!CALBuilder.isEnabled()){
            if (title != null){
                showMessage(title, ActionMessages.error_calBuilderNotEnabled_message, IStatus.ERROR);
            }
            return false; // Cannot run action since the build is not enabled.
        }
        return true;
    }
    
    /**
     * Show the given error messages to the user in a dialog.
     * @param title The title to use for the error dialog.
     * @param message A message describing the general operation that failed.
     * @param messageLogger The message logger containing the errors to show.
     * @return True if there was any errors.
     */
    public static boolean showErrors(String title, final String message, final CompilerMessageLogger messageLogger){
        if (messageLogger.getNErrors() == 0){
            return false;
        }
  
        showMessage(title, new CompilerMessagesStatus(message, messageLogger));
        return true;
    }
    
    /**
     * This class will show a dialog to allow the user to save all the dirty editors in the workspace.
     * If open returns Dialog.OK then all the dirty editors have been saved.
     *  
     * @author Greg McClement
     */
    public static class SaveAllDirtyEditors extends ListDialog{
        private final IEditorPart[] dirtyEditors = getDirtyEditors();

        public SaveAllDirtyEditors(Shell parent) {
            super(parent);
            setTitle(CALUIMessages.CoreUtilities_SaveAllModifiedResource_Title);
            setMessage(CALUIMessages.CoreUtilities_SaveAllModifiedResource_Message);
            
            setContentProvider(
                    new IStructuredContentProvider() {

                        public Object[] getElements(Object inputElement) {
                            return dirtyEditors;
                        }

                        public void dispose() {                            
                        }

                        public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
                        }
                    }
            );
            
            setLabelProvider(
                    new LabelProvider(){
                        @Override
                        public String getText(Object element){
                            IEditorPart editorPart = (IEditorPart) element;
                            return editorPart.getTitle();
                        }
                    }
            );
            
            setInput(Arrays.asList(dirtyEditors));
        }

        @Override
        public int open(){
            if (dirtyEditors.length == 0){
                // all the dirty editors have been saved.
                return Window.OK;
            }
            else{
                if (super.open() == Window.OK){
                    final ProgressMonitorDialog progressMonitorDialog = new ProgressMonitorDialog(null);
                    try {
                        progressMonitorDialog.run(false, false, new IRunnableWithProgress() {                       
                            public void run(IProgressMonitor monitor) {
                                monitor.beginTask(CALUIMessages.CoreUtilities_SaveAllModifiedResource_SavingFilesProgress, dirtyEditors.length);                                
                                for(int i = 0; i < dirtyEditors.length; ++i){
                                    dirtyEditors[i].doSave(monitor);
                                    monitor.worked(1);
                                }
                                monitor.done();
                            }});
                    } catch (InvocationTargetException e) {
                        CALEclipseUIPlugin.log(new Status(IStatus.ERROR, CALEclipseUIPlugin.PLUGIN_ID, IStatus.OK, "", e)); //$NON-NLS-1$
                        return Window.CANCEL;
                    } catch (InterruptedException e) {
                        CALEclipseUIPlugin.log(new Status(IStatus.ERROR, CALEclipseUIPlugin.PLUGIN_ID, IStatus.OK, "", e)); //$NON-NLS-1$
                        return Window.CANCEL;
                    }
                }
                return getReturnCode();
            }
        }        
    }
    /**
     * @return A list of all the editors that are dirty
     */
    private static IEditorPart[] getDirtyEditors(){
        IWorkbench workbench = PlatformUI.getWorkbench();
        IWorkbenchWindow[] workbenchWindows = workbench.getWorkbenchWindows();
        List<IEditorPart> dirtyEditors = new ArrayList<IEditorPart>();
        for(int i = 0; i < workbenchWindows.length; ++i){
            IWorkbenchWindow workbenchWindow = workbenchWindows[i];
            IWorkbenchPage[] pages = workbenchWindow.getPages();
            for(int j = 0; j < pages.length; ++j){
                IWorkbenchPage page = pages[j];
                IEditorPart[] editors = page.getDirtyEditors();
                for(int k = 0; k < editors.length; ++k){
                    dirtyEditors.add(editors[k]);
                }
            }
        }
        return dirtyEditors.toArray(new IEditorPart[dirtyEditors.size()]);
    }
    
    /**
     * Ensures that the CAL nature is on for the given project.
     * 
     * @param project
     *            to have sample nature added or removed
     */
    public static void turnOnNature(IProject project) {
        try {
            IProjectDescription description = project.getDescription();
            String[] natures = description.getNatureIds();
            
            for (int i = 0; i < natures.length; ++i) {
                if (CALEclipseCorePlugin.NATURE_ID.equals(natures[i])) {
                    // already on so leave it on
                    return; 
                }
            }
            
            CoreUtility.addCALNature(project, description, natures);
        } catch (CoreException e) {
            // Couldn't toggle the nature.
            // TODOEL: Handle this.
            CALEclipseUIPlugin.logException(e);
        }
    }
    
    /**
     * Ensure that a source exclude pattern for the lecc_runtime/ folder exists for the corresponding location for the given cal file.
     * so that the package explorer isn't littered.
     * @param calFile the cal file for which the corresponding lecc_runtime output folder should be added to the source excludes.
     */
    public static void ensureLeccFolderSrcExclude(IFile calFile) {
        IProject project = calFile.getProject();
        IJavaProject javaProject = JavaCore.create(project);
        
        IClasspathEntry[] rawClasspath;
        try {
            rawClasspath = javaProject.getRawClasspath();
        } catch (JavaModelException e) {
            Util.log(e, "Exception getting raw classpath for project: " + project);
            return;
        }
        
        for (int i = 0; i < rawClasspath.length; i++) {
            IClasspathEntry entry = rawClasspath[i];

            switch (entry.getEntryKind()) {
                case IClasspathEntry.CPE_SOURCE: 
                {
                    if (entry.getPath().isPrefixOf(calFile.getFullPath())) {
                        // match

                        IPath[] exclusionPatterns = entry.getExclusionPatterns();

                        // See if the exclusion pattern already exists.
                        for (IPath exclusionPath : exclusionPatterns) {

                            if (leccRuntimeFolderPath.equals(exclusionPath)) {
                                // already has the exclusion pattern.
                                return;
                            }
                        }

                        // Does not have the exclusion pattern so add it.
                        IPath[] newExclusionPatterns = new IPath[exclusionPatterns.length + 1];
                        System.arraycopy(exclusionPatterns, 0, newExclusionPatterns, 0, exclusionPatterns.length);
                        newExclusionPatterns[exclusionPatterns.length] = leccRuntimeFolderPath;

                        // Create a new raw classpath with the entry replaced.
                        IClasspathEntry[] newRawClasspath = rawClasspath.clone();
                        newRawClasspath[i] =
                            JavaCore.newSourceEntry(
                                    entry.getPath(), entry.getInclusionPatterns(), newExclusionPatterns, 
                                    entry.getOutputLocation(), entry.getExtraAttributes());

                        try {
                            javaProject.setRawClasspath(newRawClasspath, null);
                        } catch (JavaModelException e) {
                            Util.log(e, "Exception setting raw classpath for project: " + project);
                            return;
                        }

                        return;
                    }
                    break;
                }
                default:
                    // not a source entry.
                    break;
            }
        }
        // Couldn't find the source folder for the file.
    }

    /**
     * Adds the CAL nature to the given project.
     */
    public static void addCALNature(IProject project, IProjectDescription description, String[] natures) throws CoreException {
        // Add the nature
        String[] newNatures = new String[natures.length + 1];
        System.arraycopy(natures, 0, newNatures, 0, natures.length);
        newNatures[natures.length] = CALEclipseCorePlugin.NATURE_ID;
        description.setNatureIds(newNatures);
        project.setDescription(description, null);
    }
    
    /**
     * Toggles sample nature on a project
     * 
     * @param project
     *            to have sample nature added or removed
     */
    public static void toggleNature(IProject project) {
        try {
            IProjectDescription description = project.getDescription();
            String[] natures = description.getNatureIds();
            
            for (int i = 0; i < natures.length; ++i) {
                
                if (CALEclipseCorePlugin.NATURE_ID.equals(natures[i])) {
                    
                    // Remove the nature
                    String[] newNatures = new String[natures.length - 1];
                    System.arraycopy(natures, 0, newNatures, 0, i);
                    System.arraycopy(natures, i + 1, newNatures, i, natures.length - i - 1);
                    description.setNatureIds(newNatures);
                    project.setDescription(description, null);
                    return;
                }
            }
            
            CoreUtility.addCALNature(project, description, natures);
        } catch (CoreException e) {
            // Couldn't toggle the nature.
            // TODOEL: Handle this.
            CALEclipseUIPlugin.logException(e);
        }
    }

    public static boolean hasCALNature(IProject project){
        try {
            return project.hasNature(CALEclipseCorePlugin.NATURE_ID);
        } catch (CoreException e) {
            // Couldn't toggle the nature.
            // TODOEL: Handle this.
            CALEclipseUIPlugin.logException(e);
            return false;
        }
    }

    /**
     * Create a new Eclipse project.
     * @param project The project to create
     * @param description A description for the new project
     * @param monitor A monitor for display progress
     * @throws CoreException
     */
    private static void createProject(IProject project, IProjectDescription description, IProgressMonitor monitor) throws CoreException{
        try{
            monitor.beginTask("", 2000);
            project.create(description, new SubProgressMonitor(monitor, 1000));
            project.open(new SubProgressMonitor(monitor, 1000));
        }
        finally{
            monitor.done();
        }
    }
   
    public static SourceIdentifier.Category toCategory(Object object){
        SourceIdentifier.Category category;
        if (object instanceof Function || object instanceof ClassMethod){
            category = SourceIdentifier.Category.TOP_LEVEL_FUNCTION_OR_CLASS_METHOD;
        }
        else if (object instanceof TypeClass){
            category = SourceIdentifier.Category.TYPE_CLASS;
        }
        else if (object instanceof TypeConstructor){
            category = SourceIdentifier.Category.TYPE_CONSTRUCTOR;
        }
        else if (object instanceof DataConstructor){
            category = SourceIdentifier.Category.DATA_CONSTRUCTOR;
        }
        else{
            category = null;
            assert false;
        }

        return category;
    }
    
    /**
     * 
     * TODO ADE this must change so that the reference will be in terms of classpath 
     * variables, not the quark binary project
     * 
     * Adds a reference to the Quark binaries project to the given project if needed.
     * @param projectNeedingReference project to add the reference to
     * @return true if the project either has a reference or a reference was added. False if there is no quark binaries project in the workspace.
     */
    public static boolean addReferenceToQuarkBinariesIfNeeded(final IProject projectNeedingReference){
        final CALModelManager cmm = CALModelManager.getCALModelManager();
        
        // TODO ADE need some other way of finding the libraries
        // this will not work for the classpath variable
        final IStorage thisStorage = cmm.getInputSourceFile(CAL_Prelude.MODULE_NAME);
        if (! (thisStorage instanceof IFile)) {
            // No quark binaries project
            return false;
        }
        
        IFile thisFile = (IFile) thisStorage;
        
        
        final IProject thisProject = thisFile.getProject();
        if (CALModelManager.dependsOnStatically(projectNeedingReference, thisProject)){
            // already has a reference.
            return true;
        }
        // Not there so must add it
        
        try {
            final IProjectDescription projectDescription = projectNeedingReference.getDescription();
            IProject[] referencedProjects = projectDescription.getReferencedProjects();
            IProject[] newReferencedProjects = new IProject[referencedProjects.length + 1];
            newReferencedProjects[0] = thisProject;
            System.arraycopy(referencedProjects, 0, newReferencedProjects, 1, referencedProjects.length);
            projectDescription.setReferencedProjects(newReferencedProjects);
            
            final ProgressMonitorDialog progressMonitorDialog = new ProgressMonitorDialog(null);
            try {
                progressMonitorDialog.run(false, false, new IRunnableWithProgress() {                       
                    public void run(IProgressMonitor monitor) {
                        try{
                            projectNeedingReference.setDescription(projectDescription, progressMonitorDialog.getProgressMonitor());
                            monitor.done();
                        } catch (CoreException e) {
                            CALEclipseUIPlugin.log(new Status(IStatus.ERROR, CALEclipseUIPlugin.PLUGIN_ID, IStatus.OK, "", e)); //$NON-NLS-1$
                        }
                    }});
                return true;
            } catch (InvocationTargetException e) {
                CALEclipseUIPlugin.log(new Status(IStatus.ERROR, CALEclipseUIPlugin.PLUGIN_ID, IStatus.OK, "", e)); //$NON-NLS-1$
                return false;
            } catch (InterruptedException e) {
                CALEclipseUIPlugin.log(new Status(IStatus.ERROR, CALEclipseUIPlugin.PLUGIN_ID, IStatus.OK, "", e)); //$NON-NLS-1$
                return false;
            }
        } catch (CoreException e) {
            CALEclipseUIPlugin.log(new Status(IStatus.ERROR, CALEclipseUIPlugin.PLUGIN_ID, IStatus.OK, "", e)); //$NON-NLS-1$
            return false;
        }
    }

    /**
     * Create a quark binary project in the current workspace.
     * @param errorMessageTitle Title for any error message dialogs
     * @param projectName The name of the project to make
     * @param locationOfQuarkBinaries The location of the quark binaries
     * @throws InvocationTargetException
     * @throws InterruptedException
     */
    public static void createQuarkBinaryProject(final String errorMessageTitle, String projectName, String locationOfQuarkBinaries, Shell shell, IRunnableContext runnableContext) throws InvocationTargetException, InterruptedException{
        final IWorkspace workspace = ResourcesPlugin.getWorkspace();
        final IWorkspaceRoot workspaceRoot = workspace.getRoot();

        final IProject project = workspaceRoot.getProject(projectName);
        final IProjectDescription description = workspace.newProjectDescription(project.getName());
        final IPath location = new Path(locationOfQuarkBinaries);
        description.setLocation(location);

        WorkspaceModifyOperation op = new WorkspaceModifyOperation() {
            @Override
            protected void execute(IProgressMonitor monitor) throws CoreException {
                {
                    // if the .project and .classpath files don't exist then make them.

                    // .project file
                    {
                        IPath fromPath = location.append("eclipse-support").append(".project");
                        File fromFile = fromPath.toFile();

                        IPath toPath = location.append(".project");
                        File toFile = toPath.toFile();
                        try {
                            if (!toFile.exists()){
                                copyFile(fromFile, toFile);
                            }
                        } catch (IOException e) {
                            showMessage(errorMessageTitle, e);
                        }
                    }
                    
                    // .classpath file
                    {
                        IPath fromPath = location.append("eclipse-support").append(".classpath");
                        File fromFile = fromPath.toFile();
                        
                        IPath toPath = location.append(".classpath");
                        File toFile = toPath.toFile();
                        try {
                            if (!toFile.exists()){
                                copyFile(fromFile, toFile);
                            }
                        } catch (IOException e) {
                            showMessage(errorMessageTitle, e);
                        }
                    }
                }
                
                createProject(project, description, monitor);
            }
        };

        runnableContext.run(true, true, op);
    }

    private static void copyFile(File from, File to) throws IOException {

        FileInputStream inputStream = new FileInputStream(from);
        FileChannel inputChannel = inputStream.getChannel();

        FileOutputStream outputStream = new FileOutputStream(to);
        FileChannel outputChannel = outputStream.getChannel();

        // Copy file contents from source to destination
        outputChannel.transferFrom(inputChannel, 0, inputChannel.size());

        // Close the channels
        inputChannel.close();
        outputChannel.force(true);
        outputChannel.close();
    }

    public static void showCALElementInEditor(ScopedEntity calElement){
        CALModelManager calModelManager = CALModelManager.getCALModelManager();
        SourceMetricsManager smm = calModelManager.getSourceMetrics();
        CompilerMessageLogger messageLogger = new MessageLogger();
        List<SearchResult> results = smm.findDefinition(calElement.getName(), messageLogger);
        if (results != null && results.size() > 0){
            // if there is more than one result pick the first one
            // TODO fix this when the search backend is updated.
            Precise result = (Precise) results.iterator().next();
            IStorage definitionFile = calModelManager.getInputSourceFile(result.getName().getModuleName());
            IEditorPart editorPart;
            try {
                editorPart = CoreUtility.openInEditor(definitionFile, true);
            } catch (PartInitException e) {
                CALEclipseUIPlugin.log(new Status(IStatus.ERROR, CALEclipseUIPlugin.PLUGIN_ID, IStatus.OK, "", e)); //$NON-NLS-1$
                return;                
            }
            CoreUtility.showPosition(editorPart, definitionFile, result.getSourceRange());
        }
        else{
            return;  
        }                
    }        

    public static void showCALElementInEditor(ClassInstance classInstance){
        CALModelManager calModelManager = CALModelManager.getCALModelManager();
        SourceMetricsManager smm = calModelManager.getSourceMetrics();
        CompilerMessageLogger messageLogger = new MessageLogger();
        SourceRange position = smm.getPosition(classInstance, messageLogger);
        ModuleName moduleName = classInstance.getModuleName();
        if (position != null){
            // Open the editor to the correct position
            IStorage definitionFile = calModelManager.getInputSourceFile(moduleName);
            IEditorPart editorPart;
            try {
                editorPart = CoreUtility.openInEditor(definitionFile, true);
                CoreUtility.showPosition(editorPart, definitionFile, position);
            } catch (PartInitException e) {
                CALEclipseUIPlugin.log(new Status(IStatus.ERROR, CALEclipseUIPlugin.PLUGIN_ID, IStatus.OK, "", e)); //$NON-NLS-1$
            }
        }
        else{
            assert false;
        }        
    }
 
    /**
     * Get argument names for the given type constructor.
     */
    public static String[] getArgumentNamesFromCALDocComment(TypeConstructor typeConstructor) {
        CALDocComment caldoc = typeConstructor.getCALDocComment();

        int nArgsInCALDoc = (caldoc == null) ? -1 : caldoc.getNArgBlocks();
        int arity = typeConstructor.getTypeArity();
        
        int nArgs = Math.max(nArgsInCALDoc, arity);
        
        if (nArgs > 0) {
            String argNames[] = new String[nArgs];
            Set<String> setOfArgumentNames = new HashSet<String>();
            
            for (int i = 0; i < nArgs; i++) {
                argNames[i] = getNthArgumentName(caldoc, null, i, setOfArgumentNames);
            }
            
            return argNames;
        }
        
        return new String[]{};
    }

    private static String getNthArgumentName(CALDocComment caldoc, FunctionalAgent envEntity, int index, Set<String> setOfArgumentNames) {
        String baseArtificialName = "typeArg_" + (index + 1);
        String artificialName = baseArtificialName;

        // if the base artificial name already appears in previous arguments, then
        // make the argument name arg_x_y, where y is a supplementary disambiguating number
        // chosen so that the resulting name will not collide with any of the previous argument names

        int supplementaryDisambiguator = 1;
        while (setOfArgumentNames.contains(artificialName)) {
            artificialName = baseArtificialName + "_" + supplementaryDisambiguator;
            supplementaryDisambiguator++;
        }

        String result = artificialName;
        setOfArgumentNames.add(artificialName);
        return result;
    }

}
