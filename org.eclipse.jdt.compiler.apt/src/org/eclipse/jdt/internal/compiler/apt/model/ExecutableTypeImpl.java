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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.lang.model.type.ExecutableType;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.type.TypeVariable;
import javax.lang.model.type.TypeVisitor;

import org.eclipse.jdt.internal.compiler.lookup.MethodBinding;
import org.eclipse.jdt.internal.compiler.lookup.ReferenceBinding;
import org.eclipse.jdt.internal.compiler.lookup.TypeBinding;
import org.eclipse.jdt.internal.compiler.lookup.TypeVariableBinding;

/**
 * Implementation of the ExecutableType
 *
 */
public class ExecutableTypeImpl extends TypeMirrorImpl implements ExecutableType {
	
	public ExecutableTypeImpl(MethodBinding binding) {
		super(binding);
	}
	/* (non-Javadoc)
	 * @see javax.lang.model.type.ExecutableType#getParameterTypes()
	 */
	@Override
	public List<? extends TypeMirror> getParameterTypes() {
		ArrayList<TypeMirror> list = new ArrayList<TypeMirror>();
		TypeBinding[] parameters = ((MethodBinding) this._binding).parameters;
		if (parameters.length != 0) {
			for (TypeBinding typeBinding : parameters) {
				list.add(Factory.newTypeMirror(typeBinding));
			}
		}
		return Collections.unmodifiableList(list);
	}

	/* (non-Javadoc)
	 * @see javax.lang.model.type.ExecutableType#getReturnType()
	 */
	@Override
	public TypeMirror getReturnType() {
		return Factory.newTypeMirror(((MethodBinding) this._binding).returnType);
	}

	/* (non-Javadoc)
	 * @see javax.lang.model.type.ExecutableType#getThrownTypes()
	 */
	@Override
	public List<? extends TypeMirror> getThrownTypes() {
		ArrayList<TypeMirror> list = new ArrayList<TypeMirror>();
		ReferenceBinding[] thrownExceptions = ((MethodBinding) this._binding).thrownExceptions;
		if (thrownExceptions.length != 0) {
			for (ReferenceBinding referenceBinding : thrownExceptions) {
				list.add(Factory.newTypeMirror(referenceBinding));
			}
		}
		return Collections.unmodifiableList(list);
	}

	/* (non-Javadoc)
	 * @see javax.lang.model.type.ExecutableType#getTypeVariables()
	 */
	@Override
	public List<? extends TypeVariable> getTypeVariables() {
		ArrayList<TypeVariable> list = new ArrayList<TypeVariable>();
		TypeVariableBinding[] typeVariables = ((MethodBinding) this._binding).typeVariables();
		if (typeVariables.length != 0) {
			for (TypeVariableBinding typeVariableBinding : typeVariables) {
				list.add((TypeVariable) Factory.newTypeMirror(typeVariableBinding));
			}
		}
		return Collections.unmodifiableList(list);
	}

	/* (non-Javadoc)
	 * @see javax.lang.model.type.TypeMirror#accept(javax.lang.model.type.TypeVisitor, java.lang.Object)
	 */
	@Override
	public <R, P> R accept(TypeVisitor<R, P> v, P p) {
		return v.visitExecutable(this, p);
	}

	/* (non-Javadoc)
	 * @see javax.lang.model.type.TypeMirror#getKind()
	 */
	@Override
	public TypeKind getKind() {
		return TypeKind.EXECUTABLE;
	}
}
