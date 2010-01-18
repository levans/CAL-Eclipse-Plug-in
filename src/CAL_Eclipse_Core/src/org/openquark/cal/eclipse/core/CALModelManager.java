/*******************************************************************************
 * Copyright (c) 2005 Business Objects Software Limited and others.
 * All rights reserved. 
 * This file is made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Business Objects Software Limited - initial API and implementation based on Eclipse 3.1.2 code for
 *                             /org.eclipse.jdt.core/model/org/eclipse/jdt/internal/core/JavaModelManager.java
 *                           Eclipse source is available at: http://www.eclipse.org/downloads/
 *******************************************************************************/

/*
 * CALModelManager.java
 * Creation date: Nov 3, 2005.
 * By: Edward Lam
 */
package org.openquark.cal.eclipse.core;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.net.URLClassLoader;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.WeakHashMap;

import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IProjectDescription;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceChangeEvent;
import org.eclipse.core.resources.IResourceChangeListener;
import org.eclipse.core.resources.IResourceDelta;
import org.eclipse.core.resources.IResourceDeltaVisitor;
import org.eclipse.core.resources.ISaveContext;
import org.eclipse.core.resources.ISaveParticipant;
import org.eclipse.core.resources.ISavedState;
import org.eclipse.core.resources.IStorage;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.IWorkspaceRunnable;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.resources.IWorkspace.ProjectOrder;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jdt.core.IClasspathEntry;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IPackageFragment;
import org.eclipse.jdt.core.IPackageFragmentRoot;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.JavaModelException;
import org.openquark.cal.compiler.CodeAnalyser;
import org.openquark.cal.compiler.CompilerMessageLogger;
import org.openquark.cal.compiler.FunctionalAgent;
import org.openquark.cal.compiler.ModuleContainer;
import org.openquark.cal.compiler.ModuleName;
import org.openquark.cal.compiler.ModuleSourceDefinition;
import org.openquark.cal.compiler.ModuleSourceDefinitionGroup;
import org.openquark.cal.compiler.ModuleTypeInfo;
import org.openquark.cal.compiler.ScopedEntity;
import org.openquark.cal.compiler.SearchManager;
import org.openquark.cal.compiler.SourceMetricsManager;
import org.openquark.cal.compiler.SourceModel;
import org.openquark.cal.compiler.SourceModelUtilities;
import org.openquark.cal.compiler.TypeChecker;
import org.openquark.cal.compiler.SourceModel.ModuleDefn;
import org.openquark.cal.eclipse.core.builder.CALResourceContainerFactory;
import org.openquark.cal.eclipse.core.builder.ICALResourceContainer;
import org.openquark.cal.eclipse.core.util.Messages;
import org.openquark.cal.eclipse.core.util.Util;
import org.openquark.cal.machine.Module;
import org.openquark.cal.machine.ProgramManager;
import org.openquark.cal.machine.ProgramResourceRepository;
import org.openquark.cal.metadata.ArgumentMetadata;
import org.openquark.cal.metadata.CALFeatureMetadata;
import org.openquark.cal.metadata.FunctionalAgentMetadata;
import org.openquark.cal.metadata.MetadataManager;
import org.openquark.cal.metadata.ScopedEntityMetadata;
import org.openquark.cal.runtime.MachineType;
import org.openquark.cal.runtime.ResourceAccess;
import org.openquark.cal.services.CALFeatureName;
import org.openquark.cal.services.CALSourcePathMapper;
import org.openquark.cal.services.LocalizedResourceName;
import org.openquark.cal.services.ProgramModelManager;
import org.openquark.cal.services.ResourceManager;
import org.openquark.cal.services.ResourcePath;
import org.openquark.cal.services.UserResourceFeatureName;
import org.openquark.cal.services.UserResourcePathMapper;
import org.openquark.cal.services.WorkspaceResource;
import org.openquark.util.Pair;


/**
 * The singleton <code>CALModelManager</code> manages a single instance of <code>CALModel</code>.
 * <code>IElementChangedListener</code>s register with the <code>CALModelManager</code>,
 * and receive <code>ElementChangedEvent</code>s for the <code>CALModel</code>.
 * <p>
 * The single instance of <code>CALModelManager</code> is available from
 * the static method <code>CALModelManager.getCALModelManager()</code>.
 * 
 * Modeled on org.eclipse.jdt.internal.core.JavaModelManager.
 * 
 * @author Edward Lam
 */
public class CALModelManager {
    
    public interface MetadataChangeListener {
        void metadataSaved (CALFeatureName featureName);
    }
    
    public static boolean DEBUG = false;

    /** 
     * Flag to control which classloader is the parent of the project classloader.
     * True to use the platform plugin's classloader.
     * False to use Java's extension classloader.
     */
    private static final boolean USE_PLATFORM_PLUGIN_LOADER_AS_PARENT_LOADER;
    static {
        // HACK - change parent loader depending on whether we are in a runtime or standalone Eclipse.
        // The classloader behaviour is different for these two cases.
        String osgiBundleProperty = System.getProperty("osgi.bundles");
        
        // In the standalone Eclipse case the bundles are simple names.
        // In the runtime case they are something like "reference:file:bundlename..."
        boolean runtimeEclipse = osgiBundleProperty != null && osgiBundleProperty.startsWith("reference:");

        // TODO - can't currently make this work in runtime Eclipse
        // setting to false results in CAL Console finding the wrong version of Prelude.id.
        // setting to true results in builder not being able to find log4j classes. 
        USE_PLATFORM_PLUGIN_LOADER_AS_PARENT_LOADER = !runtimeEclipse;
    }
    
    /** Bitmask representing flags on a project delta signifying that the project dependency graph may have changed. */
    private static final int projectDependencyResourceChangeFlags = IResourceDelta.OPEN | IResourceDelta.DESCRIPTION;
    private static final int projectDependencyKindChangeFlags = IResourceDelta.ADDED | IResourceDelta.REMOVED;
    
    // TODOEL: specify machine type using properties.
    // Use this to indicate a machine-specific program manager.
    
    /** The program machine type. */
    private final static MachineType machineType = MachineType.LECC;

    /**
     * The singleton manager instance.
     */
    private static final CALModelManager INSTANCE = new CALModelManager();
    
    /** The save participant instance for this manager. */
    private final SaveParticipant saveParticipant = new SaveParticipant();
    
    // Preferences
    private PluginConfiguration pluginConfiguration = null;     // initialized by startup()

    private final ProgramResourceRepository resourceRepository;
    private final ProgramModelManager programModelManager;
    private final MetadataManager metadataManager;
    // Note: look at the input folders..
    
    private final Set<MetadataChangeListener> metadataChangeListeners = new HashSet<MetadataChangeListener>();

    /** Map from moduleName to info about that module's backing resource.  Synchronized since queries and updates can happen on different threads. */
    private final Map<ModuleName, Pair<ICALResourceContainer, IStorage>> moduleNameToSourceFileInfoMap = 
        new Hashtable<ModuleName, Pair<ICALResourceContainer,IStorage>>();

    /** 
     * Map from project name (project.getElementName()) to any classloader to use to load classes for that project. 
     *  This map is populated by calls to getClassLoader(iProject). 
     */
    private final Map<String, ClassLoader> projectNameToClassloaderCacheMap = new HashMap<String, ClassLoader>();
    
    /*
     * Resource-dependent state.
     * These are updated by the resource change listener.
     */
    
    // TODOEL: What to do if there are knots, ...?
    private ProjectOrder projectOrder;
    
    private IStorage[] duplicateSourceFiles = null;
    
    /** Map from project to info for resources in the project with invalid source names.
     *  May not contain entries for every project in the model.  */
    private final Map<IProject, Set<Pair<IStorage, IPackageFragmentRoot>>> invalidResourceNameMap = 
        new HashMap<IProject, Set<Pair<IStorage, IPackageFragmentRoot>>>();
    
    private Map<IJavaProject, EclipseModuleSourceDefinitionGroup> projectToSourceDefinitionGroupMap = null;
    private EclipseModuleSourceDefinitionGroup globalModuleSourceDefinitionGroup = null;

    private final CALResourceContainerFactory containerFactory;
    
    private final Map<IJavaProject, ICALResourceContainer[]> projectToResourceContainers = 
        new WeakHashMap<IJavaProject, ICALResourceContainer[]>();
    
    /**
     * Used to cache the source string of a module.
     */
    private final Map<ModuleName, String> moduleNameToSource = new WeakHashMap<ModuleName, String>();
    /**
     * Used to cache the source model of a module. 
     */
    private final Map<ModuleName, ModuleDefn> moduleNameToSourceModel = 
            new WeakHashMap<ModuleName, ModuleDefn>();   
    
    
    
    
    /** Holds state during delta processing */
    private final IResourceChangeListener deltaState = new IResourceChangeListener() {
        
        /*
         * From JavaModelManager: 
         *   DeltaProcessingState -- used to hold state for DeltaProcessor (which it creates).
         */
        
        public void resourceChanged(IResourceChangeEvent event) {
            /*
             * TODOEL: Placeholder
             * See DeltaProcessingState.resourceChanged().
             */
        }
    };
    
