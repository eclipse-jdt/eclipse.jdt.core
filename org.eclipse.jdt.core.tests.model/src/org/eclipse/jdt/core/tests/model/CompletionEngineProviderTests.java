/*******************************************************************************
 * Copyright (c) 2025 Red Hat, Inc. and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *******************************************************************************/
package org.eclipse.jdt.core.tests.model;

import junit.framework.Test;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.internal.compiler.impl.CompilerOptions;

/**
 * Test for the `org.eclipse.jdt.core.completionEngineProvider` extension point
 * and {@link org.eclipse.jdt.internal.core.CompletionEngineProviderDiscovery}.
 */
@SuppressWarnings("javadoc")
public class CompletionEngineProviderTests extends AbstractJavaModelCompletionTests {

	private String oldSystemProperty;
	private static final String ENGINE_PROPERTY = "ICompletionEngineProvider";

	public CompletionEngineProviderTests(String name) {
		super(name);
	}

	public static Test suite() {
		return buildModelTestSuite(CompletionEngineProviderTests.class, ALPHABETICAL_SORT);
	}

	@Override
	public void setUpSuite() throws Exception {
		super.setUpSuite();
		setupExternalJCL("jclMin");
		if (COMPLETION_PROJECT == null)  {
			COMPLETION_PROJECT = setUpJavaProject("Completion");
		} else {
			setUpProjectCompliance(COMPLETION_PROJECT, CompilerOptions.getFirstSupportedJavaVersion());
		}
		this.currentProject = COMPLETION_PROJECT;
	}

	@Override
	protected void setUp() throws Exception {
		super.setUp();
		this.oldSystemProperty = System.getProperty(ENGINE_PROPERTY);
		System.setProperty(ENGINE_PROPERTY, "org.eclipse.jdt.core.tests.model.TestCompletionEngineProvider");
	}

	@Override
	protected void tearDown() throws Exception {
		super.tearDown();
		if (this.oldSystemProperty != null) {
			System.setProperty(ENGINE_PROPERTY, this.oldSystemProperty);
		} else {
			System.clearProperty(ENGINE_PROPERTY);
		}
	}

	public void testAlwaysProvidesSameResult() throws JavaModelException {
		this.workingCopies = new ICompilationUnit[1];
		this.workingCopies[0] = getWorkingCopy(
			"/Completion/src/ArrayInitializer.java",
			"public class ArrayInitializer {\n"+
			"	int bar() {return 0;}\n"+
			"	void foo(int[] i) {\n"+
			"		i = new int[] {\n"+
			"			bar()\n"+
			"		};\n"+
			"	}\n"+
			"}\n");

		CompletionTestsRequestor2 requestor = new CompletionTestsRequestor2();

		String str = this.workingCopies[0].getSource();
		String completeBehind = "bar(";
		int cursorLocation = str.lastIndexOf(completeBehind) + completeBehind.length();
		this.workingCopies[0].codeComplete(cursorLocation, requestor, this.wcOwner);

		// this result would not be returned in the default CompletionEngine
		assertResults(
				"test[FIELD_REF]{test, null, null, test, 100}",
				requestor.getResults());
	}

}
