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

import java.util.Iterator;
import java.util.List;

/**
 * Method declaration AST node type. A method declaration
 * is the union of a method declaration and a constructor declaration.
 * For 2.0 (corresponding to JLS2):
 * <pre>
 * MethodDeclaration:
 *    [ Javadoc ] { Modifier } ( Type | <b>void</b> ) Identifier <b>(</b>
 *        [ FormalParameter 
 * 		     { <b>,</b> FormalParameter } ] <b>)</b> {<b>[</b> <b>]</b> }
 *        [ <b>throws</b> TypeName { <b>,</b> TypeName } ] ( Block | <b>;</b> )
 * ConstructorDeclaration:
 *    [ Javadoc ] { Modifier } Identifier <b>(</b>
 * 		  [ FormalParameter
 * 			 { <b>,</b> FormalParameter } ] <b>)</b>
 *        [<b>throws</b> TypeName { <b>,</b> TypeName } ] Block
 * </pre>
 * For 3.0 (corresponding to JLS3), type parameters and reified modifiers
 * (and annotations) were added:
 * <pre>
 * MethodDeclaration:
 *    [ Javadoc ] { ExtendedModifier }
 *		  [ <b>&lt;</b> TypeParameter { <b>,</b> TypeParameter } <b>&gt;</b> ]
 *        ( Type | <b>void</b> ) Identifier <b>(</b>
 *        [ FormalParameter 
 * 		     { <b>,</b> FormalParameter } ] <b>)</b> {<b>[</b> <b>]</b> }
 *        [ <b>throws</b> TypeName { <b>,</b> TypeName } ] ( Block | <b>;</b> )
 * ConstructorDeclaration:
 *    [ Javadoc ] { ExtendedModifier } Identifier <b>(</b>
 * 		  [ FormalParameter
 * 			 { <b>,</b> FormalParameter } ] <b>)</b>
 *        [<b>throws</b> TypeName { <b>,</b> TypeName } ] Block
 * </pre>
 * <p>
 * When a Javadoc comment is present, the source
 * range begins with the first character of the "/**" comment delimiter.
 * When there is no Javadoc comment, the source range begins with the first
 * character of the first modifier keyword (if modifiers), or the
 * first character of the "&lt;" token (method, no modifiers, type parameters), 
 * or the first character of the return type (method, no modifiers, no type
 * parameters), or the first character of the identifier (constructor, 
 * no modifiers). The source range extends through the last character of the
 * ";" token (if no body), or the last character of the block (if body).
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
public class MethodDeclaration extends BodyDeclaration {
	
	/**
	 * <code>true</code> for a constructor, <code>false</code> for a method.
	 * Defaults to method.
	 */
	private boolean isConstructor = false;
	
	/**
	 * The method name; lazily initialized; defaults to an unspecified,
	 * legal Java identifier.
	 */
	private SimpleName methodName = null;

	/**
	 * The parameter declarations 
	 * (element type: <code>SingleVariableDeclaration</code>).
	 * Defaults to an empty list.
	 */
	private ASTNode.NodeList parameters =
		new ASTNode.NodeList(true, SingleVariableDeclaration.class);
	
	/**
	 * The return type.
	 * 2.0 bahevior: lazily initialized; defaults to void.
	 * 3.0 behavior; lazily initialized; defaults to void; null allowed.
	 * Note that this field is ignored for constructor declarations.
	 */
	private Type returnType = null;
	
	/**
	 * Indicated whether the return type has been initialized.
	 * @sicne 3.0
	 */
	private boolean returnType2Initialized = false;
	
	/**
	 * The type paramters (element type: <code>TypeParameter</code>). 
	 * Null in 2.0. Added in 3.0; defaults to an empty list
	 * (see constructor).
	 * @since 3.0
	 */
	private ASTNode.NodeList typeParameters = null;

	/**
	 * The number of array dimensions that appear after the parameters, rather
	 * than after the return type itself; defaults to 0.
	 * 
	 * @since 2.1
	 */
	private int extraArrayDimensions = 0;

	/**
	 * The list of thrown exception names (element type: <code>Name</code>).
	 * Defaults to an empty list.
	 */
	private ASTNode.NodeList thrownExceptions =
		new ASTNode.NodeList(false, Name.class);

	/**
	 * The method body, or <code>null</code> if none.
	 * Defaults to none.
	 */
	private Block optionalBody = null;
	
	/**
	 * Creates a new AST node for a method declaration owned 
	 * by the given AST. By default, the declaration is for a method of an
	 * unspecified, but legal, name; no modifiers; no javadoc; no type 
	 * parameters; void return type; no parameters; no array dimensions after 
	 * the parameters; no thrown exceptions; and no body (as opposed to an
	 * empty body).
	 * <p>
	 * N.B. This constructor is package-private; all subclasses must be 
	 * declared in the same package; clients are unable to declare 
	 * additional subclasses.
	 * </p>
	 * 
	 * @param ast the AST that is to own this node
	 */
	MethodDeclaration(AST ast) {
		super(ast);
		if (ast.API_LEVEL >= AST.LEVEL_3_0) {
			this.modifiers = new ASTNode.NodeList(false, Modifier.class);
			this.typeParameters = new ASTNode.NodeList(false, TypeParameter.class);
		}
	}

	/* (omit javadoc for this method)
	 * Method declared on ASTNode.
	 */
	public int getNodeType() {
		return METHOD_DECLARATION;
	}

	/* (omit javadoc for this method)
	 * Method declared on ASTNode.
	 */
	ASTNode clone(AST target) {
		MethodDeclaration result = new MethodDeclaration(target);
		result.setSourceRange(this.getStartPosition(), this.getLength());
		result.setJavadoc(
			(Javadoc) ASTNode.copySubtree(target, getJavadoc()));
		if (getAST().API_LEVEL == AST.LEVEL_2_0) {
			result.setModifiers(getModifiers());
			result.setReturnType(
					(Type) ASTNode.copySubtree(target, getReturnType()));
		}
		if (getAST().API_LEVEL >= AST.LEVEL_3_0) {
			result.modifiers().addAll(ASTNode.copySubtrees(target, modifiers()));
			result.typeParameters().addAll(
					ASTNode.copySubtrees(target, typeParameters()));
			result.setReturnType2(
					(Type) ASTNode.copySubtree(target, getReturnType2()));
		}
		result.setConstructor(isConstructor());
		result.setExtraDimensions(getExtraDimensions());
		result.setName((SimpleName) getName().clone(target));
		result.parameters().addAll(
			ASTNode.copySubtrees(target, parameters()));
		result.thrownExceptions().addAll(
			ASTNode.copySubtrees(target, thrownExceptions()));
		result.setBody(
			(Block) ASTNode.copySubtree(target, getBody()));
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
			if (getAST().API_LEVEL == AST.LEVEL_2_0) {
				acceptChild(visitor, getReturnType());
			} else {
				acceptChildren(visitor, this.modifiers);
				acceptChildren(visitor, this.typeParameters);
				acceptChild(visitor, getReturnType2());
			}
			// n.b. visit return type even for constructors
			acceptChild(visitor, getName());
			acceptChildren(visitor, this.parameters);
			acceptChildren(visitor, this.thrownExceptions);
			acceptChild(visitor, getBody());
		}
		visitor.endVisit(this);
	}
	
	/**
	 * Returns whether this declaration declares a constructor or a method.
	 * 
	 * @return <code>true</code> if this is a constructor declaration,
	 *    and <code>false</code> if this is a method declaration
	 */ 
	public boolean isConstructor() {
		return this.isConstructor;
	}
	
	/**
	 * Sets whether this declaration declares a constructor or a method.
	 * 
	 * @param isConstructor <code>true</code> for a constructor declaration,
	 *    and <code>false</code> for a method declaration
	 */ 
	public void setConstructor(boolean isConstructor) {
		modifying();
		this.isConstructor = isConstructor;
	}

	/**
	 * Returns the live ordered list of type parameters of this method
	 * declaration (added in 3.0 API). This list is non-empty for parameterized methods.
	 * <p>
	 * Note that these children are not relevant for constructor declarations
	 * (although it does still figure in subtree equality comparisons
	 * and visits), and is devoid of the binding information ordinarily
	 * available.
	 * </p>
	 * <p>
	 * Note: Support for generic types is an experimental language feature 
	 * under discussion in JSR-014 and under consideration for inclusion
	 * in the 1.5 release of J2SE. The support here is therefore tentative
	 * and subject to change.
	 * </p>
	 * 
	 * @return the live list of type parameters
	 *    (element type: <code>TypeParameter</code>)
	 * @exception UnsupportedOperationException if this operation is used in
	 * a 2.0 AST
	 * @since 3.0
	 */ 
	public List typeParameters() {
		// more efficient than just calling unsupportedIn2() to check
		if (this.typeParameters == null) {
			unsupportedIn2();
		}
		return this.typeParameters;
	}
	
	/**
	 * Returns the name of the method declared in this method declaration.
	 * For a constructor declaration, this should be the same as the name 
	 * of the class.
	 * 
	 * @return the method name node
	 */ 
	public SimpleName getName() {
		if (this.methodName == null) {
			// lazy initialize - use setter to ensure parent link set too
			long count = getAST().modificationCount();
			setName(new SimpleName(getAST()));
			getAST().setModificationCount(count);
		}
		return this.methodName;
	}
	
	/**
	 * Sets the name of the method declared in this method declaration to the
	 * given name. For a constructor declaration, this should be the same as 
	 * the name of the class.
	 * 
	 * @param methodName the new method name
	 * @exception IllegalArgumentException if:
	 * <ul>
	 * <li>the node belongs to a different AST</li>
	 * <li>the node already has a parent</li>
	 * </ul>
	 */ 
	public void setName(SimpleName methodName) {
		if (methodName == null) {
			throw new IllegalArgumentException();
		}
		replaceChild(this.methodName, methodName, false);
		this.methodName = methodName;
	}

	/**
	 * Returns the live ordered list of method parameter declarations for this
	 * method declaration.
	 * 
	 * @return the live list of method parameter declarations
	 *    (element type: <code>SingleVariableDeclaration</code>)
	 */ 
	public List parameters() {
		return this.parameters;
	}
	
	/**
	 * Returns whether this method declaration declares a
	 * variable arity method (added in 3.0 API). The convenience method checks
	 * whether the last parameter is so marked.
	 * <p>
	 * Note: Varible arity methods are an experimental language feature 
	 * under discussion in JSR-201 and under consideration for inclusion
	 * in the 1.5 release of J2SE. The support here is therefore tentative
	 * and subject to change.
	 * </p>
	 * 
	 * @return <code>true</code> if this is a variable arity method declaration,
	 *    and <code>false</code> otherwise
	 * @exception UnsupportedOperationException if this operation is used in
	 * a 2.0 AST
	 * @see SingleVariableDeclaration#isVariableArity()
	 * @since 3.0
	 */ 
	public boolean isVariableArity() {
		// more efficient than just calling unsupportedIn2() to check
		if (this.modifiers == null) {
			unsupportedIn2();
		}
		if (parameters().isEmpty()) {
			return false;
		} else {
			SingleVariableDeclaration v = (SingleVariableDeclaration) parameters().get(parameters().size() - 1);
			return v.isVariableArity();
		}
	}
	
	/**
	 * Returns the live ordered list of thrown exception names in this method 
	 * declaration.
	 * 
	 * @return the live list of exception names
	 *    (element type: <code>Name</code>)
	 */ 
	public List thrownExceptions() {
		return this.thrownExceptions;
	}
	
	/**
	 * Returns the return type of the method declared in this method 
	 * declaration, exclusive of any extra array dimensions (2.0 API only). 
	 * This is one of the few places where the void type is meaningful.
	 * <p>
	 * Note that this child is not relevant for constructor declarations
	 * (although, it does still figure in subtree equality comparisons
	 * and visits), and is devoid of the binding information ordinarily
	 * available.
	 * </p>
	 * 
	 * @return the return type, possibly the void primitive type
	 * @exception UnsupportedOperationException if this operation is used in
	 * an AST later than 2.0
	 * TBD (jeem ) - deprecated In the 3.0 API, this method is replaced by
	 * <code>getReturnType2</code>, which may return <code>null</code>.
	 */ 
	public Type getReturnType() {
	    supportedOnlyIn2();
		if (this.returnType == null) {
			// lazy initialize - use setter to ensure parent link set too
			long count = getAST().modificationCount();
			setReturnType(getAST().newPrimitiveType(PrimitiveType.VOID));
			getAST().setModificationCount(count);
		}
		return this.returnType;
	}

	/**
	 * Sets the return type of the method declared in this method declaration
	 * to the given type, exclusive of any extra array dimensions (2.0 API only). This is one
	 * of the few places where the void type is meaningful.
	 * <p>
	 * Note that this child is not relevant for constructor declarations
	 * (although it does still figure in subtree equality comparisons and visits).
	 * </p>
	 * 
	 * @param type the new return type, possibly the void primitive type
	 * @exception IllegalArgumentException if:
	 * <ul>
	 * <li>the node belongs to a different AST</li>
	 * <li>the node already has a parent</li>
	 * </ul>
	 * @exception UnsupportedOperationException if this operation is used in
	 * an AST later than 2.0
	 * TBD (jeem ) - deprecated In the 3.0 API, this method is replaced by
	 * <code>setReturnType2</code>, which accepts <code>null</code>.
	 */ 
	public void setReturnType(Type type) {
	    supportedOnlyIn2();
		if (type == null) {
			throw new IllegalArgumentException();
		}
		replaceChild(this.returnType, type, false);
		this.returnType = type;
	}

	/**
	 * Returns the return type of the method declared in this method 
	 * declaration, exclusive of any extra array dimensions (added in 3.0 API). 
	 * This is one of the few places where the void type is meaningful.
	 * <p>
	 * Note that this child is not relevant for constructor declarations
	 * (although, if present, it does still figure in subtree equality comparisons
	 * and visits), and is devoid of the binding information ordinarily
	 * available. In the 2.0 API, the return type is mandatory. 
	 * In the 3.0 API, the return type is optional.
	 * </p>
	 * 
	 * @return the return type, possibly the void primitive type,
	 * or <code>null</code> if none
	 * @exception UnsupportedOperationException if this operation is used in
	 * a 2.0 AST
	 * @since 3.0
	 */ 
	public Type getReturnType2() {
	    unsupportedIn2();
		if (this.returnType == null && !returnType2Initialized) {
			// lazy initialize - use setter to ensure parent link set too
			long count = getAST().modificationCount();
			setReturnType2(getAST().newPrimitiveType(PrimitiveType.VOID));
			getAST().setModificationCount(count);
		}
		return this.returnType;
	}

	/**
	 * Sets the return type of the method declared in this method declaration
	 * to the given type, exclusive of any extra array dimensions (added in 3.0 API).
	 * This is one of the few places where the void type is meaningful.
	 * <p>
	 * Note that this child is not relevant for constructor declarations
	 * (although it does still figure in subtree equality comparisons and visits).
	 * In the 2.0 API, the return type is mandatory. 
	 * In the 3.0 API, the return type is optional.
	 * </p>
	 * 
	 * @param type the new return type, possibly the void primitive type,
	 * or <code>null</code> if none
	 * @exception UnsupportedOperationException if this operation is used in
	 * a 2.0 AST
	 * @exception IllegalArgumentException if:
	 * <ul>
	 * <li>the node belongs to a different AST</li>
	 * <li>the node already has a parent</li>
	 * </ul>
	 * @since 3.0
	 */ 
	public void setReturnType2(Type type) {
	    unsupportedIn2();
		returnType2Initialized = true;
		replaceChild(this.returnType, type, false);
		this.returnType = type;
	}

	/**
	 * Returns the number of extra array dimensions over and above the 
	 * explicitly-specified return type.
	 * <p>
	 * For example, <code>int foo()[][]</code> has a return type of 
	 * <code>int</code> and two extra array dimensions; 
	 * <code>int[][] foo()</code> has a return type of <code>int[][]</code>
	 * and zero extra array dimensions. The two constructs have different
	 * ASTs, even though there are really syntactic variants of the same
	 * method declaration.
	 * </p>
	 * 
	 * @return the number of extra array dimensions
	 * @since 2.1
	 */ 
	public int getExtraDimensions() {
		return this.extraArrayDimensions;
	}

	/**
	 * Sets the number of extra array dimensions over and above the 
	 * explicitly-specified return type.
	 * <p>
	 * For example, <code>int foo()[][]</code> is rendered as a return
	 * type of <code>int</code> with two extra array dimensions; 
	 * <code>int[][] foo()</code> is rendered as a return type of 
	 * <code>int[][]</code> with zero extra array dimensions. The two
	 * constructs have different ASTs, even though there are really syntactic
	 * variants of the same method declaration.
	 * </p>
	 * 
	 * @param dimensions the number of array dimensions
	 * @exception IllegalArgumentException if the number of dimensions is
	 *    negative
	 * @since 2.1
	 */ 
	public void setExtraDimensions(int dimensions) {
		if (dimensions < 0) {
			throw new IllegalArgumentException();
		}
		modifying();
		this.extraArrayDimensions = dimensions;
	}

	/**
	 * Returns the body of this method declaration, or <code>null</code> if 
	 * this method has <b>no</b> body.
	 * <p>
	 * Note that there is a subtle difference between having no body and having
	 * an empty body ("{}").
	 * </p>
	 * 
	 * @return the method body, or <code>null</code> if this method has no
	 *    body
	 */ 
	public Block getBody() {
		return this.optionalBody;
	}

	/**
	 * Sets or clears the body of this method declaration.
	 * <p>
	 * Note that there is a subtle difference between having no body 
	 * (as in <code>"void foo();"</code>) and having an empty body (as in
	 * "void foo() {}"). Abstract methods, and methods declared in interfaces,
	 * have no body. Non-abstract methods, and all constructors, have a body.
	 * </p>
	 * 
	 * @param body the block node, or <code>null</code> if 
	 *    there is none
	 * @exception IllegalArgumentException if:
	 * <ul>
	 * <li>the node belongs to a different AST</li>
	 * <li>the node already has a parent</li>
	 * <li>a cycle in would be created</li>
	 * </ul>
	 */ 
	public void setBody(Block body) {
		// a MethodDeclaration may occur in a Block - must check cycles
		replaceChild(this.optionalBody, body, true);
		this.optionalBody = body;
	}

	/**
	 * Resolves and returns the binding for the method or constructor declared
	 * in this method or constructor declaration.
	 * <p>
	 * Note that bindings are generally unavailable unless requested when the
	 * AST is being built.
	 * </p>
	 * 
	 * @return the binding, or <code>null</code> if the binding cannot be 
	 *    resolved
	 */	
	public IMethodBinding resolveBinding() {
		return getAST().getBindingResolver().resolveMethod(this);
	}

	/* (omit javadoc for this method)
	 * Method declared on ASTNode.
	 */
	void appendDebugString(StringBuffer buffer) {
		buffer.append("MethodDeclaration[");//$NON-NLS-1$
		buffer.append(isConstructor() ? "constructor " : "method ");//$NON-NLS-2$//$NON-NLS-1$
		buffer.append(getName().getIdentifier());
		buffer.append("(");//$NON-NLS-1$
		for (Iterator it = parameters().iterator(); it.hasNext(); ) {
			SingleVariableDeclaration d = (SingleVariableDeclaration) it.next();
			d.getType().appendPrintString(buffer);
			if (it.hasNext()) {
				buffer.append(";");//$NON-NLS-1$
			}
		}
		buffer.append(")");//$NON-NLS-1$
		if (!isConstructor()) {
			buffer.append(" returns ");//$NON-NLS-1$
			getReturnType().appendPrintString(buffer);
		}
		buffer.append("]");//$NON-NLS-1$
	}

	/* (omit javadoc for this method)
	 * Method declared on ASTNode.
	 */
	int memSize() {
		return super.memSize() + 9 * 4;
	}
	
	/* (omit javadoc for this method)
	 * Method declared on ASTNode.
	 */
	int treeSize() {
		return
			memSize()
			+ (this.optionalDocComment == null ? 0 : getJavadoc().treeSize())
			+ (this.modifiers == null ? 0 : this.modifiers.listSize())
			+ (this.typeParameters == null ? 0 : this.typeParameters.listSize())
			+ (this.methodName == null ? 0 : getName().treeSize())
			+ (this.returnType == null ? 0 : this.returnType.treeSize())
			+ this.parameters.listSize()
			+ this.thrownExceptions.listSize()
			+ (this.optionalBody == null ? 0 : getBody().treeSize());
	}
}

