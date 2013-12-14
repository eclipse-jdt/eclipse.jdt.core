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
package org.eclipse.jdt.internal.compiler.lookup;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Implementation of 18.1.2 in JLS8
 */
abstract class ConstraintFormula extends ReductionResult {

	static final List EMPTY_VARIABLE_LIST = Collections.EMPTY_LIST;
	static final ConstraintFormula[] NO_CONSTRAINTS = new ConstraintTypeFormula[0];

	// constants for unicode debug output from ASCII source files:
	static final char LEFT_ANGLE_BRACKET = '\u27E8';
	static final char RIGHT_ANGLE_BRACKET = '\u27E9';

	public abstract Object reduce(InferenceContext18 inferenceContext) throws InferenceFailureException;

	/** 5.3: compatibility check which includes the option of boxing/unboxing. */
	protected boolean isCompatibleWithInLooseInvocationContext(TypeBinding one, TypeBinding two, InferenceContext18 context) {
		if (one.isCompatibleWith(two, context.scope))
			return true;
		if (one.isBaseType()) {
			if (one != TypeBinding.NULL && !two.isBaseType()) {
				TypeBinding boxingType = context.environment.computeBoxingType(one);
				if (boxingType != one) //$IDENTITY-COMPARISON$ just checking if boxing could help
					return boxingType.isCompatibleWith(two, context.scope);
			}
		} else if (two.isBaseType() && two != TypeBinding.NULL) {
			TypeBinding boxingType = context.environment.computeBoxingType(two);
			if (boxingType != two) //$IDENTITY-COMPARISON$ just checking if boxing could help
				return one.isCompatibleWith(boxingType, context.scope);
		}
		return false;
	}

	Collection inputVariables(InferenceContext18 context) {
		return EMPTY_VARIABLE_LIST;
	}
	
	Collection outputVariables(InferenceContext18 context) {
		Set variables = new HashSet();
		this.right.collectInferenceVariables(variables);
		variables.removeAll(inputVariables(context));
		return variables;
	}

	public void applySubstitution(BoundSet solutionSet, InferenceVariable[] variables) {
		for (int i=0; i<variables.length; i++) {
			InferenceVariable variable = variables[i];
			TypeBinding instantiation = solutionSet.getInstantiation(variables[i]);
			this.right = this.right.substituteInferenceVariable(variable, instantiation);
		}
	}

	// for debug toString():
	protected void appendTypeName(StringBuffer buf, TypeBinding type) {
		if (type instanceof CaptureBinding18)
			buf.append(type.toString()); // contains more info than readable name
		else
			buf.append(type.readableName());
	}
}
