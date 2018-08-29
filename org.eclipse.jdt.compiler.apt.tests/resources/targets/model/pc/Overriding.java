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
package targets.model.pc;

public class Overriding {
	public class A {
		public void f() {} // overrides OverB.f() in context of OverC, but not in context of OverD
		private void g() {} // does not override OverB.g() in any context
		public void h() {}  // overrides OverB.h() in context of OverC and OverD
		public void j() {}
	}
	
	public interface B {
		public void f();
		public void g();
		public void h();
	}

	abstract public class C extends A implements B {
		public void h() {}
		public void j() {}
	}

	public class D extends C {
		public void f() {}
		public void g() {}
		public void j() {}
	}
}