    /**
     * this resource change listener listens for changes to the classpath and removes that project
     * from the input containers cache.
     */
    private final IResourceChangeListener classpathChangeListener = new IResourceChangeListener() {
        public void resourceChanged(IResourceChangeEvent event) {
            try {
                event.getDelta().accept(new IResourceDeltaVisitor() {
                    public boolean visit(IResourceDelta delta) throws CoreException {
                        if (delta.getResource().getType() == IResource.ROOT) {
                            return true;
                        } else if (delta.getResource().getType() == IResource.PROJECT) {
                            IJavaProject javaProject = Util.getCalProject((IProject) delta.getResource());
                            if (javaProject != null) {
                                IResourceDelta[] children = delta.getAffectedChildren();
                                for (final IResourceDelta child : children) {
                                    if (child.getResource().getName().equals(".classpath")) {
                                        projectToResourceContainers.remove(javaProject);
                                    }
                                }
                            }
                        }
                        return false;
                    } 
                });
            } catch (CoreException e) {
                Util.log(e, "Error searching through project for changes to classpath");
            }
        }  
    };
    
    
    /**
     * The save participant which handles workspace saves.
     * 
     * Note that save participants are the mechanism through which to receive any resource events which occur
     * between plugin shutdown and subsequent startup.
     * If resource changes are made when Eclipse is started up, but before this plugin is activated, processing of
     * saved state will still occur via the save participant.
     * 
     * @author Edward Lam
     */
    private class SaveParticipant implements ISaveParticipant {
        /**
         * {@inheritDoc}
         */
        public void doneSaving(ISaveContext context){
            // nothing to do
        }
        /**
         * {@inheritDoc}
         */
        public void prepareToSave(ISaveContext context) /*throws CoreException*/ {
            // nothing to do
        }
        /**
         * {@inheritDoc}
         */
        public void rollback(ISaveContext context){
            // nothing to do
        }

        /**
         * {@inheritDoc}
         */
        public void saving(ISaveContext context) throws CoreException {
            /*
             * Placeholder.
             * 
             * Update per-project info.
             * Save external lib timestamps.
             */
        }
    }
    
    /**
     * Allows the CAL runtime to access resources managed by Eclipse.
     */
    private class EclipseResourceAccess implements ResourceAccess {

        /**
         * {@inheritDoc}
         */
        public InputStream getUserResource(String moduleNameAsString, String name, String extension, Locale locale) {
            ModuleName moduleName = ModuleName.make(moduleNameAsString);
            UserResourceFeatureName featureName = new UserResourceFeatureName(moduleName, name, extension);
            LocalizedResourceName resourceName = new LocalizedResourceName(featureName, locale);
            final ResourcePath.FilePath resourcePath = UserResourcePathMapper.INSTANCE.getResourcePath(resourceName);

            IPath path = new Path(resourcePath.getPathString());
            IFile resourceFile = ((IContainer)getInputSourceFileContainer(moduleName).getPackageRoot().getResource()).getFile(path);

            try {
                return resourceFile.getContents();
            } catch (CoreException e) {
                String message = "Exception getting resource.  Module: " + moduleNameAsString + " name: " + name + " extension: " + extension + " locale: " + locale;
                Util.log(e, message);
            }
            return null;
            
        }
    }
    
    /**
     * A classloader to service requests for classes, when those requests come from code in a given project.
     * What we want is something like the EclipseClassLoader, which is the classloader for classes in Eclipse runtime bundles.
     * We can't use EclipseClassLoader itself, as it only deals with bundles.
     * <p>
     * The requirement is that a class can be loaded by at most one classloader.
     * So if there are two projects P1 and P2, and P1 refers to class C, only one classloader must ever load class C,
     *  regardless of whether P1 is a dependent or a dependee of P2, or whether P2 refers to C as well.
     * This is somewhat complicated by the fact that project dependencies form a graph, so for instance P1 can depend on 
     *  P2 and P3, and both P2 and P3 depend on P4, and any of those projects may refer to a given class.
     *  <p>
     * The way we solve this problem is to consistently use the project order returned by computeProjectOrder().
     * The algorithm is this:
     * <ol>
     *   <li> Ask the parent loader for the class.  Return if any.
     *      Parent loader = app or context classloader..
     *   <li> Iterate through direct and indirect dependee projects, in the order returned by computeProjectOrder().
     *      Ask the classloader for each in turn for the class, return the first one returned.
     * </ol>
     * @author Edward Lam
     */
    private static class ProjectClassLoader extends URLClassLoader {
        
        private static final URL[] EMPTY_URL_ARRAY = new URL[0];
        
        /** (List of IJavaProject) The java projects which are direct or indirect dependees of the project, plus the project itself. */
        private final List<IJavaProject> dependeeJavaProjects;
        
        /**
         * Factory method for this class.
         * 
         * @param projectOrder the project order returned by computeProjectOrder()
         * @param project the project for which the classes should be loaded.
         * @param parent the classloader to serve as the parent of this classloader
         *   This classloader will be asked for classes before any of the dependee project classloaders.
         * @return an instance of this class with the given args.
         */
        static ProjectClassLoader getClassLoader(IWorkspace.ProjectOrder projectOrder, IProject project, final ClassLoader parent) {
            
            Set<String> referencedProjectNameSet = getDependeeProjectNames(project);

            // Instantiate the list of dependee projects.
            // Note that this may be sized a little too big if there are non-java projects.
            final List<IJavaProject> dependeeJavaProjects = new ArrayList<IJavaProject>(referencedProjectNameSet.size());
            
            // Now populate the list with those projects which are java projects.
            //   Populate using project ordering
            IProject[] projects = projectOrder.projects;
            for (final IProject ithProject : projects) {
                // Check that this project is a dependee.
                if (referencedProjectNameSet.contains(ithProject.getName())) {
                    IJavaProject ithJavaProject = JavaCore.create(ithProject);
                    
                    // Check that this project is a java project.
                    if (ithProject.exists()) {
                        dependeeJavaProjects.add(ithJavaProject);
                    }
                }
            }

            IJavaProject javaProject = JavaCore.create(project);
            
            List<URL> urlList;
            if (javaProject != null && javaProject.exists()) {
                // Get the urls representing the classpath for the java project.
                urlList = getProjectClassPathURLs(javaProject);
            } else {
                // not a java project.
                urlList = Collections.emptyList();
            }
            
            final URL[] urls = urlList.toArray(EMPTY_URL_ARRAY);
            return AccessController.doPrivileged(new PrivilegedAction<ProjectClassLoader>() {
                public ProjectClassLoader run() {
                    return new ProjectClassLoader(dependeeJavaProjects, urls, parent);
                }
            });
        }
        
        /**
         * Constructor for a ProjectClassLoader.
         * Private - use factory method to instantiate.
         * 
         * @param dependeeJavaProjects (List of JavaProject) the JavaProjects on which the project depends, directly or indirectly.
         * @param urls the urls for the elements of the project's classpath.
         * @param parent the classloader to serve as the parent of this classloader
         */
        private ProjectClassLoader(List<IJavaProject> dependeeJavaProjects, URL[] urls, ClassLoader parent) {
            super(urls, parent);
            this.dependeeJavaProjects = dependeeJavaProjects;
        }
        
        /**
         * 
         * @param project a project in the workspace
         * @return (Set of String) the names of projects depended upon (directly or indirectly) by the project, including the name of the project itself.
         */
        private static final Set<String> getDependeeProjectNames(IProject project) {
            Set<String> dependeeProjectNameSet = new HashSet<String>();
            getDependeeProjectNamesHelper(project, dependeeProjectNameSet);
            return dependeeProjectNameSet;
        }
        
        /**
         * @param project a project in the workspace
         * @param dependeeProjectNameSet (Set of String) the accumulated set of names of projects traversed.
         */
        private static void getDependeeProjectNamesHelper(IProject project, Set<String> dependeeProjectNameSet) {
            // Check if project doesn't exist or isn't open.
            if (!project.isOpen()) {
                return;
            }
            
            String projectName = project.getName();
            
            // See if the project is already in the set of projects traversed.
            boolean alreadyTraversed = !dependeeProjectNameSet.add(projectName);
            if (alreadyTraversed) {
                return;
            }
            
            // Get the projects referenced by this project.
            IProject[] referencedProjects;
            try {
                referencedProjects = project.getReferencedProjects();
            } catch (CoreException e) {
                // Project doesn't exist or isn't open.
                // Shouldn't happen because of the check above.
                e.printStackTrace();
                return;
            }
            
            // Call recursively on referenced projects.
            for (final IProject referencedProject : referencedProjects) {
                getDependeeProjectNamesHelper(referencedProject, dependeeProjectNameSet);
            }
            
        }

