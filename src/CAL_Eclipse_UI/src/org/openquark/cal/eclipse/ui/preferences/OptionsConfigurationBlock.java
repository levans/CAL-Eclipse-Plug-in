/*******************************************************************************
 * Copyright (c) 2006 Business Objects Software Limited and others.
 * All rights reserved. 
 * This file is made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Business Objects Software Limited - initial API and implementation based on Eclipse 3.1.2 code for
 *                             /org.eclipse.jdt.ui/ui/org/eclipse/jdt/internal/ui/preferences/OptionsConfigurationBlock.java
 *                           Eclipse source is available at: http://www.eclipse.org/downloads/
 *******************************************************************************/

/*
 * OptionsConfigurationBlock.java
 * Creation date: Feb 15, 2006.
 * By: Edward Lam
 */
package org.openquark.cal.eclipse.ui.preferences;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.ProjectScope;
import org.eclipse.core.runtime.preferences.DefaultScope;
import org.eclipse.core.runtime.preferences.IEclipsePreferences;
import org.eclipse.core.runtime.preferences.IScopeContext;
import org.eclipse.core.runtime.preferences.InstanceScope;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.IDialogSettings;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Scrollable;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.eclipse.swt.widgets.Widget;
import org.eclipse.ui.forms.events.ExpansionAdapter;
import org.eclipse.ui.forms.events.ExpansionEvent;
import org.eclipse.ui.forms.widgets.ExpandableComposite;
import org.eclipse.ui.preferences.IWorkbenchPreferenceContainer;
import org.eclipse.ui.preferences.IWorkingCopyManager;
import org.eclipse.ui.preferences.WorkingCopyManager;
import org.openquark.cal.eclipse.core.CALEclipseCorePlugin;
import org.openquark.cal.eclipse.ui.CALEclipseUIPlugin;
import org.openquark.cal.eclipse.ui.util.CoreUtility;
import org.openquark.cal.eclipse.ui.wizards.IStatusChangeListener;
import org.osgi.service.prefs.BackingStoreException;


/**
 * Abstract options configuration block providing a general implementation for setting up
 * an options configuration page.
 * 
 * getFullBuildDialogStrings() - return null if a full build is not required on settings change.
 * 
 * @author Edward Lam
 */
public abstract class OptionsConfigurationBlock {
    /*
     * TODOEL: If an option change mandates recompilation, don't recompile Java files (only do cal files).
     */
    
    public static final class Key {
        
        private final String fQualifier;
        private final String fKey;
        
        public Key(String qualifier, String key) {
            fQualifier = qualifier;
            fKey = key;
        }
        
        public String getName() {
            return fKey;
        }
        
        private IEclipsePreferences getNode(IScopeContext context, IWorkingCopyManager manager) {
            IEclipsePreferences node = context.getNode(fQualifier);
            if (manager != null) {
                return manager.getWorkingCopy(node);
            }
            return node;
        }
        
        public String getStoredValue(IScopeContext context, IWorkingCopyManager manager) {
            return getNode(context, manager).get(fKey, null);
        }
        
        public String getStoredValue(IScopeContext[] lookupOrder, boolean ignoreTopScope, IWorkingCopyManager manager) {
            for (int i = ignoreTopScope ? 1 : 0; i < lookupOrder.length; i++) {
                String value = getStoredValue(lookupOrder[i], manager);
                if (value != null) {
                    return value;
                }
            }
            return null;
        }
        
        public void setStoredValue(IScopeContext context, String value, IWorkingCopyManager manager) {
            if (value != null) {
                getNode(context, manager).put(fKey, value);
            } else {
                getNode(context, manager).remove(fKey);
            }
        }
        
        /* (non-Javadoc)
         * @see java.lang.Object#toString()
         */
        @Override
        public String toString() {
            return fQualifier + '/' + fKey;
        }
        
        public String getQualifier() {
            return fQualifier;
        }
        
    }
    
    
    protected static class ControlData {

        private final Key fKey;
        private final String[] fValues;

        public ControlData(Key key, String[] values) {
            fKey = key;
            fValues = values;
        }

        public Key getKey() {
            return fKey;
        }

        public String getValue(boolean selection) {
            int index = selection ? 0 : 1;
            return fValues[index];
        }

        public String getValue(int index) {
            return fValues[index];
        }

