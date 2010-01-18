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
 * CustomAttributeEditor.java
 * Created: 8-Mar-07
 * By: Rick Cameron
 */


package org.openquark.cal.eclipse.ui.metadataeditor;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.CellEditor;
import org.eclipse.jface.viewers.ICellModifier;
import org.eclipse.jface.viewers.ILabelProviderListener;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TextCellEditor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.swt.widgets.ToolBar;
import org.eclipse.swt.widgets.ToolItem;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.openquark.cal.eclipse.ui.CALEclipseUIPlugin;
import org.openquark.cal.eclipse.ui.util.ImageLoader;



/**
 * @author Rick Cameron
 * 
 */
final class CustomAttributeEditor extends EditorComponent {
    
    private static final String NAME_PROP = "name"; //$NON-NLS-1$
    private static final String VALUE_PROP = "value"; //$NON-NLS-1$
    
    private static final ImageLoader addImageLoader = new ImageLoader (CALEclipseUIPlugin.getImageDescriptor("/icons/add.gif")); //$NON-NLS-1$
    private static final ImageLoader removeImageLoader = new ImageLoader (CALEclipseUIPlugin.getImageDescriptor("/icons/remove.gif")); //$NON-NLS-1$
    
    private class CellModifier implements ICellModifier {
        public void modify (Object element, String property, Object value) {
//          System.out.println ("modify: element is " + ((element == null) ? "null" : "a <" + element.getClass () + ">"));
            
            int index = indexForProperty (property);
          
            if (index != -1 && element instanceof TableItem) {
                TableItem tableItem = (TableItem)element;

                String[] data = (String[])tableItem.getData ();

                if (!data[index].equals (value)) {
                    data[index] = (String)value;

                    attributesTableViewer.refresh (data);

                    editorChanged ();
                }
            }                     
      }
  
      public Object getValue (Object element, String property) {
//          System.out.println ("getValue: element is " + ((element == null) ? "null" : "a <" + element.getClass () + ">"));
          
          int index = indexForProperty (property);
          
          if (index != -1 && element instanceof String[]) {
              String[] data = (String[])element;

              return data[index];
          } 
          
          return null;
      }
  
      public boolean canModify (Object element, String property) {
          return true;
      }
      
      private int indexForProperty (String property) {
          if (property.equals (NAME_PROP)) {
            return 0;
        } else if (property.equals (VALUE_PROP)) {
            return 1;
        } else {
              System.out.println ("Unknown property <" + property + ">"); //$NON-NLS-1$ //$NON-NLS-2$
              
              return -1;
          }
      }
    }

    private static final ITableLabelProvider labelProvider = new ITableLabelProvider () {
        
        public void removeListener (ILabelProviderListener listener) {
        }
    
        public boolean isLabelProperty (Object element, String property) {
            return true;
        }
    
        public void dispose () {
        }
    
        public void addListener (ILabelProviderListener listener) {
        }
    
        public String getColumnText (Object element, int columnIndex) {
            if (element instanceof String[]) {
                String[] data = (String[])element;
                
                return data[columnIndex];
            }
            return null;
        }
    
        public Image getColumnImage (Object element, int columnIndex) {
            return null;
        }
    
    };

    private Composite panel;
    
    private TableViewer attributesTableViewer;

    /**
     * Constructor CustomAttributeEditor
     * 
     * @param editorSection
     * @param key
     * @param title
     * @param description
     */
    CustomAttributeEditor (EditorSection editorSection, String key, String title, String description) {
        super (editorSection, key, title, description);
    }

    /**
     * @see org.openquark.cal.eclipse.ui.metadataeditor.EditorComponent#createEditorComponent(org.eclipse.swt.widgets.Composite, org.eclipse.ui.forms.widgets.FormToolkit)
     */
    @Override
    Control createEditorComponent (Composite parent, FormToolkit formToolkit) {
        panel = formToolkit.createComposite (parent);
        formToolkit.paintBordersFor (panel);
        GridLayoutFactory.swtDefaults ().applyTo (panel);
        
        ToolBar toolBar = new ToolBar (panel, SWT.FLAT);
        addToolbarButton (
            toolBar, 
            addImageLoader.getImage (), 
            MetadataEditorMessages.AddCustomAttributeButtonToolTip,
            new SelectionAdapter () {
                @Override
                public void widgetSelected (SelectionEvent e) {
                    onAdd ();
                }
            });
        addToolbarButton (
            toolBar, 
            removeImageLoader.getImage (), 
            MetadataEditorMessages.DeleteCustomAttributeButtonTooltip,
            new SelectionAdapter () {
                @Override
                public void widgetSelected (SelectionEvent e) {
                    onRemove ();
                }
            });

        attributesTableViewer = createTableViewer (formToolkit);
        
        return panel;
    }

