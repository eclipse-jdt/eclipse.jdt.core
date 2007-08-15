/*******************************************************************************
 * Copyright (c) 2007 BEA Systems, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     wharley - initial API and implementation
 *******************************************************************************/
package org.eclipse.jdt.internal.apt.pluggable.core;

import java.text.SimpleDateFormat;
import java.util.Date;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Plugin;
import org.eclipse.core.runtime.Status;
import org.osgi.framework.BundleContext;

/**
 * The plug-in responsible for dispatch of Java 6 (JSR269 Pluggable Annotation
 * Processing API) annotation processors in the IDE.
 * This is named Apt6Plugin to distinguish it from AptPlugin, which is responsible
 * for Java 5 (com.sun.mirror) processors.
 */
public class Apt6Plugin extends Plugin {

	private static final SimpleDateFormat TRACE_DATE_FORMAT = new SimpleDateFormat("HH:mm:ss.SSS"); //$NON-NLS-1$

	public static final String PLUGIN_ID = "org.eclipse.jdt.apt.pluggable.core"; //$NON-NLS-1$
		
	/**
	 * Status IDs for system log entries.  Must be unique per plugin.
	 */
	public static final int STATUS_EXCEPTION = 1;

	// Tracing options
	public static boolean DEBUG = false;
	public final static String APT_DEBUG_OPTION = Apt6Plugin.PLUGIN_ID + "/debug"; //$NON-NLS-1$

	private static Apt6Plugin thePlugin = null; // singleton object
	
	public Apt6Plugin() {
	}

	@Override
	public void start(BundleContext context) throws Exception {
		super.start(context);
		thePlugin = this;
		initDebugTracing();
	}
	
	private void initDebugTracing() {		
		String option = Platform.getDebugOption(APT_DEBUG_OPTION);
		if (option != null) {
			DEBUG = option.equalsIgnoreCase("true"); //$NON-NLS-1$
		}
	}
	
	public static Apt6Plugin getPlugin() {
		return thePlugin;
	}

	/**
	 * Log a status message to the platform log.  Use this for reporting exceptions.
	 * @param status
	 */
	public static void log(IStatus status) {
		thePlugin.getLog().log(status);
	}
	
	/**
	 * Convenience wrapper around log(IStatus), to log an exception
	 * with severity of ERROR.
	 */
	public static void log(Throwable e, String message) {
		log(new Status(IStatus.ERROR, PLUGIN_ID, STATUS_EXCEPTION, message, e)); 
	}
	
	/**
	 * Convenience wrapper around log(IStatus), to log an exception
	 * with severity of WARNING.
	 */
	public static void logWarning(Throwable e, String message) {		
		log(createWarningStatus(e, message));
	}
	
	/**
	 * Convenience wrapper for rethrowing exceptions as CoreExceptions,
	 * with severity of ERROR.
	 */
	public static Status createStatus(Throwable e, String message) {
		return new Status(IStatus.ERROR, PLUGIN_ID, STATUS_EXCEPTION, message, e);
	}
	
	/**
	 * Convenience wrapper for rethrowing exceptions as CoreExceptions,
	 * with severity of WARNING.
	 */
	public static Status createWarningStatus(Throwable e, String message) {
		return new Status(IStatus.WARNING, PLUGIN_ID, STATUS_EXCEPTION, message, e);
	}
	
	/**
	 * Convenience wrapper for rethrowing exceptions as CoreExceptions,
	 * with severity of INFO.
	 */
	public static Status createInfoStatus(Throwable e, String message) {
		return new Status(IStatus.INFO, PLUGIN_ID, STATUS_EXCEPTION, message, e);
	}
	
	public static void trace(final String msg) {
		if (DEBUG) {
			StringBuffer sb = new StringBuffer();
			sb.append('[');
			// SimpleDateFormat is not thread-safe, according to javadoc
			synchronized (TRACE_DATE_FORMAT) {
				sb.append(TRACE_DATE_FORMAT.format(new Date()));
			}
			sb.append('-');
			// Some threads have qualified type names; too long.
			String threadName = Thread.currentThread().getName();
			int dot = threadName.lastIndexOf('.');
			if (dot < 0) {
				sb.append(threadName);
			} else {
				sb.append(threadName.substring(dot + 1));
			}
			sb.append(']');
			sb.append(msg);
			System.out.println(sb);
		}
	}

}
