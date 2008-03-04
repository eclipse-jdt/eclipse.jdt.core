/*******************************************************************************
 * Copyright (c) 2008 BEA Systems, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
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
@interface A1 {}

interface Negative7 extends M11 {
	@A1 int method1(@A1 int arg0);
}

interface Negative7A extends Cloneable {
	@A1 int method1(@A1 int arg0);
}


