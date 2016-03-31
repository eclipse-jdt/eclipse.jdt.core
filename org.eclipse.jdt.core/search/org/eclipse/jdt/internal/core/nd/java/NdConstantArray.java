package org.eclipse.jdt.internal.core.nd.java;

import java.util.List;

import org.eclipse.jdt.internal.compiler.impl.Constant;
import org.eclipse.jdt.internal.core.nd.Nd;
import org.eclipse.jdt.internal.core.nd.field.FieldOneToMany;
import org.eclipse.jdt.internal.core.nd.field.StructDef;

/**
 * @since 3.12
 */
public final class NdConstantArray extends NdConstant {
	public static final FieldOneToMany<NdConstant> ELEMENTS;

	@SuppressWarnings("hiding")
	public static StructDef<NdConstantArray> type;

	static {
		type = StructDef.create(NdConstantArray.class, NdConstant.type);
		ELEMENTS = FieldOneToMany.create(type, NdConstant.PARENT_ARRAY, 2);
		type.done();
	}

	public NdConstantArray(Nd nd, long address) {
		super(nd, address);
	}

	public NdConstantArray(Nd nd) {
		super(nd);
	}

	public List<NdConstant> getValue() {
		return ELEMENTS.asList(getNd(), this.address);
	}

	@Override
	public Constant getConstant() {
		return null;
	}
}
