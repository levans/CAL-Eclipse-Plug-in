/*******************************************************************************
 * Copyright (c) 2006 Business Objects Software Limited and others.
 * All rights reserved. 
 * This file is made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Business Objects Software Limited - initial API and implementation based on Eclipse 3.1.2 code for
 *                             /org.eclipse.jdt.ui/ui/org/eclipse/jdt/internal/ui/search/JavaSearchPage.java
 *                           Eclipse source is available at: http://www.eclipse.org/downloads/
 *******************************************************************************/

/*
 * CALSearchPage.java
 * Creation date: Sep 26, 2006.
 * By: Edward Lam
 */
package org.openquark.cal.eclipse.ui.search;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringReader;
import java.text.Collator;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.SortedSet;
import java.util.TreeSet;

import org.eclipse.core.filebuffers.FileBuffers;
import org.eclipse.core.filebuffers.ITextFileBuffer;
import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IStorage;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.core.runtime.SubProgressMonitor;
import org.eclipse.jdt.internal.core.JarEntryFile;
import org.eclipse.jdt.internal.ui.javaeditor.JarEntryEditorInput;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.DialogPage;
import org.eclipse.jface.dialogs.IDialogSettings;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.ITextSelection;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.search.ui.ISearchPage;
import org.eclipse.search.ui.ISearchPageContainer;
import org.eclipse.search.ui.ISearchQuery;
import org.eclipse.search.ui.ISearchResult;
import org.eclipse.search.ui.NewSearchUI;
import org.eclipse.search.ui.text.AbstractTextSearchResult;
import org.eclipse.search.ui.text.IEditorMatchAdapter;
import org.eclipse.search.ui.text.IFileMatchAdapter;
import org.eclipse.search.ui.text.Match;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IFileEditorInput;
import org.eclipse.ui.IStorageEditorInput;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.IWorkingSet;
import org.eclipse.ui.IWorkingSetManager;
import org.eclipse.ui.PlatformUI;
import org.openquark.cal.compiler.CompilerMessageLogger;
import org.openquark.cal.compiler.MessageLogger;
import org.openquark.cal.compiler.ModuleName;
import org.openquark.cal.compiler.ModuleSourceDefinition;
import org.openquark.cal.compiler.ModuleTypeInfo;
import org.openquark.cal.compiler.SearchResult;
import org.openquark.cal.compiler.SourceIdentifier;
import org.openquark.cal.compiler.SourceMetrics;
import org.openquark.cal.compiler.SourceMetricsManager;
import org.openquark.cal.compiler.SourceRange;
import org.openquark.cal.eclipse.core.CALModelManager;
import org.openquark.cal.eclipse.core.builder.ICALResourceContainer;
import org.openquark.cal.eclipse.ui.CALEclipseUIPlugin;
import org.openquark.cal.eclipse.ui.actions.ActionMessages;
import org.openquark.cal.eclipse.ui.actions.ActionUtilities;
import org.openquark.cal.eclipse.ui.caleditor.CALEditor;
import org.openquark.cal.eclipse.ui.util.CoreUtility;
import org.openquark.cal.eclipse.ui.util.Messages;



/**
 * Specialized search dialog page for CAL.
 * @author Edward Lam
 */
public class CALSearchPage extends DialogPage implements ISearchPage {

    private static final int HISTORY_SIZE = 12;
    public static final String EXTENSION_POINT_ID = "org.openquark.cal.eclipse.ui.search.CALSearchPage"; //$NON-NLS-1$

    // Dialog store id constants
    private static final String PAGE_NAME = "TextSearchPage"; //$NON-NLS-1$
    private static final String STORE_CASE_SENSITIVE = "CASE_SENSITIVE"; //$NON-NLS-1$
    private static final String STORE_SEARCH_DERIVED = "SEARCH_DERIVED"; //$NON-NLS-1$
    private static final String STORE_HISTORY = "HISTORY"; //$NON-NLS-1$
    private static final String STORE_HISTORY_SIZE = "HISTORY_SIZE"; //$NON-NLS-1$
    
    private static final String STORE_SCOPE = "SCOPE"; //$NON-NLS-1$

    private List<SearchPatternData> fPreviousSearchPatterns = new ArrayList<SearchPatternData>(20);

    private IDialogSettings fDialogSettings;
    private boolean fFirstTime = true;
    private boolean fIsCaseSensitive;
    private boolean fSearchDerived;
    private int fScope = SCOPE_ALL;
    
    private final static int SCOPE_ALL = 0;
    public final static int SCOPE_REFERENCES = 1;
    public final static int SCOPE_DEFINITIONS = 2;
    private final static int SCOPE_INSTANCES_OF_TYPE = 3;
    private final static int SCOPE_INSTANCES_OF_CLASS = 4;
    private final static int SCOPE_REFERENCES_CONSTRUCTIONS = 5;
    
    private static String scope_text[] = {
        SearchMessages.SearchPage_scope_all,
        SearchMessages.SearchPage_scope_references,
        SearchMessages.SearchPage_scope_definitions,
        SearchMessages.SearchPage_scope_instancesOfType,
        SearchMessages.SearchPage_scope_instancesOfClass,
        SearchMessages.SearchPage_scope_references_constructions
    };

    private Combo fPattern;
//    private Button fIgnoreCase;
//    private CLabel fStatusLabel;

    private ISearchPageContainer fContainer;

    private static class SearchPatternData {
        public final boolean ignoreCase;
        public final String textPattern;
        public final int scope;
        public final IWorkingSet[] workingSets;

