package org.eclipse.jdt.internal.core.builder;

public interface IPackage extends IHandle {


	/**
	 * Compares this Package handle against the specified object.  Returns
	 * true if the objects are the same.  Two Package handles are the same if
	 * they both have the same fully qualified name.
	 * See Handle.equals() for more details.
	 *
	 * @see IHandle#equals
	 * @see IHandle#hashCode
	 */
	boolean equals(Object obj);
	/**
	 * Returns an array containing Type objects representing all
	 * classes and interfaces in the package represented by this object.
	 * This includes public and default (package) access top-level 
	 * classes, inner classes, and local inner classes.
	 * Returns an array of length 0 if this package has no
	 * classes or interfaces.
	 * The Types are in no particular order.
	 * 
	 * @exception NotPresentException if this package is not present.
	 */
	IType[] getAllClasses() throws NotPresentException;
	/**
	 * Returns a handle representing the class or interface
	 * with the given name.  The name is the VM class name,
	 * not including the package name.
	 * For inner classes, the name is as described in the 
	 * <em>Inner Classes Specification</em>.
	 * This is a handle-only method; the specified class 
	 * may or may not actually be present in the image.
	 *
	 * @see IType#getName
	 * @see IHandle
	 */
	IType getClassHandle(String name);
	/**
	 * Returns an array of Type objects representing all the classes
	 * and interfaces declared as members of the package represented by
	 * this object. This includes public and default (package) access
	 * classes and interfaces declared as members of the package. 
	 * This does not include inner classes and interfaces.
	 * Returns an array of length 0 if this package declares no classes
	 * or interfaces as members.
	 * The Types are in no particular order.
	 * 
	 * @exception NotPresentException if this package is not present.
	 */
	IType[] getDeclaredClasses() throws NotPresentException;
	/**
	 * Returns the fully-qualified name of the package represented 
	 * by this object, as a String. 
	 * If the package is unnamed, returns the internal identifier
	 * string of this unnamed packaged.
	 * This is a handle-only method.
	 *
	 * @return	the fully qualified name of the package
	 *			represented by this object.
	 */
	String getName();
	/**
	 * Returns an array of Package objects representing all other
	 * packages which this package directly references.
	 * This is the union of all packages directly referenced by all 
	 * classes and interfaces in this package, including packages
	 * mentioned in import declarations.
	 * <p>
	 * A direct reference in source code is a use of a package's
	 * name other than as a prefix of another package name.
	 * For example, 'java.lang.Object' contains a direct reference
	 * to the package 'java.lang', but not to the package 'java'.
	 * Also note that every package that declares at least one type
	 * contains a direct reference to java.lang in virtue of the
	 * automatic import of java.lang.*.
	 * The result does not include this package (so contrary to the note
	 * above, the result for package java.lang does not include java.lang).
	 * In other words, the result is non-reflexive and typically
	 * non-transitive.
	 * <p>
	 * The resulting packages may or may not be present in the image,
	 * since the classes and interfaces in this package may refer to missing
	 * packages.
	 * The resulting packages are in no particular order.
	 *
	 * @exception NotPresentException if this package is not present.
	 */
	 IPackage[] getReferencedPackages() throws NotPresentException;
	/**
	 * Returns an array of Package objects representing all packages
	 * in the given image context which directly reference this package.
	 * The result does not include this package.
	 * In other words, the result is non-transitive and non-reflexive.
	 * <p>
	 * The intersection of all packages in the image and those in the
	 * image context are considered, so the resulting packages are 
	 * guaranteed to be present in the image.
	 * The resulting packages are in no particular order.
	 * 
	 * @param context the ImageContext in which to restrict the search.
	 * @exception NotPresentException if this package is not present.
	 */
	IPackage[] getReferencingPackages(IImageContext context) throws NotPresentException;
	/**
	 * Returns an array of SourceFragments describing the source package 
	 * fragments from which this built package is derived.
	 * Returns an empty array if this package is not derived directly from source
	 * (e.g. package com.oti.requiem.fictional).
	 * The source coordinates in the results are set to {0, -1}.
	 *
	 * @exception NotPresentException if the package is not present.
	 */
	ISourceFragment[] getSourceFragments() throws NotPresentException;
	/**
	 * Returns true if this package is an unnamed package, false 
	 * otherwise.  See <em>The Java Language Specification</em>, 
	 * sections 7.4.1 and 7.4.2, for details.
	 * This is a handle-only method.
	 */
	boolean isUnnamed();
	/**
	 * Converts the object to a string. 
	 * The string representation is the string "package" followed by a space 
	 * and then the fully qualified name of the package. 
	 *
	 * @see IHandle#toString
	 */
	String toString();
}
