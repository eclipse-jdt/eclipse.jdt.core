/*******************************************************************************
 * Copyright (c) 2007, 2018 BEA Systems, Inc.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *    wharley@bea.com - initial API and implementation
 *
 *******************************************************************************/

package org.eclipse.jdt.apt.pluggable.tests.annotations;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 *
 * @since 3.4
 */
@Retention(RetentionPolicy.RUNTIME)
public @interface FilerTestTrigger {
	/** Name of test method to run */
	String test();

	/** Arbitrary argument */
	String arg0() default "";

	/** Arbitrary argument */
	String arg1() default "";
}
