/*******************************************************************************
 * Copyright (c) 2002 International Business Machines Corp. and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v0.5 
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v05.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 ******************************************************************************/

package org.eclipse.jdt.core.dom;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.jdt.internal.compiler.ast.AbstractMethodDeclaration;
import org.eclipse.jdt.internal.compiler.ast.AbstractVariableDeclaration;
import org.eclipse.jdt.internal.compiler.ast.AllocationExpression;
import org.eclipse.jdt.internal.compiler.ast.ArrayAllocationExpression;
import org.eclipse.jdt.internal.compiler.ast.ArrayReference;
import org.eclipse.jdt.internal.compiler.ast.AstNode;
import org.eclipse.jdt.internal.compiler.ast.BinaryExpression;
import org.eclipse.jdt.internal.compiler.ast.CharLiteral;
import org.eclipse.jdt.internal.compiler.ast.ClassLiteralAccess;
import org.eclipse.jdt.internal.compiler.ast.CompoundAssignment;
import org.eclipse.jdt.internal.compiler.ast.FalseLiteral;
import org.eclipse.jdt.internal.compiler.ast.FieldReference;
import org.eclipse.jdt.internal.compiler.ast.Literal;
import org.eclipse.jdt.internal.compiler.ast.LocalDeclaration;
import org.eclipse.jdt.internal.compiler.ast.MessageSend;
import org.eclipse.jdt.internal.compiler.ast.NameReference;
import org.eclipse.jdt.internal.compiler.ast.OperatorExpression;
import org.eclipse.jdt.internal.compiler.ast.QualifiedNameReference;
import org.eclipse.jdt.internal.compiler.ast.SingleNameReference;
import org.eclipse.jdt.internal.compiler.ast.ThisReference;
import org.eclipse.jdt.internal.compiler.ast.TrueLiteral;
import org.eclipse.jdt.internal.compiler.ast.TypeReference;
import org.eclipse.jdt.internal.compiler.lookup.FieldBinding;
import org.eclipse.jdt.internal.compiler.lookup.LocalVariableBinding;
import org.eclipse.jdt.internal.compiler.lookup.BlockScope;
import org.eclipse.jdt.internal.compiler.lookup.ReferenceBinding;

class DefaultBindingResolver extends BindingResolver {

	/**
	 * This map is used to keep the correspondance between new bindings and the 
	 * compiler bindings. This is an identity map. We should only create one object
	 * for one binding.
	 */
	Map compilerBindingsToASTBindings;
	
	/**
	 * This map is used to retrieve an old ast node using the new ast node. This is not an
	 * identity map.
	 */
	Map newAstToOldAst;
	
	/**
	 * This map is used to get an ast node from its binding (new binding)
	 */
	Map bindingsToAstNodes;
	
	/**
	 * This map is used to get a binding from its ast node
	 */
	Map astNodesToBindings;


	/**
	 * Constructor for DefaultBindingResolver.
	 */
	DefaultBindingResolver() {
		this.newAstToOldAst = new HashMap();
		this.compilerBindingsToASTBindings = new HashMap();
		this.bindingsToAstNodes = new HashMap();
		this.astNodesToBindings = new HashMap();
	}

	/**
	 * @see BindingResolver#resolveName(Name)
	 */
	IBinding resolveName(Name name) {
		// retrieve the old ast node
		if (name.getParent() instanceof PackageDeclaration) {
			return resolveNameForPackageDeclaration(name);
		}
		if (name.getParent() instanceof ImportDeclaration) {
			return null;
		}
		AstNode node = (AstNode) this.newAstToOldAst.get(name);
		if (node instanceof NameReference || node == null) {
			return resolveNameForNameReference(name, node);
		}
		if (node instanceof MessageSend) {
			return resolveNameForMessageSend(name, node);
		}
		return super.resolveName(name);
	}

