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
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.eclipse.jdt.core.compiler.CharOperation;
import org.eclipse.jdt.internal.compiler.ast.Expression;
import org.eclipse.jdt.internal.compiler.ast.Invocation;
import org.eclipse.jdt.internal.compiler.ast.Wildcard;

/**
 * Main class for new type inference as per JLS8 sect 18.
 * Keeps contextual state and drives the algorithm.
 */
public class InferenceContext18 {

	/** to conform with javac regarding https://bugs.openjdk.java.net/browse/JDK-8026527 */
	static final boolean SIMULATE_BUG_JDK_8026527 = true;
	/**
	 * Detail flag to control the extent of {@link #SIMULATE_BUG_JDK_8026527}.
	 * A setting of 'false' implements the advice from http://mail.openjdk.java.net/pipermail/lambda-spec-experts/2013-December/000447.html
	 * i.e., raw types are not considered as compatible in constraints/bounds derived from invocation arguments,
	 * but only for constraints derived from type variable bounds.
	 */
	static final boolean ARGUMENT_CONSTRAINTS_ARE_SOFT = false;

	InferenceVariable[] inferenceVariables;
	BoundSet currentBounds;
	ConstraintFormula[] initialConstraints;
	int variableCount = 0;

	List/*<InvocationSite>*/ innerPolies = new ArrayList();
	public InferenceContext18 outerContext;
	private ArrayList problemMethods;

	Scope scope;
	LookupEnvironment environment;
	ReferenceBinding object;
	
	// interim, processed by createInitialConstraintsForParameters()
	InvocationSite currentInvocation;
	Expression[] invocationArguments;
	
	// Bitmask, lower two bits:
	public static final int CHECK_STRICT = 1;
	public static final int CHECK_LOOSE = 2;
	public static final int CHECK_VARARG = 3;
	public static final int INFERENCE_KIND_MASK = 3;
	// bit 3:
	public static final int CHECK_FINISHED = 4;
	
	static class InvocationRecord {
		InvocationSite site;
		Expression[] invocationArguments;
		InferenceVariable[] inferenceVariables;
		InvocationRecord(InvocationSite site, Expression[] invocationArguments, InferenceVariable[] inferenceVariables) {
			this.site = site;
			this.invocationArguments = invocationArguments;
			this.inferenceVariables = inferenceVariables;
		}
	}
	
	/** Construct an inference context for an invocation (method/constructor). */
	public InferenceContext18(Scope scope, Expression[] arguments, InvocationSite site) {
		this.scope = scope;
		this.environment = scope.environment();
		this.object = scope.getJavaLangObject();
		this.invocationArguments = arguments;
		this.currentInvocation = site;
	}

	public InferenceContext18(Scope scope) {
		this.scope = scope;
		this.environment = scope.environment();
		this.object = scope.getJavaLangObject();
	}

	/**
	 * JLS 18.1.3: Create initial bounds from a given set of type parameters declarations.
	 * @return the set of inference variables created for the given typeParameters
	 */
	public InferenceVariable[] createInitialBoundSet(TypeVariableBinding[] typeParameters) {
		// 
		if (this.currentBounds == null) {
			this.currentBounds = new BoundSet();
		}
		if (typeParameters != null) {
			InferenceVariable[] newInferenceVariables = addInitialTypeVariableSubstitutions(typeParameters);
			this.currentBounds.addBoundsFromTypeParameters(this, typeParameters, newInferenceVariables);
			return newInferenceVariables;
		}
		return Binding.NO_INFERENCE_VARIABLES;
	}

	/**
	 * Substitute any type variables mentioned in 'type' by the corresponding inference variable, if one exists. 
	 */
	public TypeBinding substitute(TypeBinding type) {
		InferenceSubstitution inferenceSubstitution = new InferenceSubstitution(this.environment, this.inferenceVariables);
		return 	inferenceSubstitution.substitute(inferenceSubstitution, type);
	}

