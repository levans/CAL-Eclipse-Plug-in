/*******************************************************************************
 * Copyright (c) 2006 Business Objects Software Limited and others.
 * All rights reserved. 
 * This file is made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Business Objects Software Limited - initial API and implementation
 *******************************************************************************/

/*
 * ActionUtilities.java
 * Creation date: Dec 21, 2006.
 * By: Greg McClement
 */
package org.openquark.cal.eclipse.ui.actions;

import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.ITextSelection;
import org.eclipse.jface.text.TextSelection;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.texteditor.IDocumentProvider;
import org.eclipse.ui.texteditor.ITextEditor;

public class ActionUtilities {

    /**
     * Returns the document currently displayed in the editor, or <code>null</code> if none can be 
     * obtained.
     * 
     * @return the current document or <code>null</code>
     */
    public static IDocument getDocument(ITextEditor editor) {
        if (editor != null) {

            IDocumentProvider provider = editor.getDocumentProvider();
            IEditorInput input = editor.getEditorInput();
            if (provider != null && input != null) {
                return provider.getDocument(input);
            }
            
        }
        return null;
    }

    /**
     * Returns the selection on the editor or an invalid selection if none can be obtained. Returns
     * never <code>null</code>.
     * 
     * @return the current selection, never <code>null</code>
     */
    public static ITextSelection getSelection(ITextEditor editor) {
        ISelectionProvider provider = getSelectionProvider(editor);
        if (provider != null) {

            ISelection selection = provider.getSelection();
            if (selection instanceof ITextSelection) {
                return (ITextSelection)selection;
            }
        }

        // null object
        return TextSelection.emptySelection();
    }

    /**
     * Returns the editor's selection provider.
     * 
     * @return the editor's selection provider or <code>null</code>
     */
    public static ISelectionProvider getSelectionProvider(ITextEditor editor) {
        if (editor != null) {
            return editor.getSelectionProvider();
        }
        return null;
    }    
}
