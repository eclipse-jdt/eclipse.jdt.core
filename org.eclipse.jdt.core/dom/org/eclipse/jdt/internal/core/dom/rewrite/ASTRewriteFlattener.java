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
package org.eclipse.jdt.internal.core.dom.rewrite;

import java.util.List;

import org.eclipse.jface.text.Assert;

import org.eclipse.jdt.core.dom.*;

public class ASTRewriteFlattener extends GenericVisitor {

	public static String asString(ASTNode node, RewriteEventStore store) {
		ASTRewriteFlattener flattener= new ASTRewriteFlattener(store);
		node.accept(flattener);
		return flattener.getResult();
	}
	
	protected StringBuffer result;
	private RewriteEventStore store;

	public ASTRewriteFlattener(RewriteEventStore store) {
		this.store= store;
		this.result= new StringBuffer();
	}
	
	/**
	 * Returns the string accumulated in the visit.
	 *
	 * @return the serialized 
	 */
	public String getResult() {
		// convert to a string, but lose any extra space in the string buffer by copying
		return new String(this.result.toString());
	}
	
	/**
	 * Resets this printer so that it can be used again.
	 */
	public void reset() {
		this.result.setLength(0);
	}

	protected boolean visitNode(ASTNode node) {
		Assert.isTrue(false, "No implementation to flatten node: " + node.toString());  //$NON-NLS-1$
		return false;
	}
	
	/**
	 * Appends the text representation of the given modifier flags, followed by a single space.
	 * 
	 * @param modifiers the modifiers
	 * @param buf The <code>StringBuffer</code> to write the result to.
	 */
	public static void printModifiers(int modifiers, StringBuffer buf) {
		if (Modifier.isPublic(modifiers)) {
			buf.append("public "); //$NON-NLS-1$
		}
		if (Modifier.isProtected(modifiers)) {
			buf.append("protected "); //$NON-NLS-1$
		}
		if (Modifier.isPrivate(modifiers)) {
			buf.append("private "); //$NON-NLS-1$
		}
		if (Modifier.isStatic(modifiers)) {
			buf.append("static "); //$NON-NLS-1$
		}
		if (Modifier.isAbstract(modifiers)) {
			buf.append("abstract "); //$NON-NLS-1$
		}
		if (Modifier.isFinal(modifiers)) {
			buf.append("final "); //$NON-NLS-1$
		}
		if (Modifier.isSynchronized(modifiers)) {
			buf.append("synchronized "); //$NON-NLS-1$
		}
		if (Modifier.isVolatile(modifiers)) {
			buf.append("volatile "); //$NON-NLS-1$
		}
		if (Modifier.isNative(modifiers)) {
			buf.append("native "); //$NON-NLS-1$
		}
		if (Modifier.isStrictfp(modifiers)) {
			buf.append("strictfp "); //$NON-NLS-1$
		}
		if (Modifier.isTransient(modifiers)) {
			buf.append("transient "); //$NON-NLS-1$
		}
	}
	
	protected List getChildList(ASTNode parent, StructuralPropertyDescriptor childProperty) {
		return (List) getAttribute(parent, childProperty);
	}
	
	protected ASTNode getChildNode(ASTNode parent, StructuralPropertyDescriptor childProperty) {
		return (ASTNode) getAttribute(parent, childProperty);
	}
	
	protected int getIntAttribute(ASTNode parent, StructuralPropertyDescriptor childProperty) {
		return ((Integer) getAttribute(parent, childProperty)).intValue();
	}
	
	protected boolean getBooleanAttribute(ASTNode parent, StructuralPropertyDescriptor childProperty) {
		return ((Boolean) getAttribute(parent, childProperty)).booleanValue();
	}
	
	protected Object getAttribute(ASTNode parent, StructuralPropertyDescriptor childProperty) {
		return this.store.getNewValue(parent, childProperty);
	}
	
	protected void visitList(ASTNode parent, StructuralPropertyDescriptor childProperty, String separator) {
		List list= getChildList(parent, childProperty);
		for (int i= 0; i < list.size(); i++) {
			if (separator != null && i > 0) {
				this.result.append(separator);
			}
			((ASTNode) list.get(i)).accept(this);
		}
	}
	
	protected void visitList(ASTNode parent, StructuralPropertyDescriptor childProperty, String separator, String lead) {
		List list= getChildList(parent, childProperty);
		if (!list.isEmpty()) {
			this.result.append(lead);
			for (int i= 0; i < list.size(); i++) {
				if (separator != null && i > 0) {
					this.result.append(separator);
				}
				((ASTNode) list.get(i)).accept(this);
			}
		}
	}
	
	
	/*
	 * @see ASTVisitor#visit(AnonymousClassDeclaration)
	 */
	public boolean visit(AnonymousClassDeclaration node) {
		this.result.append('{');
		visitList(node, AnonymousClassDeclaration.BODY_DECLARATIONS_PROPERTY, null);
		this.result.append('}');
		return false;
	}

