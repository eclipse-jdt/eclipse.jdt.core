package org.eclipse.jdt.internal.core.pdom.java;

import org.eclipse.jdt.internal.core.pdom.Nd;
import org.eclipse.jdt.internal.core.pdom.field.FieldFloat;
import org.eclipse.jdt.internal.core.pdom.field.StructDef;

/**
 * @since 3.12
 */
public final class NdConstantFloat extends NdConstant {
	public static final FieldFloat VALUE;

	@SuppressWarnings("hiding")
	public static StructDef<NdConstantFloat> type;

	static {
		type = StructDef.create(NdConstantFloat.class, NdConstant.type);
		VALUE = type.addFloat();
		type.done();
	}

	public NdConstantFloat(Nd pdom, long address) {
		super(pdom, address);
	}

	protected NdConstantFloat(Nd pdom) {
		super(pdom);
	}

	public static NdConstantFloat create(Nd pdom, float value) {
		NdConstantFloat result = new NdConstantFloat(pdom);
		result.setValue(value);
		return result;
	}

	public void setValue(float value) {
		VALUE.put(getPDOM(), this.address, value);
	}

	public float getValue() {
		return VALUE.get(getPDOM(), this.address);
	}
}
