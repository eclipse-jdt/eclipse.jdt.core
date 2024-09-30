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

import org.eclipse.jdt.core.compiler.CharOperation;
import org.eclipse.jdt.core.dom.AbstractTypeDeclaration;
import org.eclipse.jdt.core.dom.ArrayType;
import org.eclipse.jdt.core.dom.FieldAccess;
import org.eclipse.jdt.core.dom.IBinding;
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.core.dom.MethodInvocation;
import org.eclipse.jdt.core.dom.Name;
import org.eclipse.jdt.core.dom.PrimitiveType;
import org.eclipse.jdt.core.dom.QualifiedName;
import org.eclipse.jdt.core.dom.QualifiedType;
import org.eclipse.jdt.core.dom.SimpleName;
import org.eclipse.jdt.core.dom.SimpleType;
import org.eclipse.jdt.core.dom.Type;
import org.eclipse.jdt.core.dom.VariableDeclaration;
import org.eclipse.jdt.core.search.SearchPattern;
import org.eclipse.jdt.internal.compiler.lookup.IntersectionTypeBinding18;
import org.eclipse.jdt.internal.compiler.lookup.ReferenceBinding;

public class DOMPatternLocator extends PatternLocator {
	public DOMPatternLocator(SearchPattern pattern) {
		super(pattern);
	}
	public static final int IMPOSSIBLE_MATCH = PatternLocator.IMPOSSIBLE_MATCH;
	public static final int INACCURATE_MATCH = PatternLocator.INACCURATE_MATCH;
	public static final int POSSIBLE_MATCH = PatternLocator.POSSIBLE_MATCH;
	public static final int ACCURATE_MATCH = PatternLocator.ACCURATE_MATCH;
	public static final int ERASURE_MATCH = PatternLocator.ERASURE_MATCH;


