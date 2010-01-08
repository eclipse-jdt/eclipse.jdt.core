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
		return "AnnotationContext [annotation="
				+ annotation
				+ ", typeReference="
				+ typeReference
				+ ", targetType="
				+ targetType
				+ ", typeIndex="
				+ typeIndex
				+ ", offset="
				+ offset
				+ ", boundIndex="
				+ boundIndex + ", paramIndex=" + paramIndex + "]";
	}

	private String arrayToString(Object array, int len) {
		StringBuffer buffer = new StringBuffer();
		buffer.append("[");
		for (int i = 0; i < len; i++) {
			if (i > 0)
				buffer.append(", ");
			if (array instanceof int[])
				buffer.append(((int[]) array)[i]);
		}
		buffer.append("]");
		return buffer.toString();
	}
}
