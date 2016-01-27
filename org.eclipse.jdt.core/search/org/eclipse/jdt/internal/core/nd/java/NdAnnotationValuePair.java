package org.eclipse.jdt.internal.core.nd.java;

import org.eclipse.jdt.internal.core.nd.Nd;
import org.eclipse.jdt.internal.core.nd.NdNode;
import org.eclipse.jdt.internal.core.nd.db.IString;
import org.eclipse.jdt.internal.core.nd.field.FieldManyToOne;
import org.eclipse.jdt.internal.core.nd.field.FieldOneToOne;
import org.eclipse.jdt.internal.core.nd.field.FieldString;
import org.eclipse.jdt.internal.core.nd.field.StructDef;

/**
 * @since 3.12
 */
public class NdAnnotationValuePair extends NdNode {
	public static final FieldManyToOne<NdAnnotation> APPLIES_TO;
	public static final FieldString NAME;
	public static final FieldOneToOne<NdConstant> VALUE;

	@SuppressWarnings("hiding")
	public static final StructDef<NdAnnotationValuePair> type;

	static {
		type = StructDef.create(NdAnnotationValuePair.class, NdNode.type);
		APPLIES_TO = FieldManyToOne.createOwner(type, NdAnnotation.ELEMENT_VALUE_PAIRS);
		NAME = type.addString();
		VALUE = FieldOneToOne.create(type, NdConstant.class, NdConstant.PARENT_ANNOTATION_VALUE);
		type.done();
	}

	public NdAnnotationValuePair(Nd pdom, long address) {
		super(pdom, address);
	}

	public NdAnnotationValuePair(NdAnnotation annotation, char[] name) {
		super(annotation.getPDOM());
		Nd pdom = annotation.getPDOM();
		APPLIES_TO.put(pdom, this.address, annotation);
		NAME.put(pdom, this.address, name);
	}

	public NdAnnotation getAnnotation() {
		return APPLIES_TO.get(getPDOM(), this.address);
	}

	public IString getName() {
		return NAME.get(getPDOM(), this.address);
	}

	public void setName(String name) {
		NAME.put(getPDOM(), this.address, name);
	}

	/**
	 * Returns the value of this annotation or null if none
	 */
	public NdConstant getValue() {
		return VALUE.get(getPDOM(), this.address);
	}

	public void setValue(NdConstant value) {
		VALUE.put(getPDOM(), this.address, value);
	}
}
