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
 * EditorComponent.java
 * Created: 20-Feb-07
 * By: Rick Cameron
 */


package org.openquark.cal.eclipse.ui.metadataeditor;

import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.ui.forms.widgets.FormToolkit;



/**
 * @author rcameron
 *
 */
abstract class EditorComponent {

    /** The title of the editor component. */
    private final String title;
    
    /** The description of the editor component. */
    private final String description;
    
    /** The unique key that identifies this editor inside its editor section. */
    private final String key;

    /** The editor section this component is used in. */
    private final EditorSection editorSection;

    /**
     * Constructor EditorComponent
     * 
     * @param section
     */
    EditorComponent (EditorSection section) {
        this (section, null, null, null);
    }

    /**
     * Constructor EditorComponent
     * 
     * @param editorSection
     * @param key
     */
    EditorComponent (EditorSection editorSection, String key) {
        this (editorSection, key, null, null);
    }

    /**
     * Constructor EditorComponent
     * 
     * @param editorSection
     * @param key
     * @param title
     * @param description
     */
    EditorComponent (final EditorSection editorSection, final String key, final String title, final String description) {
        this.title = title;
        this.description = description;
        this.key = key;
        this.editorSection = editorSection;
    }
    
    /**
     * @return the title of the editor. 
     */
    public String getTitle() {
        return title;
    }
    
    /**
     * @return the description of the editor. May be null if the editor doesn't have a description.
     */
    public String getDescription() {
        return description;
    }
    
    /**
     * @return the unique key that identifies this editor component.
     * Maybe be null if this editor does not have a unique key.
     */
    public String getKey() {
        return key;
    }
    
    /**
     * @return the editor section this editor is used by
     */
    EditorSection getEditorSection() {
        return editorSection;
    }
    
    /**
     * Notifies the editor section that this editor belongs to that the
     * value stored by the editor has changed.
     */
    void editorChanged() {
        editorSection.editorChanged(this);
    }
    
    /**
     * Method createEditorComponent
     * 
     * @param parent
     * @param formToolkit 
     *
     * @return Returns the newly-created {@link Control}
     */
    abstract Control createEditorComponent (Composite parent, FormToolkit formToolkit);
    
    /**
     * @return the actual editor component
     */
    public abstract Control getEditorComponent();
    
    /**
     * @return the value stored by the editor
     */
    public abstract Object getValue();
    
    /**
     * Sets the value stored by the editor.
     * @param value the new value of the editor
     */
    public abstract void setValue(Object value);

}