	// AST DOM Variants
	public int match(org.eclipse.jdt.core.dom.Annotation node, NodeSetWrapper nodeSet, MatchLocator locator) {
		// each subtype should override if needed
		return PatternLocator.IMPOSSIBLE_MATCH;
	}
	/**
	 * Check if the given ast node syntactically matches this pattern.
	 * If it does, add it to the match set.
	 * Returns the match level.
	 */
	public int match(org.eclipse.jdt.core.dom.ASTNode node, NodeSetWrapper nodeSet, MatchLocator locator) { // needed for some generic nodes
		// each subtype should override if needed
		return PatternLocator.IMPOSSIBLE_MATCH;
	}
	public int match(org.eclipse.jdt.core.dom.Expression node, NodeSetWrapper nodeSet, MatchLocator locator) {
		// each subtype should override if needed
		return PatternLocator.IMPOSSIBLE_MATCH;
	}
	public int match(org.eclipse.jdt.core.dom.FieldDeclaration node, NodeSetWrapper nodeSet, MatchLocator locator) {
		// each subtype should override if needed
		return PatternLocator.IMPOSSIBLE_MATCH;
	}
	public int match(org.eclipse.jdt.core.dom.LambdaExpression node, NodeSetWrapper nodeSet, MatchLocator locator) {
		// each subtype should override if needed
		return PatternLocator.IMPOSSIBLE_MATCH;
	}
	public int match(VariableDeclaration node, NodeSetWrapper nodeSet, MatchLocator locator) {
		// each subtype should override if needed
		return PatternLocator.IMPOSSIBLE_MATCH;
	}
	public int match(org.eclipse.jdt.core.dom.MethodDeclaration node, NodeSetWrapper nodeSet, MatchLocator locator) {
		// each subtype should override if needed
		return PatternLocator.IMPOSSIBLE_MATCH;
	}
	public int match(org.eclipse.jdt.core.dom.MemberValuePair node, NodeSetWrapper nodeSet, MatchLocator locator) {
		// each subtype should override if needed
		return PatternLocator.IMPOSSIBLE_MATCH;
	}
	public int match(MethodInvocation node, NodeSetWrapper nodeSet, MatchLocator locator) {
		// each subtype should override if needed
		return PatternLocator.IMPOSSIBLE_MATCH;
	}
	protected int match(org.eclipse.jdt.core.dom.ModuleDeclaration node, NodeSetWrapper nodeSet, MatchLocator locator) {
		return PatternLocator.IMPOSSIBLE_MATCH;
	}
	public int match(Name node, NodeSetWrapper nodeSet, MatchLocator locator) {
		// each subtype should override if needed
		return PatternLocator.IMPOSSIBLE_MATCH;
	}
	public int match(FieldAccess node, NodeSetWrapper nodeSet, MatchLocator locator) {
		// each subtype should override if needed
		return PatternLocator.IMPOSSIBLE_MATCH;
	}
	public int match(AbstractTypeDeclaration node, NodeSetWrapper nodeSet, MatchLocator locator) {
		// each subtype should override if needed
		return PatternLocator.IMPOSSIBLE_MATCH;
	}
	public int match(org.eclipse.jdt.core.dom.TypeParameter node, NodeSetWrapper nodeSet, MatchLocator locator) {
		// each subtype should override if needed
		return PatternLocator.IMPOSSIBLE_MATCH;
	}
	public int match(Type node, NodeSetWrapper nodeSet, MatchLocator locator) {
		// each subtype should override if needed
		return PatternLocator.IMPOSSIBLE_MATCH;
	}
	public int resolveLevel(org.eclipse.jdt.core.dom.ASTNode node, MatchLocator locator) {
		// each subtype should override if needed
		return PatternLocator.IMPOSSIBLE_MATCH;
	}
	public int resolveLevel(org.eclipse.jdt.core.dom.ASTNode node, IBinding binding, MatchLocator locator) {
		// each subtype should override if needed
		return PatternLocator.IMPOSSIBLE_MATCH;
	}
	protected String getQualifiedSourceName(ITypeBinding binding) {
		if (binding == null) {
			return null;
		}
		ITypeBinding type = binding.isArray() ? binding.getComponentType() : binding;
		if (type.isLocal()) {
			return qualifiedSourceName(type.getDeclaringClass()) + ".1." + binding.getName(); //$NON-NLS-1$
		} else if (type.isMember()) {
			return qualifiedSourceName(type.getDeclaringClass()) + '.' + binding.getName();
		}
		return binding.getName();
	}
	protected int resolveLevelForType(char[] simpleNamePattern, char[] qualificationPattern, ITypeBinding binding) {
//		return resolveLevelForType(qualifiedPattern(simpleNamePattern, qualificationPattern), type);
		char[] qualifiedPattern = getQualifiedPattern(simpleNamePattern, qualificationPattern);
		int level = resolveLevelForType(qualifiedPattern, binding);
		if (level == ACCURATE_MATCH || binding == null)
			return level;

		ITypeBinding type = binding.isArray() ? binding.getComponentType() : binding;
		char[] sourceName = null;
		if (type.isMember() || type.isLocal()) {
			if (qualificationPattern != null) {
				sourceName =  getQualifiedSourceName(binding).toCharArray();
			} else {
				sourceName =  binding.getQualifiedName().toCharArray();
			}
		} else if (qualificationPattern == null) {
			sourceName =  getQualifiedSourceName(binding).toCharArray();
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
	protected int resolveLevelForType(char[] qualifiedPattern, ITypeBinding type) {
		if (qualifiedPattern == null) return ACCURATE_MATCH;
		if (type == null) return INACCURATE_MATCH;

		// Type variable cannot be specified through pattern => this kind of binding cannot match it (see bug 79803)
		if (type.isTypeVariable()) return IMPOSSIBLE_MATCH;

		if (type instanceof IntersectionTypeBinding18) {
			int result = IMPOSSIBLE_MATCH, prev = IMPOSSIBLE_MATCH;
			IntersectionTypeBinding18 i18 = (IntersectionTypeBinding18) type;
			for (ReferenceBinding ref : i18.intersectingTypes) {
				result = resolveLevelForType(qualifiedPattern, ref);
				if (result == ACCURATE_MATCH) return result;
				if (result == IMPOSSIBLE_MATCH) continue;
				if (prev == IMPOSSIBLE_MATCH) prev = result;
			}
			return prev;
		}
		// NOTE: if case insensitive search then qualifiedPattern is assumed to be lowercase
		char[] qualifiedNameFromBinding = type.getQualifiedName().toCharArray();
		if( qualifiedNameFromBinding == null || qualifiedNameFromBinding.length == 0 ) {
			qualifiedNameFromBinding = type.getName().toCharArray();
		}
		boolean match1 = CharOperation.match(qualifiedPattern, qualifiedNameFromBinding, this.isCaseSensitive);
		return match1 ? ACCURATE_MATCH : IMPOSSIBLE_MATCH;
	}
}
