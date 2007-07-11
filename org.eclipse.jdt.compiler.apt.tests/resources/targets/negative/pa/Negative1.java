/*******************************************************************************
 * Copyright (c) 2007 BEA Systems, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package targets.negative.pa;

/**
 * This code is syntactic but contains semantic errors due to missing types.
 * All the A* and Missing* types are expected to be unresolved.
 * The desired behavior is specified in the javadoc for package 
 * javax.lang.model.element: in general, missing types should be replaced
 * by empty types with the same name.
 */
@interface Anno1 {
	String value() default "foo";
}
@A1.A2.A3 class Negative1 {
	@Anno1("spud") String s1;
	@A4 Missing1 m1 = MISSING_VAL1;
	@A5(@A6(@A7)) int i1;
	@A8 Missing2.Missing3.Missing4 m2;
}