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
 * EntityArgumentEditorSection.java
 * Created: 20-Feb-07
 * By: Rick Cameron
 */


package org.openquark.cal.eclipse.ui.metadataeditor;

import java.util.ArrayList;
import java.util.List;

import org.openquark.cal.compiler.LanguageInfo;
import org.openquark.cal.compiler.ModuleTypeInfo;
import org.openquark.cal.compiler.PolymorphicVarContext;
import org.openquark.cal.compiler.ScopedEntityNamingPolicy;
import org.openquark.cal.compiler.TypeExpr;
import org.openquark.cal.eclipse.core.CALModelManager;
import org.openquark.cal.metadata.ArgumentMetadata;
import org.openquark.cal.metadata.CALFeatureMetadata;
import org.openquark.cal.metadata.ClassMethodMetadata;
import org.openquark.cal.metadata.FunctionMetadata;
import org.openquark.cal.metadata.FunctionalAgentMetadata;
import org.openquark.cal.metadata.InstanceMethodMetadata;




/**
 * @author rcameron
 *
 */
final class EntityArgumentEditorSection extends EditorSection {

    /** The key for the return value description editor. */
    private static final String RETURN_VALUE_KEY = "returnValue"; //$NON-NLS-1$
    
    /** Whether the entity has a return value description. */
    private final boolean hasReturnValue;

    /**
     * Constructor EntityArgumentEditorSection
     *
     * @param parent
     * @param hasReturnValue 
     */
    public EntityArgumentEditorSection (CALMetadataEditorPanel parent, boolean hasReturnValue) {
        super (parent, 
                hasReturnValue 
                    ? MetadataEditorMessages.GemArgumentsAndReturnValue_Header 
                    : MetadataEditorMessages.GemArguments_Header);
        this.hasReturnValue = hasReturnValue;
    }

    /* (non-Javadoc)
     * @see org.openquark.cal.eclipse.ui.metadataeditor.EditorSection#doRevert()
     */
    @Override
    void doRevert () {
        // Determine the information needed to display argument information
        ArgumentMetadata[] arguments = getArgumentsFromMetadata();
        ArgumentMetadata[] adjusted = getArgumentsFromMetadata();
        
        ModuleTypeInfo moduleTypeInfo = getInput ().getModuleTypeInfo (CALModelManager.getCALModelManager ().getProgramModelManager ());
        ScopedEntityNamingPolicy namingPolicy = new ScopedEntityNamingPolicy.UnqualifiedUnlessAmbiguous (moduleTypeInfo);

        String[] typeStrings = getTypeStrings(namingPolicy);
        
        String returnValueDesc = getReturnValueDescriptionFromMetadata();
        
        adjustArgumentNames(adjusted);
        
        int nNeededArgumentEditors = arguments.length;
        
        List<EditorComponent> editors = getEditors ();
        
        // are we starting from scratch?
        if (editors.size () == 0) {
            for (int i = 0; i < nNeededArgumentEditors; i++) {
                addEditor(new EntityArgumentEditor(this, i, arguments[i], typeStrings[i], adjusted[i].getDisplayName()));
            }
            
            // finally, add the return value editor if necessary
            if (hasReturnValue) {
                addEditor(new EntityReturnValueEditor(this, RETURN_VALUE_KEY, returnValueDesc, typeStrings[typeStrings.length - 1]));
            }
        } else {
            for (int i = 0; i < editors.size (); i++) {
                Object e = editors.get (i);
                
                if (e instanceof EntityArgumentEditor) {
                    EntityArgumentEditor editor = (EntityArgumentEditor)e;
                    
                    editor.refresh (arguments[i], typeStrings[i], adjusted[i].getDisplayName());
                } else if (e instanceof EntityReturnValueEditor) {
                    EntityReturnValueEditor editor = (EntityReturnValueEditor)e;
                    
                    editor.refresh (returnValueDesc, typeStrings[typeStrings.length - 1]);
                }
            }
        }
    }

    /**
     * Method getTypeStrings
     * 
     * @param namingPolicy 
     *
     * @return Returns the Strings that represent the types of the arguments and result of the entity
     */
    private String[] getTypeStrings (ScopedEntityNamingPolicy namingPolicy) {
        TypeExpr typeExpr = getInput ().getTypeExpr ();
        
        if (typeExpr != null) {
            TypeExpr[] typePieces = typeExpr.getTypePieces ();
            String[] typeStrings = new String[typePieces.length];
            
            PolymorphicVarContext polymorphicVarContext = PolymorphicVarContext.make();
            
            for (int i = 0; i < typePieces.length; i++) {
                typeStrings[i] = typePieces[i].toString(polymorphicVarContext, namingPolicy);
            }
            
            return typeStrings;
        }
        
        return null;
    }

