package org.eclipse.jdt.internal.core.pdom.java;

import org.eclipse.jdt.internal.core.pdom.PDOM;
import org.eclipse.jdt.internal.core.pdom.PDOMNode;
import org.eclipse.jdt.internal.core.pdom.field.FieldManyToOne;
import org.eclipse.jdt.internal.core.pdom.field.StructDef;

/**
 * Represents the bound on a generic parameter (a ClassBound or InterfaceBound in
 * the sense of the Java VM spec Java SE 8 Edition, section 4.7.9.1)
 * 
 * @since 3.12
 */
public class PDOMTypeBound extends PDOMNode {
	public static final FieldManyToOne<PDOMTypeParameter> PARENT;
	public static final FieldManyToOne<PDOMTypeSignature> TYPE;

	public static final StructDef<PDOMTypeBound> type;

	static {
		type = StructDef.create(PDOMTypeBound.class, PDOMNode.type);
		PARENT = FieldManyToOne.createOwner(type, PDOMTypeParameter.BOUNDS);
		TYPE = FieldManyToOne.create(type, PDOMTypeSignature.USED_AS_TYPE_BOUND);

		type.done();
	}

	public PDOMTypeBound(PDOM pdom, long address) {
		super(pdom, address);
	}

	public PDOMTypeBound(PDOMTypeParameter parent, PDOMTypeSignature signature) {
		super(parent.getPDOM());

		PARENT.put(getPDOM(), this.address, parent);
		TYPE.put(getPDOM(), this.address, signature);
	}

	public PDOMTypeParameter getParent() {
		return PARENT.get(getPDOM(), this.address);
	}

	public PDOMTypeSignature getType() {
		return TYPE.get(getPDOM(), this.address);
	}
}