        /**
         * {@inheritDoc}
         * Override to respect the implementation as described in the class comment.
         */
        @Override
        protected synchronized Class<?> loadClass(String name, boolean resolve) throws ClassNotFoundException {
            
            // First, check if the class has already been loaded
            Class<?> c = findLoadedClass(name);
            if (c == null) {
                try {
                    // Check the parent first.
                    c = getParent().loadClass(name);
                
                } catch (ClassNotFoundException e) {
                    
                    // Look in dependee projects.
                    for (final IJavaProject javaProject : dependeeJavaProjects) {
                        
                        // Have to cast in order to gain visibility for method findClass().
                        ProjectClassLoader dependeeClassLoader = (ProjectClassLoader)CALModelManager.getCALModelManager().getClassLoader(javaProject.getProject());
                        
                        try {
                            /*
                             * Call findClass() instead of loadClass()
                             * However, call findLoadedClass() first (since findClass() doesn't check whether the class is already loaded).
                             * - loadClass() will perform the search on its parents and dependees before asking itself.
                             * - findClass() will just search the urls in the classloader itself.
                             * 
                             * Note that there is still a small amount of inefficiency here, since some classpath elements may exist in multiple places.  
                             *   eg. P1 depends on P2, but both P1 and P2 have an entry on their classpath for foo.jar.  
                             *       foo.jar will end up in the lookup list for each classloader.
                             */
                            c = dependeeClassLoader.findLoadedClass(name);
                            if (c == null) {
                                c = dependeeClassLoader.findClass(name);
                            }
                            
                        } catch (ClassNotFoundException e2) {
                            // didn't find it in this project's classloader.
                            continue;
                        }
                        
                        // Found it in this project's classloader.
                        break;
                    }                                                                                        
                    
                    // If still not found, then invoke findClass() in this loader.
                    if (c == null) {
                        c = findClass(name);
                    }
                }
            }
            
            if (resolve) {
                resolveClass(c);
            }
            return c;
        }
        
        /**
         * {@inheritDoc}
         * Override to respect the implementation as described in the class comment.
         */
        @Override
        public URL getResource(String name) {
            // Check the parent first.
            URL url = getParent().getResource(name);
            
            if (url == null) {
                // Look in dependee projects.
                for (final IJavaProject javaProject : dependeeJavaProjects) {
                    
                    // Have to cast in order to gain visibility for method findClass().
                    ProjectClassLoader dependeeClassLoader = (ProjectClassLoader)CALModelManager.getCALModelManager().getClassLoader(javaProject.getProject());
                    
                    url = dependeeClassLoader.findResource(name);
                    
                    if (url != null) {
                        // found it.
                        break;
                    }
                }                                                                                        
            }
            
            return url;
        }

        /**
         * {@inheritDoc}
         * Override to respect the implementation as described in the class comment.
         */
        @Override
        public Enumeration<URL> getResources(String name) throws IOException {
            /*
             * Step 1: call getResources() on all the locations to be checked.
             */

            // The enumerations returned from all the locations to check.
            List<Enumeration<URL>> enumerations = new ArrayList<Enumeration<URL>>();

            enumerations.add(getParent().getResources(name));

            // Look in dependee projects.
            for (IJavaProject javaProject : dependeeJavaProjects) {
                // Have to cast in order to gain visibility for method findResources().
                ProjectClassLoader dependeeClassLoader = (ProjectClassLoader)CALModelManager.getCALModelManager().getClassLoader(javaProject.getProject());

                enumerations.add(dependeeClassLoader.findResources(name));
            }

            /*
             * Step 2: Add all the locations indicated to a set.
             *   This is necessary as some of the returned locations may be identical.
             */
            Set<URL> resourceSet = new LinkedHashSet<URL>();
            for (Enumeration<URL> enumeration : enumerations) {
                while (enumeration.hasMoreElements()) {
                    resourceSet.add(enumeration.nextElement());
                }
            }

            /*
             * Step 3: Return an enumeration on the set.
             */
            return Collections.enumeration(resourceSet);
        }
    }
    
    
    
    /**
     * Constructor for a CALModelManager.
     */
    CALModelManager() {
        ProgramManager programManager = ProgramManager.getProgramManager(machineType, EclipseProgramResourcePathRepository.getResourceRepositoryProvider(), new EclipseResourceAccess());
        this.programModelManager = new ProgramModelManager(programManager);
        this.resourceRepository = programManager.getProgramResourceRepository();
        
        metadataManager = new MetadataManager (new EclipseMetadataStore ());

        containerFactory = new CALResourceContainerFactory(machineType);
        
        computeProjectBuildOrder();
        updateSourceDefinitionInfo();
        
        // Add a listener to ensure that the build order stays up to date.
        ResourcesPlugin.getWorkspace().addResourceChangeListener(new IResourceChangeListener() {

            public void resourceChanged(IResourceChangeEvent event) {

                // According to the javadoc, the resource (event.getResource()) should be null for a POST_CHANGE
                
                IResourceDelta delta = event.getDelta();
                
                if (delta.getProjectRelativePath().isEmpty()) {
                    boolean recomputeProjectBuildOrder;
                    
                    if (delta.getFullPath().isRoot()) {
                        // The workspace root.
                        
                        // Child deltas are project-level deltas.
                        // See if there are any project deltas with the relevant flags.
                        recomputeProjectBuildOrder = false;
                        IResourceDelta[] affectedChildren = delta.getAffectedChildren();
                        for (final IResourceDelta affectedChild : affectedChildren) {
                            recomputeProjectBuildOrder |= handleProjectDelta(affectedChild);
                        }
                    
                    } else {
                        // The project root.
                        recomputeProjectBuildOrder = handleProjectDelta(delta);
                    }
                    
                    if (recomputeProjectBuildOrder) {
                        // delta path is empty for projects and the workspace root.
                        computeProjectBuildOrder();
                    }
                }
                
                updateSourceDefinitionInfo();
            }

        }, IResourceChangeEvent.POST_CHANGE);
    }
    
    /**
     * Handle a resource delta for a project resource.
     * @param projectDelta the delta for the project.
     * @return true if the delta is for a project deletion or closing, false otherwise.
     */
    private boolean handleProjectDelta(IResourceDelta projectDelta) {
        boolean recomputeProjectBuildOrder = (projectDelta.getFlags() & projectDependencyResourceChangeFlags) != 0;
        recomputeProjectBuildOrder |= (projectDelta.getKind() & projectDependencyKindChangeFlags) != 0;

        IProject project = (IProject)projectDelta.getResource();
        
        // The project is gone if the project is deleted or closed.
        boolean projectGone = (projectDelta.getKind() & IResourceDelta.REMOVED) != 0;
        projectGone |= ((projectDelta.getFlags() & IResourceDelta.OPEN) != 0 && !project.isOpen());

        if (projectGone) {
            ModuleSourceDefinitionGroup msdg = getModuleSourceDefinitionGroup(project).getWritableSubGroup();
            for (int i = 0; i < msdg.getNModules(); i++){
                ModuleSourceDefinition msd = msdg.getModuleSource(i);
                programModelManager.removeModule(msd.getModuleName());
            }
        }

        return recomputeProjectBuildOrder;
    }
    
    /**
     * The runnable which will process changes between when Eclipse was last closed and the activation of the bundle.
     * Originally implemented as an inline anonymous class, but we need to load the class on the bundle activation
     * thread in order to work around an Eclipse bug (see startup() method).
     * @author Edward Lam
     */
    private class SavedStateProcessor implements IWorkspaceRunnable {
        private final IWorkspace workspace;
        
        /**
         * Constructor for a SavedStateProcessor.
         * @param workspace 
         */
        public SavedStateProcessor(IWorkspace workspace) {
            this.workspace = workspace;
        }

        /**
         * {@inheritDoc}
         */
        public void run(IProgressMonitor progress) throws CoreException {
            ISavedState savedState = workspace.addSaveParticipant(CALEclipseCorePlugin.getDefault(), saveParticipant);
            if (savedState != null) {
                savedState.processResourceChangeEvents(CALModelManager.this.deltaState);
            }
        }
    }
    
