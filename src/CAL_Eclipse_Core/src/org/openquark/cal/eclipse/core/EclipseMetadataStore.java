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
 * EclipseMetadataStore.java
 * Created: 19-Feb-07
 * By: Rick Cameron
 */


package org.openquark.cal.eclipse.core;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.FilterOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IStorage;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.jdt.core.IPackageFragmentRoot;
import org.openquark.cal.compiler.ModuleName;
import org.openquark.cal.eclipse.core.builder.ICALResourceContainer;
import org.openquark.cal.eclipse.core.util.Util;
import org.openquark.cal.metadata.MetadataPathMapper;
import org.openquark.cal.metadata.MetadataStore;
import org.openquark.cal.services.CALFeatureName;
import org.openquark.cal.services.FeatureName;
import org.openquark.cal.services.ResourceName;
import org.openquark.cal.services.ResourceStore;
import org.openquark.cal.services.Status;
import org.openquark.cal.services.WorkspaceResource;
import org.openquark.cal.services.ResourcePath.FilePath;



/**
 * @author rcameron
 *
 */
final class EclipseMetadataStore implements MetadataStore {

    /* (non-Javadoc)
     * @see org.openquark.cal.metadata.MetadataStore#getMetadataResourceNamesForAllLocales(org.openquark.cal.services.CALFeatureName)
     */
    public List<ResourceName> getMetadataResourceNamesForAllLocales (CALFeatureName featureName) {
        return Collections.singletonList (new ResourceName (featureName));
    }

    /* (non-Javadoc)
     * @see org.openquark.cal.services.ResourceStore.Module#getModuleNames()
     */
    public Set<ModuleName> getModuleNames () {
        return Collections.emptySet();
    }

    /* (non-Javadoc)
     * @see org.openquark.cal.services.ResourceStore.Module#getModuleResourceNameList(org.openquark.cal.compiler.ModuleName)
     */
    public List<ResourceName> getModuleResourceNameList (ModuleName moduleName) {
        return Collections.emptyList();
    }

    /* (non-Javadoc)
     * @see org.openquark.cal.services.ResourceStore.Module#getResourceIterator(org.openquark.cal.compiler.ModuleName)
     */
    public Iterator<WorkspaceResource> getResourceIterator (ModuleName moduleName) {
        return Collections.<WorkspaceResource>emptyList().iterator();
    }

    /* (non-Javadoc)
     * @see org.openquark.cal.services.ResourceStore.Module#removeModuleResources(org.openquark.cal.compiler.ModuleName, org.openquark.cal.services.Status)
     */
    public void removeModuleResources (ModuleName moduleName, Status removeStatus) {
        // TODO rbc
        removeStatus.add (new Status ("Not implemented"));
    }

    /* (non-Javadoc)
     * @see org.openquark.cal.services.ResourceStore#getDebugInfo(org.openquark.cal.services.ResourceName)
     */
    public String getDebugInfo (ResourceName resourceName) {
        return resourceName.toString ();
    }

    /* (non-Javadoc)
     * @see org.openquark.cal.services.ResourceStore#getInputStream(org.openquark.cal.services.ResourceName)
     */
    public InputStream getInputStream (ResourceName resourceName) {
        IStorage storage = findFile(resourceName);

        if (storage != null /*&& storage.exists()*/) {
            try {
                return storage.getContents();
            } catch (CoreException e) {
                Util.log(e, "Failed to load metadata file");
            }
        }

        return null;
    }
    
    /**
     * Method findFile
     * 
     * @param resourceName
     * 
     * @return Returns the IStorage that corresponds to the given {@link ResourceName}, or null
     */
    private static IStorage findFile(ResourceName resourceName) {
        FeatureName featureName = resourceName.getFeatureName();
        
        if (!(featureName instanceof CALFeatureName)) {
            return null;
        }
        
        CALFeatureName calFeatureName = (CALFeatureName)featureName;
        
        ICALResourceContainer container = CALModelManager.getCALModelManager().getInputSourceFileContainer(
                calFeatureName.toModuleName());
        
        IPackageFragmentRoot root = container.getPackageRoot();
        return ((IContainer) root.getResource()).
                getFile(getMetadataResourcePath (resourceName));
    }

    /**
     * Method getMetadataResourcePath
     *
     * @param resourceName
     * 
     * @return Returns the IPath that represents the location of the metadata for the given resource
     */
    private static IPath getMetadataResourcePath (ResourceName resourceName) {
        FilePath resourcePath = MetadataPathMapper.INSTANCE.getResourcePath (resourceName);
        
        String[] pathElements = resourcePath.getPathElements ();
        
        IPath result = new Path (pathElements[0]);
        
        for (int i = 1; i < pathElements.length; i++) {
            String element = pathElements[i];
            
            result = result.append (element);
        }
        
        return result;
    }
    
    
    // ADE unused delete!!!
//    /**
//     * Method filePathToIPath
//     *
//     * @param filePath
//     * 
//     * @return Returns an IPath representing the given FilePath
//     */
//    private static IPath filePathToIPath (FilePath filePath) {
//        String[] pathElements = filePath.getPathElements ();
//        
//        IPath result = new Path (pathElements[0]);
//        
//        for (int i = 1; i < pathElements.length; i++) {
//            String element = pathElements[i];
//            
//            result = result.append (element);
//        }
//        
//        return result;
//    }

