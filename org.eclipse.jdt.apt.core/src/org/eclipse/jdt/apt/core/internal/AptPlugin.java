/*******************************************************************************
 * Copyright (c) 2005, 2008 BEA Systems, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    wharley@bea.com - initial API and implementation
 *******************************************************************************/

package org.eclipse.jdt.apt.core.internal;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.core.resources.IResourceChangeEvent;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Plugin;
import org.eclipse.core.runtime.Status;
import org.eclipse.jdt.apt.core.internal.generatedfile.GeneratedResourceChangeListener;
import org.eclipse.jdt.apt.core.util.AptConfig;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaCore;
import org.osgi.framework.BundleContext;

public class AptPlugin extends Plugin {
	public static final String PLUGIN_ID = "org.eclipse.jdt.apt.core"; //$NON-NLS-1$
	
	// Tracing options
	public static boolean DEBUG = false;
	public final static String APT_DEBUG_OPTION = AptPlugin.PLUGIN_ID + "/debug"; //$NON-NLS-1$
	public static boolean DEBUG_GFM = false;
	public final static String APT_DEBUG_GFM_OPTION = AptPlugin.APT_DEBUG_OPTION + "/generatedFiles"; //$NON-NLS-1$
	public static boolean DEBUG_GFM_MAPS = false;
	public final static String APT_DEBUG_GFM_MAPS_OPTION = AptPlugin.APT_DEBUG_OPTION + "/generatedFileMaps"; //$NON-NLS-1$
	public static boolean DEBUG_COMPILATION_ENV = false;
	public final static String APT_COMPILATION_ENV_OPTION = AptPlugin.APT_DEBUG_OPTION + "/compilationEnv"; //$NON-NLS-1$

	/**
	 * Status IDs for system log entries.  Must be unique per plugin.
	 */
	public static final int STATUS_EXCEPTION = 1;
	public static final int STATUS_NOTOOLSJAR = 2;
	public static final int STATUS_CANTLOADPLUGINFACTORY = 3;
	
	/** 
	 * Marker source ID used for all APT-created markers.  Note this does not include 
	 * compilation problems, since they get created and managed by JDT on our behalf. 
	 */
	public static final String APT_MARKER_SOURCE_ID = "APT"; //$NON-NLS-1$
	
	public static final String APT_BATCH_PROCESSOR_PROBLEM_MARKER = PLUGIN_ID + ".marker"; //$NON-NLS-1$
	/** Marker ID used for build problem, e.g., missing factory jar */
	public static final String APT_LOADER_PROBLEM_MARKER = PLUGIN_ID + ".buildproblem"; //$NON-NLS-1$
	/** Marker ID used for configuration problem, e.g generated source folder not on classpath */
	public static final String APT_CONFIG_PROBLEM_MARKER = PLUGIN_ID + ".configproblem"; //$NON-NLS-1$
	/** Marker ID used for posting problems during reconcile/build */
	public static final String APT_COMPILATION_PROBLEM_MARKER = PLUGIN_ID + ".compile.problem"; //$NON-NLS-1$
	/** Marker ID used for posting problems during build by processors that don't run in reconcile */
	public static final String APT_NONRECONCILE_COMPILATION_PROBLEM_MARKER = PLUGIN_ID + ".nonreconcile.compile.problem"; //$NON-NLS-1$
	
	private static final SimpleDateFormat TRACE_DATE_FORMAT = new SimpleDateFormat("HH:mm:ss.SSS"); //$NON-NLS-1$
	
	private static AptPlugin thePlugin = null; // singleton object
	
	/**
	 * The javax.annotation.processing.Processor class, which is only available on Java 6 and higher.
	 */
	private static Class<?> _java6ProcessorClass;
	
	// Entries are added lazily in getAptProject(), and removed upon
	// project deletion in deleteAptProject().
	private static final Map<IJavaProject,AptProject> PROJECT_MAP = 
		new HashMap<IJavaProject,AptProject>();

	// Qualified names of services for which these containers may provide implementations
	public static final String JAVA5_FACTORY_NAME = "com.sun.mirror.apt.AnnotationProcessorFactory"; //$NON-NLS-1$
	public static final String JAVA6_FACTORY_NAME = "javax.annotation.processing.Processor"; //$NON-NLS-1$
	
	public void start(BundleContext context) throws Exception {
		thePlugin = this;
		super.start(context);
		initDebugTracing();
		// Do we have access to 
		
		try {
			_java6ProcessorClass = Class.forName(JAVA6_FACTORY_NAME);
		} catch (Throwable e) {
			// ignore
		}

		AptConfig.initialize();
		// DO NOT load extensions from the start() method. This can cause cycles in class loading
		// Not to mention it is bad form to load stuff early.
		// AnnotationProcessorFactoryLoader.getLoader();
		// register resource-changed listener
		// TODO: can move this into AptProject.
		int mask = 
			IResourceChangeEvent.PRE_BUILD | 
			IResourceChangeEvent.PRE_CLOSE | 
			IResourceChangeEvent.PRE_DELETE | 
			IResourceChangeEvent.POST_CHANGE;
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
	
	private void initDebugTracing() {		
		String option = Platform.getDebugOption(APT_DEBUG_OPTION);
		if(option != null) DEBUG = option.equalsIgnoreCase("true") ; //$NON-NLS-1$		
		option = Platform.getDebugOption(APT_DEBUG_GFM_OPTION);
		if(option != null) DEBUG_GFM = option.equalsIgnoreCase("true") ; //$NON-NLS-1$		
		option = Platform.getDebugOption(APT_DEBUG_GFM_MAPS_OPTION);
		if(option != null) DEBUG_GFM_MAPS = option.equalsIgnoreCase("true") ; //$NON-NLS-1$		
	}
	
	public static void trace(final String msg){
		if (DEBUG) {
			StringBuffer sb = new StringBuffer();
			sb.append('[');
			// SimpleDateFormat is not thread-safe, according to javadoc
			synchronized(TRACE_DATE_FORMAT) {
				sb.append(TRACE_DATE_FORMAT.format(new Date()));
			}
			sb.append('-');
			// Some threads have qualified type names; too long.
			String threadName = Thread.currentThread().getName();
			int dot = threadName.lastIndexOf('.');
			if (dot < 0) {
				sb.append(threadName);
			}
			else {
				sb.append(threadName.substring(dot+1));
			}
			sb.append(']');
			sb.append(msg);
			System.out.println(sb);
		}
	}
	
	/**
	 * Convenience method to report an exception in debug trace mode.
	 */
	public static void trace(String msg, Throwable t) {
		trace(msg);
		if (DEBUG) {
			t.printStackTrace(System.out);
		}
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

	/**
	 * True if we are running on a platform that supports Java 6 annotation processing,
	 * that is, if we are running on Java 6 or higher and the org.eclipse.jdt.compiler.apt
	 * plug-in is also present.
	 */
	public static boolean canRunJava6Processors() {
		if (_java6ProcessorClass == null)
			return false;
		return Platform.getBundle("org.eclipse.jdt.compiler.apt") != null; //$NON-NLS-1$
	}
	
	/**
	 * The javax.annotation.processing.Processor class.  This is only available on the
	 * Java 6 or higher platform, so it is loaded via reflection in {@link #start}.
	 */
	public static Class<?> getJava6ProcessorClass() {
		return _java6ProcessorClass;
	}

}
