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
 * HierarchicalNode.java
 * Creation date: Jan 22 2007
 * By: Greg McClement
 */

package org.openquark.cal.eclipse.ui.views;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.openquark.cal.compiler.ModuleName;

/**
 * Represents a node in the CAL Workspace view. These are only used when show element hierarchy is on.
 */
public class HierarchicalNode{
    private String name;
    private Object parent;
    private Collection<ModuleName> child_modules = new ArrayList<ModuleName>();
    private Map<String, HierarchicalNode> child_hierarchicalNamesMap = new HashMap<String, HierarchicalNode>();
    
    public HierarchicalNode(String name, Object parent){
        this.name = name; 
        this.parent = parent;
    }
    
    public String getName(){
        return name;
    }
    
    public Object getParent(){
        return parent;
    }
    
    public HierarchicalNode getParent(ModuleName moduleName){
        // make sure that the structure is set up for this module.
        add(moduleName);
        
        // search the tree for the parent node
        HierarchicalNode currentNode = this;
        for(int i = 0; i < moduleName.getNComponents() - 1; ++i){
            currentNode = currentNode.child_hierarchicalNamesMap.get(moduleName.getNthComponent(i));
            if (currentNode == null){
                return null;
            }
        }
        return currentNode;
    }
    
    public boolean hasChildren(){
        return child_hierarchicalNamesMap.size() > 0 || child_modules.size() > 0; 
    }

    public Object[] getChildren(){
        ArrayList<Object> children = new ArrayList<Object>(child_modules.size() + child_hierarchicalNamesMap.size());
        
        for(Iterator<HierarchicalNode> i = child_hierarchicalNamesMap.values().iterator(); i.hasNext();){
            children.add(i.next());
        }
        
        for(Iterator<ModuleName> i = child_modules.iterator(); i.hasNext();){
            children.add(i.next());
        }
                    
        return children.toArray();
    }
    
    /**
     * This is idempotent so you can add the same module more than once but for subsequent additions
     * there will be no changes.
     * @param moduleName The name of the module to add.
     */
    public void add(ModuleName moduleName){
        add(0, moduleName, "");
    }
    
    private void add(int currentParent, ModuleName moduleName, String pathFromRoot){
        int nextParent = currentParent+1;
        if (nextParent == moduleName.getNComponents()){
            if (!child_modules.contains(moduleName)){
                child_modules.add(moduleName);
            }
        }
        else{
            String currentName = moduleName.getNthComponent(currentParent);
            String nextPathFromRoot = pathFromRoot + "." + currentName;
            HierarchicalNode hn = child_hierarchicalNamesMap.get(currentName);
            if (hn == null){
                hn = new HierarchicalNode(currentName, this);
                child_hierarchicalNamesMap.put(currentName, hn);
                hn.add(nextParent, moduleName, nextPathFromRoot);
            }
            else{
                hn.add(nextParent, moduleName, nextPathFromRoot);
            }
        }
    }
}