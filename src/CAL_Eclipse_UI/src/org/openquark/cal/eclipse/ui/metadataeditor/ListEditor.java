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
 * ListEditor.java
 * Created: 22-Feb-07
 * By: Rick Cameron
 */


package org.openquark.cal.eclipse.ui.metadataeditor;

import java.util.Arrays;

import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.KeyAdapter;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.List;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.openquark.cal.eclipse.ui.CALEclipseUIPlugin;
import org.openquark.cal.eclipse.ui.util.ImageLoader;
import org.openquark.util.UnsafeCast;



/**
 * @author Rick Cameron
 * 
 */
final class ListEditor extends EditorComponent {

    private static boolean USE_IMAGES = false;
    
    private static final ImageLoader upImageLoader = new ImageLoader (CALEclipseUIPlugin.getImageDescriptor("/icons/up.gif")); //$NON-NLS-1$
    private static final ImageLoader downImageLoader = new ImageLoader (CALEclipseUIPlugin.getImageDescriptor("/icons/down.gif")); //$NON-NLS-1$
    
    /** The panel that contains the list and related buttons. */
    private Composite contentPanel;
    
    /** The new item text field. */
    private Text itemField;
    
    /** The list of data. */
    private List dataList; // maybe a ListViewer?
    
    /** Whether or not the text in the text field can be added as a new item. */
    private boolean canAddNewItem = true;

    /**
     * Constructor ListEditor
     * 
     * @param editorSection
     * @param key
     * @param title
     * @param description
     */
    public ListEditor (EditorSection editorSection, String key, String title, String description) {
        super (editorSection, key, title, description);
    }

    /**
     * @see org.openquark.cal.eclipse.ui.metadataeditor.EditorComponent#createEditorComponent(org.eclipse.swt.widgets.Composite, org.eclipse.ui.forms.widgets.FormToolkit)
     */
    @Override
    Control createEditorComponent (Composite parent, FormToolkit formToolkit) {
        contentPanel = formToolkit.createComposite (parent);
        formToolkit.paintBordersFor (contentPanel);
        
        contentPanel.setLayout (new GridLayout (4, false));
        
        // 0, 0
        itemField = formToolkit.createText (contentPanel, ""); //$NON-NLS-1$
        GridDataFactory.swtDefaults ().align (SWT.FILL, SWT.CENTER).grab (true, false).applyTo (itemField);
        itemField.setToolTipText (MetadataEditorMessages.ItemFieldToolTip);
        
        // 1, 0
        final Button addButton = formToolkit.createButton (contentPanel, MetadataEditorMessages.AddItemButtonLabel, SWT.PUSH);
        GridDataFactory.fillDefaults ().applyTo (addButton);
        addButton.setToolTipText (MetadataEditorMessages.AddItemButtonToolTip);
        addButton.setEnabled (false);
        
        // 2, 0
        final Button removeButton = formToolkit.createButton (contentPanel, MetadataEditorMessages.RemoveItemButtonLabel, SWT.PUSH);
        GridDataFactory.fillDefaults ().applyTo (removeButton);
        removeButton.setToolTipText (MetadataEditorMessages.RemoveItemButtonToolTip);
        removeButton.setEnabled (false);
        
        // 3, 0
        formToolkit.createLabel (contentPanel, ""); //$NON-NLS-1$
        
        // 0, 1 - 2, 4 
        dataList = new List (contentPanel, SWT.V_SCROLL);
        formToolkit.adapt (dataList, true, false);
        dataList.setData(FormToolkit.KEY_DRAW_BORDER, FormToolkit.TREE_BORDER);
        GridDataFactory.fillDefaults ().grab (true, true).span (3, 4).applyTo (dataList);
        
        // 3, 1
        formToolkit.createLabel (contentPanel, ""); //$NON-NLS-1$
        
        // 3, 2
        final Button upButton = formToolkit.createButton (
                                    contentPanel, 
                                    USE_IMAGES 
                                        ? ""  //$NON-NLS-1$
                                        : MetadataEditorMessages.MoveUpItemButtonLabel, 
                                    SWT.PUSH);
        GridDataFactory.fillDefaults ().applyTo (upButton);
        if (USE_IMAGES) {
            upButton.setImage (upImageLoader.getImage ());
        }
        upButton.setToolTipText (MetadataEditorMessages.MoveUpItemButtonToolTip);
        upButton.setEnabled (false);

        // 3, 3
        final Button downButton = formToolkit.createButton (
                                    contentPanel, 
                                    USE_IMAGES 
                                        ? ""  //$NON-NLS-1$
                                        : MetadataEditorMessages.MoveDownItemButtonLabel, 
                                    SWT.PUSH);
        GridDataFactory.fillDefaults ().applyTo (downButton);
        if (USE_IMAGES) {
            downButton.setImage (downImageLoader.getImage ());
        }
        downButton.setToolTipText (MetadataEditorMessages.MoveDownItemButtonToolTip);
        downButton.setEnabled (false);
        
        // 3, 4
        formToolkit.createLabel (contentPanel, ""); //$NON-NLS-1$
        
        // Event handlers
        
        itemField.addKeyListener (new KeyAdapter () {
            @Override
            public void keyPressed (KeyEvent event) {
                if (event.keyCode == SWT.CR) {
                    addNewItem ();
                }
                enableButtons (removeButton, upButton, downButton);
            }
        });
        
        addButton.addSelectionListener (new SelectionAdapter () {
            @Override
            public void widgetSelected (SelectionEvent e) {
                addNewItem ();
                enableButtons (removeButton, upButton, downButton);
            }
        });

        removeButton.addSelectionListener (new SelectionAdapter () {
            @Override
            public void widgetSelected (SelectionEvent e) {
                removeSelectedItem ();
                enableButtons (removeButton, upButton, downButton);
                enableAddButton (addButton);
            }
        });


        upButton.addSelectionListener (new SelectionAdapter () {
            @Override
            public void widgetSelected (SelectionEvent e) {
                moveSelectedItemUp ();
                enableButtons (removeButton, upButton, downButton);
            }
        });
        
        downButton.addSelectionListener (new SelectionAdapter () {
            @Override
            public void widgetSelected (SelectionEvent e) {
                moveSelectedItemDown ();
                enableButtons (removeButton, upButton, downButton);
            }
        });
        
        dataList.addKeyListener (new KeyAdapter () {
            @Override
            public void keyPressed (KeyEvent e) {
                if (e.keyCode == SWT.DEL) {
                    removeSelectedItem ();
                    enableButtons (removeButton, upButton, downButton);
                    enableAddButton (addButton);
                }
            }
        });
        
        dataList.addSelectionListener (new SelectionAdapter () {
            @Override
            public void widgetSelected (SelectionEvent e) {
                enableButtons (removeButton, upButton, downButton);
            }
        });
        
        itemField.addModifyListener (new ModifyListener () {
            public void modifyText (ModifyEvent e) {
                enableAddButton (addButton);
            }
        });

        return contentPanel;
    }