	private IBinding resolveNameForMessageSend(Name name, AstNode node) {
		MessageSend messageSend = (MessageSend) node;
		if (name.isSimpleName()) {
			// this can be either the qualifier or the method invocation name
			SimpleName simpleName = (SimpleName) name;
			if (simpleName.getIdentifier().equals(new String(messageSend.selector))) {
				return this.getMethodBinding(messageSend.binding); 
			} else {
				// this is the qualifier
				org.eclipse.jdt.internal.compiler.ast.Expression receiver = messageSend.receiver;
				if (receiver instanceof SingleNameReference) {
					SingleNameReference singleNameReference = (SingleNameReference) receiver;
					if (singleNameReference.isTypeReference()) {
						return this.getTypeBinding((ReferenceBinding)singleNameReference.binding);
					} else {
						// this is a variable or a field
						return this.getVariableBinding((org.eclipse.jdt.internal.compiler.lookup.VariableBinding)singleNameReference.binding);				
					}
				}
			}
		} else {
			// this is the qualifier
			org.eclipse.jdt.internal.compiler.ast.Expression receiver = messageSend.receiver;
			if (receiver instanceof QualifiedNameReference) {
				QualifiedNameReference qualifiedNameReference = (QualifiedNameReference) receiver;
		
				if (qualifiedNameReference.isTypeReference()) {
					return this.getTypeBinding((ReferenceBinding)qualifiedNameReference.binding);
				} else {
					// this is a variable or a field
					return this.getVariableBinding((org.eclipse.jdt.internal.compiler.lookup.VariableBinding) qualifiedNameReference.binding);				
				}
			}
		}
		return super.resolveName(name);
	}

	private IBinding resolveNameForMessageSend(Name name, AstNode node, int index) {
		MessageSend messageSend = (MessageSend) node;
		// this is the qualifier
		org.eclipse.jdt.internal.compiler.ast.Expression receiver = messageSend.receiver;
		if (receiver instanceof QualifiedNameReference) {
			QualifiedNameReference qualifiedNameReference = (QualifiedNameReference) receiver;
	
			if (qualifiedNameReference.isTypeReference()) {
				return this.getTypeBinding((ReferenceBinding)qualifiedNameReference.binding);
			} else {
				// this is a variable or a field
				if (index != 0) {
					return this.getVariableBinding((org.eclipse.jdt.internal.compiler.lookup.VariableBinding) qualifiedNameReference.binding).getDeclaringClass();				
				} else {
					return this.getVariableBinding((org.eclipse.jdt.internal.compiler.lookup.VariableBinding) qualifiedNameReference.binding);				
				}
			}
		}
		return super.resolveName(name);
	}

	private IBinding resolveNameForNameReference(Name name, AstNode node) {
		if (node != null) {
			if (node instanceof SingleNameReference) {
				SingleNameReference singleNameReference = (SingleNameReference) node;
				if (singleNameReference.isTypeReference()) {
					return this.getTypeBinding((ReferenceBinding)singleNameReference.binding);
				} else {
					// this is a variable or a field
					return this.getVariableBinding((org.eclipse.jdt.internal.compiler.lookup.VariableBinding)singleNameReference.binding);				
				}
			} else if (node instanceof QualifiedNameReference) {
				QualifiedNameReference qualifiedNameReference = (QualifiedNameReference) node;
		
				if (qualifiedNameReference.isTypeReference()) {
					return this.getTypeBinding((ReferenceBinding)qualifiedNameReference.binding);
				} else {
					// this is a variable or a field
					if (qualifiedNameReference.otherBindings == null) {
						return this.getVariableBinding((org.eclipse.jdt.internal.compiler.lookup.VariableBinding) qualifiedNameReference.binding);				
					} else {
						return this.getVariableBinding((org.eclipse.jdt.internal.compiler.lookup.VariableBinding) qualifiedNameReference.otherBindings[qualifiedNameReference.otherBindings.length - 1]);				
					}
				}
			}
		}
		// this might be a inner qualified name or simple name inside a qualified name
		int index = 1;
		QualifiedName firstQualifier = null;
		Name firstName = name;
		if (name.isSimpleName()) {
			if (name.getParent() instanceof QualifiedName) {
				name = (QualifiedName) name.getParent();
				firstQualifier = (QualifiedName) name;
			} else {
				return super.resolveName(name);
			}
		}
		while (name.getParent() instanceof QualifiedName) {
			index++;
			name = (QualifiedName) name.getParent();
		}
		// now we can retrieve the enclosing compiler's node corresponding to the inner name
		node = (AstNode) this.newAstToOldAst.get(name);
		if (node == null) {
			return super.resolveName(name);
		} else if (node instanceof NameReference) {
			
			QualifiedNameReference qualifiedNameReference = (QualifiedNameReference) node;
			if (firstQualifier != null) {
				// handle the first simple name in a qualified name a.b.c.d (handles the 'a' case)
				Name firstQualifierName = firstQualifier.getQualifier();
				if (firstQualifierName.isSimpleName() && firstName == firstQualifierName) {
					return this.getVariableBinding((org.eclipse.jdt.internal.compiler.lookup.VariableBinding) qualifiedNameReference.binding);				
				}
			}
			if (qualifiedNameReference.isTypeReference()) {
				return this.getTypeBinding((ReferenceBinding)qualifiedNameReference.binding);
			} else {
				// this is a variable or a field
				return this.getVariableBinding((org.eclipse.jdt.internal.compiler.lookup.VariableBinding) qualifiedNameReference.otherBindings[qualifiedNameReference.otherBindings.length - index]);				
			}
		} else if (node instanceof MessageSend) {
			return this.resolveNameForMessageSend(name, node, index);
		}
		return super.resolveName(name);
	}

