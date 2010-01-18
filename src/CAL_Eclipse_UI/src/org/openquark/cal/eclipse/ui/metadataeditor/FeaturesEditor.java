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
 * FeaturesEditor.java
 * Created: 27-Mar-07
 * By: Rick Cameron
 */


package org.openquark.cal.eclipse.ui.metadataeditor;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.DecoratingLabelProvider;
import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.ViewerSorter;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.openquark.cal.compiler.ModuleName;
import org.openquark.cal.compiler.ModuleSourceDefinition;
import org.openquark.cal.compiler.ScopedEntity;
import org.openquark.cal.eclipse.core.CALModelManager;
import org.openquark.cal.eclipse.core.builder.CALBuilder;
import org.openquark.cal.eclipse.ui.CALEclipseUIPlugin;
import org.openquark.cal.eclipse.ui.actions.ActionMessages;
import org.openquark.cal.eclipse.ui.util.ImageLoader;
import org.openquark.cal.eclipse.ui.views.CALModuleContentProvider;
import org.openquark.cal.eclipse.ui.views.HierarchicalNode;
import org.openquark.cal.eclipse.ui.views.ModuleTreeContentProvider;
import org.openquark.cal.eclipse.ui.views.ModuleTreeLabelProvider;
import org.openquark.cal.eclipse.ui.views.ProblemMarkerDecorator;
import org.openquark.cal.services.CALFeatureName;
import org.openquark.cal.services.FeatureName.FeatureType;
import org.openquark.util.UnsafeCast;



/**
 * @author Rick Cameron
 *
 */
final class FeaturesEditor extends EditorComponent {
    
    private static final int MIN_TREE_HEIGHT = 200;
    
    private static final ImageLoader moduleImageLoader = new ImageLoader (CALEclipseUIPlugin.getImageDescriptor("/icons/nav_module.png")); //$NON-NLS-1$
    private static final ImageLoader typeConsImageLoader = new ImageLoader (CALEclipseUIPlugin.getImageDescriptor("/icons/nav_typeconstructor.gif")); //$NON-NLS-1$
    private static final ImageLoader typeClassImageLoader = new ImageLoader (CALEclipseUIPlugin.getImageDescriptor("/icons/nav_typeclass.gif")); //$NON-NLS-1$
    private static final ImageLoader dataConsImageLoader = new ImageLoader (CALEclipseUIPlugin.getImageDescriptor("/icons/nav_dataconstructor.gif")); //$NON-NLS-1$
    private static final ImageLoader functionImageLoader = new ImageLoader (CALEclipseUIPlugin.getImageDescriptor("/icons/nav_function.gif")); //$NON-NLS-1$
    
    private static final LabelProvider calLabelProvider = new LabelProvider () {

        /**
         * @see org.eclipse.jface.viewers.LabelProvider#getText(java.lang.Object)
         */
        @Override
        public String getText (Object element) {
            if (element instanceof CALFeatureName) {
                CALFeatureName featureName = (CALFeatureName)element;
                
                return featureName.getName ();
            }
            
            return super.getText (element);
        }
        
        /**
         * @see org.eclipse.jface.viewers.LabelProvider#getImage(java.lang.Object)
         */
        @Override
        public Image getImage (Object element) {
            if (element instanceof CALFeatureName) {
                CALFeatureName featureName = (CALFeatureName)element;
                
                FeatureType featureType = featureName.getType ();
                
                if (featureType.equals (CALFeatureName.MODULE)) {
                    return moduleImageLoader.getImage ();
                } else if (featureType.equals (CALFeatureName.TYPE_CONSTRUCTOR)) {
                    return typeConsImageLoader.getImage ();
                } else if (featureType.equals (CALFeatureName.TYPE_CLASS)) {
                    return typeClassImageLoader.getImage ();
                } else if (featureType.equals (CALFeatureName.DATA_CONSTRUCTOR)) {
                    return dataConsImageLoader.getImage ();
                } else if (featureType.equals (CALFeatureName.FUNCTION)) {
                    return functionImageLoader.getImage ();
                }
            }
            
            return super.getImage (element);
        }
    };

