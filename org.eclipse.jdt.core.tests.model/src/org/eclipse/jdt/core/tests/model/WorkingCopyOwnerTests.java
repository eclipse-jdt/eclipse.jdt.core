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

	public void setUpSuite() throws Exception {
		createJavaProject("P");
		createFile(
			"P/X.java",
			"public class X {\n" +
			"}"
		);
	}

	public void tearDownSuite() throws Exception {
		deleteProject("P");
	}

	/*
	 * Tests that a primary compilation unit can become a working copy.
	 */
	public void testBecomeWorkingCopy1() throws CoreException {
		ICompilationUnit cu = null;
		try {
			cu = getCompilationUnit("P/X.java");
			assertTrue("should not be in working copy mode", !cu.isWorkingCopy());
			
			cu.becomeWorkingCopy(null, null);
			assertTrue("should be in working copy mode", cu.isWorkingCopy());
		} finally {
			if (cu != null) {
				cu.discardWorkingCopy();
			}
		}
	}
	
	/*
	 * Tests that a working copy remains a working copy when becomeWorkingCopy() is called.
	 */
	public void testBecomeWorkingCopy2() throws CoreException {
		ICompilationUnit workingCopy = null;
		try {
			workingCopy = getCompilationUnit("P/X.java").getWorkingCopy(new TestWorkingCopyOwner(), null, null);
			assertTrue("should be in working copy mode", workingCopy.isWorkingCopy());
			
			workingCopy.becomeWorkingCopy(null, null);
			assertTrue("should still be in working copy mode", workingCopy.isWorkingCopy());
		} finally {
			if (workingCopy != null) {
				workingCopy.discardWorkingCopy();
			}
		}
	}

	/*
	 * Tests that a primary working copy is back in compilation unit mode when discardWorkingCopy() is called.
	 */
	public void testDiscardWorkingCopy1() throws CoreException {
		ICompilationUnit cu = null;
		try {
			cu = getCompilationUnit("P/X.java");
			cu.becomeWorkingCopy(null, null);
			assertTrue("should be in working copy mode", cu.isWorkingCopy());
			
			cu.discardWorkingCopy();
			assertTrue("should no longer be in working copy mode", !cu.isWorkingCopy());
		} finally {
			if (cu != null) {
				cu.discardWorkingCopy();
			}
		}
	}

	/*
	 * Tests that the same number of calls to discardWorkingCopy() is needed for primary working copy to be back 
	 * in compilation uint mode.
	 */
	public void testDiscardWorkingCopy2() throws CoreException {
		ICompilationUnit cu = null;
		try {
			cu = getCompilationUnit("P/X.java");
			cu.becomeWorkingCopy(null, null);
			cu.becomeWorkingCopy(null, null);
			cu.becomeWorkingCopy(null, null);
			assertTrue("should be in working copy mode", cu.isWorkingCopy());
			
			cu.discardWorkingCopy();
			assertTrue("should still be in working copy mode", cu.isWorkingCopy());

			cu.discardWorkingCopy();
			cu.discardWorkingCopy();
			assertTrue("should no longer be in working copy mode", !cu.isWorkingCopy());
		} finally {
			if (cu != null) {
				int max = 3;
				while (cu.isWorkingCopy() && max-- > 0) {
					cu.discardWorkingCopy();
				}
			}
		}
	}

	/*
	 * Tests that the same number of calls to discardWorkingCopy() is needed for non-primary working copy 
	 * to be dicsarded.
	 */
	public void testDiscardWorkingCopy3() throws CoreException {
		ICompilationUnit workingCopy = null;
		try {
			ICompilationUnit cu = getCompilationUnit("P/X.java");
			TestWorkingCopyOwner owner = new TestWorkingCopyOwner();
			workingCopy = cu.getWorkingCopy(owner, null, null);
			workingCopy = cu.getWorkingCopy(owner, null, null);
			workingCopy = cu.getWorkingCopy(owner, null, null);
			assertTrue("should be in working copy mode", workingCopy.isWorkingCopy());
			
			workingCopy.discardWorkingCopy();
			assertTrue("should still be in working copy mode", workingCopy.isWorkingCopy());

			workingCopy.discardWorkingCopy();
			workingCopy.discardWorkingCopy();
			assertTrue("should no longer be in working copy mode", !workingCopy.isWorkingCopy());
		} finally {
			if (workingCopy != null) {
				int max = 3;
				while (workingCopy.isWorkingCopy() && max-- > 0) {
					workingCopy.discardWorkingCopy();
				}
			}
		}
	}

}
