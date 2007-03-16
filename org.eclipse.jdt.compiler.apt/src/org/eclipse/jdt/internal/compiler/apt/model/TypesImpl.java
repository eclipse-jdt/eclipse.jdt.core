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

import java.util.List;

import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.ArrayType;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.ExecutableType;
import javax.lang.model.type.NoType;
import javax.lang.model.type.NullType;
import javax.lang.model.type.PrimitiveType;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.type.WildcardType;
import javax.lang.model.util.Types;

import org.eclipse.jdt.internal.compiler.apt.dispatch.BaseProcessingEnvImpl;

/**
 * Utilities for working with types (as opposed to elements).
 * There is one of these for every ProcessingEnvironment.
 */
public class TypesImpl implements Types {
	
	//private final BaseProcessingEnvImpl _env;

	/*
	 * The processing env creates and caches a TypesImpl.  Other clients should
	 * not create their own; they should ask the env for it.
	 */
	public TypesImpl(BaseProcessingEnvImpl env) {
		//_env = env;
	}

	/* (non-Javadoc)
	 * @see javax.lang.model.util.Types#asElement(javax.lang.model.type.TypeMirror)
	 */
	@Override
	public Element asElement(TypeMirror t) {
		if (!(t instanceof TypeMirrorImpl)) {
			return null;
		}
		return Factory.newElement(((TypeMirrorImpl)t).binding());
	}

	/* (non-Javadoc)
	 * @see javax.lang.model.util.Types#asMemberOf(javax.lang.model.type.DeclaredType, javax.lang.model.element.Element)
	 */
	@Override
	public TypeMirror asMemberOf(DeclaredType containing, Element element) {
		// TODO Auto-generated method stub
		return null;
	}

	/* (non-Javadoc)
	 * @see javax.lang.model.util.Types#boxedClass(javax.lang.model.type.PrimitiveType)
	 */
	@Override
	public TypeElement boxedClass(PrimitiveType p) {
		// TODO Auto-generated method stub
		return null;
	}

	/* (non-Javadoc)
	 * @see javax.lang.model.util.Types#capture(javax.lang.model.type.TypeMirror)
	 */
	@Override
	public TypeMirror capture(TypeMirror t) {
		// TODO Auto-generated method stub
		return null;
	}

	/* (non-Javadoc)
	 * @see javax.lang.model.util.Types#contains(javax.lang.model.type.TypeMirror, javax.lang.model.type.TypeMirror)
	 */
	@Override
	public boolean contains(TypeMirror t1, TypeMirror t2) {
		// TODO Auto-generated method stub
		return false;
	}

	/* (non-Javadoc)
	 * @see javax.lang.model.util.Types#directSupertypes(javax.lang.model.type.TypeMirror)
	 */
	@Override
	public List<? extends TypeMirror> directSupertypes(TypeMirror t) {
		// TODO Auto-generated method stub
		return null;
	}

	/* (non-Javadoc)
	 * @see javax.lang.model.util.Types#erasure(javax.lang.model.type.TypeMirror)
	 */
	@Override
	public TypeMirror erasure(TypeMirror t) {
		// TODO Auto-generated method stub
		return null;
	}

	/* (non-Javadoc)
	 * @see javax.lang.model.util.Types#getArrayType(javax.lang.model.type.TypeMirror)
	 */
	@Override
	public ArrayType getArrayType(TypeMirror componentType) {
		// TODO Auto-generated method stub
		return null;
	}

	/* (non-Javadoc)
	 * @see javax.lang.model.util.Types#getDeclaredType(javax.lang.model.element.TypeElement, javax.lang.model.type.TypeMirror[])
	 */
	@Override
	public DeclaredType getDeclaredType(TypeElement typeElem, TypeMirror... typeArgs) {
		// TODO Auto-generated method stub
		return null;
	}

	/* (non-Javadoc)
	 * @see javax.lang.model.util.Types#getDeclaredType(javax.lang.model.type.DeclaredType, javax.lang.model.element.TypeElement, javax.lang.model.type.TypeMirror[])
	 */
	@Override
	public DeclaredType getDeclaredType(DeclaredType containing, TypeElement typeElem,
			TypeMirror... typeArgs) {
		// TODO Auto-generated method stub
		return null;
	}

	/* (non-Javadoc)
	 * @see javax.lang.model.util.Types#getNoType(javax.lang.model.type.TypeKind)
	 */
	@Override
	public NoType getNoType(TypeKind kind) {
		// TODO Auto-generated method stub
		return null;
	}

	/* (non-Javadoc)
	 * @see javax.lang.model.util.Types#getNullType()
	 */
	@Override
	public NullType getNullType() {
		// TODO Auto-generated method stub
		return null;
	}

	/* (non-Javadoc)
	 * @see javax.lang.model.util.Types#getPrimitiveType(javax.lang.model.type.TypeKind)
	 */
	@Override
	public PrimitiveType getPrimitiveType(TypeKind kind) {
		// TODO Auto-generated method stub
		return null;
	}

	/* (non-Javadoc)
	 * @see javax.lang.model.util.Types#getWildcardType(javax.lang.model.type.TypeMirror, javax.lang.model.type.TypeMirror)
	 */
	@Override
	public WildcardType getWildcardType(TypeMirror extendsBound, TypeMirror superBound) {
		// TODO Auto-generated method stub
		return null;
	}

	/* (non-Javadoc)
	 * @see javax.lang.model.util.Types#isAssignable(javax.lang.model.type.TypeMirror, javax.lang.model.type.TypeMirror)
	 */
	@Override
	public boolean isAssignable(TypeMirror t1, TypeMirror t2) {
		// TODO Auto-generated method stub
		return false;
	}

	/* (non-Javadoc)
	 * @see javax.lang.model.util.Types#isSameType(javax.lang.model.type.TypeMirror, javax.lang.model.type.TypeMirror)
	 */
	@Override
	public boolean isSameType(TypeMirror t1, TypeMirror t2) {
		// TODO Auto-generated method stub
		return false;
	}

	/* (non-Javadoc)
	 * @see javax.lang.model.util.Types#isSubsignature(javax.lang.model.type.ExecutableType, javax.lang.model.type.ExecutableType)
	 */
	@Override
	public boolean isSubsignature(ExecutableType m1, ExecutableType m2) {
		// TODO Auto-generated method stub
		return false;
	}

	/* (non-Javadoc)
	 * @see javax.lang.model.util.Types#isSubtype(javax.lang.model.type.TypeMirror, javax.lang.model.type.TypeMirror)
	 */
	@Override
	public boolean isSubtype(TypeMirror t1, TypeMirror t2) {
		// TODO Auto-generated method stub
		return false;
	}

	/* (non-Javadoc)
	 * @see javax.lang.model.util.Types#unboxedType(javax.lang.model.type.TypeMirror)
	 */
	@Override
	public PrimitiveType unboxedType(TypeMirror t) {
		// TODO Auto-generated method stub
		return null;
	}

}
