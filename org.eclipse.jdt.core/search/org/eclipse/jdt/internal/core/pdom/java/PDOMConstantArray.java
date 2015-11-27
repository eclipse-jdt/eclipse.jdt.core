package org.eclipse.jdt.internal.core.pdom.java;

import java.util.List;

import org.eclipse.jdt.internal.core.pdom.PDOM;
import org.eclipse.jdt.internal.core.pdom.field.FieldOneToMany;
import org.eclipse.jdt.internal.core.pdom.field.StructDef;

public final class PDOMConstantArray extends PDOMConstant {
	public static final FieldOneToMany<PDOMConstant> ELEMENTS;

	@SuppressWarnings("hiding")
	public static StructDef<PDOMConstantArray> type;

	static {
		type = StructDef.create(PDOMConstantArray.class, PDOMConstant.type);
		ELEMENTS = FieldOneToMany.create(type, PDOMConstant.PARENT_ARRAY, 2);
		type.done();
	}

	public PDOMConstantArray(PDOM pdom, long address) {
		super(pdom, address);
	}

	public PDOMConstantArray(PDOM pdom) {
		super(pdom);
	}

	public List<PDOMConstant> getValue() {
		return ELEMENTS.asList(getPDOM(), this.address);
	}
}
