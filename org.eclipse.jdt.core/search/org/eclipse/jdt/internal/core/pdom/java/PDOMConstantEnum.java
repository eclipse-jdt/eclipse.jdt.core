package org.eclipse.jdt.internal.core.pdom.java;

import org.eclipse.jdt.internal.core.pdom.PDOM;
import org.eclipse.jdt.internal.core.pdom.field.FieldManyToOne;
import org.eclipse.jdt.internal.core.pdom.field.FieldString;
import org.eclipse.jdt.internal.core.pdom.field.StructDef;

/**
 * @since 3.12
 */
public final class PDOMConstantEnum extends PDOMConstant {
	public static final FieldManyToOne<PDOMTypeSignature> ENUM_TYPE;
	public static final FieldString ENUM_VALUE;

	@SuppressWarnings("hiding")
	public static StructDef<PDOMConstantEnum> type;

	static {
		type = StructDef.create(PDOMConstantEnum.class, PDOMConstant.type);
		ENUM_TYPE = FieldManyToOne.create(type, PDOMTypeSignature.USED_AS_ENUM_CONSTANT);
		ENUM_VALUE = type.addString();
		type.done();
	}

	public PDOMConstantEnum(PDOM pdom, long address) {
		super(pdom, address);
	}

	protected PDOMConstantEnum(PDOM pdom) {
		super(pdom);
	}

	public static PDOMConstantEnum create(PDOMTypeSignature enumType, String enumValue) {
		PDOMConstantEnum result = new PDOMConstantEnum(enumType.getPDOM());
		result.setEnumType(enumType);
		result.setEnumValue(enumValue);
		return result;
	}

	public void setEnumType(PDOMTypeSignature enumType) {
		ENUM_TYPE.put(getPDOM(), this.address, enumType);
	}

	public void setEnumValue(String enumType) {
		ENUM_VALUE.put(getPDOM(), this.address, enumType);
	}

	public PDOMTypeSignature getValue() {
		return ENUM_TYPE.get(getPDOM(), this.address);
	}
}
