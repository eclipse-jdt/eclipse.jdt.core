/*******************************************************************************
 * Copyright (c) 2023, 2024 Red Hat, Inc. and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *******************************************************************************/
package org.eclipse.jdt.internal.core.search.matching;

import static org.eclipse.jdt.internal.core.JavaModelManager.trace;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.compiler.CharOperation;
import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.Annotation;
import org.eclipse.jdt.core.dom.AnnotationTypeMemberDeclaration;
import org.eclipse.jdt.core.dom.ExpressionMethodReference;
import org.eclipse.jdt.core.dom.IBinding;
import org.eclipse.jdt.core.dom.IMethodBinding;
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.core.dom.IVariableBinding;
import org.eclipse.jdt.core.dom.JdtCoreDomPackagePrivateUtility;
import org.eclipse.jdt.core.dom.MemberValuePair;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.MethodInvocation;
import org.eclipse.jdt.core.dom.MethodRef;
import org.eclipse.jdt.core.dom.MethodReference;
import org.eclipse.jdt.core.dom.Modifier;
import org.eclipse.jdt.core.dom.Name;
import org.eclipse.jdt.core.dom.SimpleName;
import org.eclipse.jdt.core.dom.SingleMemberAnnotation;
import org.eclipse.jdt.core.dom.SingleVariableDeclaration;
import org.eclipse.jdt.core.dom.SuperMethodInvocation;
import org.eclipse.jdt.core.dom.SuperMethodReference;
import org.eclipse.jdt.core.dom.TypeMethodReference;
import org.eclipse.jdt.core.search.SearchPattern;
import org.eclipse.jdt.internal.core.BinaryMethod;
import org.eclipse.jdt.internal.core.search.BasicSearchEngine;
import org.eclipse.jdt.internal.core.search.DOMASTNodeUtils;
import org.eclipse.jdt.internal.core.search.LocatorResponse;

public class DOMMethodLocator extends DOMPatternLocator {

	private MethodLocator locator;
	private MethodPattern pattern;
	private char[][][] allSuperDeclaringTypeNames;
	private char[][][] samePkgSuperDeclaringTypeNames;
	private Map<ASTNode, Boolean> methodDeclarationsWithInvalidParam = new HashMap<>();
	public DOMMethodLocator(MethodLocator locator) {
		super(locator.pattern);
		this.locator = locator;
		this.pattern = locator.pattern;
	}

	private IMethodBinding getDOMASTMethodBinding(ITypeBinding type, String methodName, ITypeBinding[] argumentTypes) {
		return Stream.of(type.getDeclaredMethods())
			.filter(method -> Objects.equals(method.getName(), methodName))
			.filter(method -> compatibleByErasure(method.getParameterTypes(), argumentTypes))
			.findAny()
			.orElse(null);
	}
	// can be replaced with `Arrays.equals(method.getParameterTypes(), argumentTypes, Comparator.comparing(ITypeBinding::getErasure))`
	// but JDT bugs
	private static boolean compatibleByErasure(ITypeBinding[] one, ITypeBinding[] other) {
		if (Objects.equals(one, other)) {
			return true;
		}
		if (one == null || other == null || one.length != other.length) {
			return false;
		}
		for (int i = 0; i < one.length; i++) {
			if (!Objects.equals(one[i].getErasure(), other[i].getErasure())) {
				return false;
			}
		}
		return true;
	}

	@Override
	public LocatorResponse match(org.eclipse.jdt.core.dom.MethodDeclaration node, NodeSetWrapper nodeSet, MatchLocator locator) {
		if (!this.locator.pattern.findDeclarations)
			return toResponse(IMPOSSIBLE_MATCH);

		// Verify method name
		if (!this.locator.matchesName(this.locator.pattern.selector, node.getName().getIdentifier().toCharArray()))
			return toResponse(IMPOSSIBLE_MATCH);

		// Verify parameters types
		boolean resolve = this.locator.pattern.mustResolve;
		if (this.locator.pattern.parameterSimpleNames != null) {
			int length = this.locator.pattern.parameterSimpleNames.length;
			List<SingleVariableDeclaration> args = node.parameters();
			int argsLength = args == null ? 0 : args.size();
			if (length != argsLength)
				return toResponse(IMPOSSIBLE_MATCH);
			for (int i = 0; i < argsLength; i++) {
				var arg = args.get(i);
				if (!this.matchesTypeReference(this.locator.pattern.parameterSimpleNames[i], arg.getType(), arg.isVarargs())) {
					if (!this.locator.pattern.mustResolve) {
						// Set resolution flag on node set in case of types was inferred in parameterized types from generic ones...
					 	// (see  bugs https://bugs.eclipse.org/bugs/show_bug.cgi?id=79990, 96761, 96763)
						nodeSet.setMustResolve(true);
						resolve = true;
					}
					this.methodDeclarationsWithInvalidParam.put(node, null);
				}
			}
		}

		// Verify type arguments (do not reject if pattern has no argument as it can be an erasure match)
		if (this.locator.pattern.hasMethodArguments()) {
			if (node.typeParameters() == null || node.typeParameters().size() != this.locator.pattern.methodArguments.length)
				return toResponse(IMPOSSIBLE_MATCH);
		}

		// Method declaration may match pattern
		int level = nodeSet.addMatch(node, resolve ? POSSIBLE_MATCH : ACCURATE_MATCH);
		return toResponse(level, true);
	}

	@Override
	public LocatorResponse match(AnnotationTypeMemberDeclaration node, NodeSetWrapper nodeSet, MatchLocator locator) {
		return toResponse(this.locator.pattern.findDeclarations && this.locator.matchesName(this.locator.pattern.selector, node.getName().getIdentifier().toCharArray()) ?
			POSSIBLE_MATCH : IMPOSSIBLE_MATCH);
	}

	@Override
	public LocatorResponse match(Annotation node, NodeSetWrapper nodeSet, MatchLocator locator) {
		return toResponse(this.locator.pattern.findReferences && node instanceof SingleMemberAnnotation singleMemberAnnot ?
			POSSIBLE_MATCH : IMPOSSIBLE_MATCH);
	}

