package org.eclipse.jdt.internal.compiler.lookup;
/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
import org.eclipse.jdt.internal.compiler.*;

public interface InvocationSite {
	boolean isSuperAccess();
	boolean isTypeAccess();
	void setDepth(int depth);
	void setFieldIndex(int depth);
}
