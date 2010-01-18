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
 * ArgumentEditorSection.java
 * Created: 6-Mar-07
 * By: Rick Cameron
 */


package org.openquark.cal.eclipse.ui.metadataeditor;

import org.openquark.cal.metadata.ArgumentMetadata;
import org.openquark.cal.metadata.CALExpression;


/**
 * @author rcameron
 *
 */
final class ArgumentEditorSection extends EditorSection {

    /* Keys for the editors. */
    private static final String DEFAULTS_ONLY_KEY = "defaultsOnly"; //$NON-NLS-1$
    private static final String DEFAULTS_EXPRESSION_KEY = "defaultsExpression"; //$NON-NLS-1$
    private static final String PROMPTING_EXPRESSION_KEY = "promptingExpression"; //$NON-NLS-1$

    /**
     * Constructor ArgumentEditorSection
     *
     * @param editorPanel
     */
    public ArgumentEditorSection (CALMetadataEditorPanel editorPanel) {
        super (editorPanel, MetadataEditorMessages.ArgumentProperties_Header);
        addEditor(new BooleanEditor   (this, DEFAULTS_ONLY_KEY,        MetadataEditorMessages.DefaultsOnly,        MetadataEditorMessages.DefaultsOnlyDescription));
        addEditor(new ExpressionEditor(this, DEFAULTS_EXPRESSION_KEY,  MetadataEditorMessages.DefaultsExpression,  MetadataEditorMessages.DefaultsExpressionDescription));
        addEditor(new ExpressionEditor(this, PROMPTING_EXPRESSION_KEY,  MetadataEditorMessages.PromptingExpression,  MetadataEditorMessages.PromptingExpressionDescription));
    }

    /**
     * @see org.openquark.cal.eclipse.ui.metadataeditor.EditorSection#doRevert()
     */
    @Override
    void doRevert () {
        ArgumentMetadata metadata = (ArgumentMetadata)getMetadata ();

        setEditorValue (DEFAULTS_ONLY_KEY, new Boolean (metadata.useDefaultValuesOnly ()));
        setEditorValue (DEFAULTS_EXPRESSION_KEY, metadata.getDefaultValuesExpression ());
        setEditorValue (PROMPTING_EXPRESSION_KEY, metadata.getPromptingTextExpression ());
    }

    /**
     * @see org.openquark.cal.eclipse.ui.metadataeditor.EditorSection#doSave()
     */
    @Override
    void doSave () {
        ArgumentMetadata metadata = (ArgumentMetadata) getMetadata();
        metadata.setDefaultValuesOnly(((Boolean) getEditorValue(DEFAULTS_ONLY_KEY)).booleanValue());
        metadata.setDefaultValuesExpression ((CALExpression)getEditorValue (DEFAULTS_EXPRESSION_KEY));
        metadata.setPromptingTextExpression ((CALExpression)getEditorValue (PROMPTING_EXPRESSION_KEY));
    }

    /**
     * @see org.openquark.cal.eclipse.ui.metadataeditor.EditorSection#doValidate()
     */
    @Override
    boolean doValidate () {
        return true;
    }

}
