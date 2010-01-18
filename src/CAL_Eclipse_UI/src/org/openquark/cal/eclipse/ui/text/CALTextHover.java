/*******************************************************************************
 * Copyright (c) 2007 Business Objects Software Limited and others.
 * All rights reserved. 
 * This file is made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Business Objects Software Limited - initial API and implementation based on Eclipse 3.3 code for
 *                             /org.eclipse.jdt.ui/ui/org/eclipse/jdt/internal/ui/text/JavaIndenter.java
 *                           Eclipse source is available at: http://www.eclipse.org/downloads/
 *******************************************************************************/


/*
 * CALTextHover.java
 * Created: Jul 12, 2007
 * By: Andrew Eisenberg
 */
package org.openquark.cal.eclipse.ui.text;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IInformationControl;
import org.eclipse.jface.text.IInformationControlCreator;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.ITextHover;
import org.eclipse.jface.text.ITextHoverExtension;
import org.eclipse.jface.text.ITextViewer;
import org.eclipse.jface.text.Region;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Shell;
import org.openquark.cal.caldoc.CALDocToTooltipHTMLUtilities;
import org.openquark.cal.compiler.CompilerMessageLogger;
import org.openquark.cal.compiler.DataConstructor;
import org.openquark.cal.compiler.Function;
import org.openquark.cal.compiler.FunctionalAgent;
import org.openquark.cal.compiler.IdentifierInfo;
import org.openquark.cal.compiler.IdentifierOccurrence;
import org.openquark.cal.compiler.LocalFunctionIdentifier;
import org.openquark.cal.compiler.MessageLogger;
import org.openquark.cal.compiler.ModuleName;
import org.openquark.cal.compiler.ModuleSourceDefinition;
import org.openquark.cal.compiler.ModuleTypeInfo;
import org.openquark.cal.compiler.QualifiedName;
import org.openquark.cal.compiler.ScopedEntityNamingPolicy;
import org.openquark.cal.compiler.SearchManager;
import org.openquark.cal.compiler.SearchResult;
import org.openquark.cal.compiler.SourceMetricsManager;
import org.openquark.cal.compiler.SourcePosition;
import org.openquark.cal.compiler.SourceRange;
import org.openquark.cal.compiler.TypeClass;
import org.openquark.cal.compiler.TypeConstructor;
import org.openquark.cal.compiler.SourceIdentifier.Category;
import org.openquark.cal.eclipse.core.CALModelManager;
import org.openquark.cal.eclipse.core.builder.CALBuilder;
import org.openquark.cal.eclipse.ui.CALEclipseUIPlugin;
import org.openquark.cal.eclipse.ui.CALUIMessages;
import org.openquark.cal.eclipse.ui.actions.ActionMessages;
import org.openquark.cal.eclipse.ui.actions.ActionUtilities;
import org.openquark.cal.eclipse.ui.caleditor.CALEditor;
import org.openquark.cal.eclipse.ui.caleditor.CALHyperlinkDetector;
import org.openquark.cal.eclipse.ui.caleditor.CALSourceViewer;
import org.openquark.cal.eclipse.ui.caleditor.PartiallySynchronizedDocument;
import org.openquark.cal.eclipse.ui.util.CoreUtility;
import org.openquark.cal.services.ProgramModelManager;
import org.openquark.util.Pair;

/**
 * Used to provide information to the hover control.
 * 
 * @author GMcClement
 * @author Andrew Eisenberg
 */
public class CALTextHover implements ITextHover, ITextHoverExtension{

    /**
     * This is a hack. When the users presses F2 and if previously the hover showed source code 
     * then the source code should be shown even though the hover for not-shift F2 is called.
     */
    private static boolean showingSourceCode = false;
    

    /**
     * When this is used for a tooltip hover the offset override should be updated. Then
     * when the F2 key is pressed the browser window will ignore the cursor position and
     * use the position of the tool tip. If this is used by the ShowTooltipDescription action
     * then the override will not be used instead the cursor position should be used.
     */
    private boolean updateOffset;
    
    private final int stateMask;
    
