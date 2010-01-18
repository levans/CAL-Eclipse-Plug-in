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
 * NewModuleWizard.java
 * Created: Apr 25, 2007
 * By: Greg McClement
 */

package org.openquark.cal.eclipse.ui.wizards;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.ArrayList;
import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IStorage;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.jdt.core.IClasspathEntry;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IPackageFragment;
import org.eclipse.jdt.core.IPackageFragmentRoot;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TreePath;
import org.eclipse.jface.viewers.TreeSelection;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.ISharedImages;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.ide.IDE;
import org.eclipse.ui.ide.IDE.SharedImages;
import org.eclipse.ui.wizards.newresource.BasicNewResourceWizard;
import org.openquark.cal.compiler.LanguageInfo;
import org.openquark.cal.compiler.ModuleName;
import org.openquark.cal.eclipse.core.CALModelManager;
import org.openquark.cal.eclipse.ui.CALEclipseUIPlugin;
import org.openquark.cal.eclipse.ui.CALUIMessages;
import org.openquark.cal.eclipse.ui.util.CoreUtility;
import org.openquark.cal.eclipse.ui.util.Messages;
import org.openquark.cal.services.CALSourcePathMapper;

/**
 * This dialog is for creating a CAL Module in the package explorer. This is accessed from 
 * the File/New menu.
 * 
 * @author Greg McClement
 */
public class NewModuleWizard extends BasicNewResourceWizard {
    // only page of the new module wizard
    private WizardNewModuleCreationPage mainPage;
    
    final private IWorkspaceRoot workspaceRoot = ResourcesPlugin.getWorkspace().getRoot();
    
    private static Image image_nav_namespace = null;

    /* (non-Javadoc)
     * @see org.eclipse.ui.wizards.newresource.BasicNewResourceWizard#init(org.eclipse.ui.IWorkbench, org.eclipse.jface.viewers.IStructuredSelection)
     */
    @Override
    public void init(IWorkbench workbench, IStructuredSelection selection) {
        super.init(workbench, selection);
        setWindowTitle(CALUIMessages.CALNewModuleWizard_title);
        setNeedsProgressMonitor(true);        
    }

    private boolean isJavaProject(IProject project) throws CoreException{
        return project.hasNature(JavaCore.NATURE_ID);
    }    

    private boolean isJavaSourceFolder(final IFolder folder){                
        final IJavaProject jProject = JavaCore.create(folder.getProject());
        
        IClasspathEntry[] classPathEntries;
        try {
            classPathEntries = jProject.getRawClasspath();
        } catch (JavaModelException e) {
            // not in class path
            return false;
        }
        boolean alreadyExists = false;
        for(int iClassPathEntries = 0; iClassPathEntries < classPathEntries.length; ++iClassPathEntries){
            final IClasspathEntry cpe = classPathEntries[iClassPathEntries];
            if (cpe.getPath().equals(folder.getFullPath())){
                alreadyExists = true;
                break;
            }
        }
        return alreadyExists;
    }

    private boolean isJavaSourceFolder(final IPackageFragmentRoot folder){                
        final IJavaProject jProject = folder.getJavaProject();

        IClasspathEntry[] classPathEntries;
        try {
            classPathEntries = jProject.getRawClasspath();
        } catch (JavaModelException e) {
            // not in class path
            return false;
        }
        boolean alreadyExists = false;
        for(int iClassPathEntries = 0; iClassPathEntries < classPathEntries.length; ++iClassPathEntries){
            final IClasspathEntry cpe = classPathEntries[iClassPathEntries];
            if (cpe.getPath().equals(folder.getPath())){
                alreadyExists = true;
                break;
            }
        }
        return alreadyExists;
    }
    
    private class WizardNewModuleCreationPage extends WizardPage implements ISelectionChangedListener, ModifyListener{
        private TreeViewer treeViewer;
        private Text moduleNameText;
        private Text containerNameText;

        WizardNewModuleCreationPage(String pageName, IStructuredSelection selection){
            super(pageName);
        }

        private IProject getProject(){
            String[] components = containerNameText.getText().replace('.', '/').split("/");
            return workspaceRoot.getProject(components[0]);
        }

