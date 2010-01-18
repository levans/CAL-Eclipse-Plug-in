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
 * CALMetadataEditor.java
 * Created: 19-Feb-07
 * By: Rick Cameron
 */


package org.openquark.cal.eclipse.ui.metadataeditor;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.text.MessageFormat;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorSite;
import org.eclipse.ui.IPersistableElement;
import org.eclipse.ui.part.EditorPart;
import org.openquark.cal.compiler.CALDocComment;
import org.openquark.cal.compiler.FieldName;
import org.openquark.cal.compiler.FunctionalAgent;
import org.openquark.cal.compiler.ModuleName;
import org.openquark.cal.compiler.ModuleTypeInfo;
import org.openquark.cal.compiler.QualifiedName;
import org.openquark.cal.compiler.ScopedEntity;
import org.openquark.cal.compiler.TypeExpr;
import org.openquark.cal.eclipse.core.CALModelManager;
import org.openquark.cal.metadata.ArgumentMetadata;
import org.openquark.cal.metadata.CALFeatureMetadata;
import org.openquark.cal.metadata.FunctionalAgentMetadata;
import org.openquark.cal.services.CALFeatureName;
import org.openquark.cal.services.LocaleUtilities;
import org.openquark.cal.services.ProgramModelManager;
import org.openquark.cal.services.Status;



/**
 * @author rcameron
 *
 */
public final class CALMetadataEditor extends EditorPart {
    
    public static final String ID = "org.openquark.cal.eclipse.ui.metadataeditor.CALMetadataEditor"; //$NON-NLS-1$
    
    public static abstract class Input implements IEditorInput {
        
        public static Input getInstance (ModuleName moduleName) {
            return new ModuleInput (moduleName);
        }
        
        public static Input getInstance (ScopedEntity scopedEntity) {
            return new ScopedEntityInput (scopedEntity);
        }
        
        protected Input () {}

        /* (non-Javadoc)
         * @see org.eclipse.ui.IEditorInput#exists()
         */
        public boolean exists () {
            // TODO Auto-generated method stub
            return false;
        }

        /* (non-Javadoc)
         * @see org.eclipse.ui.IEditorInput#getImageDescriptor()
         */
        public ImageDescriptor getImageDescriptor () {
            // TODO Auto-generated method stub
            return null;
        }

        /* (non-Javadoc)
         * @see org.eclipse.ui.IEditorInput#getPersistable()
         */
        public IPersistableElement getPersistable () {
            // TODO Auto-generated method stub
            return null;
        }

        /**
         * {@inheritDoc}
         */
        @SuppressWarnings("unchecked")
        public Object getAdapter (Class adapter) {
            // TODO Auto-generated method stub
            return null;
        }

        /**
         * Method getMetadata
         *
         * @return Returns {@link CALFeatureMetadata} or null
         */
        public abstract CALFeatureMetadata getMetadata ();

        /**
         * Method saveMetadata
         *
         * @param metadata
         * @param saveStatus
         * 
         * @return Returns true iff saving succeeded
         */
        public boolean saveMetadata (CALFeatureMetadata metadata, Status saveStatus) {
            return CALModelManager.getCALModelManager ().saveMetadata (metadata, saveStatus);
        }
        
        /**
         * Method adjustArgumentNames
         * 
         * @param argumentMetadatas
         */
        public abstract void adjustArgumentNames (ArgumentMetadata[] argumentMetadatas);
        
        /**
         * Method getTypeExpr
         *
         * @return Returns the TypeExpr for the input, or null
         */
        public abstract TypeExpr getTypeExpr ();

        /**
         * Method getModuleTypeInfo
         * @param programModelManager 
         *
         * @return Returns {@link ModuleTypeInfo} for the module that the entity lives in
         */
        public abstract ModuleTypeInfo getModuleTypeInfo (ProgramModelManager programModelManager);

        /**
         * Method featureNameIs
         *
         * @param featureName
         * @return Returns true if the given name matches this input
         */
        public abstract boolean featureNameIs (CALFeatureName featureName);

        /**
         * Method makeArgumentInput
         *
         * @param argumentNumber
         * @return Returns an Input representing the given argument in the current Input
         */
        public abstract Input makeArgumentInput (int argumentNumber);
        
    }
    
    private static final class ModuleInput extends Input {
        
