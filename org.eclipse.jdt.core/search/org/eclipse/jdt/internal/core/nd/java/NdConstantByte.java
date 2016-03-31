package org.eclipse.jdt.internal.core.nd.java;

import org.eclipse.jdt.internal.compiler.impl.ByteConstant;
import org.eclipse.jdt.internal.compiler.impl.Constant;
import org.eclipse.jdt.internal.core.nd.Nd;
import org.eclipse.jdt.internal.core.nd.field.FieldByte;
import org.eclipse.jdt.internal.core.nd.field.StructDef;

/**
 * @since 3.12
 */
public final class NdConstantByte extends NdConstant {
	public static final FieldByte VALUE;

	@SuppressWarnings("hiding")
	public static StructDef<NdConstantByte> type;

	static {
		type = StructDef.create(NdConstantByte.class, NdConstant.type);
		VALUE = type.addByte();
		type.done();
	}

	public NdConstantByte(Nd nd, long address) {
		super(nd, address);
	}

	protected NdConstantByte(Nd nd) {
		super(nd);
	}

	public static NdConstantByte create(Nd nd, byte value) {
		NdConstantByte result = new NdConstantByte(nd);
		result.setValue(value);
		return result;
	}

	public void setValue(byte value) {
		VALUE.put(getNd(), this.address, value);
	}

	public byte getValue() {
		return VALUE.get(getNd(), this.address);
	}

	@Override
	public Constant getConstant() {
		return ByteConstant.fromValue(getValue());
	}
}
