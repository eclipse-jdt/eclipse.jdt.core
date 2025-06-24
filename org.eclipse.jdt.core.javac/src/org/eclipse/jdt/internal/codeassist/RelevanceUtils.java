/*******************************************************************************
 * Copyright (c) 2025 Red Hat, Inc. and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *******************************************************************************/
package org.eclipse.jdt.internal.codeassist;

import java.util.Map;
import java.util.Objects;
import java.util.Set;

import org.eclipse.jdt.core.IAccessRule;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IPackageFragment;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.ITypeHierarchy;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.WorkingCopyOwner;
import org.eclipse.jdt.core.compiler.CharOperation;
import org.eclipse.jdt.core.dom.IBinding;
import org.eclipse.jdt.core.dom.IMethodBinding;
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.core.dom.IVariableBinding;
import org.eclipse.jdt.core.dom.PrimitiveType;
import org.eclipse.jdt.internal.codeassist.impl.AssistOptions;
import org.eclipse.jdt.internal.compiler.lookup.ReferenceBinding;

/**
 * Helper methods for calculating the relevance numbers of completion proposals.
 *
 * Most of these are adapted from {@link CompletionEngine}
 */
class RelevanceUtils {

	/**
	 * Returns the sum of the appropriate relevance constants based on how the
	 * current name matches the given proposal name according to the enabled rules.
	 *
	 * @param token        the uncompleted content
	 * @param proposalName the proposal text to check against the uncompleted
	 *                     content
	 * @param options      the assist options, which specifies which matching rules
	 *                     are enabled
	 * @see CompletionEngine#computeRelevanceForCaseMatching(char[], char[],
	 *      AssistOptions)
	 * @return the sum of the appropriate relevance constants
	 */
	public static int computeRelevanceForCaseMatching(char[] token, char[] proposalName, AssistOptions options) {
		if (CharOperation.equals(token, proposalName, true)) {
			return RelevanceConstants.R_EXACT_NAME + RelevanceConstants.R_CASE;
		} else if (CharOperation.equals(token, proposalName, false)) {
			return RelevanceConstants.R_EXACT_NAME;
		} else if (CharOperation.prefixEquals(token, proposalName, false)) {
			if (CharOperation.prefixEquals(token, proposalName, true))
				return RelevanceConstants.R_CASE;
		} else if (options.camelCaseMatch && CharOperation.camelCaseMatch(token, proposalName)) {
			return RelevanceConstants.R_CAMEL_CASE;
		} else if (options.substringMatch && CharOperation.substringMatch(token, proposalName)) {
			return RelevanceConstants.R_SUBSTRING;
		} else if (options.subwordMatch && CharOperation.subWordMatch(token, proposalName)) {
			return RelevanceConstants.R_SUBWORD;
		}
		return 0;
	}

	/**
	 * Returns the appropriate "qualified" relevance constant based on if the current content is qualified and if the content is expected to be qualified.
	 *
	 * @param prefixRequired true if the current completion item should be qualified, false otherwise
	 * @param prefix the completion prefix; the java identifier that completion was triggered on
	 * @param qualifiedPrefix the completion prefix with any qualifiers (eg. package qualifiers, type qualifiers, nested member access, etc.). May contain `.`
	 * @see CompletionEngine#computeRelevanceForQualification(boolean)
	 * @return the appropriate "qualified" relevance constant based on if the current content is qualified and if the content is expected to be qualified
	 */
	static int computeRelevanceForQualification(boolean prefixRequired, String prefix, String qualifiedPrefix) {
		boolean insideQualifiedReference = !prefix.equals(qualifiedPrefix);
		if (!prefixRequired && !insideQualifiedReference) {
			return RelevanceConstants.R_UNQUALIFIED;
		}
		if (prefixRequired && insideQualifiedReference) {
			return RelevanceConstants.R_QUALIFIED;
		}
		return 0;
	}