        /**
         * @return the name of the module specified in the UI. This assumes that validatePage succeeded.
         */
        private String getModuleNameString(){
            final String moduleNameString;
            final String containerNameString = containerNameText.getText();
            final int indexOfFirstDot = containerNameString.indexOf("/");
            final int indexOfSecondDot = containerNameString.indexOf(".", indexOfFirstDot + 1);
            if (indexOfSecondDot != -1){
                moduleNameString = containerNameString.substring(indexOfSecondDot + 1) + "." + moduleNameText.getText();
            }
            else{
                moduleNameString = moduleNameText.getText();
            }
            return moduleNameString;
        }
        /**
         * Create a new IFile object corresponding the the module information described in the wizard
         */
        private IFile createNewFile() throws CoreException{
            final String containerNameString = containerNameText.getText();
            final IProject project = getProject();
            String moduleNameString;
            final int indexOfFirstSlash = containerNameString.indexOf("/");
            if (indexOfFirstSlash == -1){
                final int indexOfSecondDot = containerNameString.indexOf(".", indexOfFirstSlash + 1);
                if (indexOfSecondDot != -1){
                    moduleNameString = containerNameString.substring(indexOfSecondDot + 1) + "." + moduleNameText.getText();
                }
                else{
                    moduleNameString = moduleNameText.getText();
                }
                final ModuleName moduleName = ModuleName.make(moduleNameString);
                final String[] moduleNameComponents = moduleName.getComponents();
                final String[] components = new String[moduleNameComponents.length + 1];
                components[0] = "CAL";
                System.arraycopy(moduleNameComponents, 0, components, 1, moduleNameComponents.length);
                final int justBeforeLastComponent = components.length - 1;
                
                // Make sure that the component paths exists as folders
                IFolder lastComponent = null;
                for(int i = 0; i < justBeforeLastComponent; ++i){
                    final String componentName = components[i];
                    if (lastComponent == null){
                        lastComponent = project.getFolder(components[0]);
                    }
                    else{
                        lastComponent = lastComponent.getFolder(componentName);
                    }
                    if (!lastComponent.exists()){
                        lastComponent.create(false, true, null);
                    }
                }
                final IFile file = lastComponent.getFile(moduleName.getLastComponent() + ".cal");
                file.create(getInitialContents(moduleName), false, null);
                return file;
            }
            else{
                final int indexOfSecondDot = containerNameString.indexOf(".", indexOfFirstSlash + 1);
                String sourceFolderName;
                if (indexOfSecondDot != -1){
                    sourceFolderName = containerNameString.substring(indexOfFirstSlash + 1, indexOfSecondDot);
                    moduleNameString = containerNameString.substring(indexOfSecondDot + 1) + "." + moduleNameText.getText();
                }
                else{
                    sourceFolderName = containerNameString.substring(indexOfFirstSlash + 1);
                    moduleNameString = moduleNameText.getText();
                }
                
                final IFolder CALFolder = initializeCALContainer(project.getFolder(sourceFolderName));
                final ModuleName moduleName = ModuleName.make(moduleNameString);
                final String[] moduleNameComponents = moduleName.getComponents();
                final int justBeforeLastComponent = moduleNameComponents.length - 1;
                
                // Make sure that the component paths exists as folders
                IFolder lastComponent = CALFolder;
                for(int i = 0; i < justBeforeLastComponent; ++i){
                    final String componentName = moduleNameComponents[i];
                    lastComponent = lastComponent.getFolder(componentName);
                    if (!lastComponent.exists()){
                        lastComponent.create(false, true, null);
                    }
                }
                
                final IFile file = lastComponent.getFile(moduleName.getLastComponent() + ".cal");
                file.create(getInitialContents(moduleName), false, null);
                
                return file;                
            }            
        }

        /**
         * Get the initial contents of the new module
         */
        private InputStream getInitialContents(ModuleName moduleName) {
            String initialContents = 
                "module " + moduleName.toSourceText() + ";\n" +
                "\n" +
                "import Cal.Core.Prelude;\n" +
                "\n";
            return new ByteArrayInputStream(initialContents.getBytes());
        }            

        /**
         * Create a CAL folder if none exists
         */
        private IFolder initializeCALContainer(IFolder srcFolder) throws CoreException{
          IFolder CALFolder = srcFolder.getFolder(CALSourcePathMapper.SCRIPTS_BASE_FOLDER);
          if (!CALFolder.exists()){
              CALFolder.create(false, true, null);
          }
          return CALFolder;
        }
        
        /**
         * The content provider for the list of container that the module can be put in.
         */
        private class TreeContentProvider implements IStructuredContentProvider, ITreeContentProvider{
            IWorkspaceRoot workspace;
            
            public Object[] getElements(Object inputElement) {
                IProject[] projects = workspace.getProjects();
                ArrayList<IJavaProject> javaProjects = new ArrayList<IJavaProject>();
                for(int i = 0; i < projects.length; ++i){
                    final IProject project = projects[i];
                    try {
                        if (isJavaProject(project)) {
//                          javaProjects.add(project);
                            javaProjects.add(JavaCore.create(project));
                        }
                    } catch (CoreException e) {
                        // no doesn't have the nature
                    }
                }
                return javaProjects.toArray();
            }

