/*******************************************************************************
 * Copyright (c) 2006 Business Objects Software Limited and others.
 * All rights reserved. 
 * This file is made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Business Objects Software Limited - initial API and implementation based on Eclipse 3.1.2 code for
 *                             /org.eclipse.jdt.ui/ui/org/eclipse/jdt/internal/ui/preferences/JavaEditorColoringConfigurationBlock.java
 *                           Eclipse source is available at: http://www.eclipse.org/downloads/
 *******************************************************************************/

/*
 * CALEditorColoringConfigurationBlock.java
 * Creation date: Feb 14, 2006.
 * By: Edward Lam
 */
package org.openquark.cal.eclipse.ui.preferences;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;

import org.eclipse.core.runtime.Preferences;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.FontMetrics;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Link;
import org.eclipse.swt.widgets.ScrollBar;
import org.eclipse.swt.widgets.Scrollable;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.preference.ColorSelector;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.preference.PreferenceConverter;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.StructuredViewer;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerSorter;

import org.eclipse.jface.text.Document;
import org.eclipse.jface.text.IDocument;

import org.eclipse.ui.dialogs.PreferencesUtil;
import org.eclipse.ui.texteditor.ChainedPreferenceStore;

import org.eclipse.ui.editors.text.EditorsUI;
import org.openquark.cal.eclipse.core.CoreOptionIDs;
import org.openquark.cal.eclipse.ui.CALEclipseUIPlugin;
import org.openquark.cal.eclipse.ui.caleditor.CALSourceViewer;
import org.openquark.cal.eclipse.ui.preferences.OverlayPreferenceStore.OverlayKey;
import org.openquark.cal.eclipse.ui.text.CALPartitions;
import org.openquark.cal.eclipse.ui.text.ColorManager;
import org.openquark.cal.eclipse.ui.text.PreferencesAdapter;
import org.openquark.cal.eclipse.ui.text.SimpleCALSourceViewerConfiguration;
import org.openquark.cal.eclipse.ui.util.PixelConverter;



/**
 * Configures CAL Editor syntax coloring preferences.
 * 
 * @author Edward Lam
 */
class CALEditorColoringConfigurationBlock extends AbstractConfigurationBlock {
    
    /**
     * Item in the highlighting color list.
     */
    private static class HighlightingColorListItem {
        /** Display name */
        private final String fDisplayName;
        /** Color preference key */
        private final String fColorKey;
        /** Bold preference key */
        private final String fBoldKey;
        /** Italic preference key */
        private final String fItalicKey;
        /**
         * Strikethrough preference key.
         */
        private final String fStrikethroughKey;
        /**
         * Underline preference key.
         */
        private final String fUnderlineKey;
        
        /**
         * Initialize the item with the given values.
         * @param displayName the display name
         * @param colorKey the color preference key
         * @param boldKey the bold preference key
         * @param italicKey the italic preference key
         * @param strikethroughKey the strikethrough preference key
         * @param underlineKey the underline preference key
         */
        public HighlightingColorListItem(String displayName, String colorKey, String boldKey, String italicKey, String strikethroughKey, String underlineKey) {
            fDisplayName = displayName;
            fColorKey = colorKey;
            fBoldKey = boldKey;
            fItalicKey = italicKey;
            fStrikethroughKey = strikethroughKey;
            fUnderlineKey = underlineKey;
        }
        
        /**
         * @return the bold preference key
         */
        public String getBoldKey() {
            return fBoldKey;
        }
        
        /**
         * @return the bold preference key
         */
        public String getItalicKey() {
            return fItalicKey;
        }
        
        /**
         * @return the strikethrough preference key
         */
        public String getStrikethroughKey() {
            return fStrikethroughKey;
        }
        
        /**
         * @return the underline preference key
         */
        public String getUnderlineKey() {
            return fUnderlineKey;
        }
        
        /**
         * @return the color preference key
         */
        public String getColorKey() {
            return fColorKey;
        }
        
        /**
         * @return the display name
         */
        public String getDisplayName() {
            return fDisplayName;
        }
    }
    
    private static class SemanticHighlightingColorListItem extends HighlightingColorListItem {
        
        /** Enablement preference key */
        private final String fEnableKey;
        
        /**
         * Initialize the item with the given values.
         * @param displayName the display name
         * @param colorKey the color preference key
         * @param boldKey the bold preference key
         * @param italicKey the italic preference key
         * @param strikethroughKey the strikethroughKey preference key
         * @param underlineKey the underlineKey preference key
         * @param enableKey the enable preference key
         */
        public SemanticHighlightingColorListItem(String displayName, String colorKey, String boldKey, String italicKey, String strikethroughKey, String underlineKey, String enableKey) {
            super(displayName, colorKey, boldKey, italicKey, strikethroughKey, underlineKey);
            fEnableKey = enableKey;
        }
        
