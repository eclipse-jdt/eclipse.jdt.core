package org.eclipse.jdt.internal.eval;

public class GlobalVariable {
	char[] typeName;
	char[] name;
	char[] initializer;
	int declarationStart = -1, initializerStart = -1, initExpressionStart; // positions in the global variable class definition
	int initializerLineStart = -1; // line in the global variable class definition
/**
 * Creates a new global variable with the given type name, name and initializer.
 * initializer can be null if there is none.
 */
public GlobalVariable(char[] typeName, char[] name, char[] initializer) {
	this.typeName = typeName;
	this.name = name;
	this.initializer = initializer;
}
/**
 * Returns the initializer of this global variable. The initializer is a
 * variable initializer (ie. an expression or an array initializer) as defined 
 * in the Java Language Specifications.
 */
public char[] getInitializer() {
	return this.initializer;
}
/**
 * Returns the name of this global variable.
 */
public char[] getName() {
	return this.name;
}
/**
 * Returns the dot separated fully qualified name of the type of this global variable,
 * or its simple representation if it is a primitive type (eg. int, boolean, etc.)
 */
public char[] getTypeName() {
	return this.typeName;
}
/**
 * Returns a readable representation of the receiver.
 * This is for debugging purpose only.
 */
public String toString() {
	StringBuffer buffer = new StringBuffer();
	buffer.append(this.typeName);
	buffer.append(" "/*nonNLS*/);
	buffer.append(this.name);
	if (this.initializer != null) {
		buffer.append("= "/*nonNLS*/);
		buffer.append(this.initializer);
	}
	buffer.append(";"/*nonNLS*/);
	return buffer.toString();
}
}
