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
 * CALEclipseBridgePlugin.java
 * Created: Dec 12, 2005
 * By: Edward Lam
 */
package org.openquark.cal.eclipse.bridge;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.osgi.framework.BundleContext;

/**
 * The main plugin class for the CAL Eclipse Bridge.
 */
public class CALEclipseBridgePlugin extends AbstractUIPlugin {
    
    /**
     * The plug-in identifier of the CAL Eclipse Bridge
     * (value <code>"org.openquark.cal.eclipse.bridge"</code>).
     */
    public static final String PLUGIN_ID = "org.openquark.cal.eclipse.bridge" ; //$NON-NLS-1$
    
    /** The shared instance. */
    private static CALEclipseBridgePlugin plugin;
    
    /**
     * The constructor.
     */
    public CALEclipseBridgePlugin() {
        plugin = this;
    }
    
    /**
     * This method is called upon plug-in activation.
     * 
     * {@inheritDoc}
     */
    @Override
    public void start(BundleContext context) throws Exception {
        super.start(context);
    }
    
    /**
     * This method is called when the plug-in is stopped.
     * 
     * {@inheritDoc}
     */
    @Override
    public void stop(BundleContext context) throws Exception {
        super.stop(context);
        plugin = null;
    }
    
    /**
     * Returns the shared instance.
     */
    public static CALEclipseBridgePlugin getDefault() {
        return plugin;
    }
    
    /**
     * Add an error entry to the log for this plugin.
     * @param message the associated message.
     * @param e the associated exception, if any.
     */
    public static void log(String message, Throwable e) {
        log(IStatus.ERROR, message, e);
    }
    
    /**
     * Add an entry to the log for this plugin.
     * @param severity one of the severity constants in IStatus.
     * @param message the associated message.
     * @param e the associated exception, if any.
     */
    public static void log(int severity, String message, Throwable e) {
        IStatus status = new Status(severity, PLUGIN_ID, severity, message, e); 
        getDefault().getLog().log(status);
    }       

}
