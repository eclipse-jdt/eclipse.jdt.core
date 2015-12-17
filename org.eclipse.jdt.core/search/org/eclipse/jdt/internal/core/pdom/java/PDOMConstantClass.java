package org.eclipse.jdt.internal.core.pdom.java;

import org.eclipse.jdt.internal.core.pdom.PDOM;
import org.eclipse.jdt.internal.core.pdom.field.FieldManyToOne;
import org.eclipse.jdt.internal.core.pdom.field.StructDef;

/**
 * @since 3.12
 */
public final class PDOMConstantClass extends PDOMConstant {
	public static final FieldManyToOne<PDOMTypeSignature> VALUE;

	@SuppressWarnings("hiding")
	public static StructDef<PDOMConstantClass> type;

	static {
		type = StructDef.create(PDOMConstantClass.class, PDOMConstant.type);
		VALUE = FieldManyToOne.create(type, PDOMTypeSignature.USED_AS_CONSTANT);
		type.done();
	}

	public PDOMConstantClass(PDOM pdom, long address) {
		super(pdom, address);
	}

	protected PDOMConstantClass(PDOM pdom) {
		super(pdom);
	}

	public static PDOMConstantClass create(PDOM pdom, PDOMTypeSignature value) {
		PDOMConstantClass result = new PDOMConstantClass(pdom);
		result.setValue(value);
		return result;
	}

	public void setValue(PDOMTypeSignature value) {
		VALUE.put(getPDOM(), this.address, value);
	}

	public PDOMTypeSignature getValue() {
		return VALUE.get(getPDOM(), this.address);
	}
}
