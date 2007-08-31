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
import org.eclipse.jdt.apt.core.util.AptConfig;
import org.eclipse.jdt.core.IJavaProject;

/**
 * Basic tests for the Filer interface in the IDE.
 * @see javax.annotation.processing.Filer
 */
public class FilerTests extends TestBase
{

	public FilerTests(String name) {
		super(name);
	}

	public static Test suite() {
		return new TestSuite(FilerTests.class);
	}
	
	/**
	 * Test generation of a source file, using the GenClass6 annotation
	 * @see javax.annotation.processing.Filer#createClassFile(CharSequence, javax.lang.model.element.Element...)
	 */
	public void testCreateSourceFile() throws Throwable
	{
		IJavaProject jproj = createJavaProject(_projectName);
		IProject proj = jproj.getProject();
		IdeTestUtils.copyResources(proj, "targets/filer01a", "src/targets/filer");
		AptConfig.setEnabled(jproj, true);
		fullBuild();
		expectingNoProblems();
		
		// Check whether generated sources were generated and compiled
		expectingFile(proj, ".apt_generated/gen6/Generated01.java");
		final String[] expectedClasses = { "targets.filer.Parent01", "gen6.Generated01" };
		expectingUniqueCompiledClasses(expectedClasses);
		
		// Check whether non-source resource was generated in final round
		expectingFile( proj, ".apt_generated/summary.txt" );
		
		//TODO: if a parent file is modified,
		// the generated file should be regenerated and recompiled.
		// The generated resource file should be regenerated (but not compiled).
	
		// Modify target file to remove annotation and incrementally rebuild; 
		// generated file should be deleted.
		IdeTestUtils.copyResources(proj, "targets/filer01b", "src/targets/filer");
		incrementalBuild();
		expectingNoProblems();
		expectingNoFile(proj, ".apt_generated/gen6/Generated01.java");
		expectingNoFile( proj, ".apt_generated/summary.txt" );
	}

}
