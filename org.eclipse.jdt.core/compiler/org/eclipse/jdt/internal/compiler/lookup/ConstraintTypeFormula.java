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

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jdt.internal.compiler.ast.Wildcard;

/**
 * Implementation of 18.1.2 in JLS8, cases:
 * <ul>
 * <li>S -> T <em>compatible</em></li>
 * <li>S <: T <em>subtype</em></li>
 * <li>S = T  <em>equality</em></li>
 * <li>S <= T <em>type argument containment</em></li>
 * </ul>
 */
class ConstraintTypeFormula extends ConstraintFormula {

	TypeBinding left;
	
	// this flag contributes to the workaround controlled by InferenceContext18.ARGUMENT_CONSTRAINTS_ARE_SOFT:
	boolean isSoft;

	public ConstraintTypeFormula(TypeBinding exprType, TypeBinding right, int relation) {
		this.left = exprType;
		this.right = right;
		this.relation = relation;
	}
	public ConstraintTypeFormula(TypeBinding exprType, TypeBinding right, int relation, boolean isSoft) {
		this(exprType, right, relation);
		this.isSoft = isSoft;
	}

	// return: ReductionResult or ConstraintFormula[]
	public Object reduce(InferenceContext18 inferenceContext) {
		switch (this.relation) {
		case COMPATIBLE:
			// 18.2.2:
			if (this.left.isProperType(true) && this.right.isProperType(true)) {
				if (isCompatibleWithInLooseInvocationContext(this.left, this.right, inferenceContext))
					return TRUE;
				return FALSE;
			}
			if (this.left.isBaseType() && this.left != TypeBinding.NULL) {
				TypeBinding sPrime = inferenceContext.environment.computeBoxingType(this.left);
				return new ConstraintTypeFormula(sPrime, this.right, COMPATIBLE, this.isSoft);
			}
			if (this.right.isBaseType() && this.right != TypeBinding.NULL) {
				TypeBinding tPrime = inferenceContext.environment.computeBoxingType(this.right);
				return new ConstraintTypeFormula(this.left, tPrime, COMPATIBLE, this.isSoft);
			}
			switch (this.right.kind()) {
			case Binding.ARRAY_TYPE:
				if (this.right.leafComponentType().kind() != Binding.PARAMETERIZED_TYPE)
					break;
				//$FALL-THROUGH$ array of parameterized is handled below:
			case Binding.PARAMETERIZED_TYPE:
				{																
					//															  this.right = G<T1,T2,...> or G<T1,T2,...>[]k
					TypeBinding gs = this.left.findSuperTypeOriginatingFrom(this.right);	// G<S1,S2,...> or G<S1,S2,...>[]k
					if (gs != null && gs.leafComponentType().isRawType())
						return TRUE;
					break;
				}
			}
			return new ConstraintTypeFormula(this.left, this.right, SUBTYPE, this.isSoft);
		case SUBTYPE:
			// 18.2.3:
			return reduceSubType(inferenceContext.scope, this.left, this.right);
		case SUPERTYPE:
			// 18.2.3:
			return reduceSubType(inferenceContext.scope, this.right, this.left);
		case SAME:
			// 18.2.4:
			return reduceTypeEquality();
		case TYPE_ARGUMENT_CONTAINED:
			// 18.2.3:
			if (this.right.kind() != Binding.WILDCARD_TYPE) { // "If T is a type" ... all alternatives require "wildcard"
				if (this.left.kind() != Binding.WILDCARD_TYPE) {
					return new ConstraintTypeFormula(this.left, this.right, SAME, this.isSoft);						
				} else {
					return FALSE;
				}
			} else {
				WildcardBinding t = (WildcardBinding) this.right;
				if (t.boundKind == Wildcard.UNBOUND || t.bound.id == TypeIds.T_JavaLangObject)
					return TRUE;
				if (t.boundKind == Wildcard.EXTENDS) {
					if (this.left.kind() != Binding.WILDCARD_TYPE) {
						return new ConstraintTypeFormula(this.left, t.bound, SUBTYPE, this.isSoft);
					} else {
						WildcardBinding s = (WildcardBinding) this.left;
						if (s.boundKind == Wildcard.EXTENDS) {
							return new ConstraintTypeFormula(s.bound, t.bound, SUBTYPE, this.isSoft);
						} else {
							return FALSE;
						}
					}
				} else { // SUPER 
					if (this.left.kind() != Binding.WILDCARD_TYPE) {
						return new ConstraintTypeFormula(t.bound, this.left, SUBTYPE, this.isSoft);
					} else {
						WildcardBinding s = (WildcardBinding) this.left;
						if (s.boundKind == Wildcard.SUPER) {
							return new ConstraintTypeFormula(t.bound, s.bound, SUBTYPE, this.isSoft);
						} else {
							return FALSE;
						}
					}
				}
			}
		default: throw new IllegalStateException("Unexpected relation kind "+this.relation); //$NON-NLS-1$
		}
	}

