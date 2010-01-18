/*******************************************************************************
 * Copyright (c) 2006 Business Objects Software Limited and others.
 * All rights reserved. 
 * This file is made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Business Objects Software Limited - initial API and implementation based on Eclipse 3.1.2 code for
 *                             /org.eclipse.jdt.ui/ui/org/eclipse/jdt/internal/ui/JavaUIPreferenceInitializer.java
 *                           Eclipse source is available at: http://www.eclipse.org/downloads/
 *******************************************************************************/

/*
 * CALUIPreferenceInitializer.java
 * Creation date: Feb 7, 2006.
 * By: Edward Lam
 */
package org.openquark.cal.eclipse.ui.preferences;

import org.eclipse.core.runtime.preferences.AbstractPreferenceInitializer;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.ui.editors.text.EditorsUI;


/**
 * The preference initializer for the CALEclipseUIPlugin.
 * Referenced from the plugin.xml.
 * 
 * @author Edward Lam
 */
public class CALUIPreferenceInitializer extends AbstractPreferenceInitializer {
    /**
     * {@inheritDoc}
     */
    @Override
    public void initializeDefaultPreferences() {
        IPreferenceStore store = PreferenceConstants.getPreferenceStore();
        EditorsUI.useAnnotationsPreferencePage(store);
        EditorsUI.useQuickDiffPreferencePage(store);
        PreferenceConstants.initializeDefaultValues(store);
    }
}
