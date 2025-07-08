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

import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.AbstractTypeDeclaration;
import org.eclipse.jdt.core.dom.ClassInstanceCreation;
import org.eclipse.jdt.core.dom.ConstructorInvocation;
import org.eclipse.jdt.core.dom.CreationReference;
import org.eclipse.jdt.core.dom.EnumConstantDeclaration;
import org.eclipse.jdt.core.dom.EnumDeclaration;
import org.eclipse.jdt.core.dom.IBinding;
import org.eclipse.jdt.core.dom.IMethodBinding;
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.SuperConstructorInvocation;
import org.eclipse.jdt.core.dom.Type;
import org.eclipse.jdt.core.search.SearchMatch;
import org.eclipse.jdt.core.search.SearchPattern;
import org.eclipse.jdt.internal.codeassist.DOMCompletionUtils;
import org.eclipse.jdt.internal.compiler.ast.Argument;
import org.eclipse.jdt.internal.compiler.ast.ConstructorDeclaration;
import org.eclipse.jdt.internal.core.search.DOMASTNodeUtils;
import org.eclipse.jdt.internal.core.search.LocatorResponse;

public class DOMConstructorLocator extends DOMPatternLocator {

	private ConstructorLocator locator;

	public DOMConstructorLocator(ConstructorLocator cl) {
		super(cl.pattern);
		this.locator = cl;
	}

	@Override
	public LocatorResponse match(MethodDeclaration node, NodeSetWrapper nodeSet, MatchLocator locator) {
		if (!node.isConstructor()) {
			return toResponse(IMPOSSIBLE_MATCH);
		}
		if (this.locator.pattern.fineGrain != 0 && !this.locator.pattern.findDeclarations)
			return toResponse(IMPOSSIBLE_MATCH);
		int referencesLevel = this.locator.pattern.findReferences ? matchLevelForReference(node) : IMPOSSIBLE_MATCH;
		int declarationsLevel = this.locator.pattern.findDeclarations ? this.matchLevelForDeclarations(node) : IMPOSSIBLE_MATCH;

		// use the stronger match
		int level = nodeSet.addMatch(node, referencesLevel >= declarationsLevel ? referencesLevel : declarationsLevel);
		return toResponse(level, true);
	}

	protected int matchLevelForReference(MethodDeclaration constructor) {
		if ((this.locator.pattern.parameterSimpleNames == null || this.locator.pattern.parameterSimpleNames.length == 0) &&
			constructor.isConstructor() &&
			constructor.getBody() != null &&
			constructor.getBody().statements().stream().noneMatch(st -> st instanceof SuperConstructorInvocation || st instanceof ConstructorInvocation)) {
			return this.locator.pattern.mustResolve ? POSSIBLE_MATCH : ACCURATE_MATCH;
		}
		return IMPOSSIBLE_MATCH;
	}

	@Override
	public LocatorResponse match(org.eclipse.jdt.core.dom.Expression node, NodeSetWrapper nodeSet, MatchLocator locator) { // interested
		if (!this.locator.pattern.findReferences)
			return toResponse(IMPOSSIBLE_MATCH);
		if (node instanceof CreationReference creationRef && (this.locator.pattern.declaringSimpleName == null
				|| this.matchesTypeReference(this.locator.pattern.declaringSimpleName, creationRef.getType()))) {
			int level = this.locator.pattern.mustResolve ? POSSIBLE_MATCH : INACCURATE_MATCH;
			return toResponse(level);
		}
		if (node instanceof ClassInstanceCreation newInstance) {
			int level = (this.locator.pattern.declaringSimpleName == null
					|| this.matchesTypeReference(this.locator.pattern.declaringSimpleName, newInstance.getType()))
					&& matchParametersCount(node, newInstance.arguments()) ? POSSIBLE_MATCH : IMPOSSIBLE_MATCH;
			return toResponse(level);
		}
		return toResponse(IMPOSSIBLE_MATCH);
	}

