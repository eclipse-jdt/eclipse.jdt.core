package org.eclipse.jdt.core;

public interface IMethod extends IMember {
/**
 * Returns the simple name of this method.
 * For a constructor, this returns the simple name of the declaring type.
 * Note: This holds whether the constructor appears in a source or binary type
 * (even though class files internally define constructor names to be <code>"&lt;init&gt;"</code>).
 * For the class initialization methods in binary types, this returns
 * the special name <code>"&lt;clinit&gt;"</code>.
 * This is a handle-only method.
 */
String getElementName();
/**
 * Returns the type signatures of the exceptions this method throws,
 * in the order declared in the source. Returns an empty array
 * if this method throws no exceptions.
 *
 * <p>For example, a source method declaring <code>"throws IOException"</code>,
 * would return the array <code>{"QIOException;"}</code>.
 *
 * @exception JavaModelException if this element does not exist or if an
 *		exception occurs while accessing its corresponding resource.
 *
 * @see Signature
 */
String[] getExceptionTypes() throws JavaModelException;
/**
 * Returns the number of parameters of this method.
 * This is a handle-only method.
 */
int getNumberOfParameters();
/**
 * Returns the names of parameters in this method.
 * For binary types, these names are invented as "arg"+i, where i starts at 1 
 * (even if source is associated with the binary).
 * Returns an empty array if this method has no parameters.
 *
 * <p>For example, a method declared as <code>public void foo(String text, int length)</code>
 * would return the array <code>{"text","length"}</code>.
 *
 * @exception JavaModelException if this element does not exist or if an
 *		exception occurs while accessing its corresponding resource.
 */
String[] getParameterNames() throws JavaModelException;
/**
 * Returns the type signatures for the parameters of this method.
 * Returns an empty array if this method has no parameters.
 * This is a handle-only method.
 *
 * <p>For example, a source method declared as <code>public void foo(String text, int length)</code>
 * would return the array <code>{"QString;","I"}</code>.
 *
 * @see Signature
 */
String[] getParameterTypes();
/**
 * Returns the type signature of the return value of this method.
 * For constructors, this returns the signature for void.
 *
 * <p>For example, a source method declared as <code>public String getName()</code>
 * would return <code>"QString;"</code>.
 *
 * @exception JavaModelException if this element does not exist or if an
 *		exception occurs while accessing its corresponding resource.
 *
 * @see Signature
 */
String getReturnType() throws JavaModelException;
/**
 * Returns the signature of the method. This includes the signatures for the parameter
 * types and return type, but does not include the method name or exception types.
 *
 * <p>For example, a source method declared as <code>public void foo(String text, int length)</code>
 * would return <code>"(QString;I)V"</code>.
 *
 * @exception JavaModelException if this element does not exist or if an
 *		exception occurs while accessing its corresponding resource.
 *
 * @see Signature
 */
String getSignature() throws JavaModelException;
/**
 * Returns whether this method is a constructor.
 *
 * @exception JavaModelException if this element does not exist or if an
 *		exception occurs while accessing its corresponding resource.
 */
boolean isConstructor() throws JavaModelException;
}
