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

import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.internal.core.nd.IPDOMVisitor;
import org.eclipse.jdt.internal.core.nd.Nd;
import org.eclipse.jdt.internal.core.nd.db.IString;
import org.eclipse.jdt.internal.core.nd.db.IndexException;
import org.eclipse.jdt.internal.core.nd.field.FieldByte;
import org.eclipse.jdt.internal.core.nd.field.FieldLong;
import org.eclipse.jdt.internal.core.nd.field.FieldManyToOne;
import org.eclipse.jdt.internal.core.nd.field.FieldOneToMany;
import org.eclipse.jdt.internal.core.nd.field.FieldString;
import org.eclipse.jdt.internal.core.nd.field.StructDef;
import org.eclipse.jdt.internal.core.nd.util.CharArrayUtils;

/**
 * @since 3.12
 */
public class NdType extends NdBinding {
	public static final FieldManyToOne<NdTypeId> TYPENAME;
	public static final FieldManyToOne<NdTypeSignature> SUPERCLASS;
	public static final FieldOneToMany<NdTypeInterface> INTERFACES;
	public static final FieldManyToOne<NdTypeId> DECLARING_TYPE;
	public static final FieldManyToOne<NdMethodId> DECLARING_METHOD;
	public static final FieldOneToMany<NdMethod> METHODS;
	public static final FieldString MISSING_TYPE_NAMES;
	public static final FieldString SOURCE_FILE_NAME;
	public static final FieldString INNER_CLASS_SOURCE_NAME;
	public static final FieldByte FLAGS;
	public static final FieldLong TAG_BITS;

	@SuppressWarnings("hiding")
	public static final StructDef<NdType> type;

	static {
		type = StructDef.create(NdType.class, NdBinding.type);
		TYPENAME = FieldManyToOne.create(type, NdTypeId.TYPES);
		DECLARING_TYPE = FieldManyToOne.create(type, NdTypeId.DECLARED_TYPES);
		INTERFACES = FieldOneToMany.create(type, NdTypeInterface.APPLIES_TO);
		SUPERCLASS = FieldManyToOne.create(type, NdTypeSignature.SUBCLASSES);
		DECLARING_METHOD = FieldManyToOne.create(type, NdMethodId.DECLARED_TYPES);
		METHODS = FieldOneToMany.create(type, NdMethod.PARENT, 6);
		MISSING_TYPE_NAMES = type.addString();
		SOURCE_FILE_NAME = type.addString();
		INNER_CLASS_SOURCE_NAME = type.addString();
		FLAGS = type.addByte();
		TAG_BITS = type.addLong();
		type.done();
	}

	public static final byte FLG_TYPE_ANONYMOUS 	= 0x0001;
	public static final byte FLG_TYPE_LOCAL 		= 0x0002;
	public static final byte FLG_TYPE_MEMBER 		= 0x0004;

	public NdType(Nd pdom, long address) {
		super(pdom, address);
	}

	public NdType(Nd pdom, NdResourceFile resource) {
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
//		PDOMRawLinkedList list = new PDOMRawLinkedList(getNd(), this.address + MEMBERLIST);
//		list.accept(visitor);
	}

	public NdTypeId getTypeId() {
		return TYPENAME.get(getNd(), this.address);
	}

	public void setTypeId(NdTypeId typeId) {
		TYPENAME.put(getNd(), this.address, typeId);
	}

	/**
	 * Sets the source name for this type.
	 */
	public void setSourceNameOverride(char[] sourceName) {
		char[] oldSourceName = getSourceName();
		if (!CharArrayUtils.equals(oldSourceName, sourceName)) {
			INNER_CLASS_SOURCE_NAME.put(getNd(), this.address, sourceName);
		}
	}

	public IString getSourceNameOverride() {
		return INNER_CLASS_SOURCE_NAME.get(getNd(), this.address);
	}

	public long getResourceAddress() {
		return FILE.getAddress(getNd(), this.address);
	}

