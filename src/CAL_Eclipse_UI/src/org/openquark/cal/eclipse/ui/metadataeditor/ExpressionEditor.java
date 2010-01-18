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
 * ExpressionEditor.java
 * Created: 7-Mar-07
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
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.openquark.cal.compiler.CodeAnalyser;
import org.openquark.cal.compiler.CodeQualificationMap;
import org.openquark.cal.compiler.CompilerMessageLogger;
import org.openquark.cal.compiler.MessageLogger;
import org.openquark.cal.compiler.ModuleName;
import org.openquark.cal.eclipse.core.CALModelManager;
import org.openquark.cal.metadata.CALExpression;
import org.openquark.cal.module.Cal.Core.CAL_Prelude;



/**
 * @author rcameron
 *
 */
final class ExpressionEditor extends EditorComponent {
    
    private static final int MIN_TEXT_HEIGHT = 150;
    
    private Composite contentPanel;
    
    private ComboViewer moduleContextBox;

    private Text expressionText;

    /**
     * Constructor ExpressionEditor
     *
     * @param editorSection
     * @param key
     * @param title
     * @param description
     */
    ExpressionEditor (EditorSection editorSection, String key, String title, String description) {
        super (editorSection, key, title, description);
    }

    /**
     * @see org.openquark.cal.eclipse.ui.metadataeditor.EditorComponent#createEditorComponent(org.eclipse.swt.widgets.Composite, org.eclipse.ui.forms.widgets.FormToolkit)
     */
    @Override
    Control createEditorComponent (Composite parent, FormToolkit formToolkit) {
        contentPanel = formToolkit.createComposite (parent);
        contentPanel.setData(FormToolkit.KEY_DRAW_BORDER, FormToolkit.TREE_BORDER);
        GridLayoutFactory.swtDefaults ().numColumns (2).applyTo (contentPanel);
        formToolkit.paintBordersFor (contentPanel);

        formToolkit.createLabel (contentPanel, MetadataEditorMessages.ExpressionRunInModule);
        
        moduleContextBox = new ComboViewer (contentPanel, SWT.READ_ONLY);
        formToolkit.adapt (moduleContextBox.getCombo (), true, false);
        
        moduleContextBox.setContentProvider (new ArrayContentProvider ());
        moduleContextBox.setSorter (new ViewerSorter ());
        moduleContextBox.setInput (CALModelManager.getCALModelManager ().getModuleNames ());
        
        moduleContextBox.addSelectionChangedListener (new ISelectionChangedListener () {
            public void selectionChanged (SelectionChangedEvent event) {
                editorChanged ();
            }
        });
        
        expressionText = formToolkit.createText (contentPanel, "", SWT.MULTI | SWT.WRAP | SWT.V_SCROLL); //$NON-NLS-1$
        GridDataFactory
            .swtDefaults ()
            .span (2, 1)
            .align (SWT.FILL, SWT.FILL)
            .hint (SWT.DEFAULT, MIN_TEXT_HEIGHT)
            .grab (true, false)
            .applyTo (expressionText);
        
        expressionText.addModifyListener (new ModifyListener () {
            public void modifyText (ModifyEvent e) {
                editorChanged ();
            }
        });
        
        return contentPanel;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Control getEditorComponent () {
        return contentPanel;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Object getValue () {
        ModuleName selectedModuleName = getSelectedModule ();
        
        String unqualifiedText = expressionText.getText ();
        
        if (selectedModuleName != null && unqualifiedText.trim ().length () != 0) {
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
        
        return null;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setValue (Object value) {
        CALExpression expression = (CALExpression) value;
        
        if (expression != null) {
            selectModule (expression.getModuleContext ());
            
            expressionText.setText (expression.getExpressionText ());
        } else {
            selectModule (CAL_Prelude.MODULE_NAME);
            
            expressionText.setText (""); //$NON-NLS-1$
        }
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

}
