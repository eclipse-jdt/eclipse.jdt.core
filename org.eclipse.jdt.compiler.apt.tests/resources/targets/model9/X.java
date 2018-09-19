/*******************************************************************************
 * Copyright (c) 2017 IBM Corporation.
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
package targets.model9;

import org.eclipse.jdt.compiler.apt.tests.annotations.Type;
import org.eclipse.jdt.compiler.apt.tests.annotations.Type1;
import org.eclipse.jdt.compiler.apt.tests.annotations.Type$1;
import org.eclipse.jdt.compiler.apt.tests.annotations.Type.One;

@Type("c")
public class X extends @Type("s") Object implements @Type("i1") I, @Type("i2") J {
	@Type("f") String _field = null;
	@Type("f1") X _field1 = null;
	@Type("f1") X _field11 = null;
	@Type$1 @One String _field2 = null;
	X _field3 = null;
	int _i = 10;
	public void noAnnotationHere() {
	}
	@Deprecated @Type("m") String foo() {
		return null;
	}
	void bar(@Type("p1") String p1, @Type("p2") String p2) {}
	public void bar2(@Type("receiver") X this) {}
	// Static methods and top level constructors do not have receivers
	public static void main(String[] args) {}
	@Type("constr1") public X(){}
	@Type1("constr2") public X(int i){}
}

interface I {}
interface J {}
