/*******************************************************************************
 * Copyright (c) 2014 BEA Systems, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    het@google.com - initial API and implementation
 *******************************************************************************/
package org.eclipse.jdt.apt.tests.annotations.annotationvalue;

public class CodeExample {
	public static final String PACKAGE_TRIGGER = "trigger";
	public static final String TRIGGER_CLASS = "Trigger";
	public static final String TRIGGER_CODE =
		"package trigger; \n" +
		"\n" +
		"@MyTrigger \n" +
		"public class Trigger {}";

	public static final String MYTRIGGER_CLASS = "MyTrigger";
	public static final String MYTRIGGER_CODE =
		"package trigger; \n" +
		"\n" +
		"public @interface MyTrigger {}";
}
