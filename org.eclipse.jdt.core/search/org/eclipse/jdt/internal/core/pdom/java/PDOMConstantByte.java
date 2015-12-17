package org.eclipse.jdt.internal.core.pdom.java;

import org.eclipse.jdt.internal.core.pdom.PDOM;
import org.eclipse.jdt.internal.core.pdom.field.FieldByte;
import org.eclipse.jdt.internal.core.pdom.field.StructDef;

/**
 * @since 3.12
 */
public final class PDOMConstantByte extends PDOMConstant {
	public static final FieldByte VALUE;

	@SuppressWarnings("hiding")
	public static StructDef<PDOMConstantByte> type;

	static {
		type = StructDef.create(PDOMConstantByte.class, PDOMConstant.type);
		VALUE = type.addByte();
		type.done();
	}

	public PDOMConstantByte(PDOM pdom, long address) {
		super(pdom, address);
	}

	protected PDOMConstantByte(PDOM pdom) {
		super(pdom);
	}

	public static PDOMConstantByte create(PDOM pdom, byte value) {
		PDOMConstantByte result = new PDOMConstantByte(pdom);
		result.setValue(value);
		return result;
	}

	public void setValue(byte value) {
		VALUE.put(getPDOM(), this.address, value);
	}

	public byte getValue() {
		return VALUE.get(getPDOM(), this.address);
	}
}
