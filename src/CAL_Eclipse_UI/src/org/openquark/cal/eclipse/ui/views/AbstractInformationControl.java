/*******************************************************************************
 * Copyright (c) 2007 Business Objects Software Limited and others.
 * All rights reserved. 
 * This file is made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Business Objects Software Limited - initial API and implementation based on Eclipse 3.1.2 code for
 *                             org.eclipse.jdt.internal.ui.text.AbstractInformationControl
 *                           Eclipse source is available at: http://www.eclipse.org/downloads/
 *******************************************************************************/

/*
 * AbstractInformationControl.java
 * Created: 03-Sept-07
 * By: Greg McClement
 */

package org.openquark.cal.eclipse.ui.views;

import java.util.List;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.events.FocusListener;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.KeyListener;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.MouseMoveListener;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Item;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.swt.widgets.TreeItem;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogSettings;
import org.eclipse.jface.dialogs.PopupDialog;
import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerFilter;

import org.eclipse.jface.text.IInformationControl;
import org.eclipse.jface.text.IInformationControlExtension;
import org.eclipse.jface.text.IInformationControlExtension2;

import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.commands.ActionHandler;
import org.eclipse.ui.commands.HandlerSubmission;
import org.eclipse.ui.commands.ICommand;
import org.eclipse.ui.commands.ICommandManager;
import org.eclipse.ui.commands.IKeySequenceBinding;
import org.eclipse.ui.commands.Priority;
import org.eclipse.ui.keys.KeySequence;

import org.openquark.cal.compiler.ClassInstance;
import org.openquark.cal.compiler.ScopedEntity;
import org.openquark.cal.eclipse.ui.CALEclipseUIPlugin;
import org.openquark.cal.eclipse.ui.util.CoreUtility;
import org.openquark.cal.eclipse.ui.util.StringMatcher;

/**
 * Abstract class for Show hierarchy in light-weight controls.
 *
 * @since 2.1
 */
public abstract class AbstractInformationControl extends PopupDialog implements IInformationControl, IInformationControlExtension, IInformationControlExtension2, DisposeListener {

    /**
     * The NamePatternFilter selects the elements which
     * match the given string patterns.
     *
     * @since 2.0
     */
    protected class NamePatternFilter extends ViewerFilter {

        public NamePatternFilter() {
        }

        /* (non-Javadoc)
         * Method declared on ViewerFilter.
         */
        @Override
        public boolean select(Viewer viewer, Object parentElement, Object element) {
            StringMatcher matcher= getMatcher();
            if (matcher == null || !(viewer instanceof TreeViewer))
                return true;
            TreeViewer treeViewer= (TreeViewer) viewer;

            if (!(element instanceof ScopedEntity || element instanceof ClassInstance)){
                return true;
            }

            String matchName= ((ILabelProvider) treeViewer.getLabelProvider()).getText(element);
            if (matchName != null && matcher.match(matchName))
                return true;

            return hasUnfilteredChild(treeViewer, element);
        }

        private boolean hasUnfilteredChild(TreeViewer viewer, Object element) {
//            if (element instanceof IParent) {
//                Object[] children=  ((ITreeContentProvider) viewer.getContentProvider()).getChildren(element);
//                for (int i= 0; i < children.length; i++)
//                    if (select(viewer, element, children[i]))
//                        return true;
//            }
            return false;
        }
    }

    /** The control's text widget */
    private Text fFilterText;
    /** The control's tree widget */
    private TreeViewer fTreeViewer;
    /** The current string matcher */
    protected StringMatcher fStringMatcher;
    private ICommand fInvokingCommand;
    private KeySequence[] fInvokingCommandKeySequences;

    /**
     * Fields that support the dialog menu
     * @since 3.0
     * @since 3.2 - now appended to framework menu
     */
    private Composite fViewMenuButtonComposite;

//    private CustomFiltersActionGroup fCustomFiltersActionGroup;

    private IAction fShowViewMenuAction;
    private HandlerSubmission fShowViewMenuHandlerSubmission;

    /**
     * Field for tree style since it must be remembered by the instance.
     * 
     * @since 3.2
     */
    private int fTreeStyle;
    
