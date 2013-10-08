/*******************************************************************************
 * Copyright (c) 2013 IBM Corporation.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * This is an implementation of an early-draft specification developed under the Java
 * Community Process (JCP) and is made available for testing and evaluation purposes
 * only. The code is not compatible with any specification of the JCP.
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
