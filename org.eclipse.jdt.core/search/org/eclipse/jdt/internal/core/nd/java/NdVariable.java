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
package org.eclipse.jdt.internal.core.nd.java;

import org.eclipse.jdt.internal.core.nd.Nd;
import org.eclipse.jdt.internal.core.nd.field.FieldInt;
import org.eclipse.jdt.internal.core.nd.field.FieldManyToOne;
import org.eclipse.jdt.internal.core.nd.field.FieldOneToOne;
import org.eclipse.jdt.internal.core.nd.field.FieldString;
import org.eclipse.jdt.internal.core.nd.field.StructDef;

/**
 * @since 3.12
 */
public class NdVariable extends NdBinding {
	//public static final FieldShort VARIABLE_FLAGS;
	public static final FieldManyToOne<NdTypeSignature> TYPE;
	public static final FieldInt VARIABLE_ID;
	public static final FieldManyToOne<NdMethod> DECLARING_METHOD;
	public static final FieldManyToOne<NdBinding> PARENT;
	public static final FieldString NAME;
	public static final FieldOneToOne<NdConstant> CONSTANT;

	@SuppressWarnings("hiding")
	public static StructDef<NdVariable> type;

//	public static final int FLG_VARIABLE_FIELD 				= 0x01;
//	public static final int FLG_VARIABLE_ENUM_CONSTANT 		= 0x02;
//	public static final int FLG_VARIABLE_PARAMETER 			= 0x04;
//	public static final int FLG_VARIABLE_EFFECTIVELY_FINAL 	= 0x08;

	static {
		type = StructDef.create(NdVariable.class, NdBinding.type);
		//VARIABLE_FLAGS = type.addShort();
		TYPE = FieldManyToOne.create(type, NdTypeSignature.VARIABLES_OF_TYPE);
		VARIABLE_ID = type.addInt();
		DECLARING_METHOD = FieldManyToOne.create(type, NdMethod.DECLARED_VARIABLES);
		PARENT = FieldManyToOne.create(type, NdBinding.VARIABLES);
		NAME = type.addString();
		CONSTANT = FieldOneToOne.create(type, NdConstant.class, NdConstant.PARENT_VARIABLE);
		type.done();
	}

	public NdVariable(Nd pdom, long bindingRecord) {
		super(pdom, bindingRecord);
	}

	public NdVariable(NdBinding parent) {
		super(parent.getPDOM(), parent.getFile());

		PARENT.put(getPDOM(), this.address, parent);
	}
//
//	private boolean hasVariableFlag(int toTest) {
//		return (VARIABLE_FLAGS.get(getPDOM(), this.address) & toTest) != 0;
//	}
//
//	private void setVariableFlag(int toSet) {
//		int newFlags = VARIABLE_FLAGS.get(getPDOM(), this.address) | toSet;
//		VARIABLE_FLAGS.put(getPDOM(), this.address, (short)newFlags);
//	}

	public void setName(char[] name) {
		NAME.put(getPDOM(), this.address, name);
	}

	public String getName() {
		return NAME.get(getPDOM(), this.address).getString();
	}

	public void setType(NdTypeSignature typeId) {
		TYPE.put(getPDOM(), this.address, typeId);
	}

	public void setConstant(NdConstant constant) {
		CONSTANT.put(getPDOM(), this.address, constant);
	}
}
