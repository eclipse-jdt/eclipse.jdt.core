package org.eclipse.jdt.internal.core.pdom.java;

import org.eclipse.jdt.internal.core.pdom.PDOM;
import org.eclipse.jdt.internal.core.pdom.field.FieldShort;
import org.eclipse.jdt.internal.core.pdom.field.StructDef;

/**
 * @since 3.12
 */
public final class PDOMConstantShort extends PDOMConstant {
	public static final FieldShort VALUE;

	@SuppressWarnings("hiding")
	public static StructDef<PDOMConstantShort> type;

	static {
		type = StructDef.create(PDOMConstantShort.class, PDOMConstant.type);
		VALUE = type.addShort();
		type.done();
	}

	public PDOMConstantShort(PDOM pdom, long address) {
		super(pdom, address);
	}

	protected PDOMConstantShort(PDOM pdom) {
		super(pdom);
	}

	public static PDOMConstantShort create(PDOM pdom, short value) {
		PDOMConstantShort result = new PDOMConstantShort(pdom);
		result.setValue(value);
		return result;
	}

	public void setValue(short value) {
		VALUE.put(getPDOM(), this.address, value);
	}

	public short getValue() {
		return VALUE.get(getPDOM(), this.address);
	}
}
