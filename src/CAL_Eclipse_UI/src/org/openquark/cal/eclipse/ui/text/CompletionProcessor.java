/*******************************************************************************
 * Copyright (c) 2006 Business Objects Software Limited and others.
 * All rights reserved. 
 * This file is made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Business Objects Software Limited - initial API and implementation based on Eclipse 3.1.2 code for
 *                             /org.eclipse.jdt.ui/org/eclipse/jdt/internal/ui/text/java/ContentAssistProcessor.java
 *                           Eclipse source is available at: http://www.eclipse.org/downloads/
 *******************************************************************************/
package org.openquark.cal.eclipse.ui.text;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.TreeSet;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.BadPartitioningException;
import org.eclipse.jface.text.DocumentEvent;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IDocumentExtension3;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.ITextViewer;
import org.eclipse.jface.text.ITypedRegion;
import org.eclipse.jface.text.TextSelection;
import org.eclipse.jface.text.contentassist.ICompletionProposal;
import org.eclipse.jface.text.contentassist.IContentAssistProcessor;
import org.eclipse.jface.text.contentassist.IContextInformation;
import org.eclipse.jface.text.contentassist.IContextInformationValidator;
import org.eclipse.jface.text.templates.DocumentTemplateContext;
import org.eclipse.jface.text.templates.Template;
import org.eclipse.jface.text.templates.TemplateCompletionProcessor;
import org.eclipse.jface.text.templates.TemplateContext;
import org.eclipse.jface.text.templates.TemplateContextType;
import org.eclipse.jface.viewers.DecoratingLabelProvider;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.openquark.cal.caldoc.CALDocToJavaDocUtilities;
import org.openquark.cal.compiler.CALDocComment;
import org.openquark.cal.compiler.CompilerMessageLogger;
import org.openquark.cal.compiler.DataConstructor;
import org.openquark.cal.compiler.FieldName;
import org.openquark.cal.compiler.FunctionalAgent;
import org.openquark.cal.compiler.LanguageInfo;
import org.openquark.cal.compiler.MessageLogger;
import org.openquark.cal.compiler.ModuleContainer;
import org.openquark.cal.compiler.ModuleName;
import org.openquark.cal.compiler.ModuleTypeInfo;
import org.openquark.cal.compiler.QualifiedName;
import org.openquark.cal.compiler.ScopedEntity;
import org.openquark.cal.compiler.SourceIdentifier;
import org.openquark.cal.compiler.TypeClass;
import org.openquark.cal.compiler.TypeConstructor;
import org.openquark.cal.compiler.Refactorer.AutoCompleteWithInsertImport;
import org.openquark.cal.eclipse.core.CALModelManager;
import org.openquark.cal.eclipse.core.formatter.DefaultCodeFormatterConstants;
import org.openquark.cal.eclipse.ui.CALEclipseUIPlugin;
import org.openquark.cal.eclipse.ui.actions.ActionMessages;
import org.openquark.cal.eclipse.ui.caleditor.CALEditor;
import org.openquark.cal.eclipse.ui.caleditor.CALSourceViewer;
import org.openquark.cal.eclipse.ui.templates.CALDocTemplateContextType;
import org.openquark.cal.eclipse.ui.templates.CALTemplateContextType;
import org.openquark.cal.eclipse.ui.util.CoreUtility;
import org.openquark.cal.eclipse.ui.util.Messages;
import org.openquark.cal.eclipse.ui.views.ForeignDecorator;
import org.openquark.cal.eclipse.ui.views.ScopeDecorator;
import org.openquark.cal.eclipse.ui.views.ScopedEntityLabelProvider;
import org.openquark.cal.services.AutoCompleteHelper;
import org.openquark.util.IteratorChain;
import org.openquark.util.Pair;

/**
 * The class that implements auto-complete in the CAL Editor.
 * 
 * It proposes both source code and template completions 
 * 
 * @author Greg McClement
 * @author Andrew Eisenberg
 */
public class CompletionProcessor extends TemplateCompletionProcessor implements IContentAssistProcessor {

    private final DecoratingLabelProvider labelProvider =
        new DecoratingLabelProvider(
        new DecoratingLabelProvider(
                new ScopedEntityLabelProvider(), 
                new ScopeDecorator()),
                new ForeignDecorator());
    private static Image image_nav_namespace = CALEclipseUIPlugin.getImageDescriptor("/icons/nav_namespace.png").createImage();
    private static Image image_nav_module = CALEclipseUIPlugin.getImageDescriptor("/icons/nav_module.png").createImage();


    /**
     * Compares 2 completion proposals
     * this class is used for ordering of proposals in the pop-up proposal window
     * 
     * Proposals are ordered alphabetically by display name
     */
    private class ProposalComparator implements Comparator<ICompletionProposal> {
        public int compare(ICompletionProposal proposal1, ICompletionProposal proposal2) {

            // Field completions show first
            {
                if (proposal1 instanceof FieldCompletionProposal) {
                    if (proposal2 instanceof FieldCompletionProposal) {
                        return ((FieldCompletionProposal) proposal1).compareTo(proposal2);
                    } else {
                        return -1;
                    }
                } else {
                    if (proposal2 instanceof FieldCompletionProposal) {
                        return 1;
                    } else {
                        // same so check other fields
                    }
                }
            }
                   
            // Module and hierarchical name components show next (unless all things are shown then skip this).
            {
                if (proposal1 instanceof org.eclipse.jface.text.contentassist.CompletionProposal) {
                    final org.eclipse.jface.text.contentassist.CompletionProposal cp1 = (org.eclipse.jface.text.contentassist.CompletionProposal) proposal1;
                    if (proposal2 instanceof org.eclipse.jface.text.contentassist.CompletionProposal) {
                        final org.eclipse.jface.text.contentassist.CompletionProposal cp2 = (org.eclipse.jface.text.contentassist.CompletionProposal) proposal2;
                        return (cp1).getDisplayString().compareTo((cp2).getDisplayString());
                    } else {
                        // only sort the hierarchical names first if there is context that matches the name 
                        if (cp1.getAdditionalProposalInfo() == null){
                            return -1;
                        }
                    }
                } else {
                    if (proposal2 instanceof org.eclipse.jface.text.contentassist.CompletionProposal) {
                        final org.eclipse.jface.text.contentassist.CompletionProposal cp2 = (org.eclipse.jface.text.contentassist.CompletionProposal) proposal2;
                        if (cp2.getAdditionalProposalInfo() == null){
                            return 1;
                        }
                    } else {
                        // same so check other fields
                    }
                }
            }
            
            
            // check to see if both are of CAL proposals
            // otherwise at least one of them is a template proposal
            if (proposal1 instanceof CompletionProposal && proposal2 instanceof CompletionProposal) {
                
                CompletionProposal cp1 = (CompletionProposal) proposal1;
                CompletionProposal cp2 = (CompletionProposal) proposal2;
    
                // Show the proposals where the unqualified name
                // matches the string
                // the user type in first, show the ones where part
                // of the hierarchical
                // name matches next.
                {
                    if (cp1.matchesUnqualifiedName == cp2.matchesUnqualifiedName) {
                        // same value so try comparing the other
                        // values.
                    } else if (cp1.matchesUnqualifiedName) {
                        return -1;
                    } else {
                        return 1;
                    }
                }
    
                {
                    final int compare = cp1.name.getUnqualifiedName().compareToIgnoreCase(cp2.name.getUnqualifiedName());
                    if (compare != 0) {
                        return compare;
                    }
                }
    
                return cp1.name.getModuleName().compareTo(cp2.name.getModuleName());
    
            } else if (proposal1 instanceof ICompletionProposal &&
                       proposal2 instanceof ICompletionProposal) {
                // at least one proposal is a template proposal
                // make template proposals go last
                    
                return proposal1.getDisplayString().compareToIgnoreCase(proposal2.getDisplayString());
                
            } else {
                // shouldn't happen...at least one is not a completion proposal at 
                // all
                return 0;
            }
        }
    }

