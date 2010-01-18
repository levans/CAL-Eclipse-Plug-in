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
 * ICALResourceContainer.java
 * Created: Jul 25, 2007
 * By: Andrew Eisenberg
 */
package org.openquark.cal.eclipse.core.builder;

import java.util.Set;

import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IStorage;
import org.eclipse.core.runtime.IPath;
import org.eclipse.jdt.core.IPackageFragment;
import org.eclipse.jdt.core.IPackageFragmentRoot;
import org.openquark.cal.machine.ProgramResourceLocator;

/**
 * This interface represents a container that holds CAL 
 * resources.  The container can be a source folder or 
 * a jar.  It must be on the Java classpath
 * and it must be able to be represented as an IPackageFragmentRoot
 * 
 * @author aeisenberg
 */
public interface ICALResourceContainer {
    
    /**
     * 
     * @return all CAL sources (.cal files) in this
     * this container.
     */
    public Set<IStorage> getCALSources();
    
    /**
     * Typically, only source containers that are 
     * folders are writable, jars are not.
     * 
     * @return true if the CAL resources in this container
     * can be written to false otherwise.
     */
    public boolean isWritable();
    
    /**
     * this returns the representation of the container
     * in the context of the Eclipse Java Model
     * <p>
     * For a jar container, this will correspond to the entire jar file itself.
     * For a folder container, this will correspond to the root source folder.  
     * 
     * @return the root of the package that contains
     * these sources
     */
    public IPackageFragmentRoot getPackageRoot();
    
    /**
     * returns the (project-relative) path to the container
     * if the container is stored outside of the project, 
     * then the path returned is the full path to the root
     * 
     * If the path doesn't already exist, then it is created
     * @return the container path
     */
    public IPath getPath();
    
    /**
     * Gets the storage object referred to by the resourcePath.  
     * 
     * Note that the resourcePath refers to resources in the CAL program, and
     * so live below the CAL resources directory (lecc_runtime/) 
     * 
     * @param resourcePath the path of the IStorage relative to lecc_runtime
     * @param createIfPossible if the IStorage does not exist, should it be created? Only
     * possible if this is a FileCALResourceContainer
     * @return the storage object
     */
    public IStorage getProgramResource(ProgramResourceLocator.File resourcePath, 
            boolean createIfPossible);
    
    /**
     * Gets the package referred to by the folderPath.
     * <p>
     * Note that the folderPath is refers to resources in the CAL program. and 
     * so live below the CAL resources directory (lecc_runtime/).
     * <p>
     * This method returns IPackageFragment, not IFolder because resource containers
     * that refer to jars have no concept of folders
     * <p>
     * For example, if you pass in a folder path corresponding to the Cal.Core.Prelude module, 
     * the returned package fragment will be {root}/lecc_runtime/cal_Cal_core_Prelude/
     * <p>
     * essentially translates a module name to where the resulting .cmi will be.
     * 
     * @param folderPath the path of the package fragment to retrieve relative to lecc_runtime
     * @param createIfPossible if true create the package fragment if it doesn't already exist
     * will be ignored for JarCALResourceContainer 
     * @return the package fragment
     */
    public IPackageFragment getProgramFolder(ProgramResourceLocator.Folder folderPath, 
            boolean createIfPossible);
    
    /**
     * Output folder is calculated by taking the package root and
     * adding the baseProgramResourceFolder
     * <p>
     * An output folder only exists if the container is writable (ie- 
     * this method will return null if this is jar container
     * <p>
     * Typically, this will be {root}/lecc_runtime
     * @return the output folder for this CAL resource container
     */
    public IFolder getOutputFolder();


    /**
     * @return a timestamp corresponding to the last modification of the 
     * underlying resource for this container
     */
    public long getTimeStamp();
}