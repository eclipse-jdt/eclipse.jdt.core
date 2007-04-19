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

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.AnnotationValue;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.type.DeclaredType;

import org.eclipse.jdt.internal.compiler.apt.dispatch.BaseProcessingEnvImpl;
import org.eclipse.jdt.internal.compiler.lookup.AnnotationBinding;
import org.eclipse.jdt.internal.compiler.lookup.ElementValuePair;
import org.eclipse.jdt.internal.compiler.lookup.MethodBinding;
import org.eclipse.jdt.internal.compiler.lookup.ReferenceBinding;

public class AnnotationMirrorImpl implements AnnotationMirror {
	
	public final BaseProcessingEnvImpl _env;
	public final AnnotationBinding _binding;
	
	/* package */ AnnotationMirrorImpl(BaseProcessingEnvImpl env, AnnotationBinding binding) {
		_env = env;
		_binding = binding;
	}

	public DeclaredType getAnnotationType() {
		ReferenceBinding annoType = _binding.getAnnotationType();
		return _env.getFactory().newDeclaredType(annoType);
	}

	/**
	 * @return all the members of this annotation mirror that have explicit values.
	 * Default values are not included.
	 */
	public Map<? extends ExecutableElement, ? extends AnnotationValue> getElementValues() {
		ElementValuePair[] pairs = _binding.getElementValuePairs();
		Map<ExecutableElement, AnnotationValue> valueMap =
			new HashMap<ExecutableElement, AnnotationValue>(pairs.length);
		for (ElementValuePair pair : pairs) {
			MethodBinding method = pair.getMethodBinding();
			ExecutableElement e = new ExecutableElementImpl(_env, method);
			AnnotationValue v = new AnnotationValueImpl(_env, pair.getValue(), method.returnType);
			valueMap.put(e, v);
		}
		return Collections.unmodifiableMap(valueMap);
	}

	/**
	 * {@see Elements#getElementValuesWithDefaults()}
	 * @return all the members of this annotation mirror that have explicit or default
	 * values.
	 */
	public Map<? extends ExecutableElement, ? extends AnnotationValue> getElementValuesWithDefaults() {
		ElementValuePair[] pairs = _binding.getElementValuePairs();
		ReferenceBinding annoType = _binding.getAnnotationType();
		Map<ExecutableElement, AnnotationValue> valueMap =
			new HashMap<ExecutableElement, AnnotationValue>();
		for (MethodBinding method : annoType.methods()) {
			// if binding is in ElementValuePair list, then get value from there
			boolean foundExplicitValue = false;
			for (int i = 0; i < pairs.length; ++i) {
				MethodBinding explicitBinding = pairs[i].getMethodBinding();
				if (method == explicitBinding) {
					ExecutableElement e = new ExecutableElementImpl(_env, explicitBinding);
					AnnotationValue v = new AnnotationValueImpl(_env, pairs[i].getValue(), explicitBinding.returnType);
					valueMap.put(e, v);
					foundExplicitValue = true;
					break;
				}
			}
			// else get default value if one exists
			if (!foundExplicitValue) {
				Object defaultVal = method.getDefaultValue();
				if (null != defaultVal) {
					ExecutableElement e = new ExecutableElementImpl(_env, method);
					AnnotationValue v = new AnnotationValueImpl(_env, defaultVal, method.returnType);
					valueMap.put(e, v);
				}
			}
		}
		return Collections.unmodifiableMap(valueMap);
	}

	@Override
	public boolean equals(Object obj) {
		if (obj instanceof AnnotationMirrorImpl) {
			return this._binding == ((AnnotationMirrorImpl) obj)._binding;
		}
		return false;
	}

	public int hashCode() {
		return this._binding.hashCode();
	}

	/*
	 * (non-Javadoc)
	 * Sun implementation shows the values.  We avoid that here,
	 * because getting the values is not idempotent.
	 */
	@Override
	public String toString() {
		return "@" + _binding.getAnnotationType().debugName(); //$NON-NLS-1$
	}
	
}