        /**
         * @return the enablement preference key
         */
        public String getEnableKey() {
            return fEnableKey;
        }
    }
    
    /**
     * Color list label provider.
     */
    private static class ColorListLabelProvider extends LabelProvider {
        /*
         * @see org.eclipse.jface.viewers.ILabelProvider#getText(java.lang.Object)
         */
        @Override
        public String getText(Object element) {
            if (element instanceof String) {
                return (String) element;
            }
            return ((HighlightingColorListItem)element).getDisplayName();
        }
    }
    
    /**
     * Color list content provider.
     */
    private class ColorListContentProvider implements ITreeContentProvider {
        
        /*
         * @see org.eclipse.jface.viewers.IStructuredContentProvider#getElements(java.lang.Object)
         */
        public Object[] getElements(Object inputElement) {
            return new String[] {fJavaCategory, fJavadocCategory, fCommentsCategory};
        }
        
        /*
         * @see org.eclipse.jface.viewers.IContentProvider#dispose()
         */
        public void dispose() {
        }
        
        /*
         * @see org.eclipse.jface.viewers.IContentProvider#inputChanged(org.eclipse.jface.viewers.Viewer, java.lang.Object, java.lang.Object)
         */
        public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
        }
        
        public Object[] getChildren(Object parentElement) {
            if (parentElement instanceof String) {
                String entry = (String)parentElement;
                if (fJavaCategory.equals(entry)) {
                    return fListModel.subList(7, fListModel.size()).toArray();
                }
                if (fJavadocCategory.equals(entry)) {
                    return fListModel.subList(1, 3).toArray();
                }
                if (fCommentsCategory.equals(entry)) {
                    return fListModel.subList(4, 7).toArray();
                }
            }
            return new Object[0];
        }
        
        public Object getParent(Object element) {
            if (element instanceof String) {
                return null;
            }
            int index = fListModel.indexOf(element);
            if (index < 4) {
                return fJavadocCategory;
            }
            if (index >= 7) {
                return fJavaCategory;
            }
            return fCommentsCategory;
        }
        
        public boolean hasChildren(Object element) {
            return element instanceof String;
        }
    }
    
    private static final String BOLD = PreferenceConstants.EDITOR_BOLD_SUFFIX;
    /**
     * Preference key suffix for italic preferences.
     */
    private static final String ITALIC = PreferenceConstants.EDITOR_ITALIC_SUFFIX;
    /**
     * Preference key suffix for strikethrough preferences.
     */
    private static final String STRIKETHROUGH = PreferenceConstants.EDITOR_STRIKETHROUGH_SUFFIX;
    /**
     * Preference key suffix for underline preferences.
     */
    private static final String UNDERLINE = PreferenceConstants.EDITOR_UNDERLINE_SUFFIX;

    private static final String COMPILER_TASK_TAGS = CoreOptionIDs.COMPILER_TASK_TAGS;
    /**
     * The keys of the overlay store. 
     */
    private final String[][] fSyntaxColorListModel= new String[][] {
            { PreferencesMessages.JavaEditorPreferencePage_javaDocKeywords, PreferenceConstants.EDITOR_CALDOC_KEYWORD_COLOR }, 
            { PreferencesMessages.JavaEditorPreferencePage_javaDocHtmlTags, PreferenceConstants.EDITOR_CALDOC_TAG_COLOR }, 
            { PreferencesMessages.JavaEditorPreferencePage_javaDocOthers, PreferenceConstants.EDITOR_CALDOC_DEFAULT_COLOR }, 
            { PreferencesMessages.JavaEditorPreferencePage_javaDocLinks, PreferenceConstants.EDITOR_CALDOC_LINKS_COLOR }, 
            { PreferencesMessages.JavaEditorPreferencePage_multiLineComment, PreferenceConstants.EDITOR_MULTI_LINE_COMMENT_COLOR }, 
            { PreferencesMessages.JavaEditorPreferencePage_singleLineComment, PreferenceConstants.EDITOR_SINGLE_LINE_COMMENT_COLOR }, 
            { PreferencesMessages.JavaEditorPreferencePage_javaCommentTaskTags, PreferenceConstants.EDITOR_TASK_TAG_COLOR }, 
            { PreferencesMessages.JavaEditorPreferencePage_keywords, PreferenceConstants.EDITOR_CAL_KEYWORD_COLOR }, 
            { PreferencesMessages.JavaEditorPreferencePage_keywords2, PreferenceConstants.EDITOR_CAL_KEYWORD_RETURN_COLOR }, 
            { PreferencesMessages.JavaEditorPreferencePage_operators, PreferenceConstants.EDITOR_CAL_OPERATOR_COLOR }, 
            { PreferencesMessages.JavaEditorPreferencePage_consNames, PreferenceConstants.EDITOR_CAL_ANNOTATION_COLOR }, 
            { PreferencesMessages.JavaEditorPreferencePage_literals, PreferenceConstants.EDITOR_STRING_COLOR }, 
            { PreferencesMessages.JavaEditorPreferencePage_others, PreferenceConstants.EDITOR_CAL_DEFAULT_COLOR }, 
    };
    
    private final String fJavaCategory = PreferencesMessages.JavaEditorPreferencePage_coloring_category_java;
    private final String fJavadocCategory = PreferencesMessages.JavaEditorPreferencePage_coloring_category_javadoc;
    private final String fCommentsCategory = PreferencesMessages.JavaEditorPreferencePage_coloring_category_comments; 
    
    private ColorSelector fSyntaxForegroundColorEditor;
    private Label fColorEditorLabel;
    private Button fBoldCheckBox;
    private Button fEnableCheckbox;
    /**
     * Check box for italic preference.
     */
    private Button fItalicCheckBox;
    /**
     * Check box for strikethrough preference.
     */
    private Button fStrikethroughCheckBox;
    /**
     * Check box for underline preference.
     */
    private Button fUnderlineCheckBox;
    /**
     * Highlighting color list
     */
    private final java.util.List<HighlightingColorListItem> fListModel= new ArrayList<HighlightingColorListItem>();
    /**
     * Highlighting color list viewer
     */
    private StructuredViewer fListViewer;
