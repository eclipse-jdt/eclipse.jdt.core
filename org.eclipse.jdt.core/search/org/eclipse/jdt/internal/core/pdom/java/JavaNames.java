package org.eclipse.jdt.internal.core.pdom.java;

import org.eclipse.jdt.core.compiler.CharOperation;
import org.eclipse.jdt.core.search.IJavaSearchScope;
import org.eclipse.jdt.internal.compiler.env.IBinaryType;
import org.eclipse.jdt.internal.core.pdom.indexer.CharUtil;
import org.eclipse.jdt.internal.core.util.CharArrayBuffer;

/**
 * @since 3.12
 */
public class JavaNames {
	private static final char[] CLASS_FILE_SUFFIX = ".class".toCharArray(); //$NON-NLS-1$
	private static final char[] FIELD_DESCRIPTOR_PREFIX = new char[]{'L'};
	private static final char[] METHOD_ID_SEPARATOR = new char[]{'#'};
	private static final char[] JAR_FILE_ENTRY_SEPARATOR = IJavaSearchScope.JAR_FILE_ENTRY_SEPARATOR.toCharArray();

	/**
	 * Converts a java binary name to a simple name.
	 */
	public static char[] binaryNameToSimpleName(char[] binaryName) {
		int skipIndex = Math.max(
				Math.max(CharOperation.lastIndexOf('$', binaryName), CharOperation.lastIndexOf('.', binaryName)),
				CharOperation.lastIndexOf('/', binaryName)) + 1;

		return CharUtil.substring(binaryName, skipIndex);
	}

	/**
	 * Given the binary name of a class, returns the jar-relative path of the class file within that
	 * jar, including the .class extension.
	 */
	public static char[] binaryNameToResourceRelativePath(char[] binaryName) {
		char[] relativePath = binaryName;
		int indexOfSeparator = CharOperation.indexOf('$', relativePath);
		if (indexOfSeparator >= 0) {
			relativePath = CharOperation.subarray(relativePath, 0, indexOfSeparator);
		}
		return CharOperation.concat(relativePath, CLASS_FILE_SUFFIX);
	}

	public static char[] fullyQualifiedNameToBinaryName(char[] fullyQualifiedName) {
		return CharOperation.replaceOnCopy(fullyQualifiedName, '.', '/');
	}

	public static char[] fullyQualifiedNameToFieldDescriptor(char[] fullyQualifiedName) {
		char[] result = CharUtil.concat(FIELD_DESCRIPTOR_PREFIX, fullyQualifiedName);
		CharOperation.replace(result, '.', '/');
		return result;
	}

	/**
	 * Given a PDOMType, returns its identifier in the form accepted by {@link IJavaSearchScope#encloses(String)}
	 */
	public static char[] getIndexPathFor(PDOMType type) {
		PDOMResourceFile resourceFile = type.getResourceFile();

		char[] filename = resourceFile.getFilename().getChars();
		char[] binaryName = type.getTypeId().getBinaryName();

		return CharUtil.concat(filename, JAR_FILE_ENTRY_SEPARATOR, binaryNameToResourceRelativePath(binaryName));
	}

	public static char[] binaryNameToFieldDescriptor(char[] binaryName) {
		return CharUtil.concat(FIELD_DESCRIPTOR_PREFIX, binaryName);
	}

	public static char[] fieldDescriptorToJavaName(char[] fieldDescriptor, boolean fullyQualified) {
		int arrayCount = 0;
		CharArrayBuffer result = new CharArrayBuffer();
		for(int scanPosition = 0; scanPosition < fieldDescriptor.length; scanPosition++) {
			char nextChar = fieldDescriptor[scanPosition];

			switch (nextChar) {
				case 'B' : result.append("byte"); break; //$NON-NLS-1$
				case 'C' : result.append("char"); break; //$NON-NLS-1$
				case 'D' : result.append("double"); break; //$NON-NLS-1$
				case 'F' : result.append("float"); break; //$NON-NLS-1$
				case 'I' : result.append("int"); break; //$NON-NLS-1$
				case 'J' : result.append("long"); break; //$NON-NLS-1$
				case 'L' : {
					char[] binaryName = CharUtil.substring(fieldDescriptor, scanPosition + 1);
					if (fullyQualified) {
						// Modify the binaryName string in-place to change it into a fully qualified name
						CharOperation.replace(binaryName, '/', '.');
						result.append(binaryName); break;
					} else {
						result.append(binaryNameToSimpleName(binaryName)); break;
					}
				}
				case 'S' : result.append("short"); break; //$NON-NLS-1$
				case 'Z' : result.append("boolean"); break; //$NON-NLS-1$
				case '[' : arrayCount++; break;
			}
		}

		while (--arrayCount >= 0) {
			result.append("[]"); //$NON-NLS-1$
		}

		return result.getContents();
	}

	public static char[] binaryNameToFullyQualifiedName(char[] binaryName) {
		return CharOperation.replaceOnCopy(binaryName, '/', '.');
	}

	/**
	 * Returns a method id (suitable for constructing a PDOMMethodId) given a field descriptor for its parent type
	 * and a combined method selector and method descriptor for the method
	 *
	 * @param parentTypeBinaryName a field descriptor of the sort returned by the other *ToFieldDescriptor methods.
	 * @param methodSelectorAndDescriptor a method selector and descriptor of the form returned by {@link IBinaryType#getEnclosingMethod()}
	 * @return a method id suitable for looking up a PDOMMethodId
	 */
	public static char[] getMethodId(char[] parentTypeBinaryName, char[] methodSelectorAndDescriptor) {
		return CharUtil.concat(FIELD_DESCRIPTOR_PREFIX, parentTypeBinaryName, METHOD_ID_SEPARATOR, methodSelectorAndDescriptor);
	}

	public static char[] getMethodId(char[] parentTypeBinaryName, char[] methodSelector, char[] methodDescriptor) {
		return CharUtil.concat(FIELD_DESCRIPTOR_PREFIX, parentTypeBinaryName, METHOD_ID_SEPARATOR, methodSelector, methodDescriptor);
	}

	/**
	 * Given a field descriptor, if the field descriptor points to a class this returns the binary name of the class.
	 * If the field descriptor points to any other type, this returns the empty string.
	 *
	 * @param fieldDescriptor
	 * @return ""
	 */
	public static char[] fieldDescriptorToBinaryName(char[] fieldDescriptor) {
		if (CharUtil.startsWith(fieldDescriptor, 'L')) {
			return CharUtil.substring(fieldDescriptor, 1);
		}
		return CharUtil.EMPTY_CHAR_ARRAY;
	}
}
