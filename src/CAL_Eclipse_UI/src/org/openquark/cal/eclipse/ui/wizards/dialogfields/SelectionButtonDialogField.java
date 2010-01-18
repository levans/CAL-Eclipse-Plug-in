/*******************************************************************************
 * Copyright (c) 2006 Business Objects Software Limited and others.
 * All rights reserved. 
 * This file is made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Business Objects Software Limited - initial API and implementation based on Eclipse 3.1.2 code for
 *                             /org.eclipse.jdt.ui/ui/org/eclipse/jdt/internal/ui/wizards/dialogfields/SelectionButtonDialogField.java
 *                           Eclipse source is available at: http://www.eclipse.org/downloads/
 *******************************************************************************/

/*
 * SelectionButtonDialogField.java
 * Creation date: Feb 16, 2006.
 * By: Edward Lam
 */
package org.openquark.cal.eclipse.ui.wizards.dialogfields;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.openquark.cal.eclipse.ui.util.SWTUtil;



/**
 * Dialog Field containing a single button such as a radio or checkbox button.
 * @author Edward Lam
 */
public class SelectionButtonDialogField extends DialogField {
    
    private Button fButton;
    private boolean fIsSelected;
    private DialogField[] fAttachedDialogFields;
    private final int fButtonStyle;
    
    /**
     * Creates a selection button.
     * Allowed button styles: SWT.RADIO, SWT.CHECK, SWT.TOGGLE, SWT.PUSH
     */
    public SelectionButtonDialogField(int buttonStyle) {
        super();
        fIsSelected = false;
        fAttachedDialogFields = null;
        fButtonStyle = buttonStyle;
    }
    
    /**
     * Attaches a field to the selection state of the selection button.
     * The attached field will be disabled if the selection button is not selected.
     */
    public void attachDialogField(DialogField dialogField) {
        attachDialogFields(new DialogField[] { dialogField });
    }
    
    /**
     * Attaches fields to the selection state of the selection button.
     * The attached fields will be disabled if the selection button is not selected.
     */     
    public void attachDialogFields(DialogField[] dialogFields) {
        fAttachedDialogFields = dialogFields;
        for (final DialogField dialogField : dialogFields) {
            dialogField.setEnabled(fIsSelected);
        }
    }       
    
    /**
     * Returns <code>true</code> if the given field is attached to the selection button.
     */
    public boolean isAttached(DialogField editor) {
        if (fAttachedDialogFields != null) {
            for (final DialogField dialogField : fAttachedDialogFields) {
                if (dialogField == editor) {
                    return true;
                }
            }
        }
        return false;
    }
    
    // ------- layout helpers
    
    /*
     * @see DialogField#doFillIntoGrid
     */
    @Override
    public Control[] doFillIntoGrid(Composite parent, int nColumns) {
        assertEnoughColumns(nColumns);
        
        Button button = getSelectionButton(parent);
        GridData gd = new GridData();
        gd.horizontalSpan = nColumns;
        gd.horizontalAlignment = GridData.FILL;
        if (fButtonStyle == SWT.PUSH) {
            gd.widthHint = SWTUtil.getButtonWidthHint(button);
        }                       
        
        button.setLayoutData(gd);
        
        return new Control[] { button };
    }       
    
    /*
     * @see DialogField#getNumberOfControls
     */     
    @Override
    public int getNumberOfControls() {
        return 1;       
    }       
    
    // ------- ui creation                  
    
    /**
     * Returns the selection button widget. When called the first time, the widget will be created.
     * @param group The parent composite when called the first time, or <code>null</code>
     * after.
     */             
    public Button getSelectionButton(Composite group) {
        if (fButton == null) {
            assertCompositeNotNull(group);
            
            fButton = new Button(group, fButtonStyle);
            fButton.setFont(group.getFont());                       
            fButton.setText(fLabelText);
            fButton.setEnabled(isEnabled());
            fButton.setSelection(fIsSelected);
            fButton.addSelectionListener(new SelectionListener() {
                public void widgetDefaultSelected(SelectionEvent e) {
                    doWidgetSelected(e);
                }
                public void widgetSelected(SelectionEvent e) {
                    doWidgetSelected(e);
                }
            });                             
        }
        return fButton;
    }
    
    private void doWidgetSelected(SelectionEvent e) {
        if (isOkToUse(fButton)) {
            changeValue(fButton.getSelection());
        }
    }       
    
    private void changeValue(boolean newState) {
        if (fIsSelected != newState) {
            fIsSelected = newState;
            if (fAttachedDialogFields != null) {
                boolean focusSet = false;
                for (final DialogField dialogField : fAttachedDialogFields) {
                    dialogField.setEnabled(fIsSelected);
                    if (fIsSelected && !focusSet) {
                        focusSet = dialogField.setFocus();
                    }
                }
            }
            dialogFieldChanged();
        } else if (fButtonStyle == SWT.PUSH) {
            dialogFieldChanged();
        }
    }               
    
    /* (non-Javadoc)
     * @see org.eclipse.jdt.internal.ui.wizards.dialogfields.DialogField#setLabelText(java.lang.String)
     */
    @Override
    public void setLabelText(String labeltext) {
        fLabelText = labeltext;
        if (isOkToUse(fButton)) {
            fButton.setText(labeltext);
        }
    }
    
    
    // ------ model access  
    
    /**
     * Returns the selection state of the button.
     */
    public boolean isSelected() {
        return fIsSelected;
    }
    
    /**
     * Sets the selection state of the button.
     */     
    public void setSelection(boolean selected) {
        changeValue(selected);
        if (isOkToUse(fButton)) {
            fButton.setSelection(selected);
        }
    }
    
    // ------ enable / disable management
    
    /*
     * @see DialogField#updateEnableState
     */     
    @Override
    protected void updateEnableState() {
        super.updateEnableState();
        if (isOkToUse(fButton)) {
            fButton.setEnabled(isEnabled());
        }               
    }
    
    /*(non-Javadoc)
     * @see org.eclipse.jdt.internal.ui.wizards.dialogfields.DialogField#refresh()
     */
    @Override
    public void refresh() {
        super.refresh();
        if (isOkToUse(fButton)) {
            fButton.setSelection(fIsSelected);
        }
    }
    
    
}