        public SearchPatternData(String textPattern, boolean ignoreCase, int scope, IWorkingSet[] workingSets) {
            this.ignoreCase = ignoreCase;
            this.textPattern = textPattern;
            this.scope = scope;
            this.workingSets = workingSets; // can be null
        }

        public void store(IDialogSettings settings) {
            settings.put("ignoreCase", ignoreCase); //$NON-NLS-1$
            settings.put("textPattern", textPattern); //$NON-NLS-1$
            settings.put("scope", scope); //$NON-NLS-1$ 
            if (workingSets != null) {
                String[] wsIds = new String[workingSets.length];
                for (int i = 0; i < workingSets.length; i++) {
                    wsIds[i] = workingSets[i].getId();
                }
                settings.put("workingSets", wsIds); //$NON-NLS-1$
            } else {
                settings.put("workingSets", new String[0]); //$NON-NLS-1$
            }

        }

        public static SearchPatternData create(IDialogSettings settings) {
            String textPattern = settings.get("textPattern"); //$NON-NLS-1$
            String[] wsIds = settings.getArray("workingSets"); //$NON-NLS-1$
            IWorkingSet[] workingSets = null;
            if (wsIds != null && wsIds.length > 0) {
                IWorkingSetManager workingSetManager = PlatformUI.getWorkbench().getWorkingSetManager();
                workingSets = new IWorkingSet[wsIds.length];
                for (int i = 0; workingSets != null && i < wsIds.length; i++) {
                    workingSets[i] = workingSetManager.getWorkingSet(wsIds[i]);
                    if (workingSets[i] == null) {
                        workingSets = null;
                    }
                }
            }
            try {
                int scope = settings.getInt("scope"); //$NON-NLS-1$
                boolean ignoreCase = settings.getBoolean("ignoreCase"); //$NON-NLS-1$

                return new SearchPatternData(textPattern, ignoreCase, scope, workingSets);
            } catch (NumberFormatException e) {
                return null;
            }
        }

    }
    //---- Action Handling ------------------------------------------------
    
    public boolean performAction() {
        NewSearchUI.runQueryInBackground(getSearchQuery());
        return true;
    }

    // this is temporary during development. Will be gone once I add stuff to pay attent to the file scope.
    abstract static class CALSearchQuery{
        private CALModelManager modelManager;
        protected SourceMetricsManager sourceMetrics;
        protected SourceMetricsManager.IProgressMonitor monitor;
        private SearchScope searchScope;

        CALSearchQuery(SearchScope searchScope){
            this.searchScope = searchScope;
        }

        void setMonitor(SourceMetricsManager.IProgressMonitor monitor){
            this.monitor = monitor;
        }

        /**
         * Initializes members. This exists so that the values can be initialized after the compile
         * (if necessary) is completed.
         */
        void initialize(){
            modelManager = CALModelManager.getCALModelManager();
            sourceMetrics = modelManager.getSourceMetrics();
        }
        
        boolean contains(IResource outer, IResource inner){            
            IContainer current = inner.getParent();
            while (current != null){
                if (current.equals(outer)){
                    return true;
                }
                else{
                    current = current.getParent();
                }
            }
            return false;
        }
        
        /**
         * Get a list of moduleTypeInfos to search. This considers the information in the scope pane
         * of the search dialog.
         * 
         * TODO ADE does not work for JarEntries
         * 
         * @return A set of modules to search.
         */
        SortedSet/*ModuleTypeInfo*/<ModuleTypeInfo> getSelectedModulesEx(){
            SortedSet/*ModuleTypeInfo*/<ModuleTypeInfo> moduleTypeInfos = sourceMetrics.getSortedModuleTypeInfos();
            IResource[] roots = searchScope.getRootElements();
            if (roots.length > 0){
                SortedSet<ModuleTypeInfo> selectedModuleTypeInfos = new TreeSet<ModuleTypeInfo>(moduleTypeInfos.comparator());
                for (final ModuleTypeInfo moduleTypeInfo : moduleTypeInfos) {
                    IStorage storage = modelManager.getInputSourceFile(
                            moduleTypeInfo.getModuleName());
                    IResource toSearch = null;
                    if (storage instanceof IFile) {
                        toSearch = (IFile) storage;
                    } else if (storage instanceof JarEntryFile) {
                        ICALResourceContainer container = CALModelManager.getCALModelManager()
                            .getInputSourceFileContainer(moduleTypeInfo.getModuleName());
                        
                        // set toSearch to be the IProject
                        toSearch = container.getPackageRoot().getParent().getResource();
                    }
                    if (toSearch != null) {
                        for(int i = 0; i < roots.length; ++ i) {
                            IResource root = roots[i];
                            if (root instanceof IContainer) {
                                if (contains(root, toSearch)) {
                                    selectedModuleTypeInfos.add(moduleTypeInfo);
                                }
                            } else if (root instanceof IFile) {
                                if (root.equals(toSearch)){
                                    selectedModuleTypeInfos.add(moduleTypeInfo);
                                }
                            }
                        }
                    }
                }
                return selectedModuleTypeInfos;
            }
            return moduleTypeInfos;
        }
        
        /**
         * TODO What should I do with error messages logged during a search. I don't think the user
         * would be interested in seeing them because the compiler would have already put them
         * in the problems window. For now I am just ignoring them unless there is some reason not to.
         */
        CompilerMessageLogger messageLogger = new MessageLogger(); 

        public abstract void run();
    }

