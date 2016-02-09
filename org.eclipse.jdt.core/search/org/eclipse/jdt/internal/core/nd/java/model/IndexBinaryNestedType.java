package org.eclipse.jdt.internal.core.nd.java.model;

import org.eclipse.jdt.internal.compiler.env.IBinaryNestedType;

public class IndexBinaryNestedType implements IBinaryNestedType {
	private char[] enclosingTypeName;
	private char[] name;
	private int modifiers;

	public IndexBinaryNestedType(char[] name, char[] enclosingTypeName, int modifiers) {
		super();
		this.name = name;
		this.enclosingTypeName = enclosingTypeName;
		this.modifiers = modifiers;
	}

	@Override
	public char[] getEnclosingTypeName() {
		return this.enclosingTypeName;
	}

	@Override
	public int getModifiers() {
		return this.modifiers;
	}

	@Override
	public char[] getName() {
		return this.name;
	}

}
