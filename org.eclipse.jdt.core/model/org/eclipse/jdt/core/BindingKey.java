/*******************************************************************************
 * Copyright (c) 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jdt.core;

import org.eclipse.jdt.internal.core.util.KeyKind;
import org.eclipse.jdt.internal.core.util.KeyToSignature;

/**
 * Utility class to decode or create a binding key.
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
	 * Creates a new type binding key from the given type name. The type name must be either 
	 * a fully qualified name, an array type name or a primitive type name. 
	 * If the type name is fully qualified, then it is expected to be dot-based. 
	 * Note that inner types, generic types and parameterized types are not supported.
	 * <p>
	 * For example:
	 * <pre>
	 * <code>
	 * createTypeBindingKey("int") -> "I"
	 * createTypeBindingKey("java.lang.String") -> "Ljava/lang/String;"
	 * createTypeBindingKey("boolean[]") -> "[Z"
	 * </code>
	 * </pre>
	 * </p>
	 *
	 * @param typeName the possibly qualified type name
	 * @return the encoded type signature
	 */
	public static String createTypeBindingKey(String typeName) {
		return Signature.createTypeSignature(typeName.replace('.', '/'), true/*resolved*/);
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
	 * @return the type argument signatures 
	 */
	public String[] getTypeArguments() {
		KeyToSignature keyToSignature = new KeyToSignature(this.key, KeyToSignature.TYPE_ARGUMENTS);
		keyToSignature.parse();
		return keyToSignature.getTypeArguments();
	}
	
	/**
	 * Returns whether this binding key represents a raw type.
	 * 
	 * @return whether this binding key represents a raw type
	 */
	public boolean isRawType() {
		KeyKind kind = new KeyKind(this.key);
		kind.parse();
		return (kind.flags & KeyKind.F_RAW_TYPE) != 0;
	}
	
	/**
	 * Returns whether this binding key represents a parameterized type, or if its declaring type is a parameterized type.
	 * 
	 * @return whether this binding key represents a parameterized type
	 */
	public boolean isParameterizedType() {
		KeyKind kind = new KeyKind(this.key);
		kind.parse();
		return (kind.flags & KeyKind.F_PARAMETERIZED_TYPE) != 0;
	}
	
	/**
	 * Returns whether this binding key represents a parameterized method, or if its declaring method is a parameterized method.
	 * 
	 * @return whether this binding key represents a parameterized method
	 */
	public boolean isParameterizedMethod() {
		KeyKind kind = new KeyKind(this.key);
		kind.parse();
		return (kind.flags & KeyKind.F_PARAMETERIZED_METHOD) != 0;
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
