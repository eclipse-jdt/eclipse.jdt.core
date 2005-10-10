/*******************************************************************************
 * Copyright (c) 2000, 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jdt.core.tests.model;

import java.util.Hashtable;
import java.util.List;

import org.eclipse.jdt.core.*;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.internal.codeassist.RelevanceConstants;

import junit.framework.*;

public abstract class AbstractJavaModelCompletionTests extends AbstractJavaModelTests implements RelevanceConstants {
	public static List COMPLETION_SUITES = null;
	protected static IJavaProject COMPLETION_PROJECT;
	protected class CompletionResult {
		public String proposals;
		public String context;
		public int cursorLocation;
	}
	Hashtable oldOptions;
	ICompilationUnit wc = null;
public AbstractJavaModelCompletionTests(String name) {
	super(name);
}
public ICompilationUnit getWorkingCopy(String path, String source) throws JavaModelException {
	return super.getWorkingCopy(path, source, this.wcOwner, null);
}
protected CompletionResult complete(String path, String source, String completeBehind) throws JavaModelException {
	return this.complete(path, source, false, completeBehind);
}
protected CompletionResult complete(String path, String source, boolean showPositions, String completeBehind) throws JavaModelException {
	this.wc = getWorkingCopy(path, source);

	CompletionTestsRequestor2 requestor = new CompletionTestsRequestor2(true, false, showPositions);
	String str = this.wc.getSource();
	int cursorLocation = str.lastIndexOf(completeBehind) + completeBehind.length();
	this.wc.codeComplete(cursorLocation, requestor, this.wcOwner);
	
	CompletionResult result =  new CompletionResult();
	result.proposals = requestor.getResults();
	result.context = requestor.getContext();
	result.cursorLocation = cursorLocation;
	return result;
}
public void setUpSuite() throws Exception {
	super.setUpSuite();
	this.oldOptions = JavaCore.getOptions();
	waitUntilIndexesReady();
}
protected void setUp() throws Exception {
	super.setUp();
	this.wcOwner = new WorkingCopyOwner(){};
}
public void tearDownSuite() throws Exception {
	JavaCore.setOptions(oldOptions);
		if (COMPLETION_SUITES == null) {
			deleteProject("Completion");
		} else {
			COMPLETION_SUITES.remove(getClass());
			if (COMPLETION_SUITES.size() == 0) {
				deleteProject("Completion");
				COMPLETION_SUITES = null;
			}
		}
	super.tearDownSuite();
}
protected void tearDown() throws Exception {
	if(this.wc != null) {
		this.wc.discardWorkingCopy();
	}
	super.tearDown();
}
protected void assertResults(String expected, String actual) {
	try {
		assertEquals(expected, actual);
	} catch(ComparisonFailure c) {
		System.out.println(actual);
		System.out.println();
		throw c;
	}
}
}
