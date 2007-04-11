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
import javax.lang.model.element.Modifier;
import javax.lang.model.element.PackageElement;
import javax.lang.model.element.TypeParameterElement;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;

import org.eclipse.jdt.internal.compiler.classfmt.ClassFileConstants;
import org.eclipse.jdt.internal.compiler.lookup.AnnotationBinding;
import org.eclipse.jdt.internal.compiler.lookup.ArrayBinding;
import org.eclipse.jdt.internal.compiler.lookup.BaseTypeBinding;
import org.eclipse.jdt.internal.compiler.lookup.Binding;
import org.eclipse.jdt.internal.compiler.lookup.MethodBinding;
import org.eclipse.jdt.internal.compiler.lookup.PackageBinding;
import org.eclipse.jdt.internal.compiler.lookup.ParameterizedTypeBinding;
import org.eclipse.jdt.internal.compiler.lookup.ReferenceBinding;
import org.eclipse.jdt.internal.compiler.lookup.TypeVariableBinding;
import org.eclipse.jdt.internal.compiler.lookup.VariableBinding;

/**
 * Creates javax.lang.model wrappers around JDT internal compiler bindings.
 */
public class Factory {

	/**
	 * Convert an array of compiler annotation bindings into a list of AnnotationMirror
	 * @return a non-null, possibly empty, unmodifiable list.
	 */
	public static List<? extends AnnotationMirror> getAnnotationMirrors(AnnotationBinding[] annotations) {
		if (null == annotations || 0 == annotations.length) {
			return Collections.emptyList();
		}
		List<AnnotationMirror> list = new ArrayList<AnnotationMirror>(annotations.length);
		for (AnnotationBinding annotation : annotations) {
			list.add(newAnnotationMirror(annotation));
		}
		return Collections.unmodifiableList(list);
	}

	/**
	 * Convert from the JDT's ClassFileConstants flags to the Modifier enum.
	 */
	public static Set<Modifier> getModifiers(int modifiers)
	{
		EnumSet<Modifier> result = EnumSet.noneOf(Modifier.class);
		if (0 != (modifiers & ClassFileConstants.AccPublic)) {
			result.add(Modifier.PUBLIC);
		}
		if (0 != (modifiers & ClassFileConstants.AccPrivate)) {
			result.add(Modifier.PRIVATE);
		}
		if (0 != (modifiers & ClassFileConstants.AccProtected)) {
			result.add(Modifier.PROTECTED);
		}
		if (0 != (modifiers & ClassFileConstants.AccStatic)) {
			result.add(Modifier.STATIC);
		}
		if (0 != (modifiers & ClassFileConstants.AccAbstract)) {
			result.add(Modifier.ABSTRACT);
		}
		if (0 != (modifiers & ClassFileConstants.AccFinal)) {
			result.add(Modifier.FINAL);
		}
		if (0 != (modifiers & ClassFileConstants.AccSynchronized)) {
			result.add(Modifier.SYNCHRONIZED);
		}
		if (0 != (modifiers & ClassFileConstants.AccVolatile)) {
			result.add(Modifier.VOLATILE);
		}
		if (0 != (modifiers & ClassFileConstants.AccTransient)) {
			result.add(Modifier.TRANSIENT);
		}
		if (0 != (modifiers & ClassFileConstants.AccNative)) {
			result.add(Modifier.NATIVE);
		}
		if (0 != (modifiers & ClassFileConstants.AccStrictfp)) {
			result.add(Modifier.STRICTFP);
		}
			
		return Collections.unmodifiableSet(result);
	}

	public static AnnotationMirror newAnnotationMirror(AnnotationBinding binding)
	{
		return new AnnotationMirrorImpl(binding);
	}
	
