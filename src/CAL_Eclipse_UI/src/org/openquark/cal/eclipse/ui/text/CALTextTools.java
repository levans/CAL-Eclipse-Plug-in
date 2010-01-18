/*******************************************************************************
 * Copyright (c) 2006 Business Objects Software Limited and others.
 * All rights reserved. 
 * This file is made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Business Objects Software Limited - initial API and implementation based on Eclipse 3.1.2 code for
 *                             /org.eclipse.jdt.ui/ui/org/eclipse/jdt/ui/text/JavaTextTools.java
 *                           Eclipse source is available at: http://www.eclipse.org/downloads/
 *******************************************************************************/

/*
 * CALTextTools.java
 * Creation date: Feb 7, 2006.
 * By: Edward Lam
 */
package org.openquark.cal.eclipse.ui.text;

import org.eclipse.core.runtime.Preferences;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IDocumentExtension3;
import org.eclipse.jface.text.IDocumentPartitioner;
import org.eclipse.jface.text.rules.FastPartitioner;
import org.eclipse.jface.text.rules.IPartitionTokenScanner;



/**
 * Tools required to configure a cal text viewer.
 * The color manager and all scanner exist only one time, i.e. the same instances are returned to all clients. 
 * Thus, clients share those tools.
 * <p>
 * This class may be instantiated; it is not intended to be subclassed.
 * </p>
 * 
 * @author Edward Lam
 */
public class CALTextTools {
    
    /**
     * Array with legal content types.
     * @since 3.0
     */
    private final static String[] LEGAL_CONTENT_TYPES = new String[]{
        CALPartitions.CAL_DOC,
        CALPartitions.CAL_MULTI_LINE_COMMENT,
        CALPartitions.CAL_SINGLE_LINE_COMMENT,
        CALPartitions.CAL_STRING,
        CALPartitions.CAL_CHARACTER
    };
    
    
    /** The color manager. */
    private ColorManager fColorManager;
    /** The Java partitions scanner. */
    private FastCALPartitionScanner fPartitionScanner;
    /** The preference store. */
    private IPreferenceStore fPreferenceStore;

    /**
     * The core preference store.
     */
    private Preferences fCorePreferenceStore;
    
    
    /**
     * Creates a new Java text tools collection.
     *
     * @param store the preference store to initialize the text tools. The text tool
     *                      instance installs a listener on the passed preference store to adapt itself to
     *                      changes in the preference store. In general <code>PreferenceConstants.
     *                      getPreferenceStore()</code> should be used to initialize the text tools.
     * @see org.openquark.cal.eclipse.ui.preferences.PreferenceConstants#getPreferenceStore()
     * @since 2.0
     */
    public CALTextTools(IPreferenceStore store) {
        this(store, null, true);
    }
    
    /**
     * Creates a new Java text tools collection.
     *
     * @param store the preference store to initialize the text tools. The text tool
     *                      instance installs a listener on the passed preference store to adapt itself to
     *                      changes in the preference store. In general <code>PreferenceConstants.
     *                      getPreferenceStore()</code> should be used to initialize the text tools.
     * @param autoDisposeOnDisplayDispose if <code>true</code>  the color manager
     *                      automatically disposes all managed colors when the current display gets disposed
     *                      and all calls to {@link org.eclipse.jface.text.source.ISharedTextColors#dispose()} are ignored.
     * @see org.openquark.cal.eclipse.ui.preferences.PreferenceConstants#getPreferenceStore()
     * @since 2.1
     */
    public CALTextTools(IPreferenceStore store, boolean autoDisposeOnDisplayDispose) {
        this(store, null, autoDisposeOnDisplayDispose);
    }
    
    /**
     * Creates a new Java text tools collection.
     * @param store the preference store to initialize the text tools. The text tool
     *                      instance installs a listener on the passed preference store to adapt itself to
     *                      changes in the preference store. In general <code>PreferenceConstants.
     *                      getPreferenceStore()</code> should be used to initialize the text tools.
     * @param coreStore optional preference store to initialize the text tools. The text tool
     *                      instance installs a listener on the passed preference store to adapt itself to
     *                      changes in the preference store.
     * @see org.openquark.cal.eclipse.ui.preferences.PreferenceConstants#getPreferenceStore()
     * @since 2.1
     */
    public CALTextTools(IPreferenceStore store, Preferences coreStore) {
        this(store, coreStore, true);
    }
    
