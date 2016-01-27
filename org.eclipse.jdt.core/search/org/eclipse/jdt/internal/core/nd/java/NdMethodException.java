package org.eclipse.jdt.internal.core.nd.java;

import org.eclipse.jdt.internal.core.nd.Nd;
import org.eclipse.jdt.internal.core.nd.NdNode;
import org.eclipse.jdt.internal.core.nd.field.FieldManyToOne;
import org.eclipse.jdt.internal.core.nd.field.StructDef;

public class NdMethodException extends NdNode {

	public static final FieldManyToOne<NdMethod> PARENT;
	public static final FieldManyToOne<NdTypeSignature> EXCEPTION_TYPE;

	@SuppressWarnings("hiding")
	public static StructDef<NdMethodException> type;

	static {
		type = StructDef.create(NdMethodException.class, NdNode.type);
		PARENT = FieldManyToOne.createOwner(type, NdMethod.EXCEPTIONS);
		EXCEPTION_TYPE = FieldManyToOne.create(type, NdTypeSignature.USED_AS_EXCEPTION);
		type.done();
	}

	public NdMethodException(Nd pdom, long address) {
		super(pdom, address);
	}

	public NdMethodException(NdMethod method, NdTypeSignature createTypeSignature) {
		super(method.getNd());

		PARENT.put(getNd(), this.address, method);
		EXCEPTION_TYPE.put(getNd(), this.address, createTypeSignature);
	}

}