        public int getSelection(String value) {
            if (value != null) {
                for (int i = 0; i < fValues.length; i++) {
                    if (value.equals(fValues[i])) {
                        return i;
                    }
                }
            }
            return fValues.length - 1; // assume the last option is the least severe
        }
    }
    
    private static final String SETTINGS_EXPANDED = "expanded"; //$NON-NLS-1$
    
    protected final ArrayList<Button> fCheckBoxes;
    protected final ArrayList<Combo> fComboBoxes;
    protected final ArrayList<Text> fTextBoxes;
    protected final HashMap<Scrollable, Label> fLabels;         /* Key = Combo or Text box*/
    protected final ArrayList<ExpandableComposite> fExpandedComposites;
    
    private SelectionListener fSelectionListener;
    private ModifyListener fTextModifyListener;
    
    protected IStatusChangeListener fContext;
    protected final IProject fProject; // project or null
    protected final Key[] fAllKeys;
    
    private IScopeContext[] fLookupOrder;
    
    private Shell fShell;
    
    private final IWorkingCopyManager fManager;
    private final IWorkbenchPreferenceContainer fContainer;
    
    private Map<Key, String> fDisabledProjectSettings; // null when project specific settings are turned off

    public OptionsConfigurationBlock(IStatusChangeListener context, IProject project, Key[] allKeys, IWorkbenchPreferenceContainer container) {
        fContext = context;
        fProject = project;
        fAllKeys = allKeys;
        fContainer = container;
        if (container == null) {
            fManager = new WorkingCopyManager();

        } else {
            fManager = container.getWorkingCopyManager();
        }
        
        if (fProject != null) {
            fLookupOrder = new IScopeContext[]{new ProjectScope(fProject), new InstanceScope(), new DefaultScope()};
        } else {
            fLookupOrder = new IScopeContext[]{new InstanceScope(), new DefaultScope()};
        }
        
        testIfOptionsComplete(allKeys);
        if (fProject == null || hasProjectSpecificOptions(fProject)) {
            fDisabledProjectSettings = null;
        } else {
            fDisabledProjectSettings = new IdentityHashMap<Key, String>();
            for (final Key curr : allKeys) {
                fDisabledProjectSettings.put(curr, curr.getStoredValue(fLookupOrder, false, fManager));
            }
        }

        settingsUpdated();

        fCheckBoxes = new ArrayList<Button>();
        fComboBoxes = new ArrayList<Combo>();
        fTextBoxes = new ArrayList<Text>(2);
        fLabels = new HashMap<Scrollable, Label>();
        fExpandedComposites = new ArrayList<ExpandableComposite>();
    }
    
    protected final IWorkbenchPreferenceContainer getPreferenceContainer() {
        return fContainer;
    }
    
    protected static Key getKey(String plugin, String key) {
        return new Key(plugin, key);
    }
    
    protected final static Key getJDTCoreKey(String key) {
        return getKey(CALEclipseCorePlugin.PLUGIN_ID, key);
    }
    
    protected final static Key getJDTUIKey(String key) {
        return getKey(CALEclipseUIPlugin.PLUGIN_ID, key);
    }
    
    
    private void testIfOptionsComplete(Key[] allKeys) {
        for (final Key key : allKeys) {
            if (key.getStoredValue(fLookupOrder, false, fManager) == null) {
                CALEclipseUIPlugin.logErrorMessage("preference option missing: " + key + " (" + this.getClass().getName() + ')'); //$NON-NLS-1$//$NON-NLS-2$
            }
        }
    }
    
    protected void settingsUpdated() {
    }
    
    
    public void selectOption(String key, String qualifier) {
        for (final Key curr : fAllKeys) {
            if (curr.getName().equals(key) && curr.getQualifier().equals(qualifier)) {
                selectOption(curr);
            }
        }
    }
    
    public void selectOption(Key key) {
        Control control = findControl(key);
        if (control != null) {
            if (!fExpandedComposites.isEmpty()) {
                ExpandableComposite expandable = getParentExpandableComposite(control);
                if (expandable != null) {
                    for (int i = 0; i < fExpandedComposites.size(); i++) {
                        ExpandableComposite curr = fExpandedComposites.get(i);
                        curr.setExpanded(curr == expandable);
                    }
                    expandedStateChanged(expandable);
                }
            }
            control.setFocus();
        }
    }
    
    
    public final boolean hasProjectSpecificOptions(IProject project) {
        if (project != null) {
            IScopeContext projectContext = new ProjectScope(project);
            Key[] allKeys = fAllKeys;
            for (final Key key : allKeys) {
                if (key.getStoredValue(projectContext, fManager) != null) {
                    return true;
                }
            }
        }
        return false;
    }       
    