    /**
     * Creates a new Java text tools collection.
     *
     * @param store the preference store to initialize the text tools. The text tool
     *                      instance installs a listener on the passed preference store to adapt itself to
     *                      changes in the preference store. In general <code>PreferenceConstants.
     *                      getPreferenceStore()</code> should be used to initialize the text tools.
     * @param coreStore optional preference store to initialize the text tools. The text tool
     *                      instance installs a listener on the passed preference store to adapt itself to
     *                      changes in the preference store.
     * @param autoDisposeOnDisplayDispose   if <code>true</code>  the color manager
     *                      automatically disposes all managed colors when the current display gets disposed
     *                      and all calls to {@link org.eclipse.jface.text.source.ISharedTextColors#dispose()} are ignored.
     * @see org.openquark.cal.eclipse.ui.preferences.PreferenceConstants#getPreferenceStore()
     * @since 2.1
     */
    public CALTextTools(IPreferenceStore store, Preferences coreStore, boolean autoDisposeOnDisplayDispose) {
        fPreferenceStore = store;
        fCorePreferenceStore = coreStore;

        fColorManager = new ColorManager(autoDisposeOnDisplayDispose);
        fPartitionScanner = new FastCALPartitionScanner();
    }
    
    /**
     * Disposes all the individual tools of this tools collection.
     */
    public void dispose() {
        
        fPartitionScanner = null;

        if (fColorManager != null) {
            fColorManager.dispose();
            fColorManager = null;
        }

        if (fPreferenceStore != null) {
            fPreferenceStore = null;

            if (fCorePreferenceStore != null) {
                fCorePreferenceStore = null;
            }
        }
    }
    
    /**
     * Returns the color manager which is used to manage any CAL-specific colors needed for such things like syntax highlighting.
     * 
     * @return the color manager to be used for Java text viewers
     */
    public ColorManager getColorManager() {
        return fColorManager;
    }
    
    /**
     * Returns a scanner which is configured to scan
     * Java-specific partitions, which are multi-line comments,
     * Javadoc comments, and regular Java source code.
     *
     * @return a Java partition scanner
     */
    public IPartitionTokenScanner getPartitionScanner() {
        return fPartitionScanner;
    }
    
    /**
     * Factory method for creating a Java-specific document partitioner
     * using this object's partitions scanner. This method is a
     * convenience method.
     *
     * @return a newly created Java document partitioner
     */
    public IDocumentPartitioner createDocumentPartitioner() {
        return new FastPartitioner(getPartitionScanner(), LEGAL_CONTENT_TYPES);
    }
    
    /**
     * Sets up the Java document partitioner for the given document for the default partitioning.
     *
     * @param document the document to be set up
     * @since 3.0
     */
    public void setupJavaDocumentPartitioner(IDocument document) {
        setupCALDocumentPartitioner(document, IDocumentExtension3.DEFAULT_PARTITIONING);
    }
    
    /**
     * Sets up the Java document partitioner for the given document for the given partitioning.
     *
     * @param document the document to be set up
     * @param partitioning the document partitioning
     * @since 3.0
     */
    public void setupCALDocumentPartitioner(IDocument document, String partitioning) {
        IDocumentPartitioner partitioner = createDocumentPartitioner();
        partitioner.connect(document);

        if (document instanceof IDocumentExtension3) {
            IDocumentExtension3 extension3 = (IDocumentExtension3)document;
            extension3.setDocumentPartitioner(partitioning, partitioner);
        } else {
            document.setDocumentPartitioner(partitioner);
        }
    }
    
    /**
     * Returns this text tool's preference store.
     *
     * @return the preference store
     * @since 3.0
     */
    protected IPreferenceStore getPreferenceStore() {
        return fPreferenceStore;
    }
    
    /**
     * Returns this text tool's core preference store.
     *
     * @return the core preference store
     * @since 3.0
     */
    protected Preferences getCorePreferenceStore() {
        return fCorePreferenceStore;
    }
}