	/** JLS 18.5.1: compute bounds from formal and actual parameters. */
	public void createInitialConstraintsForParameters(TypeBinding[] parameters, boolean checkVararg, TypeBinding varArgsType, MethodBinding method) {
		// TODO discriminate strict vs. loose invocations
		if (this.invocationArguments == null)
			return;
		int len = checkVararg ? parameters.length - 1 : Math.min(parameters.length, this.invocationArguments.length);
		int maxConstraints = checkVararg ? this.invocationArguments.length : len;
		int numConstraints = 0;
		if (this.initialConstraints == null) {
			this.initialConstraints = new ConstraintFormula[maxConstraints];
		} else {
			numConstraints = this.initialConstraints.length;
			maxConstraints += numConstraints;
			System.arraycopy(this.initialConstraints, 0,
					this.initialConstraints=new ConstraintFormula[maxConstraints], 0, numConstraints);
		}
		for (int i = 0; i < len; i++) {
			if (this.invocationArguments[i].isPertinentToApplicability(parameters[i], method)) {
				TypeBinding thetaF = substitute(parameters[i]);
				this.initialConstraints[numConstraints++] = new ConstraintExpressionFormula(this.invocationArguments[i], thetaF, ReductionResult.COMPATIBLE, ARGUMENT_CONSTRAINTS_ARE_SOFT);
			}
		}
		if (checkVararg && varArgsType instanceof ArrayBinding) {
			TypeBinding thetaF = substitute(((ArrayBinding) varArgsType).elementsType());
			for (int i = len; i < this.invocationArguments.length; i++) {
				if (this.invocationArguments[i].isPertinentToApplicability(varArgsType, method)) {
					this.initialConstraints[numConstraints++] = new ConstraintExpressionFormula(this.invocationArguments[i], thetaF, ReductionResult.COMPATIBLE, ARGUMENT_CONSTRAINTS_ARE_SOFT);
				}
			}
		}
		if (numConstraints == 0)
			this.initialConstraints = ConstraintFormula.NO_CONSTRAINTS;
		else if (numConstraints < maxConstraints)
			System.arraycopy(this.initialConstraints, 0, this.initialConstraints = new ConstraintFormula[numConstraints], 0, numConstraints);
	}

	public void setInitialConstraint(ConstraintFormula constraintFormula) {
		this.initialConstraints = new ConstraintFormula[] { constraintFormula };
	}

	private InferenceVariable[] addInitialTypeVariableSubstitutions(TypeBinding[] typeVariables) {
		int len = typeVariables.length;
		if (len == 0) {
			if (this.inferenceVariables == null)
				this.inferenceVariables = Binding.NO_INFERENCE_VARIABLES;
			return Binding.NO_INFERENCE_VARIABLES;
		}
		InferenceVariable[] newVariables = new InferenceVariable[len];
		for (int i = 0; i < len; i++)
			newVariables[i] = new InferenceVariable(typeVariables[i], this.variableCount++, this.currentInvocation, this.environment);
		if (this.inferenceVariables == null || this.inferenceVariables.length == 0) {
			this.inferenceVariables = newVariables;
		} else {
			// merge into this.inferenceVariables:
			int prev = this.inferenceVariables.length;
			System.arraycopy(this.inferenceVariables, 0, this.inferenceVariables = new InferenceVariable[len+prev], 0, prev);
			System.arraycopy(newVariables, 0, this.inferenceVariables, prev, len);
		}
		return newVariables;
	}

	/** Add new inference variables for the given type variables. */
	public InferenceVariable[] addTypeVariableSubstitutions(TypeBinding[] typeVariables) {
		int len2 = typeVariables.length;
		InferenceVariable[] newVariables = new InferenceVariable[len2];
		for (int i = 0; i < typeVariables.length; i++)
			newVariables[i] = new InferenceVariable(typeVariables[i], this.variableCount++, this.currentInvocation, this.environment);

		int start = 0;
		if (this.inferenceVariables != null) {
			int len1 = this.inferenceVariables.length;
			System.arraycopy(this.inferenceVariables, 0, this.inferenceVariables = new InferenceVariable[len1+len2], 0, len1);
			start = len1;
		} else {
			this.inferenceVariables = new InferenceVariable[len2];
		}
		System.arraycopy(newVariables, 0, this.inferenceVariables, start, len2);
		return newVariables;
	}

	/** JLS 18.1.3 Bounds: throws α: the inference variable α appears in a throws clause */
	public void addThrowsContraints(TypeBinding[] parameters, InferenceVariable[] variables, ReferenceBinding[] thrownExceptions) {
		for (int i = 0; i < parameters.length; i++) {
			TypeBinding parameter = parameters[i];
			for (int j = 0; j < thrownExceptions.length; j++) {
				if (TypeBinding.equalsEquals(parameter, thrownExceptions[j])) {
					this.currentBounds.inThrows.add(variables[i]);
					break;
				}
			}
		}		
	}