    private static final CALModuleContentProvider moduleContentProvider = new CALModuleContentProvider () {
        
        @Override
        public void setShowPrivateElements (boolean value) {
        }
    
        @Override
        public void setShowModuleHierarchy (boolean value) {
        }
    
        @Override
        public void setShowElementHierarchy (boolean value) {
        }
    
        @Override
        public void setLinkWithEditor (boolean value) {
        }
    
        @Override
        public boolean getShowPrivateElements () {
            return true;
        }
    
        @Override
        public boolean getShowModuleHierarchy () {
            return true;
        }
    
        @Override
        public boolean getShowElementHierarchy () {
            return true;
        }
    
        @Override
        public boolean getLinkWithEditor () {
            return false;
        }
    
        @Override
        public CALModelManager getCALModelManager () {
            return CALModelManager.getCALModelManager();
        }
    
    };

    private Composite panel;
    
    private TreeViewer otherFeatures;
    
    private TableViewer relatedFeatures;

    private Button addButton;
    private Button removeButton;

    private Button upButton;
    private Button downButton;

    private final ArrayList<CALFeatureName> relatedFeaturesModel = new ArrayList<CALFeatureName> ();

    /**
     * Constructor FeaturesEditor
     *
     * @param editorSection
     * @param key
     * @param title
     * @param description
     */
    public FeaturesEditor (EditorSection editorSection, String key, String title, String description) {
        super (editorSection, key, title, description);
    }

    /**
     * @see org.openquark.cal.eclipse.ui.metadataeditor.EditorComponent#createEditorComponent(org.eclipse.swt.widgets.Composite, org.eclipse.ui.forms.widgets.FormToolkit)
     */
    @Override
    Control createEditorComponent (Composite parent, FormToolkit formToolkit) {
        panel = formToolkit.createComposite (parent);
        formToolkit.paintBordersFor (panel);
        GridLayoutFactory.swtDefaults ().numColumns (4).applyTo (panel);
        
        Tree otherFeaturesTree = formToolkit.createTree (panel, SWT.H_SCROLL | SWT.V_SCROLL);
        GridDataFactory
            .fillDefaults ()
            .grab (true, true)
            .hint (SWT.DEFAULT, MIN_TREE_HEIGHT)
            .applyTo (otherFeaturesTree);
        
        otherFeatures = new TreeViewer (otherFeaturesTree);
        
        ILabelProvider labelProvider =
            new DecoratingLabelProvider(
                    new ModuleTreeLabelProvider (moduleContentProvider),
                    new ProblemMarkerDecorator());
        ModuleTreeContentProvider treeContentProvider = makeTreeContentProvider(otherFeatures, labelProvider);
        otherFeatures.setContentProvider (treeContentProvider);
        otherFeatures.setLabelProvider (labelProvider);
        otherFeatures.setSorter(new ViewerSorter());
        otherFeatures.setInput (treeContentProvider.getRoot ());
        
        otherFeatures.addSelectionChangedListener (new ISelectionChangedListener () {
            public void selectionChanged (SelectionChangedEvent event) {
                onOtherFeaturesSelectionChanged ();
            }
        });
        
        Composite addRemoveBox = formToolkit.createComposite (panel);
        GridLayoutFactory.swtDefaults ().numColumns (1).applyTo (addRemoveBox);
        
        addButton = createButton (formToolkit, addRemoveBox, "->", MetadataEditorMessages.AddFeatureButtonToolTip); //$NON-NLS-1$
        addButton.addSelectionListener (new SelectionAdapter () {
            @Override
            public void widgetSelected (SelectionEvent e) {
                onAdd ();
            }
        });
        addButton.setEnabled (false);
        
        removeButton = createButton (formToolkit, addRemoveBox, "<-", MetadataEditorMessages.RemoveFeatureButtonToolTip); //$NON-NLS-1$
        removeButton.addSelectionListener (new SelectionAdapter () {
            @Override
            public void widgetSelected (SelectionEvent e) {
                onRemove ();
            }
        });
        removeButton.setEnabled (false);
        
        Table relatedFeaturesTable = formToolkit.createTable (panel, SWT.H_SCROLL | SWT.V_SCROLL);
        GridDataFactory
            .fillDefaults ()
            .grab (true, true)
            .applyTo (relatedFeaturesTable);
        
        relatedFeatures = new TableViewer (relatedFeaturesTable);
        
        relatedFeatures.setContentProvider (new ArrayContentProvider ());
        relatedFeatures.setLabelProvider (calLabelProvider);
        relatedFeatures.setInput (relatedFeaturesModel);
        
        relatedFeatures.addSelectionChangedListener (new ISelectionChangedListener () {
            public void selectionChanged (SelectionChangedEvent event) {
                onRelatedFeaturesSelectionChanged ();
            }
        });
        
        Composite upDownBox = formToolkit.createComposite (panel);
        GridLayoutFactory.swtDefaults ().numColumns (1).applyTo (upDownBox);
        
        upButton = createButton (formToolkit, upDownBox, MetadataEditorMessages.MoveUpItemButtonLabel, MetadataEditorMessages.MoveUpFeatureButtonToolTip);
        upButton.addSelectionListener (new SelectionAdapter () {
            @Override
            public void widgetSelected (SelectionEvent e) {
                onUp ();
            }
        });
        upButton.setEnabled (false);
        
        downButton = createButton (formToolkit, upDownBox, MetadataEditorMessages.MoveDownItemButtonLabel, MetadataEditorMessages.MoveDownFeatureButtonToolTip);
        downButton.addSelectionListener (new SelectionAdapter () {
            @Override
            public void widgetSelected (SelectionEvent e) {
                onDown ();
            }
        });
        downButton.setEnabled (false);
        
        return panel;
    }
    
