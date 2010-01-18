/*******************************************************************************
 * Copyright (c) 2006 Business Objects Software Limited and others.
 * All rights reserved. 
 * This file is made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Business Objects Software Limited - initial API and implementation based on Eclipse 3.1.2 code for
 *                             /org.eclipse.jdt.ui/ui/org/eclipse/jdt/internal/ui/preferences/JavaBuildPreferencePage.java
 *                           Eclipse source is available at: http://www.eclipse.org/downloads/
 *******************************************************************************/

/*
 * TabKeyPreferencePage.java
 * Creation date: Feb 21, 2006.
 * By: Edward Lam
 */
package org.openquark.cal.eclipse.ui.preferences;

import org.eclipse.core.runtime.IAdaptable;

import org.eclipse.core.resources.IProject;

import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;

import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.preferences.IWorkbenchPreferenceContainer;
import org.openquark.cal.eclipse.ui.CALEclipseUIPlugin;
import org.openquark.cal.eclipse.ui.CALHelpContextIds;


/**
 * Page used to configure behaviour of the tab key.
 */
public class TabKeyPreferencePage extends PropertyAndPreferencePage {

    public static final String PREF_ID = "org.openquark.cal.eclipse.ui.preferences.TabKeyPreferencePage"; //$NON-NLS-1$
    public static final String PROP_ID = "org.openquark.cal.eclipse.ui.propertyPages.TabKeyPreferencePage"; //$NON-NLS-1$

    private TabKeyConfigurationBlock fConfigurationBlock;

    public TabKeyPreferencePage() {
        setPreferenceStore(CALEclipseUIPlugin.getDefault().getPreferenceStore());
        // setDescription(PreferencesMessages.CompliancePreferencePage_description);

        // only used when page is shown programatically
        setTitle(PreferencesMessages.CompliancePreferencePage_title);
    }

    /*
     * @see org.eclipse.jface.dialogs.IDialogPage#createControl(org.eclipse.swt.widgets.Composite)
     */
    @Override
    public void createControl(Composite parent) {
        IWorkbenchPreferenceContainer container = (IWorkbenchPreferenceContainer)getContainer();
        fConfigurationBlock = new TabKeyConfigurationBlock(getNewStatusChangedListener(), getProject(), container);

        super.createControl(parent);
        if (isProjectPreferencePage()) {
            PlatformUI.getWorkbench().getHelpSystem().setHelp(getControl(), CALHelpContextIds.COMPILER_PROPERTY_PAGE);
        } else {
            PlatformUI.getWorkbench().getHelpSystem().setHelp(getControl(), CALHelpContextIds.COMPILER_PREFERENCE_PAGE);
        }
    }

    @Override
    protected Control createPreferenceContent(Composite composite) {
        return fConfigurationBlock.createContents(composite);
    }

    @Override
    protected boolean hasProjectSpecificOptions(IProject project) {
        return fConfigurationBlock.hasProjectSpecificOptions(project);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.jdt.internal.ui.preferences.PropertyAndPreferencePage#getPreferencePageID()
     */
    @Override
    protected String getPreferencePageID() {
        return PREF_ID;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.jdt.internal.ui.preferences.PropertyAndPreferencePage#getPropertyPageID()
     */
    @Override
    protected String getPropertyPageID() {
        return PROP_ID;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.jface.dialogs.DialogPage#dispose()
     */
    @Override
    public void dispose() {
        if (fConfigurationBlock != null) {
            fConfigurationBlock.dispose();
        }
        super.dispose();
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.jdt.internal.ui.preferences.PropertyAndPreferencePage#enableProjectSpecificSettings(boolean)
     */
    @Override
    protected void enableProjectSpecificSettings(boolean useProjectSpecificSettings) {
        if (fConfigurationBlock != null) {
            fConfigurationBlock.useProjectSpecificSettings(useProjectSpecificSettings);
        }
        super.enableProjectSpecificSettings(useProjectSpecificSettings);
    }

    /*
     * @see org.eclipse.jface.preference.IPreferencePage#performDefaults()
     */
    @Override
    protected void performDefaults() {
        super.performDefaults();
        if (fConfigurationBlock != null) {
            fConfigurationBlock.performDefaults();
        }
    }

    /*
     * @see org.eclipse.jface.preference.IPreferencePage#performOk()
     */
    @Override
    public boolean performOk() {
        if (fConfigurationBlock != null && !fConfigurationBlock.performOk()) {
            return false;
        }
        return super.performOk();
    }

    /*
     * @see org.eclipse.jface.preference.IPreferencePage#performApply()
     */
    @Override
    public void performApply() {
        if (fConfigurationBlock != null) {
            fConfigurationBlock.performApply();
        }
    }

    /* (non-Javadoc)
     * @see org.eclipse.jdt.internal.ui.preferences.PropertyAndPreferencePage#setElement(org.eclipse.core.runtime.IAdaptable)
     */
    @Override
    public void setElement(IAdaptable element) {
        super.setElement(element);
        setDescription(null); // no description for property page
    }

}
