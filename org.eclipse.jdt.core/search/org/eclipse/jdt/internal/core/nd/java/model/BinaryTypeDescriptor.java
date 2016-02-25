package org.eclipse.jdt.internal.core.nd.java.model;

/**
 * Holds a lightweight identifier for an IBinaryType, with sufficient information to either read it from
 * disk or read it from the index.
 */
public final class BinaryTypeDescriptor {
	public final char[] indexPath;
	public final char[] fieldDescriptor;
	public final char[] location;
	public final char[] workspacePath;

	public BinaryTypeDescriptor(char[] location, char[] fieldDescriptor, char[] workspacePath, char[] indexPath) {
		super();
		this.location = location;
		this.fieldDescriptor = fieldDescriptor;
		this.indexPath = indexPath;
		this.workspacePath = workspacePath;
	}
}