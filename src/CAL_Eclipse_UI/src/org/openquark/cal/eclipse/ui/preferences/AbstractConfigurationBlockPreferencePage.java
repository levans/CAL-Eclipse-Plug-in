/*******************************************************************************
 * Copyright (c) 2006 Business Objects Software Limited and others.
 * All rights reserved. 
 * This file is made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Business Objects Software Limited - initial API and implementation based on Eclipse 3.1.2 code for
 *                             /org.eclipse.jdt.ui/ui/org/eclipse/jdt/internal/ui/preferences/AbstractConfigurationBlockPreferencePage.java
 *                           Eclipse source is available at: http://www.eclipse.org/downloads/
 *******************************************************************************/

/*
 * AbstractConfigurationBlockPreferencePage.java
 * Creation date: Feb 14, 2006.
 * By: Edward Lam
 */
package org.openquark.cal.eclipse.ui.preferences;

import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.preference.PreferencePage;

import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;
import org.eclipse.ui.PlatformUI;
import org.openquark.cal.eclipse.ui.CALEclipseUIPlugin;


/**
 * Abstract preference page which is used to wrap a
 * {@link org.openquark.cal.eclipse.ui.preferences.IPreferenceConfigurationBlock}.
 * 
 * @author Edward Lam
 */
public abstract class AbstractConfigurationBlockPreferencePage extends PreferencePage implements IWorkbenchPreferencePage {
    
    
    private final IPreferenceConfigurationBlock fConfigurationBlock;
    private OverlayPreferenceStore fOverlayStore;
    
    
    /**
     * Creates a new preference page.
     */
    public AbstractConfigurationBlockPreferencePage() {
        setDescription();
        setPreferenceStore();
        fOverlayStore = new OverlayPreferenceStore(getPreferenceStore(), new OverlayPreferenceStore.OverlayKey[]{});
        fConfigurationBlock = createConfigurationBlock(fOverlayStore);
    }
    
    protected abstract IPreferenceConfigurationBlock createConfigurationBlock(OverlayPreferenceStore overlayPreferenceStore);
    protected abstract String getHelpId();
    protected abstract void setDescription();
    protected abstract void setPreferenceStore();
    
    /*
     * @see IWorkbenchPreferencePage#init()
     */     
    public void init(IWorkbench workbench) {
    }
    
    /*
     * @see PreferencePage#createControl(Composite)
     */
    @Override
    public void createControl(Composite parent) {
        super.createControl(parent);
        PlatformUI.getWorkbench().getHelpSystem().setHelp(getControl(), getHelpId());
    }
    
    /*
     * @see PreferencePage#createContents(Composite)
     */
    @Override
    protected Control createContents(Composite parent) {

        fOverlayStore.load();
        fOverlayStore.start();

        Control content = fConfigurationBlock.createControl(parent);

        initialize();

        Dialog.applyDialogFont(content);
        return content;
    }
    
    private void initialize() {
        fConfigurationBlock.initialize();
    }
    
    /*
     * @see PreferencePage#performOk()
     */
    @Override
    public boolean performOk() {
        
        fConfigurationBlock.performOk();
        
        fOverlayStore.propagate();
        
        CALEclipseUIPlugin.getDefault().savePluginPreferences();
        
        return true;
    }
    
    /*
     * @see PreferencePage#performDefaults()
     */
    @Override
    public void performDefaults() {
        
        fOverlayStore.loadDefaults();
        fConfigurationBlock.performDefaults();
        
        super.performDefaults();
    }
    
    /*
     * @see DialogPage#dispose()
     */
    @Override
    public void dispose() {

        fConfigurationBlock.dispose();

        if (fOverlayStore != null) {
            fOverlayStore.stop();
            fOverlayStore = null;
        }

        super.dispose();
    }
}