	@Override
	public LocatorResponse match(org.eclipse.jdt.core.dom.ASTNode node, NodeSetWrapper nodeSet, MatchLocator locator) {
		if (!this.locator.pattern.findReferences)
			return toResponse(IMPOSSIBLE_MATCH);
		if (node instanceof ConstructorInvocation constructorInvocation) {
			if (!matchParametersCount(node, constructorInvocation.arguments())) {
				return toResponse(IMPOSSIBLE_MATCH);
			}
			if (this.locator.pattern.declaringSimpleName != null) {
				if (!this.matchesName(this.locator.pattern.declaringSimpleName, DOMCompletionUtils.findParentTypeDeclaration(node).getName().getIdentifier().toCharArray())) {
					return toResponse(IMPOSSIBLE_MATCH);
				}
			}
			int level = this.locator.pattern.mustResolve ? POSSIBLE_MATCH : INACCURATE_MATCH;
			return toResponse(level, true);
		} else if (node instanceof SuperConstructorInvocation superRef) {
			if (!matchParametersCount(node, superRef.arguments())) {
				return toResponse(IMPOSSIBLE_MATCH);
			}
			if (this.locator.pattern.declaringSimpleName != null) {
				Type superType = null;
				var current = superRef.getParent();
				while (current != null && !(current instanceof AbstractTypeDeclaration)
						&& !(current instanceof CreationReference)) {
					current = current.getParent();
				}
				if (current instanceof org.eclipse.jdt.core.dom.TypeDeclaration typeDecl) {
					superType = typeDecl.getSuperclassType();
				}
				if (current instanceof CreationReference newInstance) {
					superType = newInstance.getType();
				}
				if (!this.matchesTypeReference(this.locator.pattern.declaringSimpleName, superType)) {
					return toResponse(IMPOSSIBLE_MATCH);
				}
			}
			int level = this.locator.pattern.mustResolve ? POSSIBLE_MATCH : INACCURATE_MATCH;
			return toResponse(level, true);
		}
		if (node instanceof EnumConstantDeclaration enumConstantDecl
				&& node.getParent() instanceof EnumDeclaration enumDeclaration
				&& this.locator.matchesName(this.locator.pattern.declaringSimpleName,
						enumDeclaration.getName().getIdentifier().toCharArray())
				&& matchParametersCount(enumConstantDecl, enumConstantDecl.arguments())) {
			int level = nodeSet.addMatch(node, this.locator.pattern.mustResolve ? POSSIBLE_MATCH : ACCURATE_MATCH);
			return toResponse(level, true);
		}
		if (!this.locator.pattern.hasConstructorParameters()
			&& node instanceof CreationReference ref
			&& matchesTypeReference(this.locator.pattern.declaringSimpleName, ref.getType())) {
			return toResponse(POSSIBLE_MATCH);
		}
		return toResponse(IMPOSSIBLE_MATCH);
	}

	@Override
	public LocatorResponse resolveLevel(org.eclipse.jdt.core.dom.ASTNode node, IBinding binding, MatchLocator locator) {
		if (node instanceof MethodDeclaration decl) {
			if (this.locator.pattern.findReferences) {
				if (matchLevelForReference(decl) > IMPOSSIBLE_MATCH) {
					if (binding instanceof IMethodBinding currentConstructor) {
						var superType = currentConstructor.getDeclaringClass().getSuperclass();
						if (superType != null) {
							int superConstructorLevel = Arrays.stream(superType.getDeclaredMethods())
								.filter(IMethodBinding::isConstructor)
								.filter(superConstructor -> superConstructor.getParameterTypes().length == 0)
								.mapToInt(superConstructor -> {
									int level = matchConstructor(superConstructor);
									if (level == IMPOSSIBLE_MATCH && superConstructor != superConstructor.getMethodDeclaration()) {
										level = matchConstructor(superConstructor.getMethodDeclaration());
									}
									return level;
								}).findAny()
								.orElse(IMPOSSIBLE_MATCH);
							if (superConstructorLevel > IMPOSSIBLE_MATCH) {
								return toResponse(superConstructorLevel);
							}
						}
					}
				}
			}
			if (!this.locator.pattern.findDeclarations) {
				return toResponse(IMPOSSIBLE_MATCH);
			}
		}
		if (binding instanceof IMethodBinding constructor) {
			int level = matchConstructor(constructor);
			if (level == IMPOSSIBLE_MATCH) {
				if (constructor != constructor.getMethodDeclaration()) {
					level = matchConstructor(constructor.getMethodDeclaration());
				}
			}
			return toResponse(level);
		}
		return toResponse(IMPOSSIBLE_MATCH);
	}

