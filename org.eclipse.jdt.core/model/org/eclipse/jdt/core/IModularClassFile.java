/*******************************************************************************
 * Copyright (c) 2017 GK Software AG, and others.
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
 *     Stephan Herrmann - initial API and implementation
 *******************************************************************************/
package org.eclipse.jdt.core;

/**
 * Represents the class file of a module description ("module-info.class").
 *
 * @since 3.13 BETA_JAVA9
 * @noimplement This interface is not intended to be implemented by clients.
 */
public interface IModularClassFile extends IClassFile {
	/**
	 * Returns the module description contained in this type root.
	 * An error-free {@link IModularClassFile} should always have a module.
	 *
	 * @throws JavaModelException 
	 * @return the module description contained in the type root.
	 */
	@Override
	IModuleDescription getModule() throws JavaModelException;
}
