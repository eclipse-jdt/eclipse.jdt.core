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
package targets.model.pa;

public class A implements IA {
	public String methodIAString(int int1) 
	{ 
		_fieldAint = int1;
		return String.valueOf(_fieldAint);
	}
	
	public void methodThrows1() throws ExceptionA {
		if (_fieldAint < 0) {
			throw new ExceptionA();
		}
	}
	
	public void methodThrows2() throws ExceptionA, UnsupportedOperationException {
		if (_fieldAint > 0) {
			throw new ExceptionA();
		}
		else {
			throw new UnsupportedOperationException();
		}
	}
	
	private int _fieldAint;
}
