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
package org.eclipse.jdt.core.tests.dom;

import java.util.HashMap;
import java.util.HashSet;

import org.eclipse.jdt.core.dom.*;
import org.eclipse.jdt.core.dom.ASTVisitor;

/**
 * @author oliviert
 *
 * To change this generated comment edit the template variable "typecomment":
 * Window>Preferences>Java>Templates.
 * To enable and disable the creation of type comments go to
 * Window>Preferences>Java>Code Generation.
 */
class BindingsCollectorVisitor extends ASTVisitor {

	private HashMap hashMap;
	private HashSet set;
	
	BindingsCollectorVisitor() {
		this.hashMap = new HashMap();
		this.set = new HashSet();
	}
	
	private void collectBindings(
		ASTNode node,
		IBinding binding) {
		if (binding != null) {
			hashMap.put(node, binding);
		} else {
			set.add(node);
		}
	}

	/**
	 * @see org.eclipse.jdt.core.dom.ASTVisitor#endVisit(AnonymousClassDeclaration)
	 */
	public void endVisit(AnonymousClassDeclaration node) {
		ITypeBinding typeBinding = node.resolveBinding();
		collectBindings(node, typeBinding);
	}

	/**
	 * @see org.eclipse.jdt.core.dom.ASTVisitor#endVisit(ArrayAccess)
	 */
	public void endVisit(ArrayAccess node) {
		ITypeBinding typeBinding = node.resolveTypeBinding();
		collectBindings(node, typeBinding);
	}

	/**
	 * @see org.eclipse.jdt.core.dom.ASTVisitor#endVisit(ArrayCreation)
	 */
	public void endVisit(ArrayCreation node) {
		ITypeBinding typeBinding = node.resolveTypeBinding();
		collectBindings(node, typeBinding);
	}

	/**
	 * @see org.eclipse.jdt.core.dom.ASTVisitor#endVisit(ArrayInitializer)
	 */
	public void endVisit(ArrayInitializer node) {
		ITypeBinding typeBinding = node.resolveTypeBinding();
		collectBindings(node, typeBinding);
	}

	/**
	 * @see org.eclipse.jdt.core.dom.ASTVisitor#endVisit(ArrayType)
	 */
	public void endVisit(ArrayType node) {
		ITypeBinding typeBinding = node.resolveBinding();
		collectBindings(node, typeBinding);
	}

	/**
	 * @see org.eclipse.jdt.core.dom.ASTVisitor#endVisit(AssertStatement)
	 */
	public void endVisit(AssertStatement node) {
	}

	/**
	 * @see org.eclipse.jdt.core.dom.ASTVisitor#endVisit(Assignment)
	 */
	public void endVisit(Assignment node) {
		ITypeBinding typeBinding = node.resolveTypeBinding();
		collectBindings(node, typeBinding);
	}

	/**
	 * @see org.eclipse.jdt.core.dom.ASTVisitor#endVisit(Block)
	 */
	public void endVisit(Block node) {
	}

	/**
	 * @see org.eclipse.jdt.core.dom.ASTVisitor#endVisit(BooleanLiteral)
	 */
	public void endVisit(BooleanLiteral node) {
		ITypeBinding typeBinding = node.resolveTypeBinding();
		collectBindings(node, typeBinding);
	}

	/**
	 * @see org.eclipse.jdt.core.dom.ASTVisitor#endVisit(BreakStatement)
	 */
	public void endVisit(BreakStatement node) {
	}

	/**
	 * @see org.eclipse.jdt.core.dom.ASTVisitor#endVisit(CastExpression)
	 */
	public void endVisit(CastExpression node) {
		ITypeBinding typeBinding = node.resolveTypeBinding();
		collectBindings(node, typeBinding);
	}

	/**
	 * @see org.eclipse.jdt.core.dom.ASTVisitor#endVisit(CatchClause)
	 */
	public void endVisit(CatchClause node) {
	}

	/**
	 * @see org.eclipse.jdt.core.dom.ASTVisitor#endVisit(CharacterLiteral)
	 */
	public void endVisit(CharacterLiteral node) {
		ITypeBinding typeBinding = node.resolveTypeBinding();
		collectBindings(node, typeBinding);
	}