    protected Shell getShell() {
        return fShell;
    }
    
    protected void setShell(Shell shell) {
        fShell= shell;
    }       
    
    protected abstract Control createContents(Composite parent);
    
    protected Button addCheckBox(Composite parent, String label, Key key, String[] values, int indent) {
        ControlData data = new ControlData(key, values);

        GridData gd = new GridData(GridData.HORIZONTAL_ALIGN_FILL);
        gd.horizontalSpan = 3;
        gd.horizontalIndent = indent;

        Button checkBox = new Button(parent, SWT.CHECK);
        checkBox.setFont(JFaceResources.getDialogFont());
        checkBox.setText(label);
        checkBox.setData(data);
        checkBox.setLayoutData(gd);
        checkBox.addSelectionListener(getSelectionListener());

        makeScrollableCompositeAware(checkBox);

        String currValue = getValue(key);
        checkBox.setSelection(data.getSelection(currValue) == 0);

        fCheckBoxes.add(checkBox);

        return checkBox;
    }
    
    protected Combo addComboBox(Composite parent, String label, Key key, String[] values, String[] valueLabels, int indent) {
        GridData gd = new GridData(GridData.FILL, GridData.CENTER, true, false, 2, 1);
        gd.horizontalIndent = indent;

        Label labelControl = new Label(parent, SWT.LEFT);
        labelControl.setFont(JFaceResources.getDialogFont());
        labelControl.setText(label);
        labelControl.setLayoutData(gd);

        Combo comboBox = newComboControl(parent, key, values, valueLabels);
        comboBox.setLayoutData(new GridData(GridData.HORIZONTAL_ALIGN_FILL));

        fLabels.put(comboBox, labelControl);

        return comboBox;
    }
    
    protected Combo addInversedComboBox(Composite parent, String label, Key key, String[] values, String[] valueLabels, int indent) {
        GridData gd = new GridData(GridData.HORIZONTAL_ALIGN_BEGINNING);
        gd.horizontalIndent = indent;
        gd.horizontalSpan = 3;

        Composite composite = new Composite(parent, SWT.NONE);
        GridLayout layout = new GridLayout();
        layout.marginHeight = 0;
        layout.marginWidth = 0;
        layout.numColumns = 2;
        composite.setLayout(layout);
        composite.setLayoutData(gd);

        Combo comboBox = newComboControl(composite, key, values, valueLabels);
        comboBox.setFont(JFaceResources.getDialogFont());
        comboBox.setLayoutData(new GridData(GridData.HORIZONTAL_ALIGN_FILL));

        Label labelControl = new Label(composite, SWT.LEFT | SWT.WRAP);
        labelControl.setText(label);
        labelControl.setLayoutData(new GridData());

        fLabels.put(comboBox, labelControl);
        return comboBox;
    }
    
    protected Combo newComboControl(Composite composite, Key key, String[] values, String[] valueLabels) {
        ControlData data = new ControlData(key, values);

        Combo comboBox = new Combo(composite, SWT.READ_ONLY);
        comboBox.setItems(valueLabels);
        comboBox.setData(data);
        comboBox.addSelectionListener(getSelectionListener());
        comboBox.setFont(JFaceResources.getDialogFont());

        makeScrollableCompositeAware(comboBox);

        String currValue = getValue(key);
        comboBox.select(data.getSelection(currValue));

        fComboBoxes.add(comboBox);
        return comboBox;
    }
    
    protected Text addTextField(Composite parent, String label, Key key, int indent, int widthHint) {
        Label labelControl = new Label(parent, SWT.WRAP);
        labelControl.setText(label);
        labelControl.setFont(JFaceResources.getDialogFont());
        labelControl.setLayoutData(new GridData());

        Text textBox = new Text(parent, SWT.BORDER | SWT.SINGLE);
        textBox.setData(key);
        textBox.setLayoutData(new GridData());

        makeScrollableCompositeAware(textBox);

        fLabels.put(textBox, labelControl);

        String currValue = getValue(key);
        if (currValue != null) {
            textBox.setText(currValue);
        }
        textBox.addModifyListener(getTextModifyListener());

        GridData data = new GridData(GridData.HORIZONTAL_ALIGN_FILL);
        if (widthHint != 0) {
            data.widthHint = widthHint;
        }
        data.horizontalIndent = indent;
        data.horizontalSpan = 2;
        textBox.setLayoutData(data);

        fTextBoxes.add(textBox);
        return textBox;
    }
    
