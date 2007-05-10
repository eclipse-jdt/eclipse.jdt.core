/*******************************************************************************
 * Copyright (c) 2006 BEA Systems, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    sbandow@bea.com - initial API and implementation
 *******************************************************************************/

package org.eclipse.jdt.apt.tests.annotations.exceptionhandling;

public @interface ExceptionHandlingAnnotation {
	public enum EHAEnum { A, B };
	EHAEnum[] enumsValue() default { EHAEnum.A };
	boolean booleanValue() default false;
	String strValue() default "";
	String[] arrValue () default {""};
}
