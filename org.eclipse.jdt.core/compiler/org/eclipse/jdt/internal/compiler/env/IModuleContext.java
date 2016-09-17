/*******************************************************************************
 * Copyright (c) 2016 IBM Corporation and others.
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
package org.eclipse.jdt.internal.compiler.env;

import java.util.stream.Stream;

/**
 * The context in which a module aware environment will perform lookup.
 * Basically a supplier of {@link IModuleEnvironment}s to be included
 * during a lookup operation
 *
 */
public interface IModuleContext {
	/**
	 * A special context to represent the unnamed module context
	 * 
	 */
	IModuleContext UNNAMED_MODULE_CONTEXT = () -> {
		return Stream.empty();
	};
	/**
	 * Return the environments that are included in this context
	 * 
	 */
	public Stream<IModuleEnvironment> getEnvironment();
	
	/**
	 * Include the other module context in this context
	 * 
	 * @param other
	 * 
	 * @return A joint context that represents both the module contexts
	 */
	default IModuleContext include(IModuleContext other) {
		return () -> Stream.concat(getEnvironment(), other.getEnvironment());
	}
	/**
	 * Include all the other contexts in this context
	 * 
	 * @param other
	 * 
	 * @return A joint context that represents all the module contexts
	 */
	default IModuleContext includeAll(Stream<IModuleContext> other) {
		return () -> Stream.concat(getEnvironment(), other.flatMap(c -> c.getEnvironment()));
	}
}