	/*
	 * @see ASTVisitor#visit(ArrayAccess)
	 */
	public boolean visit(ArrayAccess node) {
		getChildNode(node, ArrayAccess.ARRAY_PROPERTY).accept(this);
		this.result.append('[');
		getChildNode(node, ArrayAccess.INDEX_PROPERTY).accept(this);
		this.result.append(']');
		return false;
	}

	/*
	 * @see ASTVisitor#visit(ArrayCreation)
	 */
	public boolean visit(ArrayCreation node) {
		this.result.append("new "); //$NON-NLS-1$
		ArrayType arrayType= (ArrayType) getChildNode(node, ArrayCreation.TYPE_PROPERTY);
		
		// get the element type and count dimensions
		Type elementType= (Type) getChildNode(arrayType, ArrayType.COMPONENT_TYPE_PROPERTY);
		int dimensions= 1; // always include this array type
		while (elementType.isArrayType()) {
			dimensions++;
			elementType = (Type) getChildNode(elementType, ArrayType.COMPONENT_TYPE_PROPERTY);
		}
		
		elementType.accept(this);
		
		List list= getChildList(node, ArrayCreation.DIMENSIONS_PROPERTY);
		for (int i= 0; i < list.size(); i++) {
			this.result.append('[');
			((ASTNode) list.get(i)).accept(this);
			this.result.append(']');
			dimensions--;
		}
		
		// add empty "[]" for each extra array dimension
		for (int i= 0; i < dimensions; i++) {
			this.result.append("[]"); //$NON-NLS-1$
		}
		ASTNode initializer= getChildNode(node, ArrayCreation.INITIALIZER_PROPERTY);
		if (initializer != null) {
			getChildNode(node, ArrayCreation.INITIALIZER_PROPERTY).accept(this);
		}
		return false;
	}
	
	/*
	 * @see ASTVisitor#visit(ArrayInitializer)
	 */
	public boolean visit(ArrayInitializer node) {
		this.result.append('{');
		visitList(node, ArrayInitializer.EXPRESSIONS_PROPERTY, String.valueOf(','));
		this.result.append('}');
		return false;
	}

	/*
	 * @see ASTVisitor#visit(ArrayType)
	 */
	public boolean visit(ArrayType node) {
		getChildNode(node, ArrayType.COMPONENT_TYPE_PROPERTY).accept(this);
		this.result.append("[]"); //$NON-NLS-1$
		return false;
	}

	/*
	 * @see ASTVisitor#visit(AssertStatement)
	 */
	public boolean visit(AssertStatement node) {
		this.result.append("assert "); //$NON-NLS-1$
		getChildNode(node, AssertStatement.EXPRESSION_PROPERTY).accept(this);
		
		ASTNode message= getChildNode(node, AssertStatement.MESSAGE_PROPERTY);
		if (message != null) {
			this.result.append(':');
			message.accept(this);
		}
		this.result.append(';');
		return false;
	}

	/*
	 * @see ASTVisitor#visit(Assignment)
	 */
	public boolean visit(Assignment node) {
		getChildNode(node, Assignment.LEFT_HAND_SIDE_PROPERTY).accept(this);
		this.result.append(getAttribute(node, Assignment.OPERATOR_PROPERTY).toString());
		getChildNode(node, Assignment.RIGHT_HAND_SIDE_PROPERTY).accept(this);
		return false;
	}



	/*
	 * @see ASTVisitor#visit(Block)
	 */
	public boolean visit(Block node) {
		this.result.append('{');
		visitList(node, Block.STATEMENTS_PROPERTY, null);
		this.result.append('}');
		return false;
	}

	/*
	 * @see ASTVisitor#visit(BooleanLiteral)
	 */
	public boolean visit(BooleanLiteral node) {
		if (node.booleanValue() == true) {
			this.result.append("true"); //$NON-NLS-1$
		} else {
			this.result.append("false"); //$NON-NLS-1$
		}
		return false;
	}

	/*
	 * @see ASTVisitor#visit(BreakStatement)
	 */
	public boolean visit(BreakStatement node) {
		this.result.append("break"); //$NON-NLS-1$
		ASTNode label= getChildNode(node, BreakStatement.LABEL_PROPERTY);
		if (label != null) {
			this.result.append(' ');
			label.accept(this);
		}
		this.result.append(';');
		return false;
	}

	/*
	 * @see ASTVisitor#visit(CastExpression)
	 */
	public boolean visit(CastExpression node) {
		this.result.append('(');
		getChildNode(node, CastExpression.TYPE_PROPERTY).accept(this);
		this.result.append(')');
		getChildNode(node, CastExpression.EXPRESSION_PROPERTY).accept(this);
		return false;
	}

