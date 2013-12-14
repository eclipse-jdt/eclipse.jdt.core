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

import org.eclipse.jdt.internal.compiler.lookup.InferenceContext18;
import org.eclipse.jdt.internal.compiler.lookup.InvocationSite;
import org.eclipse.jdt.internal.compiler.lookup.MethodBinding;
import org.eclipse.jdt.internal.compiler.lookup.TypeBinding;

/**
 * Abstraction for AST nodes that can trigger 
 * <ul>
 * <li>Invocation Applicability Inferences (18.5.1), and</li> 
 * <li>Invocation Type Inference (18.5.2).</li>
 * </ul>
 */
public interface Invocation extends InvocationSite, PolyExpression {

	Expression[] arguments();

	MethodBinding binding();

	InferenceContext18 inferenceContext();

	/** See {@link #inferenceContext()}. */
	void setInferenceKind(int checkKind);

	/**
	 * Answer one of {@link InferenceContext18#CHECK_STRICT}, {@link InferenceContext18#CHECK_LOOSE} 
	 * or {@link InferenceContext18#CHECK_VARARG}, to signal what kind of inference has been used.
	 */
	int inferenceKind();

	/**
	 * Where the AST node may hold references to the results of Invocation Applicability Inference,
	 * this method allows to update those references to the result of Invocation Type Inference.
	 * Note that potentially more than just the method binding is updated.
	 */
	TypeBinding updateBindings(MethodBinding updatedBinding);

}
