package org.eclipse.jdt.internal.compiler.lookup;

public interface InvocationSite {
	boolean isSuperAccess();
	boolean isTypeAccess();
	void setDepth(int depth);
	void setFieldIndex(int depth);
}