	/*
	 * @see ASTVisitor#visit(CatchClause)
	 */
	public boolean visit(CatchClause node) {
		this.result.append("catch ("); //$NON-NLS-1$
		getChildNode(node, CatchClause.EXCEPTION_PROPERTY).accept(this);
		this.result.append(')');
		getChildNode(node, CatchClause.BODY_PROPERTY).accept(this);
		return false;
	}

	/*
	 * @see ASTVisitor#visit(CharacterLiteral)
	 */
	public boolean visit(CharacterLiteral node) {
		this.result.append(getAttribute(node, CharacterLiteral.ESCAPED_VALUE_PROPERTY));
		return false;
	}

	/*
	 * @see ASTVisitor#visit(ClassInstanceCreation)
	 */
	public boolean visit(ClassInstanceCreation node) {
		ASTNode expression= getChildNode(node, ClassInstanceCreation.EXPRESSION_PROPERTY);
		if (expression != null) {
			expression.accept(this);
			this.result.append('.');
		}
		this.result.append("new "); //$NON-NLS-1$
		getChildNode(node, ClassInstanceCreation.NAME_PROPERTY).accept(this);
		this.result.append('(');
		visitList(node, ClassInstanceCreation.ARGUMENTS_PROPERTY, String.valueOf(','));
		this.result.append(')');
		ASTNode decl= getChildNode(node, ClassInstanceCreation.ANONYMOUS_CLASS_DECLARATION_PROPERTY);
		if (decl != null) {
			decl.accept(this);
		}
		return false;
	}

	/*
	 * @see ASTVisitor#visit(CompilationUnit)
	 */
	public boolean visit(CompilationUnit node) {
		ASTNode pack= getChildNode(node, CompilationUnit.PACKAGE_PROPERTY);
		if (pack != null) {
			pack.accept(this);
		}
		visitList(node, CompilationUnit.IMPORTS_PROPERTY, null);
		visitList(node, CompilationUnit.TYPES_PROPERTY, null);
		return false;
	}

	/*
	 * @see ASTVisitor#visit(ConditionalExpression)
	 */
	public boolean visit(ConditionalExpression node) {
		getChildNode(node, ConditionalExpression.EXPRESSION_PROPERTY).accept(this);
		this.result.append('?');
		getChildNode(node, ConditionalExpression.THEN_EXPRESSION_PROPERTY).accept(this);
		this.result.append(':');
		getChildNode(node, ConditionalExpression.ELSE_EXPRESSION_PROPERTY).accept(this);
		return false;
	}

	/*
	 * @see ASTVisitor#visit(ConstructorInvocation)
	 */
	public boolean visit(ConstructorInvocation node) {
		this.result.append("this("); //$NON-NLS-1$
		visitList(node, ConstructorInvocation.ARGUMENTS_PROPERTY, String.valueOf(','));
		this.result.append(");"); //$NON-NLS-1$
		return false;
	}

	/*
	 * @see ASTVisitor#visit(ContinueStatement)
	 */
	public boolean visit(ContinueStatement node) {
		this.result.append("continue"); //$NON-NLS-1$
		ASTNode label= getChildNode(node, ContinueStatement.LABEL_PROPERTY);
		if (label != null) {
			this.result.append(' ');
			label.accept(this);
		}
		this.result.append(';');
		return false;
	}

	/*
	 * @see ASTVisitor#visit(DoStatement)
	 */
	public boolean visit(DoStatement node) {
		this.result.append("do "); //$NON-NLS-1$
		getChildNode(node, DoStatement.BODY_PROPERTY).accept(this);
		this.result.append(" while ("); //$NON-NLS-1$
		getChildNode(node, DoStatement.EXPRESSION_PROPERTY).accept(this);
		this.result.append(");"); //$NON-NLS-1$
		return false;
	}

	/*
	 * @see ASTVisitor#visit(EmptyStatement)
	 */
	public boolean visit(EmptyStatement node) {
		this.result.append(';');
		return false;
	}

	/*
	 * @see ASTVisitor#visit(ExpressionStatement)
	 */
	public boolean visit(ExpressionStatement node) {
		getChildNode(node, ExpressionStatement.EXPRESSION_PROPERTY).accept(this);
		this.result.append(';');
		return false;
	}

	/*
	 * @see ASTVisitor#visit(FieldAccess)
	 */
	public boolean visit(FieldAccess node) {
		getChildNode(node, FieldAccess.EXPRESSION_PROPERTY).accept(this);
		this.result.append('.');
		getChildNode(node, FieldAccess.NAME_PROPERTY).accept(this);
		return false;
	}

