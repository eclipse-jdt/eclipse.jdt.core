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

import java.io.File;
import java.util.List;

import org.eclipse.core.runtime.IPath;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.internal.core.nd.Nd;
import org.eclipse.jdt.internal.core.nd.NdNode;
import org.eclipse.jdt.internal.core.nd.NdNodeTypeRegistry;
import org.eclipse.jdt.internal.core.nd.db.ChunkCache;
import org.eclipse.jdt.internal.core.nd.db.Database;
import org.eclipse.jdt.internal.core.nd.field.FieldSearchIndex;
import org.eclipse.jdt.internal.core.nd.field.FieldSearchIndex.IResultRank;
import org.eclipse.jdt.internal.core.nd.field.FieldSearchIndex.SearchCriteria;
import org.eclipse.jdt.internal.core.nd.field.StructDef;

/**
 * @since 3.12
 */
public class JavaIndex {
	// Version constants
	static final int CURRENT_VERSION = Nd.version(1, 15);
	static final int MAX_SUPPORTED_VERSION= Nd.version(1, 15);
	static final int MIN_SUPPORTED_VERSION= Nd.version(1, 15);

	// Fields for the search header
	public static final FieldSearchIndex<NdResourceFile> FILES;
	public static final FieldSearchIndex<NdTypeId> SIMPLE_INDEX;
	public static final FieldSearchIndex<NdTypeId> TYPES;
	public static final FieldSearchIndex<NdMethodId> METHODS;

	public static final StructDef<JavaIndex> type;

	static {
		type = StructDef.create(JavaIndex.class);
		FILES = FieldSearchIndex.create(type, NdResourceFile.FILENAME);
		SIMPLE_INDEX = FieldSearchIndex.create(type, NdTypeId.SIMPLE_NAME);
		TYPES = FieldSearchIndex.create(type, NdTypeId.FIELD_DESCRIPTOR);
		METHODS = FieldSearchIndex.create(type, NdMethodId.METHOD_NAME);
		type.done();
	}

	private final static class BestResourceFile implements FieldSearchIndex.IResultRank {
		public BestResourceFile() {
		}

		@Override
		public long getRank(Nd resourceFilePdom, long resourceFileAddress) {
			return NdResourceFile.TIME_LAST_SCANNED.get(resourceFilePdom, resourceFileAddress);
		}
	}

	private static final BestResourceFile bestResourceFile = new BestResourceFile();
	private final long address;
	private Nd pdom;
	private IResultRank anyResult = new IResultRank() {
		@Override
		public long getRank(Nd pdom1, long address1) {
			return 1;
		}
	};
	private static Nd globalPdom;
	private static final String INDEX_FILENAME = "index.db"; //$NON-NLS-1$
	private final static Object pdomMutex = new Object();

	public JavaIndex(Nd dom, long address) {
		this.address = address;
		this.pdom = dom;
	}

	/**
	 * Returns the most-recently-scanned resource file with the given name or null if none
	 */
	public NdResourceFile getResourceFile(String thePath) {
		return FILES.findBest(this.pdom, this.address, FieldSearchIndex.SearchCriteria.create(thePath.toCharArray()),
				bestResourceFile);
	}

	public List<NdResourceFile> getAllResourceFiles(String thePath) {
		return FILES.findAll(this.pdom, this.address, FieldSearchIndex.SearchCriteria.create(thePath.toCharArray()));
	}

	public NdTypeId findType(char[] fieldDescriptor) {
		SearchCriteria searchCriteria = SearchCriteria.create(fieldDescriptor);
		return TYPES.findBest(this.pdom, this.address, searchCriteria, this.anyResult);
	}

	/**
	 * Returns a type ID or creates a new one if it does not exist. The caller must
	 * attach a reference to it after calling this method or it may leak.
	 */
	public NdTypeId createTypeId(char[] fieldDescriptor) {
		NdTypeId existingType = findType(fieldDescriptor);

		if (existingType != null) {
			return existingType;
		}

		return new NdTypeId(this.pdom, fieldDescriptor);
	}