	/** JLS 18.5.1 Invocation Applicability Inference */
	public void inferInvocationApplicability(MethodBinding method, TypeBinding[] arguments, boolean isDiamond, int checkType) {
		ConstraintExpressionFormula.inferInvocationApplicability(this, method, arguments, isDiamond, checkType);
	}

	/** JLS 18.5.2 Invocation Type Inference 
	 * @param b1 "the bound set produced by reduction in order to demonstrate that m is applicable in 18.5.1" 
	 */
	public BoundSet inferInvocationType(BoundSet b1, TypeBinding expectedType, InvocationSite invocationSite, MethodBinding method, int checkKind)
			throws InferenceFailureException 
	{
		BoundSet previous = this.currentBounds;
		this.currentBounds = b1;
		try {
			// bullets 1&2: definitions only.
			if (expectedType != null
					&& expectedType != TypeBinding.VOID
					&& invocationSite instanceof Expression
					&& ((Expression)invocationSite).isPolyExpression(method)) 
			{
				// 3. bullet: special treatment for poly expressions
				if (!ConstraintExpressionFormula.inferPolyInvocationType(this, invocationSite, expectedType, method)) {
					return null;
				}
			}
			// 4. bullet: assemble C:
			TypeBinding[] fs;
			Expression[] arguments = this.invocationArguments;
			Set c = new HashSet();
			if (arguments != null) {
				int k = arguments.length;
				int p = method.parameters.length;
				switch (checkKind) {
					case CHECK_STRICT:
					case CHECK_LOOSE:
						fs = method.parameters;
						break;
					case CHECK_VARARG:
						fs = varArgTypes(method.parameters, k);
						break;
					default:
						throw new IllegalStateException("Unexpected checkKind "+checkKind); //$NON-NLS-1$
				}
				for (int i = 0; i < k; i++) {
					TypeBinding fsi = fs[Math.min(i, p-1)];
					TypeBinding substF = substitute(fsi);
					// For all i (1 ≤ i ≤ k), if ei is not pertinent to applicability, the set contains ⟨ei → θ Fi⟩.
					if (!arguments[i].isPertinentToApplicability(fsi, method)) {
						c.add(new ConstraintExpressionFormula(arguments[i], substF, ReductionResult.COMPATIBLE, ARGUMENT_CONSTRAINTS_ARE_SOFT));
					}
					c.add(new ConstraintExceptionFormula(arguments[i], substF));
				}
			}
			// 5. bullet: determine B3 from C
			while (!c.isEmpty()) {
				// *
				Set bottomSet = findBottomSet(c, allOutputVariables(c));
				if (bottomSet.isEmpty()) {
					bottomSet.add(pickFromCycle(c)); 
				}
				// *
				c.removeAll(bottomSet);
				// * The union of the input variables of all the selected constraints, α1, ..., αm, ...
				Set allInputs = new HashSet();
				Iterator bottomIt = bottomSet.iterator();
				while (bottomIt.hasNext()) {
					allInputs.addAll(((ConstraintFormula)bottomIt.next()).inputVariables(this));
				}
				InferenceVariable[] variablesArray = (InferenceVariable[]) allInputs.toArray(new InferenceVariable[allInputs.size()]);
				//   ... is resolved
				BoundSet solution = resolve();
				// * ~ apply substitutions to all constraints: 
				bottomIt = bottomSet.iterator();
				while (bottomIt.hasNext()) {
					ConstraintFormula constraint = ((ConstraintFormula)bottomIt.next());
					if (solution != null)
						constraint.applySubstitution(solution, variablesArray);
				// * reduce and incorporate
					if (!this.currentBounds.reduceOneConstraint(this, constraint))
						return null;
				}
			}
			// 6. bullet: solve
			BoundSet solution = solve();
			if (solution == null || !isResolved(solution))
				return null;
			return solution;
		} finally {
			this.currentBounds = previous;
		}
	}

