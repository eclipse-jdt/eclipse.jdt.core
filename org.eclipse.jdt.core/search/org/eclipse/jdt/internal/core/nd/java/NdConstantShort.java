package org.eclipse.jdt.internal.core.nd.java;

import org.eclipse.jdt.internal.core.nd.Nd;
import org.eclipse.jdt.internal.core.nd.field.FieldShort;
import org.eclipse.jdt.internal.core.nd.field.StructDef;

/**
 * @since 3.12
 */
public final class NdConstantShort extends NdConstant {
	public static final FieldShort VALUE;

	@SuppressWarnings("hiding")
	public static StructDef<NdConstantShort> type;

	static {
		type = StructDef.create(NdConstantShort.class, NdConstant.type);
		VALUE = type.addShort();
		type.done();
	}

	public NdConstantShort(Nd pdom, long address) {
		super(pdom, address);
	}

	protected NdConstantShort(Nd pdom) {
		super(pdom);
	}

	public static NdConstantShort create(Nd pdom, short value) {
		NdConstantShort result = new NdConstantShort(pdom);
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
