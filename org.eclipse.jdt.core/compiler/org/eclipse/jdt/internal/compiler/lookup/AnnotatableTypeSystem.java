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

import org.eclipse.jdt.internal.compiler.util.Util;
import org.eclipse.jdt.internal.compiler.util.SimpleLookupTable;

/* AnnotatableTypeSystem: Keep track of annotated types so as to provide unique bindings for identically annotated versions identical underlying "naked" types.
   As of now, we ensure uniqueness only for marker annotated types, i.e two instances of @NonNull String would have the same binding, while @T(1) X and @T(2) X
   will not. Binding uniqueness is only a memory optimization and is not essential for correctness of compilation. Various subsystems should expect to determine 
   binding identity/equality by calling TypeBinding.equalsEquals and not by using == operator.
 	
   ATS is a superset of UTS and is not a subclass of UTS for obvious reasons. ATS maintains a handle to the UnannotatedTypeSystem over whose types ATS adds
   annotations to create annotated types. ATS is AnnotatableTypeSystem and not AnnotatedTypeSystem, various methods may actually return unannotated types if the 
   input arguments do not specify any annotations and component types of the composite type being constructed are themselves unannotated.
 	
   We do not keep track of unannotated types here, that is done by UTS whose handle we maintain.
*/
public class AnnotatableTypeSystem {

	LookupEnvironment environment;
	UnannotatedTypeSystem unannotatedTypeSystem;
	
	private SimpleLookupTable annotatedTypes; // store of all annotated types created so far. Unlike earlier incarnation of LE, we maintain one look up table for all derived types.  
	
	public AnnotatableTypeSystem(LookupEnvironment environment) {
		this.environment = environment;
		this.unannotatedTypeSystem = new UnannotatedTypeSystem(environment);
		this.annotatedTypes = new SimpleLookupTable(16);
	}
	
	public TypeBinding getUnannotatedType(TypeBinding type) {
		return this.unannotatedTypeSystem.getUnannotatedType(type);
	}
	
	/* This method replaces the version that used to sit in LE. The parameter `annotations' is a flattened sequence of annotations, 
	   where each dimension's annotations end with a sentinel null.
	*/
	public ArrayBinding getArrayType(TypeBinding leafType, int dimensions, AnnotationBinding [] annotations) {
		
		if (!haveTypeAnnotations(leafType, annotations))
			return this.unannotatedTypeSystem.getArrayType(leafType, dimensions);
		
		TypeBinding[] cachedInfo = (TypeBinding[]) this.annotatedTypes.get(leafType);
		int index = 0;
		if (cachedInfo != null) {
			for (int max = cachedInfo.length; index < max; index++) {
				TypeBinding cachedType = cachedInfo[index];
				if (cachedType == null) break;
				if (cachedType.isArrayType() && cachedType.dimensions() == dimensions && Util.effectivelyEqual(cachedType.getTypeAnnotations(), annotations)) 
					return (ArrayBinding) cachedType;
			}
		} else {
			this.annotatedTypes.put(leafType, cachedInfo = new TypeBinding[4]);
		}
		
		int length = cachedInfo.length;
		if (index == length) {
			System.arraycopy(cachedInfo, 0, cachedInfo = new TypeBinding[length * 2], 0, length);
			this.annotatedTypes.put(leafType, cachedInfo);
		}
		// Add the newcomer, ensuring its identity is the same as the naked version of it.
		ArrayBinding unannotatedArrayType = this.unannotatedTypeSystem.getArrayType(leafType, dimensions);
		TypeBinding arrayBinding = new ArrayBinding(leafType, dimensions, this.environment);
		arrayBinding.id = unannotatedArrayType.id;
		arrayBinding.setTypeAnnotations(annotations, this.environment.globalOptions.isAnnotationBasedNullAnalysisEnabled);
		return (ArrayBinding) (cachedInfo[index] = arrayBinding);
	}

