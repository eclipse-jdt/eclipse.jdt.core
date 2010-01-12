package org.eclipse.jdt.internal.compiler.codegen;

import org.eclipse.jdt.internal.compiler.ast.Annotation;
import org.eclipse.jdt.internal.compiler.ast.TypeReference;

public class AnnotationContext {
	public Annotation annotation;
	public TypeReference typeReference;
	public int targetType;
	public int typeIndex;
	public int offset;
	public int boundIndex;
	public int paramIndex;

	public AnnotationContext(Annotation annotation, TypeReference typeReference, int targetType) {
		this.annotation = annotation;
		this.typeReference = typeReference;
		this.targetType = targetType;
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