    /**
     * This field remembers whether the text to be displayed should be shown in a browser
     * (ie- true, it's HTML) or shown in a text viewer (ie- false, it's source code)
     */
    protected boolean showInBrowser;
    
    /** 
     * this can be null if the associated CALSourceViewer is not associated
     * with a CALEditor.  This is the case for Embedded CAL
     */
    private CALEditor textEditor;
    
    public CALTextHover(boolean updateOffset, int stateMask){
        this.updateOffset = updateOffset;
        this.stateMask = stateMask;
    }

    public CALTextHover(boolean updateOffset, int stateMask, CALEditor editor){
        this(updateOffset, stateMask);
        this.textEditor = editor;
    }
    
    
    public String getHoverInfo(ITextViewer textViewer, IRegion hoverRegion) {
        // The type info has to be loaded for this to work
        if (!CALBuilder.isEnabled() || !CoreUtility.calBuilderWasInitialized()){
            return null;
        }
        

        final IDocument currentDocument = getDocument();
        
        if (currentDocument != null) {
            final PartiallySynchronizedDocument psd = (PartiallySynchronizedDocument) currentDocument;
            final IDocument document = psd.getOriginalDocument();            
            final int currentOffset = hoverRegion.getOffset();
            final int offset = psd.getOriginalOffset(currentOffset);
            // in new text so there will be no hover information
            if (offset == -1){
                return null;
            }
            try {
                final int firstLine = document.getLineOfOffset(offset);
                final int column = CoreUtility.getColumn(firstLine, offset, document);
                CALModelManager cmm = CALModelManager.getCALModelManager();
 
                ModuleName moduleName;
                try{
                    moduleName = getModuleName(cmm);
                }
                catch(IllegalArgumentException ex){
                    // CAL File is not in the correct spot in the hierarchy so there
                    // is no type information available. Don't show an error message
                    // since this is the wrong thread.
                    return null;
                }
                
                
                ModuleSourceDefinition msd = cmm.getModuleSourceDefinition(moduleName);
                if (!isSyntheticModule() && msd == null) {
                    // hmmm, we are hovering over text in a module but cannot find the module.
                    textEditor.getViewer().getTextWidget().getDisplay().asyncExec(new Runnable() {
                        // ensure we are executing in the proper thread
                        public void run() {
                            showErrorOnStatusLine(ActionMessages.error_messageBadSelection_noTypeInformation_CAL);
                        }
                    });
                    showingSourceCode = false;
                    return null;
                }
                
                /*
                 * If the compile failed then the metrics info is not available so bail early.
                 */
                if (!isSyntheticModule() && !cmm.getProgramModelManager().hasModuleInProgram(moduleName)){
                    // Only show the message if the user presses selects "Show Tooltip Description"
                    // explicitly not when the hover text is shown.
                    if (!updateOffset) {
                        showErrorOnStatusLine(ActionMessages.OpenAction_error_noSourceCodeMetrics);
                    }
                    showingSourceCode = false;
                    return null;
                }                    
                
                CompilerMessageLogger messageLogger = new MessageLogger();
                SourceMetricsManager sourceMetrics = cmm.getSourceMetrics();
                SearchManager searchManager = cmm.getSearchManager();

                // override the offset with the offset of the hover text instead of the cursor position
                // so that if F2 is pressed the correct symbol will be found.
                if (updateOffset) {
                    final CALSourceViewer calSourceViewer = (CALSourceViewer) textViewer;
                    
                    calSourceViewer.setOffset(offset);
                    
                    final StyledText textWidget = textViewer.getTextWidget();

                    // Clear the override
                    textWidget.getDisplay().syncExec(
                            new Runnable(){
                                public void run(){
                                    if (!textWidget.isDisposed()){
                                        textWidget.addListener(SWT.Hide, new Listener(){
                                            public void handleEvent(Event event) {
                                                calSourceViewer.setOffset(-1);
                                            }                                            
                                        });
                                    }
                                }
                            }
                    );
                }

                // !updateOffset means that F2 is pressed. So (showingSourceCode && !updateOffset)
                // means that the hover is showing source code currently and F2 is pressed.
                if (stateMask == SWT.CONTROL|| stateMask == SWT.SHIFT || (showingSourceCode && !updateOffset)){
                    if (stateMask == SWT.SHIFT || stateMask == SWT.CONTROL){
                        showingSourceCode = true;
                    }
                    else{
                        showingSourceCode = false;
                    }
                    return findSourceCode(firstLine, column, moduleName, messageLogger, sourceMetrics);
                }
                else{
                    showingSourceCode = false;
                    return findCALDoc(psd, currentOffset, moduleName, messageLogger, searchManager);
                }
            } catch (BadLocationException e) {
                // will only happen on concurrent modification
                CALEclipseUIPlugin.log(new Status(IStatus.ERROR, CALEclipseUIPlugin.PLUGIN_ID, IStatus.OK, "", e)); //$NON-NLS-1$
                showingSourceCode = false;
                return null;                
            }
        }
        else{
            showingSourceCode = false;
            return null;
        }
    }