	/*
	 * @see ASTVisitor#visit(FieldDeclaration)
	 */
	public boolean visit(FieldDeclaration node) {
		ASTNode javadoc= getChildNode(node, FieldDeclaration.JAVADOC_PROPERTY);
		if (javadoc != null) {
			javadoc.accept(this);
		}
		printModifiers(getIntAttribute(node, FieldDeclaration.MODIFIERS_PROPERTY), this.result);
		getChildNode(node, FieldDeclaration.TYPE_PROPERTY).accept(this);
		this.result.append(' ');
		visitList(node, FieldDeclaration.FRAGMENTS_PROPERTY, String.valueOf(','));
		this.result.append(';');
		return false;
	}

	/*
	 * @see ASTVisitor#visit(ForStatement)
	 */
	public boolean visit(ForStatement node) {
		this.result.append("for ("); //$NON-NLS-1$
		visitList(node, ForStatement.INITIALIZERS_PROPERTY, null);
		this.result.append(';');
		ASTNode expression= getChildNode(node, ForStatement.EXPRESSION_PROPERTY);
		if (expression != null) {
			expression.accept(this);
		}
		this.result.append(';');
		visitList(node, ForStatement.UPDATERS_PROPERTY, null);
		this.result.append(')');
		getChildNode(node, ForStatement.BODY_PROPERTY).accept(this);
		return false;
	}

	/*
	 * @see ASTVisitor#visit(IfStatement)
	 */
	public boolean visit(IfStatement node) {
		this.result.append("if ("); //$NON-NLS-1$
		getChildNode(node, IfStatement.EXPRESSION_PROPERTY).accept(this);
		this.result.append(')');
		getChildNode(node, IfStatement.THEN_STATEMENT_PROPERTY).accept(this);
		ASTNode elseStatement= getChildNode(node, IfStatement.ELSE_STATEMENT_PROPERTY);
		if (elseStatement != null) {
			this.result.append(" else "); //$NON-NLS-1$
			elseStatement.accept(this);
		}
		return false;
	}

	/*
	 * @see ASTVisitor#visit(ImportDeclaration)
	 */
	public boolean visit(ImportDeclaration node) {
		this.result.append("import "); //$NON-NLS-1$
		getChildNode(node, ImportDeclaration.NAME_PROPERTY).accept(this);
		if (getBooleanAttribute(node, ImportDeclaration.ON_DEMAND_PROPERTY)) {
			this.result.append(".*"); //$NON-NLS-1$
		}
		this.result.append(';');
		return false;
	}



	/*
	 * @see ASTVisitor#visit(InfixExpression)
	 */
	public boolean visit(InfixExpression node) {
		getChildNode(node, InfixExpression.LEFT_OPERAND_PROPERTY).accept(this);
		this.result.append(' ');
		String operator= getAttribute(node, InfixExpression.OPERATOR_PROPERTY).toString();
		
		this.result.append(operator);
		this.result.append(' ');
		getChildNode(node, InfixExpression.RIGHT_OPERAND_PROPERTY).accept(this);
		
		List list= getChildList(node, InfixExpression.EXTENDED_OPERANDS_PROPERTY);
		for (int i= 0; i < list.size(); i++) {
			this.result.append(operator);
			((ASTNode) list.get(i)).accept(this);
		}
		return false;
	}

	/*
	 * @see ASTVisitor#visit(InstanceofExpression)
	 */
	public boolean visit(InstanceofExpression node) {
		getChildNode(node, InstanceofExpression.LEFT_OPERAND_PROPERTY).accept(this);
		this.result.append(" instanceof "); //$NON-NLS-1$
		getChildNode(node, InstanceofExpression.RIGHT_OPERAND_PROPERTY).accept(this);
		return false;
	}

	/*
	 * @see ASTVisitor#visit(Initializer)
	 */
	public boolean visit(Initializer node) {
		ASTNode javadoc= getChildNode(node, Initializer.JAVADOC_PROPERTY);
		if (javadoc != null) {
			javadoc.accept(this);
		}
		printModifiers(getIntAttribute(node, Initializer.MODIFIERS_PROPERTY), this.result);
		getChildNode(node, Initializer.BODY_PROPERTY).accept(this);
		return false;
	}

	/*
	 * @see ASTVisitor#visit(Javadoc)
	 */
	public boolean visit(Javadoc node) {
		this.result.append("/**"); //$NON-NLS-1$
		List list= getChildList(node, Javadoc.TAGS_PROPERTY);
		for (int i= 0; i < list.size(); i++) {
			this.result.append("\n * "); //$NON-NLS-1$
			((ASTNode) list.get(i)).accept(this);
		}
		this.result.append("\n */"); //$NON-NLS-1$
		return false;
	}