	private IBinding resolveNameForPackageDeclaration(Name name) {
		PackageDeclaration packageDeclaration = (PackageDeclaration) name.getParent();
		CompilationUnit unit = (CompilationUnit) packageDeclaration.getParent();
		List types = unit.types();
		if (types.size() == 0) {
			return super.resolveName(name);
		}
		TypeDeclaration type = (TypeDeclaration) types.get(0);
		ITypeBinding typeBinding = type.resolveBinding();
		return typeBinding.getPackage();
		
	}

	/**
	 * @see BindingResolver#resolveType(Type)
	 */
	ITypeBinding resolveType(Type type) {
		// retrieve the old ast node
		TypeReference typeReference = (TypeReference) this.newAstToOldAst.get(type);
		if (typeReference == null) {
			return super.resolveType(type);
		}
		return this.getTypeBinding(typeReference.binding);
	}

	/**
	 * @see BindingResolver#resolveWellKnownType(String)
	 */
	ITypeBinding resolveWellKnownType(String name) {
		return super.resolveWellKnownType(name);
	}

	/**
	 * @see BindingResolver#resolveType(TypeDeclaration)
	 */
	ITypeBinding resolveType(TypeDeclaration type) {
		org.eclipse.jdt.internal.compiler.ast.TypeDeclaration typeDeclaration = (org.eclipse.jdt.internal.compiler.ast.TypeDeclaration) this.newAstToOldAst.get(type);
		if (typeDeclaration != null) {
			ITypeBinding typeBinding = this.getTypeBinding(typeDeclaration.binding);
			this.bindingsToAstNodes.put(typeBinding, type);
			return typeBinding;
		}
		return super.resolveType(type);
	}

	/**
	 * @see BindingResolver#resolveMethod(MethodDeclaration)
	 */
	IMethodBinding resolveMethod(MethodDeclaration method) {
		AbstractMethodDeclaration methodDeclaration = (AbstractMethodDeclaration) this.newAstToOldAst.get(method);
		if (methodDeclaration != null) {
			IMethodBinding methodBinding = this.getMethodBinding(methodDeclaration.binding);
			this.bindingsToAstNodes.put(methodBinding, method);
			return methodBinding;
		}
		return super.resolveMethod(method);
	}

	/**
	 * @see BindingResolver#resolveVariable(VariableDeclaration)
	 */
	IVariableBinding resolveVariable(VariableDeclaration variable) {
		AbstractVariableDeclaration abstractVariableDeclaration = (AbstractVariableDeclaration) this.newAstToOldAst.get(variable);
		if (abstractVariableDeclaration instanceof org.eclipse.jdt.internal.compiler.ast.FieldDeclaration) {
			org.eclipse.jdt.internal.compiler.ast.FieldDeclaration fieldDeclaration = (org.eclipse.jdt.internal.compiler.ast.FieldDeclaration) this.newAstToOldAst.get(variable);
			return this.getVariableBinding(fieldDeclaration.binding);
		}
		return this.getVariableBinding(((LocalDeclaration) abstractVariableDeclaration).binding);
	}

	/**
	 * @see BindingResolver#resolveVariable(FieldDeclaration)
	 */
	IVariableBinding resolveVariable(FieldDeclaration variable) {
		org.eclipse.jdt.internal.compiler.ast.FieldDeclaration fieldDeclaration = (org.eclipse.jdt.internal.compiler.ast.FieldDeclaration) this.newAstToOldAst.get(variable);
		return this.getVariableBinding(fieldDeclaration.binding);
	}

