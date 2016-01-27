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

import org.eclipse.jdt.internal.core.pdom.Nd;
import org.eclipse.jdt.internal.core.pdom.NdNode;
import org.eclipse.jdt.internal.core.pdom.field.FieldOneToMany;
import org.eclipse.jdt.internal.core.pdom.field.FieldOneToOne;
import org.eclipse.jdt.internal.core.pdom.field.FieldManyToOne;
import org.eclipse.jdt.internal.core.pdom.field.StructDef;

/**
 * @since 3.12
 */
public class NdAnnotation extends NdNode {
	public static final FieldManyToOne<NdBinding> PARENT_BINDING;
	public static final FieldManyToOne<NdTypeSignature> ANNOTATION_TYPE;
	public static final FieldOneToMany<NdAnnotationValuePair> ELEMENT_VALUE_PAIRS;
	public static final FieldOneToOne<NdConstantAnnotation> PARENT_CONSTANT;
	public static final FieldManyToOne<NdComplexTypeSignature> PARENT_TYPE_SIGNATURE;

	@SuppressWarnings("hiding")
	public static final StructDef<NdAnnotation> type;

	static {
		type = StructDef.create(NdAnnotation.class, NdNode.type);
		PARENT_BINDING = FieldManyToOne.createOwner(type, NdBinding.ANNOTATIONS);
		ANNOTATION_TYPE = FieldManyToOne.create(type, NdTypeSignature.ANNOTATIONS_OF_THIS_TYPE);
		ELEMENT_VALUE_PAIRS = FieldOneToMany.create(type, NdAnnotationValuePair.APPLIES_TO);
		PARENT_CONSTANT = FieldOneToOne.createOwner(type, NdConstantAnnotation.class, NdConstantAnnotation.VALUE);
		PARENT_TYPE_SIGNATURE = FieldManyToOne.createOwner(type, NdComplexTypeSignature.ANNOTATIONS);
		type.done();
	}

	public NdAnnotation(Nd pdom, long address) {
		super(pdom, address);
	}

	public NdAnnotation(Nd pdom, NdTypeSignature type) {
		super(pdom);

		ANNOTATION_TYPE.put(getPDOM(), this.address, type);
	}

	public void setParent(NdBinding binding) {
		PARENT_BINDING.put(getPDOM(), this.address, binding);
	}

	public void setParent(NdConstantAnnotation constant) {
		PARENT_CONSTANT.put(getPDOM(), this.address, constant);
	}

	public void setParent(NdComplexTypeSignature signature) {
		PARENT_TYPE_SIGNATURE.put(getPDOM(), this.address, signature);
	}

	public NdTypeSignature getType() {
		return ANNOTATION_TYPE.get(getPDOM(), this.address);
	}

	public NdBinding getAppliesTo() {
		return PARENT_BINDING.get(getPDOM(), this.address);
	}
}