    /**
     * @author GMcClement
     *
     * This class contains the list of matches for a given query. Currently it contains Match and CALMatch objects.
     * A Match object is in the list in the case that the editor has the file open and line/column information
     * can be directly converted to offset/length notation. If the file is not open in the editor then a CALMatch
     * object is added to the list. Later on when the user selects a match the CALMatch object has the line/column 
     * notation converted to offset/length notation. This avoids having to open the file for every match. The conversion
     * occurs when the file happens to be open anyway.
     * 
     */
    static class CALSearchResults extends AbstractTextSearchResult implements IEditorMatchAdapter, IFileMatchAdapter {
        private ISearchQuery searchQuery;
        private String targetName;
        private SearchScope searchScope;
        
        public CALSearchResults(String targetName, SearchScope searchScope, ISearchQuery searchQuery){
            this.searchQuery = searchQuery;
            this.targetName = targetName;
            this.searchScope = searchScope;
        }
        
        @Override
        public IEditorMatchAdapter getEditorMatchAdapter() {
            return this;
        }

        @Override
        public IFileMatchAdapter getFileMatchAdapter() {
            return this;
        }

        public String getLabel() {
            int nMatches = getMatchCount();
            if (nMatches == 1) {
                Object[] args= { targetName, searchScope.getDescription() };
                return Messages.format(SearchMessages.FileSearchQuery_singularLabel, args); 
            }
            else {
                Object[] args= { targetName, Integer.valueOf(nMatches), searchScope.getDescription() };
                return Messages.format(SearchMessages.FileSearchQuery_pluralPattern, args); 
            }
        }

        public String getTooltip() {
            return getLabel();
        }

        public ImageDescriptor getImageDescriptor() {
            return null;
        }

        public ISearchQuery getQuery() {
            return searchQuery;
        }

        public boolean isShownInEditor(Match match, IEditorPart editor) {
            IEditorInput ei= editor.getEditorInput();
            if (ei instanceof IFileEditorInput) {
                IFileEditorInput fi = (IFileEditorInput) ei;
                return match.getElement().equals(fi.getFile());
            }
            return false;
        }

        public Match[] computeContainedMatches(AbstractTextSearchResult result, IEditorPart editor) {
            IEditorInput input = editor.getEditorInput();
            if (input instanceof IStorageEditorInput){
                IStorageEditorInput fei = (IStorageEditorInput) input;
                return getMatches(fei);            
            } else {
                return null;
            }
        }

        public Match[] computeContainedMatches(AbstractTextSearchResult result, IFile file) {
            return getMatches(file);
        }

        public IFile getFile(Object element) {
            if (element instanceof IFile){
                return (IFile) element;
            }
            else{
                return null;
            }
        }
        public JarEntryFile getJarEntry(Object element) {
            if (element instanceof JarEntryFile){
                return (JarEntryFile) element;
            }
            else{
                return null;
            }
        }
    };

    public static ITextFileBuffer getTrackedFileBuffer(AbstractTextSearchResult result, Object element) {
        IFileMatchAdapter adapter= result.getFileMatchAdapter();
        if (adapter == null) {
            return null;
        }
        IFile file= adapter.getFile(element);
        if (file != null && file.exists()) {
            return FileBuffers.getTextFileBufferManager().getTextFileBuffer(file.getFullPath());
        }
        if (adapter instanceof CALSearchResults) {
            CALSearchResults calAdapter = (CALSearchResults) adapter;
            JarEntryFile jarEntry = calAdapter.getJarEntry(element);
            if (jarEntry != null) {
//                return FileBuffers.getTextFileBufferManager().
                // can't really do anything here... jar entries do not have text file buffers.
            }
        }
        return null;
    }

    /**
     * @author GMcClement
     * This class exists because CAL reports matches differently then Eclipse expects them. 
     * This class starts out containing the line/column based position. At some point, if selected
     * this will convert to character based position. This fudging is necessary because
     * CAL returns search matches in line/column terms and Eclipse wants them in character offset 
     * and length. The conversion occurs when the user selects the search result because the
     * file is already open in a buffer and indexed to make the conversion faster. 
     */
    static class CALMatch extends Match {

        private SourceRange sourceRange;
        /**
         * Not set unless convertToCharacterPosition is called.
         */
        private int start;
        /**
         * Not set unless convertToCharacterPosition is called.
         */
        private int length;
        /**
         * This starts out containing the line/column based position. At some point if selected
         * this will convert to character based position. This fudging is necessary because
         * CAL returns search matches in line/column terms and Eclipse wants them in character offset 
         * and length. The conversion occurs when the user selects the search result because the
         * file is already open in a buffer and indexed to make the conversion faster. 
         */
        private boolean isCharacterPosition = false;
        
        public CALMatch(Object element, SourceRange sourceRange) {
            super(element, Match.UNIT_LINE, sourceRange.getStartLine(), sourceRange.getStartColumn());
            this.sourceRange = sourceRange;
        }
        
        public void convertToCharacterPosition(int start, int length){
            this.start = start;
            this.length = length;
            isCharacterPosition = true;
        }
        
        public int getStart(){
            return start;
        }
        
        @Override
        public int getLength(){
            return length;
        }
        
        boolean isCharacterPosition(){
            return isCharacterPosition;
        }
        
        public SourceRange getRange(){
            return sourceRange;
        }    
    }
    
