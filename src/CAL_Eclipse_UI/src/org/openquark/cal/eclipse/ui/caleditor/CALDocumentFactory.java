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
 * CALDocumentFactory.java
 * Creation date: Feb 9, 2006.
 * By: Edward Lam
 */
package org.openquark.cal.eclipse.ui.caleditor;

import org.eclipse.core.filebuffers.IDocumentFactory;
import org.eclipse.jface.text.IDocument;


/**
 * The document factory for the CAL Eclipse UI plugin,
 * @author Edward Lam
 */
public class CALDocumentFactory implements IDocumentFactory {
    
    public CALDocumentFactory() {
    }
    
    /*
     * @see org.eclipse.core.filebuffers.IDocumentFactory#createDocument()
     */
    public IDocument createDocument() {
        return new PartiallySynchronizedDocument();
    }
}
