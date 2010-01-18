/*******************************************************************************
 * Copyright (c) 2006 Business Objects Software Limited and others.
 * All rights reserved. 
 * This file is made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Business Objects Software Limited - initial API and implementation based on Eclipse 3.1.2 code for
 *                             /org.eclipse.jdt.ui/ui/org/eclipse/jdt/ui/text/JavaSourceViewerConfiguration.java
 *                           Eclipse source is available at: http://www.eclipse.org/downloads/
 *******************************************************************************/

/*
 * CALSourceViewerConfiguration.java
 * Creation date: Feb 1, 2006.
 * By: Edward Lam
 */
package org.openquark.cal.eclipse.ui.text;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Vector;

import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IStorage;
import org.eclipse.core.runtime.Assert;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.text.AbstractInformationControlManager;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.DefaultInformationControl;
import org.eclipse.jface.text.DefaultTextDoubleClickStrategy;
import org.eclipse.jface.text.DocumentEvent;
import org.eclipse.jface.text.IAutoEditStrategy;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IInformationControl;
import org.eclipse.jface.text.IInformationControlCreator;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.ITextDoubleClickStrategy;
import org.eclipse.jface.text.ITextHover;
import org.eclipse.jface.text.ITextViewer;
import org.eclipse.jface.text.ITextViewerExtension2;
import org.eclipse.jface.text.Position;
import org.eclipse.jface.text.Region;
import org.eclipse.jface.text.contentassist.ContentAssistant;
import org.eclipse.jface.text.contentassist.ICompletionProposal;
import org.eclipse.jface.text.contentassist.ICompletionProposalExtension2;
import org.eclipse.jface.text.contentassist.IContentAssistant;
import org.eclipse.jface.text.contentassist.IContextInformation;
import org.eclipse.jface.text.formatter.IContentFormatter;
import org.eclipse.jface.text.hyperlink.IHyperlinkDetector;
import org.eclipse.jface.text.information.IInformationPresenter;
import org.eclipse.jface.text.information.IInformationProvider;
import org.eclipse.jface.text.information.IInformationProviderExtension;
import org.eclipse.jface.text.information.IInformationProviderExtension2;
import org.eclipse.jface.text.information.InformationPresenter;
import org.eclipse.jface.text.presentation.IPresentationReconciler;
import org.eclipse.jface.text.presentation.PresentationReconciler;
import org.eclipse.jface.text.quickassist.IQuickAssistAssistant;
import org.eclipse.jface.text.quickassist.IQuickAssistInvocationContext;
import org.eclipse.jface.text.quickassist.IQuickAssistProcessor;
import org.eclipse.jface.text.quickassist.QuickAssistAssistant;
import org.eclipse.jface.text.reconciler.IReconciler;
import org.eclipse.jface.text.rules.DefaultDamagerRepairer;
import org.eclipse.jface.text.rules.RuleBasedScanner;
import org.eclipse.jface.text.source.Annotation;
import org.eclipse.jface.text.source.IAnnotationHover;
import org.eclipse.jface.text.source.IAnnotationModel;
import org.eclipse.jface.text.source.ISourceViewer;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IFileEditorInput;
import org.eclipse.ui.IPartListener;
import org.eclipse.ui.IPerspectiveDescriptor;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.editors.text.TextSourceViewerConfiguration;
import org.eclipse.ui.texteditor.ITextEditor;
import org.eclipse.ui.texteditor.MarkerAnnotation;
import org.openquark.cal.compiler.CompilerMessageLogger;
import org.openquark.cal.compiler.MessageLogger;
import org.openquark.cal.compiler.ModuleName;
import org.openquark.cal.compiler.SourcePosition;
import org.openquark.cal.eclipse.core.CALEclipseCorePlugin;
import org.openquark.cal.eclipse.core.CALModelManager;
import org.openquark.cal.eclipse.core.CoreOptionIDs;
import org.openquark.cal.eclipse.core.builder.CALBuilder;
import org.openquark.cal.eclipse.core.builder.CALBuilder.IQuickFix;
import org.openquark.cal.eclipse.core.formatter.DefaultCodeFormatterConstants;
import org.openquark.cal.eclipse.ui.actions.ActionMessages;
import org.openquark.cal.eclipse.ui.actions.CALEditorActionDefinitionIds;
import org.openquark.cal.eclipse.ui.caleditor.CALDocumentProvider;
import org.openquark.cal.eclipse.ui.caleditor.CALEditor;
import org.openquark.cal.eclipse.ui.caleditor.CALHyperlinkDetector;
import org.openquark.cal.eclipse.ui.caleditor.CALEditor.AdaptedSourceViewer;
import org.openquark.cal.eclipse.ui.text.cal.CALAutoIndentStrategy;
import org.openquark.cal.eclipse.ui.text.cal.CALCodeScanner;
import org.openquark.cal.eclipse.ui.text.cal.CALDocDoubleClickStrategy;
import org.openquark.cal.eclipse.ui.text.cal.CALDoubleClickSelector;
import org.openquark.cal.eclipse.ui.text.cal.CALStringDoubleClickSelector;
import org.openquark.cal.eclipse.ui.text.caldoc.CALDocAutoIndentStrategy;
import org.openquark.cal.eclipse.ui.text.caldoc.CALDocScanner;
import org.openquark.cal.eclipse.ui.util.CodeFormatterUtil;
import org.openquark.cal.eclipse.ui.util.CoreUtility;
import org.openquark.cal.eclipse.ui.views.CALOutlineInformationControl;
import org.openquark.util.UnsafeCast;