//    /**
//     * Semantic highlighting manager
//     */
//    private SemanticHighlightingManager fSemanticHighlightingManager;
    /**
     * The previewer.
     */
    private CALSourceViewer fPreviewViewer;
    /**
     * The color manager.
     */
    private final ColorManager fColorManager;
    /**
     * The font metrics.
     */
    private FontMetrics fFontMetrics;
    
    public CALEditorColoringConfigurationBlock(OverlayPreferenceStore store) {
        super(store);
        
        fColorManager = new ColorManager(false);
        
        for (final String[] listModelElement : fSyntaxColorListModel) {
            fListModel.add(new HighlightingColorListItem (listModelElement[0], listModelElement[1], listModelElement[1] + BOLD, listModelElement[1] + ITALIC, listModelElement[1] + STRIKETHROUGH, listModelElement[1] + UNDERLINE));
        }
        
//        SemanticHighlighting[] semanticHighlightings= SemanticHighlightings.getSemanticHighlightings();
//        for (int i= 0, n= semanticHighlightings.length; i < n; i++)
//            fListModel.add(
//                    new SemanticHighlightingColorListItem(
//                            semanticHighlightings[i].getDisplayName(), 
//                            SemanticHighlightings.getColorPreferenceKey(semanticHighlightings[i]),
//                            SemanticHighlightings.getBoldPreferenceKey(semanticHighlightings[i]),
//                            SemanticHighlightings.getItalicPreferenceKey(semanticHighlightings[i]),
//                            SemanticHighlightings.getStrikethroughPreferenceKey(semanticHighlightings[i]),
//                            SemanticHighlightings.getUnderlinePreferenceKey(semanticHighlightings[i]),
//                            SemanticHighlightings.getEnabledPreferenceKey(semanticHighlightings[i])
//                    ));
        
        store.addKeys(createOverlayStoreKeys());
    }
    
    private OverlayPreferenceStore.OverlayKey[] createOverlayStoreKeys() {
        
        ArrayList<OverlayKey> overlayKeys= new ArrayList<OverlayKey>();
        
        for (int i = 0, n = fListModel.size(); i < n; i++) {
            HighlightingColorListItem item = fListModel.get(i);
            overlayKeys.add(new OverlayPreferenceStore.OverlayKey(OverlayPreferenceStore.STRING, item.getColorKey()));
            overlayKeys.add(new OverlayPreferenceStore.OverlayKey(OverlayPreferenceStore.BOOLEAN, item.getBoldKey()));
            overlayKeys.add(new OverlayPreferenceStore.OverlayKey(OverlayPreferenceStore.BOOLEAN, item.getItalicKey()));
            overlayKeys.add(new OverlayPreferenceStore.OverlayKey(OverlayPreferenceStore.BOOLEAN, item.getStrikethroughKey()));
            overlayKeys.add(new OverlayPreferenceStore.OverlayKey(OverlayPreferenceStore.BOOLEAN, item.getUnderlineKey()));

            if (item instanceof SemanticHighlightingColorListItem) {
                overlayKeys.add(new OverlayPreferenceStore.OverlayKey(OverlayPreferenceStore.BOOLEAN, ((SemanticHighlightingColorListItem)item).getEnableKey()));
            }
        }
        
        OverlayPreferenceStore.OverlayKey[] keys = new OverlayPreferenceStore.OverlayKey[overlayKeys.size()];
        overlayKeys.toArray(keys);
        return keys;
    }
    
    /**
     * Creates page for hover preferences.
     * 
     * @param parent the parent composite
     * @return the control for the preference page
     */
    public Control createControl(Composite parent) {
        initializeDialogUnits(parent);
        return createSyntaxPage(parent);
    }
    
    /**
     * Returns the number of pixels corresponding to the width of the given
     * number of characters.
     * <p>
     * This method may only be called after <code>initializeDialogUnits</code>
     * has been called.
     * </p>
     * <p>
     * Clients may call this framework method, but should not override it.
     * </p>
     * 
     * @param chars
     *            the number of characters
     * @return the number of pixels
     */
    private int convertWidthInCharsToPixels(int chars) {
        // test for failure to initialize for backward compatibility
        if (fFontMetrics == null) {
            return 0;
        }
        return Dialog.convertWidthInCharsToPixels(fFontMetrics, chars);
    }
    
    /**
     * Returns the number of pixels corresponding to the height of the given
     * number of characters.
     * <p>
     * This method may only be called after <code>initializeDialogUnits</code>
     * has been called.
     * </p>
     * <p>
     * Clients may call this framework method, but should not override it.
     * </p>
     * 
     * @param chars
     *            the number of characters
     * @return the number of pixels
     */
    private int convertHeightInCharsToPixels(int chars) {
        // test for failure to initialize for backward compatibility
        if (fFontMetrics == null) {
            return 0;
        }
        return Dialog.convertHeightInCharsToPixels(fFontMetrics, chars);
    }
    
    @Override
    public void initialize() {
        super.initialize();
        
        fListViewer.setInput(fListModel);
        fListViewer.setSelection(new StructuredSelection(fJavaCategory));
    }
    
    @Override
    public void performDefaults() {
        super.performDefaults();
        
        handleSyntaxColorListSelection();
        
//        uninstallSemanticHighlighting();
//        installSemanticHighlighting();
        
        fPreviewViewer.invalidateTextPresentation();
    }
    
    /*
     * @see org.eclipse.jdt.internal.ui.preferences.IPreferenceConfigurationBlock#dispose()
     */
    @Override
    public void dispose() {
//        uninstallSemanticHighlighting();
        fColorManager.dispose();
        
        super.dispose();
    }
    
    private void handleSyntaxColorListSelection() {
        HighlightingColorListItem item = getHighlightingColorListItem();
        if (item == null) {
            fEnableCheckbox.setEnabled(false);
            fSyntaxForegroundColorEditor.getButton().setEnabled(false);
            fColorEditorLabel.setEnabled(false);
            fBoldCheckBox.setEnabled(false);
            fItalicCheckBox.setEnabled(false);
            fStrikethroughCheckBox.setEnabled(false);
            fUnderlineCheckBox.setEnabled(false);
            return;
        }
        RGB rgb = PreferenceConverter.getColor(getPreferenceStore(), item.getColorKey());
        fSyntaxForegroundColorEditor.setColorValue(rgb);
        fBoldCheckBox.setSelection(getPreferenceStore().getBoolean(item.getBoldKey()));
        fItalicCheckBox.setSelection(getPreferenceStore().getBoolean(item.getItalicKey()));
        fStrikethroughCheckBox.setSelection(getPreferenceStore().getBoolean(item.getStrikethroughKey()));
        fUnderlineCheckBox.setSelection(getPreferenceStore().getBoolean(item.getUnderlineKey()));
        if (item instanceof SemanticHighlightingColorListItem) {
            fEnableCheckbox.setEnabled(true);
            boolean enable = getPreferenceStore().getBoolean(((SemanticHighlightingColorListItem)item).getEnableKey());
            fEnableCheckbox.setSelection(enable);
            fSyntaxForegroundColorEditor.getButton().setEnabled(enable);
            fColorEditorLabel.setEnabled(enable);
            fBoldCheckBox.setEnabled(enable);
            fItalicCheckBox.setEnabled(enable);
            fStrikethroughCheckBox.setEnabled(enable);
            fUnderlineCheckBox.setEnabled(enable);
        } else {
            fSyntaxForegroundColorEditor.getButton().setEnabled(true);
            fColorEditorLabel.setEnabled(true);
            fBoldCheckBox.setEnabled(true);
            fItalicCheckBox.setEnabled(true);
            fStrikethroughCheckBox.setEnabled(true);
            fUnderlineCheckBox.setEnabled(true);
            fEnableCheckbox.setEnabled(false);
            fEnableCheckbox.setSelection(true);
        }
    }
    
    private Control createSyntaxPage(final Composite parent) {
        
        Composite colorComposite = new Composite(parent, SWT.NONE);
        GridLayout layout = new GridLayout();
        layout.marginHeight = 0;
        layout.marginWidth = 0;
        colorComposite.setLayout(layout);

        Link link = new Link(colorComposite, SWT.NONE);
        link.setText(PreferencesMessages.JavaEditorColoringConfigurationBlock_link);
        link.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                PreferencesUtil.createPreferenceDialogOn(parent.getShell(), e.text, null, null); 
            }
        });
        // TODO replace by link-specific tooltips when
        // bug https://bugs.eclipse.org/bugs/show_bug.cgi?id=88866 gets fixed
