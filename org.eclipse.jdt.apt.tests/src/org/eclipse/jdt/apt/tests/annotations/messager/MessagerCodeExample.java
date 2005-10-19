/*******************************************************************************
 * Copyright (c) 2005 BEA Systems, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   wharley@bea.com - initial API and implementation
 *******************************************************************************/

package org.eclipse.jdt.apt.tests.annotations.messager;


/**
 * Code examples for tests of Messager interface
 */
@SuppressWarnings("nls")
public class MessagerCodeExample {

	public static final String CODE_PACKAGE = "messagertestpackage";
	public static final String CODE_CLASS_NAME = "MessagerTestClass";
	public static final String CODE_FULL_NAME = CODE_PACKAGE + "." + CODE_CLASS_NAME;
	
	// These offsets work for each code example, as long as the examples are cumulative
	public static final int INFO_START = 219;
	public static final int INFO_END = 222;
	public static final int WARNING_START = 319;
	public static final int WARNING_END = 323;
	public static final int ERROR_START = 417;
	public static final int ERROR_END = 422;

	private static final String HEADER_CHUNK = 
		"package messagertestpackage;\n" +
		"import org.eclipse.jdt.apt.tests.annotations.messager.MessagerAnnotation;" + "\n" +
		"public class MessagerTestClass {" + "\n";

	private static final String INFO_CHUNK =
		"    @MessagerAnnotation(severity=MessagerAnnotation.Severity.INFO)" + "\n" +
		"    public void Foo() {" + "\n" +
		"    }" + "\n";

	private static final String WARNING_CHUNK =
		"    @MessagerAnnotation(severity=MessagerAnnotation.Severity.WARNING)" + "\n" +
		"    public void Bar() {" + "\n" +
		"    }" + "\n";

	private static final String ERROR_CHUNK =
		"    @MessagerAnnotation(severity=MessagerAnnotation.Severity.ERROR)" + "\n" +
		"    public void Quux() {" + "\n" +
		"    }" + "\n";
	
	private static final String OK_CHUNK =
		"    @MessagerAnnotation(severity=MessagerAnnotation.Severity.OK)" + "\n" +
		"    public void Foo() {" + "\n" +
		"    }" + "\n";

	/** Code example with INFO, WARNING, and ERROR severities */
	public static final String CODE1 = 
		HEADER_CHUNK +
		INFO_CHUNK +
		WARNING_CHUNK +
		ERROR_CHUNK +
		"}";

	/** Code example with INFO and WARNING severities */
	public static final String CODE2 = 
		HEADER_CHUNK +
		INFO_CHUNK +
		WARNING_CHUNK +
		"}";
	
	/** Code example with INFO severity only */
	public static final String CODE3 = 
		HEADER_CHUNK +
		INFO_CHUNK +
		"}";
	
	/** Code example with INFO severity only */
	public static final String CODE4 = 
		HEADER_CHUNK +
		OK_CHUNK +
		"}";
}
