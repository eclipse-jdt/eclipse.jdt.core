package org.eclipse.jdt.internal.core.pdom.java;

import org.eclipse.jdt.internal.core.pdom.PDOM;
import org.eclipse.jdt.internal.core.pdom.field.FieldDouble;
import org.eclipse.jdt.internal.core.pdom.field.StructDef;

/**
 * @since 3.12
 */
public final class PDOMConstantDouble extends PDOMConstant {
	public static final FieldDouble VALUE;

	@SuppressWarnings("hiding")
	public static StructDef<PDOMConstantDouble> type;

	static {
		type = StructDef.create(PDOMConstantDouble.class, PDOMConstant.type);
		VALUE = type.addDouble();
		type.done();
	}

	public PDOMConstantDouble(PDOM pdom, long address) {
		super(pdom, address);
	}

	protected PDOMConstantDouble(PDOM pdom) {
		super(pdom);
	}

	public static PDOMConstantDouble create(PDOM pdom, double value) {
		PDOMConstantDouble result = new PDOMConstantDouble(pdom);
		result.setValue(value);
		return result;
	}

	public void setValue(double value) {
		VALUE.put(getPDOM(), this.address, value);
	}

	public double getValue() {
		return VALUE.get(getPDOM(), this.address);
	}
}
