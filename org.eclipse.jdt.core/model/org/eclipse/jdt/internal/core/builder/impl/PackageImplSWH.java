package org.eclipse.jdt.internal.core.builder.impl;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
import org.eclipse.core.runtime.IPath;
import org.eclipse.jdt.internal.core.Util;
import org.eclipse.jdt.internal.core.builder.IHandle;
import org.eclipse.jdt.internal.core.builder.IImageContext;
import org.eclipse.jdt.internal.core.builder.IPackage;
import org.eclipse.jdt.internal.core.builder.ISourceFragment;
import org.eclipse.jdt.internal.core.builder.IType;
import org.eclipse.jdt.internal.core.builder.NotPresentException;

public class PackageImplSWH extends StateSpecificHandleImpl implements IPackage {
	PackageImpl fHandle;
	/**
	 * Internal - Create a new Package
	 */
	PackageImplSWH(StateImpl state, PackageImpl handle) throws NotPresentException {
		if (state == null) throw new NotPresentException();
		fState = state;
		fHandle = handle;
	}
	/**
	 * Returns an array containing Type objects representing all
	 * classes and interfaces in the package represented by this object.
	 * This includes public and default (package) access top-level 
	 * classes, inner classes, and local inner classes.
	 * Returns an array of length 0 if this package has no
	 * classes or interfaces.
	 * The Types are in no particular order.
	 * This is a slow method.  getDeclaredClasses() should be used for most cases.
	 */
	public IType[] getAllClasses() throws NotPresentException {

		TypeStructureEntry[] entries = fState.getAllTypesForPackage(fHandle);
		if (entries == null) {
			throw new NotPresentException();
		}
		IType[] results = new IType[entries.length];
		for (int i = 0, num = entries.length; i < num; ++i) {
			results[i] = (IType)entries[i].getType().inState(fState);
		}
		return results;
	}
/**
 * getClassHandle method comment.
 * Returns a handle representing the class or interface
 * with the given name.  The name is the VM class name,
 * not including the package name.
 * For inner classes, the name is as described in the 
 * <em>Inner Classes Specification</em>.
 * This is a handle-only method; the specified class 
 * may or may not actually be present in the image.
 */
public IType getClassHandle(String name) {
	return (IType)fHandle.getClassHandle(name).inState(fState);
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
	TypeStructureEntry[] entries = fState.getAllTypesForPackage(fHandle);
	if (entries == null) {
		throw new NotPresentException();
	}
	int num = entries.length;
	IType[] results = new IType[num];
	int count = 0;
	for (int i = 0; i < num; ++i) {
		if (BinaryStructure.isPackageMember(fState.getBinaryType(entries[i]))) {
			results[count++] = (IType) entries[i].getType().inState(fState);
		}
	}
	if (count < num) {
		System.arraycopy(results, 0, results = new IType[count], 0, count);
	}
	return results;
}
/**
 * 	Returns the fully-qualified name of the package represented 
 * 	by this object, as a String. 
 * 	If the package is unnamed, returns the internal identifier
 * 	string of this unnamed packaged.
 * 	This is a handle-only method.
 */
public String getName() {
	return fHandle.getName();
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
	if (!isPresent())
		throw new NotPresentException();
	IPackage[] pkgs = fState.getReferencedPackages((IPackage)nonStateSpecific());

	/* wrapped returned packages in state handles */
	for (int i = 0; i < pkgs.length; i++) {
		pkgs[i] = (IPackage) pkgs[i].inState(fState);
	}
	return pkgs;
		
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
	public IPackage[] getReferencingPackages(IImageContext context) 
	  throws NotPresentException {
		if (!isPresent())
			throw new NotPresentException(Util.bind("element.notPresent"/*nonNLS*/));

		IPackage[] pkgs = fState.getReferencingPackages(fHandle, context);

		/* wrap packages in state */
		for (int i = 0; i < pkgs.length; i++) {
			pkgs[i] = (IPackage)pkgs[i].inState(fState);
		}
		return pkgs;
	}
/**
 * Returns an array of SourceFragments describing the source package 
 * fragments from which this built package is derived.
 * Returns an empty array if this package is not derived directly from source
 * The source coordinates in the results are set to #(-1, -1).
 *
 * If this is a default package, we must resolve the project name from the
 * internal identifier
 */
public ISourceFragment[] getSourceFragments() throws NotPresentException {

	IPath[] paths = fState.getPackageMap().getFragments(fHandle);
	if (paths == null) {
		throw new NotPresentException();
	}
	int max = paths.length;
	ISourceFragment[] frags = new ISourceFragment[max];
	for (int i = 0; i < max; i++) {
		frags[i] = new SourceFragmentImpl(-1, -1, paths[i]);
	}
	return frags;
}
/**
 * isPresent method comment.
 */
public boolean isPresent() {
	return fState.getPackageMap().containsPackage(fHandle);
}
/**
 * Returns true if this package is an unnamed package, false 
 * otherwise.  See <em>The Java Language Specification</em>, 
 * sections 7.4.1 and 7.4.2, for details.
 * This is a handle-only method.
 */
public boolean isUnnamed() {
	return fHandle.isUnnamed();
}
	/**
	  * Returns the non state specific handle
	  */
	 public IHandle nonStateSpecific() {
		 return fHandle;
	 }
}
