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

import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;

import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.PackageElement;
import javax.lang.model.element.TypeParameterElement;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.NoType;
import javax.lang.model.type.NullType;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;

import org.eclipse.jdt.internal.compiler.apt.dispatch.BaseProcessingEnvImpl;
import org.eclipse.jdt.internal.compiler.classfmt.ClassFileConstants;
import org.eclipse.jdt.internal.compiler.lookup.AnnotationBinding;
import org.eclipse.jdt.internal.compiler.lookup.ArrayBinding;
import org.eclipse.jdt.internal.compiler.lookup.BaseTypeBinding;
import org.eclipse.jdt.internal.compiler.lookup.Binding;
import org.eclipse.jdt.internal.compiler.lookup.MethodBinding;
import org.eclipse.jdt.internal.compiler.lookup.PackageBinding;
import org.eclipse.jdt.internal.compiler.lookup.ParameterizedTypeBinding;
import org.eclipse.jdt.internal.compiler.lookup.ReferenceBinding;
import org.eclipse.jdt.internal.compiler.lookup.TypeConstants;
import org.eclipse.jdt.internal.compiler.lookup.TypeIds;
import org.eclipse.jdt.internal.compiler.lookup.TypeVariableBinding;
import org.eclipse.jdt.internal.compiler.lookup.VariableBinding;
import org.eclipse.jdt.internal.compiler.lookup.WildcardBinding;

/**
 * Creates javax.lang.model wrappers around JDT internal compiler bindings.
 */
public class Factory {
	
	private final BaseProcessingEnvImpl _env;
	
	/**
	 * This object should only be constructed by the BaseProcessingEnvImpl.
	 */
	public Factory(BaseProcessingEnvImpl env) {
		_env = env;
	}

	/**
	 * Convert an array of compiler annotation bindings into a list of AnnotationMirror
	 * @return a non-null, possibly empty, unmodifiable list.
	 */
	public List<? extends AnnotationMirror> getAnnotationMirrors(AnnotationBinding[] annotations) {
		if (null == annotations || 0 == annotations.length) {
			return Collections.emptyList();
		}
		List<AnnotationMirror> list = new ArrayList<AnnotationMirror>(annotations.length);
		for (AnnotationBinding annotation : annotations) {
			if (annotation == null) continue;
			list.add(newAnnotationMirror(annotation));
		}
		return Collections.unmodifiableList(list);
	}
	
	private static void appendModifier(Set<Modifier> result, int modifiers, int modifierConstant, Modifier modifier) {
		if ((modifiers & modifierConstant) != 0) {
			result.add(modifier);
		}
	}
	