//      link.setToolTipText(PreferencesMessages.JavaEditorColoringConfigurationBlock_link_tooltip); 
        
        GridData gridData = new GridData(SWT.FILL, SWT.BEGINNING, true, false);
        gridData.widthHint = 150; // only expand further if anyone else requires it
        gridData.horizontalSpan = 2;
        link.setLayoutData(gridData);

        addFiller(colorComposite, 1);

        Label label;
        label = new Label(colorComposite, SWT.LEFT);
        label.setText(PreferencesMessages.JavaEditorPreferencePage_coloring_element);
        label.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

        Composite editorComposite = new Composite(colorComposite, SWT.NONE);
        layout = new GridLayout();
        layout.numColumns = 2;
        layout.marginHeight = 0;
        layout.marginWidth = 0;
        editorComposite.setLayout(layout);
        GridData gd = new GridData(SWT.FILL, SWT.BEGINNING, true, false);
        editorComposite.setLayoutData(gd);

        fListViewer = new TreeViewer(editorComposite, SWT.SINGLE | SWT.BORDER);
        fListViewer.setLabelProvider(new ColorListLabelProvider());
        fListViewer.setContentProvider(new ColorListContentProvider());
        fListViewer.setSorter(new ViewerSorter() {
            @Override
            public int category(Object element) {
                // don't sort the top level categories
                if (fJavaCategory.equals(element)) {
                    return 0;
                }
                if (fJavadocCategory.equals(element)) {
                    return 1;
                }
                if (fCommentsCategory.equals(element)) {
                    return 2;
                }
                // to sort semantic settings after partition based ones:
//              if (element instanceof SemanticHighlightingColorListItem)
//              return 1;
                return 0;
            }
        });
        gd = new GridData(SWT.BEGINNING, SWT.BEGINNING, false, true);
        gd.heightHint = convertHeightInCharsToPixels(9);
        int maxWidth = 0;
        for (final HighlightingColorListItem item : fListModel) {
            maxWidth = Math.max(maxWidth, convertWidthInCharsToPixels(item.getDisplayName().length()));
        }
        ScrollBar vBar = ((Scrollable)fListViewer.getControl()).getVerticalBar();
        if (vBar != null) {
            maxWidth += vBar.getSize().x * 3; // scrollbars and tree indentation guess
        }
        gd.widthHint = maxWidth;

        fListViewer.getControl().setLayoutData(gd);

        Composite stylesComposite = new Composite(editorComposite, SWT.NONE);
        layout = new GridLayout();
        layout.marginHeight = 0;
        layout.marginWidth = 0;
        layout.numColumns = 2;
        stylesComposite.setLayout(layout);
        stylesComposite.setLayoutData(new GridData(GridData.FILL_BOTH));

        fEnableCheckbox = new Button(stylesComposite, SWT.CHECK);
        fEnableCheckbox.setText(PreferencesMessages.JavaEditorPreferencePage_enable);
        gd = new GridData(GridData.FILL_HORIZONTAL);
        gd.horizontalAlignment = GridData.BEGINNING;
        gd.horizontalSpan = 2;
        fEnableCheckbox.setLayoutData(gd);

        fColorEditorLabel = new Label(stylesComposite, SWT.LEFT);
        fColorEditorLabel.setText(PreferencesMessages.JavaEditorPreferencePage_color);
        gd = new GridData(GridData.HORIZONTAL_ALIGN_BEGINNING);
        gd.horizontalIndent = 20;
        fColorEditorLabel.setLayoutData(gd);

        fSyntaxForegroundColorEditor = new ColorSelector(stylesComposite);
        Button foregroundColorButton = fSyntaxForegroundColorEditor.getButton();
        gd = new GridData(GridData.HORIZONTAL_ALIGN_BEGINNING);
        foregroundColorButton.setLayoutData(gd);

        fBoldCheckBox = new Button(stylesComposite, SWT.CHECK);
        fBoldCheckBox.setText(PreferencesMessages.JavaEditorPreferencePage_bold);
        gd = new GridData(GridData.HORIZONTAL_ALIGN_BEGINNING);
        gd.horizontalIndent = 20;
        gd.horizontalSpan = 2;
        fBoldCheckBox.setLayoutData(gd);

        fItalicCheckBox = new Button(stylesComposite, SWT.CHECK);
        fItalicCheckBox.setText(PreferencesMessages.JavaEditorPreferencePage_italic);
        gd = new GridData(GridData.HORIZONTAL_ALIGN_BEGINNING);
        gd.horizontalIndent = 20;
        gd.horizontalSpan = 2;
        fItalicCheckBox.setLayoutData(gd);

        fStrikethroughCheckBox = new Button(stylesComposite, SWT.CHECK);
        fStrikethroughCheckBox.setText(PreferencesMessages.JavaEditorPreferencePage_strikethrough);
        gd = new GridData(GridData.HORIZONTAL_ALIGN_BEGINNING);
        gd.horizontalIndent = 20;
        gd.horizontalSpan = 2;
        fStrikethroughCheckBox.setLayoutData(gd);
        
        fUnderlineCheckBox = new Button(stylesComposite, SWT.CHECK);
        fUnderlineCheckBox.setText(PreferencesMessages.JavaEditorPreferencePage_underline);
        gd = new GridData(GridData.HORIZONTAL_ALIGN_BEGINNING);
        gd.horizontalIndent = 20;
        gd.horizontalSpan = 2;
        fUnderlineCheckBox.setLayoutData(gd);

        label = new Label(colorComposite, SWT.LEFT);
        label.setText(PreferencesMessages.JavaEditorPreferencePage_preview);
        label.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

        Control previewer = createPreviewer(colorComposite);
        gd = new GridData(GridData.FILL_BOTH);
        gd.widthHint = convertWidthInCharsToPixels(20);
        gd.heightHint = convertHeightInCharsToPixels(5);
        previewer.setLayoutData(gd);
        
        fListViewer.addSelectionChangedListener(new ISelectionChangedListener() {
            public void selectionChanged(SelectionChangedEvent event) {
                handleSyntaxColorListSelection();
            }
        });
        
        foregroundColorButton.addSelectionListener(new SelectionListener() {
            public void widgetDefaultSelected(SelectionEvent e) {
                // do nothing
            }
            public void widgetSelected(SelectionEvent e) {
                HighlightingColorListItem item= getHighlightingColorListItem();
                PreferenceConverter.setValue(getPreferenceStore(), item.getColorKey(), fSyntaxForegroundColorEditor.getColorValue());
            }
        });
        
        fBoldCheckBox.addSelectionListener(new SelectionListener() {
            public void widgetDefaultSelected(SelectionEvent e) {
                // do nothing
            }
            public void widgetSelected(SelectionEvent e) {
                HighlightingColorListItem item= getHighlightingColorListItem();
                getPreferenceStore().setValue(item.getBoldKey(), fBoldCheckBox.getSelection());
            }
        });
        
        fItalicCheckBox.addSelectionListener(new SelectionListener() {
            public void widgetDefaultSelected(SelectionEvent e) {
                // do nothing
            }
            public void widgetSelected(SelectionEvent e) {
                HighlightingColorListItem item= getHighlightingColorListItem();
                getPreferenceStore().setValue(item.getItalicKey(), fItalicCheckBox.getSelection());
            }
        });
        fStrikethroughCheckBox.addSelectionListener(new SelectionListener() {
            public void widgetDefaultSelected(SelectionEvent e) {
                // do nothing
            }
            public void widgetSelected(SelectionEvent e) {
                HighlightingColorListItem item= getHighlightingColorListItem();
                getPreferenceStore().setValue(item.getStrikethroughKey(), fStrikethroughCheckBox.getSelection());
            }
        });
        
        fUnderlineCheckBox.addSelectionListener(new SelectionListener() {
            public void widgetDefaultSelected(SelectionEvent e) {
                // do nothing
            }
            public void widgetSelected(SelectionEvent e) {
                HighlightingColorListItem item= getHighlightingColorListItem();
                getPreferenceStore().setValue(item.getUnderlineKey(), fUnderlineCheckBox.getSelection());
            }
        });
        
        fEnableCheckbox.addSelectionListener(new SelectionListener() {
            public void widgetDefaultSelected(SelectionEvent e) {
                // do nothing
            }
            public void widgetSelected(SelectionEvent e) {
                HighlightingColorListItem item= getHighlightingColorListItem();
                if (item instanceof SemanticHighlightingColorListItem) {
                    boolean enable= fEnableCheckbox.getSelection();
                    getPreferenceStore().setValue(((SemanticHighlightingColorListItem)item).getEnableKey(), enable);
                    fEnableCheckbox.setSelection(enable);
                    fSyntaxForegroundColorEditor.getButton().setEnabled(enable);
                    fColorEditorLabel.setEnabled(enable);
                    fBoldCheckBox.setEnabled(enable);
                    fItalicCheckBox.setEnabled(enable);
                    fStrikethroughCheckBox.setEnabled(enable);
                    fUnderlineCheckBox.setEnabled(enable);
//                    uninstallSemanticHighlighting();
//                    installSemanticHighlighting();
                }
            }
        });
        
        colorComposite.layout(false);
        
        return colorComposite;
    }
    
    private void addFiller(Composite composite, int horizontalSpan) {
        PixelConverter pixelConverter = new PixelConverter(composite);
        Label filler = new Label(composite, SWT.LEFT);
        GridData gd = new GridData(GridData.HORIZONTAL_ALIGN_FILL);
        gd.horizontalSpan = horizontalSpan;
        gd.heightHint = pixelConverter.convertHeightInCharsToPixels(1) / 2;
        filler.setLayoutData(gd);
    }
    
    private Control createPreviewer(Composite parent) {
        
        IPreferenceStore generalTextStore = EditorsUI.getPreferenceStore();
        IPreferenceStore store = new ChainedPreferenceStore(new IPreferenceStore[]{getPreferenceStore(), new PreferencesAdapter(createTemporaryCorePreferenceStore()), generalTextStore});
        fPreviewViewer = new CALSourceViewer(parent, null, null, false, SWT.V_SCROLL | SWT.H_SCROLL | SWT.BORDER, store);
        SimpleCALSourceViewerConfiguration configuration = new SimpleCALSourceViewerConfiguration(fColorManager, store, null, CALPartitions.CAL_PARTITIONING, false);
        fPreviewViewer.configure(configuration);
        // fake 1.5 source to get 1.5 features right.
        configuration.handlePropertyChangeEvent(new PropertyChangeEvent(this, CoreOptionIDs.COMPILER_SOURCE, CoreOptionIDs.VERSION_1_4, CoreOptionIDs.VERSION_1_5));
        Font font = JFaceResources.getFont(PreferenceConstants.EDITOR_TEXT_FONT);
        fPreviewViewer.getTextWidget().setFont(font);
        CALSourcePreviewerUpdater.configureViewer(fPreviewViewer, configuration, store);
        fPreviewViewer.setEditable(false);

        String content = loadPreviewContentFromFile("ColorSettingPreviewCode.txt"); //$NON-NLS-1$
        IDocument document = new Document(content);
        CALEclipseUIPlugin.getDefault().getCALTextTools().setupCALDocumentPartitioner(document, CALPartitions.CAL_PARTITIONING);
        fPreviewViewer.setDocument(document);
        
//        installSemanticHighlighting();
        
        return fPreviewViewer.getControl();
    }
    
    
    private Preferences createTemporaryCorePreferenceStore() {
        Preferences result = new Preferences();
        
        result.setValue(COMPILER_TASK_TAGS, "TASK,TODO"); //$NON-NLS-1$
        
        return result;
    }
    
    
    private String loadPreviewContentFromFile(String filename) {
        String line;
        String separator = System.getProperty("line.separator"); //$NON-NLS-1$
        StringBuilder buffer = new StringBuilder(512);
        BufferedReader reader = null;
        try {
            reader = new BufferedReader(new InputStreamReader(getClass().getResourceAsStream(filename)));
            while ((line = reader.readLine()) != null) {
                buffer.append(line);
                buffer.append(separator);
            }
        } catch (IOException io) {
            CALEclipseUIPlugin.log(io);
        } finally {
            if (reader != null) {
                try {
                    reader.close();
                } catch (IOException e) {
                }
            }
        }
        return buffer.toString();
    }
    
    