/**
 * Configuration for a source viewer which shows Java code.
 * <p>
 * This class may be instantiated; it is not intended to be subclassed.
 * </p>
 */
public class CALSourceViewerConfiguration extends TextSourceViewerConfiguration {
    
    private CALTextTools fCALTextTools;
    ITextEditor fTextEditor;
    /**
     * The document partitioning.
     */
    private final String fDocumentPartitioning;
    /**
     * The Java source code scanner.
     */
    private AbstractCALScanner fCodeScanner;
    /**
     * The Java multi-line comment scanner.
     */
    private AbstractCALScanner fMultilineCommentScanner;
    /**
     * The Java single-line comment scanner.
     */
    private AbstractCALScanner fSinglelineCommentScanner;
    /**
     * The Java string scanner.
     */
    private AbstractCALScanner fStringScanner;
    /**
     * The Javadoc scanner.
     */
    private AbstractCALScanner fCALDocScanner;
    /**
     * The color manager.
     */
    private final ColorManager fColorManager;
    /**
     * The double click strategy.
     */
    private CALDoubleClickSelector fCALDoubleClickSelector;
    
    private final ContentAssistant assistant = new ContentAssistant();

    
    /**
     * Creates a new Java source viewer configuration for viewers in the given editor
     * using the given preference store, the color manager and the specified document partitioning.
     * <p>
     * Creates a Java source viewer configuration in the new setup without text tools. Clients are
     * allowed to call {@link CALSourceViewerConfiguration#handlePropertyChangeEvent(PropertyChangeEvent)}
     * and disallowed to call {@link CALSourceViewerConfiguration#getPreferenceStore()} on the resulting
     * Java source viewer configuration.
     * </p>
     *
     * @param colorManager the color manager
     * @param preferenceStore the preference store, can be read-only
     * @param editor the editor in which the configured viewer(s) will reside, or <code>null</code> if none
     * @param partitioning the document partitioning for this configuration, or <code>null</code> for the default partitioning
     */
    public CALSourceViewerConfiguration(ColorManager colorManager, IPreferenceStore preferenceStore, ITextEditor editor, String partitioning) {
        super(preferenceStore);
        fColorManager = colorManager;
        fTextEditor = editor;
        fDocumentPartitioning = partitioning;
        initializeScanners();
    }
    
    /**
     * Returns the Java source code scanner for this configuration.
     *
     * @return the Java source code scanner
     */
    protected RuleBasedScanner getCodeScanner() {
        return fCodeScanner;
    }
    
    /**
     * Returns the Java multi-line comment scanner for this configuration.
     *
     * @return the Java multi-line comment scanner
     */
    protected RuleBasedScanner getMultilineCommentScanner() {
        return fMultilineCommentScanner;
    }
    
    /**
     * Returns the Java single-line comment scanner for this configuration.
     *
     * @return the Java single-line comment scanner
     */
    protected RuleBasedScanner getSinglelineCommentScanner() {
        return fSinglelineCommentScanner;
    }
    
    /**
     * Returns the Java string scanner for this configuration.
     *
     * @return the Java string scanner
     */
    protected RuleBasedScanner getStringScanner() {
        return fStringScanner;
    }
    
    /**
     * Returns the JavaDoc scanner for this configuration.
     *
     * @return the JavaDoc scanner
     */
    protected RuleBasedScanner getJavaDocScanner() {
        return fCALDocScanner;
    }
    
    /**
     * Returns the color manager for this configuration.
     *
     * @return the color manager
     */
    protected ColorManager getColorManager() {
        return fColorManager;
    }
    
    /**
     * Returns the editor in which the configured viewer(s) will reside.
     *
     * @return the enclosing editor
     */
    protected ITextEditor getEditor() {
        return fTextEditor;
    }
    
    /**
     * Returns the preference store used by this configuration to initialize
     * the individual bits and pieces.
     * <p>
     * Clients are not allowed to call this method if the new setup without
     * text tools is in use.
     * @see CALSourceViewerConfiguration#CALSourceViewerConfiguration(ColorManager, IPreferenceStore, ITextEditor, String)
     * </p>
     *
     * @return the preference store used to initialize this configuration
     * @deprecated As of 3.0
     */
    @Deprecated
    protected IPreferenceStore getPreferenceStore() {
        Assert.isTrue(!isNewSetup());
        return fCALTextTools.getPreferenceStore();
    }
    
    /**
     * @return <code>true</code> iff the new setup without text tools is in use.
     */
    private boolean isNewSetup() {
        return fCALTextTools == null;
    }
    
//    /**
//     * Creates and returns a preference store which combines the preference
//     * stores from the text tools and which is read-only.
//     *
//     * @param javaTextTools the Java text tools
//     * @return the combined read-only preference store
//     */
//    private static final IPreferenceStore createPreferenceStore(CALTextTools javaTextTools) {
//        Assert.isNotNull(javaTextTools);
//        IPreferenceStore generalTextStore= EditorsUI.getPreferenceStore();
//        if (javaTextTools.getCorePreferenceStore() == null)
//            return new ChainedPreferenceStore(new IPreferenceStore[] { javaTextTools.getPreferenceStore(), generalTextStore});
//        
//        return new ChainedPreferenceStore(new IPreferenceStore[] { javaTextTools.getPreferenceStore(), new PreferencesAdapter(javaTextTools.getCorePreferenceStore()), generalTextStore });
//    }
    
