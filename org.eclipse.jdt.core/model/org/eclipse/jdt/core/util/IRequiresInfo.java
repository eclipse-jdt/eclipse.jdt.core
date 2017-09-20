/*******************************************************************************
 * Copyright (c) 2017 IBM Corporation and others.
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
package org.eclipse.jdt.core.util;

/**
 * Description of a requires info as described in JVMS9 4.7.25
 *
 * @noimplement This interface is not intended to be implemented by clients.
 * @noextend This interface is not intended to be extended by clients.
 * @since 3.13 BETA_JAVA9
 */
public interface IRequiresInfo {

	/**
	 * Answer back the requires index.
	 *
	 * @return the requires index
	 */
	int getRequiresIndex();

	/**
	 * Answer back the requires module name.
	 *
	 * @return the requires module name
	 */
	char[] getRequiresModuleName();

	/**
	 * Answer back the requires flags.
	 *
	 * @return the requires flags
	 */
	int getRequiresFlags();

	/**
	 * Answer back the requires version index.
	 *
	 * @return the requires version index
	 */
	int getRequiresVersionIndex();

	/**
	 * Answer back the requires version string.
	 *
	 * @return the requires version string
	 */
	public char[] getRequiresVersionValue();

}