    protected ScrolledPageContent getParentScrolledComposite(Control control) {
        Control parent = control.getParent();
        while (!(parent instanceof ScrolledPageContent) && parent != null) {
            parent = parent.getParent();
        }
        if (parent instanceof ScrolledPageContent) {
            return (ScrolledPageContent)parent;
        }
        return null;
    }
    
    protected ExpandableComposite getParentExpandableComposite(Control control) {
        Control parent = control.getParent();
        while (!(parent instanceof ExpandableComposite) && parent != null) {
            parent = parent.getParent();
        }
        if (parent instanceof ExpandableComposite) {
            return (ExpandableComposite)parent;
        }
        return null;
    }
    
    private void makeScrollableCompositeAware(Control control) {
        ScrolledPageContent parentScrolledComposite = getParentScrolledComposite(control);
        if (parentScrolledComposite != null) {
            parentScrolledComposite.adaptChild(control);
        }
    }
    
    protected ExpandableComposite createStyleSection(Composite parent, String label, int nColumns) {
        ExpandableComposite excomposite = new ExpandableComposite(parent, SWT.NONE, ExpandableComposite.TWISTIE | ExpandableComposite.CLIENT_INDENT);
        excomposite.setText(label);
        excomposite.setExpanded(false);
        excomposite.setFont(JFaceResources.getFontRegistry().getBold(JFaceResources.DIALOG_FONT));
        excomposite.setLayoutData(new GridData(GridData.FILL, GridData.FILL, true, false, nColumns, 1));
        excomposite.addExpansionListener(new ExpansionAdapter() {
            @Override
            public void expansionStateChanged(ExpansionEvent e) {
                expandedStateChanged((ExpandableComposite)e.getSource());
            }
        });
        fExpandedComposites.add(excomposite);
        makeScrollableCompositeAware(excomposite);
        return excomposite;
    }
    
    protected final void expandedStateChanged(ExpandableComposite expandable) {
        ScrolledPageContent parentScrolledComposite = getParentScrolledComposite(expandable);
        if (parentScrolledComposite != null) {
            parentScrolledComposite.reflow(true);
        }
    }
    
    protected void restoreSectionExpansionStates(IDialogSettings settings) {
        for (int i = 0; i < fExpandedComposites.size(); i++) {
            ExpandableComposite excomposite = fExpandedComposites.get(i);
            if (settings == null) {
                excomposite.setExpanded(i == 0); // only expand the first node by default
            } else {
                excomposite.setExpanded(settings.getBoolean(SETTINGS_EXPANDED + String.valueOf(i)));
            }
        }
    }
    
    protected void storeSectionExpansionStates(IDialogSettings settings) {
        for (int i = 0; i < fExpandedComposites.size(); i++) {
            ExpandableComposite curr = fExpandedComposites.get(i);
            settings.put(SETTINGS_EXPANDED + String.valueOf(i), curr.isExpanded());
        }
    }
    
    protected SelectionListener getSelectionListener() {
        if (fSelectionListener == null) {
            fSelectionListener = new SelectionListener() {
                public void widgetDefaultSelected(SelectionEvent e) {
                }

                public void widgetSelected(SelectionEvent e) {
                    controlChanged(e.widget);
                }
            };
        }
        return fSelectionListener;
    }
    
    protected ModifyListener getTextModifyListener() {
        if (fTextModifyListener == null) {
            fTextModifyListener = new ModifyListener() {
                public void modifyText(ModifyEvent e) {
                    textChanged((Text)e.widget);
                }
            };
        }
        return fTextModifyListener;
    }               
    
    protected void controlChanged(Widget widget) {
        ControlData data = (ControlData)widget.getData();
        String newValue = null;
        if (widget instanceof Button) {
            newValue = data.getValue(((Button)widget).getSelection());
        } else if (widget instanceof Combo) {
            newValue = data.getValue(((Combo)widget).getSelectionIndex());
        } else {
            return;
        }
        String oldValue = setValue(data.getKey(), newValue);
        validateSettings(data.getKey(), oldValue, newValue);
    }
    
