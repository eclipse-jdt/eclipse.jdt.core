package org.eclipse.jdt.core.jdom;

public interface IDOMField extends IDOMMember {
/**
 * Returns the initializer expression for this field.
 * The syntax for an initializer corresponds to VariableInitializer (JLS2 8.3). 
 * <p>
 * Note: The expression does not include a "<code>=</code>".
 * </p>
 *
 * @return the initializer expression, or <code>null</code> if this field does
 *    not have an initializer
 */
public String getInitializer();
/**
 * The <code>IDOMField</code> refinement of this <code>IDOMNode</code>
 * method returns the name of this field. The syntax for the name of a field
 * corresponds to VariableDeclaratorId (JLS2 8.3).
 */
public String getName();
/**
 * Returns the type name of this field. The syntax for a type name of a field
 * corresponds to Type in Field Declaration (JLS2 8.3).
 *
 * @return the type name
 */
public String getType();
/**
 * Sets the initializer expression for this field.
 * The syntax for an initializer corresponds to VariableInitializer (JLS2 8.3). 
 * <p>
 * Note: The expression does not include a "<code>=</code>".
 * </p>
 *
 * @param initializer the initializer expression, or <code>null</code> indicating
 *   the field does not have an initializer
 */
public void setInitializer(String initializer);
/**
 * The <code>IDOMField</code> refinement of this <code>IDOMNode</code>
 * method sets the name of this field. The syntax for the name of a field
 * corresponds to VariableDeclaratorId (JLS2 8.3).
 *
 * @exception IllegalArgumentException if <code>null</code> is specified
 */
public void setName(String name) throws IllegalArgumentException;
/**
 * Sets the type name of this field. The syntax for a type name of a field
 * corresponds to Type in Field Declaration (JLS2 8.3). Type names must be 
 * specified as they should appear in source code. For example: 
 * <code>"String"</code>, <code>"int[]"</code>, or <code>"java.io.File"</code>.
 *
 * @param typeName the type name
 * @exception IllegalArgumentException if <code>null</code> is specified
 */
public void setType(String typeName) throws IllegalArgumentException;
}