    /**
     * Build a search query object using the given CALSearchQuery implementation.
     * @param targetName 
     * @param calSearchQuery
     * @return a search query for the given cal search.
     */
    private static ISearchQuery getCALSeachQuery(final String targetName, final SearchScope searchScope, final CALSearchQuery calSearchQuery){
        return new ISearchQuery () {
            private AbstractTextSearchResult searchResults = new CALSearchResults(targetName, searchScope, this);  
            private CALModelManager cmm = CALModelManager.getCALModelManager();

            public IStatus run(final IProgressMonitor outerMonitor) throws OperationCanceledException {
                /*
                 * If the CALBuilder was not run then run it before the search in order to 
                 * initialize the CAL model. I am ignoring errors thrown from here since
                 * this is just a temporary initialization anyway.
                 */
                                
                final boolean programWasInitialized = CoreUtility.initializeCALBuilder(outerMonitor, 100, 50);
                
                calSearchQuery.initialize();
                searchResults.removeAll();
                calSearchQuery.setMonitor(new SourceMetricsManager.IProgressMonitor() {
                    private IProgressMonitor searchMonitor;
                    {
                        if (programWasInitialized){
                            searchMonitor = outerMonitor;
                        }
                        else{
                            searchMonitor = new SubProgressMonitor(outerMonitor, 50);
                        }
                    }

                    public void numberOfModules(int numberOfModules) {
                        searchMonitor.beginTask(SearchMessages.SearchPage_searchingMessage, numberOfModules);
                    }

                    public void startModule(ModuleName module) {
                        searchMonitor.worked(1);
                    }

                    public boolean isCancelled() {
                        return searchMonitor.isCanceled();
                    }
                    
                    public void done(){
                        searchMonitor.done();
                    }

                    public void moreResults(Collection<? extends SearchResult> moreResults) {
                        for (final SearchResult searchResult : moreResults) {
                            SearchResult.Precise result = (SearchResult.Precise)searchResult;
                            SourceRange range = result.getSourceRange();
                            ModuleName maybeModuleName = ModuleName.maybeMake(range.getSourceName());
                            if (maybeModuleName == null) {
                                continue;
                            }
                            
                            IStorage sourceStorage = cmm.getInputSourceFile(maybeModuleName);
                            
                            /*
                             * If the file happens to be open then convert the position from the 
                             * line/column notation that CAL uses to the offset/length notation
                             * that Eclipse uses.
                             */
                            ITextFileBuffer tfb = getTrackedFileBuffer(searchResults, sourceStorage);
                            if (tfb != null){
                                IDocument doc = tfb.getDocument();
                                try {
                                    int start = CoreUtility.convertToCharacterPosition(range.getStartLine(), range.getStartColumn(), doc);
                                    int end = CoreUtility.convertToCharacterPosition(range.getEndLine(), range.getEndColumn(), doc);
                                    int length = end - start;
                                                                        
                                    searchResults.addMatch(new Match(sourceStorage, Match.UNIT_CHARACTER, start, length));    
                                } catch (BadLocationException e) {
                                    // save it for later then
                                    searchResults.addMatch(new CALMatch(sourceStorage, range));
                                }
                            }
                            else{
                                searchResults.addMatch(new CALMatch(sourceStorage, range));
                            }
                        }
                    }                    
                }
                );
                                
                 calSearchQuery.run();

                return new IStatus() {

                    public IStatus[] getChildren() {
                        return new IStatus[] {};
                    }

                    public int getCode() {
                        return OK;
                    }

                    public Throwable getException() {
                        return null;
                    }

                    public String getMessage() {
                        return null;
                    }

                    public String getPlugin() {
                        return null;
                    }

                    public int getSeverity() {
                        return 0;
                    }

                    public boolean isMultiStatus() {
                        return false;
                    }

                    public boolean isOK() {
                        return true;
                    }

                    public boolean matches(int severityMask) {
                        return false;
                    }                    
                };
            }

            public String getLabel() {
                return SearchMessages.SearchPage_CALSearchMessage;
            }

            public boolean canRerun() {
                return true;
            }

            public boolean canRunInBackground() {
                return true;
            }

            public ISearchResult getSearchResult() {
                return searchResults;
            }
            
        };
    }

    private static class WorkingSetComparator implements Comparator<Object> {

        private Collator fCollator= Collator.getInstance();
        
        /*
         * @see Comparator#compare(Object, Object)
         */
        public int compare(Object o1, Object o2) {
            String name1= null;
            String name2= null;
            
            if (o1 instanceof IWorkingSet)
                name1= ((IWorkingSet)o1).getLabel();

            if (o2 instanceof IWorkingSet)
                name2= ((IWorkingSet)o2).getLabel();

            return fCollator.compare(name1, name2);
        }
    }

    private static String toString(IWorkingSet[] workingSets) {
        String result= ""; //$NON-NLS-1$
        if (workingSets != null && workingSets.length > 0) {
            Arrays.sort(workingSets, new WorkingSetComparator());
            boolean firstFound= false;
            for (int i= 0; i < workingSets.length; i++) {
                String workingSetName= workingSets[i].getLabel();
                if (firstFound)
                    result= Messages.format(SearchMessages.ScopePart_workingSetConcatenation, new String[] { result, workingSetName }); 
                else {
                    result= workingSetName;
                    firstFound= true;
                }
            }
        }
        return result;
    }

