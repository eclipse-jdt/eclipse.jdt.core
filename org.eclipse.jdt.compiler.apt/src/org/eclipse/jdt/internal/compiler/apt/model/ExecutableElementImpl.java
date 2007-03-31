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
import java.util.Set;

import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.AnnotationValue;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.ElementVisitor;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.Name;
import javax.lang.model.element.TypeParameterElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.TypeMirror;

import org.eclipse.jdt.core.compiler.CharOperation;
import org.eclipse.jdt.internal.compiler.ast.AbstractMethodDeclaration;
import org.eclipse.jdt.internal.compiler.ast.Argument;
import org.eclipse.jdt.internal.compiler.lookup.AnnotationBinding;
import org.eclipse.jdt.internal.compiler.lookup.LocalVariableBinding;
import org.eclipse.jdt.internal.compiler.lookup.MethodBinding;
import org.eclipse.jdt.internal.compiler.lookup.ReferenceBinding;
import org.eclipse.jdt.internal.compiler.lookup.TypeBinding;
import org.eclipse.jdt.internal.compiler.lookup.TypeConstants;
import org.eclipse.jdt.internal.compiler.lookup.TypeVariableBinding;

public class ExecutableElementImpl extends ElementImpl implements
		ExecutableElement {
	
	private Name _name = null;
	
	/* package */ ExecutableElementImpl(MethodBinding binding) {
		super(binding);
		// TODO Auto-generated constructor stub
	}

	@Override
	public <R, P> R accept(ElementVisitor<R, P> v, P p)
	{
		return v.visitExecutable(this, p);
	}

	@Override
	public List<? extends AnnotationMirror> getAnnotationMirrors() {
		AnnotationBinding[] annotations = ((MethodBinding)_binding).getAnnotations();
		if (0 == annotations.length) {
			return Collections.emptyList();
		}
		List<AnnotationMirror> list = new ArrayList<AnnotationMirror>(annotations.length);
		for (AnnotationBinding annotation : annotations) {
			list.add(AnnotationMirrorImpl.getAnnotationMirror(annotation));
		}
		return Collections.unmodifiableList(list);
	}

	@Override
	public AnnotationValue getDefaultValue() {
		// TODO Auto-generated method stub
		return null;
	}
	
	@Override
	public List<? extends Element> getEnclosedElements() {
		return Collections.emptyList();
	}

	@Override
	public Element getEnclosingElement() {
		MethodBinding binding = (MethodBinding)_binding;
		if (null == binding.declaringClass) {
			return null;
		}
		return Factory.newElement(binding.declaringClass);
	}

	@Override
	public String getFileName() {
		ReferenceBinding dc = ((MethodBinding)_binding).declaringClass;
		char[] name = dc.getFileName();
		if (name == null)
			return null;
		return new String(name);
	}

	@Override
	public ElementKind getKind() {
		MethodBinding binding = (MethodBinding)_binding;
		if (binding.isConstructor()) {
			return ElementKind.CONSTRUCTOR;
		}
		else if (CharOperation.equals(binding.selector, TypeConstants.CLINIT)) {
			return ElementKind.STATIC_INIT;
		}
		else if (CharOperation.equals(binding.selector, TypeConstants.INIT)) {
			return ElementKind.INSTANCE_INIT;
		}
		else {
			return ElementKind.METHOD;
		}
	}

	@Override
	public Set<Modifier> getModifiers() {
		MethodBinding binding = (MethodBinding)_binding;
		return Factory.getModifiers(binding.modifiers);
	}

	@Override
	public List<? extends VariableElement> getParameters() {
		MethodBinding binding = (MethodBinding)_binding;
		int length = binding.parameters == null ? 0 : binding.parameters.length;
		if (0 != length) {
			AbstractMethodDeclaration methodDeclaration = binding.sourceMethod();
			List<VariableElement> params = new ArrayList<VariableElement>(length);
			if (methodDeclaration != null) {
				for (Argument argument : methodDeclaration.arguments) {
					VariableElement param = new VariableElementImpl(argument.binding);
					params.add(param);
				}
			} else {
				// binary method
				int i = 0;
				for (TypeBinding typeBinding : binding.parameters) {
					StringBuilder builder = new StringBuilder("arg");//$NON-NLS-1$
					builder.append(i);
					VariableElement param = new VariableElementImpl(new LocalVariableBinding(String.valueOf(builder).toCharArray(), typeBinding, 0, true));
					params.add(param);
					i++;
				}
			}
			return Collections.unmodifiableList(params);
		}
		return Collections.emptyList();
	}

	@Override
	public TypeMirror getReturnType() {
		MethodBinding binding = (MethodBinding)_binding;
		if (binding.returnType == null) {
			return null;
		}
		else return Factory.newTypeMirror(binding.returnType);
	}

	@Override
	public Name getSimpleName() {
		MethodBinding binding = (MethodBinding)_binding;
		if (_name == null) {
			_name = new NameImpl(binding.selector);
		}
		return _name;
	}
	
	@Override
	public List<? extends TypeMirror> getThrownTypes() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<? extends TypeParameterElement> getTypeParameters() {
		MethodBinding binding = (MethodBinding)_binding;
		TypeVariableBinding[] variables = binding.typeVariables();
		if (variables.length == 0) {
			return Collections.emptyList();
		}
		List<TypeParameterElement> params = new ArrayList<TypeParameterElement>(variables.length); 
		for (TypeVariableBinding variable : variables) {
			params.add(Factory.newTypeParameterElement(variable, this));
		}
		return Collections.unmodifiableList(params);
	}

	@Override
	public boolean isVarArgs() {
		return ((MethodBinding) _binding).isVarargs();
	}

}
