/*******************************************************************************
 * Copyright (c) 2007 BEA Systems, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
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