    private ISearchQuery getSearchQuery() {
        // Setup search scope
        SearchScope searchScope = null;
        switch (getContainer().getSelectedScope()) {
            case ISearchPageContainer.WORKSPACE_SCOPE:
                searchScope = SearchScope.newWorkspaceScope();
                break;
            case ISearchPageContainer.SELECTION_SCOPE:
                searchScope = getSelectedResourcesScope(false, getSelection());
                break;
            case ISearchPageContainer.SELECTED_PROJECTS_SCOPE:
                searchScope = getSelectedResourcesScope(true, getSelection());
                break;
            case ISearchPageContainer.WORKING_SET_SCOPE:
                IWorkingSet[] workingSets = getContainer().getSelectedWorkingSets();
                String desc = Messages.format(SearchMessages.WorkingSetScope, toString(workingSets));
                searchScope = SearchScope.newSearchScope(desc, workingSets);
                break;
            default:
                // programming error.
                throw new IllegalStateException("Unknown scope: " + getContainer().getSelectedScope());
        }
        
        return getSearchQuery(searchScope, fScope, getPatternData().textPattern);
    }

    /**
     * Create a search for the current selection.
     * 
     * @param searchScopeCode the ISearchPageContainer scope, WORKSPACE_SCOPE, SELECTION_SCOPE, or SELECTED_PROJECTS_SCOPE.
     * @param searchKind the kind of search, SCOPE_ALL, SCOPE_REFERENCES, SCOPE_DEFINITIONS, SCOPE_INSTANCES_OF_TYPE, SCOPE_INSTANCES_OF_CLASS, SCOPE_REFERENCES_CONSTRUCTIONS.
     * @param selection
     * @return true if the search was performed. The search will only be performed if there is a valid current selection.
     */
    public static boolean performSearch(final int searchScopeCode, final int searchKind, ISelection selection) {
        SearchScope searchScope = null;
        switch (searchScopeCode) {
            case ISearchPageContainer.WORKSPACE_SCOPE:
                searchScope = SearchScope.newWorkspaceScope();
                break;
            case ISearchPageContainer.SELECTION_SCOPE:
                searchScope = getSelectedResourcesScope(false, selection);
                break;
            case ISearchPageContainer.SELECTED_PROJECTS_SCOPE:
                searchScope = getSelectedResourcesScope(true, selection);
                break;
//            case ISearchPageContainer.WORKING_SET_SCOPE:
//                IWorkingSet[] workingSets = getContainer().getSelectedWorkingSets();
//                String desc = Messages.format(SearchMessages.WorkingSetScope, ScopePart.toString(workingSets));
//                searchScope = SearchScope.newSearchScope(desc, workingSets);
//                break;
            default:
                // programming error.
                throw new IllegalStateException("Unknown scope: " + searchScopeCode);
        }
        
        final String textPattern = getPatternControlInitializeValue(selection);
        if (textPattern.equals("")){
            return false;
        }
        
        NewSearchUI.runQueryInBackground(getSearchQuery(searchScope, searchKind, textPattern));
        return true;
    }
    
    private static ISearchQuery getSearchQuery(final SearchScope searchScope, final int fScope, final String textPattern) {
        NewSearchUI.activateSearchResultView();
        searchScope.addFileNamePattern("*.cal");
        
        CALSearchQuery calSearchQuery = null;
        switch (fScope){ 
        case SCOPE_ALL:
            calSearchQuery = new CALSearchQuery(searchScope){        
            @Override
            public void run() {
                SortedSet<ModuleTypeInfo> selectedModuleTypeInfos = getSelectedModulesEx();
                sourceMetrics.findAllOccurrences(textPattern, selectedModuleTypeInfos, monitor, messageLogger);
                monitor.done();
            }            
        };
        break;
        
        case SCOPE_REFERENCES:
            calSearchQuery = new CALSearchQuery(searchScope){        
            @Override
            public void run() {
                SortedSet<ModuleTypeInfo> selectedModuleTypeInfos = getSelectedModulesEx();
                sourceMetrics.findReferences(textPattern, selectedModuleTypeInfos, monitor, messageLogger);
                monitor.done();
            }            
        };
        break;
        
        case SCOPE_DEFINITIONS:
            calSearchQuery = new CALSearchQuery(searchScope){        
            @Override
            public void run() {
                SortedSet<ModuleTypeInfo> selectedModuleTypeInfos = getSelectedModulesEx();
                SortedSet<ModuleName> selectedModuleNames = new TreeSet<ModuleName>();
                for (final ModuleTypeInfo moduleTypeInfo : selectedModuleTypeInfos) {
                    selectedModuleNames.add(moduleTypeInfo.getModuleName());
                }
                sourceMetrics.findDefinition(textPattern, selectedModuleNames, monitor, messageLogger);
                monitor.done();
            }            
        };
        break;
        
        case SCOPE_INSTANCES_OF_TYPE:            
            calSearchQuery = new CALSearchQuery(searchScope){        
            @Override
            public void run() {
                SortedSet<ModuleTypeInfo> selectedModuleTypeInfos = getSelectedModulesEx();
                sourceMetrics.findTypeInstances(textPattern, selectedModuleTypeInfos, monitor, messageLogger);
                monitor.done();
            }            
        };
        break;
        
        case SCOPE_INSTANCES_OF_CLASS:
            calSearchQuery = new CALSearchQuery(searchScope){        
            @Override
            public void run() {
                SortedSet<ModuleTypeInfo> selectedModuleTypeInfos = getSelectedModulesEx();
                sourceMetrics.findInstanceOfClass(textPattern, selectedModuleTypeInfos, monitor, messageLogger);
                monitor.done();
            }            
        };
        break;

        case SCOPE_REFERENCES_CONSTRUCTIONS:
            calSearchQuery = new CALSearchQuery(searchScope){        
            @Override
            public void run() {
                SortedSet<ModuleTypeInfo> selectedModuleTypeInfos = getSelectedModulesEx();
                sourceMetrics.findConstructions(textPattern, selectedModuleTypeInfos, monitor, messageLogger);
                monitor.done();
            }            
        };
        break;
        
        
//        default:
//            throw new UnknownError(); // programmer error.
        }

        return getCALSeachQuery(textPattern, searchScope, calSearchQuery);
    }

