package org.eclipse.jdt.internal.core.nd.java.model;

import org.eclipse.core.runtime.IPath;
import org.eclipse.jdt.internal.compiler.env.IBinaryType;
import org.eclipse.jdt.internal.core.nd.java.JavaIndex;
import org.eclipse.jdt.internal.core.nd.java.JavaNames;
import org.eclipse.jdt.internal.core.nd.java.TypeRef;

public class BinaryTypeFactory {
	public static IBinaryType create(IPath fileSystemPath, String binaryName) {
		JavaIndex index = JavaIndex.getIndex();
		TypeRef typeRef = TypeRef.create(index.getNd(),
				fileSystemPath.toString().toCharArray(),
				JavaNames.binaryNameToFieldDescriptor(binaryName.toCharArray()));
		return new IndexBinaryType(typeRef);
	}
}
