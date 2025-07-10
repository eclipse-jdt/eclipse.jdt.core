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
import java.util.function.Predicate;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.core.Signature;
import org.eclipse.jdt.core.compiler.CharOperation;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.AbstractTypeDeclaration;
import org.eclipse.jdt.core.dom.ArrayType;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.FieldAccess;
import org.eclipse.jdt.core.dom.IBinding;
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.core.dom.ImportDeclaration;
import org.eclipse.jdt.core.dom.MethodInvocation;
import org.eclipse.jdt.core.dom.Name;
import org.eclipse.jdt.core.dom.ParameterizedType;
import org.eclipse.jdt.core.dom.PrimitiveType;
import org.eclipse.jdt.core.dom.QualifiedName;
import org.eclipse.jdt.core.dom.QualifiedType;
import org.eclipse.jdt.core.dom.SimpleName;
import org.eclipse.jdt.core.dom.SimpleType;
import org.eclipse.jdt.core.dom.Type;
import org.eclipse.jdt.core.dom.VariableDeclaration;
import org.eclipse.jdt.core.search.SearchMatch;
import org.eclipse.jdt.core.search.SearchPattern;
import org.eclipse.jdt.internal.compiler.ast.Wildcard;
import org.eclipse.jdt.internal.core.search.LocatorResponse;
import org.eclipse.jdt.internal.javac.dom.JavacTypeBinding;

public class DOMPatternLocator extends PatternLocator {
	protected ASTNode currentNode;

	public DOMPatternLocator(SearchPattern pattern) {
		super(pattern);
	}

	protected LocatorResponse toResponse(int val) {
		return toResponse(val, false);
	}
	protected LocatorResponse toResponse(int val, boolean alreadyAdded) {
		return new LocatorResponse(val, false, null, alreadyAdded, true);
	}