	private Object reduceTypeEquality() {
		// 18.2.4
		if (this.left.kind() == Binding.WILDCARD_TYPE) {
			if (this.right.kind() == Binding.WILDCARD_TYPE) {
				WildcardBinding leftWC = (WildcardBinding)this.left;
				WildcardBinding rightWC = (WildcardBinding)this.right;
				if (leftWC.bound == null && rightWC.bound == null)
					return TRUE;
				if ((leftWC.boundKind == Wildcard.EXTENDS && rightWC.boundKind == Wildcard.EXTENDS)
					||(leftWC.boundKind == Wildcard.SUPER && rightWC.boundKind == Wildcard.SUPER))
				{
					return new ConstraintTypeFormula(leftWC.bound, rightWC.bound, SAME, this.isSoft);
				}						
			}
		} else {
			if (this.right.kind() != Binding.WILDCARD_TYPE) {
				// left and right are types (vs. wildcards)
				if (this.left.isProperType(true) && this.right.isProperType(true)) {
					if (TypeBinding.equalsEquals(this.left, this.right))
						return TRUE;
					return FALSE;
				}
				if (this.left instanceof InferenceVariable) {
					return new TypeBound((InferenceVariable) this.left, this.right, SAME, this.isSoft);
				}
				if (this.right instanceof InferenceVariable) {
					return new TypeBound((InferenceVariable) this.right, this.left, SAME, this.isSoft);
				}
				if (TypeBinding.equalsEquals(this.left.original(), this.right.original())) {
					TypeBinding[] leftParams = this.left.typeArguments();
					TypeBinding[] rightParams = this.right.typeArguments();
					if (leftParams == null || rightParams == null)
						return leftParams == rightParams ? TRUE : FALSE;
					if (leftParams.length != rightParams.length)
						return FALSE;
					int len = leftParams.length;
					ConstraintFormula[] constraints = new ConstraintFormula[len];
					for (int i = 0; i < len; i++) {
						constraints[i] = new ConstraintTypeFormula(leftParams[i], rightParams[i], SAME, this.isSoft);
					}
					return constraints;
				}
				if (this.left.isArrayType() && this.right.isArrayType() && this.left.dimensions() == this.right.dimensions()) {
					// checking dimensions already now is an optimization over reducing one dim at a time
					return new ConstraintTypeFormula(this.left.leafComponentType(), this.right.leafComponentType(), SAME, this.isSoft);
				}
				if (this.left.kind() == Binding.INTERSECTION_TYPE && this.right.kind() == Binding.INTERSECTION_TYPE) {
					InferenceContext18.missingImplementation("Intersection type equality NYI"); //$NON-NLS-1$
				}
			}
		}
		return FALSE;
	}

