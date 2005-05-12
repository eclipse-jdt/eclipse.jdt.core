/*******************************************************************************
 * Copyright (c) 2005 BEA Systems, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    tyeung@bea.com - initial API and implementation
 *******************************************************************************/
package org.eclipse.jdt.internal.compiler.lookup;

import org.eclipse.jdt.internal.compiler.ast.AbstractMethodDeclaration;
import org.eclipse.jdt.internal.compiler.ast.AnnotationMethodDeclaration;
import org.eclipse.jdt.internal.compiler.ast.TypeDeclaration;

/**
 * Annotation method that came from binary or source
 * @author tyeung
 *
 */
public class AnnotationMethodBinding extends MethodBinding 
{
	private Object defaultValue = null;
	/**
	 * 
	 * @param modifiers
	 * @param selector
	 * @param returnType
	 * @param declaringClass	
	 * @param defaultValue <code>null</code> for source method. 
	 */
	public AnnotationMethodBinding(int modifiers,
								   char[] selector, 
								   TypeBinding returnType, 
								   ReferenceBinding declaringClass,								  
								   Object defaultValue)
	{
		super(modifiers, selector, returnType, NoParameters, NoExceptions, declaringClass );
		this.defaultValue = defaultValue;
		setDefaultValue();	
	}
	
	private void setDefaultValue()
	{			
		if (this.declaringClass instanceof SourceTypeBinding) {
			TypeDeclaration typeDecl = ((SourceTypeBinding)this.declaringClass).scope.referenceContext;
			final AbstractMethodDeclaration methodDecl = typeDecl.declarationOf(this);
			if( methodDecl instanceof AnnotationMethodDeclaration){
				this.defaultValue = SourceElementValuePair.getValue(((AnnotationMethodDeclaration)methodDecl).defaultValue);
			}
		}
	}
	
	/**
	 * @return the default value for this annotation method.
	 *         Return <code>null</code> if there is no default value 
	 */
	public Object getDefaultValue()
	{
		return this.defaultValue;
	}	
}
