package org.eclipse.jdt.internal.core.nd.java;

import org.eclipse.jdt.internal.core.nd.Nd;
import org.eclipse.jdt.internal.core.nd.field.FieldChar;
import org.eclipse.jdt.internal.core.nd.field.StructDef;

/**
 * @since 3.12
 */
public final class NdConstantChar extends NdConstant {
	public static final FieldChar VALUE;

	@SuppressWarnings("hiding")
	public static StructDef<NdConstantChar> type;

	static {
		type = StructDef.create(NdConstantChar.class, NdConstant.type);
		VALUE = type.addChar();
		type.done();
	}

	public NdConstantChar(Nd pdom, long address) {
		super(pdom, address);
	}

	protected NdConstantChar(Nd pdom) {
		super(pdom);
	}

	public static NdConstantChar create(Nd pdom, char value) {
		NdConstantChar result = new NdConstantChar(pdom);
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
