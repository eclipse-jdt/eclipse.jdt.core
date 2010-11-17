/*******************************************************************************
 * Copyright (c) 2010 Walter Harley and others
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    eclipse@cafewalter.com - initial API and implementation
 *******************************************************************************/
package org.eclipse.jdt.apt.pluggable.tests.annotations;

/**
 * Marker interface to trigger the TestFinalRoundProc, which does nothing
 * normally but on the final round of processing generates a new Java type
 * that is annotated with this annotation.
 * <p>
 * See <a href="http://bugs.eclipse.org/329156">Bug 329156</a> and
 * <a href="http://bugs.sun.com/view_bug.do?bug_id=6634138">the
 * corresponding bug in javac</a>, which Sun fixed.
 * @since 3.7
 */
public @interface FinalRoundTestTrigger {
}