	public void setSuperclass(NdTypeSignature superclassTypeName) {
		SUPERCLASS.put(getNd(), this.address, superclassTypeName);
	}

	public NdTypeSignature getSuperclass() {
		return SUPERCLASS.get(getNd(), this.address);
	}

	public List<NdTypeInterface> getInterfaces() {
		return INTERFACES.asList(getNd(), this.address);
	}

	public NdResourceFile getResourceFile() {
		return FILE.get(getNd(), this.address);
	}

	public void setDeclaringMethod(NdMethodId createMethodId) {
		DECLARING_METHOD.put(getNd(), this.address, createMethodId);
	}

	/**
	 * @param createTypeIdFromBinaryName
	 */
	public void setDeclaringType(NdTypeId createTypeIdFromBinaryName) {
		DECLARING_TYPE.put(getNd(), this.address, createTypeIdFromBinaryName);
	}

	public NdTypeId getDeclaringType() {
		return DECLARING_TYPE.get(getNd(), this.address);
	}

	/**
	 * Sets the missing type names (if any) for this class. The names are encoded in a comma-separated list.
	 */
	public void setMissingTypeNames(char[] contents) {
		MISSING_TYPE_NAMES.put(getNd(), this.address, contents);
	}

	/**
	 * Returns the missing type names as a comma-separated list
	 */
	public IString getMissingTypeNames() {
		return MISSING_TYPE_NAMES.get(getNd(), this.address);
	}

	public void setSourceFileName(char[] sourceFileName) {
		SOURCE_FILE_NAME.put(getNd(), this.address, sourceFileName);
	}

	public IString getSourceFileName() {
		return SOURCE_FILE_NAME.get(getNd(), this.address);
	}

	public void setAnonymous(boolean anonymous) {
		setFlag(FLG_TYPE_ANONYMOUS, anonymous);
	}

	public void setIsLocal(boolean local) {
		setFlag(FLG_TYPE_LOCAL, local);
	}

	public void setIsMember(boolean member) {
		setFlag(FLG_TYPE_MEMBER, member);
	}

	public boolean isAnonymous() {
		return getFlag(FLG_TYPE_ANONYMOUS);
	}

	public boolean isLocal() {
		return getFlag(FLG_TYPE_LOCAL);
	}

	public boolean isMember() {
		return getFlag(FLG_TYPE_MEMBER);
	}

	private void setFlag(byte flagConstant, boolean value) {
		int oldFlags = FLAGS.get(getNd(), this.address);
		int newFlags =  ((oldFlags & ~flagConstant) | (value ? flagConstant : 0));
		FLAGS.put(getNd(), this.address, (byte)newFlags);
	}

	private boolean getFlag(byte flagConstant) {
		return (FLAGS.get(getNd(), this.address) & flagConstant) != 0;
	}

	public char[] getSourceName() {
		IString sourceName = getSourceNameOverride();
		if (sourceName.length() != 0) {
			return sourceName.getChars();
		}
		char[] simpleName = getTypeId().getSimpleNameCharArray();
		return JavaNames.simpleNameToSourceName(simpleName);
	}

	public NdMethodId getDeclaringMethod() {
		return DECLARING_METHOD.get(getNd(), this.address);
	}

	@Override
	public List<NdTypeParameter> getTypeParameters() {
		return TYPE_PARAMETERS.asList(getNd(), this.address);
	}

	public List<NdMethod> getMethods() {
		return METHODS.asList(getNd(), this.address);
	}

	@Override
	public String toString() {
		try {
			return "class " + new String(getSourceName()); //$NON-NLS-1$
		} catch (IndexException e) {
			return super.toString();
		}
	}

	public void setTagBits(long tagBits) {
		TAG_BITS.put(getNd(), this.address, tagBits);
	}

	public long getTagBits() {
		return TAG_BITS.get(getNd(), this.address);
	}
}
