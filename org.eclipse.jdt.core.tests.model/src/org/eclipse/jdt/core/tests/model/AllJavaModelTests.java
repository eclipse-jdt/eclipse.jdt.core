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

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

/**
 * Run all java model tests.
 */
public class AllJavaModelTests extends TestCase {
public AllJavaModelTests(String name) {
	super(name);
}
public static Test suite() {
	TestSuite suite = new TestSuite(AllJavaModelTests.class.getName());
	// Enter each test here, grouping the tests that are related

	// creation of method
	suite.addTest(CreateMembersTests.suite());
	
	// Java Naming convention tests
	suite.addTest(JavaConventionTests.suite());

	// Project & Root API unit tests
	suite.addTest(JavaProjectTests.suite());

	// Compilation unit tests
	suite.addTest(CompilationUnitTests.suite());

	// Source attachment tests
	suite.addTest(AttachSourceTests.suite());
	
	//Create type source tests
	suite.addTest(CreateTypeSourceExamplesTests.suite());

	//Create method source tests
	suite.addTest(CreateMethodSourceExamplesTests.suite());
		
	// Java search tests
	suite.addTest(JavaSearchTests.suite());
	suite.addTest(JavaSearchMultipleProjectsTests.suite());
	suite.addTest(WorkingCopySearchTests.suite());
	suite.addTest(SearchTests.suite());
		
	// Working copy tests
	suite.addTest(WorkingCopyTests.suite());
	suite.addTest(WorkingCopyNotInClasspathTests.suite());
	suite.addTest(HierarchyOnWorkingCopiesTests.suite());
	
	// test IJavaModel
	suite.addTest(JavaModelTests.suite());

	// tests to check the encoding
	suite.addTest(EncodingTests.suite());
	
	// test class name with special names like names containing '$'
	suite.addTest(ClassNameTests.suite());
	
	// IBuffer tests
	suite.addTest(BufferTests.suite());

	// Name lookup tests
	suite.addTest(NameLookupTests2.suite());

	// Classpath and output location tests
	suite.addTest(ClasspathTests.suite());

	// Delta tests
	suite.addTest(JavaElementDeltaTests.suite());
	suite.addTest(ExternalJarDeltaTests.suite());

	// Java element existence tests
	suite.addTest(ExistenceTests.suite());
	
	// Support for "open on" feature tests
	suite.addTest(ResolveTests.suite());
	
	// Support for completion tests
	suite.addTest(CompletionTests.suite());
	suite.addTest(CompletionTests2.suite());
	suite.addTest(SnippetCompletionTests.suite());
	
	// Prefix and suffix tests
	suite.addTest(NamingConventionTests.suite());
	
	// Code correction tests
	//suite.addTest(CodeCorrectionTests.suite());
	
	// Options tests
	suite.addTest(OptionTests.suite());
	
	// Type hierarchy tests
	suite.addTest(TypeHierarchyTests.suite());
	suite.addTest(TypeHierarchyNotificationTests.suite());
	suite.addTest(TypeHierarchySerializationTests.suite());
	
	// Resolve type tests
	suite.addTest(TypeResolveTests.suite());

	// Reconciler tests
	suite.addTest(ReconcilerTests.suite());

	// Copy and move operation tests
	suite.addTest(CopyMoveElementsTests.suite());
	suite.addTest(CopyMoveResourcesTests.suite());

	// Rename tests
	suite.addTest(RenameTests.suite());
	
	// Exclusion patterns tests
	suite.addTest(ExclusionPatternsTests.suite());
	
	// Signature tests
	suite.addTest(SignatureTests.suite());
	
	// Variable initializers and container initializers tests
	suite.addTest(ClasspathInitializerTests.suite());

	// Java Model Factory tests
	suite.addTest(FactoryTests.suite());
			
	// Java Element persistence tests
	suite.addTest(MementoTests.suite());
	
	// Java Element sorting tests
	suite.addTest(SortCompilationUnitElementsTests.suite());

	// Package fragment root manipulation tests
	suite.addTest(RootManipulationsTests.suite());

	return suite;
}

}