    /**
     * Completions for the current selection in the UI.
     * 
     * @author Greg McClement
     */
    abstract class CompletionProposal extends TemplateProposal {
        /**
         * The offset of the first character to be replaced with the
         * auto-complete symbol.
         */
        protected final int startOfReplacementZone;
    
        /**
         * The offset of the last character to be replaced with the
         * auto-complete symbol.
         */
        protected final int endOfReplacementZone;
    
        /**
         * The entity corresponding to the symbol being inserted.
         */
        protected final ScopedEntity scopedEntity;
    
        /**
         * The name of the scopedEntity;
         */
        protected final QualifiedName name;
    
        /**
         * Show the ones where the unqualified name matches first
         */
        protected final boolean matchesUnqualifiedName;
    
        /**
         * The module container to use for the completion proposal
         */
        private final ModuleContainer moduleContainer;
        protected final MatchInformation matchInformation;
    
        protected CompletionProposal(
                int startOfReplacementZone,
                int endOfReplacementZone, 
                ScopedEntity scopedEntity,
                boolean matchesUnqualifiedName,
                ModuleContainer moduleContainer,
                
                Template template, 
                TemplateContext context, 
                IRegion region, 
                Image image, 
                int relevance,
                MatchInformation matchInformation) {
            super(template, context, region, image, relevance);
            this.startOfReplacementZone = startOfReplacementZone;
            this.endOfReplacementZone = endOfReplacementZone;
            this.scopedEntity = scopedEntity;
            this.matchInformation = matchInformation;
            this.name = scopedEntity.getName();
            this.matchesUnqualifiedName = matchesUnqualifiedName;
            this.moduleContainer = moduleContainer;                       
        }

        private final char[] triggerCharacters = {' ', ';', '.'};
        @Override
        public char[] getTriggerCharacters(){
            return triggerCharacters;
        }
                
        /**
         * This figures out what to add to the textEditor. For functional agents arguments will
         * be added. If the names exist then the argument names are used otherwise the type
         * information is used.
         * 
         * Examples:
         * 
         * Symbol                       Replacement String
         * ======                       ==================
         * abs                          Prelude.abs (Cal.Core.Prelude.Num a => a)
         * takeWhile                    takeWhile (takeWhileTrueFunction) (array)
         * 
         * @return The auto-completion string, the offset to the first argument or end of the 
         * inserted symbol if there is not argument, the length of the string to highlight which is 
         * usually the length of the first argument.
         */
        protected abstract Object[] getReplacementString(
                ModuleContainer moduleContainer);

        @Override
        public void apply(ITextViewer viewer, char trigger, int stateMask, int offset){            
            try {
                final Object[] replacementStringAndCursorPosition = getReplacementString(moduleContainer);
                final String replacementString = (String) replacementStringAndCursorPosition[0];
                // If there is any text currently selected then that should
                // be removed as well. The
                // extraLengthToRemove figure out how long the select text
                // is
                int extraLengthToRemove = 0;
                {
                    final ISelectionProvider selectionProvider = textEditor.getSelectionProvider();
                    final ISelection selection = selectionProvider.getSelection();
                    if (selection instanceof TextSelection) {
                        final TextSelection textSelection = (TextSelection) selection;
                        extraLengthToRemove = textSelection.getLength();
                    }
                }
    
                AutoCompleteWithInsertImport refactorer = null;
                {
                    // If there is need for it set up a refactorer that does
                    // the auto-include of the
                    // symbol in the import statements.
                    final ModuleName refactorer_moduleName = (ModuleName) replacementStringAndCursorPosition[3];
                    final boolean addImports = 
                        fPreferenceStore.getBoolean(DefaultCodeFormatterConstants.ENABLE_ADD_IMPORT_INSTEAD_OF_QUALIFIED_NAME);

                    if (addImports && refactorer_moduleName != null){
                        final QualifiedName refactorer_fullName = (QualifiedName) replacementStringAndCursorPosition[4];
                        final SourceIdentifier.Category refactorer_fullNameCategory = (SourceIdentifier.Category) replacementStringAndCursorPosition[5]; 
                        final int oldTextOffset = startOfReplacementZone;
                        final int oldTextLength = endOfReplacementZone - startOfReplacementZone + extraLengthToRemove;
                        final IDocument document = viewer.getDocument();
                        final int firstLine = document.getLineOfOffset(oldTextOffset);
                        final int column = CoreUtility.getColumn(firstLine, oldTextOffset, document);
    
                        refactorer =                         
                            new AutoCompleteWithInsertImport(
                                    moduleContainer,
                                    refactorer_moduleName,
                                    refactorer_fullName,
                                    refactorer_fullNameCategory,
                                    document.get(oldTextOffset, oldTextLength),
                                    replacementString,
                                    firstLine + 1, 
                                    column + 1
                            );
                    }
                }
                if (refactorer != null) {
                    CompilerMessageLogger messageLogger = new MessageLogger();
                    refactorer.calculateModifications(messageLogger);
                    refactorer.apply(messageLogger);
                    final int cursorPosition = CoreUtility.toOffset(refactorer.getNewSourcePosition(), viewer.getDocument());
                    
                    // funny business to adjust the offset based on how 
                    // much the user had typed in to get the pattern match
                    final int startOffset = super.getReplaceOffset();
                    final int endOffset = Math.max(super.getReplaceEndOffset(), offset);
                    assert endOffset >= startOffset;

                    offsetShift = cursorPosition - offset + (endOffset - startOffset);

                    // There will probably be errors since the
                    // auto-completed code is
                    // unlikely to be valid CAL code. Ignore these and the
                    // compiler
                    // if invoked will pick them up.
                   messageLogger = null;
                }
                else{
                    offsetShift = 0;
                }
            } catch (BadLocationException e) {
                CALEclipseUIPlugin.log(new Status(IStatus.ERROR,
                        CALEclipseUIPlugin.PLUGIN_ID, IStatus.OK, "", e)); //$NON-NLS-1$
            }
            
            super.apply(viewer, trigger, stateMask, offset + offsetShift);
        }

        private int offsetShift = 0;
        
        @Override
        protected /*final*/ int getReplaceOffset() {
            return super.getReplaceOffset() + offsetShift;
        }

        @Override
        protected /*final*/ int getReplaceEndOffset() {
            return super.getReplaceEndOffset() + offsetShift;
        }
            
        @Override
        public String getAdditionalProposalInfo() {
            return null;
        }
    
        @Override
        public IContextInformation getContextInformation() {
            return null;
        }
    
        @Override
        public abstract String getDisplayString();
    
        @Override
        public Point getSelection(IDocument document) {
            return null;
        }
    }

    /**
     * The completion proposal for everything but field accesses in DataConstructors.
     */
    class DefaultCompletionProposal extends CompletionProposal{            
        
        DefaultCompletionProposal(
                int startOfReplacementZone, 
                int endOfReplacementZone, 
                ScopedEntity scopedEntity, 
                boolean matchesUnqualifiedName, 
                ModuleContainer moduleContainer,
                
                Template template, 
                TemplateContext context, 
                IRegion region, 
                Image image, 
                int relevance,
                MatchInformation matchInformation
                ){
            super(startOfReplacementZone, endOfReplacementZone, scopedEntity, matchesUnqualifiedName, moduleContainer,
                    template, context, region, image, relevance, matchInformation);
        }

