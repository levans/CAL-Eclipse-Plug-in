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
 * CALBuildConfigurationBlock.java
 * Creation date: Feb 16, 2006.
 * By: Edward Lam
 */
package org.openquark.cal.eclipse.ui.preferences;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.jface.dialogs.IDialogSettings;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.ui.preferences.IWorkbenchPreferenceContainer;
import org.openquark.cal.eclipse.core.CoreOptionIDs;
import org.openquark.cal.eclipse.ui.CALEclipseUIPlugin;
import org.openquark.cal.eclipse.ui.dialogs.StatusInfo;
import org.openquark.cal.eclipse.ui.dialogs.StatusUtil;
import org.openquark.cal.eclipse.ui.util.Messages;
import org.openquark.cal.eclipse.ui.util.PixelConverter;
import org.openquark.cal.eclipse.ui.wizards.IStatusChangeListener;



/**
 * The configuration block for setting cal build preferences.
 * @author Edward Lam
 */
public class CALBuildConfigurationBlock extends OptionsConfigurationBlock {
    
    private static final String SETTINGS_SECTION_NAME = "CALBuildConfigurationBlock"; //$NON-NLS-1$

    private static final Key PREF_ENABLE_BUILDER = getJDTCoreKey(CoreOptionIDs.CORE_CAL_BUILD_ENABLE);
    
    private static final Key PREF_PB_MAX_PER_UNIT = getJDTCoreKey(CoreOptionIDs.COMPILER_PB_MAX_PER_UNIT);

    private static final Key PREF_RESOURCE_FILTER = getJDTCoreKey(CoreOptionIDs.CORE_JAVA_BUILD_RESOURCE_COPY_FILTER);
    private static final Key PREF_BUILD_INVALID_CLASSPATH = getJDTCoreKey(CoreOptionIDs.CORE_JAVA_BUILD_INVALID_CLASSPATH);
    private static final Key PREF_BUILD_CLEAN_OUTPUT_FOLDER = getJDTCoreKey(CoreOptionIDs.CORE_JAVA_BUILD_CLEAN_OUTPUT_FOLDER);
    private static final Key PREF_ENABLE_EXCLUSION_PATTERNS = getJDTCoreKey(CoreOptionIDs.CORE_ENABLE_CLASSPATH_EXCLUSION_PATTERNS);
    private static final Key PREF_ENABLE_MULTIPLE_OUTPUT_LOCATIONS = getJDTCoreKey(CoreOptionIDs.CORE_ENABLE_CLASSPATH_MULTIPLE_OUTPUT_LOCATIONS);

    private static final Key PREF_PB_INCOMPLETE_BUILDPATH = getJDTCoreKey(CoreOptionIDs.CORE_INCOMPLETE_CLASSPATH);
    private static final Key PREF_PB_CIRCULAR_BUILDPATH = getJDTCoreKey(CoreOptionIDs.CORE_CIRCULAR_CLASSPATH);
    private static final Key PREF_PB_INCOMPATIBLE_JDK_LEVEL = getJDTCoreKey(CoreOptionIDs.CORE_INCOMPATIBLE_JDK_LEVEL);
    private static final Key PREF_PB_DUPLICATE_RESOURCE = getJDTCoreKey(CoreOptionIDs.CORE_JAVA_BUILD_DUPLICATE_RESOURCE);

    // values
//    private static final String ERROR = CoreOptionIDs.ERROR;  // unused
//    private static final String WARNING = CoreOptionIDs.WARNING;  // unused
    private static final String IGNORE = CoreOptionIDs.IGNORE;

//    private static final String ABORT = CoreOptionIDs.ABORT;  // unused
    private static final String CLEAN = CoreOptionIDs.CLEAN;

    private static final String ENABLED = CoreOptionIDs.ENABLED;
    private static final String DISABLED = CoreOptionIDs.DISABLED;
    
    private PixelConverter fPixelConverter;
    
    private IStatus fMaxNumberProblemsStatus, fResourceFilterStatus;

    private Button enableBuilderCheckBox;
    
    public CALBuildConfigurationBlock(IStatusChangeListener context, IProject project, IWorkbenchPreferenceContainer container) {
        super(context, project, getKeys(), container);
        fMaxNumberProblemsStatus = new StatusInfo();
        fResourceFilterStatus = new StatusInfo();
    }
    
