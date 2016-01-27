package org.eclipse.jdt.internal.core.nd.java;

import org.eclipse.jdt.internal.core.nd.Nd;
import org.eclipse.jdt.internal.core.nd.field.FieldByte;
import org.eclipse.jdt.internal.core.nd.field.StructDef;

/**
 * @since 3.12
 */
public final class NdConstantBoolean extends NdConstant {
	public static final FieldByte VALUE;

	@SuppressWarnings("hiding")
	public static StructDef<NdConstantBoolean> type;

	static {
		type = StructDef.create(NdConstantBoolean.class, NdConstant.type);
		VALUE = type.addByte();
		type.done();
	}

	public NdConstantBoolean(Nd pdom, long address) {
		super(pdom, address);
	}

	protected NdConstantBoolean(Nd pdom) {
		super(pdom);
	}

	public static NdConstantBoolean create(Nd pdom, boolean value) {
		NdConstantBoolean result = new NdConstantBoolean(pdom);
		result.setValue(value);
		return result;
	}

	public void setValue(boolean value) {
		VALUE.put(getNd(), this.address, value ? (byte)1 : (byte)0);
	}

	public boolean getValue() {
		return VALUE.get(getNd(), this.address) != 0;
	}
}
