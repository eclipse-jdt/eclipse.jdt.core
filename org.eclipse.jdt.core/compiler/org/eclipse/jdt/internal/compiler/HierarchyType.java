package org.eclipse.jdt.internal.compiler;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
 
import org.eclipse.jdt.internal.compiler.env.*;

/**
 * 
 * Partial implementation of an IGenericType used to
 * answer hierarchies.
 */
public class HierarchyType implements IGenericType {

	public HierarchyType enclosingType;
	public boolean isClass;
	public char[] name;
	public int modifiers;
	public ICompilationUnit originatingUnit;
public HierarchyType(HierarchyType enclosingType, boolean isClass, char[] name, int modifiers, ICompilationUnit originatingUnit) {
	this.enclosingType = enclosingType;
	this.isClass = isClass;
	this.name = name;
	this.modifiers = modifiers;
	this.originatingUnit = originatingUnit;
}
/**
 * Answer the file name which defines the type.
 *
 * The path part (optional) must be separated from the actual
 * file proper name by a java.io.File.separator.
 *
 * The proper file name includes the suffix extension (e.g. ".java")
 *
 * e.g. "c:/com/ibm/compiler/java/api/Compiler.java" 
 */
public char[] getFileName() {
	return originatingUnit.getFileName();
}
/**
 * Answer an int whose bits are set according the access constants
 * defined by the VM spec.
 */
public int getModifiers() {
	return this.modifiers;
}
/**
 * Answer whether the receiver contains the resolved binary form
 * or the unresolved source form of the type.
 */
public boolean isBinaryType() {
	return false;
}
/**
 * isClass method comment.
 */
public boolean isClass() {
	return this.isClass;
}
/**
 * isInterface method comment.
 */
public boolean isInterface() {
	return !isClass;
}
}
