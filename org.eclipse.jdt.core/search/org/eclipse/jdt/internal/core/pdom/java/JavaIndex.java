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

import java.util.List;

import org.eclipse.jdt.internal.core.pdom.PDOM;
import org.eclipse.jdt.internal.core.pdom.db.Database;
import org.eclipse.jdt.internal.core.pdom.field.FieldSearchIndex;
import org.eclipse.jdt.internal.core.pdom.field.FieldSearchIndex.IResultRank;
import org.eclipse.jdt.internal.core.pdom.field.FieldSearchIndex.SearchCriteria;
import org.eclipse.jdt.internal.core.pdom.field.StructDef;

/**
 * @since 3.12
 */
public class JavaIndex {
	public static final FieldSearchIndex<PDOMResourceFile> FILES;
	public static final FieldSearchIndex<PDOMTypeId> SIMPLE_INDEX;
	public static final FieldSearchIndex<PDOMTypeId> TYPES;
	public static final FieldSearchIndex<PDOMMethodId> METHODS;

	public static final StructDef<JavaIndex> type;

	static {
		type = StructDef.create(JavaIndex.class);
		FILES = FieldSearchIndex.create(type, PDOMResourceFile.FILENAME);
		SIMPLE_INDEX = FieldSearchIndex.create(type, PDOMTypeId.SIMPLE_NAME);
		TYPES = FieldSearchIndex.create(type, PDOMTypeId.FIELD_DESCRIPTOR);
		METHODS = FieldSearchIndex.create(type, PDOMMethodId.METHOD_NAME);
		type.done();
	}

	private final static class BestResourceFile implements FieldSearchIndex.IResultRank {
		public BestResourceFile() {
		}

		@Override
		public long getRank(PDOM resourceFilePdom, long resourceFileAddress) {
			return PDOMResourceFile.TIME_LAST_SCANNED.get(resourceFilePdom, resourceFileAddress);
		}
	}

	private static final BestResourceFile bestResourceFile = new BestResourceFile();
	private final long address;
	private PDOM pdom;
	private IResultRank anyResult = new IResultRank() {
		@Override
		public long getRank(PDOM pdom1, long address1) {
			return 1;
		}
	};

	public JavaIndex(PDOM dom, long address) {
		this.address = address;
		this.pdom = dom;
	}

	public static JavaIndex getIndex(PDOM pdom) {
		return new JavaIndex(pdom, Database.DATA_AREA);
	}

	/**
	 * Returns the most-recently-scanned resource file with the given name or null if none
	 */
	public PDOMResourceFile getResourceFile(String thePath) {
		return FILES.findBest(this.pdom, this.address, FieldSearchIndex.SearchCriteria.create(thePath.toCharArray()),
				bestResourceFile);
	}

	public List<PDOMResourceFile> getAllResourceFiles(String thePath) {
		return FILES.findAll(this.pdom, this.address, FieldSearchIndex.SearchCriteria.create(thePath.toCharArray()));
	}

	public PDOMTypeId findType(String fieldDescriptor) {
		SearchCriteria searchCriteria = SearchCriteria.create(fieldDescriptor);
		return TYPES.findBest(this.pdom, this.address, searchCriteria, this.anyResult);
	}

	public PDOMTypeSignature createTypeId(char[] fieldDescriptor) {
		return createTypeId(new String(fieldDescriptor));
	}
	
	/**
	 * Returns a type ID or creates a new one if it does not exist. The caller must
	 * attach a reference to it after calling this method or it may leak.
	 */
	public PDOMTypeId createTypeId(String fieldDescriptor) {
		PDOMTypeId existingType = findType(fieldDescriptor);

		if (existingType != null) {
			return existingType;
		}

		return new PDOMTypeId(this.pdom, fieldDescriptor);
	}

	public PDOM getPDOM() {
		return this.pdom;
	}

	public PDOMMethodId findMethodId(String methodId) {
		SearchCriteria searchCriteria = SearchCriteria.create(methodId);

		return METHODS.findBest(this.pdom, this.address, searchCriteria, this.anyResult);
	}

	public PDOMMethodId createMethodId(String methodId) {
		PDOMMethodId existingMethod = findMethodId(methodId);

		if (existingMethod != null) {
			return existingMethod;
		}

		return new PDOMMethodId(this.pdom, methodId);
	}

//	/**
//	 * Returns a method ID or creates a new one if it does not exist. The caller must
//	 * attach a reference to it after calling this method or it may leak.
//	 */
//	public PDOMMethodId createMethodId(String binaryName) {
//		PDOMMethodId existingType = findByName(binaryName, PDOMMethodId.class);
//
//		if (existingType != null) {
//			return existingType;
//		}
//		return new PDOMMethodId(getPDOM(), new String(binaryName));
//	}
//
//	public PDOMMethodId createMethodId(char[] binaryName) {
//		return createMethodId(new String(binaryName));
//	}

//
//	public PDOMPackageId createPackageId(char[] packageName) {
//		PDOMPackageId pkg = findNamedNode(null, packageName, PDOMPackageId.class);
//
//		if (pkg == null) {
//			pkg = new PDOMPackageId(this.pdom, packageName);
//		}
//
//		return pkg;
//	}
}
