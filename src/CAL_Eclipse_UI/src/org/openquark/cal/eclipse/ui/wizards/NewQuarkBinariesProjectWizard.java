/*******************************************************************************
 * Copyright (c) 2007 Business Objects SA and others.
 * All rights reserved. 
 * This file is made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Business Objects Software Limited
 *******************************************************************************/
/*
 * NewQuarkBinariesProjectWizard.java
 * Created: May 17, 2007
 * By: Greg McClement
 */

package org.openquark.cal.eclipse.ui.wizards;

import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.util.Hashtable;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.DirectoryDialog;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.wizards.newresource.BasicNewResourceWizard;
import org.openquark.cal.eclipse.core.CALModelManager;
import org.openquark.cal.eclipse.ui.CALUIMessages;
import org.openquark.cal.eclipse.ui.util.CoreUtility;
import org.openquark.cal.eclipse.ui.util.Messages;
import org.openquark.cal.module.Cal.Core.CAL_Prelude;
import org.openquark.util.UnsafeCast;

/**
 * A wizard for making a new quark binaries project. This will also potentially configure the compiler
 * to the correct version for compiling.
 * 
 * @author Greg McClement
 */
public class NewQuarkBinariesProjectWizard extends BasicNewResourceWizard {
    private WizardNewModuleCreationPage mainPage;

    public NewQuarkBinariesProjectWizard() {
    }

    @Override
    public void init(IWorkbench workbench, IStructuredSelection selection) {
        super.init(workbench, selection);
        setWindowTitle(CALUIMessages.CALNewQuarkBinariesProjectWizard_title);
        setNeedsProgressMonitor(true);
        
    }

    /**
     * Page for creating the new quark binaries project.
     * 
     * @author Greg McClement
     */
    public static class WizardNewModuleCreationPage extends WizardPage implements ISelectionChangedListener, ModifyListener{
        private Text projectNameText;
        private Text quarkBinariesLocationText;
        private Button browseButton;
        private Button updateCompilerSettingButton;
        final private IWorkspace workspace = ResourcesPlugin.getWorkspace();
        final private IWorkspaceRoot workspaceRoot = workspace.getRoot();
        
        private SelectionListener browseButtonSelectionListener; 

        WizardNewModuleCreationPage(String pageName){
            super(pageName);
            setTitle(CALUIMessages.CALNewQuarkBinariesProjectWizard_pageTitle);
            setDescription(CALUIMessages.CALNewQuarkBinariesProjectWizard_description); 

        }

        /**
         * @return true if the compile options need to be updated in order to compile the quark binaries.
         */
        public boolean updateCompilerOptions(){
            if (updateCompilerSettingButton == null){
                return false;
            }
            
            return updateCompilerSettingButton.getSelection();
            
        }
        
        /**
         * @return the name of the new quark binaries project
         */
        public String getProjectName(){
            return projectNameText.getText();            
        }
        
        /**
         * @return the location of the quark binaries files.
         */
        public String getQuarkBinariesLocation(){
            return quarkBinariesLocationText.getText();
        }

        /* (non-Javadoc)
         * @see org.eclipse.swt.events.ModifyListener#modifyText(org.eclipse.swt.events.ModifyEvent)
         */
        public void modifyText(ModifyEvent arg0) {
            this.getContainer().updateButtons();
        }
        
        /* (non-Javadoc)
         * @see org.eclipse.jface.wizard.WizardPage#isPageComplete()
         */
        @Override
        public boolean isPageComplete(){
            return validatePage();
        }
        
        @Override
        public void dispose(){
            browseButton.removeSelectionListener(browseButtonSelectionListener);   
        }
        
        public static boolean needsQuarkBinariesProject(){
            final CALModelManager cmm = CALModelManager.getCALModelManager();
            return cmm.getModuleSourceDefinition(CAL_Prelude.MODULE_NAME) == null;
        }
        
