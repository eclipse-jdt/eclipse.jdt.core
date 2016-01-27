package org.eclipse.jdt.internal.core.nd.java;

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

	public NdConstantInt(Nd pdom, long address) {
		super(pdom, address);
	}

	protected NdConstantInt(Nd pdom) {
		super(pdom);
	}

	public static NdConstantInt create(Nd pdom, int value) {
		NdConstantInt result = new NdConstantInt(pdom);
		result.setValue(value);
		return result;
	}

	public void setValue(int value) {
		VALUE.put(getPDOM(), this.address, value);
	}

	public int getValue() {
		return VALUE.get(getPDOM(), this.address);
	}
}
