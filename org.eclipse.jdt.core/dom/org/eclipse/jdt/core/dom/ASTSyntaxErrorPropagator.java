/*******************************************************************************
 * Copyright (c) 2001 International Business Machines Corp. and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v0.5 
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v05.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 ******************************************************************************/

package org.eclipse.jdt.core.dom;

import org.eclipse.jdt.internal.compiler.IProblem;
import org.eclipse.jdt.internal.compiler.problem.ProblemIrritants;

class ASTSyntaxErrorPropagator extends ASTVisitor {

	private IProblem[] problems;
	
	ASTSyntaxErrorPropagator(IProblem[] problems) {
		this.problems = problems;
	}

	private boolean checkAndTagAsMalformed(ASTNode node) {
		boolean tagWithErrors = false;
		search: for (int i = 0, max = this.problems.length; i < max; i++) {
			IProblem problem = this.problems[i];
			switch(problem.getID()) {
				case ProblemIrritants.ParsingErrorOnKeywordNoSuggestion :
				case ProblemIrritants.ParsingErrorOnKeyword :
				case ProblemIrritants.ParsingError :
				case ProblemIrritants.ParsingErrorNoSuggestion :
					break;
				default:
					continue search;
			}
			int position = problem.getSourceStart();
			int start = node.getStartPosition();
			int end = start + node.getLength();
			if ((start <= position) && (position <= end)) {
				node.setFlags(ASTNode.MALFORMED);
				// clear the bits on parent
				ASTNode currentNode = node.getParent();
				while (currentNode != null) {
					currentNode.setFlags(currentNode.getFlags() & ~ASTNode.MALFORMED);
					currentNode = currentNode.getParent();
				}
				tagWithErrors = true;
			}
		}
		return tagWithErrors;
	}

	/*
	 * @see ASTVisitor#visit(FieldDeclaration)
	 */
	public boolean visit(FieldDeclaration node) {
		return checkAndTagAsMalformed(node);		
	}

	/*
	 * @see ASTVisitor#visit(MethodDeclaration)
	 */
	public boolean visit(MethodDeclaration node) {
		return checkAndTagAsMalformed(node);		
	}

	/*
	 * @see ASTVisitor#visit(PackageDeclaration)
	 */
	public boolean visit(PackageDeclaration node) {
		return checkAndTagAsMalformed(node);		
	}

	/*
	 * @see ASTVisitor#visit(ImportDeclaration)
	 */
	public boolean visit(ImportDeclaration node) {
		return checkAndTagAsMalformed(node);		
	}

	/*
	 * @see ASTVisitor#visit(CompilationUnit)
	 */
	public boolean visit(CompilationUnit node) {
		return checkAndTagAsMalformed(node);		
	}

	/*
	 * @see ASTVisitor#visit(Initializer)
	 */
	public boolean visit(Initializer node) {
		return checkAndTagAsMalformed(node);		
	}

}