    private String getPattern() {
        return fPattern.getText();
    }

    private SearchPatternData findInPrevious(String pattern) {
        for (final SearchPatternData element : fPreviousSearchPatterns) {
            if (pattern.equals(element.textPattern)) {
                return element;
            }
        }
        return null;
    }
    
    /**
     * Return search pattern data and update previous searches.
     * An existing entry will be updated.
     * @return the search pattern data
     */
    private SearchPatternData getPatternData() {
        SearchPatternData match = findInPrevious(fPattern.getText());
        if (match != null) {
            fPreviousSearchPatterns.remove(match);
        }
        match = new SearchPatternData(
                getPattern(),
                ignoreCase(),
                getContainer().getSelectedScope(),
                getContainer().getSelectedWorkingSets());
        fPreviousSearchPatterns.add(0, match);
        return match;
    }

    private String[] getPreviousSearchPatterns() {
        int size = fPreviousSearchPatterns.size();
        String[] patterns = new String[size];
        for (int i = 0; i < size; i++)
            patterns[i] = fPreviousSearchPatterns.get(i).textPattern;
        return patterns;
    }

    private boolean ignoreCase() {
        return false;
    }
    
    /*
     * Implements method from IDialogPage
     */
    @Override
    public void setVisible(boolean visible) {
        if (visible && fPattern != null) {
            if (fFirstTime) {
                fFirstTime = false;
                // Set item and text here to prevent page from resizing
                fPattern.setItems(getPreviousSearchPatterns());
                if (!initializePatternControl()) {
                    fPattern.select(0);
                    handleWidgetSelected();
                }
            }
            fPattern.setFocus();
        }
        updateOKStatus();
        super.setVisible(visible);
    }

    final void updateOKStatus() {
        getContainer().setPerformActionEnabled(true);
    }

    //---- Widget creation ------------------------------------------------

    public void createControl(Composite parent) {
        initializeDialogUnits(parent);
        readConfiguration();

        Composite result = new Composite(parent, SWT.NONE);
        result.setFont(parent.getFont());
        GridLayout layout = new GridLayout(1, false);
        result.setLayout(layout);

        addTextPatternControls(result);

        Label separator = new Label(result, SWT.NONE);
        separator.setVisible(false);
        GridData data = new GridData(GridData.FILL, GridData.FILL, false, false, 2, 1);
        data.heightHint = convertHeightInCharsToPixels(1) / 3;
        separator.setLayoutData(data);

        setControl(result);
        Dialog.applyDialogFont(result);
//        PlatformUI.getWorkbench().getHelpSystem().setHelp(result, ISearchHelpContextIds.TEXT_SEARCH_PAGE);
    }
    
    private void addTextPatternControls(Composite group) {
        // grid layout with 2 columns

        // Info text        
        Label label = new Label(group, SWT.LEAD);
        label.setText(SearchMessages.SearchPage_containingText_text);
        label.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 2, 1));
        label.setFont(group.getFont());

        // Pattern combo
        fPattern = new Combo(group, SWT.SINGLE | SWT.BORDER);
        // Not done here to prevent page from resizing
        // fPattern.setItems(getPreviousSearchPatterns());
        fPattern.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                handleWidgetSelected();
                updateOKStatus();
            }
        });
        // add some listeners for regex syntax checking
        fPattern.addModifyListener(new ModifyListener() {
            public void modifyText(ModifyEvent e) {
                updateOKStatus();
            }
        });
        fPattern.setFont(group.getFont());
        GridData data = new GridData(GridData.FILL, GridData.FILL, true, false, 1, 1);
        data.widthHint = convertWidthInCharsToPixels(50);
        fPattern.setLayoutData(data);

//        fIgnoreCase = new Button(group, SWT.CHECK);
//        fIgnoreCase.setText(SearchMessages.SearchPage_caseSensitive);
//        fIgnoreCase.setSelection(!fIsCaseSensitive);
//        fIgnoreCase.addSelectionListener(new SelectionAdapter() {
//            public void widgetSelected(SelectionEvent e) {
//                fIsCaseSensitive = !fIgnoreCase.getSelection();
//            }
//        });
//        fIgnoreCase.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));
//        fIgnoreCase.setFont(group.getFont());

        // Text line which explains the special characters