	/**
	 * Returns the appropriate relevance constant based on if type associated with the proposal matches the expected types.
	 *
	 * @param proposalType the binding of the type associated with the proposal
	 * @param expectedTypes the expected types
	 * @return the appropriate relevance constant based on if type associated with the proposal matches the expected types
	 */
	static int computeRelevanceForExpectingType(ITypeBinding proposalType, ExpectedTypes expectedTypes) {
		if (proposalType != null) {
			int relevance = 0;
			// https://bugs.eclipse.org/bugs/show_bug.cgi?id=271296
			// If there is at least one expected type, then void proposal types attract a degraded relevance.
			if (!expectedTypes.getExpectedTypes().isEmpty() && PrimitiveType.VOID.toString().equals(proposalType.getName())) {
				return RelevanceConstants.R_VOID;
			}
			for (ITypeBinding expectedType : expectedTypes.getExpectedTypes()) {
				if(expectedTypes.allowsSubtypes()
						&& proposalType.getErasure().isSubTypeCompatible(expectedType.getErasure())) {

					if(Objects.equals(expectedType.getQualifiedName(), proposalType.getQualifiedName())) {
						return RelevanceConstants.R_EXACT_EXPECTED_TYPE;
					} else if (expectedType.isRawType() && Objects.equals(expectedType.getErasure().getQualifiedName(), proposalType.getErasure().getQualifiedName())) {
						return RelevanceConstants.R_EXACT_EXPECTED_TYPE;
					} else if (proposalType.getPackage() != null && proposalType.getPackage().isUnnamed()) {
						return RelevanceConstants.R_PACKAGE_EXPECTED_TYPE;
					}
					relevance = RelevanceConstants.R_EXPECTED_TYPE;

				}
				if(expectedTypes.allowsSupertypes() && expectedType.isSubTypeCompatible(proposalType)) {

					if(Objects.equals(expectedType.getQualifiedName(), proposalType.getQualifiedName())) {
						return RelevanceConstants.R_EXACT_EXPECTED_TYPE;
					}
					relevance = RelevanceConstants.R_EXPECTED_TYPE;
				}
				// Bug 84720 - [1.5][assist] proposal ranking by return value should consider auto(un)boxing
				// Just ensuring that the unitScope is not null, even though it's an unlikely case.
				var oneErasure = expectedType.getErasure();
				var otherErasure = proposalType.getErasure();
				if ((oneErasure.isPrimitive() && otherErasure.getQualifiedName().startsWith("java.lang.")) ||
					(otherErasure.isPrimitive() && oneErasure.getQualifiedName().startsWith("java.lang."))) {
					if (oneErasure.getName().equalsIgnoreCase(otherErasure.getName()) ||
						Set.of(oneErasure.getName().toLowerCase(), otherErasure.getName().toLowerCase()).equals(Set.of("char", "character"))) {
						relevance = CompletionEngine.R_EXPECTED_TYPE;
					}
				}
				if (KeyUtils.OBJECT_KEY.equals(expectedType.getKey()) && proposalType.isPrimitive()) {
					// can be autoboxed
					relevance = CompletionEngine.R_EXPECTED_TYPE;
				}
			}
			return relevance;
		}
		return 0;
	}

	static int computeRelevanceForExpectingType(IType proposalType, ExpectedTypes expectedTypes, WorkingCopyOwner workingCopyOwner, Map<String, ITypeHierarchy> typeHierarchyCache) {
		if (proposalType != null) {
			IPackageFragment packageFragment = (IPackageFragment)proposalType.getAncestor(IJavaElement.PACKAGE_FRAGMENT);
			int relevance = 0;
			// https://bugs.eclipse.org/bugs/show_bug.cgi?id=271296
			// If there is at least one expected type, then void proposal types attract a degraded relevance.
			if (!expectedTypes.getExpectedTypes().isEmpty() && PrimitiveType.VOID.toString().equals(proposalType.getElementName())) {
				return RelevanceConstants.R_VOID;
			}
			for (ITypeBinding expectedType : expectedTypes.getExpectedTypes()) {
				if(expectedTypes.allowsSubtypes()
						&& DOMCompletionUtils.findInSupers(proposalType, expectedType.getKey(), workingCopyOwner, typeHierarchyCache)) {
					if (expectedType.getKey().equals(proposalType.getKey())) {
						return RelevanceConstants.R_EXACT_EXPECTED_TYPE;
						// ??? I'm just guessing on the default packages name here, it might be the empty string
					} else if (packageFragment != null && packageFragment.getElementName().equals("<default>")) {
						return RelevanceConstants.R_PACKAGE_EXPECTED_TYPE;
					}
					relevance = RelevanceConstants.R_EXPECTED_TYPE;

				}
				if(expectedTypes.allowsSupertypes() && DOMCompletionUtils.findInSupers(expectedType, proposalType.getKey())) {
					if (expectedType.getKey().equals(proposalType.getKey())) {
						return RelevanceConstants.R_EXACT_EXPECTED_TYPE;
					}
					relevance = RelevanceConstants.R_EXPECTED_TYPE;
				}
				// Bug 84720 - [1.5][assist] proposal ranking by return value should consider auto(un)boxing
				// Just ensuring that the unitScope is not null, even though it's an unlikely case.
				var oneErasure = expectedType.getErasure();
				if ((oneErasure.isPrimitive() && proposalType.getFullyQualifiedName().startsWith("java.lang."))) {
					if (oneErasure.getName().equalsIgnoreCase(proposalType.getElementName()) ||
						Set.of(oneErasure.getName().toLowerCase(), proposalType.getElementName().toLowerCase()).equals(Set.of("char", "character"))) {
						relevance = CompletionEngine.R_EXPECTED_TYPE;
					}
				}
			}
			return relevance;
		}
		return 0;
	}

