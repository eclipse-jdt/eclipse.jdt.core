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

import java.util.List;

import org.eclipse.jdt.internal.core.nd.Nd;
import org.eclipse.jdt.internal.core.nd.field.FieldLong;
import org.eclipse.jdt.internal.core.nd.field.FieldManyToOne;
import org.eclipse.jdt.internal.core.nd.field.FieldOneToMany;
import org.eclipse.jdt.internal.core.nd.field.FieldOneToOne;
import org.eclipse.jdt.internal.core.nd.field.FieldShort;
import org.eclipse.jdt.internal.core.nd.field.StructDef;

/**
 * @since 3.12
 */
public class NdMethod extends NdBinding {
	public static final FieldManyToOne<NdMethodId> METHOD_ID;
	public static final FieldShort METHOD_FLAGS;
	public static final FieldManyToOne<NdType> PARENT;
	public static final FieldOneToMany<NdVariable> DECLARED_VARIABLES;
	public static final FieldOneToMany<NdMethodParameter> PARAMETERS;
	public static final FieldOneToOne<NdConstant> DEFAULT_VALUE;
	public static final FieldOneToMany<NdMethodException> EXCEPTIONS;
	public static final FieldManyToOne<NdTypeSignature> RETURN_TYPE;
	public static final FieldLong TAG_BITS;

	@SuppressWarnings("hiding")
	public static final StructDef<NdMethod> type;

	static {
		type = StructDef.create(NdMethod.class, NdBinding.type);
		METHOD_ID = FieldManyToOne.create(type, NdMethodId.METHODS);
		METHOD_FLAGS = type.addShort();
		PARENT = FieldManyToOne.create(type, NdType.METHODS);
		PARAMETERS = FieldOneToMany.create(type, NdMethodParameter.PARENT);
		DECLARED_VARIABLES = FieldOneToMany.create(type, NdVariable.DECLARING_METHOD);
		DEFAULT_VALUE = FieldOneToOne.create(type, NdConstant.class, NdConstant.PARENT_METHOD);
		EXCEPTIONS = FieldOneToMany.create(type, NdMethodException.PARENT);
		RETURN_TYPE = FieldManyToOne.create(type, NdTypeSignature.USED_AS_RETURN_TYPE);
		TAG_BITS = type.addLong();
		type.done();
	}

	public static final int FLG_GENERIC_SIGNATURE_PRESENT = 0x0001;
	public static final int FLG_THROWS_SIGNATURE_PRESENT = 0x0002;

	public NdMethod(Nd nd, long address) {
		super(nd, address);
	}

	public NdMethod(NdType parent) {
		super(parent.getNd(), parent.getFile());

		PARENT.put(getNd(), this.address, parent);
	}

	public NdMethodId getMethodId() {
		return METHOD_ID.get(getNd(), this.address);
	}

	public char[][] getArgumentNames() {
		List<NdMethodParameter> params = getMethodParameters();

		char[][] result = new char[params.size()][];
		for (int idx = 0; idx < result.length; idx++) {
			result[idx] = params.get(idx).getName().getChars();
		}
		return result;
	}

	public List<NdMethodParameter> getMethodParameters() {
		return PARAMETERS.asList(getNd(), this.address);
	}

	public void setDefaultValue(NdConstant value) {
		DEFAULT_VALUE.put(getNd(), this.address, value);
	}

	public NdConstant getDefaultValue() {
		return DEFAULT_VALUE.get(getNd(), this.address);
	}

	public void setReturnType(NdTypeSignature createTypeSignature) {
		RETURN_TYPE.put(getNd(), this.address, createTypeSignature);
	}

	public void setMethodId(NdMethodId methodId) {
		METHOD_ID.put(getNd(), this.address, methodId);
	}

	public List<NdMethodException> getExceptions() {
		return EXCEPTIONS.asList(getNd(), this.address);
	}

	/**
	 * Returns the return type for this method or null if the method returns void
	 */
	public NdTypeSignature getReturnType() {
		return RETURN_TYPE.get(getNd(), this.address);
	}

	public int getFlags() {
		return METHOD_FLAGS.get(getNd(), this.address);
	}

	public boolean hasAllFlags(int flags) {
		int ourFlags = getFlags();

		return (ourFlags & flags) == flags;
	}

	public void setFlags(int flags) {
		METHOD_FLAGS.put(getNd(), this.address, (short)(getFlags() | flags));
	}

	public void setTagBits(long bits) {
		TAG_BITS.put(getNd(), this.address, bits);
	}

	public long getTagBits() {
		return TAG_BITS.get(getNd(), this.address);
	}
}
