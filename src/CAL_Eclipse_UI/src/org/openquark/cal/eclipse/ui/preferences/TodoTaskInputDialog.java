/*******************************************************************************
 * Copyright (c) 2006 Business Objects Software Limited and others.
 * All rights reserved. 
 * This file is made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Business Objects Software Limited - initial API and implementation based on Eclipse 3.1.2 code for
 *                             /org.eclipse.jdt.ui/ui/org/eclipse/jdt/internal/ui/preferences/TodoTaskInputDialog.java
 *                           Eclipse source is available at: http://www.eclipse.org/downloads/
 *******************************************************************************/

/*
 * TodoTaskInputDialog.java
 * Creation date: Jun 27, 2006.
 * By: Edward Lam
 */
package org.openquark.cal.eclipse.ui.preferences;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jface.dialogs.StatusDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.PlatformUI;
import org.openquark.cal.eclipse.core.CoreOptionIDs;
import org.openquark.cal.eclipse.ui.CALHelpContextIds;
import org.openquark.cal.eclipse.ui.dialogs.StatusInfo;
import org.openquark.cal.eclipse.ui.preferences.TodoTaskConfigurationBlock.TodoTask;
import org.openquark.cal.eclipse.ui.wizards.dialogfields.ComboDialogField;
import org.openquark.cal.eclipse.ui.wizards.dialogfields.DialogField;
import org.openquark.cal.eclipse.ui.wizards.dialogfields.IDialogFieldListener;
import org.openquark.cal.eclipse.ui.wizards.dialogfields.LayoutUtil;
import org.openquark.cal.eclipse.ui.wizards.dialogfields.StringDialogField;


/**
 * Dialog to enter a new task tag
 * @author Edward Lam
 */
public class TodoTaskInputDialog extends StatusDialog {

    private class CompilerTodoTaskInputAdapter implements IDialogFieldListener {

        public void dialogFieldChanged(DialogField field) {
            doValidation();
        }
    }

    private final StringDialogField fNameDialogField;
    private final ComboDialogField fPriorityDialogField;

    private final List<String> fExistingNames;

    public TodoTaskInputDialog(Shell parent, TodoTask task, List<Object> existingEntries) {
        super(parent);

        fExistingNames = new ArrayList<String>(existingEntries.size());
        for (int i = 0; i < existingEntries.size(); i++) {
            TodoTask curr = (TodoTask)existingEntries.get(i);
            if (!curr.equals(task)) {
                fExistingNames.add(curr.name);
            }
        }

        if (task == null) {
            setTitle(PreferencesMessages.TodoTaskInputDialog_new_title);
        } else {
            setTitle(PreferencesMessages.TodoTaskInputDialog_edit_title);
        }

        CompilerTodoTaskInputAdapter adapter = new CompilerTodoTaskInputAdapter();

        fNameDialogField = new StringDialogField();
        fNameDialogField.setLabelText(PreferencesMessages.TodoTaskInputDialog_name_label);
        fNameDialogField.setDialogFieldListener(adapter);

        fNameDialogField.setText((task != null) ? task.name : ""); //$NON-NLS-1$

        String[] items = new String[]{PreferencesMessages.TodoTaskInputDialog_priority_high, PreferencesMessages.TodoTaskInputDialog_priority_normal,
            PreferencesMessages.TodoTaskInputDialog_priority_low};

        fPriorityDialogField = new ComboDialogField(SWT.READ_ONLY);
        fPriorityDialogField.setLabelText(PreferencesMessages.TodoTaskInputDialog_priority_label);
        fPriorityDialogField.setItems(items);
        if (task != null) {
            if (CoreOptionIDs.COMPILER_TASK_PRIORITY_HIGH.equals(task.priority)) {
                fPriorityDialogField.selectItem(0);
            } else if (CoreOptionIDs.COMPILER_TASK_PRIORITY_NORMAL.equals(task.priority)) {
                fPriorityDialogField.selectItem(1);
            } else {
                fPriorityDialogField.selectItem(2);
            }
        } else {
            fPriorityDialogField.selectItem(1);
        }
    }

    public TodoTask getResult() {
        TodoTask task = new TodoTask();
        task.name = fNameDialogField.getText().trim();
        switch (fPriorityDialogField.getSelectionIndex()) {
            case 0:
                task.priority = CoreOptionIDs.COMPILER_TASK_PRIORITY_HIGH;
                break;
            case 1:
                task.priority = CoreOptionIDs.COMPILER_TASK_PRIORITY_NORMAL;
                break;
            default:
                task.priority = CoreOptionIDs.COMPILER_TASK_PRIORITY_LOW;
                break;
        }
        return task;
    }

    @Override
    protected Control createDialogArea(Composite parent) {
        Composite composite = (Composite)super.createDialogArea(parent);

        Composite inner = new Composite(composite, SWT.NONE);
        GridLayout layout = new GridLayout();
        layout.marginHeight = 0;
        layout.marginWidth = 0;
        layout.numColumns = 2;
        inner.setLayout(layout);

        fNameDialogField.doFillIntoGrid(inner, 2);
        fPriorityDialogField.doFillIntoGrid(inner, 2);

        LayoutUtil.setHorizontalGrabbing(fNameDialogField.getTextControl(null));
        LayoutUtil.setWidthHint(fNameDialogField.getTextControl(null), convertWidthInCharsToPixels(45));

        fNameDialogField.postSetFocusOnDialogField(parent.getDisplay());

        applyDialogFont(composite);
        return composite;
    }

    private void doValidation() {
        StatusInfo status = new StatusInfo();
        String newText = fNameDialogField.getText();
        if (newText.length() == 0) {
            status.setError(PreferencesMessages.TodoTaskInputDialog_error_enterName);
        } else {
            if (newText.indexOf(',') != -1) {
                status.setError(PreferencesMessages.TodoTaskInputDialog_error_comma);
            } else if (fExistingNames.contains(newText)) {
                status.setError(PreferencesMessages.TodoTaskInputDialog_error_entryExists);
            } else if (Character.isWhitespace(newText.charAt(0)) || Character.isWhitespace(newText.charAt(newText.length() - 1))) {
                status.setError(PreferencesMessages.TodoTaskInputDialog_error_noSpace);
            }
        }
        updateStatus(status);
    }

    /*
     * @see org.eclipse.jface.window.Window#configureShell(Shell)
     */
    @Override
    protected void configureShell(Shell newShell) {
        super.configureShell(newShell);
        PlatformUI.getWorkbench().getHelpSystem().setHelp(newShell, CALHelpContextIds.TODO_TASK_INPUT_DIALOG);
    }
}