	private Object reduceSubType(Scope scope, TypeBinding subCandidate, TypeBinding superCandidate) {
		// 18.2.3 Subtyping Constraints
		if (subCandidate.isProperType(true) && superCandidate.isProperType(true)) {
			if (subCandidate.isCompatibleWith(superCandidate, scope))
				return TRUE;
			return FALSE;
		}
		if (subCandidate instanceof InferenceVariable)
			return new TypeBound((InferenceVariable)subCandidate, superCandidate, SUBTYPE, this.isSoft);
		if (superCandidate instanceof InferenceVariable)
			return new TypeBound((InferenceVariable)superCandidate, subCandidate, SUPERTYPE, this.isSoft); // normalize to have variable on LHS
		if (subCandidate.id == TypeIds.T_null)
			return TRUE;
		switch (superCandidate.kind()) {
			case Binding.GENERIC_TYPE:
			case Binding.TYPE:
			case Binding.RAW_TYPE: // TODO: check special handling of raw types?
				{
					ReferenceBinding c = (ReferenceBinding) superCandidate;
					if (subCandidate instanceof ReferenceBinding) {
						ReferenceBinding s = (ReferenceBinding) subCandidate;
						if (TypeBinding.equalsEquals(s.original(), c))
							return TRUE;
						if (TypeBinding.equalsEquals(s.superclass(), c))
							return TRUE;
						ReferenceBinding[] superInterfaces = s.superInterfaces();
						if (superInterfaces != null) {
							for (int i=0, l=superInterfaces.length; i<l; i++)
								if (TypeBinding.equalsEquals(superInterfaces[i], c))
									return TRUE;
						}
					}
					return FALSE;
				}
			case Binding.PARAMETERIZED_TYPE:
				{
					List constraints = new ArrayList();
					while (superCandidate instanceof ParameterizedTypeBinding && subCandidate != null)  {
						if (!addConstraintsFromTypeParamters(subCandidate, (ParameterizedTypeBinding) superCandidate, constraints))
							return FALSE;
						// travel to enclosing types to check if they have type parameters, too:
						// (Note: this is not explicit in the spec but has been confirmed on the EG list, see:
						//  http://mail.openjdk.java.net/pipermail/lambda-spec-experts/2013-December/000449.html ).
						superCandidate = superCandidate.enclosingType();
						subCandidate = subCandidate.enclosingType();
					}
					switch (constraints.size()) {
						case 0 : return TRUE;
						case 1 : return constraints.get(0);
						default: return constraints.toArray(new ConstraintFormula[constraints.size()]);
					}
				}
			case Binding.ARRAY_TYPE:
				TypeBinding tPrime = ((ArrayBinding)superCandidate).elementsType();
				// let S'[] be the most specific array type that is a supertype of S (or S itself)
				ArrayBinding sPrimeArray = null;
				switch(subCandidate.kind()) {
				case Binding.INTERSECTION_TYPE:
					{
						WildcardBinding intersection = (WildcardBinding) subCandidate;
						int numArrayBounds = 0;
						if (intersection.bound.isArrayType()) numArrayBounds++;
						for (int i = 0; i < intersection.otherBounds.length; i++) {
							if (intersection.otherBounds[i].isArrayType()) numArrayBounds++;
						}
						if (numArrayBounds == 0)
							return FALSE;
						InferenceContext18.missingImplementation("Cannot filter most specific array type"); //$NON-NLS-1$
						// FIXME assign sPrime
						break;
					}
				case Binding.ARRAY_TYPE:
					sPrimeArray = (ArrayBinding) subCandidate;
					break;
				default:					
					return FALSE;
				}
				TypeBinding sPrime = sPrimeArray.elementsType();
				if (!tPrime.isBaseType() && !sPrime.isBaseType()) {
					return new ConstraintTypeFormula(sPrime, tPrime, SUBTYPE, this.isSoft);
				}
				return TypeBinding.equalsEquals(tPrime, sPrime) ? TRUE : FALSE; // same primitive type?

			// "type variable" has two implementations in JDT:
			case Binding.WILDCARD_TYPE:
				// TODO If S is an intersection type of which T is an element, the constraint reduces to true. 
				if (subCandidate.kind() == Binding.INTERSECTION_TYPE)
					InferenceContext18.missingImplementation("NYI"); //$NON-NLS-1$
				WildcardBinding variable = (WildcardBinding) superCandidate;
				if (variable.boundKind == Wildcard.SUPER)
					return new ConstraintTypeFormula(subCandidate, variable.bound, SUBTYPE, this.isSoft);
				return FALSE;
			case Binding.TYPE_PARAMETER:
				// same as wildcard (but we don't have a lower bound any way)
				// TODO If S is an intersection type of which T is an element, the constraint reduces to true.
				if (subCandidate.kind() == Binding.INTERSECTION_TYPE)
					InferenceContext18.missingImplementation("NYI"); //$NON-NLS-1$
				return FALSE;
			case Binding.INTERSECTION_TYPE:
				InferenceContext18.missingImplementation("NYI"); //$NON-NLS-1$
		}
		if (superCandidate.id == TypeIds.T_null)
			return FALSE;
		throw new IllegalStateException("Unexpected RHS "+superCandidate); //$NON-NLS-1$
	}

	boolean addConstraintsFromTypeParamters(TypeBinding subCandidate, ParameterizedTypeBinding ca, List constraints) {
		TypeBinding[] ai = ca.arguments;								// C<A1,A2,...>
		if (ai == null)
			return true; // no arguments here means nothing to check
		TypeBinding cb = subCandidate.findSuperTypeOriginatingFrom(ca);	// C<B1,B2,...>
		if (cb == null)
			return false; // nothing here means we failed 
		TypeBinding[] bi = ((ParameterizedTypeBinding) cb).arguments;
		if (cb.isRawType() || bi.length == 0)
			return (this.isSoft && InferenceContext18.SIMULATE_BUG_JDK_8026527) ? true : false; // FALSE would conform to the spec 
		for (int i = 0; i < ai.length; i++)
			constraints.add(new ConstraintTypeFormula(bi[i], ai[i], TYPE_ARGUMENT_CONTAINED, this.isSoft));
		return true;
	}

	public void applySubstitution(BoundSet solutionSet, InferenceVariable[] variables) {
		super.applySubstitution(solutionSet, variables);
		for (int i=0; i<variables.length; i++) {
			InferenceVariable variable = variables[i];
			TypeBinding instantiation = solutionSet.getInstantiation(variables[i]);
			this.left = this.left.substituteInferenceVariable(variable, instantiation);
		}
	}

	// debugging
	public String toString() {
		StringBuffer buf = new StringBuffer("Type Constraint:\n"); //$NON-NLS-1$
		buf.append('\t').append(LEFT_ANGLE_BRACKET);
		appendTypeName(buf, this.left); 
		buf.append(relationToString(this.relation));
		appendTypeName(buf, this.right);
		buf.append(RIGHT_ANGLE_BRACKET);
		return buf.toString();
	}
}