	private static void decodeModifiers(Set<Modifier> result, int modifiers, int[] checkBits) {
		if (checkBits == null) return;
		for (int i = 0, max = checkBits.length; i < max; i++) {
			switch(checkBits[i]) {
				case ClassFileConstants.AccPublic :
					appendModifier(result, modifiers, checkBits[i], Modifier.PUBLIC);
					break;
				case ClassFileConstants.AccProtected:
					appendModifier(result, modifiers, checkBits[i], Modifier.PROTECTED);
					break;
				case ClassFileConstants.AccPrivate :
					appendModifier(result, modifiers, checkBits[i], Modifier.PRIVATE);
					break;
				case ClassFileConstants.AccAbstract :
					appendModifier(result, modifiers, checkBits[i], Modifier.ABSTRACT);
					break;
				case ClassFileConstants.AccStatic :
					appendModifier(result, modifiers, checkBits[i], Modifier.STATIC);
					break;
				case ClassFileConstants.AccFinal :
					appendModifier(result, modifiers, checkBits[i], Modifier.FINAL);
					break;
				case ClassFileConstants.AccSynchronized :
					appendModifier(result, modifiers, checkBits[i], Modifier.SYNCHRONIZED);
					break;
				case ClassFileConstants.AccNative :
					appendModifier(result, modifiers, checkBits[i], Modifier.NATIVE);
					break;
				case ClassFileConstants.AccStrictfp :
					appendModifier(result, modifiers, checkBits[i], Modifier.STRICTFP);
					break;
				case ClassFileConstants.AccTransient :
					appendModifier(result, modifiers, checkBits[i], Modifier.TRANSIENT);
					break;
				case ClassFileConstants.AccVolatile :
					appendModifier(result, modifiers, checkBits[i], Modifier.VOLATILE);
					break;
			}
		}
	}
	/**
	 * Convert from the JDT's ClassFileConstants flags to the Modifier enum.
	 */
	public static Set<Modifier> getModifiers(int modifiers, ElementKind kind)
	{
		EnumSet<Modifier> result = EnumSet.noneOf(Modifier.class);
		switch(kind) {
			case CONSTRUCTOR :
			case METHOD :
				// modifiers for methods
				decodeModifiers(result, modifiers, new int[] {
					ClassFileConstants.AccPublic,
					ClassFileConstants.AccProtected,
					ClassFileConstants.AccPrivate,
					ClassFileConstants.AccAbstract,
					ClassFileConstants.AccStatic,
					ClassFileConstants.AccFinal,
					ClassFileConstants.AccSynchronized,
					ClassFileConstants.AccNative,
					ClassFileConstants.AccStrictfp
				});
				break;
			case FIELD :
			case ENUM_CONSTANT :
				// for fields
				decodeModifiers(result, modifiers, new int[] {
					ClassFileConstants.AccPublic,
					ClassFileConstants.AccProtected,
					ClassFileConstants.AccPrivate,
					ClassFileConstants.AccStatic,
					ClassFileConstants.AccFinal,
					ClassFileConstants.AccTransient,
					ClassFileConstants.AccVolatile
				});
				break;
			case ANNOTATION_TYPE :
			case ENUM :
			case INTERFACE :
			case CLASS :
				// for type
				decodeModifiers(result, modifiers, new int[] {
					ClassFileConstants.AccPublic,
					ClassFileConstants.AccAbstract,
					ClassFileConstants.AccFinal,
					ClassFileConstants.AccPrivate,
					ClassFileConstants.AccStatic,
				});
		}
		return Collections.unmodifiableSet(result);
	}

	public AnnotationMirror newAnnotationMirror(AnnotationBinding binding)
	{
		return new AnnotationMirrorImpl(_env, binding);
	}
	
	public Element newElement(Binding binding) {
		switch (binding.kind()) {
		case Binding.FIELD:
		case Binding.LOCAL:
		case Binding.VARIABLE:
			return new VariableElementImpl(_env, binding);
		case Binding.TYPE:
		case Binding.GENERIC_TYPE:
			ReferenceBinding referenceBinding = (ReferenceBinding)binding;
			if (referenceBinding.sourceName == TypeConstants.PACKAGE_INFO_NAME) {
				return new PackageElementImpl(_env, referenceBinding.fPackage);
			}
			return new TypeElementImpl(_env, referenceBinding);
		case Binding.METHOD:
			return new ExecutableElementImpl(_env, (MethodBinding)binding);
		case Binding.RAW_TYPE:
		case Binding.PARAMETERIZED_TYPE:
			return new TypeElementImpl(_env, ((ParameterizedTypeBinding)binding).genericType());
		case Binding.PACKAGE:
			return new PackageElementImpl(_env, (PackageBinding)binding);
		case Binding.TYPE_PARAMETER:
			return new TypeParameterElementImpl(_env, (TypeVariableBinding)binding);
		// TODO: fill in the rest of these
		case Binding.IMPORT:
		case Binding.ARRAY_TYPE:
		case Binding.BASE_TYPE:
		case Binding.WILDCARD_TYPE:
			throw new UnsupportedOperationException("NYI: binding type " + binding.kind()); //$NON-NLS-1$
		}
		return null;
	}
	
	public DeclaredType newDeclaredType(ReferenceBinding binding) {
		if (binding.kind() == Binding.WILDCARD_TYPE) {
			// JDT wildcard binding is a subclass of reference binding, but in JSR269 they're siblings
			throw new IllegalArgumentException("A wildcard binding can't be turned into a DeclaredType"); //$NON-NLS-1$
		}
		return new DeclaredTypeImpl(_env, binding);
	}

