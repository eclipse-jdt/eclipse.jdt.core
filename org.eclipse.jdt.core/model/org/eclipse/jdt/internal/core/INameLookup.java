package org.eclipse.jdt.internal.core;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
import org.eclipse.core.resources.*;
import org.eclipse.core.runtime.IPath;

import org.eclipse.jdt.core.*;

/**
 * <code>INameLookup</code> provides name resolution within a Java project.
 * The name lookup facility uses the
 * project's classpath to prioritize the order in which package
 * fragments are searched when resolving a name.
 *
 * <p>Name lookup only returns a handle when the named element actually
 * exists in the model; otherwise <code>null</code> is returned.
 *
 * <p>There are two logical sets of methods within this interface.  Methods
 * which start with <code>find*</code> are intended to be convenience methods for quickly
 * finding an element within another element, i.e. finding a class within a
 * package.  The other set of methods all begin with <code>seek*</code>.  These methods
 * do comprehensive searches of the <code>IJavaProject</code> returning hits
 * in real time through an <code>IJavaElementRequestor</code>.
 *
 */
public interface INameLookup {
	/**
	 * Accept flag for specifying classes.
	 */
	public static final int ACCEPT_CLASSES = 0x00000002;
	/**
	 * Accept flag for specifying interfaces.
	 */
	public static final int ACCEPT_INTERFACES = 0x00000004;
/**
 * Returns the <code>ICompilationUnit</code> which defines the type
 * named <code>qualifiedTypeName</code>, or <code>null</code> if
 * none exists. The domain of the search is bounded by the classpath
 * of the <code>IJavaProject</code> this <code>INameLookup</code> was
 * obtained from.
 * <p>
 * The name must be fully qualified (eg "java.lang.Object", "java.util.Hashtable$Entry")
 */
ICompilationUnit findCompilationUnit(String qualifiedTypeName);
/**
 * Returns the package fragment whose path matches the given
 * (absolute) path, or <code>null</code> if none exist. The domain of
 * the search is bounded by the classpath of the <code>IJavaProject</code>
 * this <code>INameLookup</code> was obtained from.
 * The path can be:
 * 	- internal to the workbench: "/Project/src"
 *  - external to the workbench: "c:/jdk/classes.zip/java/lang"
 */
IPackageFragment findPackageFragment(IPath path);
/**
 * Returns the package fragment root whose path matches the given
 * (absolute) path, or <code>null</code> if none exist. The domain of
 * the search is bounded by the classpath of the <code>IJavaProject</code>
 * this <code>INameLookup</code> was obtained from.
 * The path can be:
 *	- internal to the workbench: "/Compiler/src"
 *	- external to the workbench: "c:/jdk/classes.zip"
 */
IPackageFragmentRoot findPackageFragmentRoot(IPath path);
/**
 * Returns the package fragments whose name matches the given
 * (qualified) name, or <code>null</code> if none exist.
 *
 * The name can be:
 *	- empty: ""
 *	- qualified: "pack.pack1.pack2"
 * @param partialMatch partial name matches qualify when <code>true</code>,
 *	only exact name matches qualify when <code>false</code>
 */
IPackageFragment[] findPackageFragments(String name, boolean partialMatch);
/**
 * Returns the first type in the given package whose name
 * matches the given (unqualified) name, or <code>null</code> if none
 * exist. Specifying a <code>null</code> package will result in no matches.
 * The domain of the search is bounded by the Java project from which 
 * this name lookup was obtained.
 *
 * @name the name of the type to find
 * @pkg the package to search
 * @param partialMatch partial name matches qualify when <code>true</code>,
 *	only exact name matches qualify when <code>false</code>
 * @param acceptFlags a bit mask describing if classes, interfaces or both classes and interfaces
 * 	are desired results. If no flags are specified, all types are returned.
 *
 * @see ACCEPT_CLASSES
 * @see ACCEPT_INTERFACES
 */
IType findType(String name, IPackageFragment pkg, boolean partialMatch, int acceptFlags);
/**
 * Returns the type specified by the qualified name, or <code>null</code>
 * if none exist. The domain of
 * the search is bounded by the Java project from which this name lookup was obtained.
 *
 * @name the name of the type to find
 * @param partialMatch partial name matches qualify when <code>true</code>,
 *	only exact name matches qualify when <code>false</code>
 * @param acceptFlags a bit mask describing if classes, interfaces or both classes and interfaces
 * 	are desired results. If no flags are specified, all types are returned.
 *
 * @see ACCEPT_CLASSES
 * @see ACCEPT_INTERFACES
 */
public IType findType(String name, boolean partialMatch, int acceptFlags);
/**
 * Notifies the given requestor of all package fragments with the
 * given name. Checks the requestor at regular intervals to see if the
 * requestor has canceled. The domain of
 * the search is bounded by the <code>IJavaProject</code>
 * this <code>INameLookup</code> was obtained from.
 *
 * @param partialMatch partial name matches qualify when <code>true</code>;
 *	only exact name matches qualify when <code>false</code>
 */
public void seekPackageFragments(String name, boolean partialMatch, IJavaElementRequestor requestor);
/**
 * Notifies the given requestor of all types (classes and interfaces) in the
 * given package fragment with the given (unqualified) name.
 * Checks the requestor at regular intervals to see if the requestor
 * has canceled.  If the given package fragment is <code>null</code>, all types in the
 * project whose simple name matches the given name are found.
 *
 * @param partialMatch partial name matches qualify when <code>true</code>;
 *	only exact name matches qualify when <code>false</code>
 * @param acceptFlags a bit mask describing if classes, interfaces or both classes and interfaces
 * 	are desired results. If no flags are specified, all types are returned.
 *
 * @see ACCEPT_CLASSES
 * @see ACCEPT_INTERFACES
 */
public void seekTypes(String name, IPackageFragment pkg, boolean partialMatch, int acceptFlags, IJavaElementRequestor requestor);
}
