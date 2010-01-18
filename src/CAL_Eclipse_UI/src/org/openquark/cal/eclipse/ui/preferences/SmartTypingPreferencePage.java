/*******************************************************************************
 * Copyright (c) 2006 Business Objects Software Limited and others.
 * All rights reserved. 
 * This file is made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Business Objects Software Limited - initial API and implementation based on Eclipse 3.1.2 code for
 *                             /org.eclipse.jdt.ui/ui/org/eclipse/jdt/internal/ui/preferences/SmartTypingPreferencePage.java
 *                           Eclipse source is available at: http://www.eclipse.org/downloads/
 *******************************************************************************/

/*
 * SmartTypingPreferencePage.java
 * Creation date: Feb 24, 2006.
 * By: Edward Lam
 */
package org.openquark.cal.eclipse.ui.preferences;

import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.openquark.cal.eclipse.ui.CALEclipseUIPlugin;
import org.openquark.cal.eclipse.ui.CALHelpContextIds;


/**
 * The page for setting the editor options.
 * @author Edward Lam
 */
public final class SmartTypingPreferencePage extends AbstractConfigurationBlockPreferencePage {

    /*
     * @see org.eclipse.ui.internal.editors.text.AbstractConfigureationBlockPreferencePage#getHelpId()
     */
    @Override
    protected String getHelpId() {
        return CALHelpContextIds.JAVA_EDITOR_PREFERENCE_PAGE;
    }

    /*
     * @see org.eclipse.ui.internal.editors.text.AbstractConfigurationBlockPreferencePage#setDescription()
     */
    @Override
    protected void setDescription() {
        String description = PreferencesMessages.JavaEditorPreferencePage_typing_tabTitle;
        setDescription(description);
    }

    /*
     * @see org.org.eclipse.ui.internal.editors.text.AbstractConfigurationBlockPreferencePage#setPreferenceStore()
     */
    @Override
    protected void setPreferenceStore() {
        setPreferenceStore(CALEclipseUIPlugin.getDefault().getPreferenceStore());
    }

    @Override
    protected Label createDescriptionLabel(Composite parent) {
        return null; // no description for new look.
    }

    /*
     * @see org.eclipse.ui.internal.editors.text.AbstractConfigureationBlockPreferencePage#createConfigurationBlock(org.eclipse.ui.internal.editors.text.OverlayPreferenceStore)
     */
    @Override
    protected IPreferenceConfigurationBlock createConfigurationBlock(OverlayPreferenceStore overlayPreferenceStore) {
        return new SmartTypingConfigurationBlock(overlayPreferenceStore);
    }
}
