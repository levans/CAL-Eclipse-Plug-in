/*******************************************************************************
 * Copyright (c) 2007 Business Objects SA and others.
 * All rights reserved. 
 * This file is made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Business Objects Software Limited
 *******************************************************************************/

/*
 * ForeignDecorator.java
 * Created: October 12, 2007
 * By: Greg McClement
 */

package org.openquark.cal.eclipse.ui.views;

import java.util.HashMap;
import java.util.Map;
import org.eclipse.jface.viewers.ILabelDecorator;
import org.eclipse.jface.viewers.ILabelProviderListener;
import org.eclipse.swt.graphics.Image;
import org.openquark.cal.compiler.Function;
import org.openquark.cal.compiler.TypeConstructor;
import org.openquark.cal.eclipse.ui.CALEclipseUIPlugin;

/**
 * Decorates foreign entities with an 'F' marker.
 * 
 * @author Greg McClement
 */
public class ForeignDecorator implements ILabelDecorator {

    private final Image image_foreignFunction = CALEclipseUIPlugin.getImageDescriptor("/icons/full/foreign_function.gif").createImage();
    
    /**
     * There are only a handful of images so let's do a cache.
     */
    private Map<Image, Image> imageCache_foreignFunction = new HashMap<Image, Image>();
    
    public ForeignDecorator(){
        super();
    }
    
    public Image decorateImage(Image baseImage, Object element) {
        if (element instanceof Function){
            Function function = (Function) element;
            if (function.getForeignFunctionInfo() == null){
                return baseImage;
            }
        }
        else if (element instanceof TypeConstructor){
            TypeConstructor typeConstructor = (TypeConstructor) element;
            if (typeConstructor.getForeignTypeInfo() == null){
                return baseImage;
            }
            
        }
        else {
            return baseImage;
        }

        Image image_overlay = image_foreignFunction;
        Map<Image, Image> cache = imageCache_foreignFunction;
        
        // If it's in the cache then use that
        {
            Image cachedImage = cache.get(baseImage);
            if (cachedImage != null){
                return cachedImage;
            }
        }
        
        final Overlayer overlayer = new Overlayer(baseImage, image_overlay);
        final Image image = overlayer.getImage();
        cache.put(baseImage, image);
        return image;
    }
    
    public String decorateText(String text, Object element) {
        return null;
    }

    public void addListener(ILabelProviderListener listener) {
    }

    public void dispose() {
    }

    public boolean isLabelProperty(Object element, String property) {
        return false;
    }

    public void removeListener(ILabelProviderListener listener) {
    }
}
