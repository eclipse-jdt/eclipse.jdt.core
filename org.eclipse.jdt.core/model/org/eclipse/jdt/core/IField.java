package org.eclipse.jdt.core;
/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
import org.eclipse.jdt.internal.core.*;

/**
 * Represents a field declared in a type.
 * <p>
 * This interface is not intended to be implemented by clients.
 * </p>
 */
public interface IField extends IMember {
/**
 * Returns the constant value associated with this field
 * or <code>null</code> if this field has none.
 * Returns either a subclass of <code>Number</code>, or a <code>String</code>,
 * depending on the type of the field.
 * For example, if the field is of type <code>short</code>, this returns
 * a <code>Short</code>.
 *
 * @exception JavaModelException if this element does not exist or if an
 *      exception occurs while accessing its corresponding resource
 */
public Object getConstant() throws JavaModelException;
/**
 * Returns the simple name of this field.
 */
String getElementName();
/**
 * Returns the type signature of this field.
 *
 * @see Signature
 * @exception JavaModelException if this element does not exist or if an
 *      exception occurs while accessing its corresponding resource
 */
String getTypeSignature() throws JavaModelException;
}
