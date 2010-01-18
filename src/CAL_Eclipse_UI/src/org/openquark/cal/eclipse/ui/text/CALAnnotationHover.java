/*******************************************************************************
 * Copyright (c) 2006 Business Objects Software Limited and others.
 * All rights reserved. 
 * This file is made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Business Objects Software Limited - initial API and implementation based on Eclipse 3.1.2 code for
 *                             /org.eclipse.jdt.ui/ui/org/eclipse/jdt/internal/ui/text/JavaAnnotationHover.java
 *                           Eclipse source is available at: http://www.eclipse.org/downloads/
 *******************************************************************************/

/*
 * CALAnnotationHover.java
 * Creation date: Feb 9, 2006.
 * By: Edward Lam
 */
package org.openquark.cal.eclipse.ui.text;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.core.runtime.Assert;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.Position;
import org.eclipse.jface.text.source.Annotation;
import org.eclipse.jface.text.source.IAnnotationHover;
import org.eclipse.jface.text.source.IAnnotationModel;
import org.eclipse.jface.text.source.ISourceViewer;
import org.eclipse.jface.text.source.ISourceViewerExtension2;
import org.eclipse.jface.text.source.projection.AnnotationBag;
import org.eclipse.ui.editors.text.EditorsUI;
import org.eclipse.ui.texteditor.AnnotationPreference;
import org.openquark.cal.eclipse.ui.CALEclipseUIPlugin;
import org.openquark.util.UnsafeCast;


/**
 * Determines all markers for the given line and collects, concatenates, and formats their messages.
 * @author Edward Lam
 */
public class CALAnnotationHover implements IAnnotationHover {

    private static class CALAnnotationHoverType {
    }

    public static final CALAnnotationHoverType OVERVIEW_RULER_HOVER = new CALAnnotationHoverType();
    public static final CALAnnotationHoverType TEXT_RULER_HOVER = new CALAnnotationHoverType();
    public static final CALAnnotationHoverType VERTICAL_RULER_HOVER = new CALAnnotationHoverType();

    private final IPreferenceStore fStore = CALEclipseUIPlugin.getDefault().getCombinedPreferenceStore();
    private final CALAnnotationHoverType fType;

    public CALAnnotationHover(CALAnnotationHoverType type) {
        Assert.isTrue(OVERVIEW_RULER_HOVER.equals(type) || TEXT_RULER_HOVER.equals(type) || VERTICAL_RULER_HOVER.equals(type));
        fType = type;
    }

    private boolean isRulerLine(Position position, IDocument document, int line) {
        if (position.getOffset() > -1 && position.getLength() > -1) {
            try {
                return line == document.getLineOfOffset(position.getOffset());
            } catch (BadLocationException x) {
            }
        }
        return false;
    }

    private IAnnotationModel getAnnotationModel(ISourceViewer viewer) {
        if (viewer instanceof ISourceViewerExtension2) {
            ISourceViewerExtension2 extension = (ISourceViewerExtension2)viewer;
            return extension.getVisualAnnotationModel();
        }
        return viewer.getAnnotationModel();
    }

    private boolean isDuplicateCALAnnotation(Map<Position, Object> messagesAtPosition, Position position, String message) {
        if (messagesAtPosition.containsKey(position)) {
            Object value = messagesAtPosition.get(position);
            if (message.equals(value)) {
                return true;
            }

            if (value instanceof List) {
                List<String> messages = UnsafeCast.unsafeCast(value);
                if (messages.contains(message)) {
                    return true;
                } else {
                    messages.add(message);
                }
            } else {
                ArrayList<Object> messages = new ArrayList<Object>();
                messages.add(value);
                messages.add(message);
                messagesAtPosition.put(position, messages);
            }
        } else {
            messagesAtPosition.put(position, message);
        }
        return false;
    }

    private boolean includeAnnotation(Annotation annotation, Position position, HashMap<Position, Object> messagesAtPosition) {
        AnnotationPreference preference = getAnnotationPreference(annotation);
        if (preference == null) {
            return false;
        }

        if (OVERVIEW_RULER_HOVER.equals(fType)) {
            String key = preference.getOverviewRulerPreferenceKey();
            if (key == null || !fStore.getBoolean(key)) {
                return false;
            }
        } else if (TEXT_RULER_HOVER.equals(fType)) {
            String key = preference.getTextPreferenceKey();
            if (key != null) {
                if (!fStore.getBoolean(key)) {
                    return false;
                }
            } else {
                key = preference.getHighlightPreferenceKey();
                if (key == null || !fStore.getBoolean(key)) {
                    return false;
                }
            }
        } else if (VERTICAL_RULER_HOVER.equals(fType)) {
            String key = preference.getVerticalRulerPreferenceKey();
            // backward compatibility
            if (key != null && !fStore.getBoolean(key)) {
                return false;
            }
        }

        String text = annotation.getText();
        return (text != null && !isDuplicateCALAnnotation(messagesAtPosition, position, text));
    }

