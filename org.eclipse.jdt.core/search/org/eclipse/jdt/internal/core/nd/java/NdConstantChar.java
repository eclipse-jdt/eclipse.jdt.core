package org.eclipse.jdt.internal.core.nd.java;

import org.eclipse.jdt.internal.compiler.impl.CharConstant;
import org.eclipse.jdt.internal.compiler.impl.Constant;
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

	public NdConstantChar(Nd nd, long address) {
		super(nd, address);
	}

	protected NdConstantChar(Nd nd) {
		super(nd);
	}

	public static NdConstantChar create(Nd nd, char value) {
		NdConstantChar result = new NdConstantChar(nd);
		result.setValue(value);
		return result;
	}

	public void setValue(char value) {
		VALUE.put(getNd(), this.address, value);
	}

	public char getValue() {
		return VALUE.get(getNd(), this.address);
	}

	@Override
	public Constant getConstant() {
		return CharConstant.fromValue(getValue());
	}
}
