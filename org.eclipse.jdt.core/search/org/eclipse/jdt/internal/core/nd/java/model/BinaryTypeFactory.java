package org.eclipse.jdt.internal.core.nd.java.model;

import org.eclipse.core.runtime.IPath;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.internal.compiler.env.IBinaryType;
import org.eclipse.jdt.internal.core.nd.java.JavaIndex;
import org.eclipse.jdt.internal.core.nd.java.JavaNames;
import org.eclipse.jdt.internal.core.nd.java.TypeRef;

public class BinaryTypeFactory {
	public static IBinaryType create(IJavaElement javaElement, String binaryName) {
		IPath filesystemLocation = JavaIndex.getLocationForElement(javaElement);

		JavaIndex index = JavaIndex.getIndex();
		TypeRef typeRef = TypeRef.create(index.getNd(),
				filesystemLocation.toString().toCharArray(),
				JavaNames.binaryNameToFieldDescriptor(binaryName.toCharArray()));

		char[] indexPath = (javaElement.getPath().toString() + "|" + binaryName + ".class").toCharArray(); //$NON-NLS-1$//$NON-NLS-2$
		return new IndexBinaryType(typeRef, indexPath);
	}
}
