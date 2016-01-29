package org.eclipse.jdt.internal.core.nd.java;

import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.jdt.core.compiler.CharOperation;
import org.eclipse.jdt.core.search.IJavaSearchScope;
import org.eclipse.jdt.internal.compiler.env.IBinaryType;
import org.eclipse.jdt.internal.core.nd.util.CharArrayUtils;
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

		return CharArrayUtils.substring(binaryName, skipIndex);
	}

	/**
	 * Given the binary name of a class, returns the jar-relative path of the class file within that
	 * jar, including the .class extension.
	 */
	public static char[] binaryNameToResourceRelativePath(char[] binaryName) {
		return CharOperation.concat(binaryName, CLASS_FILE_SUFFIX);
	}

	public static char[] fullyQualifiedNameToBinaryName(char[] fullyQualifiedName) {
		return CharOperation.replaceOnCopy(fullyQualifiedName, '.', '/');
	}

	public static char[] fullyQualifiedNameToFieldDescriptor(char[] fullyQualifiedName) {
		char[] result = CharArrayUtils.concat(FIELD_DESCRIPTOR_PREFIX, fullyQualifiedName);
		CharOperation.replace(result, '.', '/');
		return result;
	}

	/**
	 * Given a PDOMType, returns its identifier in the form accepted by {@link IJavaSearchScope#encloses(String)}
	 */
	public static char[] getIndexPathFor(NdType type, IWorkspaceRoot root) {
		NdResourceFile resourceFile = type.getResourceFile();

		char[] binaryName = type.getTypeId().getBinaryName();

		char[] workspaceLocation = null;
		if (root != null) {
			workspaceLocation = resourceFile.getAnyOpenWorkspaceLocation(root).toString().toCharArray();
		}

		if (workspaceLocation == null) {
			workspaceLocation = resourceFile.getFilename().getChars();
		}

		return CharArrayUtils.concat(workspaceLocation, JAR_FILE_ENTRY_SEPARATOR,
				binaryNameToResourceRelativePath(binaryName));
	}

	public static char[] binaryNameToFieldDescriptor(char[] binaryName) {
		return CharArrayUtils.concat(FIELD_DESCRIPTOR_PREFIX, binaryName);
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
					char[] binaryName = CharArrayUtils.substring(fieldDescriptor, scanPosition + 1);
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
		return CharArrayUtils.concat(FIELD_DESCRIPTOR_PREFIX, parentTypeBinaryName, METHOD_ID_SEPARATOR,
				methodSelectorAndDescriptor);
	}

	public static char[] getMethodId(char[] parentTypeBinaryName, char[] methodSelector, char[] methodDescriptor) {
		return CharArrayUtils.concat(FIELD_DESCRIPTOR_PREFIX, parentTypeBinaryName, METHOD_ID_SEPARATOR, methodSelector,
				methodDescriptor);
	}

	/**
	 * Given a field descriptor, if the field descriptor points to a class this returns the binary name of the class.
	 * If the field descriptor points to any other type, this returns the empty string.
	 *
	 * @param fieldDescriptor
	 * @return ""
	 */
	public static char[] fieldDescriptorToBinaryName(char[] fieldDescriptor) {
		if (CharArrayUtils.startsWith(fieldDescriptor, 'L')) {
			return CharArrayUtils.substring(fieldDescriptor, 1);
		}
		return CharArrayUtils.EMPTY_CHAR_ARRAY;
	}

	/**
	 * Given a simple name, this returns the source name for the type. Note that this won't work for classes that
	 * contain a $ in their source name.
	 */
	public static char[] simpleNameToSourceName(char[] chars) {
		int lastSlash = CharOperation.lastIndexOf('/', chars);
		int lastDollar = CharOperation.lastIndexOf('$', chars);
		int startPosition = Math.max(lastSlash, lastDollar) + 1;
		while (startPosition < chars.length && Character.isDigit(chars[startPosition])) {
			startPosition++;
		}
		return CharArrayUtils.substring(chars, startPosition);
	}
}
