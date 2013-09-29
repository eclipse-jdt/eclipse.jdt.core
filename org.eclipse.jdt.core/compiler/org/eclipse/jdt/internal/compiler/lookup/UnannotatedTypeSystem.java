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

import org.eclipse.jdt.internal.compiler.util.SimpleLookupTable;
import org.eclipse.jdt.internal.compiler.util.Util;

/* UnannotatedTypeSystem: An abstraction responsible for keeping track of naked a.k.a unannotated types. This piece is also responsible for id stamping unique
   types. Only those types that are "derived from" in some form or participate in the derivation in some form (by being type arguments say) get tracked and 
   id'd here. At Java 8 time, a new type can be derived from another type by (a) parameterizing it (b) creating arrays of it, (c) by creating raw forms of it, 
   (d) by creating a wild card that could parameterize it or finally by (e) annotating it. A type which is not thus derived from in one form or the other or
   participate in the derivation thus - UTS is completely oblivious to.
   
   TypeBinding.id computation: For primitive types and certain "well known" types, id assignment happens elsewhere. Here we start with an id value that is
   suitably high and proceed monotonically upwards so we will not accidentally collide with the id space in use already. id assignments happens in such a
   way that a naked type and its annotated variants - variously annotated - would all share the same id. Example: @T1 Map<@T2 String, @T3 Object> and
   Map<@T4 String, @T5 Object> and @T6 Map<String, Object> and @T7 Map<String, @T8 Object> and Map<String, @T9 Object> would all share the same id since
   the unadorned naked type in each case is the same: Map<String, Object>. None of this would share the id with Map<String, String>. Briefly put, if you
   take a certain annotated type and strip it of all annotations to come up with the naked type, that naked type and the annotated type would have the
   same id. Alternately, if you take a certain naked type and arrive at the universe of all differently annotated types, they would all share the same id while
   their bindings could be different - would be different unless they are identically annotated.
   
   Thus subsystems that are annotation agnostic could quickly ascertain binding equality by comparing the id field.
*/
public class UnannotatedTypeSystem extends TypeSystem {
	
	private int typeid = TypeIds.T_LastWellKnownTypeId;
	private TypeBinding [][] types; 
	private SimpleLookupTable annotationTypes;
	private LookupEnvironment environment;
	
	public UnannotatedTypeSystem(LookupEnvironment environment) {
		this.environment = environment;
		this.annotationTypes = new SimpleLookupTable(16);
		this.typeid = TypeIds.T_LastWellKnownTypeId;
		this.types = new TypeBinding[TypeIds.T_LastWellKnownTypeId * 2][]; 
	}

	public TypeBinding getUnannotatedType(TypeBinding type) {
		if (type.id == TypeIds.NoId) {
			if (type.hasTypeAnnotations() && !type.isTypeVariable())
				throw new IllegalStateException();
			int typesLength = this.types.length;
			if (this.typeid == typesLength)
				System.arraycopy(this.types, 0, this.types = new TypeBinding[typesLength * 2][], 0, typesLength);
			this.types[type.id = this.typeid++] = new TypeBinding[4];
		} else {
			TypeBinding nakedType = this.types[type.id] == null ? null : this.types[type.id][0];
			if (type.hasTypeAnnotations() && nakedType == null)
				throw new IllegalStateException();
			if (nakedType != null)
				return nakedType;
			this.types[type.id] = new TypeBinding[4];  // well known type, assigned id elsewhere.
		}
	
		return this.types[type.id][0] = type;
	}
	
	/* Note: parameters will not have type type annotations if lookup environment directly uses UTS as its typeSystem. However if this UTS is the underlying type system
	   for an ATS, they may and we need to materialize the unannotated versions and work on them.
	*/ 
	public ArrayBinding getArrayType(TypeBinding leafType, int dimensions) {
		TypeBinding unannotatedLeafType = getUnannotatedType(leafType);
		TypeBinding[] cachedInfo = this.types[unannotatedLeafType.id];  // by construction, cachedInfo != null now.
		int index = 0;
		for (int max = cachedInfo.length; index < max; index++) {
			TypeBinding cachedType = cachedInfo[index];
			if (cachedType == null) 
				break;
			if (!cachedType.isArrayType())
				continue;
			if (cachedType.leafComponentType() == unannotatedLeafType && cachedType.dimensions() == dimensions)
				return (ArrayBinding) cachedType;
		}

		int length = cachedInfo.length;
		if (index == length) {
			System.arraycopy(cachedInfo, 0, cachedInfo = new TypeBinding[length * 2], 0, length);
			this.types[unannotatedLeafType.id] = cachedInfo;
		}
		// Add the new array type id stamping it.
		TypeBinding arrayType = cachedInfo[index] = new ArrayBinding(unannotatedLeafType, dimensions, this.environment);
		int typesLength = this.types.length;
		if (this.typeid == typesLength)
			System.arraycopy(this.types, 0, this.types = new TypeBinding[typesLength * 2][], 0, typesLength);
		this.types[this.typeid] = new TypeBinding[1];
		return (ArrayBinding) (this.types[arrayType.id = this.typeid++][0] = arrayType);
	}

