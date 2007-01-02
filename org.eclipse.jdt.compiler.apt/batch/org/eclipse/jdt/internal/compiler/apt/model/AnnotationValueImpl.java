package org.eclipse.jdt.internal.compiler.apt.model;

import javax.lang.model.element.AnnotationValue;
import javax.lang.model.element.AnnotationValueVisitor;

import org.eclipse.jdt.internal.compiler.lookup.ElementValuePair;

public class AnnotationValueImpl implements AnnotationValue {

	/**
	 * @param value See {@link ElementValuePair#getValue()} for possible
	 * object types.
	 */
	public AnnotationValueImpl(Object value) {
		// TODO Auto-generated constructor stub
	}

	@Override
	public <R, P> R accept(AnnotationValueVisitor<R, P> v, P p) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Object getValue() {
		// TODO Auto-generated method stub
		return null;
	}

}