	// AST DOM Variants
	public LocatorResponse match(org.eclipse.jdt.core.dom.Annotation node, NodeSetWrapper nodeSet, MatchLocator locator) {
		// each subtype should override if needed
		return toResponse(PatternLocator.IMPOSSIBLE_MATCH);
	}
	/**
	 * Check if the given ast node syntactically matches this pattern.
	 * If it does, add it to the match set.
	 * Returns the match level.
	 */
	public LocatorResponse match(org.eclipse.jdt.core.dom.ASTNode node, NodeSetWrapper nodeSet, MatchLocator locator) { // needed for some generic nodes
		// each subtype should override if needed
		return toResponse(PatternLocator.IMPOSSIBLE_MATCH);
	}
	public LocatorResponse match(org.eclipse.jdt.core.dom.Expression node, NodeSetWrapper nodeSet, MatchLocator locator) {
		// each subtype should override if needed
		return toResponse(PatternLocator.IMPOSSIBLE_MATCH);
	}
	public LocatorResponse match(org.eclipse.jdt.core.dom.FieldDeclaration node, NodeSetWrapper nodeSet, MatchLocator locator) {
		// each subtype should override if needed
		return toResponse(PatternLocator.IMPOSSIBLE_MATCH);
	}
	public LocatorResponse match(org.eclipse.jdt.core.dom.LambdaExpression node, NodeSetWrapper nodeSet, MatchLocator locator) {
		// each subtype should override if needed
		return toResponse(PatternLocator.IMPOSSIBLE_MATCH);
	}
	public LocatorResponse match(org.eclipse.jdt.core.dom.MethodReference node, NodeSetWrapper nodeSet, MatchLocator locator) {
		// each subtype should override if needed
		return toResponse(PatternLocator.IMPOSSIBLE_MATCH);
	}
	public LocatorResponse match(org.eclipse.jdt.core.dom.MethodRef node, NodeSetWrapper nodeSet, MatchLocator locator) {
		// each subtype should override if needed
		return toResponse(PatternLocator.IMPOSSIBLE_MATCH);
	}
	public LocatorResponse match(VariableDeclaration node, NodeSetWrapper nodeSet, MatchLocator locator) {
		// each subtype should override if needed
		return toResponse(PatternLocator.IMPOSSIBLE_MATCH);
	}
	public LocatorResponse match(org.eclipse.jdt.core.dom.MethodDeclaration node, NodeSetWrapper nodeSet, MatchLocator locator) {
		// each subtype should override if needed
		return toResponse(PatternLocator.IMPOSSIBLE_MATCH);
	}
	public LocatorResponse match(org.eclipse.jdt.core.dom.MemberValuePair node, NodeSetWrapper nodeSet, MatchLocator locator) {
		// each subtype should override if needed
		return toResponse(PatternLocator.IMPOSSIBLE_MATCH);
	}
	public LocatorResponse match(org.eclipse.jdt.core.dom.AnnotationTypeMemberDeclaration node, NodeSetWrapper nodeSet, MatchLocator locator) {
		// each subtype should override if needed
		return toResponse(PatternLocator.IMPOSSIBLE_MATCH);
	}
	public LocatorResponse match(MethodInvocation node, NodeSetWrapper nodeSet, MatchLocator locator) {
		// each subtype should override if needed
		return toResponse(PatternLocator.IMPOSSIBLE_MATCH);
	}
	protected LocatorResponse match(org.eclipse.jdt.core.dom.ModuleDeclaration node, NodeSetWrapper nodeSet, MatchLocator locator) {
		return toResponse(PatternLocator.IMPOSSIBLE_MATCH);
	}
	public LocatorResponse match(Name node, NodeSetWrapper nodeSet, MatchLocator locator) {
		// each subtype should override if needed
		return toResponse(PatternLocator.IMPOSSIBLE_MATCH);
	}
	public LocatorResponse match(FieldAccess node, NodeSetWrapper nodeSet, MatchLocator locator) {
		// each subtype should override if needed
		return toResponse(PatternLocator.IMPOSSIBLE_MATCH);
	}
	public LocatorResponse match(AbstractTypeDeclaration node, NodeSetWrapper nodeSet, MatchLocator locator) {
		// each subtype should override if needed
		return toResponse(PatternLocator.IMPOSSIBLE_MATCH);
	}
	public LocatorResponse match(org.eclipse.jdt.core.dom.TypeParameter node, NodeSetWrapper nodeSet, MatchLocator locator) {
		// each subtype should override if needed
		return toResponse(PatternLocator.IMPOSSIBLE_MATCH);
	}
	public LocatorResponse match(Type node, NodeSetWrapper nodeSet, MatchLocator locator) {
		// each subtype should override if needed
		return toResponse(PatternLocator.IMPOSSIBLE_MATCH);
	}
	public LocatorResponse resolveLevel(org.eclipse.jdt.core.dom.ASTNode node, MatchLocator locator) {
		// each subtype should override if needed
		return toResponse(PatternLocator.IMPOSSIBLE_MATCH);
	}
	public LocatorResponse resolveLevel(org.eclipse.jdt.core.dom.ASTNode node, IBinding binding, MatchLocator locator) {
		// each subtype should override if needed
		return toResponse(PatternLocator.IMPOSSIBLE_MATCH);
	}
	protected String getQualifiedSourceName(ITypeBinding binding) {
		if (binding == null) {
			return null;
		}
		ITypeBinding type = binding.isArray() ? binding.getComponentType() : binding;
		String simpleName = type instanceof JavacTypeBinding ? ((JavacTypeBinding)type).getName(false) : binding.getName();
		String qualifier = qualifiedSourceName(type.getDeclaringClass());
		if( qualifier == null && type instanceof JavacTypeBinding jctb) {
			String qualifiedName = jctb.getQualifiedName(false);
			if( qualifiedName != null ) {
				return qualifiedName;
			}
		}
		if (type.isLocal()) {
			return qualifier + ".1." + simpleName; //$NON-NLS-1$
		} else if (type.isMember()) {
			return qualifier + '.' + simpleName;
		}
		return binding.getName();
	}
	protected int resolveLevelForType(char[] simpleNamePattern, char[] qualificationPattern, ITypeBinding binding) {
		return
			binding == null && simpleNamePattern == null && qualificationPattern == null ? ACCURATE_MATCH :
			binding != null && binding.isArray() && new String(simpleNamePattern).endsWith("[]") ? resolveLevelForType(Arrays.copyOf(simpleNamePattern, simpleNamePattern.length - 2), qualificationPattern, binding.getComponentType()) :
			resolveLevelForTypeFQN(simpleNamePattern, qualificationPattern, binding, null);
	}

