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
 * OpenDeclarationAction.java
 * Creation date: Dec 21, 2006.
 * By: Greg McClement
 */
package org.openquark.cal.eclipse.ui.actions;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.ResourceBundle;

import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.ITextSelection;
import org.eclipse.jface.text.hyperlink.IHyperlink;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.dialogs.ListDialog;
import org.eclipse.ui.texteditor.ITextEditor;
import org.eclipse.ui.texteditor.TextEditorAction;
import org.openquark.cal.compiler.SearchManager;
import org.openquark.cal.compiler.SearchResult;
import org.openquark.cal.compiler.SourceRange;
import org.openquark.cal.eclipse.core.CALModelManager;
import org.openquark.cal.eclipse.ui.caleditor.CALEditor;
import org.openquark.cal.eclipse.ui.caleditor.CALHyperlinkDetector;
import org.openquark.cal.eclipse.ui.util.CoreUtility;


/**
 * Goes to the definition of the symbol.
 * 
 * @author Greg McClement
 */
public class OpenDeclarationAction extends TextEditorAction {
    
    private final SearchManager searchManager;
    
    private final String errorTitle = ActionMessages.OpenDeclarationAction_error_title;
    
    public OpenDeclarationAction(ResourceBundle bundle, String prefix, ITextEditor editor) {
        super(bundle, prefix, editor);
        CALModelManager modelManager = CALModelManager.getCALModelManager();
        searchManager = modelManager.getSearchManager();
    }

    @Override
    public void update() {
        super.update();

        setEnabled(true);
    }

    /*
     * @see org.eclipse.jface.action.Action#run()
     */
    @Override
    public void run() {
        // update has been called by the framework
        if (!isEnabled()) {
            return;
        }
        
        if (!CoreUtility.builderEnabledCheck(errorTitle)){
            return;
        }
        
        final CALEditor textEditor = (CALEditor) getTextEditor();
        final ITextSelection selection = ActionUtilities.getSelection(textEditor);
        final IDocument document = ActionUtilities.getDocument(textEditor);

        if (document != null) {
            final int offset = selection.getOffset();
            final IHyperlink[] hyperlinks =
                CALHyperlinkDetector.getHyperlinkForRegion(textEditor, document, offset, searchManager, true);
            
            final List<IHyperlink> nonNullHyperlinks = new ArrayList<IHyperlink>();
            if (hyperlinks != null) {
                for (final IHyperlink hyperlink : hyperlinks) {
                    if (hyperlink != null) {
                        nonNullHyperlinks.add(hyperlink);
                    }
                }
            }
            
            if (!nonNullHyperlinks.isEmpty()) {
                nonNullHyperlinks.get(0).open();
            }
        }
    }
    
    /**
     * This class will show a dialog to allow the user to which search result they 
     * indended when there is an ambiguity. This can occur when the user selects
     * open declaration for [(Int, Int)] when the cursor is over the Int.
     *  
     * @author Greg McClement
     */
    public static class SelectAnOption extends ListDialog{
        public SelectAnOption(Shell parent, final IDocument document, final Object[] options) {
            super(parent);

            setTitle(ActionMessages.OpenDeclarationAction_selectOption_title);
            setMessage(ActionMessages.OpenDeclarationAction_selectOption_message);

            setContentProvider(
                    new IStructuredContentProvider() {

                        public Object[] getElements(Object inputElement) {
                            return options;
                        }

                        public void dispose() {                            
                        }

                        public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
                        }
                    }
            );

            setLabelProvider(
                    new LabelProvider(){
                        @Override
                        public String getText(Object element){
                            SearchResult.Precise sr = (SearchResult.Precise) element;
                            final SourceRange sourceRange = sr.getSourceRange();
                            // show the text corresponding to this search result
                            try {
                                final int start = CoreUtility.toOffset(sourceRange.getStartSourcePosition(), document);
                                final int end = CoreUtility.toOffset(sourceRange.getEndSourcePosition(), document);
                                final int length = end - start;
                                return document.get(start, length);
                            } catch (BadLocationException e) {
                                // Wow this should not happen, lets just show the possible match
                                // instead of the code being considered
                                return sr.getName().toString();
                            }
                        }
                    }
            );

            setInput(Arrays.asList(options));
        }
    }
}