            public void dispose() {
            }

            /* (non-Javadoc)
             * @see org.eclipse.jface.viewers.IContentProvider#inputChanged(org.eclipse.jface.viewers.Viewer, java.lang.Object, java.lang.Object)
             */
            public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
                workspace = (IWorkspaceRoot) newInput;
            }

            /* (non-Javadoc)
             * @see org.eclipse.jface.viewers.ITreeContentProvider#getChildren(java.lang.Object)
             */
            public Object[] getChildren(Object parentElement) {
                if (parentElement instanceof IJavaProject){
                    final IJavaProject project = (IJavaProject) parentElement;
                    try {
                        final IJavaElement[] javaElements = project.getChildren();
                        final ArrayList<IPackageFragmentRoot> sourceFolders = new ArrayList<IPackageFragmentRoot>();
                        for(int i = 0; i < javaElements.length; ++i){
                            final IJavaElement javaElement = javaElements[i];
                            if (javaElement instanceof IPackageFragmentRoot){
                                final IPackageFragmentRoot folder = (IPackageFragmentRoot) javaElement;
                                if (folder.getJavaProject() == folder.getParent() && !folder.isArchive() && folder.getElementName().length() != 0){
                                    if (isJavaSourceFolder(folder)){
                                        sourceFolders.add(folder);
                                    }
                                }
                            }
                        }
                        return sourceFolders.toArray();
                    } catch (CoreException e) {
                        return new Object[0];
                    }
                }
                else if (parentElement instanceof IProject){
                    final IProject project = (IProject) parentElement;
                    try {
                        IResource[] allMembers = project.members();
                        ArrayList<IResource> folders = new ArrayList<IResource>();
                        for(int i = 0; i < allMembers.length; ++i){
                            IResource member = allMembers[i];
                            if (member instanceof IFolder){
                                IFolder memberFolder = (IFolder) member;
                                if (isJavaSourceFolder(memberFolder)){
                                    folders.add(member);
                                }
                            }
                        }
                        return folders.toArray();
                    } catch (CoreException e) {
                        return new Object[0];
                    }
                }
                else if (parentElement instanceof IFolder){
                    final IFolder folder = (IFolder) parentElement;
                    IFolder CALFolder = folder;
                    if (folder.getParent() instanceof IProject){
                        CALFolder = folder.getFolder("CAL");
                        if (CALFolder == null || !CALFolder.exists()){
                            return new Object[0];
                        }
                    }

                    try {
                        IResource[] allMembers = CALFolder.members();
                        ArrayList<IFolder> folders = new ArrayList<IFolder>();
                        for(int i = 0; i < allMembers.length; ++i){
                            IResource member = allMembers[i];
                            if (member instanceof IFolder){
                                IFolder memberFolder = (IFolder) member;
                                // Only add paths that are valid hierachical component names.
                                if (LanguageInfo.isValidModuleNameComponent(memberFolder.getName())){
                                    folders.add(memberFolder);
                                }
                            }
                        }
                        return folders.toArray();
                    } catch (CoreException e) {
                        return new Object[0];
                    }
                }
                else if (parentElement instanceof IPackageFragmentRoot){
                    final IPackageFragmentRoot pfr = (IPackageFragmentRoot) parentElement;

                    final IFolder folder = (IFolder) pfr.getResource();

                    IFolder CALFolder = folder;
                    if (pfr.getParent() instanceof IJavaProject){
                        CALFolder = folder.getFolder("CAL");
                        if (CALFolder == null || !CALFolder.exists()){
                            return new Object[0];
                        }
                    }

                    try {
                        IResource[] allMembers = CALFolder.members();
                        ArrayList<IFolder> folders = new ArrayList<IFolder>();
                        for(int i = 0; i < allMembers.length; ++i){
                            IResource member = allMembers[i];
                            if (member instanceof IFolder){
                                IFolder memberFolder = (IFolder) member;
                                // Only add paths that are valid hierachical component names.
                                if (LanguageInfo.isValidModuleNameComponent(memberFolder.getName())){
                                    folders.add(memberFolder);
                                }
                            }
                        }
                        return folders.toArray();
                    } catch (CoreException e) {
                        return new Object[0];
                    }
                }                    

                return new Object[0];
            }
            
            /* (non-Javadoc)
             * @see org.eclipse.jface.viewers.ITreeContentProvider#hasChildren(java.lang.Object)
             */
            public boolean hasChildren(Object element) {
                return getChildren(element).length > 0;
            }
            
