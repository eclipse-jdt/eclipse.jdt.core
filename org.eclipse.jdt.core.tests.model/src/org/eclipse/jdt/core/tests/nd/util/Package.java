/*******************************************************************************
 * Copyright (c) 2015, 2016 Google, Inc and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Stefan Xenos (Google) - Initial implementation
 *******************************************************************************/
package org.eclipse.jdt.core.tests.nd.util;

import org.eclipse.core.runtime.ILog;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Plugin;
import org.eclipse.core.runtime.Status;
import org.eclipse.jdt.core.JavaCore;

/**
 * @noreference This class is not intended to be referenced by clients
 */
/* package */ class Package {
	public static String PLUGIN_ID = JavaCore.PLUGIN_ID;

	/**
	 * @noreference This method is not intended to be referenced by clients.
	 */
	public static void log(Throwable e) {
		String msg= e.getMessage();
		if (msg == null) {
			log("Error", e); //$NON-NLS-1$
		} else {
			log("Error: " + msg, e); //$NON-NLS-1$
		}
	}
	
	/**
	 * @noreference This method is not intended to be referenced by clients.
	 */
	public static void log(String message, Throwable e) {
		log(createStatus(message, e));
	}
	
	/**
	 * @noreference This method is not intended to be referenced by clients.
	 */
	public static IStatus createStatus(String msg, Throwable e) {
		return new Status(IStatus.ERROR, PLUGIN_ID, msg, e);
	}

	/**
	 * @noreference This method is not intended to be referenced by clients.
	 */
	public static IStatus createStatus(String msg) {
		return new Status(IStatus.ERROR, PLUGIN_ID, msg);
	}

	/**
	 * @noreference This method is not intended to be referenced by clients.
	 * 
	 * Returns the appropriate ILog for this package
	 */
	public static ILog getLog() {
		Plugin plugin = JavaCore.getPlugin();
		if (plugin == null) {
			return null;
		}
		return plugin.getLog();
	}

	/**
	 * @noreference This method is not intended to be referenced by clients.
	 */
	public static void log(IStatus status) {
		getLog().log(status);
	}
}
