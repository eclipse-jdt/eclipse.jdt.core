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
	
	// in case the receiver type does not match the actual receiver type 
	// e.g. pkg.Type.C (receiver type of C is type of source context, 
	//		but actual receiver type is pkg.Type)
	// e.g2. in presence of implicit access to enclosing type
	void setActualReceiverType(ReferenceBinding receiverType);
}