    /**
     * Called by the CALEclipseCorePlugin.startup().
     * @throws CoreException
     */
    void startup() throws CoreException {
        try {
            pluginConfiguration = new PluginConfiguration();
            pluginConfiguration.startup();
            
            final IWorkspace workspace = ResourcesPlugin.getWorkspace();
            workspace.addResourceChangeListener(this.deltaState, IResourceChangeEvent.PRE_BUILD | IResourceChangeEvent.POST_BUILD | IResourceChangeEvent.POST_CHANGE
                    | IResourceChangeEvent.PRE_DELETE | IResourceChangeEvent.PRE_CLOSE);
            
            workspace.addResourceChangeListener(this.classpathChangeListener, IResourceChangeEvent.POST_CHANGE);
            
            // Attempt workaround for Eclipse bug: https://bugs.eclipse.org/bugs/show_bug.cgi?id=106812
            // Ensure that the SavedStateProcessor class is loaded on this thread.
            // If loaded on a different thread, a timeout is in effect -- if not loaded within 5 seconds, 
            //  the class is not loaded and workspace becomes unusable.
            // Note that this timeout seems to apply to any classes loaded on a different thread during plugin activation.
            try {
                getClass().getClassLoader().loadClass(SavedStateProcessor.class.getName());
            } catch (ClassNotFoundException e) {
                Util.log(e, e.getMessage());
            }
            
            // process deltas since last activated in indexer thread so that indexes are up-to-date.
            // see https://bugs.eclipse.org/bugs/show_bug.cgi?id=38658
            Job processSavedState = new Job(Messages.savedState_jobName) {

                @Override
                protected IStatus run(IProgressMonitor monitor) {
                    try {
                        // add save participant and process delta atomically
                        // see https://bugs.eclipse.org/bugs/show_bug.cgi?id=59937
                        workspace.run(new SavedStateProcessor(workspace), monitor);
                    
                    } catch (CoreException e) {
                        return e.getStatus();
                    }
                    return Status.OK_STATUS;
                }
            };
            processSavedState.setSystem(true);
            processSavedState.setPriority(Job.SHORT); // process asap
            processSavedState.schedule();
            
        } catch (RuntimeException e) {
            shutdown();
            throw e;
        }
    }

    /**
     * Called by the CALEclipseCorePlugin.shutdown().
     */
    void shutdown() {
        CALEclipseCorePlugin calEclipseCorePlugin = CALEclipseCorePlugin.getDefault();
        calEclipseCorePlugin.savePluginPreferences();
        pluginConfiguration.shutdown();
        IWorkspace workspace = ResourcesPlugin.getWorkspace();
        workspace.removeResourceChangeListener(this.deltaState);
        workspace.removeResourceChangeListener(classpathChangeListener);
        workspace.removeSaveParticipant(calEclipseCorePlugin);

        // wait for the initialization job to finish
        try {
            Job.getJobManager().join(CALEclipseCorePlugin.PLUGIN_ID, null);
        } catch (InterruptedException e) {
            // ignore
        }
    }
    
    /**
     * @return the singleton CALModelManager
     */
    public final static CALModelManager getCALModelManager() {
        return INSTANCE;
    }
    
    /**
     * @return the workspace root.
     */
    private static IWorkspaceRoot getWorkspaceRoot() {
        return ResourcesPlugin.getWorkspace().getRoot();
    }
    
    /**
     * @return the program model manager backing the cal model.
     */
    public ProgramModelManager getProgramModelManager() {
        return programModelManager;
    }
    
    /**
     * @param optionName the option name to register.
     */
    public void addOptionName(String optionName) {
        pluginConfiguration.addOptionName(optionName);
    }
    
    /**
     * Get the value of an option.
     * @param optionName the name of the option
     * @return the value of the option, or null if the option is not defined.
     */
    public String getOption(String optionName) {
        return pluginConfiguration.getOption(optionName);
    }
    
    /**
     * Get the options defined by this manager.
     * @return Map from option name to options.
     */
    public Hashtable<String, String> getOptions() {
        return pluginConfiguration.getOptions();
    }

    /**
     * @param project a project, or null to return the global source definition group (for all projects).
     * @return if null, the global source definition group.
     * Otherwise the source definition group for that project, or null if the project is not a cal project.
     */
    public EclipseModuleSourceDefinitionGroup getModuleSourceDefinitionGroup(IProject project) {
        IJavaProject javaProject = Util.getCalProject(project);
        if (javaProject == null) {
            return globalModuleSourceDefinitionGroup;
        }
        EclipseModuleSourceDefinitionGroup msdg = projectToSourceDefinitionGroupMap.get(javaProject);
        if (msdg == null){
            // This will hardly ever happen so just make a new empty one.
            msdg = new EclipseModuleSourceDefinitionGroup(
                    new ModuleSourceDefinition[0], new ModuleSourceDefinition[0]);
        }
        return msdg;
    }
    
    /** 
     * @param moduleName the module name to get the ModuleSourceDefinition for.
     * @return The ModuleSourceDefinition for the module with the given name or null if not found.
     */
    
    public ModuleSourceDefinition getModuleSourceDefinition(ModuleName moduleName){
        if (globalModuleSourceDefinitionGroup != null) {
            return globalModuleSourceDefinitionGroup.getModuleSource(moduleName);
        }
        return null;
    }

    public SourceModel.ModuleDefn getModuleSourceModel(ModuleName moduleName, boolean ignoreErrors, CompilerMessageLogger logger){
        SourceModel.ModuleDefn sourceModel;
        // read the cache
        synchronized(moduleNameToSourceModel){
            sourceModel = moduleNameToSourceModel.get(moduleName);
            if (sourceModel != null){
                // In the cache so leave early
                return sourceModel;
            }

            // not in the cache so get it the slow way
            String moduleSource = getModuleSource(moduleName);
            if (moduleSource.length() == 0){
                return null;
            }
            sourceModel = SourceModelUtilities.TextParsing.parseModuleDefnIntoSourceModel(moduleSource, ignoreErrors, logger);
            // updated the cache
            moduleNameToSourceModel.put(moduleName, sourceModel); 
        }
        return sourceModel;
    }

    public String getModuleSource(ModuleName moduleName){
        String source;
        // check the cache
        synchronized(moduleNameToSource){
            source = moduleNameToSource.get(moduleName);
            if (source != null){
                // in the cache so leave early
                return source;
            }

            // no in the cache so get it the slow way.
            source = ModuleContainer.readModuleSource(getModuleSourceDefinition(moduleName));
            // update the cache
            moduleNameToSource.put(moduleName, source);
        }
        return source;
    }
    
    /**
     * @return the names of modules currently in the model.
     * Note that if the model is in the process of being populated this info may get quickly out of date.
     */
    public Set<ModuleName> getModuleNames() {
        synchronized (moduleNameToSourceFileInfoMap) {
            return new HashSet<ModuleName>(moduleNameToSourceFileInfoMap.keySet());
        }
    }
    
    /**
     * @return the source definition files which are calculated to be duplicated.
     * ie. if .cal files with the same name exist in different projects.
     */
    public IStorage[] getDuplicateSourceFiles() {
        return duplicateSourceFiles.clone();
    }
    
    /**
     * @param project a project
     * @return the resources with invalid names in the project.
     */
    public Set<Pair<IStorage, IPackageFragmentRoot>> getResourcesWithInvalidNames(IProject project) {
        return invalidResourceNameMap.get(project);
    }
    
    /**
     * Get the projects on which a project depends - directly or indirectly.
     * @param project the project in question
     * @return the projects on which the project depends, not including the project itself
     */
    public static IProject[] getDependeeProjects(IProject project) {
        Set<IProject> projectSet = new HashSet<IProject>();
        getDependeeProjectsHelper(project, projectSet);
        projectSet.remove(project);
        return projectSet.toArray(new IProject[projectSet.size()]);
    }
    
    /**
     * Determines if one project depends on another
     * @param thisProject the project to check 
     * @param dependsOnThisProject the project that thisProject may depend on
     * @return true if thisProject references dependsOnThisProject.
     */
    public static boolean dependsOn(IProject thisProject, IProject dependsOnThisProject) {
        Set<IProject> projectSet = new HashSet<IProject>();
        getDependeeProjectsHelper(thisProject, projectSet);
        return projectSet.contains(dependsOnThisProject);
    }
    
    /**
     * Helper method for getDependeeProjects()
     * @param project the project for which to get dependees.
     * @param projectSet (Set of IProject) the accumulated set of dependee projects.
     */
    private static void getDependeeProjectsHelper(IProject project, Set<IProject> projectSet) {
        if (!project.isOpen()) {
            return;
        }
        
        // Add to the project set.
        if (!projectSet.add(project)) {
            return;
        }
        
        try {
            IProject[] referencedProjects = project.getReferencedProjects();
            for (final IProject referencedProject : referencedProjects) {
                getDependeeProjectsHelper(referencedProject, projectSet);
            }
        
        } catch (CoreException e) {
            // Doesn't exist or isn't open.
        	Util.log(e, e.getMessage());
        }
    }
    
