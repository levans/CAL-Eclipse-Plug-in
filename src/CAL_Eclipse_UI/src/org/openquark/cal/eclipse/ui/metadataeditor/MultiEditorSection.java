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
/*
 * MultiEditorSection.java
 * Created: 20-Feb-07
 * By: Rick Cameron
 */


package org.openquark.cal.eclipse.ui.metadataeditor;



/**
 * @author rcameron
 *
 */
abstract class MultiEditorSection extends EditorSection {

    /**
     * Constructor MultiEditorSection
     *
     * @param parent
     * @param title 
     */
    public MultiEditorSection (CALMetadataEditorPanel parent, String title) {
        super (parent, title);
    }

}
