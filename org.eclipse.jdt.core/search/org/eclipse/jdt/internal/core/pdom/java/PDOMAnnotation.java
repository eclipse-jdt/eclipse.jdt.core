/*******************************************************************************
 * Copyright (c) 2015 Google, Inc and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Stefan Xenos (Google) - Initial implementation
 *******************************************************************************/
package org.eclipse.jdt.internal.core.pdom.java;

import org.eclipse.jdt.internal.core.pdom.PDOM;
import org.eclipse.jdt.internal.core.pdom.PDOMNode;
import org.eclipse.jdt.internal.core.pdom.field.FieldOneToMany;
import org.eclipse.jdt.internal.core.pdom.field.FieldOneToOne;
import org.eclipse.jdt.internal.core.pdom.field.FieldManyToOne;
import org.eclipse.jdt.internal.core.pdom.field.StructDef;

/**
 * @since 3.12
 */
public class PDOMAnnotation extends PDOMNode {
	public static final FieldManyToOne<PDOMBinding> PARENT_BINDING;
	public static final FieldManyToOne<PDOMTypeSignature> ANNOTATION_TYPE;
	public static final FieldOneToMany<PDOMAnnotationValuePair> ELEMENT_VALUE_PAIRS;
	public static final FieldOneToOne<PDOMConstantAnnotation> PARENT_CONSTANT;
	public static final FieldManyToOne<PDOMComplexTypeSignature> PARENT_TYPE_SIGNATURE;

	@SuppressWarnings("hiding")
	public static final StructDef<PDOMAnnotation> type;

	static {
		type = StructDef.create(PDOMAnnotation.class, PDOMNode.type);
		PARENT_BINDING = FieldManyToOne.createOwner(type, PDOMBinding.ANNOTATIONS);
		ANNOTATION_TYPE = FieldManyToOne.create(type, PDOMTypeSignature.ANNOTATIONS_OF_THIS_TYPE);
		ELEMENT_VALUE_PAIRS = FieldOneToMany.create(type, PDOMAnnotationValuePair.APPLIES_TO);
		PARENT_CONSTANT = FieldOneToOne.createOwner(type, PDOMConstantAnnotation.class, PDOMConstantAnnotation.VALUE);
		PARENT_TYPE_SIGNATURE = FieldManyToOne.createOwner(type, PDOMComplexTypeSignature.ANNOTATIONS);
		type.done();
	}

	public PDOMAnnotation(PDOM pdom, long record) {
		super(pdom, record);
	}

	public PDOMAnnotation(PDOM pdom, PDOMTypeSignature type) {
		super(pdom);

		ANNOTATION_TYPE.put(getPDOM(), this.address, type);
	}

	public void setParent(PDOMBinding binding) {
		PARENT_BINDING.put(getPDOM(), this.address, binding);
	}

	public void setParent(PDOMConstantAnnotation constant) {
		PARENT_CONSTANT.put(getPDOM(), this.address, constant);
	}

	public PDOMTypeSignature getType() {
		return ANNOTATION_TYPE.get(getPDOM(), this.address);
	}

	public PDOMBinding getAppliesTo() {
		return PARENT_BINDING.get(getPDOM(), this.address);
	}
}