//        fStatusLabel = new CLabel(group, SWT.LEAD);
//        fStatusLabel.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
//        fStatusLabel.setFont(group.getFont());
//        fStatusLabel.setAlignment(SWT.LEFT);
//        fStatusLabel.setText(SearchMessages.SearchPage_containingText_hint);

        // Limit To composite group
        
        Group fLimitToGroup = new Group(group, SWT.NULL);
        fLimitToGroup.setText(SearchMessages.SearchPage_scope_label);
        GridLayout layout = new GridLayout();
        layout.numColumns = 2;
        fLimitToGroup.setLayout(layout);

        buildScopeRadioButton(fLimitToGroup, SCOPE_ALL);
        buildScopeRadioButton(fLimitToGroup, SCOPE_DEFINITIONS);
        buildScopeRadioButton(fLimitToGroup, SCOPE_INSTANCES_OF_CLASS);
        buildScopeRadioButton(fLimitToGroup, SCOPE_INSTANCES_OF_TYPE);
        buildScopeRadioButton(fLimitToGroup, SCOPE_REFERENCES);
        buildScopeRadioButton(fLimitToGroup, SCOPE_REFERENCES_CONSTRUCTIONS);
    }
    
    private void buildScopeRadioButton(Composite group, final int scope){
        Button fScopeAll = new Button(group, SWT.RADIO);
        fScopeAll.setText(scope_text[scope]);
        fScopeAll.setSelection(fScope == scope);
        fScopeAll.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                fScope = scope;
                updateOKStatus();
                
                writeConfiguration();
            }
        });
        fScopeAll.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));
        fScopeAll.setFont(group.getFont());
    }
    
    private void handleWidgetSelected() {
        int selectionIndex = fPattern.getSelectionIndex();
        if (selectionIndex < 0 || selectionIndex >= fPreviousSearchPatterns.size())
            return;

        SearchPatternData patternData = fPreviousSearchPatterns.get(selectionIndex);
        if (!fPattern.getText().equals(patternData.textPattern))
            return;
//        fIgnoreCase.setSelection(patternData.ignoreCase);
        fPattern.setText(patternData.textPattern);
        if (patternData.workingSets != null)
            getContainer().setSelectedWorkingSets(patternData.workingSets);
        else
            getContainer().setSelectedScope(patternData.scope);
    }

    private boolean initializePatternControl() {
        final ISelection selection = getSelection();
        final String initialValue = getPatternControlInitializeValue(selection);
        if (initialValue == null){
            return false;
        }
        fPattern.setText(initialValue);
        return true;
    }
    
    private static String getPatternControlInitializeValue(ISelection selection) {               
        if (selection instanceof ITextSelection && !selection.isEmpty()) {
            final ITextSelection textSelection = (ITextSelection) selection;
            // The value to initialize the dialog with
            String text = null;
            IWorkbenchPart activePart = CALEclipseUIPlugin.getActivePage().getActivePart();
            // If the user has nothing selected then try to 
            // find the symbol at the current position
            // otherwise initialize with the selected text only.
            if (activePart instanceof CALEditor) {
                CALEditor textEditor = (CALEditor) activePart;
                if (CoreUtility.builderEnabledCheck(ActionMessages.OpenDeclarationAction_error_title)){
                    final IDocument document = ActionUtilities.getDocument(textEditor);

                    if (document != null) {
                        final int offset = textSelection.getOffset();
                        int firstLine;
                        try {
                            CALModelManager cmm = CALModelManager.getCALModelManager();
                            firstLine = document.getLineOfOffset(offset);
                            final int column = CoreUtility.getColumn(firstLine, offset, document);
                            ModuleName moduleName;
                            try{
                                moduleName = cmm.getModuleName(textEditor.getStorage());
                            }
                            catch(IllegalArgumentException ex){
                                // CAL file is probably in an invalid places
                                return "";
                            }

                            CoreUtility.initializeCALBuilder(null, 100, 100);

                            ModuleSourceDefinition msd = cmm.getModuleSourceDefinition(moduleName);
                            if (msd != null){
                                if (cmm.getProgramModelManager().hasModuleInProgram(moduleName)){
                                    CompilerMessageLogger messageLogger = new MessageLogger();
                                    // Figure out what symbol the user is refering to. This can be ambiguous
                                    // for cases list [(Int, Int)]
                                    {
                                        CALModelManager modelManager = CALModelManager.getCALModelManager();
                                        SourceMetrics sourceMetrics = modelManager.getSourceMetrics();

                                        SearchResult.Precise[] results = sourceMetrics.findSymbolAt(moduleName, firstLine+1, column+1, messageLogger);
                                        if (results != null && results.length == 1) {
                                            final SearchResult.Precise result = results[0];
                                            if (result.getCategory() != SourceIdentifier.Category.LOCAL_VARIABLE_DEFINITION){
                                                // make sure that the source range of the result matches
                                                // the source range of the selection. If not use the old
                                                // way.
                                                if (
                                                        // zero length
                                                        textSelection.getLength() == 0 ||
                                                        // of the range of the symbol found and the text selection match
                                                        (
                                                            textSelection.getOffset() == (CoreUtility.toOffset(result.getSourceRange().getStartSourcePosition(), document)) &&
                                                            textSelection.getLength() == (CoreUtility.toLength(result.getSourceRange(), document))
                                                        )
                                                    ){
                                                    text = result.getName().toSourceText();
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        } catch (BadLocationException e) {
                            // ignore and use the old default value
                        }
                    }
                }
            }

            if (text == null){
                text = insertEscapeChars(textSelection.getText());
            }

            return text;
        }
        
        return null;
    }
    
    private static String insertEscapeChars(String text) {
        if (text == null || text.equals("")) //$NON-NLS-1$
            return ""; //$NON-NLS-1$
        StringBuilder sbIn = new StringBuilder(text);
        BufferedReader reader = new BufferedReader(new StringReader(text));
        int lengthOfFirstLine = 0;
        try {
            lengthOfFirstLine = reader.readLine().length();
        } catch (IOException ex) {
            return ""; //$NON-NLS-1$
        }
        StringBuilder sbOut = new StringBuilder(lengthOfFirstLine + 5);
        int i = 0;
        while (i < lengthOfFirstLine) {
            char ch = sbIn.charAt(i);
            if (ch == '*' || ch == '?' || ch == '\\')
                sbOut.append("\\"); //$NON-NLS-1$
            sbOut.append(ch);
            i++;
        }
        return sbOut.toString();
    }
    
    /**
     * Sets the search page's container.
     * @param container the container to set
     */
    public void setContainer(ISearchPageContainer container) {
        fContainer = container;
    }

    private ISearchPageContainer getContainer() {
        return fContainer;
    }

    private ISelection getSelection() {
        return fContainer.getSelection();
    }

    private static SearchScope getSelectedResourcesScope(boolean isProjectScope, ISelection selection) {
        HashSet<IResource> resources = new HashSet<IResource>();
        String firstProjectName = null;
        if (selection instanceof IStructuredSelection && !selection.isEmpty()) {
            final IStructuredSelection structuredSelection = ((IStructuredSelection)selection);
            Iterator<?> iter = structuredSelection.iterator();
            while (iter.hasNext()) {
                Object curr = iter.next();

                IResource resource = null;
                if (curr instanceof IResource) {
                    resource = (IResource)curr;
                } else if (curr instanceof IAdaptable) {
                    resource = (IResource)((IAdaptable)curr).getAdapter(IResource.class);
                    if (resource == null && isProjectScope)
                        resource = (IProject)((IAdaptable)curr).getAdapter(IProject.class);
                }
                if (resource != null) {
                    if (isProjectScope) {
                        resource = resource.getProject();
                        if (firstProjectName == null) {
                            firstProjectName = resource.getName();
                        }
                    }
                    resources.add(resource);
                }
            }
        }
        if (resources.isEmpty() && isProjectScope) {
            IProject editorProject = getEditorProject();
            if (editorProject != null) {
                resources.add(editorProject);
                firstProjectName = editorProject.getName();
            }
        }

        String name;
        if (isProjectScope) {
            int elementCount = resources.size();
            if (elementCount > 1)
                name = Messages.format(SearchMessages.EnclosingProjectsScope, firstProjectName);
            else if (elementCount == 1)
                name = Messages.format(SearchMessages.EnclosingProjectScope, firstProjectName);
            else
                name = Messages.format(SearchMessages.EnclosingProjectScope, ""); //$NON-NLS-1$
        } else {
            name = SearchMessages.SelectionScope;
        }
        IResource[] arr = resources.toArray(new IResource[resources.size()]);
        return SearchScope.newSearchScope(name, arr);
    }

    private static IProject getEditorProject() {
        IWorkbenchPart activePart = CALEclipseUIPlugin.getActivePage().getActivePart();
        if (activePart instanceof IEditorPart) {
            IEditorPart editor = (IEditorPart)activePart;
            IEditorInput input = editor.getEditorInput();
            if (input instanceof IFileEditorInput) {
                return ((IFileEditorInput)input).getFile().getProject();
            } else if (input instanceof JarEntryEditorInput) {
                // in Eclipse 3.2, this is really, really hard
//                return ((JarEntryFile) ((JarEntryEditorInput) input).
//                        getStorage()).getPackageFragmentRoot().getJavaProject().getProject();
            }
        }
        return null;
    }
    //--------------- Configuration handling --------------
    
    /* (non-Javadoc)
     * @see org.eclipse.jface.dialogs.DialogPage#dispose()
     */
    @Override
    public void dispose() {
        writeConfiguration();
        super.dispose();
    }
    
    /**
     * Returns the page settings for this Text search page.
     * 
     * @return the page settings to be used
     */
    private IDialogSettings getDialogSettings() {
        IDialogSettings settings = CALEclipseUIPlugin.getDefault().getDialogSettings();
        fDialogSettings = settings.getSection(PAGE_NAME);
        if (fDialogSettings == null)
            fDialogSettings = settings.addNewSection(PAGE_NAME);
        return fDialogSettings;
    }

    /**
     * Initializes itself from the stored page settings.
     */
    private void readConfiguration() {
        IDialogSettings s = getDialogSettings();
        fIsCaseSensitive = s.getBoolean(STORE_CASE_SENSITIVE);
        fSearchDerived = s.getBoolean(STORE_SEARCH_DERIVED);
        try{
        fScope = s.getInt(STORE_SCOPE);
        }
        catch(Exception e){
            // version change. FIX THIS BEFORE CHECKIN 
        }

        try {
            int historySize = s.getInt(STORE_HISTORY_SIZE);
            for (int i = 0; i < historySize; i++) {
                IDialogSettings histSettings = s.getSection(STORE_HISTORY + i);
                if (histSettings != null) {
                    SearchPatternData data = SearchPatternData.create(histSettings);
                    if (data != null) {
                        fPreviousSearchPatterns.add(data);
                    }
                }
            }
        } catch (NumberFormatException e) {
            // ignore
        }
    }
    
    /**
     * Stores it current configuration in the dialog store.
     */
    private void writeConfiguration() {
        IDialogSettings s = getDialogSettings();
        s.put(STORE_CASE_SENSITIVE, fIsCaseSensitive);
        s.put(STORE_SEARCH_DERIVED, fSearchDerived);
        s.put(STORE_SCOPE, fScope);

        int historySize = Math.min(fPreviousSearchPatterns.size(), HISTORY_SIZE);
        s.put(STORE_HISTORY_SIZE, historySize);
        for (int i = 0; i < historySize; i++) {
            IDialogSettings histSettings = s.addNewSection(STORE_HISTORY + i);
            SearchPatternData data = fPreviousSearchPatterns.get(i);
            data.store(histSettings);
        }
    }
}