    /**
     * Initializes the scanners.
     */
    private void initializeScanners() {
        Assert.isTrue(isNewSetup());
        fCodeScanner = new CALCodeScanner(getColorManager(), fPreferenceStore);
        fMultilineCommentScanner = new CALCommentScanner(getColorManager(), fPreferenceStore, CALColorConstants.CAL_MULTI_LINE_COMMENT);
        fSinglelineCommentScanner = new CALCommentScanner(getColorManager(), fPreferenceStore, CALColorConstants.CAL_SINGLE_LINE_COMMENT);
        fStringScanner = new SingleTokenCALScanner(getColorManager(), fPreferenceStore, CALColorConstants.CAL_STRING);
        fCALDocScanner = new CALDocScanner(getColorManager(), fPreferenceStore);
    }
    
    /*
     * @see SourceViewerConfiguration#getPresentationReconciler(ISourceViewer)
     */
    @Override
    public IPresentationReconciler getPresentationReconciler(ISourceViewer sourceViewer) {
        
        PresentationReconciler reconciler = new CALPresentationReconciler();
        reconciler.setDocumentPartitioning(getConfiguredDocumentPartitioning(sourceViewer));

        DefaultDamagerRepairer dr = new DefaultDamagerRepairer(getCodeScanner());
        reconciler.setDamager(dr, IDocument.DEFAULT_CONTENT_TYPE);
        reconciler.setRepairer(dr, IDocument.DEFAULT_CONTENT_TYPE);

        dr = new DefaultDamagerRepairer(getJavaDocScanner());
        reconciler.setDamager(dr, CALPartitions.CAL_DOC);
        reconciler.setRepairer(dr, CALPartitions.CAL_DOC);

        dr = new DefaultDamagerRepairer(getMultilineCommentScanner());
        reconciler.setDamager(dr, CALPartitions.CAL_MULTI_LINE_COMMENT);
        reconciler.setRepairer(dr, CALPartitions.CAL_MULTI_LINE_COMMENT);

        dr = new DefaultDamagerRepairer(getSinglelineCommentScanner());
        reconciler.setDamager(dr, CALPartitions.CAL_SINGLE_LINE_COMMENT);
        reconciler.setRepairer(dr, CALPartitions.CAL_SINGLE_LINE_COMMENT);

        dr = new DefaultDamagerRepairer(getStringScanner());
        reconciler.setDamager(dr, CALPartitions.CAL_STRING);
        reconciler.setRepairer(dr, CALPartitions.CAL_STRING);

        dr = new DefaultDamagerRepairer(getStringScanner());
        reconciler.setDamager(dr, CALPartitions.CAL_CHARACTER);
        reconciler.setRepairer(dr, CALPartitions.CAL_CHARACTER);
        
        
        return reconciler;
    }
    
    /*
     * @see SourceViewerConfiguration#getContentAssistant(ISourceViewer)
     */
    @Override
    public IContentAssistant getContentAssistant(ISourceViewer sourceViewer) {

        /*
         * TODOEL
         */
        if (getEditor() != null) {            
            CompletionProcessor completionProcessor = getCompletionProcessor();
            if (completionProcessor != null) {
                assistant.setContentAssistProcessor(completionProcessor, IDocument.DEFAULT_CONTENT_TYPE);
                assistant.enableAutoActivation(true);
                assistant.enableAutoInsert(true);
            }
                
            return assistant;
        }
        
        return null;
    }

    /**
     * Returns whether the suffix is a suffix of this module name
     * 
     * For example, "Prelude" is a suffix of the name "Cal.Core.Prelude", or
     * "Core.Prelude" is a suffix of "Cal.Core.Prelude".
     * 
     * @param suffix The module name that might be a suffix of this name.
     * @param lastNameIsPartial The last name of the suffix is only partially entered.
     * @return true if the suffix is a suffix of this module name. 
     */
    
    public static boolean isSuffixOf(final ModuleName name, final String[] suffix, boolean lastNameIsPartial) {
        int i_suffix = suffix.length - 1;
        int i_prefix = name.getNComponents() - 1;
        if (i_suffix > i_prefix) {
            return false;
        }        
        
        if (lastNameIsPartial){
            if (!name.getNthComponent(i_prefix--).startsWith(suffix[i_suffix--])){
                return false;
            }
        }
        
        while(i_suffix >= 0){
            if (!suffix[i_suffix--].equals(name.getNthComponent(i_prefix--))){
                return false;
            }
        }
        
        return true;
    }
    
    /**
     * Returns whether the suffix is a suffix of this module name
     * 
     * For example, "Core" is the middle of "Cal.Core.Prelude".
     * 
     * @param middle The module name that might be a suffix of this name.
     * @param lastNameIsPartial The last name of the suffix is only partially entered.
     * @return true if the suffix is a suffix of this module name. 
     */
    
    public static boolean isMiddleOf(final ModuleName name, final String[] middle, boolean lastNameIsPartial) {

        // Search for the first matching component
        int i_prefix = 0;
        boolean foundMatch = false;
        for(; i_prefix < name.getNComponents(); ++i_prefix){
            if (lastNameIsPartial && middle.length == 1){
                if (name.getNthComponent(i_prefix).startsWith(middle[0])){
                    foundMatch = true;
                    break;
                }
            }
            else{
                if (middle[0].equals(name.getNthComponent(i_prefix))){
                    foundMatch = true;
                    break;
                }
            }
        }
        
        if (!foundMatch){
            return false;
        }

        // match the rest of the components
        int i_middle = 1;
        i_prefix++;
        while(true){
            if (i_middle >= middle.length){
                return true;
            }
            if (i_prefix >= name.getNComponents()){
                return false;
            }
            // if at the last name then partial matches are allowed
            if (lastNameIsPartial && i_middle == middle.length - 1){
                if (!name.getNthComponent(i_prefix++).startsWith(middle[i_middle++])){
                    return false;
                }                    
            }
            else{
                if (!middle[i_middle++].equals(name.getNthComponent(i_prefix++))){
                    return false;
                }
            }
        }
    }

