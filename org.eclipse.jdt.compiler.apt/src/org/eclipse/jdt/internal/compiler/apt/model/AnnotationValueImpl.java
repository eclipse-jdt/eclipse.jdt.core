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
import javax.lang.model.element.AnnotationValueVisitor;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.TypeMirror;

import org.eclipse.jdt.internal.compiler.impl.BooleanConstant;
import org.eclipse.jdt.internal.compiler.impl.ByteConstant;
import org.eclipse.jdt.internal.compiler.impl.CharConstant;
import org.eclipse.jdt.internal.compiler.impl.Constant;
import org.eclipse.jdt.internal.compiler.impl.DoubleConstant;
import org.eclipse.jdt.internal.compiler.impl.FloatConstant;
import org.eclipse.jdt.internal.compiler.impl.IntConstant;
import org.eclipse.jdt.internal.compiler.impl.LongConstant;
import org.eclipse.jdt.internal.compiler.impl.ShortConstant;
import org.eclipse.jdt.internal.compiler.impl.StringConstant;
import org.eclipse.jdt.internal.compiler.lookup.AnnotationBinding;
import org.eclipse.jdt.internal.compiler.lookup.ElementValuePair;
import org.eclipse.jdt.internal.compiler.lookup.FieldBinding;
import org.eclipse.jdt.internal.compiler.lookup.TypeBinding;
import org.eclipse.jdt.internal.compiler.lookup.TypeIds;

public class AnnotationValueImpl implements AnnotationValue, TypeIds {
	
	/*
	 * Additions to T_* constants in TypeIds. 
	 */
	private static final int T_AnnotationMirror = -1;
	private static final int T_EnumConstant = -2;
	private static final int T_ClassObject = -3;
	private static final int T_ArrayType = -4;
	
	/**
	 * The annotation value, as it would be returned by
	 * {@link #getValue()}.  For instance, an Integer (for an int
	 * constant), a VariableElement (for an enum constant), or
	 * a List<AnnotationValueImpl> containing multiple such (for an array type).  
	 */
	private final Object _value;
	
	/**
	 * The type stored in _value, represented as a T_* value from {@link TypeIds}
	 * or one of the additional T_* values defined in this class.
	 */
	private final int _kind;

	/**
	 * @param value
	 *            The JDT representation of a compile-time constant. See
	 *            {@link ElementValuePair#getValue()} for possible object types:
	 *            <ul>
	 *            <li>{@link org.eclipse.jdt.internal.compiler.impl.Constant} for member
	 *            of primitive type or String</li>
	 *            <li>{@link TypeBinding} for a member value of type
	 *            {@link java.lang.Class}</li>
	 *            <li>{@link FieldBinding} for an enum constant</li>
	 *            <li>{@link AnnotationBinding} for an annotation instance</li>
	 *            <li><code>Object[]</code> for a member value of array type, where the
	 *            array entries are one of the above</li>
	 *            </ul>
	 */
	public AnnotationValueImpl(Object value) {
		int kind[] = new int[1];
		if (value instanceof Object[]) {
			Object[] values = (Object[])value;
			List<AnnotationValue> convertedValues = new ArrayList<AnnotationValue>(values.length);
			for (Object oneValue : values) {
				convertedValues.add(new AnnotationValueImpl(oneValue));
			}
			_value = Collections.unmodifiableList(convertedValues);
			_kind = T_ArrayType;
		}
		else {
			_value = convertToJavaType(value, kind);
			_kind = kind[0];
		}
	}
	
	/**
	 * Convert the JDT representation of a single constant into its javax.lang.model
	 * representation.  For instance, convert a StringConstant into a String, or
	 * a FieldBinding into a VariableElement.  This does not handle the case where
	 * value is an Object[].
	 * @param value the JDT object
	 * @param kind an int array whose first element will be set to the type of the
	 * converted object, represented with T_* values from TypeIds or from this class.
	 * @return
	 */
	private Object convertToJavaType(Object value, int kind[]) {
		if (value instanceof Constant) {
			kind[0] = ((Constant) value).typeID();
			switch (kind[0]) {
			case T_boolean:
				return ((BooleanConstant) value).booleanValue();
			case T_byte:
				return ((ByteConstant) value).byteValue();
			case T_char:
				return ((CharConstant) value).charValue();
			case T_double:
				return ((DoubleConstant) value).doubleValue();
			case T_float:
				return ((FloatConstant) value).floatValue();
			case T_int:
				return ((IntConstant) value).intValue();
			case T_JavaLangString:
				return ((StringConstant) value).stringValue();
			case T_long:
				return ((LongConstant) value).longValue();
			case T_short:
				return ((ShortConstant) value).shortValue();
			}
		} else if (value instanceof FieldBinding) {
			kind[0] = T_EnumConstant;
			return (VariableElement) Factory.newElement((FieldBinding) value);
		} else if (value instanceof TypeBinding) {
			kind[0] = T_ClassObject;
			return Factory.newTypeMirror((TypeBinding) value);
		} else if (value instanceof AnnotationBinding) {
			kind[0] = T_AnnotationMirror;
			return Factory.newAnnotationMirror((AnnotationBinding) value);
		} 
		throw new IllegalArgumentException("Unexpected type for annotation value: " + value); //$NON-NLS-1$
	}

	@SuppressWarnings("unchecked") // Need to cast Object _value to a List<AnnotationValue>
	@Override
	public <R, P> R accept(AnnotationValueVisitor<R, P> v, P p) {
		switch (_kind) {
		case TypeIds.T_boolean:
			return v.visitBoolean((Boolean)_value, p);
		case TypeIds.T_byte:
			return v.visitByte((Byte)_value, p);
		case TypeIds.T_char:
			return v.visitChar((Character)_value, p);
		case TypeIds.T_double:
			return v.visitDouble((Double)_value, p);
		case TypeIds.T_float:
			return v.visitFloat((Float)_value, p);
		case TypeIds.T_int:
			return v.visitInt((Integer)_value, p);
		case TypeIds.T_JavaLangString:
			return v.visitString((String)_value, p);
		case TypeIds.T_long:
			return v.visitLong((Long)_value, p);
		case TypeIds.T_short:
			return v.visitShort((Short)_value, p);
		case T_EnumConstant:
			return v.visitEnumConstant((VariableElement)_value, p);
		case T_ClassObject:
			return v.visitType((TypeMirror)_value, p);
		case T_AnnotationMirror:
			return v.visitAnnotation((AnnotationMirror)_value, p);
		case T_ArrayType:
			return v.visitArray((List<AnnotationValue>)_value, p);
		default:
			return null;
		}
	}

	@Override
	public Object getValue() {
		return _value;
	}

	@Override
	public String toString() {
		if (null == _value) {
			return "null"; //$NON-NLS-1$
		}
		return _value.toString();
	}
}
