package org.eclipse.jdt.internal.compiler.env;

/**
 * Represents a class reference in the class file.
 */
public interface IClassReference 
{
	/** @return the name of the type reference */
	char[] getTypeName();
}
