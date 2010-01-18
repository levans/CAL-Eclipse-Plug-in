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
 * EntityReturnValueEditor.java
 * Created: 1-Mar-07
 * By: Rick Cameron
 */


package org.openquark.cal.eclipse.ui.metadataeditor;

import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.forms.widgets.FormText;
import org.eclipse.ui.forms.widgets.FormToolkit;


/**
 * @author rcameron
 *
 */
final class EntityReturnValueEditor extends EditorComponent {
    
    private static final Font BOLD_FONT = new Font (null, "Tahoma", 10, SWT.BOLD); //$NON-NLS-1$
    private static final Font ITALIC_FONT = new Font (null, "Tahoma", 10, SWT.ITALIC); //$NON-NLS-1$
    
    private final String initialDescription;
    private final String typeString;
    
    private Composite panel;
    private Text shortDescriptionField;
    
    
    /**
     * Constructor EntityReturnValueEditor
     *
     */
    EntityReturnValueEditor (EditorSection editorSection, String key, String returnValueDesc, String typeString) {
        super (editorSection, key);
        
        this.initialDescription = returnValueDesc;
        this.typeString = typeString;
    }

    /**
     * @see org.openquark.cal.eclipse.ui.metadataeditor.EditorComponent#createEditorComponent(org.eclipse.swt.widgets.Composite, org.eclipse.ui.forms.widgets.FormToolkit)
     */
    @Override
    Control createEditorComponent (Composite parent, FormToolkit formToolkit) {
        panel = formToolkit.createComposite (parent, SWT.NONE);
        GridLayoutFactory.swtDefaults ().numColumns (2).applyTo (panel);
        panel.setData(FormToolkit.KEY_DRAW_BORDER, FormToolkit.TREE_BORDER);
        formToolkit.paintBordersFor (panel);
        
        // 0, 0 - 1, 0
        FormText argumentPrototype = formToolkit.createFormText (panel, true);
        argumentPrototype.setWhitespaceNormalized (false);
        argumentPrototype.setFont ("bold", BOLD_FONT); //$NON-NLS-1$
        argumentPrototype.setFont ("italic", ITALIC_FONT); //$NON-NLS-1$
        argumentPrototype.setText ("<form><p><span font=\"bold\">" + MetadataEditorMessages.ReturnValueIndicator + "</span><span font=\"italic\"> :: " + typeString + "</span></p></form>", true, false); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
        GridDataFactory.swtDefaults ().align (SWT.FILL, SWT.CENTER).grab (true, true).span (2, 1).applyTo (argumentPrototype);
        
        // 0, 1
        Label shortDescriptionLabel = formToolkit.createLabel (panel, MetadataEditorMessages.ShortDescription);
        GridDataFactory.swtDefaults ().align (SWT.LEFT, SWT.CENTER).applyTo (shortDescriptionLabel);
        
        shortDescriptionField = formToolkit.createText (panel, safeString (initialDescription));
        GridDataFactory.swtDefaults ().align (SWT.FILL, SWT.CENTER).grab (true, false).applyTo (shortDescriptionField);
        
        shortDescriptionField.addModifyListener (new ModifyListener () {
            public void modifyText (ModifyEvent e) {
                editorChanged ();
            }
        });
        
        return panel;
    }

    private static String safeString (String s) {
        return s == null ? "" : s; //$NON-NLS-1$
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Control getEditorComponent () {
        return panel;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Object getValue () {
        String description = shortDescriptionField.getText().trim();
        
        return description.length() == 0 ? null : description;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setValue (Object value) {
        // TODO Auto-generated method stub

    }

    /**
     * Method refresh
     *
     * @param returnValueDesc
     * @param typeString
     */
    public void refresh (String returnValueDesc, String typeString) {
        shortDescriptionField.setText (safeString (returnValueDesc));
    }

}