        @Override
        public boolean validate(IDocument document, int offset, DocumentEvent event){
            try {                
                int replaceOffset= getReplaceOffset();
                if (offset >= replaceOffset) {
                    MatchInformation matchInformation = new MatchInformation(document, offset);
                    return matches(matchInformation, scopedEntity);
                }
            } catch (BadLocationException e) {
                // concurrent modification - ignore
            }
            return false;
        }
        
        /**
         * This figures out what to add to the textEditor. For functional agents arguments will
         * be added. If the names exist then the argument names are used otherwise the type
         * information is used.
         * 
         * Examples:
         * 
         * Symbol                       Replacement String
         * ======                       ==================
         * abs                          Prelude.abs (Cal.Core.Prelude.Num a => a)
         * takeWhile                    takeWhile (takeWhileTrueFunction) (array)
         * 
         * @return The auto-completion string, the offset to the first argument or end of the 
         * inserted symbol if there is not argument, the length of the string to highlight which is 
         * usually the length of the first argument and potentially information to set up a refactorer.
         */
        @Override
        protected Object[] getReplacementString(ModuleContainer moduleContainer){
            if (scopedEntity instanceof FunctionalAgent || scopedEntity instanceof TypeConstructor){
                String[] argumentNames;
                if (scopedEntity instanceof FunctionalAgent){
                    FunctionalAgent function = (FunctionalAgent) scopedEntity;
                    CALDocComment calDocComment = function.getCALDocComment();
                    argumentNames = CALDocToJavaDocUtilities.getArgumentNamesFromCALDocComment(calDocComment, function);
                }
                else{
                    TypeConstructor typeConstructor = (TypeConstructor) scopedEntity;
                    argumentNames = CoreUtility.getArgumentNamesFromCALDocComment(typeConstructor);
                }
                
                final StringBuilder replacementString = new StringBuilder();
                // add symbol name to the result.
                Object replacementNameAndRefactorer[] = determineInsertionName(scopedEntity, matchInformation);
                final String replacementName = (String) replacementNameAndRefactorer[0];
                final ModuleName refactorer_moduleName = (ModuleName) replacementNameAndRefactorer[1];
                final QualifiedName refactorer_fullName = (QualifiedName) replacementNameAndRefactorer[2];
    
                replacementString.append(replacementName);
                int cursorPosition = replacementString.length();
                int length = 0;
                for (int i = 0; i < argumentNames.length; ++i) {
                    String argumentName = argumentNames[i];
                    final boolean useParens = !LanguageInfo.isValidFunctionName(argumentName);
                    if (useParens){
                        replacementString.append(" (");
                    }
                    else{
                        replacementString.append(" ");
                    }
                    replacementString.append(argumentName);
                    // keep track of the offset to and the length of the
                    // first argument string
                    if (i == 0) {
                        length = argumentName.length();
                        if (useParens){
                            // length of " (" since
                            // I want the cursor
                            // right after the '('
                            cursorPosition += 2; 
                        }
                        else{
                            cursorPosition += 1;
                        }
                    }
                    if (useParens){
                        replacementString.append(')');
                    }
                }
    
                return new Object[] {replacementString.toString(), Integer.valueOf(cursorPosition), Integer.valueOf(length), refactorer_moduleName, refactorer_fullName, CoreUtility.toCategory(scopedEntity)};
            } else {
                Object replacementNameAndRefactorer[] = determineInsertionName(scopedEntity, matchInformation);
                final String replacementName = (String) replacementNameAndRefactorer[0];
                final ModuleName refactorer_moduleName = (ModuleName) replacementNameAndRefactorer[1];
                final QualifiedName refactorer_fullName = (QualifiedName) replacementNameAndRefactorer[2];
                return new Object[] {replacementName, Integer.valueOf(replacementName.length()), Integer.valueOf(0), refactorer_moduleName, refactorer_fullName, CoreUtility.toCategory(scopedEntity)};
            }
        }
    
        @Override
        public String getDisplayString() {
            return Messages.format(
                    ActionMessages.AutoCompleteAction_displayNameAndModule,
                    new Object[] { name.getUnqualifiedName(),
                            name.getModuleName() });
        }
    
        @Override
        public Image getImage() {
            return labelProvider.getImage(scopedEntity);
        }
    
    }

    /**
         * The completion proposal for fields of data constructors.
         */
        class FieldCompletionProposal extends CompletionProposal {
    
            private final FieldName fieldName;
    
            public FieldCompletionProposal(
                    int startOfReplacementZone,
                    int endOfReplacementZone, 
                    ScopedEntity scopedEntity,
                    boolean matchesUnqualifiedName, 
                    FieldName fieldName,
                    ModuleContainer moduleContainer,
                    
                    Template template, 
                    TemplateContext context, 
                    IRegion region, 
                    Image image, 
                    int relevance,
                    MatchInformation matchInformation) {
                super(startOfReplacementZone, endOfReplacementZone, scopedEntity, matchesUnqualifiedName, moduleContainer,
                        template, context, region, image, relevance, matchInformation);
                assert (scopedEntity instanceof DataConstructor);
                this.fieldName = fieldName;
            }

            @Override
            public boolean validate(IDocument document, int offset, DocumentEvent event){
                try {                
                    int replaceOffset= getReplaceOffset();
                    if (offset >= replaceOffset) {
                        MatchInformation matchInformation = new MatchInformation(document, offset);
                        return matches(matchInformation, scopedEntity, fieldName);
                    }
                } catch (BadLocationException e) {
                    // concurrent modification - ignore
                }
                return false;
            }
            
            public int compareTo(Object o) {
                if (o instanceof FieldCompletionProposal) {
                    final FieldCompletionProposal fcp2 = (FieldCompletionProposal) o;
                    int compare = fieldName.compareTo(fcp2.fieldName);
                    if (compare != 0) {
                        return compare;
                    }
                }
                return 0;
            }
    
            /**
             * This figures out what to add to the textEditor. For functional agents arguments will
             * be added. If the names exist then the argument names are used otherwise the type
             * information is used.
             * 
             * Examples:
             * 
             * Symbol                       Replacement String
             * ======                       ==================
             * abs                          Prelude.abs (Cal.Core.Prelude.Num a => a)
             * takeWhile                    takeWhile (takeWhileTrueFunction) (array)
             * 
             * @return The auto-completion string, the offset to the first argument or end of the 
             * inserted symbol if there is not argument, the length of the string to highlight which is 
             * usually the length of the first argument, and possibly a refactorer.
             */
            @Override
            protected Object[] getReplacementString(ModuleContainer moduleContainer) {
                final StringBuilder replacementString = new StringBuilder();
                // add symbol name to the result.
                final Object[] replacementNameAndRefactorer = determineInsertionName(scopedEntity, matchInformation);
                final String replacementName = (String) replacementNameAndRefactorer[0];
                replacementString.append(replacementName);
                replacementString.append(".");
                replacementString.append(fieldName.getCalSourceForm());
                int cursorPosition = replacementString.length();
                int length = 0;
                return new Object[] { replacementString.toString(),
                        Integer.valueOf(cursorPosition), Integer.valueOf(length), null };
            }
    
            @Override
            public String getDisplayString() {
                final String fieldNameString = fieldName.getCalSourceForm();
                return Messages.format(
                        ActionMessages.AutoCompleteAction_displayFieldName,
                        new Object[] { fieldNameString, name.toString() });
            }
    
            @Override
            public Image getImage() {
                return CALEclipseUIPlugin.getImageDescriptor("icons/cal_16x16.jpg").createImage();
            }
    
        }

    /**
     * the image that is next to all template proposals 
     */
    private final Image templateImage = CALEclipseUIPlugin.getImageDescriptor("icons/template.gif").createImage();

    private final ModuleName moduleName;

    private final CALEditor textEditor;

    // This is initialize on first use
    private List<ScopedEntity> entities;

