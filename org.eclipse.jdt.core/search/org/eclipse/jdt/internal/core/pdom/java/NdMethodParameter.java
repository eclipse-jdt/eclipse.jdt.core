package org.eclipse.jdt.internal.core.pdom.java;

import org.eclipse.jdt.internal.core.pdom.Nd;
import org.eclipse.jdt.internal.core.pdom.NdNode;
import org.eclipse.jdt.internal.core.pdom.field.FieldManyToOne;
import org.eclipse.jdt.internal.core.pdom.field.FieldString;
import org.eclipse.jdt.internal.core.pdom.field.StructDef;

public class NdMethodParameter extends NdNode {
	public static final FieldManyToOne<NdMethod> PARENT;
	public static final FieldManyToOne<NdTypeSignature> ARGUMENT_TYPE;
	public static final FieldString NAME;

	@SuppressWarnings("hiding")
	public static StructDef<NdMethodParameter> type;

	static {
		type = StructDef.create(NdMethodParameter.class, NdNode.type);
		PARENT = FieldManyToOne.create(type, NdMethod.PARAMETERS);
		ARGUMENT_TYPE = FieldManyToOne.create(type, NdTypeSignature.USED_AS_METHOD_ARGUMENT);
		NAME = type.addString();
		type.done();
	}

	public NdMethodParameter(Nd pdom, long address) {
		super(pdom, address);
	}

	public NdMethodParameter(NdMethod parent, NdTypeSignature argumentType) {
		super(parent.getPDOM());

		PARENT.put(getPDOM(), this.address, parent);
		ARGUMENT_TYPE.put(getPDOM(), this.address, argumentType);
	}

	public void setName(char[] name) {
		NAME.put(getPDOM(), this.address, name);
	}
}
