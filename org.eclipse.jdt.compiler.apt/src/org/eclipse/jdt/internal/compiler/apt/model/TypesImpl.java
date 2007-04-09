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
import org.eclipse.jdt.internal.compiler.lookup.BaseTypeBinding;
import org.eclipse.jdt.internal.compiler.lookup.Binding;
import org.eclipse.jdt.internal.compiler.lookup.ReferenceBinding;
import org.eclipse.jdt.internal.compiler.lookup.TypeBinding;

/**
 * Utilities for working with types (as opposed to elements).
 * There is one of these for every ProcessingEnvironment.
 */
public class TypesImpl implements Types {
	
	private final BaseProcessingEnvImpl _env;

	/*
	 * The processing env creates and caches a TypesImpl.  Other clients should
	 * not create their own; they should ask the env for it.
	 */
	public TypesImpl(BaseProcessingEnvImpl env) {
		_env = env;
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

	@Override
	public NoType getNoType(TypeKind kind) {
		switch (kind) {
		case NONE:
		case VOID:
			return Factory.getPrimitiveType(kind);
		default:
			throw new IllegalArgumentException();
		}
	}

	/* (non-Javadoc)
	 * @see javax.lang.model.util.Types#getNullType()
	 */
	@Override
	public NullType getNullType() {
		return Factory.getPrimitiveType(TypeKind.NULL);
	}

	/* (non-Javadoc)
	 * @see javax.lang.model.util.Types#getPrimitiveType(javax.lang.model.type.TypeKind)
	 */
	@Override
	public PrimitiveType getPrimitiveType(TypeKind kind) {
		return Factory.getPrimitiveType(kind);
	}

	/* (non-Javadoc)
	 * @see javax.lang.model.util.Types#getWildcardType(javax.lang.model.type.TypeMirror, javax.lang.model.type.TypeMirror)
	 */
	@Override
	public WildcardType getWildcardType(TypeMirror extendsBound, TypeMirror superBound) {
		// TODO Auto-generated method stub
		return null;
	}

	/**
	 * @return true if a value of type t1 can be assigned to a variable of type t2, i.e., t2 = t1.
	 */
	@Override
	public boolean isAssignable(TypeMirror t1, TypeMirror t2) {
		if (!(t1 instanceof TypeMirrorImpl) || !(t2 instanceof TypeMirrorImpl)) {
			return false; 
		}
		Binding b1 = ((TypeMirrorImpl)t1).binding();
		Binding b2 = ((TypeMirrorImpl)t2).binding();
		if (!(b1 instanceof TypeBinding) || !(b2 instanceof TypeBinding)) {
			// package, method, import, etc.
			throw new IllegalArgumentException();
		}
		return ((TypeBinding)b1).isCompatibleWith((TypeBinding)b2);
	}

	/* (non-Javadoc)
	 * @see javax.lang.model.util.Types#isSameType(javax.lang.model.type.TypeMirror, javax.lang.model.type.TypeMirror)
	 */
	@Override
	public boolean isSameType(TypeMirror t1, TypeMirror t2) {
		if (!(t1 instanceof TypeMirrorImpl) || !(t2 instanceof TypeMirrorImpl)) {
			return false;
		}
		if (t1 == t2) {
			return true;
		}
		Binding b1 = ((TypeMirrorImpl)t1).binding();
		Binding b2 = ((TypeMirrorImpl)t2).binding();
		// Wildcard types are never equal, according to the spec of this method
		return (b1 == b2 && b1.kind() != Binding.WILDCARD_TYPE);
	}

	/* (non-Javadoc)
	 * @see javax.lang.model.util.Types#isSubsignature(javax.lang.model.type.ExecutableType, javax.lang.model.type.ExecutableType)
	 */
	@Override
	public boolean isSubsignature(ExecutableType m1, ExecutableType m2) {
		// TODO Auto-generated method stub
		return false;
	}

	/**
	 * @return true if t1 is a subtype of t2, or if t1 == t2.
	 */
	@Override
	public boolean isSubtype(TypeMirror t1, TypeMirror t2) {
		if (!(t1 instanceof TypeMirrorImpl) || !(t2 instanceof TypeMirrorImpl)) {
			return false;
		}
		if (t1 == t2) {
			return true;
		}
		Binding b1 = ((TypeMirrorImpl)t1).binding();
		Binding b2 = ((TypeMirrorImpl)t2).binding();
		if (b1 == b2) {
			return true;
		}
		if (!(b1 instanceof TypeBinding) || !(b2 instanceof TypeBinding)) {
			// package, method, import, etc.
			return false;
		}
		if (b1.kind() == Binding.BASE_TYPE || b2.kind() == Binding.BASE_TYPE) {
			if (b1.kind() != b2.kind()) {
				return false;
			}
			else {
				// for primitives, compatibility implies subtype
				return ((TypeBinding)b1).isCompatibleWith((TypeBinding)b2);
			}
		}
		// TODO: array types and reference types
		throw new UnsupportedOperationException("NYI"); //$NON-NLS-1$
	}

	@Override
	public PrimitiveType unboxedType(TypeMirror t) {
		if (!(((TypeMirrorImpl)t)._binding instanceof ReferenceBinding)) {
			// Not an unboxable type - could be primitive, array, not a type at all, etc.
			throw new IllegalArgumentException();
		}
		ReferenceBinding boxed = (ReferenceBinding)((TypeMirrorImpl)t)._binding;
		TypeBinding unboxed = _env.getLookupEnvironment().computeBoxingType(boxed);
		if (unboxed.kind() != Binding.BASE_TYPE) {
			// No boxing conversion was found
			throw new IllegalArgumentException();
		}
		return Factory.getPrimitiveType((BaseTypeBinding)unboxed);
	}

}
