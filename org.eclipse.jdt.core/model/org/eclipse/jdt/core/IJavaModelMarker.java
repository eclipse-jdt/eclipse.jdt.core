package org.eclipse.jdt.core;

public interface IJavaModelMarker {

	/**
	 * Java model problem marker type (value <code>"org.eclipse.jdt.core.problem"</code>).
	 * This can be used to recognize those markers in the workspace that flag problems 
	 * detected by the Java tooling during compilation.
	 */
	public static final String JAVA_MODEL_PROBLEM_MARKER = JavaCore.PLUGIN_ID + ".problem";


	/**
	 * Java model transient problem marker type (value <code>"org.eclipse.jdt.core.transient_problem"</code>).
	 * This can be used to recognize those markers in the workspace that flag transcient
	 * problems detected by the Java tooling (such as a cycle in the build path, a problem
	 * detected by the outliner, or a problem detected during a code completion)
	 */
	public static final String TRANSIENT_PROBLEM = JavaCore.PLUGIN_ID + ".transient_problem";
	
	/** 
	 * Id marker attribute (value <code>"id"</code>).
	 * Reserved for future use.
	 */
	 public static final String ID = "id";

	/** 
	 * Flags marker attribute (value <code>"flags"</code>).
	 * Reserved for future use.
	 */
	 public static final String FLAGS = "flags";

	/** 
	 * Cycle detected marker attribute (value <code>"cycleDetected"</code>).
	 * Used only on transient problem markers.
	 * The value of this attribute is the name of the project that caused a 
	 * cycle in the projects classpaths.
	 */
	 public static final String CYCLE_DETECTED = "cycleDetected";
	/**
	 * Build path problem marker type (value <code>"org.eclipse.jdt.core.buildpath_problem"</code>).
	 * This can be used to recognize those markers in the workspace that flag problems 
	 * detected by the Java tooling during classpath setting.
	 */
	public static final String BUILDPATH_PROBLEM_MARKER = JavaCore.PLUGIN_ID + ".buildpath_problem";
}
