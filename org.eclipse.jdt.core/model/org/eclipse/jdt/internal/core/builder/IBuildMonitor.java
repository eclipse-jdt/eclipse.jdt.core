package org.eclipse.jdt.internal.core.builder;

public interface IBuildMonitor {
	/**
	 * Signals that a new build has begun
	 */
	void beginBuild(String message);
	/**
	 * Records the event of a compilation unit called "jcu" being compiled
	 */
	void compiled(String jcu);
	/**
	 * Signals that a build has finished.
	 */
	void endBuild(String message);
	/**
	 * Returns the names of the classes that have been compiled so far this build.
	 */
	String[] getCompiledClasses();
}
