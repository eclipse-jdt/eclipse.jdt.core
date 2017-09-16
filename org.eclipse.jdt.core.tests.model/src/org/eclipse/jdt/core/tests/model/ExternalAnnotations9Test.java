/*******************************************************************************
 * Copyright (c) 2017 Till Brychcy and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Till Brychcy - initial API and implementation
 *******************************************************************************/
package org.eclipse.jdt.core.tests.model;

import java.util.Hashtable;

import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IncrementalProjectBuilder;
import org.eclipse.jdt.core.IJavaModelMarker;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.tests.util.AbstractCompilerTest;

import junit.framework.Test;

@SuppressWarnings({"rawtypes", "unchecked"})
public class ExternalAnnotations9Test extends ExternalAnnotations18Test {

	public ExternalAnnotations9Test(String name) {
		super(name, "9", "JCL19_LIB");
	}
	
	// Static initializer to specify tests subset using TESTS_* static variables
	// All specified tests which do not belong to the class are skipped...
	static {
//		TESTS_NAMES = new String[] {"testBug522401"};
	}

	public static Test suite() {
		return buildModelTestSuite(ExternalAnnotations9Test.class, BYTECODE_DECLARATION_ORDER);
	}

	public String getSourceWorkspacePath() {
		// we read individual projects from within this folder:
		return super.getSourceWorkspacePathBase()+"/ExternalAnnotations9";
	}

	protected boolean hasJRE19() {
		return ((AbstractCompilerTest.getPossibleComplianceLevels() & AbstractCompilerTest.F_9) != 0);
	}

	/** Project with real JRE. */
	public void testBug522401() throws Exception {
		if (!hasJRE19()) {
			System.out.println("Skipping ExternalAnnotations9Test.testBug522401(), needs JRE9");
			return;
		}
		Hashtable options = JavaCore.getOptions();
		try {
			setupJavaProject("Test2");
			this.project.getProject().build(IncrementalProjectBuilder.FULL_BUILD, null);
			IMarker[] markers = this.project.getProject().findMarkers(IJavaModelMarker.JAVA_MODEL_PROBLEM_MARKER, false, IResource.DEPTH_INFINITE);
			assertNoMarkers(markers);
		} finally {
			JavaCore.setOptions(options);
		}
	}
}