        private final ModuleName moduleName;

        /**
         * Constructor CALMetadataEditor.ModuleInput
         * @param moduleName 
         *
         */
        ModuleInput (ModuleName moduleName) {
            this.moduleName = moduleName;
        }
        
        /* (non-Javadoc)
         * @see java.lang.Object#hashCode()
         */
        @Override
        public int hashCode () {
            final int PRIME = 31;
            int result = 1;
            result = PRIME * result + ((moduleName == null) ? 0 : moduleName.hashCode ());
            return result;
        }

        /* (non-Javadoc)
         * @see java.lang.Object#equals(java.lang.Object)
         */
        @Override
        public boolean equals (Object obj) {
            if (this == obj) {
                return true;
            }
            if (obj == null) {
                return false;
            }
            if (getClass () != obj.getClass ()) {
                return false;
            }
            final ModuleInput other = (ModuleInput)obj;
            if (moduleName == null) {
                if (other.moduleName != null) {
                    return false;
                }
            } else if (!moduleName.equals (other.moduleName)) {
                return false;
            }
            return true;
        }

        /* (non-Javadoc)
         * @see org.eclipse.ui.IEditorInput#getName()
         */
        public String getName () {
            return moduleName.toString ();
        }

        /* (non-Javadoc)
         * @see org.eclipse.ui.IEditorInput#getToolTipText()
         */
        public String getToolTipText () {
            // TODO rbc - something better?
            return getName ();
        }

        /**
         * @see org.openquark.cal.eclipse.ui.metadataeditor.CALMetadataEditor.Input#getMetadata()
         */
        @Override
        public CALFeatureMetadata getMetadata () {
            CALFeatureName featureName = CALFeatureName.getModuleFeatureName (moduleName);
            return CALModelManager.getCALModelManager ().getMetadata (featureName, getMetadataLocale ());
        }

        /**
         * @see org.openquark.cal.eclipse.ui.metadataeditor.CALMetadataEditor.Input#adjustArgumentNames(org.openquark.cal.metadata.ArgumentMetadata[])
         */
        @Override
        public void adjustArgumentNames (ArgumentMetadata[] argumentMetadatas) {
            // nothing to do
        }

        /**
         * @see org.openquark.cal.eclipse.ui.metadataeditor.CALMetadataEditor.Input#getTypeExpr()
         */
        @Override
        public TypeExpr getTypeExpr () {
            // Not applicable
            return null;
        }

        /**
         * @see org.openquark.cal.eclipse.ui.metadataeditor.CALMetadataEditor.Input#getModuleTypeInfo(ProgramModelManager)
         */
        @Override
        public ModuleTypeInfo getModuleTypeInfo (ProgramModelManager programModelManager) {
            return programModelManager.getModuleTypeInfo (moduleName);
        }

        /**
         * @see org.openquark.cal.eclipse.ui.metadataeditor.CALMetadataEditor.Input#featureNameIs(org.openquark.cal.services.CALFeatureName)
         */
        @Override
        public boolean featureNameIs (CALFeatureName featureName) {
            return featureName.equals (CALFeatureName.getModuleFeatureName (moduleName));
        }

        /**
         * @see org.openquark.cal.eclipse.ui.metadataeditor.CALMetadataEditor.Input#makeArgumentInput(int)
         */
        @Override
        public Input makeArgumentInput (int argumentNumber) {
            // not applicable
            return null;
        }

    }
    
    private static final class ScopedEntityInput extends Input {
        
        private final ScopedEntity scopedEntity;

        /**
         * Constructor CALMetadataEditor.ScopedEntityInput
         *
         */
        ScopedEntityInput (ScopedEntity scopedEntity) {
            this.scopedEntity = scopedEntity;
        }
        
        /* (non-Javadoc)
         * @see java.lang.Object#hashCode()
         */
        @Override
        public int hashCode () {
            final int PRIME = 31;
            int result = 1;
            result = PRIME * result + ((scopedEntity == null) ? 0 : scopedEntity.hashCode ());
            return result;
        }

