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
 * WorkspaceTreeContentProvider.java
 * Created: 28-Mar-07
 * By: Rick Cameron
 */


package org.openquark.cal.eclipse.ui.views;

import java.util.Collection;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceChangeEvent;
import org.eclipse.core.resources.IResourceChangeListener;
import org.eclipse.core.resources.IResourceDelta;
import org.eclipse.core.resources.IResourceDeltaVisitor;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.jface.viewers.TreeViewer;
import org.openquark.cal.compiler.ModuleName;
import org.openquark.cal.compiler.ModuleSourceDefinition;
import org.openquark.cal.eclipse.core.builder.CALBuilder;
import org.openquark.cal.eclipse.ui.CALUIMessages;
import org.openquark.cal.eclipse.ui.actions.ActionMessages;
import org.openquark.cal.services.CALSourcePathMapper;



/**
 * @author rcameron
 *
 */
final class WorkspaceTreeContentProvider extends ModuleTreeContentProvider {

    /**
     * Helper class to visit a delta and return whether anything in the delta corresponds to a deleted .cal file.
     * @author Edward Lam
     */
    private static class RemovedCalFileFinderDeltaVisitor implements IResourceDeltaVisitor {
        private boolean removedCalFileFound = false;

        public boolean visit(IResourceDelta delta) {
            //only interested in changed resources (not added or removed)
            if (delta.getKind() != IResourceDelta.REMOVED) {
                return true;
            }
            IResource resource = delta.getResource();
            if (resource.getType() == IResource.FILE && CALSourcePathMapper.CAL_FILE_EXTENSION.equalsIgnoreCase(resource.getFileExtension())) {
                removedCalFileFound = true;
            }
            return true;
        }
        
        boolean wasRemovedCalFileFound() {
            return removedCalFileFound;
        }
    };
    
    protected Action showHierarchyOfModulesAction;

    /**
     * Listens for deleted projects so the workspaceview can be updated.
     */
    private final IResourceChangeListener resourceChangeListener = new IResourceChangeListener () {

        public void resourceChanged (IResourceChangeEvent event) {
            /*
             * TODO: we could be smarter about what we update here.
             * Since we have the resource change event, we could traverse the delta and just find out which .cal files 
             * were added or removed, while still handling events like open and close projects.
             */
            
            boolean updateViewer = false;
            IResourceDelta[] deleted = event.getDelta ().getAffectedChildren (IResourceDelta.REMOVED);
            // Projects that are removed
            if (deleted.length > 0) {
                Object maybeProject = deleted[0].getResource ();
                if (maybeProject instanceof IProject) {
                    updateViewer = true;
                }
            } else {
                // Opens or closes of the project
                IResourceDelta[] changed = event.getDelta ().getAffectedChildren (IResourceDelta.CHANGED);
                if (changed.length > 0 && (changed[0].getFlags () & IResourceDelta.OPEN) > 0) {
                    updateViewer = true;
                }
            }
            if (!updateViewer) {
                RemovedCalFileFinderDeltaVisitor removedCalFileFinderDeltaVisitor = new RemovedCalFileFinderDeltaVisitor();
                try {
                    event.getDelta().accept(removedCalFileFinderDeltaVisitor);
                } catch (CoreException e) {
                    //open error dialog with syncExec or print to plugin log file
                }
                updateViewer = removedCalFileFinderDeltaVisitor.wasRemovedCalFileFound();
            }

            if (updateViewer) {
                initialize ();
                setCalBuilderWasEnabled (true);
                getViewer ().getControl ().getDisplay ().asyncExec (new Runnable () {

                    public void run () {
                        getViewer ().refresh (true);
                    }
                });
            }
            
        }

    };


    /**
     * Constructor WorkspaceTreeContentProvider
     * @param moduleContentProvider
     * @param viewer
     */
    public WorkspaceTreeContentProvider (CALModuleContentProvider moduleContentProvider, ILabelProvider labelProvider, TreeViewer viewer) {
        super (moduleContentProvider, labelProvider, viewer);
        
        makeActions ();
    }
    
