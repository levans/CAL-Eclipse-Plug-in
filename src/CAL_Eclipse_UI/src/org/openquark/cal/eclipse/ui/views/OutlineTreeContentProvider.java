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
 * OutlineTreeContentProvider.java
 * Created: 28-Mar-07
 * By: Rick Cameron
 */


package org.openquark.cal.eclipse.ui.views;

import org.eclipse.core.resources.IStorage;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerComparator;
import org.openquark.cal.compiler.ClassInstance;
import org.openquark.cal.compiler.ModuleName;
import org.openquark.cal.compiler.ModuleSourceDefinition;
import org.openquark.cal.eclipse.core.CALModelManager;
import org.openquark.cal.eclipse.core.builder.CALBuilder;
import org.openquark.cal.eclipse.ui.actions.ActionMessages;
import org.openquark.cal.eclipse.ui.caleditor.CALEditor;
import org.openquark.cal.eclipse.ui.util.Messages;

/**
 * @author rcameron
 *
 */
public final class OutlineTreeContentProvider extends ModuleTreeContentProvider {

    private final CALEditor textEditor;
    
    /**
     * Constructor OutlineTreeContentProvider
     *
     * @param textEditor
     * @param moduleContentProvider
     * @param viewer
     */
    public OutlineTreeContentProvider (CALEditor textEditor, CALModuleContentProvider moduleContentProvider, TreeViewer viewer, ILabelProvider labelProvider) {
        super (moduleContentProvider, labelProvider, viewer);
        
        this.textEditor = textEditor;
        
        makeActions ();
    }
    
    private void makeActions () {
    }

    @Override
    public Object [] getChildren(Object parent) {
        // Want the module name to show up at the top with no children for the outline view.
        if (parent instanceof ModuleName){
            return new Object[0];
        }
        else{
            return super.getChildren(parent);
        }
    }
    
    @Override
    public boolean hasChildren(Object parent) {
        // Want the module name to show up at the top with no children for the outline view.
        if (parent instanceof ModuleName){
            return false;
        }
        else{
            return super.hasChildren(parent);
        }
    }
    /**
     * @see org.openquark.cal.eclipse.ui.views.ModuleTreeContentProvider#getRootElements()
     */
    @Override
    protected Object[] getRootElements () {
        if (CALBuilder.isEnabled()){
            IStorage storage = textEditor.getStorage();
            try{
                ModuleName moduleName = CALModelManager.getCALModelManager().getModuleName(storage);
                if (moduleName == null){
                    return new Object[] {Messages.format(ActionMessages.error_invalidFileName_message, storage.getName())};
                }
                final Object[] children = super.getChildren(moduleName);
                final Object[] all = new Object[children.length + 1];
                all[0] = moduleName;
                System.arraycopy(children, 0, all, 1, children.length);            
                return all;
            }
            catch(IllegalArgumentException e){
                // Probably the name of the file makes it a CAL file but the file is not 
                // in the correct place in the hierarchy.
                return new Object[] {ActionMessages.error_calFileNotInCorrectLocation_message};
            }            
        }
        else{
            // CAL Builder is not enabled
            setCalBuilderWasEnabled (false);
            return new Object[] {ActionMessages.error_calBuilderNotEnabled_message};
        }
    }

    /**
     * @see org.openquark.cal.eclipse.ui.views.ModuleTreeContentProvider#fillLocalPullDown(org.eclipse.jface.action.IMenuManager)
     */
    @Override
    public void fillLocalPullDown (IMenuManager manager) {
        addStandardLocalMenuActions (manager);
    }

    /**
     * @see org.openquark.cal.eclipse.ui.views.ModuleTreeContentProvider#fillContextMenu(org.eclipse.jface.action.IMenuManager)
     */
    @Override
    public void fillContextMenu (IMenuManager manager) {
        addStandardContextMenuActions (manager);
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
            // In the outline view we want the module name to always appear first
            if (e1 instanceof ModuleName){
                return -1;
            }
            else if (e2 instanceof ModuleName){
                return 1;
            }
            return super.compare(viewer, adjustName(e1), adjustName(e2));
        }
    };
    
    @Override
    public ViewerComparator getComparator(){
        return viewerComparator;            
    }

    /**
     * @see org.openquark.cal.eclipse.ui.views.ModuleTreeContentProvider#refreshForModule(org.openquark.cal.compiler.ModuleSourceDefinition, boolean)
     */
    @Override
    protected void refreshForModule (ModuleSourceDefinition moduleSourceDefinition, boolean updateOnlyModule) {
        if (!getViewer().getControl().isDisposed()){
            if (updateOnlyModule){
                getViewer().update(moduleSourceDefinition.getModuleName(), null);
            }
            else{
                getViewer().refresh(true);
            }
        }
    }

}
