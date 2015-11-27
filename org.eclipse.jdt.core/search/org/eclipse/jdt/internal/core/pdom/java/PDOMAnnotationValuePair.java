package org.eclipse.jdt.internal.core.pdom.java;

import org.eclipse.jdt.internal.core.pdom.PDOM;
import org.eclipse.jdt.internal.core.pdom.PDOMNode;
import org.eclipse.jdt.internal.core.pdom.db.IString;
import org.eclipse.jdt.internal.core.pdom.field.FieldManyToOne;
import org.eclipse.jdt.internal.core.pdom.field.FieldOneToOne;
import org.eclipse.jdt.internal.core.pdom.field.FieldString;
import org.eclipse.jdt.internal.core.pdom.field.StructDef;

public class PDOMAnnotationValuePair extends PDOMNode {
	public static final FieldManyToOne<PDOMAnnotation> APPLIES_TO;
	public static final FieldString NAME;
	public static final FieldOneToOne<PDOMConstant> VALUE;

	@SuppressWarnings("hiding")
	public static final StructDef<PDOMAnnotationValuePair> type;

	static {
		type = StructDef.create(PDOMAnnotationValuePair.class, PDOMNode.type);
		APPLIES_TO = FieldManyToOne.createOwner(type, PDOMAnnotation.ELEMENT_VALUE_PAIRS);
		NAME = type.addString();
		VALUE = FieldOneToOne.create(type, PDOMConstant.class, PDOMConstant.PARENT_ANNOTATION_VALUE);
		type.done();
	}

	public PDOMAnnotationValuePair(PDOM pdom, long address) {
		super(pdom, address);
	}

	public PDOMAnnotationValuePair(PDOMAnnotation annotation, String name) {
		super(annotation.getPDOM());
		PDOM pdom = annotation.getPDOM();
		APPLIES_TO.put(pdom, this.address, annotation);
		NAME.put(pdom, this.address, name);
	}

	public PDOMAnnotation getAnnotation() {
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
	public PDOMConstant getValue() {
		return VALUE.get(getPDOM(), this.address);
	}

	public void setValue(PDOMConstant value) {
		VALUE.put(getPDOM(), this.address, value);
	}
}
