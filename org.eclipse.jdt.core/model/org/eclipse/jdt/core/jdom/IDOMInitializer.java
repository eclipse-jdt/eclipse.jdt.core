package org.eclipse.jdt.core.jdom;

public interface IDOMInitializer extends IDOMMember {
/**
 * Returns the body of this initializer. The syntax for a body corresponds to
 * InstanceInitializer (JLS2 8.6) and StaticDeclaration (JLS2 8.7).
 *
 * @return an initializer body, including braces, or <code>null</code> if
 *   no body is present
 */
public String getBody();
/**
 * The <code>IDOMInitializer</code> refinement of this <code>IDOMNode</code>
 * method returns <code>null</code>. An initializer does not have a name.
 */
public String getName();
/**
 * Sets the body of this initializer. The syntax for a body corresponds to
 * InstanceInitializer (JLS2 8.6) and StaticDeclaration (JLS2 8.7). No formatting
 * or syntax checking is performed on the body. Braces <b>must</b> be included.
 *
 * @param body an initializer body, including braces, or <code>null</code> 
 *   indicating no body
 */
public void setBody(String body);
/**
 * The <code>IDOMInitializer</code> refinement of this <code>IDOMNode</code>
 * method does nothing.
 */
public void setName(String name);
}
