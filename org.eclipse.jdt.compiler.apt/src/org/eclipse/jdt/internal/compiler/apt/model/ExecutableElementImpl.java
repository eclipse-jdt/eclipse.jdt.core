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
import javax.lang.model.element.PackageElement;
import javax.lang.model.element.TypeParameterElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.TypeMirror;

import org.eclipse.jdt.core.compiler.CharOperation;
import org.eclipse.jdt.internal.compiler.apt.dispatch.BaseProcessingEnvImpl;
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
	
	/* package */ ExecutableElementImpl(BaseProcessingEnvImpl env, MethodBinding binding) {
		super(env, binding);
	}

	@Override
	public <R, P> R accept(ElementVisitor<R, P> v, P p)
	{
		return v.visitExecutable(this, p);
	}

	@Override
	public List<? extends AnnotationMirror> getAnnotationMirrors() {
		AnnotationBinding[] annotations = ((MethodBinding)_binding).getAnnotations();
		return _env.getFactory().getAnnotationMirrors(annotations);
	}

	@Override
	public AnnotationValue getDefaultValue() {
		MethodBinding binding = (MethodBinding)_binding;
		Object defaultValue = binding.getDefaultValue();
		if (defaultValue != null) return new AnnotationValueImpl(_env, defaultValue, binding.returnType);
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
		return _env.getFactory().newElement(binding.declaringClass);
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
	PackageElement getPackage()
	{
		MethodBinding binding = (MethodBinding)_binding;
		if (null == binding.declaringClass) {
			return null;
		}
		return _env.getFactory().newPackageElement(binding.declaringClass.fPackage);
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
					VariableElement param = new VariableElementImpl(_env, argument.binding);
					params.add(param);
				}
			} else {
				// binary method
				int i = 0;
				for (TypeBinding typeBinding : binding.parameters) {
					StringBuilder builder = new StringBuilder("arg");//$NON-NLS-1$
					builder.append(i);
					VariableElement param = new VariableElementImpl(_env, new LocalVariableBinding(String.valueOf(builder).toCharArray(), typeBinding, 0, true));
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
		else return _env.getFactory().newTypeMirror(binding.returnType);
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
		MethodBinding binding = (MethodBinding)_binding;
		if (binding.thrownExceptions.length == 0) {
			return Collections.emptyList();
		}
		List<TypeMirror> list = new ArrayList<TypeMirror>(binding.thrownExceptions.length);
		for (ReferenceBinding exception : binding.thrownExceptions) {
			list.add(_env.getFactory().newTypeMirror(exception));
		}
		return list;
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
			params.add(_env.getFactory().newTypeParameterElement(variable, this));
		}
		return Collections.unmodifiableList(params);
	}

	@Override
	public boolean hides(Element hidden)
	{
		if (!(hidden instanceof ExecutableElementImpl)) {
			return false;
		}
		MethodBinding hiderBinding = (MethodBinding)_binding;
		MethodBinding hiddenBinding = (MethodBinding)((ExecutableElementImpl)hidden)._binding;
		// See JLS 8.4.8: hiding only applies to static methods
		if (!hiderBinding.isStatic() || !hiddenBinding.isStatic()) {
			return false;
		}
		
		// check names
		if (!CharOperation.equals(hiddenBinding.selector, hiderBinding.selector)) {
			return false;
		}

		
		// check parameters
		if (!_env.getLookupEnvironment().methodVerifier().doesMethodOverride(hiderBinding, hiddenBinding)) {
			return false;
		}
		
		return null != hiderBinding.declaringClass.findSuperTypeWithSameErasure(hiddenBinding.declaringClass); 
	}

	@Override
	public boolean isVarArgs() {
		return ((MethodBinding) _binding).isVarargs();
	}

}
