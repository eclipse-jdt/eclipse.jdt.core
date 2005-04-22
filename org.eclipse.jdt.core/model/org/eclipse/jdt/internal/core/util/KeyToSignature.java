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
package org.eclipse.jdt.internal.core.util;

import java.util.ArrayList;

import org.eclipse.jdt.core.compiler.CharOperation;
import org.eclipse.jdt.internal.compiler.ast.Wildcard;

/*
 * Converts a binding key into a signature 
 */
// TODO (jerome) handle methods and fields
public class KeyToSignature extends BindingKeyParser {
	
	public static final int SIGNATURE = 0;
	public static final int TYPE_ARGUMENTS = 1;
	public static final int DECLARING_TYPE = 2;
	
	public StringBuffer signature = new StringBuffer();
	private int kind;
	private ArrayList arguments = new ArrayList();
	private ArrayList typeParameters = new ArrayList();
	private int mainTypeStart = -1;
	private int mainTypeEnd;
	private int typeSigStart = -1;
	
	public KeyToSignature(BindingKeyParser parser) {
		super(parser);
	}
	
	public KeyToSignature(String key, int kind) {
		super(key);
		this.kind = kind;
	}
	
	public void consumeArrayDimension(char[] brakets) {
		this.signature.append(brakets);
	}
	
	public void consumeCapture() {
		this.signature.append('!');
	}
		
	public void consumeLocalType(char[] uniqueKey) {
		this.signature = new StringBuffer();
		// remove trailing semi-colon as it is added later in comsumeType()
		uniqueKey = CharOperation.subarray(uniqueKey, 0, uniqueKey.length-1);
		CharOperation.replace(uniqueKey, '/', '.');
		this.signature.append(uniqueKey);
	}
	
	public void consumeMethod(char[] selector, char[] methodSignature) {
		this.arguments = new ArrayList();
		if (this.kind == SIGNATURE) {
			this.signature = new StringBuffer();
			CharOperation.replace(methodSignature, '/', '.');
			this.signature.append(methodSignature);
		}
	}
	
	public void consumeMemberType(char[] simpleTypeName) {
		this.signature.append('$');
		this.signature.append(simpleTypeName);
	}

	public void consumePackage(char[] pkgName) {
		this.signature.append(pkgName);
	}
	
	public void consumeParameterizedType(char[] simpleTypeName, boolean isRaw) {
		if (simpleTypeName != null) {
			// member type
			this.signature.append('.');
			this.signature.append(simpleTypeName);
		}
		if (!isRaw) {
			this.signature.append('<');
			int length = this.arguments.size();
			for (int i = 0; i < length; i++) {
				this.signature.append(this.arguments.get(i));
			}
			this.signature.append('>');
			if (this.kind != TYPE_ARGUMENTS)
				this.arguments = new ArrayList();
		}
	}
	
	public void consumeParser(BindingKeyParser parser) {
		this.arguments.add(((KeyToSignature) parser).signature);
	}
	
	public void consumeFullyQualifiedName(char[] fullyQualifiedName) {
		this.typeSigStart = this.signature.length();
		this.signature.append('L');
		this.signature.append(CharOperation.replaceOnCopy(fullyQualifiedName, '/', '.'));
	}
	
	public void consumeSecondaryType(char[] simpleTypeName) {
		this.signature.append('~');
		this.mainTypeStart = this.signature.lastIndexOf(".") + 1; //$NON-NLS-1$
		if (this.mainTypeStart == 0)
			this.mainTypeStart = 1; // default package
		this.mainTypeEnd = this.signature.length();
		this.signature.append(simpleTypeName);
	}

	public void consumeType() {
		int length = this.typeParameters.size();
		if (length > 0) {
			StringBuffer typeParametersSig = new StringBuffer();
			typeParametersSig.append('<');
			for (int i = 0; i < length; i++) {
				typeParametersSig.append((char[]) this.typeParameters.get(i));
				typeParametersSig.append(':');
				// TODO (jerome) add type parameter bounds in binding key
			}
			typeParametersSig.append('>');
			this.signature.insert(this.typeSigStart, typeParametersSig);
			this.typeParameters = new ArrayList();
		}
		// remove main type if needed
		if (this.mainTypeStart != -1) {
			this.signature.replace(this.mainTypeStart, this.mainTypeEnd, ""); //$NON-NLS-1$
		}
		this.signature.append(';');
	}
	
	public void consumeTypeParameter(char[] typeParameterName) {
		this.typeParameters.add(typeParameterName);
	}
	
	public void consumeTypeVariable(char[] typeVariableName) {
		this.signature = new StringBuffer();
		this.signature.append('T');
		this.signature.append(typeVariableName);
		this.signature.append(';');
	}
	
	public void consumeWildCard(int wildCardKind) {
		switch (wildCardKind) {
			case Wildcard.UNBOUND:
				this.signature.append('*');
				break;
			case Wildcard.EXTENDS:
				this.signature.append('+');
				break;
			case Wildcard.SUPER:
				this.signature.append('-');
				break;
			default:
				// malformed
				return;
		}
	}
	
	public String[] getTypeArguments() {
		int length = this.arguments.size();
		String[] result = new String[length];
		for (int i = 0; i < length; i++) {
			result[i] = ((StringBuffer) this.arguments.get(i)).toString();
		}
		return result;
	}
	
	public BindingKeyParser newParser() {
		return new KeyToSignature(this);
	}
	
	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	public String toString() {
		return this.signature.toString();
	}

}
