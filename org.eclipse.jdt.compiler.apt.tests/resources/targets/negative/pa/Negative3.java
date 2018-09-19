/*******************************************************************************
 * Copyright (c) 2007 BEA Systems, Inc.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *******************************************************************************/
package targets.negative.pa;

/**
 * This code is syntactic but contains semantic errors due to missing types.
 * All the M* types (M for "Missing") are expected to be unresolved.
 * The desired behavior is specified in the javadoc for package 
 * javax.lang.model.element: in general, missing types should be replaced
 * by empty types with the same name.
 */
class Negative3 {
	M1 foo(M2.M3.M4 param) {}
}

interface I2 extends MI1 {
	M5 boo(M6.M7.M8 param);
}