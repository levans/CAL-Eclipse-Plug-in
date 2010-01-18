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
 * MetadataEditorMessages.java
 * Created: 21-Feb-07
 * By: Rick Cameron
 */


package org.openquark.cal.eclipse.ui.metadataeditor;

import org.eclipse.osgi.util.NLS;


/**
 * @author rcameron
 *
 */
final class MetadataEditorMessages extends NLS {

    private static final String BUNDLE_NAME = "org.openquark.cal.eclipse.ui.metadataeditor.MetadataEditorMessages";//$NON-NLS-1$

    public static String AddCustomAttributeButtonToolTip;
    public static String DeleteCustomAttributeButtonTooltip;

    public static String ArgumentCaption;
    public static String ClassCaption;
    public static String ConstructorCaption;
    public static String FunctionCaption;
    public static String ModuleCaption;
    public static String TypeCaption;
    public static String UnknownCaption;
    
    public static String RelatedFeatures;
    public static String RelatedFeaturesDescription;

    public static String NameColumnHeading;
    public static String ValueColumnHeading;
    public static String NewItemName;

    public static String InvalidEditorInput_Message;

    public static String CAL_Editor;

    public static String Error_Opening_Editor;

    public static String InvalidAttributeName_Message;

    public static String InvalidValues_Header;
    public static String InvalidValues_Message;

    public static String AddExampleButtonToolTip;

    public static String MoreButtonCaption;

    public static String NewExampleHint;
    public static String RemoveExampleButtonToolTip;

    public static String MoveUpItemButtonToolTip;
    public static String MoveDownItemButtonToolTip;
    
    public static String AddFeatureButtonToolTip;
    public static String RemoveFeatureButtonToolTip;
    public static String MoveUpFeatureButtonToolTip;
    public static String MoveDownFeatureButtonToolTip;

    public static String AutoRunExampleCheckBox;

    public static String Description;

    public static String PromptingExpression;
    public static String PromptingExpressionDescription;

    public static String ExpressionRunInModule;

    public static String DefaultsExpression;
    public static String DefaultsExpressionDescription;

    public static String DefaultsOnly;
    public static String DefaultsOnlyDescription;

    public static String ArgumentProperties_Header;

    public static String ReturnValueIndicator;

    public static String MoveUpItemButtonLabel;
    public static String MoveDownItemButtonLabel;

    public static String ItemFieldToolTip;
    
    public static String AddItemButtonLabel;
    public static String AddItemButtonToolTip;

    public static String RemoveItemButtonLabel;
    public static String RemoveItemButtonToolTip;
    
    public static String YesButtonLabel;
    public static String NoButtonLabel;

    public static String UsageExamples_Header;
    public static String CustomAttributes_Header;
    public static String GemProperties_Header;
    public static String GemArgumentsAndReturnValue_Header;
    public static String GemArguments_Header;
    public static String BasicProperties_Header;

    public static String InvalidArgName_Message;

    public static String VersionDescription;
    public static String Version;
    public static String AuthorDescription;
    public static String Author;
    public static String ShortDescriptionDescription;
    public static String ShortDescription;
    public static String LongDescription;
    public static String LongDescriptionDescription;
    public static String Preferred;
    public static String PreferredDescription;
    public static String Expert;
    public static String ExpertDescription;
    public static String DisplayNameDescription;
    public static String DisplayName;
    public static String Categories;
    public static String CategoriesDescription;
    
    static {
        NLS.initializeMessages(BUNDLE_NAME, MetadataEditorMessages.class);
    }
    
    /**
     * Constructor MetadataEditorMessages
     *
     * Non-instantiable
     */
    private MetadataEditorMessages () {}
}