    /** 
     * When not associated with a CALEditor, then there is no status line 
     * to show the error on, so ignore.
     */
    private void showErrorOnStatusLine(String errorMsg) {
        if (textEditor != null) {
            CoreUtility.showErrorOnStatusLine(textEditor, errorMsg);
        }
    }

    /**
     * Method getFunction.    
     * @return Function the local function or null if the module does not define a function with the given name.    
     */
    public Function getFunction(ModuleTypeInfo mti, QualifiedName qualifiedName){

        QualifiedName qn;
        String topLevelFunctionName;
        String localFunctionName;
        int index;
        
        {
            final String symbolName = qualifiedName.getUnqualifiedName();

            // functionName@lfi1@lfi2@index
            //             ^--- positionOfSeparator
            //                       ^--- startOfIndex
            final int positionOfSeparator = symbolName.indexOf('@');
            if (positionOfSeparator == -1){
                return null;
            }

            final int startOfIndex = symbolName.lastIndexOf('@');
            if (startOfIndex == positionOfSeparator){
                return null;
            }

            topLevelFunctionName = symbolName.substring(0, positionOfSeparator);
            localFunctionName = symbolName.substring(positionOfSeparator+1, startOfIndex);
            if (localFunctionName.length() == 0){
                return null;
            }
            final String indexString = symbolName.substring(startOfIndex + 1);
            if (indexString.length() == 0){
                return null;
            }
            index = Integer.parseInt(indexString);

            qn = QualifiedName.make(qualifiedName.getModuleName(), topLevelFunctionName);
        }

        final Function function = mti.getFunction(topLevelFunctionName);
        if (function == null){
            return null;
        }
        
        return function.getLocalFunction(qn, localFunctionName, index);
    }

    /**
     * Find the source code that defines the symbol at the given position.
     */
    private String findSourceCode(final int firstLine, final int column, ModuleName moduleName, CompilerMessageLogger messageLogger, SourceMetricsManager sourceMetrics) {
        SearchResult.Precise target = searchForIdentifier(firstLine, column,
                moduleName, messageLogger, sourceMetrics);
        
        if (target == null) {
            return null;
        }
        
        showInBrowser = false;
        
        final List<SearchResult> definitions = sourceMetrics.findDefinition(target, true, messageLogger);
        if (definitions.size() > 0){
            SearchResult.Precise result = (SearchResult.Precise) definitions.get(0);
            final String sourceText = CALModelManager.getCALModelManager().getModuleSource(result.getName().getModuleName());
            String typeDeclarationText = "";
            if (result.getName() instanceof QualifiedName){
                final QualifiedName functionName = (QualifiedName) result.getName();
                final SearchResult.Precise typeDeclarationResult = sourceMetrics.findTypeDeclaration(functionName, messageLogger);
                // if the type declaration is in the source code
                if (typeDeclarationResult != null){
                    typeDeclarationText = typeDeclarationResult.getSourceRange().getSelection(sourceText) + ";\n";
                }
                else{
                    // The type declaration is not in the source code so calculate it.
                    // calculate the type info
                    final ModuleTypeInfo moduleTypeInfo = CALModelManager.getCALModelManager().getModuleTypeInfo(result.getName().getModuleName());
                    Function function = moduleTypeInfo.getFunction(functionName.getUnqualifiedName());
                    if (function == null){
                        // check for local function
                        function = getFunction(moduleTypeInfo, functionName);
                    }
                    // Don't show the type for foreign functions since the signature has it already
                    if (function != null && function.getForeignFunctionInfo() == null && !function.isPrimitive()){
                        typeDeclarationText = function.getUnqualifiedDisplayName() + " :: " + function.getTypeExpr().toString(true, new ScopedEntityNamingPolicy.UnqualifiedUnlessAmbiguous(moduleTypeInfo)) + "; // (inferred)\n";
                    }
                }
            }
            final String definingText;
            
            final Category category = result.getCategory();
            if (category == Category.DATA_CONSTRUCTOR){
                definingText = result.getSourceRange().getSelection(sourceText);
            }
            else{
                definingText = getSelectionAndTrailingSemiColon(result.getSourceRange(), sourceText);
            }
                
            return typeDeclarationText + tidyUp(definingText, result.getSourceRange());
        }
        else{
            return null;  
        }
    }