    private ModuleTypeInfo moduleTypeInfo;
    
    private final IPreferenceStore fPreferenceStore;
    
    

    /**
     * Constructor for a CompletionProcessor. Call getCompletionProcessor()
     * to obtain instances.
     * 
     * @param editor
     *            the textEditor for which to perform completions.
     * @param preferenceStore  TODO
     */
    CompletionProcessor(CALEditor editor, IPreferenceStore preferenceStore) {
        this.textEditor = editor;
        moduleName = CALModelManager.getCALModelManager().getModuleName(textEditor.getStorage()); 
        this.fPreferenceStore = preferenceStore;
    }

    /**
     * Don't initialize in the constructor because this is called during
     * start up which should be fast and initialize can trigger a CAL
     * compile.
     */
    private boolean initialize() {
        CoreUtility.initializeCALBuilder(null, 100, 100);
        CALModelManager cmm = CALModelManager.getCALModelManager();
        moduleTypeInfo = cmm.getModuleTypeInfo(moduleName);
        if (moduleTypeInfo == null) {
            CoreUtility.showErrorOnStatusLine(textEditor, ActionMessages.AutoCompleteAction_moduleInfoNotAvailable);
            entities = new ArrayList<ScopedEntity>();
            return false;
        }
        entities = moduleTypeInfo.getAccessibleEntitiesInScope();
        return true;
    }

    /**
     * Find the shortest hierarchical name that is unambiguous
     * currentComponent is starting from the right most one.
     * The idea is to scan left until there are not matches left then
     * create the symbol name based on how far the scan had to go.
     *  
     * For the symbol Cal.Collections.List.takeWhile, if takeWhile is imported then
     * the shorted name is List.takeWhile. If takeWhile is not imported then List.takeWhile
     * is the shortest name.
     * 
     * @param moduleNamesOfPotentialMatches List of ModuleNames with respect to which the name should be unambiguous.
     * @param fullName The full name of the symbol being inserted.
     * @return The shortest unambiguous name in the current context.
     */
    public String getShortestName(Collection<ModuleName> moduleNamesOfPotentialMatches, QualifiedName fullName) {

        final ModuleName moduleName = fullName.getModuleName();
        int currentComponent = 0;
        // Start with a list of possible matches. Starting with the right most component
        // of the module name removed symbols that don't match until the matches list
        // is empty. At that point all of the names that had components in the hierarchical
        // name that match are removed so a name built off of the components up to the current
        // on is safe.
        while (!moduleNamesOfPotentialMatches.isEmpty()) {
            final int indexToCheck = moduleName.getNComponents() - 1 - currentComponent;
            if (indexToCheck < 0) {
                return fullName.toString();
            }

            // remove potential matches from the matches list.
            for (Iterator<ModuleName> i_matches = moduleNamesOfPotentialMatches.iterator(); i_matches.hasNext(); ) {
                ModuleName matchModuleName = i_matches.next();
                if (matchModuleName.equals(fullName.getModuleName())) {
                    i_matches.remove();
                    continue;
                }
                
                final int matchIndexToCheck = matchModuleName.getNComponents() - 1 - currentComponent;
                if (matchIndexToCheck < 0) {
                    i_matches.remove();
                    continue;
                }
                // if the component doesn't match then remove the name
                if (!matchModuleName.getNthComponent(matchIndexToCheck).equals(moduleName.getNthComponent(indexToCheck))) {
                    i_matches.remove();
                }
            }
            currentComponent++;
        }

        // build the shortened name only using the components needed.
        final int startIndex = moduleName.getNComponents() - currentComponent;
        final String[] components = moduleName.getComponents(startIndex, moduleName.getNComponents());
        StringBuilder insertionName = new StringBuilder();
        for (int i = 0; i < components.length; ++i) {
            insertionName.append(components[i]);
            insertionName.append('.');
        }
        // add the unqualified name
        insertionName.append(fullName.getUnqualifiedName());
        return insertionName.toString();
    }

    /**
     * Finds the least qualified version of the given name to insert into
     * the file.
     * 
     * @param scopedEntityBeingInserted
     *            The scoped entity corresponding to the symbol being
     *            inserted.
     * @return The least qualified unambiguous version of the given
     *         fullName.
     */
    Object[] determineInsertionName(ScopedEntity scopedEntityBeingInserted, MatchInformation matchInformation) {
        
        final QualifiedName fullName = scopedEntityBeingInserted.getName();
        List<ModuleName> moduleNamesOfEntitiesWithSameUnqualifiedName = new ArrayList<ModuleName>();
        for (final ScopedEntity scopedEntity : entities) {
            if (scopedEntity.getName().getUnqualifiedName().equals(fullName.getUnqualifiedName())) {
                // add the name to the list as long as it is not the name of
                // the symbol being added
                if (!scopedEntity.getName().equals(fullName)) {
                    if (moduleTypeInfo.isUsingEntity(scopedEntity)) {
                        moduleNamesOfEntitiesWithSameUnqualifiedName.add(scopedEntity.getName().getModuleName());
                    }
                }
            }
        }
        
        // If the user typed in scoping components then keep them.
        if (matchInformation != null && matchInformation.scopingComponents != null && matchInformation.scopingComponents.length > 0){
            StringBuffer sb = new StringBuffer();
            int length = matchInformation.scopingComponents.length;
            if (matchInformation.partialHierarchicalName){
                length--;
            }
            for (int i = 0; i < length; i++) {
                sb.append(matchInformation.scopingComponents[i]);
                sb.append('.');               
            }
            sb.append(fullName.getUnqualifiedName());
            // The user can type "Cal.Core" then autocomplete and select for example "abs".
            // This would result in an insertion string of "Cal.abs" which is not valid.
            // In this case we will ignore the scoping information that the user has typed
            // in since it was probably just used for selection the symbols.
            // This test makes sure that the name being inserted is valid.
            if (fullName.toSourceText().endsWith(sb.toString())){
                return new Object[]{sb.toString(), moduleName, fullName};
            }
        }
        // if there are not other symbols with the same unqualified name
        // then just add the unqualified name.
        if (moduleNamesOfEntitiesWithSameUnqualifiedName.isEmpty()) {
            if (moduleTypeInfo.isUsingEntity(scopedEntityBeingInserted) || fullName.getModuleName().equals(moduleTypeInfo.getModuleName())) {
                return new Object[]{fullName.getUnqualifiedName(), null, null};

            } else {
                final boolean enabledInsertSingleTitleAutomatically = 
                    fPreferenceStore.getBoolean(DefaultCodeFormatterConstants.ENABLE_ADD_IMPORT_INSTEAD_OF_QUALIFIED_NAME);
                
                // Fill the matches with the names of imported modules
                final String shortestName = getShortestName(new ArrayList<ModuleName>(moduleTypeInfo.getImportedModules()), fullName);
                if (!enabledInsertSingleTitleAutomatically || shortestName.indexOf('.') == -1) {
                    return new Object[]{shortestName, null, null};
                } else {
                    return new Object[]{fullName.getUnqualifiedName(), moduleName, fullName};
                }
            }
        } else {
            return new Object[]{getShortestName(moduleNamesOfEntitiesWithSameUnqualifiedName, fullName), null, null};
        }
    }

    /**
     * Check if the unqualified name of the given entities matches the given
     * prefix. If the given prefix is the empty string this matches.
     */
    private static boolean matchesPrefix(ScopedEntity entityToCheck, String prefixToMatch, boolean partialUnqualifiedName) {

        final String unqualifiedName = entityToCheck.getName().getUnqualifiedName();
        if (partialUnqualifiedName) {
            if (unqualifiedName.length() < prefixToMatch.length()) {
                return false;
            }
            final String scopedEntity_prefix = unqualifiedName.substring(0, prefixToMatch.length());

            return
            // matches no prefix
            scopedEntity_prefix.length() == 0 ||
            // or matches a non-zero length prefix.
                    prefixToMatch.compareToIgnoreCase(scopedEntity_prefix) == 0;
        } else {
            return unqualifiedName.compareToIgnoreCase(prefixToMatch) == 0;
        }
    }

