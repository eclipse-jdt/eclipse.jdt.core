package org.eclipse.jdt.internal.core.nd.java;

import org.eclipse.jdt.internal.compiler.impl.Constant;
import org.eclipse.jdt.internal.compiler.lookup.TypeIds;
import org.eclipse.jdt.internal.core.nd.Nd;
import org.eclipse.jdt.internal.core.nd.NdNode;
import org.eclipse.jdt.internal.core.nd.field.FieldManyToOne;
import org.eclipse.jdt.internal.core.nd.field.FieldOneToOne;
import org.eclipse.jdt.internal.core.nd.field.StructDef;

/**
 * @since 3.12
 */
public abstract class NdConstant extends NdNode {
	// Parent pointers. Only one will be non-null.
	// TODO(sxenos): Create something like a union to hold these, to eliminate this
	// sparse data
	public static final FieldManyToOne<NdConstantArray> PARENT_ARRAY;
	public static final FieldOneToOne<NdAnnotationValuePair> PARENT_ANNOTATION_VALUE;
	public static final FieldOneToOne<NdVariable> PARENT_VARIABLE;
	public static final FieldOneToOne<NdMethod> PARENT_METHOD;

	@SuppressWarnings("hiding")
	public static StructDef<NdConstant> type;

	static {
		type = StructDef.createAbstract(NdConstant.class, NdNode.type);
		PARENT_ARRAY = FieldManyToOne.createOwner(type, NdConstantArray.ELEMENTS);
		PARENT_ANNOTATION_VALUE = FieldOneToOne.createOwner(type, NdAnnotationValuePair.class,
				NdAnnotationValuePair.VALUE);
		PARENT_VARIABLE = FieldOneToOne.createOwner(type, NdVariable.class, NdVariable.CONSTANT);
		PARENT_METHOD = FieldOneToOne.createOwner(type, NdMethod.class, NdMethod.DEFAULT_VALUE);
		type.done();
	}

	public NdConstant(Nd pdom, long address) {
		super(pdom, address);
	}

	protected NdConstant(Nd pdom) {
		super(pdom);
	}

	public static NdConstant create(Nd pdom, Constant constant) {
		if (constant == Constant.NotAConstant) {
			return null;
		}

		switch (constant.typeID()) {
			case TypeIds.T_boolean:
				return NdConstantBoolean.create(pdom, constant.booleanValue());
			case TypeIds.T_byte:
				return NdConstantByte.create(pdom, constant.byteValue());
			case TypeIds.T_char:
				return NdConstantChar.create(pdom, constant.charValue());
			case TypeIds.T_double:
				return NdConstantDouble.create(pdom, constant.doubleValue());
			case TypeIds.T_float:
				return NdConstantFloat.create(pdom, constant.floatValue());
			case TypeIds.T_int:
				return NdConstantInt.create(pdom, constant.intValue());
			case TypeIds.T_long:
				return NdConstantLong.create(pdom, constant.longValue());
			case TypeIds.T_short:
				return NdConstantShort.create(pdom, constant.shortValue());
			case TypeIds.T_JavaLangString:
				return NdConstantString.create(pdom, constant.stringValue());
			default:
				throw new IllegalArgumentException("Unknown typeID() " + constant.typeID()); //$NON-NLS-1$
		}
	}

	public void setParent(NdConstantArray result) {
		PARENT_ARRAY.put(getNd(), this.address, result);
	}

	/**
	 * Returns the {@link Constant} corresponding to the value of this {@link NdConstant} or null if the receiver
	 * corresponds to a {@link Constant}.
	 */
	public abstract Constant getConstant();
}
