package org.eclipse.jdt.core.dom;

import org.eclipse.jdt.internal.compiler.IProblem;

class ASTErrorPropagator extends ASTVisitor {

	private IProblem[] problems;
	
	ASTErrorPropagator(IProblem[] problems) {
		this.problems = problems;
	}

	private boolean checkAndTagAsMalformed(ASTNode node) {
		boolean tagWithErrors = false;
		for (int i = 0, max = this.problems.length; i < max; i++) {
			int position = this.problems[i].getSourceStart();
			int start = node.getStartPosition();
			int end = start + node.getLength();
			if ((start <= position) && (position <= end)) {
				node.setFlags(ASTNode.MALFORMED);
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

}
