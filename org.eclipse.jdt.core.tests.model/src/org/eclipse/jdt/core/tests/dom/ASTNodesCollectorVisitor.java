package org.eclipse.jdt.core.tests.dom;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.eclipse.jdt.core.dom.*;
import org.eclipse.jdt.core.dom.ASTVisitor;

class ASTNodesCollectorVisitor extends ASTVisitor {

	private Set detachedAstNodes;
	
	/**
	 * 
	 * @see java.lang.Object#Object()
	 */
	ASTNodesCollectorVisitor() {
		this.detachedAstNodes = new HashSet();
	}

	private void add(ASTNode node) {
		this.detachedAstNodes.add(node);
	}
		
	/**
	 * @see org.eclipse.jdt.core.dom.ASTVisitor#endVisit(org.eclipse.jdt.core.dom.AnonymousClassDeclaration)
	 */
	public void endVisit(AnonymousClassDeclaration node) {
		add(node);
		detachedListElement(node.bodyDeclarations());
	}

	/**
	 * @see org.eclipse.jdt.core.dom.ASTVisitor#endVisit(org.eclipse.jdt.core.dom.ArrayAccess)
	 */
	public void endVisit(ArrayAccess node) {
	}

	/**
	 * @see org.eclipse.jdt.core.dom.ASTVisitor#endVisit(org.eclipse.jdt.core.dom.ArrayCreation)
	 */
	public void endVisit(ArrayCreation node) {
	}

	/**
	 * @see org.eclipse.jdt.core.dom.ASTVisitor#endVisit(org.eclipse.jdt.core.dom.ArrayInitializer)
	 */
	public void endVisit(ArrayInitializer node) {
	}

	/**
	 * @see org.eclipse.jdt.core.dom.ASTVisitor#endVisit(org.eclipse.jdt.core.dom.ArrayType)
	 */
	public void endVisit(ArrayType node) {
	}

	/**
	 * @see org.eclipse.jdt.core.dom.ASTVisitor#endVisit(org.eclipse.jdt.core.dom.AssertStatement)
	 */
	public void endVisit(AssertStatement node) {
	}

	/**
	 * @see org.eclipse.jdt.core.dom.ASTVisitor#endVisit(org.eclipse.jdt.core.dom.Assignment)
	 */
	public void endVisit(Assignment node) {
	}

	/**
	 * @see org.eclipse.jdt.core.dom.ASTVisitor#endVisit(org.eclipse.jdt.core.dom.Block)
	 */
	public void endVisit(Block node) {
	}

	private void detachedListElement(List list) {
		for (int i = 0; i < list.size(); i++) {
			list.remove(0);
		}
	}

	/**
	 * @see org.eclipse.jdt.core.dom.ASTVisitor#endVisit(org.eclipse.jdt.core.dom.BooleanLiteral)
	 */
	public void endVisit(BooleanLiteral node) {
	}

	/**
	 * @see org.eclipse.jdt.core.dom.ASTVisitor#endVisit(org.eclipse.jdt.core.dom.BreakStatement)
	 */
	public void endVisit(BreakStatement node) {
		
	}

	/**
	 * @see org.eclipse.jdt.core.dom.ASTVisitor#endVisit(org.eclipse.jdt.core.dom.CastExpression)
	 */
	public void endVisit(CastExpression node) {
	}

	/**
	 * @see org.eclipse.jdt.core.dom.ASTVisitor#endVisit(org.eclipse.jdt.core.dom.CatchClause)
	 */
	public void endVisit(CatchClause node) {
	}

	/**
	 * @see org.eclipse.jdt.core.dom.ASTVisitor#endVisit(org.eclipse.jdt.core.dom.CharacterLiteral)
	 */
	public void endVisit(CharacterLiteral node) {
	}

	/**
	 * @see org.eclipse.jdt.core.dom.ASTVisitor#endVisit(org.eclipse.jdt.core.dom.ClassInstanceCreation)
	 */
	public void endVisit(ClassInstanceCreation node) {
		node.setName(node.getAST().newSimpleName("XXX")); //$NON-NLS-1$
	}

	/**
	 * @see org.eclipse.jdt.core.dom.ASTVisitor#endVisit(org.eclipse.jdt.core.dom.CompilationUnit)
	 */
	public void endVisit(CompilationUnit node) {
		detachedListElement(node.imports());
		node.setPackage(node.getAST().newPackageDeclaration());
	}

