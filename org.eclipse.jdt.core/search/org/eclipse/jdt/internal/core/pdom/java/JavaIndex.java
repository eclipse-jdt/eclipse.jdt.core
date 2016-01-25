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

import java.io.File;
import java.util.List;

import org.eclipse.core.runtime.IPath;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.internal.core.pdom.PDOM;
import org.eclipse.jdt.internal.core.pdom.PDOMNode;
import org.eclipse.jdt.internal.core.pdom.PDOMNodeTypeRegistry;
import org.eclipse.jdt.internal.core.pdom.db.ChunkCache;
import org.eclipse.jdt.internal.core.pdom.db.Database;
import org.eclipse.jdt.internal.core.pdom.field.FieldSearchIndex;
import org.eclipse.jdt.internal.core.pdom.field.FieldSearchIndex.IResultRank;
import org.eclipse.jdt.internal.core.pdom.field.FieldSearchIndex.SearchCriteria;
import org.eclipse.jdt.internal.core.pdom.field.StructDef;

/**
 * @since 3.12
 */
public class JavaIndex {
	// Version constants
	static final int CURRENT_VERSION = PDOM.version(1, 11);
	static final int MAX_SUPPORTED_VERSION= PDOM.version(1, 11);
	static final int MIN_SUPPORTED_VERSION= PDOM.version(1, 11);

	// Fields for the search header
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
	private static PDOM globalPdom;
	private static final String INDEX_FILENAME = "index.db"; //$NON-NLS-1$
	private final static Object pdomMutex = new Object();

	public JavaIndex(PDOM dom, long address) {
		this.address = address;
		this.pdom = dom;
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

	public PDOMTypeId createTypeId(char[] fieldDescriptor) {
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

	public static boolean isEnabled() {
		return true;
	}

	public static PDOM getGlobalPDOM() {
		PDOM localPdom;
		synchronized (pdomMutex) {
			localPdom = globalPdom;
		}
	
		if (localPdom != null) {
			return localPdom;
		}
	
		localPdom = new PDOM(getDBFile(), ChunkCache.getSharedInstance(), createTypeRegistry(),
				MIN_SUPPORTED_VERSION, MAX_SUPPORTED_VERSION, CURRENT_VERSION);
	
		synchronized (pdomMutex) {
			if (globalPdom == null) {
				globalPdom = localPdom;
			}
			return globalPdom;
		}
	}

	public static JavaIndex getIndex(PDOM pdom) {
		return new JavaIndex(pdom, Database.DATA_AREA);
	}

	public static JavaIndex getIndex() {
		return getIndex(getGlobalPDOM());
	}

	public static int getCurrentVersion() {
		return CURRENT_VERSION;
	}

	static File getDBFile() {
		IPath stateLocation = JavaCore.getPlugin().getStateLocation();
		return stateLocation.append(INDEX_FILENAME).toFile();
	}

	static PDOMNodeTypeRegistry<PDOMNode> createTypeRegistry() {
		PDOMNodeTypeRegistry<PDOMNode> registry = new PDOMNodeTypeRegistry<>();
		registry.register(0x0000, PDOMAnnotation.type.getFactory());
		registry.register(0x0010, PDOMAnnotationValuePair.type.getFactory());
		registry.register(0x0020, PDOMBinding.type.getFactory());
		registry.register(0x0028, PDOMComplexTypeSignature.type.getFactory());
		registry.register(0x0030, PDOMConstant.type.getFactory());
		registry.register(0x0040, PDOMConstantAnnotation.type.getFactory());
		registry.register(0x0050, PDOMConstantArray.type.getFactory());
		registry.register(0x0060, PDOMConstantBoolean.type.getFactory());
		registry.register(0x0070, PDOMConstantByte.type.getFactory());
		registry.register(0x0080, PDOMConstantChar.type.getFactory());
		registry.register(0x0090, PDOMConstantClass.type.getFactory());
		registry.register(0x00A0, PDOMConstantDouble.type.getFactory());
		registry.register(0x00B0, PDOMConstantEnum.type.getFactory());
		registry.register(0x00C0, PDOMConstantFloat.type.getFactory());
		registry.register(0x00D0, PDOMConstantInt.type.getFactory());
		registry.register(0x00E0, PDOMConstantLong.type.getFactory());
		registry.register(0x00F0, PDOMConstantShort.type.getFactory());
		registry.register(0x0100, PDOMConstantString.type.getFactory());
		registry.register(0x0110, PDOMMethod.type.getFactory());
		registry.register(0x0120, PDOMMethodId.type.getFactory());
		registry.register(0x0150, PDOMResourceFile.type.getFactory());
		registry.register(0x0160, PDOMTreeNode.type.getFactory());
		registry.register(0x0170, PDOMType.type.getFactory());
		registry.register(0x0180, PDOMTypeArgument.type.getFactory());
		registry.register(0x0190, PDOMTypeBound.type.getFactory());
		registry.register(0x01A0, PDOMTypeInterface.type.getFactory());
		registry.register(0x01B0, PDOMTypeParameter.type.getFactory());
		registry.register(0x01C0, PDOMTypeSignature.type.getFactory());
		registry.register(0x01D0, PDOMTypeId.type.getFactory());
		registry.register(0x01E0, PDOMTypeInterface.type.getFactory());
		registry.register(0x01F0, PDOMVariable.type.getFactory());
		return registry;
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