    /**
     * Creates a tree information control with the given shell as parent. The given
     * styles are applied to the shell and the tree widget.
     *
     * @param parent the parent shell
     * @param shellStyle the additional styles for the shell
     * @param treeStyle the additional styles for the tree widget
     * @param invokingCommandId the id of the command that invoked this control or <code>null</code>
     * @param showStatusField <code>true</code> iff the control has a status field at the bottom
     */
    public AbstractInformationControl(Shell parent, int shellStyle, int treeStyle, String invokingCommandId, boolean showStatusField) {
        super(parent, shellStyle, true, true, true, true, null, null);
        if (invokingCommandId != null) {
            ICommandManager commandManager= PlatformUI.getWorkbench().getCommandSupport().getCommandManager();
            fInvokingCommand= commandManager.getCommand(invokingCommandId);
            if (fInvokingCommand != null && !fInvokingCommand.isDefined())
                fInvokingCommand= null;
            else
                // Pre-fetch key sequence - do not change because scope will change later.
                getInvokingCommandKeySequences();
        }
        fTreeStyle= treeStyle;
        // Title and status text must be set to get the title label created, so force empty values here. 
        if (hasHeader())
            setTitleText(""); //$NON-NLS-1$
        setInfoText(""); //  //$NON-NLS-1$

        // Create all controls early to preserve the life cycle of the original implementation.
        create();

        // Status field text can only be computed after widgets are created.
        setInfoText(getStatusFieldText());
    }

