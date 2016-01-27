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

import org.eclipse.jdt.internal.core.nd.Nd;
import org.eclipse.jdt.internal.core.nd.db.Database;
import org.eclipse.jdt.internal.core.nd.db.IString;
import org.eclipse.jdt.internal.core.nd.db.IndexException;
import org.eclipse.jdt.internal.core.nd.field.FieldLong;
import org.eclipse.jdt.internal.core.nd.field.FieldOneToMany;
import org.eclipse.jdt.internal.core.nd.field.FieldSearchKey;
import org.eclipse.jdt.internal.core.nd.field.StructDef;
import org.eclipse.jdt.internal.core.nd.field.FieldSearchIndex.IResultRank;
import org.eclipse.jdt.internal.core.nd.field.FieldSearchIndex.SearchCriteria;

/**
 * Represents a source of java classes (such as a .jar or .class file).
 * @since 3.12
 */
public class NdResourceFile extends NdTreeNode {
	public static final FieldSearchKey<JavaIndex> FILENAME;
	public static final FieldOneToMany<NdBinding> ALL_NODES;
	public static final FieldLong TIME_LAST_SCANNED;
	public static final FieldLong SIZE_LAST_SCANNED;
	public static final FieldLong HASHCODE_LAST_SCANNED;

	@SuppressWarnings("hiding")
	public static final StructDef<NdResourceFile> type;

	static {
		type = StructDef.create(NdResourceFile.class, NdTreeNode.type);
		FILENAME = FieldSearchKey.create(type, JavaIndex.FILES);
		ALL_NODES = FieldOneToMany.create(type, NdBinding.FILE, 16);
		TIME_LAST_SCANNED = type.addLong();
		SIZE_LAST_SCANNED = type.addLong();
		HASHCODE_LAST_SCANNED = type.addLong();
		type.done();
	}

	public NdResourceFile(Nd dom, long address) {
		super(dom, address);
	}

	public NdResourceFile(Nd pdom) {
		super(pdom, null);
	}

	/**
	 * Determines whether this file is still in the index. If a PDOMResourceFile instance is retained while the database
	 * lock is released and reobtained, this method should be invoked to ensure that the PDOMResourceFile has not been
	 * deleted in the meantime.
	 */
	public boolean isInIndex() {
		try {
			Nd pdom = getPDOM();
			// In the common case where the resource file was deleted and the memory hasn't yet been reused,
			// this will fail.
			if (NODE_TYPE.get(pdom, this.address) != pdom.getNodeType(getClass())) {
				return false;
			}

			char[] filename = FILENAME.get(getPDOM(), this.address).getChars();

			NdResourceFile result = JavaIndex.FILES.findBest(pdom, Database.DATA_AREA,
					SearchCriteria.create(filename), new IResultRank() {
						@Override
						public long getRank(Nd testPdom, long testAddress) {
							if (testAddress == NdResourceFile.this.address) {
								return 1;
							}
							return -1;
						}
					});

			return (this.equals(result));
		} catch (IndexException e) {
			// Read errors are expected here. It's possible that the resource file has been deleted and something
			// new was written to this address, in which case we may be reading random gibberish from the database.
			// This is likely to cause an exception.
			return false;
		}
	}

	public IString getFilename() {
		return FILENAME.get(getPDOM(), this.address);
	}

	public void setFilename(String filename) {
		FILENAME.put(getPDOM(), this.address, filename);
	}

	public FileFingerprint getFingerprint() {
		return new FileFingerprint(
				getTimeLastScanned(),
				getSizeLastScanned(),
				getHashcodeLastScanned());
	}

	private long getHashcodeLastScanned() {
		return HASHCODE_LAST_SCANNED.get(getPDOM(), this.address);
	}

	public long getTimeLastScanned() {
		return TIME_LAST_SCANNED.get(getPDOM(), this.address);
	}

	public long getSizeLastScanned() {
		return SIZE_LAST_SCANNED.get(getPDOM(), this.address);
	}

	public void setFingerprint(FileFingerprint newFingerprint) {
		TIME_LAST_SCANNED.put(getPDOM(), this.address, newFingerprint.getTime());
		HASHCODE_LAST_SCANNED.put(getPDOM(), this.address, newFingerprint.getHash());
		SIZE_LAST_SCANNED.put(getPDOM(), this.address, newFingerprint.getSize());
	}
}
