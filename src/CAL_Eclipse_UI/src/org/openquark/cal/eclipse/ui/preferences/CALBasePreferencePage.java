/*******************************************************************************
 * Copyright (c) 2006 Business Objects Software Limited and others.
 * All rights reserved. 
 * This file is made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Business Objects Software Limited - initial API and implementation based on Eclipse 3.1.2 code for
 *                             /org.eclipse.jdt.ui/ui/org/eclipse/jdt/internal/ui/preferences/JavaBasePreferencePage.java
 *                           Eclipse source is available at: http://www.eclipse.org/downloads/
 *******************************************************************************/

/*
 * CALBasePreferencePage.java
 * Creation date: Feb 16, 2006.
 * By: Edward Lam
 */
package org.openquark.cal.eclipse.ui.preferences;

import java.util.ArrayList;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.preference.PreferencePage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;
import org.eclipse.ui.PlatformUI;
import org.openquark.cal.eclipse.ui.CALEclipseUIPlugin;
import org.openquark.cal.eclipse.ui.CALHelpContextIds;


/**
 * The base page for setting general cal plugin preferences.
 * See PreferenceConstants to access or change these values through public API.
 * @author Edward Lam
 */
public class CALBasePreferencePage extends PreferencePage implements IWorkbenchPreferencePage {
    
//    private static final String OPEN_TYPE_HIERARCHY = PreferenceConstants.OPEN_TYPE_HIERARCHY;
//    private static final String OPEN_TYPE_HIERARCHY_IN_PERSPECTIVE = PreferenceConstants.OPEN_TYPE_HIERARCHY_IN_PERSPECTIVE;
//    private static final String OPEN_TYPE_HIERARCHY_IN_VIEW_PART = PreferenceConstants.OPEN_TYPE_HIERARCHY_IN_VIEW_PART;
//
//    private static final String DOUBLE_CLICK = PreferenceConstants.DOUBLE_CLICK;
//    private static final String DOUBLE_CLICK_GOES_INTO = PreferenceConstants.DOUBLE_CLICK_GOES_INTO;
//    private static final String DOUBLE_CLICK_EXPANDS = PreferenceConstants.DOUBLE_CLICK_EXPANDS;

    private final ArrayList<Button> fCheckBoxes;
    private final ArrayList<Button> fRadioButtons;
    private final ArrayList<Text> fTextControls;

    public CALBasePreferencePage() {
        super();
        setPreferenceStore(CALEclipseUIPlugin.getDefault().getPreferenceStore());
        setDescription(PreferencesMessages.JavaBasePreferencePage_description);

        fRadioButtons = new ArrayList<Button>();
        fCheckBoxes = new ArrayList<Button>();
        fTextControls = new ArrayList<Text>();
    }
    
    /*
     * @see IWorkbenchPreferencePage#init(IWorkbench)
     */
    public void init(IWorkbench workbench) {
    }               
    
    /*
     * @see PreferencePage#createControl(Composite)
     */
    @Override
    public void createControl(Composite parent) {
        super.createControl(parent);
        PlatformUI.getWorkbench().getHelpSystem().setHelp(getControl(), CALHelpContextIds.JAVA_BASE_PREFERENCE_PAGE);
    }       
    
    // ADE not used!  Delete me!
//    private Button addRadioButton(Composite parent, String label, String key, String value) {
//        GridData gd = new GridData(GridData.HORIZONTAL_ALIGN_FILL);
//
//        Button button = new Button(parent, SWT.RADIO);
//        button.setText(label);
//        button.setData(new String[]{key, value});
//        button.setLayoutData(gd);
//
//        button.setSelection(value.equals(getPreferenceStore().getString(key)));
//
//        fRadioButtons.add(button);
//        return button;
//    }
//    
//    private Button addCheckBox(Composite parent, String label, String key) {
//        GridData gd = new GridData(GridData.HORIZONTAL_ALIGN_FILL);
//
//        Button button = new Button(parent, SWT.CHECK);
//        button.setText(label);
//        button.setData(key);
//        button.setLayoutData(gd);
//
//        button.setSelection(getPreferenceStore().getBoolean(key));
//
//        fCheckBoxes.add(button);
//        return button;
//    }
    
