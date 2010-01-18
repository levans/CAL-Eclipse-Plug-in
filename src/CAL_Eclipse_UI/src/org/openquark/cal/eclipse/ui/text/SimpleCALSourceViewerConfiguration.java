/*******************************************************************************
 * Copyright (c) 2006 Business Objects Software Limited and others.
 * All rights reserved. 
 * This file is made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Business Objects Software Limited - initial API and implementation based on Eclipse 3.1.2 code for
 *                             /org.eclipse.jdt.ui/ui/org/eclipse/jdt/internal/ui/text/SimpleJavaSourceViewerConfiguration.java
 *                           Eclipse source is available at: http://www.eclipse.org/downloads/
 *******************************************************************************/

/*
 * SimpleCALSourceViewerConfiguration.java
 * Creation date: Feb 9, 2006.
 * By: Edward Lam
 */
package org.openquark.cal.eclipse.ui.text;

import org.eclipse.jface.preference.IPreferenceStore;

import org.eclipse.jface.text.IAutoEditStrategy;
import org.eclipse.jface.text.IInformationControlCreator;
import org.eclipse.jface.text.ITextHover;
import org.eclipse.jface.text.formatter.IContentFormatter;
import org.eclipse.jface.text.hyperlink.IHyperlinkDetector;
import org.eclipse.jface.text.information.IInformationPresenter;
import org.eclipse.jface.text.source.IAnnotationHover;
import org.eclipse.jface.text.source.ISourceViewer;

import org.eclipse.ui.texteditor.ITextEditor;

/**
 * A simple {@linkplain org.openquark.cal.eclipse.ui.text.CALSourceViewerConfiguration Java source viewer configuration}.
 * <p>
 * This simple source viewer configuration basically provides syntax coloring
 * and disables all other features like code assist, quick outlines, hyperlinking, etc.
 * </p>
 * 
 * @author Edward Lam
 */
public class SimpleCALSourceViewerConfiguration extends CALSourceViewerConfiguration {
    
    
    private final boolean fConfigureFormatter;
    
    /**
     * Creates a new Java source viewer configuration for viewers in the given editor
     * using the given preference store, the color manager and the specified document partitioning.
     *
     * @param colorManager the color manager
     * @param preferenceStore the preference store, can be read-only
     * @param editor the editor in which the configured viewer(s) will reside, or <code>null</code> if none
     * @param partitioning the document partitioning for this configuration, or <code>null</code> for the default partitioning
     * @param configureFormatter <code>true</code> if a content formatter should be configured
     */
    public SimpleCALSourceViewerConfiguration(ColorManager colorManager, IPreferenceStore preferenceStore, ITextEditor editor, String partitioning, boolean configureFormatter) {
        super(colorManager, preferenceStore, editor, partitioning);
        fConfigureFormatter= configureFormatter;
    }
    
    /*
     * @see org.eclipse.jface.text.source.SourceViewerConfiguration#getAutoEditStrategies(org.eclipse.jface.text.source.ISourceViewer, java.lang.String)
     */
    @Override
    public IAutoEditStrategy[] getAutoEditStrategies(ISourceViewer sourceViewer, String contentType) {
        return null;
    }
    
    /*
     * @see SourceViewerConfiguration#getAnnotationHover(ISourceViewer)
     */
    @Override
    public IAnnotationHover getAnnotationHover(ISourceViewer sourceViewer) {
        return null;
    }
    
    /*
     * @see SourceViewerConfiguration#getOverviewRulerAnnotationHover(ISourceViewer)
     */
    @Override
    public IAnnotationHover getOverviewRulerAnnotationHover(ISourceViewer sourceViewer) {
        return null;
    }
    
    /*
     * @see SourceViewerConfiguration#getConfiguredTextHoverStateMasks(ISourceViewer, String)
     */
    @Override
    public int[] getConfiguredTextHoverStateMasks(ISourceViewer sourceViewer, String contentType) {
        return null;
    }
    
    /*
     * @see SourceViewerConfiguration#getTextHover(ISourceViewer, String, int)
     */
    @Override
    public ITextHover getTextHover(ISourceViewer sourceViewer, String contentType, int stateMask) {
        return null;
    }
    
    /*
     * @see SourceViewerConfiguration#getTextHover(ISourceViewer, String)
     */
    @Override
    public ITextHover getTextHover(ISourceViewer sourceViewer, String contentType) {
        return null;
    }
    
    /*
     * @see SourceViewerConfiguration#getContentFormatter(ISourceViewer)
     */
    @Override
    public IContentFormatter getContentFormatter(ISourceViewer sourceViewer) {
        if (fConfigureFormatter) {
            return super.getContentFormatter(sourceViewer);
        } else {
            return null;
        }
    }
    
    /*
     * @see SourceViewerConfiguration#getInformationControlCreator(ISourceViewer)
     */
    @Override
    public IInformationControlCreator getInformationControlCreator(ISourceViewer sourceViewer) {
        return null;
    }
    
    /*
     * @see SourceViewerConfiguration#getInformationPresenter(ISourceViewer)
     */
    @Override
    public IInformationPresenter getInformationPresenter(ISourceViewer sourceViewer) {
        return null;
    }
    
    /*
     * @see org.eclipse.jdt.ui.text.JavaSourceViewerConfiguration#getOutlinePresenter(org.eclipse.jface.text.source.ISourceViewer, boolean)
     */
    @Override
    public IInformationPresenter getOutlinePresenter(ISourceViewer sourceViewer, boolean doCodeResolve) {
        return null;
    }
    
    /*
     * @see org.eclipse.jdt.ui.text.JavaSourceViewerConfiguration#getHierarchyPresenter(org.eclipse.jface.text.source.ISourceViewer, boolean)
     */
    @Override
    public IInformationPresenter getHierarchyPresenter(ISourceViewer sourceViewer, boolean doCodeResolve) {
        return null;
    }
    
    /*
     * @see org.eclipse.jface.text.source.SourceViewerConfiguration#getHyperlinkDetectors(org.eclipse.jface.text.source.ISourceViewer)
     */
    @Override
    public IHyperlinkDetector[] getHyperlinkDetectors(ISourceViewer sourceViewer) {
        return null;
    }
}
