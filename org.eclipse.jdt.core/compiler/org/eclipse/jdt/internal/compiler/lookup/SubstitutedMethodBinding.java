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
 * Binding denoting a method after type parameter substitutions got performed
 */
public class SubstitutedMethodBinding extends MethodBinding {
    
    public MethodBinding originalMethod;
    
public SubstitutedMethodBinding(MethodBinding originalMethod, ParameterizedTypeBinding parameterizedDeclaringClass) {
    this.originalMethod = originalMethod;
	this.modifiers = originalMethod.modifiers;
	this.selector = originalMethod.selector;
	this.thrownExceptions = originalMethod.thrownExceptions;
	this.declaringClass = parameterizedDeclaringClass;

	this.returnType = parameterizedDeclaringClass.substitute(originalMethod.returnType);
	this.parameters = parameterizedDeclaringClass.substitute(originalMethod.parameters);
}
}