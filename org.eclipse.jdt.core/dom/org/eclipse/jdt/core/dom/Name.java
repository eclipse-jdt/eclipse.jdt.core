/*******************************************************************************
 * Copyright (c) 2000, 2012 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * This is an implementation of an early-draft specification developed under the Java
 * Community Process (JCP) and is made available for testing and evaluation purposes
 * only. The code is not compatible with any specification of the JCP.
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/

package org.eclipse.jdt.core.dom;

import java.util.List;

/**
 * Abstract base class for all AST nodes that represent names.
 * There are exactly two kinds of name: simple ones
 * (<code>SimpleName</code>) and qualified ones (<code>QualifiedName</code>).
 * <p>
 * For JLS2, JLS3 or JLS4:
 * <pre>
 * Name:
 *     SimpleName
 *     QualifiedName
 * </pre>
 * <p>
 * For JLS8, the Name may be preceded by optional type annotations.
 * While the additional locations where Java SE8 allows annotations
 * to be written are only in type references and type parameters, there
 * are various situations in which a parser is unable to decide whether
 * a name it sees constitutes a type name or not and ends up creating
 * Name nodes.
 * </p>
 * <p>
 * For example, given the qualified type reference java.util.List,
 * the ASTParser cannot decide when bindings resolution is NOT requested
 * via {@link ASTParser#setResolveBindings}, whether java constitutes a
 * type name or a non-type name (i.e., a package name)
 * </p>
 * <p>
 * Note also that the parser cannot disambiguate on the basis of the
 * presence of type annotations at a certain place. 
 * In @NonNull java.util.List, java is still a package name even though
 * it is preceded by annotations.
 * </p>
 * For JLS8:
 * <pre>
 * Name:
 *     {Annotation} SimpleName
 *     {Annotation} QualifiedName
 * </pre>
 * </p>
 * @since 2.0
 * @noextend This class is not intended to be subclassed by clients.
 */
public abstract class Name extends Expression implements IDocElement {

	/**
	 * Approximate base size of an expression node instance in bytes,
	 * including object header and instance fields.
	 */
	static final int BASE_NAME_NODE_SIZE = BASE_NODE_SIZE + 2 * 4;

	/**
	 * This index represents the position inside a qualified name.
	 */
	int index;
	
	/**
	 * The type annotations (element type: {@link Annotation}).
	 * @since 3.9
	 */
	protected ASTNode.NodeList annotations = null;

	/**
	 * Creates a new AST node for a name owned by the given AST.
	 * <p>
	 * N.B. This constructor is package-private.
	 * </p>
	 *
	 * @param ast the AST that is to own this node
	 */
	Name(AST ast) {
		super(ast);
	}

	/**
	 * Returns whether this name is a simple name
	 * (<code>SimpleName</code>).
	 *
	 * @return <code>true</code> if this is a simple name, and
	 *    <code>false</code> otherwise
	 */
	public final boolean isSimpleName() {
		return (this instanceof SimpleName);
	}

	/**
	 * Returns whether this name is a qualified name
	 * (<code>QualifiedName</code>).
	 *
	 * @return <code>true</code> if this is a qualified name, and
	 *    <code>false</code> otherwise
	 */
	public final boolean isQualifiedName() {
		return (this instanceof QualifiedName);
	}

	/**
	 * Resolves and returns the binding for the entity referred to by this name.
	 * <p>
	 * Note that bindings are generally unavailable unless requested when the
	 * AST is being built.
	 * </p>
	 *
	 * @return the binding, or <code>null</code> if the binding cannot be
	 *    resolved
	 */
	public final IBinding resolveBinding() {
		return this.ast.getBindingResolver().resolveName(this);
	}

	/**
	 * Returns the standard dot-separated representation of this name.
	 * If the name is a simple name, the result is the name's identifier.
	 * If the name is a qualified name, the result is the name of the qualifier
	 * (as computed by this method) followed by "." followed by the name's
	 * identifier.
	 *
	 * @return the fully qualified name
	 * @since 3.0
	 */
	public final String getFullyQualifiedName() {
		if (isSimpleName()) {
			// avoid creating garbage for common case
			return ((SimpleName) this).getIdentifier();
		} else {
			StringBuffer buffer = new StringBuffer(50);
			appendName(buffer);
			return new String(buffer);
		}
	}

	/**
	 * Appends the standard representation of this name to the given string
	 * buffer.
	 *
	 * @param buffer the buffer
	 * @since 3.0
	 */
	abstract void appendName(StringBuffer buffer);
	
	/**
	 * Returns the live ordered list of annotations for this Name node.
	 *
	 * @return the live list of annotations (element type: {@link Annotation})
	 * @exception UnsupportedOperationException if this operation is used
	 *            in a JLS2, JLS3 or JLS4 AST
	 * @since 3.9
	 */
	public List annotations() {
		if (this.annotations == null) {
			unsupportedIn2_3_4(); 
		}
		return this.annotations;
	}
}
