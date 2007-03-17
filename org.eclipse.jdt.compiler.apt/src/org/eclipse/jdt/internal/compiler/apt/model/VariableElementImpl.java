/*******************************************************************************
 * Copyright (c) 2007 BEA Systems, Inc. 
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    wharley@bea.com - initial API and implementation
 *    
 *******************************************************************************/

package org.eclipse.jdt.internal.compiler.apt.model;

import javax.lang.model.element.VariableElement;

import org.eclipse.jdt.internal.compiler.lookup.Binding;

/**
 * Implementation of VariableElement, which represents a a field, enum constant, 
 * method or constructor parameter, local variable, or exception parameter
 */
public class VariableElementImpl extends ElementImpl implements VariableElement {

	VariableElementImpl(Binding binding) {
		super(binding);
	}

	/* (non-Javadoc)
	 * @see javax.lang.model.element.VariableElement#getConstantValue()
	 */
	@Override
	public Object getConstantValue() {
		// TODO Auto-generated method stub
		return null;
	}

}