        /* (non-Javadoc)
         * @see org.eclipse.jface.dialogs.IDialogPage#createControl(org.eclipse.swt.widgets.Composite)
         */
        public void createControl(Composite parent) {
            Composite topLevel = new Composite(parent, SWT.NONE);
            GridLayout gridLayout = new GridLayout();
            gridLayout.numColumns = 2;
            topLevel.setLayout(gridLayout);
            topLevel.setLayoutData(new GridData(GridData.VERTICAL_ALIGN_FILL
                    | GridData.HORIZONTAL_ALIGN_FILL));
            topLevel.setFont(parent.getFont());
            
            {
                Label projectNameLabel = new Label(topLevel, 0);
                projectNameLabel.setText(CALUIMessages.CALNewModuleWizard_projectNameLabel);
                projectNameLabel.setFont(parent.getFont());
                GridData gridData = new GridData();
                gridData.horizontalSpan = 2;
                projectNameLabel.setLayoutData(gridData);
            }
            
            {
                projectNameText = new Text(topLevel, SWT.BORDER);
                projectNameText.addModifyListener(this);
                projectNameText.setFont(parent.getFont());
                GridData gridData = new GridData(GridData.HORIZONTAL_ALIGN_FILL | GridData.GRAB_HORIZONTAL);
                gridData.horizontalSpan = 2;
                projectNameText.setLayoutData(gridData);
            }
            
            {
                Label quarkBinariesLocationLabel = new Label(topLevel, 0);
                quarkBinariesLocationLabel.setText(CALUIMessages.CALNewModuleWizard_quarkBinariesLocationLabel);
                quarkBinariesLocationLabel.setFont(parent.getFont());
                GridData gridData = new GridData();
                gridData.horizontalSpan = 2;
                quarkBinariesLocationLabel.setLayoutData(gridData);
            }

            {
                quarkBinariesLocationText = new Text(topLevel, SWT.BORDER);
                quarkBinariesLocationText.addModifyListener(this);
                quarkBinariesLocationText.setFont(parent.getFont());
                quarkBinariesLocationText.setLayoutData(new GridData(GridData.HORIZONTAL_ALIGN_FILL | GridData.GRAB_HORIZONTAL));
            }
            
            {
                browseButton = new Button(topLevel, SWT.PUSH);
                browseButton.setText(CALUIMessages.CALNewModuleWizard_browseButtonLabel);
                browseButton.setLayoutData(new GridData(GridData.HORIZONTAL_ALIGN_FILL));
                browseButtonSelectionListener =
                    new SelectionListener(){

                    public void widgetDefaultSelected(SelectionEvent arg0) {
                    }

                    public void widgetSelected(SelectionEvent arg0) {
                        DirectoryDialog dialog = new DirectoryDialog(getShell());
                        dialog.setMessage(CALUIMessages.CALNewModuleWizard_selectLocationOfOpenQuarkFiles);
                        final String locationOfQuarkFiles = dialog.open();
                        if (locationOfQuarkFiles != null){
                            quarkBinariesLocationText.setText(locationOfQuarkFiles);
                        }
                    }

                };
                browseButton.addSelectionListener(browseButtonSelectionListener);
            }

            if (needToUpdateCompilerOptions()){
                updateCompilerSettingButton = new Button(topLevel, SWT.CHECK);
                updateCompilerSettingButton.setText(CALUIMessages.CALNewModuleWizard_updateCompilerOptionsButtonLabel);
                GridData gridData = new GridData(GridData.HORIZONTAL_ALIGN_FILL | GridData.GRAB_HORIZONTAL);
                gridData.horizontalSpan = 2;
                updateCompilerSettingButton.setLayoutData(gridData);
                updateCompilerSettingButton.setSelection(true);
            }
            
            // If the compiler options need to be update that show a check box for that.
            if (!needsQuarkBinariesProject()){
                Label notNeededLabel = new Label(topLevel, SWT.WRAP);
                notNeededLabel.setForeground(Display.getCurrent().getSystemColor(SWT.COLOR_RED));
                notNeededLabel.setText(CALUIMessages.CALNewModuleWizard_error_alreadyHasQuarkBinaries);
                notNeededLabel.setFont(parent.getFont());
            }

            // Initialize values here so when the validifier is called all the widget are set up
            projectNameText.setText("Quark");

            // all done, finish up
            setErrorMessage(null);
            setMessage(null);
            setControl(topLevel);
        }

