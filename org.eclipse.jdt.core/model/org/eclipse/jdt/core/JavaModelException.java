package org.eclipse.jdt.core;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
 
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;

import org.eclipse.jdt.internal.core.JavaModelStatus;

/**
 * A checked exception representing a failure in the Java model.
 * Java model exceptions contain a Java-specific status object describing the
 * cause of the exception.
 * <p>
 * This class is not intended to be subclassed by clients. Instances of this
 * class are automatically created by the Java model when problems arise, so
 * there is generally no need for clients to create instances.
 * </p>
 *
 * @see IJavaModelStatus
 * @see IJavaModelStatusConstants
 */
public class JavaModelException extends CoreException {
/**
 * Creates a Java model exception that wrappers the given <code>Throwable</code>.
 * The exception contains a Java-specific status object with severity
 * <code>IStatus.ERROR</code> and the given status code.
 *
 * @param exception the <code>Throwable</code>
 * @param code one of the Java-specific status codes declared in
 *   <code>IJavaModelStatusConstants</code>
 * @return the new Java model exception
 * @see IJavaModelStatusConstants
 * @see org.eclipse.core.runtime.IStatus#ERROR
 */
public JavaModelException(Throwable e, int code) {
	this(new JavaModelStatus(code, e)); 
}
/**
 * Creates a Java model exception for the given <code>CoreException</code>.
 * Equivalent to 
 * <code>JavaModelException(exception,IJavaModelStatusConstants.CORE_EXCEPTION</code>.
 *
 * @param exception the <code>CoreException</code>
 * @return the new Java model exception
 */
public JavaModelException(CoreException exception) {
	this(new JavaModelStatus(exception));
}
/**
 * Creates a Java model exception for the given Java-specific status object.
 *
 * @param status the Java-specific status object
 * @return the new Java model exception
 */
public JavaModelException(IJavaModelStatus status) {
	super(status);
}
/**
 * Returns the underlying <code>Throwable</code> that caused the failure.
 *
 * @return the wrappered <code>Throwable</code>, or <code>null</code> if the
 *   direct case of the failure was at the Java model layer
 */
public Throwable getException() {
	return getStatus().getException();
}
/**
 * Returns the Java model status object for this exception.
 * Equivalent to <code>(IJavaModelStatus) getStatus()</code>.
 *
 * @return a status object
 */
public IJavaModelStatus getJavaModelStatus() {
	return (IJavaModelStatus) getStatus();
}
/**
 * Returns whether this exception indicates that a Java model element does not
 * exist. Such exceptions have a status with a code of
 * <code>IJavaModelStatusConstants.ELEMENT_DOES_NOT_EXIST</code>.
 * This is a convenience method.
 *
 * @return <code>true</code> if this exception indicates that a Java model
 *   element does not exist
 * @see IJavaModelStatus#isDoesNotExist
 * @see IJavaModelStatusConstants#ELEMENT_DOES_NOT_EXIST
 */
public boolean isDoesNotExist() {
	IJavaModelStatus javaModelStatus = getJavaModelStatus();
	return javaModelStatus != null && javaModelStatus.isDoesNotExist();
}
/**
 * Returns a printable representation of this exception suitable for debugging
 * purposes only.
 */
public String toString() {
	StringBuffer buffer= new StringBuffer();
	buffer.append("Java Model Exception: "); //$NON-NLS-1$
	if (getException() != null) {
		if (getException() instanceof CoreException) {
			CoreException c= (CoreException)getException();
			buffer.append("Core Exception [code "); //$NON-NLS-1$
			buffer.append(c.getStatus().getCode());
			buffer.append("] "); //$NON-NLS-1$
			buffer.append(c.getStatus().getMessage());
		} else {
			buffer.append(getException().toString());
		}
	} else {
		buffer.append(getStatus().toString());
	}
	return buffer.toString();
}
}
