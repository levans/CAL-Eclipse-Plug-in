/*******************************************************************************
 * Copyright (c) 2007 Business Objects SA and others.
 * All rights reserved. 
 * This file is made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Business Objects Software Limited - initial API and implementation based on Eclipse 3.1.2 code for
 *                             /org.eclipse.jdt.ui/ui/org/eclipse/jdt/internal/ui/preferences/SmartTypingConfigurationBlock.java
 *                           Eclipse source is available at: http://www.eclipse.org/downloads/
 *******************************************************************************/
/*
 * CALEditorContentAssistBlock.java
 * Created: Apr 12, 2007
 * By: Greg McClement
 */
package org.openquark.cal.eclipse.ui.preferences;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.jface.dialogs.IDialogSettings;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.preferences.IWorkbenchPreferenceContainer;
import org.openquark.cal.eclipse.core.formatter.DefaultCodeFormatterConstants;
import org.openquark.cal.eclipse.ui.CALEclipseUIPlugin;
import org.openquark.cal.eclipse.ui.dialogs.StatusInfo;
import org.openquark.cal.eclipse.ui.dialogs.StatusUtil;
import org.openquark.cal.eclipse.ui.util.PixelConverter;
import org.openquark.cal.eclipse.ui.wizards.IStatusChangeListener;

/**
 * Configures CAL Editor content assist preferences.
 * 
 * @author Greg McClement
 */
public class CALEditorContentAssistBlock extends OptionsConfigurationBlock {
    
    private static final String SETTINGS_SECTION_NAME = "CALEditorContentAssistBlock"; //$NON-NLS-1$
    
    // org.openquark.cal.eclipse.core.content.assist.auto.completion.triggers

    private static final Key PREF_ENABLE_AUTO_COMPLETION = getJDTCoreKey(DefaultCodeFormatterConstants.ENABLE_AUTO_COMPLETION);
    private static final Key PREF_ENABLE_INSERT_SINGLE_PROPOSAL_AUTOMATICALLY = getJDTCoreKey(DefaultCodeFormatterConstants.ENABLE_INSERT_SINGLE_PROPOSAL_AUTOMATICALLY);
    private static final Key PREF_ENABLE_ADD_IMPORT_INSTEAD_OF_QUALIFIED_NAME = getJDTCoreKey(DefaultCodeFormatterConstants.ENABLE_ADD_IMPORT_INSTEAD_OF_QUALIFIED_NAME);
    private static final Key PREF_ENABLE_FILL_ARGUMENT_NAMES_ON_COMPLETION = getJDTCoreKey(DefaultCodeFormatterConstants.ENABLE_FILL_ARGUMENT_NAMES_ON_COMPLETION);
    private static final Key PREF_AUTO_COMPLETION_TRIGGERS = getJDTCoreKey(DefaultCodeFormatterConstants.AUTO_COMPLETION_TRIGGERS);
    
    // values
    
    private PixelConverter fPixelConverter;
    
    private IStatus fAutoCompletionTriggersStatus;

    private Text autoActivationTriggers;

    private Button enableAutoActivationCheckBox;

    public CALEditorContentAssistBlock(IStatusChangeListener context, IProject project, IWorkbenchPreferenceContainer container) {
        super(context, project, getKeys(), container);
        fAutoCompletionTriggersStatus = new StatusInfo();
    }

