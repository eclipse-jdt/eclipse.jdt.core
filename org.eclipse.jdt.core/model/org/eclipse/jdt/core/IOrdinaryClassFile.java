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
 * Ordinary form of a {@link IClassFile} which holds exactly one <code>IType</code>.
 * 
 * @since 3.13 BETA_JAVA9
 * @noimplement This interface is not intended to be implemented by clients.
 */
public interface IOrdinaryClassFile extends IClassFile {
	/**
	 * Returns the type contained in this class file.
	 * This is a handle-only method. The type may or may not exist.
	 *
	 * <p>This method supersedes the corresponding super method.
	 * This method will never throw {@link UnsupportedOperationException}.</p>
	 *
	 * @return the type contained in this class file
	 */
	@Override
	IType getType();
}
