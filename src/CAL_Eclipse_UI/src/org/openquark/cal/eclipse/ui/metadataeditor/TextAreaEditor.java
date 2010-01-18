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
 * TextAreaEditor.java
 * Created: 22-Feb-07
 * By: Rick Cameron
 */


package org.openquark.cal.eclipse.ui.metadataeditor;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.ui.forms.widgets.TableWrapData;


/**
 * @author Rick Cameron
 * 
 */
final class TextAreaEditor extends EditorComponent {

    private static final int MIN_HEIGHT = 150;
    
    private Text textArea = null;


    /**
     * Constructor TextAreaEditor
     * 
     * @param editorSection
     * @param key
     * @param title
     * @param description
     */
    public TextAreaEditor (EditorSection editorSection, String key, String title, String description) {
        super (editorSection, key, title, description);
    }

    /**
     * @see org.openquark.cal.eclipse.ui.metadataeditor.EditorComponent#createEditorComponent(org.eclipse.swt.widgets.Composite, org.eclipse.ui.forms.widgets.FormToolkit)
     */
    @Override
    Control createEditorComponent (Composite parent, FormToolkit formToolkit) {
        textArea = formToolkit.createText (parent, "", SWT.MULTI | SWT.WRAP | SWT.V_SCROLL); //$NON-NLS-1$
        TableWrapData tableWrapData = new TableWrapData ();
        tableWrapData.heightHint = MIN_HEIGHT;
        textArea.setLayoutData (tableWrapData);

        textArea.addModifyListener (new ModifyListener () {
            public void modifyText (ModifyEvent arg0) {
                editorChanged ();
            }
        });
        
        return textArea;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Control getEditorComponent () {
        return textArea;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Object getValue () {
        String text = textArea.getText();
        return text != null && text.trim().length() > 0 ? text : null;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setValue (Object value) {
        if (value == null) {
            textArea.setText (""); //$NON-NLS-1$
        } else {
            textArea.setText((String) value);
        }
    }

}
