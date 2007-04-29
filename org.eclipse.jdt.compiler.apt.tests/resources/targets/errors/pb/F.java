/*******************************************************************************
 * Copyright (c) 2007 BEA Systems, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
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
