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
 * EclipseModuleSourceDefinitionGroup.java
 * Created: Jul 25, 2007
 * By: Andrew Eisenberg
 */
package org.openquark.cal.eclipse.core;

import org.openquark.cal.compiler.ModuleSourceDefinition;
import org.openquark.cal.compiler.ModuleSourceDefinitionGroup;


/**
 * This class is like its super class, but designates a sub-set of its sources
 * to be writable.  The rest are readonly.
 * 
 * The writable sources will be compiled.  Presumably, they live in files in a source
 * folder.  The read only sources presumably live in a jar file
 * @author aeisenberg
 *
 */
public class EclipseModuleSourceDefinitionGroup extends ModuleSourceDefinitionGroup {

    private final ModuleSourceDefinitionGroup writableSubGroup;
    
    /**
     * 
     * @param allSources all sources in the project
     * @param writableSources must be a sub-set of allSources
     */
    public EclipseModuleSourceDefinitionGroup(ModuleSourceDefinition[] allSources, ModuleSourceDefinition[] writableSources) {
        super(allSources);
        writableSubGroup = new ModuleSourceDefinitionGroup(writableSources);
    }
    public ModuleSourceDefinitionGroup getWritableSubGroup() {
        return writableSubGroup;
    }
}