    /**
     * Method adjustArgumentNames
     *
     * @param argumentMetadatas
     */
    private void adjustArgumentNames (ArgumentMetadata[] argumentMetadatas) {
        getInput ().adjustArgumentNames (argumentMetadatas);
    }

    /* (non-Javadoc)
     * @see org.openquark.cal.eclipse.ui.metadataeditor.EditorSection#doSave()
     */
    @Override
    void doSave () {
        List<EditorComponent> allEditors = getEditors();
        List<ArgumentMetadata> argMetadata = new ArrayList<ArgumentMetadata>();
        String returnValueDesc = null;
        
        for (int i = 0, size = allEditors.size(); i < size; i++) {
            EditorComponent editor = allEditors.get(i);
            
            if (editor instanceof EntityArgumentEditor) {
                argMetadata.add((ArgumentMetadata)editor.getValue());
            } else if (editor instanceof EntityReturnValueEditor) {
                returnValueDesc = (String)editor.getValue();
            }
        }
        
        setArgumentsIntoMetadata(argMetadata.toArray(new ArgumentMetadata[argMetadata.size()]));
        
        setReturnValueDescriptionIntoMetadata(returnValueDesc);
    }

    
    /** Set the specified arguments into the metadata. */
    private void setArgumentsIntoMetadata(ArgumentMetadata[] argMetadata) {
        if (getMetadata() instanceof InstanceMethodMetadata) {
            ((InstanceMethodMetadata)getMetadata()).setArguments(argMetadata);
        } else {
            ((FunctionalAgentMetadata)getMetadata()).setArguments(argMetadata);
        }
    }
    
    /** Set the specified return value description into the metadata. */
    private void setReturnValueDescriptionIntoMetadata(String returnValueDesc) {
        CALFeatureMetadata metadata = getMetadata();
        if (metadata instanceof FunctionMetadata) {
            ((FunctionMetadata)metadata).setReturnValueDescription(returnValueDesc);
        } else if (metadata instanceof ClassMethodMetadata) {
            ((ClassMethodMetadata)metadata).setReturnValueDescription(returnValueDesc);
        } else if (metadata instanceof InstanceMethodMetadata) {
            ((InstanceMethodMetadata)metadata).setReturnValueDescription(returnValueDesc);
        }
    }
    


    /* (non-Javadoc)
     * @see org.openquark.cal.eclipse.ui.metadataeditor.EditorSection#doValidate()
     */
    @Override
    boolean doValidate () {
        // For arguments the display name has to be a valid CAL identifier.
        List<EditorComponent> allEditors = getEditors();
        
        for (final EditorComponent ithEditor : allEditors) {
            
            if (ithEditor instanceof EntityArgumentEditor) {
                EntityArgumentEditor editor = (EntityArgumentEditor) ithEditor;
                ArgumentMetadata metadata = (ArgumentMetadata) editor.getValue();
                
                if (metadata.getDisplayName() != null &&
                        !LanguageInfo.isValidFunctionName(metadata.getDisplayName())) {
                    
                    setEditorErrorMessage(editor, MetadataEditorMessages.InvalidArgName_Message);
                    return false;
                }
                
                setEditorErrorMessage(editor, null);
                
            } else if (ithEditor instanceof EntityReturnValueEditor) {
                // no need to validate the return value description
            }
        }
        
        return true;
    }

    /** Get the arguments from the metadata. */
    private ArgumentMetadata[] getArgumentsFromMetadata() {
        CALFeatureMetadata metadata = getMetadata();
        
        if (metadata instanceof InstanceMethodMetadata) {
            return ((InstanceMethodMetadata)metadata).getArguments();
        } else if (metadata instanceof FunctionalAgentMetadata) {
            return ((FunctionalAgentMetadata)metadata).getArguments();
        } else {
            return null;
        }
    }
   
    /** Get the return value description from the metadata. */
    private String getReturnValueDescriptionFromMetadata() {
        CALFeatureMetadata metadata = getMetadata();
        
        if (metadata instanceof FunctionMetadata) {
            return ((FunctionMetadata)metadata).getReturnValueDescription();
        } else if (metadata instanceof ClassMethodMetadata) {
            return ((ClassMethodMetadata)metadata).getReturnValueDescription();
        } else if (metadata instanceof InstanceMethodMetadata) {
            return ((InstanceMethodMetadata)metadata).getReturnValueDescription();
        } else {
            return null;
        }
    }

}