    /**
     * Determines if one project depends on another statically. 
     * @param thisProject the project to check 
     * @param dependsOnThisProject the project that thisProject may depend on
     * @return true if thisProject references dependsOnThisProject.
     */
    public static boolean dependsOnStatically(IProject thisProject, IProject dependsOnThisProject) {
        return dependsOnStaticallyHelper(thisProject, dependsOnThisProject);
    }

    /**
     * Helper method for getDependeeProjects()
     * @param project the project for which to get dependees.
     * @param dependsOnThisProject the one that 'project' may depend on. 
     */
    private static boolean dependsOnStaticallyHelper(IProject project, IProject dependsOnThisProject) {
        if (!project.isOpen()) {
            return false;
        }
        
        try {
            IProjectDescription projDesc = project.getDescription();
            IProject[] referencedProjects = projDesc.getReferencedProjects();
            Set<IProject> dynamicProjects = new HashSet<IProject>();
            for (final IProject dynamicProject : projDesc.getDynamicReferences()) {
                dynamicProjects.add(dynamicProject);
            }
            for (final IProject referencedProject : referencedProjects) {
                if (dynamicProjects.contains(referencedProject)){
                    // project is dynamic so don't consider it.
                }
                else{
                    if (referencedProject.equals(dependsOnThisProject)){
                        return true;
                    }
                    
                    if (dependsOnStaticallyHelper(referencedProject, dependsOnThisProject)){
                        return true;
                    }
                }                
            }
            return false;        
        } catch (CoreException e) {
            // Doesn't exist or isn't open.
        	Util.log(e, e.getMessage());
        }
        return false;
    }
    
    /**
     * Update the project build order.
     * 
     * Computation of project order is expensive, so this should take place relatively rarely.
     * 
     * There are only a very limited set of changes to a workspace that could affect the project order: 
     * creating, renaming, or deleting a project; opening or closing a project; adding or removing a project reference.
     */
    private synchronized void computeProjectBuildOrder() {
        
        /*
         * Note that this method is synchronized in order to synchronize modifications to projectNameToClassLoaderMap.
         */
        ProjectOrder newProjectOrder = ResourcesPlugin.getWorkspace().computeProjectOrder(getWorkspaceRoot().getProjects());
        
        // TODOEL:
        //
        // It is possible that an add/remove/dependency change has occurred but hasn't been reconciled.
        //    In this case, the builder will:
        //    a) Compile current modules from existing .cmi files.
        //    b) Throw a resource change event for the unreconciled change.
        //    c) Call the builder again to bring build state up to date.
        //    
        //    The problem here is that calling (c) may not have caused modules in (a) to be recompoiled.
        //    Thus, we may end up with two different classloaders hanging around for the same project.
        // 
        // For now, we partially work around this by checking that the new project order is different from the old one.
        // I'm not sure if it's a full work-around, as it is difficult to get Eclipse into this situation.
        // Note that this solution is undesirable, as calculating the project order is relatively expensive.
        // Another problem is that a change in project dependencies will require all modules to be recompiled, 
        //   since the classloader cache will be cleared (and so classloaders will have to be regenerated).
        //
        if (this.projectOrder == null || !Arrays.equals(projectOrder.projects, newProjectOrder.projects)) {
            this.projectOrder = newProjectOrder;
            projectNameToClassloaderCacheMap.clear();
        }
    }
    
    /**
     * @param iProject the project for which a classloader should be obtained.
     * @return the classloader to use to load classes for that project.
     */
    public synchronized ClassLoader getClassLoader(IProject iProject) {
        /*
         * Note that this method is synchronized in order to synchronize modifications to projectNameToClassLoaderMap.
         */
        String projectName = iProject.getName();
        ClassLoader cachedClassLoader = projectNameToClassloaderCacheMap.get(projectName);
        if (cachedClassLoader != null) {
            return cachedClassLoader;
        }
        
        try {
            // A special classloader which overrides loadClass
            cachedClassLoader = ProjectClassLoader.getClassLoader(projectOrder, iProject, getParentLoader()); 
            projectNameToClassloaderCacheMap.put(projectName, cachedClassLoader);
            return cachedClassLoader;
        
        } catch (Throwable e) {
            Util.log(e, "Unable to create a classloader for the project"); //$NON-NLS-1$
        }
        
        return null;
    }
    
    /**
     * Clear the ProjectClassloader cached for retrieval by getClassLoader().
     * Note that other cached classloaders may also be cleared if they depend on the given project's classloader.
     * @param iProject a project whose classloader will be removed from the cache.
     */
    public synchronized void clearClassLoader(IProject iProject) {
        // Clear classloaders for projects which come after this one in the build order,
        //  since they will be invalidated because of the ProjectClassLoader consulting mechanism.
        IProject[] projects = projectOrder.projects;

        boolean removeProjectsWhileIterating = false;
        for (final IProject ithProject : projects) {
            if (ithProject == iProject) {
                removeProjectsWhileIterating = true;
            }
            
            if (removeProjectsWhileIterating) {
                projectNameToClassloaderCacheMap.remove(iProject.getName());
            }
        }
    }
    
    /**
     * @return the classloader to serve as the parent loader of the project classloaders.
     */
    private static ClassLoader getParentLoader() {
        if (USE_PLATFORM_PLUGIN_LOADER_AS_PARENT_LOADER) {
            /*
             * Return the platform plugin classloader.
             * 
             * ***** IMPORTANT LIMITATION ******
             * This classloader loads all classes in the plugin, plus all classes it depends on.
             * In particular, this includes:
             *   - all classes in the plugin, including the platform and utilities classes.
             *   - JRE classes
             * 
             * These may conflict with the equivalent classes if any in the workspace.
             * For instance, if a change is made to a platform or utilities class, it will not be visible to this classloader.
             * 
             * In practice this only affects those making changes in the implementation of CAL.
             * However it leads to some classloading inefficiency as the plugin and JRE classes appear twice in the classpath.
             */
            
            return MachineType.class.getClassLoader();

        } else {
        
            /*
             * Return the extension classloader.
             * 
             * ***** IMPORTANT LIMITATION ******
             * Note that this classloader is the classloader used to create the current jre's runtime classes.
             * This won't work if the runtime environment used to run Eclipse is different from the jdk on the project's classpath.
             * 
             * For instance:
             *   If we run Eclipse using java 5, but compile a project using Java 6, 
             *     we will have this classloader as a 5 classloader attempting to load Java 6 classes.
             *     This results in an UnsupportedClassVersionError.
             *   If we run Eclipse using java 6, but compile a project using Java 5, 
             *     we will have this classloader as a Java 6 classloader attempting to load Java 5 classes.
             *     Because of delegation, this classloader will be checked before the project classloaders.
             *     This classloader may return a class which exists in Java 6 but not in Java 5.
             */

            /*
             * NOTE:
             *   Does this only work for Sun jvms?
             *   For known Sun jvms, the system classloader hierarchy is:
             *     App classloader        - loads classes from the classpath.
             *     Ext classloader        - loads classes from the ext dirs.
             *     Bootstrap classloader  - loads java runtime classes.
             */
            ClassLoader systemClassLoader = ClassLoader.getSystemClassLoader();
            ClassLoader parent = systemClassLoader.getParent();
            if (parent != null) {
                return parent;
            }
            return systemClassLoader;
        }
    }
    
    /** 
     * Build the list of URLs for the given project 
     * Resolve container (eg. JRE jars) and dependencies and project output folder 
     *  
     * @param project the Java project
     * @return (List of URL) the urls comprising the project class path.
     */
    public static List<URL> getProjectClassPathURLs(IJavaProject project) {
        List<URL> classpathURLs = new ArrayList<URL>(); /* List of URL */
        try {
            // Add the default output location for the project.
            classpathURLs.add(getURLForWorkspacePath(project.getOutputLocation().addTrailingSeparator()));
            
            // configured classpath 
            // This seems not to get paths of ancestor projects.
            IClasspathEntry classpathEntries[] = project.getResolvedClasspath(true);
            
            for (final IClasspathEntry classpathEntry : classpathEntries) {
                if (classpathEntry.getEntryKind() == IClasspathEntry.CPE_LIBRARY) {
                    IPath libraryPath = classpathEntry.getPath();
                    if (libraryPath != null) {
                        // TODO/HACK(?): On unix, there is no way to determine whether the library path is a filesystem path or a workspace-relative path
//                        classpathURLs.add(getURLForWorkspacePath(libraryPath));
                        classpathURLs.add(getRawLocationURL(libraryPath));

                    } else {
                        Util.log(null, "Library not in workspace: " + classpathEntry.toString(), IStatus.INFO); //$NON-NLS-1$
                    }
                    
                } else if (classpathEntry.getEntryKind() == IClasspathEntry.CPE_SOURCE) {
                    // Source folder in the current project
                    // Add the corresponding output location if it's not the default location
                    
                    IPath outPath = classpathEntry.getOutputLocation();
                    if (outPath != null) {
                        // Non-default output location.
                        URL urlForWorkspacePath = getURLForWorkspacePath(outPath.addTrailingSeparator());
                        if (!classpathURLs.contains(urlForWorkspacePath)) {  // check for multiple source folders using the same output folder
                            classpathURLs.add(urlForWorkspacePath);
                        }

                    } else {
                        // This source path uses the default output location for the project. 
                        // Already added above (first statement of this try block).
                    }
                    
                } else if (classpathEntry.getEntryKind() == IClasspathEntry.CPE_PROJECT) {
                    // Another project.
                    // Shouldn't have to add anything since this classloader is supposed to find classes in referenced projects' classloaders first.

                    if (false) {
                        // add urls for that project's binary output
                        
                        // Also, get the source locations in the classpath for the project, and add the corresponding output locations.
                        IProject iProject = (IProject)ResourcesPlugin.getWorkspace().getRoot().findMember(classpathEntry.getPath());

                        // Note that since the project is on the classpath it must be a java project.
                        IJavaProject javaProject = JavaCore.create(iProject);

                        // Add the default output location for the java project
                        classpathURLs.addAll(getOutputFolderURLs(javaProject));
                    } else {
                        if (DEBUG) {
                            Util.log(null, "project loader - ignored " + classpathEntry.toString(), IStatus.INFO); //$NON-NLS-1$
                        }
                    }
                }
                
            }
            
        } catch (Exception e) {
            Util.log(e, "Could not build project path"); //$NON-NLS-1$
        }
        
        return classpathURLs;
    }
    
