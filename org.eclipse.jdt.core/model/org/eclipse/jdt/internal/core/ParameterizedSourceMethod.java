/*******************************************************************************
 * Copyright (c) 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jdt.internal.core;

/**
 * Handle representing a source method that is parameterized.
 * The uniqueKey contains the genericSignature of the parameterized method.
 */
public class ParameterizedSourceMethod extends SourceMethod {
	
	public String genericDeclaringTypeSignature;
	public String genericSignature;
	public String[] genericTypeArgumentsSignatures;
	
	/*
	 * See class comments.
	 */
	public ParameterizedSourceMethod(JavaElement parent, String name, String[] parameterTypes, String genericDeclaringTypeSignature, String genericSignature, String[] genericTypeArgumentsSignatures) {
		super(parent, name, parameterTypes);
		this.genericDeclaringTypeSignature = genericDeclaringTypeSignature;
		this.genericSignature = genericSignature;
		this.genericTypeArgumentsSignatures = genericTypeArgumentsSignatures;
	}

	/**
	 * @private Debugging purposes
	 */
	protected void toStringInfo(int tab, StringBuffer buffer, Object info) {
		super.toStringInfo(tab, buffer, info);
		buffer.append(" declaringSignature="); //$NON-NLS-1$
		buffer.append(genericDeclaringTypeSignature);
		buffer.append(" signature="); //$NON-NLS-1$
		buffer.append(genericSignature);
		buffer.append(" typeArgumentsSignature="); //$NON-NLS-1$
		int length = this.genericTypeArgumentsSignatures == null ? 0 : this.genericTypeArgumentsSignatures.length;
		if(length > 0) {
			buffer.append('<');
			for (int i = 0; i < length; i++) {
				if(i != 0) buffer.append(',');
				buffer.append(genericTypeArgumentsSignatures[i]);
			}
			buffer.append('>');
		} else {
			buffer.append("null"); //$NON-NLS-1$
		}
	}
}
