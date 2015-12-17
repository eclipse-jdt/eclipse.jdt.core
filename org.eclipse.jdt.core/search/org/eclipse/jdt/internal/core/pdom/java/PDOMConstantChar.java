package org.eclipse.jdt.internal.core.pdom.java;

import org.eclipse.jdt.internal.core.pdom.PDOM;
import org.eclipse.jdt.internal.core.pdom.field.FieldChar;
import org.eclipse.jdt.internal.core.pdom.field.StructDef;

/**
 * @since 3.12
 */
public final class PDOMConstantChar extends PDOMConstant {
	public static final FieldChar VALUE;

	@SuppressWarnings("hiding")
	public static StructDef<PDOMConstantChar> type;

	static {
		type = StructDef.create(PDOMConstantChar.class, PDOMConstant.type);
		VALUE = type.addChar();
		type.done();
	}

	public PDOMConstantChar(PDOM pdom, long address) {
		super(pdom, address);
	}

	protected PDOMConstantChar(PDOM pdom) {
		super(pdom);
	}

	public static PDOMConstantChar create(PDOM pdom, char value) {
		PDOMConstantChar result = new PDOMConstantChar(pdom);
		result.setValue(value);
		return result;
	}

	public void setValue(char value) {
		VALUE.put(getPDOM(), this.address, value);
	}

	public char getValue() {
		return VALUE.get(getPDOM(), this.address);
	}
}