    /**
     * Method enableButtons
     *
     */
    private void enableButtons () {
        onOtherFeaturesSelectionChanged ();
        onRelatedFeaturesSelectionChanged ();
    }

    /**
     * Method onOtherFeaturesSelectionChanged
     *
     */
    private void onOtherFeaturesSelectionChanged () {
        CALFeatureName selectedOtherFeature = getSelectedOtherFeature ();
        
        boolean enable = selectedOtherFeature != null && !relatedFeaturesModel.contains (selectedOtherFeature);
        
        addButton.setEnabled (enable);
    }

    /**
     * Method onRelatedFeaturesSelectionChanged
     *
     */
    private void onRelatedFeaturesSelectionChanged () {
        removeButton.setEnabled (getSelectedRelatedFeature () != null);
        
        int selectionIndex = relatedFeatures.getTable ().getSelectionIndex ();
        
        upButton.setEnabled (selectionIndex > 0);
        downButton.setEnabled (selectionIndex != -1 && selectionIndex + 1 < relatedFeaturesModel.size ());
    }

    /**
     * Method makeTreeContentProvider
     * 
     * @param treeViewer 
     *
     * @return Returns a ModuleTreeContentProvider
     */
    private ModuleTreeContentProvider makeTreeContentProvider (TreeViewer treeViewer, ILabelProvider labelProvider) {
        return new ModuleTreeContentProvider (moduleContentProvider, labelProvider, treeViewer) {
        
            @Override
            protected void refreshForModule (ModuleSourceDefinition moduleSourceDefinition, boolean updateOnlyModuleName) {
                initialize ();
                getViewer ().refresh(true);
            }
        
            @Override
            protected Object[] getRootElements () {
                if (getInvisibleRoot () == null) {
                    initialize ();
                }

                if (CALBuilder.isEnabled ()) {
                    if (getInvisibleRoot () instanceof Collection) {
                        Collection<Object> collection = UnsafeCast.unsafeCast(getInvisibleRoot());
                        return collection.toArray ();
                    } else if (getInvisibleRoot () instanceof HierarchicalNode) {
                        return ((HierarchicalNode)getInvisibleRoot ()).getChildren ();
                    } else {
                        return new Object[0];
                    }
                } else {
                    // CAL Builder is not enabled
                    setCalBuilderWasEnabled (false);
                    return new Object[] { ActionMessages.error_calBuilderNotEnabled_message };
                }
            }
        
            @Override
            public void fillLocalPullDown (IMenuManager manager) {
            }
        
            @Override
            public void fillContextMenu (IMenuManager manager) {
            }
        
        };
    }