    /**
     * @param javaProject a java project
     * @return (List of URL) the urls which comprise the output folders for all source folders for the given 
     * java project, including the project's default output folder.
     * @throws MalformedURLException
     * @throws JavaModelException
     */
    private static List<URL> getOutputFolderURLs(IJavaProject javaProject) throws MalformedURLException, JavaModelException {
        List<URL> urlEntryList = new ArrayList<URL>();
        
        // Add the default output location for the java project
        urlEntryList.add(getURLForWorkspacePath(javaProject.getOutputLocation().addTrailingSeparator()));

        // Now get the output locations for all source folders in the java project.
        
        // The raw classpath should be ok since we are only looking for source entries.
        IClasspathEntry[] projectEntryRawClasspath = javaProject.getRawClasspath();

        for (final IClasspathEntry projectEntryRawClasspathEntry : projectEntryRawClasspath) {

            if (projectEntryRawClasspathEntry.getEntryKind() == IClasspathEntry.CPE_SOURCE) {

                // Don't have to add the default output location any more.
                IPath outputLocation = projectEntryRawClasspathEntry.getOutputLocation();
                if (outputLocation != null) {
                    URL urlForWorkspacePath = getURLForWorkspacePath(outputLocation.addTrailingSeparator());
                    
                    // Also check that it's not already in the list (eg. if multiple source folders map to the same output folder).
                    if (!urlEntryList.contains(urlForWorkspacePath)) {
                        urlEntryList.add(urlForWorkspacePath);
                    }
                }
            }
        }
        
        return urlEntryList;
    }
    
    /**
     * Helper method to convert a workspace relative path to a URL.
     * An error will be logged to the console if the url could not be determined.
     * 
     * @param workspaceRelativePath the workspace-relative path to convert.
     * Note that this path must have a trailing separator to be considered a folder.
     * 
     * @return the corresponding URL, or null if it could not be determined.
     * @throws MalformedURLException
     */
    private static URL getURLForWorkspacePath(IPath workspaceRelativePath) throws MalformedURLException {
        IWorkspaceRoot root = ResourcesPlugin.getWorkspace().getRoot();
        IResource resource = root.findMember(workspaceRelativePath);
        if (resource != null) {
            URI location = resource.getLocationURI();
            if (location != null) { 
                // HACK(?): urlEntry must have a trailing separator, or else the URLClassLoader will not think the URL is a folder.
                // urlEntry = location.toURL();

                String locationString = location.toString();
                if (workspaceRelativePath.hasTrailingSeparator() && !locationString.endsWith("/")) {
                    locationString += "/";
                }
                return new URL(locationString);
            }
        }

        Util.log(new Exception(), "Unknown path encountered: " + workspaceRelativePath, IStatus.ERROR); //$NON-NLS-1$
        return null;
    }
    
    /**
     * Given a path, return the corresponding url.
     * HACK: On unix, there is no way to determine whether the library path is a filesystem path or a workspace-relative path
     * 
     * @param pathForURL the path to a file
     * @return a url corresponding to the file's location, or null if the file does not exist.
     * @throws MalformedURLException if there was a problem converting the file's location to a url.
     */
    private static URL getRawLocationURL(IPath pathForURL) throws MalformedURLException {
        File file = getRawLocationFile(pathForURL);
        return file == null ? null : file.toURL();
    }

    /**
     * @param simplePath a path to a file
     * @return the corresponding file, or null if the file does not exist.
     */
    private static File getRawLocationFile(IPath simplePath) {
        IResource resource = ResourcesPlugin.getWorkspace().getRoot().findMember(simplePath);
        if (resource != null) {
            IPath rawLocation = resource.getRawLocation();
            if (rawLocation == null) {
                System.err.println("Can't get raw location for resource: " + resource.getFullPath());
                return null;
            }
            return rawLocation.toFile();
        }
        
        return simplePath.toFile();
    }

    /**
     * Update the class members related to module source definitions.
     */
    private void updateSourceDefinitionInfo() {
        
        this.moduleNameToSourceFileInfoMap.clear();
        this.invalidResourceNameMap.clear();
        
        ICALResourceContainer[] inputSourceContainers = getInputFolders();
        
        Map<ModuleName, ModuleSourceDefinition> moduleSourceMap = new LinkedHashMap<ModuleName, ModuleSourceDefinition>();
        Map<ModuleName, ModuleSourceDefinition> writableSourceMap = new LinkedHashMap<ModuleName, ModuleSourceDefinition>();

        List<IStorage> duplicatedMemberFileList = new ArrayList<IStorage>();
        
        // contains all sources for the project
        Map<IJavaProject, List<ModuleSourceDefinition>> projectToAllModuleSourcesMap = new HashMap<IJavaProject, List<ModuleSourceDefinition>>();

        // contains only the writable sources for the project
        Map<IJavaProject, List<ModuleSourceDefinition>> projectToWritableModuleSourcesMap = new HashMap<IJavaProject, List<ModuleSourceDefinition>>();
    
        for (final ICALResourceContainer inputContainer : inputSourceContainers) {
            IPackageFragmentRoot packageRoot = inputContainer.getPackageRoot();
            IJavaProject javaProject = packageRoot.getJavaProject();
            
            Set<IStorage> sourceFiles = inputContainer.getCALSources();         // ~slow

            for (final IStorage memberStorage : sourceFiles) {
                
                ModuleName moduleName = Util.getModuleNameFromStorage(memberStorage);
                if (moduleName == null) {
                    // Storage doesn't correspond to a valid module name.
                    // Add to invalid resource name map.
                    
                    Set<Pair<IStorage, IPackageFragmentRoot>> invalidResourcesForProjectSet = invalidResourceNameMap.get(javaProject.getProject());
                    if (invalidResourcesForProjectSet == null) {
                        invalidResourcesForProjectSet = new HashSet<Pair<IStorage, IPackageFragmentRoot>>();
                        invalidResourceNameMap.put(javaProject.getProject(), invalidResourcesForProjectSet);
                    }
                    invalidResourcesForProjectSet.add(Pair.make(memberStorage, packageRoot));

                    continue;
                }

                moduleNameToSourceFileInfoMap.put(moduleName, new Pair<ICALResourceContainer, IStorage>(inputContainer, memberStorage));
                
                // Check for duplicates.
                if (moduleSourceMap.containsKey(moduleName)) {
                    duplicatedMemberFileList.add(memberStorage);

                } else {
                    // Add the module source definition.
                    ModuleSourceDefinition moduleSourceDefinition;
                
                    if (inputContainer.isWritable()) {
                        moduleSourceDefinition = new EclipseFileModuleSourceDefinition(moduleName, (IFile)memberStorage);
                    } else {
                        moduleSourceDefinition = new EclipseStorageModuleSourceDefinition(moduleName, memberStorage, inputContainer.getTimeStamp());
                    }
                    // add to global map
                    moduleSourceMap.put(moduleName, moduleSourceDefinition);
                    if (inputContainer.isWritable()) {
                        writableSourceMap.put(moduleName, moduleSourceDefinition);
                    }
                    
                    // finally add to the sources list
                    List<ModuleSourceDefinition> moduleSourceList = projectToAllModuleSourcesMap.get(javaProject);
                    if (moduleSourceList == null) {
                        moduleSourceList = new ArrayList<ModuleSourceDefinition>();
                        projectToAllModuleSourcesMap.put(javaProject, moduleSourceList);

                        // do the writable list at the same time
                        projectToWritableModuleSourcesMap.put(javaProject, new ArrayList<ModuleSourceDefinition>());
                    }
                    moduleSourceList.add(moduleSourceDefinition);
                    
                    // add to the writable sources list only if
                    // its container is writable
                    if (inputContainer.isWritable()) {
                        List<ModuleSourceDefinition> writableModuleSourceList = projectToWritableModuleSourcesMap.get(javaProject);
                        writableModuleSourceList.add(moduleSourceDefinition);
                    }
                }
            }
        }
        
        IStorage[] duplicateSourceFiles = duplicatedMemberFileList.toArray(new IStorage[duplicatedMemberFileList.size()]);
        
        // For each project with a cal nature, create a module source definition from the cal files in its input folder.
        this.duplicateSourceFiles = duplicateSourceFiles;
        this.projectToSourceDefinitionGroupMap = new HashMap<IJavaProject, EclipseModuleSourceDefinitionGroup>();
        for (final Map.Entry<IJavaProject, List<ModuleSourceDefinition>> entry : projectToAllModuleSourcesMap.entrySet()) {
            IJavaProject javaProject = entry.getKey();
            List<ModuleSourceDefinition> projectSourcesList = entry.getValue();
            ModuleSourceDefinition[] projectDefnArray = projectSourcesList.toArray(new ModuleSourceDefinition[projectSourcesList.size()]);
            
            List<ModuleSourceDefinition> writableSourcesList = projectToWritableModuleSourcesMap.get(javaProject);
            ModuleSourceDefinition[] writableDefnArray = 
                writableSourcesList.toArray(new ModuleSourceDefinition[writableSourcesList.size()]);
            
            
            
            // Clear the source model caches. 
            for (int i = 0; i < projectDefnArray.length; ++i) {
                final ModuleName moduleName = projectDefnArray[i].getModuleName();
                moduleNameToSource.remove(moduleName);
                moduleNameToSourceModel.remove(moduleName);   
            }
            
            projectToSourceDefinitionGroupMap.put(javaProject, 
                    new EclipseModuleSourceDefinitionGroup(projectDefnArray, writableDefnArray));
        }
        
        // Create the global ModuleSourceDefinitionGroup.
        ModuleSourceDefinition[] moduleSourceDefinitionArray = 
            moduleSourceMap.values().toArray(new ModuleSourceDefinition[moduleSourceMap.size()]);
        ModuleSourceDefinition[] writableSourceDefinitionArray = 
            writableSourceMap.values().toArray(new ModuleSourceDefinition[writableSourceMap.size()]);
        this.globalModuleSourceDefinitionGroup = 
                new EclipseModuleSourceDefinitionGroup(moduleSourceDefinitionArray, writableSourceDefinitionArray);
    }
    
