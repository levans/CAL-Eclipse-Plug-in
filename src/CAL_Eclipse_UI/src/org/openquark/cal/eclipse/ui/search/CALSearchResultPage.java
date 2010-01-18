/*******************************************************************************
 * Copyright (c) 2006 Business Objects Software Limited and others.
 * All rights reserved. 
 * This file is made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Business Objects Software Limited - initial API and implementation
 *******************************************************************************/

/*
 * CALSearchResultPage.java
 * Creation date: Dec 11, 2006.
 * By: Greg McClement
 */
package org.openquark.cal.eclipse.ui.search;

import java.text.MessageFormat;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IStorage;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jdt.internal.core.JarEntryFile;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.viewers.ILabelProviderListener;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerSorter;
import org.eclipse.search.ui.text.AbstractTextSearchResult;
import org.eclipse.search.ui.text.AbstractTextSearchViewPage;
import org.eclipse.search.ui.text.Match;
import org.eclipse.swt.graphics.Image;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.model.WorkbenchLabelProvider;
import org.eclipse.ui.texteditor.ITextEditor;
import org.openquark.cal.compiler.SourceRange;
import org.openquark.cal.eclipse.ui.CALEclipseUIPlugin;
import org.openquark.cal.eclipse.ui.search.CALSearchPage.CALMatch;
import org.openquark.cal.eclipse.ui.search.CALSearchPage.CALSearchResults;
import org.openquark.cal.eclipse.ui.util.CoreUtility;


/**
 * @author GMcClement
 *
 * Implements a search result page for the Eclipse search view.
 * 
 */
public class CALSearchResultPage extends AbstractTextSearchViewPage {
    /**
     * The content provider for the tree viewer that appears in the search results pane.
     */
    private CALSearchTreeContentProvider contentProvider;

    protected void elementsChanged(Object[] objects) {
        if (contentProvider != null) {
            contentProvider.elementsChanged(objects);
        }
    }

    /* 
     * Shows the given match in the editor. This could convert a CALMatch object that
     * has line/column notation into offset/length notation.
     */
    protected void showMatch(Match match, int currentOffset, int currentLength, boolean activate) throws PartInitException {
        IEditorPart editor = null;
        editor = CoreUtility.openInEditor((IStorage) match.getElement(), false);

        if (editor != null && activate) {
            editor.getEditorSite().getPage().activate(editor);
        }

        if (editor instanceof ITextEditor) {
            ITextEditor textEditor = (ITextEditor) editor;
            if (match instanceof CALMatch) {
                CALMatch calMatch = (CALMatch) match;
                if (calMatch.isCharacterPosition()) {
                    textEditor.selectAndReveal(calMatch.getStart(), calMatch.getLength());
                } else {
                    SourceRange range = calMatch.getRange();
                    
                    IDocument doc = textEditor.getDocumentProvider().getDocument(textEditor.getEditorInput());
                    try {
                        int start = CoreUtility.convertToCharacterPosition(range.getStartLine(), range.getStartColumn(), doc);
                        int end = CoreUtility.convertToCharacterPosition(range.getEndLine(), range.getEndColumn(), doc);
                        int length = end - start;

                        calMatch.convertToCharacterPosition(start, length);
                        textEditor.selectAndReveal(start, length);
                    } catch (BadLocationException e) {
                        // will only happen on concurrent modification
                        CALEclipseUIPlugin.log(new Status(IStatus.ERROR, CALEclipseUIPlugin.PLUGIN_ID, IStatus.OK, "", e)); //$NON-NLS-1$
                    }
                }

            } else {
                textEditor.selectAndReveal(currentOffset, currentLength);
            }
        }
    }

    protected void clear() {
        if (contentProvider != null)
            contentProvider.clear();
    }

    protected void configureTreeViewer(TreeViewer viewer) {
        viewer.setLabelProvider(new CALSearchLabelProvider(this));
        contentProvider = new CALSearchTreeContentProvider(viewer);
        viewer.setContentProvider(contentProvider);
        viewer.setSorter(new ViewerSorter());
    }
    
    /**
     * @author GMcClement
     *
     * Provides labels for the matches that appear in the search results pane.
     */
    public static class CALSearchLabelProvider extends LabelProvider {
        /**
         * The page that the tree control is on. This is saved so that the
         * input can be accessed in order to count the number matches. 
         */
        private CALSearchResultPage page;

        private static Image image_cal_file = CALEclipseUIPlugin.getImageDescriptor("/icons/calfile.png").createImage();
        
        /**
         * Helper class to get icons for standard resources.
         */
        private WorkbenchLabelProvider imageProvider;

        CALSearchLabelProvider(CALSearchResultPage page) {
            if (page == null) {
                throw new IllegalArgumentException();
            }
            // save page because page.getInput() will return null until the page
            // is configured.
            this.page = page;
            this.imageProvider = new WorkbenchLabelProvider();
        }

        public void removeListener(ILabelProviderListener listener) {
            super.removeListener(listener);
            imageProvider.removeListener(listener);
        }

        public void addListener(ILabelProviderListener listener) {
            super.addListener(listener);
            imageProvider.addListener(listener);
        }

        public void dispose() {
            super.dispose();
            imageProvider.dispose();
        }