        /* (non-Javadoc)
         * @see java.lang.Object#equals(java.lang.Object)
         */
        @Override
        public boolean equals (Object obj) {
            if (this == obj) {
                return true;
            }
            if (obj == null) {
                return false;
            }
            if (getClass () != obj.getClass ()) {
                return false;
            }
            final ScopedEntityInput other = (ScopedEntityInput)obj;
            if (scopedEntity == null) {
                if (other.scopedEntity != null) {
                    return false;
                }
            } else if (!scopedEntity.equals (other.scopedEntity)) {
                return false;
            }
            return true;
        }

        /* (non-Javadoc)
         * @see org.eclipse.ui.IEditorInput#getName()
         */
        public String getName () {
            QualifiedName qualifiedName = scopedEntity.getName ();
            ModuleName moduleName = qualifiedName.getModuleName ();
            
            return MessageFormat.format ("{0}.{1}", new Object [] {moduleName.getLastComponent (), qualifiedName.getUnqualifiedName () } ); //$NON-NLS-1$
        }

        /* (non-Javadoc)
         * @see org.eclipse.ui.IEditorInput#getToolTipText()
         */
        public String getToolTipText () {
            // TODO rbc - something better?
            return getName ();
        }

        /**
         * @see org.openquark.cal.eclipse.ui.metadataeditor.CALMetadataEditor.Input#getMetadata()
         */
        @Override
        public CALFeatureMetadata getMetadata () {
            return CALModelManager.getCALModelManager ().getMetadata (scopedEntity, getMetadataLocale ());
        }

        /**
         * @see org.openquark.cal.eclipse.ui.metadataeditor.CALMetadataEditor.Input#adjustArgumentNames(org.openquark.cal.metadata.ArgumentMetadata[])
         */
        @Override
        public void adjustArgumentNames (ArgumentMetadata[] argumentMetadatas) {
            if (scopedEntity instanceof FunctionalAgent) {
                adjustArgumentNames ((FunctionalAgent)scopedEntity, argumentMetadatas);
            }
            
            // otherwise, nothing to do
        }
        
        /**
         * Method adjustArgumentNames
         * 
         * @param entity
         * @param arguments
         */
        private static void adjustArgumentNames(FunctionalAgent entity, ArgumentMetadata[] arguments) {
            adjustArgumentNames(entity, entity.getCALDocComment(), arguments);
        }
        
        /**
         * Method adjustArgumentNames
         * 
         * @param entity
         * @param caldoc
         * @param arguments
         */
        private static void adjustArgumentNames(FunctionalAgent entity, CALDocComment caldoc, ArgumentMetadata[] arguments) {
            
            int numNames = entity.getNArgumentNames();
            String[] codeNames = new String[numNames];
            
            for (int i = 0; i < numNames; i++) {
                codeNames[i] = entity.getArgumentName(i);
            }
            
            adjustArgumentNames(codeNames, true, caldoc, arguments);
        }
        
