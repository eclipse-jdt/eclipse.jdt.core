package org.eclipse.jdt.internal.core.nd.java;

import org.eclipse.jdt.internal.compiler.impl.Constant;
import org.eclipse.jdt.internal.core.nd.Nd;
import org.eclipse.jdt.internal.core.nd.field.FieldOneToOne;
import org.eclipse.jdt.internal.core.nd.field.StructDef;

/**
 * @since 3.12
 */
public final class NdConstantAnnotation extends NdConstant {
	public static final FieldOneToOne<NdAnnotationInConstant> VALUE;

	@SuppressWarnings("hiding")
	public static StructDef<NdConstantAnnotation> type;

	static {
		type = StructDef.create(NdConstantAnnotation.class, NdConstant.type);
		VALUE = FieldOneToOne.create(type, NdAnnotationInConstant.class, NdAnnotationInConstant.OWNER);
		type.done();
	}

	public NdConstantAnnotation(Nd nd, long address) {
		super(nd, address);
	}

	protected NdConstantAnnotation(Nd nd) {
		super(nd);
	}

	public static NdConstantAnnotation create(Nd nd, NdAnnotationInConstant value) {
		NdConstantAnnotation result = new NdConstantAnnotation(nd);
		result.setValue(value);
		return result;
	}

	public void setValue(NdAnnotationInConstant value) {
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
