/*******************************************************************************
 * Copyright (c) 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jdt.core;

import org.eclipse.jdt.internal.core.util.KeyToSignature;

/**
 * Utility class to decode a binding key.
 * 
 * @see org.eclipse.jdt.core.dom.IBinding#getKey()
 * @since 3.1
 */
public class BindingKey {
	
	private String key;
	
	/**
	 * Creates a new binding key.
	 * 
	 * @param key the key to decode
	 */
	public BindingKey(String key) {
		this.key = key;
	}
	
	/**
	 * Returns the declaring type signature of the element represented by this binding key.
	 * Returns the signature of the element if it is a type.
	 * 
	 * @return the declaring type signature
	 */
	public String getDeclaringTypeSignature() {
		KeyToSignature keyToSignature = new KeyToSignature(this.key, KeyToSignature.DECLARING_TYPE);
		keyToSignature.parse();
		return keyToSignature.signature.toString();
	}
	
	/**
	 * Returns the type argument signatures of the element represented by this binding key.
	 * If this binding key doesn't represent a parameterized type or a pamaterized method,
	 * returns an empty array.
	 * 
	 * @return the the type argument signatures 
	 */
	public String[] getTypeArguments() {
		KeyToSignature keyToSignature = new KeyToSignature(this.key, KeyToSignature.TYPE_ARGUMENTS);
		keyToSignature.parse();
		return keyToSignature.getTypeArguments();
	}
	
	/**
	 * Transforms this binding key into a signature.
	 * 
	 * @return the signature for this binding key
	 */
	public String toSignature() {
		KeyToSignature keyToSignature = new KeyToSignature(this.key, KeyToSignature.SIGNATURE);
		keyToSignature.parse();
		return keyToSignature.signature.toString();
	}
	
	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	public String toString() {
		return this.key;
	}
}
