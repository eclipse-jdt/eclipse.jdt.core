package org.eclipse.jdt.internal.core.nd.java;

import org.eclipse.jdt.internal.core.nd.Nd;
import org.eclipse.jdt.internal.core.nd.db.IString;
import org.eclipse.jdt.internal.core.nd.field.FieldString;
import org.eclipse.jdt.internal.core.nd.field.StructDef;

/**
 * @since 3.12
 */
public final class NdConstantString extends NdConstant {
	public static final FieldString VALUE;

	@SuppressWarnings("hiding")
	public static StructDef<NdConstantString> type;

	static {
		type = StructDef.create(NdConstantString.class, NdConstant.type);
		VALUE = type.addString();
		type.done();
	}

	public NdConstantString(Nd pdom, long address) {
		super(pdom, address);
	}

	protected NdConstantString(Nd pdom) {
		super(pdom);
	}

	public static NdConstantString create(Nd pdom, String value) {
		NdConstantString result = new NdConstantString(pdom);
		result.setValue(value);
		return result;
	}

	public void setValue(String value) {
		VALUE.put(getNd(), this.address, value);
	}

	public IString getValue() {
		return VALUE.get(getNd(), this.address);
	}
}