    protected void textChanged(Text textControl) {
        Key key = (Key)textControl.getData();
        String number = textControl.getText();
        String oldValue = setValue(key, number);
        validateSettings(key, oldValue, number);
    }       
    
    protected boolean checkValue(Key key, String value) {
        return value.equals(getValue(key));
    }
    
    protected String getValue(Key key) {
        if (fDisabledProjectSettings != null) {
            return fDisabledProjectSettings.get(key);
        }
        return key.getStoredValue(fLookupOrder, false, fManager);
    }
    
    
    protected boolean getBooleanValue(Key key) {
        return Boolean.valueOf(getValue(key)).booleanValue();
    }
    
    protected String setValue(Key key, String value) {
        if (fDisabledProjectSettings != null) {
            return fDisabledProjectSettings.put(key, value);
        }
        String oldValue = getValue(key);
        key.setStoredValue(fLookupOrder[0], value, fManager);
        return oldValue;
    }
    
    protected String setValue(Key key, boolean value) {
        return setValue(key, String.valueOf(value));
    }
    
    /**
     * Retuens the value as actually stored in the preference store.
     * @param key
     * @return the value as actually stored in the preference store.
     */
    protected String getStoredValue(Key key) {
        return key.getStoredValue(fLookupOrder, false, fManager);
    }
    
    /* (non-javadoc)
     * Update fields and validate.
     * @param changedKey Key that changed, or null, if all changed.
     */     
    protected abstract void validateSettings(Key changedKey, String oldValue, String newValue);
    
    
    protected String[] getTokens(String text, String separator) {
        StringTokenizer tok = new StringTokenizer(text, separator); 
        int nTokens = tok.countTokens();
        String[] res = new String[nTokens];
        for (int i = 0; i < res.length; i++) {
            res[i] = tok.nextToken().trim();
        }
        return res;
    }
    
    private boolean getChanges(IScopeContext currContext, List<Key> changedSettings) {
        boolean needsBuild = false;
        for (final Key key : fAllKeys) {
            String oldVal = key.getStoredValue(currContext, null);
            String val = key.getStoredValue(currContext, fManager);
            if (val == null) {
                if (oldVal != null) {
                    changedSettings.add(key);
                    needsBuild |= !oldVal.equals(key.getStoredValue(fLookupOrder, true, fManager));
                }
            } else if (!val.equals(oldVal)) {
                changedSettings.add(key);
                needsBuild |= oldVal != null || !val.equals(key.getStoredValue(fLookupOrder, true, fManager));
            }
        }
        return needsBuild;
    }
    
    public void useProjectSpecificSettings(boolean enable) {
        boolean hasProjectSpecificOption = fDisabledProjectSettings == null;
        if (enable != hasProjectSpecificOption && fProject != null) {
            if (enable) {
                for (final Key curr : fAllKeys) {
                    String val = fDisabledProjectSettings.get(curr);
                    curr.setStoredValue(fLookupOrder[0], val, fManager);
                }
                fDisabledProjectSettings = null;
                updateControls();
            } else {
                fDisabledProjectSettings = new IdentityHashMap<Key, String>();
                for (final Key curr : fAllKeys) {
                    String oldSetting = curr.getStoredValue(fLookupOrder, false, fManager);
                    fDisabledProjectSettings.put(curr, oldSetting);
                    curr.setStoredValue(fLookupOrder[0], null, fManager); // clear project settings
                }
            }
        }
    }
    
    
    public boolean performOk() {
        return processChanges(fContainer);
    }
    
    public boolean performApply() {
        return processChanges(null); // apply directly
    }
    