	public ParameterizedTypeBinding getParameterizedType(ReferenceBinding genericType, TypeBinding[] typeArguments, ReferenceBinding enclosingType, AnnotationBinding [] annotations) {
		
		if (!haveTypeAnnotations(genericType, enclosingType, typeArguments, annotations))
			return this.unannotatedTypeSystem.getParameterizedType(genericType, typeArguments, enclosingType);
		
		/* When restoring annotations from class files, we encounter a situation where the generic type comes in attributed with the annotations that should
		   really belong to the parameterized type that is being created just now. e.g @T List<String> => comes in as (@T List)<String>. The question really 
		   is List being parameterized by String and then the resultant type is annotated or is "@T List" being parameterized with String ? We don't care one
		   way or other except that we would want a uniform treatment. As a stop gap, we "repair" the situation here, so it is consistent with treatment of 
		   type references in source code. Probably need similar treatment for raw types and wildcards ?
		*/
		AnnotationBinding [] misplacedAnnotations = genericType.getTypeAnnotations();
		if (misplacedAnnotations != null && misplacedAnnotations != Binding.NO_ANNOTATIONS) {
			if (annotations != null && annotations != Binding.NO_ANNOTATIONS)
				throw new IllegalStateException(); // cannot cut both ways.
			annotations = misplacedAnnotations;
			genericType = (ReferenceBinding) this.unannotatedTypeSystem.getUnannotatedType(genericType);
		}
			
		int index = 0;
		TypeBinding[] cachedInfo = (TypeBinding[]) this.annotatedTypes.get(genericType);
		if (cachedInfo != null) {
			for (int max = cachedInfo.length; index < max; index++){
				TypeBinding cachedType = cachedInfo[index];
				if (cachedType == null) 
					break;
				if (!cachedType.isParameterizedType())
					continue;
				if (cachedType.enclosingType() == enclosingType && Util.effectivelyEqual(annotations, cachedType.getTypeAnnotations()) && Util.effectivelyEqual(cachedType.typeArguments(), typeArguments))
					return (ParameterizedTypeBinding) cachedType;
			}
		} else {
			this.annotatedTypes.put(genericType, cachedInfo = new TypeBinding[4]);
		}
		int length = cachedInfo.length;
		if (index == length) {
			System.arraycopy(cachedInfo, 0, cachedInfo = new TypeBinding[length * 2], 0, length);
			this.annotatedTypes.put(genericType, cachedInfo);
		}
		// Add the new comer, retaining the same type binding id as the naked type.
		ParameterizedTypeBinding unannotatedParameterizedType = this.unannotatedTypeSystem.getParameterizedType(genericType, typeArguments, enclosingType);
		TypeBinding parameterizedType = new ParameterizedTypeBinding(genericType, typeArguments, enclosingType, this.environment);
		parameterizedType.id = unannotatedParameterizedType.id;
		parameterizedType.setTypeAnnotations(annotations, this.environment.globalOptions.isAnnotationBasedNullAnalysisEnabled);
		return (ParameterizedTypeBinding) (cachedInfo[index] = parameterizedType);
	}
	
	public RawTypeBinding getRawType(ReferenceBinding genericType, ReferenceBinding enclosingType, AnnotationBinding [] annotations) {
		
		if (!haveTypeAnnotations(genericType, enclosingType, null, annotations))
			return this.unannotatedTypeSystem.getRawType(genericType, enclosingType);
		
		TypeBinding[] cachedInfo = (TypeBinding[]) this.annotatedTypes.get(genericType);
		int index = 0;
		if (cachedInfo != null) {
			for (int max = cachedInfo.length; index < max; index++) {
				TypeBinding cachedType = cachedInfo[index];
				if (cachedType == null)
					break;
				if (cachedType.isRawType() && cachedType.enclosingType() == enclosingType && Util.effectivelyEqual(cachedType.getTypeAnnotations(), annotations))
					return (RawTypeBinding) cachedType;
			}
		} else {
			this.annotatedTypes.put(genericType, cachedInfo = new TypeBinding[4]);
		}
		
		int length = cachedInfo.length;
		if (index == length) {
			System.arraycopy(cachedInfo, 0, cachedInfo = new TypeBinding[length * 2], 0, length);
			this.annotatedTypes.put(genericType, cachedInfo);
		}
		// Add the new comer, retaining the same type binding id as the naked type.
		RawTypeBinding unannotatedRawType = this.unannotatedTypeSystem.getRawType(genericType, enclosingType);
		TypeBinding rawType = new RawTypeBinding(genericType, enclosingType, this.environment);
		rawType.id = unannotatedRawType.id;
		rawType.setTypeAnnotations(annotations, this.environment.globalOptions.isAnnotationBasedNullAnalysisEnabled);
		return (RawTypeBinding) (cachedInfo[index] = rawType);
	}
		
