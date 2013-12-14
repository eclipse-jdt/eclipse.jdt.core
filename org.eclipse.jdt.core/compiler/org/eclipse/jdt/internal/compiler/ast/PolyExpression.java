/*******************************************************************************
 * Copyright (c) 2013 GK Software AG.
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
package org.eclipse.jdt.internal.compiler.ast;

/**
 * Abstraction of AST nodes that can be poly expressions and participate in type inference.
 * (not included: ConditionalExpression).
 */
public interface PolyExpression {

	/** Mark that inference for this expression has finished. */
	void markInferenceFinished();

	/** Answer whether type inference for this expression has finished (if needed). */
	boolean hasInferenceFinished();
}
