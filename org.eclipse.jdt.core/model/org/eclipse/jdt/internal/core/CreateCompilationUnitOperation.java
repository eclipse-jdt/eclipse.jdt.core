package org.eclipse.jdt.internal.core;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
import org.eclipse.core.resources.*;

import org.eclipse.jdt.core.*;
import org.eclipse.jdt.core.jdom.DOMFactory;
import org.eclipse.jdt.core.jdom.IDOMCompilationUnit;

import java.io.*;

/**
 * <p>This operation creates a compilation unit (CU).
 *	The operation will fail if the CU already exists.
 *
 * <p>Note: It is possible to create a CU automatically when creating a
 * class or interface. Thus, the preferred method of creating a CU is
 * to perform a create type operation rather than
 * first creating a CU and secondly creating a type inside the CU.
 *
 * <p>Required Attributes:<ul>
 *  <li>The package fragment in which to create the compilation unit.
 *  <li>The name of the compilation unit.  
 *      Do not include the <code>".java"</code> suffix (ex. <code>"Object"</code> -
 * 		the <code>".java"</code> will be added for the name of the compilation unit.)
 *  <li>
  * </ul>
 */
public class CreateCompilationUnitOperation extends JavaModelOperation {

	/**
	 * The name of the compilation unit being created.
	 */
	protected String fName;
	/**
	 * The source code to use when creating the element.
	 */
	protected String fSource= null;
/**
 * When executed, this operation will create a compilation unit with the given name.
 * The name should have the ".java" suffix.
 */
public CreateCompilationUnitOperation(IPackageFragment parentElement, String name, String source, boolean force) {
	super(null, new IJavaElement[] {parentElement}, force);
	fName = name;
	fSource = source;
}
/**
 * Creates a compilation unit.
 *
 * @exception JavaModelException if unable to create the compilation unit.
 */
protected void executeOperation() throws JavaModelException {
	beginTask(Util.bind("operation.createUnitProgress"), 2); //$NON-NLS-1$
	JavaElementDelta delta = newJavaElementDelta();
	ICompilationUnit unit = getCompilationUnit();
	IPackageFragment pkg = (IPackageFragment) getParentElement();
	IContainer folder = (IContainer) pkg.getUnderlyingResource();
	InputStream stream = new ByteArrayInputStream(BufferManager.stringToBytes(fSource));
	worked(1);
	createFile(folder, unit.getElementName(), stream, fForce);
	worked(1);
	fResultElements = new IJavaElement[] {getCompilationUnit()};
	for (int i = 0; i < fResultElements.length; i++) {
		delta.added(fResultElements[i]);
	}
	addDelta(delta);
	done();
}
/**
 * @see CreateElementInCUOperation#getCompilationUnit()
 */
protected ICompilationUnit getCompilationUnit() {
	return ((IPackageFragment)getParentElement()).getCompilationUnit(fName);
}
/**
 * Possible failures: <ul>
 *  <li>NO_ELEMENTS_TO_PROCESS - the package fragment supplied to the operation is
 * 		<code>null</code>.
 *	<li>INVALID_NAME - the compilation unit name provided to the operation 
 * 		is <code>null</code> or has an invalid syntax
 *  <li>INVALID_CONTENTS - the source specified for the compiliation unit is null
 * </ul>
 */
public IJavaModelStatus verify() {
	if (getParentElement() == null) {
		return new JavaModelStatus(IJavaModelStatusConstants.NO_ELEMENTS_TO_PROCESS);
	}
	if (!JavaConventions.validateCompilationUnitName(fName).isOK()) {
		return new JavaModelStatus(IJavaModelStatusConstants.INVALID_NAME, fName);
	}
	if (fSource == null) {
		return new JavaModelStatus(IJavaModelStatusConstants.INVALID_CONTENTS);
	}
	return JavaModelStatus.VERIFIED_OK;
}
}