    private List<Annotation> getCALAnnotationsForLine(ISourceViewer viewer, int line) {
        IAnnotationModel model = getAnnotationModel(viewer);
        if (model == null) {
            return null;
        }

        IDocument document = viewer.getDocument();
        List<Annotation> calAnnotations = new ArrayList<Annotation>();
        HashMap<Position, Object> messagesAtPosition = new HashMap<Position, Object>();

        for (Iterator<Annotation> iterator = UnsafeCast.unsafeCast(model.getAnnotationIterator()); iterator.hasNext(); ) {
            Annotation annotation = iterator.next();

            Position position = model.getPosition(annotation);
            if (position == null) {
                continue;
            }

            if (!isRulerLine(position, document, line)) {
                continue;
            }

            if (annotation instanceof AnnotationBag) {
                AnnotationBag bag = (AnnotationBag)annotation;
                for (Iterator<Annotation> e = UnsafeCast.unsafeCast(bag.iterator()); e.hasNext(); ) {
                    annotation = e.next();
                    position = model.getPosition(annotation);
                    if (position != null && includeAnnotation(annotation, position, messagesAtPosition)) {
                        calAnnotations.add(annotation);
                    }
                }
                continue;
            }

            if (includeAnnotation(annotation, position, messagesAtPosition)) {
                calAnnotations.add(annotation);
            }
        }

        return calAnnotations;
    }

    /*
     * @see IVerticalRulerHover#getHoverInfo(ISourceViewer, int)
     */
    public String getHoverInfo(ISourceViewer sourceViewer, int lineNumber) {
        List<Annotation> calAnnotations = getCALAnnotationsForLine(sourceViewer, lineNumber);
        if (calAnnotations != null) {

            if (calAnnotations.size() == 1) {

                // optimization
                Annotation annotation = calAnnotations.get(0);
                String message = annotation.getText();
                if (message != null && message.trim().length() > 0) {
                    return formatSingleMessage(message);
                }

            } else {

                List<String> messages = new ArrayList<String>();

                for (final Annotation annotation : calAnnotations) {
                    String message = annotation.getText();
                    if (message != null && message.trim().length() > 0) {
                        messages.add(message.trim());
                    }
                }

                if (messages.size() == 1) {
                    return formatSingleMessage(messages.get(0));
                }

                if (messages.size() > 1) {
                    return formatMultipleMessages(messages);
                }
            }
        }
        return null;
    }

    /*
     * Formats a message as HTML text.
     */
    private String formatSingleMessage(String message) {
        StringBuilder buffer = new StringBuilder();
        HTMLPrinter.addPageProlog(buffer);
        HTMLPrinter.addParagraph(buffer, HTMLPrinter.convertToHTMLContent(message));
        HTMLPrinter.addPageEpilog(buffer);
        return buffer.toString();
    }

    /*
     * Formats several message as HTML text.
     */
    private String formatMultipleMessages(List<String> messages) {
        StringBuilder buffer = new StringBuilder();
        HTMLPrinter.addPageProlog(buffer);
//      HTMLPrinter.addParagraph(buffer, HTMLPrinter.convertToHTMLContent(CALUIMessages.JavaAnnotationHover_multipleMarkersAtThisLine));
        HTMLPrinter.addParagraph(buffer, HTMLPrinter.convertToHTMLContent("Multiple markers at this line"));

        HTMLPrinter.startBulletList(buffer);
        for (final String string : messages) {
            HTMLPrinter.addBullet(buffer, HTMLPrinter.convertToHTMLContent(string));
        }
        HTMLPrinter.endBulletList(buffer);

        HTMLPrinter.addPageEpilog(buffer);
        return buffer.toString();
    }

    /**
     * Returns the annotation preference for the given annotation.
     *
     * @param annotation the annotation
     * @return the annotation preference or <code>null</code> if none
     */
    private AnnotationPreference getAnnotationPreference(Annotation annotation) {
        return EditorsUI.getAnnotationPreferenceLookup().getAnnotationPreference(annotation);
    }
}
