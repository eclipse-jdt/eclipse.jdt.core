/*******************************************************************************
 * Copyright (c) 2000, 2003 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/

package org.eclipse.jdt.core.dom;

/**
 * A visitor for abstract syntax trees.
 * <p>
 * For each different concrete AST node type <it>T</it> there are
 * a pair of methods:
 * <ul>
 * <li><code>public boolean visit(<it>T</it> node)</code> - Visits
 * the given node to perform some arbitrary operation. If <code>true</code>
 * is returned, the given node's child nodes will be visited next; however,
 * if <code>false</code> is returned, the given node's child nodes will 
 * not be visited. The default implementation provided by this class does
 * nothing and returns <code>true</code>. Subclasses may reimplement
 * this method as needed.</li>
 * <li><code>public void endVisit(<it>T</it> node)</code> - Visits
 * the given node to perform some arbitrary operation. When used in the
 * conventional way, this method is called after all of the given node's
 * children have been visited (or immediately, if <code>visit</code> returned
 * <code>false</code>). The default implementation provided by this class does
 * nothing. Subclasses may reimplement this method as needed.</li>
 * </ul>
 * </p>
 * In addition, there are a pair of methods for visiting AST nodes in the 
 * abstract, regardless of node type:
 * <ul>
 * <li><code>public void preVisit(ASTNode node)</code> - Visits
 * the given node to perform some arbitrary operation. 
 * This method is invoked prior to the appropriate type-specific
 * <code>visit</code> method.
 * The default implementation of this method does nothing.
 * Subclasses may reimplement this method as needed.</li>
 * <li><code>public void postVisit(ASTNode node)</code> - Visits
 * the given node to perform some arbitrary operation. 
 * This method is invoked after the appropriate type-specific
 * <code>endVisit</code> method.
 * The default implementation of this method does nothing.
 * Subclasses may reimplement this method as needed.</li>
 * </ul>
 * <p>
 * For nodes with list-valued properties, the child nodes within the list
 * are visited in order. For nodes with multiple properties, the child nodes
 * are visited in the order that most closely corresponds to the lexical
 * reading order of the source program. For instance, for a type declaration
 * node, the child ordering is: name, superclass, superinterfaces, and 
 * body declarations.
 * </p>
 * <p>
 * While it is possible to modify the tree in the visitor, care is required to
 * ensure that the consequences are as expected and desirable.
 * During the course of an ordinary visit starting at a given node, every node
 * in the subtree is visited exactly twice, first with <code>visit</code> and
 * then with <code>endVisit</code>. During a traversal of a stationary tree, 
 * each node is either behind (after <code>endVisit</code>), ahead (before 
 * <code>visit</code>), or in progress (between <code>visit</code> and
 * the matching <code>endVisit</code>). Changes to the "behind" region of the
 * tree are of no consequence to the visit in progress. Changes to the "ahead"
 * region will be taken in stride. Changes to the "in progress" portion are
 * the more interesting cases. With a node, the various properties are arranged
 * in a linear list, with a cursor that separates the properties that have
 * been visited from the ones that are still to be visited (the cursor
 * is between the elements, rather than on an element). The cursor moves from
 * the head to the tail of this list, advancing to the next position just
 * <it>before</it> <code>visit</code> if called for that child. After the child
 * subtree has been completely visited, the visit moves on the child 
 * immediately after the cursor. Removing a child while it is being visited
 * does not alter the course of the visit. But any children added at positions
 * after the cursor are considered in the "ahead" portion and will be visited.
 * </p>
 * <p>
 * Cases to watch out for:
 * <ul>
 * <li>Moving a child node further down the list. This could result in the
 * child subtree being visited multiple times; these visits are sequential.</li>
 * <li>Moving a child node up into an ancestor. If the new home for
 * the node is in the "ahead" portion, the subtree will be visited 
 * a second time; again, these visits are sequential.</li>
 * <li>Moving a node down into a child. If the new home for
 * the node is in the "ahead" portion, the subtree will be visited 
 * a second time; in this case, the visits will be nested. In some cases,
 * this can lead to a stack overflow or out of memory condition.</li>
 * </ul>
 * </p>
 * 
 * @see ASTNode#accept
 */
public abstract class ASTVisitor {
	
	/**
	 * Visits the given AST node prior to the type-specific visit.
	 * (before <code>visit</code>).
	 * <p>
	 * The default implementation does nothing. Subclasses may reimplement.
	 * </p>
	 * 
	 * @param node the node to visit
	 */
	public void preVisit(ASTNode node) {
		// default implementation: do nothing
	}
	
	/**
	 * Visits the given AST node following the type-specific visit
	 * (after <code>endVisit</code>).
	 * <p>
	 * The default implementation does nothing. Subclasses may reimplement.
	 * </p>
	 * 
	 * @param node the node to visit
	 */
	public void postVisit(ASTNode node) {
		// default implementation: do nothing
	}