	/*
	 * @see ASTVisitor#visit(LabeledStatement)
	 */
	public boolean visit(LabeledStatement node) {
		getChildNode(node, LabeledStatement.LABEL_PROPERTY).accept(this);
		this.result.append(": "); //$NON-NLS-1$
		getChildNode(node, LabeledStatement.BODY_PROPERTY).accept(this);
		return false;
	}

	/*
	 * @see ASTVisitor#visit(MethodDeclaration)
	 */
	public boolean visit(MethodDeclaration node) {
		ASTNode javadoc= getChildNode(node, MethodDeclaration.JAVADOC_PROPERTY);
		if (javadoc != null) {
			javadoc.accept(this);
		}
		printModifiers(getIntAttribute(node, MethodDeclaration.MODIFIERS_PROPERTY), this.result);
		if (!getBooleanAttribute(node, MethodDeclaration.CONSTRUCTOR_PROPERTY)) {
			getChildNode(node, MethodDeclaration.RETURN_TYPE_PROPERTY).accept(this);
			this.result.append(' ');
		}
		getChildNode(node, MethodDeclaration.NAME_PROPERTY).accept(this);
		this.result.append('(');
		visitList(node, MethodDeclaration.PARAMETERS_PROPERTY, String.valueOf(','));
		this.result.append(')');
		int extraDims= getIntAttribute(node, MethodDeclaration.EXTRA_DIMENSIONS_PROPERTY);
		for (int i = 0; i < extraDims; i++) {
			this.result.append("[]"); //$NON-NLS-1$
		}		
		visitList(node, MethodDeclaration.THROWN_EXCEPTIONS_PROPERTY, String.valueOf(','), " throws "); //$NON-NLS-1$
		ASTNode body= getChildNode(node, MethodDeclaration.BODY_PROPERTY);
		if (body == null) {
			this.result.append(';');
		} else {
			body.accept(this);
		}
		return false;
	}

	/*
	 * @see ASTVisitor#visit(MethodInvocation)
	 */
	public boolean visit(MethodInvocation node) {
		ASTNode expression= getChildNode(node, MethodInvocation.EXPRESSION_PROPERTY);
		if (expression != null) {
			expression.accept(this);
			this.result.append('.');
		}
		getChildNode(node, MethodInvocation.NAME_PROPERTY).accept(this);
		this.result.append('(');
		visitList(node, MethodInvocation.ARGUMENTS_PROPERTY, String.valueOf(','));
		this.result.append(')');
		return false;
	}

	/*
	 * @see ASTVisitor#visit(NullLiteral)
	 */
	public boolean visit(NullLiteral node) {
		this.result.append("null"); //$NON-NLS-1$
		return false;
	}

	/*
	 * @see ASTVisitor#visit(NumberLiteral)
	 */
	public boolean visit(NumberLiteral node) {
		this.result.append(getAttribute(node, NumberLiteral.TOKEN_PROPERTY).toString());
		return false;
	}

	/*
	 * @see ASTVisitor#visit(PackageDeclaration)
	 */
	public boolean visit(PackageDeclaration node) {
		this.result.append("package "); //$NON-NLS-1$
		getChildNode(node, PackageDeclaration.NAME_PROPERTY).accept(this);
		this.result.append(';');
		return false;
	}

	/*
	 * @see ASTVisitor#visit(ParenthesizedExpression)
	 */
	public boolean visit(ParenthesizedExpression node) {
		this.result.append('(');
		getChildNode(node, ParenthesizedExpression.EXPRESSION_PROPERTY).accept(this);
		this.result.append(')');
		return false;
	}

	/*
	 * @see ASTVisitor#visit(PostfixExpression)
	 */
	public boolean visit(PostfixExpression node) {
		getChildNode(node, PostfixExpression.OPERAND_PROPERTY).accept(this);
		this.result.append(getAttribute(node, PostfixExpression.OPERATOR_PROPERTY).toString());
		return false;
	}

	/*
	 * @see ASTVisitor#visit(PrefixExpression)
	 */
	public boolean visit(PrefixExpression node) {
		this.result.append(getAttribute(node, PrefixExpression.OPERATOR_PROPERTY).toString());
		getChildNode(node, PrefixExpression.OPERAND_PROPERTY).accept(this);
		return false;
	}

	/*
	 * @see ASTVisitor#visit(PrimitiveType)
	 */
	public boolean visit(PrimitiveType node) {
		this.result.append(getAttribute(node, PrimitiveType.PRIMITIVE_TYPE_CODE_PROPERTY).toString());
		return false;
	}

	/*
	 * @see ASTVisitor#visit(QualifiedName)
	 */
	public boolean visit(QualifiedName node) {
		getChildNode(node, QualifiedName.QUALIFIER_PROPERTY).accept(this);
		this.result.append('.');
		getChildNode(node, QualifiedName.NAME_PROPERTY).accept(this);
		return false;
	}