	public Nd getPDOM() {
		return this.pdom;
	}

	public NdMethodId findMethodId(char[] methodId) {
		SearchCriteria searchCriteria = SearchCriteria.create(methodId);

		return METHODS.findBest(this.pdom, this.address, searchCriteria, this.anyResult);
	}

	public NdMethodId createMethodId(char[] methodId) {
		NdMethodId existingMethod = findMethodId(methodId);

		if (existingMethod != null) {
			return existingMethod;
		}

		return new NdMethodId(this.pdom, methodId);
	}

	public static boolean isEnabled() {
		return true;
	}

	public static Nd getGlobalPDOM() {
		Nd localPdom;
		synchronized (pdomMutex) {
			localPdom = globalPdom;
		}

		if (localPdom != null) {
			return localPdom;
		}

		localPdom = new Nd(getDBFile(), ChunkCache.getSharedInstance(), createTypeRegistry(),
				MIN_SUPPORTED_VERSION, MAX_SUPPORTED_VERSION, CURRENT_VERSION);

		synchronized (pdomMutex) {
			if (globalPdom == null) {
				globalPdom = localPdom;
			}
			return globalPdom;
		}
	}

	public static JavaIndex getIndex(Nd pdom) {
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

	static NdNodeTypeRegistry<NdNode> createTypeRegistry() {
		NdNodeTypeRegistry<NdNode> registry = new NdNodeTypeRegistry<>();
		registry.register(0x0000, NdAnnotation.type.getFactory());
		registry.register(0x0010, NdAnnotationValuePair.type.getFactory());
		registry.register(0x0020, NdBinding.type.getFactory());
		registry.register(0x0028, NdComplexTypeSignature.type.getFactory());
		registry.register(0x0030, NdConstant.type.getFactory());
		registry.register(0x0040, NdConstantAnnotation.type.getFactory());
		registry.register(0x0050, NdConstantArray.type.getFactory());
		registry.register(0x0060, NdConstantBoolean.type.getFactory());
		registry.register(0x0070, NdConstantByte.type.getFactory());
		registry.register(0x0080, NdConstantChar.type.getFactory());
		registry.register(0x0090, NdConstantClass.type.getFactory());
		registry.register(0x00A0, NdConstantDouble.type.getFactory());
		registry.register(0x00B0, NdConstantEnum.type.getFactory());
		registry.register(0x00C0, NdConstantFloat.type.getFactory());
		registry.register(0x00D0, NdConstantInt.type.getFactory());
		registry.register(0x00E0, NdConstantLong.type.getFactory());
		registry.register(0x00F0, NdConstantShort.type.getFactory());
		registry.register(0x0100, NdConstantString.type.getFactory());
		registry.register(0x0110, NdMethod.type.getFactory());
		registry.register(0x0120, NdMethodException.type.getFactory());
		registry.register(0x0130, NdMethodId.type.getFactory());
		registry.register(0x0140, NdMethodParameter.type.getFactory());
		registry.register(0x0150, NdResourceFile.type.getFactory());
		registry.register(0x0160, NdTreeNode.type.getFactory());
		registry.register(0x0170, NdType.type.getFactory());
		registry.register(0x0180, NdTypeArgument.type.getFactory());
		registry.register(0x0190, NdTypeBound.type.getFactory());
		registry.register(0x01A0, NdTypeInterface.type.getFactory());
		registry.register(0x01B0, NdTypeParameter.type.getFactory());
		registry.register(0x01C0, NdTypeSignature.type.getFactory());
		registry.register(0x01D0, NdTypeId.type.getFactory());
		registry.register(0x01E0, NdTypeInterface.type.getFactory());
		registry.register(0x01F0, NdVariable.type.getFactory());
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
