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
 * CALHyperlinkDetector.java
 * Creation date: Aug 15, 2007
 * By: Greg McClement
 */
package org.openquark.cal.eclipse.ui.caleditor;

import java.lang.reflect.AccessibleObject;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Member;
import java.lang.reflect.Method;

import org.eclipse.core.resources.IStorage;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jdt.core.IField;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IJavaModel;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.ISourceRange;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.Signature;
import org.eclipse.jdt.ui.JavaUI;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.ITextViewer;
import org.eclipse.jface.text.hyperlink.IHyperlink;
import org.eclipse.jface.text.hyperlink.IHyperlinkDetector;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.PartInitException;
import org.openquark.cal.compiler.CompilerMessageLogger;
import org.openquark.cal.compiler.ForeignFunctionInfo;
import org.openquark.cal.compiler.ForeignTypeInfo;
import org.openquark.cal.compiler.Function;
import org.openquark.cal.compiler.IdentifierInfo;
import org.openquark.cal.compiler.IdentifierOccurrence;
import org.openquark.cal.compiler.MessageLogger;
import org.openquark.cal.compiler.ModuleName;
import org.openquark.cal.compiler.ModuleSourceDefinition;
import org.openquark.cal.compiler.ModuleTypeInfo;
import org.openquark.cal.compiler.QualifiedName;
import org.openquark.cal.compiler.SearchManager;
import org.openquark.cal.compiler.SourcePosition;
import org.openquark.cal.compiler.SourceRange;
import org.openquark.cal.compiler.TypeConstructor;
import org.openquark.cal.compiler.UnableToResolveForeignEntityException;
import org.openquark.cal.eclipse.core.CALModelManager;
import org.openquark.cal.eclipse.ui.CALEclipseUIPlugin;
import org.openquark.cal.eclipse.ui.actions.ActionMessages;
import org.openquark.cal.eclipse.ui.actions.ActionUtilities;
import org.openquark.cal.eclipse.ui.util.CoreUtility;
import org.openquark.cal.eclipse.ui.util.Messages;
import org.openquark.util.Pair;

/**
 * This class to detects hyperlinks in CAL source. 
 * 
 * @author Greg McClement
 */
public class CALHyperlinkDetector implements IHyperlinkDetector {

    private final SearchManager searchManager;

    private final static IHyperlink[] noHyperlinks = null;
        
    public CALHyperlinkDetector() {
        CALModelManager modelManager = CALModelManager.getCALModelManager();
        searchManager = modelManager.getSearchManager();
    }

    public IHyperlink[] detectHyperlinks(ITextViewer textViewer,
            IRegion region, boolean canShowMultipleHyperlinks) {
        if (!CoreUtility.builderEnabledCheck((String) null)){
            return noHyperlinks;
        }
        
        final CALEditor.AdaptedSourceViewer sourceViewer = (CALEditor.AdaptedSourceViewer) textViewer;
        final CALEditor textEditor = sourceViewer.getEditor();
        final IDocument document = ActionUtilities.getDocument(textEditor);

        if (document != null) {
            final int offset = region.getOffset();
            return getHyperlinkForRegion(textEditor, document, offset, searchManager, false);
        }
        return noHyperlinks; // FIX ME
    }

