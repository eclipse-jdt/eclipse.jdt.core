/*******************************************************************************
 * Copyright (c) 2000, 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/

package org.eclipse.jdt.core.dom;

import java.util.List;

/**
 * Class instance creation expression AST node type.
 * For 2.0 (corresponding to JLS2):
 * <pre>
 * ClassInstanceCreation:
 *        [ Expression <b>.</b> ] <b>new</b> Name
 *            <b>(</b> [ Expression { <b>,</b> Expression } ] <b>)</b>
 *            [ AnonymousClassDeclaration ]
 * </pre>
 * For 3.0 (corresponding to JLS3), the type name is generalized to
 * a type so that parameterized types can be instantiated:
 * <pre>
 * ClassInstanceCreation:
 *        [ Expression <b>.</b> ] <b>new</b> Type
 *            <b>(</b> [ Expression { <b>,</b> Expression } ] <b>)</b>
 *            [ AnonymousClassDeclaration ]
 * </pre>
 * <p>
 * Not all node arragements will represent legal Java constructs. In particular,
 * it is nonsense if the type is a primitive type or an array type (primitive
 * types cannot be instantiated, and array creations must be represented with
 * <code>ArrayCreation</code> nodes). The normal use is when the type is a
 * simple, qualified, or parameterized type.
 * </p>
 * <p>
 * A type like "A.B" can be represented either of two ways:
 * <ol>
 * <li>
 * <code>QualifiedType(SimpleType(SimpleName("A")),SimpleName("B"))</code>
 * </li>
 * <li>
 * <code>SimpleType(QualifiedName(SimpleName("A"),SimpleName("B")))</code>
 * </li>
 * </ol>
 * The first form is preferred when "A" is known to be a type (as opposed
 * to a package). However, a parser cannot always determine this. Clients
 * should be prepared to handle either rather than make assumptions.
 * (Note also that the first form became possible as of 3.0; only the second
 * form existed in 2.0 and 2.1.)
 * </p>
 * <p>
 * Note: Support for generic types is an experimental language feature 
 * under discussion in JSR-014 and under consideration for inclusion
 * in the 1.5 release of J2SE. The support here is therefore tentative
 * and subject to change.
 * </p>
 * 
 * @since 2.0
 */
public class ClassInstanceCreation extends Expression {

	/**
	 * The optional expression; <code>null</code> for none; defaults to none.
	 */
	private Expression optionalExpression = null;
	
	/**
	 * The type name; lazily initialized; defaults to a unspecified,
	 * legal type name. Not used in 3.0.
	 */
	private Name typeName = null;
	
	/**
	 * The type; lazily initialized; defaults to a unspecified type.
	 * @since 3.0
	 */
	private Type type = null;
	
	/**
	 * The list of argument expressions (element type: 
	 * <code>Expression</code>). Defaults to an empty list.
	 */
	private ASTNode.NodeList arguments =
		new ASTNode.NodeList(true, Expression.class);
		
	/**
	 * The optional anonymous class declaration; <code>null</code> for none; 
	 * defaults to none.
	 */
	private AnonymousClassDeclaration optionalAnonymousClassDeclaration = null;
	
	/**
	 * Creates a new AST node for a class instance creation expression owned 
	 * by the given AST. By default, there is no qualifying expression,
	 * an unspecified type, an empty list of arguments,
	 * and does not declare an anonymous class.
	 * <p>
	 * N.B. This constructor is package-private; all subclasses must be 
	 * declared in the same package; clients are unable to declare 
	 * additional subclasses.
	 * </p>
	 * 
	 * @param ast the AST that is to own this node
	 */
	ClassInstanceCreation (AST ast) {
		super(ast);
	}

	/* (omit javadoc for this method)
	 * Method declared on ASTNode.
	 */
	public int getNodeType() {
		return CLASS_INSTANCE_CREATION;
	}

