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
		"package mirrortestpackage;\n" +
		"import org.eclipse.jdt.apt.tests.annotations.mirrortest.MirrorDeclarationTestAnnotation;" + "\n" +
		"@MirrorDeclarationTestAnnotation(s=\"fred\", value=5)" + "\n" +
		"public class DeclarationTestClass {" + "\n" +
		"	public class ClassDec {" + "\n" +
		"		public ClassDec() {}" + "\n" +
		"		public ClassDec(int j) {}" + "\n" +
		"		public static final int i = 1;" + "\n" +
		"		public float f;" + "\n" +
		"		public static final String s = \"hello\";" + "\n" +
		"		public java.util.GregorianCalendar gc;" + "\n" +
		"		public void methodDec(int k, String... t) throws Exception {}" + "\n" +
		"		public void methodDecNoArg(){}" + "\n" +
		"		@MirrorDeclarationTestAnnotation() public Object foo(Object o) throws Exception {\n" +
		"			return null;\n" +
		"		}\n" +
		"		@MirrorDeclarationTestAnnotation() public Object foo2(Object o) throws Exception {\n" +
		"			return null;\n" +
		"		}\n" +
		"		@MirrorDeclarationTestAnnotation() public Object foo3(Object o) throws Exception {\n" +
		"			return null;\n" +
		"		}\n" +
		"	}" + "\n" +
		"	public enum EnumDec {" + "\n" +
		"		aardvark, anteater" + "\n" +
		"	}" + "\n" +
		"}";
}