	private int matchReference(SimpleName name, List<?> args) {
		if (!this.locator.pattern.findReferences) return IMPOSSIBLE_MATCH;

		if (!this.locator.matchesName(this.locator.pattern.selector, name.getIdentifier().toCharArray())) return IMPOSSIBLE_MATCH;
		if (this.locator.pattern.parameterSimpleNames != null && (!this.locator.pattern.varargs || DOMASTNodeUtils.insideDocComment(name))) {
			int length = this.locator.pattern.parameterSimpleNames.length;
			int argsLength = args == null ? 0 : args.size();
			if (length != argsLength) return IMPOSSIBLE_MATCH;
		}
		return this.locator.pattern.mustResolve ? POSSIBLE_MATCH : ACCURATE_MATCH;
	}
	@Override
	public LocatorResponse match(MethodInvocation node, NodeSetWrapper nodeSet, MatchLocator locator) {
		int level = this.matchReference(node.getName(), node.arguments());
		if( level == IMPOSSIBLE_MATCH )
			return toResponse(IMPOSSIBLE_MATCH);
		return toResponse(nodeSet.addMatch(node, level), true);
	}
	@Override
	public LocatorResponse match(MethodRef node, NodeSetWrapper nodeSet, MatchLocator locator) {
		int level = this.matchReference(node.getName(), node.parameters());
		if( level == IMPOSSIBLE_MATCH )
			return toResponse(IMPOSSIBLE_MATCH);
		return toResponse(nodeSet.addMatch(node, level), true);
	}
	@Override
	public LocatorResponse match(MethodReference node, NodeSetWrapper nodeSet, MatchLocator locator) {
		SimpleName name = node instanceof TypeMethodReference typeMethodRef ? typeMethodRef.getName() :
			node instanceof SuperMethodReference superMethodRef ? superMethodRef.getName() :
			node instanceof ExpressionMethodReference exprMethodRef ? exprMethodRef.getName() :
			null;
		if (name == null) {
			return toResponse(IMPOSSIBLE_MATCH);
		}
		if (this.locator.matchesName(this.locator.pattern.selector, name.getIdentifier().toCharArray())) {
			nodeSet.setMustResolve(true);
			return toResponse(nodeSet.addMatch(node, POSSIBLE_MATCH), true);
		} else {
			return toResponse(IMPOSSIBLE_MATCH);
		}

	}
	@Override
	public LocatorResponse match(org.eclipse.jdt.core.dom.Expression expression, NodeSetWrapper nodeSet, MatchLocator locator) {
		int level = IMPOSSIBLE_MATCH;
		if (expression instanceof SuperMethodInvocation node) {
			level = this.matchReference(node.getName(), node.arguments());
		}
		if (expression.getLocationInParent() == SingleMemberAnnotation.VALUE_PROPERTY
			&& this.locator.pattern.matchesName(this.locator.pattern.selector, "value".toCharArray())
			&& this.locator.pattern.parameterCount == 0) {
			// TODO: also check annotation name matches pattern (if available)
			level = POSSIBLE_MATCH;
		}
		if( level == IMPOSSIBLE_MATCH)
			return toResponse(IMPOSSIBLE_MATCH);
		return toResponse(nodeSet.addMatch(expression, level), true);
	}

	@Override
	public LocatorResponse match(Name node, NodeSetWrapper nodeSet, MatchLocator locator) {
		if( node.getParent() instanceof MethodInvocation mi && mi.getName() == node) {
//			if( nodeSet.getTrustedMatch(mi) > IMPOSSIBLE_MATCH ) {
				return toResponse(IMPOSSIBLE_MATCH);
//			}
		}

		if (node.getLocationInParent() == MemberValuePair.NAME_PROPERTY
			&& this.locator.pattern.parameterCount == 0
			&& node instanceof SimpleName simpleName) {
			return toResponse(matchReference(simpleName, null));
		}

		String name = node.toString();
		String[] segments = name.split("\\."); //$NON-NLS-1$
		String lastSegment = segments == null || segments.length == 0 ? null : segments[segments.length-1];
		boolean matchesLastSegment = this.locator.pattern.selector == null ? true :
			this.locator.matchesName(this.locator.pattern.selector, (lastSegment == null ? "" : lastSegment).toCharArray()); //$NON-NLS-1$
		boolean matchesPrefix = this.locator.pattern.declaringPackageName == null ? true :
			name.startsWith(new String(this.locator.pattern.declaringPackageName));
		int level = matchesLastSegment && matchesPrefix ? POSSIBLE_MATCH : IMPOSSIBLE_MATCH;
		return toResponse(level);
	}

	protected int matchMethod(ASTNode node, IMethodBinding method, boolean skipImpossibleArg, boolean bindingIsDeclaration) {
		if (!this.locator.matchesName(this.locator.pattern.selector, method.getName().toCharArray())) return IMPOSSIBLE_MATCH;

		int level = matchMethodBindingReturn(method);
		if (level == IMPOSSIBLE_MATCH)
			return level;

		if (this.pattern.declaringSimpleName == null && this.locator.pattern.returnSimpleName != null) {
			// look at return type only if declaring type is not specified
			level = resolveLevelForType(this.locator.pattern.returnSimpleName, this.locator.pattern.returnQualification, method.getReturnType());
			if (level == IMPOSSIBLE_MATCH)
				return level;
		}


		level = matchMethodParametersTypes(node, method, skipImpossibleArg, level);
		if (level == IMPOSSIBLE_MATCH)
			return level;

		level = matchMethodTypeArguments(node, method, skipImpossibleArg, level, bindingIsDeclaration);
		if (level == IMPOSSIBLE_MATCH)
			return level;

		int typeParamMatches = validateReceiverTypeArguments(node, method, level, bindingIsDeclaration);
		if( typeParamMatches == DOMTypeReferenceLocator.TYPE_PARAMS_NO_MATCH) level = IMPOSSIBLE_MATCH;
		if( typeParamMatches == DOMTypeReferenceLocator.TYPE_PARAMS_COUNT_MATCH) level = ERASURE_MATCH;
		if( isPatternExactMatch()) {
			if( typeParamMatches == DOMTypeReferenceLocator.TYPE_PARAMS_NO_MATCH) {
				return IMPOSSIBLE_MATCH;
			}
		}
		boolean isErasurePattern = isPatternErasureMatch();
		boolean isEquivPattern = isPatternEquivalentMatch();
		if( level == ERASURE_MATCH && !isErasurePattern && !isEquivPattern)
			level = IMPOSSIBLE_MATCH;

		return level;
	}

