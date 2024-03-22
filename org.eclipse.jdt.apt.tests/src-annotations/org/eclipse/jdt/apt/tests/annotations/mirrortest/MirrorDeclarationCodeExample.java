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

public class MirrorDeclarationCodeExample
{
	public static final String CODE_PACKAGE = "mirrortestpackage";
	public static final String CODE_CLASS_NAME = "DeclarationTestClass";
	public static final String CODE_FULL_NAME = CODE_PACKAGE + "." + CODE_CLASS_NAME;

	public static final String CODE =
		"""
		package mirrortestpackage;
		import org.eclipse.jdt.apt.tests.annotations.mirrortest.MirrorDeclarationTestAnnotation;\
		
		@MirrorDeclarationTestAnnotation(s="fred", value=5)\
		
		public class DeclarationTestClass {\
		
			public class ClassDec {\
		
				public ClassDec() {}\
		
				public ClassDec(int j) {}\
		
				public static final int i = 1;\
		
				public float f;\
		
				public static final String s = "hello";\
		
				public java.util.GregorianCalendar gc;\
		
				public void methodDec(int k, String... t) throws Exception {}\
		
				public void methodDecNoArg(){}\
		
				@MirrorDeclarationTestAnnotation() public Object foo(Object o) throws Exception {
					return null;
				}
				@MirrorDeclarationTestAnnotation() public Object foo2(Object o) throws Exception {
					return null;
				}
				@MirrorDeclarationTestAnnotation() public Object foo3(Object o) throws Exception {
					return null;
				}
			}\
		
			public enum EnumDec {\
		
				aardvark, anteater\
		
			}\
		
		}""";
}