    /**
     * Method createTableViewer
     * @param formToolkit 
     *
     * @return Returns a configured {@link TableViewer}
     */
    private TableViewer createTableViewer (FormToolkit formToolkit) {
        Table attributesTable = createTable (formToolkit);
        GridDataFactory
            .swtDefaults ()
            .align (SWT.FILL, SWT.FILL)
            .grab (true, true)
            .hint (SWT.DEFAULT, 100)
            .applyTo (attributesTable);
        
        final TableViewer tableViewer = new TableViewer (attributesTable);
        tableViewer.setContentProvider (new ArrayContentProvider ());
        tableViewer.setLabelProvider (labelProvider);
        tableViewer.setColumnProperties (new String[] { NAME_PROP, VALUE_PROP });
        tableViewer.setCellEditors (new CellEditor[] { 
                                        new TextCellEditor (attributesTable), 
                                        new TextCellEditor (attributesTable) 
                                    });
        tableViewer.setCellModifier (new CellModifier ());
        
        return tableViewer;
    }

    /**
     * Method createTable
     * 
     * @param formToolkit
     */
    private Table createTable (FormToolkit formToolkit) {
        Table attributesTable = formToolkit.createTable (panel, SWT.FULL_SELECTION);
//        attributesTable.setBackground (ColorConstants.GREY);
        attributesTable.setHeaderVisible (true);
        attributesTable.setLinesVisible (true);
        
        TableColumn nameColumn = new TableColumn (attributesTable, SWT.LEFT);
        nameColumn.setText (MetadataEditorMessages.NameColumnHeading); 
        nameColumn.setWidth (100);
        
        TableColumn valueColumn = new TableColumn (attributesTable, SWT.LEFT);
        valueColumn.setText (MetadataEditorMessages.ValueColumnHeading); 
        valueColumn.setWidth (200);
        
        return attributesTable;
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

    /**
     * Method onAdd
     * 
     */
    private void onAdd () {
        attributesTableViewer.cancelEditing ();
        
        Object item = new String[] { MetadataEditorMessages.NewItemName, "" }; //$NON-NLS-1$ 
        
        attributesTableViewer.add (item);
//        setBackgroundColourOfItems ();
        
        attributesTableViewer.editElement (item, 0);
        
        editorChanged ();
    }

    /**
     * Method onRemove
     * 
     */
    private void onRemove () {
        ISelection selection = attributesTableViewer.getSelection ();
        
        if (selection instanceof IStructuredSelection) {
            IStructuredSelection structuredSelection = (IStructuredSelection)selection;
            
            if (structuredSelection.size () == 1) {
                Object selectedElement = structuredSelection.getFirstElement ();
                
                attributesTableViewer.cancelEditing ();
                
                Table table = attributesTableViewer.getTable ();
                int index = table.getSelectionIndex ();
                
                attributesTableViewer.remove (selectedElement);
                
                index = Math.min (index, table.getItemCount () - 1);
                if (index != -1) {
                    table.setSelection (index);
                }
                
                editorChanged ();
            }
        }
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
        List<Object> attributes = new ArrayList<Object> ();
        
        Table table = attributesTableViewer.getTable ();
        
        for (int index = 0; index < table.getItemCount (); ++index) {
            TableItem item = table.getItem (index);

            assert (item.getData () instanceof String[]);
            
            attributes.add (item.getData ());
        }
        
        return attributes;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setValue (Object value) {
        attributesTableViewer.setInput (value);
        
//        setBackgroundColourOfItems ();
    }

//    /**
//     * Method setBackgroundColourOfItems
//     *
//     */
//    private void setBackgroundColourOfItems () {
//        Table table = attributesTableViewer.getTable ();
//        
//        for (int i = 0; i < table.getItemCount (); ++i)
//            table.getItem (i).setBackground (ColorConstants.WHITE);
//    }

}
