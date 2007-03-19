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

import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.AnnotationValue;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Name;
import javax.lang.model.element.TypeParameterElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.TypeMirror;

import org.eclipse.jdt.core.compiler.CharOperation;
import org.eclipse.jdt.internal.compiler.lookup.AnnotationBinding;
import org.eclipse.jdt.internal.compiler.lookup.MethodBinding;
import org.eclipse.jdt.internal.compiler.lookup.ReferenceBinding;
import org.eclipse.jdt.internal.compiler.lookup.TypeBinding;
import org.eclipse.jdt.internal.compiler.lookup.TypeConstants;

public class ExecutableElementImpl extends ElementImpl implements
		ExecutableElement {
	
	private Name _name = null;
	
	/* package */ ExecutableElementImpl(MethodBinding binding) {
		super(binding);
		// TODO Auto-generated constructor stub
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jdt.internal.compiler.apt.model.ElementImpl#getAnnotationMirrors()
	 */
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

	public AnnotationValue getDefaultValue() {
		// TODO Auto-generated method stub
		return null;
	}
	
	@Override
	public List<? extends Element> getEnclosedElements() {
		return Collections.emptyList();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jdt.internal.compiler.apt.model.ElementImpl#getEnclosingElement()
	 */
	@Override
	public Element getEnclosingElement() {
		MethodBinding binding = (MethodBinding)_binding;
		if (null == binding.declaringClass) {
			return null;
		}
		return Factory.newElement(binding.declaringClass);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jdt.internal.compiler.apt.model.ElementImpl#getFileName()
	 */
	@Override
	public String getFileName() {
		ReferenceBinding dc = ((MethodBinding)_binding).declaringClass;
		char[] name = dc.getFileName();
		if (name == null)
			return null;
		return new String(name);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jdt.internal.compiler.apt.model.ElementImpl#getKind()
	 */
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

	public List<? extends VariableElement> getParameters() {
		MethodBinding binding = (MethodBinding)_binding;
		if (0 == binding.parameters.length) {
			return Collections.emptyList();
		}
		List<VariableElement> params = new ArrayList<VariableElement>(binding.parameters.length);
		for (TypeBinding paramBinding : binding.parameters) {
			VariableElement param = new VariableElementImpl(paramBinding);
			params.add(param);
		}
		return Collections.unmodifiableList(params);
	}

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
			if (binding.isConstructor()) {
				_name = new NameImpl(binding.declaringClass.sourceName());
			} else {
				_name = new NameImpl(binding.selector);
			}
		}
		return _name;
	}
	
	public List<? extends TypeMirror> getThrownTypes() {
		// TODO Auto-generated method stub
		return null;
	}

	public List<? extends TypeParameterElement> getTypeParameters() {
		// TODO Auto-generated method stub
		return null;
	}

	public boolean isVarArgs() {
		// TODO Auto-generated method stub
		return false;
	}

}
