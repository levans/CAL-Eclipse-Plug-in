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
 * ExampleEditorSection.java
 * Created: 20-Feb-07
 * By: Rick Cameron
 */


package org.openquark.cal.eclipse.ui.metadataeditor;

import java.util.List;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.ToolBar;
import org.eclipse.swt.widgets.ToolItem;
import org.eclipse.ui.forms.widgets.TableWrapData;
import org.openquark.cal.eclipse.ui.CALEclipseUIPlugin;
import org.openquark.cal.eclipse.ui.metadataeditor.ExampleEditor.ExampleEditorListener;
import org.openquark.cal.eclipse.ui.util.ImageLoader;
import org.openquark.cal.metadata.CALExample;
import org.openquark.cal.metadata.FunctionalAgentMetadata;




/**
 * @author rcameron
 *
 */
final class ExampleEditorSection extends EditorSection {

    private static final ImageLoader addImageLoader = new ImageLoader (CALEclipseUIPlugin.getImageDescriptor("/icons/add.gif")); //$NON-NLS-1$
    private final ExampleEditorListener exampleEditorListener = new ExampleEditorListener () {
        public void onRemove (ExampleEditor editor) {
            onRemoveEditor (editor);
        }
    };
    
    private final ToolBar toolBar;
    private final Label infoLabel;

    /**
     * Constructor ExampleEditorSection
     *
     * @param parent
     */
    public ExampleEditorSection (CALMetadataEditorPanel parent) {
        super (parent, MetadataEditorMessages.UsageExamples_Header);
    
        // 0, 0
        toolBar = createToolbar ();
        
        // 1, 0
        infoLabel = getEditorPanel ().getFormToolkit ().createLabel (getContentPanel(), MetadataEditorMessages.NewExampleHint);
        infoLabel.setLayoutData (new TableWrapData (TableWrapData.LEFT, TableWrapData.MIDDLE));
    }

    /**
     * Method createToolbar
     * 
     * @return Returns the ToolBar
     */
    private ToolBar createToolbar () {
        ToolBar toolBar = new ToolBar (getContentPanel (), SWT.FLAT);

        ToolItem addButton = addToolbarButton (
                toolBar, 
                addImageLoader.getImage (),
                MetadataEditorMessages.AddExampleButtonToolTip,
                new SelectionAdapter () {
                    @Override
                    public void widgetSelected (SelectionEvent e) {
                        onAdd ();
                    }
                });
        
        addButton.setToolTipText (MetadataEditorMessages.AddExampleButtonToolTip);
        
        return toolBar;
    }
    
    /**
     * Method onAdd
     * 
     */
    private void onAdd () {
        ExampleEditor exampleEditor = new ExampleEditor (this, exampleEditorListener);
        addEditor (exampleEditor);
        exampleEditor.setValue (null); // The editor will use defaults
        
        updateInfoLabel ();

        reflowSection ();
    }

    /**
     * Method addToolbarButton
     * 
     * @param toolBar
     * @param image 
     * @param tooltip 
     * @param selectionListener
     * 
     * @return Returns the {@link ToolItem}
     */
    private ToolItem addToolbarButton (ToolBar toolBar, Image image, String tooltip, SelectionAdapter selectionListener) {
        ToolItem toolItem = new ToolItem (toolBar, SWT.PUSH);
        toolItem.setImage (image);
        toolItem.setToolTipText (tooltip);
        toolItem.addSelectionListener (selectionListener);
        
        return toolItem;
    }

    /* (non-Javadoc)
     * @see org.openquark.cal.eclipse.ui.metadataeditor.EditorSection#doRevert()
     */
    @Override
    void doRevert () {
        FunctionalAgentMetadata metadata = (FunctionalAgentMetadata)getMetadata ();
        
        CALExample[] examples = metadata.getExamples ();
        
        List<EditorComponent> editors = getEditors ();
        
        boolean createEditors = editors.size () != examples.length;
        
        for (int i = 0; i < examples.length; i++) {
            CALExample example = examples[i];
        
            if (createEditors) {
                ExampleEditor exampleEditor = new ExampleEditor (this, exampleEditorListener);
                addEditor (exampleEditor);
                exampleEditor.setValue (example);
            } else {
                EditorComponent editor = editors.get (i);
                
                editor.setValue (example);
            }
        }
        
        updateInfoLabel ();
    }
    
    /**
     * @see org.openquark.cal.eclipse.ui.metadataeditor.EditorSection#addEditor(org.openquark.cal.eclipse.ui.metadataeditor.EditorComponent)
     */
    @Override
    void addEditor (EditorComponent editor) {
        super.addEditor (editor);
        
        Control editorComponent = editor.getEditorComponent ();
        
        toolBar.moveBelow (editorComponent);
        infoLabel.moveBelow (toolBar);
    }

    /* (non-Javadoc)
     * @see org.openquark.cal.eclipse.ui.metadataeditor.EditorSection#doSave()
     */
    @Override
    void doSave () {
        FunctionalAgentMetadata metadata = (FunctionalAgentMetadata)getMetadata ();

        List<EditorComponent> editors = getEditors ();
        
        CALExample[] examples = new CALExample [editors.size ()];
        
        for (int i = 0; i < editors.size (); ++i) {
            EditorComponent editorComponent = editors.get (i);
            
            examples[i] = (CALExample)editorComponent.getValue ();
        }
        
        metadata.setExamples (examples);
    }

    /* (non-Javadoc)
     * @see org.openquark.cal.eclipse.ui.metadataeditor.EditorSection#doValidate()
     */
    @Override
    boolean doValidate () {
        return true;
    }

    private void onRemoveEditor (ExampleEditor editor) {
        removeEditor (editor);
        
        updateInfoLabel ();
    }
    
    /**
     * Method updateInfoLabel
     * 
     */
    private void updateInfoLabel () {
//        infoLabel.setVisible (getEditors ().size () == 0);
    }

}