	/**
	 * Simplified API to perform Invocation Type Inference (JLS 18.5.2)
	 * and (if successful) return the solution.
	 * @param invocation invocation being inferred
	 * @param targetType target type for this invocation
	 * @return a method binding with updated type parameters, or null if no solution was found
	 */
	public MethodBinding getInvocationTypeInferenceSolution(Invocation invocation, TypeBinding targetType) {
		// start over from a previous candidate but discard its type variable instantiations
		// TODO: should we retain any instantiations of type variables not owned by the method? 
		MethodBinding method = invocation.binding().original();
		BoundSet result = null;
		try {
			result = inferInvocationType(this.currentBounds, targetType, invocation, method, invocation.inferenceKind());
		} catch (InferenceFailureException e) {
			return null;
		}
		if (result != null) {
			TypeBinding[] solutions = getSolutions(method.typeVariables(), invocation, result);
			if (solutions != null)
				return this.environment.createParameterizedGenericMethod(method, solutions);
		}
		return null;
	}

	private Object pickFromCycle(Set c) {
		missingImplementation("Breaking a dependency cycle NYI"); //$NON-NLS-1$
		return null; // never
	}

	private Set findBottomSet(Set constraints, Set allOutputVariables) {
		// 18.5.2 bullet 5.1
		//  A subset of constraints is selected, satisfying the property
		// that, for each constraint, no input variable depends on an
		// output variable of another constraint in C ...
		Set result = new HashSet();
		Iterator it = constraints.iterator();
		constraintLoop: while (it.hasNext()) {
			ConstraintFormula constraint = (ConstraintFormula)it.next();
			Iterator inputIt = constraint.inputVariables(this).iterator();
			Iterator outputIt = allOutputVariables.iterator();
			while (inputIt.hasNext()) {
				InferenceVariable in = (InferenceVariable) inputIt.next();
				while (outputIt.hasNext()) {
					if (this.currentBounds.dependsOnResolutionOf(in, (InferenceVariable) outputIt.next()))
						continue constraintLoop;
				}
			}
			result.add(constraint);
		}		
		return result;
	}

	Set allOutputVariables(Set constraints) {
		Set result = new HashSet();
		Iterator it = constraints.iterator();
		while (it.hasNext()) {
			result.addAll(((ConstraintFormula)it.next()).outputVariables(this));
		}
		return result;
	}

	// ========== Below this point: implementation of the generic algorithm: ==========

	/**
	 * Try to solve the inference problem defined by constraints and bounds previously registered.
	 * @return a bound set representing the solution, or null if inference failed
	 * @throws InferenceFailureException a compile error has been detected during inference
	 */
	public /*@Nullable*/ BoundSet solve() throws InferenceFailureException {
		if (!reduce())
			return null;
		if (!this.currentBounds.incorporate(this))
			return null;

		return resolve();
	}

	/**
	 * JLS 18.2. reduce all initial constraints 
	 * @throws InferenceFailureException 
	 */
	private boolean reduce() throws InferenceFailureException {
		if (this.initialConstraints != null) {
			for (int i = 0; i < this.initialConstraints.length; i++) {
				if (!this.currentBounds.reduceOneConstraint(this, this.initialConstraints[i]))
					return false;
			}
		}
		this.initialConstraints = null;
		return true;
	}

	/**
	 * Have all inference variables been instantiated successfully?
	 */
	public boolean isResolved(BoundSet boundSet) {
		if (this.inferenceVariables != null) {
			for (int i = 0; i < this.inferenceVariables.length; i++) {
				if (!boundSet.isInstantiated(this.inferenceVariables[i]))
					return false;
			}
		}
		return true;
	}

	/**
	 * Retrieve the resolved solutions for all given type variables.
	 * @param typeParameters
	 * @param boundSet where instantiations are to be found
	 * @return array containing the substituted types or <code>null</code> elements for any type variable that could not be substituted.
	 */
	public TypeBinding /*@Nullable*/[] getSolutions(TypeVariableBinding[] typeParameters, InvocationSite site, BoundSet boundSet) {
		int len = typeParameters.length;
		TypeBinding[] substitutions = new TypeBinding[len];
		for (int i = 0; i < typeParameters.length; i++) {
			for (int j = 0; j < this.inferenceVariables.length; j++) {
				InferenceVariable variable = this.inferenceVariables[j];
				if (variable.site == site && TypeBinding.equalsEquals(variable.typeParameter, typeParameters[i])) {
					substitutions[i] = boundSet.getInstantiation(variable);
					break;
				}
			}
			if (substitutions[i] == null)
				return null;
		}
		return substitutions;
	}