//    /**
//     * Install Semantic Highlighting on the previewer
//     * 
//     */
//    private void installSemanticHighlighting() {
//        if (fSemanticHighlightingManager == null) {
//            fSemanticHighlightingManager= new SemanticHighlightingManager();
//            fSemanticHighlightingManager.install(fPreviewViewer, fColorManager, getPreferenceStore(), createPreviewerRanges());
//        }
//    }
//    
//    
//    /**
//     * Uninstall Semantic Highlighting from the previewer
//     * 
//     */
//    private void uninstallSemanticHighlighting() {
//        if (fSemanticHighlightingManager != null) {
//            fSemanticHighlightingManager.uninstall();
//            fSemanticHighlightingManager= null;
//        }
//    }
//    
//    
//    /**
//     * Create the hard coded previewer ranges
//     * 
//     * @return the hard coded previewer ranges
//     */
//    private SemanticHighlightingManager.HighlightedRange[][] createPreviewerRanges() {
//        return new SemanticHighlightingManager.HighlightedRange[][] {
//                { createHighlightedRange( 6, 13,  9, SemanticHighlightings.DEPRECATED_MEMBER) },
//                { createHighlightedRange( 6, 23,  1, SemanticHighlightings.TYPE_VARIABLE) },
//                { createHighlightedRange( 7, 26,  8, SemanticHighlightings.STATIC_FINAL_FIELD), createHighlightedRange(7, 26, 8, SemanticHighlightings.STATIC_FIELD), createHighlightedRange(7, 26, 8, SemanticHighlightings.FIELD) },
//                { createHighlightedRange( 9, 20, 11, SemanticHighlightings.STATIC_FIELD), createHighlightedRange(9, 20, 11, SemanticHighlightings.FIELD) },
//                { createHighlightedRange(11,  9,  1, SemanticHighlightings.TYPE_VARIABLE) },
//                { createHighlightedRange(11, 11,  5, SemanticHighlightings.FIELD) },
//                { createHighlightedRange(13, 19,  5, SemanticHighlightings.ANNOTATION_ELEMENT_REFERENCE) },
//                { createHighlightedRange(14, 12,  3, SemanticHighlightings.METHOD_DECLARATION), createHighlightedRange(14, 12,  3, SemanticHighlightings.METHOD) },
//                { createHighlightedRange(14, 24,  9, SemanticHighlightings.PARAMETER_VARIABLE) },
//                { createHighlightedRange(15,  2, 14, SemanticHighlightings.ABSTRACT_METHOD_INVOCATION), createHighlightedRange(15,  2, 14, SemanticHighlightings.METHOD) },
//                { createHighlightedRange(16,  6,  5, SemanticHighlightings.LOCAL_VARIABLE_DECLARATION) },
//                { createHighlightedRange(16, 16,  8, SemanticHighlightings.INHERITED_METHOD_INVOCATION), createHighlightedRange(16, 16,  8, SemanticHighlightings.METHOD) },
//                { createHighlightedRange(17,  2, 12, SemanticHighlightings.STATIC_METHOD_INVOCATION), createHighlightedRange(17,  2, 12, SemanticHighlightings.METHOD) },
//                { createHighlightedRange(18, 9,  3, SemanticHighlightings.METHOD) },
//                { createHighlightedRange(18, 13,  5, SemanticHighlightings.LOCAL_VARIABLE) },
//                { createHighlightedRange(18, 22,  9, SemanticHighlightings.AUTOBOXING) },
//        };
//    }
//    
//    
//    /**
//     * Create a highlighted range on the previewers document with the given line, column, length and key.
//     * 
//     * @param line the line
//     * @param column the column
//     * @param length the length
//     * @param key the key
//     * @return the highlighted range
//     */
//    private HighlightedRange createHighlightedRange(int line, int column, int length, String key) {
//        try {
//            IDocument document= fPreviewViewer.getDocument();
//            int offset= document.getLineOffset(line) + column;
//            return new HighlightedRange(offset, length, key);
//        } catch (BadLocationException x) {
//            CALEclipseUIPlugin.log(x);
//        }
//        return null;
//    }
    
    
    /**
     * Returns the current highlighting color list item.
     * 
     * @return the current highlighting color list item
     */
    private HighlightingColorListItem getHighlightingColorListItem() {
        IStructuredSelection selection = (IStructuredSelection)fListViewer.getSelection();
        Object element = selection.getFirstElement();
        if (element instanceof String) {
            return null;
        }
        return (HighlightingColorListItem)element;
    }
    
    /**
     * Initializes the computation of horizontal and vertical dialog units based
     * on the size of current font.
     * <p>
     * This method must be called before any of the dialog unit based conversion
     * methods are called.
     * </p>
     * 
     * @param testControl
     *            a control from which to obtain the current font
     */
    private void initializeDialogUnits(Control testControl) {
        // Compute and store a font metric
        GC gc = new GC(testControl);
        gc.setFont(JFaceResources.getDialogFont());
        fFontMetrics = gc.getFontMetrics();
        gc.dispose();
    }
}
