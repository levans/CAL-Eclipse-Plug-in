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
package org.openquark.cal.eclipse.ui.templates;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.resources.IStorage;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.templates.TemplateContext;
import org.eclipse.jface.text.templates.TemplateVariable;
import org.eclipse.jface.text.templates.TemplateVariableResolver;
import org.openquark.cal.compiler.CompilerMessageLogger;
import org.openquark.cal.compiler.DataConstructor;
import org.openquark.cal.compiler.FieldName;
import org.openquark.cal.compiler.Function;
import org.openquark.cal.compiler.IdentifierInfo;
import org.openquark.cal.compiler.IdentifierOccurrence;
import org.openquark.cal.compiler.LocalFunctionIdentifier;
import org.openquark.cal.compiler.MessageLogger;
import org.openquark.cal.compiler.ModuleName;
import org.openquark.cal.compiler.ModuleTypeInfo;
import org.openquark.cal.compiler.SearchManager;
import org.openquark.cal.compiler.SourceModel;
import org.openquark.cal.compiler.IdentifierInfo.TopLevel.FunctionOrClassMethod;
import org.openquark.cal.compiler.IdentifierOccurrence.Binding.Definition;
import org.openquark.cal.eclipse.core.CALModelManager;
import org.openquark.cal.eclipse.ui.caleditor.CALEditor;
import org.openquark.cal.eclipse.ui.caleditor.PartiallySynchronizedDocument;
import org.openquark.cal.eclipse.ui.caleditor.CALEditor.AdaptedSourceViewer;
import org.openquark.cal.eclipse.ui.text.CompletionProcessor;
import org.openquark.cal.eclipse.ui.text.CompletionProcessor.ViewTemplateContext;
import org.openquark.cal.eclipse.ui.util.CoreUtility;

/**
 * A variable resolver for the CAL doc variables. 
 * 
 * @author Greg McClement
 */
public class CALTemplateVariableResolver extends TemplateVariableResolver {
    private final CALModelManager modelManager;
    
    public CALTemplateVariableResolver(){
        super();
        modelManager = CALModelManager.getCALModelManager();
    }

    @Override
    public void resolve(TemplateVariable variable, TemplateContext context) {
        if (variable.getName().equals("context_keyword")){
//            variable.setResolved(true);  3.3 feature
            final String[] values = {"", "module =", "function =", "typeClass =", "typeConstructor =", "dataConstructor ="};
            variable.setValues(values);
            return;
        }
        else if (variable.getName().startsWith("scope")){
//          variable.setResolved(true);  3.3 feature
          final String[] values = {"", "private", "protected", "public"};
          variable.setValues(values);
          return;
      }
        if (variable.getName().equals("argument_name")){
            CompletionProcessor.ViewTemplateContext dtc = (ViewTemplateContext) context;
            CALEditor.AdaptedSourceViewer asv = (AdaptedSourceViewer) dtc.getViewer();
            CALEditor calEditor = asv.getEditor();
            final IStorage storage = calEditor.getStorage();
                        
            ModuleName moduleName;
            try{
                moduleName = modelManager.getModuleName(storage);
            }
            catch(IllegalArgumentException ex){
                return; // not found
            }
            if (moduleName == null){
                return; // not found
            }                
            
            PartiallySynchronizedDocument psd = (PartiallySynchronizedDocument) dtc.getDocument();
            final int startOffset = dtc.getStart();
            final IDocument document = psd.getOriginalDocument();
            final int offset = psd.estimateOriginalOffset(startOffset);
                        
            try {
                final int firstLine = document.getLineOfOffset(offset);
                final int column = CoreUtility.getColumn(firstLine, offset, document);

                final String[] arguments = getArguments(moduleName, firstLine, column);
                if (arguments != null){
                    variable.setValues(arguments);
                }
            } catch (BadLocationException e) {                
            }
            return;
        }
        
        super.resolve(variable, context);
    }

