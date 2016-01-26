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
import org.eclipse.jdt.internal.core.pdom.field.FieldInt;
import org.eclipse.jdt.internal.core.pdom.field.FieldManyToOne;
import org.eclipse.jdt.internal.core.pdom.field.FieldOneToOne;
import org.eclipse.jdt.internal.core.pdom.field.FieldShort;
import org.eclipse.jdt.internal.core.pdom.field.FieldString;
import org.eclipse.jdt.internal.core.pdom.field.StructDef;

/**
 * @since 3.12
 */
public class PDOMVariable extends PDOMBinding {	
	public static final FieldShort VARIABLE_FLAGS;
	public static final FieldManyToOne<PDOMTypeSignature> TYPE;
	public static final FieldInt VARIABLE_ID;
	public static final FieldManyToOne<PDOMMethod> DECLARING_METHOD;
	public static final FieldManyToOne<PDOMBinding> PARENT;
	public static final FieldString NAME;
	public static final FieldOneToOne<PDOMConstant> CONSTANT;

	@SuppressWarnings("hiding")
	public static StructDef<PDOMVariable> type;

	public static final int FLG_VARIABLE_FIELD 				= 0x01;
	public static final int FLG_VARIABLE_ENUM_CONSTANT 		= 0x02;
	public static final int FLG_VARIABLE_PARAMETER 			= 0x04;
	public static final int FLG_VARIABLE_EFFECTIVELY_FINAL 	= 0x08;

	static {
		type = StructDef.create(PDOMVariable.class, PDOMBinding.type);
		VARIABLE_FLAGS = type.addShort();
		TYPE = FieldManyToOne.create(type, PDOMTypeSignature.VARIABLES_OF_TYPE);
		VARIABLE_ID = type.addInt();
		DECLARING_METHOD = FieldManyToOne.create(type, PDOMMethod.DECLARED_VARIABLES);
		PARENT = FieldManyToOne.create(type, PDOMBinding.VARIABLES);
		NAME = type.addString();
		CONSTANT = FieldOneToOne.create(type, PDOMConstant.class, PDOMConstant.PARENT_VARIABLE);
		type.done();
	}

	public PDOMVariable(PDOM pdom, long bindingRecord) {
		super(pdom, bindingRecord);
	}

	public PDOMVariable(PDOMBinding parent) {
		super(parent.getPDOM(), parent.getFile());

		PARENT.put(getPDOM(), this.address, parent);
	}

	private boolean hasVariableFlag(int toTest) {
		return (VARIABLE_FLAGS.get(getPDOM(), this.address) & toTest) != 0;
	}

	private void setVariableFlag(int toSet) {
		int newFlags = VARIABLE_FLAGS.get(getPDOM(), this.address) | toSet;
		VARIABLE_FLAGS.put(getPDOM(), this.address, (short)newFlags);
	}

	public void setName(String name) {
		NAME.put(getPDOM(), this.address, name);
	}

	public String getName() {
		return NAME.get(getPDOM(), this.address).getString();
	}

	public void setType(PDOMTypeSignature typeId) {
		TYPE.put(getPDOM(), this.address, typeId);
	}

	public void setConstant(PDOMConstant constant) {
		CONSTANT.put(getPDOM(), this.address, constant);
	}
}