	boolean matchParametersCount(org.eclipse.jdt.core.dom.ASTNode node,
			List<org.eclipse.jdt.core.dom.Expression> args) {
		if (this.locator.pattern.parameterSimpleNames != null
				&& (!this.locator.pattern.varargs || DOMASTNodeUtils.insideDocComment(node))) {
			int length = this.locator.pattern.parameterCount;
			if (length < 0)
				length = this.locator.pattern.parameterSimpleNames.length;
			int argsLength = args == null ? 0 : args.size();
			if (length != argsLength) {
				return false;
			}
		}
		return true;
	}
	protected int matchLevelForDeclarations(ConstructorDeclaration constructor) {
		// constructor name is stored in selector field
		if (this.locator.pattern.declaringSimpleName != null && !this.locator.matchesName(this.locator.pattern.declaringSimpleName, constructor.selector))
			return IMPOSSIBLE_MATCH;

		if (this.locator.pattern.parameterSimpleNames != null) {
			int length = this.locator.pattern.parameterSimpleNames.length;
			Argument[] args = constructor.arguments;
			int argsLength = args == null ? 0 : args.length;
			if (length != argsLength) return IMPOSSIBLE_MATCH;
		}

		// Verify type arguments (do not reject if pattern has no argument as it can be an erasure match)
		if (this.locator.pattern.hasConstructorArguments()) {
			if (constructor.typeParameters == null || constructor.typeParameters.length != this.locator.pattern.constructorArguments.length) return IMPOSSIBLE_MATCH;
		}

		return this.locator.pattern.mustResolve ? POSSIBLE_MATCH : ACCURATE_MATCH;
	}
	protected int matchConstructor(IMethodBinding constructor) {
		if (!constructor.isConstructor()) return IMPOSSIBLE_MATCH;

		// declaring type, simple name has already been matched by matchIndexEntry()
		int level = this.resolveLevelForType(this.locator.pattern.declaringSimpleName, this.locator.pattern.declaringQualification, constructor.getDeclaringClass());
		if (level == IMPOSSIBLE_MATCH) return IMPOSSIBLE_MATCH;

		// parameter types
		int parameterCount = this.locator.pattern.parameterCount;
		if (parameterCount > -1) {
			if (parameterCount != constructor.getParameterTypes().length) return IMPOSSIBLE_MATCH;
			for (int i = 0; i < parameterCount; i++) {
				// TODO (frederic) use this call to refine accuracy on parameter types
//				int newLevel = resolveLevelForType(this.pattern.parameterSimpleNames[i], this.pattern.parameterQualifications[i], this.pattern.parametersTypeArguments[i], 0, constructor.parameters[i]);
				int newLevel = this.resolveLevelForType(this.locator.pattern.parameterSimpleNames[i], this.locator.pattern.parameterQualifications[i], constructor.getParameterTypes()[i]);
				if (level > newLevel) {
					if (newLevel == IMPOSSIBLE_MATCH) {
//						if (isErasureMatch) {
//							return ERASURE_MATCH;
//						}
						return IMPOSSIBLE_MATCH;
					}
					level = newLevel; // can only be downgraded
				}
			}
		}
		return level;
	}

	protected int matchLevelForDeclarations(MethodDeclaration constructor) {
		// constructor name is stored in selector field
		if (this.locator.pattern.declaringSimpleName != null && !this.locator.matchesName(this.locator.pattern.declaringSimpleName, constructor.getName().toString().toCharArray()))
			return IMPOSSIBLE_MATCH;

		if (this.locator.pattern.parameterSimpleNames != null) {
			int length = this.locator.pattern.parameterSimpleNames.length;
			var args = constructor.parameters();
			int argsLength = args == null ? 0 : args.size();
			if (length != argsLength) return IMPOSSIBLE_MATCH;
		}

		// Verify type arguments (do not reject if pattern has no argument as it can be an erasure match)
		if (this.locator.pattern.hasConstructorArguments()) {
			if (constructor.typeParameters() == null || constructor.typeParameters().size() != this.locator.pattern.constructorArguments.length) return IMPOSSIBLE_MATCH;
		}

		return this.locator.pattern.mustResolve ? POSSIBLE_MATCH : ACCURATE_MATCH;
	}