    /**
     * 
     * @param moduleName
     * @param firstLine zero based line offset (document co-ordinates)
     * @param column zero based column offset (document co-ordinates)
     * @return an array containing the argument names of the scoped entity. This returns null when there are no arguments.
     */
    public static String[] getArguments(final ModuleName moduleName, final int firstLine, final int column){
        final CALModelManager modelManager = CALModelManager.getCALModelManager();
        final SearchManager searchManager = modelManager.getSearchManager();
        
        CompilerMessageLogger messageLogger = new MessageLogger();
        List<IdentifierOccurrence<?>> identifiers = searchManager.findSymbolsAfter(moduleName, firstLine + 1, column + 1, messageLogger);
        IdentifierInfo identifierInfo = null;
        for(IdentifierOccurrence<?> identifier : identifiers){
            if (identifier instanceof Definition){
                if (identifier.getIdentifierInfo() instanceof IdentifierInfo.Local.Parameter){
                    // keep going
                    /*
                     * This can happen for cases like
                     * 
                     * /**
                     *  * <cursor is here>
                     *  * @arg x the fancy arg
                     *  *
                     *  *\/
                     *  foobar a = (Prelude.undefined :: (a->b->c));
                     */
                }
                else if (identifier.getIdentifierInfo() instanceof IdentifierInfo.TypeVariable){
                    // keep going
                    /*
                     * This can happen for cases like
                     * 
                     * /**
                     *  * <cursor is here>
                     *  *
                     *  *\/
                     *  t3 :: Prelude.Num a => a;
                     *  t3 = t2;
                     */
                }
                else{
                    // done.
                    identifierInfo = identifier.getIdentifierInfo();
                    break;
                }
            }
            else if (identifier instanceof IdentifierOccurrence.Reference.NonQualifiable){
                if (identifier.getSourceElement() instanceof SourceModel.FunctionTypeDeclaration ||
                        identifier.getSourceElement() instanceof SourceModel.LocalDefn.Function.TypeDeclaration
                ){
                    // type declaration.
                    identifierInfo = identifier.getIdentifierInfo();
                    break;                            
                }
            }
        }
        if (identifierInfo == null){
            return null;
        }
        ModuleTypeInfo mti = modelManager.getModuleTypeInfo(moduleName);
        if (identifierInfo instanceof IdentifierInfo.TopLevel.FunctionOrClassMethod){
            IdentifierInfo.TopLevel.FunctionOrClassMethod fcm = (FunctionOrClassMethod) identifierInfo;
            Function function = mti.getFunction(fcm.getResolvedName().getUnqualifiedName());
            if (function != null){
                return getValues(function);
            }
        }
        else if (identifierInfo instanceof IdentifierInfo.TopLevel.DataCons){
            IdentifierInfo.TopLevel.DataCons localName =
                (IdentifierInfo.TopLevel.DataCons) identifierInfo;

            DataConstructor dataConstructor = mti.getDataConstructor(localName.getResolvedName().getUnqualifiedName());
            if (dataConstructor != null){
                return getValues(dataConstructor);
            }
        }
        else if (identifierInfo instanceof IdentifierInfo.Local.Function){
            IdentifierInfo.Local.Parameter.Function localName =
                (IdentifierInfo.Local.Parameter.Function) identifierInfo;

            final LocalFunctionIdentifier localFunctionIdentifier = localName.getLocalFunctionIdentifier();
            Function topLevelFunction = mti.getFunction(localFunctionIdentifier.getToplevelFunctionName().getUnqualifiedName());
            if (topLevelFunction != null){
                Function localFunction = topLevelFunction.getLocalFunction(localFunctionIdentifier);
                if (localFunction != null){
                    return getValues(localFunction);
                }
            }
        }
        return null;
    }

    private static String[] getValues(Function function) {
        int nArgs = function.getNArgumentNames();
        if (nArgs > 0){
            ArrayList<String> args = new ArrayList<String>(nArgs);
            for(int i = 0; i < nArgs; ++i){
                args.add(function.getArgumentName(i));
            }
            return args.toArray(new String[nArgs]);
        }
        return null;
    }
    
    public static String[] getValues(DataConstructor dataConstructor) {
        int nArgs = dataConstructor.getNArgumentNames();
        if (nArgs > 0){
            ArrayList<String> args = new ArrayList<String>(nArgs);
            for(int i = 0; i < nArgs; ++i){
                FieldName fieldName = dataConstructor.getNthFieldName(i);
                args.add(fieldName.getCalSourceForm());    
            }
            return args.toArray(new String[nArgs]);
        }
        return null;
    }
}