	/**
	 * Convenience method - equivalent to {@code (PackageElement)Factory.newElement(binding)}
	 */
	public PackageElement newPackageElement(PackageBinding binding)
	{
		return new PackageElementImpl(_env, binding);
	}
	
	public NullType getNullType() {
		return NoTypeImpl.NULL_TYPE;
	}

	public NoType getNoType(TypeKind kind)
	{
		switch (kind) {
		case NONE:
			return NoTypeImpl.NO_TYPE_NONE;
		case VOID:
			return NoTypeImpl.NO_TYPE_VOID;
		case PACKAGE:
			return NoTypeImpl.NO_TYPE_PACKAGE;
		default:
			throw new IllegalArgumentException();
		}
	}

	/**
	 * Get a type mirror object representing the specified primitive type kind.
	 * @throw IllegalArgumentException if a non-primitive TypeKind is requested
	 */
	public PrimitiveTypeImpl getPrimitiveType(TypeKind kind)
	{
		switch (kind) {
		case BOOLEAN:
			return PrimitiveTypeImpl.BOOLEAN;
		case BYTE:
			return PrimitiveTypeImpl.BYTE;
		case CHAR:
			return PrimitiveTypeImpl.CHAR;
		case DOUBLE:
			return PrimitiveTypeImpl.DOUBLE;
		case FLOAT:
			return PrimitiveTypeImpl.FLOAT;
		case INT:
			return PrimitiveTypeImpl.INT;
		case LONG:
			return PrimitiveTypeImpl.LONG;
		case SHORT:
			return PrimitiveTypeImpl.SHORT;
		default:
			throw new IllegalStateException();
		}
	}
	
	/**
	 * Convenience method to get the PrimitiveTypeImpl corresponding to a particular BaseTypeBinding.
	 */
	public PrimitiveTypeImpl getPrimitiveType(BaseTypeBinding binding) {
		return getPrimitiveType(PrimitiveTypeImpl.getKind(binding));
	}

	/**
	 * Given a binding of uncertain type, try to create the right sort of TypeMirror for it.
	 */
	public TypeMirror newTypeMirror(Binding binding) {
		switch (binding.kind()) {
		case Binding.FIELD:
		case Binding.LOCAL:
		case Binding.VARIABLE:
			// For variables, return the type of the variable
			return newTypeMirror(((VariableBinding)binding).type);
			
		case Binding.PACKAGE:
			return getNoType(TypeKind.PACKAGE);
			
		case Binding.IMPORT:
			throw new UnsupportedOperationException("NYI: import type " + binding.kind()); //$NON-NLS-1$

		case Binding.METHOD:
			return new ExecutableTypeImpl(_env, (MethodBinding) binding);
			
		case Binding.TYPE:
		case Binding.RAW_TYPE:
		case Binding.GENERIC_TYPE:
		case Binding.PARAMETERIZED_TYPE:
			return new DeclaredTypeImpl(_env, (ReferenceBinding)binding);
			
		case Binding.ARRAY_TYPE:
			return new ArrayTypeImpl(_env, (ArrayBinding)binding);
			
		case Binding.BASE_TYPE:
			BaseTypeBinding btb = (BaseTypeBinding)binding;
			switch (btb.id) {
			case TypeIds.T_void:
				return getNoType(TypeKind.VOID);
			case TypeIds.T_null:
				return getNullType();
			default:
				return getPrimitiveType(PrimitiveTypeImpl.getKind((BaseTypeBinding)binding));
			}

		case Binding.WILDCARD_TYPE:
			return new WildcardTypeImpl(_env, (WildcardBinding) binding);

		case Binding.TYPE_PARAMETER:
			return new TypeVariableImpl(_env, (TypeVariableBinding) binding);
		}
		return null;
	}

	/**
	 * @param declaringElement the class, method, etc. that is parameterized by this parameter.
	 */
	public TypeParameterElement newTypeParameterElement(TypeVariableBinding variable, Element declaringElement)
	{
		return new TypeParameterElementImpl(_env, variable, declaringElement);
	}

}
