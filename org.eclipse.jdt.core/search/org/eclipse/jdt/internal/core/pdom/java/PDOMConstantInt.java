package org.eclipse.jdt.internal.core.pdom.java;

import org.eclipse.jdt.internal.core.pdom.PDOM;
import org.eclipse.jdt.internal.core.pdom.field.FieldInt;
import org.eclipse.jdt.internal.core.pdom.field.StructDef;

public final class PDOMConstantInt extends PDOMConstant {
	public static final FieldInt VALUE;

	@SuppressWarnings("hiding")
	public static StructDef<PDOMConstantInt> type;

	static {
		type = StructDef.create(PDOMConstantInt.class, PDOMConstant.type);
		VALUE = type.addInt();
		type.done();
	}

	public PDOMConstantInt(PDOM pdom, long address) {
		super(pdom, address);
	}

	protected PDOMConstantInt(PDOM pdom) {
		super(pdom);
	}

	public static PDOMConstantInt create(PDOM pdom, int value) {
		PDOMConstantInt result = new PDOMConstantInt(pdom);
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
