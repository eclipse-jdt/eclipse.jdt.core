/*******************************************************************************
 * Copyright (c) 2008 BEA Systems, Inc.
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
 * Test case for annotations on non-generic parameters.
 * This code contains missing types, including missing annotation types.  
 * The missing types all start with 'M'.
 * The desired behavior is specified in the javadoc for package 
 * javax.lang.model.element: in general, missing types should be replaced
 * by empty types with the same name.
 */
class Negative6 {
	@M11 M12 method1(@M13(@M14(@M15)) M16 arg0) { return null; }
	@M21("foo") String method2(@M22(value2 = "bar") int arg0) { return null; }
	String method3(@M31(1) String arg0, @M32(2) String arg1) { return null; }
	@M41 Negative6(@M42 M43 arg0) {}
}


