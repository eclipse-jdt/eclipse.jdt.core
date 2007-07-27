/*******************************************************************************
 * Copyright (c) 2007 BEA Systems, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    wharley@bea.com - initial API and implementation
 *******************************************************************************/

package org.eclipse.jdt.apt.pluggable.tests;

import junit.framework.Test;
import junit.framework.TestSuite;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.Platform;
import org.eclipse.jdt.core.IJavaProject;
import org.osgi.framework.Bundle;

/**
 * Ensure that the apt.pluggable code is getting loaded, the test environment is as expected,
 * and the test utilities themselves are working.
 * Keeping these "infrastructure" tests separate from the "real" tests helps avoid confusion when 
 * the real tests fail. 
 */
public class InfrastructureTests extends TestBase
{
	public InfrastructureTests(String name) {
		super(name);
	}

	public static Test suite() {
		return new TestSuite(InfrastructureTests.class);
	}
	
	/**
	 * Test that the apt.pluggable.core plug-in is present.  Obviously since this test plug-in
	 * depends on it, this test will never fail; it will either succeed or not run at all.
	 */
	public void testPluginLoaded() throws Throwable
	{
		Bundle bundle = Platform.getBundle("org.eclipse.jdt.apt.pluggable.core");
		assertNotNull("Couldn't get org.eclipse.jdt.apt.pluggable.core bundle", bundle);
	}
	
	/**
	 * Can we create a Java 1.6 test project populated with resources from this plug-in?
	 * If not, there is not much point in testing the rest of annotation processing.
	 */
	public void testProjectBuild() throws Throwable
	{
		IJavaProject jproj = createJavaProject(_projectName);
		IProject proj = jproj.getProject();
		
		IdeTestUtils.copyResources(proj, "targets/infrastructure", "src/targets/infrastructure");
		fullBuild();
		expectingNoProblems();
		String[] expectedClasses = { "targets.infrastructure.NoAnno" };
		expectingCompiledClasses(expectedClasses);
	}

}
