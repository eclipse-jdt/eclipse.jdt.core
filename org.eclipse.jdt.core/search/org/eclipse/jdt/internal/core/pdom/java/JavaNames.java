package org.eclipse.jdt.internal.core.pdom.java;

import org.eclipse.jdt.core.search.IJavaSearchScope;
import org.eclipse.jdt.internal.compiler.env.IBinaryType;

/**
 * @since 3.12
 */
public class JavaNames {
	/**
	 * Converts a java binary name to a simple name.
	 */
	public static String binaryNameToSimpleName(String binaryName) {
		int skipIndex = Math.max(Math.max(binaryName.lastIndexOf('$'), binaryName.lastIndexOf('.')),
				binaryName.lastIndexOf('/')) + 1;

		return binaryName.substring(skipIndex, binaryName.length());
	}

	/**
	 * Given the binary name of a class, returns the jar-relative path of the class file within that
	 * jar, including the .class extension.
	 */
	public static String binaryNameToResourceRelativePath(String binaryName) {
		String relativePath = binaryName;
		int indexOfSeparator = binaryName.indexOf('$');
		if (indexOfSeparator >= 0) {
			relativePath = binaryName.substring(0, indexOfSeparator);
		}
		return relativePath + ".class"; //$NON-NLS-1$
	}

	public static String fullyQualifiedNameToBinaryName(String fullyQualifiedName) {
		return fullyQualifiedName.replace('.', '/');
	}

	public static String fullyQualifiedNameToFieldDescriptor(String fullyQualifiedName) {
		return "L" + fullyQualifiedName.replace('.', '/'); //$NON-NLS-1$
	}

	/**
	 * Given a PDOMType, returns its identifier in the form accepted by {@link IJavaSearchScope#encloses(String)}
	 */
	public static String getIndexPathFor(PDOMType type) {
		PDOMResourceFile resourceFile = type.getResourceFile();

		String filename = resourceFile.getFilename().getString();
		String binaryName = type.getTypeId().getBinaryName();

		return filename + IJavaSearchScope.JAR_FILE_ENTRY_SEPARATOR + binaryNameToResourceRelativePath(binaryName);
	}

	public static String binaryNameToFieldDescriptor(String binaryName) {
		return "L" + binaryName; //$NON-NLS-1$
	}

	public static String fieldDescriptorToJavaName(String fieldDescriptor, boolean fullyQualified) {
		int arrayCount = 0;
		StringBuffer result = new StringBuffer();
		for(int scanPosition = 0; scanPosition < fieldDescriptor.length(); scanPosition++) {
			char nextChar = fieldDescriptor.charAt(scanPosition);
	
			switch (nextChar) {
				case 'B' : result.append("byte"); break; //$NON-NLS-1$
				case 'C' : result.append("char"); break; //$NON-NLS-1$
				case 'D' : result.append("double"); break; //$NON-NLS-1$
				case 'F' : result.append("float"); break; //$NON-NLS-1$
				case 'I' : result.append("int"); break; //$NON-NLS-1$
				case 'J' : result.append("long"); break; //$NON-NLS-1$
				case 'L' : {
					String binaryName = fieldDescriptor.substring(scanPosition + 1);
					if (fullyQualified) {
						result.append(binaryNameToFullyQualifiedName(binaryName)); break;
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

		return result.toString();
	}

	private static String binaryNameToFullyQualifiedName(String binaryName) {
		return binaryName.replace('/', '.');
	}

	/**
	 * Returns a method id (suitable for constructing a PDOMMethodId) given a field descriptor for its parent type
	 * and a combined method selector and method descriptor for the method
	 * 
	 * @param parentTypeFieldDescriptor a field descriptor of the sort returned by the other *ToFieldDescriptor methods.
	 * @param methodSelectorAndDescriptor a method selector and descriptor of the form returned by {@link IBinaryType#getEnclosingMethod()}
	 * @return a method id suitable for looking up a PDOMMethodId
	 */
	public static String methodNameToMethodId(String parentTypeFieldDescriptor, String methodSelectorAndDescriptor) {
		return parentTypeFieldDescriptor + "#" + methodSelectorAndDescriptor;
	}

	/**
	 * Given a field descriptor, if the field descriptor points to a class this returns the binary name of the class.
	 * If the field descriptor points to any other type, this returns the empty string. 
	 * 
	 * @param fieldDescriptor
	 * @return ""
	 */
	public static String fieldDescriptorToBinaryName(String fieldDescriptor) {
		if (fieldDescriptor.startsWith("L")) { //$NON-NLS-1$
			return fieldDescriptor.substring(1);
		}
		return "";
	}
}
