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
 * CALModuleContentProvider.java
 * Creation date: Jan 22 2007
 * By: Greg McClement
 */

package org.openquark.cal.eclipse.ui.views;

import org.eclipse.ui.IMemento;
import org.openquark.cal.eclipse.core.CALModelManager;


public abstract class CALModuleContentProvider{
    public abstract boolean getShowModuleHierarchy();   
    public abstract boolean getShowElementHierarchy();
    public abstract boolean getShowPrivateElements();
    public abstract boolean getLinkWithEditor();
    
    public abstract void setShowModuleHierarchy(boolean value);   
    public abstract void setShowElementHierarchy(boolean value);
    public abstract void setShowPrivateElements(boolean value);
    public abstract void setLinkWithEditor(boolean value);
    
    public abstract CALModelManager getCALModelManager();
    
    private static final String TAG_showModuleHierarchy = "showModuleHierarchy";
    private static final String TAG_showElementHierarchy = "showElementHierarchy";
    private static final String TAG_showPrivateElements = "showPrivateElements";    
    private static final String TAG_linkWithEditor = "linkWithEditor";    
    
    public void saveState(IMemento memento) {
        memento.putInteger(TAG_showModuleHierarchy, getShowModuleHierarchy() ? 1 : 0);
        memento.putInteger(TAG_showElementHierarchy, getShowElementHierarchy() ? 1 : 0);
        memento.putInteger(TAG_showPrivateElements, getShowPrivateElements() ? 1 : 0);
        memento.putInteger(TAG_linkWithEditor, getLinkWithEditor() ? 1 : 0);
    }

    public void loadState(IMemento memento){
        {
            Integer value = memento.getInteger(TAG_showModuleHierarchy);
            if (value == null){
                setShowModuleHierarchy(true);
            }
            else{
                setShowModuleHierarchy(value.intValue() == 1 ? true : false);
            }
        }

        {
            Integer value = memento.getInteger(TAG_showElementHierarchy);
            if (value == null){
                setShowElementHierarchy(true);
            }
            else{
                setShowElementHierarchy(value.intValue() == 1 ? true : false);
            }
        }

        {
            Integer value = memento.getInteger(TAG_showPrivateElements);
            if (value == null){
                setShowPrivateElements(true);
            }
            else{
                setShowPrivateElements(value.intValue() == 1 ? true : false);
            }
        }

        {
            Integer value = memento.getInteger(TAG_linkWithEditor);
            if (value == null){
                setLinkWithEditor(true);
            }
            else{
                setLinkWithEditor(value.intValue() == 1 ? true : false);
            }
        }
    }
}