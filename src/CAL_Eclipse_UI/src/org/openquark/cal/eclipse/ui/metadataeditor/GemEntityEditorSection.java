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
 * GemEntityEditorSection.java
 * Created: 20-Feb-07
 * By: Rick Cameron
 */


package org.openquark.cal.eclipse.ui.metadataeditor;

import java.util.Arrays;
import java.util.List;

import org.openquark.cal.metadata.FunctionalAgentMetadata;
import org.openquark.cal.metadata.InstanceMethodMetadata;
import org.openquark.util.UnsafeCast;



/**
 * @author rcameron
 *
 */
final class GemEntityEditorSection extends EditorSection {

    /* Keys for the editors. */
    private static final String CATEGORIES_KEY = "categories"; //$NON-NLS-1$
    
    /**
     * Constructor GemEntityEditorSection
     *
     * @param parent
     */
    public GemEntityEditorSection (CALMetadataEditorPanel parent) {
        super (parent, MetadataEditorMessages.GemProperties_Header);
        
        addEditor(new ListEditor(this, CATEGORIES_KEY, MetadataEditorMessages.Categories, MetadataEditorMessages.CategoriesDescription));
    }

    /* (non-Javadoc)
     * @see org.openquark.cal.eclipse.ui.metadataeditor.EditorSection#doRevert()
     */
    @Override
    void doRevert () {
        List<String> categories = Arrays.asList(getCategoriesFromMetadata());
        setEditorValue(CATEGORIES_KEY, categories);
    }

    /* (non-Javadoc)
     * @see org.openquark.cal.eclipse.ui.metadataeditor.EditorSection#doSave()
     */
    @Override
    void doSave () {
        List<String> categories = UnsafeCast.unsafeCast(getEditorValue(CATEGORIES_KEY));
        String[] catArray = new String[categories.size()];
        
        for (int i = 0; i < catArray.length; i++) {
            catArray[i] = categories.get(i);
        }
        
        setCategoriesIntoMetadata(catArray);
    }

    /* (non-Javadoc)
     * @see org.openquark.cal.eclipse.ui.metadataeditor.EditorSection#doValidate()
     */
    @Override
    boolean doValidate () {
        return true;
    }

    /** Set the specified categories into the metadata. */
    private void setCategoriesIntoMetadata(String[] catArray) {
        if (getMetadata() instanceof InstanceMethodMetadata) {
            ((InstanceMethodMetadata)getMetadata()).setCategories(catArray);
        } else {
            ((FunctionalAgentMetadata)getMetadata()).setCategories(catArray);
        }
    }

    /** Get the categories from the metadata. */
    private String[] getCategoriesFromMetadata() {
        if (getMetadata() instanceof InstanceMethodMetadata) {
            return ((InstanceMethodMetadata)getMetadata()).getCategories();
        } else {
            return ((FunctionalAgentMetadata)getMetadata()).getCategories();
        }
    }
}