	private int validateReceiverTypeArguments(ASTNode node, IMethodBinding method, int level,
			boolean bindingIsDeclaration) {
		// This method is substantially copied from DOMTypeReferenceLocator
		boolean erasureMatch = isPatternErasureMatch();
		boolean equivMatch = isPatternEquivalentMatch();
		boolean exactMatch = isPatternExactMatch();
		boolean patternHasTypeArgs = this.locator.pattern.hasTypeArguments();

		if( patternHasTypeArgs && !(erasureMatch || equivMatch || exactMatch )) {
			return DOMTypeReferenceLocator.TYPE_PARAMS_NO_MATCH;
		}
		if( patternHasTypeArgs && !(erasureMatch || equivMatch || exactMatch )) {
			return DOMTypeReferenceLocator.TYPE_PARAMS_NO_MATCH;
		}

		char[][][] fromPattern = this.locator.pattern.getTypeArguments();
		if( fromPattern == null ) {
			return DOMTypeReferenceLocator.TYPE_PARAMS_MATCH;
		}
		if( node instanceof MethodInvocation mi && mi.getExpression() != null ) {
			ASTNode expr = mi.getExpression();
			IBinding b = DOMASTNodeUtils.getBinding(expr);
			if( b instanceof IVariableBinding vb) {
				b = vb.getType();
			}
			if( b instanceof ITypeBinding tb ) {
				boolean bindingIsRaw = tb.isRawType();
				ITypeBinding[] typeArgs = tb.getTypeArguments();
				if( fromPattern.length == 0 ) {
					if( typeArgs == null || typeArgs.length == 0 ) {
						return DOMTypeReferenceLocator.TYPE_PARAMS_MATCH;
					}
					return DOMTypeReferenceLocator.TYPE_PARAMS_NO_MATCH;
				}
				char[][] thisLevelTypeParams = fromPattern[0];
				boolean emptyPatternParams = thisLevelTypeParams == null || thisLevelTypeParams.length == 0;
				if( emptyPatternParams) {
					if( exactMatch && emptyPatternParams && (typeArgs != null && typeArgs.length > 0) ) {
						return DOMTypeReferenceLocator.TYPE_PARAMS_NO_MATCH;
					}
				} else {
					if( typeArgs == null || typeArgs.length != thisLevelTypeParams.length) {
						if( thisLevelTypeParams.length == 0 ) {
							return DOMTypeReferenceLocator.TYPE_PARAMS_COUNT_MATCH;
						}
						if (typeArgs.length==0) {
							if( equivMatch && bindingIsRaw) {
								return DOMTypeReferenceLocator.TYPE_PARAMS_MATCH;
							}
							if( !bindingIsRaw && !(equivMatch || erasureMatch)) {
								return DOMTypeReferenceLocator.TYPE_PARAMS_NO_MATCH;
							}
							if( !equivMatch || bindingIsRaw)
								return DOMTypeReferenceLocator.TYPE_PARAMS_MATCH;
						}
						return DOMTypeReferenceLocator.TYPE_PARAMS_NO_MATCH;
					}
					for( int j = 0; j < thisLevelTypeParams.length; j++ ) {
						ITypeBinding domBinding = typeArgs[j];
						String patternSig = new String(thisLevelTypeParams[j]);
						IBinding patternBinding = JdtCoreDomPackagePrivateUtility.findBindingForType(node, patternSig);
						if( patternBinding == null ) {
							boolean plusOrMinus = patternSig.startsWith("+") || patternSig.startsWith("-");
							String safePatternString = plusOrMinus ? patternSig.substring(1) : patternSig;
							if( safePatternString.startsWith("Q")) {
								patternBinding = JdtCoreDomPackagePrivateUtility.findUnresolvedBindingForType(node, patternSig);
							} else {
								patternBinding = JdtCoreDomPackagePrivateUtility.findBindingForType(node, safePatternString);
							}
						}
						boolean singleTypeArgMatches = TypeArgumentMatchingUtility.validateSingleTypeArgMatches(exactMatch, patternSig, patternBinding, domBinding, this.locator);
						if( !singleTypeArgMatches ) {
							return DOMTypeReferenceLocator.TYPE_PARAMS_COUNT_MATCH;
						}

					}
				}
			}
		}
		return DOMTypeReferenceLocator.TYPE_PARAMS_MATCH;
	}

	private IBinding findPossiblyUnresolvedBindingForType(ASTNode node, String patternSig) {
		IBinding patternBinding = JdtCoreDomPackagePrivateUtility.findBindingForType(node, patternSig);
		if( patternBinding == null ) {
			boolean plusOrMinus = patternSig.startsWith("+") || patternSig.startsWith("-");
			String safePatternString = plusOrMinus ? patternSig.substring(1) : patternSig;
			if( safePatternString.startsWith("Q")) {
				patternBinding = JdtCoreDomPackagePrivateUtility.findUnresolvedBindingForType(node, safePatternString);
			}
		}
		return patternBinding;
	}