	/** When inference produces a new constraint, reduce it to a suitable type bound and add the latter to the bound set. */
	public boolean reduceAndIncorporate(ConstraintFormula constraint) throws InferenceFailureException {
		return this.currentBounds.reduceOneConstraint(this, constraint); // TODO(SH): should we immediately call a diat incorporate, or can we simply wait for the next round?
	}

	/**
	 * <b>JLS 18.4</b> Resolution
	 * @return answer null if some constraint resolved to FALSE, otherwise the boundset representing the solution
	 * @throws InferenceFailureException 
	 */
	private /*@Nullable*/ BoundSet resolve() throws InferenceFailureException {
		// NOTE: 18.5.2 ... 
		// "(While it was necessary to demonstrate that the inference variables in B1 could be resolved
		//   in order to establish applicability, the resulting instantiations are not considered part of B1.)
		// For this reason, resolve works on a temporary bound set, copied before any modification.
		BoundSet tmpBoundSet = this.currentBounds;
		if (this.inferenceVariables != null) {
			for (int i = 0; i < this.inferenceVariables.length; i++) {
				InferenceVariable currentVariable = this.inferenceVariables[i];
				if (this.currentBounds.isInstantiated(currentVariable)) continue;
				// find a minimal set of dependent variables:
				Set variableSet = new HashSet();
				int numUninstantiated = addDependencies(tmpBoundSet, variableSet, i);
				final int numVars = variableSet.size();
				
				if (numUninstantiated > 0 && numVars > 0) {
					final InferenceVariable[] variables = (InferenceVariable[]) variableSet.toArray(new InferenceVariable[numVars]);
					if (!tmpBoundSet.hasCaptureBound(variableSet)) {
						// try to instantiate this set of variables in a fresh copy of the bound set:
						BoundSet prevBoundSet = tmpBoundSet;
						tmpBoundSet = tmpBoundSet.copy();
						for (int j = 0; j < variables.length; j++) {
							InferenceVariable variable = variables[j];
							// try lower bounds:
							TypeBinding[] lowerBounds = tmpBoundSet.lowerBounds(variable, true/*onlyProper*/);
							if (lowerBounds != Binding.NO_TYPES) {
								TypeBinding lub = this.scope.lowerUpperBound(lowerBounds);
								if (lub == TypeBinding.VOID || lub == null)
									return null;
								tmpBoundSet.addBound(new TypeBound(variable, lub, ReductionResult.SAME));
							} else {
								TypeBinding[] upperBounds = tmpBoundSet.upperBounds(variable, true/*onlyProper*/);
								// check exception bounds:
								if (tmpBoundSet.inThrows.contains(variable) && tmpBoundSet.hasOnlyTrivialExceptionBounds(variable, upperBounds)) {
									TypeBinding runtimeException = this.scope.getType(TypeConstants.JAVA_LANG_RUNTIMEEXCEPTION, 3);
									tmpBoundSet.addBound(new TypeBound(variable, runtimeException, ReductionResult.SAME));
								} else {
									// try upper bounds:
									if (upperBounds != Binding.NO_TYPES) {
										TypeBinding glb;
										if (upperBounds.length == 1) {
											glb = upperBounds[0];
										} else {
											ReferenceBinding[] glbs = Scope.greaterLowerBound((ReferenceBinding[])upperBounds);
											if (glbs == null)
												throw new UnsupportedOperationException("no glb for "+Arrays.asList(upperBounds)); //$NON-NLS-1$
											else if (glbs.length == 1)
												glb = glbs[0];
											else
												glb = new IntersectionCastTypeBinding(glbs, this.environment);
										}
										tmpBoundSet.addBound(new TypeBound(variable, glb, ReductionResult.SAME));
									}
								}
							}
						}
						if (tmpBoundSet.incorporate(this))
							continue;
						tmpBoundSet = prevBoundSet;// clean-up for second attempt
					}
					// Otherwise, a second attempt is made...
					final CaptureBinding18[] zs = new CaptureBinding18[numVars];
					for (int j = 0; j < numVars; j++)
						zs[j] = freshCapture(variables[j]);
					Substitution theta = new Substitution() {
						public LookupEnvironment environment() { 
							return InferenceContext18.this.environment;
						}
						public boolean isRawSubstitution() {
							return false;
						}
						public TypeBinding substitute(TypeVariableBinding typeVariable) {
							for (int j = 0; j < numVars; j++)
								if (variables[j] == typeVariable) //$IDENTITY-COMPARISON$ InferenceVariable does not participate in type annotation encoding
									return zs[j];
							return typeVariable;
						}
					};
					for (int j = 0; j < numVars; j++) {
						InferenceVariable variable = variables[j];
						CaptureBinding18 zsj = zs[j];
						// add lower bounds:
						TypeBinding[] lowerBounds = tmpBoundSet.lowerBounds(variable, false/*onlyProper*/);
						if (lowerBounds != Binding.NO_TYPES) {
							lowerBounds = Scope.substitute(theta, lowerBounds);
							TypeBinding lub = this.scope.lowerUpperBound(lowerBounds);
							if (lub != TypeBinding.VOID && lub != null)
								zsj.lowerBound = lub;
						}
						// add upper bounds:
						TypeBinding[] upperBounds = tmpBoundSet.upperBounds(variable, false/*onlyProper*/);
						if (upperBounds != Binding.NO_TYPES) {
							for (int k = 0; k < upperBounds.length; k++)
								upperBounds[k] = Scope.substitute(theta, upperBounds[k]);
							if (!setUpperBounds(zsj, upperBounds))
								continue; // at violation of well-formedness skip this candidate and proceed
						}
						if (tmpBoundSet == this.currentBounds)
							tmpBoundSet = tmpBoundSet.copy();
						// FIXME: remove capture bounds
						tmpBoundSet.addBound(new TypeBound(variable, zsj, ReductionResult.SAME));
					}
					if (tmpBoundSet.incorporate(this))
						continue;
					return null;
				}
			}
		}
		return tmpBoundSet;
	}
	
