package org.eclipse.jdt.internal.core.nd.java;

import org.eclipse.jdt.internal.core.nd.Nd;
import org.eclipse.jdt.internal.core.nd.field.FieldDouble;
import org.eclipse.jdt.internal.core.nd.field.StructDef;

/**
 * @since 3.12
 */
public final class NdConstantDouble extends NdConstant {
	public static final FieldDouble VALUE;

	@SuppressWarnings("hiding")
	public static StructDef<NdConstantDouble> type;

	static {
		type = StructDef.create(NdConstantDouble.class, NdConstant.type);
		VALUE = type.addDouble();
		type.done();
	}

	public NdConstantDouble(Nd pdom, long address) {
		super(pdom, address);
	}

	protected NdConstantDouble(Nd pdom) {
		super(pdom);
	}

	public static NdConstantDouble create(Nd pdom, double value) {
		NdConstantDouble result = new NdConstantDouble(pdom);
		result.setValue(value);
		return result;
	}

	public void setValue(double value) {
		VALUE.put(getNd(), this.address, value);
	}

	public double getValue() {
		return VALUE.get(getNd(), this.address);
	}
}
