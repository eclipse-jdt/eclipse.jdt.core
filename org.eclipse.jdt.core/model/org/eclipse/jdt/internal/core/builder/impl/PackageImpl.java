package org.eclipse.jdt.internal.core.builder.impl;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
import org.eclipse.jdt.internal.core.Util;
import org.eclipse.jdt.internal.core.builder.IHandle;
import org.eclipse.jdt.internal.core.builder.IImageContext;
import org.eclipse.jdt.internal.core.builder.IPackage;
import org.eclipse.jdt.internal.core.builder.ISourceFragment;
import org.eclipse.jdt.internal.core.builder.IState;
import org.eclipse.jdt.internal.core.builder.IType;
import org.eclipse.jdt.internal.core.builder.NotPresentException;
import org.eclipse.jdt.internal.core.builder.StateSpecificException;

public class PackageImpl extends NonStateSpecificHandleImpl implements IPackage {
	JavaDevelopmentContextImpl fDevelopmentContext;
	String fName;
	boolean fIsUnnamed;

	public static final String DEFAULT_PACKAGE_PREFIX = "Default-"/*nonNLS*/;
/**
 * Creates a new package
 * @param name of package
 * @param isUnnamed whether the package is unnamed
 */
PackageImpl(JavaDevelopmentContextImpl ctx, String name, boolean isUnnamed) {
	fName = name;
	fIsUnnamed = isUnnamed;
	fDevelopmentContext = ctx;
}
	/**
	 * Appends the signature for this package to the given StringBuffer.
	 * If includeUnnamed is true, then the identifiers for unnamed packages
	 * are included, preceded by '$'.  Otherwise, they are excluded.
	 * Returns true if a signature was written, false otherwise.
	 */
	boolean appendSignature(StringBuffer sb, boolean includeUnnamed) {
		if (includeUnnamed || !fIsUnnamed) {
			sb.append(fName);
			return true;
		}
		else {
			return false;
		}
	}
	/**
	 * Compares this object against the specified object.
	 *	Returns true if the objects are the same.
	 */
	public boolean equals(Object o) {
		if (this == o) return true;
		if (!(o instanceof PackageImpl)) return false;

		PackageImpl pkg = (PackageImpl) o;
		return fName.equals(pkg.fName) 
				&& fIsUnnamed == pkg.fIsUnnamed
				&& fDevelopmentContext.equals(pkg.fDevelopmentContext);
	}
/**
 * Returns an array containing Type objects representing all
 * classes and interfaces in the package represented by this object.
 * This includes public and default (package) access top-level 
 * classes, inner classes, and local inner classes.
 * Returns an array of length 0 if this package has no
 * classes or interfaces.
 * The Types are in no particular order.
 */
public IType[] getAllClasses() throws NotPresentException {
	return nonStateSpecific(inCurrentState0().getAllClasses());
}
	/**
	 * Returns a handle representing the class or interface
	 * with the given name.  The name is the VM class name,
	 * not including the package name.
	 * For inner classes, the name is as described in the 
	 * <em>Inner Classes Specification</em>.
	 * This is a handle-only method; the specified class 
	 * may or may not actually be present in the image.
	 */
	public IType getClassHandle(String name) {
		return new ClassOrInterfaceHandleImpl(name, this);
	}
/**
 * Returns an array of Type objects representing all the classes
 * and interfaces declared as members of the package represented by
 * this object. This includes public and default (package) access
 * classes and interfaces declared as members of the package. 
 * This does not include inner classes and interfaces.
 * Returns an array of length 0 if this package declares no classes
 * or interfaces as members.
 * The Types are in no particular order.
 */
public IType[] getDeclaredClasses() throws NotPresentException {
	return nonStateSpecific(inCurrentState0().getDeclaredClasses());
}
/**
 * Return the internal representation of the development context that owns this object
 */
JavaDevelopmentContextImpl getInternalDC() {
	return fDevelopmentContext;
}
/**
 * 	Returns the fully-qualified name of the package represented 
 * 	by this object, as a String. 
 * 	If the package is unnamed, returns the internal identifier
 * 	string of this unnamed packaged.
 * 	This is a handle-only method.
 */
public String getName() {
	return fName;
}
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
 */
public IPackage[] getReferencedPackages() throws NotPresentException {
	return nonStateSpecific(inCurrentState0().getReferencedPackages());
}
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
 */
public IPackage[] getReferencingPackages(IImageContext context) throws NotPresentException 
{
	return nonStateSpecific(inCurrentState0().getReferencingPackages(context));
}
/**
 * Returns an array of SourceFragments describing the source package 
 * fragments from which this built package is derived.
 * Returns an empty array if this package is not derived directly from source
 * (e.g. package com.oti.requiem.fictional).
 * The source coordinates in the results are set to #(1 0).
 */
public ISourceFragment[] getSourceFragments() throws NotPresentException {
	return inCurrentState0().getSourceFragments();
}
	/**
	 * Returns a consistent hash code for this object
	 */
	public int hashCode() {
		return fName.hashCode();
	}
	/**
	 * Returns a state-specific version of this handle in the current state
	 */
	private PackageImplSWH inCurrentState0() {
		return new PackageImplSWH(fDevelopmentContext.fCurrentState, this);
	}
	/**
	 * Returns a state specific version of this handle in the given state.
	 */
	public IHandle inState(IState s) throws org.eclipse.jdt.internal.core.builder.StateSpecificException {
		return new PackageImplSWH((StateImpl) s, this);
	}
/**
 * Returns true if this package is an unnamed package, false 
 * otherwise.  See <em>The Java Language Specification</em>, 
 * sections 7.4.1 and 7.4.2, for details.
 * This is a handle-only method.
 */
public boolean isUnnamed() {
	return fIsUnnamed;
}
	/**
	 * Returns a constant indicating what kind of handle this is.
	 */
	public int kind() {
		return K_JAVA_PACKAGE;
	}
/**
 * Returns the readable name for the given package,
 * suitable for use in progress messages.
 */
public static String readableName(IPackage pkg) {
	String name = pkg.getName();
	if (pkg.isUnnamed()) {
		return Util.bind("build.defaultPackageName"/*nonNLS*/, name.substring(DEFAULT_PACKAGE_PREFIX.length()));
	} else {
		return Util.bind("build.packageName"/*nonNLS*/, name);
	}
}
	/**
	 * Returns a string representation of the package.  For debugging purposes
	 * only (NON-NLS).
	 */
	public String toString() {
		String result = "package "/*nonNLS*/;
		if (isUnnamed()) 
			result += "{unnamed, id="/*nonNLS*/ + getName() + "}"/*nonNLS*/;
		else
			result += getName();
		return result;
	}
}
