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
 * EntityArgumentEditor.java
 * Created: 1-Mar-07
 * By: Rick Cameron
 */


package org.openquark.cal.eclipse.ui.metadataeditor;

import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.forms.widgets.FormText;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.openquark.cal.eclipse.ui.metadataeditor.CALMetadataEditor.Input;
import org.openquark.cal.metadata.ArgumentMetadata;



/**
 * @author rcameron
 *
 */
final class EntityArgumentEditor extends EditorComponent {

    private static final Font BOLD_FONT = new Font (null, "Tahoma", 10, SWT.BOLD); //$NON-NLS-1$
    private static final Font ITALIC_FONT = new Font (null, "Tahoma", 10, SWT.ITALIC); //$NON-NLS-1$
    
    private final int argumentNumber;
    private ArgumentMetadata metadata;
    private String adjustedName;
    private final String typeString;
    
    private Composite panel;
    private Text displayNameField;
    private Text shortDescriptionField;
    private FormText argumentPrototype;

    /**
     * Constructor EntityArgumentEditor
     *
     * @param editorSection
     * @param argNum
     * @param metadata
     * @param typeString
     * @param adjustedName
     */
    public EntityArgumentEditor (EditorSection editorSection, int argNum, ArgumentMetadata metadata, String typeString, String adjustedName) {
        super (editorSection);
        
        this.argumentNumber = argNum;
        this.metadata = metadata;
        this.adjustedName = adjustedName;
        this.typeString = typeString;
    }

    /**
     * @see org.openquark.cal.eclipse.ui.metadataeditor.EditorComponent#createEditorComponent(org.eclipse.swt.widgets.Composite, org.eclipse.ui.forms.widgets.FormToolkit)
     */
    @Override
    Control createEditorComponent (Composite parent, FormToolkit formToolkit) {
        panel = formToolkit.createComposite (parent, SWT.NONE);
        GridLayoutFactory.swtDefaults ().numColumns (3).applyTo (panel);
        panel.setData(FormToolkit.KEY_DRAW_BORDER, FormToolkit.TREE_BORDER);
        formToolkit.paintBordersFor (panel);
        
        argumentPrototype = formToolkit.createFormText (panel, true);
        argumentPrototype.setWhitespaceNormalized (false);
        argumentPrototype.setFont ("bold", BOLD_FONT); //$NON-NLS-1$
        argumentPrototype.setFont ("italic", ITALIC_FONT); //$NON-NLS-1$
        setPrototypeText ();
        GridDataFactory.swtDefaults ().align (SWT.FILL, SWT.CENTER).grab (true, true).span (2, 1).applyTo (argumentPrototype);
        
        // 2, 0
        Button moreButton = formToolkit.createButton (panel, MetadataEditorMessages.MoreButtonCaption, SWT.PUSH);
        moreButton.addSelectionListener (new SelectionAdapter () {
            @Override
            public void widgetSelected (SelectionEvent e) {
                onMore ();
            }
        });
        
        // 0, 1
        Label displayNameLabel = formToolkit.createLabel (panel, MetadataEditorMessages.DisplayName);
        GridDataFactory.swtDefaults ().align (SWT.LEFT, SWT.CENTER).applyTo (displayNameLabel);
        
        displayNameField = formToolkit.createText (panel, safeString (metadata.getDisplayName ()));
        GridDataFactory.swtDefaults ().align (SWT.FILL, SWT.CENTER).grab (true, false).span (2, 1).applyTo (displayNameField);
        
        // 0, 2
        Label shortDescriptionLabel = formToolkit.createLabel (panel, MetadataEditorMessages.ShortDescription);
        GridDataFactory.swtDefaults ().align (SWT.LEFT, SWT.CENTER).applyTo (shortDescriptionLabel);
        
        shortDescriptionField = formToolkit.createText (panel, safeString (metadata.getShortDescription ()));
        GridDataFactory.swtDefaults ().align (SWT.FILL, SWT.CENTER).grab (true, false).span (2, 1).applyTo (shortDescriptionField);
        
        displayNameField.addModifyListener (new ModifyListener () {
            public void modifyText (ModifyEvent e) {
                editorChanged ();
            }
        });
        
        shortDescriptionField.addModifyListener (new ModifyListener () {
            public void modifyText (ModifyEvent e) {
                editorChanged ();
            }
        });
        
        return panel;
    }

    /**
     * Method onMore
     *
     */
    private void onMore () {
        Input argumentInput = getEditorSection ().getInput ().makeArgumentInput (argumentNumber);
        
        try {
            getActivePage ().openEditor (argumentInput, CALMetadataEditor.ID);
        } catch (PartInitException e) {
            MessageDialog.openError (null, MetadataEditorMessages.CAL_Editor, MetadataEditorMessages.Error_Opening_Editor + e.getLocalizedMessage ());
        }
    }
    
    /**
     * Method getActivePage
     *
     * @return Returns the active {@link IWorkbenchPage}
     */
    private static IWorkbenchPage getActivePage () {
        return PlatformUI.getWorkbench ().getActiveWorkbenchWindow ().getActivePage ();
    }


    /**
     * Method setPrototypeText
     *
     */
    private void setPrototypeText () {
        argumentPrototype.setText ("<form><p><span font=\"bold\">" + adjustedName + "</span><span font=\"italic\"> :: " + typeString + "</span></p></form>", true, false); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
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
        ArgumentMetadata currentValue = (ArgumentMetadata)metadata.copy ();
        currentValue.setDisplayName (displayNameField.getText ());
        currentValue.setShortDescription (shortDescriptionField.getText ());
        return currentValue;
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
     * @param metadata
     * @param typeString
     * @param adjustedName
     */
    void refresh (ArgumentMetadata metadata, String typeString, String adjustedName) {
        this.metadata = metadata;
        this.adjustedName = adjustedName;
        
        setPrototypeText ();
        
        displayNameField.setText (safeString (metadata.getDisplayName ()));
        shortDescriptionField.setText (safeString (metadata.getShortDescription ()));
    }

}
