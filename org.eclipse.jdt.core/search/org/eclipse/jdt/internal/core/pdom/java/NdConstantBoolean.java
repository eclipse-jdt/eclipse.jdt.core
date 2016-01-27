package org.eclipse.jdt.internal.core.pdom.java;

import org.eclipse.jdt.internal.core.pdom.Nd;
import org.eclipse.jdt.internal.core.pdom.field.FieldByte;
import org.eclipse.jdt.internal.core.pdom.field.StructDef;

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
		VALUE.put(getPDOM(), this.address, value ? (byte)1 : (byte)0);
	}

	public boolean getValue() {
		return VALUE.get(getPDOM(), this.address) != 0;
	}
}
