/*******************************************************************************
 * Copyright (c) 2005, 2007 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jdt.internal.compiler.apt.model;

import javax.lang.model.element.AnnotationValue;
import javax.lang.model.element.AnnotationValueVisitor;

import org.eclipse.jdt.internal.compiler.impl.IntConstant;
import org.eclipse.jdt.internal.compiler.impl.StringConstant;
import org.eclipse.jdt.internal.compiler.lookup.ElementValuePair;

public class AnnotationValueImpl implements AnnotationValue {
	
	private final Object _value;

	/**
	 * @param value See {@link ElementValuePair#getValue()} for possible
	 * object types.
	 */
	public AnnotationValueImpl(Object value) {
		_value = value;
		//TODO: determine object type
	}

	@Override
	public <R, P> R accept(AnnotationValueVisitor<R, P> v, P p) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Object getValue() {
		// TODO: use object type determined in c'tor
		if (_value instanceof StringConstant) {
			return ((StringConstant)_value).stringValue();
		}
		else if (_value instanceof IntConstant) {
			return ((IntConstant)_value).intValue();
		}
		return null;
	}

	@Override
	public String toString() {
		// TODO: use object type determined in c'tor
		return String.valueOf(getValue());
	}
}
