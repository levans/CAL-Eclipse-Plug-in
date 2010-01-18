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
 * CALWorkspace.java
 * Creation date: Jan 22 2007
 * By: Greg McClement
 */

package org.openquark.cal.eclipse.ui.views;

import java.util.LinkedList;
import java.util.List;

import org.eclipse.core.resources.IStorage;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.ListenerList;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IMenuListener;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.viewers.DecoratingLabelProvider;
import org.eclipse.jface.viewers.DoubleClickEvent;
import org.eclipse.jface.viewers.IDoubleClickListener;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.TreePath;
import org.eclipse.jface.viewers.TreeSelection;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.ViewerSorter;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.ui.IActionBars;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IMemento;
import org.eclipse.ui.IViewSite;
import org.eclipse.ui.IWorkbenchActionConstants;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.part.DrillDownAdapter;
import org.eclipse.ui.part.ViewPart;
import org.openquark.cal.compiler.ClassInstance;
import org.openquark.cal.compiler.CompilerMessageLogger;
import org.openquark.cal.compiler.MessageLogger;
import org.openquark.cal.compiler.ModuleName;
import org.openquark.cal.compiler.ScopedEntity;
import org.openquark.cal.compiler.SearchResult;
import org.openquark.cal.compiler.SourceMetricsManager;
import org.openquark.cal.compiler.SourceRange;
import org.openquark.cal.compiler.SearchResult.Precise;
import org.openquark.cal.eclipse.core.CALModelManager;
import org.openquark.cal.eclipse.ui.CALEclipseUIPlugin;
import org.openquark.cal.eclipse.ui.caleditor.CALEditor;
import org.openquark.cal.eclipse.ui.util.CoreUtility;



/**
 * This sample class demonstrates how to plug-in a new
 * workbench view. The view shows data obtained from the
 * model. The sample creates a dummy model on the fly,
 * but a real implementation would connect to the model
 * available either in this or another plug-in (e.g. the workspace).
 * The view is connected to the model using a content provider.
 * <p>
 * The view uses a label provider to define how model
 * objects should be presented in the view. Each
 * view can present the same model objects using
 * different labels and icons, if needed. Alternatively,
 * a single label provider can be shared between views
 * in order to ensure that objects of the same type are
 * presented in the same way everywhere.
 * <p>
 */

public class CALWorkspace extends ViewPart implements CALEditor.SelectedEntitiesChanged {
    TreeViewer viewer;
    private DrillDownAdapter drillDownAdapter;
    private Action doubleClickAction;
    private ModuleTreeContentProvider moduleTreeContentProvider;

    private final CALModelManager calModelManager = CALModelManager.getCALModelManager();

    public String[] getHierarchicalNameComponents(String moduleName){
        return moduleName.split("\\.");
    }    

    /**
     * If true all the hierarchical names for part of a tree in the UI. If false then the modules
     * are all show at the top level with the complete module path in the name.
     */
    boolean showModuleHierarchy = true;
    
    boolean showElementHierarchy = true;

    boolean showPrivateElements = true;
    
    // reserved for future use. not currently implemented. 
    boolean linkWithEditor = false;

    /**
     * List of all the workspaces that are listening to changed in the current editor selection.
     */
    private static ListenerList workspaces = new ListenerList();
        
    static class ModuleElementsByType{
        private ModuleName moduleName;
        private String name;
        private Object[] children;
        
        public ModuleElementsByType(ModuleName moduleName, String name, Object[] children){
            this.moduleName = moduleName;
            this.name = name;
            this.children = children;
        }
        
        public String getName(){
            return name;
        }

        public Object getParent(){
            return moduleName;
        }
        
        public Object[] getChildren(){
            return children;
        }
        
        public boolean hasChildren(){
            return children.length > 0;
        }
    }
    
    static class NameSorter extends ViewerSorter {
    }

    /**
     * The constructor.
     */
    public CALWorkspace() {
        workspaces.add(this);        
    }

    @Override
    public void dispose(){
        workspaces.remove(this);
    }
    
    CALModuleContentProvider calModuleContentProvider = new CALModuleContentProvider() {
        @Override
        public boolean getShowModuleHierarchy() {
            return showModuleHierarchy;
        }

        @Override
        public boolean getShowElementHierarchy() {
            return showElementHierarchy;
        }
        
        @Override
        public boolean getShowPrivateElements() {
            return showPrivateElements;
        }

        @Override
        public boolean getLinkWithEditor() {
            return linkWithEditor;
        }

        @Override
        public void setShowModuleHierarchy(boolean value) {
            showModuleHierarchy = value;
        }

        @Override
        public void setShowElementHierarchy(boolean value) {
            showElementHierarchy = value;
        }
        
        @Override
        public void setShowPrivateElements(boolean value) {
            showPrivateElements = value;
        }
        
        @Override
        public void setLinkWithEditor(boolean value) {
            linkWithEditor = value;
        }
        
        @Override
        public CALModelManager getCALModelManager() {
            return calModelManager;
        }
    };
    
