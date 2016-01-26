package org.eclipse.jdt.internal.core.pdom.java;

import org.eclipse.jdt.internal.core.pdom.PDOM;
import org.eclipse.jdt.internal.core.pdom.PDOMNode;
import org.eclipse.jdt.internal.core.pdom.field.FieldManyToOne;
import org.eclipse.jdt.internal.core.pdom.field.StructDef;

public class PDOMMethodException extends PDOMNode {

	public static final FieldManyToOne<PDOMMethod> PARENT;
	public static final FieldManyToOne<PDOMTypeSignature> EXCEPTION_TYPE;

	@SuppressWarnings("hiding")
	public static StructDef<PDOMMethodException> type;

	static {
		type = StructDef.create(PDOMMethodException.class, PDOMNode.type);
		PARENT = FieldManyToOne.createOwner(type, PDOMMethod.EXCEPTIONS);
		EXCEPTION_TYPE = FieldManyToOne.create(type, PDOMTypeSignature.USED_AS_EXCEPTION);
		type.done();
	}

	public PDOMMethodException(PDOM pdom, long address) {
		super(pdom, address);
	}

	public PDOMMethodException(PDOMMethod method, PDOMTypeSignature createTypeSignature) {
		super(method.getPDOM());

		PARENT.put(getPDOM(), this.address, method);
		EXCEPTION_TYPE.put(getPDOM(), this.address, createTypeSignature);
	}

}
