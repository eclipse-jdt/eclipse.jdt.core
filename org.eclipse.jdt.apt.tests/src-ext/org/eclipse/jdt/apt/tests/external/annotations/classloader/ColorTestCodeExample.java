/*******************************************************************************
 * Copyright (c) 2005 BEA Systems, Inc.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *    jgarms@bea.com - initial API and implementation
 *
 *******************************************************************************/
package org.eclipse.jdt.apt.tests.external.annotations.classloader;

public class ColorTestCodeExample {

	public static final String CODE_PACKAGE = "colortestpackage";
	public static final String CODE_CLASS_NAME = "ColorTest";
	public static final String CODE_FULL_NAME = CODE_PACKAGE + "." + CODE_CLASS_NAME;

	public static final String CODE =
		"""
		package colortestpackage;\r
		\r
		import org.eclipse.jdt.apt.tests.external.annotations.classloader.Color;\r
		import org.eclipse.jdt.apt.tests.external.annotations.classloader.ColorAnnotation;\r
		import org.eclipse.jdt.apt.tests.external.annotations.classloader.ColorWrapper;\r
		\r
		@ColorAnnotation(color = Color.RED)\r
		@ColorWrapper(colors = {@ColorAnnotation(color = Color.GREEN), @ColorAnnotation(color = Color.BLUE)})\r
		public class ColorTest {\r
		\r
		}""";
}