    /**
     * Check if the prefix (unqualified name) and scope (hierarchical part)
     * matches the given scoped entity.
     */
    private static boolean matchesPrefixAndScope(ScopedEntity scopedEntity, String[] scopingComponents, String prefix, boolean partialHierarchicalName, boolean partialUnqualifiedName){
        // See if the symbol name matches the partial name that the user has types in
        if (!matchesPrefix(scopedEntity, prefix, partialUnqualifiedName)){
            return false; // no match
        }

        if (scopingComponents != null && scopingComponents.length != 0){
            if (CALSourceViewerConfiguration.isSuffixOf(scopedEntity.getName().getModuleName(), scopingComponents, partialHierarchicalName)){
                // match
            }
            else if (prefix.length() == 0 && CALSourceViewerConfiguration.isMiddleOf(scopedEntity.getName().getModuleName(), scopingComponents, partialHierarchicalName)){
                // match
            }
            else{
                return false;
            }
        }
        return true;
    }

    /**
     * This is a helper for determining if the current position matches the given completion proposal.
     * @author Greg McClement
     */
    static class MatchInformation{
        private final boolean partialHierarchicalName;
        private String[] scopingComponents = null;
        private final List<Integer> componentPositions;
        private final boolean isDefiningTypeExpression;
        private final boolean isDefiningFriendExpression;
        private final int startOfReplacementZone;        
        // True if the last name in the hierarchical names is only partially entered.
        private String prefix;
        private boolean matchesPrefix;
        
        MatchInformation(final IDocument document, final int offset) throws BadLocationException{
            this.isDefiningFriendExpression = isDefiningFriendExpression(document, offset);
            if (this.isDefiningFriendExpression){
                this.isDefiningTypeExpression = true;
            }
            else{
                this.isDefiningTypeExpression = isDefiningTypeExpression(document, offset);
            }
            AutoCompleteHelper ach = new AutoCompleteHelper(
                    new AutoCompleteHelper.Document() {
                        
                        public char getChar(int offset) {
                            try {
                                return document.getChar(offset);
                            } catch (BadLocationException e) {
                                // This should not occur except as a programmer error
                                CALEclipseUIPlugin.log(new Status(IStatus.ERROR, CALEclipseUIPlugin.PLUGIN_ID, IStatus.OK, "", e)); //$NON-NLS-1$
                                return 0;
                            }
                        }

                        public String get(int startIndex, int length) {
                            try {
                                return document.get(startIndex, length);
                            } catch (BadLocationException e) {
                                // This should not occur except as a programmer error
                                CALEclipseUIPlugin.log(new Status(IStatus.ERROR, CALEclipseUIPlugin.PLUGIN_ID, IStatus.OK, "", e)); //$NON-NLS-1$
                                return null;
                            }
                        }
                    });
            
            prefix = ach.getLastIncompleteIdentifier(offset);
            final Pair<String, List<Integer>> scopingAndOffset = ach.getIdentifierScoping(offset);
            String scoping = scopingAndOffset.fst();
            componentPositions = scopingAndOffset.snd();
            startOfReplacementZone = (componentPositions.get(0)).intValue();
            matchesPrefix = prefix.length() > 0;
            
            // If the prefix looks like a hierarchical name then adjust
            // things.
            if (prefix.length() > 0 && Character.isUpperCase(prefix.charAt(0))) {
                if (scoping.length() == 0) {
                    scoping = prefix;
                    scopingComponents = new String[1];
                    scopingComponents[0] = prefix;
                } else {
                    final String components[] = scoping.split("\\.");
                    scopingComponents = new String[components.length + 1];
                    System.arraycopy(components, 0, scopingComponents, 0, components.length);
                    scopingComponents[components.length] = prefix;
                }
                prefix = "";
                partialHierarchicalName = true;
            } else {
                partialHierarchicalName = false;
                if (scoping.length() > 0) {
                    scopingComponents = scoping.split("\\.");
                }
            }            
        }

        public boolean getMatchesPrefix(){
            return this.matchesPrefix;
        }
        
        public boolean resetMatchesPrefix(){
            boolean oldValue = matchesPrefix;
            matchesPrefix = prefix.length() > 0;
            return oldValue;
        }
        
        public void setMatchesPrefix(boolean matchesPrefix){
            this.matchesPrefix = matchesPrefix;
        }
    }
    
    /**
     * Keeps track of the current viewer in the context.
     * 
     * @author Greg McClement
     */
    public class ViewTemplateContext extends DocumentTemplateContext{
        private final ITextViewer viewer;
        
        ViewTemplateContext(TemplateContextType type, ITextViewer viewer, IDocument document, int offset, int length){
            super(type, document, offset, length);
            this.viewer = viewer;
        }
        
        public ITextViewer getViewer(){
            return viewer;
        }
    }
    
    @Override
    protected TemplateContext createContext(ITextViewer viewer, IRegion region) {
        TemplateContextType contextType= getContextType(viewer, region);
        if (contextType != null) {
            IDocument document= viewer.getDocument();
            return new ViewTemplateContext(contextType, viewer, document, region.getOffset(), region.getLength());
        }
        return null;
    }

    /**
     * Keeps track of module and hierachical name matches.
     * 
     * @author Greg McClement
     */
    private static class Match implements Comparable<Match>{
        private final String replacementString;
        private final String displayString;
        
        public Match(String replacementString){
            assert replacementString != null;
            
            this.replacementString = replacementString;
            this.displayString = replacementString;
        }

        public Match(String replacementString, String displayString){
            assert replacementString != null;
            assert displayString != null;
            
            this.replacementString = replacementString;
            this.displayString = displayString;                        
        }

        public int compareTo(Match otherMatch) {
            int compare = replacementString.compareTo(otherMatch.replacementString);
            if (compare == 0){
                compare = displayString.compareTo(otherMatch.displayString);
            }
            return compare;
        }
        
        public String getReplacementString(){
            return replacementString;
        }
        
        public String getDisplayString(){
            return displayString;
        }                    
    }
    