	/* Note: parameters will not have type type annotations if lookup environment directly uses UTS as its typeSystem. However if this UTS is the underlying type system
	   for an ATS, they may and we need to materialize the unannotated versions and work on them.
	*/ 
	public ParameterizedTypeBinding getParameterizedType(ReferenceBinding genericType, TypeBinding[] typeArguments, ReferenceBinding enclosingType) {
		ReferenceBinding unannotatedGenericType = (ReferenceBinding) getUnannotatedType(genericType);
		int typeArgumentsLength = typeArguments == null ? 0: typeArguments.length;
		TypeBinding [] unannotatedTypeArguments = typeArguments == null ? null : new TypeBinding[typeArgumentsLength];
		for (int i = 0; i < typeArgumentsLength; i++) {
			unannotatedTypeArguments[i] = getUnannotatedType(typeArguments[i]);
		}
		ReferenceBinding unannotatedEnclosingType = enclosingType == null ? null : (ReferenceBinding) getUnannotatedType(enclosingType);
		
		TypeBinding[] cachedInfo = this.types[unannotatedGenericType.id];  // by construction, cachedInfo != null now.
		int index = 0;
		for (int max = cachedInfo.length; index < max; index++) {
			TypeBinding cachedType = cachedInfo[index];
			if (cachedType == null) 
				break;
			if (!cachedType.isParameterizedType())
				continue;
			if (cachedType.enclosingType() == unannotatedEnclosingType && Util.effectivelyEqual(cachedType.typeArguments(), unannotatedTypeArguments))
				return (ParameterizedTypeBinding) cachedType;
		}

		int length = cachedInfo.length;
		if (index == length) {
			System.arraycopy(cachedInfo, 0, cachedInfo = new TypeBinding[length * 2], 0, length);
			this.types[unannotatedGenericType.id] = cachedInfo;
		}
		TypeBinding parameterizedType = cachedInfo[index] = new ParameterizedTypeBinding(unannotatedGenericType, unannotatedTypeArguments, unannotatedEnclosingType, this.environment);
	
		int typesLength = this.types.length;
		if (this.typeid == typesLength)
			System.arraycopy(this.types, 0, this.types = new TypeBinding[typesLength * 2][], 0, typesLength);
		this.types[this.typeid] = new TypeBinding[1];
		return (ParameterizedTypeBinding) (this.types[parameterizedType.id = this.typeid++][0] = parameterizedType);
	}

	/* Note: parameters will not have type type annotations if lookup environment directly uses UTS as its typeSystem. However if this UTS is the underlying type system
	   for an ATS, they may and we need to materialize the unannotated versions and work on them.
	*/ 
	public RawTypeBinding getRawType(ReferenceBinding genericType, ReferenceBinding enclosingType) {
		ReferenceBinding unannotatedGenericType = (ReferenceBinding) getUnannotatedType(genericType);
		ReferenceBinding unannotatedEnclosingType = enclosingType == null ? null : (ReferenceBinding) getUnannotatedType(enclosingType);
	
		TypeBinding[] cachedInfo = this.types[unannotatedGenericType.id];  // by construction, cachedInfo != null now.
		int index = 0;
		for (int max = cachedInfo.length; index < max; index++) {
			TypeBinding cachedType = cachedInfo[index];
			if (cachedType == null) 
				break;
			if (!cachedType.isRawType())
				continue;
			if (cachedType.enclosingType() == unannotatedEnclosingType)
				return (RawTypeBinding) cachedType;
		}

		int length = cachedInfo.length;
		if (index == length) {
			System.arraycopy(cachedInfo, 0, cachedInfo = new TypeBinding[length * 2], 0, length);
			this.types[unannotatedGenericType.id] = cachedInfo;
		}
		
		TypeBinding rawTytpe = cachedInfo[index] = new RawTypeBinding(unannotatedGenericType, unannotatedEnclosingType, this.environment);
		int typesLength = this.types.length;
		if (this.typeid == typesLength)
			System.arraycopy(this.types, 0, this.types = new TypeBinding[typesLength * 2][], 0, typesLength);
		this.types[this.typeid] = new TypeBinding[1];
		return (RawTypeBinding) (this.types[rawTytpe.id = this.typeid++][0] = rawTytpe);
	}

