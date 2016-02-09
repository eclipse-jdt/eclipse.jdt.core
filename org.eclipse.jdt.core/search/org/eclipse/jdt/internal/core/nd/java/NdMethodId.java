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
import org.eclipse.jdt.internal.core.nd.NdNode;
import org.eclipse.jdt.internal.core.nd.db.IString;
import org.eclipse.jdt.internal.core.nd.field.FieldOneToMany;
import org.eclipse.jdt.internal.core.nd.field.FieldSearchKey;
import org.eclipse.jdt.internal.core.nd.field.StructDef;
import org.eclipse.jdt.internal.core.nd.util.CharArrayUtils;

/**
 * Represents the fully-qualified signature a method. Holds back-pointers to all the entities that refer to the name,
 * along with pointers to all methods that have this fully-qualified name. Note that this isn't the class declaration
 * itself. If there are multiple jar files containing a class of the same fully-qualified name, there may also be
 * multiple methods with the same method ID.
 * @since 3.12
 */
public class NdMethodId extends NdNode {
	public static final FieldSearchKey<JavaIndex> METHOD_NAME;
	public static final FieldOneToMany<NdMethod> METHODS;
//	public static final FieldOneToMany<PDOMType> USED_AS_FUNCTIONAL_INTERFACE_METHOD;
	public static final FieldOneToMany<NdType> DECLARED_TYPES;

	@SuppressWarnings("hiding")
	public static final StructDef<NdMethodId> type;

	static {
		type = StructDef.create(NdMethodId.class, NdNode.type);
		METHOD_NAME = FieldSearchKey.create(type, JavaIndex.METHODS);
		METHODS = FieldOneToMany.create(type, NdMethod.METHOD_ID, 2);
//		USED_AS_FUNCTIONAL_INTERFACE_METHOD = FieldOneToMany.create(type, PDOMType.class,
//				PDOMType.FUNCTIONAL_INTERFACE_METHOD, 0);
		DECLARED_TYPES = FieldOneToMany.create(type, NdType.DECLARING_METHOD);

		type.useStandardRefCounting().done();
	}

	public NdMethodId(Nd pdom, long address) {
		super(pdom, address);
	}

	/**
	 *
	 * @param pdom
	 * @param methodIdentifier a field descriptor for the method type followed by a "#" followed by a method selector
	 *  followed by method descriptor. For example, "Lorg/eclipse/MyClass#foo()Ljava/lang/Object;V"
	 */
	public NdMethodId(Nd pdom, char[] methodIdentifier) {
		super(pdom);

		METHOD_NAME.put(pdom, this.address, methodIdentifier);
	}

	public List<NdType> getDeclaredTypes() {
		return DECLARED_TYPES.asList(getNd(), this.address);
	}

	/**
	 * Returns the field descriptor for the type followed by a # followed by the method selector
	 * followed by the method descriptor. For example, "Lorg/eclipse/MyClass#foo()Ljava/lang/Object;V"
	 */
	public IString getMethodName() {
		return METHOD_NAME.get(getNd(), this.address);
	}

	public char[] getSelector() {
		char[] name = getMethodName().getChars();
		int selectorStart = CharArrayUtils.indexOf('#', name) + 1;
		int selectorEnd = CharArrayUtils.indexOf('(', name, selectorStart, name.length);
		if (selectorEnd == -1) {
			selectorEnd = name.length;
		}
		return CharArrayUtils.subarray(name, selectorStart, selectorEnd);
	}

	public boolean isConstructor() {
		return JavaNames.isConstructor(getSelector());
	}

	public char[] getMethodDescriptor() {
		char[] name = getMethodName().getChars();
		int descriptorStart = CharArrayUtils.indexOf('(', name, 0, name.length);
		return CharArrayUtils.subarray(name, descriptorStart, name.length);
	}

	public boolean isClInit() {
		return JavaNames.isClinit(getSelector());
	}
}