	// === FIXME(stephan): this capture business is a bit drafty: ===
	int captureId = 0;
	
	/** For 18.4: "Let Z1, ..., Zn be fresh type variables" use capture bindings. */
	private CaptureBinding18 freshCapture(InferenceVariable variable) {
		char[] sourceName = CharOperation.concat("Z-".toCharArray(), variable.sourceName); //$NON-NLS-1$
		return new CaptureBinding18(this.scope.enclosingSourceType(), sourceName, variable.typeParameter.shortReadableName(), this.captureId++, this.environment);
	}
	// === ===
	
	private boolean setUpperBounds(CaptureBinding18 typeVariable, TypeBinding[] substitutedUpperBounds) {
		// 18.4: ... define the upper bound of Zi as glb(L1θ, ..., Lkθ)
		if (substitutedUpperBounds.length == 1) {
			typeVariable.setUpperBounds(substitutedUpperBounds, this.object); // shortcut
		} else {
			TypeBinding[] glbs = Scope.greaterLowerBound(substitutedUpperBounds, this.scope, this.environment);
			if (glbs == null)
				return false;
			// for deterministic results sort this array by id:
			sortTypes(glbs);
			if (!typeVariable.setUpperBounds(glbs, this.object))
				return false;
		}
		return true;
	}

	static void sortTypes(TypeBinding[] types) {
		Arrays.sort(types, new Comparator() {
			public int compare(Object o1, Object o2) {
				int i1 = ((TypeBinding)o1).id, i2 = ((TypeBinding)o2).id; 
				return (i1<i2 ? -1 : (i1==i2 ? 0 : 1));
			}
		});
	}

	/** 
	 * starting with our i'th inference variable collect all variables
	 * reachable via dependencies (regardless of relation kind).
	 * @param variableSet collect all variables found into this set
	 * @param i seed index into {@link #inferenceVariables}.
	 * @return count of uninstantiated variables added to the set.
	 */
	private int addDependencies(BoundSet boundSet, Set variableSet, int i) {
		InferenceVariable currentVariable = this.inferenceVariables[i];
		if (boundSet.isInstantiated(currentVariable)) return 0;
		if (!variableSet.add(currentVariable)) return 1;
		int numUninstantiated = 1;
		for (int j = 0; j < this.inferenceVariables.length; j++) {
			if (i == j) continue;
			if (boundSet.dependsOnResolutionOf(currentVariable, this.inferenceVariables[j]))
				numUninstantiated += addDependencies(boundSet, variableSet, j);
		}
		return numUninstantiated;
	}

	private TypeBinding[] varArgTypes(TypeBinding[] parameters, int k) {
		TypeBinding[] types = new TypeBinding[k];
		int declaredLength = parameters.length;
		System.arraycopy(parameters, 0, types, 0, declaredLength);
		TypeBinding last = parameters[declaredLength-1];
		for (int i = declaredLength; i < k; i++)
			types[i] = last;
		return types;
	}
	
