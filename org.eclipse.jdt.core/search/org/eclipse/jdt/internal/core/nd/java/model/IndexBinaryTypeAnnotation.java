package org.eclipse.jdt.internal.core.nd.java.model;

import org.eclipse.jdt.internal.compiler.env.IBinaryAnnotation;
import org.eclipse.jdt.internal.compiler.env.IBinaryTypeAnnotation;

public class IndexBinaryTypeAnnotation implements IBinaryTypeAnnotation {
	private int targetType;

	// info is used in different ways:
	// TargetType 0x00: CLASS_TYPE_PARAMETER: type parameter index
	// TargetType 0x01: METHOD_TYPE_PARAMETER: type parameter index
	// TargetType 0x10: CLASS_EXTENDS: supertype index (-1 = superclass, 0..N superinterface)
	// TargetType 0x11: CLASS_TYPE_PARAMETER_BOUND: type parameter index
	// TargetType 0x12: METHOD_TYPE_PARAMETER_BOUND: type parameter index
	// TargetType 0x16: METHOD_FORMAL_PARAMETER: method formal parameter index
	// TargetType 0x17: THROWS: throws type index
	private int info;

	// TargetType 0x11: CLASS_TYPE_PARAMETER_BOUND: bound index
	// TargetType 0x12: METHOD_TYPE_PARAMETER_BOUND: bound index
	private int info2;


	private int[] typePath;
	private IBinaryAnnotation annotation;

	public IndexBinaryTypeAnnotation(int targetType, int info, int info2, int[] typePath, IBinaryAnnotation annotation) {
		this.targetType = targetType;
		this.info = info;
		this.info2 = info2;
		this.typePath = typePath;
		this.annotation = annotation;
	}

	@Override
	public IBinaryAnnotation getAnnotation() {
		return this.annotation;
	}

	@Override
	public int getTargetType() {
		return this.targetType;
	}

	@Override
	public int[] getTypePath() {
		return this.typePath;
	}

	@Override
	public int getSupertypeIndex() {
		return this.info;
	}

	@Override
	public int getTypeParameterIndex() {
		return this.info;
}

	@Override
	public int getBoundIndex() {
		return this.info2;
	}

	@Override
	public int getMethodFormalParameterIndex() {
		return this.info;
	}

	@Override
	public int getThrowsTypeIndex() {
		return this.info;
	}

}