	/**
	 * @see BindingResolver#resolveExpressionType(Expression)
	 */
	ITypeBinding resolveExpressionType(Expression expression) {
		if (expression instanceof ClassInstanceCreation) {
			AstNode astNode = (AstNode) this.newAstToOldAst.get(expression);
			if (astNode instanceof org.eclipse.jdt.internal.compiler.ast.TypeDeclaration) {
				org.eclipse.jdt.internal.compiler.ast.TypeDeclaration typeDeclaration = (org.eclipse.jdt.internal.compiler.ast.TypeDeclaration) astNode;
				if (typeDeclaration != null) {
					ITypeBinding typeBinding = this.getTypeBinding(typeDeclaration.binding);
					this.bindingsToAstNodes.put(typeBinding, expression);
					return typeBinding;
				}
			} else {
				// should be an AllocationExpression
				AllocationExpression allocationExpression = (AllocationExpression) astNode;
				return this.getMethodBinding(allocationExpression.binding).getDeclaringClass();
			}
		} else if (expression instanceof Name) {
			IBinding binding = this.resolveName((Name) expression);
			if (binding == null) {
				return null;
			}
			switch(binding.getKind()) {
				case IBinding.TYPE :
					return (ITypeBinding) binding;
				case IBinding.VARIABLE :
					return ((IVariableBinding) binding).getType();
			}
		} else if (expression instanceof ArrayInitializer) {
			org.eclipse.jdt.internal.compiler.ast.ArrayInitializer oldAst = (org.eclipse.jdt.internal.compiler.ast.ArrayInitializer) this.newAstToOldAst.get(expression);
			if (oldAst == null || oldAst.binding == null) {
				return super.resolveExpressionType(expression);
			}
			return this.getTypeBinding(oldAst.binding);
		} else if (expression instanceof ArrayCreation) {
			ArrayAllocationExpression arrayAllocationExpression = (ArrayAllocationExpression) this.newAstToOldAst.get(expression);
			return this.getTypeBinding(arrayAllocationExpression.arrayTb);
		} else if (expression instanceof Assignment) {
			Assignment assignment = (Assignment) expression;
			return this.resolveExpressionType(assignment.getLeftHandSide());
		} else if (expression instanceof PostfixExpression) {
			PostfixExpression postFixExpression = (PostfixExpression) expression;
			return this.resolveExpressionType(postFixExpression.getOperand());
		} else if (expression instanceof PrefixExpression) {
			PrefixExpression preFixExpression = (PrefixExpression) expression;
			return this.resolveExpressionType(preFixExpression.getOperand());
		} else if (expression instanceof CastExpression) {
			org.eclipse.jdt.internal.compiler.ast.CastExpression castExpression = (org.eclipse.jdt.internal.compiler.ast.CastExpression) this.newAstToOldAst.get(expression);
			return this.getTypeBinding(castExpression.castTb);
		} else if (expression instanceof TypeLiteral) {
			ClassLiteralAccess classLiteralAccess = (ClassLiteralAccess) this.newAstToOldAst.get(expression);
			return this.getTypeBinding(classLiteralAccess.targetType);
		} else if (expression instanceof BooleanLiteral) {
			BooleanLiteral booleanLiteral = (BooleanLiteral) expression;
			if (booleanLiteral.booleanValue()) {
				TrueLiteral trueLiteral = (TrueLiteral) this.newAstToOldAst.get(booleanLiteral);
				return this.getTypeBinding(trueLiteral.literalType(null));
			} else {
				FalseLiteral falseLiteral = (FalseLiteral) this.newAstToOldAst.get(booleanLiteral);
				return this.getTypeBinding(falseLiteral.literalType(null));
			}
		} else if (expression instanceof NullLiteral) {
			org.eclipse.jdt.internal.compiler.ast.NullLiteral nullLiteral = (org.eclipse.jdt.internal.compiler.ast.NullLiteral) this.newAstToOldAst.get(expression);
			return this.getTypeBinding(nullLiteral.literalType(null));
		} else if (expression instanceof CharacterLiteral) {
			CharLiteral charLiteral = (CharLiteral) this.newAstToOldAst.get(expression);
			return this.getTypeBinding(charLiteral.literalType(null));
		} else if (expression instanceof NumberLiteral) {
			Literal literal = (Literal) this.newAstToOldAst.get(expression);
			return this.getTypeBinding(literal.literalType(null));
		} else if (expression instanceof InfixExpression) {
			OperatorExpression operatorExpression = (OperatorExpression) this.newAstToOldAst.get(expression);
			return this.getTypeBinding(operatorExpression.typeBinding);
		} else if (expression instanceof FieldAccess) {
			FieldReference fieldReference = (FieldReference) this.newAstToOldAst.get(expression);
			return this.getVariableBinding(fieldReference.binding).getType();
		} else if (expression instanceof SuperFieldAccess) {
			FieldReference fieldReference = (FieldReference) this.newAstToOldAst.get(expression);
			return this.getVariableBinding(fieldReference.binding).getType();
		} else if (expression instanceof ArrayAccess) {
			ArrayReference arrayReference = (ArrayReference) this.newAstToOldAst.get(expression);
			return this.getTypeBinding(arrayReference.arrayElementBinding);
		} else if (expression instanceof ThisExpression) {
			ThisReference thisReference = (ThisReference) this.newAstToOldAst.get(expression);
			return this.getTypeBinding(thisReference.resolveType(this.retrieveEnclosingScope(expression)));
		} else if (expression instanceof MethodInvocation) {
			MessageSend messageSend = (MessageSend)  this.newAstToOldAst.get(expression);
			return this.getMethodBinding(messageSend.binding).getReturnType();
		} else if (expression instanceof ParenthesizedExpression) {
			ParenthesizedExpression parenthesizedExpression = (ParenthesizedExpression) expression;
			return this.resolveExpressionType(parenthesizedExpression.getExpression());
		}
		return super.resolveExpressionType(expression);
	}

