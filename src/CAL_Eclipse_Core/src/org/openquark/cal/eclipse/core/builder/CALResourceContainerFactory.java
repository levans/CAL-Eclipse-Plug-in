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
 * CALResourceContainerFactory.java
 * Created: Jul 25, 2007
 * By: Andrew Eisenberg
 */
package org.openquark.cal.eclipse.core.builder;

import org.eclipse.jdt.core.IPackageFragmentRoot;
import org.eclipse.jdt.core.JavaModelException;
import org.openquark.cal.eclipse.core.util.Util;
import org.openquark.cal.runtime.MachineType;
import org.openquark.cal.services.ProgramResourcePathMapper;

/**
 * This class is in charge of creating CALResourceContainers.
 * There  is one instance of this class for the CALBuilder.  
 * The pathMapper field is passed to all CALResourceContainers as they
 * are instantiated.  So, all of the containers share the same pathMapper
 * object.
 * @author aeisenberg
 *
 */
public class CALResourceContainerFactory {
    
    final private ProgramResourcePathMapper pathMapper;
    
    public CALResourceContainerFactory(MachineType machineType) {
        pathMapper = new ProgramResourcePathMapper(machineType);
    }
    
    public ICALResourceContainer create(IPackageFragmentRoot root) {
        try {
            switch (root.getKind()) {
            case IPackageFragmentRoot.K_SOURCE:
                return new FolderCALResourceContainer(root, pathMapper);
                
            case IPackageFragmentRoot.K_BINARY:
                return new JarCALResourceContainer(root, pathMapper);
                
            }
        } catch (JavaModelException e) {
            Util.log(e, "Error getting resource " + root);
        }
        
        // only if there has been an error
        return new EmptyCALResourceContainer(root);
    }
}
