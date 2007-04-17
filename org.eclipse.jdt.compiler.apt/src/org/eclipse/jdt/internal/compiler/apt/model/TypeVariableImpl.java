/*******************************************************************************
 * Copyright (c) 2006 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/

package org.eclipse.jdt.internal.compiler.apt.model;

import javax.lang.model.element.Element;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.type.TypeVariable;
import javax.lang.model.type.TypeVisitor;

import org.eclipse.jdt.internal.compiler.lookup.TypeVariableBinding;

/**
 * @author OThomann
 *
 */
public class TypeVariableImpl extends TypeMirrorImpl implements TypeVariable {
	
	public TypeVariableImpl(TypeVariableBinding binding) {
		super(binding);
	}
	/* (non-Javadoc)
	 * @see javax.lang.model.type.TypeVariable#asElement()
	 */
	@Override
	public Element asElement() {
		return Factory.newElement(this._binding);
	}

	/* (non-Javadoc)
	 * @see javax.lang.model.type.TypeVariable#getLowerBound()
	 */
	@Override
	public TypeMirror getLowerBound() {
		// TODO Auto-generated method stub
		throw new UnsupportedOperationException("NYI: TypeVariableImpl.getLowerBound()"); //$NON-NLS-1$
	}

	/* (non-Javadoc)
	 * @see javax.lang.model.type.TypeVariable#getUpperBound()
	 */
	@Override
	public TypeMirror getUpperBound() {
		// TODO Auto-generated method stub
		throw new UnsupportedOperationException("NYI: TypeVariableImpl.getUpperBound()"); //$NON-NLS-1$
	}

	/* (non-Javadoc)
	 * @see javax.lang.model.type.TypeMirror#accept(javax.lang.model.type.TypeVisitor, java.lang.Object)
	 */
	@Override
	public <R, P> R accept(TypeVisitor<R, P> v, P p) {
		return v.visitTypeVariable(this, p);
	}
}