    /**
     * finds the symbol at the particular line and column.
     * 
     * subclasses that are not associated with a CALEditor can override
     * @param firstLine
     * @param column
     * @param moduleName
     * @param messageLogger
     * @param sourceMetrics
     * @return the symbol at the given line and column
     */
    protected SearchResult.Precise searchForIdentifier(final int firstLine,
            final int column, ModuleName moduleName,
            CompilerMessageLogger messageLogger,
            SourceMetricsManager sourceMetrics) {
        SearchResult.Precise[] results = sourceMetrics.findSymbolAt(moduleName, firstLine+1, column+1, messageLogger);
        if (results == null || results.length != 1) {
            return null;
        }
        SearchResult.Precise target = results[0];
        return target;
    }

    /**
     * Gets the source text and any trailing semi-colons. This is a hack that will be removed once
     * the source ranges of source model elements have the correct values.
     * TODO remove this function when the source model is fixed.
     * @param sourceText sourceText that the source range refers to
     * @return the text in the sourceText string covered by this source range.
     */
    public String getSelectionAndTrailingSemiColon(SourceRange sourceRange, String sourceText){
        final int startIndex = sourceRange.getStartSourcePosition().getPosition(sourceText);
        int endIndex = sourceRange.getEndSourcePosition().getPosition(sourceText);
        // move the end index past the trailing semi-colons
        //  (space | tab | newline | semicolon)* (semicolon)
        final int lastIndex = sourceText.length();
        int lastSemicolon = endIndex;
        while(endIndex < lastIndex){
            final char ch = sourceText.charAt(endIndex);
            if (Character.isWhitespace(ch)){
                // continue
            }
            else if (ch == ';'){
                lastSemicolon = endIndex;
            }
            else{
                break;
            }
            ++endIndex;
        }
        
        return sourceText.substring(startIndex, lastSemicolon+1);
    }

    /**
     * The source code can come in indented. I want to remove the indentation since this is pointless
     * For example
     * 
     *              public "char"
     *   public Char deriving Eq, Ord, 
     *       Inputable, Outputable;
     *   
     * will be converted to
     * 
     * public "char"
     * public Char deriving Eq, Ord, 
     *     Inputable, Outputable;
     * 
     * @param sourceCode the source code to tidy up.
     * @return the tidied up source code. 
     */
    private String tidyUp(String sourceCode, SourceRange sourceRange){
        String[] lines = sourceCode.split("\\n");
        final int length = lines.length;
        int min = sourceRange.getStartColumn() - 1;
        for(int i = 1; i < length; ++i){
            String line = lines[i];
            final int nLeadingSpaces = getNLeadingSpaces(line);
            if (nLeadingSpaces < min && (nLeadingSpaces != line.length())){
                min = nLeadingSpaces;
            }
        }
        
        final StringBuilder tidiedCode = new StringBuilder();
        // The first line is always moved over to the start
        tidiedCode.append(lines[0].substring(getNLeadingSpaces(lines[0])));
        tidiedCode.append('\n');
        // For the rest of the lines remove the same leading blanks.
        for(int i = 1; i < length; ++i){
            // Don't substring the all blank lines
            if (lines[i].length() > min){
                tidiedCode.append(lines[i].substring(min));
            }
            tidiedCode.append('\n');
        }
        return tidiedCode.toString();
    }
    
