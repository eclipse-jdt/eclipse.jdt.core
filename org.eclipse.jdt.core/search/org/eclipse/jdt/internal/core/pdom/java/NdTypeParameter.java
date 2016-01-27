package org.eclipse.jdt.internal.core.pdom.java;

import org.eclipse.jdt.internal.core.pdom.Nd;
import org.eclipse.jdt.internal.core.pdom.NdNode;
import org.eclipse.jdt.internal.core.pdom.field.FieldManyToOne;
import org.eclipse.jdt.internal.core.pdom.field.FieldOneToMany;
import org.eclipse.jdt.internal.core.pdom.field.FieldString;
import org.eclipse.jdt.internal.core.pdom.field.StructDef;

/**
 * Represents a TypeParameter, as described in Section 4.7.9.1 of
 * the java VM specification, Java SE 8 edititon.
 * @since 3.12
 */
public class NdTypeParameter extends NdNode {
	public static final FieldManyToOne<NdBinding> PARENT;
	public static final FieldString IDENTIFIER;
	public static final FieldOneToMany<NdTypeBound> BOUNDS;

	public static final StructDef<NdTypeParameter> type;

	static {
		type = StructDef.create(NdTypeParameter.class, NdNode.type);
		PARENT = FieldManyToOne.createOwner(type, NdType.TYPE_PARAMETERS);
		IDENTIFIER = type.addString();
		BOUNDS = FieldOneToMany.create(type, NdTypeBound.PARENT);

		type.done();
	}

	public NdTypeParameter(Nd pdom, long address) {
		super(pdom, address);
	}

	public NdTypeParameter(NdBinding parent, char[] identifier) {
		super(parent.getPDOM());

		PARENT.put(getPDOM(), this.address, parent);
		IDENTIFIER.put(getPDOM(), this.address, identifier);
	}
}