    @Override
    public ICompletionProposal[] computeCompletionProposals(ITextViewer viewer, int offset) {
        if (!CoreUtility.builderEnabledCheck(textEditor)){
            return new ICompletionProposal[0];
        }

        if (!initialize()) {
            return new ICompletionProposal[0];
        }

        // update the auto-insert
        {
            if (viewer instanceof CALSourceViewer) {
                CALSourceViewer calViewer = (CALSourceViewer) viewer;
                final boolean enabledInsertSingleTitleAutomatically = fPreferenceStore.getBoolean(DefaultCodeFormatterConstants.ENABLE_INSERT_SINGLE_PROPOSAL_AUTOMATICALLY);
                calViewer.getContentAssistant().enableAutoInsert(enabledInsertSingleTitleAutomatically);                
            }
        }

        try {
            final IDocument document = viewer.getDocument();

            boolean isComment = false;
            if (document instanceof IDocumentExtension3){
                IDocumentExtension3 docExtension = (IDocumentExtension3) document;
                ITypedRegion partition = docExtension.getPartition(CALPartitions.CAL_PARTITIONING, offset, false);
                String partitionType = partition.getType();
                if (partitionType.equals(CALPartitions.CAL_STRING)){
                    // if the last char type was an auto-complete char then ignore this. If
                    // the user explicitly invokes the completion processor let it go. At 
                    // this point I can't tell if the user invoke auto-complete and the previous
                    // char just happens to be a dot. But this is consistent with the Java behaviour.
                    final char lastChar = document.get(offset - 1, 1).charAt(0);
                    for (final char aac : autoActivationCharacters) {
                        if (lastChar == aac){
                            return new ICompletionProposal[0];
                        }                        
                    }
                }
                else if (CALPartitions.isComment(partitionType)){
                    isComment = true;
                }
            }
            
            // The end of the area that the auto-completed symbol should be
            // placed.
            final int endOfReplacementZone = offset;
            final MatchInformation matchInformation = new MatchInformation(document, offset);
            final IRegion region = new IRegion() {
                public int getLength() {
                    return endOfReplacementZone - matchInformation.startOfReplacementZone;
                }

                public int getOffset() {
                    return matchInformation.startOfReplacementZone;
                }                
            };
         
            final TemplateContext context = createContext(viewer, region);

            final ModuleContainer moduleContainer = CALModelManager
                    .getCALModelManager().getModuleContainer(textEditor.getSourceManagerFactory(true));
            Iterator<ScopedEntity> iteratorOfImported = entities.iterator();
            TreeSet<ICompletionProposal> proposals = new TreeSet<ICompletionProposal>(new ProposalComparator());
            Iterator<ScopedEntity> iterator = iteratorOfImported;
            // If the user has specified a module explicitly 
            // then get entities from that module
            // even though they may not have imported the module.
            if (matchInformation.scopingComponents != null){
                try{
                    final ModuleName moduleNameUserSpecified = ModuleName.make(matchInformation.scopingComponents);
                    final CALModelManager cmm = CALModelManager.getCALModelManager();
                    ModuleTypeInfo moduleTypeInfo = cmm.getModuleTypeInfo(moduleNameUserSpecified);
                    if (moduleTypeInfo != null) {
                        final Iterator<ScopedEntity> moreEntities = moduleTypeInfo.getAccessibleEntitiesForModule(moduleName).iterator();
                        iterator = new IteratorChain<ScopedEntity>(iteratorOfImported, moreEntities);
                    }
                }
                catch(Exception e){
                    // User typed in an invalid module name so we cannot get module type info for it.
                    // Users will do that sometimes so let's just ignore it so they don't feel bad.
                }
            }

            boolean showingFields = false;
            boolean showingNonFields = false;
            if (!isComment && !matchInformation.isDefiningFriendExpression){
                while (iterator.hasNext()) {
                    final ScopedEntity scopedEntity = iterator.next();

                    // Maybe this is a field completion for a data structure
                    // This will always have scoping.
                    if (matchInformation.scopingComponents != null && scopedEntity instanceof DataConstructor) {
                        final DataConstructor dataConstructor = (DataConstructor) scopedEntity;
                        // ignore the scoping since the other names would be the variable name in this case.                         
                        final int arity = dataConstructor.getArity();
                        for (int i = 0; i < arity; ++i) {
                            final FieldName fieldName = dataConstructor.getNthFieldName(i);

                            final String name = getReplacementString(moduleContainer, scopedEntity, fieldName);
                            Template template = new Template(name, name, context.getContextType().getId(), name, true);                    
                            int relevance = 1;

                            if (matches(matchInformation, scopedEntity, fieldName)){
                                final int start = (matchInformation.componentPositions.get(matchInformation.componentPositions.size()-2)).intValue();
                                final IRegion fieldRegion = new IRegion() {
                                    public int getLength() {
                                        return endOfReplacementZone - start;
                                    }

                                    public int getOffset() {
                                        return start;
                                    }                
                                };
                                final TemplateContext fieldContext = createContext(viewer, fieldRegion);

                                proposals.add(new FieldCompletionProposal(
                                        start, endOfReplacementZone,
                                        scopedEntity, matchInformation.resetMatchesPrefix(), fieldName,
                                        moduleContainer,

                                        template, 
                                        fieldContext,
                                        fieldRegion,
                                        labelProvider.getImage(scopedEntity), 
                                        relevance,
                                        matchInformation));
                                showingFields = true;
                            }
                        }
                    }

                    if (!matches(matchInformation, scopedEntity)) {
                        continue;
                    }

                    // If we are defining a type expression only show type
                    // classes and constructors
                    final boolean validForTypeDefinition = scopedEntity instanceof TypeClass || scopedEntity instanceof TypeConstructor;
                    if (matchInformation.isDefiningTypeExpression) {
                        if (validForTypeDefinition) {
                        } else {
                            continue;
                        }
                    } else {
                        if (validForTypeDefinition) {
                            continue;
                        }
                    }

                    {
                        // Initialize name and pattern
                        String name;
                        StringBuilder pattern = new StringBuilder();
                        {
                            Object[] rs = getReplacementString(moduleContainer, scopedEntity, matchInformation);
                            name = (String) rs[0];
                            String[] arguments = (String[]) rs[1];
                            pattern.append(name);
                            for (int i = 0; i < arguments.length; ++i) {
                                pattern.append(" ${").append(arguments[i]).append("}");
                            }
                        }

                        Template template = new Template(name, name, context.getContextType().getId(), pattern.toString(), true);                    
                        int relevance = 1;
                        final boolean matchesPrefix = matchInformation.resetMatchesPrefix();

                        showingNonFields = true;
                        proposals.add(new DefaultCompletionProposal(
                                matchInformation.startOfReplacementZone, 
                                endOfReplacementZone,
                                scopedEntity, 
                                matchesPrefix, 
                                moduleContainer,

                                template, 
                                context,
                                region,
                                labelProvider.getImage(scopedEntity), 
                                relevance,
                                matchInformation));
                    }                
                }
            }

            // look for hierarchical names that could be completions. 
            
            if (!isComment && matchInformation.prefix.equals("")){
                CALModelManager cmm = CALModelManager.getCALModelManager();
                final Collection<ModuleName> moduleNames = cmm.getModuleNames();
                HashSet<Match> hierarchicalNames = new HashSet<Match>();
                final boolean showAllMatches = matchInformation.scopingComponents == null || matchInformation.scopingComponents.length == 0;
                for (final ModuleName moduleName :  moduleNames) {
                    boolean partialHierarchicalName = matchInformation.partialHierarchicalName;
                    {
                        // if showing all matches the pick out the component parts
                        if (showAllMatches){
                            for (int i = 0; i < moduleName.getNComponents() - 1; ++i) {
                                hierarchicalNames.add(new Match(moduleName.getNthComponent(i)));
                            }
                            if (matchInformation.isDefiningFriendExpression){
                                hierarchicalNames.add(new Match(moduleName.toSourceText(), moduleName.getLastComponent()));
                            }
                            else{
                                hierarchicalNames.add(new Match(moduleName.getLastComponent()));
                            }

                        } else {
                        // pick out the parts that complete the currently expression in the textEditor.
                        // For example "Cal.Collec" is completed by "Collections".
                            getPotentialMatches(moduleName, matchInformation.scopingComponents, partialHierarchicalName, hierarchicalNames);
                        }
                    }
                }
                for (final Match match : hierarchicalNames) {
                    final Image image;
                    final String replacementName;
                    final String displayName;
//                    if (match instanceof ModuleName){
//                        hierarchicalName = ((ModuleName) match).getLastComponent();
//                        image = image_nav_module;
//                    }
//                    else
                    {
                        replacementName = match.getReplacementString();
                        displayName = match.getDisplayString();
                        image = image_nav_namespace;
                    }
                    final int replacementOffset = (matchInformation.componentPositions.get(matchInformation.componentPositions.size() - 1)).intValue();
                    final int replacementLength;
                    if (matchInformation.partialHierarchicalName){
                        replacementLength = (matchInformation.scopingComponents[matchInformation.scopingComponents.length - 1]).length();
                    }
                    else{                
                        replacementLength = 0;
                    }
                    
                    // make sure that the current component matches the last scoping component
                    if (matchInformation.scopingComponents != null){
                        if (!replacementName.startsWith(matchInformation.scopingComponents[matchInformation.scopingComponents.length-1])){
                            continue;
                        }
                    }
                    
                    ICompletionProposal completionProposal = 
                        new org.eclipse.jface.text.contentassist.CompletionProposal(
                                replacementName, 
                                replacementOffset, 
                                replacementLength, 
                                replacementName.length(),
                                image, 
                                displayName, 
                                null, 
                                showAllMatches ? "All" : null);
                    proposals.add(completionProposal);
                }
            }
            
            if (matchInformation.isDefiningTypeExpression){
                // if defining type expression then don't show templates
            }
            else if (showingFields && !showingNonFields){
                // if only fields are being shown the don't show 
                // the templates since they would not make any sense.
            }
            else if (!matchInformation.isDefiningFriendExpression){
                // Only show template when the user is not completing an expression with scoping
                // information such as "Cal.Collections."
                if (matchInformation.scopingComponents == null || (matchInformation.scopingComponents.length <= 1 && matchInformation.partialHierarchicalName)){
                    String matchThis = matchInformation.prefix;
                    if (matchInformation.scopingComponents != null){
                        if (matchInformation.scopingComponents.length == 1 && matchInformation.partialHierarchicalName){
                            matchThis = matchInformation.scopingComponents[0];
                        }
                    }
                    List<ICompletionProposal> templateProposals = computeApplicableTemplates(matchThis, viewer, offset);
                    proposals.addAll(templateProposals);
                }
            }

            if (proposals.size() == 0) {
                CoreUtility.showErrorOnStatusLine(textEditor, ActionMessages.AutoCompleteAction_noCompletionsAvailable);
            }
            
            return proposals.toArray(new ICompletionProposal[proposals.size()]);

        } catch (Exception e) {
            CALEclipseUIPlugin.log(new Status(IStatus.ERROR, CALEclipseUIPlugin.PLUGIN_ID, IStatus.OK, "", e)); //$NON-NLS-1$
            return super.computeCompletionProposals(viewer, offset);
        }
    }

