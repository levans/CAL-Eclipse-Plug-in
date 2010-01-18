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
 * ExampleEditor.java
 * Created: 9-Mar-07
 * By: Rick Cameron
 */


package org.openquark.cal.eclipse.ui.metadataeditor;

import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.ComboViewer;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.ViewerSorter;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Text;
import org.eclipse.swt.widgets.ToolBar;
import org.eclipse.swt.widgets.ToolItem;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.openquark.cal.compiler.CodeAnalyser;
import org.openquark.cal.compiler.CodeQualificationMap;
import org.openquark.cal.compiler.CompilerMessageLogger;
import org.openquark.cal.compiler.MessageLogger;
import org.openquark.cal.compiler.ModuleName;
import org.openquark.cal.eclipse.core.CALModelManager;
import org.openquark.cal.eclipse.ui.CALEclipseUIPlugin;
import org.openquark.cal.eclipse.ui.util.ImageLoader;
import org.openquark.cal.metadata.CALExample;
import org.openquark.cal.metadata.CALExpression;
import org.openquark.cal.module.Cal.Core.CAL_Prelude;



/**
 * @author Rick Cameron
 * 
 */
final class ExampleEditor extends EditorComponent {
    
    interface ExampleEditorListener {
        
        void onRemove (ExampleEditor editor);
        
    }

    private static final int MIN_TEXT_HEIGHT = 150;

    private static final ImageLoader removeImageLoader = new ImageLoader (CALEclipseUIPlugin.getImageDescriptor("/icons/remove.gif")); //$NON-NLS-1$

    private final ExampleEditorListener listener;
    
    private Composite contentPanel;
    
    private Text descriptionText;
    private ComboViewer moduleContextBox;
    private Button runAutomaticallyCheck;

    private Text expressionText; // SWT version
//    private final JTextArea expressionText = new JTextArea (); // Swing version

    private boolean changeNotificationEnabled = true;

    /**
     * Constructor ExampleEditor
     * 
     * @param section
     */
    ExampleEditor (EditorSection section, ExampleEditorListener listener) {
        super (section);
        
        this.listener = listener;
    }

    /**
     * @see org.openquark.cal.eclipse.ui.metadataeditor.EditorComponent#createEditorComponent(org.eclipse.swt.widgets.Composite, org.eclipse.ui.forms.widgets.FormToolkit)
     */
    @Override
    Control createEditorComponent (Composite parent, FormToolkit formToolkit) {
        contentPanel = formToolkit.createComposite (parent);
        contentPanel.setData(FormToolkit.KEY_DRAW_BORDER, FormToolkit.TREE_BORDER);
        GridLayoutFactory.swtDefaults ().numColumns (3).applyTo (contentPanel);
        formToolkit.paintBordersFor (contentPanel);
        
        // 0, 0
        formToolkit.createLabel (contentPanel, MetadataEditorMessages.Description);
        
        // 1, 0
        descriptionText = formToolkit.createText (contentPanel, ""); //$NON-NLS-1$
        GridDataFactory.swtDefaults ().align (SWT.FILL, SWT.CENTER).grab (true, false).applyTo (descriptionText);
        
        // 2, 0
        ToolBar toolBar = new ToolBar (contentPanel, SWT.FLAT);
        ToolItem toolItem = new ToolItem (toolBar, SWT.PUSH);
        toolItem.setImage (removeImageLoader.getImage ());
        toolItem.setToolTipText (MetadataEditorMessages.RemoveExampleButtonToolTip);
        toolItem.addSelectionListener (new SelectionAdapter () {
            @Override
            public void widgetSelected (SelectionEvent e) {
                onRemove ();
            }
        });
        
        // 0, 1
        formToolkit.createLabel (contentPanel, MetadataEditorMessages.ExpressionRunInModule);
        
        // 1, 1 - 2, 1
        moduleContextBox = new ComboViewer (contentPanel, SWT.READ_ONLY);
        formToolkit.adapt (moduleContextBox.getCombo (), true, false);
        GridDataFactory
            .swtDefaults ()
            .align (SWT.FILL, SWT.DEFAULT)
            .span (2, 1)
            .grab (true, false)
            .applyTo (moduleContextBox.getCombo ());
        
        moduleContextBox.setContentProvider (new ArrayContentProvider ());
        moduleContextBox.setSorter (new ViewerSorter ());
        moduleContextBox.setInput (CALModelManager.getCALModelManager ().getModuleNames ());
        
        moduleContextBox.addSelectionChangedListener (new ISelectionChangedListener () {
            public void selectionChanged (SelectionChangedEvent event) {
                editorChanged ();
            }
        });
        
        // 0, 2 - 2, 2
        runAutomaticallyCheck = formToolkit.createButton (contentPanel, MetadataEditorMessages.AutoRunExampleCheckBox, SWT.CHECK);
        GridDataFactory.swtDefaults ().span (3, 1).applyTo (runAutomaticallyCheck);
        
        // 0, 3 - 2, 3
        // SWT version
        expressionText = formToolkit.createText (contentPanel, "", SWT.MULTI | SWT.WRAP | SWT.V_SCROLL); //$NON-NLS-1$
        GridDataFactory
            .swtDefaults ()
            .span (3, 1)
            .align (SWT.FILL, SWT.FILL)
            .hint (SWT.DEFAULT, MIN_TEXT_HEIGHT)
            .grab (true, false)
            .applyTo (expressionText);
        
        expressionText.addModifyListener (new ModifyListener () {
            public void modifyText (ModifyEvent e) {
                editorChanged ();
            }
        });

        // Swing version
//        Composite frameHolder = formToolkit.createComposite (contentPanel, SWT.EMBEDDED);
//        frameHolder.addFocusListener (new FocusAdapter () {
//        
//            public void focusGained (FocusEvent e) {
//                SwingUtilities.invokeLater (new Runnable () {
//                    public void run () {
//                        expressionText.requestFocus ();
//                    }
//                });
//            }
//        
//        });
//        
//        GridDataFactory
//            .swtDefaults ()
//            .span (3, 1)
//            .align (SWT.FILL, SWT.FILL)
//            .hint (SWT.DEFAULT, MIN_TEXT_HEIGHT)
//            .grab (true, false)
//            .applyTo (frameHolder);
//        Frame frame = SWT_AWT.new_Frame (frameHolder);
//        
//        expressionText.addMouseListener (new MouseAdapter () {
//        
//            public void mousePressed (MouseEvent e) {
//                if (SwingUtilities.isEventDispatchThread ())
//                    expressionText.requestFocus ();
//                else
//                    SwingUtilities.invokeLater (new Runnable () {
//                        public void run () {
//                            expressionText.requestFocus ();
//                        }
//                    });
//            }
//        
//        });
//        
//        frame.add (new JScrollPane (expressionText));
//        
//        expressionText.getDocument ().addDocumentListener (new DocumentListener () {
//        
//            public void removeUpdate (DocumentEvent e) {
//                postEditorChanged ();
//            }
//
//            public void insertUpdate (DocumentEvent e) {
//                postEditorChanged ();
//            }
//        
//            public void changedUpdate (DocumentEvent e) {
//                postEditorChanged ();
//            }
//        
//            private void postEditorChanged () {
//                if (changeNotificationEnabled)
//                    Display.getDefault ().asyncExec (new Runnable () {
//                        public void run () {
//                            editorChanged ();
//                        }
//                    });
//            }
//            
//        });
        
        return contentPanel;
    }

