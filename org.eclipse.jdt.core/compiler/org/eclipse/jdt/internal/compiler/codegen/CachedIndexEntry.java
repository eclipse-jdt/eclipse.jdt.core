package org.eclipse.jdt.internal.compiler.codegen;

public class CachedIndexEntry {
	public char[] signature;
	public int index;
	
	public CachedIndexEntry(char[] signature, int index) {
		this.signature = signature;
		this.index = index;
	}
}
