package org.eclipse.jdt.internal.core.builder;

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
 * 		image context with packages:
 * 			this.data.package[0]
 * 			this.data.package[1]
 *					...
 * 			this.data.package[n-1]
 * 			
 * The string returned is only for debugging purposes,
 * and the contents of the string may change in the future.
 * @return java.lang.String
 */
public String toString();
}
