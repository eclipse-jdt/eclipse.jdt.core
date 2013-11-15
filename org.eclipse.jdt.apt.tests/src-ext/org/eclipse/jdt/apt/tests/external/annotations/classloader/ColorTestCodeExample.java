/*******************************************************************************
 * Copyright (c) 2005 BEA Systems, Inc. 
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
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
		"package colortestpackage;\r\n" + 
		"\r\n" + 
		"import org.eclipse.jdt.apt.tests.external.annotations.classloader.Color;\r\n" + 
		"import org.eclipse.jdt.apt.tests.external.annotations.classloader.ColorAnnotation;\r\n" + 
		"import org.eclipse.jdt.apt.tests.external.annotations.classloader.ColorWrapper;\r\n" + 
		"\r\n" + 
		"@ColorAnnotation(color = Color.RED)\r\n" + 
		"@ColorWrapper(colors = {@ColorAnnotation(color = Color.GREEN), @ColorAnnotation(color = Color.BLUE)})\r\n" + 
		"public class ColorTest {\r\n" + 
		"\r\n" + 
		"}";
}