	private boolean isPatternErasureMatch() {
		int r = this.locator.pattern.getMatchRule();
		return (r & SearchPattern.R_ERASURE_MATCH) == SearchPattern.R_ERASURE_MATCH;
	}
	private boolean isPatternEquivalentMatch() {
		int r = this.locator.pattern.getMatchRule();
		return (r & SearchPattern.R_EQUIVALENT_MATCH) == SearchPattern.R_EQUIVALENT_MATCH;
	}
	private boolean isPatternExactMatch() {
		int r = this.locator.pattern.getMatchRule();
		return (r & SearchPattern.R_FULL_MATCH) == SearchPattern.R_FULL_MATCH;
	}

	private int matchMethodTypeArguments(ASTNode node, IMethodBinding method,
			boolean skipImpossibleArg, int level, boolean bindingIsDeclaration) {
		boolean potentialMatchOnly = false;
		if (this.locator.pattern.hasMethodArguments()) {
			ITypeBinding[] argBindings = method.getTypeArguments();
			char[][] goal = this.locator.pattern.methodArguments;
			if( goal == null || goal.length == 0) {
				return level;
			}
			if( argBindings == null || argBindings.length == 0 ) {
				// just check from the node real quick
				List<ASTNode> typeArgsFromNode = null;
				if( node instanceof MethodInvocation mi) {
					typeArgsFromNode = mi.typeArguments();
					potentialMatchOnly = !bindingIsDeclaration ? true : false;
				} else if( node instanceof MethodDeclaration md) {
					typeArgsFromNode = md.typeParameters();
				}
				if(typeArgsFromNode != null && typeArgsFromNode.size() > 0 ) {
					// Something is wrong with the binding. Maybe an error node
					List<ITypeBinding> tmp = typeArgsFromNode.stream().map(DOMASTNodeUtils::getBinding).
							filter(x -> x instanceof ITypeBinding).
							map(x -> (ITypeBinding)x).
							collect(Collectors.toList());
					argBindings = tmp.toArray(new ITypeBinding[tmp.size()]);
				}
			}

			boolean isExactPattern = isPatternExactMatch();
			boolean isErasurePattern = isPatternErasureMatch();
			boolean isEquivPattern = isPatternEquivalentMatch();

			if( argBindings == null || argBindings.length == 0 ) {
				if( goal == null || goal.length == 0 )
					return level;
				// we have a raw usage of this method with no type params in use
				if( isExactPattern || (!isErasurePattern && !isEquivPattern)) {
					return IMPOSSIBLE_MATCH;
				}
				if( !bindingIsDeclaration ) {
					return IMPOSSIBLE_MATCH;
				}
				// Now we have only declarations, so you need to check type Params instead of type args.
				ITypeBinding[] tmp = method.getTypeParameters();
				if( tmp != null && tmp.length > 0 ) {
					return goal != null && goal.length == tmp.length ? ERASURE_MATCH : IMPOSSIBLE_MATCH;
				}
				return ERASURE_MATCH;
			}

			// Now we need to do the hard work of comparing one to another
			if( argBindings.length != goal.length  )
				return IMPOSSIBLE_MATCH;

			boolean inaccurateFound = false;
			boolean erasureFound = false;
			for( int i = 0; i < argBindings.length; i++ ) {
				// Compare each
				String goaliString = new String(goal[i]);
				IBinding patternBinding = findPossiblyUnresolvedBindingForType(node, goaliString);
				if( argBindings[i].isTypeVariable() && patternBinding == null ) {
					continue;
				}
				boolean match = TypeArgumentMatchingUtility.validateSingleTypeArgMatches(isExactPattern, goaliString, patternBinding, argBindings[i]);
				if( !match ) {
					if( isExactPattern || (!isErasurePattern && !isEquivPattern)) {
						return IMPOSSIBLE_MATCH;
					}
					if( potentialMatchOnly ) {
						inaccurateFound = true;
					} else {
						erasureFound = true;
					}
				}
			}
			if(inaccurateFound)
				return INACCURATE_MATCH;
			if( erasureFound ) {
				return ERASURE_MATCH;
			}
		}
		return level;
	}

	protected int matchMethodBindingReturn(IMethodBinding binding) {
		// look at return type only if declaring type is not specified
		if (this.locator.pattern.declaringSimpleName == null) {
			// TODO (frederic) use this call to refine accuracy on return type
			// int newLevel = resolveLevelForType(this.locator.pattern.returnSimpleName, this.locator.pattern.returnQualification, this.locator.pattern.returnTypeArguments, 0, method.returnType);
			if (resolveLevelForType(this.locator.pattern.returnSimpleName, this.locator.pattern.returnQualification,
					binding.getReturnType()) == IMPOSSIBLE_MATCH)
				return IMPOSSIBLE_MATCH;
		}
		return ACCURATE_MATCH;
	}