	protected int resolveLevelForTypeFQN(char[] simpleNamePattern, char[] qualificationPattern, ITypeBinding binding, IImportDiscovery discovery) {
		int level = 0;
		if (simpleNamePattern == null) {
			return ACCURATE_MATCH;
		}
		if (qualificationPattern == null && simpleNamePattern != null) {
			level = resolveLevelForTypeSourceName(simpleNamePattern, (binding.isArray() ? binding : binding.getErasure()).getName().toCharArray(), binding);
		}
		if (level == ACCURATE_MATCH || level == ERASURE_MATCH) {
			return level;
		}
		char[] qualifiedPattern = getQualifiedPattern(simpleNamePattern, qualificationPattern);
		level = resolveLevelForTypeFQN(qualifiedPattern, binding, discovery);
		if (level == ACCURATE_MATCH || binding == null)
			return level;

		ITypeBinding type = binding.isArray() ? binding.getComponentType() : binding;
		char[] sourceName = null;
		if (type.isMember() || type.isLocal()) {
			if (qualificationPattern != null) {
				sourceName = getQualifiedSourceName(binding).toCharArray();
			} else {
				sourceName = binding.getQualifiedName().toCharArray();
			}
		} else if (qualificationPattern == null) {
			sourceName = getQualifiedSourceName(binding).toCharArray();
		}
		if (type.isRecovered()) {
			if (qualificationPattern == null || !type.getQualifiedName().contains(".") ) {
				level = resolveLevelForTypeSourceName(simpleNamePattern, binding.getName().toCharArray(), type);
				if (level > IMPOSSIBLE_MATCH) {
					return INACCURATE_MATCH;
				}
			}
		}
		if (sourceName == null)
			return IMPOSSIBLE_MATCH;
		return resolveLevelForTypeSourceName(qualifiedPattern, sourceName, type);
	}
	public static String qualifiedSourceName(ITypeBinding binding) {
		if (binding == null) {
			return null;
		}
		if (binding.isLocal()) {
			return binding.isMember()
				? qualifiedSourceName(binding.getDeclaringClass()) + '.' +  binding.getName()
				: qualifiedSourceName(binding.getDeclaringClass()) + ".1." + binding.getName(); //$NON-NLS-1$
		}
		return binding.getQualifiedName();
	}
	private Name getBaseTypeName(Type type) {
		if( type instanceof SimpleType simp) {
			return simp.getName();
		}
		if( type instanceof QualifiedType qn) {
			return qn.getName();
		}
		if( type instanceof ArrayType arr) {
			return getBaseTypeName(arr.getElementType());
		}
		if (type instanceof ParameterizedType parameterized) {
			return getBaseTypeName(parameterized.getType());
		}

		return null;
	}

