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

import java.util.Objects;

import org.eclipse.jdt.core.compiler.CharOperation;
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.core.dom.PrimitiveType;
import org.eclipse.jdt.internal.codeassist.impl.AssistOptions;

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
//				if (this.unitScope != null && this.unitScope.isBoxingCompatibleWith(proposalType, this.expectedTypes[i])) {
//					relevance = CompletionEngine.R_EXPECTED_TYPE;
//				}
			}
			return relevance;
		}
		return 0;
	}

}