    /**
     * Get the completion processor for the current editor.
     * @return the completion processor for the current editor, or null if none applies.
     */
    private CompletionProcessor getCompletionProcessor() {
        // ensure that we are working with a CALEditor and not an
        // embedded editor (see CAL_Eclipse_EmbeddedEditor project)
        ITextEditor textEditor = getEditor();   
        if (textEditor == null || !(textEditor instanceof CALEditor)) {
            return null;
        }
        final CALEditor calEditor = (CALEditor) textEditor;
        
        if (calEditor.getEditorInput() instanceof IFileEditorInput) {
            try{
                return new CompletionProcessor(calEditor, fPreferenceStore);
            }
            catch(IllegalArgumentException e){
                // The name of the file does not 
                // correspond to a valid module name.
                return null;
            }
        }
        return null;
    }
    /*
     * @see SourceViewerConfiguration#getReconciler(ISourceViewer)
     */
    @Override
    public IReconciler getReconciler(ISourceViewer sourceViewer) {
        return null;
    }
    
    /*
     * @see org.eclipse.jface.text.source.SourceViewerConfiguration#getAutoEditStrategies(org.eclipse.jface.text.source.ISourceViewer, java.lang.String)
     */
    @Override
    public IAutoEditStrategy[] getAutoEditStrategies(ISourceViewer sourceViewer, String contentType) {
        String partitioning = getConfiguredDocumentPartitioning(sourceViewer);
        if (CALPartitions.CAL_DOC.equals(contentType) || CALPartitions.CAL_MULTI_LINE_COMMENT.equals(contentType)) {
            return new IAutoEditStrategy[]{new CALDocAutoIndentStrategy(partitioning)};
        }
//        else if (CALPartitions.CAL_STRING.equals(contentType))
//            return new IAutoEditStrategy[]{new SmartSemicolonAutoEditStrategy(partitioning), new JavaStringAutoIndentStrategy(partitioning)};
//        else if (CALPartitions.CAL_CHARACTER.equals(contentType) || IDocument.DEFAULT_CONTENT_TYPE.equals(contentType))
//            return new IAutoEditStrategy[]{new SmartSemicolonAutoEditStrategy(partitioning), new JavaAutoIndentStrategy(partitioning, getProject())};
//        else
//            return new IAutoEditStrategy[]{new JavaAutoIndentStrategy(partitioning, getProject())};
        return new IAutoEditStrategy[] {
//                new DefaultIndentLineAutoEditStrategy()
                new CALAutoIndentStrategy(partitioning, getProject())         // does the wrong thing, eg. newline at end of an indented comment -> not indented
        };
    }

    /*
     * @see SourceViewerConfiguration#getDoubleClickStrategy(ISourceViewer, String)
     */
    @Override
    public ITextDoubleClickStrategy getDoubleClickStrategy(ISourceViewer sourceViewer, String contentType) {
        if (CALPartitions.CAL_DOC.equals(contentType)) {
            return new CALDocDoubleClickStrategy();
        }
        if (CALPartitions.CAL_MULTI_LINE_COMMENT.equals(contentType) || CALPartitions.CAL_SINGLE_LINE_COMMENT.equals(contentType)) {
            return new DefaultTextDoubleClickStrategy();
        } else if (CALPartitions.CAL_STRING.equals(contentType) || CALPartitions.CAL_CHARACTER.equals(contentType)) {
            return new CALStringDoubleClickSelector(getConfiguredDocumentPartitioning(sourceViewer));
        }
        if (fCALDoubleClickSelector == null) {
            fCALDoubleClickSelector = new CALDoubleClickSelector();
        }
        return fCALDoubleClickSelector;
    }
    
    /*
     * @see SourceViewerConfiguration#getDefaultPrefixes(ISourceViewer, String)
     */
    @Override
    public String[] getDefaultPrefixes(ISourceViewer sourceViewer,
            String contentType) {
        return new String[] { "//", "" }; //$NON-NLS-1$ //$NON-NLS-2$
    }

    /*
     * @see SourceViewerConfiguration#getIndentPrefixes(ISourceViewer, String)
     */
    @Override
    public String[] getIndentPrefixes(ISourceViewer sourceViewer,
            String contentType) {

        Vector<String> vector = new Vector<String>();

        // prefix[0] is either '\t' or ' ' x tabWidth, depending on useSpaces

        IProject project = getProject();
        final int tabWidth = CodeFormatterUtil.getTabWidth(project);
        final int indentWidth = CodeFormatterUtil.getIndentWidth(project);
        int spaceEquivalents = Math.min(tabWidth, indentWidth);
        boolean useSpaces;
//        if (project == null)
            useSpaces = CoreOptionIDs.SPACE.equals(CALEclipseCorePlugin.getOption(DefaultCodeFormatterConstants.FORMATTER_TAB_CHAR)) || tabWidth > indentWidth;
//        else
//            useSpaces = CoreOptionIDs.SPACE.equals(project.getOption(DefaultCodeFormatterConstants.FORMATTER_TAB_CHAR, true)) || tabWidth > indentWidth;

        for (int i = 0; i <= spaceEquivalents; i++) {
            StringBuilder prefix = new StringBuilder();

            if (useSpaces) {
                for (int j = 0; j + i < spaceEquivalents; j++) {
                    prefix.append(' ');
                }

                if (i != 0) {
                    prefix.append('\t');
                }
            } else {
                for (int j = 0; j < i; j++) {
                    prefix.append(' ');
                }

                if (i != spaceEquivalents) {
                    prefix.append('\t');
                }
            }

            vector.add(prefix.toString());
        }

        vector.add(""); //$NON-NLS-1$

        return vector.toArray(new String[vector.size()]);
    }

