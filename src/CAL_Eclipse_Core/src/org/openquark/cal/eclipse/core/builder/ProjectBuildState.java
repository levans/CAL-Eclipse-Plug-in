/*******************************************************************************
 * Copyright (c) 2007 Business Objects SA and others.
 * All rights reserved. 
 * This file is made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Business Objects SA - initial API and implementation.
 *******************************************************************************/

/*
 * ProjectBuildState.java
 * Created: May 17, 2007
 * By: Edward Lam
 */

package org.openquark.cal.eclipse.core.builder;

import org.openquark.cal.eclipse.core.EclipseModuleSourceDefinitionGroup;


/**
 * The recorded CAL-specific build state for an Eclipse project.
 * @author Edward Lam
 */
public class ProjectBuildState {

    private final String projectName;
    private final EclipseModuleSourceDefinitionGroup moduleSourceDefinitionGroup;
    
    
    /**
     * @param calBuilder the calBuilder for which the build state should be captured.
     */
    ProjectBuildState(CALBuilder calBuilder) {
        this.projectName = calBuilder.getProject().getName();
        this.moduleSourceDefinitionGroup = calBuilder.getModuleSourceDefinitionGroup();
    }

    /**
     * @return the name of the project for which the state was recorded.
     */
    public String getProjectName() {
        return projectName;
    }
    
    /**
     * @return the ModuleSourceDefinitionGroup for the project at the time the state was recorded.
     */
    public EclipseModuleSourceDefinitionGroup getModuleSourceDefinitionGroup() {
        return moduleSourceDefinitionGroup;
    }

}
