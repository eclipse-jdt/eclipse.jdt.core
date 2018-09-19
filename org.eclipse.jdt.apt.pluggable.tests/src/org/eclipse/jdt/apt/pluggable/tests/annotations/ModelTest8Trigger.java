/*******************************************************************************
 * Copyright (c) 2014 Jesper Steen Moller and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *    Jesper Steen Moller - initial API and implementation
 *
 *******************************************************************************/

package org.eclipse.jdt.apt.pluggable.tests.annotations;

public @interface ModelTest8Trigger {
	/** Name of test method to run */
	String test();

	/** Arbitrary argument */
	String arg0() default "";

	/** Arbitrary argument */
	String arg1() default "";
}
