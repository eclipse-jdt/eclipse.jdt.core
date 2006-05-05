/*******************************************************************************
 * Copyright (c) 2005 BEA Systems, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    wharley@bea.com - initial API and implementation
 *******************************************************************************/


package org.eclipse.jdt.apt.tests;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

/**
 * Run all annotation processor tests.
 * Annotation processors may be registered by using this test plugin to extend 
 * <code>org.eclipse.jdt.apt.core.annotationProcessorFactory</code>, providing
 * the name of an annotation processor factory class implemented in this plugin.
 */
public class TestAll extends TestCase {
	
	public TestAll(String testName) 
	{
		super(testName);
	}
	
	public static Test suite() 
	{
		TestSuite suite = new TestSuite();
		
		suite.addTest(AptReconcileTests.suite());
		suite.addTest(AptBuilderTests.suite() );
		suite.addTest(APITests.suite());
		suite.addTest(MirrorTests.suite());
		suite.addTest(ReadAnnotationTests.suite());
		suite.addTest(PreferencesTests.suite());
		suite.addTest(FactoryLoaderTests.suite());
		suite.addTest(MirrorDeclarationTests.suite());
		suite.addTest(MirrorUtilTests.suite());
		suite.addTest(AnnotationValueConversionTests.suite());
		suite.addTest(JavaVersionTests.suite());
		suite.addTest(RegressionTests.suite());
		suite.addTest(FileGenerationTests.suite());
		suite.addTest(MixedModeTesting.suite());
		suite.addTest(ExceptionHandlingTests.suite());
		suite.addTest(ScannerTests.suite());
	
		return suite;
		
	}
}
