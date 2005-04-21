/*******************************************************************************
 * Copyright (c) 2005 BEA Systems, Inc. 
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    mkaufman@bea.com - initial API and implementation
 *    
 *******************************************************************************/

package org.eclipse.jdt.apt.tests.jdtcoretests;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

public class AllJdtCoreTests extends TestCase {
	
	public AllJdtCoreTests(String testName) 
	{
		super(testName);
	}
	

	public static Class[] getAllTestClasses()
	{
		//
		// please be careful of the ordering of the test classes below. 
		// the tests passing successfully seems to be dependent on the order
		// the test classes are specified.
		//
		Class[] classes = { 
			org.eclipse.jdt.core.tests.builder.Tests.class,
			org.eclipse.jdt.core.tests.formatter.FormatterRegressionTests.class,
			org.eclipse.jdt.core.tests.dom.RunAllTests.class,
			org.eclipse.jdt.core.tests.model.AllJavaModelTests.class,			 
			org.eclipse.jdt.core.tests.compiler.parser.TestAll.class,
			org.eclipse.jdt.core.tests.eval.TestAll.class,
			org.eclipse.jdt.core.tests.compiler.regression.TestAll.class
		};
		
		return classes;
	}

	public static TestSuite suite()
	{
		TestSuite ts = new TestSuite( AllJdtCoreTests.class.getName() );

		Class[] testClasses = getAllTestClasses();
		for( int i = 0; i < testClasses.length; i++ )
		{
			Class testClass = testClasses[i];

			// call the suite() method and add the resulting suite
			// to the suite
			try
			{
				Method suiteMethod = testClass.getDeclaredMethod(
					"suite", new Class[0] ); //$NON-NLS-1$
				Test suite = ( Test ) suiteMethod.invoke( null, new Object[0] );
				ts.addTest( suite );
			}
			catch( IllegalAccessException e )
			{
				e.printStackTrace();
			}
			catch( InvocationTargetException e )
			{
				e.getTargetException().printStackTrace();
			}
			catch( NoSuchMethodException e )
			{

			}
		}
		return ts;
	}

}