    /**
     * This is a callback that will allow us
     * to create the viewer and initialize it.
     * @param parent 
     */
    @Override
    public void createPartControl(Composite parent) {
        viewer = new TreeViewer(parent, SWT.MULTI | SWT.H_SCROLL | SWT.V_SCROLL);
        drillDownAdapter = new DrillDownAdapter(viewer);
        DecoratingLabelProvider labelProvider = 
            new DecoratingLabelProvider(
                    new DecoratingLabelProvider(
                            new ModuleTreeLabelProvider(calModuleContentProvider),
                            new ScopeDecorator()),
                            new ProblemMarkerDecorator());
        viewer.setContentProvider(moduleTreeContentProvider = new WorkspaceTreeContentProvider (calModuleContentProvider, labelProvider, viewer));
        viewer.setLabelProvider(labelProvider);
        viewer.setSorter(new NameSorter());
        viewer.setInput(moduleTreeContentProvider.getRoot ());
        makeActions();
        hookContextMenu();
        hookDoubleClickAction();
        contributeToActionBars();
        
        getSite ().setSelectionProvider (viewer);
    }

    private void hookContextMenu() {
        MenuManager menuMgr = new MenuManager("#PopupMenu");
        menuMgr.setRemoveAllWhenShown(true);
        menuMgr.addMenuListener(new IMenuListener() {
            public void menuAboutToShow(IMenuManager manager) {
                CALWorkspace.this.fillContextMenu(manager);
            }
        });
        Menu menu = menuMgr.createContextMenu(viewer.getControl());
        viewer.getControl().setMenu(menu);
        getSite().registerContextMenu(menuMgr, viewer);
    }

    private void contributeToActionBars() {
        IActionBars bars = getViewSite().getActionBars();
        fillLocalPullDown(bars.getMenuManager());
//        fillLocalToolBar(bars.getToolBarManager());
    }

    private void fillLocalPullDown(IMenuManager manager) {
        moduleTreeContentProvider.fillLocalPullDown(manager);
    }

    private void fillContextMenu(IMenuManager manager) {
        moduleTreeContentProvider.fillContextMenu(manager);
        manager.add(new Separator());
        drillDownAdapter.addNavigationActions(manager);
        // Other plug-ins can contribute there actions here
        manager.add(new Separator(IWorkbenchActionConstants.MB_ADDITIONS));
        manager.add(new Separator(IWorkbenchActionConstants.MB_ADDITIONS + "-end"));
    }
    
    private void makeActions() {
        doubleClickAction = new Action() {
            @Override
            public void run() {
                ISelection selection = viewer.getSelection();
                Object obj = ((IStructuredSelection)selection).getFirstElement();
                if (obj instanceof ModuleName){
                    IStorage definitionFile = calModelManager.getInputSourceFile((ModuleName) obj);
                    try {
                        CoreUtility.openInEditor(definitionFile, true);
                    } catch (PartInitException e) {
                        CALEclipseUIPlugin.log(new Status(IStatus.ERROR, CALEclipseUIPlugin.PLUGIN_ID, IStatus.OK, "", e)); //$NON-NLS-1$
                        return;
                    }
                }
                else if (obj instanceof ScopedEntity){
                    ScopedEntity scopedEntity = (ScopedEntity) obj;
                    CoreUtility.showCALElementInEditor(scopedEntity);
                }
                else if (obj instanceof ClassInstance){
                    CoreUtility.showCALElementInEditor((ClassInstance) obj);
                }
                else{
                }
            }
        };
    }
    
    private void hookDoubleClickAction() {
        viewer.addDoubleClickListener(new IDoubleClickListener() {
            public void doubleClick(DoubleClickEvent event) {
                doubleClickAction.run();
            }
        });
    }

    /**
     * Passing the focus request to the viewer's control.
     */
    @Override
    public void setFocus() {
        if (viewer != null){
            viewer.getControl().setFocus();
        }
    }
    
    @Override
    public void init(IViewSite site, IMemento memento) throws PartInitException {
        super.init(site, memento);
        if (memento != null){
            calModuleContentProvider.loadState(memento);
        }
    }

    @Override
    public void saveState(IMemento memento){
        if (memento != null){
            calModuleContentProvider.saveState(memento);
        }
    }

    public static Object[] getSelectedEntitiesChangedListeners(){
        return workspaces.getListeners();
    }

    public void selectedEntitiesChanged(LinkedList<Object> pathToEntity) {
        if (linkWithEditor){
            TreeSelection treeSelection = new TreeSelection(new TreePath(pathToEntity.toArray()));
            viewer.setSelection(treeSelection);
        }
    }
}
