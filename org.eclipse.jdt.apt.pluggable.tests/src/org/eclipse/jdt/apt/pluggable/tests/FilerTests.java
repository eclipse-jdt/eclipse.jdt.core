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

import java.util.Map;

import junit.framework.Test;
import junit.framework.TestSuite;

import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.IPath;
import org.eclipse.jdt.apt.core.internal.util.FactoryContainer;
import org.eclipse.jdt.apt.core.internal.util.FactoryPath;
import org.eclipse.jdt.apt.core.internal.util.FactoryContainer.FactoryType;
import org.eclipse.jdt.apt.core.util.AptConfig;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.compiler.CategorizedProblem;
import org.eclipse.jdt.core.tests.builder.Problem;

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
		// Temporary workaround for https://bugs.eclipse.org/bugs/show_bug.cgi?id=201931
		// Bail out on Linux
		String osName = System.getProperty("os.name");
		if (null == osName || !osName.contains("Windows")) {
			return;
		}

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
		
		final String[] expectedClasses2 = { "targets.filer.Parent01" };
		expectingUniqueCompiledClasses(expectedClasses2);

		expectingNoFile(proj, ".apt_generated/gen6/Generated01.java");
		expectingNoFile( proj, ".apt_generated/summary.txt" );
	}

	/**
	 * Test generation of a source file that is referenced by the parent, using the GenClass6 annotation
	 * @see javax.annotation.processing.Filer#createSourceFile(CharSequence, javax.lang.model.element.Element...)
	 */
	public void testCreateSourceFileWithGenReference() throws Throwable
	{
		// Temporary workaround for https://bugs.eclipse.org/bugs/show_bug.cgi?id=201931
		// Bail out on Linux
		String osName = System.getProperty("os.name");
		if (null == osName || !osName.contains("Windows")) {
			return;
		}

		IJavaProject jproj = createJavaProject(_projectName);
		IProject proj = jproj.getProject();
		IdeTestUtils.copyResources(proj, "targets/filer02a", "src/targets/filer");
		
		// Make sure that there are no Java 5 processors on the factory path - see comment below.
		FactoryPath fp = (FactoryPath) AptConfig.getFactoryPath(jproj);
		for (Map.Entry<FactoryContainer, FactoryPath.Attributes> entry : fp.getAllContainers().entrySet()) {
			if (entry.getKey().getType() == FactoryType.PLUGIN) {
				String id = entry.getKey().getId();
				if (!Apt6TestsPlugin.PLUGIN_ID.equals(id)) {
					fp.disablePlugin(id);
				}
			}
		}
		AptConfig.setFactoryPath(jproj, fp);
		AptConfig.setEnabled(jproj, true);
		fullBuild();
		expectingNoProblems();
		
		// Check whether generated sources were generated and compiled
		expectingFile(proj, ".apt_generated/gen6/Generated02.java");
		final String[] expectedClasses = { "targets.filer.Parent02", "gen6.Generated02" };
		expectingUniqueCompiledClasses(expectedClasses);
		
		// Modify target file to change name of generated file and incrementally rebuild; 
		// generated file should be deleted.
		IdeTestUtils.copyResources(proj, "targets/filer02b", "src/targets/filer");
		incrementalBuild();
		
		IPath parentPath = proj.getFullPath().append("src/targets/filer/Parent02.java");
		expectingOnlySpecificProblemFor(parentPath, new Problem("Parent02", "gen6.Generated02 cannot be resolved to a type", parentPath, 842, 858, CategorizedProblem.CAT_TYPE, IMarker.SEVERITY_ERROR));
		
		// This test only works if there are no Java 5 processors (e.g., apt.tests plugin) in the factory path.
		// If Java 5 processors are present, then gen6.Generated02 will also be recompiled, before it's deleted.
		final String[] expectedClasses2 = { "gen6.XxxGenerated02", "targets.filer.Parent02" };
		expectingUniqueCompiledClasses(expectedClasses2);

		expectingNoFile(proj, ".apt_generated/gen6/Generated02.java");
	}
	
	// Temporarily disabled, functionality not yet implemented
	public void _testGetResource01() throws Throwable {
		ProcessorTestStatus.reset();
		IJavaProject jproj = createJavaProject(_projectName);
		IProject proj = jproj.getProject();
		IPath projPath = proj.getFullPath();
		
		env.addClass(projPath.append("src"), "p", "Trigger",
				"package p;\n" +
				"import org.eclipse.jdt.apt.pluggable.tests.annotations.FilerTestTrigger;\n" +
				"@FilerTestTrigger(test = \"testGetResource01\", arg0 = \"src/p\", arg1 = \"Trigger.java\")" +
				"public class Trigger {\n" +
				"}"
			); 
		
		// Make sure that there are no Java 5 processors on the factory path - see comment below.
		FactoryPath fp = (FactoryPath) AptConfig.getFactoryPath(jproj);
		for (Map.Entry<FactoryContainer, FactoryPath.Attributes> entry : fp.getAllContainers().entrySet()) {
			if (entry.getKey().getType() == FactoryType.PLUGIN) {
				String id = entry.getKey().getId();
				if (!Apt6TestsPlugin.PLUGIN_ID.equals(id)) {
					fp.disablePlugin(id);
				}
			}
		}
		AptConfig.setFactoryPath(jproj, fp);
		AptConfig.setEnabled(jproj, true);
		fullBuild();
		expectingNoProblems();
		assertTrue("Processor did not run", ProcessorTestStatus.processorRan());
		assertEquals("Processor reported errors", ProcessorTestStatus.NO_ERRORS, ProcessorTestStatus.getErrors());
	}


}
