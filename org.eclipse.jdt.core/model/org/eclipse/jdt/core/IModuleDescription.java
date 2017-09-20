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
 *
 * @noimplement This interface is not intended to be implemented by clients.
 * @since 3.13 BETA_JAVA9
 */
public interface IModuleDescription extends IMember {
	// empty block
}
