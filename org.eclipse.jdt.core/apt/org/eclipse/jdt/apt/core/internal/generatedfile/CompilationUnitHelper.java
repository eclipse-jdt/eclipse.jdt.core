package org.eclipse.jdt.apt.core.internal.generatedfile;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jdt.apt.core.internal.AptPlugin;
import org.eclipse.jdt.core.IBuffer;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IPackageFragment;
import org.eclipse.jdt.core.IPackageFragmentRoot;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.WorkingCopyOwner;

/*******************************************************************************
 * Copyright (c) 2006, 2007 BEA Systems, Inc.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *    wharley@bea.com - refactored, and reinstated reconcile-time type gen
 *******************************************************************************/

/**
 * Helper utilities to create, modify, save, and discard compilation units and their
 * working copies.  Basically, calls to ICompilationUnit.
 * These are encapsulated here not so much because the code is complex, but rather to
 * make it very clear what the algorithms are (as opposed to distributing these calls
 * throughout other code).  All calls to the Java Model involved in generating types
 * should go through methods here.
 */
public class CompilationUnitHelper
{

	/**
	 * Update the contents of a working copy and commit it to disk.
	 */
	public void commitNewContents(ICompilationUnit wc, String contents, IProgressMonitor monitor) throws JavaModelException {
		IBuffer b = wc.getBuffer();
		b.setContents(contents);
		wc.commitWorkingCopy(true, monitor);
	}

	/**
	 * Get an in-memory working copy.  This does not create the type or package on disk.
	 * <p>
	 * The methods called by this routine are all read-only with respect to the resource
	 * tree, so they do not require taking any scheduling locks.  Therefore we think
	 * it's safe to call this method within a synchronized block.
	 * @param typeName the fully qualified type name, e.g., "foo.Bar"
	 * @param root the package fragment root within which the type will be created
	 * @return a working copy that is ready to be modified.  The working copy may not
	 * yet be backed by a file on disk.
	 */
	public ICompilationUnit getWorkingCopy(String typeName, IPackageFragmentRoot root)
	{
		String[] names = parseTypeName(typeName);
		String pkgName = names[0];
		String fname = names[1];

		IPackageFragment pkgFragment;
		ICompilationUnit workingCopy = null;
		try {
			pkgFragment = root.getPackageFragment(pkgName );
			workingCopy = pkgFragment.getCompilationUnit(fname);
			workingCopy.becomeWorkingCopy(null);
		} catch (JavaModelException e) {
			AptPlugin.log(e, "Unable to become working copy: " + typeName); //$NON-NLS-1$
			return null;
		}
		if (AptPlugin.DEBUG_GFM) AptPlugin.trace(
				"Created working copy: root = " + //$NON-NLS-1$
				root + ",\n\tfragment = " + pkgFragment + ",\n\twc = " + workingCopy); //$NON-NLS-1$ //$NON-NLS-2$
		return workingCopy;
	}

	/**
	 * Discard a working copy, ie, remove it from memory. Each call to
	 * {@link #getWorkingCopy(String typeName, IPackageFragmentRoot root)}
	 * must be balanced with exactly one call to this method.
	 */
	public void discardWorkingCopy(ICompilationUnit wc)
	{
		if (null == wc)
			return;
		if (AptPlugin.DEBUG_GFM) AptPlugin.trace(
				"discarding working copy: " + wc.getElementName()); //$NON-NLS-1$
		try {
			wc.discardWorkingCopy();
		} catch (JavaModelException e) {
			AptPlugin.log(e, "Unable to discard working copy: " + wc.getElementName()); //$NON-NLS-1$
		}
	}

	/**
	 * Update the contents of an existing working copy.
	 *
	 * @param contents
	 *            the new text.
	 * @param reconcile
	 *            true if the changes should be reconciled.
	 * @return true if the contents were modified as a result.
	 */
	public boolean updateWorkingCopyContents(String contents, ICompilationUnit wc,
			WorkingCopyOwner wcOwner, boolean reconcile)
	{
		boolean modified = true;
		IBuffer b = null;
		try {
			b = wc.getBuffer();
		} catch (JavaModelException e) {
			AptPlugin.log(e, "Unable to get buffer for working copy: " + wc.getElementName()); //$NON-NLS-1$
			return false;
		}
		// We need to do this diff to tell our caller whether this is a modification.
		// It's not obvious to me that the caller actually needs to know, so
		// this might just be a needless performance sink. - WHarley 11/06
		modified = !contents.equals(b.getContents());

		b.setContents(contents);
		if (AptPlugin.DEBUG_GFM_MAPS) AptPlugin.trace(
				"updated contents of working copy: " //$NON-NLS-1$
				+ wc.getElementName() + " modified = " + modified); //$NON-NLS-1$
		if (reconcile && modified) {
			try {
				wc.reconcile(ICompilationUnit.NO_AST, true, wcOwner, null);
			} catch (JavaModelException e) {
				AptPlugin.log(e, "Unable to reconcile generated type: " + wc.getElementName()); //$NON-NLS-1$
			}
		}
		return modified;
	}

	/**
	 * Create a package fragment on disk.
	 * @param pkgName the name of the package.
	 * @param root the package fragment root under which to place the package.
	 * @return a package fragment, or null if there was an error.
	 */
	public IPackageFragment createPackageFragment(String pkgName, IPackageFragmentRoot root, IProgressMonitor progressMonitor) {
		IPackageFragment pkgFrag = null;
		try {
			pkgFrag = root.createPackageFragment(pkgName, true,
					progressMonitor);
		} catch (JavaModelException e) {
			AptPlugin.log(e, "Unable to create package fragment for package " + pkgName); //$NON-NLS-1$
		}

		return pkgFrag;
	}

	/**
	 * Given a fully qualified type name, generate the package name and the local filename
	 * including the extension. For instance, type name <code>foo.bar.Baz</code> is
	 * turned into package <code>foo.bar</code> and filename <code>Baz.java</code>.
	 *
	 * @param qualifiedName
	 *            a fully qualified type name
	 * @return a String array containing {package name, filename}
	 */
	private String[] parseTypeName(String qualifiedName) {
		String[] names = new String[2];
		String pkgName;
		String fname;
		int idx = qualifiedName.lastIndexOf( '.' );
		if ( idx > 0 )
		{
		    pkgName = qualifiedName.substring( 0, idx );
		    fname =
				qualifiedName.substring(idx + 1, qualifiedName.length()) + ".java"; //$NON-NLS-1$
		}
		else
		{
			pkgName = ""; //$NON-NLS-1$
			fname = qualifiedName + ".java"; //$NON-NLS-1$
		}
		names[0] = pkgName;
		names[1] = fname;
		return names;
	}

}
