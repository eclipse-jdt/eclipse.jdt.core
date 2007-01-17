package org.eclipse.jdt.internal.compiler.apt.model;

import java.util.HashMap;
import java.util.Map;

import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.AnnotationValue;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.type.DeclaredType;

import org.eclipse.jdt.internal.compiler.lookup.AnnotationBinding;
import org.eclipse.jdt.internal.compiler.lookup.ElementValuePair;
import org.eclipse.jdt.internal.compiler.lookup.ReferenceBinding;

public class AnnotationMirrorImpl implements AnnotationMirror {
	
	AnnotationBinding _binding;
	
	public static AnnotationMirror getAnnotationMirror(AnnotationBinding binding)
	{
		return new AnnotationMirrorImpl(binding);
	}
	
	private AnnotationMirrorImpl(AnnotationBinding binding) {
		_binding = binding;
	}

	public DeclaredType getAnnotationType() {
		ReferenceBinding annoType = _binding.getAnnotationType();
		return DeclaredTypeImpl.newDeclaredTypeImpl(annoType);
	}

	public Map<? extends ExecutableElement, ? extends AnnotationValue> getElementValues() {
		ElementValuePair[] pairs = _binding.getElementValuePairs();
		Map<ExecutableElement, AnnotationValue> valueMap =
			new HashMap<ExecutableElement, AnnotationValue>(pairs.length);
		for (ElementValuePair pair : pairs) {
			ExecutableElement e = new ExecutableElementImpl(pair.getMethodBinding());
			AnnotationValue v = new AnnotationValueImpl(pair.getValue());
			valueMap.put(e, v);
		}
		return valueMap;
	}

}
