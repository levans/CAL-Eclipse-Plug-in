/*******************************************************************************
 * Copyright (c) 2006 Business Objects Software Limited and others.
 * All rights reserved. 
 * This file is made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Business Objects Software Limited - initial API and implementation based on Eclipse 3.1.2 code for
 *                             /org.eclipse.jdt.ui/ui/org/eclipse/jdt/internal/ui/preferences/JavaBuildConfigurationBlock.java
 *                           Eclipse source is available at: http://www.eclipse.org/downloads/
 *******************************************************************************/

/*
 * TabKeyConfigurationBlock.java
 * Creation date: Feb 17, 2006.
 * By: Edward Lam
 */
package org.openquark.cal.eclipse.ui.preferences;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.jface.dialogs.IDialogSettings;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.preferences.IWorkbenchPreferenceContainer;
import org.openquark.cal.eclipse.core.CoreOptionIDs;
import org.openquark.cal.eclipse.core.formatter.DefaultCodeFormatterConstants;
import org.openquark.cal.eclipse.ui.CALEclipseUIPlugin;
import org.openquark.cal.eclipse.ui.dialogs.StatusInfo;
import org.openquark.cal.eclipse.ui.dialogs.StatusUtil;
import org.openquark.cal.eclipse.ui.preferences.formatter.FormatterMessages;
import org.openquark.cal.eclipse.ui.util.Messages;
import org.openquark.cal.eclipse.ui.util.PixelConverter;
import org.openquark.cal.eclipse.ui.wizards.IStatusChangeListener;



/**
 * Configuration block for specifying behaviour of the tab key.
 * @author Edward Lam
 */
public class TabKeyConfigurationBlock extends OptionsConfigurationBlock {
    
    private static final String SETTINGS_SECTION_NAME = "JavaBuildConfigurationBlock"; //$NON-NLS-1$
    
    private static final Key PREF_TAB_POLICY = getJDTCoreKey(DefaultCodeFormatterConstants.FORMATTER_TAB_CHAR);
    private static final Key PREF_INDENT_SIZE = getJDTCoreKey(DefaultCodeFormatterConstants.FORMATTER_INDENTATION_SIZE);
    private static final Key PREF_TAB_SIZE = getJDTCoreKey(DefaultCodeFormatterConstants.FORMATTER_TAB_SIZE);
    
    // values
    
    private PixelConverter fPixelConverter;
    
    private IStatus fIndentSizeStatus, fTabSizeStatus;

    private Text indentSizeTextField;

    public TabKeyConfigurationBlock(IStatusChangeListener context, IProject project, IWorkbenchPreferenceContainer container) {
        super(context, project, getKeys(), container);
        fIndentSizeStatus = new StatusInfo();
        fTabSizeStatus = new StatusInfo();
    }
    
    private static Key[] getKeys() {
        Key[] keys = new Key[] { PREF_TAB_POLICY, PREF_INDENT_SIZE, PREF_TAB_SIZE };
        return keys;
    }
    
    /*
     * @see org.eclipse.jface.preference.PreferencePage#createContents(Composite)
     */
    @Override
    protected Control createContents(Composite parent) {
        fPixelConverter = new PixelConverter(parent);
        setShell(parent.getShell());
        
        Composite mainComp = new Composite(parent, SWT.NONE);
        mainComp.setFont(parent.getFont());
        GridLayout layout = new GridLayout();
        layout.marginHeight = 0;
        layout.marginWidth = 0;
        mainComp.setLayout(layout);
        
        Composite othersComposite = createBuildPathTabContent(mainComp);
        GridData gridData = new GridData(GridData.FILL, GridData.FILL, true, true);
        gridData.heightHint = fPixelConverter.convertHeightInCharsToPixels(20);
        othersComposite.setLayoutData(gridData);
        
        validateSettings(null, null, null);
        
        return mainComp;
    }
    
    
    private Composite createBuildPathTabContent(Composite parent) { 
        String[] valuesTabChar = new String[] { CoreOptionIDs.SPACE, CoreOptionIDs.TAB, DefaultCodeFormatterConstants.MIXED };
        String[] valuesTabCharLabels= new String[] {
                FormatterMessages.IndentationTabPage_general_group_option_tab_policy_SPACE, 
                FormatterMessages.IndentationTabPage_general_group_option_tab_policy_TAB, 
                FormatterMessages.IndentationTabPage_general_group_option_tab_policy_MIXED
        };
        
        int nColumns = 3;
        
        final ScrolledPageContent pageContent = new ScrolledPageContent(parent);
        
        GridLayout layout = new GridLayout();
        layout.numColumns = nColumns;
        layout.marginHeight = 0;
        layout.marginWidth = 0;
        
        Composite composite = pageContent.getBody();
        composite.setLayout(layout);
        
        Composite subComposite = new Composite(composite, SWT.NONE);
        subComposite.setFont(composite.getFont());
        
        layout = new GridLayout();
        layout.marginHeight = 0;
        layout.marginWidth = 0;
        layout.numColumns = 1;
        subComposite.setLayout(layout);
        
        layout = new GridLayout();
        layout.numColumns = nColumns;
        
        Group group = new Group(subComposite, SWT.NONE);
        group.setFont(subComposite.getFont());
        group.setText(FormatterMessages.IndentationTabPage_general_group_title);
        group.setLayoutData(new GridData(GridData.FILL, GridData.FILL, false, false));
        group.setLayout(layout);
        
        String label = FormatterMessages.IndentationTabPage_general_group_option_tab_policy;
        addComboBox(group, label, PREF_TAB_POLICY, valuesTabChar, valuesTabCharLabels, 0);
        
        {
            label = FormatterMessages.IndentationTabPage_general_group_option_indent_size;
            indentSizeTextField = addTextField(group, label, PREF_INDENT_SIZE, 0, 0);
            GridData gd = (GridData)indentSizeTextField.getLayoutData();
            gd.widthHint = fPixelConverter.convertWidthInCharsToPixels(2);
            gd.horizontalAlignment = GridData.END;
            indentSizeTextField.setTextLimit(1);
        }
        
        {
            label = FormatterMessages.IndentationTabPage_general_group_option_tab_size;
            Text tabSizeTextField = addTextField(group, label, PREF_TAB_SIZE, 0, 0);
            GridData gd = (GridData)tabSizeTextField.getLayoutData();
            gd.widthHint = fPixelConverter.convertWidthInCharsToPixels(2);
            gd.horizontalAlignment = GridData.END;
            tabSizeTextField.setTextLimit(1);
        }
        
        IDialogSettings section = CALEclipseUIPlugin.getDefault().getDialogSettings().getSection(SETTINGS_SECTION_NAME);
        restoreSectionExpansionStates(section);
        
        return pageContent;
    }
    