	/**
	 * @see org.eclipse.jdt.core.dom.ASTVisitor#endVisit(org.eclipse.jdt.core.dom.ConditionalExpression)
	 */
	public void endVisit(ConditionalExpression node) {
	}

	/**
	 * @see org.eclipse.jdt.core.dom.ASTVisitor#endVisit(org.eclipse.jdt.core.dom.ConstructorInvocation)
	 */
	public void endVisit(ConstructorInvocation node) {
	}

	/**
	 * @see org.eclipse.jdt.core.dom.ASTVisitor#endVisit(org.eclipse.jdt.core.dom.ContinueStatement)
	 */
	public void endVisit(ContinueStatement node) {
	}

	/**
	 * @see org.eclipse.jdt.core.dom.ASTVisitor#endVisit(org.eclipse.jdt.core.dom.DoStatement)
	 */
	public void endVisit(DoStatement node) {
	}

	/**
	 * @see org.eclipse.jdt.core.dom.ASTVisitor#endVisit(org.eclipse.jdt.core.dom.EmptyStatement)
	 */
	public void endVisit(EmptyStatement node) {
	}

	/**
	 * @see org.eclipse.jdt.core.dom.ASTVisitor#endVisit(org.eclipse.jdt.core.dom.ExpressionStatement)
	 */
	public void endVisit(ExpressionStatement node) {
	}

	/**
	 * @see org.eclipse.jdt.core.dom.ASTVisitor#endVisit(org.eclipse.jdt.core.dom.FieldAccess)
	 */
	public void endVisit(FieldAccess node) {
		node.setName(node.getAST().newSimpleName("XXX")); //$NON-NLS-1$
	}

	/**
	 * @see org.eclipse.jdt.core.dom.ASTVisitor#endVisit(org.eclipse.jdt.core.dom.FieldDeclaration)
	 */
	public void endVisit(FieldDeclaration node) {
		detachedListElement(node.fragments());
	}

	/**
	 * @see org.eclipse.jdt.core.dom.ASTVisitor#endVisit(org.eclipse.jdt.core.dom.ForStatement)
	 */
	public void endVisit(ForStatement node) {
	}

	/**
	 * @see org.eclipse.jdt.core.dom.ASTVisitor#endVisit(org.eclipse.jdt.core.dom.IfStatement)
	 */
	public void endVisit(IfStatement node) {
	}

	/**
	 * @see org.eclipse.jdt.core.dom.ASTVisitor#endVisit(org.eclipse.jdt.core.dom.ImportDeclaration)
	 */
	public void endVisit(ImportDeclaration node) {
		add(node);
		node.setName(node.getAST().newSimpleName("XXX")); //$NON-NLS-1$
	}

	/**
	 * @see org.eclipse.jdt.core.dom.ASTVisitor#endVisit(org.eclipse.jdt.core.dom.InfixExpression)
	 */
	public void endVisit(InfixExpression node) {
	}

	/**
	 * @see org.eclipse.jdt.core.dom.ASTVisitor#endVisit(org.eclipse.jdt.core.dom.Initializer)
	 */
	public void endVisit(Initializer node) {
	}

	/**
	 * @see org.eclipse.jdt.core.dom.ASTVisitor#endVisit(org.eclipse.jdt.core.dom.InstanceofExpression)
	 */
	public void endVisit(InstanceofExpression node) {
	}

	/**
	 * @see org.eclipse.jdt.core.dom.ASTVisitor#endVisit(org.eclipse.jdt.core.dom.Javadoc)
	 */
	public void endVisit(Javadoc node) {
	}

	/**
	 * @see org.eclipse.jdt.core.dom.ASTVisitor#endVisit(org.eclipse.jdt.core.dom.LabeledStatement)
	 */
	public void endVisit(LabeledStatement node) {
	}

	/**
	 * @see org.eclipse.jdt.core.dom.ASTVisitor#endVisit(org.eclipse.jdt.core.dom.MethodDeclaration)
	 */
	public void endVisit(MethodDeclaration node) {
		node.setName(node.getAST().newSimpleName("XXX")); //$NON-NLS-1$
	}

