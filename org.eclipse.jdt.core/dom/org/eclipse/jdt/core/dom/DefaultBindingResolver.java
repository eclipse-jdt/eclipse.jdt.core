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
import org.eclipse.jdt.internal.compiler.ast.AnonymousLocalTypeDeclaration;
import org.eclipse.jdt.internal.compiler.ast.ArrayAllocationExpression;
import org.eclipse.jdt.internal.compiler.ast.ArrayReference;
import org.eclipse.jdt.internal.compiler.ast.AstNode;
import org.eclipse.jdt.internal.compiler.ast.BinaryExpression;
import org.eclipse.jdt.internal.compiler.ast.CharLiteral;
import org.eclipse.jdt.internal.compiler.ast.ClassLiteralAccess;
import org.eclipse.jdt.internal.compiler.ast.CompilationUnitDeclaration;
import org.eclipse.jdt.internal.compiler.ast.CompoundAssignment;
import org.eclipse.jdt.internal.compiler.ast.FalseLiteral;
import org.eclipse.jdt.internal.compiler.ast.FieldReference;
import org.eclipse.jdt.internal.compiler.ast.ImportReference;
import org.eclipse.jdt.internal.compiler.ast.Literal;
import org.eclipse.jdt.internal.compiler.ast.LocalDeclaration;
import org.eclipse.jdt.internal.compiler.ast.MessageSend;
import org.eclipse.jdt.internal.compiler.ast.NameReference;
import org.eclipse.jdt.internal.compiler.ast.OperatorExpression;
import org.eclipse.jdt.internal.compiler.ast.QualifiedNameReference;
import org.eclipse.jdt.internal.compiler.ast.QualifiedSuperReference;
import org.eclipse.jdt.internal.compiler.ast.SingleNameReference;
import org.eclipse.jdt.internal.compiler.ast.ThisReference;
import org.eclipse.jdt.internal.compiler.ast.TrueLiteral;
import org.eclipse.jdt.internal.compiler.ast.TypeReference;
import org.eclipse.jdt.internal.compiler.lookup.Binding;
import org.eclipse.jdt.internal.compiler.lookup.BindingIds;
import org.eclipse.jdt.internal.compiler.lookup.CompilationUnitScope;
import org.eclipse.jdt.internal.compiler.lookup.FieldBinding;
import org.eclipse.jdt.internal.compiler.lookup.LocalVariableBinding;
import org.eclipse.jdt.internal.compiler.lookup.BlockScope;
import org.eclipse.jdt.internal.compiler.lookup.ReferenceBinding;
import org.eclipse.jdt.internal.compiler.lookup.Scope;
import org.eclipse.jdt.internal.compiler.util.CharOperation;

/**
 * Internal class for resolving bindings using old ASTs.
 */
class DefaultBindingResolver extends BindingResolver {
	
	private static final char[][] JAVA_LANG_STRINGBUFFER = new char[][] {"java".toCharArray(), "lang".toCharArray(), "StringBuffer".toCharArray()};
	private static final char[][] JAVA_LANG_EXCEPTION = new char[][] {"java".toCharArray(), "lang".toCharArray(), "Exception".toCharArray()};

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
	 * Compilation unit scope
	 */
	private CompilationUnitScope scope;
	
	/**
	 * Constructor for DefaultBindingResolver.
	 */
	DefaultBindingResolver(AST ast) {
		super(ast);
		this.newAstToOldAst = new HashMap();
		this.compilerBindingsToASTBindings = new HashMap();
		this.bindingsToAstNodes = new HashMap();
		this.astNodesToBindings = new HashMap();
	}
	
	/**
	 * Constructor for DefaultBindingResolver.
	 */
	DefaultBindingResolver(AST ast, CompilationUnitScope scope) {
		this(ast);
		this.scope = scope;
	}
	
	/*
	 * Method declared on BindingResolver.
	 */
	IBinding resolveName(Name name) {
		if (this.modificationCount != this.ast.modificationCount()) {
			return null;
		}
		ASTNode parent = name.getParent();
		if (parent instanceof MethodInvocation
			|| parent instanceof SuperMethodInvocation) {
			return internalResolveNameForMethodInvocation(name);
		}
		if (parent instanceof FieldAccess
		   || parent instanceof SuperFieldAccess) {
			return internalResolveNameForFieldAccess(name);
		}
		if (parent instanceof PackageDeclaration) {
			return internalResolveNameForPackageDeclaration(name);
		}
		if (parent instanceof SimpleType) {
			return internalResolveNameForSimpleType(name);
		}
		if (parent instanceof ThisExpression) {
			return internalResolveNameForThisExpression(name);
		}
		if (name instanceof QualifiedName) {
			return internalResolveNameForQualifiedName(name);
		}
		if (name instanceof SimpleName) {
			return internalResolveNameForSimpleName(name);
		}
		return super.resolveName(name);
	}

