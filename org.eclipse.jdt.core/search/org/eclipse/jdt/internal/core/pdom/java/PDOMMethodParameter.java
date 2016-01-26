package org.eclipse.jdt.internal.core.pdom.java;

import org.eclipse.jdt.internal.core.pdom.PDOM;
import org.eclipse.jdt.internal.core.pdom.PDOMNode;
import org.eclipse.jdt.internal.core.pdom.field.FieldManyToOne;
import org.eclipse.jdt.internal.core.pdom.field.FieldString;
import org.eclipse.jdt.internal.core.pdom.field.StructDef;

public class PDOMMethodParameter extends PDOMNode {
	public static final FieldManyToOne<PDOMMethod> PARENT;
	public static final FieldManyToOne<PDOMTypeSignature> ARGUMENT_TYPE;
	public static final FieldString NAME;

	@SuppressWarnings("hiding")
	public static StructDef<PDOMMethodParameter> type;

	static {
		type = StructDef.create(PDOMMethodParameter.class, PDOMNode.type);
		PARENT = FieldManyToOne.create(type, PDOMMethod.PARAMETERS);
		ARGUMENT_TYPE = FieldManyToOne.create(type, PDOMTypeSignature.USED_AS_METHOD_ARGUMENT);
		NAME = type.addString();
		type.done();
	}

	public PDOMMethodParameter(PDOM pdom, long address) {
		super(pdom, address);
	}

	public PDOMMethodParameter(PDOMMethod parent, PDOMTypeSignature argumentType) {
		super(parent.getPDOM());

		PARENT.put(getPDOM(), this.address, parent);
		ARGUMENT_TYPE.put(getPDOM(), this.address, argumentType);
	}

	public void setName(char[] name) {
		NAME.put(getPDOM(), this.address, name);
	}
}
