package org.eclipse.jdt.internal.compiler.apt.model;

import java.util.List;

import javax.lang.model.element.AnnotationValue;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Name;
import javax.lang.model.element.TypeParameterElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.TypeMirror;

import org.eclipse.jdt.internal.compiler.lookup.MethodBinding;

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

	ExecutableElementImpl(MethodBinding binding) {
		super(binding);
		// TODO Auto-generated constructor stub
	}

	public AnnotationValue getDefaultValue() {
		// TODO Auto-generated method stub
		return null;
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
