/*******************************************************************************
 * Copyright (c) 2005, 2007 BEA Systems, Inc.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *    jgarms@bea.com - initial API and implementation
 *
 *******************************************************************************/
package org.eclipse.jdt.apt.core.build;

import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.IncrementalProjectBuilder;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.equinox.app.IApplication;
import org.eclipse.equinox.app.IApplicationContext;

/**
 * Commandline entry point for building a workspace using APT.
 * Currently cleans and then builds the entire workspace.<P>
 *
 * Sample commandline invocation:
 *
 * %ECLIPSE_HOME%/eclipsec -nosplash -application org.eclipse.jdt.apt.core.aptBuild -data %WORKSPACE%
 *
 * This class should not be referenced programmatically by other
 * Java code. This class exists only for the purpose of launching
 * the AptBuilder from the command line.  The fields and methods on this
 * class are not API.
 */
public class AptBuilder implements IApplication {

	/**
	 * Runs this runnable with the given application context and returns a result.
	 * The content of the args is unchecked and should conform to the expectations of
	 * the runnable being invoked. Typically this is a <code>String</code> array.
	 * Applications can return any object they like. If an <code>Integer</code> is returned
	 * it is treated as the program exit code if Eclipse is exiting.
	 *
	 * @param context the given application context passed to the application
	 * @return the return value of the application
	 * @exception Exception if there is a problem running this runnable.
	 * @see #EXIT_OK
	 * @see #EXIT_RESTART
	 * @see #EXIT_RELAUNCH
	 */
	@Override
	public Object start(IApplicationContext context) throws Exception {
		IWorkspace workspace = ResourcesPlugin.getWorkspace();
		IProgressMonitor progressMonitor = new SystemOutProgressMonitor();
		workspace.build(IncrementalProjectBuilder.CLEAN_BUILD, progressMonitor);
		workspace.build(IncrementalProjectBuilder.FULL_BUILD, progressMonitor);

		return IApplication.EXIT_OK;
	}

	@Override
	public void stop() {
		// nothing to do
	}

	/**
	 * Sends all progress to StdOut
	 */
	private static class SystemOutProgressMonitor extends NullProgressMonitor {

		@Override
		public void beginTask(String name, int totalWork) {
			if (name != null && name.length() > 0)
				System.out.println(name);
		}

		@Override
		public void subTask(String name) {
			if (name != null && name.length() > 0)
				System.out.println(name);
		}
	}

}
