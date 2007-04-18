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

import org.eclipse.jdt.internal.compiler.apt.dispatch.BaseProcessingEnvImpl;
import org.eclipse.jdt.internal.compiler.lookup.TypeVariableBinding;

/**
 * @author OThomann
 *
 */
public class TypeVariableImpl extends TypeMirrorImpl implements TypeVariable {
	
	public TypeVariableImpl(BaseProcessingEnvImpl env, TypeVariableBinding binding) {
		super(env, binding);
	}
	/* (non-Javadoc)
	 * @see javax.lang.model.type.TypeVariable#asElement()
	 */
	@Override
	public Element asElement() {
		return _env.getFactory().newElement(this._binding);
	}

	/* (non-Javadoc)
	 * @see javax.lang.model.type.TypeVariable#getLowerBound()
	 */
	@Override
	public TypeMirror getLowerBound() {
		// TODO might be more complex than this
		return this._env.getFactory().getNullType();
	}

	/* (non-Javadoc)
	 * @see javax.lang.model.type.TypeVariable#getUpperBound()
	 */
	@Override
	public TypeMirror getUpperBound() {
		return this._env.getFactory().newDeclaredType((TypeVariableBinding) this._binding);
	}

	/* (non-Javadoc)
	 * @see javax.lang.model.type.TypeMirror#accept(javax.lang.model.type.TypeVisitor, java.lang.Object)
	 */
	@Override
	public <R, P> R accept(TypeVisitor<R, P> v, P p) {
		return v.visitTypeVariable(this, p);
	}
}
