/*******************************************************************************
 * Copyright (c) 2000, 2003 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jdt.core;

/**
 * Markers used by the Java model.
 * <p>
 * This interface declares constants only; it is not intended to be implemented
 * or extended.
 * </p>
 */
public interface IJavaModelMarker {

	/**
	 * Java model problem marker type (value <code>"org.eclipse.jdt.core.problem"</code>).
	 * This can be used to recognize those markers in the workspace that flag problems 
	 * detected by the Java tooling during compilation.
	 */
	public static final String JAVA_MODEL_PROBLEM_MARKER = JavaCore.PLUGIN_ID + ".problem"; //$NON-NLS-1$


	/**
	 * Java model transient problem marker type (value <code>"org.eclipse.jdt.core.transient_problem"</code>).
	 * This can be used to recognize those markers in the workspace that flag transient
	 * problems detected by the Java tooling (such as a problem
	 * detected by the outliner, or a problem detected during a code completion)
	 */
	public static final String TRANSIENT_PROBLEM = JavaCore.PLUGIN_ID + ".transient_problem"; //$NON-NLS-1$

	/**
	 * Java model task marker type (value <code>"org.eclipse.jdt.core.task"</code>).
	 * This can be used to recognize task markers in the workspace that correspond to tasks
	 * specified in Java source comments and detected during compilation (for example, 'TO-DO: ...').
	 * Tasks are identified by a task tag, which can be customized through <code>JavaCore</code>
	 * option <code>"org.eclipse.jdt.core.compiler.taskTag"</code>.
	 * @since 2.1
	 */
	public static final String TASK_MARKER = JavaCore.PLUGIN_ID + ".task"; //$NON-NLS-1$

    
    /** 
	 * Id marker attribute (value <code>"arguments"</code>).
	 * Arguments are concatenated into one String, prefixed with an argument count (followed with colon
	 * separator) and separated with '#' characters. For example:
	 *     { "foo", "bar" } is encoded as "2:foo#bar",     
	 *     {  } is encoded as "0: "
	 * @since 2.0
	 */
	 public static final String ARGUMENTS = "arguments"; //$NON-NLS-1$
    
	/** 
	 * Id marker attribute (value <code>"id"</code>).
	 */
	 public static final String ID = "id"; //$NON-NLS-1$

	/** 
	 * Flags marker attribute (value <code>"flags"</code>).
	 * Reserved for future use.
	 */
	 public static final String FLAGS = "flags"; //$NON-NLS-1$

	/** 
	 * Cycle detected marker attribute (value <code>"cycleDetected"</code>).
	 * Used only on buildpath problem markers.
	 * The value of this attribute is either "true" or "false".
	 */
	 public static final String CYCLE_DETECTED = "cycleDetected"; //$NON-NLS-1$

	/**
	 * Build path problem marker type (value <code>"org.eclipse.jdt.core.buildpath_problem"</code>).
	 * This can be used to recognize those markers in the workspace that flag problems 
	 * detected by the Java tooling during classpath setting.
	 */
	public static final String BUILDPATH_PROBLEM_MARKER = JavaCore.PLUGIN_ID + ".buildpath_problem"; //$NON-NLS-1$
	
	/** 
	 * Classpath file format marker attribute (value <code>"classpathFileFormat"</code>).
	 * Used only on buildpath problem markers.
	 * The value of this attribute is either "true" or "false".
	 * 
	 * @since 2.0
	 */
	 public static final String CLASSPATH_FILE_FORMAT = "classpathFileFormat"; //$NON-NLS-1$
	
}
