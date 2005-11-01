/*******************************************************************************
 * Copyright (c) 2005 BEA Systems, Inc. 
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    jgarms@bea.com - initial API and implementation
 *    
 *******************************************************************************/
package org.eclipse.jdt.apt.core.build;

import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.IncrementalProjectBuilder;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.IPlatformRunnable;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;

/**
 * Commandline entry point for building a workspace using APT.
 * Currently cleans and then builds the entire workspace.<P>
 * 
 * Sample commandline invocation:
 * 
 * java -cp %ECLIPSE_HOME%/startup.jar org.eclipse.core.launcher.Main 
 * 	-noupdate -application org.eclipse.jdt.apt.core.aptBuild -data %WORKSPACE%
 */
public class AptBuilder implements IPlatformRunnable {

	public Object run(Object args) throws Exception {
		IWorkspace workspace = ResourcesPlugin.getWorkspace();
		IProgressMonitor progressMonitor = new SystemOutProgressMonitor();
		workspace.build(IncrementalProjectBuilder.CLEAN_BUILD, progressMonitor);
		workspace.build(IncrementalProjectBuilder.FULL_BUILD, progressMonitor);
		
		return IPlatformRunnable.EXIT_OK;
	}
	
	/**
	 * Sends all progress to StdOut
	 */
	private static class SystemOutProgressMonitor extends NullProgressMonitor {

		public void beginTask(String name, int totalWork) {
			if (name != null && name.length() > 0)
				System.out.println(name);
		}

		public void subTask(String name) {
			if (name != null && name.length() > 0)
				System.out.println(name);
		}
	}

}
