package org.eclipse.jdt.core.eval;

public interface IGlobalVariable {
	/**
	 * Returns the initializer of this global variable. 
	 * The syntax for an initializer corresponds to VariableInitializer (JLS2 8.3).
	 *
	 * @return the initializer expression, or <code>null</code> if this global does
	 *    not have an initializer
	 */
	public String getInitializer();
	/**
	 * Returns the name of this global variable.
	 *
	 * @return the name of the global variable
	 */
	public String getName();
	/**
	 * Returns the fully qualified name of the type of this global
	 * variable, or its simple representation if it is a primitive type 
	 * (<code>int</code>, <code>boolean</code>, etc.).
	 * <p>
	 * The syntax for a type name corresponds to Type in Field Declaration (JLS2 8.3).
	 * </p>
	 * @return the type name
	 */
	public String getTypeName();
}
