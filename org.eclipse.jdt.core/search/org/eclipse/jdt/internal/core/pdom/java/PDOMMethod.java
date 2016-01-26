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
import org.eclipse.jdt.internal.core.pdom.field.FieldManyToOne;
import org.eclipse.jdt.internal.core.pdom.field.FieldOneToMany;
import org.eclipse.jdt.internal.core.pdom.field.FieldOneToOne;
import org.eclipse.jdt.internal.core.pdom.field.FieldShort;
import org.eclipse.jdt.internal.core.pdom.field.StructDef;

/**
 * @since 3.12
 */
public class PDOMMethod extends PDOMBinding {
	public static final FieldManyToOne<PDOMMethodId> METHOD_ID;
	public static final FieldShort METHOD_FLAGS;
	public static final FieldManyToOne<PDOMType> PARENT;
	public static final FieldOneToMany<PDOMVariable> DECLARED_VARIABLES;
	public static final FieldOneToMany<PDOMMethodParameter> PARAMETERS;
	public static final FieldOneToOne<PDOMConstant> DEFAULT_VALUE;
	public static final FieldOneToMany<PDOMMethodException> EXCEPTIONS;
	public static final FieldManyToOne<PDOMTypeSignature> RETURN_TYPE;

	@SuppressWarnings("hiding")
	public static final StructDef<PDOMMethod> type;

	static {
		type = StructDef.create(PDOMMethod.class, PDOMBinding.type);
		METHOD_ID = FieldManyToOne.create(type, PDOMMethodId.METHODS);
		METHOD_FLAGS = type.addShort();
		PARENT = FieldManyToOne.create(type, PDOMType.METHODS);
		PARAMETERS = FieldOneToMany.create(type, PDOMMethodParameter.PARENT);
		DECLARED_VARIABLES = FieldOneToMany.create(type, PDOMVariable.DECLARING_METHOD);
		DEFAULT_VALUE = FieldOneToOne.create(type, PDOMConstant.class, PDOMConstant.PARENT_METHOD);
		EXCEPTIONS = FieldOneToMany.create(type, PDOMMethodException.PARENT);
		RETURN_TYPE = FieldManyToOne.create(type, PDOMTypeSignature.USED_AS_RETURN_TYPE);
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
	 * @param record
	 */
	public PDOMMethod(PDOM pdom, long record) {
		super(pdom, record);
	}

	public PDOMMethod(PDOMType parent) {
		super(parent.getPDOM(), parent.getFile());
	}

	public PDOMMethodId getMethodId() {
		return METHOD_ID.get(getPDOM(), this.address);
	}

	public void setDefaultValue(PDOMConstant value) {
		DEFAULT_VALUE.put(getPDOM(), this.address, value);
	}

	public PDOMConstant getDefaultValue() {
		return DEFAULT_VALUE.get(getPDOM(), this.address);
	}

	public void setReturnType(PDOMTypeSignature createTypeSignature) {
		RETURN_TYPE.put(getPDOM(), this.address, createTypeSignature);
	}

	public void setMethodId(PDOMMethodId methodId) {
		METHOD_ID.put(getPDOM(), this.address, methodId);
	}
}
