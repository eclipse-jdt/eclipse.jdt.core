package org.eclipse.jdt.internal.core.nd.java;

import java.util.List;
import java.util.Objects;

import org.eclipse.jdt.internal.core.nd.Nd;
import org.eclipse.jdt.internal.core.nd.db.IString;
import org.eclipse.jdt.internal.core.nd.field.FieldOneToMany;
import org.eclipse.jdt.internal.core.nd.field.FieldSearchKey;
import org.eclipse.jdt.internal.core.nd.field.StructDef;

/**
 * @since 3.12
 */
public class NdTypeId extends NdTypeSignature {
	public static final FieldSearchKey<JavaIndex> FIELD_DESCRIPTOR;
	public static final FieldSearchKey<JavaIndex> SIMPLE_NAME;
	public static final FieldOneToMany<NdType> TYPES;
	public static final FieldOneToMany<NdComplexTypeSignature> USED_AS_COMPLEX_TYPE;
	public static final FieldOneToMany<NdType> DECLARED_TYPES;

	@SuppressWarnings("hiding")
	public static final StructDef<NdTypeId> type;

	private String fName;

	static {
		type = StructDef.create(NdTypeId.class, NdTypeSignature.type);
		FIELD_DESCRIPTOR = FieldSearchKey.create(type, JavaIndex.TYPES);
		SIMPLE_NAME = FieldSearchKey.create(type, JavaIndex.SIMPLE_INDEX);
		TYPES = FieldOneToMany.create(type, NdType.TYPENAME, 2);
		USED_AS_COMPLEX_TYPE = FieldOneToMany.create(type, NdComplexTypeSignature.RAW_TYPE);
		DECLARED_TYPES = FieldOneToMany.create(type, NdType.DECLARING_TYPE);
		type.useStandardRefCounting().done();
	}

	public NdTypeId(Nd pdom, long address) {
		super(pdom, address);
	}

	public NdTypeId(Nd pdom, char[] fieldDescriptor) {
		super(pdom);

		char[] simpleName = JavaNames.fieldDescriptorToJavaName(fieldDescriptor, true);
		FIELD_DESCRIPTOR.put(pdom, this.address, fieldDescriptor);
		SIMPLE_NAME.put(pdom, this.address, simpleName);
	}

	public NdType findTypeByResourceAddress(long resourceAddress) {
		int size = TYPES.size(getNd(), this.address);
		for (int idx = 0; idx < size; idx++) {
			NdType next = TYPES.get(getNd(), this.address, idx);

			if (next.getResourceAddress() == resourceAddress) {
				return next;
			}
		}
		return null;
	}

	public List<NdType> getTypes() {
		return TYPES.asList(getNd(), this.address);
	}

	public IString getFieldDescriptor() {
		return FIELD_DESCRIPTOR.get(getNd(), this.address);
	}

	public char[] getBinaryName() {
		return JavaNames.fieldDescriptorToBinaryName(getFieldDescriptor().getChars());
	}

	public IString getSimpleName() {
		return SIMPLE_NAME.get(getNd(), this.address);
	}

	public char[] getSimpleNameCharArray() {
		if (this.fName == null) {
			this.fName= getSimpleName().getString();
		}
		return this.fName.toCharArray();
	}

	public boolean hasSimpleName(String name) {
		if (this.fName != null)
			return this.fName.equals(name);

		return getSimpleName().equals(name);
	}

	public void setSimpleName(String name) {
		if (Objects.equals(name, this.fName)) {
			return;
		}
		this.fName = name;
		SIMPLE_NAME.put(getNd(), this.address, name);
	}

	@Override
	public NdTypeId getRawType() {
		return this;
	}
}