	private int matchMethodParametersTypes(ASTNode node, IMethodBinding method, boolean skipImpossibleArg, int level) {
		// parameter types
		boolean isExactPattern = isPatternExactMatch();
		int parameterCount = this.locator.pattern.parameterSimpleNames == null ? -1 : this.locator.pattern.parameterSimpleNames.length;
		if (parameterCount > -1) {
			// global verification
			if (method.getParameterTypes() == null) return INACCURATE_MATCH;
			if (parameterCount != method.getParameterTypes().length) return IMPOSSIBLE_MATCH;
			if (method.isRecovered()) {
				// return inaccurate match for ambiguous call (bug 80890)
				return INACCURATE_MATCH;
			}
			boolean foundTypeVariable = false;
			IMethodBinding focusMethodBinding = null;
			boolean checkedFocus = false;
			boolean isBinary = this.locator.pattern!= null && this.locator.pattern.focus instanceof BinaryMethod;
			// verify each parameter
			ITypeBinding[] paramTypes = method.getParameterTypes();
			for (int i = 0; i < parameterCount; i++) {
				ITypeBinding argType = paramTypes[i];
				int newLevel = IMPOSSIBLE_MATCH;
				boolean foundLevel = false;
				if (argType.isMember() || this.locator.pattern.parameterQualifications[i] != null) {
					if (!checkedFocus) {
						focusMethodBinding = getDOMASTMethodBinding(this.locator.pattern, node.getAST());
						checkedFocus = true;
					}
					if (focusMethodBinding != null) {// textual comparison insufficient
						ITypeBinding[] parameters = focusMethodBinding.getParameterTypes();
						if (parameters.length >= parameterCount) {
							newLevel = (isBinary ? argType.getErasure().isEqualTo(parameters[i].getErasure()) : argType.isEqualTo((parameters[i]))) ?
									ACCURATE_MATCH : IMPOSSIBLE_MATCH;
							foundLevel = true;
						}
					}
				} else {
					// TODO (frederic) use this call to refine accuracy on parameter types
//					 newLevel = resolveLevelForType(this.locator.pattern.parameterSimpleNames[i], this.locator.pattern.parameterQualifications[i], this.locator.pattern.parametersTypeArguments[i], 0, argType);
					newLevel = this.resolveLevelForType(this.locator.pattern.parameterSimpleNames[i], this.locator.pattern.parameterQualifications[i], argType);
					// TODO testMethodReferencesElementPatternSingleTypeParameter04 says should be potential match only
					if( argType.isGenericType() ) {
						// this param is also a generic and has its own nested types, but we don't know what they are
						if( newLevel == ACCURATE_MATCH ) {
							ITypeBinding[] nestedParams = argType.getTypeParameters();
							char[][][][] ptaAll = this.locator.pattern.parametersTypeArguments;
							char[][][] ptaThisLevel = ptaAll == null || ptaAll.length == 0 ? null : ptaAll[0];
							boolean patternHasTypeArgs = ptaThisLevel == null || ptaThisLevel.length <= i ?  false : true;
							if( patternHasTypeArgs) {
								char[][] thisParamTypeArgs = ptaThisLevel[i];
								for( int q = 0; q < thisParamTypeArgs.length; q++ ) {
									char[] fromPattern = thisParamTypeArgs[q];
									ITypeBinding fromBinding = nestedParams == null || nestedParams.length == 0 ? null : nestedParams[q];
									if( fromBinding == null ) {
										// pattern expects, binding doesn't have
										newLevel = INACCURATE_MATCH;
									} else {
										//see if they match?
										String fromPatternString = new String(fromPattern);
										IBinding patternBinding = findPossiblyUnresolvedBindingForType(node, fromPatternString);
										boolean match = TypeArgumentMatchingUtility.validateSingleTypeArgMatches(isExactPattern, fromPatternString, patternBinding, fromBinding);
										if( !match ) {
											newLevel = INACCURATE_MATCH;
										}
									}
								}
							}
						}
					}
				}
				if (level > newLevel) {
					if (newLevel == IMPOSSIBLE_MATCH) {
						if (skipImpossibleArg) {
							// Do not consider match as impossible while finding declarations and source level >= 1.5
						 	// (see  bugs https://bugs.eclipse.org/bugs/show_bug.cgi?id=79990, 96761, 96763)
							if (!foundLevel) {
								newLevel = level;
							}
						} else if (argType.isTypeVariable()) {
							newLevel = level;
							foundTypeVariable = true;
						} else {
							return IMPOSSIBLE_MATCH;
						}
					}
					level = newLevel; // can only be downgraded
				}
			}
			if (foundTypeVariable) {
				if (!Modifier.isStatic(method.getModifiers()) && !Modifier.isPrivate(method.getModifiers())) {
					// https://bugs.eclipse.org/bugs/show_bug.cgi?id=123836, No point in textually comparing type variables, captures etc with concrete types.
					if (!checkedFocus)
						focusMethodBinding = getDOMASTMethodBinding(this.locator.pattern, node.getAST());
					if (focusMethodBinding != null
						/* && matchOverriddenMethod(focusMethodBinding.getDeclaringClass(), focusMethodBinding, method)*/
						 && (focusMethodBinding.overrides(method) || method.overrides(focusMethodBinding))) {
						return ACCURATE_MATCH;
					}
				}
				return IMPOSSIBLE_MATCH;
			}
		}
		return level;
	}

	public IMethodBinding getDOMASTMethodBinding(MethodPattern methodPattern, AST ast) {
		char[] typeName = PatternLocator.qualifiedPattern(methodPattern.declaringSimpleName, methodPattern.declaringQualification);
		if( typeName != null ) {
			var type = ast.resolveWellKnownType(new String(typeName));
			if (type != null) {
				for (IMethodBinding method : type.getDeclaredMethods()) {
					if (Objects.equals(method.getJavaElement(), methodPattern.focus)) {
						return method;
					}
				}
			}
		}
		return null;
	}