	public InvocationRecord enterPolyInvocation(InvocationSite invocation, Expression[] innerArguments) {
		InvocationRecord record = new InvocationRecord(this.currentInvocation, this.invocationArguments, this.inferenceVariables);
		this.inferenceVariables = null;
		this.invocationArguments = innerArguments;
		this.currentInvocation = invocation;
		
		// schedule for re-binding the inner after inference success:
		this.innerPolies.add(invocation);
		if (invocation instanceof Invocation) {
			InferenceContext18 innerContext = ((Invocation) invocation).inferenceContext();
			if (innerContext != null)
				innerContext.outerContext = this;
		}
		return record;
	}

	public void leavePolyInvocation(InvocationRecord record) {
		// merge inference variables:
		int l1 = this.inferenceVariables.length;
		int l2 = record.inferenceVariables.length;
		// move to back, add previous to front:
		System.arraycopy(this.inferenceVariables, 0, this.inferenceVariables=new InferenceVariable[l1+l2], l2, l1);
		System.arraycopy(record.inferenceVariables, 0, this.inferenceVariables, 0, l2);

		// replace invocation site & arguments:
		this.currentInvocation = record.site;
		this.invocationArguments = record.invocationArguments;
	}
	
	/**
	 * After inference has finished, iterate all inner poly expressions, that have been
	 * included in the inference. For each of these update some type information
	 * from the inference result and perhaps trigger follow-up resolving as needed.
	 */
	public void rebindInnerPolies(BoundSet bounds, TypeBinding[] argumentTypes) {
		int len = this.innerPolies.size();
		for (int i = 0; i < len; i++) {
			Expression inner = (Expression) this.innerPolies.get(i);
			if (inner instanceof Invocation) {
				Invocation innerMessage = (Invocation) inner;
				MethodBinding original = innerMessage.binding().original();

				// apply inference results onto the allocation type of inner diamonds:
				if (original.isConstructor() && inner.isPolyExpression()) {
					ReferenceBinding declaringClass = original.declaringClass;
					TypeBinding[] arguments = getSolutions(declaringClass.typeVariables(), innerMessage, bounds);
					declaringClass = this.environment.createParameterizedType(declaringClass, arguments, declaringClass.enclosingType());
					original = ((ParameterizedTypeBinding)declaringClass).createParameterizedMethod(original);
				}
				
				// apply inference results onto the binding of the inner invocation:
				TypeBinding[] solutions = getSolutions(original.typeVariables(), innerMessage, bounds);
				if (solutions == null) 
					continue; // play safe, but shouldn't happen in a resolved context
				ParameterizedGenericMethodBinding innerBinding = this.environment.createParameterizedGenericMethod(original, solutions);
				innerMessage.updateBindings(innerBinding);
				innerMessage.markInferenceFinished(); // invocation type inference has already happened on the inner, too.
				
				// finalize resolving of arguments of the inner invocation:
				TypeBinding[] innerParameters = innerBinding.parameters;
				int inferenceKind = innerMessage.inferenceKind();
				boolean isVarargs = (inferenceKind == CHECK_VARARG) && innerBinding.isVarargs();
				TypeBinding varArgsType = isVarargs ? ((ArrayBinding)innerParameters[innerParameters.length-1]).elementsType() : null; 
				Expression[] arguments = innerMessage.arguments();
				if (arguments != null) {
					for (int j = 0; j < arguments.length; j++) {
						TypeBinding param = (varArgsType == null || (j < innerParameters.length-1))
												? innerParameters[j]
												: varArgsType;
						arguments[j].checkAgainstFinalTargetType(param);
					}
				}
			}
			// inner FunctionalExpression don't seem to be included in inference.
			// TODO recheck any inquires on those actually involve inference of which the results are included here. 
		}
	}

