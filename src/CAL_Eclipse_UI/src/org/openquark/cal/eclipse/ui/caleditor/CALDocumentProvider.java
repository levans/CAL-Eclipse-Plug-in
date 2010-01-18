/*******************************************************************************
 * Copyright (c) 2006 Business Objects Software Limited and others.
 * All rights reserved. 
 * This file is made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Business Objects Software Limited - initial API and implementation
 *******************************************************************************/

/*
 * CALDocumentProvider.java
 * Creation date: Feb 9, 2006.
 * By: Edward Lam
 */
package org.openquark.cal.eclipse.ui.caleditor;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IMarker;
import org.eclipse.jface.text.quickassist.IQuickFixableAnnotation;
import org.eclipse.jface.text.source.Annotation;
import org.eclipse.jface.text.source.IAnnotationModel;
import org.eclipse.ui.editors.text.ForwardingDocumentProvider;
import org.eclipse.ui.editors.text.TextFileDocumentProvider;
import org.eclipse.ui.texteditor.IDocumentProvider;
import org.eclipse.ui.texteditor.MarkerAnnotation;
import org.eclipse.ui.texteditor.ResourceMarkerAnnotationModel;
import org.openquark.cal.compiler.ModuleName;
import org.openquark.cal.eclipse.core.CALModelManager.SourceManagerFactory;
import org.openquark.cal.eclipse.core.builder.CALBuilder;
import org.openquark.cal.eclipse.ui.text.CALPartitions;


/**
 * A document provider for CAL source files.
 * @author Edward Lam
 */
public class CALDocumentProvider extends TextFileDocumentProvider {

    /**
     * A CAL marker for problems. This one is quick fixable otherwise the same as the default marker.
     */
    public static class CALMarkerAnnotation extends MarkerAnnotation implements IQuickFixableAnnotation {
        boolean quickFixableState;
        boolean isQuickFixable;
        public CALMarkerAnnotation(IMarker marker) {
            super(marker);
        }

        public void setQuickFixable(boolean state) {
            isQuickFixable = state;
            quickFixableState = true;
        }

        public boolean isQuickFixableStateSet() {
            return quickFixableState;
        }

        public boolean isQuickFixable() {
            return isQuickFixable;
        }
    }

    @Override
    protected IAnnotationModel createAnnotationModel(IFile file) {
        return new ResourceMarkerAnnotationModel(file) {

            @Override
            protected MarkerAnnotation createMarkerAnnotation(IMarker marker) {
                return new CALMarkerAnnotation(marker);
            }
        };
    }

    /**
     * Get quick fixes for the given problem if any.
     */
    public static CALBuilder.IQuickFix[] getQuickFixes(Annotation annotation, ModuleName moduleName, SourceManagerFactory smf){
        if (annotation instanceof CALMarkerAnnotation){
            CALMarkerAnnotation cma = (CALMarkerAnnotation) annotation;
            return CALBuilder.getQuickFixes(cma.getMarker(), moduleName, smf);
        }
        else{
            return new CALBuilder.IQuickFix[0];
        }
    }
    
    /**
     * Get quick fixes for the given problem if any.
     */
    public static boolean canFix(Annotation annotation){
        if (annotation instanceof CALMarkerAnnotation){
            CALMarkerAnnotation cma = (CALMarkerAnnotation) annotation;
            return CALBuilder.canFix(cma.getMarker());
        }
        else{
            return false;
        }
    }
    
    /**
     * Constructor for a CALDocumentProvider.
     */
    public CALDocumentProvider() {
        IDocumentProvider provider = new TextFileDocumentProvider();
        provider = new ForwardingDocumentProvider(CALPartitions.CAL_PARTITIONING, new CALDocumentSetupParticipant(), provider);
        setParentDocumentProvider(provider);

        /*
         * TODOEL
         */

//        fGlobalAnnotationModelListener = new GlobalAnnotationModelListener();
//        fPropertyListener = new IPropertyChangeListener() {
//
//            public void propertyChange(PropertyChangeEvent event) {
//                if (HANDLE_TEMPORARY_PROBLEMS.equals(event.getProperty()))
//                    enableHandlingTemporaryProblems();
//            }
//        };
//        JavaPlugin.getDefault().getPreferenceStore().addPropertyChangeListener(fPropertyListener);
    }

}