    private int getNLeadingSpaces(String string){
        final int length = string.length();
        for(int i = 0; i < length; ++i){
            if (string.charAt(i) != ' '){
                return i;
            }
        }
        return length;
    }
    
    /**
     * Retrieve the CALdoc for the selected symbol if any. 
     * @param psd
     * @param currentOffset
     * @param moduleName Name of the module to search
     * @param messageLogger
     * @param searchManager
     * @return the CALDoc for the symbol at the given position. If no CALDoc exists, null will be returned.
     * @throws BadLocationException 
     */
    private String findCALDoc(final PartiallySynchronizedDocument psd, final int currentOffset, ModuleName moduleName, CompilerMessageLogger messageLogger, SearchManager searchManager) throws BadLocationException {

        final Pair<SourcePosition, SourcePosition> posAndPosToTheLeft = CALHyperlinkDetector.getCurrentPositionAndPositionToTheLeft(psd, currentOffset, moduleName);
        // current position is in new code so no hovertext is available
        if (posAndPosToTheLeft == null){
            return null;
        }
        
        IdentifierOccurrence<?> result = searchManager.findSymbolAt(moduleName, posAndPosToTheLeft.fst(), posAndPosToTheLeft.snd(), messageLogger);
        if (result == null) {
            return null;
        }
        showInBrowser = true;
        return findCALDocForOccurrence(moduleName, result);
    }
    
