/*******************************************************************************
 * Copyright (c) 2000-2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jdt.internal.compiler.lookup;

/*
 * A wildcard acts as an argument for parameterized types, allowing to
 * abstract parameterized types, e.g. List<String> is not compatible with List<Object>, 
 * but compatible with List<?>.
 */
public class WildcardBinding extends TypeVariableBinding {

    public final static char[] WILDCARD_NAME = { '?' };
    
	public WildcardBinding() {
	    super(WILDCARD_NAME, -1);
		this.modifiers = AccPublic | AccGenericSignature; // treat wildcard as public
		this.tagBits ^= HasTypeVariable;
	}
	
	/**
	 * Returns true if the type was declared as a type variable
	 */
	public boolean isTypeVariable() {
	    return false;
	}    
	/**
	 * Returns true if the type is a wildcard
	 */
	public boolean isWildcard() {
	    return true;
	}
}
