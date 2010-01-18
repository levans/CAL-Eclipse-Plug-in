/*******************************************************************************
 * Copyright (c) 2007 Business Objects Software Limited and others.
 * All rights reserved. 
 * This file is made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Business Objects Software Limited - initial API and implementation based on Eclipse 3.1.2 code for
 *                             org.eclipse.jdt.internal.ui.text.JavaOutlineInformationControl.java
 *                           Eclipse source is available at: http://www.eclipse.org/downloads/
 *******************************************************************************/

/*
 * CALOutlineInformationControl.java
 * Created: 03-Sept-07
 * By: Greg McClement
 */

package org.openquark.cal.eclipse.ui.views;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.KeyAdapter;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.swt.widgets.Widget;

import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.viewers.AbstractTreeViewer;
import org.eclipse.jface.viewers.DecoratingLabelProvider;
import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.ViewerFilter;

import org.eclipse.ui.keys.KeySequence;
import org.eclipse.ui.keys.SWTKeySupport;

import org.openquark.cal.compiler.ModuleName;
import org.openquark.cal.eclipse.core.CALModelManager;
import org.openquark.cal.eclipse.ui.CALEclipseUIPlugin;
import org.openquark.cal.eclipse.ui.preferences.PreferenceConstants;
import org.openquark.cal.eclipse.ui.util.StringMatcher;