	protected boolean matchesTypeReference(char[] pattern, Type type, boolean isVarargs) {
		if (pattern == null) return true; // null is as if it was "*"
		if (type == null) return true; // treat as an inexact match

		var name = getBaseTypeName(type);
		var simpleName = name instanceof SimpleName simple ? simple.getIdentifier() :
			name instanceof QualifiedName qName ? qName.getName().getIdentifier() :
			type instanceof PrimitiveType primitive ? primitive.getPrimitiveTypeCode().toString() :
			null;
		if (simpleName == null) {
			return true;
		}
		int dimensions = type instanceof ArrayType arrayType ? arrayType.dimensions().size() : 0;
		if (isVarargs) {
			dimensions++;
		}
		for (int i = 0; i < dimensions; i++) {
			simpleName += "[]"; //$NON-NLS-1$
		}
		return matchesName(pattern, simpleName.toCharArray());
	}
	protected boolean matchesTypeReference(char[] pattern, Type type) {
		return matchesTypeReference(pattern, type, false);
	}
	protected int resolveLevelForTypeSourceName(char[] qualifiedPattern, char[] sourceName, ITypeBinding type) {
		switch (this.matchMode) {
			case SearchPattern.R_PREFIX_MATCH:
				if (CharOperation.prefixEquals(qualifiedPattern, sourceName, this.isCaseSensitive)) {
					return ACCURATE_MATCH;
				}
				break;
			case SearchPattern.R_CAMELCASE_MATCH:
				if ((qualifiedPattern.length>0 && sourceName.length>0 && qualifiedPattern[0] == sourceName[0])) {
					if (CharOperation.camelCaseMatch(qualifiedPattern, sourceName, false)) {
						return ACCURATE_MATCH;
					}
					if (!this.isCaseSensitive && CharOperation.prefixEquals(qualifiedPattern, sourceName, false)) {
						return ACCURATE_MATCH;
					}
				}
				break;
			case SearchPattern.R_CAMELCASE_SAME_PART_COUNT_MATCH:
				if ((qualifiedPattern.length>0 && sourceName.length>0 && qualifiedPattern[0] == sourceName[0])) {
					if (CharOperation.camelCaseMatch(qualifiedPattern, sourceName, true)) {
						return ACCURATE_MATCH;
					}
				}
				break;
			default:
				if( type != null && type.isLocal() ) {
					if (CharOperation.prefixEquals(qualifiedPattern, sourceName, this.isCaseSensitive)) {
						return ACCURATE_MATCH;
					}
				}
				if (CharOperation.match(qualifiedPattern, sourceName, this.isCaseSensitive)) {
					return ACCURATE_MATCH;
				}
		}
		return IMPOSSIBLE_MATCH;
	}

	public static interface IImportDiscovery {
		public String findImportForString(String s);
	}

	protected int resolveLevelForTypeFQN(char[] qualifiedPattern, ITypeBinding type) {
		return resolveLevelForTypeFQN(qualifiedPattern, type, null);
	}
	protected int resolveLevelForTypeFQN(char[] qualifiedPattern, ITypeBinding type, IImportDiscovery discovery) {

		if (qualifiedPattern == null) return ACCURATE_MATCH;
		if (type == null) return INACCURATE_MATCH;

		// Type variable cannot be specified through pattern => this kind of binding cannot match it (see bug 79803)
		if (type.isTypeVariable()) return IMPOSSIBLE_MATCH;

		if (type.isIntersectionType()) {
			int result = IMPOSSIBLE_MATCH, prev = IMPOSSIBLE_MATCH;
			for (ITypeBinding ref : type.getTypeBounds()) {
				result = resolveLevelForTypeFQN(qualifiedPattern, ref);
				if (result == ACCURATE_MATCH) return result;
				if (result == IMPOSSIBLE_MATCH) continue;
				if (prev == IMPOSSIBLE_MATCH) prev = result;
			}
			return prev;
		}
		// NOTE: if case insensitive search then qualifiedPattern is assumed to be lowercase
		String qnfb = getQualifiedSourceName(type);
		int qnfbLastDot = qnfb.lastIndexOf('.');
		int patternFirstDot = CharOperation.indexOf('.', qualifiedPattern);
		if( qnfbLastDot != -1 && patternFirstDot == -1) {
			// qnfb is actually qualified but the char array is not.
			String qnfbLastSegment = qnfbLastDot == qnfb.length() - 1 ? null : qnfb.substring(qnfbLastDot+1);
			if( qnfbLastSegment != null ) {
				boolean match1 = CharOperation.match(qualifiedPattern, qnfbLastSegment.toCharArray(), this.isCaseSensitive);
				return match1 ? ACCURATE_MATCH : IMPOSSIBLE_MATCH;
			}
		}
		char[] qualifiedNameFromBinding = qnfb == null ? null : qnfb.toCharArray();
		if( qualifiedNameFromBinding == null || qualifiedNameFromBinding.length == 0 ) {
			qualifiedNameFromBinding = type.getName().toCharArray();
		}
		boolean match1 = CharOperation.match(qualifiedPattern, qualifiedNameFromBinding, this.isCaseSensitive);
		if( match1 ) {
			return ACCURATE_MATCH;
		}

		// There's a chance our "qualified name" is not fully qualified.
		if( patternFirstDot != -1 && discovery != null ) {
			String firstSegment = new String(qualifiedPattern, 0, patternFirstDot);
			String fqqnImport = discovery.findImportForString(firstSegment);
			if( fqqnImport != null ) {
				String fqqnPattern = fqqnImport.substring(0, fqqnImport.length() - firstSegment.length()) + new String(qualifiedPattern);
				boolean match2 = CharOperation.match(fqqnPattern.toCharArray(), qualifiedNameFromBinding, this.isCaseSensitive);
				if( match2 ) {
					return ACCURATE_MATCH;
				}
			}
		}
		return IMPOSSIBLE_MATCH;
	}

