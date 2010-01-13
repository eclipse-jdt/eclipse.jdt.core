/*******************************************************************************
 * Copyright (c) 2010 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jdt.internal.compiler.codegen;

import org.eclipse.jdt.internal.compiler.ast.Annotation;
import org.eclipse.jdt.internal.compiler.ast.TypeReference;
import org.eclipse.jdt.internal.compiler.lookup.LocalVariableBinding;

public class AnnotationContext {
	public static final int VISIBLE = 0x1;
	public static final int INVISIBLE = 0x2;
	public Annotation annotation;
	public TypeReference typeReference;
	public int targetType;
	public int typeIndex;
	public int offset;
	public int boundIndex;
	public int paramIndex;
	public int visibility;
	public Annotation[] primaryAnnotations;
	public LocalVariableBinding variableBinding;

	public AnnotationContext(
			Annotation annotation,
			TypeReference typeReference,
			int targetType,
			Annotation[] primaryAnnotations) {
		this.annotation = annotation;
		this.typeReference = typeReference;
		this.targetType = targetType;
		this.primaryAnnotations = primaryAnnotations;
	}

	public String toString() {
		return "AnnotationContext [annotation=" //$NON-NLS-1$
				+ this.annotation
				+ ", typeReference=" //$NON-NLS-1$
				+ this.typeReference
				+ ", targetType=" //$NON-NLS-1$
				+ this.targetType
				+ ", typeIndex=" //$NON-NLS-1$
				+ this.typeIndex
				+ ", offset=" //$NON-NLS-1$
				+ this.offset
				+ ", boundIndex=" //$NON-NLS-1$
				+ this.boundIndex + ", paramIndex=" + this.paramIndex + "]"; //$NON-NLS-1$ //$NON-NLS-2$
	}
}
