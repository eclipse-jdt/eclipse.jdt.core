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

public class J {
	int fieldInt; // same-named fields in F, G, and H do NOT hide this; unrelated enclosing classes
	
	int fieldInt() { return 0; } // does not hide, and is not hidden by, any of the fields named fieldInt in F, G, H, or this.
	
	public class FChild {} // does not hide, and is not hidden by, same class in F or H
	
	public class F {} // does not hide, and is not hidden by, outer class F
	
	public static void staticMethod() {} // does not hide, and is not hidden by, F.staticMethod()
}
