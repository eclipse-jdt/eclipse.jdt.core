/*******************************************************************************
 * Copyright (c) 2013 IBM Corporation.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package targets.model8;

import org.eclipse.jdt.compiler.apt.tests.annotations.Type;

public class Y {
	@Type("f") String @Type("f1") [] @Type("f2") [] _field2 @Type("f3") [], _field3 @Type("f4") [][] = null;
	
	@Deprecated @Type("m") String @Type("m1") [] foo() @Type("m2") [] @Type("m3") [] {
		return null;
	}
	void bar (@Type("p1") String [] a @Type("p2") [], @Type("p3") int @Type("p4") [] b [] @Type("p5") []) {}
	void foo2() throws @Type("e1") NullPointerException, @Type("e2") ArrayIndexOutOfBoundsException {}
	void bar2 (@Type("p1") String @Type("p2") [] @Type("p3") ... args) {}
}