	private int boundKind(ITypeBinding type) {
		if (type.isUpperbound()) {
			return Wildcard.EXTENDS;
		}
		if (type.getBound() == null ) {
			return Wildcard.UNBOUND;
		}
		return Wildcard.SUPER;
	}

	/*
	 * Update pattern locator match comparing type arguments with pattern ones.
	 * Try to resolve pattern and look for compatibility with type arguments
	 * to set match rule.
	 */
	protected void updateMatch(ITypeBinding[] argumentsBinding, char[][] patternArguments, boolean hasTypeParameters) {
		// First compare lengthes
		int patternTypeArgsLength = patternArguments==null ? 0 : patternArguments.length;
		int typeArgumentsLength = argumentsBinding == null ? 0 : argumentsBinding.length;

		// Initialize match rule
		int matchRule = this.match.getRule();
		if (this.match.isRaw()) {
			if (patternTypeArgsLength != 0) {
				matchRule &= ~SearchPattern.R_FULL_MATCH;
			}
		}
		if (hasTypeParameters) {
			matchRule = SearchPattern.R_ERASURE_MATCH;
		}

		// Compare arguments lengthes
		if (patternTypeArgsLength == typeArgumentsLength) {
			if (!this.match.isRaw() && hasTypeParameters) {
				// generic patterns are always not compatible match
				this.match.setRule(SearchPattern.R_ERASURE_MATCH);
				return;
			}
		} else {
			if (patternTypeArgsLength==0) {
				if (!this.match.isRaw() || hasTypeParameters) {
					this.match.setRule(matchRule & ~SearchPattern.R_FULL_MATCH);
				}
			} else  if (typeArgumentsLength==0) {
				// raw binding is always compatible
				this.match.setRule(matchRule & ~SearchPattern.R_FULL_MATCH);
			} else {
				this.match.setRule(0); // impossible match
			}
			return;
		}
		if (argumentsBinding == null || patternArguments == null) {
			this.match.setRule(matchRule);
			return;
		}

		// Compare binding for each type argument only if pattern is not erasure only and at first level
		if (!hasTypeParameters && !this.match.isRaw() && (this.match.isEquivalent() || this.match.isExact())) {
			for (int i=0; i<typeArgumentsLength; i++) {
				// Get parameterized type argument binding
				ITypeBinding argumentBinding = argumentsBinding[i];
				if (argumentBinding.isCapture()) {
					ITypeBinding capturedWildcard = argumentBinding.getWildcard();
					if (capturedWildcard != null) argumentBinding = capturedWildcard;
				}
				// Get binding for pattern argument
				char[] patternTypeArgument = patternArguments[i];
				char patternWildcard = patternTypeArgument[0];
				char[] patternTypeName = patternTypeArgument;
				int patternWildcardKind = -1;
				switch (patternWildcard) {
					case Signature.C_STAR:
						if (argumentBinding.isWildcardType()) {
							if (boundKind(argumentBinding) == Wildcard.UNBOUND) continue;
						}
						matchRule &= ~SearchPattern.R_FULL_MATCH;
						continue; // unbound parameter always match
					case Signature.C_EXTENDS :
						patternWildcardKind = Wildcard.EXTENDS;
						patternTypeName = CharOperation.subarray(patternTypeArgument, 1, patternTypeArgument.length);
						break;
					case Signature.C_SUPER :
						patternWildcardKind = Wildcard.SUPER;
						patternTypeName = CharOperation.subarray(patternTypeArgument, 1, patternTypeArgument.length);
						break;
					default :
						break;
				}
				patternTypeName = Signature.toCharArray(patternTypeName);
				ITypeBinding patternBinding = findType(new String(patternTypeName));//locator.getType(patternTypeArgument, patternTypeName);

				// If have no binding for pattern arg, then we won't be able to refine accuracy
				if (patternBinding == null) {
					if (argumentBinding.isWildcardType()) {
						if (boundKind(argumentBinding) == Wildcard.UNBOUND) {
							matchRule &= ~SearchPattern.R_FULL_MATCH;
						} else {
							this.match.setRule(SearchPattern.R_ERASURE_MATCH);
							return;
						}
					}
					continue;
				}

				// Verify the pattern binding is compatible with match type argument binding
				switch (patternWildcard) {
					case Signature.C_STAR : // UNBOUND pattern
						// unbound always match => skip to next argument
						matchRule &= ~SearchPattern.R_FULL_MATCH;
						continue;
					case Signature.C_EXTENDS : // EXTENDS pattern
						if (argumentBinding.isWildcardType()) { // argument is a wildcard
							// It's ok if wildcards are identical
							if (boundKind(argumentBinding) == patternWildcardKind && argumentBinding.getBound().isEqualTo(patternBinding)) {
								continue;
							}
							// Look for wildcard compatibility
							switch (boundKind(argumentBinding)) {
								case Wildcard.EXTENDS:
									if (argumentBinding.getBound() == null || argumentBinding.getBound().isAssignmentCompatible(patternBinding)) {
										// valid when arg extends a subclass of pattern
										matchRule &= ~SearchPattern.R_FULL_MATCH;
										continue;
									}
									break;
								case Wildcard.SUPER:
									break;
								case Wildcard.UNBOUND:
									matchRule &= ~SearchPattern.R_FULL_MATCH;
									continue;
							}
						} else if (argumentBinding.isAssignmentCompatible(patternBinding)) {
							// valid when arg is a subclass of pattern
							matchRule &= ~SearchPattern.R_FULL_MATCH;
							continue;
						}
						break;
					case Signature.C_SUPER : // SUPER pattern
						if (argumentBinding.isWildcardType()) { // argument is a wildcard
							// It's ok if wildcards are identical
							if (boundKind(argumentBinding) == patternWildcardKind && argumentBinding.getBound().isEqualTo(patternBinding)) {
								continue;
							}
							// Look for wildcard compatibility
							switch (boundKind(argumentBinding)) {
								case Wildcard.EXTENDS:
									break;
								case Wildcard.SUPER:
									if (argumentBinding.getBound() == null || patternBinding.isAssignmentCompatible(argumentBinding.getBound())) {
										// valid only when arg super a superclass of pattern
										matchRule &= ~SearchPattern.R_FULL_MATCH;
										continue;
									}
									break;
								case Wildcard.UNBOUND:
									matchRule &= ~SearchPattern.R_FULL_MATCH;
									continue;
							}
						} else if (patternBinding.isAssignmentCompatible(argumentBinding)) {
							// valid only when arg is a superclass of pattern
							matchRule &= ~SearchPattern.R_FULL_MATCH;
							continue;
						}
						break;
					default:
						if (argumentBinding.isWildcardType()) {
							switch (boundKind(argumentBinding)) {
								case Wildcard.EXTENDS:
									if (argumentBinding.getBound() == null || patternBinding.isAssignmentCompatible(argumentBinding.getBound())) {
										// valid only when arg extends a superclass of pattern
										matchRule &= ~SearchPattern.R_FULL_MATCH;
										continue;
									}
									break;
								case Wildcard.SUPER:
									if (argumentBinding.getBound() == null || argumentBinding.getBound().isAssignmentCompatible(patternBinding)) {
										// valid only when arg super a subclass of pattern
										matchRule &= ~SearchPattern.R_FULL_MATCH;
										continue;
									}
									break;
								case Wildcard.UNBOUND:
									matchRule &= ~SearchPattern.R_FULL_MATCH;
									continue;
							}
						} else if (argumentBinding.isEqualTo(patternBinding))
							// valid only when arg is equals to pattern
							continue;
						break;
				}

				// Argument does not match => erasure match will be the only possible one
				this.match.setRule(SearchPattern.R_ERASURE_MATCH);
				return;
			}
		}

		// Set match rule
		this.match.setRule(matchRule);
	}