            /* (non-Javadoc)
             * @see org.eclipse.jface.viewers.ITreeContentProvider#getParent(java.lang.Object)
             */
            public Object getParent(Object element) {
                if (element instanceof IJavaProject || element instanceof IProject){
                    return workspace; 
                }
                else if (element instanceof IPackageFragmentRoot){
                    return ((IPackageFragmentRoot) element).getJavaProject();
                }
                else if (element instanceof IFolder){
                    final IFolder parentFolder = (IFolder) ((IFolder) element).getParent();
                    if (parentFolder.getName().equals("CAL")){
                        return JavaCore.create(parentFolder.getParent());
                    }
                    else{
                        return parentFolder;
                    }
                }
                else if (element instanceof IPackageFragment){
                    return ((IPackageFragment) element).getParent();
                }
                else{
                    return null;
                }
            }
        }

        /**
         * The label provider for the list of containers that the module can be put in.
         */
        public class TreeLabelProvider extends LabelProvider{
            /* (non-Javadoc)
             * @see org.eclipse.jface.viewers.LabelProvider#getText(java.lang.Object)
             */
            @Override
            public String getText(Object element) {
                if (element instanceof IJavaProject){
                    return ((IJavaProject) element).getElementName();
                }
                else if (element instanceof IProject){
                    return ((IProject) element).getName();
                }
                else if (element instanceof IFolder){
                    return ((IFolder) element).getName();
                }
                else if (element instanceof IPackageFragmentRoot){
                    IPackageFragmentRoot pfr = (IPackageFragmentRoot) element;
                    return pfr.getPath().removeFirstSegments(1).toString();
                }
                return element.toString();
            }
            
            /* (non-Javadoc)
             * @see org.eclipse.jface.viewers.LabelProvider#getImage(java.lang.Object)
             */
            @Override
            public Image getImage(Object element) {
                if (element instanceof IProject){
                    String imageKey = SharedImages.IMG_OBJ_PROJECT;
                    return PlatformUI.getWorkbench().getSharedImages().getImage(imageKey);
                }
                else if (element instanceof IJavaProject){
                    String imageKey = SharedImages.IMG_OBJ_PROJECT;
                    return PlatformUI.getWorkbench().getSharedImages().getImage(imageKey);
                }
                else if (element instanceof IFolder){
                    IFolder folder = (IFolder) element;
                    if (folder.getParent() instanceof IProject){ 
                        String imageKey = ISharedImages.IMG_OBJ_FOLDER;
                        return PlatformUI.getWorkbench().getSharedImages().getImage(imageKey);
                    }
                    else{
                        if (image_nav_namespace == null){
                            image_nav_namespace = CALEclipseUIPlugin.getImageDescriptor("/icons/nav_namespace.png").createImage();
                        }
                        return image_nav_namespace;
                    }
                }
                else if (element instanceof IPackageFragmentRoot){
//                    IPackageFragmentRoot folder = (IPackageFragmentRoot) element;
//                    if (folder.getParent() instanceof IProject){ 
                        String imageKey = ISharedImages.IMG_OBJ_FOLDER;
                        return PlatformUI.getWorkbench().getSharedImages().getImage(imageKey);
//                    }
//                    else{
//                        if (image_nav_namespace == null){
//                            image_nav_namespace = CALEclipseUIPlugin.getImageDescriptor("/icons/nav_namespace.png").createImage();
//                        }
//                        return image_nav_namespace;
//                    }
                }
                
                assert false;
                return null;
            }
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
        
        private String getName(Object treeElement){
            if (treeElement instanceof IJavaProject){
                IJavaProject project = (IJavaProject) treeElement;
                return project.getProject().getName();
            }
            else if (treeElement instanceof IProject){
                return ((IProject) treeElement).getName();
            }
            else if (treeElement instanceof IPackageFragmentRoot){
                IPackageFragmentRoot pfr = (IPackageFragmentRoot) treeElement;
                final String projectName = pfr.getJavaProject().getProject().getName();                                
                final String folderName = pfr.getPath().removeFirstSegments(1).toString();
                return projectName + "/" + folderName;
                
            }
            else if (treeElement instanceof IPackageFragment){
                final IPackageFragment packageFragment = (IPackageFragment) treeElement;
                final String name = packageFragment.getElementName();
                final IPackageFragmentRoot folder = (IPackageFragmentRoot) packageFragment.getParent();
                if (folder.getJavaProject() == folder.getParent() && !folder.isArchive()){
                    final String projectName = folder.getJavaProject().getProject().getName();
                    final String folderName;
                    if (folder.getResource() == folder.getJavaProject().getProject()){
                        folderName = "";
                    }
                    else{
                        folderName = "/" + folder.getElementName();
                    }
                    final int startOfSlash = name.indexOf("/");
                    String componentNames = "";
                    if (startOfSlash != -1){
                        componentNames = "." + name.substring(startOfSlash + 1);    
                    }
                    return projectName + folderName + componentNames;
                }
                else{
                    return "";
                }
            }
            else if (treeElement instanceof IContainer){
                StringBuilder sb = new StringBuilder();
                IContainer container = (IContainer) treeElement;
                // Separator for package names is '/'
                String separator = ".";
                while(true){
                    final Object next;
                    if (container.getName().equals("CAL")){
                        // Separator for java packages is '/'
                        separator = "/";
                        next = container.getParent();
                    }
                    else{
                        sb.insert(0, container.getName());
                        next = container.getParent();
                        if (next instanceof IProject){
                            sb.insert(0, "/");
                            break;
                        }
                        else{
                            sb.insert(0, separator);
                        }
                    }
                    
                    // This is not a place that CAL Files can be put so use the project
                    if (container == null){
                        return getName(((IContainer) treeElement).getProject());
                    }
                    else if (next instanceof IContainer){
                        container = (IContainer) next;
                    }
                    else{
                        // something wicked this way comes
                        assert false;
                        return getName(((IContainer) treeElement).getProject());
                    }
                }
                sb.insert(0, container.getProject().getName());
                return sb.toString();
            }
            else{
                return "";
            }
            
        }
        