    private static boolean matches(final ModuleName name, int startIndex, String[] components, boolean lastNameIsPartial){
        for (int i = 0; i < components.length; i++, startIndex++) {
            if (startIndex < name.getNComponents() && !name.getNthComponent(startIndex).equals(components[i])){
                if (lastNameIsPartial){
                    // another chance for a match
                    if (i == components.length - 1){
                        if (name.getNthComponent(startIndex).startsWith(components[i])){
                            // partial match on the last components
                            return true;
                        }
                    }
                }
                return false;
            }
        }
        return true;
    }
    
    /**
     * This takes a module name and text that the user has typed in and tries to select naming components
     * that match. For example, if the module name is "Cal.Collections.List" and the user typed in 
     * "Cal.Collections.Li" then "List" and "List_Test" will be added to the hierarchical names list.
     */
    public static void getPotentialMatches(final ModuleName name, final String[] middle, boolean lastNameIsPartial, HashSet<Match> hierarchicalNames) {
        for (int j = 0; j < name.getNComponents(); ++j) {
            if (matches(name, j, middle, lastNameIsPartial)) {
                if (lastNameIsPartial){
                    final int selectedComponent = j + middle.length - 1;
                    if (selectedComponent + 1 >= name.getNComponents()){
                        hierarchicalNames.add(new Match(name.getLastComponent()));
                    } else {
                        hierarchicalNames.add(new Match(name.getNthComponent(selectedComponent)));
                    }

                } else {                  
                    // j + middle.length if < length of name.getNComponents
                    if (j + middle.length < name.getNComponents()){
                        final int selectedComponent = j + middle.length;
                        if (selectedComponent + 1 == name.getNComponents()){
                            hierarchicalNames.add(new Match(name.getLastComponent()));

                        } else {
                            hierarchicalNames.add(new Match(name.getNthComponent(selectedComponent)));
                        }
                    }
                }
            }
        }
    }


    /**
     * True if it looks like the user is defining a type expression. Basically scan backwards from 
     * the current position. If a ';' or start of file is encounter then return false. If a '::' is 
     * found and the '::' is preceeded by a CAL identifier then return true. I added the identifier 
     * check for cases such as "t = (1::Int) ", auto-completing at the end will think this is a 
     * type declaration without the extra check.
     * @throws BadLocationException 
     */
    private static boolean isDefiningTypeExpression(IDocument doc, int offset) throws BadLocationException {
        for (int n = offset - 1; n >= 0; n--) {
            final char c = doc.getChar(n);
            switch (c) {
            case ';':
                return false;
            case ':':
                if (n == 0) {
                    return false;
                }
                final char c2 = doc.getChar(n - 1);
                if (c2 == ':') {
                    // skip spaces
                    n -= 2;
                    while(n >= 0 && Character.isWhitespace(doc.getChar(n))){
                        n--;
                    }
                    // skip any trailing digits
                    while(n >= 0 && Character.isDigit(doc.getChar(n))){
                        n--;
                    }
                    if (n <= 0){
                        return false;
                    }

                    if (LanguageInfo.isCALVarPart(doc.getChar(n))){
                        // something like "var1232 ::" 
                        return true;
                    }
                    else{
                        return false;
                    }
                }
            case '|':
                // This is temporary until I fix the partitioner to be more knowledgeable
                return false;
                
            // default:
                // no match so continue onward
            }
        }
        return false;
    }

    /**
     * True if the user is defining a friend expression.
     */
    private static boolean isDefiningFriendExpression(IDocument doc, int offset) throws BadLocationException {
        int n = offset - 1;
        // skip possible completions
        while(n >= 0){
            final char c = doc.getChar(n);
            if (LanguageInfo.isCALVarPart(doc.getChar(n))){
            }
            else if (c == '.'){
            }
            else{
                break;
            }
            --n;
        }
        
        // skip spaces
        while(n >= 0){
            final char c = doc.getChar(n);
            if (!Character.isWhitespace(c)){
                break;
            }
            --n;
        }
        
        // look for the word friend
        if (lookForWord(doc, n, "friend")){
            return true;
        }
        // look for the word import
        else if (lookForWord(doc, n, "import")){
            return true;
        }
        else{
            return false;
        }
    }

    private static boolean lookForWord(IDocument doc, int n, String word)
            throws BadLocationException {
        int wordIndex = word.length() - 1;
        while(n >= 0 && wordIndex >= 0){
            final char c = Character.toLowerCase(doc.getChar(n));
            --n;
            if (c == word.charAt(wordIndex--)){
            }
            else{
                return false;
            }
        }
        if (wordIndex <= 0){
            return true;
        }
        else{
            return false;
        }
    }
    
    @Override
    public IContextInformation[] computeContextInformation(
            ITextViewer viewer, int offset) {
        return new IContextInformation[0];
    }

    private final char[] autoActivationCharacters = { '.' };

    private final char[] no_chars = {};

    @Override
    public char[] getCompletionProposalAutoActivationCharacters() {
        // if the property is not set this counts as on.
        if (!fPreferenceStore.contains(DefaultCodeFormatterConstants.ENABLE_AUTO_COMPLETION)){
            return autoActivationCharacters;
        }
        
        if (fPreferenceStore.getBoolean(DefaultCodeFormatterConstants.ENABLE_AUTO_COMPLETION)) {
            final String configuredAutoActivationCharacters = fPreferenceStore.getString(DefaultCodeFormatterConstants.AUTO_COMPLETION_TRIGGERS);
            if (configuredAutoActivationCharacters == null) {
                return autoActivationCharacters;
            } else {
                return configuredAutoActivationCharacters.toCharArray();
            }
        } else {
            return no_chars;
        }
    }

