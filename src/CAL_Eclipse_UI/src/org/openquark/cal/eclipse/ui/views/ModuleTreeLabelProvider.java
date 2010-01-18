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
 * ModuleTreeLabelProvider.java
 * Creation date: Jan 22 2007
 * By: Greg McClement
 */
package org.openquark.cal.eclipse.ui.views;

import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.swt.graphics.Image;
import org.openquark.cal.compiler.ClassInstance;
import org.openquark.cal.compiler.ModuleName;
import org.openquark.cal.compiler.ModuleSourceDefinition;
import org.openquark.cal.compiler.ModuleTypeInfo;
import org.openquark.cal.compiler.ScopedEntityNamingPolicy;
import org.openquark.cal.eclipse.ui.CALEclipseUIPlugin;
import org.openquark.cal.eclipse.ui.CALUIMessages;
import org.openquark.cal.eclipse.ui.util.Messages;
import org.openquark.cal.eclipse.ui.views.CALWorkspace.ModuleElementsByType;


/**
 * The label provider for the CAL Workspace and the Outline view of CAL files.
 */
public class ModuleTreeLabelProvider extends LabelProvider {

    /**
     * Show the form of the name with the module hierarchy.
     */
    private final CALModuleContentProvider moduleContentProvider;
    private final ScopedEntityLabelProvider scopedEntityLabelProvider = new ScopedEntityLabelProvider();
    
    private static Image image_nav_vault = null;
    private static Image image_nav_namespace = null;
    private static Image image_nav_module = null;
    private static Image image_nav_classinstance = null;
    
    public ModuleTreeLabelProvider(CALModuleContentProvider moduleContentProvider) {
        this.moduleContentProvider = moduleContentProvider;
    }

    public String getText(Object obj) {
        if (obj instanceof HierarchicalNode){
            HierarchicalNode hn = (HierarchicalNode) obj;
            return hn.getName();
        }
        else if (obj instanceof ModuleElementsByType){
            return ((ModuleElementsByType) obj).getName();
        }
        if (obj instanceof ModuleName){
            ModuleName moduleName = (ModuleName) obj;
            String moduleNameString;
            if (moduleContentProvider.getShowModuleHierarchy()){
                moduleNameString = moduleName.getLastComponent();
            }
            else{
                ModuleName prefix = moduleName.getImmediatePrefix();
                if (prefix == null){
                    moduleNameString = moduleName.toString();                    
                }
                else{
                    moduleNameString = Messages.format(CALUIMessages.CALWorkspace_ModuleName, new Object[] {moduleName.getLastComponent(), prefix});
                }
            }
            
            if (moduleContentProvider.getCALModelManager().getModuleTypeInfo(moduleName) == null){
                return Messages.format(CALUIMessages.CALWorkspace_CompileFailed, moduleNameString);
            }
            else{
                return moduleNameString;
            }
        }
        else if (obj instanceof ClassInstance){
            ClassInstance ci = (ClassInstance) obj;
            
            ModuleTypeInfo moduleTypeInfo = moduleContentProvider.getCALModelManager().getModuleTypeInfo(ci.getModuleName());
            if (moduleTypeInfo != null){
                final ScopedEntityNamingPolicy namingPolicy = new ScopedEntityNamingPolicy.UnqualifiedUnlessAmbiguous (moduleTypeInfo);
                return ci.getNameWithContext(namingPolicy).toString();    
            }
            return ci.getNameWithContext().toString();
        }
        else{
            return scopedEntityLabelProvider.getText(obj);
        }
    }
    
    public Image getImage(Object obj) {
        return getImageInternal(obj);
    }
    
    private Image getImageInternal(Object obj) {
        if (obj instanceof HierarchicalNode){
            if (image_nav_namespace == null){
                image_nav_namespace = CALEclipseUIPlugin.getImageDescriptor("/icons/nav_namespace.png").createImage();
            }
            return image_nav_namespace;
        }
        else if (obj instanceof ModuleElementsByType){
            if (image_nav_vault == null){
                image_nav_vault = CALEclipseUIPlugin.getImageDescriptor("/icons/nav_vault.gif").createImage();            
            }
            return image_nav_vault;
        }
        else if (obj instanceof ModuleSourceDefinition || obj instanceof ModuleName){
            if (image_nav_module == null){
                image_nav_module = CALEclipseUIPlugin.getImageDescriptor("/icons/nav_module.png").createImage();
            }
            return image_nav_module;
        }
        else if (obj instanceof ClassInstance){
            if (image_nav_classinstance == null){
                image_nav_classinstance = CALEclipseUIPlugin.getImageDescriptor("/icons/nav_classinstance.gif").createImage();
            }
            return image_nav_classinstance;
        }
        else{
            return scopedEntityLabelProvider.getImage(obj);
        }
    }

}