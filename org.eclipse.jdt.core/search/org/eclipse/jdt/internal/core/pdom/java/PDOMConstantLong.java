package org.eclipse.jdt.internal.core.pdom.java;

import org.eclipse.jdt.internal.core.pdom.PDOM;
import org.eclipse.jdt.internal.core.pdom.field.FieldLong;
import org.eclipse.jdt.internal.core.pdom.field.StructDef;

/**
 * @since 3.12
 */
public final class PDOMConstantLong extends PDOMConstant {
	public static final FieldLong VALUE;

	@SuppressWarnings("hiding")
	public static StructDef<PDOMConstantLong> type;

	static {
		type = StructDef.create(PDOMConstantLong.class, PDOMConstant.type);
		VALUE = type.addLong();
		type.done();
	}

	public PDOMConstantLong(PDOM pdom, long address) {
		super(pdom, address);
	}

	protected PDOMConstantLong(PDOM pdom) {
		super(pdom);
	}

	public static PDOMConstantLong create(PDOM pdom, long value) {
		PDOMConstantLong result = new PDOMConstantLong(pdom);
		result.setValue(value);
		return result;
	}

	public void setValue(long value) {
		VALUE.put(getPDOM(), this.address, value);
	}

	public long getValue() {
		return VALUE.get(getPDOM(), this.address);
	}
}