	/*
	 * @see ASTVisitor#visit(ReturnStatement)
	 */
	public boolean visit(ReturnStatement node) {
		this.result.append("return"); //$NON-NLS-1$
		ASTNode expression= getChildNode(node, ReturnStatement.EXPRESSION_PROPERTY);
		if (expression != null) {
			this.result.append(' ');
			expression.accept(this);
		}
		this.result.append(';');
		return false;
	}

	/*
	 * @see ASTVisitor#visit(SimpleName)
	 */
	public boolean visit(SimpleName node) {
		this.result.append(getAttribute(node, SimpleName.IDENTIFIER_PROPERTY));
		return false;
	}

	/*
	 * @see ASTVisitor#visit(SimpleType)
	 */
	public boolean visit(SimpleType node) {
		return true;
	}

	/*
	 * @see ASTVisitor#visit(SingleVariableDeclaration)
	 */
	public boolean visit(SingleVariableDeclaration node) {
		printModifiers(getIntAttribute(node, SingleVariableDeclaration.MODIFIERS_PROPERTY), this.result);
		getChildNode(node, SingleVariableDeclaration.TYPE_PROPERTY).accept(this);
		this.result.append(' ');
		getChildNode(node, SingleVariableDeclaration.NAME_PROPERTY).accept(this);
		int extraDimensions= getIntAttribute(node, SingleVariableDeclaration.EXTRA_DIMENSIONS_PROPERTY);
		for (int i = 0; i < extraDimensions; i++) {
			this.result.append("[]"); //$NON-NLS-1$
		}			
		ASTNode initializer= getChildNode(node, SingleVariableDeclaration.INITIALIZER_PROPERTY);
		if (initializer != null) {
			this.result.append('=');
			initializer.accept(this);
		}
		return false;
	}

	/*
	 * @see ASTVisitor#visit(StringLiteral)
	 */
	public boolean visit(StringLiteral node) {
		this.result.append(getAttribute(node, StringLiteral.ESCAPED_VALUE_PROPERTY));
		return false;
	}

	/*
	 * @see ASTVisitor#visit(SuperConstructorInvocation)
	 */
	public boolean visit(SuperConstructorInvocation node) {
		ASTNode expression= getChildNode(node, SuperConstructorInvocation.EXPRESSION_PROPERTY);
		if (expression != null) {
			expression.accept(this);
			this.result.append('.');
		}
		this.result.append("super("); //$NON-NLS-1$
		visitList(node, SuperConstructorInvocation.ARGUMENTS_PROPERTY, String.valueOf(','));
		this.result.append(");"); //$NON-NLS-1$
		return false;
	}

	/*
	 * @see ASTVisitor#visit(SuperFieldAccess)
	 */
	public boolean visit(SuperFieldAccess node) {
		ASTNode qualifier= getChildNode(node, SuperFieldAccess.QUALIFIER_PROPERTY);
		if (qualifier != null) {
			qualifier.accept(this);
			this.result.append('.');
		}
		this.result.append("super."); //$NON-NLS-1$
		getChildNode(node, SuperFieldAccess.NAME_PROPERTY).accept(this);
		return false;
	}

	/*
	 * @see ASTVisitor#visit(SuperMethodInvocation)
	 */
	public boolean visit(SuperMethodInvocation node) {
		ASTNode qualifier= getChildNode(node, SuperMethodInvocation.QUALIFIER_PROPERTY);
		if (qualifier != null) {
			qualifier.accept(this);
			this.result.append('.');
		}
		this.result.append("super."); //$NON-NLS-1$
		getChildNode(node, SuperMethodInvocation.NAME_PROPERTY).accept(this);
		this.result.append('(');
		visitList(node, SuperMethodInvocation.ARGUMENTS_PROPERTY, String.valueOf(','));
		this.result.append(')');
		return false;
	}

	/*
	 * @see ASTVisitor#visit(SwitchCase)
	 */
	public boolean visit(SwitchCase node) {
		ASTNode expression= getChildNode(node, SwitchCase.EXPRESSION_PROPERTY);
		if (expression == null) {
			this.result.append("default"); //$NON-NLS-1$
		} else {
			this.result.append("case "); //$NON-NLS-1$
			expression.accept(this);
		}
		this.result.append(':');
		return false;
	}

	/*
	 * @see ASTVisitor#visit(SwitchStatement)
	 */
	public boolean visit(SwitchStatement node) {
		this.result.append("switch ("); //$NON-NLS-1$
		getChildNode(node, SwitchStatement.EXPRESSION_PROPERTY).accept(this);
		this.result.append(')');
		this.result.append('{');
		visitList(node, SwitchStatement.STATEMENTS_PROPERTY, null);
		this.result.append('}');
		return false;
	}

