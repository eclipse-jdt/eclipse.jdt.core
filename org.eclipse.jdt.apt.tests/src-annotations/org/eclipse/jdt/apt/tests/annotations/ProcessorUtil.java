/*******************************************************************************
 * Copyright (c) 2014 BEA Systems, Inc.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *    wharley@bea.com - initial API and implementation
 *    het@google.com - Bug 441790
 *******************************************************************************/

package org.eclipse.jdt.apt.tests.annotations;

import java.util.Collection;
import java.util.Map;

import com.sun.mirror.declaration.AnnotationMirror;
import com.sun.mirror.declaration.AnnotationTypeDeclaration;
import com.sun.mirror.declaration.AnnotationTypeElementDeclaration;
import com.sun.mirror.declaration.AnnotationValue;
import com.sun.mirror.declaration.EnumConstantDeclaration;
import com.sun.mirror.type.AnnotationType;
import com.sun.mirror.type.TypeMirror;

/**
 * Utilities for use by APT test processors
 * @since 3.4
 */
public final class ProcessorUtil
{
	/**
	 * Represent an annotation mirror and its explicit values as a String.  Use this,
	 * rather than toString(), in order to have reliable and stable results.
	 */
	public static String annoMirrorToString(AnnotationMirror mirror) {
		AnnotationType type = mirror.getAnnotationType();
		if (type == null) {
			return "[Annotation of unknown (null) type]";
		}
		AnnotationTypeDeclaration decl = type.getDeclaration();
		if (decl == null) {
			return "[Annotation with null type declaration]";
		}
		StringBuilder sb = new StringBuilder();
		sb.append('@');
		sb.append(decl.getSimpleName());
		sb.append('(');
		Map<AnnotationTypeElementDeclaration, AnnotationValue> values = mirror.getElementValues();
		boolean first = true;
		for (Map.Entry<AnnotationTypeElementDeclaration, AnnotationValue> entry : values.entrySet()) {
			if (!first) {
				sb.append(", ");
			}
			first = false;
			sb.append(annoValuePairToString(entry.getKey(), entry.getValue()));
		}
		sb.append(')');
		return sb.toString();
	}

	public static String annoValuePairToString(AnnotationTypeElementDeclaration key, AnnotationValue value)
	{
		if (key == null) {
			return "[Null annotation value declaration]";
		}
		StringBuilder sb = new StringBuilder();
		sb.append(key.getSimpleName());
		sb.append(" = ");
		sb.append(annoValueToString(value));
		return sb.toString();
	}

	private static String annoValueToString(AnnotationValue value) {
		if (value == null) {
			return "null";
		}
		Object v = value.getValue();
		if (v == null) {
			return "null";
		}
		if (v instanceof EnumConstantDeclaration) {
			return ((EnumConstantDeclaration)v).getSimpleName();
		}
		else if (v instanceof TypeMirror) {
			// TODO: clearly we also need a typeMirrorToString utility function
			return ((TypeMirror)v).toString();
		}
		else if (v instanceof AnnotationMirror) {
			return annoMirrorToString((AnnotationMirror)v);
		}
		else if (v instanceof Collection<?>) {
			// Collection<AnnotationValue>, for an array value
			StringBuilder sb = new StringBuilder();
			sb.append('{');
			boolean first = true;
			for (Object item : (Collection<?>)v) {
				if (!first) {
					sb.append(", ");
				}
				first = false;
				sb.append(annoValueToString((AnnotationValue)item));
			}
			sb.append('}');
			return sb.toString();
		}
		// boxed primitive or String
		return v.toString();
	}
}
