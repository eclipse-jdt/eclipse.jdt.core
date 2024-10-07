/*******************************************************************************
 * Copyright (c) 2022 Dominik Wiedner and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Dominik Wiedner - performance test for PTBKey hash calculation
 *******************************************************************************/
package org.eclipse.jdt.core.tests.performance;

import junit.framework.Test;
import junit.framework.TestSuite;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.IPath;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.tests.builder.TestingEnvironment;
import org.eclipse.jdt.core.tests.junit.extension.TestCase;
import org.eclipse.jdt.core.tests.util.Util;
import org.eclipse.test.performance.Dimension;

public class PTBKeyHashCalculationTest extends TestCase {

	private TestingEnvironment env;

	public PTBKeyHashCalculationTest(String name) {
		super(name);
	}

	public static Test suite() {
		return new TestSuite(PTBKeyHashCalculationTest.class);
	}

	@Override
	protected void setUp() throws Exception {
		super.setUp();

		if (this.env == null) {
			this.env = new TestingEnvironment();
			this.env.openEmptyWorkspace();
		}
		this.env.resetWorkspace();

		final IPath projectPath = this.env.addProject("PTBKeyTest", "1.8");
		this.env.addExternalJars(projectPath, Util.getJavaClassLibs());
		this.env.removePackageFragmentRoot(projectPath, "");
		this.env.addPackageFragmentRoot(projectPath, "src");
		this.env.setOutputFolder(projectPath, "bin");
		final IJavaProject javaProj = this.env.getJavaProject(projectPath);
		final IProject proj = javaProj.getProject();
		final IPath projPath = proj.getFullPath();
		final IPath root = projPath.append("src");

		this.env.addClass(root, "test", "MainClass",
				"package test;\n" +
				"public abstract class MainClass {\n" +
				"    protected Object createField(Object o) { return null; }\n" +
				"}"
		);

		this.env.addClass(root, "test", "BaseClass",
				"package test;\n" +
				"import java.util.Iterator;\n" +
				"public class BaseClass<I extends BaseClass<I, J>, J> {\n" +
				"    protected abstract class MyIterator implements Iterator<I> {\n" +
				"        @Override public boolean hasNext() { return false; }\n" +
				"        @Override public I next() { return null; }\n" +
				"    }\n" +
				"    protected class SomeOtherClass {\n" +
				"        public final void doSomething() { System.out.println(\"test\"); }\n" +
				"    }\n" +
				"    private class OtherIterator extends MyIterator {\n" +
				"        @Override public I next() { return null; }\n" +
				"    }\n" +
				"}"
		);

		// issues are only observable with a higher amount of classes
		for (int i = 0; i < 1000; i++) {
			final StringBuilder sb = new StringBuilder();
			sb.append("package example;\n")
				.append("import test.BaseClass;\n")
				.append("public class TestClass").append(i).append(" extends MainClass {\n");

			for (int j = 0; j < 10; j++) {
				sb.append("\tpublic final class Inner").append(j).append(" extends BaseClass<Inner").append(j).append(", String> {\n");

				for (int x = 0; x < 2; x++) {
					sb.append("\t\tpublic final class Inner").append(j).append("_").append(x).append(" extends BaseClass<Inner").append(j).append("_").append(x).append(", String> {\n");
					for (int y = 0; y < 30; y++) {
						sb.append("\t\t\tpublic final Object field_").append(j).append("_").append(x).append("_").append(y).append(" = createField(this);\n");
					}
					sb.append("\t\t}\n");
				}

				for (int y = 0; y < 30; y++) {
					sb.append("\t\tpublic final Object field_").append(j).append("_").append(y).append(" = createField(this);\n");
				}

				sb.append("\t}\n");
			}
			sb.append("}");

			this.env.addClass(root, "test", "TestClass" + i, sb.toString());
		}
	}

	@Override
	protected void tearDown() throws Exception {
		super.tearDown();

		this.env.resetWorkspace();
		JavaCore.setOptions(JavaCore.getDefaultOptions());
	}

	public void testHashCalculation() throws Exception {
		// should finish in under 2 minutes with the fix of #551
		tagAsSummary("PTBKey hash calculation", Dimension.CPU_TIME);
		for (int idx = 0; idx < 2; idx++) {
			startMeasuring();
			this.env.fullBuild();
			stopMeasuring();
		}

		commitMeasurements();
		assertPerformance();
	}
}
