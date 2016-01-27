package org.eclipse.jdt.internal.core.pdom.java;

import org.eclipse.jdt.internal.core.pdom.PDOM;
import org.eclipse.jdt.internal.core.pdom.PDOMNode;
import org.eclipse.jdt.internal.core.pdom.field.FieldManyToOne;
import org.eclipse.jdt.internal.core.pdom.field.FieldOneToMany;
import org.eclipse.jdt.internal.core.pdom.field.FieldString;
import org.eclipse.jdt.internal.core.pdom.field.StructDef;

/**
 * Represents a TypeParameter, as described in Section 4.7.9.1 of
 * the java VM specification, Java SE 8 edititon.
 * @since 3.12
 */
public class PDOMTypeParameter extends PDOMNode {
	public static final FieldManyToOne<PDOMBinding> PARENT;
	public static final FieldString IDENTIFIER;
	public static final FieldOneToMany<PDOMTypeBound> BOUNDS;

	public static final StructDef<PDOMTypeParameter> type;

	static {
		type = StructDef.create(PDOMTypeParameter.class, PDOMNode.type);
		PARENT = FieldManyToOne.createOwner(type, PDOMType.TYPE_PARAMETERS);
		IDENTIFIER = type.addString();
		BOUNDS = FieldOneToMany.create(type, PDOMTypeBound.PARENT);

		type.done();
	}

	public PDOMTypeParameter(PDOM pdom, long address) {
		super(pdom, address);
	}

	public PDOMTypeParameter(PDOMBinding parent, char[] identifier) {
		super(parent.getPDOM());

		PARENT.put(getPDOM(), this.address, parent);
		IDENTIFIER.put(getPDOM(), this.address, identifier);
	}
}
