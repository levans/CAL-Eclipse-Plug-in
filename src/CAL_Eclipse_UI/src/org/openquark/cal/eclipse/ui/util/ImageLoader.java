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
 * ImageLoader.java
 * Created: 9-Mar-07
 * By: Rick Cameron
 */


package org.openquark.cal.eclipse.ui.util;

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.swt.graphics.Image;


/**
 * @author Rick Cameron
 * 
 */
public final class ImageLoader {
    
    private final ImageDescriptor imageDescriptor;
    
    private Image image = null;

    /**
     * Constructor ImageLoader
     * 
     * @param imageDescriptor
     */
    public ImageLoader (ImageDescriptor imageDescriptor) {
        this.imageDescriptor = imageDescriptor;
    }
    
    /**
     * Method getImage
     * 
     * @return Returns the {@link Image}
     */
    public Image getImage () {
        if (image == null) {
            image = imageDescriptor.createImage ();
        }
        
        return image;
    }

}
