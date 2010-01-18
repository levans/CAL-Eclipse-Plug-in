/*******************************************************************************
 * Copyright (c) 2007 Business Objects Software Limited and others.
 * All rights reserved. 
 * This file is made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Business Objects Software Limited - initial API and implementation
 *     
 *     org/eclipse/ant/internal/ui/editor/templates/BuildFileContextType.java
 *     addGlobalResolvers() method
 *     Eclipse source is available at: http://www.eclipse.org/downloads/
 *******************************************************************************/
package org.openquark.cal.eclipse.ui.templates;

import org.eclipse.jface.text.templates.GlobalTemplateVariables;
import org.eclipse.jface.text.templates.TemplateContextType;

/**
 * The template context that is applicable for all CALDoc regions. 
 * 
 * @author Greg McClement
 */
public class CALDocTemplateContextType extends TemplateContextType {
    public final static String NAME = "CALDoc";

    public final static String ID = "org.openquark.cal.eclipse.ui.CALDocTemplateContext";

    public CALDocTemplateContextType() {
        super();
        addGlobalResolvers();
    }

    /**
     * the default variables that do nice things like add the date, username, or
     * selected line
     */
    private void addGlobalResolvers() {
        addResolver(new GlobalTemplateVariables.Cursor());
        addResolver(new GlobalTemplateVariables.WordSelection());
        addResolver(new GlobalTemplateVariables.LineSelection());
        addResolver(new GlobalTemplateVariables.Dollar());
        addResolver(new GlobalTemplateVariables.Date());
        addResolver(new GlobalTemplateVariables.Year());
        addResolver(new GlobalTemplateVariables.Time());
        addResolver(new GlobalTemplateVariables.User());
    }

}
