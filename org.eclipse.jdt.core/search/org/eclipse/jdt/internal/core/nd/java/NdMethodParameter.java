package org.eclipse.jdt.internal.core.nd.java;

import org.eclipse.jdt.internal.core.nd.Nd;
import org.eclipse.jdt.internal.core.nd.NdNode;
import org.eclipse.jdt.internal.core.nd.field.FieldManyToOne;
import org.eclipse.jdt.internal.core.nd.field.FieldString;
import org.eclipse.jdt.internal.core.nd.field.StructDef;

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
		super(parent.getNd());

		PARENT.put(getNd(), this.address, parent);
		ARGUMENT_TYPE.put(getNd(), this.address, argumentType);
	}

	public void setName(char[] name) {
		NAME.put(getNd(), this.address, name);
	}
}