        /**
         * Assigns default argument display names to the arguments that do not have a display
         * name. Also disambiguates display names of the arguments.
         * @param originalNames original names that can be used if there's no existing name
         * @param originalNamesFromEntity whether the original names came from the gem entity
         * @param caldoc the CALDoc that may contain '@arg' blocks declaring argument names, or null if there is none.
         * @param arguments array of argument metadata for which to adjust the display names
         */
        private static void adjustArgumentNames(String[] originalNames, boolean originalNamesFromEntity, CALDocComment caldoc, ArgumentMetadata[] arguments) {
            
            Map<String, Integer> argNameToFrequencyMap = new HashMap<String, Integer>();
            Map<String, Integer> argNameToSuffixMap = new HashMap<String, Integer>();

            // Assign the original and default names and count the frequency of each argument name.
            for (int i = 0; i < arguments.length; i++) {

                String argName = arguments[i].getDisplayName();
                
                if (argName == null) {
                    
                    String nameFromCALDoc = null;
                    if (caldoc != null && i < caldoc.getNArgBlocks()) {
                        FieldName argNameAsFieldName = caldoc.getNthArgBlock(i).getArgName();
                        if (argNameAsFieldName instanceof FieldName.Textual) {
                            nameFromCALDoc = argNameAsFieldName.getCalSourceForm();
                        }
                    }
                    
                    String originalName = null;
                    if (i < originalNames.length) {
                        originalName = originalNames[i];
                    }
                    
                    if (originalNamesFromEntity) {
                        // if the CALDoc name is present, we will use it instead of the name from the gem entity
                        // since the CALDoc name is specified by the user, where the name from the gem entity
                        // may have come from an automatically extracted parameter name for a foreign function
                        
                        if (nameFromCALDoc != null) {
                            argName = nameFromCALDoc;
                        } else if (originalName != null) {
                            argName = originalName;
                        } else {
                            argName = ArgumentMetadata.DEFAULT_ARGUMENT_NAME;
                        }
                    } else {
                        // since the original names do not come from the gem entity,
                        // they take precedence over the names in the CALDoc
                        
                        if (originalName != null) {
                            argName = originalName;
                        } else if (nameFromCALDoc != null) {
                            argName = nameFromCALDoc;
                        } else {
                            argName = ArgumentMetadata.DEFAULT_ARGUMENT_NAME;
                        }
                    }

                    arguments[i].setDisplayName(argName);
                }
                            
                Integer frequency = argNameToFrequencyMap.get(argName);
                frequency = frequency != null ? Integer.valueOf(frequency.intValue() + 1) : Integer.valueOf(1);
                argNameToFrequencyMap.put(argName, frequency);
            }
            
            // Disambiguate the names that occur more than once.
            for (final ArgumentMetadata argumentMetadata : arguments) {

                String argName = argumentMetadata.getDisplayName();
                int frequency = argNameToFrequencyMap.get(argName).intValue();
                
                if (frequency > 1) {

                    Integer suffix = argNameToSuffixMap.get(argName);
                    suffix = suffix != null ? Integer.valueOf(suffix.intValue() + 1) : Integer.valueOf(1);
                    argNameToSuffixMap.put(argName, suffix);
                    
                    argName = argName + "_" + suffix; //$NON-NLS-1$
                    argumentMetadata.setDisplayName(argName);
                }
            }
        }
        
        /**
         * @see org.openquark.cal.eclipse.ui.metadataeditor.CALMetadataEditor.Input#getTypeExpr()
         */
        @Override
        public TypeExpr getTypeExpr () {
            if (scopedEntity instanceof FunctionalAgent) {
                return ((FunctionalAgent)scopedEntity).getTypeExpr ();
            }
            
            return null;
        }

        /**
         * @see org.openquark.cal.eclipse.ui.metadataeditor.CALMetadataEditor.Input#getModuleTypeInfo(ProgramModelManager)
         */
        @Override
        public ModuleTypeInfo getModuleTypeInfo (ProgramModelManager programModelManager) {
            QualifiedName qualifiedName = scopedEntity.getName ();
            ModuleName moduleName = qualifiedName.getModuleName ();
            
            return programModelManager.getModuleTypeInfo (moduleName);
        }

        /**
         * @see org.openquark.cal.eclipse.ui.metadataeditor.CALMetadataEditor.Input#featureNameIs(org.openquark.cal.services.CALFeatureName)
         */
        @Override
        public boolean featureNameIs (CALFeatureName featureName) {
            return featureName.equals (CALFeatureName.getScopedEntityFeatureName(scopedEntity));
        }

        /**
         * @see org.openquark.cal.eclipse.ui.metadataeditor.CALMetadataEditor.Input#makeArgumentInput(int)
         */
        @Override
        public Input makeArgumentInput (int argumentNumber) {
            if (scopedEntity instanceof FunctionalAgent) {
                return new ArgumentInput (((FunctionalAgent)scopedEntity), argumentNumber);
            }
            
            return null;
        }

    }
    
    private static final class ArgumentInput extends Input {
        
        private final FunctionalAgent functionalAgent;
        
        private final int argumentNumber;
        
        /**
         * Constructor ArgumentInput
         *
         * @param functionalAgent
         * @param argumentNumber
         */
        ArgumentInput (final FunctionalAgent functionalAgent, final int argumentNumber) {
            this.functionalAgent = functionalAgent;
            this.argumentNumber = argumentNumber;
        }

        /**
         * @see java.lang.Object#hashCode()
         */
        @Override
        public int hashCode () {
            final int PRIME = 31;
            int result = 1;
            result = PRIME * result + argumentNumber;
            result = PRIME * result + ((functionalAgent == null) ? 0 : functionalAgent.hashCode ());
            return result;
        }

