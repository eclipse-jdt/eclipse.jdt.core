/*******************************************************************************
 * Copyright (c) 2007, 2013 BEA Systems, Inc. and others
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * This is an implementation of an early-draft specification developed under the Java
 * Community Process (JCP) and is made available for testing and evaluation purposes
 * only. The code is not compatible with any specification of the JCP.
 *
 * Contributors:
 *    wharley@bea.com - initial API and implementation
 *    IBM Corporation - Java 8 support
 *******************************************************************************/

package org.eclipse.jdt.internal.compiler.apt.model;

import java.lang.annotation.Annotation;
import java.util.List;

import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.type.NoType;
import javax.lang.model.type.NullType;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeVisitor;

/**
 * An implementation of NoType, which is used to represent certain psuedo-types.
 * @see NoType
 */
public class NoTypeImpl implements NoType, NullType
{
	private final TypeKind _kind;
	
	public static final NoType NO_TYPE_NONE = new NoTypeImpl(TypeKind.NONE);
	public static final NoType NO_TYPE_VOID = new NoTypeImpl(TypeKind.VOID);
	public static final NoType NO_TYPE_PACKAGE = new NoTypeImpl(TypeKind.PACKAGE);
	public static final NullType NULL_TYPE = new NoTypeImpl(TypeKind.NULL);
	
	private NoTypeImpl(TypeKind kind) {
		_kind = kind;
	}

	@Override
	public <R, P> R accept(TypeVisitor<R, P> v, P p)
	{
		switch(this.getKind())
		{
			case NULL :
				return v.visitNull(this, p);
			default: 
				return v.visitNoType(this, p);
		}
	}

	@Override
	public TypeKind getKind()
	{
		return _kind;
	}
	
	public String toString()
	{
		switch (_kind) {
		default:
		case NONE:
			return "<none>"; //$NON-NLS-1$
		case NULL:
			return "null"; //$NON-NLS-1$
		case VOID:
			return "void"; //$NON-NLS-1$
		case PACKAGE:
			return "package"; //$NON-NLS-1$
		}
	}

	public List<? extends AnnotationMirror> getAnnotationMirrors() {
		return Factory.EMPTY_ANNOTATION_MIRRORS;
	}

	public <A extends Annotation> A getAnnotation(Class<A> annotationType) {
		return null;
	}

	@SuppressWarnings("unchecked")
	public <A extends Annotation> A[] getAnnotationsByType(Class<A> annotationType) {
		return (A[]) Factory.EMPTY_ANNOTATIONS;
	}

}
