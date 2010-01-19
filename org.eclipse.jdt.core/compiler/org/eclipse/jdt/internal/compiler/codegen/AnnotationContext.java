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
	public int info;
	public int boundIndex;
	public int visibility;
	public Annotation[] primaryAnnotations;
	public LocalVariableBinding variableBinding;
	public Annotation[][] annotationsOnDimensions;

	public AnnotationContext(
			Annotation annotation,
			TypeReference typeReference,
			int targetType,
			Annotation[] primaryAnnotations,
			int visibility,
			Annotation[][] annotationsOnDimensions) {
		this.annotation = annotation;
		this.typeReference = typeReference;
		this.targetType = targetType;
		this.primaryAnnotations = primaryAnnotations;
		this.visibility = visibility;
		this.annotationsOnDimensions = annotationsOnDimensions;
	}

	public String toString() {
		return "AnnotationContext [annotation=" //$NON-NLS-1$
				+ this.annotation
				+ ", typeReference=" //$NON-NLS-1$
				+ this.typeReference
				+ ", targetType=" //$NON-NLS-1$
				+ this.targetType
				+ ", info =" //$NON-NLS-1$
				+ this.info
				+ ", boundIndex=" //$NON-NLS-1$
				+ this.boundIndex
				+ "]"; //$NON-NLS-1$
	}
}