    private static Key[] getKeys() {
        Key[] keys = new Key[] { 
                PREF_AUTO_COMPLETION_TRIGGERS, 
                PREF_ENABLE_AUTO_COMPLETION,
                PREF_ENABLE_INSERT_SINGLE_PROPOSAL_AUTOMATICALLY,
                PREF_ENABLE_ADD_IMPORT_INSTEAD_OF_QUALIFIED_NAME,
                PREF_ENABLE_FILL_ARGUMENT_NAMES_ON_COMPLETION
                };
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

        {
            Group group = new Group(subComposite, SWT.NONE);
            group.setFont(subComposite.getFont());
            group.setText(PreferencesMessages.ContentAssistPage_insertion_group_title);
            group.setLayoutData(new GridData(GridData.FILL, GridData.FILL, false, false));
            group.setLayout(layout);

            String[] titles = {
                    PreferencesMessages.ContentAssistPage_insertion_insert_single_proposal_automatically,
                    PreferencesMessages.ContentAssistPage_insertion_add_import_instead_of_qualified_name,
                    PreferencesMessages.ContentAssistPage_fill_argument_names_on_completion
            };
            
            Key[] keys = {
                    PREF_ENABLE_INSERT_SINGLE_PROPOSAL_AUTOMATICALLY,
                    PREF_ENABLE_ADD_IMPORT_INSTEAD_OF_QUALIFIED_NAME,
                    PREF_ENABLE_FILL_ARGUMENT_NAMES_ON_COMPLETION
            };

            for(int i = 0; i < titles.length; ++i){
                final Button checkBox = addCheckBox(
                        group, 
                        titles[i], 
                        keys[i], 
                        new String[] {"true", "false"}, 0);

                // Initialize the default value
                if (getValue(keys[i]) == null){
                    setValue(keys[i], "true");
                }
                checkBox.setSelection(getBooleanValue(keys[i]));
            }

        }

        {
            Group group = new Group(subComposite, SWT.NONE);
            group.setFont(subComposite.getFont());
            group.setText(PreferencesMessages.ContentAssistPage_general_group_title);
            group.setLayoutData(new GridData(GridData.FILL, GridData.FILL, false, false));
            group.setLayout(layout);

            {
                enableAutoActivationCheckBox = addCheckBox(
                        group, 
                        PreferencesMessages.JavaEditorPreferencePage_enableAutoActivation, 
                        PREF_ENABLE_AUTO_COMPLETION, 
                        new String[] {"true", "false"}, 0);

                // Initialize the default value
                if (getValue(PREF_ENABLE_AUTO_COMPLETION) == null){
                    setValue(PREF_ENABLE_AUTO_COMPLETION, "true");
                }
                enableAutoActivationCheckBox.setSelection(getBooleanValue(PREF_ENABLE_AUTO_COMPLETION));
            }

            {
                String label = PreferencesMessages.ContentAssistPage_general_autoCompletion_triggers_code;
                autoActivationTriggers = addTextField(group, label, PREF_AUTO_COMPLETION_TRIGGERS, 0, 20);
                if (autoActivationTriggers.getText().length() == 0){
                    autoActivationTriggers.setText(".");
                }
                GridData gd = (GridData)autoActivationTriggers.getLayoutData();
                gd.widthHint = fPixelConverter.convertWidthInCharsToPixels(2);
                gd.horizontalAlignment = GridData.END;
                autoActivationTriggers.setEnabled(true);
                autoActivationTriggers.setEnabled(enableAutoActivationCheckBox.getSelection());
            }

            IDialogSettings section = CALEclipseUIPlugin.getDefault().getDialogSettings().getSection(SETTINGS_SECTION_NAME);
            restoreSectionExpansionStates(section);

            enableAutoActivationCheckBox.addSelectionListener(new SelectionListener() {
                public void widgetDefaultSelected(SelectionEvent e) {
                    // do nothing
                }
                public void widgetSelected(SelectionEvent e) {
                    autoActivationTriggers.setEnabled(enableAutoActivationCheckBox.getSelection());
                }
            });
        }

        return pageContent;
    }
    
    /* (non-javadoc)
     * Update fields and validate.
     * @param changedKey Key that changed, or null, if all changed.
     */
    @Override
    protected void validateSettings(Key changedKey, String oldValue, String newValue) {
        
        if (changedKey != null) {
            if (PREF_AUTO_COMPLETION_TRIGGERS.equals(changedKey)) {
                fAutoCompletionTriggersStatus = validateAutoCompletionTriggers();
            } else {
                updateEnableStates();
                return;
            }
        } else {
            updateEnableStates();
            fAutoCompletionTriggersStatus = validateAutoCompletionTriggers();
        }
        IStatus status = StatusUtil.getMostSevere(new IStatus[]{fAutoCompletionTriggersStatus});
        fContext.statusChanged(status);
    }
    
    private void updateEnableStates() {
        autoActivationTriggers.setEnabled(enableAutoActivationCheckBox.getSelection());
    }
    
    @Override
    protected String[] getFullBuildDialogStrings(boolean workspaceSettings) {
        // Full build not required when settings are changed.
        return null;
    }
    
    private IStatus validateAutoCompletionTriggers() {
        String number = getValue(PREF_AUTO_COMPLETION_TRIGGERS);
        StatusInfo status = new StatusInfo();
        if (number != null && number.length() == 0) {
            status.setError(PreferencesMessages.ContentAssistBlock_emptyTrigger);
        } else {
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