	public boolean visit(AnonymousClassDeclaration node) {
		return true;
	}
	public boolean visit(ArrayAccess node) {
		return true;
	}
	public boolean visit(ArrayCreation node) {
		return true;
	}
	public boolean visit(ArrayInitializer node) {
		return true;
	}
	public boolean visit(ArrayType node) {
		return true;
	}
	public boolean visit(AssertStatement node) {
		return true;
	}
	public boolean visit(Assignment node) {
		return true;
	}
	public boolean visit(Block node) {
		return true;
	}
	public boolean visit(BooleanLiteral node) {
		return true;
	}
	public boolean visit(BreakStatement node) {
		return true;
	}
	public boolean visit(CastExpression node) {
		return true;
	}
	public boolean visit(CatchClause node) {
		return true;
	}
	public boolean visit(CharacterLiteral node) {
		return true;
	}
	public boolean visit(ClassInstanceCreation node) {
		return true;
	}
	public boolean visit(CompilationUnit node) {
		return true;
	}
	public boolean visit(ConditionalExpression node) {
		return true;
	}
	public boolean visit(ConstructorInvocation node) {
		return true;
	}
	public boolean visit(ContinueStatement node) {
		return true;
	}
	public boolean visit(DoStatement node) {
		return true;
	}
	public boolean visit(EmptyStatement node) {
		return true;
	}
	public boolean visit(ExpressionStatement node) {
		return true;
	}
	public boolean visit(FieldAccess node) {
		return true;
	}
	public boolean visit(FieldDeclaration node) {
		return true;
	}
	public boolean visit(ForStatement node) {
		return true;
	}
	public boolean visit(IfStatement node) {
		return true;
	}
	public boolean visit(ImportDeclaration node) {
		return true;
	}
	public boolean visit(InfixExpression node) {
		return true;
	}
	public boolean visit(InstanceofExpression node) {
		return true;
	}
	public boolean visit(Initializer node) {
		return true;
	}
	public boolean visit(Javadoc node) {
		return true;
	}
	public boolean visit(LabeledStatement node) {
		return true;
	}
	public boolean visit(MethodDeclaration node) {
		return true;
	}
	public boolean visit(MethodInvocation node) {
		return true;
	}
	public boolean visit(NullLiteral node) {
		return true;
	}
	public boolean visit(NumberLiteral node) {
		return true;
	}
	public boolean visit(PackageDeclaration node) {
		return true;
	}
	public boolean visit(ParenthesizedExpression node) {
		return true;
	}
	public boolean visit(PostfixExpression node) {
		return true;
	}
	public boolean visit(PrefixExpression node) {
		return true;
	}
	public boolean visit(PrimitiveType node) {
		return true;
	}
	public boolean visit(QualifiedName node) {
		return true;
	}
	public boolean visit(ReturnStatement node) {
		return true;
	}
	public boolean visit(SimpleName node) {
		return true;
	}
	public boolean visit(SimpleType node) {
		return true;
	}
	public boolean visit(StringLiteral node) {
		return true;
	}
	public boolean visit(SuperConstructorInvocation node) {
		return true;
	}
	public boolean visit(SuperFieldAccess node) {
		return true;
	}
	public boolean visit(SuperMethodInvocation node) {
		return true;
	}
	public boolean visit(SwitchCase node) {
		return true;
	}
	public boolean visit(SwitchStatement node) {
		return true;
	}
	public boolean visit(SynchronizedStatement node) {
		return true;
	}
	public boolean visit(ThisExpression node) {
		return true;
	}
	public boolean visit(ThrowStatement node) {
		return true;
	}
	public boolean visit(TryStatement node) {
		return true;
	}
	public boolean visit(TypeDeclaration node) {
		return true;
	}
	public boolean visit(TypeDeclarationStatement node) {
		return true;
	}
	public boolean visit(TypeLiteral node) {
		return true;
	}
	public boolean visit(SingleVariableDeclaration node) {
		return true;
	}
	public boolean visit(VariableDeclarationExpression node) {
		return true;
	}
	public boolean visit(VariableDeclarationStatement node) {
		return true;
	}
	public boolean visit(VariableDeclarationFragment node) {
		return true;
	}
	public boolean visit(WhileStatement node) {
		return true;
	}

