package org.eclipse.jdt.internal.compiler.env;

/**
 * Represents a reference to a enum constant in the class file 
 */
public interface IEnumConstantReference 
{
	/**
	 * @return name of the enum type in the class file format
	 */
	char[] getTypeName();
	
	/**
	 * @return the name of the enum constant reference.
	 */
	char[] getEnumConstantName();
}