	private int computeResolveLevel(org.eclipse.jdt.core.dom.ASTNode node, IBinding binding, MatchLocator locator) {
		boolean isExactPattern = isPatternExactMatch();
		boolean isErasurePattern = isPatternErasureMatch();
		boolean isEquivPattern = isPatternEquivalentMatch();

		if (node.getLocationInParent() == SingleMemberAnnotation.VALUE_PROPERTY
			&& node.getParent() instanceof SingleMemberAnnotation singleMemberAnnotation) {
			var valuePairs = singleMemberAnnotation.resolveAnnotationBinding().getDeclaredMemberValuePairs();
			if (valuePairs != null && valuePairs.length > 0) {
				binding = valuePairs[0].getMethodBinding();
			}
		}
		if (binding instanceof IMethodBinding method) {
			boolean skipVerif = this.locator.pattern.findDeclarations /*&& this.locator.mayBeGeneric*/;
			int methodLevel = matchMethod(node, method, skipVerif, false);
			if (methodLevel == IMPOSSIBLE_MATCH) {
				IMethodBinding decl = method.getMethodDeclaration();
				if (method != decl) {
					methodLevel = matchMethod(node, decl, skipVerif, true);
				}
				if (methodLevel == IMPOSSIBLE_MATCH) {
					return IMPOSSIBLE_MATCH;
				} else {
					method = decl;
				}
			}

			// declaring type
			if (this.locator.pattern.declaringSimpleName == null && this.locator.pattern.declaringQualification == null)
				return methodLevel; // since any declaring class will do

			boolean subType = ((method.getModifiers() & Modifier.STATIC) == 0) && ((method.getModifiers() & Modifier.PRIVATE) == 0);
			if (subType && this.locator.pattern.declaringQualification != null && method.getDeclaringClass() != null && method.getDeclaringClass().getPackage() != null) {
				subType = CharOperation.compareWith(this.locator.pattern.declaringQualification, method.getDeclaringClass().getPackage().getName().toCharArray()) == 0;
			}
			ITypeBinding declaring = method.getDeclaringClass();
			int declaringLevel = subType
				? resolveLevelAsSubtype(this.locator.pattern.declaringSimpleName, this.locator.pattern.declaringQualification, declaring, method.getName(), null, declaring.getPackage().getName(), (method.getModifiers() & Modifier.DEFAULT) != 0)
				: this.resolveLevelForType(this.locator.pattern.declaringSimpleName, this.locator.pattern.declaringQualification, declaring);
			int weakerLevel = findWeakerLevel((methodLevel & PatternLocator.MATCH_LEVEL_MASK), (declaringLevel & PatternLocator.MATCH_LEVEL_MASK));
			int matchLevel = (weakerLevel & PatternLocator.MATCH_LEVEL_MASK);
			if( matchLevel != ACCURATE_MATCH) {
				char[][][] superTypeNames = (Modifier.isDefault(method.getModifiers()) && this.pattern.focus == null) ? this.samePkgSuperDeclaringTypeNames: this.allSuperDeclaringTypeNames;
				if (superTypeNames != null && resolveLevelAsSuperInvocation(method.getDeclaringClass(), method.getParameterTypes(), superTypeNames, true)) {
						declaringLevel = methodLevel // since this is an ACCURATE_MATCH so return the possibly weaker match
							| SUPER_INVOCATION_FLAVOR; // this is an overridden method => add flavor to returned level
				}
				if ((declaringLevel & FLAVORS_MASK) != 0) {
					// level got some flavors => return it
					return declaringLevel;
				}
				if( isExactPattern || (!isErasurePattern && !isEquivPattern)) {
					return IMPOSSIBLE_MATCH;
				} else if( isEquivPattern && matchLevel == ERASURE_MATCH) {
					return IMPOSSIBLE_MATCH;
				}
			}
			return weakerLevel;
		}
		return INACCURATE_MATCH;
	}

	protected int resolveLevel(MethodInvocation messageSend) {
		IMethodBinding method = messageSend.resolveMethodBinding();
		if (method == null) {
			return INACCURATE_MATCH;
		}
//		if (messageSend.resolvedType == null) {
//			// Closest match may have different argument numbers when ProblemReason is NotFound
//			// see MessageSend#resolveType(BlockScope)
//			// see bug https://bugs.eclipse.org/bugs/show_bug.cgi?id=97322
//			if (this.pattern.parameterSimpleNames == null || messageSend.arguments().size() == this.pattern.parameterSimpleNames.length) {
//				return INACCURATE_MATCH;
//			}
//			return IMPOSSIBLE_MATCH;
//		}

		int methodLevel = matchMethod(messageSend, method, false, false);
		if (methodLevel == IMPOSSIBLE_MATCH) {
			if (method != method.getMethodDeclaration())
				methodLevel = matchMethod(messageSend, method.getMethodDeclaration(), false, true);
			if (methodLevel == IMPOSSIBLE_MATCH)
				return IMPOSSIBLE_MATCH;
			method = method.getMethodDeclaration();
		}

		// receiver type
		if (this.pattern.declaringSimpleName == null && this.pattern.declaringQualification == null)
			return methodLevel; // since any declaring class will do

		int declaringLevel;
		ITypeBinding receiverType = messageSend.getExpression() != null ? messageSend.getExpression().resolveTypeBinding() : method.getDeclaringClass();
		if (receiverType.isArray()) {
			receiverType = messageSend.getAST().resolveWellKnownType(Object.class.getName());
		}
		if (isVirtualInvoke(method, messageSend) && receiverType != null && !receiverType.isArray() && !receiverType.isIntersectionType()) {
			var packageBinding = receiverType.getPackage();
			declaringLevel = resolveLevelAsSubtype(this.pattern.declaringSimpleName, this.pattern.declaringQualification, receiverType, method.getName(), method.getParameterTypes(), packageBinding != null ? packageBinding.getName() : null, Modifier.isDefault(method.getModifiers()));
			if (declaringLevel == IMPOSSIBLE_MATCH) {
				if (method.getDeclaringClass() == null || this.allSuperDeclaringTypeNames == null) {
					declaringLevel = INACCURATE_MATCH;
				} else {
					char[][][] superTypeNames = (Modifier.isDefault(method.getModifiers()) && this.pattern.focus == null) ? this.samePkgSuperDeclaringTypeNames: this.allSuperDeclaringTypeNames;
					if (superTypeNames != null && resolveLevelAsSuperInvocation(receiverType, method.getParameterTypes(), superTypeNames, true)) {
							declaringLevel = methodLevel // since this is an ACCURATE_MATCH so return the possibly weaker match
								| SUPER_INVOCATION_FLAVOR; // this is an overridden method => add flavor to returned level
					}
				}
			}
			if ((declaringLevel & FLAVORS_MASK) != 0) {
				// level got some flavors => return it
				return declaringLevel;
			}
		} else {
			declaringLevel = resolveLevelForType(this.pattern.declaringSimpleName, this.pattern.declaringQualification, method.getDeclaringClass());
		}
		return (methodLevel & MATCH_LEVEL_MASK) > (declaringLevel & MATCH_LEVEL_MASK) ? declaringLevel : methodLevel; // return the weaker match
	}
	protected boolean isVirtualInvoke(IMethodBinding method, MethodInvocation messageSend) {
		return !Modifier.isStatic(method.getModifiers()) && !Modifier.isPrivate(method.getModifiers())
			&& !(Modifier.isDefault(method.getModifiers()) && this.pattern.focus != null
			&& !CharOperation.equals(this.pattern.declaringPackageName, method.getDeclaringClass().getPackage().getName().toCharArray()));
}

