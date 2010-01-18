/*******************************************************************************
 * Copyright (c) 2005 Business Objects Software Limited and others.
 * All rights reserved. 
 * This file is made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Business Objects Software Limited - initial API and implementation based on Eclipse 3.1.2 code for
 *                             /org.eclipse.jdt.ui/ui/org/eclipse/jdt/internal/ui/IJavaHelpContextIds.java
 *                           Eclipse source is available at: http://www.eclipse.org/downloads/
 *******************************************************************************/

/*
 * CALEclipseUIPlugin.java
 * Creation date: Nov 1, 2005.
 * By: Edward Lam
 */
package org.openquark.cal.eclipse.ui;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;

import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.dialogs.ErrorDialog;
import org.eclipse.jface.dialogs.IDialogSettings;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.text.templates.ContextTypeRegistry;
import org.eclipse.jface.text.templates.persistence.TemplateStore;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.editors.text.EditorsUI;
import org.eclipse.ui.editors.text.templates.ContributionContextTypeRegistry;
import org.eclipse.ui.editors.text.templates.ContributionTemplateStore;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.eclipse.ui.texteditor.ChainedPreferenceStore;
import org.openquark.cal.eclipse.core.CALEclipseCorePlugin;
import org.openquark.cal.eclipse.ui.caleditor.CALDocumentProvider;
import org.openquark.cal.eclipse.ui.templates.CALDocTemplateContextType;
import org.openquark.cal.eclipse.ui.templates.CALTemplateContextType;
import org.openquark.cal.eclipse.ui.text.CALTextTools;
import org.openquark.cal.eclipse.ui.text.PreferencesAdapter;
import org.openquark.cal.eclipse.ui.util.SWTUtil;
import org.osgi.framework.BundleContext;


/**
 * The main plugin class to be used in the desktop.
 * @author Edward Lam
 */
public class CALEclipseUIPlugin extends AbstractUIPlugin {

    /**
     * The plug-in identifier of the CAL ui support
     * (value <code>org.openquark.cal.eclipse.ui</code>).
     */
    public static final String PLUGIN_ID = "org.openquark.cal.eclipse.ui" ; //$NON-NLS-1$
    
    /**
     * The editor identifier of the CAL text editor
     * (value <code>org.openquark.cal.eclipse.ui.CALEditor</code>).
     */
    public static final String EDITOR_ID = "org.openquark.cal.eclipse.ui.CALEditor"; //$NON-NLS-1$
    
    // The shared instance.
    private static CALEclipseUIPlugin plugin;

    /** The instance of CALTextTools for this plugin. */
    private CALTextTools fCALTextTools;
    /** The instance of CALDocumentProvider for this plugin. */
    private CALDocumentProvider calDocumentProvider;

    /** for templates */
    private ContextTypeRegistry fContextTypeRegistry;
    
    /** The instance that stores all CAL code Templates */
    private TemplateStore fTemplateStore;
    
    /** Key to store customized templates in the preferences
     *  org.openquark.cal.eclipse.ui.custom_templates
     */
    private static final String TEMPLATES_KEY= PLUGIN_ID + ".custom_templates"; //$NON-NLS-1$

    
    /**
     * The combined preference store.
     * @since 3.0
     */
    private IPreferenceStore fCombinedPreferenceStore;
    
    /**
     * The constructor.
     */
    public CALEclipseUIPlugin() {
        plugin = this;
    }

    /**
     * This method is called upon plug-in activation
     */
    @Override
    public void start(BundleContext context) throws Exception {
        super.start(context);
    }

    /**
     * This method is called when the plug-in is stopped
     */
    @Override
    public void stop(BundleContext context) throws Exception {
        super.stop(context);
        plugin = null;
    }

    /**
     * Returns the shared instance.
     */
    public static CALEclipseUIPlugin getDefault() {
        return plugin;
    }

    /**
     * Returns an image descriptor for the image file at the given plug-in relative path.
     * 
     * TODO - use an ImageRegistry to defer image creation.
     * 
     * @param path the path
     * @return the image descriptor
     */
    public static ImageDescriptor getImageDescriptor(String path) {
        return AbstractUIPlugin.imageDescriptorFromPlugin(PLUGIN_ID, path);
    }
    
    /* *********************************
     * The following seems to be boilerplate found in many plugin classes.
     * Largely based on the PDEPlugin class in org.eclipse.pde.ui plugin.
     * 
     * *********************************/
    
    public static IWorkspace getWorkspace() {
        return ResourcesPlugin.getWorkspace();
    }

    /**
     * @return if there is a workbench window currently active, its currently active page if any.
     * Otherwise null.
     */
    public static IWorkbenchPage getActivePage() {
        return getDefault().internalGetActivePage();
    }

    /**
     * @return if there is a workbench window currently active, its currently active page if any.
     * Otherwise null.
     */
    private IWorkbenchPage internalGetActivePage() {
        IWorkbenchWindow window = getWorkbench().getActiveWorkbenchWindow();
        if (window == null) {
            return null;
        }
        return window.getActivePage();
    }
    
    /**
     * @return the currently active workbench window, or null if there isn't any.
     */
    public static IWorkbenchWindow getActiveWorkbenchWindow() {
        return getDefault().getWorkbench().getActiveWorkbenchWindow();
    }

    /**
     * @return the shell containing the currently active workbench's window controls,
     * or null if no such window exists, the shell does not exist, or the window is closed.
     */
    public static Shell getActiveWorkbenchShell() {
        IWorkbenchWindow window = getActiveWorkbenchWindow();
        if (window != null) {
            return window.getShell();
        }
        return null;
    }

