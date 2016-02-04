package org.eclipse.jdt.internal.core.nd.java;

import org.eclipse.jdt.internal.compiler.impl.Constant;
import org.eclipse.jdt.internal.core.nd.Nd;
import org.eclipse.jdt.internal.core.nd.field.FieldOneToOne;
import org.eclipse.jdt.internal.core.nd.field.StructDef;

/**
 * @since 3.12
 */
public final class NdConstantAnnotation extends NdConstant {
	public static final FieldOneToOne<NdAnnotation> VALUE;

	@SuppressWarnings("hiding")
	public static StructDef<NdConstantAnnotation> type;

	static {
		type = StructDef.create(NdConstantAnnotation.class, NdConstant.type);
		VALUE = FieldOneToOne.create(type, NdAnnotation.class, NdAnnotation.PARENT_CONSTANT);
		type.done();
	}

	public NdConstantAnnotation(Nd pdom, long address) {
		super(pdom, address);
	}

	protected NdConstantAnnotation(Nd pdom) {
		super(pdom);
	}

	public static NdConstantAnnotation create(Nd pdom, NdAnnotation value) {
		NdConstantAnnotation result = new NdConstantAnnotation(pdom);
		result.setValue(value);
		return result;
	}

	public void setValue(NdAnnotation value) {
		VALUE.put(getNd(), this.address, value);
	}

	public NdAnnotation getValue() {
		return VALUE.get(getNd(), this.address);
	}

	@Override
	public Constant getConstant() {
		return null;
	}
}
