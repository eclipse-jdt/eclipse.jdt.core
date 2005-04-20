/*******************************************************************************
 * Copyright (c) 2000, 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jdt.core.tests.performance.util;

import org.eclipse.core.runtime.IPluginDescriptor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Plugin;
import org.eclipse.core.runtime.Status;

public class JdtCorePerformanceMeterPlugin extends Plugin {
	
	private static final String PLUGIN_ID= "org.eclipse.jdt.core.tests.performance"; //$NON-NLS-1$
	private static JdtCorePerformanceMeterPlugin fgPlugin;
	/** Status code describing an internal error */
	public static final int INTERNAL_ERROR= 1;

	public JdtCorePerformanceMeterPlugin(IPluginDescriptor descriptor) {
		super(descriptor);
		fgPlugin= this;
	}
	
	public static JdtCorePerformanceMeterPlugin getDefault() {
		return fgPlugin;
	}
	
	// logging
		
	public static void logError(String message) {
		if (message == null)
			message= ""; //$NON-NLS-1$
		log(new Status(IStatus.ERROR, PLUGIN_ID, INTERNAL_ERROR, message, null));
	}

	public static void logWarning(String message) {
		if (message == null)
			message= ""; //$NON-NLS-1$
		log(new Status(IStatus.WARNING, PLUGIN_ID, IStatus.OK, message, null));
	}

	public static void log(Throwable e) {
		log(new Status(IStatus.ERROR, PLUGIN_ID, INTERNAL_ERROR, "Internal Error", e)); //$NON-NLS-1$
	}
	
	public static void log(IStatus status) {
	    if (fgPlugin != null) {
	        fgPlugin.getLog().log(status);
	    } else {
	        switch (status.getSeverity()) {
	        case IStatus.ERROR:
		        System.err.println("Error: " + status.getMessage()); //$NON-NLS-1$
	            break;
	        case IStatus.WARNING:
		        System.err.println("Warning: " + status.getMessage()); //$NON-NLS-1$
	            break;
	        }
	        Throwable exception= status.getException();
	        if (exception != null)
	            exception.printStackTrace(System.err);
	    }
	}
}