    /**
     * Method onRemove
     * 
     */
    private void onRemove () {
        listener.onRemove (this);
    }

    /**
      * {@inheritDoc}
     */
    @Override
    public Object getValue () {
        CALExpression expression = getCALExpression ();
        
        String description = descriptionText.getText ();
        boolean evaluateExample = runAutomaticallyCheck.getSelection ();
        
        return new CALExample (expression, description, evaluateExample);
    }
    
    /**
     * Method getCALExpression
     * 
     * @return Returns the {@link CALExpression}
     */
    private CALExpression getCALExpression () {
        ModuleName selectedModuleName = getSelectedModule ();
        
        String unqualifiedText = expressionText.getText ();
        
        if (selectedModuleName != null) {
            CodeAnalyser codeAnalyser = CALModelManager.getCALModelManager ().getCodeAnalyser (selectedModuleName);
            
            CompilerMessageLogger messageLogger = new MessageLogger();
            CodeAnalyser.QualificationResults qualificationResults = codeAnalyser.qualifyExpression(unqualifiedText, null, null, messageLogger);
            
            CodeQualificationMap qualificationMap;
            String qualifiedExpressionText;
            
            if (qualificationResults == null) {
                qualificationMap = new CodeQualificationMap ();
                qualifiedExpressionText = unqualifiedText;
                
                System.err.println (messageLogger.getCompilerMessages ());
            } else {
                qualificationMap = qualificationResults.getQualificationMap ();
                qualifiedExpressionText = qualificationResults.getQualifiedCode ();
            }
                
            return new CALExpression (selectedModuleName, unqualifiedText, qualificationMap, qualifiedExpressionText);
        }
        
        return makeDefaultCALExpression ();
    }

    /**
     * Method makeDefaultCALExpression
     * 
     * @return Returns a default {@link CALExpression}
     */
    private CALExpression makeDefaultCALExpression () {
        return new CALExpression (CAL_Prelude.MODULE_NAME, "", new CodeQualificationMap (), ""); //$NON-NLS-1$ //$NON-NLS-2$
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setValue (Object value) {
        CALExample example = (CALExample)value;

        if (example != null) {
            String description = example.getDescription ();
            
            descriptionText.setText (description == null ? "" : description); //$NON-NLS-1$
            runAutomaticallyCheck.setSelection (example.evaluateExample ());
            
            setCALExpression (example.getExpression ());
        } else {
            descriptionText.setText (""); //$NON-NLS-1$
            runAutomaticallyCheck.setSelection (true);
            
            setCALExpression (null);
        }
    }

    /**
     * Method setCALExpression
     * 
     * @param expression 
     */
    private void setCALExpression (CALExpression expression) {
        if (expression != null) {
            selectModule (expression.getModuleContext ());
            setExpressionText (expression.getExpressionText ());
        } else {
            selectModule (CAL_Prelude.MODULE_NAME);
            setExpressionText (""); //$NON-NLS-1$
        }
    }

    /**
     * Method setExpressionText
     * @param string
     *
     */
    private void setExpressionText (String string) {
        changeNotificationEnabled = false;
        expressionText.setText (string);
        changeNotificationEnabled = true;
    }

    private void selectModule (ModuleName moduleName) {
        moduleContextBox.setSelection (new StructuredSelection (moduleName));
    }
    
    private ModuleName getSelectedModule () {
        ISelection selection = moduleContextBox.getSelection ();
        
        if (selection instanceof IStructuredSelection) {
            return (ModuleName)((IStructuredSelection)selection).getFirstElement ();
        }
        
        return null;
    }

    @Override
    public Control getEditorComponent () {
        return contentPanel;
    }

}
