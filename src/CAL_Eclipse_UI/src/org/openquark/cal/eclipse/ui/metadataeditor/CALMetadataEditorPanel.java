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
 * CALMetadataEditorPanel.java
 * Created: 20-Feb-07
 * By: Rick Cameron
 */


package org.openquark.cal.eclipse.ui.metadataeditor;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.ui.forms.widgets.ScrolledForm;
import org.eclipse.ui.forms.widgets.Section;
import org.eclipse.ui.forms.widgets.TableWrapLayout;
import org.openquark.cal.eclipse.ui.metadataeditor.CALMetadataEditor.Input;
import org.openquark.cal.metadata.ArgumentMetadata;
import org.openquark.cal.metadata.CALFeatureMetadata;
import org.openquark.cal.metadata.ClassMethodMetadata;
import org.openquark.cal.metadata.DataConstructorMetadata;
import org.openquark.cal.metadata.FunctionMetadata;
import org.openquark.cal.metadata.FunctionalAgentMetadata;
import org.openquark.cal.metadata.InstanceMethodMetadata;
import org.openquark.cal.metadata.ModuleMetadata;
import org.openquark.cal.metadata.TypeClassMetadata;
import org.openquark.cal.metadata.TypeConstructorMetadata;
import org.openquark.cal.services.Status;



/**
 * @author rcameron
 *
 */
final class CALMetadataEditorPanel {

    public static final String PROP_HASCHANGED = "hasChanged"; //$NON-NLS-1$

    private static final boolean ALWAYS_USE_FLAT_BORDERS = true;

    private final CALMetadataEditor.Input input;
    
    /** The metadata object being edited by this editor panel. */
    private CALFeatureMetadata metadata;

    private final FormToolkit formToolkit;
    
    private ScrolledForm mainForm;
    
    /** The editor sections contained in this editor panel in the order they appear. */
    private final List<EditorSection> editorSections = new ArrayList<EditorSection> ();
    
    /** Whether or not the values stored by the editors in the editor panel have changed. */
    private boolean hasChanged;
    
    private final PropertyChangeSupport propertyChangeSupport = new PropertyChangeSupport (this);

    /**
     * Constructor CALMetadataEditorPanel
     *
     * @param parent
     * @param input 
     */
    CALMetadataEditorPanel (Composite parent, CALMetadataEditor.Input input) {
        this.input = input;
        
        metadata = input.getMetadata ();
        
        formToolkit = new FormToolkit (parent.getDisplay ());
        if (ALWAYS_USE_FLAT_BORDERS) {
            formToolkit.setBorderStyle (SWT.NULL);
        }

        createContents (parent);
    }

    /**
     * Method isDirty
     *
     * @return Returns true iff the metadata has been modified
     */
    boolean isDirty () {
        return hasChanged;
    }
    
    
    /**
     * @return the formToolkit
     */
    FormToolkit getFormToolkit () {
        return formToolkit;
    }
    
    /**
     * Method createSection
     *
     * @param sectionStyle
     * @return Returns the new Section
     */
    Section createSection (int sectionStyle) {
        return formToolkit.createSection (mainForm.getBody (), sectionStyle);
    }
    
    /**
     * Method createContents
     * @param parent 
     *
     */
    private void createContents (Composite parent) {
        mainForm = formToolkit.createScrolledForm (parent);
        TableWrapLayout tableWrapLayout = new TableWrapLayout ();
        tableWrapLayout.numColumns = 1;
        mainForm.getBody ().setLayout (tableWrapLayout);
        
        addSections ();
        revert ();
    }

    /**
     * Method addSections
     *
     */
    private void addSections () {
        // Add the basic metadata editing section
        addSection (new FeatureEditorSection (this));

        // Add entity metadata editing sections
        if (metadata instanceof FunctionalAgentMetadata || metadata instanceof InstanceMethodMetadata) {
            addSection(new GemEntityEditorSection(this));
            
            boolean hasReturnValue = metadata instanceof FunctionMetadata 
                                     || metadata instanceof ClassMethodMetadata 
                                     || metadata instanceof InstanceMethodMetadata;
            
            addSection(new EntityArgumentEditorSection(this, hasReturnValue));
            
            addSection(new ExampleEditorSection(this));
        }

        // Add an argument editing section
        if (metadata instanceof ArgumentMetadata) {
            addSection(new ArgumentEditorSection(this));
        }
        
        // Add a custom attribute section
        addSection(new CustomAttributeEditorSection(this));
        
        EditorSection firstSection = editorSections.get (0);
        firstSection.setExpanded (true);
    }

    /**
     * Method addSection
     *
     * @param section
     */
    private void addSection (EditorSection section) {
        editorSections.add (section);
    }