	/**
	 * @see org.eclipse.jdt.core.dom.ASTVisitor#endVisit(ClassInstanceCreation)
	 */
	public void endVisit(ClassInstanceCreation node) {
		ITypeBinding typeBinding = node.resolveTypeBinding();
		collectBindings(node, typeBinding);
	}

	/**
	 * @see org.eclipse.jdt.core.dom.ASTVisitor#endVisit(CompilationUnit)
	 */
	public void endVisit(CompilationUnit node) {
	}

	/**
	 * @see org.eclipse.jdt.core.dom.ASTVisitor#endVisit(ConditionalExpression)
	 */
	public void endVisit(ConditionalExpression node) {
		ITypeBinding typeBinding = node.resolveTypeBinding();
		collectBindings(node, typeBinding);
	}

	/**
	 * @see org.eclipse.jdt.core.dom.ASTVisitor#endVisit(ConstructorInvocation)
	 */
	public void endVisit(ConstructorInvocation node) {
		IMethodBinding methodBinding = node.resolveConstructorBinding();
		collectBindings(node, methodBinding);
	}

	/**
	 * @see org.eclipse.jdt.core.dom.ASTVisitor#endVisit(ContinueStatement)
	 */
	public void endVisit(ContinueStatement node) {
	}

	/**
	 * @see org.eclipse.jdt.core.dom.ASTVisitor#endVisit(DoStatement)
	 */
	public void endVisit(DoStatement node) {
	}

	/**
	 * @see org.eclipse.jdt.core.dom.ASTVisitor#endVisit(EmptyStatement)
	 */
	public void endVisit(EmptyStatement node) {
	}

	/**
	 * @see org.eclipse.jdt.core.dom.ASTVisitor#endVisit(ExpressionStatement)
	 */
	public void endVisit(ExpressionStatement node) {}
	

	/**
	 * @see org.eclipse.jdt.core.dom.ASTVisitor#endVisit(FieldAccess)
	 */
	public void endVisit(FieldAccess node) {
		ITypeBinding typeBinding = node.resolveTypeBinding();
		collectBindings(node, typeBinding);
	}
	
	/**
	 * @see org.eclipse.jdt.core.dom.ASTVisitor#endVisit(FieldDeclaration)
	 */
	public void endVisit(FieldDeclaration node) {
	}

	/**
	 * @see org.eclipse.jdt.core.dom.ASTVisitor#endVisit(ForStatement)
	 */
	public void endVisit(ForStatement node) {
	}

	/**
	 * @see org.eclipse.jdt.core.dom.ASTVisitor#endVisit(IfStatement)
	 */
	public void endVisit(IfStatement node) {
	}

	/**
	 * @see org.eclipse.jdt.core.dom.ASTVisitor#endVisit(ImportDeclaration)
	 */
	public void endVisit(ImportDeclaration node) {
		IBinding binding = node.resolveBinding();
		collectBindings(node, binding);
	}

	/**
	 * @see org.eclipse.jdt.core.dom.ASTVisitor#endVisit(InfixExpression)
	 */
	public void endVisit(InfixExpression node) {
		ITypeBinding typeBinding = node.resolveTypeBinding();
		collectBindings(node, typeBinding);
	}

	/**
	 * @see org.eclipse.jdt.core.dom.ASTVisitor#endVisit(Initializer)
	 */
	public void endVisit(Initializer node) {
	}

	/**
	 * @see org.eclipse.jdt.core.dom.ASTVisitor#endVisit(InstanceofExpression)
	 */
	public void endVisit(InstanceofExpression node) {
		ITypeBinding typeBinding = node.resolveTypeBinding();
		collectBindings(node, typeBinding);
	}

	/**
	 * @see org.eclipse.jdt.core.dom.ASTVisitor#endVisit(Javadoc)
	 */
	public void endVisit(Javadoc node) {
	}

	/**
	 * @see org.eclipse.jdt.core.dom.ASTVisitor#endVisit(LabeledStatement)
	 */
	public void endVisit(LabeledStatement node) {
	}

