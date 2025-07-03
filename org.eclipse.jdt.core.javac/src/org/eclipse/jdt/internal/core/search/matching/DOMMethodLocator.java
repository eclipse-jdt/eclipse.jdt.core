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

import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.compiler.CharOperation;
import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.Annotation;
import org.eclipse.jdt.core.dom.AnnotationTypeMemberDeclaration;
import org.eclipse.jdt.core.dom.Expression;
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
import org.eclipse.jdt.core.dom.ThisExpression;
import org.eclipse.jdt.core.dom.TypeMethodReference;
import org.eclipse.jdt.core.search.IJavaSearchConstants;
import org.eclipse.jdt.core.search.SearchMatch;
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
		if( type == null )
			return null;
		return Stream.of(type.getDeclaredMethods())
				.filter(method -> Objects.equals(method.getName(), methodName))
				.filter(method -> compatibleByErasure(method.getParameterTypes(), argumentTypes))
				.findAny().orElse(null);
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
		int fineGrain = this.locator.pattern.fineGrain;
		if (fineGrain != 0) {
			Expression expr = node.getExpression();
			if ((fineGrain & IJavaSearchConstants.IMPLICIT_THIS_REFERENCE) == 0 && expr == null) {
				return toResponse(IMPOSSIBLE_MATCH);
			}
			if ((fineGrain & IJavaSearchConstants.QUALIFIED_REFERENCE) == 0 && expr != null && !(expr instanceof ThisExpression)) {
				return toResponse(IMPOSSIBLE_MATCH);
			}
			if ((fineGrain & IJavaSearchConstants.THIS_REFERENCE) == 0 && expr instanceof ThisExpression) {
				return toResponse(IMPOSSIBLE_MATCH);
			}
		}
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
		if ((this.locator.pattern.fineGrain & IJavaSearchConstants.METHOD_REFERENCE_EXPRESSION) == 0) {
			return toResponse(IMPOSSIBLE_MATCH);
		}
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
			if (this.pattern.fineGrain != 0 && (this.pattern.fineGrain & IJavaSearchConstants.SUPER_REFERENCE) == 0) {
				return toResponse(IMPOSSIBLE_MATCH);
			}
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

	private ITypeBinding findPossiblyUnresolvedBindingForType(ASTNode node, String patternSig) {
		ITypeBinding patternBinding = JdtCoreDomPackagePrivateUtility.findBindingForType(node, patternSig) instanceof ITypeBinding ptb
				? ptb : null;
		if( patternBinding == null ) {
			boolean plusOrMinus = patternSig.startsWith("+") || patternSig.startsWith("-");
			String safePatternString = plusOrMinus ? patternSig.substring(1) : patternSig;
			if( safePatternString.startsWith("Q")) {
				patternBinding = JdtCoreDomPackagePrivateUtility.findBindingForType(node, patternSig) instanceof ITypeBinding ptb
					? ptb : null;
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
			boolean isExactPattern = isPatternExactMatch();
			boolean isErasurePattern = isPatternErasureMatch();
			boolean isEquivPattern = isPatternEquivalentMatch();
			boolean methodIsRaw = method.isRawMethod();
			ITypeBinding[] argBindings = node instanceof MethodDeclaration ?
					method.getTypeParameters() :
					method.getTypeArguments();
			char[][] goal = this.locator.pattern.methodArguments;

			if( goal.length > 0 && methodIsRaw && isExactPattern) {
				return IMPOSSIBLE_MATCH;
			}


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
				ITypeBinding patternBinding = findPossiblyUnresolvedBindingForType(node, goaliString);
				ITypeBinding argBinding = argBindings[i];
				if(argBinding.isTypeVariable() && patternBinding == null ) {
					continue;
				}
				boolean match =
					TypeArgumentMatchingUtility.validateSingleTypeArgMatches(isExactPattern, goaliString, patternBinding, argBindings[i])
					| (bindingIsDeclaration && patternBinding != null && patternBinding.isCastCompatible(argBinding));
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
		if (node.getLocationInParent() == SingleMemberAnnotation.VALUE_PROPERTY
			&& node.getParent() instanceof SingleMemberAnnotation singleMemberAnnotation) {
			var valuePairs = singleMemberAnnotation.resolveAnnotationBinding().getDeclaredMemberValuePairs();
			if (valuePairs != null && valuePairs.length > 0) {
				binding = valuePairs[0].getMethodBinding();
			}
		}

		if (binding instanceof IMethodBinding method) {
			boolean skipVerif = this.locator.pattern.findDeclarations /*&& this.locator.mayBeGeneric*/;
			return resolveLevelForNodeWithMethodBinding(node, method, null, skipVerif, true);
		}
		return IMPOSSIBLE_MATCH;
	}

	protected int resolveLevel(MethodInvocation messageSend) {
		IMethodBinding invocationBinding = messageSend.resolveMethodBinding();
		ITypeBinding initialReceiverType = messageSend.getExpression() != null ? messageSend.getExpression().resolveTypeBinding() : null;
		return resolveLevelForNodeWithMethodBinding(messageSend, invocationBinding, initialReceiverType, false, false);
	}

	protected int resolveLevelForNodeWithMethodBinding(ASTNode messageSend,
			IMethodBinding invocationBinding, ITypeBinding initialReceiverType,
			boolean skipVerif, boolean nullParamsForSubTypeCheck) {
		boolean isExactPattern = isPatternExactMatch();
		boolean isErasurePattern = isPatternErasureMatch();
		boolean isEquivPattern = isPatternEquivalentMatch();

		IMethodBinding invocationOrDeclarationBinding = invocationBinding;
		IMethodBinding declarationBinding = null;
		if (invocationOrDeclarationBinding == null) {
			return INACCURATE_MATCH;
		}

		int invocationLevel = matchMethod(messageSend, invocationBinding, skipVerif, false);
		int declarationLevel = IMPOSSIBLE_MATCH;
		if (invocationLevel == IMPOSSIBLE_MATCH) {
			declarationBinding = invocationBinding.getMethodDeclaration();
			declarationLevel = matchMethod(messageSend, declarationBinding, skipVerif, true);
			if (declarationLevel == IMPOSSIBLE_MATCH)
				return IMPOSSIBLE_MATCH;
			invocationOrDeclarationBinding = declarationBinding;
		}

		int invocOrDeclLevel = invocationLevel == IMPOSSIBLE_MATCH ? declarationLevel : invocationLevel;
		if (invocationBinding.isRawMethod() && (this.locator.pattern.hasTypeArguments() || this.locator.pattern.hasTypeParameters())) {
			invocOrDeclLevel = findWeakerLevel(invocOrDeclLevel, ERASURE_MATCH);
		}
		// receiver type
		if (this.pattern.declaringSimpleName == null && this.pattern.declaringQualification == null) {
			// since any declaring class will do
			return invocOrDeclLevel;
		}

//		ITypeBinding invocationDeclClass = invocationBinding.getDeclaringClass();
//		ITypeBinding declBindingClass = declarationBinding == null ? null : declarationBinding.getDeclaringClass();
//		String k1 = invocationDeclClass == null ? null : invocationDeclClass.getKey();
//		String k2 = declBindingClass == null ? null : declBindingClass.getKey();
//		String q1 = invocationDeclClass == null ? null : invocationDeclClass.getQualifiedName();
//		String q2 = declBindingClass == null ? null : declBindingClass.getQualifiedName();
//		String b1 = invocationDeclClass == null ? null : invocationDeclClass.getBinaryName();
//		String b2 = declBindingClass == null ? null : declBindingClass.getBinaryName();
		int declaringLevel;
		ITypeBinding receiverType = initialReceiverType != null ? initialReceiverType : invocationOrDeclarationBinding.getDeclaringClass();
		if (shouldResolveSubSuperLevel(messageSend, receiverType, invocationOrDeclarationBinding, invocOrDeclLevel)) {
			declaringLevel = resolveSubSuperLevel(messageSend, receiverType, invocationOrDeclarationBinding, invocOrDeclLevel, nullParamsForSubTypeCheck);
		} else {
			declaringLevel = resolveLevelForType(this.pattern.declaringSimpleName, this.pattern.declaringQualification, invocationOrDeclarationBinding.getDeclaringClass());
		}

		int declaringFlavors = declaringLevel & FLAVORS_MASK;

		int noFlavorInvocOrDecl = invocOrDeclLevel & MATCH_LEVEL_MASK;
		int noFlavorDeclaringLevel = declaringLevel & MATCH_LEVEL_MASK;
		int weakerMethod = findWeakerLevel(noFlavorInvocOrDecl, noFlavorDeclaringLevel);
		int matchLevel = (weakerMethod & PatternLocator.MATCH_LEVEL_MASK);

		int retval = weakerMethod;
		if( matchLevel != ACCURATE_MATCH) {
			boolean isDefault = Modifier.isDefault(invocationOrDeclarationBinding.getModifiers());
			boolean nullFocus = this.pattern.focus == null;
			char[][][] superTypeNames = isDefault && nullFocus ? this.samePkgSuperDeclaringTypeNames: this.allSuperDeclaringTypeNames;
			if (superTypeNames != null && resolveLevelAsSuperInvocation(invocationOrDeclarationBinding.getDeclaringClass(), invocationOrDeclarationBinding.getParameterTypes(), superTypeNames, true)) {
				// since this is an ACCURATE_MATCH so return the possibly weaker match
				// this is an overridden method => add flavor to returned level
				declaringLevel = invocOrDeclLevel | SUPER_INVOCATION_FLAVOR;
			}
			if ((declaringLevel & FLAVORS_MASK) != 0) {
				// level got some flavors => return it
				retval = declaringLevel;
			} else if( isExactPattern || (!isErasurePattern && !isEquivPattern)) {
				retval = IMPOSSIBLE_MATCH;
			} else if( isEquivPattern && matchLevel == ERASURE_MATCH) {
				retval = IMPOSSIBLE_MATCH;
			}
		} else if (declaringFlavors != 0) {
			// level got some flavors => return it
			retval = declaringLevel;
		}

		return retval;
	}

	private boolean shouldResolveSubSuperLevel(ASTNode messageSend, ITypeBinding receiverType, IMethodBinding invocationOrDeclarationBinding, int invocOrDeclLevel) {
		if (receiverType != null && receiverType.isArray()) {
			receiverType = messageSend.getAST().resolveWellKnownType(Object.class.getName());
		}
		boolean isVirtuallyInvoked = isVirtualInvoke(invocationOrDeclarationBinding);
		boolean excluded = receiverType == null || receiverType.isArray() || receiverType.isIntersectionType();
		if (isVirtuallyInvoked && !excluded) {
			return true;
		}
		return false;
	}

	private int resolveSubSuperLevel(ASTNode messageSend, ITypeBinding receiverType, IMethodBinding invocationOrDeclarationBinding,
			int invocOrDeclLevel, boolean useNullParameterTypes) {
		int retLevel;
		if (receiverType == null || receiverType.isArray()) {
			receiverType = messageSend.getAST().resolveWellKnownType(Object.class.getName());
		}
		var packageBinding = receiverType.getPackage();
		boolean isDefault = Modifier.isDefault(invocationOrDeclarationBinding.getModifiers());
		String packageBindingName = packageBinding != null ? packageBinding.getName() : null;
		ITypeBinding[] parameterTypes = useNullParameterTypes ? null : invocationOrDeclarationBinding.getParameterTypes();
		String bindingName = invocationOrDeclarationBinding.getName();
		retLevel = resolveLevelAsSubtype(this.pattern.declaringSimpleName, this.pattern.declaringQualification,
				receiverType, bindingName, parameterTypes, packageBindingName, isDefault, invocationOrDeclarationBinding);
		if (retLevel == IMPOSSIBLE_MATCH) {
			if (invocationOrDeclarationBinding.getDeclaringClass() == null || this.allSuperDeclaringTypeNames == null) {
				retLevel = INACCURATE_MATCH;
			} else {
				boolean nullFocusDefault = Modifier.isDefault(invocationOrDeclarationBinding.getModifiers()) && this.pattern.focus == null;
				char[][][] superTypeNames = nullFocusDefault ? this.samePkgSuperDeclaringTypeNames: this.allSuperDeclaringTypeNames;
				if (superTypeNames != null && resolveLevelAsSuperInvocation(receiverType, invocationOrDeclarationBinding.getParameterTypes(), superTypeNames, true)) {
					retLevel = invocOrDeclLevel // since this is an ACCURATE_MATCH so return the possibly weaker match
							| SUPER_INVOCATION_FLAVOR; // this is an overridden method => add flavor to returned level
				}
			}
		}
		return retLevel;
	}

	protected boolean isVirtualInvoke(IMethodBinding method) {
		// This method makes absolutely zero sense to me.
		String t = method == null ? null :
			method.getDeclaringClass() == null ? null :
				method.getDeclaringClass().getPackage() == null ? null :
					method.getDeclaringClass().getPackage().getName();
		boolean notStatic = !Modifier.isStatic(method.getModifiers());
		boolean notPrivate = !Modifier.isPrivate(method.getModifiers());
		boolean isDefault = Modifier.isDefault(method.getModifiers());
		boolean nonNullFocus = this.pattern.focus != null;
		boolean packageMatch = CharOperation.equals(this.pattern.declaringPackageName, t.toCharArray());
		boolean r = notStatic && notPrivate	&& !(isDefault && nonNullFocus && !packageMatch);
		return r;
	}

	@Override
	public LocatorResponse resolveLevel(org.eclipse.jdt.core.dom.ASTNode node, IBinding binding, MatchLocator locator) {
		if (node instanceof MethodInvocation invocation) {
			return toResponse(resolveLevel(invocation));
		}
		int level = computeResolveLevel(node, binding, locator);
		if (node instanceof MethodDeclaration declaration && binding != null) {
			if( level == IMPOSSIBLE_MATCH ) {
				return toResponse(IMPOSSIBLE_MATCH);
			}
			boolean matchesDecl = matchesDeclaration(node, binding.getJavaElement(), declaration.resolveBinding(), locator);
			return toResponse(matchesDecl ? level : IMPOSSIBLE_MATCH);
		}
		return toResponse(level);
	}

	private int findWeakerLevel(int i, int j) {
		int levelI = i & PatternLocator.MATCH_LEVEL_MASK;
		int levelJ = j & PatternLocator.MATCH_LEVEL_MASK;
		int[] ints = {DOMPatternLocator.IMPOSSIBLE_MATCH,
				DOMPatternLocator.POSSIBLE_MATCH,
				DOMPatternLocator.INACCURATE_MATCH,
				DOMPatternLocator.ERASURE_MATCH,
				DOMPatternLocator.ACCURATE_MATCH};
		List<Integer> list = Arrays.stream(ints).boxed().collect(Collectors.toList());
		int iIndex = list.indexOf(levelI);
		int jIndex = list.indexOf(levelJ);
		return iIndex > jIndex ? j : i;
	}

	protected int resolveLevelAsSubtype_basic(char[] simplePattern, char[] qualifiedPattern, ITypeBinding type,
			IMethodBinding method, String packageName, boolean isDefault, boolean methodIdenticalToOriginal) {
		if (type == null) return INACCURATE_MATCH;

		int level = this.resolveLevelForType(simplePattern, qualifiedPattern, type);
		if (level != IMPOSSIBLE_MATCH) {
			if (isDefault && !Objects.equals(packageName, type.getPackage().getName())) {
				return IMPOSSIBLE_MATCH;
			}

			// if concrete, then method is overridden
			if( !methodIdenticalToOriginal && isConcrete(method, type)) {
				level |= PatternLocator.OVERRIDDEN_METHOD_FLAVOR;
			}
			return level;
		}
		return -1;
	}

	private boolean isConcrete(IMethodBinding method, ITypeBinding type) {
		boolean nullMethod = method == null;
		boolean abstractMethod = method == null ? false : Modifier.isAbstract(method.getModifiers());
		boolean abstractType = Modifier.isAbstract(type.getModifiers());
		if ((!nullMethod && !abstractMethod || !abstractType) && !type.isInterface()) {
			return true;
		}
		return false;
	}

	protected int resolveLevelAsSubtype_super(char[] simplePattern, char[] qualifiedPattern, ITypeBinding type,
			String methodName, ITypeBinding[] argumentTypes, IMethodBinding method,
			String packageName, boolean isDefault, IMethodBinding originalQuery) {
		// matches superclass
		if (!type.isInterface() && !type.getQualifiedName().equals(Object.class.getName())) {
			int level = resolveLevelAsSubtype(simplePattern, qualifiedPattern, type.getSuperclass(), methodName, argumentTypes, packageName, isDefault, originalQuery);
			if (level != IMPOSSIBLE_MATCH) {
				// need to verify if method may be overridden
				if (method != null) { // one method match in hierarchy
					if ((level & PatternLocator.OVERRIDDEN_METHOD_FLAVOR) != 0) {
						// this method is already overridden on a super class, current match is impossible
						// testMethodDeclaration01 requires we NOT return impossible here, wtf
						return IMPOSSIBLE_MATCH;
					}
					if (isConcrete(method, type)) {
						// store the fact that the method is overridden
						level |= PatternLocator.OVERRIDDEN_METHOD_FLAVOR;
					}
				}
				return level | PatternLocator.SUB_INVOCATION_FLAVOR; // add flavor to returned level
			}
		}
		return -1;
	}

	protected int resolveLevelAsSubtype_interfaces(char[] simplePattern, char[] qualifiedPattern, ITypeBinding type,
			String methodName, ITypeBinding[] argumentTypes, IMethodBinding method,
			String packageName, boolean isDefault, IMethodBinding originalQuery) {
		// matches interfaces
		boolean concrete = isConcrete(method, type);
		ITypeBinding[] interfaces = type.getInterfaces();
		//if (interfaces == null) return INACCURATE_MATCH;
		if (interfaces == null) return -1;
		for (ITypeBinding ref : interfaces) {
			int level = resolveLevelAsSubtype(simplePattern, qualifiedPattern,
					ref, methodName, null, packageName, isDefault, originalQuery);
			if (level != IMPOSSIBLE_MATCH) {
				// if concrete class, then method is overridden
				if (concrete) {
					level |= PatternLocator.OVERRIDDEN_METHOD_FLAVOR;
				}
				return level | PatternLocator.SUB_INVOCATION_FLAVOR; // add flavor to returned level
			}
		}
		return -1;
	}

	private boolean compareDeclaringClass(IMethodBinding b1, IMethodBinding b2) {
		if( b1 == null || b2 == null )
			return b1 == b2;
		String b1ClassFqqn = b1 == null || b1.getDeclaringClass() == null ? null : b1.getDeclaringClass().getQualifiedName();
		String b2ClassFqqn = b2 == null || b2.getDeclaringClass() == null ? null : b2.getDeclaringClass().getQualifiedName();
		String b1TrimTypeParms = b1ClassFqqn.contains("<") ? b1ClassFqqn.substring(0,b1ClassFqqn.indexOf("<")) : b1ClassFqqn;
		String b2TrimTypeParms = b2ClassFqqn.contains("<") ? b2ClassFqqn.substring(0,b2ClassFqqn.indexOf("<")) : b2ClassFqqn;
		return b1TrimTypeParms.equals(b2TrimTypeParms);
	}

	protected int resolveLevelAsSubtype(char[] simplePattern,
			char[] qualifiedPattern, ITypeBinding type,
			String methodName, ITypeBinding[] argumentTypes,
			String packageName, boolean isDefault,
			IMethodBinding originalQuery) {

		// This binding might be null, because as we search up the heirarchy tree,
		// there might be intermediate classes or interfaces where the searched-for
		// method does not exist. This is not grounds for disqualification.
		IMethodBinding method = getDOMASTMethodBinding(type, methodName, argumentTypes);

		boolean methodIdenticalToOriginal = compareDeclaringClass(originalQuery, method);
		int r1 = resolveLevelAsSubtype_basic(simplePattern, qualifiedPattern, type, method, packageName, isDefault, methodIdenticalToOriginal);
		if( r1 != -1 )
			return r1;

		r1 = resolveLevelAsSubtype_super(simplePattern, qualifiedPattern, type, methodName, argumentTypes, method, packageName, isDefault, originalQuery);
		if( r1 != -1 )
			return r1;

		r1 = resolveLevelAsSubtype_interfaces(simplePattern, qualifiedPattern, type, methodName, argumentTypes, method, packageName, isDefault, originalQuery);
		if( r1 != -1 )
			return r1;

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

	/*
	 * Subclasses can override this if they want to make last minute changes to the match
	 */
	@Override
	public void reportSearchMatch(MatchLocator locator, ASTNode node, SearchMatch match) throws CoreException {
		if (!(node instanceof MethodDeclaration)
			&& !(node instanceof AnnotationTypeMemberDeclaration)
			&& DOMASTNodeUtils.getBinding(node) instanceof IMethodBinding methodBinding
			&& (methodBinding.isGenericMethod() || methodBinding.isParameterizedMethod())
			&& pattern.focus instanceof IMethod method
			&& !pattern.hasMethodArguments()) {
			int rule = match.getRule();
			rule &= ~SearchPattern.R_FULL_MATCH;
			rule |= SearchPattern.R_EQUIVALENT_MATCH | SearchPattern.R_ERASURE_MATCH;
			match.setRule(rule);
		}
		if (DOMASTNodeUtils.getBinding(node) instanceof IMethodBinding methodBinding
			&& (methodBinding.isRawMethod() || methodBinding.getDeclaringClass().isRawType())
			&& (pattern.hasMethodArguments() || pattern.hasTypeArguments())) {
			int rule = match.getRule();
			rule &= ~SearchPattern.R_FULL_MATCH;
			rule |= SearchPattern.R_EQUIVALENT_MATCH | SearchPattern.R_ERASURE_MATCH;
			match.setRule(rule);
		}
		if( preferParamaterizedNode() ) {
			if( node instanceof MethodInvocation iv) {
				List<?> l = iv.typeArguments();
				if( l != null && l.size() > 0 ) {
					int start = ((ASTNode)l.get(0)).getStartPosition();
					if( start > 0 ) {
						int newStart = start - 1;
						int currOffset = match.getOffset();
						if( newStart < currOffset) {
							int diff = currOffset - newStart;
							match.setOffset(newStart);
							match.setLength(match.getLength() + diff);
						}
					}
				}
			}
		}
		SearchMatchingUtility.reportSearchMatch(locator, match);
	}


	private boolean preferParamaterizedNode() {
		int patternRule = this.locator.pattern.getMatchRule();
		boolean patternIsErasureMatch = isPatternErasureMatch();
		boolean patternIsEquivMatch = isPatternEquivalentMatch();
		boolean patternIsExactMatch = isPatternExactMatch();

		boolean hasMethodArgs = this.locator.pattern.hasMethodArguments();
		boolean hasMethodParams = this.locator.pattern.hasMethodParameters();
		boolean emptyTypeArgsPattern = this.locator.pattern.methodArguments == null ||
				this.locator.pattern.methodArguments.length == 0;
		if( patternIsEquivMatch || patternIsExactMatch)
			return hasMethodArgs;
		if( patternIsErasureMatch) {
			return false;
		}
		return true;
	}
}
