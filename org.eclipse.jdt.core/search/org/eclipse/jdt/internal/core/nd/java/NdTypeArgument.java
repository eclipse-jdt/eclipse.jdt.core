package org.eclipse.jdt.internal.core.nd.java;

import org.eclipse.jdt.internal.core.nd.Nd;
import org.eclipse.jdt.internal.core.nd.NdNode;
import org.eclipse.jdt.internal.core.nd.field.FieldByte;
import org.eclipse.jdt.internal.core.nd.field.FieldManyToOne;
import org.eclipse.jdt.internal.core.nd.field.StructDef;

/**
 * @since 3.12
 */
public class NdTypeArgument extends NdNode {
	public static final FieldManyToOne<NdComplexTypeSignature> PARENT;
	public static final FieldManyToOne<NdTypeSignature> TYPE_SIGNATURE;
	public static final FieldByte WILDCARD;

	public static final StructDef<NdTypeArgument> type;

	static {
		type = StructDef.create(NdTypeArgument.class, NdNode.type);
		PARENT = FieldManyToOne.createOwner(type, NdComplexTypeSignature.TYPE_ARGUMENTS);
		TYPE_SIGNATURE = FieldManyToOne.create(type, NdTypeSignature.USED_AS_TYPE_ARGUMENT);
		WILDCARD = type.addByte();
		type.done();
	}

	public static final int WILDCARD_NONE = 0;
	public static final int WILDCARD_EXTENDS = 1;
	public static final int WILDCARD_SUPER = 2;
	public static final int WILDCARD_QUESTION = 2;

	public NdTypeArgument(Nd pdom, long address) {
		super(pdom, address);
	}

	public NdTypeArgument(Nd pdom, NdComplexTypeSignature typeSignature) {
		super(pdom);

		PARENT.put(pdom, this.address, typeSignature);
	}

	/**
	 * Sets the wildcard to use, one of the WILDCARD_* constants.
	 * 
	 * @param wildcard
	 */
	public void setWildcard(int wildcard) {
		WILDCARD.put(getPDOM(), this.address, (byte) wildcard);
	}

	public void setType(NdTypeSignature typeSignature) {
		TYPE_SIGNATURE.put(getPDOM(), this.address, typeSignature);
	}

	public int getWildcard() {
		return WILDCARD.get(getPDOM(), this.address);
	}

	public NdComplexTypeSignature getParent() {
		return PARENT.get(getPDOM(), this.address);
	}

	public NdTypeSignature getType() {
		return TYPE_SIGNATURE.get(getPDOM(), this.address);
	}
}