    /**
     * Method dataListContains
     * 
     * @param text
     * 
     * @return Returns true iff the given text is in the dataList
     */
    private boolean dataListContains (String text) {
        String[] items = dataList.getItems ();
        
        for (final String item : items) {
            if (item.equals (text)) {
                return true;
            }
        }
        
        return false;
    }

    /**
     * Method moveSelectedItemUp
     * 
     */
    private void moveSelectedItemUp () {
        int index = dataList.getSelectionIndex ();
        
        if (index > 0) {
            String item = dataList.getItem (index);
            dataList.remove (index);
            dataList.add (item, index - 1);
            dataList.setSelection (index - 1);
        }
    }

    /**
     * Method moveSelectedItemDown
     * 
     */
    private void moveSelectedItemDown () {
        int index = dataList.getSelectionIndex ();

        if (index != -1 && index < dataList.getItemCount () - 1) {
            String item = dataList.getItem (index);
            dataList.remove (index);
            dataList.add (item, index + 1);
            dataList.setSelection (index + 1);
        }
    }
    
    /**
     * Adds a new item with the value entered in the item text field to
     * the list, if the new item is valid and not already in the list.
     */    
    private void addNewItem () {
        if (canAddNewItem) {
//            DefaultListModel listModel = (DefaultListModel) dataList.getModel();
//            listModel.addElement(itemField.getText());
//            dataList.setSelectedValue(itemField.getText(), true);
            
            dataList.add (itemField.getText ());
            dataList.setSelection (dataList.getItemCount () - 1);
            
            itemField.setText(""); //$NON-NLS-1$
            
            editorChanged();
        }        
    }

    /**
     * Removes the selected item from the list.
     */
    private void removeSelectedItem () {
        int index = dataList.getSelectionIndex ();
        
        if (index >= 0) {
            dataList.remove (index);
            
            editorChanged ();
        }
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public Control getEditorComponent () {
        return contentPanel;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Object getValue () {
        String[] items = dataList.getItems ();
        
        return Arrays.asList (items);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setValue (Object value) {
        dataList.removeAll ();

        if (value instanceof java.util.List) {
            java.util.List<Object> list = UnsafeCast.unsafeCast(value);
            for (final Object listElement : list) {
                if (listElement instanceof String) {
                    dataList.add ((String)listElement);
                }
            }
        }
    }

    /**
     * Method enableButtons
     * 
     * @param removeButton
     * @param upButton
     * @param downButton
     */
    private void enableButtons (final Button removeButton, final Button upButton, final Button downButton) {
        int selectedIndex = dataList.getSelectionIndex ();
        
        upButton.setEnabled (selectedIndex > 0);
        downButton.setEnabled (selectedIndex != -1 && selectedIndex < dataList.getItemCount () - 1);
        removeButton.setEnabled (selectedIndex != -1);
    }

    /**
     * Method enableAddButton
     * 
     * @param addButton
     */
    private void enableAddButton (final Button addButton) {
        String text = itemField.getText ();
        
        canAddNewItem = text.trim ().length () > 0 && !dataListContains (text);
        
        addButton.setEnabled (canAddNewItem);
    }

}
