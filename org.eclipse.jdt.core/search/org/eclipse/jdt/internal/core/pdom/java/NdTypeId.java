package org.eclipse.jdt.internal.core.pdom.java;

import java.util.List;
import java.util.Objects;

import org.eclipse.jdt.internal.core.pdom.Nd;
import org.eclipse.jdt.internal.core.pdom.db.IString;
import org.eclipse.jdt.internal.core.pdom.field.FieldOneToMany;
import org.eclipse.jdt.internal.core.pdom.field.FieldSearchKey;
import org.eclipse.jdt.internal.core.pdom.field.StructDef;

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

	public NdTypeId(Nd pdom, long record) {
		super(pdom, record);
	}

	public NdTypeId(Nd pdom, char[] fieldDescriptor) {
		super(pdom);

		char[] simpleName = JavaNames.fieldDescriptorToJavaName(fieldDescriptor, true);
		FIELD_DESCRIPTOR.put(pdom, this.address, fieldDescriptor);
		SIMPLE_NAME.put(pdom, this.address, simpleName);
	}

	public NdType findTypeByResourceAddress(long resourceAddress) {
		int size = TYPES.size(getPDOM(), this.address);
		for (int idx = 0; idx < size; idx++) {
			NdType next = TYPES.get(getPDOM(), this.address, idx);

			if (next.getResourceAddress() == resourceAddress) {
				return next;
			}
		}
		return null;
	}

	public List<NdType> getTypes() {
		return TYPES.asList(getPDOM(), this.address);
	}

	public IString getFieldDescriptor() {
		return FIELD_DESCRIPTOR.get(getPDOM(), this.address);
	}

	public char[] getBinaryName() {
		return JavaNames.fieldDescriptorToBinaryName(getFieldDescriptor().getChars());
	}

	public IString getSimpleName() {
		return SIMPLE_NAME.get(getPDOM(), this.address);
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
		SIMPLE_NAME.put(getPDOM(), this.address, name);
	}

	@Override
	public NdTypeId getRawType() {
		return this;
	}
}
