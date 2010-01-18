/*******************************************************************************
 * Copyright (c) 2006 Business Objects Software Limited and others.
 * All rights reserved. 
 * This file is made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Business Objects Software Limited - initial API and implementation based on Eclipse 3.1.2 code for
 *                             /org.eclipse.jdt.ui/ui/org/eclipse/jdt/internal/ui/preferences/JavaSourcePreviewerUpdater.java
 *                           Eclipse source is available at: http://www.eclipse.org/downloads/
 *******************************************************************************/

/*
 * CALSourcePreviewerUpdater.java
 * Creation date: Feb 14, 2006.
 * By: Edward Lam
 */
package org.openquark.cal.eclipse.ui.preferences;

import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.graphics.Font;

import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.core.runtime.Assert;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.PropertyChangeEvent;

import org.eclipse.jface.text.source.SourceViewer;
import org.openquark.cal.eclipse.ui.text.CALSourceViewerConfiguration;


/**
 * Handles CAL editor font changes for Java source preview viewers.
 * 
 * @author Edward Lam
 */
class CALSourcePreviewerUpdater {
    /*
     * Not intended to be instantiated
     * Note: the JavaSourcePreviewerUpdater configures the viewer by a call to the constructor, which contains all the code.
     */
    private CALSourcePreviewerUpdater() {
    }
    
    /**
     * Creates a cal source preview updater for the given viewer, configuration and preference store.
     *
     * @param viewer the viewer
     * @param configuration the configuration
     * @param preferenceStore the preference store
     */
    static void configureViewer(final SourceViewer viewer, final CALSourceViewerConfiguration configuration, final IPreferenceStore preferenceStore) {
        Assert.isNotNull(viewer);
        Assert.isNotNull(configuration);
        Assert.isNotNull(preferenceStore);
        final IPropertyChangeListener fontChangeListener = new IPropertyChangeListener() {
            /*
             * @see org.eclipse.jface.util.IPropertyChangeListener#propertyChange(org.eclipse.jface.util.PropertyChangeEvent)
             */
            public void propertyChange(PropertyChangeEvent event) {
                if (event.getProperty().equals(PreferenceConstants.EDITOR_TEXT_FONT)) {
                    Font font = JFaceResources.getFont(PreferenceConstants.EDITOR_TEXT_FONT);
                    viewer.getTextWidget().setFont(font);
                }
            }
        };
        final IPropertyChangeListener propertyChangeListener = new IPropertyChangeListener() {
            /*
             * @see org.eclipse.jface.util.IPropertyChangeListener#propertyChange(org.eclipse.jface.util.PropertyChangeEvent)
             */
            public void propertyChange(PropertyChangeEvent event) {
                if (configuration.affectsTextPresentation(event)) {
                    configuration.handlePropertyChangeEvent(event);
                    viewer.invalidateTextPresentation();
                }
            }
        };
        viewer.getTextWidget().addDisposeListener(new DisposeListener() {
            /*
             * @see org.eclipse.swt.events.DisposeListener#widgetDisposed(org.eclipse.swt.events.DisposeEvent)
             */
            public void widgetDisposed(DisposeEvent e) {
                preferenceStore.removePropertyChangeListener(propertyChangeListener);
                JFaceResources.getFontRegistry().removeListener(fontChangeListener);
            }
        });
        JFaceResources.getFontRegistry().addListener(fontChangeListener);
        preferenceStore.addPropertyChangeListener(propertyChangeListener);
    }
}