    /**
     * @param moduleName the name of a module.
     * @return the module's source file, or null if the module's source file could not be found in the workspace.
     */
    public IStorage getInputSourceFile(ModuleName moduleName) {
        Pair<ICALResourceContainer, IStorage> inputModuleInfo = getInputModuleSourceFileInfo(moduleName);
        if (inputModuleInfo != null) {
            return inputModuleInfo.snd();
        }
        return null;
    }
    
    /**
     * @param moduleName the name of a module.
     * @return the folder containing the module's source file, or null if the module's source file could not be found in the workspace.
     */
    public ICALResourceContainer getInputSourceFileContainer(ModuleName moduleName) {
        Pair<ICALResourceContainer, IStorage> inputModuleInfo = getInputModuleSourceFileInfo(moduleName);
        if (inputModuleInfo != null) {
            return inputModuleInfo.fst();
        }
        return null;
    }
    
    /**
     * @param moduleStorage the file containing the module.
     * @return the name of the module corresponding to the given file.  
     * null if the storage name doesn't correspond to a module name.
     */
    public ModuleName getModuleName(IStorage moduleStorage) {
        return Util.getModuleNameFromStorage(moduleStorage);
    }
    
    /**
     * @param moduleName the name of a module
     * @return the module's resource container and source file, or null if the module's source file could not be found in the workspace.
     */
    private Pair<ICALResourceContainer, IStorage> getInputModuleSourceFileInfo(ModuleName moduleName) {
        return moduleNameToSourceFileInfoMap.get(moduleName);
    }
    
    /**
     * finds a resource container that contains the calFile passed in
     * 
     * This method works by comparing path segments of the calFile argument
     * and each potential resource container
     * @param calFile the file whose input container to look for
     * @return the resource container of the file
     * 
     */
    public ICALResourceContainer getInputFolder(IFile calFile) {
        IPath filePath = calFile.getFullPath();
        // find the correct CAL folder segment
        int segment = -1;
        for (int i = filePath.segmentCount()-1; i >= 0; i--) {
            if (filePath.segment(i).equals(CALSourcePathMapper.SCRIPTS_BASE_FOLDER)) {
                segment = i;
                break;
            }
        }
        
        if (segment > -1) {
            // take the segment that is one above the CAL folder segment
            // this finds the package root
            IPath folderPath = 
                filePath.removeLastSegments(filePath.segmentCount() - segment);
            
            // search through all folders in this project for the container
            // with the same package root.
            ICALResourceContainer[] folders = 
                getInputFolders(Util.getCalProject(calFile.getProject()));
            for (final ICALResourceContainer resourceContainer : folders) {
                // we only care about resource containers that are writable (ie- not in a jar)
                if (resourceContainer.isWritable()) {
                    IPath resourcePath = resourceContainer.getPath();
                    if (resourcePath.segment(resourcePath.segmentCount()-2).equals(folderPath.lastSegment())) {
                        return resourceContainer;
                    }
                }
            }
        }
        
        // none found
        return null;
        
    }
    /**
     * @param javaProject the java project whose resource containers are being searched for
     * @return the input folders for the project.  These end with the path CALSourcePathMapper.SCRIPTS_BASE_FOLDER.
     */
    public ICALResourceContainer[] getInputFolders(final IJavaProject javaProject) {
        
        if (javaProject == null) {
            return new ICALResourceContainer[0];
        }
        
        // check for cached version
        if (projectToResourceContainers.containsKey(javaProject)) {
            return projectToResourceContainers.get(javaProject);
        }
        
        // (List of IPackageFragment) the input folders, as a list.
        List<ICALResourceContainer> inputContainerList = new ArrayList<ICALResourceContainer>();
        
        // Get its resolved classpath entries.
        IClasspathEntry[] resolvedClasspath;
        try {
            resolvedClasspath = javaProject.getResolvedClasspath(true);
        } catch (JavaModelException e) {
            // Should never happen.
            IllegalStateException ise = new IllegalStateException(e);
            Util.log(ise, "Unable to get classpath for " + javaProject);
            throw ise;
        }

        // Iterate over entries which correspond to Java source folders.
        for (final IClasspathEntry classpathEntry : resolvedClasspath) {
            switch (classpathEntry.getEntryKind()) {
            
            case IClasspathEntry.CPE_SOURCE:
                
                try {
                    IPackageFragmentRoot root = javaProject.findPackageFragmentRoot(classpathEntry.getPath());
                    if (root != null && root.exists()) {
                        
                        // get the top-level CAL fragment if it exists
                        IPackageFragment frag = root.getPackageFragment(CALSourcePathMapper.SCRIPTS_BASE_FOLDER);
                        if (frag != null && frag.exists()) {
                            inputContainerList.add(containerFactory.create(root));
                        }
                    }
                } catch (JavaModelException e) {
                    Util.log(e, "Error getting input folders for " + classpathEntry);
                }
                break;
                
            case IClasspathEntry.CPE_LIBRARY:
                // check to see if this points to a car-jar
                // then look for the CAL fragment
                try {
                    IPackageFragmentRoot root = javaProject.findPackageFragmentRoot(classpathEntry.getPath());
                    if (root != null && root.exists() && root.getElementName().endsWith(".car.jar")) {
                        // get the top-level CAL fragment if it exists
                        IPackageFragment frag = root.getPackageFragment(CALSourcePathMapper.SCRIPTS_BASE_FOLDER);
                        if (frag != null && frag.exists()) {
                            inputContainerList.add(containerFactory.create(root));
                        }
                    }
                } catch (JavaModelException e) {
                    Util.log(e, "Error getting input folders for " + classpathEntry);
                }
                break;
            }
            
        }
        ICALResourceContainer[] containers = inputContainerList.toArray(new ICALResourceContainer[inputContainerList.size()]);
        
        // add a resource listener so that we know to invalidate the cache when classpath changes 
        final IFile classpathFile = javaProject.getProject().getFile(".classpath");
        if (classpathFile != null && classpathFile.exists()) {
            // put it in the cache
            projectToResourceContainers.put(javaProject, containers);
        }

        return containers;
    }

