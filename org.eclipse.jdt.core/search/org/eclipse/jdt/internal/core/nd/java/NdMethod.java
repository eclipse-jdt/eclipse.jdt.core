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
		type.done();
	}

	private static final int METHOD_IS_CONSTRUCTOR = 0x0001;
	private static final int METHOD_IS_DEFAULT_CONSTRUCTOR = 0x0002;
	private static final int METHOD_IS_ANNOTATION_MEMBER = 0x0004;
	private static final int METHOD_IS_GENERIC = 0x0008;
	private static final int METHOD_IS_PARAMETERIZED = 0x0010;
	private static final int METHOD_IS_RAW = 0x0020;
	private static final int METHOD_IS_VARARGS = 0x0040;

	/**
	 * @param pdom
	 * @param address
	 */
	public NdMethod(Nd pdom, long address) {
		super(pdom, address);
	}

	public NdMethod(NdType parent) {
		super(parent.getPDOM(), parent.getFile());
	}

	public NdMethodId getMethodId() {
		return METHOD_ID.get(getPDOM(), this.address);
	}

	public void setDefaultValue(NdConstant value) {
		DEFAULT_VALUE.put(getPDOM(), this.address, value);
	}

	public NdConstant getDefaultValue() {
		return DEFAULT_VALUE.get(getPDOM(), this.address);
	}

	public void setReturnType(NdTypeSignature createTypeSignature) {
		RETURN_TYPE.put(getPDOM(), this.address, createTypeSignature);
	}

	public void setMethodId(NdMethodId methodId) {
		METHOD_ID.put(getPDOM(), this.address, methodId);
	}
}
