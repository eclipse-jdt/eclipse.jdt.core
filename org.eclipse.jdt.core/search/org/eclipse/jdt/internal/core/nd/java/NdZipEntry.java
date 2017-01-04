package org.eclipse.jdt.internal.core.nd.java;

import org.eclipse.jdt.internal.core.nd.Nd;
import org.eclipse.jdt.internal.core.nd.NdNode;
import org.eclipse.jdt.internal.core.nd.db.IString;
import org.eclipse.jdt.internal.core.nd.field.FieldManyToOne;
import org.eclipse.jdt.internal.core.nd.field.FieldString;
import org.eclipse.jdt.internal.core.nd.field.StructDef;

/**
 * Stores a (non-class) file within a .jar file.
 */
public class NdZipEntry extends NdNode {
	public static final FieldManyToOne<NdResourceFile> JAR_FILE;
	public static final FieldString FILE_NAME;

	@SuppressWarnings("hiding")
	public static final StructDef<NdZipEntry> type;

	static {
		type = StructDef.create(NdZipEntry.class, NdNode.type);
		JAR_FILE = FieldManyToOne.createOwner(type, NdResourceFile.ZIP_ENTRIES);
		FILE_NAME = type.addString();

		type.done();
	}

	public NdZipEntry(Nd nd, long address) {
		super(nd, address);
	}

	public NdZipEntry(NdResourceFile nd, String path) {
		super(nd.getNd());

		JAR_FILE.put(nd.getNd(), getAddress(), nd);
		FILE_NAME.put(nd.getNd(), getAddress(), path);
	}

	public IString getFileName() {
		return FILE_NAME.get(getNd(), getAddress());
	}
}
