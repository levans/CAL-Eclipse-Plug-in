/*******************************************************************************
 * Copyright (c) 2006 Business Objects Software Limited and others.
 * All rights reserved. 
 * This file is made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Business Objects Software Limited - initial API and implementation based on Eclipse 3.1.2 code for
 *                             /org.eclipse.jdt.ui/core extension/org/eclipse/jdt/internal/corext/util/CodeFormatterUtil.java
 *                           Eclipse source is available at: http://www.eclipse.org/downloads/
 *******************************************************************************/


/*
 * ModuleTreeContentProvider.java
 * Creation date: Jan 22 2007
 * By: Greg McClement
 */

package org.openquark.cal.eclipse.ui.views;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.SortedSet;
import java.util.TreeSet;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.IStatusLineManager;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerComparator;
import org.eclipse.swt.SWT;
import org.openquark.cal.compiler.ClassInstance;
import org.openquark.cal.compiler.ClassMethod;
import org.openquark.cal.compiler.DataConstructor;
import org.openquark.cal.compiler.Function;
import org.openquark.cal.compiler.ModuleName;
import org.openquark.cal.compiler.ModuleSourceDefinition;
import org.openquark.cal.compiler.ModuleTypeInfo;
import org.openquark.cal.compiler.Scope;
import org.openquark.cal.compiler.ScopedEntity;
import org.openquark.cal.compiler.TypeClass;
import org.openquark.cal.compiler.TypeConstructor;
import org.openquark.cal.eclipse.core.CALModelManager;
import org.openquark.cal.eclipse.core.builder.CALBuilder;
import org.openquark.cal.eclipse.ui.CALUIMessages;
import org.openquark.cal.eclipse.ui.util.CoreUtility;
import org.openquark.cal.eclipse.ui.views.CALWorkspace.ModuleElementsByType;
import org.openquark.cal.machine.StatusListener;


/**
 * A content provider for the CAL Workspace and the Outline view of CAL files.
 */
