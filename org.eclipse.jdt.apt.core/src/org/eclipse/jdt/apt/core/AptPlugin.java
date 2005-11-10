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

import java.io.IOException;
import java.io.InputStream;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResourceChangeEvent;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.*;
import org.eclipse.jdt.apt.core.internal.AnnotationProcessorFactoryLoader;
import org.eclipse.jdt.apt.core.internal.generatedfile.GeneratedFileManager;
import org.eclipse.jdt.apt.core.internal.generatedfile.GeneratedResourceChangeListener;
import org.eclipse.jdt.apt.core.util.AptConfig;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.JavaModelException;
import org.osgi.framework.BundleContext;

public class AptPlugin extends Plugin {
	public static final String PLUGIN_ID = "org.eclipse.jdt.apt.core"; //$NON-NLS-1$
	
	private static final String TOOLSJARNAME = "./tools.jar"; //$NON-NLS-1$
	
	/**
	 * Status IDs for system log entries.  Must be unique per plugin.
	 */
	public static final int STATUS_EXCEPTION = 1;
	public static final int STATUS_NOTOOLSJAR = 2;
	public static final int STATUS_CANTLOADPLUGINFACTORY = 3;
	public static final String ERRTXT_NOTOOLSJAR = Messages.AptPlugin_couldNotFindToolsDotJar;
	
	/** Marker ID used for build problem, e.g., missing factory jar */
	public static final String APT_BUILD_PROBLEM_MARKER = PLUGIN_ID + ".buildproblem"; //$NON-NLS-1$
	
	private static AptPlugin thePlugin = null; // singleton object
	
	public void start(BundleContext context) throws Exception {
		thePlugin = this;
		super.start(context);
		initDebugTracing();
		checkToolsJar();
		AptConfig.initialize();
		AnnotationProcessorFactoryLoader.getLoader();
		// register resource-changed listener
		int mask = IResourceChangeEvent.PRE_BUILD | IResourceChangeEvent.PRE_CLOSE | IResourceChangeEvent.PRE_DELETE;
		final IWorkspace workspace = ResourcesPlugin.getWorkspace();
		workspace.addResourceChangeListener( new GeneratedResourceChangeListener(), mask );
	
		final IWorkspaceRoot root = workspace.getRoot();
		if(root != null){
			final IProject[] projects = root.getProjects();
			try{
				for( IProject proj : projects ){
					if( proj.hasNature(JavaCore.NATURE_ID) && proj.exists() && 
						proj.isOpen() && AptConfig.isEnabled(JavaCore.create( proj ))){
						final GeneratedFileManager mgr = GeneratedFileManager.getGeneratedFileManager(proj);
						mgr.ensureGeneratedSourceFolder(null);
					}
				}
			}
			catch( JavaModelException e) { e.printStackTrace(); }
		}
		if( DEBUG )
			trace("addded listener"); //$NON-NLS-1$
	}

	/**
	 * Check for the Sun mirror interfaces.  If they aren't found,
	 * log an error.
	 */
	private void checkToolsJar() {
		boolean foundToolsJar = true;
		InputStream is = null;
		try {
			is = thePlugin.openStream(new Path(TOOLSJARNAME));
		}
		catch (IOException e) {
			foundToolsJar = false;
		}
		finally {
			try {if (is != null) is.close();} catch (IOException ioe) {}
		}
		if (!foundToolsJar) {
			log(new Status(IStatus.ERROR, PLUGIN_ID, STATUS_NOTOOLSJAR, ERRTXT_NOTOOLSJAR, null));
		}
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
	
	public static boolean DEBUG = false;
	public final static String APT_DEBUG_OPTION = AptPlugin.PLUGIN_ID + "/debug"; //$NON-NLS-1$
}
