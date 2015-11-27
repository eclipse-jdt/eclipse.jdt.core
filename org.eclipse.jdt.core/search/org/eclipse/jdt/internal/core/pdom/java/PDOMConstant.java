package org.eclipse.jdt.internal.core.pdom.java;

import org.eclipse.jdt.internal.compiler.impl.Constant;
import org.eclipse.jdt.internal.compiler.lookup.TypeIds;
import org.eclipse.jdt.internal.core.pdom.PDOM;
import org.eclipse.jdt.internal.core.pdom.PDOMNode;
import org.eclipse.jdt.internal.core.pdom.field.FieldManyToOne;
import org.eclipse.jdt.internal.core.pdom.field.FieldOneToOne;
import org.eclipse.jdt.internal.core.pdom.field.StructDef;

public abstract class PDOMConstant extends PDOMNode {
	// Parent pointers. Only one will be non-null.
	// TODO(sxenos): Create something like a union to hold these, to eliminate this
	// sparse data
	public static final FieldManyToOne<PDOMConstantArray> PARENT_ARRAY;
	public static final FieldOneToOne<PDOMAnnotationValuePair> PARENT_ANNOTATION_VALUE;
	public static final FieldOneToOne<PDOMVariable> PARENT_VARIABLE;

	@SuppressWarnings("hiding")
	public static StructDef<PDOMConstant> type;

	static {
		type = StructDef.createAbstract(PDOMConstant.class, PDOMNode.type);
		PARENT_ARRAY = FieldManyToOne.createOwner(type, PDOMConstantArray.ELEMENTS);
		PARENT_ANNOTATION_VALUE = FieldOneToOne.createOwner(type, PDOMAnnotationValuePair.class,
				PDOMAnnotationValuePair.VALUE);
		PARENT_VARIABLE = FieldOneToOne.createOwner(type, PDOMVariable.class, PDOMVariable.CONSTANT);
		type.done();
	}

	public PDOMConstant(PDOM pdom, long address) {
		super(pdom, address);
	}

	protected PDOMConstant(PDOM pdom) {
		super(pdom);
	}

	public static PDOMConstant create(PDOM pdom, Constant constant) {
		if (constant == Constant.NotAConstant) {
			return null;
		}

		switch (constant.typeID()) {
			case TypeIds.T_boolean:
				return PDOMConstantBoolean.create(pdom, constant.booleanValue());
			case TypeIds.T_byte:
				return PDOMConstantByte.create(pdom, constant.byteValue());
			case TypeIds.T_char:
				return PDOMConstantChar.create(pdom, constant.charValue());
			case TypeIds.T_double:
				return PDOMConstantDouble.create(pdom, constant.doubleValue());
			case TypeIds.T_float:
				return PDOMConstantFloat.create(pdom, constant.floatValue());
			case TypeIds.T_int:
				return PDOMConstantInt.create(pdom, constant.intValue());
			case TypeIds.T_long:
				return PDOMConstantLong.create(pdom, constant.longValue());
			case TypeIds.T_short:
				return PDOMConstantShort.create(pdom, constant.shortValue());
			case TypeIds.T_JavaLangString:
				return PDOMConstantString.create(pdom, constant.stringValue());
			default:
				throw new IllegalArgumentException("Unknown typeID() " + constant.typeID()); //$NON-NLS-1$
		}
	}

	public void setParent(PDOMConstantArray result) {
		PARENT_ARRAY.put(getPDOM(), this.address, result);
	}
}
