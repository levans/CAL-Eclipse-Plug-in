/*******************************************************************************
 * Copyright (c) 2006 Business Objects Software Limited and others.
 * All rights reserved. 
 * This file is made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Business Objects Software Limited - initial API and implementation based on Eclipse 3.1.2 code for
 *                             /org.eclipse.search/search/org/eclipse/search/internal/core/SearchScope.java
 *                           Eclipse source is available at: http://www.eclipse.org/downloads/
 *******************************************************************************/

/*
 * SearchScope.java
 * Creation date: Oct 3, 2006.
 * By: Edward Lam
 */
package org.openquark.cal.eclipse.ui.search;

import java.util.ArrayList;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.Assert;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.IPath;
import org.eclipse.ui.IWorkingSet;

/**
 * @author Edward Lam
 */
public class SearchScope {

    /**
     * Returns a workspace scope.
     * @return a workspace scope.
     */
    public static SearchScope newWorkspaceScope() {
        return new SearchScope(SearchMessages.WorkspaceScope, new IResource[] { ResourcesPlugin.getWorkspace().getRoot() }); 
    }

    /**
     * Returns a scope for the given resources.
     * @param description description of the scope
     * @param resources the resources to be contained
     * @return a scope for the given resources.
     */
    public static SearchScope newSearchScope(String description, IResource[] resources) {
        return new SearchScope(description, removeRedundantEntries(resources));
    }

    /**
     * Returns a scope for the given working sets
     * @param description description of the scope
     * @param workingSets the working sets to be contained
     * @return a scope for the given working sets
     */
    public static SearchScope newSearchScope(String description, IWorkingSet[] workingSets) {
        return new SearchScope(description, convertToResources(workingSets));
    }

    private String fDescription;
    private final IResource[] fRootElements;

    private SearchScope(String description, IResource[] resources) {
        Assert.isNotNull(description);
        fDescription = description;
        fRootElements = resources;
    }

    /**
     * Returns the description of the scope
     * @return the description of the scope
     */
    public String getDescription() {
        return fDescription;
    }

    /**
     * Returns the root elements of this scope
     * @return the root elements of this scope
     */
    public IResource[] getRootElements() {
        return fRootElements;
    }

    /**
     * Adds an file name pattern  to the scope.
     * @param pattern
     */
    public void addFileNamePattern(String pattern) {
    }

    private static IResource[] removeRedundantEntries(IResource[] elements) {
        ArrayList<IResource> res = new ArrayList<IResource>();
        for (int i = 0; i < elements.length; i++) {
            IResource curr = elements[i];
            addToList(res, curr);
        }
        return res.toArray(new IResource[res.size()]);
    }

    private static IResource[] convertToResources(IWorkingSet[] workingSets) {
        ArrayList<IResource> res = new ArrayList<IResource>();
        for (int i = 0; i < workingSets.length; i++) {
            IAdaptable[] elements = workingSets[i].getElements();
            for (int k = 0; k < elements.length; k++) {
                IResource curr = (IResource)elements[k].getAdapter(IResource.class);
                if (curr != null) {
                    addToList(res, curr);
                }
            }
        }
        return res.toArray(new IResource[res.size()]);
    }

    private static void addToList(ArrayList<IResource> res, IResource curr) {
        IPath currPath = curr.getFullPath();
        for (int k = res.size() - 1; k >= 0; k--) {
            IResource other = res.get(k);
            IPath otherPath = other.getFullPath();
            if (otherPath.isPrefixOf(currPath)) {
                return;
            }
            if (currPath.isPrefixOf(otherPath)) {
                res.remove(k);
            }
        }
        res.add(curr);
    }
}
