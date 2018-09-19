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
package targets.errors.pb;

public class F {
	
	enum C {
		CONST1, CONST2
	}

	int field;

	static {
	}

	{
		field = 1;
	}

	F(int i) {
		this.field = i;
	}

	static class Member {
	}

	public void foo(int i) throws Exception {}
}
