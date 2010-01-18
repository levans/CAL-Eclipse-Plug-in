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
 * CALSourceViewerConfiguration.java
 * Creation date: Mar 26, 2007.
 * By: Greg McClement
 */

package org.openquark.cal.eclipse.perspectives;

import org.eclipse.search.ui.NewSearchUI;
import org.eclipse.ui.IFolderLayout;
import org.eclipse.ui.IPageLayout;
import org.eclipse.ui.IPerspectiveFactory;
import org.eclipse.ui.console.IConsoleConstants;
import org.eclipse.ui.progress.IProgressConstants;

public class CALPerspectiveFactory implements IPerspectiveFactory {

    private static final String ID_CALWORKSPACE = "org.openquark.cal.eclipse.ui.views.CALWorkspace";
    
    public void createInitialLayout(IPageLayout layout) {
        final String editorArea = layout.getEditorArea();

        {
            layout.addNewWizardShortcut("org.openquark.cal.eclipse.ui.NewModuleWizard");
            layout.addNewWizardShortcut("org.eclipse.jdt.ui.wizards.NewPackageCreationWizard"); //$NON-NLS-1$
            layout.addNewWizardShortcut("org.eclipse.jdt.ui.wizards.NewClassCreationWizard"); //$NON-NLS-1$
            layout.addNewWizardShortcut("org.eclipse.jdt.ui.wizards.NewInterfaceCreationWizard"); //$NON-NLS-1$
            layout.addNewWizardShortcut("org.eclipse.jdt.ui.wizards.NewEnumCreationWizard"); //$NON-NLS-1$
            layout.addNewWizardShortcut("org.eclipse.jdt.ui.wizards.NewAnnotationCreationWizard"); //$NON-NLS-1$
            layout.addNewWizardShortcut("org.eclipse.jdt.ui.wizards.NewSourceFolderCreationWizard");     //$NON-NLS-1$
            layout.addNewWizardShortcut("org.eclipse.jdt.ui.wizards.NewSnippetFileCreationWizard"); //$NON-NLS-1$
            layout.addNewWizardShortcut("org.eclipse.ui.wizards.new.folder");//$NON-NLS-1$
            layout.addNewWizardShortcut("org.eclipse.ui.wizards.new.file");//$NON-NLS-1$
            layout.addNewWizardShortcut("org.eclipse.ui.editors.wizards.UntitledTextFileWizard");//$NON-NLS-1$
        }
        
        layout.addPerspectiveShortcut("org.openquark.cal.eclipse.ui.CALPerspective");
        layout.addPerspectiveShortcut("org.eclipse.debug.ui.DebugPerspective");
        layout.addPerspectiveShortcut("org.eclipse.jdt.ui.JavaPerspective");
        layout.addPerspectiveShortcut("org.eclipse.jdt.ui.JavaBrowsingPerspective");
                
        layout.addShowViewShortcut("org.openquark.cal.eclipse.ui.views.CALWorkspace");
        
        {
            // views - search
            layout.addShowViewShortcut(NewSearchUI.SEARCH_VIEW_ID);
            
//            // views - debugging
//            layout.addShowViewShortcut(IConsoleConstants.ID_CONSOLE_VIEW);

            // views - standard workbench
            layout.addShowViewShortcut(IPageLayout.ID_OUTLINE);
            layout.addShowViewShortcut(IPageLayout.ID_PROBLEM_VIEW);
            layout.addShowViewShortcut(IPageLayout.ID_RES_NAV);
            layout.addShowViewShortcut(IPageLayout.ID_TASK_LIST);
            layout.addShowViewShortcut(IProgressConstants.PROGRESS_VIEW_ID);
            layout.addShowViewShortcut(IConsoleConstants.ID_CONSOLE_VIEW);
        }
        
        layout.addView(ID_CALWORKSPACE, IPageLayout.LEFT, 0.20f, editorArea);
        
        final IFolderLayout bottom = layout.createFolder("bottom", IPageLayout.BOTTOM, 0.66f, editorArea);
        bottom.addView(IPageLayout.ID_PROBLEM_VIEW);
        bottom.addView(IPageLayout.ID_TASK_LIST);

        layout.addView(IPageLayout.ID_OUTLINE, IPageLayout.RIGHT, 0.75f, editorArea);
    }

}