	/**
	 * Returns the appropriate relevance based on if the given type is expected, without taking into account sub/super classes.
	 *
	 * @param type the type completion to get the relevance of
	 * @param expectedTypes the information on the expected type
	 * @return the appropriate relevance based on if the given type is expected, without taking into account sub/super classes
	 */
	static int simpleComputeRelevanceForExpectingType(IType type, ExpectedTypes expectedTypes) {
		return (expectedTypes.getExpectedTypes().stream().map(ITypeBinding::getQualifiedName).anyMatch(type.getFullyQualifiedName()::equals) ? RelevanceConstants.R_EXACT_EXPECTED_TYPE :
			expectedTypes.getExpectedTypes().stream().map(ITypeBinding::getQualifiedName).anyMatch(Object.class.getName()::equals) ? RelevanceConstants.R_EXPECTED_TYPE :
			0);
	}

	/**
	 * Returns the appropriate relevance number based on if the given member is directly implemented in the qualifying type.
	 *
	 * @see CompletionEngine#computeRelevanceForInheritance(ReferenceBinding, ReferenceBinding)
	 * @param qualifyingType the qualifying type
	 * @param member the member to check if it's directly or indirectly inherited
	 * @return the appropriate relevance number based on if the given member is directly implemented in the qualifying type
	 */
	static int computeRelevanceForInheritance(ITypeBinding qualifyingType, IBinding member) {
		if (qualifyingType == null) {
			return 0;
		}
		if (member instanceof IMethodBinding methodBinding) {
			if (methodBinding.getDeclaringClass().getKey().equals(qualifyingType.getKey())) {
				return RelevanceConstants.R_NON_INHERITED;
			}
		} else if (member instanceof IVariableBinding varBinding) {
			if (varBinding.getDeclaringClass().getKey().equals(qualifyingType.getKey())) {
				return RelevanceConstants.R_NON_INHERITED;
			}
		} else if (member instanceof ITypeBinding typeBinding) {
			if (typeBinding.getDeclaringClass().getKey().equals(qualifyingType.getKey())) {
				return RelevanceConstants.R_NON_INHERITED;
			}
		}
		return 0;
	}

	/**
	 * Returns the appropriate relevance number based on if the given member type is directly implemented in the qualifying type.
	 *
	 * @see CompletionEngine#computeRelevanceForInheritance(ReferenceBinding, ReferenceBinding)
	 * @param qualifyingType the qualifying type
	 * @param memberType the member type to check if it's directly or indirectly inherited
	 * @return the appropriate relevance number based on if the given member is directly implemented in the qualifying type
	 */
	static int computeRelevanceForInheritance(ITypeBinding qualifyingType, IType memberType) {
		if (qualifyingType == null || memberType.getDeclaringType() == null) {
			return 0;
		}
		if (memberType.getDeclaringType().getKey().equals(qualifyingType.getKey())) {
			return RelevanceConstants.R_NON_INHERITED;
		}
		return 0;
	}

	static int computeRelevanceForInteresting(IType potentiallyInterestingType, ExpectedTypes expectedTypes) {
		String typeKey = potentiallyInterestingType.getKey();
		for (ITypeBinding uninterestingType : expectedTypes.getUninterestingTypes()) {
			if (uninterestingType.getKey().equals(typeKey)) {
				return 0;
			}
		}
		return RelevanceConstants.R_INTERESTING;
	}

	/**
	 * Returns the appropriate relevance number based on the access rule, taking into account if access warnings/errors are disabled.
	 *
	 * i.e. if the errors for accessing an inaccessible class are disabled,
	 * the relevance number is the same as if there are no access restrictions.
	 *
	 * @param accessRuleKind the access rule
	 * @param settings the compiler settings
	 * @return the appropriate relevance number
	 */
	static int computeRelevanceForRestrictions(int accessRuleKind, Map<String, String> settings) {
		if (accessRuleKind == IAccessRule.K_ACCESSIBLE) {
			return RelevanceConstants.R_NON_RESTRICTED;
		}
		if (accessRuleKind == IAccessRule.K_NON_ACCESSIBLE
				&& settings.get(JavaCore.COMPILER_PB_FORBIDDEN_REFERENCE).equals(JavaCore.IGNORE)) {
			return RelevanceConstants.R_NON_RESTRICTED;
		}
		if (accessRuleKind == IAccessRule.K_DISCOURAGED
				&& settings.get(JavaCore.COMPILER_PB_DISCOURAGED_REFERENCE).equals(JavaCore.IGNORE)) {
			return RelevanceConstants.R_NON_RESTRICTED;
		}
		return 0;
	}

}