	public void endVisit(AnonymousClassDeclaration node) {
		// default implementation: do nothing
	}
	public void endVisit(ArrayAccess node) {
		// default implementation: do nothing
	}
	public void endVisit(ArrayCreation node) {
		// default implementation: do nothing
	}
	public void endVisit(ArrayInitializer node) {
		// default implementation: do nothing
	}
	public void endVisit(ArrayType node) {
		// default implementation: do nothing
	}
	public void endVisit(AssertStatement node) {
		// default implementation: do nothing
	}
	public void endVisit(Assignment node) {
		// default implementation: do nothing
	}
	public void endVisit(Block node) {
		// default implementation: do nothing
	}
	public void endVisit(BooleanLiteral node) {
		// default implementation: do nothing
	}
	public void endVisit(BreakStatement node) {
		// default implementation: do nothing
	}
	public void endVisit(CastExpression node) {
		// default implementation: do nothing
	}
	public void endVisit(CatchClause node) {
		// default implementation: do nothing
	}
	public void endVisit(CharacterLiteral node) {
		// default implementation: do nothing
	}
	public void endVisit(ClassInstanceCreation node) {
		// default implementation: do nothing
	}
	public void endVisit(CompilationUnit node) {
		// default implementation: do nothing
	}
	public void endVisit(ConditionalExpression node) {
		// default implementation: do nothing
	}
	public void endVisit(ConstructorInvocation node) {
		// default implementation: do nothing
	}
	public void endVisit(ContinueStatement node) {
		// default implementation: do nothing
	}
	public void endVisit(DoStatement node) {
		// default implementation: do nothing
	}
	public void endVisit(EmptyStatement node) {
		// default implementation: do nothing
	}
	public void endVisit(ExpressionStatement node) {
		// default implementation: do nothing
	}
	public void endVisit(FieldAccess node) {
		// default implementation: do nothing
	}
	public void endVisit(FieldDeclaration node) {
		// default implementation: do nothing
	}
	public void endVisit(ForStatement node) {
		// default implementation: do nothing
	}
	public void endVisit(IfStatement node) {
		// default implementation: do nothing
	}
	public void endVisit(ImportDeclaration node) {
		// default implementation: do nothing
	}
	public void endVisit(InfixExpression node) {
		// default implementation: do nothing
	}
	public void endVisit(InstanceofExpression node) {
		// default implementation: do nothing
	}
	public void endVisit(Initializer node) {
		// default implementation: do nothing
	}
	public void endVisit(Javadoc node) {
		// default implementation: do nothing
	}
	public void endVisit(LabeledStatement node) {
		// default implementation: do nothing
	}
	public void endVisit(MethodDeclaration node) {
		// default implementation: do nothing
	}
	public void endVisit(MethodInvocation node) {
		// default implementation: do nothing
	}
	public void endVisit(NullLiteral node) {
		// default implementation: do nothing
	}
	public void endVisit(NumberLiteral node) {
		// default implementation: do nothing
	}
	public void endVisit(PackageDeclaration node) {
		// default implementation: do nothing
	}
	public void endVisit(ParenthesizedExpression node) {
		// default implementation: do nothing
	}
	public void endVisit(PostfixExpression node) {
		// default implementation: do nothing
	}
	public void endVisit(PrefixExpression node) {
		// default implementation: do nothing
	}
	public void endVisit(PrimitiveType node) {
		// default implementation: do nothing
	}
	public void endVisit(QualifiedName node) {
		// default implementation: do nothing
	}
	public void endVisit(ReturnStatement node) {
		// default implementation: do nothing
	}
	public void endVisit(SimpleName node) {
		// default implementation: do nothing
	}
	public void endVisit(SimpleType node) {
		// default implementation: do nothing
	}
	public void endVisit(StringLiteral node) {
		// default implementation: do nothing
	}
	public void endVisit(SuperConstructorInvocation node) {
		// default implementation: do nothing
	}
	public void endVisit(SuperFieldAccess node) {
		// default implementation: do nothing
	}
	public void endVisit(SuperMethodInvocation node) {
		// default implementation: do nothing
	}
	public void endVisit(SwitchCase node) {
		// default implementation: do nothing
	}
	public void endVisit(SwitchStatement node) {
		// default implementation: do nothing
	}
	public void endVisit(SynchronizedStatement node) {
		// default implementation: do nothing
	}
	public void endVisit(ThisExpression node) {
		// default implementation: do nothing
	}
	public void endVisit(ThrowStatement node) {
		// default implementation: do nothing
	}
	public void endVisit(TryStatement node) {
		// default implementation: do nothing
	}
	public void endVisit(TypeDeclaration node) {
		// default implementation: do nothing
	}
	public void endVisit(TypeDeclarationStatement node) {
		// default implementation: do nothing
	}
	public void endVisit(TypeLiteral node) {
		// default implementation: do nothing
	}
	public void endVisit(SingleVariableDeclaration node) {
		// default implementation: do nothing
	}
	public void endVisit(VariableDeclarationExpression node) {
		// default implementation: do nothing
	}
	public void endVisit(VariableDeclarationStatement node) {
		// default implementation: do nothing
	}
	public void endVisit(VariableDeclarationFragment node) {
		// default implementation: do nothing
	}
	public void endVisit(WhileStatement node) {
		// default implementation: do nothing
	}
}
