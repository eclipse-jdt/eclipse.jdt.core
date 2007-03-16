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

import java.util.List;

import javax.lang.model.element.AnnotationValue;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Name;
import javax.lang.model.element.TypeParameterElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.TypeMirror;

import org.eclipse.jdt.core.compiler.CharOperation;
import org.eclipse.jdt.internal.compiler.lookup.MethodBinding;
import org.eclipse.jdt.internal.compiler.lookup.ReferenceBinding;
import org.eclipse.jdt.internal.compiler.lookup.TypeConstants;

public class ExecutableElementImpl extends ElementImpl implements
		ExecutableElement {
	
	private Name _name = null;
	
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

	/* package */ ExecutableElementImpl(MethodBinding binding) {
		super(binding);
		// TODO Auto-generated constructor stub
	}

	public AnnotationValue getDefaultValue() {
		// TODO Auto-generated method stub
		return null;
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
		// TODO Auto-generated method stub
		return null;
	}

	public TypeMirror getReturnType() {
		// TODO Auto-generated method stub
		return null;
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
