/*******************************************************************************
 * Copyright (c) 2006 Business Objects Software Limited and others.
 * All rights reserved. 
 * This file is made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Business Objects Software Limited - initial API and implementation based on Eclipse 3.1.2 code for
 *                             /org.eclipse.jdt.ui/ui/org/eclipse/jdt/internal/ui/text/JavaPresentationReconciler.java
 *                           Eclipse source is available at: http://www.eclipse.org/downloads/
 *******************************************************************************/

/*
 * CALPresentationReconciler.java
 * Creation date: Feb 9, 2006.
 * By: Edward Lam
 */
package org.openquark.cal.eclipse.ui.text;

import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.TextPresentation;
import org.eclipse.jface.text.presentation.PresentationReconciler;


/**
 * Presentation reconciler, adding functionality for operation without a viewer.
 *
 * @author Edward Lam
 */
public class CALPresentationReconciler extends PresentationReconciler {
    
    /** Last used document */
    private IDocument fLastDocument;

    // Called by the semantic highlighter:
    /**
     * Constructs a "repair description" for the given damage and returns this description as a text presentation.
     * <p>
     * NOTE: Should not be used if this reconciler is installed on a viewer.
     * </p>
     * 
     * @param damage the damage to be repaired
     * @param document the document whose presentation must be repaired
     * @return the presentation repair description as text presentation
     */
    public TextPresentation createRepairDescription(IRegion damage, IDocument document) {
        if (document != fLastDocument) {
            setDocumentToDamagers(document);
            setDocumentToRepairers(document);
        }
        return createPresentation(damage, document);
    }
}