	/**
	 * @see org.eclipse.jdt.core.dom.ASTVisitor#endVisit(MethodDeclaration)
	 */
	public void endVisit(MethodDeclaration node) {
		IMethodBinding methodBinding = node.resolveBinding();
		collectBindings(node, methodBinding);
	}

	/**
	 * @see org.eclipse.jdt.core.dom.ASTVisitor#endVisit(MethodInvocation)
	 */
	public void endVisit(MethodInvocation node) {
		ITypeBinding typeBinding = node.resolveTypeBinding();
		collectBindings(node, typeBinding);
	}

	/**
	 * @see org.eclipse.jdt.core.dom.ASTVisitor#endVisit(NullLiteral)
	 */
	public void endVisit(NullLiteral node) {
		ITypeBinding typeBinding = node.resolveTypeBinding();
		collectBindings(node, typeBinding);
	}

	/**
	 * @see org.eclipse.jdt.core.dom.ASTVisitor#endVisit(NumberLiteral)
	 */
	public void endVisit(NumberLiteral node) {
		ITypeBinding typeBinding = node.resolveTypeBinding();
		collectBindings(node, typeBinding);
	}

	/**
	 * @see org.eclipse.jdt.core.dom.ASTVisitor#endVisit(PackageDeclaration)
	 */
	public void endVisit(PackageDeclaration node) {
		IPackageBinding packageBinding = node.resolveBinding();
		collectBindings(node, packageBinding);
	}

	/**
	 * @see org.eclipse.jdt.core.dom.ASTVisitor#endVisit(ParenthesizedExpression)
	 */
	public void endVisit(ParenthesizedExpression node) {
		ITypeBinding typeBinding = node.resolveTypeBinding();
		collectBindings(node, typeBinding);
	}

	/**
	 * @see org.eclipse.jdt.core.dom.ASTVisitor#endVisit(PostfixExpression)
	 */
	public void endVisit(PostfixExpression node) {
		ITypeBinding typeBinding = node.resolveTypeBinding();
		collectBindings(node, typeBinding);
	}

	/**
	 * @see org.eclipse.jdt.core.dom.ASTVisitor#endVisit(PrefixExpression)
	 */
	public void endVisit(PrefixExpression node) {
		ITypeBinding typeBinding = node.resolveTypeBinding();
		collectBindings(node, typeBinding);
	}

	/**
	 * @see org.eclipse.jdt.core.dom.ASTVisitor#endVisit(PrimitiveType)
	 */
	public void endVisit(PrimitiveType node) {
		ITypeBinding typeBinding = node.resolveBinding();
		collectBindings(node, typeBinding);
	}

	/**
	 * @see org.eclipse.jdt.core.dom.ASTVisitor#endVisit(QualifiedName)
	 */
	public void endVisit(QualifiedName node) {
		IBinding binding = node.resolveBinding();
		collectBindings(node, binding);
	}

	/**
	 * @see org.eclipse.jdt.core.dom.ASTVisitor#endVisit(ReturnStatement)
	 */
	public void endVisit(ReturnStatement node) {
	}

	/**
	 * @see org.eclipse.jdt.core.dom.ASTVisitor#endVisit(SimpleName)
	 */
	public void endVisit(SimpleName node) {
		IBinding binding = node.resolveBinding();
		collectBindings(node, binding);
	}

	/**
	 * @see org.eclipse.jdt.core.dom.ASTVisitor#endVisit(SimpleType)
	 */
	public void endVisit(SimpleType node) {
		ITypeBinding typeBinding = node.resolveBinding();
		collectBindings(node, typeBinding);
	}

	/**
	 * @see org.eclipse.jdt.core.dom.ASTVisitor#endVisit(SingleVariableDeclaration)
	 */
	public void endVisit(SingleVariableDeclaration node) {
		IVariableBinding variableBinding = node.resolveBinding();
		collectBindings(node, variableBinding);
	}

	/**
	 * @see org.eclipse.jdt.core.dom.ASTVisitor#endVisit(StringLiteral)
	 */
	public void endVisit(StringLiteral node) {
		ITypeBinding typeBinding = node.resolveTypeBinding();
		collectBindings(node, typeBinding);
	}

