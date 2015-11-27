package org.eclipse.jdt.internal.core.pdom.java;

import org.eclipse.jdt.internal.core.pdom.PDOM;
import org.eclipse.jdt.internal.core.pdom.field.FieldByte;
import org.eclipse.jdt.internal.core.pdom.field.StructDef;

public final class PDOMConstantBoolean extends PDOMConstant {
	public static final FieldByte VALUE;

	@SuppressWarnings("hiding")
	public static StructDef<PDOMConstantBoolean> type;

	static {
		type = StructDef.create(PDOMConstantBoolean.class, PDOMConstant.type);
		VALUE = type.addByte();
		type.done();
	}

	public PDOMConstantBoolean(PDOM pdom, long address) {
		super(pdom, address);
	}

	protected PDOMConstantBoolean(PDOM pdom) {
		super(pdom);
	}

	public static PDOMConstantBoolean create(PDOM pdom, boolean value) {
		PDOMConstantBoolean result = new PDOMConstantBoolean(pdom);
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