    protected String findCALDocForOccurrence(ModuleName currentModuleName, IdentifierOccurrence<?> occurrence) {
        ProgramModelManager programModelManager = CALModelManager.getCALModelManager().getProgramModelManager();
        IdentifierInfo name = occurrence.getIdentifierInfo();
        
        if (name instanceof IdentifierInfo.Module){
            final ModuleTypeInfo resultModuleTypeInfo = programModelManager.getModuleTypeInfo(((IdentifierInfo.Module)name).getResolvedName());
            
            if (resultModuleTypeInfo == null){
                return "";
            }
            else{
                return CALDocToTooltipHTMLUtilities.getHTMLForCALDocCommentOfModule(
                        programModelManager, 
                        resultModuleTypeInfo);
            }
        } else if (name instanceof IdentifierInfo.TypeVariable) {
            return CALDocToTooltipHTMLUtilities.getHTMLForTypeVariable(programModelManager, currentModuleName, (IdentifierInfo.TypeVariable)name);
            
        } else if (name instanceof IdentifierInfo.RecordFieldName) {
            return CALDocToTooltipHTMLUtilities.getHTMLForRecordFieldName(programModelManager, currentModuleName, (IdentifierInfo.RecordFieldName)name);
            
        } else if (name instanceof IdentifierInfo.DataConsFieldName) {
            IdentifierInfo.DataConsFieldName fieldName =
                (IdentifierInfo.DataConsFieldName)name;
            
            if (fieldName.getAssociatedDataConstructors().size() == 0) {
                throw new IllegalStateException();
            }
            
            ModuleName dataConsModule = fieldName.getFirstAssociatedDataConstructor().getResolvedName().getModuleName();
            ModuleTypeInfo resultModuleTypeInfo = programModelManager.getModuleTypeInfo(dataConsModule);
            if (resultModuleTypeInfo == null) {
                return null;
            }
            
            List<DataConstructor> dataConsList = new ArrayList<DataConstructor>();
            for (final IdentifierInfo.TopLevel.DataCons assocDataCons : fieldName.getAssociatedDataConstructors()) {
                DataConstructor dataCons = resultModuleTypeInfo.getDataConstructor(assocDataCons.getResolvedName().getUnqualifiedName());
                if (dataCons != null){
                    dataConsList.add(dataCons);
                }
            }
            
            if (dataConsList.isEmpty()) {
                return null;
            }
            
            return CALDocToTooltipHTMLUtilities.getHTMLForCALDocCommentOfDataConsFieldName(programModelManager, dataConsList, fieldName);
            
        } else if (name instanceof IdentifierInfo.Local) {
            ModuleTypeInfo resultModuleTypeInfo = programModelManager.getModuleTypeInfo(currentModuleName);
            if (resultModuleTypeInfo == null) {
                return null;
            }

            // Look for CALDoc for the various kinds of local identifiers

            if (name instanceof IdentifierInfo.Local.Function){
                IdentifierInfo.Local.Parameter.Function localName =
                    (IdentifierInfo.Local.Parameter.Function)name;
                
                final LocalFunctionIdentifier localFunctionIdentifier = localName.getLocalFunctionIdentifier();
                Function topLevelFunction = resultModuleTypeInfo.getFunction(localFunctionIdentifier.getToplevelFunctionName().getUnqualifiedName());
                if (topLevelFunction != null) {
                    Function localFunction = topLevelFunction.getLocalFunction(localFunctionIdentifier);
                    if (localFunction != null) {
                        return CALDocToTooltipHTMLUtilities.getHTMLForCALDocCommentOfLocalFunction(programModelManager, localFunction, localName);
                    }
                }
                
            } else if (name instanceof IdentifierInfo.Local.PatternMatchVariable) {
                IdentifierInfo.Local.Parameter.PatternMatchVariable localName =
                    (IdentifierInfo.Local.Parameter.PatternMatchVariable)name;
                
                final LocalFunctionIdentifier localFunctionIdentifier = localName.getLocalFunctionIdentifier();
                Function topLevelFunction = resultModuleTypeInfo.getFunction(localFunctionIdentifier.getToplevelFunctionName().getUnqualifiedName());
                if (topLevelFunction != null) {
                    Function localFunction = topLevelFunction.getLocalFunction(localFunctionIdentifier);
                    if (localFunction != null) {
                        return CALDocToTooltipHTMLUtilities.getHTMLForCALDocCommentOfLocalPatternMatchVar(programModelManager, localFunction, localName);
                    }
                }
                
            } else if (name instanceof IdentifierInfo.Local.Parameter.TopLevelFunctionOrClassMethod) {
                IdentifierInfo.Local.Parameter.TopLevelFunctionOrClassMethod paramName =
                    (IdentifierInfo.Local.Parameter.TopLevelFunctionOrClassMethod)name;
                
                FunctionalAgent function = resultModuleTypeInfo.getFunctionOrClassMethod(paramName.getAssociatedFunction().getResolvedName().getUnqualifiedName());
                if (function != null){
                    return CALDocToTooltipHTMLUtilities.getHTMLForCALDocCommentOfFunctionParameter(programModelManager, function, paramName);
                }
                
            } else if (name instanceof IdentifierInfo.Local.Parameter.LocalFunction) {
                IdentifierInfo.Local.Parameter.LocalFunction paramName =
                    (IdentifierInfo.Local.Parameter.LocalFunction)name;
                
                final LocalFunctionIdentifier localFunctionIdentifier = paramName.getAssociatedFunction().getLocalFunctionIdentifier();
                Function topLevelFunction = resultModuleTypeInfo.getFunction(localFunctionIdentifier.getToplevelFunctionName().getUnqualifiedName());
                if (topLevelFunction != null) {
                    Function localFunction = topLevelFunction.getLocalFunction(localFunctionIdentifier);
                    if (localFunction != null) {
                        return CALDocToTooltipHTMLUtilities.getHTMLForCALDocCommentOfFunctionParameter(programModelManager, localFunction, paramName);
                    }
                }
            }
            
            // In all other cases, just return a simple tooltip with the kind of identifier and name displayed
            return CALDocToTooltipHTMLUtilities.getHTMLForSimpleLocalVariable(programModelManager, currentModuleName, (IdentifierInfo.Local)name);
            
        } else if (name instanceof IdentifierInfo.TopLevel){
            QualifiedName resultQualifiedName = ((IdentifierInfo.TopLevel)name).getResolvedName();
            ModuleTypeInfo resultModuleTypeInfo = programModelManager.getModuleTypeInfo(resultQualifiedName.getModuleName());
            if (resultModuleTypeInfo == null) {
                return null;
            }
            
            // Look for CALDoc for the various kinds of top-level identifiers

            // Functions

            if (name instanceof IdentifierInfo.TopLevel.FunctionOrClassMethod){
                FunctionalAgent function = resultModuleTypeInfo.getFunctionOrClassMethod(resultQualifiedName.getUnqualifiedName());
                if (function != null){
                    return CALDocToTooltipHTMLUtilities.getHTMLForCALDocCommentOfScopedEntity(programModelManager, function);
                }
            }

            // Type constructors

            if (name instanceof IdentifierInfo.TopLevel.TypeCons){
                TypeConstructor typeCons = resultModuleTypeInfo.getTypeConstructor(resultQualifiedName.getUnqualifiedName());
                if (typeCons != null){
                    return CALDocToTooltipHTMLUtilities.getHTMLForCALDocCommentOfScopedEntity(programModelManager, typeCons);
                }
            }

            // Data Constructors

            if (name instanceof IdentifierInfo.TopLevel.DataCons){            
                DataConstructor dataCons = resultModuleTypeInfo.getDataConstructor(resultQualifiedName.getUnqualifiedName());
                if (dataCons != null){
                    return CALDocToTooltipHTMLUtilities.getHTMLForCALDocCommentOfScopedEntity(programModelManager, dataCons);
                }
            }


            // Type Classes

            if (name instanceof IdentifierInfo.TopLevel.TypeClass){
                TypeClass typeClass = resultModuleTypeInfo.getTypeClass(resultQualifiedName.getUnqualifiedName());
                if (typeClass != null){
                    return CALDocToTooltipHTMLUtilities.getHTMLForCALDocCommentOfScopedEntity(programModelManager, typeClass);
                }
            }
        }
        // to Do: doci, docim
        return null;
    }