	private IBinding internalResolveNameForPackageDeclaration(Name name) {
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
	/*
	 * Method declared on BindingResolver.
	 */
	ITypeBinding resolveType(Type type) {
		if (this.modificationCount != this.ast.modificationCount()) {
			return null;
		}
		// retrieve the old ast node
		AstNode node = (AstNode) this.newAstToOldAst.get(type);
		if (node != null) {
			if (node instanceof TypeReference) {
				TypeReference typeReference = (TypeReference) node;
				return this.getTypeBinding(typeReference.binding);
			} else if (node instanceof SingleNameReference) {
				SingleNameReference singleNameReference = (SingleNameReference) node;
				if (singleNameReference.isTypeReference()) {
					return this.getTypeBinding((ReferenceBinding)singleNameReference.binding);
				} else {
					// it should be a type reference
					return null;
				}
			} else if (node instanceof QualifiedNameReference) {
				QualifiedNameReference qualifiedNameReference = (QualifiedNameReference) node;
				if (qualifiedNameReference.isTypeReference()) {
					return this.getTypeBinding((ReferenceBinding)qualifiedNameReference.binding);
				} else {
					// it should be a type reference
					return null;
				}
			}
		}
		return null;
	}
	/*
	 * Method declared on BindingResolver.
	 */
	ITypeBinding resolveWellKnownType(String name) {
		if (("boolean".equals(name))
			|| ("char".equals(name))
			|| ("byte".equals(name))
			|| ("short".equals(name))
			|| ("int".equals(name))
			|| ("long".equals(name))
			|| ("float".equals(name))
			|| ("double".equals(name))
			|| ("void".equals(name))) {
			return this.getTypeBinding(this.scope.getBaseType(name.toCharArray()));
		} else if ("java.lang.Object".equals(name)) {
			return this.getTypeBinding(this.scope.getJavaLangObject());
		} else if ("java.lang.String".equals(name)) {
			return this.getTypeBinding(this.scope.getJavaLangString());
		} else if ("java.lang.StringBuffer".equals(name)) {
			return this.getTypeBinding(this.scope.getType(JAVA_LANG_STRINGBUFFER));
		} else if ("java.lang.Throwable".equals(name)) {
			return this.getTypeBinding(this.scope.getJavaLangThrowable());
		} else if ("java.lang.Exception".equals(name)) {
			return this.getTypeBinding(this.scope.getType(JAVA_LANG_EXCEPTION));
		} else if ("java.lang.RuntimeException".equals(name)) {
			return this.getTypeBinding(this.scope.getJavaLangRuntimeException());
		} else if ("java.lang.Error".equals(name)) {
			return this.getTypeBinding(this.scope.getJavaLangError());
		} else if ("java.lang.Class".equals(name)) {
			return this.getTypeBinding(this.scope.getJavaLangClass());
	    } else {
			return super.resolveWellKnownType(name);
		}
	}
	/*
	 * Method declared on BindingResolver.
	 */
	ITypeBinding resolveType(TypeDeclaration type) {
		if (this.modificationCount != this.ast.modificationCount()) {
			return null;
		}
		org.eclipse.jdt.internal.compiler.ast.TypeDeclaration typeDeclaration = (org.eclipse.jdt.internal.compiler.ast.TypeDeclaration) this.newAstToOldAst.get(type);
		if (typeDeclaration != null) {
			ITypeBinding typeBinding = this.getTypeBinding(typeDeclaration.binding);
			this.bindingsToAstNodes.put(typeBinding, type);
			return typeBinding;
		}
		return super.resolveType(type);
	}
	/*
	 * Method declared on BindingResolver.
	 */
	IMethodBinding resolveMethod(MethodDeclaration method) {
		if (this.modificationCount != this.ast.modificationCount()) {
			return null;
		}
		AbstractMethodDeclaration methodDeclaration = (AbstractMethodDeclaration) this.newAstToOldAst.get(method);
		if (methodDeclaration != null) {
			IMethodBinding methodBinding = this.getMethodBinding(methodDeclaration.binding);
			this.bindingsToAstNodes.put(methodBinding, method);
			return methodBinding;
		}
		return super.resolveMethod(method);
	}
	/*
	 * Method declared on BindingResolver.
	 */
	IVariableBinding resolveVariable(VariableDeclaration variable) {
		if (this.modificationCount != this.ast.modificationCount()) {
			return null;
		}
		AbstractVariableDeclaration abstractVariableDeclaration = (AbstractVariableDeclaration) this.newAstToOldAst.get(variable);
		if (abstractVariableDeclaration instanceof org.eclipse.jdt.internal.compiler.ast.FieldDeclaration) {
			org.eclipse.jdt.internal.compiler.ast.FieldDeclaration fieldDeclaration = (org.eclipse.jdt.internal.compiler.ast.FieldDeclaration) abstractVariableDeclaration;
			IVariableBinding variableBinding = this.getVariableBinding(fieldDeclaration.binding);
			this.bindingsToAstNodes.put(variableBinding, variable);
			return variableBinding;
		}
		IVariableBinding variableBinding = this.getVariableBinding(((LocalDeclaration) abstractVariableDeclaration).binding);
		this.bindingsToAstNodes.put(variableBinding, variable);
		return variableBinding;
	}
	/*
	 * Method declared on BindingResolver.
	 */
	IVariableBinding resolveVariable(FieldDeclaration variable) {
		if (this.modificationCount != this.ast.modificationCount()) {
			return null;
		}
		org.eclipse.jdt.internal.compiler.ast.FieldDeclaration fieldDeclaration = (org.eclipse.jdt.internal.compiler.ast.FieldDeclaration) this.newAstToOldAst.get(variable);
		IVariableBinding variableBinding = this.getVariableBinding(fieldDeclaration.binding);
		this.bindingsToAstNodes.put(variableBinding, variable);
		return variableBinding;
	}
	/*
	 * Method declared on BindingResolver.
	 */
	ITypeBinding resolveExpressionType(Expression expression) {
		if (this.modificationCount != this.ast.modificationCount()) {
			return null;
		}
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
				IMethodBinding methodBinding = this.getMethodBinding(allocationExpression.binding);
				if (methodBinding == null) {
					return null;
				} else {
					return methodBinding.getDeclaringClass();
				}
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
		} else if (expression instanceof StringLiteral) {
			org.eclipse.jdt.internal.compiler.ast.StringLiteral stringLiteral = (org.eclipse.jdt.internal.compiler.ast.StringLiteral) this.newAstToOldAst.get(expression);
			return this.getTypeBinding(stringLiteral.literalType(this.retrieveEnclosingScope(expression)));
		} else if (expression instanceof TypeLiteral) {
			ClassLiteralAccess classLiteralAccess = (ClassLiteralAccess) this.newAstToOldAst.get(expression);
			return this.getTypeBinding(this.scope.getJavaLangClass());
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
			IVariableBinding variableBinding = this.getVariableBinding(fieldReference.binding);
			if (variableBinding == null) {
				return null;
			} else {
				return variableBinding.getType();
			}
		} else if (expression instanceof SuperFieldAccess) {
			FieldReference fieldReference = (FieldReference) this.newAstToOldAst.get(expression);
			IVariableBinding variableBinding = this.getVariableBinding(fieldReference.binding);
			if (variableBinding == null) {
				return null;
			} else {
				return variableBinding.getType();
			}
		} else if (expression instanceof ArrayAccess) {
			ArrayReference arrayReference = (ArrayReference) this.newAstToOldAst.get(expression);
			return this.getTypeBinding(arrayReference.arrayElementBinding);
		} else if (expression instanceof ThisExpression) {
			ThisReference thisReference = (ThisReference) this.newAstToOldAst.get(expression);
			return this.getTypeBinding(thisReference.resolveType(this.retrieveEnclosingScope(expression)));
		} else if (expression instanceof MethodInvocation) {
			MessageSend messageSend = (MessageSend)  this.newAstToOldAst.get(expression);
			IMethodBinding methodBinding = this.getMethodBinding(messageSend.binding);
			if (methodBinding == null) {
				return null;
			} else {
				return methodBinding.getReturnType();
			}
		} else if (expression instanceof ParenthesizedExpression) {
			ParenthesizedExpression parenthesizedExpression = (ParenthesizedExpression) expression;
			return this.resolveExpressionType(parenthesizedExpression.getExpression());
		}
		return super.resolveExpressionType(expression);
	}

