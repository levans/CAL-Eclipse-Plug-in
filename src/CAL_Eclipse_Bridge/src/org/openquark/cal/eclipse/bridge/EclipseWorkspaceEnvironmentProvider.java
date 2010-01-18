/*
 * Copyright (c) 2007 BUSINESS OBJECTS SOFTWARE LIMITED
 * All rights reserved.
 * 
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 * 
 *     * Redistributions of source code must retain the above copyright notice,
 *       this list of conditions and the following disclaimer.
 *  
 *     * Redistributions in binary form must reproduce the above copyright
 *       notice, this list of conditions and the following disclaimer in the
 *       documentation and/or other materials provided with the distribution.
 *  
 *     * Neither the name of Business Objects nor the names of its contributors
 *       may be used to endorse or promote products derived from this software
 *       without specific prior written permission.
 *  
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 */


/*
 * EclipseWorkspaceEnvironmentProvider.java
 * Created: Jan 6, 2006
 * By: Edward Lam
 */
package org.openquark.cal.eclipse.bridge;

import java.io.File;

import org.openquark.cal.machine.ProgramResourceRepository.Provider;
import org.openquark.cal.runtime.MachineType;
import org.openquark.cal.services.BasicCALWorkspace;
import org.openquark.cal.services.BasicProgramResourcePathRepository;
import org.openquark.cal.services.CALWorkspace;
import org.openquark.cal.services.CALWorkspaceEnvironmentProvider;
import org.openquark.cal.services.NullaryEnvironment;
import org.openquark.cal.services.ProgramResourceManager;
import org.openquark.cal.services.ResourceManager;
import org.openquark.cal.services.ResourcePath;
import org.openquark.util.FileSystemHelper;



/**
 * A workspace provider which provides CALWorkspaces of type EclipseCALWorkspace.
 * @author Edward Lam
 */
public class EclipseWorkspaceEnvironmentProvider implements CALWorkspaceEnvironmentProvider {

    /**
     * The name of the subdirectory to go under the system temp directory if no suitable source generation root directory can
     * be found using regular means.
     */
    private static final String TEMPORARY_SOURCE_GENERATION_ROOT_SUBDIRECTORY_NAME = "openquark.cal";

    /**
     * The provider factory for the Eclipse workspace environment.
     * 
     * These are instantiated reflectively when referenced using the system property WorkspaceConfiguration.PROVIDER_FACTORY_PROPERTY.
     * 
     * @author Edward Lam
     */
    public static class Factory implements CALWorkspaceEnvironmentProvider.Factory {
        public CALWorkspaceEnvironmentProvider createCALWorkspaceProvider(String workspaceID) {
            return new EclipseWorkspaceEnvironmentProvider(workspaceID);
        }
    }
    
    /** The discrete workspace id, or null for a nullary workspace. */
    private final String workspaceID;
    
    /** 
     * A cal workspace with the given workspace id, or null if getCALWorkspace() has not been called.
     * Lazily instantiated by getCALWorkspace(). 
     */
    private CALWorkspace calWorkspace = null;

    /**
     * Constructor for a EclipseWorkspaceProvider.
     * @param workspaceID the discrete workspace id, or null for a nullary workspace.
     */
    EclipseWorkspaceEnvironmentProvider(String workspaceID) {
        this.workspaceID = workspaceID;
    }
    
    /**
     * @return the default root folder for source generation.
     */
    private File getDefaultSourceGenerationRoot() {
        return NullaryEnvironment.getNullaryEnvironment().getFile(ResourcePath.EMPTY_PATH, false);
    }

    /**
     * {@inheritDoc}
     */
    public CALWorkspace getCALWorkspace() {
        if (calWorkspace == null) {
            if (workspaceID == null) {
                calWorkspace = BasicCALWorkspace.Nullary.makeNullaryWorkspace();
            } else {
                calWorkspace = BasicCALWorkspace.Discrete.makeDiscreteWorkspace(workspaceID);
            }
        }
        return calWorkspace;
    }

    /**
     * {@inheritDoc}
     */
    public Provider getDefaultProgramResourceRepositoryProvider() {
        File defaultSourceGenerationRoot = getDefaultSourceGenerationRoot();
        if (defaultSourceGenerationRoot == null) {
            defaultSourceGenerationRoot = getTemporarySourceGenerationRoot();
        }
        return BasicProgramResourcePathRepository.getResourceRepositoryProvider(defaultSourceGenerationRoot);
    }

    /**
     * {@inheritDoc}
     */
    public ResourceManager getDefaultProgramResourceManager(MachineType machineType) {
        File defaultSourceGenerationRoot = getDefaultSourceGenerationRoot();
        if (defaultSourceGenerationRoot == null) {
            defaultSourceGenerationRoot = getTemporarySourceGenerationRoot();
        }
        return ProgramResourceManager.FileSystemBased.make(machineType, defaultSourceGenerationRoot);
    }
    
    /**
     * {@inheritDoc}
     */
    public NullaryEnvironment getNullaryEnvironment() {
        return new EclipseNullaryEnvironment();
    }
    
    /**
     * @return a temporary directory for the source generation root.
     */
    private static File getTemporarySourceGenerationRoot() {
        File root = new File(System.getProperty("java.io.tmpdir"), TEMPORARY_SOURCE_GENERATION_ROOT_SUBDIRECTORY_NAME);
        FileSystemHelper.ensureDirectoryExists(root);
        return root;
    }
}