	public static Element newElement(Binding binding) {
		switch (binding.kind()) {
		case Binding.FIELD:
		case Binding.LOCAL:
		case Binding.VARIABLE:
			return new VariableElementImpl(binding);
		case Binding.TYPE:
		case Binding.GENERIC_TYPE:
			return new TypeElementImpl((ReferenceBinding)binding);
		case Binding.METHOD:
			return new ExecutableElementImpl((MethodBinding)binding);
		case Binding.RAW_TYPE:
		case Binding.PARAMETERIZED_TYPE:
			return new TypeElementImpl(((ParameterizedTypeBinding)binding).genericType());
		case Binding.PACKAGE:
			return new PackageElementImpl((PackageBinding)binding);
		// TODO: fill in the rest of these
		case Binding.IMPORT:
		case Binding.ARRAY_TYPE:
		case Binding.BASE_TYPE:
		case Binding.WILDCARD_TYPE:
		case Binding.TYPE_PARAMETER:
			throw new UnsupportedOperationException("NYI: binding type " + binding.kind()); //$NON-NLS-1$
		}
		return null;
	}
	
	public static DeclaredType newDeclaredType(ReferenceBinding binding) {
		if (binding.kind() == Binding.WILDCARD_TYPE) {
			// JDT wildcard binding is a subclass of reference binding, but in JSR269 they're siblings
			throw new IllegalArgumentException("A wildcard binding can't be turned into a DeclaredType"); //$NON-NLS-1$
		}
		return new DeclaredTypeImpl(binding);
	}

	/**
	 * Convenience method - equivalent to {@code (PackageElement)Factory.newElement(binding)}
	 */
	public static PackageElement newPackageElement(PackageBinding binding)
	{
		return new PackageElementImpl(binding);
	}

	/**
	 * Get a type mirror object representing the specified primitive type kind.
	 * This is used for VoidType, NoType and NullType as well as the usual int, float, etc. 
	 * @throw IllegalArgumentException if a non-primitive TypeKind is requested
	 */
	public static PrimitiveTypeImpl getPrimitiveType(TypeKind kind)
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
		case NONE:
			// TODO: how can we represent the NONE type?
			throw new UnsupportedOperationException("NYI"); //$NON-NLS-1$
		case NULL:
			return PrimitiveTypeImpl.NULL;
		case SHORT:
			return PrimitiveTypeImpl.SHORT;
		case VOID:
			return PrimitiveTypeImpl.VOID;
		default:
			throw new IllegalStateException();
		}
	}
	
	/**
	 * Convenience method to get the PrimitiveTypeImpl corresponding to a particular BaseTypeBinding.
	 */
	public static PrimitiveTypeImpl getPrimitiveType(BaseTypeBinding binding) {
		return getPrimitiveType(PrimitiveTypeImpl.getKind(binding));
	}

	public static TypeMirror newTypeMirror(Binding binding) {
		switch (binding.kind()) {
		case Binding.FIELD:
		case Binding.LOCAL:
		case Binding.VARIABLE:
			// For variables, return the type of the variable
			return newTypeMirror(((VariableBinding)binding).type);
			
		case Binding.METHOD:
		case Binding.PACKAGE:
		case Binding.IMPORT:
			throw new IllegalArgumentException("Invalid binding kind: " + binding.kind()); //$NON-NLS-1$
			
		case Binding.TYPE:
		case Binding.RAW_TYPE:
			return new DeclaredTypeImpl((ReferenceBinding)binding);
			
		case Binding.ARRAY_TYPE:
			return new ArrayTypeImpl((ArrayBinding)binding);
			
		case Binding.BASE_TYPE:
			return getPrimitiveType(PrimitiveTypeImpl.getKind((BaseTypeBinding)binding));
			
			// TODO: fill in the rest of these
		case Binding.PARAMETERIZED_TYPE:
		case Binding.WILDCARD_TYPE:
		case Binding.GENERIC_TYPE:
		case Binding.TYPE_PARAMETER:
			throw new UnsupportedOperationException("NYI: binding type " + binding.kind()); //$NON-NLS-1$
		}
		return null;
	}

	/**
	 * @param declaringElement the class, method, etc. that is parameterized by this parameter.
	 */
	public static TypeParameterElement newTypeParameterElement(TypeVariableBinding variable, Element declaringElement)
	{
		return new TypeParameterElementImpl(variable, declaringElement);
	}

}
