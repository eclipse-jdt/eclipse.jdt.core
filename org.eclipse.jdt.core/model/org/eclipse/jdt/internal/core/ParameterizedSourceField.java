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
 * Handle representing a source field that is parameterized.
 * The uniqueKey contains the genericSignature of the parameterized field.
 */
public class ParameterizedSourceField extends SourceField {
	
	public String genericDeclaringTypeSignature;
	public String genericSignature;
	
	/*
	 * See class comments.
	 */
	public ParameterizedSourceField(JavaElement parent, String name, String declaringTypeSignature, String typeSignature) {
		super(parent, name);
		this.genericDeclaringTypeSignature = declaringTypeSignature;
		this.genericSignature = typeSignature;
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
	}
}
