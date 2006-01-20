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

import java.util.Map;
import java.util.WeakHashMap;

import org.eclipse.core.resources.IResourceChangeEvent;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Plugin;
import org.eclipse.core.runtime.Status;
import org.eclipse.jdt.apt.core.internal.AnnotationProcessorFactoryLoader;
import org.eclipse.jdt.apt.core.internal.AptProject;
import org.eclipse.jdt.apt.core.internal.generatedfile.GeneratedResourceChangeListener;
import org.eclipse.jdt.apt.core.util.AptConfig;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaCore;
import org.osgi.framework.BundleContext;

public class AptPlugin extends Plugin {
	public static final String PLUGIN_ID = "org.eclipse.jdt.apt.core"; //$NON-NLS-1$
	
	/**
	 * Status IDs for system log entries.  Must be unique per plugin.
	 */
	public static final int STATUS_EXCEPTION = 1;
	public static final int STATUS_NOTOOLSJAR = 2;
	public static final int STATUS_CANTLOADPLUGINFACTORY = 3;
	
	public static final String APT_BATCH_PROCESSOR_PROBLEM_MARKER = PLUGIN_ID + ".marker"; //$NON-NLS-1$
	/** Marker ID used for build problem, e.g., missing factory jar */
	public static final String APT_LOADER_PROBLEM_MARKER = PLUGIN_ID + ".buildproblem"; //$NON-NLS-1$
	/** Marker ID used for configuration problem, e.g generated source folder not on classpath */
	public static final String APT_CONFIG_PROBLEM_MARKER = PLUGIN_ID + ".configproblem"; //$NON-NLS-1$
	/** Marker ID used for posting problems during reconcile/build */
	public static final String APT_COMPILATION_PROBLEM_MARKER = PLUGIN_ID + ".compile.problem"; //$NON-NLS-1$	
	
	private static AptPlugin thePlugin = null; // singleton object
	
	// Use a weak hash map so that we don't prevent java projects from getting
	// garbage collected
	private static final Map<IJavaProject,AptProject> PROJECT_MAP = 
		new WeakHashMap<IJavaProject,AptProject>();
	
	public void start(BundleContext context) throws Exception {
		thePlugin = this;
		super.start(context);
		initDebugTracing();
		AptConfig.initialize();
		AnnotationProcessorFactoryLoader.getLoader();
		// register resource-changed listener
		// TODO: can move this into AptProject.
		int mask = IResourceChangeEvent.PRE_BUILD | IResourceChangeEvent.PRE_CLOSE | IResourceChangeEvent.PRE_DELETE;
		JavaCore.addPreProcessingResourceChangedListener( new GeneratedResourceChangeListener(), mask );
		if( DEBUG )
			trace("registered resource change listener"); //$NON-NLS-1$
	}

	public void stop(BundleContext context) throws Exception {
		super.stop(context);
	}
	
	public static AptPlugin getPlugin() {
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
		// TODO: before ship, remove this printing. Instead just log
		System.err.println(message);
		if (e != null) {
			e.printStackTrace();
		}
		
		log(new Status(IStatus.ERROR, PLUGIN_ID, STATUS_EXCEPTION, message, e)); 
	}
	
	/**
	 * Convenience wrapper around log(IStatus), to log an exception
	 * with severity of WARNING.
	 */
	public static void logWarning(Throwable e, String message) {
		// TODO: before ship, remove this printing. Instead just log
		// Note: we don't include the stack here, but it goes in the log
		System.err.println(message);
		
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
	
	private void initDebugTracing() {		
		String option = Platform.getDebugOption(APT_DEBUG_OPTION);
		if(option != null) DEBUG = option.equalsIgnoreCase("true") ; //$NON-NLS-1$		
	}
	
	public static void trace(final String msg){
		if(DEBUG)
			System.err.println("[ " + Thread.currentThread().getName() + " ] " + msg );  //$NON-NLS-1$ //$NON-NLS-2$
	}
	
	private static AptProject getAptProject(IJavaProject javaProject, boolean create){
		synchronized(PROJECT_MAP){
			AptProject aptProject = PROJECT_MAP.get(javaProject);
			if (aptProject != null) {
				return aptProject;
			}
			else{
				if( create ){
					aptProject = new AptProject(javaProject);
					PROJECT_MAP.put(javaProject, aptProject);
					return aptProject;
				}
				else
					return null;
			}
		}
	}
	
	public static AptProject getAptProject(IJavaProject javaProject) {
		return getAptProject(javaProject, true);
	}
	
	public static void deleteAptProject(IJavaProject javaProject) {
		synchronized (PROJECT_MAP) {
			PROJECT_MAP.remove(javaProject);
		}
	}
	
	public static boolean DEBUG = false;
	public final static String APT_DEBUG_OPTION = AptPlugin.PLUGIN_ID + "/debug"; //$NON-NLS-1$
}