	protected void updateMatch(ITypeBinding parameterizedBinding, char[][][] patternTypeArguments, boolean patternHasTypeParameters, int depth) {
		// Set match raw flag
		boolean endPattern = patternTypeArguments==null  ? true  : depth>=patternTypeArguments.length;
		ITypeBinding[] argumentsBindings = parameterizedBinding.getTypeArguments();
		boolean isRaw = parameterizedBinding.isRawType()|| (argumentsBindings==null && parameterizedBinding.getTypeDeclaration().isGenericType());
		if (isRaw && !this.match.isRaw()) {
			this.match.setRaw(isRaw);
		}

		// Update match
		if (!endPattern && patternTypeArguments != null) {
			// verify if this is a reference to the generic type itself
			if (!isRaw && patternHasTypeParameters && argumentsBindings != null) {
				boolean needUpdate = false;
				ITypeBinding[] typeVariables = parameterizedBinding.getTypeDeclaration().getTypeParameters();
				int length = argumentsBindings.length;
				if (length == typeVariables.length) {
					for (int i=0; i<length; i++) {
						if (!argumentsBindings[i].isEqualTo(typeVariables[i])) {
							needUpdate = true;
							break;
						}
					}
				}
				if (needUpdate) {
					char[][] patternArguments =  patternTypeArguments[depth];
					updateMatch(argumentsBindings, patternArguments, patternHasTypeParameters);
				}
			} else {
				char[][] patternArguments =  patternTypeArguments[depth];
				updateMatch(argumentsBindings, patternArguments, patternHasTypeParameters);
			}
		}

		// Recurse
		ITypeBinding enclosingType = parameterizedBinding.getDeclaringClass();
		if (enclosingType != null && (enclosingType.isParameterizedType() || enclosingType.isRawType())) {
			updateMatch(enclosingType, patternTypeArguments, patternHasTypeParameters, depth+1);
		}
	}

