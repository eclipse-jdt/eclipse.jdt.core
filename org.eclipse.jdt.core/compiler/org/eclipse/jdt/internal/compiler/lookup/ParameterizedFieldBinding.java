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
package org.eclipse.jdt.internal.compiler.lookup;

/**
 * Binding denoting a field after type substitution got performed.
 * On parameterized type bindings, all fields got substituted, regardless whether
 * their signature did involve generics or not, so as to get the proper declaringClass for
 * these fields.
 */
public class ParameterizedFieldBinding extends FieldBinding {
    
    public FieldBinding originalField;
    
	public ParameterizedFieldBinding(ParameterizedTypeBinding parameterizedDeclaringClass, FieldBinding originalField) {
	    super (
	            originalField.name, 
	            parameterizedDeclaringClass.substitute(originalField.type), 
	            originalField.modifiers, 
	            parameterizedDeclaringClass, 
	            originalField.constant);
	    this.originalField = originalField;
	}
}