    private static Key[] getKeys() {
        Key[] keys = new Key[] {
                PREF_ENABLE_BUILDER,
                PREF_PB_MAX_PER_UNIT, PREF_RESOURCE_FILTER, PREF_BUILD_INVALID_CLASSPATH, PREF_PB_INCOMPLETE_BUILDPATH, PREF_PB_CIRCULAR_BUILDPATH,
                PREF_BUILD_CLEAN_OUTPUT_FOLDER, PREF_PB_DUPLICATE_RESOURCE,
                PREF_PB_INCOMPATIBLE_JDK_LEVEL, PREF_ENABLE_EXCLUSION_PATTERNS, PREF_ENABLE_MULTIPLE_OUTPUT_LOCATIONS,
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
//        String[] abortIgnoreValues = new String[] { ABORT, IGNORE };
        String[] cleanIgnoreValues = new String[] { CLEAN, IGNORE };
        String[] enableDisableValues = new String[] { ENABLED, DISABLED };
        
//        String[] errorWarning = new String[] { ERROR, WARNING };
//        
//        String[] errorWarningLabels = new String[] {
//                PreferencesMessages.JavaBuildConfigurationBlock_error,  
//                PreferencesMessages.JavaBuildConfigurationBlock_warning
//        };
//        
//        String[] errorWarningIgnore = new String[] { ERROR, WARNING, IGNORE };
//        String[] errorWarningIgnoreLabels = new String[] {
//                PreferencesMessages.JavaBuildConfigurationBlock_error,  
//                PreferencesMessages.JavaBuildConfigurationBlock_warning, 
//                PreferencesMessages.JavaBuildConfigurationBlock_ignore
//        };
        
        int nColumns = 3;
        
        final ScrolledPageContent pageContent = new ScrolledPageContent(parent);
        
        GridLayout layout = new GridLayout();
        layout.numColumns = nColumns;
        layout.marginHeight = 0;
        layout.marginWidth = 0;
        
        Composite composite = pageContent.getBody();
        composite.setLayout(layout);
        
//        String label = PreferencesMessages.JavaBuildConfigurationBlock_section_general;
//        ExpandableComposite excomposite = createStyleSection(composite, label, nColumns);
//
//        Composite othersComposite = new Composite(excomposite, SWT.NONE);
//        excomposite.setClient(othersComposite);
//        othersComposite.setLayout(new GridLayout(nColumns, false));

        String label = PreferencesMessages.CALBuildConfigurationBlock_enable_cal_builder_label;
        this.enableBuilderCheckBox = addCheckBox(composite, label, PREF_ENABLE_BUILDER, enableDisableValues, 0);
        
//        label = PreferencesMessages.JavaBuildConfigurationBlock_pb_max_per_unit_label;
//        Text text = addTextField(othersComposite, label, PREF_PB_MAX_PER_UNIT, 0, 0);
//        GridData gd = (GridData)text.getLayoutData();
//        gd.widthHint = fPixelConverter.convertWidthInCharsToPixels(8);
//        gd.horizontalAlignment = GridData.END;
//        text.setTextLimit(6);

//        label = PreferencesMessages.JavaBuildConfigurationBlock_enable_exclusion_patterns_label;
//        addCheckBox(othersComposite, label, PREF_ENABLE_EXCLUSION_PATTERNS, enableDisableValues, 0);

//        label = PreferencesMessages.JavaBuildConfigurationBlock_enable_multiple_outputlocations_label;
//        addCheckBox(othersComposite, label, PREF_ENABLE_MULTIPLE_OUTPUT_LOCATIONS, enableDisableValues, 0);
//
//        label = PreferencesMessages.JavaBuildConfigurationBlock_section_build_path_problems;
//        excomposite = createStyleSection(composite, label, nColumns);
//
//        othersComposite = new Composite(excomposite, SWT.NONE);
//        excomposite.setClient(othersComposite);
//        othersComposite.setLayout(new GridLayout(nColumns, false));
//
//        label = PreferencesMessages.JavaBuildConfigurationBlock_build_invalid_classpath_label;
//        addCheckBox(othersComposite, label, PREF_BUILD_INVALID_CLASSPATH, abortIgnoreValues, 0);
//
//        label = PreferencesMessages.JavaBuildConfigurationBlock_pb_incomplete_build_path_label;
//        addComboBox(othersComposite, label, PREF_PB_INCOMPLETE_BUILDPATH, errorWarning, errorWarningLabels, 0);
//
//        label = PreferencesMessages.JavaBuildConfigurationBlock_pb_build_path_cycles_label;
//        addComboBox(othersComposite, label, PREF_PB_CIRCULAR_BUILDPATH, errorWarning, errorWarningLabels, 0);
//
//        label = PreferencesMessages.JavaBuildConfigurationBlock_pb_check_prereq_binary_level_label;
//        addComboBox(othersComposite, label, PREF_PB_INCOMPATIBLE_JDK_LEVEL, errorWarningIgnore, errorWarningIgnoreLabels, 0);

//        label = PreferencesMessages.JavaBuildConfigurationBlock_section_output_folder;
//        excomposite = createStyleSection(composite, label, nColumns);
//
//        othersComposite = new Composite(excomposite, SWT.NONE);
//        excomposite.setClient(othersComposite);
//        othersComposite.setLayout(new GridLayout(nColumns, false));

//        label = PreferencesMessages.JavaBuildConfigurationBlock_pb_duplicate_resources_label;
//        addComboBox(othersComposite, label, PREF_PB_DUPLICATE_RESOURCE, errorWarning, errorWarningLabels, 0);

        label = PreferencesMessages.JavaBuildConfigurationBlock_build_clean_outputfolder_label;
        addCheckBox(composite, label, PREF_BUILD_CLEAN_OUTPUT_FOLDER, cleanIgnoreValues, 0);

//        label = PreferencesMessages.JavaBuildConfigurationBlock_resource_filter_label;
//        text = addTextField(othersComposite, label, PREF_RESOURCE_FILTER, 0, 0);
//        gd = (GridData)text.getLayoutData();
//        gd.grabExcessHorizontalSpace = true;
//        gd.widthHint = fPixelConverter.convertWidthInCharsToPixels(10);
//
//        Label description = new Label(othersComposite, SWT.WRAP);
//        description.setText(PreferencesMessages.JavaBuildConfigurationBlock_resource_filter_description);
//        gd = new GridData(GridData.HORIZONTAL_ALIGN_FILL);
//        gd.horizontalSpan = nColumns;
//        gd.widthHint = fPixelConverter.convertWidthInCharsToPixels(60);
//        description.setLayoutData(gd);

        IDialogSettings section = CALEclipseUIPlugin.getDefault().getDialogSettings().getSection(SETTINGS_SECTION_NAME);
        restoreSectionExpansionStates(section);
        
        return pageContent;
    }
    
    /*
     * (non-javadoc) Update fields and validate. @param changedKey Key that changed, or null, if all changed.
     */     
    @Override
    protected void validateSettings(Key changedKey, String oldValue, String newValue) {
        
        if (changedKey != null) {
            if (PREF_ENABLE_BUILDER.equals(changedKey)) {
                updateEnableStates();
            } if (PREF_PB_MAX_PER_UNIT.equals(changedKey)) {
                fMaxNumberProblemsStatus = validateMaxNumberProblems();
            } else if (PREF_RESOURCE_FILTER.equals(changedKey)) {
                fResourceFilterStatus = validateResourceFilters();
            } else {
                return;
            }
        } else {
            updateEnableStates();
            fMaxNumberProblemsStatus = validateMaxNumberProblems();
//            fResourceFilterStatus = validateResourceFilters();
        }
        IStatus status = StatusUtil.getMostSevere(new IStatus[]{fMaxNumberProblemsStatus, fResourceFilterStatus});
        fContext.statusChanged(status);
    }
    
    /*
     * Update the controls' enable state
     */             
    private void updateEnableStates() {
        // update the UI
        boolean enabled = checkValue(PREF_ENABLE_BUILDER, ENABLED);
        for (int i = fCheckBoxes.size() - 1; i >= 0; i--) {
            Control curr = fCheckBoxes.get(i);
            
            // Don't disable the checkbox allowing builder enablement.
            if (curr != enableBuilderCheckBox) {
                curr.setEnabled(enabled);
            }
        }
        for (int i = fComboBoxes.size() - 1; i >= 0; i--) {
            Control curr = fComboBoxes.get(i);
            curr.setEnabled(enabled);
        }
        for (int i = fTextBoxes.size() - 1; i >= 0; i--) {
            Control curr = fTextBoxes.get(i);
            curr.setEnabled(enabled);
        }

    }
    @Override
    protected String[] getFullBuildDialogStrings(boolean workspaceSettings) {
        String title = PreferencesMessages.JavaBuildConfigurationBlock_needsbuild_title;
        String message;
        if (workspaceSettings) {
            message = PreferencesMessages.JavaBuildConfigurationBlock_needsfullbuild_message;
        } else {
            message = PreferencesMessages.JavaBuildConfigurationBlock_needsprojectbuild_message;
        }
        return new String[] { title, message };
    }
    
    private IStatus validateMaxNumberProblems() {
        String number = getValue(PREF_PB_MAX_PER_UNIT);
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
    
    private IStatus validateResourceFilters() {
        String text = getValue(PREF_RESOURCE_FILTER);

        IWorkspace workspace = ResourcesPlugin.getWorkspace();

        String[] filters = getTokens(text, ","); //$NON-NLS-1$
        for (final String filter : filters) {
            String fileName = filter.replace('*', 'x');
            int resourceType = IResource.FILE;
            int lastCharacter = fileName.length() - 1;
            if (lastCharacter >= 0 && fileName.charAt(lastCharacter) == '/') {
                fileName = fileName.substring(0, lastCharacter);
                resourceType = IResource.FOLDER;
            }
            IStatus status = workspace.validateName(fileName, resourceType);
            if (status.matches(IStatus.ERROR)) {
                String message = Messages.format(PreferencesMessages.JavaBuildConfigurationBlock_filter_invalidsegment_error, status.getMessage());
                return new StatusInfo(IStatus.ERROR, message);
            }
        }
        return new StatusInfo();
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