        /**
         * @see java.lang.Object#equals(java.lang.Object)
         */
        @Override
        public boolean equals (Object obj) {
            if (this == obj) {
                return true;
            }
            if (obj == null) {
                return false;
            }
            if (getClass () != obj.getClass ()) {
                return false;
            }
            final ArgumentInput other = (ArgumentInput)obj;
            if (argumentNumber != other.argumentNumber) {
                return false;
            }
            if (functionalAgent == null) {
                if (other.functionalAgent != null) {
                    return false;
                }
            } else if (!functionalAgent.equals (other.functionalAgent)) {
                return false;
            }
            return true;
        }


        /**
         * @see org.openquark.cal.eclipse.ui.metadataeditor.CALMetadataEditor.Input#adjustArgumentNames(org.openquark.cal.metadata.ArgumentMetadata[])
         */
        @Override
        public void adjustArgumentNames (ArgumentMetadata[] argumentMetadatas) {
            // nothing to do
        }

        /**
         * @see org.openquark.cal.eclipse.ui.metadataeditor.CALMetadataEditor.Input#getMetadata()
         */
        @Override
        public CALFeatureMetadata getMetadata () {
            FunctionalAgentMetadata functionalAgentMetadata = (FunctionalAgentMetadata)CALModelManager.getCALModelManager ().getMetadata (functionalAgent, getMetadataLocale ());
            
            ArgumentMetadata[] arguments = functionalAgentMetadata.getArguments ();
            
            if (arguments != null && arguments.length > argumentNumber) {
                return arguments[argumentNumber];
            }
            
            return null;
        }
        
        /**
         * @see org.openquark.cal.eclipse.ui.metadataeditor.CALMetadataEditor.Input#saveMetadata(org.openquark.cal.metadata.CALFeatureMetadata, org.openquark.cal.services.Status)
         */
        @Override
        public boolean saveMetadata (CALFeatureMetadata metadata, Status saveStatus) {
            if (!(metadata instanceof ArgumentMetadata)) {
                return false;
            }
            
            FunctionalAgentMetadata functionalAgentMetadata = (FunctionalAgentMetadata)CALModelManager.getCALModelManager ().getMetadata (functionalAgent, getMetadataLocale ());
            
            ArgumentMetadata[] arguments = functionalAgentMetadata.getArguments ();
            
            if (arguments != null && arguments.length > argumentNumber) {
                arguments[argumentNumber] = (ArgumentMetadata)metadata;
                
                functionalAgentMetadata.setArguments (arguments);
                
                return super.saveMetadata (functionalAgentMetadata, saveStatus);
            }
                
            return false;
        }
        
        /**
         * @see org.openquark.cal.eclipse.ui.metadataeditor.CALMetadataEditor.Input#getModuleTypeInfo(org.openquark.cal.services.ProgramModelManager)
         */
        @Override
        public ModuleTypeInfo getModuleTypeInfo (ProgramModelManager programModelManager) {
            QualifiedName qualifiedName = functionalAgent.getName ();
            ModuleName moduleName = qualifiedName.getModuleName ();
            
            return programModelManager.getModuleTypeInfo (moduleName);
        }

        /**
         * @see org.openquark.cal.eclipse.ui.metadataeditor.CALMetadataEditor.Input#getTypeExpr()
         */
        @Override
        public TypeExpr getTypeExpr () {
            TypeExpr[] typePieces = functionalAgent.getTypeExpr ().getTypePieces ();
            
            // typePieces should have one element for each argument and one for the return type
            if (typePieces.length > argumentNumber + 1) {
                return typePieces[argumentNumber];
            }
            
            return null;
        }

        /**
         * @see org.openquark.cal.eclipse.ui.metadataeditor.CALMetadataEditor.Input#featureNameIs(org.openquark.cal.services.CALFeatureName)
         */
        @Override
        public boolean featureNameIs (CALFeatureName featureName) {
            return featureName.equals (CALFeatureName.getScopedEntityFeatureName(functionalAgent));
        }

        /**
         * @see org.eclipse.ui.IEditorInput#getName()
         */
        public String getName () {
            QualifiedName qualifiedName = functionalAgent.getName ();
            ModuleName moduleName = qualifiedName.getModuleName ();
            
            return MessageFormat.format ("{0}.{1} - {2}",  //$NON-NLS-1$
                    new Object [] { 
                        moduleName.getLastComponent (), 
                        qualifiedName.getUnqualifiedName (),
                        functionalAgent.getArgumentName (argumentNumber)
                    });
        }

