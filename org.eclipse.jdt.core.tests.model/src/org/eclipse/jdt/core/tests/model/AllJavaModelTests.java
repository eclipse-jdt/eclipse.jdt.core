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

import org.eclipse.jdt.core.tests.junit.extension.TestCase;

import junit.framework.Test;
import junit.framework.TestSuite;

/**
 * Run all java model tests.
 */
public class AllJavaModelTests extends junit.framework.TestCase {
public AllJavaModelTests(String name) {
	super(name);
}
static Class[] getAllTestClasses() {
	return new Class[] {
		CreateMembersTests.class,
		JavaConventionTests.class,
		JavaProjectTests.class,
		CompilationUnitTests.class,
		AttachSourceTests.class,
		RunJavaSearchTests.class,
		WorkingCopyTests.class,
		WorkingCopyNotInClasspathTests.class,
		HierarchyOnWorkingCopiesTests.class,
		JavaModelTests.class,
		EncodingTests.class,
		ClassNameTests.class,
		BufferTests.class,
		NameLookupTests2.class,
		ClasspathTests.class,
		JavaElementDeltaTests.class,
		ExternalJarDeltaTests.class,
		ExistenceTests.class,
		ResolveTests.class,
		ResolveTests_1_5.class,
		SelectionJavadocModelTests.class,
		CompletionTests.class,
		CompletionTests2.class,
		CompletionTests_1_5.class,
		SnippetCompletionTests.class,
		NamingConventionTests.class,
		CodeCorrectionTests.class,
		OptionTests.class,
		TypeHierarchyTests.class,
		TypeHierarchyNotificationTests.class,
		TypeHierarchySerializationTests.class,
		TypeResolveTests.class,
		ReconcilerTests.class,
		CopyMoveElementsTests.class,
		CopyMoveResourcesTests.class,
		RenameTests.class,
		ExclusionPatternsTests.class,
		InclusionPatternsTests.class,
		SignatureTests.class,
		ClasspathInitializerTests.class,
		FactoryTests.class,
		MementoTests.class,
		SortCompilationUnitElementsTests.class,
		RootManipulationsTests.class,
		OverflowingCacheTests.class,
		WorkingCopyOwnerTests.class,
		DeleteTests.class,
		LocalElementTests.class,
		GetSourceTests.class,
		CreatePackageTests.class,
		CreateCompilationUnitTests.class,
		ClassFileTests.class,
		BindingKeyTests.class
	};
}
public static Test suite() {
	TestSuite suite = new TestSuite(AllJavaModelTests.class.getName());

	// Enter each test here, grouping the tests that are related

	// Hack to load all classes before computing their suite of test cases
	// this allow to reset test cases subsets while running all Java Model tests...
	getAllTestClasses();

	// Reset forgotten subsets of tests
	TestCase.TESTS_PREFIX = null;
	TestCase.TESTS_NAMES = null;
	TestCase.TESTS_NUMBERS = null;
	TestCase.TESTS_RANGE = null;

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
	
	// Java search tests
	suite.addTest(RunJavaSearchTests.suite());
		
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
	suite.addTest(ResolveTests_1_5.suite());
	
	// Support for completion tests
	suite.addTest(CompletionTests.suite());
	suite.addTest(CompletionTests2.suite());
	suite.addTest(SnippetCompletionTests.suite());
	suite.addTest(CompletionTests_1_5.suite());
	
	// Prefix and suffix tests
	suite.addTest(NamingConventionTests.suite());
	
	// Code correction tests
	suite.addTest(CodeCorrectionTests.suite());
	
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
	
	// Inclusion patterns tests
	suite.addTest(InclusionPatternsTests.suite());
	
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
	
	// Owverflowing cache tests
	suite.addTest(OverflowingCacheTests.suite());
	
	// Working copy owner tests
	suite.addTest(WorkingCopyOwnerTests.suite());

	// Delete Java element tests
	suite.addTest(DeleteTests.suite());
	
	// Local element tests
	suite.addTest(LocalElementTests.suite());
	
	// Get source tests
	suite.addTest(GetSourceTests.suite());
		
	// Create packages tests
	suite.addTest(CreatePackageTests.suite());

	// Create compilation units tests
	suite.addTest(CreateCompilationUnitTests.suite());
	
	// Create search participant tests
	suite.addTest(SearchParticipantTests.suite());
	
	// Class file tests
	suite.addTest(ClassFileTests.suite());

	includeDeprecatedJDOMTests(suite);

	return suite;
}

/**
 * @deprecated JDOM is obsolete
 */
private static void includeDeprecatedJDOMTests(TestSuite suite) {
	//Create type source tests
	suite.addTest(CreateTypeSourceExamplesTests.suite());

	//Create method source tests
	suite.addTest(CreateMethodSourceExamplesTests.suite());
}

}