        /* (non-Javadoc)
         * @see org.eclipse.jface.dialogs.IDialogPage#createControl(org.eclipse.swt.widgets.Composite)
         */
        public void createControl(Composite parent) {
            Composite topLevel = new Composite(parent, SWT.NONE);
            topLevel.setLayout(new GridLayout());
            topLevel.setLayoutData(new GridData(GridData.VERTICAL_ALIGN_FILL
                    | GridData.HORIZONTAL_ALIGN_FILL));
            topLevel.setFont(parent.getFont());

            Label containerNameLabel = new Label(topLevel, 0);
            containerNameLabel.setText(CALUIMessages.CALNewModuleWizard_containerNameLabel);
            containerNameLabel.setFont(parent.getFont());
            
            containerNameText = new Text(topLevel, SWT.BORDER);
            containerNameText.addModifyListener(this);
            containerNameText.setFont(parent.getFont());
            containerNameText.setLayoutData(new GridData(GridData.HORIZONTAL_ALIGN_FILL | GridData.GRAB_HORIZONTAL));
            
            {
                treeViewer = new TreeViewer(topLevel, SWT.MULTI | SWT.H_SCROLL | SWT.V_SCROLL | SWT.BORDER);
                treeViewer.addSelectionChangedListener(this);
                treeViewer.setContentProvider(new TreeContentProvider());
                treeViewer.setLabelProvider(new TreeLabelProvider());
                treeViewer.setInput(workspaceRoot);
                treeViewer.getControl().setFont(parent.getFont());
                treeViewer.getControl().setLayoutData(new GridData(GridData.HORIZONTAL_ALIGN_FILL | GridData.GRAB_HORIZONTAL | GridData.VERTICAL_ALIGN_FILL | GridData.GRAB_VERTICAL));
            }

            Label moduleNameLabel = new Label(topLevel, 0);
            moduleNameLabel.setText(CALUIMessages.CALNewModuleWizard_moduleNameLabel);
            moduleNameLabel.setFont(parent.getFont());
            
            moduleNameText = new Text(topLevel, SWT.BORDER);
            moduleNameText.addModifyListener(this);
            moduleNameText.setFont(parent.getFont());
            moduleNameText.setLayoutData(new GridData(GridData.HORIZONTAL_ALIGN_FILL | GridData.GRAB_HORIZONTAL));
                        
            // Figure out what the current selection corresponds to
            {
                final ISelection selection = getSelection();
                Object selectedObject = null;
                if (selection instanceof TreeSelection){
                    final TreeSelection treeSelection = (TreeSelection) selection;
                    if (!treeSelection.isEmpty()){
                        selectedObject = treeSelection.getFirstElement();
                    }                    
                }
                else if (selection instanceof StructuredSelection){
                    final StructuredSelection structureSelection = (StructuredSelection) selection;
                    selectedObject = structureSelection.getFirstElement();
                }

                if (selectedObject != null){
                    setTreeSelection(selectedObject);
                }
            }
            
            // all done, finish up
            setErrorMessage(null);
            setMessage(null);
            setControl(topLevel);
            
            // if the container name is valid set the focus to the module name
            {
                Object[] validateMessage = getValidateMessage();
                if (validateMessage != null && ((Boolean) validateMessage[0]).booleanValue()){
                    moduleNameText.setFocus();                    
                }
            }
        }

