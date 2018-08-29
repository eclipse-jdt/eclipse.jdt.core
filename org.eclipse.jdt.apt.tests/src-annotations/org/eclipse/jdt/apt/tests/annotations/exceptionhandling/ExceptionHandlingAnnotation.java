/*******************************************************************************
 * Copyright (c) 2007 BEA Systems, Inc.
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
 *******************************************************************************/

package org.eclipse.jdt.apt.tests.annotations.exceptionhandling;

public @interface ExceptionHandlingAnnotation {
	public enum EHAEnum { A, B }
	EHAEnum[] enumsValue() default { EHAEnum.A };
	boolean booleanValue() default false;
	String strValue() default "";
	String[] arrValue () default {""};
}
