/*******************************************************************************
 * Copyright (c) 2007 Business Objects Software Limited and others.
 * All rights reserved. 
 * This file is made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Business Objects Software Limited - initial API and implementation
 *******************************************************************************/
package org.openquark.cal.eclipse.ui.preferences;

import org.eclipse.ui.texteditor.templates.TemplatePreferencePage;
import org.openquark.cal.eclipse.ui.CALEclipseUIPlugin;

/**
 * Uses the standard template page, and is configured for CAL
 * 
 * @author Andrew Eisenberg
 */
public class CALTemplatePreferencePage extends TemplatePreferencePage {
  public CALTemplatePreferencePage() {
    setTemplateStore(CALEclipseUIPlugin.getDefault().getTemplateStore());
    setContextTypeRegistry(CALEclipseUIPlugin.getDefault().getTemplateContextRegistry());
    setPreferenceStore(CALEclipseUIPlugin.getDefault().getPreferenceStore());
  }
}
