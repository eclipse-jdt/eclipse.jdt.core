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
import org.eclipse.jdt.internal.core.pdom.field.FieldOneToMany;
import org.eclipse.jdt.internal.core.pdom.field.FieldManyToOne;
import org.eclipse.jdt.internal.core.pdom.field.FieldShort;
import org.eclipse.jdt.internal.core.pdom.field.StructDef;

/**
 * @since 3.12
 */
public class PDOMMethod extends PDOMBinding {
	public static final FieldManyToOne<PDOMMethodId> METHOD_ID;
	public static final FieldShort METHOD_FLAGS;
//	public static final FieldManyToOne<PDOMBinding> DECLARING_MEMBER;
	public static final FieldOneToMany<PDOMVariable> DECLARED_VARIABLES;
	// TODO(sxenos): specify the default type for annotation paramaters
	// (see {@link IMethodBinding#getDefaultValue()}
	
	@SuppressWarnings("hiding")
	public static final StructDef<PDOMMethod> type;

	static {
		type = StructDef.create(PDOMMethod.class, PDOMBinding.type);
		METHOD_ID = FieldManyToOne.create(type, PDOMMethodId.METHODS);
		METHOD_FLAGS = type.addShort();
//		DECLARING_MEMBER = FieldManyToOne.create(type, PDOMBinding.DECLARED_LAMBDAS);
		DECLARED_VARIABLES = FieldOneToMany.create(type, PDOMVariable.DECLARING_METHOD);
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

	public PDOMMethodId getMethodId() {
		return METHOD_ID.get(getPDOM(), this.address);
	}
}
