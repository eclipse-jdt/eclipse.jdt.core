package org.eclipse.jdt.internal.core.builder.impl;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
import org.eclipse.jdt.internal.compiler.env.*;
import org.eclipse.jdt.internal.core.Assert;
import org.eclipse.jdt.internal.core.builder.*;

import org.eclipse.jdt.internal.compiler.util.CharOperation;
import org.eclipse.jdt.internal.core.Util;

/**
 * Abstract indictment class.  All indictments have a name and
 * certain behaviour in common.
 */
public abstract class Indictment {
	protected char[] fName;
	protected int fKind;

	public static final int K_TYPE = 0;
	public static final int K_METHOD = 1;
	public static final int K_FIELD = 2;
	public static final int K_ABSTRACT_METHOD = 3;
	public static final int K_HIERARCHY = 4;
	/**
	 * Creates a new indictment with the given name
	 */
	protected Indictment(char[] name) {
		fName = name;
	}
	/**
	 * Creates and returns an indictment.
	 * @param type the originating type of the added or removed abstract
	 * methods.
	 */
	public static Indictment createAbstractMethodIndictment(IType type) {
		return new AbstractMethodCollaboratorIndictment(type);
	}
	/**
	 * Creates and returns an indictment.
	 */
	public static Indictment createFieldIndictment(IBinaryField field) {
		return new FieldCollaboratorIndictment(field.getName());
	}
/**
 * Creates and returns an indictment.
 */
public static Indictment createHierarchyIndictment(IBinaryType type) {
	return new TypeHierarchyIndictment(BinaryStructure.getDeclaredName(type));
}
	/**
	 * Creates and returns a method indictment.
	 * Returns null if no indictment should be issued for the method.
	 */
	public static Indictment createMethodIndictment(IType owner, IBinaryType type, IBinaryMethod method) {
		/* Is it a clinit? */
		if (method.getSelector().length > 0 && method.getSelector()[0] == '<' && !method.isConstructor()) {
			return null;
		}
		int parmCount = Util.getParameterCount(method.getMethodDescriptor());
		char[] name;
		if (method.isConstructor()) {
			name = CharOperation.concat('<', BinaryStructure.getDeclaredName(type), '>');
		}
		else {
			name = method.getSelector();
		}
		return new MethodCollaboratorIndictment(owner, name, parmCount);
	}
	/**
	 * Creates and returns an indictment.
	 * If the binary type is known, use createTypeIndictment(IBinaryType) instead.
	 * This should only be used when the type is known to be a package member.
	 * The name must be a top level, unqualified name.
	 */
	public static Indictment createTypeIndictment(String name) {
		//Assert.isTrue(name.indexOf('.') == -1);
		return new TypeCollaboratorIndictment(name.toCharArray());
	}
	/**
	 * Creates and returns an indictment.
	 */
	public static Indictment createTypeIndictment(IBinaryNestedType type) {
		return new TypeCollaboratorIndictment(BinaryStructure.getDeclaredName(type));
	}
	/**
	 * Creates and returns an indictment.
	 */
	public static Indictment createTypeIndictment(IBinaryType type) {
		return new TypeCollaboratorIndictment(BinaryStructure.getDeclaredName(type));
	}
	/**
	 * Returns true if indictments are equal, false otherwise
	 */
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null) return false;
		if (!this.getClass().equals(o.getClass())) return false;

		Indictment i = (Indictment)o;
		return this.fName.equals(i.fName);
	}
	/**
	 * Returns the key used by IndictmentSet to organize indictments.
	 */
	char[] getKey() {
		return fName;
	}
	/**
	 * Returns what kind of indictment this is
	 */
	public abstract int getKind();
/**
 * Returns the key to use for method indictments, where the method has the given
 * name and parameter count.
 */
static char[] getMethodIndictmentKey(char[] methodName, int parmCount) {
	return (Util.toString(methodName) + '/' + parmCount).toCharArray();
}
	/**
	 * Returns the indictment name.  Either the name of a field, method,
	 * type or interface.  All type names are unqualified.
	 */
	public String getName() {
		return new String(fName);
	}
	/**
	 * Returns a hashcode for the indictment
	 */
	public int hashCode() {
		return getKind() * 10 + fName.hashCode();
	}
}