    /**
     * Apply the changes from this block.
     * Perform a full build if required.
     * @param container If non-null, the build job will be registered as an update job on this container.
     * @return whether the processing of changes was successful.
     */
    private boolean processChanges(IWorkbenchPreferenceContainer container) {

        IScopeContext currContext = fLookupOrder[0];

        List<Key> changedOptions = new ArrayList<Key>();
        boolean needsBuild = getChanges(currContext, changedOptions);
        if (changedOptions.isEmpty()) {
            return true;
        }

        boolean doBuild = false;
        if (needsBuild) {
            String[] strings = getFullBuildDialogStrings(fProject == null);
            if (strings != null) {
                MessageDialog dialog = new MessageDialog(getShell(), strings[0], null, strings[1], MessageDialog.QUESTION, 
                        new String[]{IDialogConstants.YES_LABEL, IDialogConstants.NO_LABEL, IDialogConstants.CANCEL_LABEL}, 2);
                int res = dialog.open();
                if (res == Window.OK) {
                    doBuild = true;
                } else if (res != Window.CANCEL) {
                    return false; // cancel pressed
                }
            }
        }
        if (container != null) {
            // no need to apply the changes to the original store: will be done by the page container
            if (doBuild) { // post build
                container.registerUpdateJob(CoreUtility.getBuildJob(fProject, true));
            }
        } else {
            // apply changes right away
            try {
                fManager.applyChanges();
            } catch (BackingStoreException e) {
                CALEclipseUIPlugin.log(e);
                return false;
            }
            if (doBuild) {
                CoreUtility.getBuildJob(fProject, true).schedule();
            }

        }
        return true;
    }
    
    protected abstract String[] getFullBuildDialogStrings(boolean workspaceSettings);
    
    
    public void performDefaults() {
        for (final Key curr : fAllKeys) {
            String defValue = curr.getStoredValue(fLookupOrder, true, fManager);
            setValue(curr, defValue);
        }

        settingsUpdated();
        updateControls();
        validateSettings(null, null, null);
    }
    
    public void performRevert() {
        for (final Key curr : fAllKeys) {
            String origValue = curr.getStoredValue(fLookupOrder, false, null);
            setValue(curr, origValue);
        }

        settingsUpdated();
        updateControls();
        validateSettings(null, null, null);
    }
    
    public void dispose() {
    }
    
    protected void updateControls() {
        // update the UI
        for (int i = fCheckBoxes.size() - 1; i >= 0; i--) {
            updateCheckBox(fCheckBoxes.get(i));
        }
        for (int i = fComboBoxes.size() - 1; i >= 0; i--) {
            updateCombo(fComboBoxes.get(i));
        }
        for (int i = fTextBoxes.size() - 1; i >= 0; i--) {
            updateText(fTextBoxes.get(i));
        }
    }
    
    protected void updateCombo(Combo curr) {
        ControlData data = (ControlData)curr.getData();

        String currValue = getValue(data.getKey());
        curr.select(data.getSelection(currValue));
    }
    
    protected void updateCheckBox(Button curr) {
        ControlData data = (ControlData)curr.getData();

        String currValue = getValue(data.getKey());
        curr.setSelection(data.getSelection(currValue) == 0);
    }
    
    protected void updateText(Text curr) {
        Key key = (Key)curr.getData();

        String currValue = getValue(key);
        if (currValue != null) {
            curr.setText(currValue);
        }
    }
    
    protected Button getCheckBox(Key key) {
        for (int i = fCheckBoxes.size() - 1; i >= 0; i--) {
            Button curr = fCheckBoxes.get(i);
            ControlData data = (ControlData)curr.getData();
            if (key.equals(data.getKey())) {
                return curr;
            }
        }
        return null;
    }
    
    protected Combo getComboBox(Key key) {
        for (int i = fComboBoxes.size() - 1; i >= 0; i--) {
            Combo curr = fComboBoxes.get(i);
            ControlData data = (ControlData)curr.getData();
            if (key.equals(data.getKey())) {
                return curr;
            }
        }
        return null;
    }
    
    protected Text getTextControl(Key key) {
        for (int i = fTextBoxes.size() - 1; i >= 0; i--) {
            Text curr = fTextBoxes.get(i);
            ControlData data = (ControlData)curr.getData();
            if (key.equals(data.getKey())) {
                return curr;
            }
        }
        return null;
    }
    
    protected Control findControl(Key key) {
        Combo comboBox = getComboBox(key);
        if (comboBox != null) {
            return comboBox;
        }
        Button checkBox = getCheckBox(key);
        if (checkBox != null) {
            return checkBox;
        }
        Text text = getTextControl(key);
        if (text != null) {
            return text;
        }
        return null;
    }
    
    protected void setComboEnabled(Key key, boolean enabled) {
        Combo combo = getComboBox(key);
        Label label = fLabels.get(combo);
        combo.setEnabled(enabled);
        label.setEnabled(enabled);
    }
}
