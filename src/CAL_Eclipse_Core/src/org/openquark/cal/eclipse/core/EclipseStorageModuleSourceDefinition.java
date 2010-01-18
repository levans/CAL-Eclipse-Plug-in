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
 * EclipseFileModuleSourceDefinition.java
 * Created: Aug 1, 2007
 * By: Andrew Eisenberg
 */
package org.openquark.cal.eclipse.core;

import java.io.InputStream;

import org.eclipse.core.resources.IStorage;
import org.eclipse.core.runtime.CoreException;
import org.openquark.cal.compiler.ModuleName;
import org.openquark.cal.compiler.ModuleSourceDefinition;


/**
 * This class is a ModuleSourceDefinition created from
 * Eclipse IStorage objects
 * @author aeisenberg
 *
 */
class EclipseStorageModuleSourceDefinition extends ModuleSourceDefinition {
    private final IStorage memberStorage;
    private final long timeStamp;

    EclipseStorageModuleSourceDefinition(ModuleName moduleName, IStorage memberFile, long timeStamp) {
        super(moduleName);
        this.memberStorage = memberFile;
        this.timeStamp = timeStamp;
    }

    @Override
    public InputStream getInputStream(org.openquark.cal.services.Status status) {
        try {
            return memberStorage.getContents();

        } catch (CoreException e) {
            // This resource does not exist.
            // This resource is not local.
            // Shouldn't happen since force == true: 
            //    The workspace is not in sync with the corresponding location in the local file system.
            String message = "Exception getting input stream for file: " + memberStorage.getName() + ".";
            status.add(new org.openquark.cal.services.Status(org.openquark.cal.services.Status.Severity.ERROR, message, e));
            return null;
        }
    }

    @Override
    public long getTimeStamp() {
        return timeStamp;
    }

    @Override
    public String getDebugInfo() {
        return "from Eclipse file: " + memberStorage.getName();
    }
}