    @Override
    public char[] getContextInformationAutoActivationCharacters() {
        return null;
    }

    @Override
    public IContextInformationValidator getContextInformationValidator() {
        return null;
    }

    @Override
    public String getErrorMessage() {
        return null;
    }
    
    @Override
    protected TemplateContextType getContextType(ITextViewer viewer, IRegion region) {
        final IDocument document = viewer.getDocument();
        if (document instanceof IDocumentExtension3){
            try {
                IDocumentExtension3 docExtension = (IDocumentExtension3) document;
                ITypedRegion partition = docExtension.getPartition(CALPartitions.CAL_PARTITIONING, region.getOffset(), false);
                String partitionType = partition.getType();
                if (partitionType.equals(CALPartitions.CAL_DOC)){
                    return CALEclipseUIPlugin.getDefault().getTemplateContextRegistry().getContextType(CALDocTemplateContextType.ID);                
                }
                else if (
                        partitionType.equals(CALPartitions.CAL_SINGLE_LINE_COMMENT) ||
                        partitionType.equals(CALPartitions.CAL_MULTI_LINE_COMMENT)
                        ){
                    // there are no auto-completes inside commments
                    return null;
                }
            } catch (BadLocationException e) {
                // ignore return the default
            } catch (BadPartitioningException e) {
                // ignore return the default
            }
        }

        return CALEclipseUIPlugin.getDefault().getTemplateContextRegistry().getContextType(CALTemplateContextType.ID);
    }

    /**
     * Shows the template image next to all template proposals
     */
    @Override
    protected Image getImage(Template template) {
        return templateImage;
    }
    
    /**
     * CAL only has one context for now.  Therefore, all possible templates
     * are available at all possible locations.  But in the future we may want to add a
     * CalDoc context.  
     * 
     * @param contextTypeId
     *            the template context ID
     * @return the list of templates available in this context
     */
    @Override
    protected Template[] getTemplates(String contextTypeId) {
        return CALEclipseUIPlugin.getDefault().getTemplateStore().getTemplates(
                contextTypeId);
    }
    
    
    protected List<ICompletionProposal> computeApplicableTemplates(String prefix, 
            ITextViewer viewer, int offset) {
        
        ICompletionProposal[] unfiltered = super.computeCompletionProposals(viewer, offset);
        List<ICompletionProposal> filtered = new ArrayList<ICompletionProposal>(unfiltered.length);
        for (final ICompletionProposal completionProposal : unfiltered) {
            if (matchesPrefix(completionProposal.getDisplayString(), prefix)) {
                filtered.add(completionProposal);
            }
        }
        return filtered;
    }
    
    private boolean matchesPrefix(String display, String prefixToMatch) {
        return display.startsWith(prefixToMatch);
    }
    
    protected Object[] getReplacementString(ModuleContainer moduleContainer, ScopedEntity scopedEntity, MatchInformation matchInformation){
        if (scopedEntity instanceof FunctionalAgent || scopedEntity instanceof TypeConstructor){
            String[] argumentNames = null;
            if (scopedEntity instanceof FunctionalAgent){
                final FunctionalAgent function = (FunctionalAgent) scopedEntity;
                argumentNames = CALDocToJavaDocUtilities.getArgumentNamesFromCALDocComment(function.getCALDocComment(), function);
            }
            else if (scopedEntity instanceof TypeConstructor){
                final TypeConstructor typeConstructor = (TypeConstructor) scopedEntity;
                argumentNames = CoreUtility.getArgumentNamesFromCALDocComment(typeConstructor);
            }
            else{
                assert false;
            }
            
            final StringBuilder replacementString = new StringBuilder();
            // add symbol name to the result.
            Object replacementNameAndRefactorer[] = determineInsertionName(scopedEntity, matchInformation);
            final String replacementName = (String) replacementNameAndRefactorer[0];
            final ModuleName refactorer_moduleName = (ModuleName) replacementNameAndRefactorer[1];
            final QualifiedName refactorer_fullName = (QualifiedName) replacementNameAndRefactorer[2];

            replacementString.append(replacementName);

            return new Object[] {replacementString.toString(), argumentNames, refactorer_moduleName, refactorer_fullName, CoreUtility.toCategory(scopedEntity)};
        } else {
            Object replacementNameAndRefactorer[] = determineInsertionName(scopedEntity, matchInformation);
            final String replacementName = (String) replacementNameAndRefactorer[0];
            final ModuleName refactorer_moduleName = (ModuleName) replacementNameAndRefactorer[1];
            final QualifiedName refactorer_fullName = (QualifiedName) replacementNameAndRefactorer[2];
            return new Object[] {replacementName, new String[] {}, refactorer_moduleName, refactorer_fullName, CoreUtility.toCategory(scopedEntity)};
        }
    }

    private String getReplacementString(ModuleContainer moduleContainer, ScopedEntity scopedEntity, FieldName fieldName) {
        final StringBuilder replacementString = new StringBuilder();
        // add symbol name to the result.
        final Object[] replacementNameAndRefactorer = determineInsertionName(scopedEntity, null);
        final String replacementName = (String) replacementNameAndRefactorer[0];
        replacementString.append(replacementName);
        replacementString.append(".");
        replacementString.append(fieldName.getCalSourceForm());
        return replacementString.toString();
    }

    private boolean matches(MatchInformation matchInformation, ScopedEntity scopedEntity, FieldName fieldName){
        if (matchInformation.partialHierarchicalName){
            return false;
        }
        
        final String lastComponent = matchInformation.scopingComponents[matchInformation.scopingComponents.length-1];
        if (!matchesPrefixAndScope(scopedEntity, null, lastComponent, false, false)){
            return false;
        }
        
        final String prefix = matchInformation.prefix;
        // if there is a prefix only show the fields
        // that match it in
        // a case insensitive way
        if (prefix.length() > 0) {
            final String fieldNameString = fieldName.getCalSourceForm();
            if (prefix.length() > fieldNameString.length()) {
                return false;
            } else if (fieldNameString.substring(0, prefix.length()).compareToIgnoreCase(prefix) != 0) {
                return false;
            }
            
        }
        return true;
    }
    
    private boolean matches(MatchInformation matchInformation, ScopedEntity scopedEntity){
        final String prefix = matchInformation.prefix;
        final boolean partialHierarchicalName = matchInformation.partialHierarchicalName;
        final String scopingComponents[] = matchInformation.scopingComponents;
        if (scopingComponents != null && partialHierarchicalName) {
            // if a type expression is being defined then the last
            // part of the scoping
            // could actually be the prefix.
            String[] scopingComponentsWithoutLastName = new String[scopingComponents.length - 1];
            System.arraycopy(scopingComponents, 0,
                    scopingComponentsWithoutLastName, 0,
                    scopingComponentsWithoutLastName.length);
            final String lastComponent = scopingComponents[scopingComponents.length - 1];
            if (!matchesPrefixAndScope(scopedEntity,
                    scopingComponentsWithoutLastName,
                    lastComponent, partialHierarchicalName, true)) {
                // No match so try the old way. This is done second
                // so that the matchesPrefix
                // flag can be set properly
                if (!matchesPrefixAndScope(scopedEntity, scopingComponents, prefix, partialHierarchicalName, true)) {
                    return false;
                }
            } else {
                matchInformation.setMatchesPrefix(true);
            }
        } else {
            if (!matchesPrefixAndScope(scopedEntity, scopingComponents, prefix, partialHierarchicalName, true)) {
                return false;
            }
        }
        return true;
    }
    
}
