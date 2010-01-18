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
 * BooleanEditor.java
 * Created: 22-Feb-07
 * By: Rick Cameron
 */


package org.openquark.cal.eclipse.ui.metadataeditor;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.RowLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.ui.forms.widgets.FormToolkit;


/**
 * @author Rick Cameron
 * 
 */
final class BooleanEditor extends EditorComponent {
    
    private Composite editorPanel;
    
    private Button yesButton;
    private Button noButton;

    private final SelectionListener selectionListener = new SelectionListener () {

        public void widgetSelected (SelectionEvent arg0) {
            editorChanged ();
        }

        public void widgetDefaultSelected (SelectionEvent arg0) {
            editorChanged ();
        }

    };

    /**
     * Constructor BooleanEditor
     * 
     * @param editorSection
     * @param key
     * @param title
     * @param description
     */
    public BooleanEditor (EditorSection editorSection, String key, String title, String description) {
        super (editorSection, key, title, description);
    }

    /**
     * @see org.openquark.cal.eclipse.ui.metadataeditor.EditorComponent#createEditorComponent(org.eclipse.swt.widgets.Composite, org.eclipse.ui.forms.widgets.FormToolkit)
     */
    @Override
    Control createEditorComponent (Composite parent, FormToolkit formToolkit) {
        editorPanel = formToolkit.createComposite (parent);
        editorPanel.setLayout (new RowLayout (SWT.HORIZONTAL));
        
        yesButton = formToolkit.createButton (editorPanel, MetadataEditorMessages.YesButtonLabel, SWT.RADIO);
        yesButton.addSelectionListener (selectionListener);
        
        noButton = formToolkit.createButton (editorPanel, MetadataEditorMessages.NoButtonLabel, SWT.RADIO);
        noButton.addSelectionListener (selectionListener);
        
        return editorPanel;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Control getEditorComponent () {
        return editorPanel;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Object getValue () {
        return Boolean.valueOf (yesButton.getSelection ());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setValue (Object value) {
        if (((Boolean)value).booleanValue ()) {
            yesButton.setSelection (true);
        } else {
            noButton.setSelection (true);
        }
    }

}