	/**
	 * @see BindingResolver#findDeclaringNode(IBinding)
	 */
	public ASTNode findDeclaringNode(IBinding binding) {
		return (ASTNode) this.bindingsToAstNodes.get(binding);
	}

	/**
	 * @see BindingResolver#store(ASTNode, AstNode)
	 */
	void store(ASTNode node, AstNode oldASTNode) {
		this.newAstToOldAst.put(node, oldASTNode);
	}

	protected ITypeBinding getTypeBinding(org.eclipse.jdt.internal.compiler.lookup.TypeBinding referenceBinding) {
		if (referenceBinding == null || !referenceBinding.isValidBinding()) {
			return null;
		}
		TypeBinding binding = (TypeBinding) this.compilerBindingsToASTBindings.get(referenceBinding);
		if (binding != null) {
			return binding;
		}
		binding = new TypeBinding(this, referenceBinding);
		this.compilerBindingsToASTBindings.put(referenceBinding, binding);
		return binding;
	}

	/*
	 * @see BindingResolver#getPackageBinding(PackageBinding)
	 */
	protected IPackageBinding getPackageBinding(org.eclipse.jdt.internal.compiler.lookup.PackageBinding packageBinding) {
		if (!packageBinding.isValidBinding()) {
			return null;
		}
		IPackageBinding binding = (IPackageBinding) this.compilerBindingsToASTBindings.get(packageBinding);
		if (binding != null) {
			return binding;
		}
		binding = new PackageBinding(this, packageBinding);
		this.compilerBindingsToASTBindings.put(packageBinding, binding);
		return binding;
	}

	/*
	 * @see BindingResolver#getVariableBinding(LocalVariableBinding)
	 */
	protected IVariableBinding getVariableBinding(org.eclipse.jdt.internal.compiler.lookup.VariableBinding variableBinding) {
		if (!variableBinding.isValidBinding()) {
			return null;
		}
		IVariableBinding binding = (IVariableBinding) this.compilerBindingsToASTBindings.get(variableBinding);
		if (binding != null) {
			return binding;
		}
		binding = new VariableBinding(this, variableBinding);
		this.compilerBindingsToASTBindings.put(variableBinding, binding);
		return binding;
	}
	
	/*
	 * @see BindingResolver#getMethodBinding(MethodBinding)
	 */
	protected IMethodBinding getMethodBinding(org.eclipse.jdt.internal.compiler.lookup.MethodBinding methodBinding) {
		if (!methodBinding.isValidBinding()) {
			return null;
		}
		IMethodBinding binding = (IMethodBinding) this.compilerBindingsToASTBindings.get(methodBinding);
		if (binding != null) {
			return binding;
		}
		binding = new MethodBinding(this, methodBinding);
		this.compilerBindingsToASTBindings.put(methodBinding, binding);
		return binding;
	}

	private BlockScope retrieveEnclosingScope(ASTNode node) {
		ASTNode currentNode = node;
		while(!(currentNode instanceof MethodDeclaration) && !(currentNode instanceof Initializer)) {
			currentNode = currentNode.getParent();
		}
		if (currentNode instanceof Initializer) {
			Initializer initializer = (Initializer) currentNode;
			while(!(currentNode instanceof TypeDeclaration)) {
				currentNode = currentNode.getParent();
			}
			org.eclipse.jdt.internal.compiler.ast.TypeDeclaration typeDecl = (org.eclipse.jdt.internal.compiler.ast.TypeDeclaration) this.newAstToOldAst.get(currentNode);
			if ((initializer.getModifiers() & Modifier.STATIC) != 0) {
				return typeDecl.staticInitializerScope;
			} else {
				return typeDecl.initializerScope;
			}
		}
		// this is a MethodDeclaration
		AbstractMethodDeclaration abstractMethodDeclaration = (AbstractMethodDeclaration) this.newAstToOldAst.get(currentNode);
		return abstractMethodDeclaration.scope;
	}	
}
