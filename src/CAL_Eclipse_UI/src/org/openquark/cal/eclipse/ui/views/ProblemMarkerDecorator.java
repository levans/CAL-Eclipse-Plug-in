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

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IStorage;
import org.eclipse.jface.viewers.ILabelDecorator;
import org.eclipse.jface.viewers.ILabelProviderListener;
import org.eclipse.swt.graphics.Image;
import org.openquark.cal.compiler.ModuleName;
import org.openquark.cal.eclipse.core.CALModelManager;
import org.openquark.cal.eclipse.core.builder.CALBuilder;
import org.openquark.cal.eclipse.ui.CALEclipseUIPlugin;
import org.openquark.cal.eclipse.ui.views.CALWorkspace.ModuleElementsByType;

/**
 * Decorates ScopedEntities with markers indicating their scope.
 * @author Greg McClement
 */
public class ProblemMarkerDecorator implements ILabelDecorator {

    private final Image image_errors = CALEclipseUIPlugin.getImageDescriptor("/icons/full/ErrorDecal.gif").createImage();
    private final Image image_warnings = CALEclipseUIPlugin.getImageDescriptor("/icons/full/warningDecal.gif").createImage();
    
    public ProblemMarkerDecorator(){
        super();
    }

    public Image getProblemMarker(HierarchicalNode hn){
        final Object[] children = hn.getChildren();
        for (int i = 0; i < children.length; i++) {
            final Object child = children[i];
            if (child instanceof ModuleName){
                Image image = getProblemMarker((ModuleName) child);
                if (image != null){
                    return image;
                }
            }
            else if (child instanceof HierarchicalNode){
                Image image = getProblemMarker((HierarchicalNode) child);
                if (image != null){
                    return image;
                }
            }
            else if (child instanceof ModuleElementsByType){
                Image image = getProblemMarker((ModuleElementsByType) child);
                if (image != null){
                    return image;
                }
            }
        }
        return null;
    }

    public Image getProblemMarker(ModuleElementsByType mebt){
        final Object[] children = mebt.getChildren();
        for (int i = 0; i < children.length; i++) {
            final Object child = children[i];
            if (child instanceof ModuleName){
                Image image = getProblemMarker((ModuleName) child);
                if (image != null){
                    return image;
                }
            }
            else if (child instanceof ModuleElementsByType){
                Image image = getProblemMarker((ModuleElementsByType) child);
                if (image != null){
                    return image;
                }
            }
        }
        return null;
    }
    
    private Image getProblemMarker(ModuleName moduleName) {
        CALModelManager cmm = CALModelManager.getCALModelManager();
        IStorage definitionFile = cmm.getInputSourceFile(moduleName);
        
        if (! (definitionFile instanceof IFile)) {
            return null;
        }
        IMarker[] markers = CALBuilder.getProblemsFor((IFile) definitionFile);
        boolean hasWarnings = false;
        for (int i = 0; i < markers.length; i++) {
            final IMarker marker = markers[i];
            if (marker.getAttribute(IMarker.SEVERITY, 0) == IMarker.SEVERITY_ERROR){
                return image_errors;
            }
            else if (marker.getAttribute(IMarker.SEVERITY, 0) == IMarker.SEVERITY_WARNING){
                hasWarnings = true;
            }
        }
        if (hasWarnings){
            return image_warnings;
        }
        else{
            return null;
        }
    }

    
    public Image decorateImage(Image baseImage, Object element) {
        Image problemMarker = null;
        if (element instanceof ModuleName){
            problemMarker = getProblemMarker((ModuleName) element);
        }
        else if (element instanceof HierarchicalNode){
            problemMarker = getProblemMarker((HierarchicalNode) element);
        }
        else if (element instanceof ModuleElementsByType){
            problemMarker = getProblemMarker((ModuleElementsByType) element);
        }

        if (problemMarker != null){
            final Overlayer overlayer = new Overlayer(baseImage, problemMarker);
            final Image image = overlayer.getImage();
            return image;
        }
        
        return baseImage;
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