	/*
	 * Subclasses can override this if they want to make last minute changes to the match
	 */
	public void reportSearchMatch(MatchLocator locator, ASTNode node, SearchMatch match) throws CoreException {
		this.match = match;
		SearchMatchingUtility.reportSearchMatch(locator, match);
	}

	public final void setCurrentMatch(SearchMatch match) {
		this.match = match;
	}

	public void setCurrentNode(ASTNode node) {
		this.currentNode = node;
	}

	private ITypeBinding findType(String name) {
		if (this.currentNode == null) {
			return null;
		}
		ITypeBinding res = this.currentNode.getAST().resolveWellKnownType(name);
		if (res != null) {
			return res;
		}
		ASTNode cursor = this.currentNode;
		while (cursor != null) {
			if (cursor instanceof CompilationUnit unit) {
				var explicitlyImported = ((List<ImportDeclaration>)unit.imports())
					.stream()
					.filter(Predicate.not(ImportDeclaration::isStatic))
					.filter(Predicate.not(ImportDeclaration::isOnDemand))
					.filter(decl -> decl.getName().toString().endsWith('.' + name))
					.map(ImportDeclaration::getName)
					.map(Name::toString)
					.map(this.currentNode.getAST()::resolveWellKnownType)
					.filter(Objects::nonNull)
					.findFirst()
					.orElse(null);
				if (explicitlyImported != null) {
					return explicitlyImported;
				}
				var importedOnDemand = ((List<ImportDeclaration>)unit.imports())
					.stream()
					.filter(Predicate.not(ImportDeclaration::isStatic))
					.filter(ImportDeclaration::isOnDemand)
					.map(decl -> decl.getName().toString() + '.' + name)
					.map(this.currentNode.getAST()::resolveWellKnownType)
					.filter(Objects::nonNull)
					.findFirst()
					.orElse(null);
				if (importedOnDemand != null) {
					return importedOnDemand;
				}
			}
			cursor = cursor.getParent();
		}
		return this.currentNode.getAST().resolveWellKnownType("java.lang." + name);
	}
}
