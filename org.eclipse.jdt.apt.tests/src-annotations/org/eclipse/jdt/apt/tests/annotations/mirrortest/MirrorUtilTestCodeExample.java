/*******************************************************************************
 * Copyright (c) 2005, 2007 BEA Systems, Inc.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *    sbandow@bea.com - initial API and implementation
 *
 *******************************************************************************/

package org.eclipse.jdt.apt.tests.annotations.mirrortest;

/**
 * Holds information for the mirror tests.
 */
public class MirrorUtilTestCodeExample {

	public static final String CODE_PACKAGE = "testpackage";

	public static final String CODE_CLASS_NAME = "DeclarationsTestClass";

	public static final String CODE_FULL_NAME = CODE_PACKAGE + "." + CODE_CLASS_NAME;

	public static final String CODE =
		"""
		package testpackage;\
		
		import org.eclipse.jdt.apt.tests.annotations.mirrortest.MirrorUtilTestAnnotation;\
		
		@MirrorUtilTestAnnotation\
		
		public class DeclarationsTestClass\
		
		{\
		
		    public class A\
		
		    {\
		
		        public int field; \
		
		        public void method(){}\
		
		    }\
		
		    public class B extends A\
		
		    {\
		
		        public int field; \
		
		        public void method(){}\
		
		    }\
		
		    public class C\
		
		    {\
		
		        public int field; \
		
		        public void method(){}\
		
		    }\
		
		    public class D extends A\
		
		    {\
		
		    	public Object field;\
		
		    	public void method(String s){}\
		
		    }\
		
		    public interface I\
		
		    {\
		
		    	public int field = 1;\
		
		       public void method();\
		
		    }\
		
		    public interface J\
		
		    {\
		
		    	public int field = 2;\
		
		    }\
		
		    public class K implements I, J\
		
		    {\
		
		    	public int field;\
		
		       public void method(){}\
		
		    }\
		
		}""";

}
