/*******************************************************************************
 * Copyright (c) 2006 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/

package org.eclipse.jdt.core.dom;

import org.eclipse.jdt.core.compiler.IProblem;

/**
 * Internal AST visitor for propagating syntax errors.
 */
class ASTRecoveryPropagator extends DefaultASTVisitor {
	private IProblem[] problems;
	
	ASTRecoveryPropagator(IProblem[] problems) {
		// visit Javadoc.tags() as well
		this.problems = problems;
	}
	
	protected boolean visitNode(ASTNode node) {
		return checkAndTagAsMalformed(node);
	}
	
	private boolean checkAndTagAsMalformed(ASTNode node) {
		boolean tagWithErrors = false;
		search: for (int i = 0, max = this.problems.length; i < max; i++) {
			IProblem problem = this.problems[i];
			switch(problem.getID()) {
				case IProblem.ParsingErrorOnKeywordNoSuggestion :
				case IProblem.ParsingErrorOnKeyword :
				case IProblem.ParsingError :
				case IProblem.ParsingErrorNoSuggestion :
				case IProblem.ParsingErrorInsertTokenBefore :
				case IProblem.ParsingErrorInsertTokenAfter :
				case IProblem.ParsingErrorDeleteToken :
				case IProblem.ParsingErrorDeleteTokens :
				case IProblem.ParsingErrorMergeTokens :
				case IProblem.ParsingErrorInvalidToken :
				case IProblem.ParsingErrorMisplacedConstruct :
				case IProblem.ParsingErrorReplaceTokens :
				case IProblem.ParsingErrorNoSuggestionForTokens :
				case IProblem.ParsingErrorUnexpectedEOF :
				case IProblem.ParsingErrorInsertToComplete :
				case IProblem.ParsingErrorInsertToCompleteScope :
				case IProblem.ParsingErrorInsertToCompletePhrase :
				case IProblem.EndOfSource :
				case IProblem.InvalidHexa :
				case IProblem.InvalidOctal :
				case IProblem.InvalidCharacterConstant :
				case IProblem.InvalidEscape :
				case IProblem.InvalidInput :
				case IProblem.InvalidUnicodeEscape :
				case IProblem.InvalidFloat :
				case IProblem.NullSourceString :
				case IProblem.UnterminatedString :
				case IProblem.UnterminatedComment :
				case IProblem.InvalidDigit :
					break;
				default:
					continue search;
			}
			int problemStart = problem.getSourceStart();
			int problemEnd = problem.getSourceEnd();
			int start = node.getStartPosition();
			int end = start + node.getLength();
			if ((start <= problemStart) && (problemStart <= end) ||
					(start <= problemEnd) && (problemEnd <= end)) {
				node.setFlags(node.getFlags() | ASTNode.RECOVERED);
				tagWithErrors = true;
			}
		}
		return tagWithErrors;
	}
}