    private IProject getProject() {
        return null;
//        ITextEditor editor= getEditor();
//        if (editor == null)
//            return null;
//        
//        IJavaElement element= null;
//        IEditorInput input= editor.getEditorInput();
//        IDocumentProvider provider= editor.getDocumentProvider();
//        if (provider instanceof ICompilationUnitDocumentProvider) {
//            ICompilationUnitDocumentProvider cudp= (ICompilationUnitDocumentProvider) provider;
//            element= cudp.getWorkingCopy(input);
//        } else if (input instanceof IClassFileEditorInput) {
//            IClassFileEditorInput cfei= (IClassFileEditorInput) input;
//            element= cfei.getClassFile();
//        }
//        
//        if (element == null)
//            return null;
//        
//        return element.getJavaProject();
    }
    
    /*
     * @see SourceViewerConfiguration#getTabWidth(ISourceViewer)
     */
    @Override
    public int getTabWidth(ISourceViewer sourceViewer) {
        return CodeFormatterUtil.getTabWidth(getProject());
    }
    
    /*
     * @see SourceViewerConfiguration#getAnnotationHover(ISourceViewer)
     */
    @Override
    public IAnnotationHover getAnnotationHover(ISourceViewer sourceViewer) {
        return new CALAnnotationHover(CALAnnotationHover.VERTICAL_RULER_HOVER);
    }
    
    /*
     * @see SourceViewerConfiguration#getOverviewRulerAnnotationHover(ISourceViewer)
     */
    @Override
    public IAnnotationHover getOverviewRulerAnnotationHover(ISourceViewer sourceViewer) {
        return new CALAnnotationHover(CALAnnotationHover.OVERVIEW_RULER_HOVER);
    }
    
    /**
     * 
     * {@inheritDoc}
     */
    @Override
    public int[] getConfiguredTextHoverStateMasks(ISourceViewer sourceViewer, String contentType) {
        return new int[] {
            ITextViewerExtension2.DEFAULT_HOVER_STATE_MASK,
            SWT.SHIFT,
//            SWT.CONTROL
        };
    }

    
    /*
     * @see SourceViewerConfiguration#getTextHover(ISourceViewer, String, int)
     */
    @Override
    public ITextHover getTextHover(ISourceViewer sourceViewer, String contentType, int stateMask) {
        // XXX Hack.  We need to determine if we are being called from CALInformationProvider or from 
        // the Source Viewer.  In the former, we want the hover to be sticky (hence first argument is true.  
        // In the latter we do not want the hover to stick.
        // We control this by looking at the first two arguments, which in the case of the CALInformationProvider 
        // will always be null.
        return new CALTextHover(sourceViewer != null || contentType != null, stateMask, (CALEditor) getEditor());
    }
    
    /*
     * @see SourceViewerConfiguration#getTextHover(ISourceViewer, String)
     */
    @Override
    public ITextHover getTextHover(ISourceViewer sourceViewer, String contentType) {
        return getTextHover(sourceViewer, contentType, ITextViewerExtension2.DEFAULT_HOVER_STATE_MASK);
    }
    
    /*
     * @see SourceViewerConfiguration#getConfiguredContentTypes(ISourceViewer)
     */
    @Override
    public String[] getConfiguredContentTypes(ISourceViewer sourceViewer) {
        return new String[] {
                IDocument.DEFAULT_CONTENT_TYPE,
                CALPartitions.CAL_DOC,
                CALPartitions.CAL_MULTI_LINE_COMMENT,
                CALPartitions.CAL_SINGLE_LINE_COMMENT,
                CALPartitions.CAL_STRING,
                CALPartitions.CAL_CHARACTER
        };
    }
    
    /*
     * @see org.eclipse.jface.text.source.SourceViewerConfiguration#getConfiguredDocumentPartitioning(org.eclipse.jface.text.source.ISourceViewer)
     */
    @Override
    public String getConfiguredDocumentPartitioning(ISourceViewer sourceViewer) {
        if (fDocumentPartitioning != null) {
            return fDocumentPartitioning;
        }
        return super.getConfiguredDocumentPartitioning(sourceViewer);
    }
    
    /*
     * @see SourceViewerConfiguration#getContentFormatter(ISourceViewer)
     */
    @Override
    public IContentFormatter getContentFormatter(ISourceViewer sourceViewer) {
        return null;
    }
    
    /**
     * Creates a hover control creator that is used for hovers over the 
     * overview bar and other annotation hovers
     */
    @Override
    public IInformationControlCreator getInformationControlCreator(ISourceViewer sourceViewer) {
        return new IInformationControlCreator() {
            public IInformationControl createInformationControl(Shell parent) {
                return new DefaultInformationControl(parent, SWT.NONE, new HTMLTextPresenter(true));
            }
        };
    }
    
    /*
     * @see SourceViewerConfiguration#getInformationPresenter(ISourceViewer)
     */
    @Override
    public IInformationPresenter getInformationPresenter(ISourceViewer sourceViewer) {
        InformationPresenter presenter= new InformationPresenter(new IInformationControlCreator() {
            public IInformationControl createInformationControl(Shell parent) {
                int shellStyle= SWT.RESIZE | SWT.TOOL;
                int style= SWT.V_SCROLL | SWT.H_SCROLL;
                return new DefaultInformationControl(parent, shellStyle, style, new HTMLTextPresenter(false));
            }
        });
        presenter.setDocumentPartitioning(getConfiguredDocumentPartitioning(sourceViewer));
        IInformationProvider provider= new CALInformationProvider(getEditor());
        presenter.setInformationProvider(provider, IDocument.DEFAULT_CONTENT_TYPE);
        presenter.setSizeConstraints(60, 10, true, true);
        return presenter;
    }
    