    /**
     * Create the main content for this information control.
     * 
     * @param parent The parent composite
     * @return The control representing the main content.
     * @since 3.2
     */
    @Override
    protected Control createDialogArea(Composite parent) {
        fTreeViewer= createTreeViewer(parent, fTreeStyle);

//        fCustomFiltersActionGroup= new CustomFiltersActionGroup(getId(), fTreeViewer);

        final Tree tree= fTreeViewer.getTree();
        tree.addKeyListener(new KeyListener() {
            public void keyPressed(KeyEvent e)  {
                if (e.character == 0x1B) // ESC
                    dispose();
            }
            public void keyReleased(KeyEvent e) {
                // do nothing
            }
        });

        tree.addSelectionListener(new SelectionListener() {
            public void widgetSelected(SelectionEvent e) {
                // do nothing
            }
            public void widgetDefaultSelected(SelectionEvent e) {
                gotoSelectedElement();
            }
        });

        tree.addMouseMoveListener(new MouseMoveListener()    {
            TreeItem fLastItem= null;
            public void mouseMove(MouseEvent e) {
                if (tree.equals(e.getSource())) {
                    Object o= tree.getItem(new Point(e.x, e.y));
                    if (o instanceof TreeItem) {
                        if (!o.equals(fLastItem)) {
                            fLastItem= (TreeItem)o;
                            tree.setSelection(new TreeItem[] { fLastItem });
                        } else if (e.y < tree.getItemHeight() / 4) {
                            // Scroll up
                            Point p= tree.toDisplay(e.x, e.y);
                            Item item= fTreeViewer.scrollUp(p.x, p.y);
                            if (item instanceof TreeItem) {
                                fLastItem= (TreeItem)item;
                                tree.setSelection(new TreeItem[] { fLastItem });
                            }
                        } else if (e.y > tree.getBounds().height - tree.getItemHeight() / 4) {
                            // Scroll down
                            Point p= tree.toDisplay(e.x, e.y);
                            Item item= fTreeViewer.scrollDown(p.x, p.y);
                            if (item instanceof TreeItem) {
                                fLastItem= (TreeItem)item;
                                tree.setSelection(new TreeItem[] { fLastItem });
                            }
                        }
                    }
                }
            }
        });

        tree.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseUp(MouseEvent e) {

                if (tree.getSelectionCount() < 1)
                    return;

                if (e.button != 1)
                    return;

                if (tree.equals(e.getSource())) {
                    Object o= tree.getItem(new Point(e.x, e.y));
                    TreeItem selection= tree.getSelection()[0];
                    if (selection.equals(o))
                        gotoSelectedElement();
                }
            }
        });

        installFilter();

        addDisposeListener(this);
        return fTreeViewer.getControl();
    }
    
    /**
     * Creates a tree information control with the given shell as parent. The given
     * styles are applied to the shell and the tree widget.
     *
     * @param parent the parent shell
     * @param shellStyle the additional styles for the shell
     * @param treeStyle the additional styles for the tree widget
     */
    public AbstractInformationControl(Shell parent, int shellStyle, int treeStyle) {
        this(parent, shellStyle, treeStyle, null, false);
    }

    protected abstract TreeViewer createTreeViewer(Composite parent, int style);

    /**
     * Returns the name of the dialog settings section.
     *
     * @return the name of the dialog settings section
     */
    protected abstract String getId();

    protected TreeViewer getTreeViewer() {
        return fTreeViewer;
    }

    /**
     * Returns <code>true</code> if the control has a header, <code>false</code> otherwise.
     * <p>
     * The default is to return <code>false</code>.
     * </p>
     * 
     * @return <code>true</code> if the control has a header
     */
    protected boolean hasHeader() {
        // default is to have no header
        return false;
    }

    protected Text getFilterText() {
        return fFilterText;
    }

    protected Text createFilterText(Composite parent) {
        fFilterText= new Text(parent, SWT.NONE);
        Dialog.applyDialogFont(fFilterText);

        GridData data= new GridData(GridData.FILL_HORIZONTAL);
        data.horizontalAlignment= GridData.FILL;
        data.verticalAlignment= GridData.CENTER;
        fFilterText.setLayoutData(data);

        fFilterText.addKeyListener(new KeyListener() {
            public void keyPressed(KeyEvent e) {
                if (e.keyCode == 0x0D) // return
                    gotoSelectedElement();
                if (e.keyCode == SWT.ARROW_DOWN)
                    fTreeViewer.getTree().setFocus();
                if (e.keyCode == SWT.ARROW_UP)
                    fTreeViewer.getTree().setFocus();
                if (e.character == 0x1B) // ESC
                    dispose();
            }
            public void keyReleased(KeyEvent e) {
                // do nothing
            }
        });

        return fFilterText;
    }

    protected void createHorizontalSeparator(Composite parent) {
        Label separator= new Label(parent, SWT.SEPARATOR | SWT.HORIZONTAL | SWT.LINE_DOT);
        separator.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
    }

    protected void updateStatusFieldText() {
        setInfoText(getStatusFieldText());
    }

    /**
     * Handles click in status field.
     * <p>
     * Default does nothing.
     * </p>
     */
    protected void handleStatusFieldClicked() {
    }

    protected String getStatusFieldText() {
        return ""; //$NON-NLS-1$
    }

    private void installFilter() {
        fFilterText.setText(""); //$NON-NLS-1$

        fFilterText.addModifyListener(new ModifyListener() {
            public void modifyText(ModifyEvent e) {
                String text= ((Text) e.widget).getText();
                int length= text.length();
                if (length > 0 && text.charAt(length -1 ) != '*') {
                    text= text + '*';
                }
                setMatcherString(text, true);
            }
        });
    }

    /**
     * The string matcher has been modified. The default implementation
     * refreshes the view and selects the first matched element
     */
    protected void stringMatcherUpdated() {
        // refresh viewer to re-filter
        fTreeViewer.getControl().setRedraw(false);
        fTreeViewer.refresh();
        fTreeViewer.expandAll();
        selectFirstMatch();
        fTreeViewer.getControl().setRedraw(true);
    }

    /**
     * Sets the patterns to filter out for the receiver.
     * <p>
     * The following characters have special meaning:
     *   ? => any character
     *   * => any string
     * </p>
     *
     * @param pattern the pattern
     * @param update <code>true</code> if the viewer should be updated
     */
    protected void setMatcherString(String pattern, boolean update) {
        if (pattern.length() == 0) {
            fStringMatcher= null;
        } else {
            boolean ignoreCase= pattern.toLowerCase().equals(pattern);
            fStringMatcher= new StringMatcher(pattern, ignoreCase, false);
        }
        
        if (update)
            stringMatcherUpdated();
    }

    protected StringMatcher getMatcher() {
        return fStringMatcher;
    }

    /**
     * Implementers can modify
     *
     * @return the selected element
     */
    protected Object getSelectedElement() {
        if (fTreeViewer == null)
            return null;

        return ((IStructuredSelection) fTreeViewer.getSelection()).getFirstElement();
    }

    private void gotoSelectedElement() {
        Object selectedElement= getSelectedElement();
        if (selectedElement != null) {
//            try {
                dispose();
                if (selectedElement instanceof ScopedEntity){
                    CoreUtility.showCALElementInEditor((ScopedEntity) selectedElement);
                }
                else if (selectedElement instanceof ClassInstance){
                    CoreUtility.showCALElementInEditor((ClassInstance) selectedElement);
                }
//            } catch (CoreException ex) {
//                CALEclipseUIPlugin.log(new Status(IStatus.ERROR, CALEclipseUIPlugin.PLUGIN_ID, IStatus.OK, "", ex)); //$NON-NLS-1$
//            }
        }
    }

    /**
     * Selects the first element in the tree which
     * matches the current filter pattern.
     */
    protected void selectFirstMatch() {
        Tree tree= fTreeViewer.getTree();
        Object element= findElement(tree.getItems());
        if (element != null)
            fTreeViewer.setSelection(new StructuredSelection(element), true);
        else
            fTreeViewer.setSelection(StructuredSelection.EMPTY);
    }

    private Object findElement(TreeItem[] items) {
        ILabelProvider labelProvider= (ILabelProvider)fTreeViewer.getLabelProvider();
        for (TreeItem element2 : items) {
            Object element= element2.getData();
            if (fStringMatcher == null)
                return element;

            if (element != null) {
                String label= labelProvider.getText(element);
                if (fStringMatcher.match(label))
                    return element;
            }

            element= findElement(element2.getItems());
            if (element != null)
                return element;
        }
        return null;
    }

    /**
     * {@inheritDoc}
     */
    public void setInformation(String information) {
        // this method is ignored, see IInformationControlExtension2
    }

    /**
     * {@inheritDoc}
     */
    public abstract void setInput(Object information);

    /**
     * Fills the view menu.
     * Clients can extend or override.
     *
     * @param viewMenu the menu manager that manages the menu
     * @since 3.0
     */
    protected void fillViewMenu(IMenuManager viewMenu) {
//        fCustomFiltersActionGroup.fillViewMenu(viewMenu);
    }

    /*
     * Overridden to call the old framework method.
     * 
     * @see org.eclipse.jface.dialogs.PopupDialog#fillDialogMenu(IMenuManager)
     * @since 3.2
     */
    @Override
    protected void fillDialogMenu(IMenuManager dialogMenu) {
        super.fillDialogMenu(dialogMenu);
        fillViewMenu(dialogMenu);
    }

    protected void inputChanged(Object newInput, Object newSelection) {
        fFilterText.setText(""); //$NON-NLS-1$
        fTreeViewer.setInput(newInput);
        if (newSelection != null) {
            fTreeViewer.setSelection(new StructuredSelection(newSelection));
        }
    }

    /**
     * {@inheritDoc}
     */
    public void setVisible(boolean visible) {
        if (visible) {
            open();
        } else {
            removeHandlerAndKeyBindingSupport();
            saveDialogBounds(getShell());
            getShell().setVisible(false);
            removeHandlerAndKeyBindingSupport();
        }
    }
    
    /*
     * @see org.eclipse.jface.dialogs.PopupDialog#open()
     * @since 3.3
     */
    @Override
    public int open() {
        addHandlerAndKeyBindingSupport();
        return super.open();
    }

    /**
     * {@inheritDoc}
     */
    public final void dispose() {
        close();
    }

    /**
     * {@inheritDoc}
     * @param event can be null
     * <p>
     * Subclasses may extend.
     * </p>
     */
    public void widgetDisposed(DisposeEvent event) {
        removeHandlerAndKeyBindingSupport();
        fTreeViewer= null;
        fFilterText= null;
    }

    /**
     * Adds handler and key binding support.
     * 
     * @since 3.2
     */
    protected void addHandlerAndKeyBindingSupport() {
        // Register action with command support
        if (fShowViewMenuHandlerSubmission == null) {
            fShowViewMenuHandlerSubmission= new HandlerSubmission(null, getShell(), null, fShowViewMenuAction.getActionDefinitionId(), new ActionHandler(fShowViewMenuAction), Priority.MEDIUM);
            PlatformUI.getWorkbench().getCommandSupport().addHandlerSubmission(fShowViewMenuHandlerSubmission);
        }
    }

    /**
     * Removes handler and key binding support.
     * 
     * @since 3.2
     */
    protected void removeHandlerAndKeyBindingSupport() {
        // Remove handler submission
        if (fShowViewMenuHandlerSubmission != null)
            PlatformUI.getWorkbench().getCommandSupport().removeHandlerSubmission(fShowViewMenuHandlerSubmission);

    }

    /**
     * {@inheritDoc}
     */
    public boolean hasContents() {
        return fTreeViewer != null && fTreeViewer.getInput() != null;
    }

    /**
     * {@inheritDoc}
     */
    public void setSizeConstraints(int maxWidth, int maxHeight) {
        // ignore
    }

    /**
     * {@inheritDoc}
     */
    public Point computeSizeHint() {
        // return the shell's size - note that it already has the persisted size if persisting
        // is enabled.
        return getShell().getSize();
    }

    /**
     * {@inheritDoc}
     */
    public void setLocation(Point location) {
        /*
         * If the location is persisted, it gets managed by PopupDialog - fine. Otherwise, the location is
         * computed in Window#getInitialLocation, which will center it in the parent shell / main
         * monitor, which is wrong for two reasons:
         * - we want to center over the editor / subject control, not the parent shell
         * - the center is computed via the initalSize, which may be also wrong since the size may 
         *   have been updated since via min/max sizing of AbstractInformationControlManager.
         * In that case, override the location with the one computed by the manager. Note that
         * the call to constrainShellSize in PopupDialog.open will still ensure that the shell is
         * entirely visible.
         */
        if (!getPersistBounds() || getDialogSettings() == null)
            getShell().setLocation(location);
    }

    /**
     * {@inheritDoc}
     */
    public void setSize(int width, int height) {
        if (!getPersistBounds() || getDialogSettings() == null){
            getShell().setSize(width, height);
        }
    }

    /**
     * {@inheritDoc}
     */
    public void addDisposeListener(DisposeListener listener) {
        getShell().addDisposeListener(listener);
    }

    /**
     * {@inheritDoc}
     */
    public void removeDisposeListener(DisposeListener listener) {
        getShell().removeDisposeListener(listener);
    }

    /**
     * {@inheritDoc}
     */
    public void setForegroundColor(Color foreground) {
        applyForegroundColor(foreground, getContents());
    }

    /**
     * {@inheritDoc}
     */
    public void setBackgroundColor(Color background) {
        applyBackgroundColor(background, getContents());
    }

    /**
     * {@inheritDoc}
     */
    public boolean isFocusControl() {
        return fTreeViewer.getControl().isFocusControl() || fFilterText.isFocusControl();
    }

    /**
     * {@inheritDoc}
     */
    public void setFocus() {
        getShell().forceFocus();
        fFilterText.setFocus();
    }

    /**
     * {@inheritDoc}
     */
    public void addFocusListener(FocusListener listener) {
        getShell().addFocusListener(listener);
    }

    /**
     * {@inheritDoc}
     */
    public void removeFocusListener(FocusListener listener) {
        getShell().removeFocusListener(listener);
    }

    final protected ICommand getInvokingCommand() {
        return fInvokingCommand;
    }

    final protected KeySequence[] getInvokingCommandKeySequences() {
        if (fInvokingCommandKeySequences == null) {
            if (getInvokingCommand() != null) {
                List list= getInvokingCommand().getKeySequenceBindings();
                if (!list.isEmpty()) {
                    fInvokingCommandKeySequences= new KeySequence[list.size()];
                    for (int i= 0; i < fInvokingCommandKeySequences.length; i++) {
                        fInvokingCommandKeySequences[i]= ((IKeySequenceBinding) list.get(i)).getKeySequence();
                    }
                    return fInvokingCommandKeySequences;
                }
            }
        }
        return fInvokingCommandKeySequences;
    }

    /*
     * @see org.eclipse.jface.dialogs.PopupDialog#getDialogSettings()
     */
    @Override
    protected IDialogSettings getDialogSettings() {
        String section_name= getId();

        IDialogSettings section= CALEclipseUIPlugin.getDefault().getDialogSettings().getSection(section_name);
        if (section == null) {
            section= CALEclipseUIPlugin.getDefault().getDialogSettings().addNewSection(section_name);
        }

        return section;
    }
    
    /*
     * Overridden to insert the filter text into the title and menu area.
     * 
     * @since 3.2
     */
    @Override
    protected Control createTitleMenuArea(Composite parent) {
        fViewMenuButtonComposite= (Composite) super.createTitleMenuArea(parent);

        // If there is a header, then the filter text must be created
        // underneath the title and menu area.

        if (hasHeader()) {
            fFilterText= createFilterText(parent);
        }

        // Create show view menu action
        fShowViewMenuAction= new Action("showViewMenu") { //$NON-NLS-1$
            /*
             * @see org.eclipse.jface.action.Action#run()
             */
            @Override
            public void run() {
                showDialogMenu();
            }
        };
        fShowViewMenuAction.setEnabled(true);
        fShowViewMenuAction.setActionDefinitionId("org.eclipse.ui.window.showViewMenu"); //$NON-NLS-1$

        return fViewMenuButtonComposite;
    }

    /*
     * Overridden to insert the filter text into the title control
     * if there is no header specified.
     * @since 3.2
     */
    @Override
    protected Control createTitleControl(Composite parent) {
        if (hasHeader()) {
            return super.createTitleControl(parent);
        }
        fFilterText= createFilterText(parent);
        return fFilterText;
    }
    
    /*
     * @see org.eclipse.jface.dialogs.PopupDialog#setTabOrder(org.eclipse.swt.widgets.Composite)
     */
    @Override
    protected void setTabOrder(Composite composite) {
        if (hasHeader()) {
            composite.setTabList(new Control[] { fFilterText, fTreeViewer.getTree() });
        } else {
            fViewMenuButtonComposite.setTabList(new Control[] { fFilterText });
            composite.setTabList(new Control[] { fViewMenuButtonComposite, fTreeViewer.getTree() });
        }
    }
}
