package org.eclipse.jdt.internal.core;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
import org.eclipse.core.resources.*;

import org.eclipse.jdt.core.*;
import org.eclipse.jdt.core.jdom.*;
import org.eclipse.jdt.internal.core.jdom.DOMNode;

/**
 * <p>This operation adds/replaces a package declaration in an existing compilation unit.
 * If the compilation unit already includes the specified package declaration,
 * it is not generated (it does not generate duplicates).
 *
 * <p>Required Attributes:<ul>
 *  <li>Compilation unit element
 *  <li>Package name
 * </ul>
 */
public class CreatePackageDeclarationOperation
	extends CreateElementInCUOperation {
	/**
	 * The name of the package declaration being created
	 */
	protected String fName = null;
	/**
	 * When executed, this operation will add a package declaration to the given compilation unit.
	 */
	public CreatePackageDeclarationOperation(
		String name,
		ICompilationUnit parentElement) {
		super(parentElement);
		fName = name;
	}

	/**
	 * @see CreateTypeMemberOperation#generateElementDOM
	 */
	protected IDOMNode generateElementDOM() throws JavaModelException {
		IJavaElement[] children = getCompilationUnit().getChildren();
		//look for an existing package declaration
		for (int i = 0; i < children.length; i++) {
			if (children[i].getElementType() == IJavaElement.PACKAGE_DECLARATION) {
				IPackageDeclaration pck = (IPackageDeclaration) children[i];
				IDOMPackage pack = (IDOMPackage) ((JavaElement) pck).findNode(fCUDOM);
				if (!pack.getName().equals(fName)) {
					// get the insertion position before setting the name, as this makes it a detailed node
					// thus the start position is always 0
					DOMNode node = (DOMNode) pack;
					fInsertionPosition = node.getStartPosition();
					fReplacementLength = node.getEndPosition() - fInsertionPosition + 1;
					pack.setName(fName);
					fCreatedElement = (org.eclipse.jdt.internal.core.jdom.DOMNode) pack;
				} else {
					//equivalent package declaration already exists
					fCreationOccurred = false;
				}

				return null;
			}
		}
		IDOMPackage pack = (new DOMFactory()).createPackage();
		pack.setName(fName);
		return pack;
	}

	/**
	 * Creates and returns the handle for the element this operation created.
	 */
	protected IJavaElement generateResultHandle() {
		return getCompilationUnit().getPackageDeclaration(fName);
	}

	/**
	 * @see CreateElementInCUOperation#getMainTaskName
	 */
	public String getMainTaskName() {
		return "Creating a package declaration...";
	}

	/**
	 * Sets the correct position for new package declaration:<ul>
	 * <li> before the first import
	 * <li> if no imports, before the first type
	 * <li> if no type - first thing in the CU
	 * <li> 
	 */
	protected void initializeDefaultPosition() {
		try {
			ICompilationUnit cu = getCompilationUnit();
			IImportDeclaration[] imports = cu.getImports();
			if (imports.length > 0) {
				createBefore(imports[0]);
				return;
			}
			IType[] types = cu.getTypes();
			if (types.length > 0) {
				createBefore(types[0]);
				return;
			}
		} catch (JavaModelException npe) {
		}
	}

	/**
	 * Possible failures: <ul>
	 *  <li>NO_ELEMENTS_TO_PROCESS - no compilation unit was supplied to the operation 
	 *  <li>INVALID_NAME - a name supplied to the operation was not a valid
	 * 		package declaration name.
	 * </ul>
	 * @see IJavaModelStatus
	 * @see JavaNamingConventions
	 */
	public IJavaModelStatus verify() {
		IJavaModelStatus status = super.verify();
		if (!status.isOK()) {
			return status;
		}
		if (!JavaConventions.validatePackageName(fName).isOK()) {
			return new JavaModelStatus(IJavaModelStatusConstants.INVALID_NAME, fName);
		}
		return JavaModelStatus.VERIFIED_OK;
	}

}
