package org.eclipse.jdt.internal.core;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
import org.eclipse.core.resources.*;
import org.eclipse.core.resources.*;

import org.eclipse.jdt.core.IJavaModelStatusConstants;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IPackageDeclaration;
import org.eclipse.jdt.core.IJavaModelStatus;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IPackageFragment;

/**
 * Commits the contents of a working copy compilation
 * unit to its original element and resource, bringing
 * the Java Model up-to-date with the current contents of the working
 * copy.
 *
 * <p>It is possible that the contents of the
 * original resource have changed since the working copy was created,
 * in which case there is an update conflict. This operation allows
 * for two settings to resolve conflict set by the <code>fForce</code> flag:<ul>
 * <li>force flag is <code>false</code> - in this case an <code>JavaModelException</code>
 * 	is thrown</li>
 * <li>force flag is <code>true</code> - in this case the contents of
 * 	the working copy are applied to the underlying resource even though
 * 	the working copy was created before a subsequent change in the
 * 	resource</li>
 * </ul>
 *
 * <p>The default conflict resolution setting is the force flag is <code>false</code>
 *
 * @exception JavaModelOperation An exception is thrown either if the commit could not
 * be performed or if the new content of the compilation unit violates some Java Model
 * constraint (e.g. if the new package declaration doesn't match the name of the folder
 * containing the compilation unit).
 */
public class CommitWorkingCopyOperation extends JavaModelOperation {
/**
 * Constructs an operation to commit the contents of a working copy
 * to its original compilation unit.
 */
public CommitWorkingCopyOperation(ICompilationUnit element, boolean force) {
	super(new IJavaElement[] {element}, force);
}
/**
 * Checks that the package declaration in the compilation unit matches the actual
 * package fragment the CU is defined in.
 *
 * @exception JavaModelException with an <code>INVALID_PACKAGE</code> JavaModelStatus if the
 * package declaration is invalid.
 * @see IJavaModelStatusConstants.INVALID_PACKAGE
 */
private void checkPackageDeclaration(ICompilationUnit cu)
	throws JavaModelException {
	IPackageFragment frag = (IPackageFragment) cu.getParent();
	IPackageDeclaration[] decls = cu.getPackageDeclarations();
	String pkgName = frag.getElementName();
	if (pkgName.equals(IPackageFragment.DEFAULT_PACKAGE_NAME)) {
		if (decls != null && decls.length > 0) {
			throw new JavaModelException(
				new JavaModelStatus(
					IJavaModelStatusConstants.INVALID_PACKAGE, 
					cu, 
					decls[0].getElementName())); 
		}
	} else {
		if (decls == null
			|| decls.length != 1
			|| !pkgName.equals(decls[0].getElementName())) {
			throw new JavaModelException(
				new JavaModelStatus(
					IJavaModelStatusConstants.INVALID_PACKAGE, 
					cu, 
					(decls == null || decls.length == 0) ? IPackageFragment.DEFAULT_PACKAGE_NAME : decls[0].getElementName())); 
		}
	}
}
/**
 * @exception JavaModelException if setting the source
 * 	of the original compilation unit fails
 */
protected void executeOperation() throws JavaModelException {
	beginTask(Util.bind("workingCopy.commit"), 2); //$NON-NLS-1$
	ICompilationUnit copy = getCompilationUnit();
	ICompilationUnit original = (ICompilationUnit) copy.getOriginalElement();

	
	// creates the delta builder (this remembers the content of the cu)	
	JavaElementDeltaBuilder deltaBuilder = new JavaElementDeltaBuilder(original);

	// save the cu
	original.getBuffer().setContents(copy.getBuffer().getCharacters());
	original.save(fMonitor, fForce);

	// make sure working copy is in sync
	copy.restore();
	worked(1);

	// build the deltas
	deltaBuilder.buildDeltas();

	// add the deltas to the list of deltas created during this operation
	if (deltaBuilder.delta != null) {
		addDelta(deltaBuilder.delta);
	}
	worked(1);
	
	done();
//	checkPackageDeclaration(original);
}
/**
 * Returns the compilation unit this operation is working on.
 */
protected ICompilationUnit getCompilationUnit() {
	return (ICompilationUnit)getElementToProcess();
}
/**
 * Possible failures: <ul>
 *	<li>INVALID_ELEMENT_TYPES - the compilation unit supplied to this
 *		operation is not a working copy
 *  <li>ELEMENT_NOT_PRESENT - the compilation unit the working copy is
 *		based on no longer exists.
 *  <li>UPDATE_CONFLICT - the original compilation unit has changed since
 *		the working copy was created and the operation specifies no force
 *  </ul>
 */
public IJavaModelStatus verify() {
	ICompilationUnit cu = getCompilationUnit();
	if (!cu.isWorkingCopy()) {
		return new JavaModelStatus(IJavaModelStatusConstants.INVALID_ELEMENT_TYPES, cu);
	}
	ICompilationUnit original= (ICompilationUnit)cu.getOriginalElement();
	IResource resource= null;
	try {
		resource = original.getUnderlyingResource();
	} catch (JavaModelException e) {
		return e.getJavaModelStatus();
	}
	if (!cu.isBasedOn(resource) && !fForce) {
		return new JavaModelStatus(IJavaModelStatusConstants.UPDATE_CONFLICT);
	}
	return JavaModelStatus.VERIFIED_OK;
}
}
