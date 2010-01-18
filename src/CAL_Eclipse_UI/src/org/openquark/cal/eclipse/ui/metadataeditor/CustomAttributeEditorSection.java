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
 * CustomAttributeEditorSection.java
 * Created: 20-Feb-07
 * By: Rick Cameron
 */


package org.openquark.cal.eclipse.ui.metadataeditor;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.openquark.cal.metadata.CALFeatureMetadata;
import org.openquark.util.UnsafeCast;



/**
 * @author rcameron
 *
 */
final class CustomAttributeEditorSection extends EditorSection {
    
    private static final String ATTRIBUTES_KEY = "attributes";  //$NON-NLS-1$
    
    /**
     * Constructor CustomAttributeEditorSection
     *
     * @param parent
     */
    public CustomAttributeEditorSection (CALMetadataEditorPanel parent) {
        super (parent, MetadataEditorMessages.CustomAttributes_Header);
        
        addEditor (new CustomAttributeEditor (this, ATTRIBUTES_KEY, null, null));
    }

    /* (non-Javadoc)
     * @see org.openquark.cal.eclipse.ui.metadataeditor.EditorSection#doRevert()
     */
    @Override
    void doRevert () {
        List<String[]> attributes = new ArrayList<String[]> ();
        
        CALFeatureMetadata metadata = getMetadata ();
        
        for (Iterator<String> iter = metadata.getAttributeNames(); iter.hasNext(); ) {
            String name = iter.next();
            
            String value = metadata.getAttribute (name);
            
            attributes.add (new String[] { name, value });
        }
        
        setEditorValue (ATTRIBUTES_KEY, attributes);
    }

    /* (non-Javadoc)
     * @see org.openquark.cal.eclipse.ui.metadataeditor.EditorSection#doSave()
     */
    @Override
    void doSave () {
        List<String[]> attributes = UnsafeCast.unsafeCast(getEditorValue(ATTRIBUTES_KEY));

        CALFeatureMetadata metadata = getMetadata();
        
        metadata.clearAttributes ();
        
        for (final String[] element : attributes) {
            metadata.setAttribute(element[0], element[1]);
        }
    }

    /* (non-Javadoc)
     * @see org.openquark.cal.eclipse.ui.metadataeditor.EditorSection#doValidate()
     */
    @Override
    boolean doValidate () {
        List<String[]> attributes = UnsafeCast.unsafeCast(getEditorValue (ATTRIBUTES_KEY));
        
        Set<String> names = new HashSet<String> ();
        
        for (final String[] element : attributes) {
            String name = element[0];
            
            if (names.contains (name)) {
                setEditorErrorMessage (getEditor (ATTRIBUTES_KEY), MetadataEditorMessages.InvalidAttributeName_Message);
                
                return false;
            
            } else {
                names.add (name);
            }
        }
        
        setEditorErrorMessage (getEditor (ATTRIBUTES_KEY), null);
        
        return true;
    }

}