	/* (omit javadoc for this method)
	 * Method declared on ASTNode.
	 */
	ASTNode clone(AST target) {
		ClassInstanceCreation result = new ClassInstanceCreation(target);
		result.setSourceRange(this.getStartPosition(), this.getLength());
		result.setExpression(
			(Expression) ASTNode.copySubtree(target, getExpression()));
		if (getAST().API_LEVEL == AST.LEVEL_2_0) {
			result.setName((Name) getName().clone(target));
		}
		if (getAST().API_LEVEL >= AST.LEVEL_3_0) {
			result.setType((Type) getType().clone(target));
		}
		result.arguments().addAll(ASTNode.copySubtrees(target, arguments()));
		result.setAnonymousClassDeclaration(
			(AnonymousClassDeclaration) 
			   ASTNode.copySubtree(target, getAnonymousClassDeclaration()));
		return result;
	}

	/* (omit javadoc for this method)
	 * Method declared on ASTNode.
	 */
	public boolean subtreeMatch(ASTMatcher matcher, Object other) {
		// dispatch to correct overloaded match method
		return matcher.match(this, other);
	}

	/* (omit javadoc for this method)
	 * Method declared on ASTNode.
	 */
	void accept0(ASTVisitor visitor) {
		boolean visitChildren = visitor.visit(this);
		if (visitChildren) {
			// visit children in normal left to right reading order
			acceptChild(visitor, getExpression());
			if (getAST().API_LEVEL == AST.LEVEL_2_0) {
				acceptChild(visitor, getName());
			}
			if (getAST().API_LEVEL >= AST.LEVEL_3_0) {
				acceptChild(visitor, getType());
			}
			acceptChildren(visitor, arguments);
			acceptChild(visitor, getAnonymousClassDeclaration());
		}
		visitor.endVisit(this);
	}
	
	/**
	 * Returns the expression of this class instance creation expression, or 
	 * <code>null</code> if there is none.
	 * 
	 * @return the expression node, or <code>null</code> if there is none
	 */ 
	public Expression getExpression() {
		return optionalExpression;
	}
	
	/**
	 * Sets or clears the expression of this class instance creation expression.
	 * 
	 * @param expression the expression node, or <code>null</code> if 
	 *    there is none
	 * @exception IllegalArgumentException if:
	 * <ul>
	 * <li>the node belongs to a different AST</li>
	 * <li>the node already has a parent</li>
	 * <li>a cycle in would be created</li>
	 * </ul>
	 */ 
	public void setExpression(Expression expression) {
		// a ClassInstanceCreation may occur inside an Expression
		// must check cycles
		replaceChild(this.optionalExpression, expression, true);
		this.optionalExpression = expression;
	}

	/**
	 * Returns the name of the type instantiated in this class instance 
	 * creation expression (2.0 API only).
	 * 
	 * @return the type name node
	 * @exception UnsupportedOperationException if this operation is used in
	 * an AST later than 2.0
	 * TBD (jeem ) - deprecated In the 3.0 API, this method is replaced by <code>getType</code>,
	 * which returns a <code>Type</code> instead of a <code>Name</code>.
	 */ 
	public Name getName() {
	    supportedOnlyIn2();
		if (typeName == null) {
			// lazy initialize - use setter to ensure parent link set too
			long count = getAST().modificationCount();
			setName(new SimpleName(getAST()));
			getAST().setModificationCount(count);
		}
		return typeName;
	}
	
	/**
	 * Sets the name of the type instantiated in this class instance 
	 * creation expression (2.0 API only).
	 * 
	 * @param name the new type name
	 * @exception IllegalArgumentException if:
	 * <ul>
	 * <li>the node belongs to a different AST</li>
	 * <li>the node already has a parent</li>`
	 * </ul>
	 * @exception UnsupportedOperationException if this operation is used in
	 * an AST later than 2.0
	 * TBD (jeem ) deprecated In the 3.0 API, this method is replaced by <code>setType</code>,
	 * which expects a <code>Type</code> instead of a <code>Name</code>.
	 */ 
	public void setName(Name name) {
	    supportedOnlyIn2();
		if (name == null) {
			throw new IllegalArgumentException();
		}
		replaceChild(this.typeName, name, false);
		this.typeName = name;
	}

