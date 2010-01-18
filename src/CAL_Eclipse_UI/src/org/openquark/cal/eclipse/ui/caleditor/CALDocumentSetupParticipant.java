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
 * CALDocumentSetupParticipant.java
 * Creation date: Feb 9, 2006.
 * By: Edward Lam
 */
package org.openquark.cal.eclipse.ui.caleditor;

import org.eclipse.core.filebuffers.IDocumentSetupParticipant;
import org.eclipse.jface.text.IDocument;
import org.openquark.cal.eclipse.ui.CALEclipseUIPlugin;
import org.openquark.cal.eclipse.ui.text.CALPartitions;
import org.openquark.cal.eclipse.ui.text.CALTextTools;


/**
 * A participant in the setup of a CAL document.
 * 
 * Sets a CAL partitioner on the given document.
 * Referenced by the org.eclipse.core.filebuffers.documentSetup extension point.
 * 
 * @author Edward Lam
 */
public class CALDocumentSetupParticipant implements IDocumentSetupParticipant {

    /**
     * {@inheritDoc}
     */
    public void setup(IDocument document) {
        CALTextTools tools = CALEclipseUIPlugin.getDefault().getCALTextTools();
        tools.setupCALDocumentPartitioner(document, CALPartitions.CAL_PARTITIONING);
    }

}
