/*******************************************************************************
 * Copyright (c) 2005 Business Objects Software Limited and others.
 * All rights reserved. 
 * This file is made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Business Objects Software Limited - initial API and implementation based on Eclipse 3.1.2 code for
 *                             /org.eclipse.jdt.core/model/org/eclipse/jdt/internal/core/builder/AbstractImageBuilder.java
 *                             /org.eclipse.jdt.core/model/org/eclipse/jdt/internal/core/builder/BatchImageBuilder.java
 *                             /org.eclipse.jdt.core/model/org/eclipse/jdt/internal/core/builder/IncrementalImageBuilder.java
 *                           Eclipse source is available at: http://www.eclipse.org/downloads/
 *******************************************************************************/

/*
 * CALBuilder.java
 * Creation date: Nov 2, 2005.
 * By: Edward Lam
 */
package org.openquark.cal.eclipse.core.builder;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceDelta;
import org.eclipse.core.resources.IResourceDeltaVisitor;
import org.eclipse.core.resources.IResourceVisitor;
import org.eclipse.core.resources.IStorage;
import org.eclipse.core.resources.IWorkspaceRunnable;
import org.eclipse.core.resources.IncrementalProjectBuilder;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.jdt.core.IPackageFragmentRoot;
import org.openquark.cal.compiler.CompilerMessage;
import org.openquark.cal.compiler.CompilerMessageLogger;
import org.openquark.cal.compiler.ForeignContextProvider;
import org.openquark.cal.compiler.MessageKind;
import org.openquark.cal.compiler.MessageLogger;
import org.openquark.cal.compiler.ModuleContainer;
import org.openquark.cal.compiler.ModuleName;
import org.openquark.cal.compiler.ModuleSourceDefinitionGroup;
import org.openquark.cal.compiler.ModuleTypeInfo;
import org.openquark.cal.compiler.QualifiedName;
import org.openquark.cal.compiler.Refactorer;
import org.openquark.cal.compiler.Scope;
import org.openquark.cal.compiler.ScopedEntity;
import org.openquark.cal.compiler.SourceIdentifier;
import org.openquark.cal.compiler.SourcePosition;
import org.openquark.cal.compiler.SourceRange;
import org.openquark.cal.compiler.ModuleContainer.ISourceManager;
import org.openquark.cal.compiler.ModuleContainer.ISourceManager2;
import org.openquark.cal.compiler.ScopedEntityNamingPolicy.UnqualifiedIfUsingOrSameModule;
import org.openquark.cal.compiler.SourceIdentifier.Category;
import org.openquark.cal.eclipse.core.CALEclipseCorePlugin;
import org.openquark.cal.eclipse.core.CALModelManager;
import org.openquark.cal.eclipse.core.CALModelMarker;
import org.openquark.cal.eclipse.core.CoreOptionIDs;
import org.openquark.cal.eclipse.core.EclipseModuleSourceDefinitionGroup;
import org.openquark.cal.eclipse.core.CALModelManager.SourceManagerFactory;
import org.openquark.cal.eclipse.core.util.Messages;
import org.openquark.cal.eclipse.core.util.Util;
import org.openquark.cal.machine.ProgramResourceLocator;
import org.openquark.cal.machine.StatusListener;
import org.openquark.cal.services.FileSystemResourceHelper;
import org.openquark.cal.services.ProgramModelManager;
import org.openquark.cal.services.ResourcePath;
import org.openquark.util.Pair;
import org.openquark.util.UnsafeCast;


/**
 * The incremental project builder for CAL.
 * It is important to note that the incremental project builder is responsible for building only a single project.
 * 
 * Static members are used to track state across a complete build iteration, which may involve invocation of
 * multiple incremental project builders.
 * 
 * @author Edward Lam
 */
public class CALBuilder extends IncrementalProjectBuilder {

    public static boolean DEBUG = false;
    
    /** The number of ticks with which new monitors are initialized. */
    private static final int TICKS_PER_MONITOR = 1000;

    /** The set of projects for which cal modules have been loaded. 
     * TODOEL: (HACK?) It would be nicer to serialize this info, and find in per-project info. */
    // not used!!
    //    private static final Set loadedProjects = new HashSet();

    /**
     * The shared foreign context provider for modules built by the cal builder.
     */
    private static final ForeignContextProvider sharedForeignContextProvider = new CALBuilderForeignContextProvider();
    
    /** 
     * The last built state of the current project. 
     * This is updated after a build for this builder's project has taken place.
     * It is null if clean() is called, or if never built.
     * */
    private ProjectBuildState lastState;

    /**
     * List of listeners of modules that have been compiled.
     */
    private static final List<BuildMessagesListener> listeners = new ArrayList<BuildMessagesListener>();    
    
    /**
     * The foreign context provider for modules built by the cal builder.
     * @author Edward Lam
     */
    private static class CALBuilderForeignContextProvider implements ForeignContextProvider {
        /**
         * {@inheritDoc}
         */
        public ClassLoader getClassLoader(ModuleName moduleName) {
            // Get the source file for the module.
            IStorage storage = CALModelManager.getCALModelManager().getInputSourceFile(moduleName);
            if (storage == null) {
                return null;
            }
            
            if (storage instanceof IFile) {
                // Get the project which contains the source file, and get the corresponding java project.
                IFile inputFile = (IFile)storage;
                return CALModelManager.getCALModelManager().getClassLoader(inputFile.getProject());
            
            } else {
                // eg. a Jar Entry
                ICALResourceContainer container = CALModelManager.getCALModelManager().getInputSourceFileContainer(moduleName);
                if (container == null) {
                    return null;
                }
                IProject project = container.getPackageRoot().getJavaProject().getProject();
                return CALModelManager.getCALModelManager().getClassLoader(project);
            }
        }
    }

    /**
     * A helper class to find changed files in the CAL input folders for this project by visiting a resource delta.
     * @author Edward Lam
     */
    private static class ChangedCALFolderFileFinder implements IResourceDeltaVisitor {
        
        /** Map<IResource, IFolder> the map of files to their input folders for files in the delta which were added. */
        private final Map<IFile, IFolder> addedFiles = new HashMap<IFile, IFolder>(); 
        
        /** Map<IResource, IFolder> the map of files to their input folders for files in the delta which were removed. */
        private final Map<IFile, IFolder> removedFiles = new HashMap<IFile, IFolder>(); 
        
        /** Map<IResource, IFolder> the map of files to their input folders for files in the delta which changed. */
        private final Map<IFile, IFolder> changedFiles = new HashMap<IFile, IFolder>(); 
        
        /** The input folders for this project. */
        private final ICALResourceContainer[] resourceContainers;

        ChangedCALFolderFileFinder(IProject project) {
            ICALResourceContainer[] containers = CALModelManager.getCALModelManager().getInputFolders(Util.getCalProject(project));
            List<ICALResourceContainer> inputFoldersList = new LinkedList<ICALResourceContainer>();
            for (final ICALResourceContainer container : containers) {
                // we only care about source (writable) packages, not binary
                if (container.isWritable()) {
                    inputFoldersList.add(container);
                }
            }
            
            this.resourceContainers = inputFoldersList.toArray(new ICALResourceContainer[inputFoldersList.size()]);
            
        }

        /**
         * {@inheritDoc}
         */
        public boolean visit(IResourceDelta delta) throws CoreException {
            // The delta resource can be anything from the changed project to the changed .cal file.
            IResource deltaResource = delta.getResource();
            
            if (deltaResource.getType() == IResource.FILE) {
                IFile deltaFile = (IFile)deltaResource;
                
                IFolder deltaInputFolder = null;
                for (final ICALResourceContainer resourceContainer : resourceContainers) {
                    IPath inputFolderFullPath = resourceContainer.getPath();
                    // this will only find writable resource containers (ie. not in a jar), but that is fine
                    if (inputFolderFullPath.matchingFirstSegments(deltaResource.getFullPath()) == inputFolderFullPath.segmentCount()) {
                        IFolder inputFolder = deltaResource.getProject().getFolder(inputFolderFullPath);
                        deltaInputFolder = inputFolder;
                        break;
                    }
                }

                if (deltaInputFolder != null) {
                    switch (delta.getKind()) {
                    case IResourceDelta.ADDED:
                        addedFiles.put(deltaFile, deltaInputFolder);
                        break;
                    case IResourceDelta.REMOVED:
                        removedFiles.put(deltaFile, deltaInputFolder);
                        break;
                    case IResourceDelta.CHANGED:
                        changedFiles.put(deltaFile, deltaInputFolder);
                        break;
                    default:
                        // must be a phantom?
                        break;
                    }
                }
                return false;
            }
            
            // Must be a container.
            IPath deltaResourcePath = deltaResource.getFullPath();      // This can be anything from the changed project to the changed .cal file.
            int nDeltaResourceSegments = deltaResourcePath.segmentCount();
            
            for (final ICALResourceContainer container : resourceContainers) {
                IPath resourceContainerPath = container.getPath();

                if (deltaResourcePath.equals(resourceContainerPath)) {
                    return true;
                    
                } else {
                    int nInputFolderSegments = resourceContainerPath.segmentCount();
                    
                    if (nDeltaResourceSegments < nInputFolderSegments) {
                        // Check if the delta is an ancestor of the input folder.
                        if (deltaResourcePath.matchingFirstSegments(resourceContainerPath) == nDeltaResourceSegments) {
                            return true;
                        }
                    
                    } else if (nDeltaResourceSegments > nInputFolderSegments) {
                        // The delta is deeper than the input folder, check if the input folder is an acestor of the delta
                        if (resourceContainerPath.matchingFirstSegments(deltaResourcePath) == nInputFolderSegments) {
                            return true;
                        }
                        
                    } else {
                        //   (nDeltaResourceSegments == nInputFolderSegments)   -- Same number of segments, but the segments don't match.
                    }
                }
            }
            
            return false;
        }

