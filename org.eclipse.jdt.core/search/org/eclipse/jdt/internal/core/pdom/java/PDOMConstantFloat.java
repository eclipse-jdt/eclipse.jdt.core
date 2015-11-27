package org.eclipse.jdt.internal.core.pdom.java;

import org.eclipse.jdt.internal.core.pdom.PDOM;
import org.eclipse.jdt.internal.core.pdom.field.FieldFloat;
import org.eclipse.jdt.internal.core.pdom.field.StructDef;

public final class PDOMConstantFloat extends PDOMConstant {
	public static final FieldFloat VALUE;

	@SuppressWarnings("hiding")
	public static StructDef<PDOMConstantFloat> type;

	static {
		type = StructDef.create(PDOMConstantFloat.class, PDOMConstant.type);
		VALUE = type.addFloat();
		type.done();
	}

	public PDOMConstantFloat(PDOM pdom, long address) {
		super(pdom, address);
	}

	protected PDOMConstantFloat(PDOM pdom) {
		super(pdom);
	}

	public static PDOMConstantFloat create(PDOM pdom, float value) {
		PDOMConstantFloat result = new PDOMConstantFloat(pdom);
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
