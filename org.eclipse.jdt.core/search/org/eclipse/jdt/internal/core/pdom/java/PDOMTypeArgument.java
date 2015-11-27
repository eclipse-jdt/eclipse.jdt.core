package org.eclipse.jdt.internal.core.pdom.java;

import org.eclipse.jdt.internal.core.pdom.PDOM;
import org.eclipse.jdt.internal.core.pdom.PDOMNode;
import org.eclipse.jdt.internal.core.pdom.field.FieldByte;
import org.eclipse.jdt.internal.core.pdom.field.FieldManyToOne;
import org.eclipse.jdt.internal.core.pdom.field.StructDef;

public class PDOMTypeArgument extends PDOMNode {
	public static final FieldManyToOne<PDOMComplexTypeSignature> PARENT;
	public static final FieldManyToOne<PDOMTypeSignature> TYPE_SIGNATURE;
	public static final FieldByte WILDCARD;

	public static final StructDef<PDOMTypeArgument> type;

	static {
		type = StructDef.create(PDOMTypeArgument.class, PDOMNode.type);
		PARENT = FieldManyToOne.createOwner(type, PDOMComplexTypeSignature.TYPE_ARGUMENTS);
		TYPE_SIGNATURE = FieldManyToOne.create(type, PDOMTypeSignature.USED_AS_TYPE_ARGUMENT);
		WILDCARD = type.addByte();
		type.done();
	}

	public static final int WILDCARD_NONE = 0;
	public static final int WILDCARD_EXTENDS = 1;
	public static final int WILDCARD_SUPER = 2;
	public static final int WILDCARD_QUESTION = 2;

	public PDOMTypeArgument(PDOM pdom, long address) {
		super(pdom, address);
	}

	public PDOMTypeArgument(PDOM pdom, PDOMComplexTypeSignature typeSignature) {
		super(pdom);

		TYPE_SIGNATURE.put(pdom, this.address, typeSignature);
	}

	/**
	 * Sets the wildcard to use, one of the WILDCARD_* constants.
	 * 
	 * @param wildcard
	 */
	public void setWildcard(int wildcard) {
		WILDCARD.put(getPDOM(), this.address, (byte) wildcard);
	}

	public void setType(PDOMTypeSignature typeSignature) {
		TYPE_SIGNATURE.put(getPDOM(), this.address, typeSignature);
	}

	public int getWildcard() {
		return WILDCARD.get(getPDOM(), this.address);
	}

	public PDOMComplexTypeSignature getParent() {
		return PARENT.get(getPDOM(), this.address);
	}

	public PDOMTypeSignature getType() {
		return TYPE_SIGNATURE.get(getPDOM(), this.address);
	}
}