    @Override
    protected Control createContents(Composite parent) {
        initializeDialogUnits(parent);

        Composite result = new Composite(parent, SWT.NONE);
        GridLayout layout = new GridLayout();
        layout.marginHeight = convertVerticalDLUsToPixels(IDialogConstants.VERTICAL_MARGIN);
        layout.marginWidth = 0;
        layout.verticalSpacing = convertVerticalDLUsToPixels(10);
        layout.horizontalSpacing = convertHorizontalDLUsToPixels(IDialogConstants.HORIZONTAL_SPACING);
        result.setLayout(layout);
        
        // new Label(composite, SWT.NONE); // spacer
        // Group linkSettings= new Group(result, SWT.NONE);
        // linkSettings.setLayout(new GridLayout());
        // linkSettings.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
        // linkSettings.setText(PreferencesMessages.getString("JavaBasePreferencePage.linkSettings.text")); //$NON-NLS-1$
        //addCheckBox(linkSettings, PreferencesMessages.getString("JavaBasePreferencePage.linkJavaBrowsingViewsCheckbox.text"), LINK_BROWSING_VIEW_TO_EDITOR); //$NON-NLS-1$
        //addCheckBox(linkSettings, PreferencesMessages.getString("JavaBasePreferencePage.linkPackageView"), LINK_PACKAGES_TO_EDITOR); //$NON-NLS-1$
        //addCheckBox(linkSettings, PreferencesMessages.getString("JavaBasePreferencePage.linkTypeHierarchy"), LINK_TYPEHIERARCHY_TO_EDITOR); //$NON-NLS-1$
        
        // new Label(result, SWT.NONE); // spacer
        
//        Group doubleClickGroup = new Group(result, SWT.NONE);
//        doubleClickGroup.setLayout(new GridLayout());
//        doubleClickGroup.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
//        doubleClickGroup.setText(PreferencesMessages.JavaBasePreferencePage_doubleclick_action);
//        addRadioButton(doubleClickGroup, PreferencesMessages.JavaBasePreferencePage_doubleclick_gointo, DOUBLE_CLICK, DOUBLE_CLICK_GOES_INTO);
//        addRadioButton(doubleClickGroup, PreferencesMessages.JavaBasePreferencePage_doubleclick_expand, DOUBLE_CLICK, DOUBLE_CLICK_EXPANDS); 
        
        // new Label(result, SWT.NONE); // spacer
        
//        Group typeHierarchyGroup = new Group(result, SWT.NONE);
//        typeHierarchyGroup.setLayout(new GridLayout());
//        typeHierarchyGroup.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
//        typeHierarchyGroup.setText(PreferencesMessages.JavaBasePreferencePage_openTypeHierarchy);
//        addRadioButton(typeHierarchyGroup, PreferencesMessages.JavaBasePreferencePage_inPerspective, OPEN_TYPE_HIERARCHY, OPEN_TYPE_HIERARCHY_IN_PERSPECTIVE);
//        addRadioButton(typeHierarchyGroup, PreferencesMessages.JavaBasePreferencePage_inView, OPEN_TYPE_HIERARCHY, OPEN_TYPE_HIERARCHY_IN_VIEW_PART);

//        Group refactoringGroup = new Group(result, SWT.NONE);
//        refactoringGroup.setLayout(new GridLayout());
//        refactoringGroup.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
//        refactoringGroup.setText(PreferencesMessages.JavaBasePreferencePage_refactoring_title); 
//        addCheckBox(refactoringGroup, 
//                PreferencesMessages.JavaBasePreferencePage_refactoring_auto_save, 
//                PreferenceConstants.REFACTOR_SAVE_ALL_EDITORS /*RefactoringSavePreferences.PREF_SAVE_ALL_EDITORS*/);
//        
//        Group group = new Group(result, SWT.NONE);
//        group.setLayout(new GridLayout());
//        group.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
//        group.setText(PreferencesMessages.JavaBasePreferencePage_search);
//
//        addCheckBox(group, PreferencesMessages.JavaBasePreferencePage_search_small_menu, PreferenceConstants.SEARCH_USE_REDUCED_MENU);

        Dialog.applyDialogFont(result);
        return result;
    }
    
    /*
     * @see PreferencePage#performDefaults()
     */
    @Override
    protected void performDefaults() {
        IPreferenceStore store = getPreferenceStore();
        for (int i = 0; i < fCheckBoxes.size(); i++) {
            Button button = fCheckBoxes.get(i);
            String key = (String)button.getData();
            button.setSelection(store.getDefaultBoolean(key));
        }
        for (int i = 0; i < fRadioButtons.size(); i++) {
            Button button = fRadioButtons.get(i);
            String[] info = (String[])button.getData();
            button.setSelection(info[1].equals(store.getDefaultString(info[0])));
        }
        for (int i = 0; i < fTextControls.size(); i++) {
            Text text = fTextControls.get(i);
            String key = (String)text.getData();
            text.setText(store.getDefaultString(key));
        }
        super.performDefaults();
    }
    
    /*
     * @see IPreferencePage#performOk()
     */
    @Override
    public boolean performOk() {
        IPreferenceStore store = getPreferenceStore();
        for (int i = 0; i < fCheckBoxes.size(); i++) {
            Button button = fCheckBoxes.get(i);
            String key = (String)button.getData();
            store.setValue(key, button.getSelection());
        }
        for (int i = 0; i < fRadioButtons.size(); i++) {
            Button button = fRadioButtons.get(i);
            if (button.getSelection()) {
                String[] info = (String[])button.getData();
                store.setValue(info[0], info[1]);
            }
        }
        for (int i = 0; i < fTextControls.size(); i++) {
            Text text = fTextControls.get(i);
            String key = (String)text.getData();
            store.setValue(key, text.getText());
        }

        CALEclipseUIPlugin.getDefault().savePluginPreferences();
        return super.performOk();
    }
    
}


