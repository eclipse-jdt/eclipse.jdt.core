/*******************************************************************************
 * Copyright (c) 2005, 2007 IBM Corporation and others.
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

import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Name;
import javax.lang.model.element.NestingKind;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.TypeParameterElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.TypeMirror;

import org.eclipse.jdt.core.compiler.CharOperation;
import org.eclipse.jdt.internal.compiler.lookup.FieldBinding;
import org.eclipse.jdt.internal.compiler.lookup.MethodBinding;
import org.eclipse.jdt.internal.compiler.lookup.ReferenceBinding;

public class TypeElementImpl extends ElementImpl implements TypeElement {
	
	/* package */ TypeElementImpl(ReferenceBinding binding) {
		super(binding);
		// TODO Auto-generated constructor stub
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.jdt.internal.compiler.apt.model.ElementImpl#getEnclosedElements()
	 */
	@Override
	public List<? extends Element> getEnclosedElements() {
		ReferenceBinding binding = (ReferenceBinding)_binding;
		List<Element> enclosed = new ArrayList<Element>(binding.fieldCount() + binding.methods().length);
		for (MethodBinding method : binding.methods()) {
			ExecutableElement executable = new ExecutableElementImpl(method);
			enclosed.add(executable);
		}
		for (FieldBinding field : binding.fields()) {
			 VariableElement variable = new VariableElementImpl(field);
			 enclosed.add(variable);
		}
		return Collections.unmodifiableList(enclosed);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jdt.internal.compiler.apt.model.ElementImpl#getEnclosingElement()
	 */
	@Override
	public Element getEnclosingElement() {
		ReferenceBinding binding = (ReferenceBinding)_binding;
		ReferenceBinding enclosingType = binding.enclosingType();
		if (null == enclosingType) {
			// this is a top level type; get its package
			return new PackageElementImpl(binding.fPackage);
		}
		else {
			return Factory.newElement(binding.enclosingType());
		}
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jdt.internal.compiler.apt.model.ElementImpl#getFileName()
	 */
	@Override
	public String getFileName() {
		char[] name = ((ReferenceBinding)_binding).getFileName();
		if (name == null)
			return null;
		return new String(name);
	}
	
	public List<? extends TypeMirror> getInterfaces() {
		ReferenceBinding binding = (ReferenceBinding)_binding;
		if (null == binding.superInterfaces() || binding.superInterfaces().length == 0) {
			return Collections.emptyList();
		}
		List<TypeMirror> interfaces = new ArrayList<TypeMirror>(binding.superInterfaces().length);
		for (ReferenceBinding interfaceBinding : binding.superInterfaces()) {
			TypeMirror interfaceType = Factory.newTypeMirror(interfaceBinding);
			interfaces.add(interfaceType);
		}
		return Collections.unmodifiableList(interfaces);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jdt.internal.compiler.apt.model.ElementImpl#getKind()
	 */
	@Override
	public ElementKind getKind() {
		ReferenceBinding refBinding = (ReferenceBinding)_binding;
		// The order of these comparisons is important: e.g., enum is subset of class
		if (refBinding.isEnum()) {
			return ElementKind.ENUM;
		}
		else if (refBinding.isAnnotationType()) {
			return ElementKind.ANNOTATION_TYPE;
		}
		else if (refBinding.isInterface()) {
			return ElementKind.INTERFACE;
		}
		else if (refBinding.isClass()) {
			return ElementKind.CLASS;
		}
		else {
			throw new IllegalArgumentException("TypeElement " + new String(refBinding.shortReadableName()) +  //$NON-NLS-1$
					" has unexpected attributes " + refBinding.modifiers); //$NON-NLS-1$
		}
	}

	public NestingKind getNestingKind() {
		// TODO Auto-generated method stub
		return null;
	}

	public Name getQualifiedName() {
		ReferenceBinding binding = (ReferenceBinding)_binding;
		//TODO: what is the right way to get this (including member types, parameterized types, ...?
		return new NameImpl(CharOperation.concatWith(binding.compoundName, '.'));
	}

	public TypeMirror getSuperclass() {
		ReferenceBinding binding = (ReferenceBinding)_binding;
		ReferenceBinding superBinding = binding.superclass();
		if (null == superBinding) {
			return null;
		}
		// superclass of a type must be a DeclaredType
		return Factory.newDeclaredType(superBinding);
	}
	
	public List<? extends TypeParameterElement> getTypeParameters() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String toString() {
		return new String(_binding.readableName());
	}

}
