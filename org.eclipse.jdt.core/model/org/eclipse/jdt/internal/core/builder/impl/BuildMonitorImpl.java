package org.eclipse.jdt.internal.core.builder.impl;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
import org.eclipse.jdt.internal.core.builder.IBuildMonitor;

import java.util.Vector;

/**
 * The build monitor tracks what compilation units have been compiled during a
 * given build.  This allows test suites to evaluate the efficiency of incremental
 * compilation.
 */
public class BuildMonitorImpl implements IBuildMonitor {
	protected Vector fClasses = new Vector();

	/**
	 * Creates a new BuildMonitorImpl.
	 */
	public BuildMonitorImpl() {
	}

	/**
	 * Signals that a new build has begun.
	 */
	public void beginBuild(String message) {
		//		System.out.println(message);
		fClasses.removeAllElements();
	}

	/**
	 * Signals that a compilation unit with the given name has been compiled
	 */
	public void compiled(String jcu) {
		//		System.out.println("<BM>Compiled: " + jcu);
		fClasses.addElement(jcu);
	}

	/**
	 * Signals that a new build has begun.
	 */
	public void endBuild(String message) {
		//		System.out.println(message);		
	}

	/**
	 * Returns the compiled classes
	 */
	public String[] getCompiledClasses() {
		String[] results = new String[fClasses.size()];
		fClasses.copyInto(results);
		return results;
	}

}
