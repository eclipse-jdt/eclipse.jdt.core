package org.eclipse.jdt.internal.core.pdom.java;

import org.eclipse.jdt.internal.core.pdom.PDOM;
import org.eclipse.jdt.internal.core.pdom.db.IString;
import org.eclipse.jdt.internal.core.pdom.field.FieldManyToOne;
import org.eclipse.jdt.internal.core.pdom.field.FieldOneToMany;
import org.eclipse.jdt.internal.core.pdom.field.FieldString;
import org.eclipse.jdt.internal.core.pdom.field.StructDef;

/**
 * Represents a type signature that is anything other than a trivial reference to a concrete
 * type. If a type reference includes annotations, generic arguments, wildcards, or is a
 * type variable, this object represents it. 
 * <p>
 * Arrays are encoded in a special way. The RAW_TYPE points to a sentinel type called '['
 * and the first type argument holds the array type.
 * 
 * @since 3.12
 */
public class PDOMComplexTypeSignature extends PDOMTypeSignature {
	public static final FieldString VARIABLE_IDENTIFIER;
	public static final FieldManyToOne<PDOMTypeId> RAW_TYPE;
	public static final FieldOneToMany<PDOMAnnotation> ANNOTATIONS;
	public static final FieldOneToMany<PDOMTypeArgument> TYPE_ARGUMENTS;
	public static final FieldManyToOne<PDOMComplexTypeSignature> DECLARING_TYPE;
	public static final FieldOneToMany<PDOMComplexTypeSignature> DECLARED_TYPES;

	@SuppressWarnings("hiding")
	public static final StructDef<PDOMComplexTypeSignature> type;

	static {
		type = StructDef.create(PDOMComplexTypeSignature.class, PDOMTypeSignature.type);
		VARIABLE_IDENTIFIER = type.addString();
		RAW_TYPE = FieldManyToOne.create(type, PDOMTypeId.USED_AS_COMPLEX_TYPE);
		ANNOTATIONS = FieldOneToMany.create(type, PDOMAnnotation.PARENT_TYPE_SIGNATURE);
		TYPE_ARGUMENTS = FieldOneToMany.create(type, PDOMTypeArgument.PARENT);
		DECLARING_TYPE = FieldManyToOne.create(type, null);
		DECLARED_TYPES = FieldOneToMany.create(type, DECLARING_TYPE);

		type.useStandardRefCounting().done();
	}

	public PDOMComplexTypeSignature(PDOM pdom, long record) {
		super(pdom, record);
	}

	public PDOMComplexTypeSignature(PDOM pdom) {
		super(pdom);
	}

	@Override
	public PDOMTypeId getRawType() {
		return RAW_TYPE.get(getPDOM(), this.address);
	}

	public void setVariableIdentifier(String variableIdentifier) {
		VARIABLE_IDENTIFIER.put(getPDOM(), this.address, variableIdentifier);
	}

	/**
	 * If this type is a variable, this returns the identifier
	 * 
	 * @return
	 */
	public IString getVariableIdentifier() {
		return VARIABLE_IDENTIFIER.get(getPDOM(), this.address);
	}

	public void setRawType(PDOMTypeId rawType) {
		RAW_TYPE.put(getPDOM(), this.address, rawType);
	}

	public void setGenericDeclaringType(PDOMComplexTypeSignature enclosingType) {
		DECLARING_TYPE.put(getPDOM(), this.address, enclosingType);
	}

	/**
	 * Returns the declaring type (as reported by the type's generic signature).
	 * Not to be confused with the declaring type as stored in the class file.
	 * That is stored in {@link PDOMType#getDeclaringType}. Any class that is
	 * nested inside another class with generic arguments will have one of
	 * these. Classes nested inside non-generic classes won't have one of these,
	 * and neither will non-nested classes.
	 */
	public PDOMComplexTypeSignature getGenericDeclaringType() {
		return DECLARING_TYPE.get(getPDOM(), this.address);
	}
}
