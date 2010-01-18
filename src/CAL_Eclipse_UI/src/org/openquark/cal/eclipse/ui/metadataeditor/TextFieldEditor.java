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
 * TextFieldEditor.java
 * Created: 20-Feb-07
 * By: Rick Cameron
 */


package org.openquark.cal.eclipse.ui.metadataeditor;

import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.forms.widgets.FormToolkit;


/**
 * @author rcameron
 *
 */
final class TextFieldEditor extends EditorComponent {

    private Text textField = null;

    /**
     * Constructor TextFieldEditor
     *
     * @param editorSection
     * @param key
     * @param title
     * @param description
     */
    public TextFieldEditor (EditorSection editorSection, String key, String title, String description) {
        super (editorSection, key, title, description);
    }

    /**
     * @see org.openquark.cal.eclipse.ui.metadataeditor.EditorComponent#createEditorComponent(org.eclipse.swt.widgets.Composite, org.eclipse.ui.forms.widgets.FormToolkit)
     */
    @Override
    Control createEditorComponent (Composite parent, FormToolkit formToolkit) {
        textField = formToolkit.createText (parent, ""); //$NON-NLS-1$
        
        textField.addModifyListener (new ModifyListener () {
            public void modifyText (ModifyEvent arg0) {
                editorChanged ();
            }
        });
        
        return textField;
    }

    /* (non-Javadoc)
     * @see org.openquark.cal.eclipse.ui.metadataeditor.EditorComponent#getEditorComponent()
     */
    @Override
    public Control getEditorComponent () {
        return textField;
    }

    /* (non-Javadoc)
     * @see org.openquark.cal.eclipse.ui.metadataeditor.EditorComponent#getValue()
     */
    @Override
    public Object getValue () {
        String text = textField.getText();
        return text != null && text.trim().length() > 0 ? text : null;
    }

    /* (non-Javadoc)
     * @see org.openquark.cal.eclipse.ui.metadataeditor.EditorComponent#setValue(java.lang.Object)
     */
    @Override
    public void setValue (Object value) {
        if (value == null) {
            textField.setText (""); //$NON-NLS-1$
        } else {
            textField.setText((String) value);
        }
    }

}