	private boolean isParameterizedWithOwnVariables(ITypeBinding type) {
		// substiture for org.eclipse.jdt.compiler.lookup.TypeBindingisParameterizedWithOwnVariables
		return false;
	}

	@Override
	public void reportSearchMatch(MatchLocator locator, ASTNode node, SearchMatch match) throws CoreException {
		if (! (node instanceof MethodDeclaration || node instanceof EnumConstantDeclaration)) {
			IMethodBinding constructorBinding = DOMASTNodeUtils.getBinding(node) instanceof IMethodBinding binding
				&& binding.isConstructor() ? binding : null;
			if (constructorBinding == null) {
				return;
			}
			match.setRaw(constructorBinding.isRawMethod() || constructorBinding.getDeclaringClass().isRawType());
			if (constructorBinding.getTypeArguments().length > 0 || constructorBinding.isRawMethod()) { // parameterized generic method
				updateMatch(constructorBinding.getTypeArguments(), this.locator.pattern.constructorArguments, this.locator.pattern.hasConstructorParameters());

				// Update match regarding declaring class type arguments
				if (constructorBinding.getDeclaringClass().isParameterizedType() || constructorBinding.getDeclaringClass().isRawType()) {
					ITypeBinding parameterizedBinding = constructorBinding.getDeclaringClass();
					if (!this.locator.pattern.hasTypeArguments() && this.locator.pattern.hasConstructorArguments() || isParameterizedWithOwnVariables(parameterizedBinding)) {
						// special case for constructor pattern which defines arguments but no type
						// in this case, we only use refined accuracy for constructor
					} else if (this.locator.pattern.hasTypeArguments() && !this.locator.pattern.hasConstructorArguments()) {
						// special case for constructor pattern which defines no constructor arguments but has type ones
						// in this case, we do not use refined accuracy
						updateMatch(parameterizedBinding, this.locator.pattern.getTypeArguments(), this.locator.pattern.hasTypeParameters(), 0);
					} else {
						updateMatch(parameterizedBinding, this.locator.pattern.getTypeArguments(), this.locator.pattern.hasTypeParameters(), 0);
					}
				} else if (this.locator.pattern.hasTypeArguments()) {
					this.match.setRule(SearchPattern.R_ERASURE_MATCH);
				}

				// Update match regarding constructor parameters
				// TODO ? (frederic)
			} else if (constructorBinding.getDeclaringClass().isParameterizedType() || constructorBinding.getDeclaringClass().isRawType()) {
				ITypeBinding parameterizedBinding = constructorBinding.getDeclaringClass();
				if (!this.locator.pattern.hasTypeArguments() && this.locator.pattern.hasConstructorArguments()) {
					// special case for constructor pattern which defines arguments but no type
					updateMatch(parameterizedBinding, new char[][][] {this.locator.pattern.constructorArguments}, this.locator.pattern.hasTypeParameters(), 0);
				} else {
					updateMatch(parameterizedBinding, this.locator.pattern.getTypeArguments(), this.locator.pattern.hasTypeParameters(), 0);
				}
			} else if (this.locator.pattern.hasTypeArguments() || this.locator.pattern.hasConstructorArguments()) { // binding has no type params, compatible erasure if pattern does
				this.match.setRule(SearchPattern.R_ERASURE_MATCH);
			}

			// See whether it is necessary to report or not
			if (this.match.getRule() == 0) return; // impossible match
			boolean report = (this.isErasureMatch && this.match.isErasure()) || (this.isEquivalentMatch && this.match.isEquivalent()) || this.match.isExact();
			if (!report) return;

			// Report match
	//		int offset = reference.sourceStart;
	//		this.match.setOffset(offset);
	//		this.match.setLength(reference.sourceEnd - offset + 1);
	//		if (reference instanceof FieldDeclaration) { // enum declaration
	//			FieldDeclaration enumConstant  = reference;
	//			if (enumConstant.initialization instanceof QualifiedAllocationExpression) {
	//				locator.reportAccurateEnumConstructorReference(this.match, enumConstant, (QualifiedAllocationExpression) enumConstant.initialization);
	//				return;
	//			}
	//		}
		}

		super.reportSearchMatch(locator, node, match);
	}
}
