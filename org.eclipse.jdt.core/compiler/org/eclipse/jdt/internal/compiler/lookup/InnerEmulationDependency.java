package org.eclipse.jdt.internal.compiler.lookup;
/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
import org.eclipse.jdt.internal.compiler.*;

public class InnerEmulationDependency{
	public BlockScope scope;
	public boolean wasEnclosingInstanceSupplied;
	public boolean useDirectAccess;
    
public InnerEmulationDependency(BlockScope scope, boolean wasEnclosingInstanceSupplied, boolean useDirectAccess) {
	this.scope = scope;
	this.wasEnclosingInstanceSupplied = wasEnclosingInstanceSupplied;
	this.useDirectAccess = useDirectAccess;
}
}