    /**
     * This is the hook needed so that the tooltip shows in a dialog that has a helper string on the bottom
     * that says "Press 'F2' for focus." and other things. 
     */
    public class CALInformationProvider implements IInformationProvider, IInformationProviderExtension2 {

        /**
         * Needed so update is called and the current hover information is used (if any).
         */
        class EditorWatcher implements IPartListener {

            /**
             * @see IPartListener#partOpened(IWorkbenchPart)
             */
            public void partOpened(IWorkbenchPart part) {
                update();
            }

            /**
             * @see IPartListener#partDeactivated(IWorkbenchPart)
             */
            public void partDeactivated(IWorkbenchPart part) {
            }

            /**
             * @see IPartListener#partClosed(IWorkbenchPart)
             */
            public void partClosed(IWorkbenchPart part) {
                if (part == fEditor) {
                    fEditor.getSite().getWorkbenchWindow().getPartService().removePartListener(fPartListener);
                    fPartListener= null;
                }
            }

            /**
             * @see IPartListener#partActivated(IWorkbenchPart)
             */
            public void partActivated(IWorkbenchPart part) {
                update();
            }

            public void partBroughtToTop(IWorkbenchPart part) {
                update();
            }
        }

        protected IEditorPart fEditor;
        protected IPartListener fPartListener;

        protected String fCurrentPerspective;
        protected CALTextHover fImplementation;

        public CALInformationProvider(IEditorPart editor) {

            fEditor= editor;

            if (fEditor != null) {

                fPartListener= new EditorWatcher();
                IWorkbenchWindow window= fEditor.getSite().getWorkbenchWindow();
                window.getPartService().addPartListener(fPartListener);

                update();
            }
        }

        protected void update() {
            IWorkbenchWindow window= fEditor.getSite().getWorkbenchWindow();
            IWorkbenchPage page= window.getActivePage();
            if (page != null) {

                IPerspectiveDescriptor perspective= page.getPerspective();
                if (perspective != null)  {
                    String perspectiveId= perspective.getId();

                    if (fCurrentPerspective == null || !fCurrentPerspective.equals(perspectiveId)) {
                        fCurrentPerspective= perspectiveId;
                        fImplementation= (CALTextHover) getTextHover(null, null, ITextViewerExtension2.DEFAULT_HOVER_STATE_MASK);
                    }
                }
            }
        }

        /*
         * @see IInformationProvider#getSubject(ITextViewer, int)
         */
        public IRegion getSubject(ITextViewer textViewer, int offset) {
            // just return the current position since the lookup code will determine
            // what symbol is selected.
            return new Region(offset, 1);
        }

        /*
         * @see IInformationProvider#getInformation(ITextViewer, IRegion)
         */
        public String getInformation(ITextViewer textViewer, IRegion subject) {
            if (fImplementation != null) {
                String s= fImplementation.getHoverInfo(textViewer, subject);
                if (s != null && s.trim().length() > 0) {
                    return s;
                }
            }

            return null;
        }

        /*
         * This is the hover that gets created when F2 is pressed on an existing hover
         * @see IInformationProviderExtension2#getInformationPresenterControlCreator()
         * @since 3.1
         */
        public IInformationControlCreator getInformationPresenterControlCreator() {
            return new IInformationControlCreator() {
                public IInformationControl createInformationControl(Shell parent) {
                    int shellStyle= SWT.RESIZE | SWT.TOOL;
                    int style= SWT.V_SCROLL | SWT.H_SCROLL;
                    if (fImplementation.showInBrowser && BrowserInformationControl.isAvailable(parent)) {
                        return new BrowserInformationControl(parent, shellStyle, style);
                    } else {
                        return new CALSourceInformationControl(parent, shellStyle, style);
                    }
                }
            };
        }
    }


    
    /**
     * Returns the outline presenter which will determine and shown
     * information requested for the current cursor position.
     *
     * @param sourceViewer the source viewer to be configured by this configuration
     * @param doCodeResolve a boolean which specifies whether code resolve should be used to compute the Java element
     * @return an information presenter
     * @since 2.1
     */
    public IInformationPresenter getOutlinePresenter(ISourceViewer sourceViewer, boolean doCodeResolve) {
        InformationPresenter presenter;
        if (doCodeResolve)
            presenter= new InformationPresenter(getOutlinePresenterControlCreator(sourceViewer, CALEditorActionDefinitionIds.OPEN_STRUCTURE));
        else
            presenter= new InformationPresenter(getOutlinePresenterControlCreator(sourceViewer, CALEditorActionDefinitionIds.SHOW_OUTLINE));
        presenter.setDocumentPartitioning(getConfiguredDocumentPartitioning(sourceViewer));
        presenter.setAnchor(AbstractInformationControlManager.ANCHOR_GLOBAL);
        IInformationProvider provider= new CALElementProvider(getEditor(), doCodeResolve);
        
        final String partitions[] = {
        		IDocument.DEFAULT_CONTENT_TYPE,
        		fDocumentPartitioning,
        		CALPartitions.CAL_PARTITIONING,
        		CALPartitions.CAL_SINGLE_LINE_COMMENT,
        		CALPartitions.CAL_MULTI_LINE_COMMENT,
        		CALPartitions.CAL_DOC,
        		CALPartitions.CAL_STRING,
        		CALPartitions.CAL_CHARACTER};
        for(String partition : partitions){
            presenter.setInformationProvider(provider, partition);
        }
        
        presenter.setSizeConstraints(50, 20, true, false);
        return presenter;
    }
    
