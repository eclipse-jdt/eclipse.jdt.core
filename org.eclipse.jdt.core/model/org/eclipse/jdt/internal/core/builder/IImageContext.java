package org.eclipse.jdt.internal.core.builder;
/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
import org.eclipse.jdt.core.*;

/**
 * The <code>IImageContext</code> represents a subset of
 * a Java image.  ImageContext objects are used in queries, such as 
 * getSubclasses(), which require more context than just the local
 * definition of a class.  This is to allow the Image to contain 
 * a very large (conceptually infinite) number of packages, without 
 * forcing them all to be considered when performing such non-local
 * queries.
 * 
 * <p>For now, an ImageContext is defined as a set of non-state-specific packages.
 * The ImageContext itself is conceptually non-state-specific.
 * 
 * @see IImage#createImageContext
 * @see IImage#getPackages
 * @see IImage#getAllClasses
 * @see IType#getSubclasses
 */
public interface IImageContext 
{

	/**
	 * Returns the DevelopmentContext for which the ImageContext was created.
	 */
	IDevelopmentContext getDevelopmentContext();
	/**
	 * Returns the Packages of which this ImageContext consists.
	 * The resulting Packages are in no particular order.
	 *
	 * @return an array of non-state-specific Package handles.
	 */
	IPackage[] getPackages();
/**
 * Return a string of the form:
 *      image context with packages:
 *          this.data.package[0]
 *          this.data.package[1]
 *                  ...
 *          this.data.package[n-1]
 *          
 * The string returned is only for debugging purposes,
 * and the contents of the string may change in the future.
 * @return java.lang.String
 */
public String toString();
}
