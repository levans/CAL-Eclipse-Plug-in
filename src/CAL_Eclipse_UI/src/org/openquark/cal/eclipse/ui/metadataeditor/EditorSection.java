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
 * EditorSection.java
 * Created: 20-Feb-07
 * By: Rick Cameron
 */


package org.openquark.cal.eclipse.ui.metadataeditor;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.ui.forms.widgets.ExpandableComposite;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.ui.forms.widgets.Section;
import org.eclipse.ui.forms.widgets.TableWrapData;
import org.eclipse.ui.forms.widgets.TableWrapLayout;
import org.openquark.cal.eclipse.ui.metadataeditor.CALMetadataEditor.Input;
import org.openquark.cal.metadata.CALFeatureMetadata;



/**
 * @author rcameron
 *
 */
public abstract class EditorSection {

    /** The editor panel that is using this editor section. */
    private final CALMetadataEditorPanel editorPanel;

    /** The {@link Section} that holds the editors for this EditorSection */
    private final Section section;

    /** The panel that holds the various editing components. */
    private final Composite contentPanel;
    
    /**
     * The map that stores the label used to display the title for an editor. 
     */
    private final Map<EditorComponent, Label> editorToTitleMap = new HashMap<EditorComponent, Label>();
   
    /**
     * The map that stores the label used to display error message for an editor.
     */
    private final Map<EditorComponent, Label> editorToErrorMap = new HashMap<EditorComponent, Label>();
    
    /**
     * The map that stores the editors with unique keys used in this section.
     */
    private final Map<String, EditorComponent> keyToEditorMap = new HashMap<String, EditorComponent>();
    
    /**
     * The list of editors in this section in the same order they were added.
     */
    private final List<EditorComponent> allEditors = new ArrayList<EditorComponent>();


    /**
     * Constructor EditorSection
     *
     * @param editorPanel
     * @param title 
     */
    public EditorSection (CALMetadataEditorPanel editorPanel, String title) {
        this.editorPanel = editorPanel;
        
        section = editorPanel.createSection (ExpandableComposite.TITLE_BAR | ExpandableComposite.TWISTIE);
        section.setLayoutData (new TableWrapData (TableWrapData.FILL_GRAB));
        section.setText (title);
        
        FormToolkit formToolkit = editorPanel.getFormToolkit ();
        contentPanel = formToolkit.createComposite (section);
        formToolkit.paintBordersFor (contentPanel);
        TableWrapLayout layout = new TableWrapLayout ();
        layout.numColumns = 2;
        contentPanel.setLayout (layout);
        
        section.setClient (contentPanel);
    }

    
    /**
     * Method getContentPanel
     * 
     * @return Returns the content panel
     */
    protected Composite getContentPanel () {
        return contentPanel;
    }
    
    /**
     * Method addEditor
     *
     * @param editor
     */
    void addEditor (EditorComponent editor) {
        FormToolkit formToolkit = editorPanel.getFormToolkit ();
        
        String title = editor.getTitle ();
        
        // Only add a title label if the editor has a title
        if (title != null) {
            Label titleLabel = formToolkit.createLabel (contentPanel, title);
            titleLabel.setToolTipText(editor.getDescription());
            titleLabel.setLayoutData (new TableWrapData (TableWrapData.LEFT, TableWrapData.MIDDLE));
            
            editorToTitleMap.put(editor, titleLabel);
        }

        Control control = editor.createEditorComponent (contentPanel, formToolkit);

        assert !(control.getLayoutData () instanceof GridData);

        TableWrapData tableWrapData = new TableWrapData ();
        if (control.getLayoutData () instanceof TableWrapData) {
            tableWrapData = (TableWrapData)control.getLayoutData ();
        }

        tableWrapData.grabHorizontal = true;
        tableWrapData.align = TableWrapData.FILL;
        tableWrapData.colspan = title == null ? 2 : 1;

        control.setLayoutData (tableWrapData);

        allEditors.add(editor);
        if (editor.getKey() != null) {
            keyToEditorMap.put(editor.getKey(), editor);
        }
    }
    
    /**
     * Method removeEditor
     * 
     * @param editor
     */
    void removeEditor (EditorComponent editor) {
        if (editor.getEditorComponent ().getParent () != contentPanel) {
            throw new IllegalArgumentException ("Not a valid editor"); //$NON-NLS-1$
        }
        
        // remove editor from the editor set
        allEditors.remove(editor);
        if (editor.getKey() != null) {
            keyToEditorMap.remove(editor.getKey());
        }

        // remove editor and error + title components
        editor.getEditorComponent().dispose ();
        
        Object maybeLabel = editorToErrorMap.remove(editor);
        
        if (maybeLabel instanceof Label) {
            Label label = (Label)maybeLabel;
            
            label.dispose ();
        }
        
        Object maybeTitle = editorToTitleMap.remove(editor);
        
        if (maybeTitle instanceof Control) {
            Control title = (Control)maybeTitle;
            
            title.dispose ();
        }

        reflowSection ();
    }
    