    /**
     * Produces a hyperlink for the region.
     * @param textEditor the CAL editor.
     * @param currentDocument the document.
     * @param currentOffset the offset.
     * @param searchManager the search manager.
     * @param reportBadSelection whether to report a bad selection to the user.
     * @return a hyperlink, or null.
     */
    public static IHyperlink[] getHyperlinkForRegion(final CALEditor textEditor, final IDocument currentDocument, final int currentOffset, SearchManager searchManager, boolean reportBadSelection) {
        try {
            final PartiallySynchronizedDocument psd = (PartiallySynchronizedDocument) currentDocument;
            
            final CALModelManager cmm = CALModelManager.getCALModelManager();
            final IStorage storage = textEditor.getStorage();
            
            ModuleName moduleName;
            try{
                moduleName = cmm.getModuleName(storage);
            }
            catch(IllegalArgumentException ex){
                // CAL File is not in the correct spot in the hierarchy so there
                // is no type information available.
                CoreUtility.showMessage(ActionMessages.OpenDeclarationAction_error_title, ActionMessages.error_calFileNotInCorrectLocation_message, IStatus.ERROR);
                return noHyperlinks;
            }
            if (moduleName == null){
                final String errorMessage = Messages.format(ActionMessages.error_invalidFileName_message, textEditor.getStorage().getName());
                CoreUtility.showMessage(ActionMessages.OpenDeclarationAction_error_title, errorMessage, IStatus.ERROR);
                return noHyperlinks;
            }                

            final Pair<SourcePosition, SourcePosition> posAndPosToTheLeft = getCurrentPositionAndPositionToTheLeft(psd, currentOffset, moduleName);
            // current position is in new code so no hyperlinks are possible
            if (posAndPosToTheLeft == null){
                return noHyperlinks;
            }
            
            CoreUtility.initializeCALBuilder(null, 100, 100);

            ModuleSourceDefinition msd = cmm.getModuleSourceDefinition(moduleName);
            if (msd == null){
                CoreUtility.showErrorOnStatusLine(textEditor, ActionMessages.error_messageBadSelection_noTypeInformation_CAL, false);
                return noHyperlinks;
            }
            
            if (!cmm.getProgramModelManager().hasModuleInProgram(moduleName)){
                CoreUtility.showErrorOnStatusLine(textEditor, ActionMessages.OpenAction_error_noSourceCodeMetrics, false);
                return noHyperlinks;
            }
            
            CompilerMessageLogger messageLogger = new MessageLogger();
            {
                IdentifierOccurrence<?> occurrence = searchManager.findSymbolAt(moduleName, posAndPosToTheLeft.fst(), posAndPosToTheLeft.snd(), messageLogger);
                if (occurrence == null) {
                    if (reportBadSelection) {
                        CoreUtility.showErrorOnStatusLine(textEditor, ActionMessages.OpenAction_error_messageBadSelection_CAL);
                    }
                    return noHyperlinks;
                }
                
                final IHyperlink hyperlink = getHyperlink(textEditor, psd, cmm, searchManager, messageLogger, occurrence, moduleName, reportBadSelection);
                if (hyperlink == null) {
                    return noHyperlinks;
                } else {
                    return new IHyperlink[] {hyperlink};
                }
            }
        } catch (BadLocationException e) {
            // will only happen on concurrent modification
            CALEclipseUIPlugin.log(new Status(IStatus.ERROR, CALEclipseUIPlugin.PLUGIN_ID, IStatus.OK, "", e)); //$NON-NLS-1$
            return noHyperlinks;                
        } catch (PartInitException e) {
            CALEclipseUIPlugin.log(new Status(IStatus.ERROR, CALEclipseUIPlugin.PLUGIN_ID, IStatus.OK, "", e)); //$NON-NLS-1$
            return noHyperlinks;
        }
    }

    /**
     * Given a partially synchronized document and an offset in that document, return a pair of {@link SourcePosition} for
     * use with the {@link SearchManager} API - the first is the corresponding position in the <i>original document</i>, and
     * the second is the position in the original document corresponding to the spot immediate to the left of the position
     * in the current (partially synchronized) document. The second position can be null to indicate that the previous position
     * is not in the original document.
     * 
     * @param currentDocument the current document.
     * @param currentOffset the offset in the current document.
     * @param moduleName the associated module name.
     * @return the pair of source positions. Null if the position is in new code.
     * @throws BadLocationException
     */
    public static Pair<SourcePosition, SourcePosition> getCurrentPositionAndPositionToTheLeft(final PartiallySynchronizedDocument currentDocument, final int currentOffset, ModuleName moduleName) throws BadLocationException {
        final IDocument originalDocument = currentDocument.getOriginalDocument();

        final int offset = currentDocument.getOriginalOffset(currentOffset);
        if (offset == -1){
            return null;
        }
        final int offsetOfPosToTheLeft = currentDocument.getOriginalOffset(currentOffset-1);
        
        final int firstLine = originalDocument.getLineOfOffset(offset);
        final int column = CoreUtility.getColumn(firstLine, offset, originalDocument);
        final SourcePosition sourcePosition = SearchManager.makeSourcePosition(firstLine+1, column+1, moduleName);

        final int columnInCurrentDoc = CoreUtility.getColumn(currentDocument.getLineOfOffset(currentOffset), currentOffset, currentDocument);

        final SourcePosition sourcePositionToTheLeft;
        if (columnInCurrentDoc == 0 || offsetOfPosToTheLeft < 0) {
            sourcePositionToTheLeft = null;
        } else {
            final int line = originalDocument.getLineOfOffset(offsetOfPosToTheLeft);
            final int col = CoreUtility.getColumn(line, offsetOfPosToTheLeft, originalDocument);
            sourcePositionToTheLeft = SearchManager.makeSourcePosition(line+1, col+1, moduleName);
        }
        
        return Pair.make(sourcePosition, sourcePositionToTheLeft);
    }

