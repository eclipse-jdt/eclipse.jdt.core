package org.eclipse.jdt.internal.compiler.lookup;

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
