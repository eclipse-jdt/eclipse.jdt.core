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

import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.eclipse.jdt.core.compiler.CharOperation;
import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.Annotation;
import org.eclipse.jdt.core.dom.AnnotationTypeMemberDeclaration;
import org.eclipse.jdt.core.dom.IBinding;
import org.eclipse.jdt.core.dom.IMethodBinding;
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.core.dom.JdtCoreDomPackagePrivateUtility;
import org.eclipse.jdt.core.dom.MemberValuePair;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.MethodInvocation;
import org.eclipse.jdt.core.dom.Modifier;
import org.eclipse.jdt.core.dom.Name;
import org.eclipse.jdt.core.dom.SimpleName;
import org.eclipse.jdt.core.dom.SingleMemberAnnotation;
import org.eclipse.jdt.core.dom.SingleVariableDeclaration;
import org.eclipse.jdt.core.dom.SuperMethodInvocation;
import org.eclipse.jdt.core.search.SearchPattern;
import org.eclipse.jdt.internal.core.BinaryMethod;
import org.eclipse.jdt.internal.core.search.DOMASTNodeUtils;
import org.eclipse.jdt.internal.core.search.LocatorResponse;

public class DOMMethodLocator extends DOMPatternLocator {

	private MethodLocator locator;
	public DOMMethodLocator(MethodLocator locator) {
		super(locator.pattern);
		this.locator = locator;
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
					// Do not return as impossible when source level is at least 1.5
					if (this.locator.mayBeGeneric) {
						if (!this.locator.pattern.mustResolve) {
							// Set resolution flag on node set in case of types was inferred in parameterized types from generic ones...
						 	// (see  bugs https://bugs.eclipse.org/bugs/show_bug.cgi?id=79990, 96761, 96763)
							nodeSet.setMustResolve(true);
							resolve = true;
						}
				//		this.methodDeclarationsWithInvalidParam.put(node, null);
					} else {
						return toResponse(IMPOSSIBLE_MATCH);
					}
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
	public LocatorResponse match(org.eclipse.jdt.core.dom.Expression expression, NodeSetWrapper nodeSet, MatchLocator locator) {
		int level = expression instanceof SuperMethodInvocation node ? this.matchReference(node.getName(), node.arguments()) :
			IMPOSSIBLE_MATCH;
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

		int level = matchMethodBindingName(method);
		if (level == IMPOSSIBLE_MATCH)
			return level;

		level = resolveLevelForType(this.locator.pattern.returnSimpleName, this.locator.pattern.returnQualification, method.getReturnType());
		if (level == IMPOSSIBLE_MATCH)
			return level;


		level = matchMethodBindingParameters(node, method, skipImpossibleArg, level);
		if (level == IMPOSSIBLE_MATCH)
			return level;

		level = matchMethodBindingTypeArguments(node, method, skipImpossibleArg, level, bindingIsDeclaration);
		return level;
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

	private int matchMethodBindingTypeArguments(ASTNode node, IMethodBinding method,
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
						return INACCURATE_MATCH;
					}
					return ERASURE_MATCH;
				}
			}
		}

		return level;
	}

	protected int matchMethodBindingName(IMethodBinding binding) {
		int level = ACCURATE_MATCH;
		// look at return type only if declaring type is not specified
		if (this.locator.pattern.declaringSimpleName == null) {
			// TODO (frederic) use this call to refine accuracy on return type
			// int newLevel = resolveLevelForType(this.locator.pattern.returnSimpleName, this.locator.pattern.returnQualification, this.locator.pattern.returnTypeArguments, 0, method.returnType);
			int newLevel = resolveLevelForType(this.locator.pattern.returnSimpleName, this.locator.pattern.returnQualification,
					binding.getReturnType());
			if (level > newLevel) {
				if (newLevel == IMPOSSIBLE_MATCH)
					return IMPOSSIBLE_MATCH;
				level = newLevel; // can only be downgraded
			}
		}
		return level;
	}

	private int matchMethodBindingParameters(ASTNode node, IMethodBinding method, boolean skipImpossibleArg, int level) {
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
	@Override
	public LocatorResponse resolveLevel(org.eclipse.jdt.core.dom.ASTNode node, IBinding binding, MatchLocator locator) {
		boolean isExactPattern = isPatternExactMatch();
		boolean isErasurePattern = isPatternErasureMatch();
		boolean isEquivPattern = isPatternEquivalentMatch();

		if (node instanceof SingleMemberAnnotation singleMemberAnnotation) {
			var valuePairs = singleMemberAnnotation.resolveAnnotationBinding().getDeclaredMemberValuePairs();
			if (valuePairs != null && valuePairs.length > 0) {
				binding = valuePairs[0].getMethodBinding();
			}
		}
		if (binding instanceof IMethodBinding method) {
			boolean skipVerif = this.locator.pattern.findDeclarations && this.locator.mayBeGeneric;
			int methodLevel = matchMethod(node, method, skipVerif, false);
			if (methodLevel == IMPOSSIBLE_MATCH) {
				IMethodBinding decl = method.getMethodDeclaration();
				if (method != decl) {
					methodLevel = matchMethod(node, decl, skipVerif, true);
				}
				if (methodLevel == IMPOSSIBLE_MATCH) {
					return toResponse(IMPOSSIBLE_MATCH);
				} else {
					method = decl;
				}
			}

			if( node instanceof Name) {
				// We are a simple name and can't match complex pattern with type args
				boolean patternHasTypeArgs = this.locator.pattern.parameterSimpleNames != null && this.locator.pattern.parameterSimpleNames.length > 0
						&& this.locator.pattern.parameterSimpleNames[0] != null;
				if(patternHasTypeArgs) {
					return toResponse(IMPOSSIBLE_MATCH);
				}
			}

			// declaring type
			if (this.locator.pattern.declaringSimpleName == null && this.locator.pattern.declaringQualification == null)
				return toResponse(methodLevel); // since any declaring class will do

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
				if( isExactPattern || (!isErasurePattern && !isEquivPattern)) {
					return toResponse(IMPOSSIBLE_MATCH);
				} else if( isEquivPattern && matchLevel == ERASURE_MATCH) {
					return toResponse(IMPOSSIBLE_MATCH);
				}
			}

			return toResponse(weakerLevel);
		}
		 return toResponse(INACCURATE_MATCH);
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


}