    private static IHyperlink getHyperlink(final CALEditor textEditor,
            final PartiallySynchronizedDocument psd, final CALModelManager cmm,
            final SearchManager searchManager, CompilerMessageLogger messageLogger,
            final IdentifierOccurrence<?> target, ModuleName moduleName,
            boolean reportBadSelection) throws BadLocationException, PartInitException {
        
        final IdentifierInfo identifierInfo = target.getIdentifierInfo();
        
        if (identifierInfo instanceof IdentifierInfo.Module){
            return getHyperlinkToModule(psd, cmm, ((IdentifierInfo.Module)identifierInfo).getResolvedName(), target.getSourceRange());
        }
        // If the target refers to java source we have to access the Java model.
        else if (target instanceof IdentifierOccurrence.ForeignDescriptor<?>){
            return getHyperlinkToJavaDefinition(textEditor, psd, cmm, (IdentifierInfo.TopLevel)identifierInfo, target.getSourceRange());
        }
        else{
            return getHyperlinkToDefinition(textEditor, psd, cmm, searchManager, messageLogger, target, moduleName, reportBadSelection);
        }
    }

    /**
     * Produces a hyperlink to a module.
     * @param psd the document.
     * @param cmm the CAL model manager.
     * @param target the target.
     * @param sourceRange the source range of the hyperlink.
     * @return a hyperlink, or null.
     * @throws BadLocationException
     */
    private static IHyperlink getHyperlinkToModule(final PartiallySynchronizedDocument psd, final CALModelManager cmm, final ModuleName target, final SourceRange sourceRange) throws BadLocationException {
        final IHyperlink hyperlink = new IHyperlink() {
            private IRegion region = CoreUtility.toRegion(sourceRange, psd);
            
            public IRegion getHyperlinkRegion(){
                return region;                            
            }
            public String getTypeLabel(){
                return null;
            }
            
            public String getHyperlinkText(){
                return target.toSourceText();
            }
    
            public void open(){
                try {
                    IStorage definitionFile = cmm.getInputSourceFile(target);
                    CoreUtility.openInEditor(definitionFile, true);
                } catch (PartInitException e) {
                    CALEclipseUIPlugin.log(new Status(IStatus.ERROR, CALEclipseUIPlugin.PLUGIN_ID, IStatus.OK, "", e)); //$NON-NLS-1$
                }
            }
        };
        return hyperlink;
    }