    public IRegion getHoverRegion(ITextViewer textViewer, int offset) {
        Point selection = textViewer.getSelectedRange();
        if (selection.x <= offset && offset < selection.x + selection.y) {
            return new Region(selection.x, selection.y);
        }
        return new Region(offset, 0);
    }

    /**
     * {@inheritDoc}
     */
    public IInformationControlCreator getHoverControlCreator() {
        return new IInformationControlCreator() {
            public IInformationControl createInformationControl(Shell parent) {
                int shellStyle = SWT.TOOL | SWT.NO_TRIM;
                int style = SWT.NONE;
                if (showInBrowser && BrowserInformationControl.isAvailable(parent)) {
                    // Show in HTML browser
                    return new BrowserInformationControl(parent, shellStyle, style, CALUIMessages.GetHoverFocus + "  " + CALUIMessages.GetCodeHoverAffordance);
                } else {
                    // Show in text viewer
                    return new CALSourceInformationControl(parent, shellStyle, style, CALUIMessages.GetHoverFocus);
                }
            }
        };
    }

    /**
     * Subclasses not associated with a CALEditor can override
     * @param cmm 
     * @return the module name of the CAL code being hovered over
     */
    protected ModuleName getModuleName(CALModelManager cmm) {
        return CALModelManager.getCALModelManager().getModuleName(textEditor.getStorage()); 
    }

    /** 
     * Subclasses that do are not tied to a CALEditor can override
     * @return the document of the hover
     */
    protected IDocument getDocument() {
        return ActionUtilities.getDocument(textEditor);
    }
 
    
    /**
     * Override if the module used in the hover does not exist in the CALModelManager
     * @return true if the module does not exist in the CALModelManager, false if it does
     */
    protected boolean isSyntheticModule() {
        return false;
    }
}