    /**
     * @return the source input folders for all CAL projects.
     */
    private ICALResourceContainer[] getInputFolders() {
        // TODOEL - get these from the per project infos / project options.
        
        IProject[] projects = projectOrder.projects;
        
        // convert to Java projects with CAL Nature
        List<IJavaProject> calProjects = new LinkedList<IJavaProject>();
        for (final IProject project : projects) {
            IJavaProject javaProject = Util.getCalProject(project);
            if (javaProject != null) {
                calProjects.add(javaProject);
            }
        }

        List<ICALResourceContainer> inputContainerList = new ArrayList<ICALResourceContainer>();
        for (final IJavaProject javaProject : calProjects) {
            inputContainerList.addAll(Arrays.asList(getInputFolders(javaProject)));
        }
        
        return inputContainerList.toArray(new ICALResourceContainer[inputContainerList.size()]);
    }
    
    
    /**
     * Returns the output folders for the given java project.
     * 
     * The method returns folders, not package fragments.
     * 
     * The output folders are calculated off of the input folders:
     * take the root of the input folder and add "lecc_runtime" onto it
     * 
     * @param javaProject
     * @return the output folders for javaProject
     */
    public IFolder[] getOutputFolders(IJavaProject javaProject) {
        List<IFolder> outputFolders = new ArrayList<IFolder>();
        ICALResourceContainer[] resourceContainers = getInputFolders(javaProject);
        for (final ICALResourceContainer resourceContainer : resourceContainers) {
            if (resourceContainer.isWritable()) {
                outputFolders.add(resourceContainer.getOutputFolder());
            }
        }
        
        return outputFolders.toArray(new IFolder[outputFolders.size()]);
    }

    /**
     * @return the program resource repository.
     */
    public ProgramResourceRepository getResourceRepository() {
        return resourceRepository;
    }
    

    /**
     * @param moduleName Name of the module to get the code analyser
     * @return A code Analyser for the given module.
     */
    public CodeAnalyser getCodeAnalyser(ModuleName moduleName){
        TypeChecker typeChecker = programModelManager.getTypeChecker();
        ModuleTypeInfo mti = programModelManager.getModule(moduleName).getModuleTypeInfo();
        return new CodeAnalyser(typeChecker, mti, true, false);
    }

    public TypeChecker getTypeChecker(){
        return programModelManager.getTypeChecker();
    }
    
    /**
     * @param moduleName Name of the module to get the type info for.
     * @return The ModuleTypeInfo for the given module. Return null if there is no module type info.
     */
    public ModuleTypeInfo getModuleTypeInfo(ModuleName moduleName){
        Module module = programModelManager.getModule(moduleName);
        if (module == null){
            return null;
        } else {
            return module.getModuleTypeInfo();
        }
    }

    public interface SourceManagerFactory {
        public ModuleContainer.ISourceManager getSourceManager(ModuleName name);    
    }
    
    public ModuleContainer getModuleContainer(final SourceManagerFactory smf){
        return new ModuleContainer (){    
            final List<Module> modules = programModelManager.getModules();
            // current this maybe null for read only uses.m
            SourceManagerFactory sourceManagerFactory = smf;

            @Override
            public int getNModules() {
                return modules.size();
            }

            @Override
            public ModuleTypeInfo getNthModuleTypeInfo(int i) {
                return modules.get(i).getModuleTypeInfo(); // TODO: Optimize this if needed.
            }

            @Override
            public ModuleTypeInfo getModuleTypeInfo(ModuleName moduleName) {
                Module module = programModelManager.getModule(moduleName);
                if (module == null){
                    return null;
                }
                else{
                    return module.getModuleTypeInfo();
                }
            }

            @Override
            public ModuleSourceDefinition getSourceDefinition(ModuleName moduleName) {
                CALModelManager cmm = CALModelManager.getCALModelManager();
                return cmm.getModuleSourceDefinition(moduleName);
            }

            @Override
            public ModuleDefn getSourceModel(ModuleName moduleName, boolean ignoreErrors, CompilerMessageLogger logger) {
                CALModelManager cmm = CALModelManager.getCALModelManager();
                return cmm.getModuleSourceModel(moduleName, ignoreErrors, logger);
            }        

            @Override
            public String getModuleSource(ModuleName moduleName){
                return CALModelManager.getCALModelManager().getModuleSource(moduleName);
            }

            @Override
            public ResourceManager getResourceManager(ModuleName moduleName, String resourceType) {
                assert(resourceType.equals(WorkspaceResource.METADATA_RESOURCE_TYPE));
                return metadataManager;
            }

            @Override
            public ISourceManager getSourceManager(ModuleName moduleName) {
                return sourceManagerFactory.getSourceManager(moduleName);
            }

            @Override
            public boolean renameFeature(CALFeatureName oldFeatureName, CALFeatureName newFeatureName, org.openquark.cal.services.Status renameStatus) {
                return false;
            }

            @Override
            public boolean saveMetadata(CALFeatureMetadata metadata, org.openquark.cal.services.Status saveStatus) {
                return false;
            }
        };  
    }

    /** 
     * @return the source metrics object for the program
     */
    public SourceMetricsManager getSourceMetrics(){
        return new SourceMetricsManager(getModuleContainer(null));
    }
    
    /**
     * @return a {@link SearchManager} for the modules in the CAL model.
     */
    public SearchManager getSearchManager() {
        return new SearchManager(getModuleContainer(null));
    }

 
    //
    // Metadata management
    //
    
    public void addMetadataChangeListener (MetadataChangeListener listener) {
        metadataChangeListeners.add (listener);
    }
    
    public void removeMetadataChangeListener (MetadataChangeListener listener) {
        metadataChangeListeners.remove (listener);
    }

    private void fireMetadataChanged (CALFeatureName featureName) {
        Set<MetadataChangeListener> listeners = new HashSet<MetadataChangeListener> (metadataChangeListeners);
        
        for (final MetadataChangeListener listener : listeners) {
            listener.metadataSaved (featureName);
        }
    }

    /**
     * Method getMetadata
     *
     * @param featureName
     * @param locale
     * 
     * @return Returns the {@link CALFeatureMetadata}
     */
    public CALFeatureMetadata getMetadata (CALFeatureName featureName, Locale locale) {
        return metadataManager.getMetadata (featureName, locale);
    }

    /**
     * Method getMetadata
     *
     * @param scopedEntity
     * @param locale
     * 
     * @return Returns the {@link ScopedEntityMetadata}
     */
    public ScopedEntityMetadata getMetadata (ScopedEntity scopedEntity, Locale locale) {
        ScopedEntityMetadata metadata = metadataManager.getMetadata (scopedEntity, locale);
        
        ensureMetadataValid (scopedEntity, metadata);
        
        return metadata;
    }
    
    /**
     * Method ensureMetadataValid
     * @param scopedEntity 
     * @param metadata
     */
    private void ensureMetadataValid (ScopedEntity scopedEntity, ScopedEntityMetadata metadata) {
        // Add argument metadata if missing
        if (metadata instanceof FunctionalAgentMetadata) {
            FunctionalAgentMetadata functionalAgentMetadata = (FunctionalAgentMetadata)metadata;
            
            FunctionalAgent functionalAgent = (FunctionalAgent)scopedEntity;
            
            int numArgs = functionalAgent.getTypeExpr().getArity();
            
            ArgumentMetadata[] currentArgs = functionalAgentMetadata.getArguments();
            
            if (numArgs != currentArgs.length) {
                ArgumentMetadata[] actualArgs = new ArgumentMetadata[numArgs];
                
                int argsToCopy = Math.min (currentArgs.length, numArgs);
                
                System.arraycopy(currentArgs, 0, actualArgs, 0, argsToCopy);
                
                for (int i = argsToCopy; i < actualArgs.length; i++) {
                    actualArgs[i] = new ArgumentMetadata(CALFeatureName.getArgumentFeatureName(i), metadata.getLocale());
                }
                
                functionalAgentMetadata.setArguments(actualArgs);
            }
        }
    }

    /**
     * Method saveMetadata
     *
     * @param metadata
     * @param saveStatus
     * 
     * @return Returns true iff the metadata was saved successfully
     */
    public boolean saveMetadata (CALFeatureMetadata metadata, org.openquark.cal.services.Status saveStatus) {
        if (metadataManager.saveMetadata (metadata, saveStatus)) {
            fireMetadataChanged (metadata.getFeatureName ());
            
            return true;
        }
        
        return false;
    }

    public void clearCachedResourceContainers(IProject currentProject) {
        projectToResourceContainers.remove(Util.getCalProject(currentProject));
    }
    
}