    /**
     * Returns the settings for a dialog section for this UI plug-in.
     * The dialog settings is used to hold persistent state data for the various
     * wizards and dialogs of this plug-in in the context of a workbench. 
     * <p>
     * If an error occurs reading the dialog store, an empty one is quietly created
     * and returned.
     * </p>
     *
     * @param name the name of the section
     * @return the dialog settings for the named section.
     * If the section settings did not previously exist, a new one will be created and returned.
     */
    public IDialogSettings getDialogSettingsSection(String name) {
        IDialogSettings dialogSettings = getDialogSettings();
        IDialogSettings section = dialogSettings.getSection(name);
        if (section == null) {
            section = dialogSettings.addNewSection(name);
        }
        return section;
    }

    /**
     * Log an exception to this plugin's log.
     * @param e the exception to log.
     */
    public static void log(Throwable e) {
        if (e instanceof InvocationTargetException) {
            e = ((InvocationTargetException) e).getTargetException();
        }
        
        IStatus status = null;
        if (e instanceof CoreException) {
            status = ((CoreException) e).getStatus();
        
        } else {
            // AntlrUIPlugin has "getMessage("Plugin.internal_error")" instead of "e.getMessage()"
            status = new Status(IStatus.ERROR, PLUGIN_ID, IStatus.OK, e.getMessage(), e);
        }
        
        log(status);
    }
    
    /**
     * Log a status object to this plugin's log.
     * @param status the status to log.
     */
    public static void log(IStatus status) {
        getDefault().getLog().log(status);
    }
    
    /**
     * Log a message to this plugin's log.
     * @param message the message to log.
     */
    public static void logErrorMessage(String message) {
        log(new Status(IStatus.ERROR, PLUGIN_ID, IStatus.ERROR, message, null));
    }
    
    /**
     * Log an exception to the console.
     * @param e the exception to log.
     */
    public static void logException(Throwable e) {
        logException(e, null, null);
    }

    /**
     * Log an exception to the console.
     * @param e the exception to log.
     * @param title
     * @param message
     */
    public static void logException(Throwable e, final String title, String message) {
        
        if (e instanceof InvocationTargetException) {
            e = ((InvocationTargetException)e).getTargetException();
        }
        
        IStatus status = null;
        if (e instanceof CoreException) {
            status = ((CoreException)e).getStatus();
        
        } else {
            if (message == null) {
                message = e.getMessage();
            }
            
            if (message == null) {
                message = e.toString();
            }
            
            status = new Status(IStatus.ERROR, PLUGIN_ID, IStatus.OK, message, e);
        }
        
        getDefault().getLog().log(status);
        Display display = SWTUtil.getStandardDisplay();
        
        final IStatus fstatus = status;
        
        display.asyncExec(new Runnable() {
            public void run() {
                ErrorDialog.openError(null, title, null, fstatus);
            }
        });
    }
    
    /*
     * JavaPlugin / JavaTextTools.
     */
    
    /**
     * Returns a combined preference store, this store is read-only.
     * 
     * @return the combined preference store
     */
    public IPreferenceStore getCombinedPreferenceStore() {
        if (fCombinedPreferenceStore == null) {
            IPreferenceStore generalTextStore = EditorsUI.getPreferenceStore();
            fCombinedPreferenceStore = new ChainedPreferenceStore(
                    new IPreferenceStore[] {getPreferenceStore(), new PreferencesAdapter(CALEclipseCorePlugin.getDefault().getPluginPreferences()), generalTextStore});
        }
        return fCombinedPreferenceStore;
    }
    
    /**
     * @return The instance of CALTextTools for this plugin.
     */
    public synchronized CALTextTools getCALTextTools() {
        if (fCALTextTools == null) {
            fCALTextTools= new CALTextTools(getPreferenceStore(), CALEclipseCorePlugin.getDefault().getPluginPreferences());
        }
        return fCALTextTools;
    }
    
    /**
     * @return The instance of CALDocumentProvider for this plugin. 
     */
    public synchronized CALDocumentProvider getCALDocumentProvider() {
        if (calDocumentProvider == null) {
            calDocumentProvider = new CALDocumentProvider();
        }
        return calDocumentProvider;
    }
    
    
    /**
     * Returns the template context type registry for the CAL UI plug-in.
     * 
     * @return the template context type registry for the CAL UI plug-in
     */
    public ContextTypeRegistry getTemplateContextRegistry() {
      if (fContextTypeRegistry == null) {
        ContributionContextTypeRegistry registry= new ContributionContextTypeRegistry();
        registry.addContextType(CALTemplateContextType.ID);
        registry.addContextType(CALDocTemplateContextType.ID);

        fContextTypeRegistry= registry;
      }

      return fContextTypeRegistry;
    }
    
    /**
     * Returns the template store for the CAL editor templates.
     * 
     * @return the template store for the CAL editor templates
     */
    public TemplateStore getTemplateStore() {
      if (fTemplateStore == null) {
        final IPreferenceStore store= getPreferenceStore();
        fTemplateStore= new ContributionTemplateStore(
            getTemplateContextRegistry(), store, TEMPLATES_KEY);

        try {
          fTemplateStore.load();
        } catch (IOException e) {
          log(e);
        }
        fTemplateStore.startListeningForPreferenceChanges();
      }
      
      return fTemplateStore;
    }
    
    public static String getPluginId(){
        return PLUGIN_ID;
    }
}
