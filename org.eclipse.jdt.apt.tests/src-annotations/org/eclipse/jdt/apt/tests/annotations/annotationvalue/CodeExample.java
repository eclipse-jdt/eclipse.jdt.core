/*******************************************************************************
 * Copyright (c) 2014 BEA Systems, Inc.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
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
