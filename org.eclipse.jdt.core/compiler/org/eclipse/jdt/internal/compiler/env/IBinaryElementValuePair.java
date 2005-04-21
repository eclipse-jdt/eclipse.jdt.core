package org.eclipse.jdt.internal.compiler.env;


/**
 * This represents the class file information about a member value pair of an annotaiton. 
 */
public interface IBinaryElementValuePair
{	
	/** @return the name of the member */
	char[] getMemberName();
	
	/**
	 * Return {@link org.eclipse.jdt.internal.compiler.impl.Constant} for compile-time
	 * constant of primitive type, as well as String literals.
	 * Return {@link IEnumConstantReference} if value is an enum constant
	 * Return {@link IBinaryAnnotation} for annotation type.
	 * Return {@link IClassReference} for member of type {@link java.lang.Class}.
	 * Return {@link Object}[] for array type.
	 * @return the value of this member value pair
	 */
	Object getMemberValue();
	
}
