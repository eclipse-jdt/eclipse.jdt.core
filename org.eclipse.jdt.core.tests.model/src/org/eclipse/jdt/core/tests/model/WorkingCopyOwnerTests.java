/*******************************************************************************
 * Copyright (c) 2000, 2003 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jdt.core.tests.model;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.core.IBuffer;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.WorkingCopyOwner;

import junit.framework.Test;

/**
 * Tests APIs that take a WorkingCopyOwner.
 */
public class WorkingCopyOwnerTests extends ModifyingResourceTests {

	public class TestWorkingCopyOwner extends WorkingCopyOwner {
		public IBuffer createBuffer(ICompilationUnit workingCopy) {
			return new TestBuffer(workingCopy);
		}
		
		public String toString() {
			return "Test working copy owner";
		}
	}
	
	public static Test suite() {
		return new Suite(WorkingCopyOwnerTests.class);
	}

	public WorkingCopyOwnerTests(String name) {
		super(name);
	}

	/*
	 * Tests that a primary compilation unit can become a working copy.
	 */
	public void testBecomeWorkingCopy1() throws CoreException {
		ICompilationUnit cu = null;
		try {
			createJavaProject("P");
			createFile(
				"P/x.java",
				"public class X {\n" +
				"}"
			);
			cu = getCompilationUnit("P/X.java");
			assertTrue("should not be in working copy mode", !cu.isWorkingCopy());
			
			cu.becomeWorkingCopy(null, null);
			assertTrue("should be in working copy mode", cu.isWorkingCopy());
		} finally {
			if (cu != null) {
				while (cu.isWorkingCopy()) {
					cu.discardWorkingCopy();
				}
			}
			deleteProject("P");
		}
	}
}
