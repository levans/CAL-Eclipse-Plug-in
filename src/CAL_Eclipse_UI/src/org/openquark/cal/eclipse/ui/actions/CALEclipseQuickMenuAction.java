/*******************************************************************************
 * Copyright (c) 2006 Business Objects Software Limited and others.
 * All rights reserved. 
 * This file is made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Business Objects Software Limited - initial API and implementation based on Eclipse 3.1.2 code for
 *                             /org.eclipse.jdt.ui/ui/org/eclipse/jdt/internal/ui/actions/JDTQuickMenuAction.java
 *                           Eclipse source is available at: http://www.eclipse.org/downloads/
 *******************************************************************************/

/*
 * CALEclipseQuickMenuAction.java
 * Creation date: Feb 23, 2006.
 * By: Edward Lam
 */
package org.openquark.cal.eclipse.ui.actions;

import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.graphics.Point;

import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.ITextSelection;
import org.eclipse.jface.text.ITextViewerExtension5;
import org.eclipse.jface.text.Region;
import org.eclipse.jface.text.source.ISourceViewer;
import org.openquark.cal.eclipse.ui.caleditor.CALEditor;
import org.openquark.cal.eclipse.ui.text.CALWordFinder;



/**
 * @author Edward Lam
 */
public abstract class CALEclipseQuickMenuAction extends QuickMenuAction { 
    
    private CALEditor fEditor;

    public CALEclipseQuickMenuAction(String commandId) {
        super(commandId);
    }

    public CALEclipseQuickMenuAction(CALEditor editor, String commandId) {
        super(commandId);
        fEditor = editor;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected Point computeMenuLocation(StyledText text) {
        if (fEditor == null || text != fEditor.getViewer().getTextWidget()) {
            return null;
        }
        return computeWordStart();
    }

    private Point computeWordStart() {
        ITextSelection selection = (ITextSelection)fEditor.getSelectionProvider().getSelection();
        IRegion textRegion = CALWordFinder.findWord(fEditor.getViewer().getDocument(), selection.getOffset());
        if (textRegion == null) {
            return null;
        }

        IRegion widgetRegion = modelRange2WidgetRange(textRegion);
        if (widgetRegion == null) {
            return null;
        }

        int start = widgetRegion.getOffset();

        StyledText styledText = fEditor.getViewer().getTextWidget();
        Point result = styledText.getLocationAtOffset(start);
        result.y += styledText.getLineHeight();

        if (!styledText.getClientArea().contains(result)) {
            return null;
        }
        return result;
    }

    private IRegion modelRange2WidgetRange(IRegion region) {
        ISourceViewer viewer = fEditor.getViewer();
        if (viewer instanceof ITextViewerExtension5) {
            ITextViewerExtension5 extension = (ITextViewerExtension5)viewer;
            return extension.modelRange2WidgetRange(region);
        }

        IRegion visibleRegion = viewer.getVisibleRegion();
        int start = region.getOffset() - visibleRegion.getOffset();
        int end = start + region.getLength();
        if (end > visibleRegion.getLength()) {
            end = visibleRegion.getLength();
        }

        return new Region(start, end - start);
    }
}
