package org.eclipse.jdt.internal.core.pdom.java;

import java.io.File;

import org.eclipse.core.runtime.IPath;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.internal.core.pdom.PDOM;
import org.eclipse.jdt.internal.core.pdom.PDOMNode;
import org.eclipse.jdt.internal.core.pdom.PDOMNodeTypeRegistry;
import org.eclipse.jdt.internal.core.pdom.db.ChunkCache;

/**
 * @since 3.12
 */
public class JavaPDOM {
	private static final int MIN_SUPPORTED_VERSION= PDOM.version(1, 6);
	private static final int MAX_SUPPORTED_VERSION= PDOM.version(1, Short.MAX_VALUE);
	private static final int CURRENT_VERSION = PDOM.version(1, 6);

	private static final String INDEX_FILENAME = "index.db"; //$NON-NLS-1$
	private final static Object pdomMutex = new Object();
	private static PDOM pdom;

	public static boolean isEnabled() {
		return true;
	}

	public static PDOM getPDOM() {
		PDOM localPdom;
		synchronized (pdomMutex) {
			localPdom = pdom;
		}

		if (localPdom != null) {
			return localPdom;
		}

		localPdom = new PDOM(getDBFile(), ChunkCache.getSharedInstance(), createTypeRegistry(),
				MIN_SUPPORTED_VERSION, MAX_SUPPORTED_VERSION, CURRENT_VERSION);

		synchronized (pdomMutex) {
			if (pdom == null) {
				pdom = localPdom;
			}
			return pdom;
		}
	}

	public static JavaIndex getIndex() {
		return JavaIndex.getIndex(getPDOM());
	}

	public static int getCurrentVersion() {
		return CURRENT_VERSION;
	}

	private static File getDBFile() {
		IPath stateLocation = JavaCore.getPlugin().getStateLocation();
		return stateLocation.append(INDEX_FILENAME).toFile();
	}

	private static PDOMNodeTypeRegistry<PDOMNode> createTypeRegistry() {
		PDOMNodeTypeRegistry<PDOMNode> registry = new PDOMNodeTypeRegistry<>();
		registry.register(0x0000, PDOMAnnotation.type.getFactory());
		registry.register(0x0010, PDOMAnnotationValuePair.type.getFactory());
		registry.register(0x0020, PDOMBinding.type.getFactory());
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
		registry.register(0x01A0, PDOMTypeSignature.type.getFactory());
		registry.register(0x01A0, PDOMTypeId.type.getFactory());
		registry.register(0x01B0, PDOMTypeInterface.type.getFactory());
		registry.register(0x01D0, PDOMVariable.type.getFactory());
		return registry;
	}
}