    /**
     * Produces a hyperlink to a java definition.
     * @param textEditor the CAL editor.
     * @param psd the document.
     * @param cmm the CAL model manager.
     * @param target the target.
     * @return a hyperlink, or null.
     * @throws BadLocationException
     * @throws PartInitException
     */
    private static IHyperlink getHyperlinkToJavaDefinition(final CALEditor textEditor, final PartiallySynchronizedDocument psd, final CALModelManager cmm, final IdentifierInfo.TopLevel target, final SourceRange targetSourceRange) throws BadLocationException, PartInitException {
        final QualifiedName qn = target.getResolvedName();
        final ModuleTypeInfo mti = cmm.getModuleTypeInfo(qn.getModuleName());
        IWorkspaceRoot workspaceRoot = ResourcesPlugin.getWorkspace().getRoot();
        IJavaModel jm = JavaCore.create(workspaceRoot);
        try {
            IJavaProject[] jps = jm.getJavaProjects();
            // Look for a type name
            if (target instanceof IdentifierInfo.TopLevel.TypeCons){
                final TypeConstructor tc = mti.getTypeConstructor(qn.getUnqualifiedName());
                if (tc == null){                                
                    CoreUtility.showErrorOnStatusLine(textEditor, ActionMessages.OpenAction_error_noSourceCodeMetrics);
                }
                else{
                    final ForeignTypeInfo fi = tc.getForeignTypeInfo();
                    // Inner class names are separated with '$' but the Java API wanted '.' for everything
                    final String name;
                    try {
                        name = fi.getForeignType().getName().replace('$', '.');
                    } catch (UnableToResolveForeignEntityException e) {
                        CoreUtility.showErrorOnStatusLine(textEditor, ActionMessages.OpenAction_error_sourceCodeNotAvailable_CAL);
                        return null;                
                    }
                    for(int i_jps = 0; i_jps < jps.length; ++i_jps){
                        final IJavaProject jp = jps[i_jps];
                        final IType type = jp.findType(name);
                        if (type != null){
                            final IHyperlink hyperlink = new IHyperlink() {
                                private IRegion region = CoreUtility.toRegion(targetSourceRange, psd);
                                
                                public IRegion getHyperlinkRegion(){
                                    return region;                            
                                }
                                public String getTypeLabel(){
                                    return null;
                                }
                                
                                public String getHyperlinkText(){
                                    return target.getResolvedName().toSourceText();
                                }
    
                                public void open(){
                                    try {
                                        IJavaElement javaElement = type;
                                        IEditorPart part;
                                        part = JavaUI.openInEditor(type);
                                        JavaUI.revealInEditor(part, javaElement);                                                    
                                    } catch (JavaModelException e) {
                                        CALEclipseUIPlugin.log(new Status(IStatus.ERROR, CALEclipseUIPlugin.PLUGIN_ID, IStatus.OK, "", e)); //$NON-NLS-1$
                                    } catch (PartInitException e) {
                                        CALEclipseUIPlugin.log(new Status(IStatus.ERROR, CALEclipseUIPlugin.PLUGIN_ID, IStatus.OK, "", e)); //$NON-NLS-1$
                                    }
                                }
                            };
                            return hyperlink;                                                                
                        }
                    }                            
                    CoreUtility.showErrorOnStatusLine(textEditor, ActionMessages.OpenAction_error_sourceCodeNotAvailable_CAL);
                }
            }
            // look for a member or field
            else if (target instanceof IdentifierInfo.TopLevel.FunctionOrClassMethod){
                final Function fa = mti.getFunction(qn.getUnqualifiedName());
                final ForeignFunctionInfo fi = fa.getForeignFunctionInfo();
                if (fi instanceof ForeignFunctionInfo.Invocation){
                    final ForeignFunctionInfo.Invocation invocation = (ForeignFunctionInfo.Invocation) fi;
                    final AccessibleObject ao;
                    try {
                        ao = invocation.getJavaProxy();
                    } catch (UnableToResolveForeignEntityException e) {
                        CoreUtility.showErrorOnStatusLine(textEditor, ActionMessages.OpenAction_error_sourceCodeNotAvailable_CAL);
                        return null;                
                    }
                    // Figure out what class contains this member/field/constructor
                    Class<?> declaringClass = ((Member) ao).getDeclaringClass();
                    if (declaringClass != null){
                        // Search through the java projects to find this symbol
                        for(int i_jps = 0; i_jps < jps.length; ++i_jps){
                            IJavaProject jp = jps[i_jps];
                            final IType type = jp.findType(declaringClass.getName());
                            // we found the matching class
                            if (type != null){
                                IJavaElement javaElement = type;
                                ISourceRange sourceRange = type.getNameRange();
                                // Search the found class for the given method  
                                if (ao instanceof Method){
                                    final Method method = (Method) ao;
                                    final Class<?>[] parameterTypes = method.getParameterTypes();
                                    // Calculate the type names.
                                    final String[] parameterTypeStrings = new String[parameterTypes.length];
                                    for(int i_parameterTypes = 0; i_parameterTypes < parameterTypes.length; ++i_parameterTypes){
                                        final String longTypeName = parameterTypes[i_parameterTypes].getName();
                                        parameterTypeStrings[i_parameterTypes] = Signature.createTypeSignature(longTypeName, false);
                                    }
                                    final IMethod iMethod = type.getMethod(method.getName(), parameterTypeStrings);
                                    
                                    final IHyperlink hyperlink = new IHyperlink() {
                                        private IRegion region = CoreUtility.toRegion(targetSourceRange, psd);
                                        
                                        public IRegion getHyperlinkRegion(){
                                            return region;                            
                                        }
                                        public String getTypeLabel(){
                                            return null;
                                        }
                                        
                                        public String getHyperlinkText(){
                                            return target.getResolvedName().toSourceText();
                                        }
    
                                        public void open(){
                                            try {
                                                IEditorPart part = JavaUI.openInEditor(iMethod);
                                                JavaUI.revealInEditor(part, (IJavaElement) iMethod);
                                            } catch (JavaModelException e) {
                                                CALEclipseUIPlugin.log(new Status(IStatus.ERROR, CALEclipseUIPlugin.PLUGIN_ID, IStatus.OK, "", e)); //$NON-NLS-1$
                                            } catch (PartInitException e) {
                                                CALEclipseUIPlugin.log(new Status(IStatus.ERROR, CALEclipseUIPlugin.PLUGIN_ID, IStatus.OK, "", e)); //$NON-NLS-1$
                                            }
                                        }
                                    };
                                    return hyperlink;                                                                
                                }
                                // Search the class for the given constructor
                                else if (ao instanceof Constructor){
                                    final Constructor<?> constructor = (Constructor<?>) ao;
                                    final Class<?>[] parameterTypes = constructor.getParameterTypes();
                                    String[] parameterTypeStrings = new String[parameterTypes.length];
                                    for(int i_parameterTypes = 0; i_parameterTypes < parameterTypes.length; ++i_parameterTypes){
                                        parameterTypeStrings[i_parameterTypes] = Signature.createTypeSignature(parameterTypes[i_parameterTypes].getName(), true);
                                    }
                                    final String classNameOnly = Signature.getSimpleName(constructor.getName());
                                    final IMethod iMethod = type.getMethod(classNameOnly, parameterTypeStrings);
                                    final IHyperlink hyperlink = new IHyperlink() {
                                        private IRegion region = CoreUtility.toRegion(targetSourceRange, psd);
                                        
                                        public IRegion getHyperlinkRegion(){
                                            return region;                            
                                        }
                                        
                                        public String getTypeLabel(){
                                            return null;
                                        }
                                        
                                        public String getHyperlinkText(){
                                            return target.getResolvedName().toSourceText();
                                        }
    
                                        public void open(){
                                            try {
                                                IEditorPart part = JavaUI.openInEditor(iMethod);
                                                JavaUI.revealInEditor(part, (IJavaElement) iMethod);
                                            } catch (JavaModelException e) {
                                                CALEclipseUIPlugin.log(new Status(IStatus.ERROR, CALEclipseUIPlugin.PLUGIN_ID, IStatus.OK, "", e)); //$NON-NLS-1$
                                            } catch (PartInitException e) {
                                                CALEclipseUIPlugin.log(new Status(IStatus.ERROR, CALEclipseUIPlugin.PLUGIN_ID, IStatus.OK, "", e)); //$NON-NLS-1$
                                            }
                                        }
                                    };
                                    return hyperlink;                                                                
                                }
                                // Search the class for the given field
                                else if (ao instanceof Field){
                                    final Field field = (Field) ao;
                                    final IField iField = type.getField(field.getName());
                                    final IHyperlink hyperlink = new IHyperlink() {
                                        private IRegion region = CoreUtility.toRegion(targetSourceRange, psd);
                                        
                                        public IRegion getHyperlinkRegion(){
                                            return region;                            
                                        }
                                        public String getTypeLabel(){
                                            return null;
                                        }
                                        
                                        public String getHyperlinkText(){
                                            return target.getResolvedName().toSourceText();
                                        }
    
                                        public void open(){
                                            try {
                                                final IEditorPart part = JavaUI.openInEditor(iField);
                                                JavaUI.revealInEditor(part, (IJavaElement) iField);
                                            } catch (JavaModelException e) {
                                                CALEclipseUIPlugin.log(new Status(IStatus.ERROR, CALEclipseUIPlugin.PLUGIN_ID, IStatus.OK, "", e)); //$NON-NLS-1$
                                            } catch (PartInitException e) {
                                                CALEclipseUIPlugin.log(new Status(IStatus.ERROR, CALEclipseUIPlugin.PLUGIN_ID, IStatus.OK, "", e)); //$NON-NLS-1$
                                            }
                                        }
                                    };
                                    return hyperlink;                                                                
                                }
                                if (null == CoreUtility.openJavaElementInEditor(javaElement, sourceRange)){
                                    CoreUtility.showErrorOnStatusLine(textEditor, ActionMessages.OpenAction_error_sourceCodeNotAvailable_CAL);
                                    return null;
                                }
                                break;
                            }
                        }
                    }
                }
            }
        } catch (JavaModelException e) {
        }
        return null;
    }

