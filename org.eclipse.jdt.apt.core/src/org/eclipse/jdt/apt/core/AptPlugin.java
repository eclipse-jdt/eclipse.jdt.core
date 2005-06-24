/*******************************************************************************
 * Copyright (c) 2005 BEA Systems, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    wharley@bea.com - initial API and implementation
 *******************************************************************************/

package org.eclipse.jdt.apt.core;

import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Plugin;
import org.eclipse.jdt.apt.core.util.AptConfig;
import org.osgi.framework.BundleContext;

public class AptPlugin extends Plugin {
	public static final String PLUGIN_ID= "org.eclipse.jdt.apt.core"; //$NON-NLS-1$
	private static AptPlugin thePlugin = null; // singleton object
	
	public void start(BundleContext context) throws Exception {
		thePlugin = this;
		super.start(context);
		initDebugTracing();
		AptConfig.initialize();
	}

	public void stop(BundleContext context) throws Exception {
		super.stop(context);
	}
	
	public static AptPlugin getPlugin() {
		return thePlugin;
	}
	
	private void initDebugTracing() {		
		String option = Platform.getDebugOption(APT_DEBUG_OPTION);
		if(option != null) DEBUG = option.equalsIgnoreCase("true") ; //$NON-NLS-1$		
	}
	
	public static boolean DEBUG = false;
	public final static String APT_DEBUG_OPTION = AptPlugin.PLUGIN_ID + "/debug";
}