        /**
         * @see org.eclipse.ui.IEditorInput#getToolTipText()
         */
        public String getToolTipText () {
            // TODO rbc: something better?
            return getName ();
        }

        /**
         * @see org.openquark.cal.eclipse.ui.metadataeditor.CALMetadataEditor.Input#makeArgumentInput(int)
         */
        @Override
        public Input makeArgumentInput (int argumentNumber) {
            return new ArgumentInput (functionalAgent, argumentNumber);
        }

    }
    
    private final CALModelManager.MetadataChangeListener metadataChangeListener = 
        new CALModelManager.MetadataChangeListener () {
            public void metadataSaved (CALFeatureName featureName) {
                Input input = (Input)getEditorInput ();
                
                if (input != null && input.featureNameIs (featureName)) {
                    metadataChanged ();
                }
            }
        };

    private CALMetadataEditorPanel editorPanel;

    /**
     * Constructor CALMetadataEditor
     *
     */
    public CALMetadataEditor () {
        CALModelManager.getCALModelManager ().addMetadataChangeListener (metadataChangeListener);
    }
    
    /**
     * Method getMetadataLocale
     *
     * @return Returns the Locale to be used when loading metadata
     */
    private static Locale getMetadataLocale () {
        // TODO: set this in preferences
        return LocaleUtilities.INVARIANT_LOCALE;
    }

    /**
     * Method metadataChanged
     *
     */
    private void metadataChanged () {
        editorPanel.refresh ();
    }

    /* (non-Javadoc)
     * @see org.eclipse.ui.part.EditorPart#doSave(org.eclipse.core.runtime.IProgressMonitor)
     */
    @Override
    public void doSave (IProgressMonitor monitor) {
        editorPanel.doSave (monitor);
    }

    /* (non-Javadoc)
     * @see org.eclipse.ui.part.EditorPart#doSaveAs()
     */
    @Override
    public void doSaveAs () {
        // TODO Auto-generated method stub

    }

    /* (non-Javadoc)
     * @see org.eclipse.ui.part.EditorPart#init(org.eclipse.ui.IEditorSite, org.eclipse.ui.IEditorInput)
     */
    @Override
    public void init (IEditorSite site, IEditorInput editorInput) {
        if (!(editorInput instanceof Input)) {
            throw new IllegalArgumentException (MetadataEditorMessages.InvalidEditorInput_Message);
        } 
        
        setSite (site);
        setInput (editorInput);
        
        setPartName (MessageFormat.format ("{0}", new Object [] { editorInput.getName () })); //$NON-NLS-1$
    }

    /* (non-Javadoc)
     * @see org.eclipse.ui.part.EditorPart#isDirty()
     */
    @Override
    public boolean isDirty () {
        return editorPanel.isDirty ();
    }

    /* (non-Javadoc)
     * @see org.eclipse.ui.part.EditorPart#isSaveAsAllowed()
     */
    @Override
    public boolean isSaveAsAllowed () {
        return false;
    }

    /* (non-Javadoc)
     * @see org.eclipse.ui.part.WorkbenchPart#createPartControl(org.eclipse.swt.widgets.Composite)
     */
    @Override
    public void createPartControl (Composite parent) {
        editorPanel = new CALMetadataEditorPanel (parent, (Input)getEditorInput ());
        
        editorPanel.addPropertyChangeListener (new PropertyChangeListener () {
            public void propertyChange (PropertyChangeEvent evt) {
                if (evt.getPropertyName ().equals (CALMetadataEditorPanel.PROP_HASCHANGED)) {
                    firePropertyChange (PROP_DIRTY);
                }
            }
        });
    }

    /* (non-Javadoc)
     * @see org.eclipse.ui.part.WorkbenchPart#setFocus()
     */
    @Override
    public void setFocus () {
        if (editorPanel != null) {
            editorPanel.setFocus ();
        }
    }

    /* (non-Javadoc)
     * @see org.eclipse.ui.part.WorkbenchPart#dispose()
     */
    @Override
    public void dispose () {
        editorPanel.dispose ();
        
        CALModelManager.getCALModelManager ().removeMetadataChangeListener (metadataChangeListener);
        
        super.dispose ();
    }
}