	public WildcardBinding getWildcard(ReferenceBinding genericType, int rank, TypeBinding bound, TypeBinding[] otherBounds, int boundKind, AnnotationBinding [] annotations) {
		
		if (!haveTypeAnnotations(genericType, bound, otherBounds, annotations))
			return this.unannotatedTypeSystem.getWildcard(genericType, rank, bound, otherBounds, boundKind);
		
		if (genericType == null) // pseudo wildcard denoting composite bounds for lub computation
			genericType = ReferenceBinding.LUB_GENERIC;

		TypeBinding[] cachedInfo = (TypeBinding[]) this.annotatedTypes.get(genericType);
		int index = 0;
		if (cachedInfo != null) {
			for (int max = cachedInfo.length; index < max; index++) {
				TypeBinding cachedType = cachedInfo[index];
				if (cachedType == null) 
					break;
				if (!cachedType.isWildcard())
					continue;
				if (cachedType.rank() != rank || cachedType.boundKind() != boundKind || cachedType.bound() != bound)
					continue;
				if (Util.effectivelyEqual(cachedType.additionalBounds(), otherBounds) && Util.effectivelyEqual(cachedType.getTypeAnnotations(), annotations))
					return (WildcardBinding) cachedType;
			}
		} else {
			this.annotatedTypes.put(genericType, cachedInfo = new TypeBinding[4]);
		}

		int length = cachedInfo.length;
		if (index == length) {
			System.arraycopy(cachedInfo, 0, cachedInfo = new TypeBinding[length * 2], 0, length);
			this.annotatedTypes.put(genericType, cachedInfo);
		}
		// Add the new comer, retaining the same type binding id as the naked type.
		TypeBinding unannotatedWildcard = this.unannotatedTypeSystem.getWildcard(genericType, rank, bound, otherBounds, boundKind);
		TypeBinding wildcard = new WildcardBinding(genericType, rank, bound, otherBounds, boundKind, this.environment);
		wildcard.id = unannotatedWildcard.id;
		wildcard.setTypeAnnotations(annotations, this.environment.globalOptions.isAnnotationBasedNullAnalysisEnabled);
		return (WildcardBinding) (cachedInfo[index] = wildcard);
	}

	// Private subroutine for getAnnotatedType(TypeBinding type, AnnotationBinding[][] annotations)
	private TypeBinding getAnnotatedType(TypeBinding type, TypeBinding enclosingType, TypeBinding [] typeArguments, AnnotationBinding[] annotations) {
		TypeBinding[] cachedInfo = (TypeBinding[]) this.annotatedTypes.get(type);
		int i = 0;
		if (cachedInfo != null) {
			for (int length = cachedInfo.length; i < length; i++) {
				TypeBinding cachedType = cachedInfo[i];
				if (cachedType == null) break;
				if (cachedType.enclosingType() == enclosingType) {
					if (Util.effectivelyEqual(cachedType.getTypeAnnotations(), annotations) && Util.effectivelyEqual(cachedType.typeArguments(), typeArguments)) {
						return cachedType;
					}
				}
			}
		} else {
			this.annotatedTypes.put(type, cachedInfo = new TypeBinding[4]);
		}
		int length = cachedInfo.length;
		if (i == length) {
			System.arraycopy(cachedInfo, 0, cachedInfo = new TypeBinding[length * 2], 0, length);
			this.annotatedTypes.put(type, cachedInfo);
		}
		/* Add the new comer, retaining the same type binding id as the naked type. To materialize the new comer we can't use new since this is a general
		   purpose method designed to deal type bindings of all types. "Clone" the incoming type, specializing for any enclosing type and type arguments
		   that may themselves be possibly be annotated. This is so the binding for @Outer Outer.Inner != Outer.@Inner Inner != @Outer Outer.@Inner Inner.
		   Likewise so the bindings for @Readonly List<@NonNull String> != @Readonly List<@Nullable String> != @Readonly List<@Interned String> 
		*/
		TypeBinding unannotatedType = this.unannotatedTypeSystem.getUnannotatedType(type);
		TypeBinding annotatedType = type.clone(enclosingType, typeArguments);
		annotatedType.id = unannotatedType.id;
		annotatedType.setTypeAnnotations(annotations, this.environment.globalOptions.isAnnotationBasedNullAnalysisEnabled);
		return cachedInfo[i] = annotatedType;
	}

