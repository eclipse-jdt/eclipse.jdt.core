package org.eclipse.jdt.internal.core.pdom.java;

import org.eclipse.jdt.internal.core.pdom.PDOM;
import org.eclipse.jdt.internal.core.pdom.field.FieldOneToOne;
import org.eclipse.jdt.internal.core.pdom.field.StructDef;

/**
 * @since 3.12
 */
public final class PDOMConstantAnnotation extends PDOMConstant {
	public static final FieldOneToOne<PDOMAnnotation> VALUE;

	@SuppressWarnings("hiding")
	public static StructDef<PDOMConstantAnnotation> type;

	static {
		type = StructDef.create(PDOMConstantAnnotation.class, PDOMConstant.type);
		VALUE = FieldOneToOne.create(type, PDOMAnnotation.class, PDOMAnnotation.PARENT_CONSTANT);
		type.done();
	}

	public PDOMConstantAnnotation(PDOM pdom, long address) {
		super(pdom, address);
	}

	protected PDOMConstantAnnotation(PDOM pdom) {
		super(pdom);
	}

	public static PDOMConstantAnnotation create(PDOM pdom, PDOMAnnotation value) {
		PDOMConstantAnnotation result = new PDOMConstantAnnotation(pdom);
		result.setValue(value);
		return result;
	}

	public void setValue(PDOMAnnotation value) {
		VALUE.put(getPDOM(), this.address, value);
	}

	public PDOMAnnotation getValue() {
		return VALUE.get(getPDOM(), this.address);
	}
}
