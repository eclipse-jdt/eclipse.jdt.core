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

import targets.negative.pz.*;

/**
 * This code is syntactic but contains semantic errors due to missing types.
 * The desired behavior is specified in the javadoc for package 
 * javax.lang.model.element: in general, missing types should be replaced
 * by empty types with the same name.
 */
@interface Anno1 {
	
}
@interface Anno4 {
	int value();
}
class Negative2 {
	@Anno2(123) void m1(); // Anno2 is in pz and is not public
	@Anno1 @FakeAnno3 void m2(); // FakeAnno3 is undefined
	@Anno2(456) @FakeAnno3 void m3(); // Anno2 is not visible and FakeAnno3 is undefined
	@Anno4("notAnInt") void m4(); // Anno4 takes an int value
}