    /**
     * Method reflowSection
     *
     */
    void reflowSection () {
        // This will force a reflow of the section
        section.setExpanded (section.isExpanded ());
    }

    /**
     * @return the set of editors used in this section
     */
    List<EditorComponent> getEditors() {
        return new ArrayList<EditorComponent>(allEditors);
    }
    
    /**
     * Method setExpanded
     *
     * @param b
     */
    void setExpanded (boolean b) {
        section.setExpanded (b);
    }

    /**
     * Method getMetadata
     *
     * @return Returns the {@link CALFeatureMetadata} being edited
     */
    CALFeatureMetadata getMetadata() {
        return getEditorPanel().getMetadata();
    }
    
    /**
     * Method getInput
     *
     * @return Returns the current {@link org.openquark.cal.eclipse.ui.metadataeditor.CALMetadataEditor.Input}
     */
    Input getInput () {
        return getEditorPanel ().getInput ();
    }
    
    /**
     * Method getEditorPanel
     *
     * @return Returns the {@link CALMetadataEditorPanel}
     */
    protected CALMetadataEditorPanel getEditorPanel () {
        return editorPanel;
    }

    /**
     * @param key the key of the editor
     * @return the editor with the given key
     */
    EditorComponent getEditor(String key) {
        
        if (!keyToEditorMap.containsKey(key)) {
            throw new IllegalArgumentException("no editor with the given key: " + key); //$NON-NLS-1$
        }
        
        return keyToEditorMap.get(key);
    }
    
    /**
     * @param key the key of the editor whose value to fetch
     * @return the value stored by the editor component. This may be null if the
     * editor is storing a null value.
     */
    Object getEditorValue(String key) {
        EditorComponent editor = keyToEditorMap.get(key);
        
        if (editor == null) {
            throw new IllegalArgumentException("no editor with the given key"); //$NON-NLS-1$
        }
        
        return editor.getValue();
    }


    /**
     * Sets the value of the editor with the given key.
     * @param key the key of the editor
     * @param value the new value for the editor
     */    
    void setEditorValue(String key, Object value) {
        EditorComponent editor = keyToEditorMap.get(key);
        
        if (editor == null) {
            throw new IllegalArgumentException("no editor with the given key"); //$NON-NLS-1$
        }
        
        editor.setValue(value);
    }

    /**
     * Sets an error message for the given editor. Use null to clear the error message.
     * @param editor the editor to set the error message for
     * @param message the error message to use
     */
    void setEditorErrorMessage(EditorComponent editor, String message) {
        Label titleLabel = editorToTitleMap.get(editor);
        
        if (titleLabel != null) {
            titleLabel.setForeground(message != null ? ColorConstants.RED : ColorConstants.BLACK);
        }
        
        if (message == null || message.length () == 0) {
            Label errorLabel = editorToErrorMap.get(editor);

            if (errorLabel != null) {
                errorLabel.dispose ();
                editorToErrorMap.remove (editor);

                reflowSection ();
            }
        } else {
            Label errorLabel = editorToErrorMap.get(editor);
            
            if (errorLabel != null) {
                errorLabel.setText (message);
            } else {
                FormToolkit formToolkit = editorPanel.getFormToolkit ();
                
                errorLabel = formToolkit.createLabel (contentPanel, message);
                errorLabel.setForeground (ColorConstants.RED);
    
                TableWrapData data = new TableWrapData (TableWrapData.FILL, TableWrapData.MIDDLE, 1, 2);
                errorLabel.setLayoutData (data);
             
                if (titleLabel != null) {
                    errorLabel.moveAbove (titleLabel);
                } else {
                    errorLabel.moveAbove (editor.getEditorComponent ());
                }
    
                editorToErrorMap.put(editor, errorLabel);
                
                reflowSection ();
            }
        }
    }    
    

    /**
     * Method editorChanged
     *
     * @param component
     */
    void editorChanged (EditorComponent component) {
        editorPanel.sectionChanged(this);
        doValidate();
    }

    /**
     * Validates the metadata entered by the user. If any editor in the section contains
     * invalid data this should return false and set the appropriate error message for the
     * editors that contains invalid data.
     * @return true if data is valid, false if there is invalid data
     */
    abstract boolean doValidate();
    
    /**
     * Saves the values stored by the editor components back into the metadata.
     */
    abstract void doSave();
    
    /**
     * Reverts the values stored in the editor components to the values currently
     * stored in the metadata object being edited.
     */
    abstract void doRevert();

}