    /**
     * Method onAdd
     *
     */
    private void onAdd () {
        CALFeatureName featureName = getSelectedOtherFeature ();
        
        if (featureName != null) {
            addFeatureName (featureName);
        }
    }

    /**
     * Method getSelectedOtherFeature
     *
     * @return Returns the CALFeatureName selected in the otherFeatures tree, or null
     */
    private CALFeatureName getSelectedOtherFeature () {
        ISelection selection = otherFeatures.getSelection ();
        
        if (selection instanceof IStructuredSelection) {
            IStructuredSelection treeSelection = (IStructuredSelection)selection;
            
            Object firstElement = treeSelection.getFirstElement ();
            
            if (firstElement instanceof ModuleName) {
                ModuleName moduleName = (ModuleName)firstElement;
                
                return CALFeatureName.getModuleFeatureName (moduleName);
            } else if (firstElement instanceof ScopedEntity) {
                ScopedEntity scopedEntity = (ScopedEntity)firstElement;
                
                return CALFeatureName.getScopedEntityFeatureName (scopedEntity);
            }
        }
        
        return null;
    }


    /**
     * Method addFeatureName
     * 
     * @param featureName 
     */
    private void addFeatureName (CALFeatureName featureName) {
        if (!relatedFeaturesModel.contains (featureName)) {
            relatedFeaturesModel.add (featureName);
            relatedFeatures.refresh ();
            
            editorChanged ();
        }
    }

    /**
     * Method onRemove
     * 
     */
    private void onRemove () {
        CALFeatureName selectedRelatedFeature = getSelectedRelatedFeature ();

        if (selectedRelatedFeature != null) {
            relatedFeaturesModel.remove (selectedRelatedFeature);
            relatedFeatures.refresh ();

            editorChanged ();
        }
    }
    
    /**
     * Method onUp
     *
     */
    private void onUp () {
        int selectionIndex = relatedFeatures.getTable ().getSelectionIndex ();
        
        if (selectionIndex > 0) {
            CALFeatureName featureName = relatedFeaturesModel.remove (selectionIndex);
            relatedFeaturesModel.add (selectionIndex - 1, featureName);
            
            relatedFeatures.refresh ();
            
            editorChanged ();
        }
    }

    /**
     * Method onDown
     *
     */
    private void onDown () {
        int selectionIndex = relatedFeatures.getTable ().getSelectionIndex ();
        
        if (selectionIndex != -1 && selectionIndex + 1 < relatedFeaturesModel.size ()) {
            CALFeatureName featureName = relatedFeaturesModel.remove (selectionIndex);
            relatedFeaturesModel.add (selectionIndex + 1, featureName);
            
            relatedFeatures.refresh ();
            
            editorChanged ();
        }
    }

    /**
     * Method getSelectedRelatedFeature
     *
     * @return Returns the {@link CALFeatureName} selected in relatedFeatures
     */
    private CALFeatureName getSelectedRelatedFeature () {
        ISelection selection = relatedFeatures.getSelection ();
        
        if (selection instanceof IStructuredSelection) {
            IStructuredSelection structuredSelection = (IStructuredSelection)selection;
            
            Object firstElement = structuredSelection.getFirstElement ();
            if (firstElement instanceof CALFeatureName) {
                return (CALFeatureName)firstElement;
            }
        }
        
        return null;
    }

    /**
     * Method createButton
     *
     * @param formToolkit
     * @param parent
     * @param caption 
     * @param tooltip 
     * 
     * @return Returns the Button
     */
    private Button createButton (FormToolkit formToolkit, Composite parent, String caption, String tooltip) {
        Button button = formToolkit.createButton (parent, caption, SWT.PUSH);
        button.setToolTipText (tooltip);
        GridDataFactory.fillDefaults ().applyTo (button);
        
        return button;
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
        return Collections.unmodifiableList (relatedFeaturesModel);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setValue (Object value) {
        List<CALFeatureName> featureNames = UnsafeCast.unsafeCast(value);
        
        relatedFeaturesModel.clear ();
        relatedFeaturesModel.addAll (featureNames);
        
        relatedFeatures.refresh ();
        
        enableButtons ();
    }

}