	/*
	 * @see ASTVisitor#visit(SynchronizedStatement)
	 */
	public boolean visit(SynchronizedStatement node) {
		this.result.append("synchronized ("); //$NON-NLS-1$
		getChildNode(node, SynchronizedStatement.EXPRESSION_PROPERTY).accept(this);
		this.result.append(')');
		getChildNode(node, SynchronizedStatement.BODY_PROPERTY).accept(this);
		return false;
	}

	/*
	 * @see ASTVisitor#visit(ThisExpression)
	 */
	public boolean visit(ThisExpression node) {
		ASTNode qualifier= getChildNode(node, ThisExpression.QUALIFIER_PROPERTY);
		if (qualifier != null) {
			qualifier.accept(this);
			this.result.append('.');
		}
		this.result.append("this"); //$NON-NLS-1$
		return false;
	}

	/*
	 * @see ASTVisitor#visit(ThrowStatement)
	 */
	public boolean visit(ThrowStatement node) {
		this.result.append("throw "); //$NON-NLS-1$
		getChildNode(node, ThrowStatement.EXPRESSION_PROPERTY).accept(this);
		this.result.append(';');
		return false;
	}

	/*
	 * @see ASTVisitor#visit(TryStatement)
	 */
	public boolean visit(TryStatement node) {
		this.result.append("try "); //$NON-NLS-1$
		getChildNode(node, TryStatement.BODY_PROPERTY).accept(this);
		this.result.append(' ');
		visitList(node, TryStatement.CATCH_CLAUSES_PROPERTY, null);
		ASTNode finallyClause= getChildNode(node, TryStatement.FINALLY_PROPERTY);
		if (finallyClause != null) {
			this.result.append(" finally "); //$NON-NLS-1$
			finallyClause.accept(this);
		}
		return false;
	}

	/*
	 * @see ASTVisitor#visit(TypeDeclaration)
	 */
	public boolean visit(TypeDeclaration node) {
		ASTNode javadoc= getChildNode(node, TypeDeclaration.JAVADOC_PROPERTY);
		if (javadoc != null) {
			javadoc.accept(this);
		}
		printModifiers(getIntAttribute(node, TypeDeclaration.MODIFIERS_PROPERTY), this.result);
		
		boolean isInterface= getBooleanAttribute(node, TypeDeclaration.INTERFACE_PROPERTY);
		this.result.append(isInterface ? "interface " : "class "); //$NON-NLS-1$ //$NON-NLS-2$
		getChildNode(node, TypeDeclaration.NAME_PROPERTY).accept(this);
		this.result.append(' ');
		ASTNode superclass= getChildNode(node, TypeDeclaration.SUPERCLASS_PROPERTY);
		if (superclass != null) {
			this.result.append("extends "); //$NON-NLS-1$
			superclass.accept(this);
			this.result.append(' ');
		}
		
		String lead= isInterface ? "extends " : "implements ";  //$NON-NLS-1$//$NON-NLS-2$
		visitList(node, TypeDeclaration.SUPER_INTERFACES_PROPERTY, String.valueOf(','), lead);
		this.result.append('{');
		visitList(node, TypeDeclaration.BODY_DECLARATIONS_PROPERTY, null);
		this.result.append('}');
		return false;
	}

	/*
	 * @see ASTVisitor#visit(TypeDeclarationStatement)
	 */
	public boolean visit(TypeDeclarationStatement node) {
		getChildNode(node, TypeDeclarationStatement.TYPE_DECLARATION_PROPERTY).accept(this);
		return false;
	}

	/*
	 * @see ASTVisitor#visit(TypeLiteral)
	 */
	public boolean visit(TypeLiteral node) {
		getChildNode(node, TypeLiteral.TYPE_PROPERTY).accept(this);
		this.result.append(".class"); //$NON-NLS-1$
		return false;
	}

	/*
	 * @see ASTVisitor#visit(VariableDeclarationExpression)
	 */
	public boolean visit(VariableDeclarationExpression node) {
		printModifiers(getIntAttribute(node, VariableDeclarationExpression.MODIFIERS_PROPERTY), this.result);
		getChildNode(node, VariableDeclarationExpression.TYPE_PROPERTY).accept(this);
		this.result.append(' ');
		visitList(node, VariableDeclarationExpression.FRAGMENTS_PROPERTY, String.valueOf(','));
		return false;
	}