    /* (non-javadoc)
     * Update fields and validate.
     * @param changedKey Key that changed, or null, if all changed.
     */
    @Override
    protected void validateSettings(Key changedKey, String oldValue, String newValue) {
        
        if (changedKey != null) {
            if (PREF_INDENT_SIZE.equals(changedKey)) {
                fIndentSizeStatus = validateIndentSize();
            } else if (PREF_TAB_SIZE.equals(changedKey)) {
                fTabSizeStatus = validateTabSize();
            } else {
                updateEnableStates();
                return;
            }
        } else {
            updateEnableStates();
            fIndentSizeStatus = validateIndentSize();
            fTabSizeStatus = validateTabSize();
        }
        IStatus status = StatusUtil.getMostSevere(new IStatus[]{fIndentSizeStatus, fTabSizeStatus});
        fContext.statusChanged(status);
    }
    
    private void updateEnableStates() {
        boolean indentSizeEnabled = !checkValue(PREF_TAB_POLICY, CoreOptionIDs.TAB);
        indentSizeTextField.setEnabled(indentSizeEnabled);
    }
    
    @Override
    protected String[] getFullBuildDialogStrings(boolean workspaceSettings) {
        // Full build not required when settings are changed.
        return null;
    }
    
    private IStatus validateIndentSize() {
        String number = getValue(PREF_INDENT_SIZE);
        StatusInfo status = new StatusInfo();
        if (number.length() == 0) {
            status.setError(PreferencesMessages.JavaBuildConfigurationBlock_empty_input);
        } else {
            try {
                int value = Integer.parseInt(number);
                if (value <= 0) {
                    status.setError(Messages.format(PreferencesMessages.JavaBuildConfigurationBlock_invalid_input, number));
                }
            } catch (NumberFormatException e) {
                status.setError(Messages.format(PreferencesMessages.JavaBuildConfigurationBlock_invalid_input, number));
            }
        }
        return status;
    }
    
    private IStatus validateTabSize() {
        String number = getValue(PREF_TAB_SIZE);
        StatusInfo status = new StatusInfo();
        if (number.length() == 0) {
            status.setError(PreferencesMessages.JavaBuildConfigurationBlock_empty_input);
        } else {
            try {
                int value = Integer.parseInt(number);
                if (value <= 0) {
                    status.setError(Messages.format(PreferencesMessages.JavaBuildConfigurationBlock_invalid_input, number));
                }
            } catch (NumberFormatException e) {
                status.setError(Messages.format(PreferencesMessages.JavaBuildConfigurationBlock_invalid_input, number));
            }
        }
        return status;
    }
    
    /* (non-Javadoc)
     * @see org.eclipse.jdt.internal.ui.preferences.OptionsConfigurationBlock#dispose()
     */
    @Override
    public void dispose() {
        IDialogSettings settings = CALEclipseUIPlugin.getDefault().getDialogSettings().addNewSection(SETTINGS_SECTION_NAME);
        storeSectionExpansionStates(settings);
        super.dispose();
    }
    
}
