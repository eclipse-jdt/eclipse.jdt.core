package org.eclipse.jdt.internal.core.nd.java;

import org.eclipse.jdt.internal.core.nd.Nd;
import org.eclipse.jdt.internal.core.nd.field.FieldManyToOne;
import org.eclipse.jdt.internal.core.nd.field.FieldString;
import org.eclipse.jdt.internal.core.nd.field.StructDef;

/**
 * @since 3.12
 */
public final class NdConstantEnum extends NdConstant {
	public static final FieldManyToOne<NdTypeSignature> ENUM_TYPE;
	public static final FieldString ENUM_VALUE;

	@SuppressWarnings("hiding")
	public static StructDef<NdConstantEnum> type;

	static {
		type = StructDef.create(NdConstantEnum.class, NdConstant.type);
		ENUM_TYPE = FieldManyToOne.create(type, NdTypeSignature.USED_AS_ENUM_CONSTANT);
		ENUM_VALUE = type.addString();
		type.done();
	}

	public NdConstantEnum(Nd pdom, long address) {
		super(pdom, address);
	}

	protected NdConstantEnum(Nd pdom) {
		super(pdom);
	}

	public static NdConstantEnum create(NdTypeSignature enumType, String enumValue) {
		NdConstantEnum result = new NdConstantEnum(enumType.getPDOM());
		result.setEnumType(enumType);
		result.setEnumValue(enumValue);
		return result;
	}

	public void setEnumType(NdTypeSignature enumType) {
		ENUM_TYPE.put(getPDOM(), this.address, enumType);
	}

	public void setEnumValue(String enumType) {
		ENUM_VALUE.put(getPDOM(), this.address, enumType);
	}

	public NdTypeSignature getValue() {
		return ENUM_TYPE.get(getPDOM(), this.address);
	}
}
