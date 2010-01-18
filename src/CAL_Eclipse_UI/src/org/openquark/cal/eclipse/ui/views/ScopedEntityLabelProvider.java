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
 * ScopedEntityLabelProvider.java
 * Creation date: June 19 2007
 * By: Greg McClement
 */
package org.openquark.cal.eclipse.ui.views;

import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.swt.graphics.Image;
import org.eclipse.ui.ISharedImages;
import org.eclipse.ui.PlatformUI;
import org.openquark.cal.compiler.ClassMethod;
import org.openquark.cal.compiler.DataConstructor;
import org.openquark.cal.compiler.Function;
import org.openquark.cal.compiler.ScopedEntity;
import org.openquark.cal.compiler.TypeClass;
import org.openquark.cal.compiler.TypeConstructor;
import org.openquark.cal.eclipse.ui.CALEclipseUIPlugin;


/**
 * The label provider for the CAL Workspace and the Outline view of CAL files.
 */
public class ScopedEntityLabelProvider extends LabelProvider {

    private static Image image_nav_function = null;
    private static Image image_nav_typeconstructor = null;
    private static Image image_nav_typeclass = null;
    private static Image image_nav_dataconstructor = null;
    
    public ScopedEntityLabelProvider() {
    }

    @Override
    public String getText(Object obj) {
        if (obj instanceof ScopedEntity){
            return ((ScopedEntity) obj).getName().getUnqualifiedName().toString();
        }
        else if (obj instanceof DataConstructor){
            DataConstructor dc = (DataConstructor) obj;
            return dc.getName().toString();
        }
        else{
            return obj.toString();
        }
    }
    
    @Override
    public Image getImage(Object obj) {
        return getImageInternal(obj);
    }
    
    private static Image getImageInternal(Object obj) {
        if (obj instanceof Function || obj instanceof ClassMethod){
            if (image_nav_function == null){
                image_nav_function = CALEclipseUIPlugin.getImageDescriptor("/icons/nav_function.gif").createImage();
            }
            return image_nav_function;
        }
        else if (obj instanceof TypeConstructor){
            if (image_nav_typeconstructor == null){
                image_nav_typeconstructor = CALEclipseUIPlugin.getImageDescriptor("/icons/nav_typeconstructor.gif").createImage();
            }
            return image_nav_typeconstructor;
        }
        else if (obj instanceof TypeClass){
            if (image_nav_typeclass == null){
                image_nav_typeclass = CALEclipseUIPlugin.getImageDescriptor("/icons/nav_typeclass.gif").createImage();
            }
            return image_nav_typeclass;
        }
        else if (obj instanceof DataConstructor){
            if (image_nav_dataconstructor == null){
                image_nav_dataconstructor = CALEclipseUIPlugin.getImageDescriptor("/icons/nav_dataconstructor.gif").createImage();
            }
            return image_nav_dataconstructor;
        }
        
        String imageKey = ISharedImages.IMG_OBJ_ELEMENT;
        return PlatformUI.getWorkbench().getSharedImages().getImage(imageKey);
    }

}