        private void setTreeSelection(Object selectedObject) {
            if (selectedObject instanceof IJavaProject){
                containerNameText.setText(getName(selectedObject));
                // Set current selection
                {
                    // If there is a valid source folder then initialize
                    // the container with the first source folder in the 
                    // project.
                    IJavaProject project = (IJavaProject) selectedObject;
                    try {
                        for(IJavaElement je : project.getChildren()){
                            if (je instanceof IPackageFragmentRoot){
                                setTreeSelection(je);
                                return;
                            }
                        }
                    } catch (JavaModelException e) {
                        // No biggie, use the default of selecting the current project.
                    }
                    final Object[] pathObjects = new Object[2];
                    pathObjects[0] = workspaceRoot;
                    pathObjects[1] = project;
                    treeViewer.setSelection(new TreeSelection(new TreePath(pathObjects)));
                }
            }
            else if (selectedObject instanceof IPackageFragmentRoot){
                final IPackageFragmentRoot pfr = (IPackageFragmentRoot) selectedObject;
                if (pfr.getJavaProject() == pfr.getParent() && !pfr.isArchive()){
                    if (pfr.getResource() instanceof IFolder){
                        containerNameText.setText(getName(selectedObject));

                        // Set current selection
                        {
                            final Object[] pathObjects = new Object[3];
                            pathObjects[0] = workspaceRoot;
                            pathObjects[1] = pfr.getJavaProject();
                            pathObjects[2] = pfr;
                            treeViewer.setSelection(new TreeSelection(new TreePath(pathObjects)));
                        }
                    }
                    else if (pfr.getResource() instanceof IProject){
                        containerNameText.setText(getName(pfr.getJavaProject()));

                        // Set current selection
                        {
                            final Object[] pathObjects = new Object[2];
                            pathObjects[0] = workspaceRoot;
                            pathObjects[1] = pfr.getJavaProject();
                            treeViewer.setSelection(new TreeSelection(new TreePath(pathObjects)));
                        }
                    }
                }
                else{
                    containerNameText.setText(getName(pfr.getJavaProject()));

                    // Set current selection
                    {
                        final Object[] pathObjects = new Object[1];
                        pathObjects[0] = workspaceRoot;
                        treeViewer.setSelection(new TreeSelection(new TreePath(pathObjects)));
                    }
                }
            }
            else if (selectedObject instanceof IFolder){
                final IFolder folder = (IFolder) selectedObject;
                containerNameText.setText(getName(folder.getProject()));
                
                IJavaElement javaElement = JavaCore.create(folder);
                // If the navigator is used to invoke the new module
                // wizard then the current selection will not be a java
                // element. But if the folder corresponds to a java element
                // then initialize the dialog using that. Otherwise just initialize 
                // the container with the current package.
                if (javaElement != null){
                    setTreeSelection(javaElement);
                }
                else{
                // Set selection in tree viewer
                    final ArrayList<IAdaptable> pathObjects = new ArrayList<IAdaptable>();
                    pathObjects.add(workspaceRoot);
                    {
                        IJavaProject javaProject = JavaCore.create(folder.getProject());
                        if (javaProject != null){
                            pathObjects.add(javaProject);
                        }
                    }

                    treeViewer.setSelection(new TreeSelection(new TreePath(pathObjects.toArray())));
                }                
            }
            else if (selectedObject instanceof IProject){
                final IProject project = (IProject) selectedObject;
                final IJavaElement javaElement = JavaCore.create(project);
                // If the navigator is used to invoke the new module
                // wizard then the current selection will not be a java
                // element. But if the project corresponds to a java element
                // then initialize the dialog using that. 
                if (javaElement != null){
                    setTreeSelection(javaElement);
                }
                else{                
                    // no valid initialization possible
                }
            }
            else if (selectedObject instanceof IFile){
                final IFile file = (IFile) selectedObject;
                containerNameText.setText(getName(file.getProject()));

                // Set selection in tree viewer
                {
                    final ArrayList<IAdaptable> pathObjects = new ArrayList<IAdaptable>();
                    pathObjects.add(workspaceRoot);
                    {
                        IJavaProject javaProject = JavaCore.create(file.getProject());
                        if (javaProject != null){
                            pathObjects.add(javaProject);
                        }
                    }

                    treeViewer.setSelection(new TreeSelection(new TreePath(pathObjects.toArray())));
                }
            }
            else if (selectedObject instanceof IPackageFragment){
                final IPackageFragment packageFragment = (IPackageFragment) selectedObject;
                final String name = packageFragment.getElementName();
                final String[] components = name.split("\\.", 0);
                if (components[0].equals("CAL")){
                    final IPackageFragmentRoot packageFragmentRoot = (IPackageFragmentRoot) packageFragment.getParent();
                    if (packageFragmentRoot.getJavaProject() == packageFragmentRoot.getParent() && !packageFragmentRoot.isArchive()){
                        containerNameText.setText(getName(selectedObject));

                        // Set current selection
                        {
                            final ArrayList<IAdaptable> pathObjects = new ArrayList<IAdaptable>();
                            pathObjects.add(workspaceRoot);
                            pathObjects.add(packageFragmentRoot.getJavaProject());
                            if (packageFragmentRoot.getResource() instanceof IFolder){
                                pathObjects.add(packageFragmentRoot);
                            }
                            {
                                IFolder folder = null;
                                
                                if (packageFragmentRoot.getResource() instanceof IFolder){
                                    folder = ((IFolder) packageFragmentRoot.getResource()).getFolder("CAL");
                                }
                                else if (packageFragmentRoot.getResource() instanceof IProject){
                                    folder = ((IProject) packageFragmentRoot.getResource()).getFolder("CAL");
                                }
                                else{
                                    // skip this
                                }

                                if (folder != null){
                                    if (folder == null || !folder.exists()){
                                        // cannot go deeper
                                    }
                                    else{
                                        for(int iC = 1; iC < components.length; ++iC){
                                            final String componentName = components[iC];
                                            if (LanguageInfo.isValidModuleNameComponent(componentName)){
                                                folder = folder.getFolder(componentName);
                                                pathObjects.add(folder);
                                            }
                                        }
                                    }
                                }
                            }
                            treeViewer.setSelection(new TreeSelection(new TreePath(pathObjects.toArray())));
                        }                                    
                    }
                }
            }
            else{
            }
        }