	/* Take a type and apply annotations to various components of it. By construction when we see the type reference @Outer Outer.@Middle Middle.@Inner Inner,
	   we first construct the binding for Outer.Middle.Inner and then annotate various parts of it. Likewise for PQTR's binding.
	*/
	public TypeBinding getAnnotatedType(TypeBinding type, AnnotationBinding[][] annotations) {
		
		if (type == null || !type.isValidBinding() || annotations == null || annotations.length == 0)
			return type;
		
		TypeBinding annotatedType = null;
		switch (type.kind()) {
			case Binding.ARRAY_TYPE:
				ArrayBinding arrayBinding = (ArrayBinding) type;
				annotatedType = getArrayType(arrayBinding.leafComponentType, arrayBinding.dimensions, flattenedAnnotations(annotations));
				break;
			case Binding.BASE_TYPE:
			case Binding.TYPE:
			case Binding.GENERIC_TYPE:
			case Binding.PARAMETERIZED_TYPE:
			case Binding.RAW_TYPE:
			case Binding.TYPE_PARAMETER:
			case Binding.WILDCARD_TYPE:
				/* Taking the binding of QTR as an example, there could be different annotatable components, but we come in a with a single binding, e.g: 
				   @T Z;                                      type => Z  annotations => [[@T]]
				   @T Y.@T Z                                  type => Z  annotations => [[@T][@T]]
				   @T X.@T Y.@T Z                             type => Z  annotations => [[][][@T][@T][@T]] 
				   java.lang.@T X.@T Y.@T Z
				   in all these cases the incoming type binding is for Z, but annotations are for different levels. Align their layout for proper attribution.
				 */
				int levels = type.depth() + 1;
				TypeBinding [] types = new TypeBinding[levels];
				types[--levels] = type;
				TypeBinding enclosingType = type.enclosingType();
				while (enclosingType != null) {
					types[--levels] = enclosingType;
					enclosingType = enclosingType.enclosingType();
				}
				// Locate the outermost type being annotated. Beware annotations.length could be > types.length (for package qualified names in QTR/PQTR)
				levels = annotations.length;
				int i, j = types.length - levels;
				for (i = 0 ; i < levels; i++, j++) {
					if (annotations[i] != null && annotations[i].length > 0)
						break;
				}
				if (i == levels) // empty annotations array ? 
					return type;
				// types[j] is the first component being annotated. Its annotations are annotations[i]
				for (enclosingType = j == 0 ? null : types[j - 1]; i < levels; i++, j++) {
					annotatedType = getAnnotatedType(types[j], enclosingType, types[j].typeArguments(), annotations[i]);
					enclosingType = annotatedType;
				}
				break;
		}
		return annotatedType;
	}

	public AnnotationBinding getAnnotationType(ReferenceBinding annotationType) {
		return this.unannotatedTypeSystem.getAnnotationType(annotationType); // deflect, annotation type uses cannot be type annotated.
	}

	private boolean haveTypeAnnotations(TypeBinding baseType, TypeBinding someType, TypeBinding[] someTypes, AnnotationBinding[] annotations) {
		if (baseType != null && baseType.hasTypeAnnotations())
			return true;
		if (someType != null && someType.hasTypeAnnotations())
			return true;
		for (int i = 0, length = annotations == null ? 0 : annotations.length; i < length; i++)
			if (annotations [i] != null)
				return true;
		for (int i = 0, length = someTypes == null ? 0 : someTypes.length; i < length; i++)
			if (someTypes[i].hasTypeAnnotations())
				return true;
		return false;
	}

	private boolean haveTypeAnnotations(TypeBinding leafType, AnnotationBinding[] annotations) {
		return haveTypeAnnotations(leafType, null, null, annotations);
	}

	/* Utility method to "flatten" annotations. For multidimensional arrays, we encode the annotations into a flat array 
	   where a null separates the annotations of dimension n from dimension n - 1 as well as dimenion n + 1. There is a
	   final null always.
	*/
	static AnnotationBinding [] flattenedAnnotations (AnnotationBinding [][] annotations) {

		if (annotations == null || annotations.length == 0)
			return Binding.NO_ANNOTATIONS;

		int levels = annotations.length;
		int length = levels;
		for (int i = 0; i < levels; i++) {
			length += annotations[i] == null ? 0 : annotations[i].length;
		}
		if (length == 0)
			return Binding.NO_ANNOTATIONS;

		AnnotationBinding[] series = new AnnotationBinding [length];
		int index = 0;
		for (int i = 0; i < levels; i++) {
			final int annotationsLength = annotations[i] == null ? 0 : annotations[i].length;
			if (annotationsLength > 0) {
				System.arraycopy(annotations[i], 0, series, index, annotationsLength);
				index += annotationsLength;
			}
			series[index++] = null;
		}
		if (index != length)
			throw new IllegalStateException();
		return series;
	}

	public final void reset() { // develop amnesia 
		this.annotatedTypes = new SimpleLookupTable(16);
		this.unannotatedTypeSystem.reset();
	}

	public void updateCaches(UnresolvedReferenceBinding unresolvedType, ReferenceBinding resolvedType) {
		if (this.annotatedTypes.get(unresolvedType) != null) { // update the key
			Object[] keys = this.annotatedTypes.keyTable;
			for (int i = 0, l = keys.length; i < l; i++) {
				if (keys[i] == unresolvedType) {
					keys[i] = resolvedType; // hashCode is based on compoundName so this works.
					break;
				}
			}
		}
		this.unannotatedTypeSystem.updateCaches(unresolvedType.prototype, unresolvedType.prototype.resolvedType);
	}
}
