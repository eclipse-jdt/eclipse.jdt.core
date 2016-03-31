package org.eclipse.jdt.internal.core.nd.java;

import org.eclipse.jdt.internal.compiler.impl.Constant;
import org.eclipse.jdt.internal.compiler.impl.IntConstant;
import org.eclipse.jdt.internal.core.nd.Nd;
import org.eclipse.jdt.internal.core.nd.field.FieldInt;
import org.eclipse.jdt.internal.core.nd.field.StructDef;

/**
 * @since 3.12
 */
public final class NdConstantInt extends NdConstant {
	public static final FieldInt VALUE;

	@SuppressWarnings("hiding")
	public static StructDef<NdConstantInt> type;

	static {
		type = StructDef.create(NdConstantInt.class, NdConstant.type);
		VALUE = type.addInt();
		type.done();
	}

	public NdConstantInt(Nd nd, long address) {
		super(nd, address);
	}

	protected NdConstantInt(Nd nd) {
		super(nd);
	}

	public static NdConstantInt create(Nd nd, int value) {
		NdConstantInt result = new NdConstantInt(nd);
		result.setValue(value);
		return result;
	}

	public void setValue(int value) {
		VALUE.put(getNd(), this.address, value);
	}

	public int getValue() {
		return VALUE.get(getNd(), this.address);
	}

	@Override
	public Constant getConstant() {
		return IntConstant.fromValue(getValue());
	}
}