	/*
	 * @see BindingResolver#resolveImport(ImportDeclaration)
	 */
	IBinding resolveImport(ImportDeclaration importDeclaration) {
		if (this.modificationCount != this.ast.modificationCount()) {
			return null;
		}
		AstNode node = (AstNode) this.newAstToOldAst.get(importDeclaration);
		if (node instanceof ImportReference) {
			ImportReference importReference = (ImportReference) node;
			if (importReference.onDemand) {
				Binding binding = this.scope.getTypeOrPackage(CharOperation.subarray(importReference.tokens, 0, importReference.tokens.length));
				if ((binding != null) && (binding.isValidBinding())) {
					IPackageBinding packageBinding = this.getPackageBinding((org.eclipse.jdt.internal.compiler.lookup.PackageBinding) binding);
					this.bindingsToAstNodes.put(packageBinding, importDeclaration);
					return packageBinding;
				}
			} else {
				Binding binding = this.scope.getTypeOrPackage(importReference.tokens);
				if (binding != null && binding.isValidBinding()) {
					ITypeBinding typeBinding = this.getTypeBinding((org.eclipse.jdt.internal.compiler.lookup.TypeBinding) binding);
					this.bindingsToAstNodes.put(typeBinding, importDeclaration);
					return typeBinding;
				}
			}
		}
		return super.resolveImport(importDeclaration);
	}