	/**
	 * @see org.eclipse.jdt.core.dom.ASTVisitor#endVisit(org.eclipse.jdt.core.dom.MethodInvocation)
	 */
	public void endVisit(MethodInvocation node) {
		add(node);
		node.setName(node.getAST().newSimpleName("XXX")); //$NON-NLS-1$
	}

	/**
	 * @see org.eclipse.jdt.core.dom.ASTVisitor#endVisit(org.eclipse.jdt.core.dom.NullLiteral)
	 */
	public void endVisit(NullLiteral node) {
	}

	/**
	 * @see org.eclipse.jdt.core.dom.ASTVisitor#endVisit(org.eclipse.jdt.core.dom.NumberLiteral)
	 */
	public void endVisit(NumberLiteral node) {
	}

	/**
	 * @see org.eclipse.jdt.core.dom.ASTVisitor#endVisit(org.eclipse.jdt.core.dom.PackageDeclaration)
	 */
	public void endVisit(PackageDeclaration node) {
		add(node);
		node.setName(node.getAST().newSimpleName("XXX")); //$NON-NLS-1$
	}

	/**
	 * @see org.eclipse.jdt.core.dom.ASTVisitor#endVisit(org.eclipse.jdt.core.dom.ParenthesizedExpression)
	 */
	public void endVisit(ParenthesizedExpression node) {
	}

	/**
	 * @see org.eclipse.jdt.core.dom.ASTVisitor#endVisit(org.eclipse.jdt.core.dom.PostfixExpression)
	 */
	public void endVisit(PostfixExpression node) {
	}

	/**
	 * @see org.eclipse.jdt.core.dom.ASTVisitor#endVisit(org.eclipse.jdt.core.dom.PrefixExpression)
	 */
	public void endVisit(PrefixExpression node) {
	}

	/**
	 * @see org.eclipse.jdt.core.dom.ASTVisitor#endVisit(org.eclipse.jdt.core.dom.PrimitiveType)
	 */
	public void endVisit(PrimitiveType node) {
	}

	/**
	 * @see org.eclipse.jdt.core.dom.ASTVisitor#endVisit(org.eclipse.jdt.core.dom.QualifiedName)
	 */
	public void endVisit(QualifiedName node) {
		add(node);
		node.setQualifier(node.getAST().newSimpleName("sss")); //$NON-NLS-1$
		node.setName(node.getAST().newSimpleName("sss")); //$NON-NLS-1$
	}

	/**
	 * @see org.eclipse.jdt.core.dom.ASTVisitor#endVisit(org.eclipse.jdt.core.dom.ReturnStatement)
	 */
	public void endVisit(ReturnStatement node) {
	}

	/**
	 * @see org.eclipse.jdt.core.dom.ASTVisitor#endVisit(org.eclipse.jdt.core.dom.SimpleName)
	 */
	public void endVisit(SimpleName node) {
		ASTNode parent = node.getParent();
		switch(parent.getNodeType()) {
			case ASTNode.CONTINUE_STATEMENT :
			case ASTNode.BREAK_STATEMENT :
			case ASTNode.LABELED_STATEMENT :
				break;
			default :
				add(node);
		}
	}

	/**
	 * @see org.eclipse.jdt.core.dom.ASTVisitor#endVisit(org.eclipse.jdt.core.dom.SimpleType)
	 */
	public void endVisit(SimpleType node) {
		node.setName(node.getAST().newSimpleName("XXX")); //$NON-NLS-1$
	}

	/**
	 * @see org.eclipse.jdt.core.dom.ASTVisitor#endVisit(org.eclipse.jdt.core.dom.SingleVariableDeclaration)
	 */
	public void endVisit(SingleVariableDeclaration node) {
		node.setName(node.getAST().newSimpleName("XXX")); //$NON-NLS-1$
	}

	/**
	 * @see org.eclipse.jdt.core.dom.ASTVisitor#endVisit(org.eclipse.jdt.core.dom.StringLiteral)
	 */
	public void endVisit(StringLiteral node) {
	}

	/**
	 * @see org.eclipse.jdt.core.dom.ASTVisitor#endVisit(org.eclipse.jdt.core.dom.SuperConstructorInvocation)
	 */
	public void endVisit(SuperConstructorInvocation node) {
	}