        public String getText(Object element) {
            if (element instanceof IResource) {
                IResource resource = (IResource) element;
                String text = imageProvider.getText(resource);
                // return text;
    
                AbstractTextSearchResult searchResult = page.getInput();
                int matchCount = 0;
                matchCount = searchResult.getMatchCount(element);
                if (matchCount <= 1)
                    return text;
                String format = SearchMessages.CALSearchLabelProvider_count_format;
                return MessageFormat.format(format, new Object[] { text, Integer.valueOf(matchCount) });
            } else if (element instanceof JarEntryFile) {
                // for jar entries
                return ((JarEntryFile) element).getName(); 
            } else {
                return null;
            }
            
        }

        public Image getImage(Object element) {
            if (element instanceof IResource) {
                return imageProvider.getImage(element);
            } else if (element instanceof JarEntryFile) {
                return image_cal_file;
            } else {
                return null;
            }

        }

    }

    private final static Object[] EMPTY_ARRAY = new Object[0];

    /**
     * @author GMcClement
     *
     * Provides the result hierarchy for the tree viewer. Basically the results are show 
     * using the package/file folder hierarchy that the underlying files are stored in.
     */
    public static class CALSearchTreeContentProvider implements ITreeContentProvider {

        /**
         * Contains the matches that resulted from the search.
         */
        private CALSearchResults searchResults;

        private TreeViewer viewer;

        /**
         * Maps each object to a set of children of that object.
         */
        private Map<Object, Set<Object>> objectToChildrenMap;

        CALSearchTreeContentProvider(TreeViewer viewer) {
            this.viewer = viewer;
        }

        void clear(){
            // set up the map again
            initialize(searchResults);
            // update the UI
            viewer.refresh();
        }
        
        public Object[] getChildren(Object parentElement) {
            Set<Object> children = objectToChildrenMap.get(parentElement);
            if (children == null) {
                return EMPTY_ARRAY;
            } else {
                return children.toArray();
            }
        }

        public Object getParent(Object element) {
            if (element instanceof IProject) {
                return null; // root
            } else if (element instanceof IResource) {
                return ((IResource) element).getParent();
            } else {
                return null;
            }
        }

        public boolean hasChildren(Object element) {
            Set<Object> children = objectToChildrenMap.get(element);
            if (children == null) {
                return false;
            } else {
                return children.size() > 0;
            }
        }

        public Object[] getElements(Object inputElement) {
            return getChildren(inputElement);
        }

        public void dispose() {
        }

        private void initialize(CALSearchResults searchResults) {
            if (searchResults == null){
                searchResults = null;
            }
            else{
                this.searchResults = searchResults;
                objectToChildrenMap = new HashMap<Object, Set<Object>>();
                if (searchResults != null) {
                    Object[] elements = searchResults.getElements();
                    for (int i = 0; i < elements.length; ++i) {
                        insert(elements[i], false);
                    }
                }
            }
        }

        public synchronized void elementsChanged(Object[] updatedElements) {
            for (int i = 0; i < updatedElements.length; i++) {
                if (searchResults.getMatchCount(updatedElements[i]) > 0)
                    insert(updatedElements[i], true);
                else
                    remove(updatedElements[i], true);
            }
        }
                
        protected void remove(Object element, boolean refreshViewer) {            
            
            Object parent = getParent(element);
            if (parent == null){
                if (refreshViewer){
                    viewer.remove(element); // at the root
                }
            }
            else{
                Set<Object> siblings = objectToChildrenMap.get(parent);
                if (siblings != null) {
                    siblings.remove(element);
                    if (siblings.size() == 0){
                        remove(parent, refreshViewer);
                        return;
                    }
                }
                
                if (refreshViewer){
                    viewer.refresh(parent);
                }
            }
        }

        protected void insert(Object child, boolean refreshViewer) {
            Object parent = getParent(child);
            while (parent != null){
                if (insert(parent, child)){
                    if (refreshViewer){
                        viewer.add(parent, child);
                    }
                } 
                else{
                    if (refreshViewer){
                        viewer.refresh(parent);
                    }
                    return;
                }
                child = parent;
                parent = getParent(child);
            }
            if (insert(searchResults, child)){
                if (refreshViewer){
                    viewer.add(searchResults, child);
                }
            }
        }
        
        /**
         * Add the child to the set of children the given parent.
         *  
         * @param parent
         * @param child
         * @return <tt>true</tt> if the set of children did not already contain the specified element.
         */
        
        private boolean insert(Object parent, Object child) {
            Set<Object> children = objectToChildrenMap.get(parent);
            if (children == null){
                children = new HashSet<Object>();
                objectToChildrenMap.put(parent, children);
            }
            return children.add(child);
        }

        public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
            if (newInput instanceof CALSearchResults) {
                initialize((CALSearchResults) newInput);
            }
        }
    }

    public Object[] getElements(Object inputElement) {
        if (inputElement instanceof AbstractTextSearchResult) {
            return ((AbstractTextSearchResult) inputElement).getElements();
        } else {
            return null;
        }
    }

    public void dispose() {
        super.dispose();
        contentProvider.dispose();
    }

    protected void configureTableViewer(TableViewer viewer) {
    }

}
