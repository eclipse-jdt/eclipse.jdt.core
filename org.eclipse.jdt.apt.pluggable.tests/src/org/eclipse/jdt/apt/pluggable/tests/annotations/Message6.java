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
 *    wharley@bea.com - initial API and implementation
 *******************************************************************************/

package org.eclipse.jdt.apt.pluggable.tests.annotations;

import javax.tools.Diagnostic;


/**
 * This annotation may be processed to produce a log entry,
 * warning, or error via the Messager API.
 */
public @interface Message6 {
	/**
	 * Message severity.  INFO will be reported as a log entry.
	 */
	Diagnostic.Kind value() default Diagnostic.Kind.WARNING;

	/**
	 * Optional message text
	 */
	String text() default "";
}