    /* (non-Javadoc)
     * Only gets an output stream if the resource is a file.
     * Returns null if the resource is a jar entry
     * @see org.openquark.cal.services.ResourceStore#getOutputStream(org.openquark.cal.services.ResourceName, org.openquark.cal.services.Status)
     */
    public OutputStream getOutputStream (final ResourceName resourceName, Status status) {
        IStorage storage = findFile (resourceName);
        
        if (storage == null || (storage instanceof IFile)) {
            return null;
        }
        final IFile file = (IFile) storage;
        
        final ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream ();
        
        FilterOutputStream outputStream = new FilterOutputStream (byteArrayOutputStream) {
            @Override
            public void close () throws IOException {
                super.close ();
                
                // now we can write the data to the real file...
                byte[] data = byteArrayOutputStream.toByteArray ();
                
                ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream (data);
                
                try {
                    if (file.exists()) {
                        file.setContents(byteArrayInputStream, true, false, null);
                    } else {
                        IFolder parent = (IFolder) file.getParent();
                        Util.createFolder(parent, true, false, null);
                        file.create(byteArrayInputStream, true, null);
                    }
                } catch (CoreException e) {
                    e.printStackTrace();
                    
                    throw new IOException (e.getLocalizedMessage ());
                } finally {
                    byteArrayInputStream.close ();
                }
            }
        };
        
        return outputStream;
    }

    /* (non-Javadoc)
     * @see org.openquark.cal.services.ResourceStore#getResourceIterator()
     */
    public Iterator<WorkspaceResource> getResourceIterator () {
        return Collections.<WorkspaceResource>emptyList().iterator();
    }

    /* (non-Javadoc)
     * @see org.openquark.cal.services.ResourceStore#getResourceType()
     */
    public String getResourceType () {
        return "EclipseWorkspaceResource";
    }

    /* (non-Javadoc)
     * @see org.openquark.cal.services.ResourceStore#getTimeStamp(org.openquark.cal.services.ResourceName)
     */
    public long getTimeStamp (ResourceName resourceName) {
        return 0;
    }

    /* (non-Javadoc)
     * @see org.openquark.cal.services.ResourceStore#hasFeature(org.openquark.cal.services.ResourceName)
     */
    public boolean hasFeature (ResourceName resourceName) {
        // TODO rbc
        return false;
    }

    /* (non-Javadoc)
     * @see org.openquark.cal.services.ResourceStore#isRemovable(org.openquark.cal.services.ResourceName)
     */
    public boolean isRemovable (ResourceName resourceName) {
        // it's removable if it exists
        return hasFeature (resourceName);
    }

    /* (non-Javadoc)
     * @see org.openquark.cal.services.ResourceStore#isWriteable()
     */
    public boolean isWriteable () {
        return true;
    }

    /* (non-Javadoc)
     * @see org.openquark.cal.services.ResourceStore#isWriteable(org.openquark.cal.services.ResourceName)
     */
    public boolean isWriteable (ResourceName resourceName) {
        // it's writeable if it exists
        return hasFeature (resourceName);
    }

    /* (non-Javadoc)
     * @see org.openquark.cal.services.ResourceStore#removeAllResources(org.openquark.cal.services.Status)
     */
    public void removeAllResources (Status removeStatus) {
        // TODO rbc
        removeStatus.add (new Status ("Not implemented"));
    }

    /* (non-Javadoc)
     * @see org.openquark.cal.services.ResourceStore#removeResource(org.openquark.cal.services.ResourceName, org.openquark.cal.services.Status)
     */
    public void removeResource (ResourceName resourceName, Status removeStatus) {
        // TODO rbc
        removeStatus.add (new Status ("Not implemented"));
    }

    /* (non-Javadoc)
     * @see org.openquark.cal.services.ResourceStore#renameResource(org.openquark.cal.services.ResourceName, org.openquark.cal.services.ResourceName, org.openquark.cal.services.ResourceStore, org.openquark.cal.services.Status)
     */
    public boolean renameResource (ResourceName oldResourceName, ResourceName newResourceName,
            ResourceStore newResourceStore, Status renameStatus) {
        // TODO rbc
        renameStatus.add (new Status ("Not implemented"));
        
        return false;
    }

}
