/*******************************************************************************
 * Copyright (c) 2017 IBM Corporation.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * This is an implementation of an early-draft specification developed under the Java
 * Community Process (JCP) and is made available for testing and evaluation purposes
 * only. The code is not compatible with any specification of the JCP.
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jdt.core;

/**
 * Represents a Java module descriptor. The module description could either come from source or binary.
 * A simple module looks like the following:
 * <pre>
 * module my.module {
 * 		exports my.pack1;
 * 		exports my.pack2;
 * 		requires java.sql;
 * }
 * </pre>
 * In this example, the Java module descriptor contains two <code>exports</code> 
 * statements ({@link IPackageExport}) and a <code>requires</code> clause ({@link IModuleReference})
 *
 * @since 3.13 BETA_JAVA9
 */
public interface IModuleDescription extends IMember {

	/**
	 * Returns all the packages exported by this module. The exports appear
	 * in the order they are declared in the source or class file.
	 *
	 * @exception JavaModelException if this element does not exist or if an
	 *		exception occurs while accessing its corresponding resource.
	 * @return the exported packages
	 */
	public IPackageExport[] getExportedPackages() throws JavaModelException;

	/**
	 * Returns all the modules this module requires. The required modules appear
	 * in the order they are declared in the source or class file.
	 *
	 * @exception JavaModelException if this element does not exist or if an
	 *		exception occurs while accessing its corresponding resource.
	 * @return the required modules
	 */
	public IModuleReference[] getRequiredModules() throws JavaModelException;

	/**
	 * Returns all the services that this module provides.
	 *
	 * @exception JavaModelException if this element does not exist or if an
	 *		exception occurs while accessing its corresponding resource.
	 * @return services provided by this module
	 */
	public IProvidedService[] getProvidedServices() throws JavaModelException;

	/**
	 * Returns all the services that this module declares to be using.
	 *
	 * @exception JavaModelException if this element does not exist or if an
	 *		exception occurs while accessing its corresponding resource.
	 * @return the services used by this module.
	 */
	public String[] getUsedServices() throws JavaModelException;
	/**
	 * 
	 */
	public IOpenPackage[] getOpenedPackages() throws JavaModelException;

	/**
	 * Represents a <code>exports</code> statement in a Java module description.
	 */
	public interface IPackageExport extends IJavaElement, ISourceReference {

		/**
		 * Returns the name of the package being exported.
		 *	TODO: This is redundant, can easily be fulfilled with getElementName()
		 * @return the exported package name
		 */
		public String getPackageName();

		/**
		 * Returns the modules that this package is specifically being
		 * exported to. 
		 * 
		 * @return the modules that this package is being exported to.
		 * @throws JavaModelException
		 */
		String[] getTargetModules() throws JavaModelException;
	}

	/**
	 * Represents a <code>requires</code> clause in a Java module description.
	 */
	public interface IModuleReference  extends IJavaElement, ISourceReference {

		/**
		 * Returns the name of the required module.
		 * TODO: This is redundant, can easily be fulfilled with getElementName()
		 * @return the required module name
		 */
		public String getModuleName();

		/**
		 * Specifies whether this module is being re-exported by depending module. In other words,
		 * the package and types defined in this module will be visible to other modules that declare the
		 * referring module in their <code>requires</code> clause.
		 *
		 * @return whether this module is made public by the referring module.
		 * @throws JavaModelException
		 */
		public boolean isPublic() throws JavaModelException;
	}
	/**
	 * Represents a <code>provides with</code> clause in a Java module description 
	 */
	public interface IProvidedService extends IJavaElement, ISourceReference {
		/**
		 * Returns the name of the service being provided, which is usually the
		 * fully qualified name of the interface.
		 *
		 * @return the service name
		 */
		public String getServiceName();
		/**
		 * Returns the name of the class that provides an implementation for the declared 
		 * service. This is usually the fully qualified name of the implementing type.
		 *
		 * @return the name of the implementation
		 */
		public String[] getImplementationNames();
	}
	/**
	 * 
	 */
	public interface IOpenPackage extends IPackageExport {
		// Essentially same as IPackageExport
	}
}
