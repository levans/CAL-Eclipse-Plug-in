/*******************************************************************************
 * Copyright (c) 2007 Business Objects SA and others.
 * All rights reserved. 
 * This file is made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Business Objects SA - initial API and implementation
 *******************************************************************************/
/*
 * Overlayer.java
 * Created: Jun 20, 2007
 * By: Greg McClement
 */

package org.openquark.cal.eclipse.ui.views;

import org.eclipse.jface.resource.CompositeImageDescriptor;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;

/**
 * This overlays one image over another. The overlayed image is put in the top left corner.
 * @author Greg McClement
 */
public class Overlayer extends CompositeImageDescriptor{

    private Image baseImage;
    private Image overlayImage;
    
    Overlayer(Image baseImage, Image overlayImage){
        this.baseImage = baseImage;
        this.overlayImage = overlayImage;
    }
    
    public Image getImage() {
        return createImage();
    }

    protected void drawCompositeImage(int width, int height) {
        drawImage(baseImage.getImageData(), 0, 0);
        drawImage(overlayImage.getImageData(), 0, 0);
    }

    protected Point getSize() {
        return new Point(baseImage.getBounds().width, baseImage.getBounds().height);
    }        
};

