/*******************************************************************************
 * Copyright (c) 2004 IBM Corporation and others.
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
 * Annotation type member declaration AST node type (added in 3.0 API).
 * <pre>
 * AnnotationTypeMemberDeclaration:
 *   [ Javadoc ] { ExtendedModifier }
 *       Type Identifier <b>(</b> <b>)</b> [ <b>default</b> Expression ] <b>;</b>
 * </pre>
 * <p>
 * Note that annotation type member declarations are only meaningful as
 * elements of {@link AnnotationTypeDeclaration#members AnnotationTypeDeclaration.members}.
 * </p>
 * <p>
 * When a Javadoc comment is present, the source
 * range begins with the first character of the "/**" comment delimiter.
 * When there is no Javadoc comment, the source range begins with the first
 * character of the first modifier keyword (if modifiers), 
 * or the first character of the member type (no modifiers). 
 * The source range extends through the last character of the
 * ";" token.
 * </p>
 * <p>
 * Note: Support for annotation metadata is an experimental language feature 
 * under discussion in JSR-175 and under consideration for inclusion
 * in the 1.5 release of J2SE. The support here is therefore tentative
 * and subject to change.
 * </p>
 * 
 * @since 3.0
 */
public class AnnotationTypeMemberDeclaration extends BodyDeclaration {
	
	/**
	 * The member name; lazily initialized; defaults to an unspecified,
	 * legal Java identifier.
	 */
	private SimpleName memberName = null;

	/**
	 * The member type; lazily initialized; defaults to int.
	 */
	private Type memberType = null;
	
	/**
	 * The optional default expression; <code>null</code> for none; defaults to none.
	 */
	private Expression optionalDefaultValue = null;
	
	/**
	 * Creates a new AST node for an annotation type member declaration owned 
	 * by the given AST. By default, the declaration is for a member of an
	 * unspecified, but legal, name; no modifiers; no javadoc;
	 * an unspecified value type; and no default value.
	 * <p>
	 * N.B. This constructor is package-private; all subclasses must be 
	 * declared in the same package; clients are unable to declare 
	 * additional subclasses.
	 * </p>
	 * 
	 * @param ast the AST that is to own this node
	 */
	AnnotationTypeMemberDeclaration(AST ast) {
		super(ast);
	}

	/* (omit javadoc for this method)
	 * Method declared on ASTNode.
	 */
	public int getNodeType() {
		return ANNOTATION_TYPE_MEMBER_DECLARATION;
	}

	/* (omit javadoc for this method)
	 * Method declared on ASTNode.
	 */
	ASTNode clone(AST target) {
		AnnotationTypeMemberDeclaration result = new AnnotationTypeMemberDeclaration(target);
		result.setSourceRange(this.getStartPosition(), this.getLength());
		result.setJavadoc(
			(Javadoc) ASTNode.copySubtree(target, getJavadoc()));
		result.modifiers().addAll(ASTNode.copySubtrees(target, modifiers()));
		result.setType((Type) ASTNode.copySubtree(target, getType()));
		result.setName((SimpleName) getName().clone(target));
		result.setDefault((Expression) ASTNode.copySubtree(target, getDefault()));
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
			acceptChild(visitor, getJavadoc());
			acceptChildren(visitor, this.modifiers);
			acceptChild(visitor, getType());
			acceptChild(visitor, getName());
			acceptChild(visitor, getDefault());
		}
		visitor.endVisit(this);
	}
	
	/**
	 * Returns the name of the annotation type member declared in this declaration.
	 * 
	 * @return the member name node
	 */ 
	public SimpleName getName() {
		if (this.memberName == null) {
			// lazy initialize - use setter to ensure parent link set too
			long count = getAST().modificationCount();
			setName(new SimpleName(getAST()));
			getAST().setModificationCount(count);
		}
		return this.memberName;
	}
	
	/**
	 * Sets the name of the annotation type member declared in this declaration to the
	 * given name.
	 * 
	 * @param memberName the new member name
	 * @exception IllegalArgumentException if:
	 * <ul>
	 * <li>the node belongs to a different AST</li>
	 * <li>the node already has a parent</li>
	 * </ul>
	 */ 
	public void setName(SimpleName memberName) {
		if (memberName == null) {
			throw new IllegalArgumentException();
		}
		replaceChild(this.memberName, memberName, false);
		this.memberName = memberName;
	}

	/**
	 * Returns the type of the annotation type member declared in this 
	 * declaration.
	 * 
	 * @return the type of the member
	 */ 
	public Type getType() {
		if (this.memberType == null) {
			// lazy initialize - use setter to ensure parent link set too
			long count = getAST().modificationCount();
			setType(getAST().newPrimitiveType(PrimitiveType.INT));
			getAST().setModificationCount(count);
		}
		return this.memberType;
	}

	/**
	 * Sets the type of the annotation type member declared in this declaration
	 * to the given type.
	 * 
	 * @param type the new member type
	 * @exception IllegalArgumentException if:
	 * <ul>
	 * <li>the node belongs to a different AST</li>
	 * <li>the node already has a parent</li>
	 * </ul>
	 */ 
	public void setType(Type type) {
		if (type == null) {
			throw new IllegalArgumentException();
		}
		replaceChild(this.memberType, type, false);
		this.memberType = type;
	}

	/**
	 * Returns the default value of this annotation type member, or 
	 * <code>null</code> if there is none.
	 * 
	 * @return the expression node, or <code>null</code> if there is none
	 */ 
	public Expression getDefault() {
		return this.optionalDefaultValue;
	}
	
	/**
	 * Sets or clears the default value of this annotation type member.
	 * 
	 * @param defaultValue the expression node, or <code>null</code> if 
	 *    there is none
	 * @exception IllegalArgumentException if:
	 * <ul>
	 * <li>the node belongs to a different AST</li>
	 * <li>the node already has a parent</li>
	 * <li>a cycle in would be created</li>
	 * </ul>
	 */ 
	public void setDefault(Expression defaultValue) {
		// a AnnotationTypeMemberDeclaration may occur inside an Expression - must check cycles
		replaceChild(this.optionalDefaultValue, defaultValue, true);
		this.optionalDefaultValue = defaultValue;
	}
	
	/**
	 * Resolves and returns the binding for the annotation type member declared
	 * in this declaration.
	 * <p>
	 * Note that bindings are generally unavailable unless requested when the
	 * AST is being built.
	 * </p>
	 * 
	 * @return the binding, or <code>null</code> if the binding cannot be 
	 *    resolved
	 */	
	public IVariableBinding resolveBinding() {
		return getAST().getBindingResolver().resolveMember(this);
	}

	/* (omit javadoc for this method)
	 * Method declared on ASTNode.
	 */
	int memSize() {
		return super.memSize() + 3 * 4;
	}
	
	/* (omit javadoc for this method)
	 * Method declared on ASTNode.
	 */
	int treeSize() {
		return
			memSize()
			+ (this.optionalDocComment == null ? 0 : getJavadoc().treeSize())
			+ this.modifiers.listSize()
			+ (this.memberName == null ? 0 : getName().treeSize())
			+ (this.memberType == null ? 0 : getType().treeSize())
			+ (this.optionalDefaultValue == null ? 0 : getDefault().treeSize());
	}
}