        private String getModuleName(){
            return moduleNameText.getText();
        }

        /**
         * @return true if all the entries on the current page are valid otherwise the error message is set and false is returned
         */
        protected boolean validatePage() {
            setErrorMessage(null);
            Object[] result = getValidateMessage();
            if (result != null){
                String message = (String) result[1];
                setErrorMessage(message);
                return false;
            }
            return true;
        }

        /**
         * @return an array where the first element is a boolean that is true if the container is valid
         * and false when it is not. The second element is the error message. If there is no problem
         * then null is returned.
         */
        private Object[] getValidateMessage(){
            // Project not null check
            {
                String containerNameString = containerNameText.getText();
                if (containerNameString.length() == 0){                    
                    return new Object[] {Boolean.valueOf(false), CALUIMessages.CALNewModuleWizard_error_containerNameMustNotBeNull};
                }
            }
            
            {
                IProject project;
                try{
                    project = getProject();
                }
                catch(IllegalArgumentException e){
                    return new Object[] {Boolean.valueOf(false), e.toString()};
                }
                if (project == null || !project.exists()){
                    return new Object[] {Boolean.valueOf(false), CALUIMessages.CALNewModuleWizard_error_containerMustHaveProjectName};
                }
                
                final String containerNameString = containerNameText.getText();
                // first component is the project name
                // the second is the source folders
                // the third are any hierarchical components
                final String sourceFolders;
                final String hierarchicalComponents;
                {
                    int indexOfFirstSlash = containerNameString.indexOf('/');
                    if (indexOfFirstSlash == -1){
                        int indexOfFirstDot = containerNameString.indexOf('.', indexOfFirstSlash);
                        if (indexOfFirstDot == -1){
                            sourceFolders = "";
                            hierarchicalComponents = "";
                        }
                        else{
                            sourceFolders = "";
                            hierarchicalComponents = containerNameString.substring(indexOfFirstDot + 1);
                        }
                        containerNameString.split("\\.");
                    }
                    else{
                        int indexOfFirstDot = containerNameString.indexOf('.', indexOfFirstSlash);
                        if (indexOfFirstDot == -1){
                            sourceFolders = containerNameString.substring(indexOfFirstSlash + 1);
                            hierarchicalComponents = "";
                        }
                        else{
                            sourceFolders = containerNameString.substring(indexOfFirstSlash + 1, indexOfFirstDot);
                            hierarchicalComponents = containerNameString.substring(indexOfFirstDot + 1);
                        }
                        containerNameString.split("\\.");
                        
                        // Check the source folder exists. This can happen when the project has no source folders
                        if (sourceFolders.length() == 0){
                            final String message = CALUIMessages.CALNewModuleWizard_error_sourceFolderMustBeSpecified;
                            return new Object[] {Boolean.valueOf(false), message};
                        }
                        
                        // Check the source folder exists
                        {
                            IJavaProject jProject = JavaCore.create(project);
                            try{
                                IFolder srcFolder = jProject.getProject().getFolder(sourceFolders);
                                if (!srcFolder.exists()){
                                    final String message = Messages.format(CALUIMessages.CALNewModuleWizard_error_sourceFolderMustExist, new Object[] {srcFolder.getName()});
                                    return new Object[] {Boolean.valueOf(false), message};
                                }
                            }
                            catch(IllegalArgumentException ex){
                                // go on to the next test 
                            }
                        }
                    }
                }

                // Check the hierarchical component names for validity
                {
                    String[] components = hierarchicalComponents.split("\\."); 
                    for(int i = 1; i < components.length; ++i){
                        final String component = components[i];
                        if (!LanguageInfo.isValidModuleNameComponent(component)){
                            final String message = Messages.format(CALUIMessages.CALNewModuleWizard_error_invalidContainerComponent, new Object[] {component});
                            return new Object[] {Boolean.valueOf(false), message};
                        }
                    }
                }
                // Split ignores trailing dots
                if (containerNameString.endsWith("/")){
                    return new Object[] {Boolean.valueOf(false), CALUIMessages.CALNewModuleWizard_error_noTrailingDot};
                }
            }

            // Check the module name
            {
                // does the name have valid structure
                
                if (!LanguageInfo.isValidModuleName(getModuleName())){
                    return new Object[] {Boolean.valueOf(true), CALUIMessages.CALNewModuleWizard_error_invalidModuleName};
                }

                final String moduleNameString = getModuleNameString();
                
                // Split ignores trailing dots
                if (moduleNameString.endsWith(".")){
                    return new Object[] {Boolean.valueOf(true), CALUIMessages.CALNewModuleWizard_error_invalidModuleName};
                }
                
                // make sure a module with this name does not exist
                {
                    final CALModelManager cmm = CALModelManager.getCALModelManager();
                    final ModuleName moduleName = ModuleName.make(moduleNameString);
                    final IStorage thisStorage = cmm.getInputSourceFile(moduleName);
                    if (thisStorage instanceof IFile) {
                        return new Object[] {Boolean.valueOf(true), CALUIMessages.CALNewModuleWizard_error_moduleAlreadyExists};
                    }
                }
            }
            return null;
        }
        