    /**
     * Produces a hyperlink to a definition.
     * @param textEditor the CAL editor.
     * @param psd the document.
     * @param cmm the CAL model manager.
     * @param searchManager the search manager.
     * @param messageLogger the message logger.
     * @param target the target.
     * @param currentModuleName the current module name.
     * @param reportBadSelection whether to report a bad selection to the user.
     * @return a hyperlink, or null.
     * @throws BadLocationException
     */
    private static IHyperlink getHyperlinkToDefinition(final CALEditor textEditor, final PartiallySynchronizedDocument psd, final CALModelManager cmm, final SearchManager searchManager, CompilerMessageLogger messageLogger, final IdentifierOccurrence<?> target, final ModuleName currentModuleName, boolean reportBadSelection) throws BadLocationException {
        
        final IdentifierInfo identifierInfo = target.getIdentifierInfo();
        final ModuleName targetModuleName;
        if (identifierInfo instanceof IdentifierInfo.Module) {
            targetModuleName = ((IdentifierInfo.Module)identifierInfo).getResolvedName();
        } else if (identifierInfo instanceof IdentifierInfo.TopLevel) {
            targetModuleName = ((IdentifierInfo.TopLevel)identifierInfo).getResolvedName().getModuleName();
        } else if (identifierInfo instanceof IdentifierInfo.DataConsFieldName) {
            targetModuleName = ((IdentifierInfo.DataConsFieldName)identifierInfo).getFirstAssociatedDataConstructor().getResolvedName().getModuleName();
        } else {
            targetModuleName = currentModuleName;
        }
        
        final IdentifierOccurrence<?> definition = searchManager.findDefinition(targetModuleName, target, messageLogger);
        if (definition != null){
            final IHyperlink hyperlink = new IHyperlink() {
                private IRegion region = CoreUtility.toRegion(target.getSourceRange(), psd);
                
                public IRegion getHyperlinkRegion(){
                    return region;                            
                }
                public String getTypeLabel(){
                    return null;
                }
                
                public String getHyperlinkText(){
                    return target.getIdentifierInfo().toString();
                }

                public void open(){
                    try {
                        IStorage definitionFile = cmm.getInputSourceFile(targetModuleName);
                        CALEditor definitionEditor = (CALEditor) CoreUtility.openInEditor(definitionFile, true);
                        final PartiallySynchronizedDocument definitionDocument = (PartiallySynchronizedDocument) ActionUtilities.getDocument(definitionEditor);
                        CoreUtility.showPosition(definitionEditor, definitionFile, definitionDocument, definition.getSourceRange(), false);                                    
                    } catch (PartInitException e) {
                        CALEclipseUIPlugin.log(new Status(IStatus.ERROR, CALEclipseUIPlugin.PLUGIN_ID, IStatus.OK, "", e)); //$NON-NLS-1$
                    }
                }
            };
            return hyperlink;                        
        }
        else{
            if (reportBadSelection) {
                CoreUtility.showErrorOnStatusLine(textEditor, ActionMessages.OpenAction_error_messageBadSelection_CAL);
            }
            return null;  
        }
    }

}