public abstract class ModuleTreeContentProvider implements IStructuredContentProvider, 
                                            ITreeContentProvider {
        /**
         * Show the form of the name with the module hierarchy.
         */
        private final CALModuleContentProvider moduleContentProvider;

        /**
         * The root of the viewer. An arbitrary unique object!
         */
        private final Object root = new Object ();
        
        private final CALModelManager calModelManager = CALModelManager.getCALModelManager();
        
        private TreeViewer viewer;
        // if the cal builder is not enabled then a message saying this is shown. When
        // the cal builder is enabled we want to refresh the dialog from scratch and get
        // rid of that message.
        private boolean calBuilderWasEnabled = true;
        
        /**
         * If the tree is flat then the root it a collection of modules. If the tree is structed
         * then the root is a HierarchicalNode.
         */
        private Object invisibleRoot;
        
        private Map<ModuleName, Object> moduleSourceDefinitionToModuleElementByTypeMap_functions = new HashMap<ModuleName, Object>();
        private Map<ModuleName, Object> moduleSourceDefinitionToModuleElementByTypeMap_typeClasses = new HashMap<ModuleName, Object>();
        private Map<ModuleName, Object> moduleSourceDefinitionToModuleElementByTypeMap_typeConstructors = new HashMap<ModuleName, Object>();
        private Map<ModuleName, Object> moduleSourceDefinitionToModuleElementByTypeMap_classInstances = new HashMap<ModuleName, Object>();

        // Actions for controlling the layout content
        private Action showClassOfCALElementsAction;
        private Action showPrivateElementsAction;
        private Action linkWithEditorAction;
        
        private StatusListener compileStatusListener = null;

        protected final ILabelProvider labelProvider;
        
        /**
         * @param moduleContentProvider The adapter for exchanging information between this content provider and the object that refers to it.
         * @param viewer The tree viewer that the content provider is for. This is used to update the view on compiles.
         */
        public ModuleTreeContentProvider(CALModuleContentProvider moduleContentProvider, ILabelProvider labelProvider, TreeViewer viewer) {
            this.moduleContentProvider = moduleContentProvider;
            this.labelProvider = labelProvider;
            this.viewer = viewer;
            makeActions();
            CoreUtility.initializeCALBuilderInBackground();
            hookIntoCompiler();
            viewer.setComparator(getComparator());
        }
        
        private static final ViewerComparator viewerComparator = new ViewerComparator() {
            Object adjustName(Object object){
                if (object instanceof ClassInstance){
                    final ClassInstance classInstance = (ClassInstance) object;
                    return classInstance.getName().replaceFirst("\\(", "");
                }
                return object;
            }
            
            @Override
            public int compare(Viewer viewer, Object e1, Object e2){
                return super.compare(viewer, adjustName(e1), adjustName(e2));
            }
        };
        
        public ViewerComparator getComparator(){
            return viewerComparator;            
        }
        
        /**
         * Method getRoot
         *
         * @return Returns the arbitrary root object
         */
        public Object getRoot () {
            return root;
        }
        
        /**
         * @return the moduleContentProvider
         */
        protected CALModuleContentProvider getModuleContentProvider () {
            return moduleContentProvider;
        }
        
        
        /**
         * @return the viewer
         */
        protected TreeViewer getViewer () {
            return viewer;
        }
        
        protected boolean wasCalBuilderEnabled () {
            return calBuilderWasEnabled;
        }
        
        protected void setCalBuilderWasEnabled (boolean value) {
            calBuilderWasEnabled = value;
        }

        protected boolean canLinkWithEditor(){
            return true;
        }
        
        /**
         * @return the invisibleRoot
         */
        protected Object getInvisibleRoot () {
            return invisibleRoot;
        }

        public abstract void fillLocalPullDown(IMenuManager manager);
        
        protected void addStandardLocalMenuActions (IMenuManager manager) {
            manager.add(showClassOfCALElementsAction);
            manager.add(showPrivateElementsAction);
            if (canLinkWithEditor()){
                manager.add(linkWithEditorAction);
            }
        }

        public abstract void fillContextMenu(IMenuManager manager); 
        
        protected void addStandardContextMenuActions (IMenuManager manager) {
            manager.add(showClassOfCALElementsAction);
            manager.add(showPrivateElementsAction);
            if (canLinkWithEditor()){
                manager.add(linkWithEditorAction);
            }
        }

        /**
         * Depending on if the class of elements are shown add the appropriate object to the start
         * of the path to the given object in the tree viewer hierarchy.
         * @param scopedEntities A (partial) path to an entity in the tree viewer.
         */
        public void augmentPath(LinkedList<Object> scopedEntities){
            if (moduleContentProvider.getShowElementHierarchy()){
                Object entity = scopedEntities.getFirst();
                if (entity instanceof Function){                   
                    ModuleName moduleName = ((ScopedEntity) entity).getName().getModuleName();
                    scopedEntities.addFirst(moduleSourceDefinitionToModuleElementByTypeMap_functions.get(moduleName));
                }
                else if (entity instanceof TypeClass){
                    ModuleName moduleName = ((ScopedEntity) entity).getName().getModuleName();
                    scopedEntities.addFirst(moduleSourceDefinitionToModuleElementByTypeMap_typeClasses.get(moduleName));
                }
                else if (entity instanceof TypeConstructor){
                    ModuleName moduleName = ((ScopedEntity) entity).getName().getModuleName();
                    scopedEntities.addFirst(moduleSourceDefinitionToModuleElementByTypeMap_typeConstructors.get(moduleName));
                }
                else if (entity instanceof ClassInstance){
                    ModuleName moduleName = ((ClassInstance) entity).getModuleName();
                    scopedEntities.addFirst(moduleSourceDefinitionToModuleElementByTypeMap_classInstances.get(moduleName));
                }
            }
        }

        public void makeContributions(IToolBarManager toolBarManager){            
        }

        public void makeContributions(IStatusLineManager statusLineManager){            
        }

        protected void setShowModuleHierarchy(boolean showModuleHierarchy){
            if (showModuleHierarchy == moduleContentProvider.getShowModuleHierarchy()){
                return;
            }
            else{
                ISelection selection = viewer.getSelection();
                moduleContentProvider.setShowModuleHierarchy(showModuleHierarchy);
                initialize();
                viewer.refresh(root);
                viewer.setSelection(selection, true);
            }
        }

        private void setShowElementHierarchy(boolean showElementHierarchy){
            if (showElementHierarchy == moduleContentProvider.getShowElementHierarchy()){
                return;
            }
            else{
                ISelection selection = viewer.getSelection();
                moduleContentProvider.setShowElementHierarchy(showElementHierarchy);
                initialize();
                viewer.refresh(root);
                viewer.setSelection(selection, true);
           }
        }
        
        private void setShowPrivateElements(boolean showPrivateElements){
            if (showPrivateElements == moduleContentProvider.getShowPrivateElements()){
                return;
            }
            else{
                ISelection selection = viewer.getSelection();
                moduleContentProvider.setShowPrivateElements(showPrivateElements);
                initialize();
                viewer.refresh(root);
                viewer.setSelection(selection, true);
           }
        }

        
        private void makeActions(){
            // Show Hierarchy of CAL Elements Action
            {
                showClassOfCALElementsAction = new Action("", IAction.AS_CHECK_BOX) {
                    @Override
                    public void run() {
                        setShowElementHierarchy(isChecked());
                    }
                };
                showClassOfCALElementsAction.setText(CALUIMessages.CALWorkspace_showClassOfCALElements);
                showClassOfCALElementsAction.setChecked(moduleContentProvider.getShowElementHierarchy());
                showClassOfCALElementsAction.setToolTipText(CALUIMessages.CALWorkspace_showClassOfCALElements_tooltip);
//                showClassOfCALElementsAction.setImageDescriptor(PlatformUI.getWorkbench().getSharedImages().
//                        getImageDescriptor(ISharedImages.IMG_OBJS_INFO_TSK));
            }
            
            // Show Hierarchy of CAL Elements Action
            {
                showPrivateElementsAction = new Action("", IAction.AS_CHECK_BOX) {
                    @Override
                    public void run() {
                        setShowPrivateElements(isChecked());
                    }
                };
                showPrivateElementsAction.setText(CALUIMessages.CALWorkspace_showPrivateElements);
                showPrivateElementsAction.setChecked(moduleContentProvider.getShowPrivateElements());
                showPrivateElementsAction.setToolTipText(CALUIMessages.CALWorkspace_showPrivateElements_tooltip);
//                showPrivateElementsAction.setImageDescriptor(PlatformUI.getWorkbench().getSharedImages().
//                        getImageDescriptor(ISharedImages.IMG_OBJS_INFO_TSK));
            }
            
            linkWithEditorAction = new Action("", IAction.AS_CHECK_BOX) {
                @Override
                public void run() {
                    setLinkWithEditor(isChecked());
                }
            };
            linkWithEditorAction.setText(CALUIMessages.CALWorkspace_linkWithEditor);
            linkWithEditorAction.setChecked(getModuleContentProvider ().getLinkWithEditor());
//            linkWithEditorAction.setToolTipText(CALUIMessages.CALWorkspace_linkWithEditor_tooltip);
//            linkWithEditorAction.setImageDescriptor(PlatformUI.getWorkbench().getSharedImages().
//                    getImageDescriptor(ISharedImages.IMG_OBJS_INFO_TSK));

        }

        private void setLinkWithEditor(boolean linkWithEditor){
            getModuleContentProvider ().setLinkWithEditor(linkWithEditor);
        }
        
        public void inputChanged(Viewer v, Object oldInput, Object newInput) {
        }
        
        public void dispose() {
            calModelManager.getProgramModelManager().removeStatusListener(compileStatusListener);
        }
        
        /**
         * @see org.eclipse.jface.viewers.IStructuredContentProvider#getElements(java.lang.Object)
         * 
         * Only called to retrieve the root elements
         */
        public Object[] getElements(Object parent) {
            if (parent.equals(root))
                return getRootElements ();
            
            return getChildren(parent);
        }
        
        /**
         * Method getRootElements
         *
         * @return Returns the top-level elements
         */
        abstract protected Object[] getRootElements ();
        
        /* 
         * This is needed in order to ensure that when the configuration of the viewer is change that
         * the same objects that are currently visible will stay visible.
         * @see org.eclipse.jface.viewers.ITreeContentProvider#getParent(java.lang.Object)
         */
        public Object getParent(Object child) {
            if (child instanceof HierarchicalNode){
                if (this.moduleContentProvider.getShowModuleHierarchy()){
                    return ((HierarchicalNode) child).getParent();
                }
                else{
                    // no longer has a parent in this configuration
                    return null;
                }
            }
            else if (child instanceof ModuleName){
                if (this.moduleContentProvider.getShowModuleHierarchy()){
                    // Get the hierachical node that contains this module
                    HierarchicalNode hn = (HierarchicalNode) invisibleRoot;
                    return hn.getParent((ModuleName) child);
                }
                else{
                    // No structure so the root is the parent
                    return root;
                }
            }
            else if (child instanceof ScopedEntity){
                if (child instanceof DataConstructor){  
                    return ((DataConstructor) child).getTypeConstructor();
                }
                else if (this.moduleContentProvider.getShowElementHierarchy()){
                    // ensure the structure is set up
                    ModuleName moduleName = ((ScopedEntity) child).getName().getModuleName();
                    getChildren(moduleName);
                    if (child instanceof Function){
                        return moduleSourceDefinitionToModuleElementByTypeMap_functions.get(moduleName);
                    }
                    else if (child instanceof TypeClass){
                        return moduleSourceDefinitionToModuleElementByTypeMap_typeClasses.get(moduleName);
                    }
                    else if (child instanceof TypeConstructor){
                        return moduleSourceDefinitionToModuleElementByTypeMap_typeConstructors.get(moduleName);
                    }
                    else if (child instanceof ClassInstance){
                        return moduleSourceDefinitionToModuleElementByTypeMap_typeConstructors.get(moduleName);
                    }
                    else{
                        return null;
                    }
                }
                else{
                    return ((ScopedEntity) child).getName().getModuleName();
                }
            }
            else if (child instanceof ModuleElementsByType){
                return ((ModuleElementsByType) child).getParent();
            }
            else{
                return null;
            }
        }
        
        private Comparator<Object> comparator = new Comparator<Object>() {

            public int compare(Object o1, Object o2) {
                if (o1 instanceof ScopedEntity){
                    final ScopedEntity se1 = (ScopedEntity) o1;
                    if (o2 instanceof ScopedEntity){
                        ScopedEntity se2 = (ScopedEntity) o2;

                        // If names compare as equal the model removes them so for "Either" and "either" with
                        // just a case insensitive compare would cause one of them to be not shown. 
                        // I want all the E's to show up together so I don't want to just switch to a 
                        // case sensitive compare. So let's do a case insensitive compare then a 
                        // case sensitive compare.
                        final int compare = se1.getName().getUnqualifiedName().compareToIgnoreCase(se2.getName().getUnqualifiedName());
                        if (compare != 0){
                            return compare;
                        }

                        return se1.getName().getUnqualifiedName().compareTo(se2.getName().getUnqualifiedName());
                    }
                    else if (o2 instanceof ClassInstance){
                        return labelProvider.getText(o1).compareToIgnoreCase(labelProvider.getText(o2));
                    }
                }
                else if (o1 instanceof ClassInstance){
                    return labelProvider.getText(o1).compareToIgnoreCase(labelProvider.getText(o2));
                }
                return 0;
            }
            
        };

        private void add(Collection<Object> collection, Object[] objects){
            for (final Object element : objects) {
                collection.add(element);
            }
        }
        
        public Object [] getChildren(Object parent) {
            return getChildren(parent, false);
        }
        
        private Object [] getChildren(Object parent, boolean alwaysGetChildren) {
            if (parent instanceof HierarchicalNode){
                return ((HierarchicalNode) parent).getChildren();
            }
            else if (parent instanceof ModuleName){
                ModuleName moduleName = (ModuleName) parent;
                ModuleTypeInfo mti = calModelManager.getModuleTypeInfo(moduleName);
                if (this.moduleContentProvider.getShowElementHierarchy()){
                    ArrayList<Object> children = new ArrayList<Object>(4);
                    if (mti != null){
                        {
                            Object mebt = moduleSourceDefinitionToModuleElementByTypeMap_functions.get(moduleName);
                            if (mebt == null){
                                if (mti.getNFunctions() > 0){
                                    SortedSet<ScopedEntity> elements = new TreeSet<ScopedEntity>(comparator);
                                    for(int i = 0; i < mti.getNFunctions(); ++i){
                                        ScopedEntity scopedEntity = mti.getNthFunction(i);
                                        if (moduleContentProvider.getShowPrivateElements() ||
                                                scopedEntity.getScope() != Scope.PRIVATE){
                                            elements.add(scopedEntity);
                                        }
                                    }
                                    mebt = new CALWorkspace.ModuleElementsByType(moduleName, CALUIMessages.CALWorkspace_CALElementType_Functions, elements.toArray());
                                    children.add(mebt);
                                    moduleSourceDefinitionToModuleElementByTypeMap_functions.put(moduleName, mebt);
                                }
                            }
                            else{
                                children.add(mebt);
                            }
                        }
                        {
                            Object mebt = moduleSourceDefinitionToModuleElementByTypeMap_typeClasses.get(moduleName);
                            if (mebt == null){
                                if (mti.getNTypeClasses() > 0){
                                    SortedSet<ScopedEntity> elements = new TreeSet<ScopedEntity>(comparator);
                                    for(int i = 0; i < mti.getNTypeClasses(); ++i){
                                        ScopedEntity scopedEntity = mti.getNthTypeClass(i);
                                        if (moduleContentProvider.getShowPrivateElements() ||
                                                scopedEntity.getScope() != Scope.PRIVATE){
                                            elements.add(scopedEntity);
                                        }
                                    }
                                    mebt = new CALWorkspace.ModuleElementsByType(moduleName, CALUIMessages.CALWorkspace_CALElementType_TypeClasses, elements.toArray());
                                    children.add(mebt);          
                                    moduleSourceDefinitionToModuleElementByTypeMap_typeClasses.put(moduleName, mebt);
                                }
                            }
                            else{
                                children.add(mebt);
                            }
                        }

                        {
                            Object mebt = moduleSourceDefinitionToModuleElementByTypeMap_typeConstructors.get(moduleName);
                            if (mebt == null){
                                if (mti.getNTypeConstructors() > 0){
                                    SortedSet<ScopedEntity> elements = new TreeSet<ScopedEntity>(comparator);
                                    for(int i = 0; i < mti.getNTypeConstructors(); ++i){
                                        ScopedEntity scopedEntity = mti.getNthTypeConstructor(i);
                                        if (moduleContentProvider.getShowPrivateElements() ||
                                                scopedEntity.getScope() != Scope.PRIVATE){
                                            elements.add(scopedEntity);
                                        }
                                    }
                                    mebt = new CALWorkspace.ModuleElementsByType(moduleName, CALUIMessages.CALWorkspace_CALElementType_TypeConstructors, elements.toArray());
                                    children.add(mebt);          
                                    moduleSourceDefinitionToModuleElementByTypeMap_typeConstructors.put(moduleName, mebt);
                                }
                            }
                            else{
                                children.add(mebt);
                            }
                        }
                        
                        {
                            Object mebt = moduleSourceDefinitionToModuleElementByTypeMap_classInstances.get(moduleName);
                            if (mebt == null){
                                if (mti.getNClassInstances() > 0){
                                    SortedSet<ClassInstance> elements = new TreeSet<ClassInstance>(comparator);
                                    for(int i = 0; i < mti.getNClassInstances(); ++i){
                                        ClassInstance classInstance = mti.getNthClassInstance(i);
                                        if (classInstance.getInstanceStyle() == ClassInstance.InstanceStyle.EXPLICIT){
                                            elements.add(classInstance);
                                        }
                                    }
                                    if (elements.size() > 0){
                                        mebt = new CALWorkspace.ModuleElementsByType(moduleName, CALUIMessages.CALWorkspace_CALElementType_ClassInstances, elements.toArray());
                                        children.add(mebt);          
                                        moduleSourceDefinitionToModuleElementByTypeMap_classInstances.put(moduleName, mebt);
                                    }
                                }
                            }
                            else{
                                children.add(mebt);
                            }
                        }
                        
                    }
                    return children.toArray();
                }
                else{
                    SortedSet<Object> children = new TreeSet<Object>(comparator);
                    if (mti != null){
                        for(int i = 0; i < mti.getNFunctions(); ++i){
                            ScopedEntity scopedEntity = mti.getNthFunction(i);
                            if (moduleContentProvider.getShowPrivateElements() ||
                                    scopedEntity.getScope() != Scope.PRIVATE){
                                children.add(scopedEntity);
                            }
                        }
                        for(int i = 0; i < mti.getNTypeClasses(); ++i){
                            ScopedEntity scopedEntity = mti.getNthTypeClass(i);
                            if (moduleContentProvider.getShowPrivateElements() ||
                                    scopedEntity.getScope() != Scope.PRIVATE){
                                children.add(scopedEntity);
                            }
                            add(children, getChildren(scopedEntity, true));
                        }
                        for(int i = 0; i < mti.getNTypeConstructors(); ++i){
                            ScopedEntity scopedEntity = mti.getNthTypeConstructor(i);
                            if (moduleContentProvider.getShowPrivateElements() ||
                                    scopedEntity.getScope() != Scope.PRIVATE){
                                children.add(scopedEntity);
                            }
                            add(children, getChildren(scopedEntity, true));
                        }
                        for(int i = 0; i < mti.getNClassInstances(); ++i){
                            ClassInstance classInstance = mti.getNthClassInstance(i);
                            if (classInstance.getInstanceStyle() == ClassInstance.InstanceStyle.EXPLICIT){
                                children.add(classInstance);
                            }
                        }
                    }
                    return children.toArray();
                }
            }
            else if (parent instanceof Function){
                Function function = (Function) parent;
                if (function.getNLocalFunctions() > 0){
                    return new Object[0];    
                }
                return new Object[0];
            }
            else if (
                    parent instanceof TypeConstructor &&
                    (alwaysGetChildren || this.moduleContentProvider.getShowElementHierarchy())){
                TypeConstructor tc = (TypeConstructor) parent;
                final int nDataConstructors = tc.getNDataConstructors();
                ArrayList<DataConstructor> children = new ArrayList<DataConstructor>(nDataConstructors);
                for(int i = 0; i < nDataConstructors; ++i){
                    if (moduleContentProvider.getShowPrivateElements() ||
                        tc.getNthDataConstructor(i).getScope() != Scope.PRIVATE){
                        children.add(tc.getNthDataConstructor(i));
                    }
                }
                return children.toArray();
            }
            else if (parent instanceof TypeClass && 
                    (alwaysGetChildren || this.moduleContentProvider.getShowElementHierarchy())){
                TypeClass tc = (TypeClass) parent;
                final int nClassMethods = tc.getNClassMethods();
                ArrayList<ClassMethod> children = new ArrayList<ClassMethod>(nClassMethods);
                for(int i = 0; i < nClassMethods; ++i){
                    if (moduleContentProvider.getShowPrivateElements() ||
                        tc.getNthClassMethod(i).getScope() != Scope.PRIVATE){
                        children.add(tc.getNthClassMethod(i));
                    }
                }
                return children.toArray();
            }
            else if (parent instanceof ModuleElementsByType){
                return ((ModuleElementsByType) parent).getChildren();
            }
            else{
                return new Object[0];
            }
        }
        
        public boolean hasChildren(Object parent) {
            if (parent instanceof HierarchicalNode){
                return ((HierarchicalNode) parent).hasChildren();
            }
            else if (parent instanceof ModuleName){
                ModuleName moduleName = (ModuleName) parent;
                ModuleTypeInfo mti = this.moduleContentProvider.getCALModelManager().getModuleTypeInfo(moduleName);
                if (mti == null){
                    // module has no definitions in it. This can happen.
                    return false;
                }
                return
                    mti.getNFunctions() > 0 ||
                    mti.getNTypeClasses() > 0 ||
                    mti.getNTypeConstructors() > 0 || 
                    mti.getNClassInstances() > 0;
            }
            else if (parent instanceof ModuleElementsByType){
                return ((ModuleElementsByType) parent).hasChildren();
            }
            else if (parent instanceof TypeConstructor){
                if (!this.moduleContentProvider.getShowElementHierarchy()){
                    return false;
                }
                
                TypeConstructor tc = (TypeConstructor) parent;
                if (moduleContentProvider.getShowPrivateElements()){
                    return tc.getNDataConstructors() > 0;
                }
                else{
                    for(int i = 0; i < tc.getNDataConstructors(); ++i){
                        if (tc.getNthDataConstructor(i).getScope() != Scope.PRIVATE){
                            return true;
                        }
                    }
                    return false;
                }
            }
            else if (parent instanceof TypeClass){
                if (!this.moduleContentProvider.getShowElementHierarchy()){
                    return false;
                }
                
                TypeClass tc = (TypeClass) parent;
                if (moduleContentProvider.getShowPrivateElements()){
                    return tc.getNClassMethods() > 0;
                }
                else{
                    for(int i = 0; i < tc.getNClassMethods(); ++i){
                        if (tc.getNthClassMethod(i).getScope() != Scope.PRIVATE){
                            return true;
                        }
                    }
                    return false;
                }
            }
            else{
                return false;
            }
        }
        
        /**
         * Remove any cached information associated with the given module.
         * 
         * @param moduleName the name of the module that has changed.
         */
        private void moduleHasChanged(ModuleName moduleName){
            moduleSourceDefinitionToModuleElementByTypeMap_functions.remove(moduleName);
            moduleSourceDefinitionToModuleElementByTypeMap_typeClasses.remove(moduleName);
            moduleSourceDefinitionToModuleElementByTypeMap_typeConstructors.remove(moduleName);
            moduleSourceDefinitionToModuleElementByTypeMap_classInstances.remove(moduleName);
        }
        
        /*
         * We will set up a dummy model to initialize tree heararchy.
         * In a real code, you will connect to a real model and
         * expose its hierarchy.
         */
        
        protected void initialize() {
            if (this.moduleContentProvider.getShowModuleHierarchy()){
                // Hierarchical modules
                HierarchicalNode root = new HierarchicalNode("Invisible Root", this.root);
                invisibleRoot = root;
                for (final Object element : calModelManager.getModuleNames()) {
                    root.add((ModuleName) element);
                }
            }
            else{
                // Flat modules
                invisibleRoot = calModelManager.getModuleNames();
            }
            
            moduleSourceDefinitionToModuleElementByTypeMap_functions.clear();
            moduleSourceDefinitionToModuleElementByTypeMap_typeClasses.clear();
            moduleSourceDefinitionToModuleElementByTypeMap_typeConstructors.clear();
            moduleSourceDefinitionToModuleElementByTypeMap_classInstances.clear();
        }
        
        private void hookIntoCompiler(){
            CALBuilder.addListener(new CALBuilder.BuildMessagesListener(){
                public void notify(Collection<ModuleName> modulesWithErrors) {
                    for (final ModuleName moduleName : modulesWithErrors) {
                        refreshModule(moduleName, false);
                    }
                }
            });
            // When the compiled modules change then update the model
            calModelManager.getProgramModelManager().addStatusListener(
                    compileStatusListener = new StatusListener.StatusListenerAdapter() {

                        @Override
                        public void setModuleStatus(StatusListener.Status.Module moduleStatus, ModuleName moduleName) {
                            if (viewer != null && moduleStatus == StatusListener.SM_LOADED){
                                refreshModule(moduleName, false);
                            }
                        }
                    }
            );
        }
        
        private void refreshModule(ModuleName moduleName, final boolean updateOnlyModuleName) {
            try{
                final ModuleSourceDefinition msd = calModelManager.getModuleSourceDefinition(moduleName);
                viewer.getControl().getDisplay().asyncExec(
                        new Runnable(){
                            public void run() {
                                if (msd != null){  // msd is null if using an embedded editor
                                    moduleHasChanged(msd.getModuleName());
                                    refreshForModule (msd, updateOnlyModuleName);
                                    calBuilderWasEnabled = true;
                                }
                            }                                                
                        });
            }
            catch(org.eclipse.swt.SWTException ex){
                /*
                 * For some reason if the object is not visible and a refresh is called for
                 * this can through a disposed error.
                 */
                if (ex.code == SWT.ERROR_WIDGET_DISPOSED){
                    // ignore
                }
            }
        }

        protected abstract void refreshForModule (ModuleSourceDefinition moduleSourceDefinition, boolean updateOnlyModule);
        
    }
