/*******************************************************************************
 * Copyright (c) 2003 IBM Corporation and others.
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
 * Type node for a qualified type (added in 3.0 API).
 * <pre>
 * QualifiedType:
 *    Type <b>.</b> SimpleName
 * </pre>
 * <p>
 * Not all node arragements will represent legal Java constructs. In particular,
 * it is nonsense if the type is an array type or primitive type. The normal use
 * is when the type is a simple or parameterized type.
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
 * The first form is preferred when "A" is known to be a type. However, a 
 * parser cannot always determine this. Clients should be prepared to handle
 * either rather than make assumptions. (Note also that the first form
 * became possible as of 3.0; only the second form existed in 2.0 and 2.1.)
 * </p>
 * <p>
 * Note: Support for generic types is an experimental language feature 
 * under discussion in JSR-014 and under consideration for inclusion
 * in the 1.5 release of J2SE. The support here is therefore tentative
 * and subject to change.
 * </p>
 * 
 * @since 3.0
 */
public class QualifiedType extends Type {
	/** 
	 * The type node; lazily initialized; defaults to a type with
	 * an unspecfied, but legal, simple name.
	 */
	private Type qualifier = null;
	
	/**
	 * The name being qualified; lazily initialized; defaults to a unspecified,
	 * legal Java identifier.
	 */
	private SimpleName name = null;

	/**
	 * Creates a new unparented node for a qualified type owned by the
	 * given AST. By default, an unspecified, but legal, qualifier and name.
	 * <p>
	 * N.B. This constructor is package-private.
	 * </p>
	 * 
	 * @param ast the AST that is to own this node
	 */
	QualifiedType(AST ast) {
		super(ast);
	}

	/* (omit javadoc for this method)
	 * Method declared on ASTNode.
	 */
	public int getNodeType() {
		return QUALIFIED_TYPE;
	}

	/* (omit javadoc for this method)
	 * Method declared on ASTNode.
	 */
	ASTNode clone(AST target) {
		QualifiedType result = new QualifiedType(target);
		result.setSourceRange(this.getStartPosition(), this.getLength());
		result.setQualifier((Type) ((ASTNode) getQualifier()).clone(target));
		result.setName((SimpleName) ((ASTNode) getName()).clone(target));
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
			acceptChild(visitor, getQualifier());
			acceptChild(visitor, getName());
		}
		visitor.endVisit(this);
	}
	
	/**
	 * Returns the qualifier of this qualified type.
	 * 
	 * @return the qualifier of this qualified type
	 */ 
	public Type getQualifier() {
		if (this.qualifier == null) {
			// lazy initialize - use setter to ensure parent link set too
			long count = getAST().modificationCount();
			setQualifier(new SimpleType(getAST()));
			getAST().setModificationCount(count);
		}
		return this.qualifier;
	}
	
	/**
	 * Sets the qualifier of this qualified type to the given type.
	 * 
	 * @param type the new qualifier of this qualified type
	 * @exception IllegalArgumentException if:
	 * <ul>
	 * <li>the node belongs to a different AST</li>
	 * <li>the node already has a parent</li>
	 * </ul>
	 */ 
	public void setQualifier(Type type) {
		if (type == null) {
			throw new IllegalArgumentException();
		}
		replaceChild(this.qualifier, type, true);
		this.qualifier = type;
	}

	/**
	 * Returns the name part of this qualified type.
	 * 
	 * @return the name being qualified 
	 */ 
	public SimpleName getName() {
		if (this.name == null) {
			// lazy initialize - use setter to ensure parent link set too
			long count = getAST().modificationCount();
			setName(new SimpleName(getAST()));
			getAST().setModificationCount(count);
		}
		return this.name;
	}
	
	/**
	 * Sets the name part of this qualified type to the given simple name.
	 * 
	 * @param name the identifier of this qualified name
	 * @exception IllegalArgumentException if:
	 * <ul>
	 * <li>the node belongs to a different AST</li>
	 * <li>the node already has a parent</li>
	 * </ul>
	 */ 
	public void setName(SimpleName name) {
		if (name == null) {
			throw new IllegalArgumentException();
		}
		replaceChild(this.name, name, false);
		this.name = name;
	}
	
	/* (omit javadoc for this method)
	 * Method declared on ASTNode.
	 */
	int memSize() {
		// treat Code as free
		return BASE_NODE_SIZE + 2 * 4;
	}
	
	/* (omit javadoc for this method)
	 * Method declared on ASTNode.
	 */
	int treeSize() {
		return 
			memSize()
			+ (qualifier == null ? 0 : getQualifier().treeSize())
			+ (name == null ? 0 : getName().treeSize());
	}
}