	/*
	 * @see BindingResolver#resolvePackage(PackageDeclaration)
	 */
	IPackageBinding resolvePackage(PackageDeclaration pkg) {
		if (this.modificationCount != this.ast.modificationCount()) {
			return null;
		}
		AstNode node = (AstNode) this.newAstToOldAst.get(pkg);
		if (node instanceof ImportReference) {
			ImportReference importReference = (ImportReference) node;
			Binding binding = this.scope.getTypeOrPackage(CharOperation.subarray(importReference.tokens, 0, importReference.tokens.length));
			if ((binding != null) && (binding.isValidBinding())) {
				IPackageBinding packageBinding = this.getPackageBinding((org.eclipse.jdt.internal.compiler.lookup.PackageBinding) binding);
				this.bindingsToAstNodes.put(packageBinding, pkg);
				return packageBinding;
			}
		}
		return super.resolvePackage(pkg);
	}

	/*
	 * Method declared on BindingResolver.
	 */
	public ASTNode findDeclaringNode(IBinding binding) {
		if (this.modificationCount != this.ast.modificationCount()) {
			return null;
		}
		return (ASTNode) this.bindingsToAstNodes.get(binding);
	}
	/*
	 * Method declared on BindingResolver.
	 */
	void store(ASTNode node, AstNode oldASTNode) {
		this.newAstToOldAst.put(node, oldASTNode);
	}
	/*
	 * Method declared on BindingResolver.
	 */
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
	 * Method declared on BindingResolver.
	 */
	protected IPackageBinding getPackageBinding(org.eclipse.jdt.internal.compiler.lookup.PackageBinding packageBinding) {
		if (packageBinding == null || !packageBinding.isValidBinding()) {
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
	 * Method declared on BindingResolver.
	 */
	protected IVariableBinding getVariableBinding(org.eclipse.jdt.internal.compiler.lookup.VariableBinding variableBinding) {
		if (variableBinding == null || !variableBinding.isValidBinding()) {
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
	 * Method declared on BindingResolver.
	 */
	protected IMethodBinding getMethodBinding(org.eclipse.jdt.internal.compiler.lookup.MethodBinding methodBinding) {
		if (methodBinding == null || !methodBinding.isValidBinding()) {
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
	
	private IBinding internalResolveNameForQualifiedName(Name name) {
		QualifiedName qualifiedName = (QualifiedName) name;
		ASTNode parent = qualifiedName.getParent();
		int index = 0;
		while (parent instanceof QualifiedName) {
			qualifiedName = (QualifiedName) parent;
			parent = parent.getParent();
			index++;
		}
		return returnBindingForQualifiedNamePart(qualifiedName, index);
	}

	private IBinding returnBindingForQualifiedNamePart(ASTNode parent, int index) {
		// now we can retrieve the compiler's node
		AstNode node = (AstNode) this.newAstToOldAst.get(parent);
		if (node instanceof QualifiedNameReference) {
			QualifiedNameReference qualifiedNameReference = (QualifiedNameReference) node;
			if (qualifiedNameReference.isTypeReference()) {
				return this.getTypeBinding((ReferenceBinding)qualifiedNameReference.binding);
			} else {
				int qualifiedNameLength = qualifiedNameReference.tokens.length;
				int indexInQualifiedName = qualifiedNameLength - index; // one-based
				int indexOfFirstFieldBinding = qualifiedNameReference.indexOfFirstFieldBinding; // one-based
				int otherBindingLength = qualifiedNameLength - indexOfFirstFieldBinding;
				if (indexInQualifiedName < indexOfFirstFieldBinding) {
					// a extra lookup is required`
					Scope scope = retrieveEnclosingScope(parent);
					Binding binding = scope.getTypeOrPackage(CharOperation.subarray(qualifiedNameReference.tokens, 0, indexInQualifiedName));
					if (binding != null && binding.isValidBinding()) {
						if (binding instanceof org.eclipse.jdt.internal.compiler.lookup.PackageBinding) {
							return this.getPackageBinding((org.eclipse.jdt.internal.compiler.lookup.PackageBinding)binding);
						} else {
							// it is a type
							return this.getTypeBinding((org.eclipse.jdt.internal.compiler.lookup.TypeBinding)binding);
						}
					}
					return null;
				} else {
					if (indexInQualifiedName == indexOfFirstFieldBinding) {
						Binding binding = qualifiedNameReference.binding;
						if (binding != null && binding.isValidBinding()) {
							return this.getVariableBinding((org.eclipse.jdt.internal.compiler.lookup.VariableBinding) binding);				
						} else {
							return null;
						}
					} else {
						return this.getVariableBinding(qualifiedNameReference.otherBindings[otherBindingLength - index - 1]);				
					}
				}
			}
		}
		return null;
	}

	private IBinding internalResolveNameForSimpleName(Name name) {
		AstNode node = (AstNode) this.newAstToOldAst.get(name);
		if (node == null) {
			ASTNode parent = name.getParent();
			if (parent instanceof QualifiedName) {
				// retrieve the qualified name and remember at which position is the simple name
				QualifiedName qualifiedName = (QualifiedName) parent;
				int index = -1;
				if (qualifiedName.getQualifier() == name) {
					index++;
				}
				while (parent instanceof QualifiedName) {
					qualifiedName = (QualifiedName) parent;
					parent = parent.getParent();
					index++;
				}
				return returnBindingForQualifiedNamePart(qualifiedName, index);
			}
		}
		if (node instanceof SingleNameReference) {
			SingleNameReference singleNameReference = (SingleNameReference) node;
			if (singleNameReference.isTypeReference()) {
				return this.getTypeBinding((ReferenceBinding)singleNameReference.binding);
			} else {
				// this is a variable or a field
				Binding binding = singleNameReference.binding;
				if (binding != null && binding.isValidBinding()) {
					return this.getVariableBinding((org.eclipse.jdt.internal.compiler.lookup.VariableBinding) binding);				
				} else {
					return null;
				}
			}
		} else if (node instanceof QualifiedSuperReference) {
			QualifiedSuperReference qualifiedSuperReference = (QualifiedSuperReference) node;
			return this.getTypeBinding(qualifiedSuperReference.qualification.binding);
		} else if (node instanceof LocalDeclaration) {
			return this.getVariableBinding(((LocalDeclaration)node).binding);
		} else if (node instanceof FieldReference) {
			return getVariableBinding(((FieldReference) node).binding);
		}
		return null;
	}

	private IBinding internalResolveNameForMethodInvocation(Name name) {
		ASTNode parent = name.getParent();
		if (parent instanceof MethodInvocation) {
			MethodInvocation methodInvocation = (MethodInvocation) parent;
			if (name == methodInvocation.getExpression()) {
				if (name.isQualifiedName()) {
					return this.internalResolveNameForQualifiedName(name);
				} else {
					return this.internalResolveNameForSimpleName(name);
				}
			} else {
				AstNode node = (AstNode) this.newAstToOldAst.get(name);
				if (node instanceof MessageSend) {
					MessageSend messageSend = (MessageSend) node;
					return getMethodBinding(messageSend.binding);
				} else if (name.isQualifiedName()) {
					return this.internalResolveNameForQualifiedName(name);
				} else {
					return this.internalResolveNameForSimpleName(name);
				}
			}
		} else {
			SuperMethodInvocation superMethodInvocation = (SuperMethodInvocation) parent;
			if (name == superMethodInvocation.getQualifier()) {
				if (name.isQualifiedName()) {
					return this.internalResolveNameForQualifiedName(name);
				} else {
					return this.internalResolveNameForSimpleName(name);
				}
			} else {
				AstNode node = (AstNode) this.newAstToOldAst.get(name);
				if (node instanceof MessageSend) {
					MessageSend messageSend = (MessageSend) node;
					return getMethodBinding(messageSend.binding);
				} else if (name.isQualifiedName()) {
					return this.internalResolveNameForQualifiedName(name);
				} else {
					return this.internalResolveNameForSimpleName(name);
				}
			}
		}
	}
	
	private IBinding internalResolveNameForFieldAccess(Name name) {
		if (name.isQualifiedName()) {
			return this.internalResolveNameForQualifiedName(name);
		} else {
			return this.internalResolveNameForSimpleName(name);
		}
	}

	private IBinding internalResolveNameForSimpleType(Name name) {
		AstNode node = (AstNode) this.newAstToOldAst.get(name);
		if (node instanceof TypeReference) {
			return this.getTypeBinding(((TypeReference) node).binding);
		} else if (node instanceof NameReference) {
			NameReference nameReference = (NameReference) node;
			if (nameReference.isTypeReference()) {
				return this.getTypeBinding((org.eclipse.jdt.internal.compiler.lookup.TypeBinding) nameReference.binding);
			}
		}
		return null;
	}	

	private IBinding internalResolveNameForThisExpression(Name name) {
		AstNode node = (AstNode) this.newAstToOldAst.get(name);
		if (node instanceof TypeReference) {
			return this.getTypeBinding(((TypeReference) node).binding);
		}
		return null;
	}
	
	/*
	 * @see BindingResolver#resolveConstructor(ClassInstanceCreation)
	 */
	IMethodBinding resolveConstructor(ClassInstanceCreation expression) {
		if (this.modificationCount != this.ast.modificationCount()) {
			return null;
		}
		AstNode node = (AstNode) this.newAstToOldAst.get(expression);
		if (node instanceof AnonymousLocalTypeDeclaration) {
			AnonymousLocalTypeDeclaration anonymousLocalTypeDeclaration = (AnonymousLocalTypeDeclaration) node;
			return this.getMethodBinding(anonymousLocalTypeDeclaration.allocation.binding);
		} else if (node instanceof AllocationExpression) {
			return this.getMethodBinding(((AllocationExpression)node).binding);
		}
		return null;
	}

	/*
	 * @see BindingResolver#resolveConstructor(ConstructorInvocation)
	 */
	IMethodBinding resolveConstructor(ConstructorInvocation expression) {
		if (this.modificationCount != this.ast.modificationCount()) {
			return null;
		}
		AstNode node = (AstNode) this.newAstToOldAst.get(expression);
		if (node instanceof MessageSend) {
			MessageSend messageSend = (MessageSend) node;
			return this.getMethodBinding(messageSend.binding);
		}
		return null;
	}

	/*
	 * @see BindingResolver#resolveConstructor(SuperConstructorInvocation)
	 */
	IMethodBinding resolveConstructor(SuperConstructorInvocation expression) {
		if (this.modificationCount != this.ast.modificationCount()) {
			return null;
		}
		AstNode node = (AstNode) this.newAstToOldAst.get(expression);
		if (node instanceof MessageSend) {
			MessageSend messageSend = (MessageSend) node;
			return this.getMethodBinding(messageSend.binding);
		}
		return null;
	}
	/*
	 * @see BindingResolver#resolveType(AnonymousClassDeclaration)
	 */
	ITypeBinding resolveType(AnonymousClassDeclaration type) {
		if (this.modificationCount != this.ast.modificationCount()) {
			return null;
		}
		org.eclipse.jdt.internal.compiler.ast.AnonymousLocalTypeDeclaration anonymousLocalTypeDeclaration = (org.eclipse.jdt.internal.compiler.ast.AnonymousLocalTypeDeclaration) this.newAstToOldAst.get(type);
		if (anonymousLocalTypeDeclaration != null) {
			ITypeBinding typeBinding = this.getTypeBinding(anonymousLocalTypeDeclaration.binding);
			this.bindingsToAstNodes.put(typeBinding, type);
			return typeBinding;
		}
		return super.resolveType(type);
	}

}