        public void selectionChanged(SelectionChangedEvent event) {
            TreeSelection selection = (TreeSelection) event.getSelection();
            Object selectedElement = selection.getFirstElement();
            containerNameText.setText(getName(selectedElement));
        }
    }
    
    /* (non-Javadoc)
     * @see org.eclipse.jface.wizard.Wizard#addPages()
     */
    @Override
    public void addPages() {
        super.addPages();
        // The new module page
        
        {
            mainPage = new WizardNewModuleCreationPage("newFilePage1", getSelection()){            
//                protected String getNewFileLabel(){
//                    return CALUIMessages.CALNewModuleWizard_newFileLabel;
//                }            
            };
            mainPage.setTitle(CALUIMessages.CALNewModuleWizard_pageTitle);
            mainPage.setDescription(CALUIMessages.CALNewModuleWizard_description); 
            addPage(mainPage);
        }
    }

    /* (non-Javadoc)
     * Method declared on BasicNewResourceWizard.
     */
    @Override
    protected void initializeDefaultPageImageDescriptor() {
//       ImageDescriptor desc = CALEclipseUIPlugin.getImageDescriptor("/icons/nav_module.png");//$NON-NLS-1$
//       setDefaultPageImageDescriptor(desc);
    }

    /* (non-Javadoc)
     * Method declared on IWizard.
     */
    @Override
    public boolean performFinish() {
        IFile file;
        try {
            file = mainPage.createNewFile();
        } catch (CoreException e) {
            // TODO Auto-generated catch block
            mainPage.setErrorMessage(e.getMessage());
            return false;
        }
        if (file == null) {
            return false;
        }

        IProject project = file.getProject();
        
        // Ensure the the project has the CAL Nature so that the builder will work on it.
        CoreUtility.turnOnNature(project);
        
        // For the src folder, ensure an exclude pattern for the lecc_runtime/ folder so that the package explorer isn't littered.
        CoreUtility.ensureLeccFolderSrcExclude(file);
        
        selectAndReveal(file);

        // Open editor on new file.
        IWorkbenchWindow dw = getWorkbench().getActiveWorkbenchWindow();
        try {
            if (dw != null) {
                IWorkbenchPage page = dw.getActivePage();
                if (page != null) {
                    IDE.openEditor(page, file, true);
                }
            }
        } catch (PartInitException e) {
            CoreUtility.showMessage(CALUIMessages.CALNewModuleWizard_errorTitle, e.getMessage(), IStatus.ERROR);
        }

        
        if (!CoreUtility.addReferenceToQuarkBinariesIfNeeded(project)){
            CoreUtility.showMessage(CALUIMessages.CALNewModuleWizard_errorTitle, CALUIMessages.CALNewModuleWizard_missingQuarkBinariesProject, IStatus.WARNING);
        }
        
        return true;
    }
}