        private boolean needToUpdateCompilerOptions(){
            {
                final String compilerCompliance = JavaCore.getOption(JavaCore.COMPILER_COMPLIANCE);
                if (
                        compilerCompliance.equals(JavaCore.VERSION_1_1) ||
                        compilerCompliance.equals(JavaCore.VERSION_1_2) ||
                        compilerCompliance.equals(JavaCore.VERSION_1_3) ||
                        compilerCompliance.equals(JavaCore.VERSION_1_4)
                ){
                    return true;
                }
            }

            {
                final String compilerSource = JavaCore.getOption(JavaCore.COMPILER_CODEGEN_TARGET_PLATFORM);
                if (
                        compilerSource.equals(JavaCore.VERSION_1_1) ||
                        compilerSource.equals(JavaCore.VERSION_1_2) ||
                        compilerSource.equals(JavaCore.VERSION_1_3) ||
                        compilerSource.equals(JavaCore.VERSION_1_4)
                ){
                    return true;
                }                    
            }

            {
                final String compilerSource = JavaCore.getOption(JavaCore.COMPILER_SOURCE);
                if (
                        compilerSource.equals(JavaCore.VERSION_1_1) ||
                        compilerSource.equals(JavaCore.VERSION_1_2) ||
                        compilerSource.equals(JavaCore.VERSION_1_3) ||
                        compilerSource.equals(JavaCore.VERSION_1_4)
                ){
                    return true;
                }
            }
            
            return false;
        }
        
        /**
         * @return true if all the entries on the current page are valid otherwise the error message is set and false is returned
         */
        protected boolean validatePage() {
            setErrorMessage(null);
            // Project name checks
            {
                // Name must not be blank
                final String projectNameString = projectNameText.getText();
                if (projectNameString.length() == 0){                    
                    setErrorMessage(CALUIMessages.CALNewQuarkBinariesProjectWizard_error_projectNameMustNotBeNull);
                    projectNameText.setFocus();
                    return false;
                }
                
                // Name must be unique
                IProject project = workspaceRoot.getProject(projectNameString);
                if (project.exists()){
                    final String message = Messages.format(CALUIMessages.CALNewQuarkBinariesProjectWizard_error_projectNameMustNotExist, new Object[] {projectNameString});
                    setErrorMessage(message);
                    return false;
                }
            }

            // Quark Binaries location checks
            {
                // Name must not be blank
                final String quarkBinariesLocationString = quarkBinariesLocationText.getText();
                if (quarkBinariesLocationString.length() == 0){                    
                    setErrorMessage(CALUIMessages.CALNewQuarkBinariesProjectWizard_error_quarkBinariesLocationMustNotBeNull);
                    quarkBinariesLocationText.setFocus();
                    return false;
                }
                
                // Name must name an existing directory
                File directory = new File(quarkBinariesLocationString);
                if (!directory.exists()){
                    final String message = Messages.format(CALUIMessages.CALNewQuarkBinariesProjectWizard_error_quarkBinariesLocationMustExist, new Object[] {quarkBinariesLocationString});
                    setErrorMessage(message);
                    return false;
                }
                
                // check that the directory structure is right
                {
                    final IPath location = new Path(quarkBinariesLocationString);
                    IPath path = location.append("bin").append("java").append("release").append("calPlatform.jar");
                    
                    File file = path.toFile();
                    if (!file.exists()){
                        final String message = Messages.format(CALUIMessages.CALNewQuarkBinariesProjectWizard_error_missingCALPlatformFile, new Object[] {quarkBinariesLocationString});
                        setErrorMessage(message);
                        return false;
                    }
                }
                
                {
                    IWorkspace workspace = ResourcesPlugin.getWorkspace();
                    final IPath location = new Path(quarkBinariesLocationString);
                    IWorkspaceRoot root = workspace.getRoot();
                    IPath fullPath = root.getLocation();
                    // check that the workspace is not in the quark binaries directory.
                    if (location.isPrefixOf(fullPath)){
                        setErrorMessage(CALUIMessages.CALNewQuarkBinariesProjectWizard_error_workspaceShouldNotBeUnderQuarkBinariesDirectory);
                        return false;
                    }
                    // check that the quark binaries is not in the directory workspace.
                    if (fullPath.isPrefixOf(location)){
                        setErrorMessage(CALUIMessages.CALNewQuarkBinariesProjectWizard_error_quarkBinariesShouldNotBeUnderDirectoryWorkspace);
                        return false;
                    }
                }
                
            }
            
            
            return true;
        }
        
