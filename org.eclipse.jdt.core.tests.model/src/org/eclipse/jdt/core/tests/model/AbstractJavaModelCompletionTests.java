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

import org.eclipse.jdt.core.*;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.internal.codeassist.RelevanceConstants;

import junit.framework.*;

public abstract class AbstractJavaModelCompletionTests extends AbstractJavaModelTests implements RelevanceConstants {
	protected class CompletionResult {
		public String proposals;
		public String context;
	}
	Hashtable oldOptions;
	ICompilationUnit wc = null;
	WorkingCopyOwner owner = null; 
public AbstractJavaModelCompletionTests(String name) {
	super(name);
}
public ICompilationUnit getWorkingCopy(String path, String source) throws JavaModelException {
	return super.getWorkingCopy(path, source, this.owner, null);
}
protected CompletionResult complete(String path, String source, String completeBehind) throws JavaModelException {
	this.wc = getWorkingCopy(path, source);

	CompletionTestsRequestor2 requestor = new CompletionTestsRequestor2(true);
	String str = this.wc.getSource();
	int cursorLocation = str.lastIndexOf(completeBehind) + completeBehind.length();
	this.wc.codeComplete(cursorLocation, requestor, this.owner);
	
	CompletionResult result =  new CompletionResult();
	result.proposals = requestor.getResults();
	result.context = requestor.getContext();
	return result;
}
public void setUpSuite() throws Exception {
	super.setUpSuite();
	this.oldOptions = JavaCore.getOptions();
	
	waitUntilIndexesReady();
}
protected void setUp() throws Exception {
	super.setUp();
	
	this.owner = new WorkingCopyOwner(){};
}
public void tearDownSuite() throws Exception {
	JavaCore.setOptions(oldOptions);
	
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