    public class CALElementProvider implements IInformationProvider, IInformationProviderExtension {

        private CALEditor fEditor;

        public CALElementProvider(IEditorPart editor) {
            if (editor instanceof CALEditor) {
                fEditor = (CALEditor)editor;
            }
        }

        public CALElementProvider(IEditorPart editor, boolean useCodeResolve) {
            this(editor);
        }

        /*
         * @see IInformationProvider#getSubject(ITextViewer, int)
         */
        public IRegion getSubject(ITextViewer textViewer, int offset) {
//            if (textViewer != null && fEditor != null) {
//                IRegion region= JavaWordFinder.findWord(textViewer.getDocument(), offset);
//                if (region != null)
//                    return region;
//                else
//                    return new Region(offset, 0);
//            }
            return new Region(offset, 0);
        }

        /*
         * @see IInformationProvider#getInformation(ITextViewer, IRegion)
         */
        public String getInformation(ITextViewer textViewer, IRegion subject) {
            return getInformation2(textViewer, subject).toString();
        }

        /*
         * @see IInformationProviderExtension#getElement(ITextViewer, IRegion)
         */
        public Object getInformation2(ITextViewer textViewer, IRegion subject) {
            if (fEditor == null)
                return null;

            if (textViewer instanceof CALEditor.AdaptedSourceViewer){
                CALEditor.AdaptedSourceViewer asv = (AdaptedSourceViewer) textViewer;
                final IStorage storage = asv.getEditor().getStorage();
                try{
                    CALModelManager cmm = CALModelManager.getCALModelManager();
                    return cmm.getModuleName(storage);
                }
                catch(IllegalArgumentException ex){
                    // CAL File is not in the correct spot in the hierarchy so there
                    // is no type information available.
                    CoreUtility.showMessage(ActionMessages.OpenDeclarationAction_error_title, ActionMessages.error_calFileNotInCorrectLocation_message, IStatus.ERROR);
                    return null;
                }
            }
            return null;
        }
    }

    /**
     * Returns the hierarchy presenter which will determine and shown type hierarchy
     * information requested for the current cursor position.
     *
     * @param sourceViewer the source viewer to be configured by this configuration
     * @param doCodeResolve a boolean which specifies whether code resolve should be used to compute the Java element
     * @return an information presenter
     */
    public IInformationPresenter getHierarchyPresenter(ISourceViewer sourceViewer, boolean doCodeResolve) {
        // TODOEL
        return null;
    }
    
    /**
     * Determines whether the preference change encoded by the given event
     * changes the behavior of one of its contained components.
     *
     * @param event the event to be investigated
     * @return <code>true</code> if event causes a behavioral change
     */
    public boolean affectsTextPresentation(PropertyChangeEvent event) {
        return  fCodeScanner.affectsBehavior(event)
        || fMultilineCommentScanner.affectsBehavior(event)
        || fSinglelineCommentScanner.affectsBehavior(event)
        || fStringScanner.affectsBehavior(event)
        || fCALDocScanner.affectsBehavior(event);
    }
    
    /**
     * Adapts the behavior of the contained components to the change
     * encoded in the given event.
     * <p>
     * Clients are not allowed to call this method if the old setup with
     * text tools is in use.
     * </p>
     *
     * @param event the event to which to adapt
     * @see CALSourceViewerConfiguration#CALSourceViewerConfiguration(ColorManager, IPreferenceStore, ITextEditor, String)
     */
    public void handlePropertyChangeEvent(PropertyChangeEvent event) {
        Assert.isTrue(isNewSetup());
        if (fCodeScanner.affectsBehavior(event)) {
            fCodeScanner.adaptToPreferenceChange(event);
        }
        if (fMultilineCommentScanner.affectsBehavior(event)) {
            fMultilineCommentScanner.adaptToPreferenceChange(event);
        }
        if (fSinglelineCommentScanner.affectsBehavior(event)) {
            fSinglelineCommentScanner.adaptToPreferenceChange(event);
        }
        if (fStringScanner.affectsBehavior(event)) {
            fStringScanner.adaptToPreferenceChange(event);
        }
        if (fCALDocScanner.affectsBehavior(event)) {
            fCALDocScanner.adaptToPreferenceChange(event);
        }
    }
    
    /*
     * @see org.eclipse.jface.text.source.SourceViewerConfiguration#getHyperlinkDetectors(org.eclipse.jface.text.source.ISourceViewer)
     */
    @Override
    public IHyperlinkDetector[] getHyperlinkDetectors(ISourceViewer sourceViewer) {
        IHyperlinkDetector[] hyperlinkDetectors = super.getHyperlinkDetectors(sourceViewer);
        ArrayList<IHyperlinkDetector> newHD = new ArrayList<IHyperlinkDetector>();
        for (IHyperlinkDetector hd : hyperlinkDetectors){
            newHD.add(hd);            
        }
        newHD.add(new CALHyperlinkDetector());
        return newHD.toArray(new IHyperlinkDetector[newHD.size()]);
    }
    
    public class QuickAssistProcessor implements IQuickAssistProcessor {
        private final HashMap<IMarker, ICompletionProposal[]> fResMap = new HashMap<IMarker, ICompletionProposal[]>();
        