	@Override
	public LocatorResponse resolveLevel(org.eclipse.jdt.core.dom.ASTNode node, IBinding binding, MatchLocator locator) {
		if (node instanceof MethodInvocation invocation) {
			return toResponse(resolveLevel(invocation));
		}
		int level = computeResolveLevel(node, binding, locator);
		if (node instanceof MethodDeclaration declaration) {
			return toResponse(level > IMPOSSIBLE_MATCH && matchesDeclaration(node, binding.getJavaElement(), declaration.resolveBinding(), locator) ? level : IMPOSSIBLE_MATCH);
		}
		return toResponse(level);
	}
	private int findWeakerLevel(int i, int j) {
		int[] ints = {DOMPatternLocator.IMPOSSIBLE_MATCH,
				DOMPatternLocator.POSSIBLE_MATCH,
				DOMPatternLocator.INACCURATE_MATCH,
				DOMPatternLocator.ERASURE_MATCH,
				DOMPatternLocator.ACCURATE_MATCH};
		List<Integer> list = Arrays.stream(ints).boxed().collect(Collectors.toList());
		int iIndex = list.indexOf(i);
		int jIndex = list.indexOf(j);
		return iIndex > jIndex ? j : i;
	}

	protected int resolveLevelAsSubtype(char[] simplePattern, char[] qualifiedPattern, ITypeBinding type, String methodName, ITypeBinding[] argumentTypes, String packageName, boolean isDefault) {
		if (type == null) return INACCURATE_MATCH;

		int level = this.resolveLevelForType(simplePattern, qualifiedPattern, type);
		if (level != IMPOSSIBLE_MATCH) {
			if (isDefault && !Objects.equals(packageName, type.getPackage().getName())) {
				return IMPOSSIBLE_MATCH;
			}
			IMethodBinding method = argumentTypes == null ? null : getDOMASTMethodBinding(type, methodName, argumentTypes);
			if (((method != null && !Modifier.isAbstract(method.getModifiers()) || !Modifier.isAbstract(type.getModifiers()))) && !type.isInterface()) { // if concrete, then method is overridden
				level |= PatternLocator.OVERRIDDEN_METHOD_FLAVOR;
			}
			return level;
		}

		// matches superclass
		if (!type.isInterface() && !type.getQualifiedName().equals(Object.class.getName())) {
			level = resolveLevelAsSubtype(simplePattern, qualifiedPattern, type.getSuperclass(), methodName, argumentTypes, packageName, isDefault);
			if (level != IMPOSSIBLE_MATCH) {
				if (argumentTypes != null) {
					// need to verify if method may be overridden
					IMethodBinding method = getDOMASTMethodBinding(type, methodName, argumentTypes);
					if (method != null) { // one method match in hierarchy
						if ((level & PatternLocator.OVERRIDDEN_METHOD_FLAVOR) != 0) {
							// this method is already overridden on a super class, current match is impossible
							return IMPOSSIBLE_MATCH;
						}
						if (!Modifier.isAbstract(method.getModifiers()) && !type.isInterface()) {
							// store the fact that the method is overridden
							level |= PatternLocator.OVERRIDDEN_METHOD_FLAVOR;
						}
					}
				}
				return level | PatternLocator.SUB_INVOCATION_FLAVOR; // add flavor to returned level
			}
		}

		// matches interfaces
		ITypeBinding[] interfaces = type.getInterfaces();
		if (interfaces == null) return INACCURATE_MATCH;
		for (ITypeBinding ref : interfaces) {
			level = resolveLevelAsSubtype(simplePattern, qualifiedPattern, ref, methodName, null, packageName, isDefault);
			if (level != IMPOSSIBLE_MATCH) {
				if (!Modifier.isAbstract(type.getModifiers()) && !type.isInterface()) { // if concrete class, then method is overridden
					level |= PatternLocator.OVERRIDDEN_METHOD_FLAVOR;
				}
				return level | PatternLocator.SUB_INVOCATION_FLAVOR; // add flavor to returned level
			}
		}
		return IMPOSSIBLE_MATCH;
	}
	/*
	 * Return whether the given type binding or one of its possible super interfaces
	 * matches a type in the declaring type names hierarchy.
	 */
	private boolean resolveLevelAsSuperInvocation(ITypeBinding type, ITypeBinding[] argumentTypes, char[][][] superTypeNames, boolean methodAlreadyVerified) {
		char[][] compoundName = Arrays.stream(type.getQualifiedName().split("\\.")).map(String::toCharArray).toArray(char[][]::new);
		for (char[][] superTypeName : superTypeNames) {
			if (CharOperation.equals(superTypeName, compoundName)) {
				// need to verify if the type implements the pattern method
				if (methodAlreadyVerified) return true; // already verified before enter into this method (see resolveLevel(MessageSend))
				if (Arrays.stream(type.getDeclaredMethods())
					.filter(method -> this.locator.matchesName(this.locator.pattern.selector, method.getName().toCharArray()))
					.anyMatch(method -> Arrays.equals(method.getParameterTypes(), argumentTypes, Comparator.comparing(t -> t.getErasure().getKey())))) {
					return true;
				}
			}
		}

		// If the given type is an interface then a common super interface may be found
		// in a parallel branch of the super hierarchy, so we need to verify all super interfaces.
		// If it's a class then there's only one possible branch for the hierarchy and
		// this branch has been already verified by the test above
		if (type.isInterface()) {
			ITypeBinding[] interfaces = type.getInterfaces();
			if (interfaces == null) return false;
			for (ITypeBinding ref : interfaces) {
				if (resolveLevelAsSuperInvocation(ref, argumentTypes, superTypeNames, false)) {
					return true;
				}
			}
		}
		return false;
	}

