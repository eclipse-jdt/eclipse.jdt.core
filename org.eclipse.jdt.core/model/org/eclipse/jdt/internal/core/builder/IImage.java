package org.eclipse.jdt.internal.core.builder;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
import org.eclipse.core.runtime.IPath;

/**
 * The <code>IImage</code> represents a Java image.
 * 
 * <p>An image contains zero or more packages.
 * All types (classes and interfaces) declared in the image's
 * packages are also contained in the image.
 * Classes and interfaces contained in a image
 * can refer only to other classes and interfaces contained
 * in the same image.
 * 
 * <p>The assumption is that images contain a very large number of
 * packages (conceptually, an infinite number of packages), most
 * of which are irrelevant for the purposes at hand.
 *
 * <p>Methods never throw NotPresentException for an Image since the
 * Image is considered to be always present.
 */

public interface IImage extends IHandle {

	/*
	 * The following methods are needed for the following reasons:
	 *	- unlike java.lang, we don't have separate classes 
	 *	  to hold the TYPE constants as in java.lang.Boolean.TYPE, etc., 
	 *	- they can't be constants because primitive types are tied to a DC 
	 *	  and can actually be state-specific, since they are instances 
	 *	  of Type which extends Handle.
	 */

	/** Returns the IType representing the primitive type boolean. */
	IType booleanType();
	/** Returns the Type representing the primitive type byte. */
	IType byteType();
	/** Returns the Type representing the primitive type char. */
	IType charType();
	/*
	 * ImageContext stuff.
	 */

	/**
	 * Returns an ImageContext consisting of the given packages.
	 * This object and the packages must be non-state-specific.
	 * This is a handle-only operation.  The packages need not
	 * be present in the image.
	 *
	 * @exception StateSpecificException 
	 *		if this object or any of the packages are state-specific.
	 * @see #getPackages
	 * @see #getAllClasses
	 */
	IImageContext createImageContext(IPackage[] packages)
		throws StateSpecificException;
	/** Returns the Type representing the primitive type double. */
	IType doubleType();
	/**
	 * Compares this Image handle against the specified object.  Returns
	 * true if the objects are the same.  Two Image handles are the same if
	 * they belong to the same development context, and if they are both
	 * non-state-specific or are both state-specific on the same state.
	 * See Handle.equals() for more details.
	 *
	 * @see IHandle#equals
	 * @see IHandle#hashCode
	 */
	boolean equals(Object obj);
	/** Returns the Type representing the primitive type float. */
	IType floatType();
	/**
	 * Returns an array containing Type objects representing all
	 * classes and interfaces in the given ImageContext.
	 * This includes public and default (package) access top-level 
	 * classes, inner classes, and local inner classes.
	 * The result is the intersection of all classes present in this image
	 * and the classes in the ImageContext, so the resulting classes
	 * are all present in the image.
	 * The resulting Types are in no particular order.
	 *
	 * @param context the ImageContext in which to restrict the search.
	 * @see IPackage#getAllClasses()
	 */
	IType[] getAllClasses(IImageContext context);
	/**
	 * Returns an array of all packages present in the image.  Note that this
	 * method defies the concept of a potentially infinite image, and should only
	 * be used by clients that must operate over the entire image (search, code assist)
	 */
	IPackage[] getAllPackages();
	/**
	 * Returns all types which were built from the indicated workspace element.
	 * If the element is a compilation unit, this includes all types declared in
	 * the compilation unit which were successfully built, including member types
	 * and local types.
	 * If the element is a class file, this includes the type represented by the
	 * class file.
	 * If the element is a package, this includes all types produced by compilation
	 * units and class files in the package fragment.
	 * If the element is a project, this includes all types produced by all packages
	 * and zip files in the project.
	 * If the element is a zip file, this includes all types produced by compilation
	 * units and class files in the zip file.
	 * If the element was not built (for example, it is not present in the workspace,
	 * or was not included in the class path), this returns an empty array.
	 */
	IType[] getBuiltClasses(IPath path);
	/*
	 * Handle creation stuff.
	 */

	/**
	 * Returns a handle representing the package with the given 
	 * name.  For named packages, this is the fully qualified
	 * name.  For unnamed packages, it is some internal identifying
	 * string.
	 * See <em>The Java Language Specification</em> section 7.4.1 and
	 * 7.4.2 for more details.
	 * This is a handle-only method; the specified package 
	 * may or may not actually be present in the image.
	 *
	 * @parameter name the name of the package.
	 * @parameter isUnnamed a boolean indicating whether the package is unnamed.
	 * @see IPackage#getName
	 * @see IHandle
	 */
	IPackage getPackageHandle(String name, boolean isUnnamed);
	/**
	 * Returns an array of Package objects representing all
	 * packages contained in the given ImageContext.
	 * The result is the intersection of the packages present in this image
	 * and the packages in the ImageContext, so the resulting packages
	 * are all present in the image.
	 * The resulting Packages are in no particular order.
	 *
	 * @param context the ImageContext in which to restrict the search.
	 */
	IPackage[] getPackages(IImageContext context);
	/** Returns the Type representing the primitive type int. */
	IType intType();
	/**
	 * Returns true if the object represented by the receiver is present 
	 * in the development context, false otherwise.  If the receiver is 
	 * state-specific, checks whether it is present in this object's state.
	 * If the receiver is non-state-specific, checks whether it is present
	 * in the current state of the development context.  In the latter case,
	 * if there is no current state, returns false.
	 */
	boolean isPresent();
	/** Returns the Type representing the primitive type long. */
	IType longType();
	/** Returns the Type representing the primitive type short. */
	IType shortType();
	/**
	 * Returns a string describing this Image handle.  The string
	 * representation is the string <code>"image"</code>.
	 *
	 * @see IHandle#toString
	 */
	String toString();
	/** Returns the Type representing the keyword void. */
	IType voidType();
}