	/*
	 * @see ASTVisitor#visit(VariableDeclarationFragment)
	 */
	public boolean visit(VariableDeclarationFragment node) {
		getChildNode(node, VariableDeclarationFragment.NAME_PROPERTY).accept(this);
		int extraDimensions= getIntAttribute(node, VariableDeclarationFragment.EXTRA_DIMENSIONS_PROPERTY);
		for (int i = 0; i < extraDimensions; i++) {
			this.result.append("[]"); //$NON-NLS-1$
		}
		ASTNode initializer= getChildNode(node, VariableDeclarationFragment.INITIALIZER_PROPERTY);
		if (initializer != null) {
			this.result.append('=');
			initializer.accept(this);
		}
		return false;
	}

	/*
	 * @see ASTVisitor#visit(VariableDeclarationStatement)
	 */
	public boolean visit(VariableDeclarationStatement node) {
		printModifiers(getIntAttribute(node, VariableDeclarationStatement.MODIFIERS_PROPERTY), this.result);
		getChildNode(node, VariableDeclarationStatement.TYPE_PROPERTY).accept(this);
		this.result.append(' ');
		visitList(node, VariableDeclarationStatement.FRAGMENTS_PROPERTY, String.valueOf(','));
		this.result.append(';');
		return false;
	}

	/*
	 * @see ASTVisitor#visit(WhileStatement)
	 */
	public boolean visit(WhileStatement node) {
		this.result.append("while ("); //$NON-NLS-1$
		getChildNode(node, WhileStatement.EXPRESSION_PROPERTY).accept(this);
		this.result.append(')');
		getChildNode(node, WhileStatement.BODY_PROPERTY).accept(this);
		return false;
	}
	
	
	/* (non-Javadoc)
	 * @see org.eclipse.jdt.core.dom.ASTVisitor#visit(org.eclipse.jdt.core.dom.BlockComment)
	 */
	public boolean visit(BlockComment node) {
		return false; // cant flatten, needs source
	}
	/* (non-Javadoc)
	 * @see org.eclipse.jdt.core.dom.ASTVisitor#visit(org.eclipse.jdt.core.dom.LineComment)
	 */
	public boolean visit(LineComment node) {
		return false; // cant flatten, needs source
	}
	/* (non-Javadoc)
	 * @see org.eclipse.jdt.core.dom.ASTVisitor#visit(org.eclipse.jdt.core.dom.MemberRef)
	 */
	public boolean visit(MemberRef node) {
		ASTNode qualifier= getChildNode(node, MemberRef.QUALIFIER_PROPERTY);
		if (qualifier != null) {
			qualifier.accept(this);
		}
		this.result.append('#');
		getChildNode(node, MemberRef.NAME_PROPERTY).accept(this);
		return false;
	}
	/* (non-Javadoc)
	 * @see org.eclipse.jdt.core.dom.ASTVisitor#visit(org.eclipse.jdt.core.dom.MethodRef)
	 */
	public boolean visit(MethodRef node) {
		ASTNode qualifier= getChildNode(node, MethodRef.QUALIFIER_PROPERTY);
		if (qualifier != null) {
			qualifier.accept(this);
		}
		this.result.append('#');
		getChildNode(node, MethodRef.NAME_PROPERTY).accept(this);
		this.result.append('(');
		visitList(node, MethodRef.PARAMETERS_PROPERTY, ","); //$NON-NLS-1$
		this.result.append(')');
		return false;
	}
	/* (non-Javadoc)
	 * @see org.eclipse.jdt.core.dom.ASTVisitor#visit(org.eclipse.jdt.core.dom.MethodRefParameter)
	 */
	public boolean visit(MethodRefParameter node) {
		getChildNode(node, MethodRefParameter.TYPE_PROPERTY).accept(this);
		ASTNode name= getChildNode(node, MethodRefParameter.NAME_PROPERTY);
		if (name != null) {
			this.result.append(' ');
			name.accept(this);
		}
		return false;
	}
	/* (non-Javadoc)
	 * @see org.eclipse.jdt.core.dom.ASTVisitor#visit(org.eclipse.jdt.core.dom.TagElement)
	 */
	public boolean visit(TagElement node) {
		Object tagName= getAttribute(node, TagElement.TAG_NAME_PROPERTY);
		if (tagName != null) {
			this.result.append((String) tagName);
		}
		List list= getChildList(node, TagElement.FRAGMENTS_PROPERTY);
		for (int i= 0; i < list.size(); i++) {
			if (i > 0 || tagName != null) {
				this.result.append(' ');
			}
			ASTNode curr= (ASTNode) list.get(i);
			if (curr instanceof TagElement) {
				this.result.append('{');
				curr.accept(this);
				this.result.append('}');
			} else {
				curr.accept(this);
			}
		}
		return false;
	}
	/* (non-Javadoc)
	 * @see org.eclipse.jdt.core.dom.ASTVisitor#visit(org.eclipse.jdt.core.dom.TextElement)
	 */
	public boolean visit(TextElement node) {
		this.result.append(getAttribute(node, TextElement.TEXT_PROPERTY));
		return false;
	}
}