	/**
	 * Returns the type instantiated in this class instance creation
	 * expression (added in 3.0 API).
	 * 
	 * @return the type node
	 * @exception UnsupportedOperationException if this operation is used in
	 * a 2.0 AST
	 * @since 3.0
	 */ 
	public Type getType() {
	    unsupportedIn2();
		if (this.type == null) {
			// lazy initialize - use setter to ensure parent link set too
			long count = getAST().modificationCount();
			setType(new SimpleType(getAST()));
			getAST().setModificationCount(count);
		}
		return this.type;
	}
	
	/**
	 * Sets the type instantiated in this class instance creation
	 * expression (added in 3.0 API).
	 * 
	 * @param name the new type
	 * @exception IllegalArgumentException if:
	 * <ul>
	 * <li>the node belongs to a different AST</li>
	 * <li>the node already has a parent</li>`
	 * </ul>
	 * @exception UnsupportedOperationException if this operation is used in
	 * a 2.0 AST
	 * @since 3.0
	 */ 
	public void setType(Type type) {
	    unsupportedIn2();
		if (type == null) {
			throw new IllegalArgumentException();
		}
		replaceChild(this.type, type, false);
		this.type = type;
	}

	/**
	 * Returns the live ordered list of argument expressions in this class
	 * instance creation expression.
	 * 
	 * @return the live list of argument expressions (possibly empty)
	 *    (element type: <code>Expression</code>)
	 */ 
	public List arguments() {
		return arguments;
	}
	
	/**
	 * Returns the anonymous class declaration introduced by this
	 * class instance creation expression, if it has one.
	 * 
	 * @return the anonymous class declaration, or <code>null</code> if none
	 */ 
	public AnonymousClassDeclaration getAnonymousClassDeclaration() {
		return optionalAnonymousClassDeclaration;
	}
	
	/**
	 * Sets whether this class instance creation expression declares
	 * an anonymous class (that is, has class body declarations).
	 * 
	 * @param decl the anonymous class declaration, or <code>null</code> 
	 *    if none
	 */ 
	public void setAnonymousClassDeclaration(AnonymousClassDeclaration decl) {
		// a ClassInstanceCreation may occur inside an AnonymousClassDeclaration
		// must check cycles
		replaceChild(this.optionalAnonymousClassDeclaration, decl, true);
		this.optionalAnonymousClassDeclaration = decl;
	}

	/**
	 * Resolves and returns the binding for the constructor invoked by this
	 * expression. For anonymous classes, the binding is that of the anonymous
	 * constructor.
	 * <p>
	 * Note that bindings are generally unavailable unless requested when the
	 * AST is being built.
	 * </p>
	 * 
	 * @return the constructor binding, or <code>null</code> if the binding
	 *    cannot be resolved
	 */	
	public IMethodBinding resolveConstructorBinding() {
		return getAST().getBindingResolver().resolveConstructor(this);
	}

	/* (omit javadoc for this method)
	 * Method declared on ASTNode.
	 */
	int memSize() {
		// treat Code as free
		return BASE_NODE_SIZE + 5 * 4;
	}
	
	/* (omit javadoc for this method)
	 * Method declared on ASTNode.
	 */
	int treeSize() {
		// n.b. type == null for ast.API_LEVEL == 2.0
		// n.b. typeName == null for ast.API_LEVEL >= 3.0
		return 
			memSize()
			+ (typeName == null ? 0 : getName().treeSize())
			+ (type == null ? 0 : getType().treeSize())
			+ (optionalExpression == null ? 0 : getExpression().treeSize())
			+ arguments.listSize()
			+ (optionalAnonymousClassDeclaration == null ? 0 : getAnonymousClassDeclaration().treeSize());
	}
}

