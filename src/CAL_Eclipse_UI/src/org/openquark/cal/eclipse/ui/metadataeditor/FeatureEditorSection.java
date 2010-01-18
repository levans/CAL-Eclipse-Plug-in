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
 * FeatureEditorSection.java
 * Created: 20-Feb-07
 * By: Rick Cameron
 */


package org.openquark.cal.eclipse.ui.metadataeditor;

import java.util.ArrayList;
import java.util.List;

import org.openquark.cal.compiler.LanguageInfo;
import org.openquark.cal.metadata.ArgumentMetadata;
import org.openquark.cal.metadata.CALFeatureMetadata;
import org.openquark.cal.services.CALFeatureName;
import org.openquark.util.UnsafeCast;




/**
 * @author Rick Cameron
 *
 */
final class FeatureEditorSection extends EditorSection {

    /* Keys for the editors. */
    private static final String DISPLAY_NAME_KEY = "displayName"; //$NON-NLS-1$
    private static final String SHORT_DESCRIPTION_KEY = "shortDescription"; //$NON-NLS-1$
    private static final String LONG_DESCRIPTION_KEY = "longDescription"; //$NON-NLS-1$
    private static final String AUTHOR_KEY = "author"; //$NON-NLS-1$
    private static final String VERSION_KEY = "version"; //$NON-NLS-1$
    private static final String PREFERRED_KEY = "preferred"; //$NON-NLS-1$
    private static final String EXPERT_KEY = "expert"; //$NON-NLS-1$
    private static final String RELATED_FEATURES_KEY = "relatedFeatures"; //$NON-NLS-1$
    
    /**
     * Constructor FeatureEditorSection
     *
     * @param parent
     */
    public FeatureEditorSection (CALMetadataEditorPanel parent) {
        super (parent, MetadataEditorMessages.BasicProperties_Header);
        
        // Add editor components for the basic feature metadata items
        addEditor(new TextFieldEditor(this, DISPLAY_NAME_KEY,      MetadataEditorMessages.DisplayName,      MetadataEditorMessages.DisplayNameDescription));
        addEditor(new TextFieldEditor(this, SHORT_DESCRIPTION_KEY, MetadataEditorMessages.ShortDescription, MetadataEditorMessages.ShortDescriptionDescription));
        addEditor(new TextAreaEditor (this, LONG_DESCRIPTION_KEY,  MetadataEditorMessages.LongDescription,  MetadataEditorMessages.LongDescriptionDescription));
        addEditor(new TextFieldEditor(this, AUTHOR_KEY,            MetadataEditorMessages.Author,           MetadataEditorMessages.AuthorDescription));
        addEditor(new TextFieldEditor(this, VERSION_KEY,           MetadataEditorMessages.Version,          MetadataEditorMessages.VersionDescription));
        addEditor(new BooleanEditor  (this, PREFERRED_KEY,         MetadataEditorMessages.Preferred,        MetadataEditorMessages.PreferredDescription));
        addEditor(new BooleanEditor  (this, EXPERT_KEY,            MetadataEditorMessages.Expert,           MetadataEditorMessages.ExpertDescription));
        addEditor(new FeaturesEditor (this, RELATED_FEATURES_KEY,  MetadataEditorMessages.RelatedFeatures,  MetadataEditorMessages.RelatedFeaturesDescription));
    }

    /* (non-Javadoc)
     * @see org.openquark.cal.eclipse.ui.metadataeditor.EditorSection#doRevert()
     */
    @Override
    void doRevert () {
        CALFeatureMetadata metadata = getMetadata();
        
        setEditorValue(DISPLAY_NAME_KEY,      metadata.getDisplayName());
        setEditorValue(SHORT_DESCRIPTION_KEY, metadata.getShortDescription());
        setEditorValue(LONG_DESCRIPTION_KEY,  metadata.getLongDescription());
        setEditorValue(AUTHOR_KEY,            metadata.getAuthor());
        setEditorValue(VERSION_KEY,           metadata.getVersion());
        setEditorValue(PREFERRED_KEY,         new Boolean(metadata.isPreferred()));
        setEditorValue(EXPERT_KEY,            new Boolean(metadata.isExpert()));
        
        List<CALFeatureName> relatedFeatures = new ArrayList<CALFeatureName>();
        int count = metadata.getNRelatedFeatures();
        for (int i = 0; i < count; i++) {
            relatedFeatures.add(metadata.getNthRelatedFeature(i));
        }
        setEditorValue(RELATED_FEATURES_KEY, relatedFeatures);
    }

    /* (non-Javadoc)
     * @see org.openquark.cal.eclipse.ui.metadataeditor.EditorSection#doSave()
     */
    @Override
    void doSave () {
        CALFeatureMetadata metadata = getMetadata();
        
        metadata.setDisplayName((String) getEditorValue(DISPLAY_NAME_KEY));
        metadata.setShortDescription((String) getEditorValue(SHORT_DESCRIPTION_KEY));
        metadata.setLongDescription((String) getEditorValue(LONG_DESCRIPTION_KEY));
        metadata.setAuthor((String) getEditorValue(AUTHOR_KEY));
        metadata.setVersion((String) getEditorValue(VERSION_KEY));

        Boolean preferred = (Boolean) getEditorValue(PREFERRED_KEY);
        metadata.setPreferred(preferred.booleanValue());
        
        Boolean expert = (Boolean) getEditorValue(EXPERT_KEY);
        metadata.setExpert(expert.booleanValue());
        
        metadata.clearRelatedFeatures();
        List<CALFeatureName> relatedFeatures = UnsafeCast.unsafeCast(getEditorValue(RELATED_FEATURES_KEY));
        for (final CALFeatureName relatedFeature : relatedFeatures) {
            metadata.addRelatedFeature(relatedFeature);
        }
    }

    /* (non-Javadoc)
     * @see org.openquark.cal.eclipse.ui.metadataeditor.EditorSection#doValidate()
     */
    @Override
    boolean doValidate () {
        // For arguments the display name has to be a valid CAL identifier.
        if (getMetadata() instanceof ArgumentMetadata) {

            EditorComponent editor = getEditor(DISPLAY_NAME_KEY);
            String displayName = (String) editor.getValue();            

            if (displayName != null && !LanguageInfo.isValidFunctionName(displayName)) {
                    
                setEditorErrorMessage(editor, MetadataEditorMessages.InvalidArgName_Message);
                return false;
            }
            
            setEditorErrorMessage(editor, null);
        }
        
        return true;
    }

}
