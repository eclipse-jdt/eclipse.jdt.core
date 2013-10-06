/*******************************************************************************
 * Copyright (c) 2013 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * This is an implementation of an early-draft specification developed under the Java
 * Community Process (JCP) and is made available for testing and evaluation purposes
 * only. The code is not compatible with any specification of the JCP.
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jdt.internal.compiler.lookup;

public abstract class TypeSystem {
	
	public boolean isAnnotatedTypeSystem() {
		return false;
	}

	public abstract AnnotationBinding getAnnotationType(ReferenceBinding annotationType, boolean requireResolved);
	
	public abstract ArrayBinding getArrayType(TypeBinding leafComponentType, int dimensions);
	
	public ArrayBinding getArrayType(TypeBinding leafComponentType, int dimensions, AnnotationBinding[] annotations) {
		return getArrayType(leafComponentType, dimensions);
	}

	public abstract ParameterizedTypeBinding getParameterizedType (ReferenceBinding genericType, TypeBinding[] typeArguments, ReferenceBinding enclosingType);
	
	public ParameterizedTypeBinding getParameterizedType(ReferenceBinding genericType, TypeBinding[] typeArguments, ReferenceBinding enclosingType, AnnotationBinding[] annotations) {
		return getParameterizedType(genericType, typeArguments, enclosingType);
	}

	public abstract RawTypeBinding getRawType(ReferenceBinding genericType, ReferenceBinding enclosingType);
	
	public RawTypeBinding getRawType(ReferenceBinding genericType, ReferenceBinding enclosingType, AnnotationBinding[] annotations) {
		return getRawType(genericType, enclosingType);
	}

	public abstract WildcardBinding getWildcard(ReferenceBinding genericType, int rank, TypeBinding bound, TypeBinding[] otherBounds, int boundKind);
	
	public WildcardBinding getWildcard(ReferenceBinding genericType, int rank, TypeBinding bound, TypeBinding[] otherBounds, int boundKind, AnnotationBinding[] annotations) {
		return getWildcard(genericType, rank, bound, otherBounds, boundKind);
	}

	public TypeBinding getAnnotatedType(TypeBinding type, AnnotationBinding[][] annotations) {
		return type;
	}

	
	public abstract void reset();

	public abstract void updateCaches(UnresolvedReferenceBinding unresolvedType, ReferenceBinding resolvedType);

	public abstract TypeBinding getUnannotatedType(TypeBinding type);

	// Given a type, return all its variously annotated versions.
	public TypeBinding[] getAnnotatedTypes(TypeBinding type) {
		return Binding.NO_TYPES;
	}

	public ReferenceBinding getMemberType(ReferenceBinding memberType, ReferenceBinding enclosingType) {
		return memberType;  // nothing to do for plain vanilla type system.
	}
}