        public boolean canAssist(IQuickAssistInvocationContext invocationContext) {
            return true;
        }

        public boolean canFix(Annotation annotation) {
            if (!(annotation instanceof MarkerAnnotation)) {
                return false;
            }
            return CALDocumentProvider.canFix(annotation);
        }

        ICompletionProposal getResult(final CALBuilder.IQuickFix quickFix){
            return new CP(quickFix);
        }

        private class CP implements ICompletionProposal, ICompletionProposalExtension2{
            private final IQuickFix quickFix;

            CP(final CALBuilder.IQuickFix quickFix) {
                this.quickFix = quickFix;
            }
            
            public void apply(ITextViewer viewer, char trigger, int stateMask, int offset) {
                CompilerMessageLogger messageLogger = new MessageLogger();                    
                final IDocument document = viewer.getDocument();
                try {
                    final int firstLine = document.getLineOfOffset(offset);
                    final int column = CoreUtility.getColumn(firstLine, offset, document);
                    final SourcePosition newSourcePosition = quickFix.applyFix(firstLine + 1, column + 1, messageLogger);
                    CoreUtility.showErrors(ActionMessages.Error_messageDialog_title, ActionMessages.QuickAssist_failed, messageLogger);
                    if (newSourcePosition != null){
                        final int cursorPosition = CoreUtility.toOffset(newSourcePosition, viewer.getDocument());
                        CALEditor.AdaptedSourceViewer asv = (AdaptedSourceViewer) viewer;                        
                        asv.getEditor().selectAndReveal(cursorPosition, 0);
                    }

                } catch (BadLocationException e) {
                }
            }

            public void selected(ITextViewer viewer, boolean smartToggle) {
            }

            public void unselected(ITextViewer viewer) {
            }

            public boolean validate(IDocument document, int offset, DocumentEvent event) {
                return true;
            }

            // This will not be called because the newer interface is used.
            public void apply(IDocument document) {
            }

            public String getAdditionalProposalInfo() {
                return null;
            }

            public IContextInformation getContextInformation() {
                return null;
            }

            public String getDisplayString() {
                return quickFix.getDescription();
            }

            public Image getImage() {
                return null;
            }

            public Point getSelection(IDocument document) {
                return null;
            }
        };

        public ICompletionProposal[] computeQuickAssistProposals(IQuickAssistInvocationContext invocationContext) {
            CALEditor.AdaptedSourceViewer sv = (CALEditor.AdaptedSourceViewer) invocationContext.getSourceViewer();
            IAnnotationModel amodel = invocationContext.getSourceViewer().getAnnotationModel();
            IDocument doc = invocationContext.getSourceViewer().getDocument();
            
            int offset = invocationContext.getOffset();
            ArrayList<ICompletionProposal> list = new ArrayList<ICompletionProposal>();
            // look through the annotation for this line that have quick fixes.
            for (Iterator<Annotation> it = UnsafeCast.unsafeCast(amodel.getAnnotationIterator()); it.hasNext(); ) {
                Annotation key = it.next();
                if (!(key instanceof MarkerAnnotation)) {
                    continue;
                }
                
                MarkerAnnotation annotation = (MarkerAnnotation)key;
                IMarker marker = annotation.getMarker();

                // The possible solutions are cached.
                ICompletionProposal[] mapping = fResMap.get(marker);
                if (mapping == null){
                    CALBuilder.IQuickFix[] qfs = CALDocumentProvider.getQuickFixes(annotation, sv.getModuleName(), sv.getSourceManagerFactory(true));
                    mapping = new ICompletionProposal[qfs.length];
                    for(int i = 0; i < qfs.length; ++i){
                        mapping[i] = getResult(qfs[i]);
                    }
                    fResMap.put(marker, mapping);
                }
                if (mapping != null) {
                    Position pos = amodel.getPosition(annotation);
                    try {
                        int line = doc.getLineOfOffset(pos.getOffset());
                        int start = pos.getOffset();
                        String delim = doc.getLineDelimiter(line);
                        int delimLength = delim != null ? delim.length() : 0;
                        int end = doc.getLineLength(line) + start - delimLength;
                        if (offset >= start && offset <= end) {
                            for (final ICompletionProposal completionProposal : mapping) {
                                list.add(completionProposal);
                            }
                        }
                    } catch (BadLocationException e) {
                    }                    
                }
            }
            return list.toArray(new ICompletionProposal[list.size()]);
        }

        // error messages from trying to generate quick fixes.
        public String getErrorMessage() {
            return null;
        }            
    }

    @Override
    public IQuickAssistAssistant getQuickAssistAssistant(ISourceViewer sourceViewer){
        QuickAssistAssistant qaa = new QuickAssistAssistant();
        qaa.setQuickAssistProcessor(new QuickAssistProcessor());
        return qaa;
    }
    
    /**
     * Returns the outline presenter control creator. The creator is a factory creating outline
     * presenter controls for the given source viewer. This implementation always returns a creator
     * for <code>JavaOutlineInformationControl</code> instances.
     *
     * @param sourceViewer the source viewer to be configured by this configuration
     * @param commandId the ID of the command that opens this control
     * @return an information control creator
     * @since 2.1
     */
    private IInformationControlCreator getOutlinePresenterControlCreator(ISourceViewer sourceViewer, final String commandId) {
        return new IInformationControlCreator() {
            public IInformationControl createInformationControl(Shell parent) {
                int shellStyle= SWT.RESIZE;
                int treeStyle= SWT.V_SCROLL | SWT.H_SCROLL;
                return new CALOutlineInformationControl(parent, shellStyle, treeStyle, commandId);
            }
        };
    }

}