	/* Note: parameters will not have type type annotations if lookup environment directly uses UTS as its typeSystem. However if this UTS is the underlying type system
	   for an ATS, they may and we need to materialize the unannotated versions and work on them.
	*/ 
	public WildcardBinding getWildcard(ReferenceBinding genericType, int rank, TypeBinding bound, TypeBinding[] otherBounds, int boundKind) {
		if (genericType == null) // pseudo wildcard denoting composite bounds for lub computation
			genericType = ReferenceBinding.LUB_GENERIC;
		
		ReferenceBinding unannotatedGenericType = (ReferenceBinding) getUnannotatedType(genericType);
		int otherBoundsLength = otherBounds == null ? 0: otherBounds.length;
		TypeBinding [] unannotatedOtherBounds = otherBounds == null ? null : new TypeBinding[otherBoundsLength];
		for (int i = 0; i < otherBoundsLength; i++) {
			unannotatedOtherBounds[i] = getUnannotatedType(otherBounds[i]);
		}
		TypeBinding unannotatedBound = bound == null ? null : getUnannotatedType(bound);

		TypeBinding[] cachedInfo = this.types[unannotatedGenericType.id];  // by construction, cachedInfo != null now.
		int index = 0;
		for (int max = cachedInfo.length; index < max; index++) {
			TypeBinding cachedType = cachedInfo[index];
			if (cachedType == null) 
				break;
			if (!cachedType.isWildcard())
				continue;
			if (cachedType.rank() != rank || cachedType.boundKind() != boundKind || cachedType.bound() != unannotatedBound)
				continue;
			if (Util.effectivelyEqual(cachedType.additionalBounds(), unannotatedOtherBounds))
				return (WildcardBinding) cachedType;
		}

		int length = cachedInfo.length;
		if (index == length) {
			System.arraycopy(cachedInfo, 0, cachedInfo = new TypeBinding[length * 2], 0, length);
			this.types[unannotatedGenericType.id] = cachedInfo;
		}
		TypeBinding wildcard = cachedInfo[index] = new WildcardBinding(unannotatedGenericType, rank, unannotatedBound, unannotatedOtherBounds, boundKind, this.environment);
	
		int typesLength = this.types.length;
		if (this.typeid == typesLength)
			System.arraycopy(this.types, 0, this.types = new TypeBinding[typesLength * 2][], 0, typesLength);
		this.types[this.typeid] = new TypeBinding[1];
		return (WildcardBinding) (this.types[wildcard.id = this.typeid++][0] = wildcard);
	}

	/* Return a unique annotation binding for an annotation with either no or all default element-value pairs.
	   We may return a resolved annotation when requested for unresolved one, but not vice versa. 
	*/
	public AnnotationBinding getAnnotationType(ReferenceBinding annotationType, boolean requiredResolved) {
		AnnotationBinding annotation = (AnnotationBinding) this.annotationTypes.get(annotationType);
		if (annotation == null) {
			if (requiredResolved)
				annotation = new AnnotationBinding(annotationType, Binding.NO_ELEMENT_VALUE_PAIRS);
			else 
				annotation = new UnresolvedAnnotationBinding(annotationType, Binding.NO_ELEMENT_VALUE_PAIRS, this.environment);
			this.annotationTypes.put(annotationType, annotation);
		}
		if (requiredResolved)
			annotation.resolve();
		return annotation;
	}

	public void reset() {
		this.annotationTypes = new SimpleLookupTable(16);
		this.typeid = TypeIds.T_LastWellKnownTypeId;
		this.types = new TypeBinding[TypeIds.T_LastWellKnownTypeId * 2][];
	}
	
	public void updateCaches(UnresolvedReferenceBinding unresolvedType, ReferenceBinding resolvedType) {
		final int unresolvedTypeId = unresolvedType.id;
		if (unresolvedTypeId == TypeIds.NoId)
			return;
		if (this.types[unresolvedTypeId] != null && this.types[unresolvedTypeId][0] == unresolvedType) {
			resolvedType.id = unresolvedTypeId;
			this.types[unresolvedTypeId][0] = resolvedType;
		}
		if (this.annotationTypes.get(unresolvedType) != null) { // update the key
			Object[] keys = this.annotationTypes.keyTable;
			for (int i = 0, l = keys.length; i < l; i++) {
				if (keys[i] == unresolvedType) {
					keys[i] = resolvedType; // hashCode is based on compoundName so this works.
					break;
				}
			}
		}
	}
}