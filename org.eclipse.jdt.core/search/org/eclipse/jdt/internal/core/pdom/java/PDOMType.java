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

import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.internal.core.pdom.IPDOMVisitor;
import org.eclipse.jdt.internal.core.pdom.PDOM;
import org.eclipse.jdt.internal.core.pdom.field.FieldManyToOne;
import org.eclipse.jdt.internal.core.pdom.field.FieldOneToMany;
import org.eclipse.jdt.internal.core.pdom.field.StructDef;

import java.util.List;

/**
 * @since 3.12
 */
public class PDOMType extends PDOMBinding {
	public static final FieldManyToOne<PDOMTypeId> TYPENAME;
	public static final FieldManyToOne<PDOMTypeSignature> SUPERCLASS;
	public static final FieldOneToMany<PDOMTypeInterface> INTERFACES;
	public static final FieldManyToOne<PDOMTypeId> DECLARING_TYPE;
	public static final FieldManyToOne<PDOMMethodId> DECLARING_METHOD;
	public static final FieldOneToMany<PDOMMethod> METHODS;

	@SuppressWarnings("hiding")
	public static final StructDef<PDOMType> type;

	static {
		type = StructDef.create(PDOMType.class, PDOMBinding.type);
		TYPENAME = FieldManyToOne.create(type, PDOMTypeId.TYPES);
		DECLARING_TYPE = FieldManyToOne.create(type, PDOMTypeId.DECLARED_TYPES);
		INTERFACES = FieldOneToMany.create(type, PDOMTypeInterface.APPLIES_TO);
		SUPERCLASS = FieldManyToOne.create(type, PDOMTypeSignature.SUBCLASSES);
		DECLARING_METHOD = FieldManyToOne.create(type, PDOMMethodId.DECLARED_TYPES);
		METHODS = FieldOneToMany.create(type, PDOMMethod.PARENT, 6);
		type.done();
	}

	public static final long FLG_TYPE_ANNOTATION 	= 0x00000001;
	public static final long FLG_TYPE_ANONYMOUS 	= 0x00000002;
	public static final long FLG_TYPE_ARRAY 		= 0x00000004;
	public static final long FLG_TYPE_CAPTURE 		= 0x00000008;
	public static final long FLG_TYPE_CLASS 		= 0x00000010;
	public static final long FLG_TYPE_ENUM 			= 0x00000020;
	public static final long FLG_TYPE_FROM_SOURCE 	= 0x00000040;
	public static final long FLG_TYPE_GENERIC 		= 0x00000080;
	public static final long FLG_TYPE_INTERFACE 	= 0x00000100;
	public static final long FLG_TYPE_LOCAL 		= 0x00000200;
	public static final long FLG_TYPE_MEMBER 		= 0x00000400;
	public static final long FLG_TYPE_NESTED 		= 0x00000800;
	public static final long FLG_TYPE_NULLTYPE 		= 0x00001000;
	public static final long FLG_TYPE_PARAMETERIZED = 0x00002000;
	public static final long FLG_TYPE_PRIMITIVE 	= 0x00004000;
	public static final long FLG_TYPE_RAW			= 0x00008000;
	public static final long FLG_TYPE_TOP_LEVEL 	= 0x00010000;
	public static final long FLG_TYPE_VARIABLE 		= 0x00020000;
	public static final long FLG_TYPE_UPPER_BOUND 	= 0x00040000;
	public static final long FLG_TYPE_WILDCARD 		= 0x00080000;

	public PDOMType(PDOM pdom, long address) {
		super(pdom, address);
	}

	public PDOMType(PDOM pdom, PDOMResourceFile resource) {
		super(pdom, resource);
	}

	@Override
	public void accept(IPDOMVisitor visitor) {
		//PDOMJavaClassScope.acceptViaCache(this, visitor, false);
	}

	/**
	 * Called to populate the cache for the bindings in the class scope.
	 */
	public void acceptUncached(IPDOMVisitor visitor) throws CoreException {
		super.accept(visitor);
//		PDOMRawLinkedList list = new PDOMRawLinkedList(getPDOM(), this.record + MEMBERLIST);
//		list.accept(visitor);
	}

	public PDOMTypeId getTypeId() {
		return TYPENAME.get(getPDOM(), this.address);
	}

	public void setTypeId(PDOMTypeId typeId) {
		TYPENAME.put(getPDOM(), this.address, typeId);
	}

	public long getResourceAddress() {
		return FILE.getAddress(getPDOM(), this.address);
	}

	public void setSuperclass(PDOMTypeSignature superclassTypeName) {
		SUPERCLASS.put(getPDOM(), this.address, superclassTypeName);
	}

	public PDOMTypeSignature getSuperclass() {
		return SUPERCLASS.get(getPDOM(), this.address);
	}

	public List<PDOMTypeInterface> getInterfaces() {
		return INTERFACES.asList(getPDOM(), this.address);
	}

	public PDOMResourceFile getResourceFile() {
		return FILE.get(getPDOM(), this.address);
	}

	public void setDeclaringMethod(PDOMMethodId createMethodId) {
		DECLARING_METHOD.put(getPDOM(), this.address, createMethodId);
	}

	/**
	 * @param createTypeIdFromBinaryName
	 */
	public void setDeclaringType(PDOMTypeId createTypeIdFromBinaryName) {
		DECLARING_TYPE.put(getPDOM(), this.address, createTypeIdFromBinaryName);
	}

	public PDOMTypeId getDeclaringType() {
		return DECLARING_TYPE.get(getPDOM(), this.address);
	}
}