	/**
	 * @see org.eclipse.jdt.core.dom.ASTVisitor#endVisit(org.eclipse.jdt.core.dom.SuperFieldAccess)
	 */
	public void endVisit(SuperFieldAccess node) {
		node.setName(node.getAST().newSimpleName("XXX")); //$NON-NLS-1$
		node.setQualifier(node.getAST().newSimpleName("XXX")); //$NON-NLS-1$
	}

	/**
	 * @see org.eclipse.jdt.core.dom.ASTVisitor#endVisit(org.eclipse.jdt.core.dom.SuperMethodInvocation)
	 */
	public void endVisit(SuperMethodInvocation node) {
		node.setName(node.getAST().newSimpleName("XXX")); //$NON-NLS-1$
		node.setQualifier(node.getAST().newSimpleName("XXX")); //$NON-NLS-1$
	}

	/**
	 * @see org.eclipse.jdt.core.dom.ASTVisitor#endVisit(org.eclipse.jdt.core.dom.SwitchCase)
	 */
	public void endVisit(SwitchCase node) {
	}

	/**
	 * @see org.eclipse.jdt.core.dom.ASTVisitor#endVisit(org.eclipse.jdt.core.dom.SwitchStatement)
	 */
	public void endVisit(SwitchStatement node) {
	}

	/**
	 * @see org.eclipse.jdt.core.dom.ASTVisitor#endVisit(org.eclipse.jdt.core.dom.SynchronizedStatement)
	 */
	public void endVisit(SynchronizedStatement node) {
	}

	/**
	 * @see org.eclipse.jdt.core.dom.ASTVisitor#endVisit(org.eclipse.jdt.core.dom.ThisExpression)
	 */
	public void endVisit(ThisExpression node) {
		node.setQualifier(node.getAST().newSimpleName("XXX")); //$NON-NLS-1$
	}

	/**
	 * @see org.eclipse.jdt.core.dom.ASTVisitor#endVisit(org.eclipse.jdt.core.dom.ThrowStatement)
	 */
	public void endVisit(ThrowStatement node) {
	}

	/**
	 * @see org.eclipse.jdt.core.dom.ASTVisitor#endVisit(org.eclipse.jdt.core.dom.TryStatement)
	 */
	public void endVisit(TryStatement node) {
	}

	/**
	 * @see org.eclipse.jdt.core.dom.ASTVisitor#endVisit(org.eclipse.jdt.core.dom.TypeDeclaration)
	 */
	public void endVisit(TypeDeclaration node) {
		add(node);
		node.setName(node.getAST().newSimpleName("XXX")); //$NON-NLS-1$
		node.setSuperclass(node.getAST().newSimpleName("XXX")); //$NON-NLS-1$
		detachedListElement(node.bodyDeclarations());
	}

	/**
	 * @see org.eclipse.jdt.core.dom.ASTVisitor#endVisit(org.eclipse.jdt.core.dom.TypeDeclarationStatement)
	 */
	public void endVisit(TypeDeclarationStatement node) {
	}

	/**
	 * @see org.eclipse.jdt.core.dom.ASTVisitor#endVisit(org.eclipse.jdt.core.dom.TypeLiteral)
	 */
	public void endVisit(TypeLiteral node) {
	}

	/**
	 * @see org.eclipse.jdt.core.dom.ASTVisitor#endVisit(org.eclipse.jdt.core.dom.VariableDeclarationExpression)
	 */
	public void endVisit(VariableDeclarationExpression node) {
		detachedListElement(node.fragments());
	}

	/**
	 * @see org.eclipse.jdt.core.dom.ASTVisitor#endVisit(org.eclipse.jdt.core.dom.VariableDeclarationFragment)
	 */
	public void endVisit(VariableDeclarationFragment node) {
		add(node);
		node.setName(node.getAST().newSimpleName("XXX")); //$NON-NLS-1$
	}

	/**
	 * @see org.eclipse.jdt.core.dom.ASTVisitor#endVisit(org.eclipse.jdt.core.dom.VariableDeclarationStatement)
	 */
	public void endVisit(VariableDeclarationStatement node) {
		detachedListElement(node.fragments());
	}

	/**
	 * @see org.eclipse.jdt.core.dom.ASTVisitor#endVisit(org.eclipse.jdt.core.dom.WhileStatement)
	 */
	public void endVisit(WhileStatement node) {
	}

	/**
	 * Returns the detachedAstNodes.
	 * @return Set
	 */
	public Set getDetachedAstNodes() {
		return detachedAstNodes;
	}

}
