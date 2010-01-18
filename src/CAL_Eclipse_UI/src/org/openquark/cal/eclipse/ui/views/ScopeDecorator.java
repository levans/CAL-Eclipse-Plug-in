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
package org.openquark.cal.eclipse.ui.views;

import java.util.HashMap;
import java.util.Map;
import org.eclipse.jface.viewers.ILabelDecorator;
import org.eclipse.jface.viewers.ILabelProviderListener;
import org.eclipse.swt.graphics.Image;
import org.openquark.cal.compiler.ScopedEntity;
import org.openquark.cal.eclipse.ui.CALEclipseUIPlugin;

/**
 * Decorates ScopedEntities with markers indicating their scope.
 * @author Greg McClement
 */
public class ScopeDecorator implements ILabelDecorator {

    private final Image image_private = CALEclipseUIPlugin.getImageDescriptor("/icons/full/scope_private.gif").createImage();
    private final Image image_protected = CALEclipseUIPlugin.getImageDescriptor("/icons/full/scope_protected.gif").createImage();
    private final Image image_public = CALEclipseUIPlugin.getImageDescriptor("/icons/full/scope_public.gif").createImage();
    
    /**
     * There are only a handfull of images so let's do a cache.
     */
    private Map<Image, Image> imageCache_private = new HashMap<Image, Image>();
    private Map<Image, Image> imageCache_protected = new HashMap<Image, Image>();
    private Map<Image, Image> imageCache_public = new HashMap<Image, Image>();
    
    public ScopeDecorator(){
        super();
    }
    
    public Image decorateImage(Image baseImage, Object element) {
        if (!(element instanceof ScopedEntity)){
            return baseImage;
        }

        final ScopedEntity scopedEntity = (ScopedEntity) element;
        Image image_overlay = null;
        Map<Image, Image> cache = null;
        if (scopedEntity.getScope().isPrivate()){
            cache = imageCache_private;
            image_overlay = image_private;
        }
        else if (scopedEntity.getScope().isProtected()){
            cache = imageCache_protected;
            image_overlay = image_protected;
        }
        else{ // if (scopedEntity.getScope().isPublic())
            cache = imageCache_public;
            image_overlay = image_public;
        }
        
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
