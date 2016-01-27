package org.eclipse.jdt.internal.core.nd.java;

import org.eclipse.jdt.internal.core.nd.Nd;
import org.eclipse.jdt.internal.core.nd.field.FieldLong;
import org.eclipse.jdt.internal.core.nd.field.StructDef;

/**
 * @since 3.12
 */
public final class NdConstantLong extends NdConstant {
	public static final FieldLong VALUE;

	@SuppressWarnings("hiding")
	public static StructDef<NdConstantLong> type;

	static {
		type = StructDef.create(NdConstantLong.class, NdConstant.type);
		VALUE = type.addLong();
		type.done();
	}

	public NdConstantLong(Nd pdom, long address) {
		super(pdom, address);
	}

	protected NdConstantLong(Nd pdom) {
		super(pdom);
	}

	public static NdConstantLong create(Nd pdom, long value) {
		NdConstantLong result = new NdConstantLong(pdom);
		result.setValue(value);
		return result;
	}

	public void setValue(long value) {
		VALUE.put(getNd(), this.address, value);
	}

	public long getValue() {
		return VALUE.get(getNd(), this.address);
	}
}