/**
 * Show outline in light-weight control.
 *
 * @since 2.1
 */
 public class CALOutlineInformationControl extends AbstractInformationControl {

    private KeyAdapter fKeyAdapter;
    private QuickOutlineTreeContentProvider fOutlineContentProvider;
    private ILabelProvider fInnerLabelProvider;

    /**
     * Category filter action group.
     * @since 3.2
     */

    private class OutlineTreeViewer extends TreeViewer {
        private OutlineTreeViewer(Tree tree) {
            super(tree);
        }

        /**
         * {@inheritDoc}
         */
        @Override
        protected Object[] getFilteredChildren(Object parent) {            
            if (parent instanceof ModuleName){
                return new Object[0];
            }

            Object[] result = getRawChildren(parent);
            ViewerFilter[] filters = getFilters();
            if (filters != null) {
                for (ViewerFilter element : filters)
                    result = element.filter(this, parent, result);
            }
            return result;
        }

        /**
         * {@inheritDoc}
         */
        @Override
        protected void internalExpandToLevel(Widget node, int level) {
            super.internalExpandToLevel(node, level);
        }
    }

    /**
     * Creates a new Java outline information control.
     *
     * @param parent
     * @param shellStyle
     * @param treeStyle
     * @param commandId
     */
    public CALOutlineInformationControl(Shell parent, int shellStyle, int treeStyle, String commandId) {
        super(parent, shellStyle, treeStyle, commandId, true);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected Text createFilterText(Composite parent) {
        Text text= super.createFilterText(parent);
        text.addKeyListener(getKeyAdapter());
        return text;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected TreeViewer createTreeViewer(Composite parent, int style) {
        Tree tree= new Tree(parent, SWT.SINGLE | (style & ~SWT.MULTI));
        GridData gd= new GridData(GridData.FILL_BOTH);
        gd.heightHint= tree.getItemHeight() * 12;
        tree.setLayoutData(gd);

        final TreeViewer treeViewer= new OutlineTreeViewer(tree);
        // Hard-coded filters
        treeViewer.addFilter(new NamePatternFilter());
        fInnerLabelProvider =
            new DecoratingLabelProvider(                
            new DecoratingLabelProvider(                
            new DecoratingLabelProvider(
                    new ModuleTreeLabelProvider(calModuleContentProvider),
                    new ScopeDecorator()), 
                    new ProblemMarkerDecorator()),
                    new ForeignDecorator());
        
        treeViewer.setLabelProvider(fInnerLabelProvider);

        fOutlineContentProvider= 
            new QuickOutlineTreeContentProvider(
                    calModuleContentProvider,
                    treeViewer,
                    fInnerLabelProvider
                    );
        treeViewer.setContentProvider(fOutlineContentProvider);
        treeViewer.setAutoExpandLevel(AbstractTreeViewer.ALL_LEVELS);


        treeViewer.getTree().addKeyListener(getKeyAdapter());
        return treeViewer;
    }

    public static final CALModuleContentProvider calModuleContentProvider = new CALModuleContentProvider() {
        @Override
        public boolean getShowModuleHierarchy() {
            return false;
        }

        @Override
        public boolean getShowElementHierarchy() {
            return CALEclipseUIPlugin.getDefault().getPreferenceStore().getBoolean(PreferenceConstants.EDITOR_QUICK_OUTLINE_SHOW_ELEMENT_HIERARCHY);
        }
        
        @Override
        public boolean getShowPrivateElements() {
            return CALEclipseUIPlugin.getDefault().getPreferenceStore().getBoolean(PreferenceConstants.EDITOR_QUICK_OUTLINE_SHOW_PRIVATE_SYMBOLS);
        }

        @Override
        public boolean getLinkWithEditor() {
            return false;
        }

        @Override
        public void setShowModuleHierarchy(boolean value) {
        }

        @Override
        public void setShowElementHierarchy(boolean value) {
            CALEclipseUIPlugin.getDefault().getPreferenceStore().setDefault(PreferenceConstants.EDITOR_QUICK_OUTLINE_SHOW_ELEMENT_HIERARCHY, value);
        }
        
        @Override
        public void setShowPrivateElements(boolean value) {
            CALEclipseUIPlugin.getDefault().getPreferenceStore().setDefault(PreferenceConstants.EDITOR_QUICK_OUTLINE_SHOW_PRIVATE_SYMBOLS, value);
        }
        
        @Override
        public void setLinkWithEditor(boolean value) {            
        }
        
        @Override
        public CALModelManager getCALModelManager() {
            return CALModelManager.getCALModelManager();
        }
    };
    
    /**
     * {@inheritDoc}
     */
    @Override
    protected String getStatusFieldText() {
        KeySequence[] sequences= getInvokingCommandKeySequences();
        if (sequences == null || sequences.length == 0)
            return ""; //$NON-NLS-1$

        String keySequence= sequences[0].format();
        return "";
    }

    /*
     * @see org.eclipse.jdt.internal.ui.text.AbstractInformationControl#getId()
     * @since 3.0
     */
    @Override
    protected String getId() {
        return "org.eclipse.jdt.internal.ui.text.QuickOutline"; //$NON-NLS-1$
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setInput(Object information) {
        if (information == null || information instanceof String) {
            inputChanged(null, null);
            return;
        }
        //        inputChanged(fInput, information);
        fOutlineContentProvider.setInput((ModuleName) information);
        inputChanged(fOutlineContentProvider.getRoot(), information);

//        fCategoryFilterActionGroup.setInput(getInputForCategories());
    }

    private KeyAdapter getKeyAdapter() {
        if (fKeyAdapter == null) {
            fKeyAdapter= new KeyAdapter() {
                @Override
                public void keyPressed(KeyEvent e) {
                    int accelerator = SWTKeySupport.convertEventToUnmodifiedAccelerator(e);
                    KeySequence keySequence = KeySequence.getInstance(SWTKeySupport.convertAcceleratorToKeyStroke(accelerator));
                    KeySequence[] sequences= getInvokingCommandKeySequences();
                    if (sequences == null)
                        return;
                    for (KeySequence element : sequences) {
                        if (element.equals(keySequence)) {
                            e.doit= false;
                            return;
                        }
                    }
                }
            };
        }
        return fKeyAdapter;
    }

    /**
     * {@inheritDoc}
     */
     @Override
    protected void handleStatusFieldClicked() {
//         toggleShowInheritedMembers();
     }

     /*
      * @see org.eclipse.jdt.internal.ui.text.AbstractInformationControl#fillViewMenu(org.eclipse.jface.action.IMenuManager)
      */
     @Override
    protected void fillViewMenu(IMenuManager viewMenu) {
         super.fillViewMenu(viewMenu);
         fOutlineContentProvider.fillLocalPullDown(viewMenu);
//         viewMenu.add(fShowOnlyMainTypeAction); 

//         viewMenu.add(new Separator("Sorters")); //$NON-NLS-1$
//         viewMenu.add(fLexicalSortingAction);
//         viewMenu.add(fSortByDefiningTypeAction);

//         fCategoryFilterActionGroup.setInput(getInputForCategories());
//         fCategoryFilterActionGroup.contributeToViewMenu(viewMenu);  
     }

     /*
      * @see org.eclipse.jdt.internal.ui.text.AbstractInformationControl#setMatcherString(java.lang.String, boolean)
      * @since 3.2
      */
     @Override
    protected void setMatcherString(String pattern, boolean update) {
         if (pattern.length() == 0) {
             super.setMatcherString(pattern, update);
             return;
         }

         final boolean ignoreCase= pattern.toLowerCase().equals(pattern);
         fStringMatcher = new StringMatcher(pattern, ignoreCase, false);

         if (update)
             stringMatcherUpdated();
     }
 }
