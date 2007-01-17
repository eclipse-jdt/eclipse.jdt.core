package org.eclipse.jdt.internal.compiler.apt.model;

import javax.lang.model.element.AnnotationValue;
import javax.lang.model.element.AnnotationValueVisitor;

import org.eclipse.jdt.internal.compiler.impl.Constant;
import org.eclipse.jdt.internal.compiler.impl.StringConstant;
import org.eclipse.jdt.internal.compiler.lookup.ElementValuePair;

public class AnnotationValueImpl implements AnnotationValue {
	
	private final Object _value;

	/**
	 * @param value See {@link ElementValuePair#getValue()} for possible
	 * object types.
	 */
	public AnnotationValueImpl(Object value) {
		_value = value;
		//TODO: determine object type
	}

	@Override
	public <R, P> R accept(AnnotationValueVisitor<R, P> v, P p) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Object getValue() {
		// TODO: use object type determined in c'tor
		if (_value instanceof Constant) {
			if (_value instanceof StringConstant) {
				return ((StringConstant)_value).stringValue();
			}
		}
		return null;
	}

}
