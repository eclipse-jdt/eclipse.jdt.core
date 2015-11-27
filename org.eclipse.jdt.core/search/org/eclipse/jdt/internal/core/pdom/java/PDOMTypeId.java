package org.eclipse.jdt.internal.core.pdom.java;

import java.util.List;
import java.util.Objects;

import org.eclipse.jdt.internal.core.pdom.PDOM;
import org.eclipse.jdt.internal.core.pdom.db.IString;
import org.eclipse.jdt.internal.core.pdom.field.FieldOneToMany;
import org.eclipse.jdt.internal.core.pdom.field.FieldSearchKey;
import org.eclipse.jdt.internal.core.pdom.field.StructDef;

public class PDOMTypeId extends PDOMTypeSignature {
	public static final FieldSearchKey<JavaIndex> FIELD_DESCRIPTOR;
	public static final FieldSearchKey<JavaIndex> SIMPLE_NAME;
	public static final FieldOneToMany<PDOMType> TYPES;
	public static final FieldOneToMany<PDOMType> DECLARED_TYPES;
	public static final FieldOneToMany<PDOMComplexTypeSignature> USED_AS_COMPLEX_TYPE;

	@SuppressWarnings("hiding")
	public static final StructDef<PDOMTypeId> type;

	private String fName;

	static {
		type = StructDef.create(PDOMTypeId.class, PDOMTypeSignature.type);
		FIELD_DESCRIPTOR = FieldSearchKey.create(type, JavaIndex.TYPES);
		SIMPLE_NAME = FieldSearchKey.create(type, JavaIndex.SIMPLE_INDEX);
		TYPES = FieldOneToMany.create(type, PDOMType.TYPENAME, 2);
		DECLARED_TYPES = FieldOneToMany.create(type, PDOMType.DECLARING_TYPE);
		USED_AS_COMPLEX_TYPE = FieldOneToMany.create(type, PDOMComplexTypeSignature.RAW_TYPE);
		type.useStandardRefCounting().done();
	}

	public PDOMTypeId(PDOM pdom, long record) {
		super(pdom, record);
	}

	public PDOMTypeId(PDOM pdom, String fieldDescriptor) {
		super(pdom);

		String simpleName = JavaNames.fieldDescriptorToJavaName(fieldDescriptor, true);
		FIELD_DESCRIPTOR.put(pdom, this.address, fieldDescriptor);
		SIMPLE_NAME.put(pdom, this.address, simpleName);
	}

	public PDOMType findTypeByResourceAddress(long resourceAddress) {
		int size = TYPES.size(getPDOM(), this.address);
		for (int idx = 0; idx < size; idx++) {
			PDOMType next = TYPES.get(getPDOM(), this.address, idx);

			if (next.getResourceAddress() == resourceAddress) {
				return next;
			}
		}
		return null;
	}

	public List<PDOMType> getTypes() {
		return TYPES.asList(getPDOM(), this.address);
	}

	public IString getFieldDescriptor() {
		return FIELD_DESCRIPTOR.get(getPDOM(), this.address);
	}

	public String getBinaryName() {
		return JavaNames.fieldDescriptorToBinaryName(getFieldDescriptor().getString());
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
	public PDOMTypeId getRawType() {
		return this;
	}
}