	/**
	 * @see org.eclipse.jdt.core.dom.ASTVisitor#endVisit(SuperConstructorInvocation)
	 */
	public void endVisit(SuperConstructorInvocation node) {
		IMethodBinding methodBinding = node.resolveConstructorBinding();
		collectBindings(node, methodBinding);
	}

	/**
	 * @see org.eclipse.jdt.core.dom.ASTVisitor#endVisit(SuperFieldAccess)
	 */
	public void endVisit(SuperFieldAccess node) {
		ITypeBinding typeBinding = node.resolveTypeBinding();
		collectBindings(node, typeBinding);
	}

	/**
	 * @see org.eclipse.jdt.core.dom.ASTVisitor#endVisit(SuperMethodInvocation)
	 */
	public void endVisit(SuperMethodInvocation node) {
		ITypeBinding typeBinding = node.resolveTypeBinding();
		collectBindings(node, typeBinding);
	}

	/**
	 * @see org.eclipse.jdt.core.dom.ASTVisitor#endVisit(SwitchCase)
	 */
	public void endVisit(SwitchCase node) {
	}

	/**
	 * @see org.eclipse.jdt.core.dom.ASTVisitor#endVisit(SwitchStatement)
	 */
	public void endVisit(SwitchStatement node) {
	}

	/**
	 * @see org.eclipse.jdt.core.dom.ASTVisitor#endVisit(SynchronizedStatement)
	 */
	public void endVisit(SynchronizedStatement node) {
	}

	/**
	 * @see org.eclipse.jdt.core.dom.ASTVisitor#endVisit(ThisExpression)
	 */
	public void endVisit(ThisExpression node) {
		ITypeBinding typeBinding = node.resolveTypeBinding();
		collectBindings(node, typeBinding);
	}

	/**
	 * @see org.eclipse.jdt.core.dom.ASTVisitor#endVisit(ThrowStatement)
	 */
	public void endVisit(ThrowStatement node) {
	}

	/**
	 * @see org.eclipse.jdt.core.dom.ASTVisitor#endVisit(TryStatement)
	 */
	public void endVisit(TryStatement node) {
	}

	/**
	 * @see org.eclipse.jdt.core.dom.ASTVisitor#endVisit(TypeDeclaration)
	 */
	public void endVisit(TypeDeclaration node) {
		ITypeBinding typeBinding = node.resolveBinding();
		collectBindings(node, typeBinding);
	}

	/**
	 * @see org.eclipse.jdt.core.dom.ASTVisitor#endVisit(TypeDeclarationStatement)
	 */
	public void endVisit(TypeDeclarationStatement node) {
	}

	/**
	 * @see org.eclipse.jdt.core.dom.ASTVisitor#endVisit(TypeLiteral)
	 */
	public void endVisit(TypeLiteral node) {
		ITypeBinding typeBinding = node.resolveTypeBinding();
		collectBindings(node, typeBinding);
	}

	/**
	 * @see org.eclipse.jdt.core.dom.ASTVisitor#endVisit(VariableDeclarationExpression)
	 */
	public void endVisit(VariableDeclarationExpression node) {
		ITypeBinding typeBinding = node.resolveTypeBinding();
		collectBindings(node, typeBinding);
	}

	/**
	 * @see org.eclipse.jdt.core.dom.ASTVisitor#endVisit(VariableDeclarationFragment)
	 */
	public void endVisit(VariableDeclarationFragment node) {
		IVariableBinding variableBinding = node.resolveBinding();
		collectBindings(node, variableBinding);
	}

	/**
	 * @see org.eclipse.jdt.core.dom.ASTVisitor#endVisit(VariableDeclarationStatement)
	 */
	public void endVisit(VariableDeclarationStatement node) {
	}

	/**
	 * @see org.eclipse.jdt.core.dom.ASTVisitor#endVisit(WhileStatement)
	 */
	public void endVisit(WhileStatement node) {
	}

	/**
	 * Returns the hashMap.
	 * @return HashMap
	 */
	public HashMap getBindingsMap() {
		return hashMap;
	}

	/**
	 * Returns the set.
	 * @return HashSet
	 */
	public HashSet getUnresolvedNodesSet() {
		return set;
	}

}
