/*******************************************************************************
 * Copyright (c) 2000, 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jdt.internal.compiler.impl;

import org.eclipse.jdt.internal.compiler.util.Util;

public class BooleanConstant extends Constant {

	boolean value;
	
	public BooleanConstant(boolean value) {
		this.value = value;
	}

	public boolean booleanValue() {
		return value;
	}

	public String stringValue() {
		//spec 15.17.11
		String s = Util.toBoolean(value).toString();
		if (s == null) return "null"; //$NON-NLS-1$
		return s;
	}

	public String toString(){
		return "(boolean)" + value ;  //$NON-NLS-1$
	}

	public int typeID() {
		return T_boolean;
	}
}
