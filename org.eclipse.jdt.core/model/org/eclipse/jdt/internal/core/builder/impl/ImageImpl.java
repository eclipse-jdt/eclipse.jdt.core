package org.eclipse.jdt.internal.core.builder.impl;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
import org.eclipse.core.runtime.IPath;

import org.eclipse.jdt.internal.core.builder.*;

public class ImageImpl
	extends NonStateSpecificHandleImpl 
	implements IImage {
		JavaDevelopmentContextImpl fDevelopmentContext;	

	/**
	 * Creates a new image
	 */
	public ImageImpl(JavaDevelopmentContextImpl dc) {
		fDevelopmentContext = dc;
	}
/**
 * Returns the Type representing the primitive type boolean.
 */
public IType booleanType() {
	return fDevelopmentContext.fBooleanType;
}
/**
 * Returns the Type representing the primitive type byte
 */
public org.eclipse.jdt.internal.core.builder.IType byteType() {
	return fDevelopmentContext.fByteType;
}
/**
 *Returns the Type representing the primitive type char
 */
public org.eclipse.jdt.internal.core.builder.IType charType() {
	return fDevelopmentContext.fCharType;
}
/**
 * Returns an Image Context consisting of all the given packages.
 * This object and the packages must all be non-state-specific.
 * This is a handle-only operation.  The packages need not
 *	be present in the image.
 */
public IImageContext createImageContext(IPackage[] packages) 
	throws StateSpecificException {
	// copy array and verify that the packages are non-state-specific
	IPackage[] pkgs = new IPackage[packages.length];
	for (int  i = 0; i < packages.length; i++) {
		if ((packages[i].kind() != K_JAVA_PACKAGE) ||
			(packages[i].isStateSpecific())) {
				throw new StateSpecificException();
		}
		pkgs[i] = packages[i];
	}
	// create the image context
	return new ImageContextImpl(fDevelopmentContext, pkgs);
}
/**
 * Returns the Type representing the primitive type double
 */
public org.eclipse.jdt.internal.core.builder.IType doubleType() {
	return fDevelopmentContext.fDoubleType;
}
	/**
	 * Compares this Image handle against the specified object.  Returns
	 *	true if the objects are the same.  Two Image handles are the same if
	 *	they belong to the same development context, and if they are both
	 *	non-state-specific or are both state-specific on the same state.
	 *	See Handle.equals() for more details.
	 */
	public boolean equals(Object o) {
		if (this == o) return true;
		if (!(o instanceof ImageImpl)) return false;

		ImageImpl image = (ImageImpl) o;
		return fDevelopmentContext.equals(image.fDevelopmentContext);
	}
/**
 * Returns the Type representing the primitive type float
 */
public org.eclipse.jdt.internal.core.builder.IType floatType() {
	return fDevelopmentContext.fFloatType;
}
/**
 * Returns an array containing Type objects representing all
 * classes and interfaces in the given ImageContext.
 * This includes public and default (package) access top-level 
 * classes, inner classes, and local inner classes.
 * The result is the intersection of all classes present in this image
 * and the classes in the ImageContext, so the resulting classes
 * are all present in the image.
 * The resulting Types are in no particular order.
 */
public IType[] getAllClasses(IImageContext context) {
	StateImpl state = (StateImpl) getDevelopmentContext().getCurrentState();
	IPackage[] pkgs = (context == null ? state.getPackageMap().getAllPackagesAsArray() : context.getPackages());
	java.util.Vector result = new java.util.Vector(pkgs.length * 25);
	for (int i = 0; i < pkgs.length; ++i) {
		TypeStructureEntry[] entries = state.getAllTypesForPackage(pkgs[i]);
		// entries is null if package is missing
		if (entries != null) {
			for (int j = 0, len = entries.length; j < len; ++j) {
				result.addElement(entries[j].getType());
			}
		}
	}
	// convert the Vector to an array
	IType[] types = new IType[result.size()];
	result.copyInto(types);
	return types;
}
	/**
	 * Returns an array of all packages present in the image.  Note that this
	 * method defies the concept of a potentially infinite image, and should only
	 * be used by clients that must operate over the entire image (search, code assist)
	 */
	public IPackage[] getAllPackages() {
		StateImpl state = (StateImpl)fDevelopmentContext.getCurrentState();
		return state.getPackageMap().getAllPackagesAsArray();
	}
	/**
	 * @see IImage
	 */
	public IType[] getBuiltClasses(IPath path) {
		return nonStateSpecific(((IImage) inCurrentState()).getBuiltClasses(path));
	}
/**
 * Return the internal representation of the development context that owns this object
 */
JavaDevelopmentContextImpl getInternalDC() {
	return fDevelopmentContext;
}
/**
 * Returns a handle representing the package with the given 
 * name.  For named packages, this is the fully qualified
 * name.  For unnamed packages, it is some internal identifying
 * string.
 * See <em>The Java Language Specification</em> section 7.4.1 and
 * 7.4.2 for more details.
 * This is a handle-only method; the specified package 
 * may or may not actually be present in the image.
 */
public IPackage getPackageHandle(String name, boolean isUnnamed) {
	return new PackageImpl(fDevelopmentContext, name, isUnnamed);
}
/**
 * Returns an array of Package objects representing all
 * packages contained in the given ImageContext.
 * The result is the intersection of the packages present in this image
 * and the packages in the ImageContext, so the resulting packages
 * are all present in the image.
 * The resulting Packages are in no particular order.
 */
public IPackage[] getPackages(IImageContext context) {
	if (context == null) {
		return ((StateImpl) fDevelopmentContext.getCurrentState()).getAllPackagesAsArray();
	}
	return nonStateSpecific(((IImage) inCurrentState()).getPackages(context));
}
	/**
	 * Returns a consistent hash code for this object
	 */
	public int hashCode() {
		return fDevelopmentContext.hashCode();
	}
/**
 * Returns the state wrapped handle
 */
public IHandle inState(IState state) {
	return state.getImage();
}
/**
 * Returns the Type representing the primitive type int
 */
public org.eclipse.jdt.internal.core.builder.IType intType() {
	return fDevelopmentContext.fIntType;
}
/**
 * kind method comment.
 */
public int kind() {
	return K_JAVA_IMAGE;
}
/**
 * Returns the Type representing the primitive type long
 */
public org.eclipse.jdt.internal.core.builder.IType longType() {
	return fDevelopmentContext.fLongType;
}
/**
 * Returns the Type representing the primitive type short
 */
public org.eclipse.jdt.internal.core.builder.IType shortType() {
	return fDevelopmentContext.fShortType;
}
	/**
	 * Returns a string representation of the image handle.	
	 */
	public String toString() {
		return "image";
	}
/**
 * Returns the Type representing the primitive type void
 */
public org.eclipse.jdt.internal.core.builder.IType voidType() {
	return fDevelopmentContext.fVoidType;
}
}
