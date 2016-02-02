package org.eclipse.jdt.internal.core.nd.java;

import java.util.List;

import org.eclipse.jdt.internal.core.nd.DatabaseRef;
import org.eclipse.jdt.internal.core.nd.Nd;
import org.eclipse.jdt.internal.core.nd.Supplier;

/**
 * Holds utility functions for constructing {@link DatabaseRef} objects to java Nd types. Such references
 * can be retained while releasing and reacquiring a read lock, unlike ordinary pointers to database objects,
 * which must be discarded every time a read lock is released.
 */
public final class ReferenceUtil {
	/**
	 * Creates a {@link DatabaseRef} to the given {@link NdType}.
	 */
	public static DatabaseRef<NdType> createTypeRef(NdType type) {
		final Nd nd = type.getNd();
		final char[] fieldDescriptor = type.getTypeId().getRawType().getFieldDescriptor().getChars();
		final char[] fileName = type.getResourceFile().getFilename().getChars();
		return new DatabaseRef<NdType>(type.getNd(), getTypeSupplier(nd, fileName, fieldDescriptor), type);
	}

	/**
	 * Creates a {@link DatabaseRef} to the {@link NdType} with the given resource path and field descriptor.
	 */
	public static DatabaseRef<NdType> createTypeRef(Nd nd, char[] resourcePath, char[] fieldDescriptor) {
		return new DatabaseRef<NdType>(nd, getTypeSupplier(nd, resourcePath, fieldDescriptor));
	}

	/**
	 * Creates a {@link DatabaseRef} to the given {@link NdTypeId}.
	 */
	public static DatabaseRef<NdTypeId> createTypeIdRef(NdTypeId typeId) {
		return new DatabaseRef<NdTypeId>(typeId.getNd(),
				getTypeIdSupplier(typeId.getNd(), typeId.getFieldDescriptor().getChars()));
	}

	/**
	 * Creates a {@link DatabaseRef} to the {@link NdTypeId} with the given field descriptor.
	 */
	public static DatabaseRef<NdTypeId> createTypeIdRef(Nd nd, char[] fieldDescriptor) {
		return new DatabaseRef<NdTypeId>(nd, getTypeIdSupplier(nd, fieldDescriptor));
	}

	private static Supplier<NdTypeId> getTypeIdSupplier(final Nd nd, final char[] fieldDescriptor) {
		return new Supplier<NdTypeId>() {
			@Override
			public NdTypeId get() {
				return JavaIndex.getIndex(nd).findType(fieldDescriptor);
			}
		};
	}

	private static Supplier<NdType> getTypeSupplier(final Nd nd, final char[] fileName, final char[] fieldDescriptor) {
		return new Supplier<NdType>() {
			@Override
			public NdType get() {
				NdTypeId typeId = JavaIndex.getIndex(nd).findType(fieldDescriptor);

				List<NdType> implementations = typeId.getTypes();
				for (NdType next : implementations) {
					if (next.getResourceFile().getFilename().compare(fileName, false) == 0) {
						return next;
					}
				}
				return null;
			}
		};
	}
}
