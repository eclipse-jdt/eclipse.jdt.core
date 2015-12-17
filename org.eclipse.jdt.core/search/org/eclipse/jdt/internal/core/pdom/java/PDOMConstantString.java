package org.eclipse.jdt.internal.core.pdom.java;

import org.eclipse.jdt.internal.core.pdom.PDOM;
import org.eclipse.jdt.internal.core.pdom.db.IString;
import org.eclipse.jdt.internal.core.pdom.field.FieldString;
import org.eclipse.jdt.internal.core.pdom.field.StructDef;

/**
 * @since 3.12
 */
public final class PDOMConstantString extends PDOMConstant {
	public static final FieldString VALUE;

	@SuppressWarnings("hiding")
	public static StructDef<PDOMConstantString> type;

	static {
		type = StructDef.create(PDOMConstantString.class, PDOMConstant.type);
		VALUE = type.addString();
		type.done();
	}

	public PDOMConstantString(PDOM pdom, long address) {
		super(pdom, address);
	}

	protected PDOMConstantString(PDOM pdom) {
		super(pdom);
	}

	public static PDOMConstantString create(PDOM pdom, String value) {
		PDOMConstantString result = new PDOMConstantString(pdom);
		result.setValue(value);
		return result;
	}

	public void setValue(String value) {
		VALUE.put(getPDOM(), this.address, value);
	}

	public IString getValue() {
		return VALUE.get(getPDOM(), this.address);
	}
}