    /**
     * @see org.openquark.cal.eclipse.ui.views.ModuleTreeContentProvider#initialize()
     */
    @Override
    protected void initialize () {
        super.initialize ();
        
        ResourcesPlugin.getWorkspace().addResourceChangeListener(resourceChangeListener, IResourceChangeEvent.POST_CHANGE);
    }
    
    /**
     * @see org.openquark.cal.eclipse.ui.views.ModuleTreeContentProvider#dispose()
     */
    @Override
    public void dispose () {
        super.dispose ();
        
        ResourcesPlugin.getWorkspace().removeResourceChangeListener(resourceChangeListener);
    }
    
    private void makeActions () {
        showHierarchyOfModulesAction = new Action("", IAction.AS_CHECK_BOX) {
            @Override
            public void run() {
                setShowModuleHierarchy(isChecked());
            }
        };
        showHierarchyOfModulesAction.setText(CALUIMessages.CALWorkspace_showHierarchyOfModules);
        showHierarchyOfModulesAction.setChecked(getModuleContentProvider ().getShowModuleHierarchy());
        showHierarchyOfModulesAction.setToolTipText(CALUIMessages.CALWorkspace_showHierarchyOfModules_tooltip);
//        showHierarchyOfModulesAction.setImageDescriptor(PlatformUI.getWorkbench().getSharedImages().
//                getImageDescriptor(ISharedImages.IMG_OBJS_INFO_TSK));
    }

    /**
     * @see org.openquark.cal.eclipse.ui.views.ModuleTreeContentProvider#getRootElements()
     */
    @Override
    protected Object[] getRootElements () {
        if (getInvisibleRoot () == null) {
            initialize ();
        }

        if (CALBuilder.isEnabled ()) {
            if (getInvisibleRoot () instanceof Collection) {
                Collection<?> collection = ((Collection<?>)getInvisibleRoot ());
                return collection.toArray ();
            } else if (getInvisibleRoot () instanceof HierarchicalNode) {
                return ((HierarchicalNode)getInvisibleRoot ()).getChildren ();
            } else {
                return new Object[0];
            }
        } else {
            // CAL Builder is not enabled
            setCalBuilderWasEnabled (false);
            return new Object[] { ActionMessages.error_calBuilderNotEnabled_message };
        }
    }

    /**
     * @see org.openquark.cal.eclipse.ui.views.ModuleTreeContentProvider#fillLocalPullDown(org.eclipse.jface.action.IMenuManager)
     */
    @Override
    public void fillLocalPullDown (IMenuManager manager) {
        manager.add(showHierarchyOfModulesAction);
        
        addStandardLocalMenuActions (manager);
    }

    /**
     * @see org.openquark.cal.eclipse.ui.views.ModuleTreeContentProvider#fillContextMenu(org.eclipse.jface.action.IMenuManager)
     */
    @Override
    public void fillContextMenu (IMenuManager manager) {
        manager.add(showHierarchyOfModulesAction);
        
        addStandardContextMenuActions (manager);
    }

    private void refreshAncestors(Object object){
        Object parent = getParent(object);
        while(parent instanceof HierarchicalNode){
            getViewer().update(parent, null);
            parent = getParent(parent);            
        }
    }
    /**
     * @see org.openquark.cal.eclipse.ui.views.ModuleTreeContentProvider#refreshForModule(org.openquark.cal.compiler.ModuleSourceDefinition, boolean)
     */
    @Override
    protected void refreshForModule (ModuleSourceDefinition moduleSourceDefinition, boolean updateOnlyModule) {
        final ModuleName moduleName = moduleSourceDefinition.getModuleName();
        if (wasCalBuilderEnabled () && null != getViewer ().testFindItem(moduleName)){
            if (updateOnlyModule){
                getViewer().update(moduleName, null);
            }
            else{
                getViewer().refresh(moduleName, true);
            }
            refreshAncestors(moduleName);
        }
        else {
            initialize ();
            if (!getViewer().getControl().isDisposed()){
                getViewer().refresh(true);
            }
        }
    }

}