	@Override
	public void initializePolymorphicSearch(MatchLocator locator) {
		long start = 0;
		if (BasicSearchEngine.VERBOSE) {
			start = System.currentTimeMillis();
		}
		try {
			SuperTypeNamesCollector namesCollector =
				new SuperTypeNamesCollector(
					this.pattern,
					this.pattern.declaringSimpleName,
					this.pattern.declaringQualification,
					locator,
					this.pattern.declaringType,
					locator.progressMonitor);
			this.allSuperDeclaringTypeNames = namesCollector.collect();
			this.samePkgSuperDeclaringTypeNames = namesCollector.getSamePackageSuperTypeNames();
		} catch (JavaModelException e) {
			// inaccurate matches will be found
		}
		if (BasicSearchEngine.VERBOSE) {
			trace("Time to initialize polymorphic search: "+(System.currentTimeMillis()-start)); //$NON-NLS-1$
		}
	}

	public boolean matchesDeclaration(ASTNode reference, IJavaElement element, IMethodBinding methodBinding, MatchLocator locator) {
		// If method parameters verification was not valid, then try to see if method arguments can match a method in hierarchy
		if (this.methodDeclarationsWithInvalidParam.containsKey(reference)) {
			// First see if this reference has already been resolved => report match if validated
			Boolean report = this.methodDeclarationsWithInvalidParam.get(reference);
			if (report != null) {
				return report.booleanValue();
			}
			if (matchOverriddenMethod(methodBinding.getDeclaringClass(), methodBinding, null)) {
				this.methodDeclarationsWithInvalidParam.put(reference, Boolean.TRUE);
				return true;
			}
			if (isTypeInSuperDeclaringTypeNames(methodBinding.getDeclaringClass().getQualifiedName())) {
				IMethodBinding patternBinding = getMethodBindingFromPattern(reference.getAST());
				if (patternBinding != null) {
					if (!matchOverriddenMethod(patternBinding.getDeclaringClass(), patternBinding, methodBinding)) {
						this.methodDeclarationsWithInvalidParam.put(reference, Boolean.FALSE);
						return false;
					}
				}
				this.methodDeclarationsWithInvalidParam.put(reference, Boolean.TRUE);
				return true;
			}
			this.methodDeclarationsWithInvalidParam.put(reference, Boolean.FALSE);
			return false;
		}
		return true;
	}

	private boolean isTypeInSuperDeclaringTypeNames(String qualifiedName) {
		if (this.allSuperDeclaringTypeNames == null) {
			return false;
		}
		char[][] compound = Arrays.stream(qualifiedName.split("\\.")).map(String::toCharArray).toArray(char[][]::new);
		return Arrays.stream(this.allSuperDeclaringTypeNames).anyMatch(name -> CharOperation.equals(name, compound));
	}

	// This works for only methods of parameterized types.
	private boolean matchOverriddenMethod(ITypeBinding type, IMethodBinding method, IMethodBinding matchMethod) {
		if (type == null || this.pattern.selector == null) return false;

		List<ITypeBinding> parents = new ArrayList<>();
		if (!type.isInterface() && !Objects.equals(type.getQualifiedName(), Object.class.getName())) {
			parents.add(type.getSuperclass());
		}
		parents.addAll(Arrays.asList(type.getInterfaces()));
		// matches superclass
		for (ITypeBinding superType : parents) {
			if (superType.isParameterizedType()) {
				for (IMethodBinding candidate : superType.getDeclaredMethods()) {
					if (Arrays.equals(candidate.getName().toCharArray(), this.pattern.selector) && areParametersEqual(candidate, method)) {
						if (matchMethod == null) {
							if (methodParametersEqualsPattern(candidate.getMethodDeclaration() /* .original() with ECJ */)) return true;
						} else {
							if (areParametersEqual(candidate.getMethodDeclaration() /* .original() with ECJ */, matchMethod)) return true;
						}
					}
				}
			}
			if (matchOverriddenMethod(superType, method, matchMethod)) {
				return true;
			}
		}
		return false;
	}

	private boolean areParametersEqual(IMethodBinding one, IMethodBinding another) {
		var first = one.getParameterTypes();
		var second = another.getParameterTypes();
		if (Objects.equals(first, second)) {
			return true;
		}
		if (first == null || second == null) {
			return false;
		}
		if (first.length != second.length) {
			return false;
		}
		for (int i = 0; i < first.length; i++) {
			if (!first[i].isEqualTo(second[i])) {
				return false;
			}
		}
		return true;
	}

	/*
	 * Return whether method parameters are equals to pattern ones.
	 */
	private boolean methodParametersEqualsPattern(IMethodBinding method) {
		ITypeBinding[] methodParameters = method.getParameterTypes();

		int length = methodParameters.length;
		if (length != this.pattern.parameterSimpleNames.length) return false;

		for (int i = 0; i < length; i++) {
			char[] paramQualifiedName = qualifiedPattern(this.pattern.parameterSimpleNames[i], this.pattern.parameterQualifications[i]);
			if (!CharOperation.match(paramQualifiedName, methodParameters[i].getName() /* readableName() with ECJ*/.toCharArray(), this.isCaseSensitive)) {
				return false;
			}
		}
		return true;
	}

	private IMethodBinding getMethodBindingFromPattern(AST context) {
		if (this.pattern.focus instanceof IMethod method) {
			var type = method.getDeclaringType().getFullyQualifiedName('$');
			var typeBinding = context.resolveWellKnownType(type);
			if (typeBinding != null) {
				var res = Arrays.stream(typeBinding.getDeclaredMethods())
					.filter(child -> Objects.equals(method.getElementName(), child.getName()))
					.filter(child -> method.getParameterTypes().length == child.getParameterTypes().length)
					.filter(child -> Objects.equals(method, child.getJavaElement()))
					.findFirst();
				if (res.isPresent()) {
					return res.get();
				}
			}
		}
		return null;
	}
}