        /**
         * @return a copy of the added cal files from the delta.
         */
        public Set<IFile> getAddedFiles() {
            return Collections.unmodifiableSet(addedFiles.keySet());
        }
        /**
         * @return a copy of the removed cal files from the delta.
         */
        public Set<IFile> getRemovedFiles() {
            return Collections.unmodifiableSet(removedFiles.keySet());
        }
        /**
         * @return a copy of the changed cal files from the delta.
         */
        public Set<IFile> getChangedFiles() {
            return Collections.unmodifiableSet(changedFiles.keySet());
        }
    }

    /**
     * A status listener which provides the build notifier with updates.
     * @author Edward Lam
     */
    private static class BuildStatusListener implements StatusListener {
        
        /** (Set of ModuleName) the names of modules for which SM_LOADED module status events have been received by this listener. */
        private final Set/*ModuleName*/<ModuleName> loadedModuleNames = new HashSet<ModuleName>();
        
        private final IProgressMonitor progressMonitor;

        BuildStatusListener(IProgressMonitor progressMonitor) {
            progressMonitor = Util.monitorFor(progressMonitor);
            this.progressMonitor = progressMonitor;
            progressMonitor.beginTask("", TICKS_PER_MONITOR);
        }
        
        /**
         * {@inheritDoc}
         */
        public void incrementCompleted(double d) {
            checkAbort();
            
            // Note that d is a percentage.
            progressMonitor.worked((int)((d / 100.0) * TICKS_PER_MONITOR));
        }

        /**
         * {@inheritDoc}
         */
        public void setModuleStatus(StatusListener.Status.Module moduleStatus, ModuleName moduleName) {
            checkAbort();
            if (moduleStatus == StatusListener.SM_LOADED) {
                progressMonitor.subTask("Loaded module " + moduleName);
                loadedModuleNames.add(moduleName);
            }
        }

        /**
         * {@inheritDoc}
         */
        public void setEntityStatus(StatusListener.Status.Entity entityStatus, String entityName) {
            checkAbort();
            if (entityStatus == StatusListener.SM_COMPILED) {
                progressMonitor.subTask("Compiled: " + entityName);
            } else if (entityStatus == StatusListener.SM_ENTITY_GENERATED) {
                progressMonitor.subTask("Generated: " + entityName);
            } else if (entityStatus == StatusListener.SM_ENTITY_GENERATED_FILE_WRITTEN) {
                progressMonitor.subTask("Wrote: " + entityName);
            }
        }
        
        /**
         * @return (Set of ModuleName) the names of modules for which SM_LOADED module status events have been received by this listener.
         */
        public Set/*ModuleName*/<ModuleName> getLoadedModuleNames() {
            return loadedModuleNames;
        }
        
        /**
         * Abort compilation if cancelation is requested.
         */
        private void checkAbort() {
            // TODOEL: This successfully aborts compilation, but currently is reported as an internal error, ie. "Please contact Business Objects."
            // The JavaBuilder calls BuildNotifier.checkCancelWithinCompiler, which throws an AbortCompilation runtime exception.
            //
            // One possibility here is to log a fatal error to the compiler's message logger.
            Util.checkCanceled(progressMonitor);
        }
        
        /**
         * Signal to the status listener that compilation is done.
         */
        public void done() {
            progressMonitor.done();
        }
    }
    
    /**
     * Hook allowing to initialize some static state before a complete build iteration.
     * This hook is invoked during PRE_AUTO_BUILD notification
     */
    public static void buildStarting() {
        // build is about to start
    }

    /**
     * Hook allowing to reset some static state after a complete build iteration.
     * This hook is invoked during POST_AUTO_BUILD notification
     */
    public static void buildFinished() {
        /*
         * TODOEL: Not yet called.
         */
        GlobalBuildState.resetProblemCounters();
    }

    /**
     * Get a subprogress monitor of a given monitor.
     * @param parentMonitor the monitor for which to get a subprogress monitor.
     * @param fraction the fraction of the parent monitor's ticks to which to allocate to the child.
     * @return a subprogress monitor with the given attributes.
     */
    private static IProgressMonitor getSubMonitor(IProgressMonitor parentMonitor, double fraction) {
        return Util.subMonitorFor(parentMonitor, (int)(TICKS_PER_MONITOR * fraction));
    }
    