        public void selectionChanged(SelectionChangedEvent event) {
        }
    }

    /* (non-Javadoc)
     * @see org.eclipse.jface.wizard.Wizard#addPages()
     */
    @Override
    public void addPages() {
        super.addPages();
        mainPage = new WizardNewModuleCreationPage("newFilePage1"); //$NON-NLS-1$
        addPage(mainPage);
    }
    
    @Override
    public boolean performFinish() {
        try {
            // If the users wants then update the compiler setting to Java version 1.4
            if (mainPage.updateCompilerOptions()){
                Hashtable<String, String> options = UnsafeCast.unsafeCast(JavaCore.getOptions());
                
                {
                    final String compilerCompliance = JavaCore.getOption(JavaCore.COMPILER_COMPLIANCE);
                    if (
                            compilerCompliance.equals(JavaCore.VERSION_1_1) ||
                            compilerCompliance.equals(JavaCore.VERSION_1_2) ||
                            compilerCompliance.equals(JavaCore.VERSION_1_3) ||
                            compilerCompliance.equals(JavaCore.VERSION_1_4)
                    ){
                        options.put(JavaCore.COMPILER_COMPLIANCE, JavaCore.VERSION_1_5);
                    }
                }

                {
                    final String compilerSource = JavaCore.getOption(JavaCore.COMPILER_CODEGEN_TARGET_PLATFORM);
                    if (
                            compilerSource.equals(JavaCore.VERSION_1_1) ||
                            compilerSource.equals(JavaCore.VERSION_1_2) ||
                            compilerSource.equals(JavaCore.VERSION_1_3) ||
                            compilerSource.equals(JavaCore.VERSION_1_4)
                    ){
                        options.put(JavaCore.COMPILER_CODEGEN_TARGET_PLATFORM, JavaCore.VERSION_1_5);
                    }                    
                }

                {
                    final String compilerSource = JavaCore.getOption(JavaCore.COMPILER_SOURCE);
                    if (
                            compilerSource.equals(JavaCore.VERSION_1_1) ||
                            compilerSource.equals(JavaCore.VERSION_1_2) ||
                            compilerSource.equals(JavaCore.VERSION_1_3) ||
                            compilerSource.equals(JavaCore.VERSION_1_4)
                    ){
                        options.put(JavaCore.COMPILER_SOURCE, JavaCore.VERSION_1_5);
                    }
                }

                JavaCore.setOptions(options);
            }
            
            // Create the project
            CoreUtility.createQuarkBinaryProject(CALUIMessages.CALNewQuarkBinariesProjectWizard_title, mainPage.getProjectName(), mainPage.getQuarkBinariesLocation(), getShell(), getContainer());

        } catch (InvocationTargetException e) {
            CoreUtility.showMessage(CALUIMessages.CALNewQuarkBinariesProjectWizard_title, e);
        } catch (InterruptedException e) {
            CoreUtility.showMessage(CALUIMessages.CALNewQuarkBinariesProjectWizard_title, e);
        }
        return true;
    }    
}