    /**
     * Method updateTitleLabel
     *
     */
    private void updateTitleLabel() {
        mainForm.setText (getTypeString () + " " + getDisplayText ()); //$NON-NLS-1$
    }


//    /**
//     * Method getArgText
//     *
//     * @return Returns a String that describes the argument
//     */
//    private String getArgText () {
//      if (address.getParameter(NavAddress.ARGUMENT_PARAMETER) != null) {
//      
//      int argumentNumber = Integer.parseInt(address.getParameter(NavAddress.ARGUMENT_PARAMETER));
//      ModuleTypeInfo moduleInfo = owner.getPerspective().getWorkingModuleTypeInfo();
//      ScopedEntityNamingPolicy namingPolicy = new UnqualifiedUnlessAmbiguous(moduleInfo);
//      NavAddress parentAddress;
//      if (address.getMethod() == NavAddress.INSTANCE_METHOD_METHOD) {
//          parentAddress = NavAddress.getAddress(address.toFeatureName()); // this strips out the &argument=n parameter
//      } else {
//          parentAddress = address.withAllStripped();
//      }
//      String[] typeStrings = NavAddressHelper.getTypeStrings(owner, parentAddress, namingPolicy);
//      argTypeLabel.setText(" :: " + typeStrings[argumentNumber]);
//  }
//        
//        return "";
//    }

    /**
     * Method getDisplayText
     *
     * @return Returns a String that describes the feature
     */
    private String getDisplayText () {
//        if (metadata.getDisplayName () != null)
//            return metadata.getDisplayName ();
        
        return input.getName ();
    }

    /**
     * Method getTypeString
     *
     * @return Returns a string that represents the type of metadata being edited
     */
    private String getTypeString () {
        if (metadata instanceof ModuleMetadata) {
            return MetadataEditorMessages.ModuleCaption;
        } else if (metadata instanceof FunctionMetadata) {
            return MetadataEditorMessages.FunctionCaption;
        } else if (metadata instanceof TypeClassMetadata) {
            return MetadataEditorMessages.ClassCaption;
        } else if (metadata instanceof TypeConstructorMetadata) {
            return MetadataEditorMessages.TypeCaption;
        } else if (metadata instanceof DataConstructorMetadata) {
            return MetadataEditorMessages.ConstructorCaption;
        } else if (metadata instanceof ArgumentMetadata) {
            return MetadataEditorMessages.ArgumentCaption;
        } else {
            return MetadataEditorMessages.UnknownCaption;
        }
    }


    /**
     * Method revert
     *
     */
    private void revert () {
        for (final EditorSection section : editorSections) {
            section.doRevert();
            section.doValidate();
        }

        setChangeFlag (false);
        updateTitleLabel();
    }

    /**
     * Method doSave
     *
     * @param monitor
     */
    void doSave (IProgressMonitor monitor) {
        // Make sure all sections are valid
        if (!checkValues()) {
            MessageDialog.openError (null, MetadataEditorMessages.InvalidValues_Header, MetadataEditorMessages.InvalidValues_Message);
            
            return;
        }
        
        // Save each section
        for (final EditorSection section : editorSections) {
            section.doSave();
        }
        
        // Now try to permanently store the metadata.
        // If this succeeds, the metadata will be reloaded
        saveMetadata();
    }

    /**
     * Method checkValues
     *
     * @return Returns true iff the metadata is valid
     */
    private boolean checkValues () {
        boolean hasErrors = false;

        for (final EditorSection section : editorSections) {
            if (!section.doValidate()) {
                hasErrors = true;
            }
        }
        
        return !hasErrors;
    }

    /**
     * Method saveMetadata
     *
     * @return Returns true iff the metadata was saved successfully
     */
    private boolean saveMetadata () {
        Status saveStatus = new Status ("Save status"); //$NON-NLS-1$
        
        return getInput ().saveMetadata (metadata, saveStatus);
    }

    /**
     * Method sectionChanged
     *
     * @param section
     */
    void sectionChanged (EditorSection section) {
        setChangeFlag (true);
    }

    /**
     * Method setChangeFlag
     * @param value 
     *
     */
    private void setChangeFlag (boolean value) {
        boolean oldValue = hasChanged;
        
        hasChanged = value;
        
        propertyChangeSupport.firePropertyChange (PROP_HASCHANGED, oldValue, hasChanged);
    }

    /**
     * Method getMetadata
     *
     * @return Returns the {@link CALFeatureMetadata} being edited
     */
    CALFeatureMetadata getMetadata () {
        return metadata;
    }
    
    /**
     * Method getInput
     *
     * @return Returns the current {@link org.openquark.cal.eclipse.ui.metadataeditor.CALMetadataEditor.Input}
     */
    Input getInput () {
        return input;
    }
    
    //
    // Property change support
    //

    void addPropertyChangeListener (PropertyChangeListener listener) {
        propertyChangeSupport.addPropertyChangeListener (listener);
    }
    
    void removePropertyChangeListener (PropertyChangeListener listener) {
        propertyChangeSupport.removePropertyChangeListener (listener);
    }

    /**
     * Method setFocus
     *
     */
    void setFocus () {
        mainForm.setFocus ();
    }

    /**
     * Method dispose
     *
     */
    void dispose () {
        formToolkit.dispose ();
    }

    /**
     * Method refresh
     *
     */
    void refresh () {
        metadata = input.getMetadata ();
        
        revert ();
    }
    
}