    /**
     * @return true if the CALBuilder is enabled.
     */
    public static boolean isEnabled(){
        return CoreOptionIDs.ENABLED.equals(CALEclipseCorePlugin.getOption(CoreOptionIDs.CORE_CAL_BUILD_ENABLE));
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    protected IProject[] build(int kind, Map args, IProgressMonitor monitor) throws CoreException {
        // Check if the current project is valid.
        IProject currentProject = getProject();
        if (currentProject == null || !currentProject.isAccessible()) {
            return new IProject[0];
        }
        
        // Check if the builder is enabled.
        if (!isEnabled()) {
            // Remove CAL builder problem markers.
            removeProblemsAndTasksFor(currentProject);
            return new IProject[0];
        }
        
        if (DEBUG) {
            Util.printlnWithDate("\nStarting build of " + currentProject.getName()); //$NON-NLS-1$
        }
        
        
        monitor = Util.monitorFor(monitor);
        
        monitor.beginTask("", TICKS_PER_MONITOR);
        
        boolean ok = false;
        try {
            Util.checkCanceled(monitor);
            if (isWorthBuilding()) {
                if (kind == FULL_BUILD) {
                    buildAll(getSubMonitor(monitor, 1.0));
                
                } else {
                    Set<IResourceDelta> deltas = findDeltas(currentProject, getSubMonitor(monitor, 0.2));
                    if (deltas == null) {
                        buildAll(getSubMonitor(monitor, 0.8));
                    } else {
                        buildDeltas(deltas, getSubMonitor(monitor, 0.8));
                    }
                }
                ok = true;
            }
        } catch (CoreException e) {
            Util.log(e, "CALBuilder handling CoreException while building: " + currentProject.getName()); //$NON-NLS-1$
            IMarker marker = currentProject.createMarker(CALModelMarker.CAL_MODEL_PROBLEM_MARKER);
            marker.setAttribute(IMarker.MESSAGE, Messages.bind(Messages.build_inconsistentProject, e.getLocalizedMessage()));
            marker.setAttribute(IMarker.SEVERITY, IMarker.SEVERITY_ERROR);
        } finally {
            // If the build failed, clear the previously built state, forcing a full build next time.
            if (!ok) {
//                clearLastState();
            }
            
            monitor.done();
            cleanup();
        }
        IProject[] requiredProjects = getRequiredProjects(currentProject, true);
        if (DEBUG) {
            Util.printlnWithDate("Finished build of " + currentProject.getName()); //$NON-NLS-1$
        }
        return requiredProjects;
    }

    /**
     * Common pre-build set up.
     * @param monitor the tracking monitor.
     */
    private void preBuild(IProgressMonitor monitor) {
        Util.checkCanceled(monitor);
        monitor.subTask(Messages.build_preparingBuild);
//        if (DEBUG && lastState != null) {
//            System.out.println("Clearing last state : " + lastState); //$NON-NLS-1$
//        }
//        clearLastState(); // clear the previously built state so if the build fails, a full build will occur next time
    }
    
    /**
     * Common post-build debrief.
     * @param logger the logger from the build.
     * @param updatedInputFiles (List of IResource) the files which were updated (loaded or otherwise changed) during this build.
     * ie. the resource delta was IResourceDelta.CHANGED (not ADDED or REMOVED).
     * If null, all resources in the project are taken as changed.
     * @param monitor the tracking monitor.
     */
    private void postBuild(CompilerMessageLogger logger, List<IFile> updatedInputFiles, IProgressMonitor monitor) {

        monitor.beginTask("", TICKS_PER_MONITOR);
        
        try {
            // For now, dump all errors to the console.
            for (final CompilerMessage message : logger.getCompilerMessages()) {
                System.out.println(message);
            }
            
            // Update the problem markers from the logger.
            updateProblemMarkers(updatedInputFiles, logger);
            
            // Log internal errors from the logger;
            logInternalErrors(logger);
            
            // mark output folders as derived.
            IFolder[] outputFolders = getOutputFolders();
            for (final IFolder folder : outputFolders) {
                try {
                    folder.setDerived(true);
                } catch (CoreException e) {
                    // Resource doesn't exist.
                    // this could happen if the file was added to the project while the build was going on. 
                }
            }
            
            if (logger.getNErrors() > 0) {
                // If the build failed, clear the previously built state, forcing a full build next time.
//                clearLastState();
            }
            
            // Record the new build state.
            this.lastState = new ProjectBuildState(this);

        } finally {
            monitor.done();
        }
    }
    
    /**
     * A message logger that will not keep messages that are already in the logger.
     * 
     * @author Greg McClement
     */
    private static class MessageLoggerWithNoDuplicates extends MessageLogger{
        /**
         * @param range1 first range to compare
         * @param range2 second range to compare
         * @return true if the given source range cover the same positions
         */
        public boolean same(SourceRange range1, SourceRange range2){
            if (SourcePosition.compareByPosition.compare(range1.getStartSourcePosition(), range2.getStartSourcePosition()) != 0){
                return false;
            }
            
            if (SourcePosition.compareByPosition.compare(range1.getEndSourcePosition(), range2.getEndSourcePosition()) != 0){
                return false;
            }
            
            return true;
        }

        private boolean same(CompilerMessage m1, CompilerMessage m2){
            if (m1.getSourceRange() != null && m2.getSourceRange() != null){
                if (!same(m1.getSourceRange(), m2.getSourceRange())){
                    return false;
                }
            }
            else{
                if (m1.getSourceRange() == null && m2.getSourceRange() == null){
                }
                else{
                    // on is null and the other isn't
                    return false;
                }
            }
            
            if (!m1.toString().equals(m2.toString())){
                return false;
            }
            
            return true;
        }
        
        private boolean alreadyLogged(CompilerMessage passedInMessage) {
            for (final CompilerMessage message : getCompilerMessages()) {
                if (same(passedInMessage, message)) {
                    return true;
                }
            }
            return false;
        }
        
        @Override
        public void logMessage(CompilerMessage compilerMessage) {
            if (alreadyLogged(compilerMessage)){
                return;
            }
            super.logMessage(compilerMessage);
        }
        
        @Override
        public void logMessages(CompilerMessageLogger otherLogger) {
            final List<CompilerMessage> messages = otherLogger.getCompilerMessages();
            for (final CompilerMessage message : messages) {
                logMessage(message);
            }
        }
    }
    
    /**
     * @return a new message logger instance.
     */
    private CompilerMessageLogger getNewMessageLogger() {
        CompilerMessageLogger messageLogger = new MessageLoggerWithNoDuplicates();
        String number = CALEclipseCorePlugin.getOption(CoreOptionIDs.COMPILER_PB_MAX_PER_UNIT);
        try {
            Integer.parseInt(number);
        } catch (NumberFormatException e) {
            // What to do??
        }
        return messageLogger;
    }
    
    
    
    /**
     * Helper message to determine whether an "Unresolved external module import" message is one we can ignore.
     * @param compilerMessage a compiler message encountered during compilation
     * @return whether this message is an "Unresolved external module import" message which can be ignored
     *  because it results from a compilation error in the module being resolved.
     */
    private boolean isIgnorableUnresolvedExternalModuleImportMessage(CompilerMessage compilerMessage) {
        MessageKind messageKind = compilerMessage.getMessageKind();
        String message = messageKind.getMessage();
        
        // "Unresolved external module import Cal.Core.String."
        String unresolvedExternalModuleImportPrefixString = "Unresolved external module import ";
        if (!message.startsWith(unresolvedExternalModuleImportPrefixString)) {
            return false;
        }
        
        // The error messages looks like
        // 
        //      Unresolved external module import MathZogu.
        //      Unresolved external module import Math. Did you mean Cal.Utilities.Math?
        //

        int indexOfEndOfFirstSentence = message.indexOf("Did you mean");
        if (indexOfEndOfFirstSentence == -1) {
            indexOfEndOfFirstSentence = message.length() - 1;
        }

        // Parse out the module name.
        int endOfSentenceIndex = message.lastIndexOf('.', indexOfEndOfFirstSentence); // This is the period at the end of the sentence, guaranteed not a part of a hierarchical module name.
        if (endOfSentenceIndex < 0) {
            assert false;
            return false;
        }

        ModuleName moduleName = ModuleName.maybeMake(message.substring(unresolvedExternalModuleImportPrefixString.length(), endOfSentenceIndex));
        if (moduleName == null) {
            assert false;
            return false;
        }

        // Only ignorable if the input file exists in a project referenced by this one.
        
        ICALResourceContainer container = CALModelManager.getCALModelManager().getInputSourceFileContainer(moduleName);
        if (container == null) {
            return false;
        }
        IProject inputSourceFileProject = 
            container.getPackageRoot().getJavaProject().getProject();
        

        
        IProject[] referencedProjects = null;
        try {
            referencedProjects = getProject().getReferencedProjects();
        
        } catch (CoreException e) {
            // shouldn't happen
            e.printStackTrace();
            return false;
        }

        // We can ignore if it's indeed in a referenced project.  ie. it just has a compile error.
        return Arrays.asList(referencedProjects).contains(inputSourceFileProject);
    }
    
    /**
     * Update the problem markers associated with the build of this project.
     * @param updatedInputFiles (List of IResource) the files which were loaded or otherwise changed during this build.
     * ie. the resource delta was IResourceDelta.CHANGED (not ADDED or REMOVED).
     * If null, all resources in the project are taken as changed.
     * @param messageLogger the logger from which to update warnings and errors.
     */
    private void updateProblemMarkers(List<IFile> updatedInputFiles, CompilerMessageLogger messageLogger) {
        /*
         * TODOEL: Create a marker subtype for compilation problems.
         * Just remove those problems in this method -- there may be other types of problems in this project.
         */
        
        CALModelManager modelManager = CALModelManager.getCALModelManager();

        /*
         * Delete problem markers.
         */
        
        final HashSet<ModuleName> wasCompiled = new HashSet<ModuleName>();
        if (updatedInputFiles == null) {
            removeProblemsFor(getProject());
        } else {
            for (final IFile resource : updatedInputFiles) {
                removeProblemsFor(resource);
                ModuleName moduleName = modelManager.getModuleName(resource);
                if (moduleName != null) {
                    wasCompiled.add(moduleName);
                }
            }
        }
        
        /*
         * Add new problem markers.
         */
        for (final CompilerMessage cm : messageLogger.getCompilerMessages(CompilerMessage.Severity.WARNING)) {
            final CompilerMessage compilerMessage = cm;
            
            /*
             * TODOEL: ignore fatal?  (ie. unable to recover..)
             */
            // Only consider things at least as bad as warnings..
            if (compilerMessage.getSeverity().compareTo(CompilerMessage.Severity.WARNING) < 0) {
                continue;
            }

            // Check for messages that we can ignore and for the UnsupportedClassVersionError messages.
            final boolean isUnsupportedClassVersionError;
            {
                if (compilerMessage.getException() instanceof OperationCanceledException) {
                    // Compilation was aborted.
//                    continue;
                    
                    // We may have to do this instead to get the builder to call us again on this project.
                    throw (RuntimeException)compilerMessage.getException();
                }
                
                /* 
                 * **HACK** -- don't have access to the message kind class, since those are package private.
                 *   Want a comparison such as: 
                 *     if (messageKind.getClass() == MessageKind.Error.UnresolvedExternalModuleImport.class)  { ... }
                 *   For now we parse the actual message string -- *yuck*.
                 */
                MessageKind messageKind = compilerMessage.getMessageKind();
                String message = messageKind.getMessage();
                
                if (message.indexOf("java.lang.UnsupportedClassVersionError") != -1){
                    isUnsupportedClassVersionError = true;
                } else {
                    isUnsupportedClassVersionError = false;
                    if (isIgnorableUnresolvedExternalModuleImportMessage(compilerMessage)) {
                        continue;
                    }
                }
            }

            // Get the resource for the marker.
            IStorage markerResourceTemp = null;
            final SourceRange sourceRange = compilerMessage.getSourceRange();
            if (sourceRange != null) {
                ModuleName sourceName = ModuleName.maybeMake(sourceRange.getSourceName());
                if (sourceName != null) {
                    wasCompiled.add(sourceName);
                    markerResourceTemp = modelManager.getInputSourceFile(sourceName);
                }
            }
            
            // Assign to final var so that it can be acccessed by the runnable.
            // we only care about updating markers if this is a file, not a jar entry
            if (! (markerResourceTemp instanceof IFile)) {
                return;
            }
            final IFile markerResource = (IFile) markerResourceTemp;
            
            // Construct the attribute map then create the marker with the attributes.
            try {
                // The attributes for the marker.
                final Map<String, Object> attributeMap = new HashMap<String, Object>();

                // attributeMap.put(IMarker.MESSAGE, Messages.bind(Messages.build_inconsistentProject, "foo"));

                String markerMessage = compilerMessage.getMessage();
                Exception compileException = compilerMessage.getException();
                if (isUnsupportedClassVersionError){
                    markerMessage += " " + Messages.error_hint_UnsupportedClassVersionError;
                }
                if (compileException != null) {
                    markerMessage += "  " + compileException.getLocalizedMessage();
                }

                attributeMap.put(IMarker.MESSAGE, markerMessage);
                if (compilerMessage.getSeverity() == CompilerMessage.Severity.WARNING) {
                    attributeMap.put(IMarker.SEVERITY, Integer.valueOf(IMarker.SEVERITY_WARNING));
                } else {
                    attributeMap.put(IMarker.SEVERITY, Integer.valueOf(IMarker.SEVERITY_ERROR));
                }

                // Add source position.
                if (sourceRange != null) {

                    // Get the source text.
                    // Note: SLOW.
                    ByteArrayOutputStream baos = new ByteArrayOutputStream(4096);
                    InputStream contents = null;
                    try {
                        contents = (markerResource).getContents();
                        FileSystemResourceHelper.transferData(contents, baos);
                    } catch (CoreException e) {
                        e.printStackTrace();
                    } catch (IOException e) {
                        e.printStackTrace();
                    } finally {
                        try {
                            baos.close();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }

                        if (contents != null) {
                            try {
                                contents.close();
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        }
                    }
                    String sourceText = new String(baos.toByteArray());         // TODOEL: converted using platform charset.

                    SourcePosition startSourcePosition = sourceRange.getStartSourcePosition();
                    if (startSourcePosition.getLine() != 0){
                        final int startPosition;
                        try {
                            startPosition = sourceRange.getStartSourcePosition().getPosition(sourceText);
                        
                        } catch (IllegalArgumentException e){
                            // TEMP: shouldn't happen.
                            // dump some info so that source position problems can be debugged.

                            // A string with the compiler messages for this build.
                            StringBuilder messagesBuf = new StringBuilder();
                            for (final CompilerMessage message : messageLogger.getCompilerMessages(CompilerMessage.Severity.WARNING)) {
                                messagesBuf.append(message.toString() + '\n');
                            }

                            // the path to the marker with the problem.
                            String pathString = markerResource.getFullPath().toString();
                            
                            // Log to the error log.
                            String errorMessage = "Exception getting position from resource " + pathString + ".\n" + messagesBuf.toString();
                            
                            System.err.println(errorMessage);
                            Util.log(e, errorMessage); 

                            // rethrow the IllegalArgumentException for now.
                            throw e;
                        }
                        
                        attributeMap.put(IMarker.CHAR_START, Integer.valueOf(startPosition));
                        try {
                            final int endPosition = sourceRange.getEndSourcePosition().getPosition(sourceText);                            
                            attributeMap.put(IMarker.CHAR_END, Integer.valueOf(endPosition));
                        
                        } catch (IllegalArgumentException e){
                            // If the error is at the end of file then the source range goes one past the
                            // end of the file so this exception will be thrown. In this case, we will just
                            // use the start position as the end position since that is the correct spot 
                            // anyway.
                            attributeMap.put(IMarker.CHAR_END, Integer.valueOf(startPosition));    
                        }

                        int lineNumber = sourceRange.getStartLine();
                        if (lineNumber > 0) {
                            attributeMap.put(IMarker.LINE_NUMBER, Integer.valueOf(sourceRange.getStartLine()));
                            attributeMap.put("startColumn", Integer.valueOf(sourceRange.getStartColumn()));

                            // Don't set the location attribute unless we don't have a line number.
                            // If this is unset, the line number attribute is used in the location column.
                            // attributeMap.put(IMarker.LOCATION, "#" + sourcePosition.getLine());
                        }
                    
                    } else {
                        // an error that does not map to source
                        attributeMap.put(IMarker.CHAR_START, Integer.valueOf(0));
                        attributeMap.put(IMarker.CHAR_END, Integer.valueOf(0));
                    }
                }

                // Post the marker (unless it's a duplicate)
                postMarkerIfNonDuplicate(markerResource, attributeMap);

            } catch (CoreException e) {
                // TODOEL Auto-generated catch block
                // Project or marker doesn't exist.
                Util.log(e, e.getMessage());
            }
            
            /*
             * TODOEL: quick fix?  :)
             */
        }
        
        // Add problems for duplicate resources
        IStorage[] duplicateSourceFiles = modelManager.getDuplicateSourceFiles();
        IProject project = getProject();
        for (final IStorage duplicateIStorage : duplicateSourceFiles) {
            if (duplicateIStorage instanceof IFile) {
            
                final IFile duplicateSourceFile = (IFile)duplicateIStorage;
                if (duplicateSourceFile.getProject() == project) {
                    // Batch marker creation with updates so that only 1 resource change is broadcast.
                    try {
                        ResourcesPlugin.getWorkspace().run(new IWorkspaceRunnable() {
                            public void run(IProgressMonitor monitor) throws CoreException {
                                IMarker marker = duplicateSourceFile.createMarker(CALModelMarker.CAL_MODEL_PROBLEM_MARKER);
                                
                                marker.setAttribute(IMarker.MESSAGE, "Source file " + duplicateSourceFile.getName() + " already exists.");
                                marker.setAttribute(IMarker.SEVERITY, IMarker.SEVERITY_ERROR);
                                
                            }
                        }, null);
                        
                    } catch (CoreException e) {
                        // TODOEL Auto-generated catch block
                        // Project or marker doesn't exist.
                        e.printStackTrace();
                    }
                }
            }
        }

        // Add problems for invalid resource names.
        {
            Set<Pair<IStorage, IPackageFragmentRoot>> resourcesWithInvalidNames = modelManager.getResourcesWithInvalidNames(project);
            if (resourcesWithInvalidNames != null) {
                
                for (Pair<IStorage, IPackageFragmentRoot> invalidResourceInfo : resourcesWithInvalidNames) {
                    IStorage iStorage = invalidResourceInfo.fst();
                    String message = "Resource name does not correspond to a module name: " + iStorage.getFullPath().toString();
                    attachErrorMarkerToStorage(iStorage, invalidResourceInfo.snd(), message);
                }
            }
        }

        notify(wasCompiled);
    }
    
    /**
     * Attach an error marker to the given IStorage
     * @param iStorage the IStorage to which the marker should be attached.
     * @param packageRoot the package fragment root associated with iStorage.
     * @param message the message associated with the marker.
     */
    private void attachErrorMarkerToStorage(IStorage iStorage, IPackageFragmentRoot packageRoot, String message) {

        // Determine the associated resource.
        IResource markerResource;
        if (iStorage instanceof IResource) {
            markerResource = (IResource)iStorage;

        } else {
            // The storage isn't a resource.  The package root should correspond to a resource.
            markerResource = (IResource)packageRoot.getAdapter(IResource.class);

            if (markerResource == null) {
                // Maybe the best we can do is to associate the marker with the project.
                markerResource = getProject();
            }
        }

        // Add an error marker
        Map<String, Object> attributeMap = new HashMap<String, Object>();

        attributeMap.put(IMarker.MESSAGE, message);
        attributeMap.put(IMarker.SEVERITY, Integer.valueOf(IMarker.SEVERITY_ERROR));

        try {
            CALBuilder.postMarkerIfNonDuplicate(markerResource, attributeMap);
        } catch (CoreException e) {
            Util.log(e, "Exception posting marker to resource " + markerResource);
        }

    }
    
    /**
     * Post the given marker, unless there is already a marker which contains the attributes in the attribute map.
     * The marker can have other attributes as well, for instance if the user has marked it as important -- these will be ignored.
     * 
     * @param markerResource the resource on which the marker will be created.
     * @param attributeMap the attribute names and values the marker will have.
     * @throws CoreException if there is a problem creating the marker or accessing its attributes.
     */
    private static void postMarkerIfNonDuplicate(final IResource markerResource, final Map<String, Object> attributeMap) throws CoreException {
        IMarker[] existingMarkers = markerResource.findMarkers(CALModelMarker.CAL_MODEL_PROBLEM_MARKER, false, IResource.DEPTH_ZERO);

        boolean markerExists = false;
        for (final IMarker existingMarker : existingMarkers) {
            // Check whether there is a marker with the same attributes.
            // The marker can have other attributes as well, for instance if the user has marked it as important.
            if (markerHasAttributes(existingMarker, attributeMap)) {
                markerExists = true;
                break;
            }
        }

        // Post the marker.
        if (!markerExists) {
            // Batch marker creation with updates so that only 1 resource change is broadcast.
            ResourcesPlugin.getWorkspace().run(new IWorkspaceRunnable() {
                public void run(IProgressMonitor monitor) throws CoreException {

                    IMarker marker = markerResource.createMarker(CALModelMarker.CAL_MODEL_PROBLEM_MARKER);
                    marker.setAttributes(attributeMap);
                }
            }, null);
        }

    }
    /** 
     * Strings (in resource bundle) which correspond to internal errors - case insensitive
     * Used by logInternalErrors()
     * HACK - we shouldn't be analyzing the message text.
     */
    private static final String[] internalErrorStrings = {
            "Please contact Business Objects.",
            "internal coding error",
            "Error while packaging module",
            "Could not create a module",   // CouldNotCreateModuleInPackage
            "Tree parsing error"
    };

    /**
     * Analyze the messageLogger from a recent compilation and log anything which corresponds to internal errors to the Eclipse error log.
     * @param messageLogger the logger whose messages will be analyzed.
     */
    private void logInternalErrors(CompilerMessageLogger messageLogger) {
        for (final CompilerMessage cm : messageLogger.getCompilerMessages(CompilerMessage.Severity.WARNING)) {
            final CompilerMessage compilerMessage = cm;
            String upperCaseCompilerMessageText = compilerMessage.getMessage().toUpperCase();

            /*
             * Only consider fatals which are internal errors:
             *   InternalCodingError
             *   CodeGenerationAbortedDueToInternalCodingError
             *   CompilationAbortedDueToInternalModuleLoadingError
             *   ErrorWhilePackagingModule
             *   CouldNotCreateModuleInPackage
             *   TreeParsingError
             *   UnexpectedUnificationFailure
             *   MoreThanOneDefinitionInANonRecursiveLet
             *   UnliftedLambdaExpression
             */
            boolean isInternalError = false;
            
            for (final String internalErrorString : internalErrorStrings) {
                String upperCaseInternalErrorString = internalErrorString.toUpperCase();
                if (upperCaseCompilerMessageText.indexOf(upperCaseInternalErrorString) > -1) {
                    isInternalError = true;
                    break;
                }
            }
            
            if (isInternalError) {
                String messageToLog = "Internal compiler error.\nCompiler message: " + compilerMessage.getMessage();
                Exception exceptionToLog = compilerMessage.getException(); // can be null

                Util.log(exceptionToLog, messageToLog);
            }
        }
    }

    /**
     * Return whether a marker has the attributes in a provided attribute map.
     * @param marker the marker to consider
     * @param attributeMap the attribute map to consider
     * @return whether the marker has the attributes in the attribute map.
     * False if any attribute values differ.  Note that the marker is allowed to have more attributes than provided in the map.
     * @throws CoreException if there was a problem getting the value of the attribute from either of the markers.
     */
    private static boolean markerHasAttributes(IMarker marker, Map<String, Object> attributeMap) throws CoreException {

        Map<String, Object> markerAttributeMap = UnsafeCast.unsafeCast(marker.getAttributes());  // attribute value - string, integer, boolean, or null
        
        // Check for too many attributes.
        if (markerAttributeMap.size() > attributeMap.size()) {
            return false;
        }
        
        // Check for different attribute values.
        for (final Map.Entry<String, Object> mapEntry : markerAttributeMap.entrySet()) {
            Object value1 = mapEntry.getValue();
            Object value2 = attributeMap.get(mapEntry.getKey());
            
            if (value1 == null) {
                if (value2 != null) {
                    return false;
                } else {
                    // both null
                }
            
            } else {
                if (value2 == null || !value1.equals(value2)) {
                    return false;
                } else {
                    // same value
                }
            }
        }
        
        // They're the same.
        return true;
    }
    
    /**
     * @return CompilationOptions to use when compiling modules from this builder.
     */
    private ProgramModelManager.CompilationOptions getCompilationOptions() {
        // Add a compilation option for a custom foreign context provider.
        ProgramModelManager.CompilationOptions compilationOptions = new ProgramModelManager.CompilationOptions();
        compilationOptions.setCustomForeignContextProvider(sharedForeignContextProvider);
        return compilationOptions;
    }
    
    /**
     * Build everything for the current project.
     */
    private void buildAll(IProgressMonitor monitor) {
        monitor.beginTask("", TICKS_PER_MONITOR);
        try {
            preBuild(getSubMonitor(monitor, 0.1));
//          BatchImageBuilder imageBuilder = new BatchImageBuilder(this);
//          imageBuilder.build();
//          recordNewState(imageBuilder.newState);
            
            // analyse deltas: 
            //   find source files in delta: 0.1
            //   find affected sources: 0.1
            // add affected sources: 0.05
            // compile loop: 0.4
            // Should update status listener to notify when finished compiling a module.
            
            CALModelManager modelManager = CALModelManager.getCALModelManager();
            
            // Remove any and all modules referenced by the old build state.
            if (lastState != null) {
                ModuleSourceDefinitionGroup moduleSourceDefinitionGroup = lastState.getModuleSourceDefinitionGroup();
                for (int i = 0; i < moduleSourceDefinitionGroup.getNModules(); i++) {
                    ModuleName moduleName = moduleSourceDefinitionGroup.getModuleSource(i).getModuleName();
                    modelManager.getProgramModelManager().removeModule(moduleName);
                }
            }

            EclipseModuleSourceDefinitionGroup sourceDefinitionGroup = getModuleSourceDefinitionGroup();
            CompilerMessageLogger logger = getNewMessageLogger();
            
            if (sourceDefinitionGroup != null) {
                
                aboutToCompile(getProject().getName(), monitor);
                
                ProgramModelManager programModelManager = modelManager.getProgramModelManager();
                
                BuildStatusListener buildStatusListener = new BuildStatusListener(getSubMonitor(monitor, 0.7));
                programModelManager.addStatusListener(buildStatusListener);
                try {
                    // Compile all modules in the program model manager.
                    programModelManager.compile(sourceDefinitionGroup, logger, false, null, getCompilationOptions());
                } finally {
                    programModelManager.removeStatusListener(buildStatusListener);
                }
                
                buildStatusListener.done();
            
            } else {
                // No source definition group.
            }
            
            postBuild(logger, null, getSubMonitor(monitor, 0.2));
        
        } finally {
            monitor.done();
        }
    }
    
    /**
     * @return the ModuleSourceDefinitionGroup for the current project.
     */
    EclipseModuleSourceDefinitionGroup getModuleSourceDefinitionGroup() {
        IProject currentProject = getProject();
        return CALModelManager.getCALModelManager().getModuleSourceDefinitionGroup(currentProject);
    }

    /**
     * Announce that a unit is about to be compiled.
     * @param unitName the name of the unit to compile.
     * @param monitor the tracking monitor.
     */
    public void aboutToCompile(String unitName, IProgressMonitor monitor) {
        /*
         * TODOEL:
         * The Java compiler has a nice feature where, as problems are accumulated, 
         * it displays a running total of the number of problems which are new, and which are fixed.
         */
        String message = Messages.bind(Messages.build_compiling, unitName);
        monitor.subTask(message);
    }
    
    /**
     * Build deltas.
     * @param deltas (IResourceDelta) Set of resource deltas for that project.
     */
    private void buildDeltas(Set<IResourceDelta> deltas, IProgressMonitor monitor) {
        monitor.beginTask("", TICKS_PER_MONITOR);
        try {
            // JavaBuilder goes through the IncrementalImageBuilder.
            preBuild(getSubMonitor(monitor, 0.1));
            
            IProject project = getProject();
            CALModelManager modelManager = CALModelManager.getCALModelManager();
            ProgramModelManager programModelManager = modelManager.getProgramModelManager();
            
            EclipseModuleSourceDefinitionGroup definitionGroup = getModuleSourceDefinitionGroup();
            
            monitor.subTask(Messages.build_analyzingSources);
            monitor.subTask(Messages.build_analyzingDeltas);
            
            // Get the changed resources, convert this info into changed modules.
            // Relevant resource: it's in an input folder, and it ends with ".cal"
            
            Set<IFile> addedInputFiles = new HashSet<IFile>();
            Set<IFile> removedInputFiles = new HashSet<IFile>();
            Set<IFile> changedInputFiles = new HashSet<IFile>();
            for (final IResourceDelta resourceDelta : deltas) {
                ChangedCALFolderFileFinder finder = new ChangedCALFolderFileFinder(project);
                try {
                    resourceDelta.accept(finder);
                } catch (CoreException e) {
                    // open error dialog with syncExec, or dump to log.
                }
                
                addedInputFiles.addAll(finder.getAddedFiles());
                removedInputFiles.addAll(finder.getRemovedFiles());
                changedInputFiles.addAll(finder.getChangedFiles());
            }
            
            // Remove cal resources for removed cal files.
            
            boolean badnessHasOccurred = false;
            for (final IFile removedInputFile : removedInputFiles) {
                
                ModuleName moduleName = Util.getModuleNameFromStorage(removedInputFile);
                if (moduleName == null) {
                    // the storage name doesn't correspond to a module name
                    continue;
                }

                // figure out which resource folder must be deleted
                // must do this now, while we can still locate the resource
                ProgramResourceLocator.Folder moduleResourceLocator = new ProgramResourceLocator.Folder(moduleName, ResourcePath.EMPTY_PATH);
                IFolder resourceFolder = getFolder(moduleResourceLocator, removedInputFile);

                // Update program for removed modules and dependees.
                // We only really need to do this for removed modules, to guard against stale ModuleTypeInfo in the ProgramModelManager.
                if (lastState != null) {
                    ModuleSourceDefinitionGroup moduleSourceDefinitionGroup = lastState.getModuleSourceDefinitionGroup();
                    if (moduleSourceDefinitionGroup.getModuleSource(moduleName) != null) {
                        boolean moduleRemoved = modelManager.getProgramModelManager().removeModule(moduleName);
                        if (!moduleRemoved) {
                            System.err.println("Could not remove module: " + moduleName);
                        }
                    }
                }

                // OK, now we can delete the resources
                try {
                    if (resourceFolder != null && resourceFolder.exists()) {
                        // force, progressMonitor
                        resourceFolder.delete(true, null);
                    }
                } catch (CoreException e) {
                    Util.log(new IOException().initCause(e), "Couldn't delete " + resourceFolder);
                    badnessHasOccurred = true;
                }
            }
            if (badnessHasOccurred) {
                return;
            }
            
            if (definitionGroup == null) {
                // This can happen if we removed the last cal file from a definition group, causing it to no longer be a group.
                return;
            }
            ModuleSourceDefinitionGroup writableSubGroup = definitionGroup.getWritableSubGroup();

            List<ModuleName> namesOfModulesToCompileList = new ArrayList<ModuleName>();
            
            getAffectedModuleSourceFiles(removedInputFiles, writableSubGroup, namesOfModulesToCompileList);
            getAffectedModuleSourceFiles(addedInputFiles, writableSubGroup, namesOfModulesToCompileList);
            getAffectedModuleSourceFiles(changedInputFiles, writableSubGroup, namesOfModulesToCompileList);
            
            aboutToCompile(getProject().getName(), monitor);
            
            CompilerMessageLogger logger = getNewMessageLogger();
            BuildStatusListener buildStatusListener = new BuildStatusListener(getSubMonitor(monitor, 0.7));
            programModelManager.addStatusListener(buildStatusListener);
            try {
                ProgramModelManager.CompilationOptions compilationOptions = getCompilationOptions();
                if (!namesOfModulesToCompileList.isEmpty()) {
                    /*
                     * TODOEL: Don't compile twice.
                     *   We should be able to perform the dirty computation ourselves (ie. which modules depend on the modules with deltas).
                     */
                    // Compile modules with deltas.
                    ModuleName[] namesOfModulesToCompile = namesOfModulesToCompileList.toArray(new ModuleName[namesOfModulesToCompileList.size()]);
                    programModelManager.makeModules(namesOfModulesToCompile, definitionGroup, logger, compilationOptions);
                }
                
                // Still need to recompile dirty, since dependee modules may have changed.
                programModelManager.compile(definitionGroup, logger, true, buildStatusListener, compilationOptions);
                
            } finally {
                programModelManager.removeStatusListener(buildStatusListener);
            }

            // Work out which cal files which were compiled.
            
            // Start out with changed files.
            // Might not show up below if a file to compile has a compile error (and thus not loaded).
            Set<IFile> updatedInputFiles = new HashSet<IFile>(changedInputFiles);
            
            // Add all the files which were loaded (valid changes + compiled dependencies)
            for (final ModuleName loadedModuleName : buildStatusListener.getLoadedModuleNames()) {
                IStorage inputSourceFile = CALModelManager.getCALModelManager().getInputSourceFile(loadedModuleName);
                if (inputSourceFile instanceof IFile) {
                    // only files have been compiled
                    updatedInputFiles.add((IFile)inputSourceFile);
                }
            }
            
            List<IFile> updatedInputFilesList = new ArrayList<IFile>(updatedInputFiles);
            
            postBuild(logger, updatedInputFilesList, getSubMonitor(monitor, 0.2));

        } finally {
            monitor.done();
        }
    }
    
    /**
     * Determines the resource folder corresponding to the calFile
     * @param moduleResourceLocator the folder path of the resource to locate
     * @param calFile the cal file that creates the folder to locate
     * @return the folder, somewhere under the resource folder (lecc_runtime)
     */
    private IFolder getFolder(ProgramResourceLocator.Folder moduleResourceLocator,
            IFile calFile) {
        
        ICALResourceContainer container = 
            CALModelManager.getCALModelManager().getInputFolder(calFile);
        return (IFolder) container.getProgramFolder(moduleResourceLocator, false).getResource();
    }

    /**
     * Helper method to build up the names of modules to compile.
     * @param calFiles the set of cal files to analyze.
     * @param definitionGroup the current module source definition group.
     * @param namesOfModulesToCompileList the list of modules to populate as affected.  
     *   Names of modules from the first argument will be added to this list if they are in the source definition group.
     */
    private static void getAffectedModuleSourceFiles(Set<IFile> calFiles, ModuleSourceDefinitionGroup definitionGroup, List<ModuleName> namesOfModulesToCompileList) {
        for (final IFile calFile : calFiles) {

            ModuleName moduleName = Util.getModuleNameFromStorage(calFile);

            if (moduleName != null) {

                // Check that the module source is actually in the module source definition group.
                // It may not be, if there is another module source with the same name in a dependee project.
                if (definitionGroup.getModuleSource(moduleName) != null) {
                    namesOfModulesToCompileList.add(moduleName);
                }
            } else {
                // the storage name doesn't correspond to a module name
            }
        }
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    protected void clean(IProgressMonitor monitor) throws CoreException {
        IProject currentProject = getProject();
        if (currentProject == null || !currentProject.isAccessible()) {
            return;
        }
        if (DEBUG) {
            Util.printlnWithDate("\nCleaning " + currentProject.getName()); //$NON-NLS-1$
        }

        monitor.beginTask("", TICKS_PER_MONITOR);
        try {
            Util.checkCanceled(monitor);
            if (DEBUG) {
                System.out.println("Clearing last state as part of clean : " + lastState); //$NON-NLS-1$
            }
            if (lastState != null) {
                ProgramModelManager programModelManager = CALModelManager.getCALModelManager().getProgramModelManager();
                ModuleSourceDefinitionGroup moduleSourceDefinitionGroup = lastState.getModuleSourceDefinitionGroup();
                for (int i = 0; i < moduleSourceDefinitionGroup.getNModules(); i++) {
                    ModuleName moduleName = moduleSourceDefinitionGroup.getModuleSource(i).getModuleName();
                    programModelManager.removeModule(moduleName);
                }
            }

            clearLastState();
            removeProblemsAndTasksFor(currentProject);
            
            monitor.subTask(Messages.build_cleaningOutput);
            cleanOutputFolders(getSubMonitor(monitor, 1.0));
             
            CALModelManager.getCALModelManager().clearClassLoader(currentProject);
            CALModelManager.getCALModelManager().clearCachedResourceContainers(currentProject);
        
        } catch (CoreException e) {
            Util.log(e, "CALBuilder handling CoreException while cleaning: " + currentProject.getName()); //$NON-NLS-1$
            IMarker marker = currentProject.createMarker(CALModelMarker.CAL_MODEL_PROBLEM_MARKER);
            marker.setAttribute(IMarker.MESSAGE, Messages.bind(Messages.build_inconsistentProject, e.getLocalizedMessage()));
            marker.setAttribute(IMarker.SEVERITY, IMarker.SEVERITY_ERROR);
        
        } finally {
            monitor.done();
            cleanup();
        }
        
        if (DEBUG) {
            Util.printlnWithDate("Finished cleaning " + currentProject.getName()); //$NON-NLS-1$
        }
    }
    
    /**
     * @return the output folders for the builder's project.
     */
    private IFolder[] getOutputFolders() {
        return CALModelManager.getCALModelManager().getOutputFolders(Util.getCalProject(getProject()));
    }
    
    /**
     * Clean up the project's output folder(s).
     * @throws CoreException
     */
    private void cleanOutputFolders(IProgressMonitor monitor) throws CoreException {
        
        boolean cleanOutputFolders = CoreOptionIDs.CLEAN.equals(
                CALEclipseCorePlugin.getOption(CoreOptionIDs.CORE_JAVA_BUILD_CLEAN_OUTPUT_FOLDER));
        if (!cleanOutputFolders) {
            return;
        }
        
        IContainer[] outputFolders = getOutputFolders();
        int nOutputfolders = outputFolders.length;
        monitor.beginTask("Cleaning output folders", nOutputfolders);
        
        try {
            for (int i = 0; i < nOutputfolders; i++) {
                IContainer outputFolder = outputFolders[i];
                
                // TODOEL: the output folder should already exist.
                if (!outputFolder.exists()) {
                    continue;
                }
                
                IResource[] members = outputFolder.members();
                for (final IResource member : members) {
                    if (!member.isDerived()) {
                        member.accept(new IResourceVisitor() {
                            
                            public boolean visit(IResource resource) throws CoreException {
                                resource.setDerived(true);
                                return resource.getType() != IResource.FILE;
                            }
                        });
                    }
                    member.delete(IResource.FORCE, null);
                }
                monitor.worked(1);
                Util.checkCanceled(monitor);
            }
        } finally {
            monitor.done();
        }
    }
    
    private void cleanup() {
//        this.nameEnvironment = null;
//        this.binaryLocationsPerProject = null;
//        this.lastState = null;
//        this.extraResourceFileFilters = null;
//        this.extraResourceFolderFilters = null;
    }

    private void clearLastState() {
        lastState = null;
//        JavaModelManager.getJavaModelManager().setLastBuiltState(currentProject, null);
    }

    /**
     * Find deltas in a project.
     * @param currentProject the project for which to find the deltas.
     * @return (Set of IResourceDelta) The set of deltas for the project.
     * Null if the project wasn't associated with the builder.
     * May also be empty.
     */
    private Set<IResourceDelta> findDeltas(IProject currentProject, IProgressMonitor monitor) {
        monitor.beginTask("", TICKS_PER_MONITOR);
        try {
            monitor.subTask(Messages.bind(Messages.build_readingDelta, currentProject.getName()));
            IResourceDelta delta = getDelta(currentProject);
            Set<IResourceDelta> deltas = new HashSet<IResourceDelta>(3);
            if (delta != null) {
                if (delta.getKind() != IResourceDelta.NO_CHANGE) {
                    if (DEBUG) {
                        System.out.println("Found source delta for: " + currentProject.getName()); //$NON-NLS-1$
                    }
                    deltas.add(delta);
                }
            } else {
                if (DEBUG) {
                    System.out.println("Missing delta for: " + currentProject.getName()); //$NON-NLS-1$
                }
                monitor.subTask(""); //$NON-NLS-1$
                return null;
            }

            monitor.subTask(""); //$NON-NLS-1$
            return deltas;
        
        } finally {
            monitor.done();
        }
    }

    /**
     * Return the list of projects for which it requires a resource delta. This builder's project
     * is implicitly included and need not be specified. Builders must re-specify the list 
     * of interesting projects every time they are run as this is not carried forward
     * beyond the next build. Missing projects should be specified but will be ignored until
     * they are added to the workspace.
     */
    private IProject[] getRequiredProjects(IProject currentProject, boolean includeBinaryPrerequisites) {
        // The Java builder has to consider binary resources on the class path from projects not included in project references.
        // Also consider missing projects.
        
        // For now, depends on all referenced projects.
        // We should really limit to projects with a cal nature.
        // If we try to institute something like classpaths, we can try to emulate behaviour of Java as above.
        // Perhaps first, we can try to calculate what the project dependencies are using the model?
        return CALModelManager.getDependeeProjects(currentProject);
    }

    /**
     * Pre-build check for whether we should attempt to build the current project.
     * @return whether a build should be attempted for the current project.
     * @throws CoreException
     */
    private boolean isWorthBuilding() throws CoreException {
        // The Java builder aborts if there are classpath errors..
        return true;
    }

    public static IMarker[] getProblemsFor(IResource resource) {
        try {
            if (resource != null && resource.exists()) {
                return resource.findMarkers(CALModelMarker.CAL_MODEL_PROBLEM_MARKER, false, IResource.DEPTH_INFINITE);
            }
        } catch (CoreException e) {
            // assume there are no problems
        }
        return new IMarker[0];
    }
    
    public static IMarker[] getTasksFor(IResource resource) {
        try {
            if (resource != null && resource.exists()) {
                return resource.findMarkers(CALModelMarker.TASK_MARKER, false, IResource.DEPTH_INFINITE);
            }
        } catch (CoreException e) {
            // assume there are no tasks
        }
        return new IMarker[0];
    }
    
    public static void removeProblemsFor(IResource resource) {
        try {
            if (resource != null && resource.exists()) {
                resource.deleteMarkers(CALModelMarker.CAL_MODEL_PROBLEM_MARKER, false, IResource.DEPTH_INFINITE);
            }
        } catch (CoreException e) {
            // assume there were no problems
        }
    }
    
    public static void removeTasksFor(IResource resource) {
        try {
            if (resource != null && resource.exists()) {
                resource.deleteMarkers(CALModelMarker.TASK_MARKER, false, IResource.DEPTH_INFINITE);
            }
        } catch (CoreException e) {
            // assume there were no problems
        }
    }
    
    public static void removeProblemsAndTasksFor(IResource resource) {
        try {
            if (resource != null && resource.exists()) {
                resource.deleteMarkers(CALModelMarker.CAL_MODEL_PROBLEM_MARKER, false, IResource.DEPTH_INFINITE);
                resource.deleteMarkers(CALModelMarker.TASK_MARKER, false, IResource.DEPTH_INFINITE);
            }
        } catch (CoreException e) {
            // assume there were no problems
        }
    }

    /**
     * String representation for debugging purposes
     */
    @Override
    public String toString() {
        IProject currentProject = getProject();
        return currentProject == null ? 
                "CALBuilder for unknown project" :              //$NON-NLS-1$
                "CALBuilder for " + currentProject.getName();   //$NON-NLS-1$
    }
    
    public static void addListener(BuildMessagesListener listener){
        synchronized(listeners){
            listeners.add(listener);
        }
    }

    public static void removeListener(BuildMessagesListener listener){
        synchronized(listeners){
            listeners.remove(listener);
        }
    }
    
    public static void notify(Collection<ModuleName> wasCompiled){
        final List<BuildMessagesListener> listenersCopy;
        synchronized(listeners){
            listenersCopy = new ArrayList<BuildMessagesListener>(listeners);            
        }
        
        final Collection<ModuleName> unmodifiableModulesWithErrors = Collections.unmodifiableCollection(wasCompiled);
        for (final BuildMessagesListener listener : listenersCopy) {
            listener.notify(unmodifiableModulesWithErrors);
        }
    }
    
    /**
     * Called with a list of modules that have been compiled by the builder.
     * 
     * @author Greg McClement
     */
    public interface BuildMessagesListener{
        public void notify(Collection<ModuleName> modulesWithErrors);
    }
    
    /**
     * A quick fix for a given compiler error.
     */
    public interface IQuickFix{
        public SourcePosition applyFix(final int startLine, final int startColumn, CompilerMessageLogger messageLogger);
        public String getDescription();
    }

    /**
     * Create a quick fix that will import the given symbol into the given module.
     */
    private static IQuickFix createImportQuickFix(final QualifiedName importSymbol, final SourceIdentifier.Category category, final ModuleName moduleName, final SourceManagerFactory smf){        
        return new IQuickFix(){            
            public SourcePosition applyFix(final int startLine, final int startColumn, CompilerMessageLogger messageLogger){
                Refactorer.InsertImport refactorer = new Refactorer.InsertImport_Symbol(
                        CALModelManager.getCALModelManager().getModuleContainer(smf),
                        moduleName,
                        importSymbol,
                        category, 
                        startLine,
                        startColumn);
                
                refactorer.calculateModifications(messageLogger);
                if (messageLogger.getNErrors() == 0){
                    refactorer.apply(messageLogger);
                }
                return refactorer.getNewSourcePosition();
            }
            
            public String getDescription(){
                return Messages.bind(Messages.quickFix_importThisModule, importSymbol);
            }
        };
    }

    /**
     * Create a quick fix that will import one module into another module.
     */
    private static IQuickFix createImportOnlyQuickFix(final ModuleName importModule, final ModuleName moduleName, final SourceManagerFactory smf){        
        return new IQuickFix(){            
            public SourcePosition applyFix(final int startLine, final int startColumn, CompilerMessageLogger messageLogger){
                Refactorer.InsertImport refactorer = new Refactorer.InsertImport_Only(
                        CALModelManager.getCALModelManager().getModuleContainer(smf),
                        moduleName,
                        importModule,
                        startLine,
                        startColumn);

                refactorer.calculateModifications(messageLogger);
                if (messageLogger.getNErrors() == 0){
                    refactorer.apply(messageLogger);
                }
                return refactorer.getNewSourcePosition();
            }
            
            public String getDescription(){
                return Messages.bind(Messages.quickFix_importThisModule, importModule);
            }
        };
    }

    /**
     * Create a quick fix that will qualify the given name. This does not use the ractorer and instead
     * directly modifies the source. This is better because the undo is able to function propertly when 
     * just the effected code is modified.
     */
    private static IQuickFix createQualifyNameQuickFixDirect(final ModuleName moduleName, final String oldText, final String newText, final int startIndex, final int endIndex, final SourceManagerFactory smf){        
        final ModuleContainer moduleContainer = CALModelManager.getCALModelManager().getModuleContainer(smf);
        return new IQuickFix(){            
            public SourcePosition applyFix(final int startLine, final int startColumn, CompilerMessageLogger messageLogger){
                final ISourceManager sourceManager = moduleContainer.getSourceManager(moduleName);
                if (sourceManager instanceof ISourceManager2){
                    ISourceManager2 sourceManager2 = (ISourceManager2) sourceManager;
                    sourceManager2.saveSource(moduleName, startIndex, endIndex, newText);
                }
                return null;
            }
            
            public String getDescription(){
                return Messages.bind(Messages.quickFix_fullyQualifyName, newText);
            }
        };
    }
    
    /**
     * Get quick fixes for the given compile error.
     * 
     * TODO Fix this hack after Joseph fixes some back end stuff
     * @param marker the marker of the problem to quickfix
     * @param moduleName the module of the module where the quickfix resides
     * @param smf SourceManagerFactory
     * @return list of quick fixes appropriate at the marker
     */
    public static IQuickFix[] getQuickFixes(IMarker marker, ModuleName moduleName, SourceManagerFactory smf) {
        String errorMessage = marker.getAttribute(IMarker.MESSAGE, "");

        SourceIdentifier.Category category = null;

        if (errorMessage.indexOf("Attempt to use undefined type") != -1){
            category = SourceIdentifier.Category.TYPE_CONSTRUCTOR;
        }
        else if (errorMessage.indexOf("Attempt to use undefined data constructor") != -1){
            category = SourceIdentifier.Category.DATA_CONSTRUCTOR;
        }
        else if (errorMessage.indexOf("Attempt to use undefined class") != -1){
            category = SourceIdentifier.Category.TYPE_CLASS;
        }
        else if (errorMessage.indexOf("Attempt to use undefined function") != -1){
            category = SourceIdentifier.Category.TOP_LEVEL_FUNCTION_OR_CLASS_METHOD;
        }

        final int startCharFromOne = marker.getAttribute(IMarker.CHAR_START, -1) + 1;
        final int endCharFromOne = marker.getAttribute(IMarker.CHAR_END, -1) + 1;
                
        // The error lists multiple possible imports.
        {
            String searchKey = "Was one of these intended: ";
            int beginIndex = errorMessage.indexOf(searchKey);
            if (beginIndex != -1){
                final String oldText = getSymbolName(errorMessage);
                String rest = errorMessage.substring(beginIndex + searchKey.length());
                rest = rest.replaceAll("\\?", "");
                String alternatives[] = rest.split(", ");
                IQuickFix[] qf = new IQuickFix[alternatives.length * 2];
                for(int i = 0; i < alternatives.length; ++i){
                    QualifiedName qualifiedName = QualifiedName.makeFromCompoundName(alternatives[i]);
                    qf[i] = createImportQuickFix(qualifiedName, category, moduleName, smf);
                    qf[i + alternatives.length] = createQualifyNameQuickFixDirect(moduleName, oldText, qualifiedName.toSourceText(), startCharFromOne, endCharFromOne, smf);
                }
                return qf;
            }
        }
        
        {
            // The error looks like this:
            //    Attempt to use undefined function 'zip4'. Was 'Cal.Collections.List.zip4' intended?
            final String searchKey1 = "Attempt to use undefined ";
            final CALModelManager cmm = CALModelManager.getCALModelManager();
            final ModuleTypeInfo moduleTypeInfo = cmm.getModuleTypeInfo(moduleName);
            final UnqualifiedIfUsingOrSameModule namingProvider; 
            if (moduleTypeInfo != null) {
                namingProvider = new UnqualifiedIfUsingOrSameModule(moduleTypeInfo);
            } else {
                namingProvider = null;
            }
            int beginIndex = errorMessage.indexOf(searchKey1);
            if (beginIndex != -1){
                final String oldText = getSymbolName(errorMessage);
                String searchKey2 = "Was '";
                beginIndex = errorMessage.indexOf("Was '", beginIndex);
                if (beginIndex != -1){
                    beginIndex += searchKey2.length();
                    int endIndex = errorMessage.indexOf("'", beginIndex);
                    String importSymbol = errorMessage.substring(beginIndex, endIndex);
                    IQuickFix[] qf = new IQuickFix[2];
                    final QualifiedName qualifiedName = QualifiedName.makeFromCompoundName(importSymbol);                    
                    qf[0] = createImportQuickFix(qualifiedName, category, moduleName, smf);
                    String name = qualifiedName.toSourceText();
                    if (namingProvider != null){
                        ScopedEntity scopedEntity = getScopedEntity(qualifiedName, category);
                        if (scopedEntity != null){
                            name = namingProvider.getName(scopedEntity);
                        }
                    }
                    qf[1] = createQualifyNameQuickFixDirect(moduleName, oldText, name, startCharFromOne, endCharFromOne, smf);
                    return qf;
                } else {
                    // no suggestions so look in the workspace for possible matches
                    final ProgramModelManager programModelManager = cmm.getProgramModelManager();
                    final ModuleName[] moduleNames = programModelManager.getModuleNamesInProgram();
                    final ArrayList<IQuickFix> quickFixes = new ArrayList<IQuickFix>();
                    for (int i = 0; i < moduleNames.length; ++i) {
                        final ModuleTypeInfo mti = cmm.getModuleTypeInfo(moduleNames[i]);
                        if (mti == null) {
                            continue;
                        }
                        
                        List<ScopedEntity> choices = new ArrayList<ScopedEntity>();
                        // the category can be wrong for these kinds of errors
                        {
                            final ScopedEntity se = mti.getFunctionalAgent(oldText);
                            if (se != null &&
                            		(category == null || category == SourceIdentifier.Category.TOP_LEVEL_FUNCTION_OR_CLASS_METHOD)
                            		){
                                category = SourceIdentifier.Category.TOP_LEVEL_FUNCTION_OR_CLASS_METHOD;
                                final Scope scope = se.getScope();
                                if (scope.isPublic() || (scope.isProtected() && mti.hasFriendModule(moduleName))){
                                    choices.add(se);
                                }
                            }
                        }
                        
                        {
                            final ScopedEntity se = mti.getDataConstructor(oldText);
                            if (se != null &&
                            		(category == null || category == SourceIdentifier.Category.DATA_CONSTRUCTOR)
                            		){
                                category = SourceIdentifier.Category.DATA_CONSTRUCTOR;
                                final Scope scope = se.getScope();
                                if (scope.isPublic() || (scope.isProtected() && mti.hasFriendModule(moduleName))){
                                    choices.add(se);
                                }
                            }
                        }

                        {
                            final ScopedEntity se = mti.getTypeConstructor(oldText);
                            if (se != null &&
                            		(category == null || category == SourceIdentifier.Category.TYPE_CONSTRUCTOR)
                            		){
                                category = SourceIdentifier.Category.TYPE_CONSTRUCTOR;
                                final Scope scope = se.getScope();
                                if (scope.isPublic() || (scope.isProtected() && mti.hasFriendModule(moduleName))){
                                    choices.add(se);
                                }
                            }
                        }

                        {
                            final ScopedEntity se = mti.getTypeClass(oldText);
                            if (se != null &&
                            		(category == null || category == SourceIdentifier.Category.TYPE_CLASS)
                            		){
                                category = SourceIdentifier.Category.TYPE_CLASS;
                                final Scope scope = se.getScope();
                                if (scope.isPublic() || (scope.isProtected() && mti.hasFriendModule(moduleName))){
                                    choices.add(se);
                                }
                            }
                        }

                        if (!choices.isEmpty()){
                            final ModuleName thisModuleName = moduleName;

                            // Should not return a jar entry because all jar entries are read-only.
                            // since we are doing quick-fixes here, should not be read-only.
                            // However, to be safe, check to make sure it is an IFile.
                            IStorage storage = cmm.getInputSourceFile(thisModuleName);
                            if (! (storage instanceof IFile)) {
                                Util.log("Attempt to get quick fixes for a read-only file: " + storage.getName());
                                return new IQuickFix[0];
                            }
                            final IFile thisFile = (IFile) storage;
                            
                            IProject thisProject = null;
                            ArrayList<IProject> dependeeProjects = new ArrayList<IProject>();
                            if (thisFile != null){
                                thisProject = thisFile.getProject();
                                IProject[] projects = CALModelManager.getDependeeProjects(thisProject);
                                dependeeProjects.add(thisProject);
                                for (final IProject iProject : projects) {
                                    dependeeProjects.add(iProject);    
                                }                   
                            }
                            for (final ScopedEntity choice : choices) {
                                final ModuleName choiceModuleName = choice.getName().getModuleName();
                                // Only show quick fixes or symbols in projects that are referenced
                                {
                                    
                                    // check that the current project depends on the project containing
                                    // the import symbol.
                                    if (thisProject != null){
                                        // ADE get the storage object, not the file
                                        IProject project = cmm.getInputSourceFileContainer(choiceModuleName)
                                                .getPackageRoot().getJavaProject().getProject();
                                        if (project == null || !dependeeProjects.contains(project)){
                                            continue; // skip this one
                                        }
                                    }

                                    // Make sure that a circular dependencies would not be introduced
                                    {
                                        ModuleTypeInfo moduleTypeInfoToCheck = cmm.getModuleTypeInfo(choiceModuleName);
                                        if (null != moduleTypeInfoToCheck.getDependeeModuleTypeInfo(thisModuleName)){
                                            // skip this otherwise there will be circular module dependencies.
                                            continue;
                                        }
                                    }
                                }
                                
                                QualifiedName qualifiedName = choice.getName();
                                quickFixes.add(createImportQuickFix(choice.getName(), category, moduleName, smf));

                                String name = qualifiedName.toSourceText();
                                if (namingProvider != null){
                                    ScopedEntity scopedEntity = getScopedEntity(qualifiedName, category);
                                    if (scopedEntity != null){
                                        name = namingProvider.getName(scopedEntity);
                                    }
                                    else{
                                        name = namingProvider.getName(choice);
                                    }
                                }                                
                                quickFixes.add(createQualifyNameQuickFixDirect(moduleName, oldText, name, startCharFromOne, endCharFromOne, smf));
                            }
                        }                        
                    }
                    return quickFixes.toArray(new IQuickFix[quickFixes.size()]);
                }
            }
        }
        
        // Quick fix for when the module name is not qualified enough in the import statement
        //
        // Unresolved external module import Math. Did you mean Cal.Utilities.Math?

        if (errorMessage.indexOf("Unresolved external module import ") != -1){
            final String secondSentenceStart = "Did you mean ";
            final int secondSentenceStartIndex = errorMessage.indexOf(secondSentenceStart);
            if (secondSentenceStartIndex != -1){
                final int questionMarkIndex = errorMessage.lastIndexOf('?');
                if (questionMarkIndex != -1){
                    final int startOfQualifiedName = secondSentenceStartIndex + secondSentenceStart.length();
                    final String qualifiedName = errorMessage.substring(startOfQualifiedName, questionMarkIndex);
                    final int periodInFirstSentence = errorMessage.lastIndexOf('.', secondSentenceStartIndex);
                    final int startOfUnqualifiedNameIndex = errorMessage.lastIndexOf(' ', periodInFirstSentence) + 1;
                    final String problemName = errorMessage.substring(startOfUnqualifiedNameIndex, periodInFirstSentence);
                    IQuickFix[] qf = new IQuickFix[1];
                    qf[0] = createQualifyNameQuickFixDirect(moduleName, problemName, qualifiedName, startCharFromOne, endCharFromOne, smf);
                    return qf;
                }
            }            
        }

        if (errorMessage.indexOf("Unexpected token '='.  Was the equality operator ('==') intended?") != -1){
            IQuickFix[] qf = new IQuickFix[1];
            qf[0] = createQualifyNameQuickFixDirect(moduleName, "=", "==", startCharFromOne, endCharFromOne, smf);
            return qf;
        }

        // Error looks like this
        //
        //      The module Frnd has not been imported into T234.

        {
            String searchFor = " has not been imported into";
            if (errorMessage.indexOf(searchFor) != -1){
                final String prefix = "The module ";
                final int startOfModuleName = prefix.length();
                final int endOfModuleName = errorMessage.indexOf(searchFor);
                final String missingModuleNameString = errorMessage.substring(startOfModuleName, endOfModuleName);
                final ModuleName missingModuleName = ModuleName.make(missingModuleNameString);
                // The module name in the error does not necessarily have the hierarchical
                // components so I have to look for a match in the workspace. This is written
                // to get around that the module has not compiled properly.
                final Set<ModuleName> matches = getMatchingModules(missingModuleName);
                final ArrayList<IQuickFix> quickFixes = new ArrayList<IQuickFix>(matches.size());
                for(ModuleName match : matches){
                    quickFixes.add(createImportOnlyQuickFix(match, moduleName, smf));
                }
                return quickFixes.toArray(new IQuickFix[quickFixes.size()]);
            }
        }
        
        return new IQuickFix[0];
    }

    /**
     * Returns modules from the workspace the potentially match the given module name. This is used
     * instead of the module name resolved because potentially the module is not compiled properly.
     * @param moduleName
     * @return Set of module names which potentially match.
     */
    private static Set<ModuleName> getMatchingModules(ModuleName moduleName){
        final Set<ModuleName> matches = new HashSet<ModuleName>();
        final Set<ModuleName> allModules = CALModelManager.getCALModelManager().getModuleNames();
        for(ModuleName targetModuleName : allModules){
            final String[] targetComponents = targetModuleName.getComponents();
            final String[] components = moduleName.getComponents();
            if (components.length > targetComponents.length){
                continue;
            }
            boolean itMatches = true;
            int iTC = targetComponents.length - 1;
            for(int i = components.length - 1; i >= 0; --i, --iTC){
                if (!components[i].equals(targetComponents[iTC])){
                    itMatches = false;
                    break;
                }
            }
            if (itMatches){
                matches.add(targetModuleName);
            }
        }
        if (matches.size() == 0){
            // This is the best guess then
            matches.add(moduleName);
        }
        return matches;
    }

    private static String getSymbolName(String errorMessage) {
        // Attempt to use undefined data constructor 'Duder'
        //
        // Find the name in the single quotes
        //
        // An example error message is 
        //
        //      "Attempt to use undefined function 'arbitrary'. Was 'Cal.Utilities.QuickCheck.arbitrary' intended?"
        //

        final int startOfName = errorMessage.indexOf('\'', 0) + 1;
        final int endOfName = errorMessage.indexOf('\'', startOfName);
        return errorMessage.substring(startOfName, endOfName);                
    }

    /**
     * Get the scoped entity with the given name and type.
     * @param qualifiedName the name of the scoped entity
     * @param category the category of the scoped entity
     * @return the scoped entity with the given name and category
     */
    private static ScopedEntity getScopedEntity(QualifiedName qualifiedName, Category category){
        final ModuleTypeInfo moduleTypeInfo = CALModelManager.getCALModelManager().getModuleTypeInfo(qualifiedName.getModuleName());
        String unqualifiedName = qualifiedName.getUnqualifiedName();
        if (category == SourceIdentifier.Category.TOP_LEVEL_FUNCTION_OR_CLASS_METHOD){
            ScopedEntity scopedEntity = moduleTypeInfo.getFunction(unqualifiedName);
            if (scopedEntity == null){
                // maybe its a class
                return moduleTypeInfo.getClassMethod(unqualifiedName);
            }
            return scopedEntity;
        }
        else if (category == SourceIdentifier.Category.TYPE_CLASS){
            return moduleTypeInfo.getTypeClass(unqualifiedName);
        }
        else if (category == SourceIdentifier.Category.TYPE_CONSTRUCTOR){
            return moduleTypeInfo.getTypeConstructor(unqualifiedName);
        }
        else if (category == SourceIdentifier.Category.DATA_CONSTRUCTOR){
            return moduleTypeInfo.getDataConstructor(unqualifiedName);
        }
        else{
            return null;
        }
    }
    
    /**
     * Get quick fixes for the given compile error.
     * 
     * TODO Fix this hack after Joseph fixes some back end stuff
     * @param marker the marker to examine
     * @return true if this marker can be quick fixed, false otherwise
     */
    public static boolean canFix(IMarker marker) {
        String errorMessage = marker.getAttribute(IMarker.MESSAGE, "");

        // The error lists multiple possible imports.
        {
            String searchKey = "Was one of these intended: ";
            int beginIndex = errorMessage.indexOf(searchKey);
            if (beginIndex != -1){
                String rest = errorMessage.substring(beginIndex + searchKey.length());
                rest = rest.replaceAll("\\?", "");
                String alternatives[] = rest.split(", ");
                return alternatives.length > 0;
            }
        }
        
        {
            // The error looks like this:
            //    Attempt to use undefined function 'zip4'. Was 'Cal.Collections.List.zip4' intended?
            String searchKey1 = "Attempt to use undefined ";
            int beginIndex = errorMessage.indexOf(searchKey1);
            if (beginIndex != -1){
                return true;
//                String searchKey2 = "Was '";
//                beginIndex = errorMessage.indexOf("Was '", beginIndex);
//                if (beginIndex != -1){
//                    beginIndex += searchKey2.length();
//                    int endIndex = errorMessage.indexOf("'", beginIndex);
//                    String importSymbol = errorMessage.substring(beginIndex, endIndex);
//                    return importSymbol.length() > 0;
//                }
            }
        }

        // Error looks like this
        //
        //    Unresolved external module import Math. Did you mean Cal.Utilities.Math?

        {
            if (errorMessage.indexOf("Unresolved external module import ") != -1){
                final String secondSentenceStart = "Did you mean ";
                final int secondSentenceStartIndex = errorMessage.indexOf(secondSentenceStart);
                if (secondSentenceStartIndex != -1){
                    return true;
                }
            }
        }
        
        // Error looks like this
        //
        //   Unexpected token '='.  Was the equality operator ('==') intended? 

        if (errorMessage.indexOf("Unexpected token '='.  Was the equality operator ('==') intended?") != -1){
            return true;
        }
                
        // Error looks like this
        //
        //      The module Frnd has not been imported into T234.

        if (errorMessage.indexOf("has not been imported into") != -1){
            return true;
        }

        return false;        
    }
    
}