	// debugging:
	public String toString() {
		StringBuffer buf = new StringBuffer("Inference Context"); //$NON-NLS-1$
		if (isResolved(this.currentBounds))
			buf.append(" (resolved)"); //$NON-NLS-1$
		buf.append('\n');
		if (this.inferenceVariables != null) {
			buf.append("Inference Variables:\n"); //$NON-NLS-1$
			for (int i = 0; i < this.inferenceVariables.length; i++) {
				buf.append('\t').append(this.inferenceVariables[i].sourceName).append("\t:\t"); //$NON-NLS-1$
				if (this.currentBounds.isInstantiated(this.inferenceVariables[i]))
					buf.append(this.currentBounds.getInstantiation(this.inferenceVariables[i]).readableName());
				else
					buf.append("NOT INSTANTIATED"); //$NON-NLS-1$
				buf.append('\n');
			}
		}
		if (this.initialConstraints != null) {
			buf.append("Initial Constraints:\n"); //$NON-NLS-1$
			for (int i = 0; i < this.initialConstraints.length; i++)
				if (this.initialConstraints[i] != null)
					buf.append('\t').append(this.initialConstraints[i].toString()).append('\n');
		}
		if (this.currentBounds != null)
			buf.append(this.currentBounds.toString());
		return buf.toString();
	}

	public void addProblemMethod(ProblemMethodBinding problemMethod) {
		if (this.problemMethods == null)
			this.problemMethods = new ArrayList();
		this.problemMethods.add(problemMethod);
	}

	public static ParameterizedTypeBinding parameterizedWithWildcard(TypeBinding returnType) {
		if (returnType == null || returnType.kind() != Binding.PARAMETERIZED_TYPE)
			return null;
		ParameterizedTypeBinding parameterizedType = (ParameterizedTypeBinding) returnType;
		TypeBinding[] arguments = parameterizedType.arguments;
		for (int i = 0; i < arguments.length; i++) {
			if (arguments[i].isWildcard())
				return parameterizedType;
		}
		return null;
	}

	/**
	 * Create initial bound set for 18.5.3 Functional Interface Parameterization Inference
	 * @param functionalInterface the functional interface F<A1,..Am>
	 * @return the parameter types Q1..Qk of the function type of the type F<α1, ..., αm> 
	 */
	public TypeBinding[] createBoundsForFunctionalInterfaceParameterizationInference(ParameterizedTypeBinding functionalInterface) {
		this.currentBounds = new BoundSet();
		TypeBinding[] a = functionalInterface.arguments;
		InferenceVariable[] alpha = addInitialTypeVariableSubstitutions(a);

		for (int i = 0; i < a.length; i++) {
			TypeBound bound;
			if (a[i].kind() == Binding.WILDCARD_TYPE) {
				WildcardBinding wildcard = (WildcardBinding) a[i];
				switch(wildcard.boundKind) {
    				case Wildcard.EXTENDS :
    					bound = new TypeBound(alpha[i], wildcard.allBounds(), ReductionResult.SUBTYPE);
    					break;
    				case Wildcard.SUPER :
    					bound = new TypeBound(alpha[i], wildcard.bound, ReductionResult.SUPERTYPE);
    					break;
    				case Wildcard.UNBOUND :
    					bound = new TypeBound(alpha[i], this.object, ReductionResult.SUBTYPE);
    					break;
    				default:
    					continue; // cannot
				}
			} else {
				bound = new TypeBound(alpha[i], a[i], ReductionResult.SAME);
			}
			this.currentBounds.addBound(bound);
		}
		TypeBinding falpha = substitute(functionalInterface);
		return falpha.getSingleAbstractMethod(this.scope, true).parameters;
	}

	public boolean reduceWithEqualityConstraints(TypeBinding[] p, TypeBinding[] q) {
		for (int i = 0; i < p.length; i++) {
			try {
				if (!this.reduceAndIncorporate(new ConstraintTypeFormula(p[i], q[i], ReductionResult.SAME)))
					return false;
			} catch (InferenceFailureException e) {
				return false;
			}
		}
		return true;
	}

	public TypeBinding[] getFunctionInterfaceArgumentSolutions(TypeBinding[] a) {
		int m = a.length;
		TypeBinding[] aprime = new TypeBinding[m];
		for (int i = 0; i < this.inferenceVariables.length; i++) {
			InferenceVariable alphai = this.inferenceVariables[i];
			TypeBinding t = this.currentBounds.getInstantiation(alphai);
			if (t != null)
				aprime[i] = t;
			else
				aprime[i] = a[i];
		}
		return aprime;
	}

	// INTERIM: infrastructure for detecting failures caused by specific known incompleteness:
	public static void missingImplementation(String msg) {
		throw new UnsupportedOperationException(msg);
	}
}
