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

package org.eclipse.jdt.core.dom;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.jdt.core.compiler.CharOperation;
import org.eclipse.jdt.internal.compiler.ast.*;
import org.eclipse.jdt.internal.compiler.lookup.*;

/**
 * Internal class for resolving bindings using old ASTs.
 */
class DefaultBindingResolver extends BindingResolver {
	
	private static final char[][] JAVA_LANG_STRINGBUFFER = new char[][] {"java".toCharArray(), "lang".toCharArray(), "StringBuffer".toCharArray()}; //$NON-NLS-3$//$NON-NLS-2$//$NON-NLS-1$
	private static final char[][] JAVA_LANG_EXCEPTION = new char[][] {"java".toCharArray(), "lang".toCharArray(), "Exception".toCharArray()};//$NON-NLS-3$//$NON-NLS-2$//$NON-NLS-1$

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
	 * This map is used to get an ast node from its binding key.
	 */
	Map bindingKeysToAstNodes;
	
	/**
	 * This map is used to get a binding from its ast node
	 */
	Map astNodesToBindings;
	
	/**
	 * This map is used to retrieve the corresponding block scope for a ast node
	 */
	Map astNodesToBlockScope;
	
	/**
	 * Compilation unit scope
	 */
	private CompilationUnitScope scope;
	
	/**
	 * Constructor for DefaultBindingResolver.
	 */
	DefaultBindingResolver() {
		this.newAstToOldAst = new HashMap();
		this.compilerBindingsToASTBindings = new HashMap();
		this.bindingsToAstNodes = new HashMap();
		this.astNodesToBindings = new HashMap();
		this.astNodesToBlockScope = new HashMap();
		this.bindingKeysToAstNodes = new HashMap();
	}
	
	/**
	 * Constructor for DefaultBindingResolver.
	 */
	DefaultBindingResolver(CompilationUnitScope scope) {
		this();
		this.scope = scope;
	}
	
	/*
	 * Method declared on BindingResolver.
	 */
	IBinding resolveName(Name name) {
		AstNode node = (AstNode) this.newAstToOldAst.get(name);
		int index = name.index;
		if (node instanceof QualifiedNameReference) {
			QualifiedNameReference qualifiedNameReference = (QualifiedNameReference) node;
			final char[][] tokens = qualifiedNameReference.tokens;
			int qualifiedNameLength = tokens.length;
			int indexInQualifiedName = qualifiedNameLength - index; // one-based
			int indexOfFirstFieldBinding = qualifiedNameReference.indexOfFirstFieldBinding; // one-based
			int otherBindingLength = qualifiedNameLength - indexOfFirstFieldBinding;
			if (indexInQualifiedName < indexOfFirstFieldBinding) {
				// a extra lookup is required
				BlockScope internalScope = (BlockScope) this.astNodesToBlockScope.get(name);
				Binding binding = null;
				if (internalScope == null) {
					binding = this.scope.getTypeOrPackage(CharOperation.subarray(tokens, 0, indexInQualifiedName));
				} else {
					binding = internalScope.getTypeOrPackage(CharOperation.subarray(tokens, 0, indexInQualifiedName));
				}
				if (binding instanceof org.eclipse.jdt.internal.compiler.lookup.PackageBinding) {
					return this.getPackageBinding((org.eclipse.jdt.internal.compiler.lookup.PackageBinding)binding);
				} else if (binding instanceof org.eclipse.jdt.internal.compiler.lookup.TypeBinding) {
					// it is a type
					return this.getTypeBinding((org.eclipse.jdt.internal.compiler.lookup.TypeBinding)binding);
				}
			} else if (indexInQualifiedName == indexOfFirstFieldBinding) {
				if (qualifiedNameReference.isTypeReference()) {
					return this.getTypeBinding((ReferenceBinding)qualifiedNameReference.binding);
				} else {
					Binding binding = qualifiedNameReference.binding;
					if (binding != null) {
						if (binding.isValidBinding()) {
							return this.getVariableBinding((org.eclipse.jdt.internal.compiler.lookup.VariableBinding) binding);				
						} else {
							if (binding instanceof ProblemFieldBinding) {
								ProblemFieldBinding problemFieldBinding = (ProblemFieldBinding) binding;
								switch(problemFieldBinding.problemId()) {
									case ProblemReasons.NotVisible : 
									case ProblemReasons.NonStaticReferenceInStaticContext :
										ReferenceBinding declaringClass = problemFieldBinding.declaringClass;
										if (declaringClass != null) {
											FieldBinding exactBinding = declaringClass.getField(tokens[tokens.length - 1]);
											if (exactBinding != null) {
												IVariableBinding variableBinding = (IVariableBinding) this.compilerBindingsToASTBindings.get(exactBinding);
												if (variableBinding != null) {
													return variableBinding;
												}
												variableBinding = new VariableBinding(this, exactBinding);
												this.compilerBindingsToASTBindings.put(exactBinding, variableBinding);
												return variableBinding;
											}
										}
										break;
								}
							}
						}
					}
				}
			} else {
				/* This is the case for a name which is part of a qualified name that
				 * cannot be resolved. See PR 13063.
				 */
				if (qualifiedNameReference.otherBindings == null || (otherBindingLength - index - 1) < 0) {
					return null;
				} else {
					return this.getVariableBinding(qualifiedNameReference.otherBindings[otherBindingLength - index - 1]);				
				}
			}
		} else if (node instanceof QualifiedTypeReference) {
			QualifiedTypeReference qualifiedTypeReference = (QualifiedTypeReference) node;
			if (qualifiedTypeReference.resolvedType == null) {
				return null;
			}
			if (index == 0) {
				return this.getTypeBinding(qualifiedTypeReference.resolvedType.leafComponentType());
			} else {
				int qualifiedTypeLength = qualifiedTypeReference.tokens.length;
				int indexInQualifiedName = qualifiedTypeLength - index; // one-based
				if (indexInQualifiedName >= 0) {
					BlockScope internalScope = (BlockScope) this.astNodesToBlockScope.get(name);
					Binding binding = null;
					if (internalScope == null) {
						binding = this.scope.getTypeOrPackage(CharOperation.subarray(qualifiedTypeReference.tokens, 0, indexInQualifiedName));
					} else {
						binding = internalScope.getTypeOrPackage(CharOperation.subarray(qualifiedTypeReference.tokens, 0, indexInQualifiedName));
					}
					if (binding instanceof org.eclipse.jdt.internal.compiler.lookup.PackageBinding) {
						return this.getPackageBinding((org.eclipse.jdt.internal.compiler.lookup.PackageBinding)binding);
					} else if (binding instanceof org.eclipse.jdt.internal.compiler.lookup.TypeBinding) {
						// it is a type
						return this.getTypeBinding((org.eclipse.jdt.internal.compiler.lookup.TypeBinding)binding);
					} else {
						return null;
					}
				}
			}
		} else if (node instanceof ImportReference) {
			ImportReference importReference = (ImportReference) node;
			int importReferenceLength = importReference.tokens.length;
			int indexInImportReference = importReferenceLength - index; // one-based
			if (indexInImportReference >= 0) {
				Binding binding = this.scope.getTypeOrPackage(CharOperation.subarray(importReference.tokens, 0, indexInImportReference));
				if (binding != null) {
					if (binding instanceof org.eclipse.jdt.internal.compiler.lookup.PackageBinding) {
						return this.getPackageBinding((org.eclipse.jdt.internal.compiler.lookup.PackageBinding)binding);
					} else if (binding instanceof org.eclipse.jdt.internal.compiler.lookup.TypeBinding) {
						// it is a type
						return this.getTypeBinding((org.eclipse.jdt.internal.compiler.lookup.TypeBinding)binding);
					} else {
						return null;
					}
				}
			}
		} else if (node instanceof CompilationUnitDeclaration) {
			CompilationUnitDeclaration compilationUnitDeclaration = (CompilationUnitDeclaration) node;
			org.eclipse.jdt.internal.compiler.ast.TypeDeclaration[] types = compilationUnitDeclaration.types;
			if (types == null || types.length == 0) {
				return null;
			}
			org.eclipse.jdt.internal.compiler.ast.TypeDeclaration type = types[0];
			if (type != null) {
				ITypeBinding typeBinding = this.getTypeBinding(type.binding);
				if (typeBinding != null) {
					return typeBinding.getPackage();
				}
			}
		} else if (node instanceof AbstractMethodDeclaration) {
			AbstractMethodDeclaration methodDeclaration = (AbstractMethodDeclaration) node;
			if (methodDeclaration != null) {
				IMethodBinding methodBinding = this.getMethodBinding(methodDeclaration.binding);
				if (methodBinding != null) {
					return methodBinding;
				}
			}
		} else if (node instanceof org.eclipse.jdt.internal.compiler.ast.TypeDeclaration) {
			org.eclipse.jdt.internal.compiler.ast.TypeDeclaration typeDeclaration = (org.eclipse.jdt.internal.compiler.ast.TypeDeclaration) node;
			ITypeBinding typeBinding = this.getTypeBinding(typeDeclaration.binding);
			if (typeBinding != null) {
				return typeBinding;
			}
		} if (node instanceof SingleNameReference) {
			SingleNameReference singleNameReference = (SingleNameReference) node;
			if (singleNameReference.isTypeReference()) {
				return this.getTypeBinding((ReferenceBinding)singleNameReference.binding);
			} else {
				// this is a variable or a field
				Binding binding = singleNameReference.binding;
				if (binding != null) {
					if (binding.isValidBinding()) {
						return this.getVariableBinding((org.eclipse.jdt.internal.compiler.lookup.VariableBinding) binding);				
					} else {
						/*
						 * http://dev.eclipse.org/bugs/show_bug.cgi?id=24449
						 */
						if (binding instanceof ProblemFieldBinding) {
							ProblemFieldBinding problemFieldBinding = (ProblemFieldBinding) binding;
							switch(problemFieldBinding.problemId()) {
								case ProblemReasons.NotVisible : 
								case ProblemReasons.NonStaticReferenceInStaticContext :
								case ProblemReasons.NonStaticReferenceInConstructorInvocation :
									ReferenceBinding declaringClass = problemFieldBinding.declaringClass;
									FieldBinding exactBinding = declaringClass.getField(problemFieldBinding.name);
									if (exactBinding != null) {
										IVariableBinding variableBinding2 = (IVariableBinding) this.compilerBindingsToASTBindings.get(exactBinding);
										if (variableBinding2 != null) {
											return variableBinding2;
										}
										variableBinding2 = new VariableBinding(this, exactBinding);
										this.compilerBindingsToASTBindings.put(exactBinding, variableBinding2);
										return variableBinding2;
									}
									break;
							}
						}
					}
	 			}				
			}
		} else if (node instanceof QualifiedSuperReference) {
			QualifiedSuperReference qualifiedSuperReference = (QualifiedSuperReference) node;
			return this.getTypeBinding(qualifiedSuperReference.qualification.resolvedType);
		} else if (node instanceof LocalDeclaration) {
			return this.getVariableBinding(((LocalDeclaration)node).binding);
		} else if (node instanceof FieldReference) {
			return getVariableBinding(((FieldReference) node).binding);
		} else if (node instanceof SingleTypeReference) {
			SingleTypeReference singleTypeReference = (SingleTypeReference) node;
			org.eclipse.jdt.internal.compiler.lookup.TypeBinding binding = singleTypeReference.resolvedType;
			if (binding != null && binding instanceof org.eclipse.jdt.internal.compiler.lookup.TypeBinding) { // TODO (olivier) Unnecessary instanceof check, the expression of type TypeBinding is already compatible with type TypeBinding
				return this.getTypeBinding(binding.leafComponentType());
			}
		} else if (node instanceof org.eclipse.jdt.internal.compiler.ast.FieldDeclaration) {
			org.eclipse.jdt.internal.compiler.ast.FieldDeclaration fieldDeclaration = (org.eclipse.jdt.internal.compiler.ast.FieldDeclaration) node;
			return this.getVariableBinding(fieldDeclaration.binding);
		} else if (node instanceof MessageSend) {
			MessageSend messageSend = (MessageSend) node;
			return getMethodBinding(messageSend.binding);
		}
		return null;
	}

	/*
	 * Method declared on BindingResolver.
	 */
	ITypeBinding resolveType(Type type) {
		// retrieve the old ast node
		AstNode node = (AstNode) this.newAstToOldAst.get(type);
		org.eclipse.jdt.internal.compiler.lookup.TypeBinding binding = null;
		if (node != null) {
			if (node instanceof TypeReference) {
				TypeReference typeReference = (TypeReference) node;
				binding = typeReference.resolvedType;
			} else if (node instanceof SingleNameReference && ((SingleNameReference)node).isTypeReference()) {
				binding = (org.eclipse.jdt.internal.compiler.lookup.TypeBinding) (((SingleNameReference)node).binding);
			} else if (node instanceof QualifiedNameReference && ((QualifiedNameReference)node).isTypeReference()) {
				binding = (org.eclipse.jdt.internal.compiler.lookup.TypeBinding) (((QualifiedNameReference)node).binding);
			} else if (node instanceof ArrayAllocationExpression) {
				binding = ((ArrayAllocationExpression) node).resolvedType;
			}
			if (binding != null) {
				if (type.isArrayType()) {
					ArrayType arrayType = (ArrayType) type;
					if (binding.isArrayType()) {
						ArrayBinding arrayBinding = (ArrayBinding) binding;
						return getTypeBinding(this.scope.createArray(arrayBinding.leafComponentType, arrayType.getDimensions()));
					} else {
						return getTypeBinding(this.scope.createArray(binding, arrayType.getDimensions()));
					}
				} else {
					if (binding.isArrayType()) {
						ArrayBinding arrayBinding = (ArrayBinding) binding;
						return getTypeBinding(arrayBinding.leafComponentType);
					} else {
						return getTypeBinding(binding);
					}
				}
			}
		} else if (type.isPrimitiveType()) {
			if (((PrimitiveType) type).getPrimitiveTypeCode() == PrimitiveType.VOID) {
				return this.getTypeBinding(BaseTypes.VoidBinding);
			}
		}
		return null;
	}
	
	/*
	 * Method declared on BindingResolver.
	 */
	ITypeBinding resolveWellKnownType(String name) {
		if (("boolean".equals(name))//$NON-NLS-1$
			|| ("char".equals(name))//$NON-NLS-1$
			|| ("byte".equals(name))//$NON-NLS-1$
			|| ("short".equals(name))//$NON-NLS-1$
			|| ("int".equals(name))//$NON-NLS-1$
			|| ("long".equals(name))//$NON-NLS-1$
			|| ("float".equals(name))//$NON-NLS-1$
			|| ("double".equals(name))//$NON-NLS-1$
			|| ("void".equals(name))) {//$NON-NLS-1$
			return this.getTypeBinding(this.scope.getBaseType(name.toCharArray()));
		} else if ("java.lang.Object".equals(name)) {//$NON-NLS-1$
			return this.getTypeBinding(this.scope.getJavaLangObject());
		} else if ("java.lang.String".equals(name)) {//$NON-NLS-1$
			return this.getTypeBinding(this.scope.getJavaLangString());
		} else if ("java.lang.StringBuffer".equals(name)) {//$NON-NLS-1$
			return this.getTypeBinding(this.scope.getType(JAVA_LANG_STRINGBUFFER));
		} else if ("java.lang.Throwable".equals(name)) {//$NON-NLS-1$
			return this.getTypeBinding(this.scope.getJavaLangThrowable());
		} else if ("java.lang.Exception".equals(name)) {//$NON-NLS-1$
			return this.getTypeBinding(this.scope.getType(JAVA_LANG_EXCEPTION));
		} else if ("java.lang.RuntimeException".equals(name)) {//$NON-NLS-1$
			return this.getTypeBinding(this.scope.getJavaLangRuntimeException());
		} else if ("java.lang.Error".equals(name)) {//$NON-NLS-1$
			return this.getTypeBinding(this.scope.getJavaLangError());
		} else if ("java.lang.Class".equals(name)) {//$NON-NLS-1$ 
			return this.getTypeBinding(this.scope.getJavaLangClass());
	    } else {
			return null;
		}
	}
	/*
	 * Method declared on BindingResolver.
	 */
	ITypeBinding resolveType(TypeDeclaration type) {
		final Object node = this.newAstToOldAst.get(type);
		if (node instanceof org.eclipse.jdt.internal.compiler.ast.TypeDeclaration) {
			org.eclipse.jdt.internal.compiler.ast.TypeDeclaration typeDeclaration = (org.eclipse.jdt.internal.compiler.ast.TypeDeclaration) node;
			if (typeDeclaration != null) {
				ITypeBinding typeBinding = this.getTypeBinding(typeDeclaration.binding);
				if (typeBinding == null) {
					return null;
				}
				this.bindingsToAstNodes.put(typeBinding, type);
				String key = typeBinding.getKey();
				if (key != null) {
					this.bindingKeysToAstNodes.put(key, type);				
				}
				return typeBinding;
			}
		}
		return null;
	}
	/*
	 * Method declared on BindingResolver.
	 */
	IMethodBinding resolveMethod(MethodDeclaration method) {
		Object oldNode = this.newAstToOldAst.get(method);
		if (oldNode instanceof AbstractMethodDeclaration) {
			AbstractMethodDeclaration methodDeclaration = (AbstractMethodDeclaration) oldNode;
			if (methodDeclaration != null) {
				IMethodBinding methodBinding = this.getMethodBinding(methodDeclaration.binding);
				if (methodBinding == null) {
					return null;
				}
				this.bindingsToAstNodes.put(methodBinding, method);
				String key = methodBinding.getKey();
				if (key != null) {
					this.bindingKeysToAstNodes.put(key, method);				
				}
				return methodBinding;
			}
		}
		return null;
	}
	/*
	 * Method declared on BindingResolver.
	 */
	IMethodBinding resolveMethod(MethodInvocation method) {
		Object oldNode = this.newAstToOldAst.get(method);
		if (oldNode instanceof MessageSend) {
			MessageSend messageSend = (MessageSend) oldNode;
			if (messageSend != null) {
				return this.getMethodBinding(messageSend.binding);
			}
		}
		return null;
	}
	/*
	 * Method declared on BindingResolver.
	 */
	IMethodBinding resolveMethod(SuperMethodInvocation method) {
		Object oldNode = this.newAstToOldAst.get(method);
		if (oldNode instanceof MessageSend) {
			MessageSend messageSend = (MessageSend) oldNode;
			if (messageSend != null) {
				return this.getMethodBinding(messageSend.binding);
			}
		}
		return null;
	}
	/*
	 * Method declared on BindingResolver.
	 */
	IVariableBinding resolveVariable(VariableDeclaration variable) {
		final Object node = this.newAstToOldAst.get(variable);
		if (node instanceof AbstractVariableDeclaration) {
			AbstractVariableDeclaration abstractVariableDeclaration = (AbstractVariableDeclaration) node;
			if (abstractVariableDeclaration instanceof org.eclipse.jdt.internal.compiler.ast.FieldDeclaration) {
				org.eclipse.jdt.internal.compiler.ast.FieldDeclaration fieldDeclaration = (org.eclipse.jdt.internal.compiler.ast.FieldDeclaration) abstractVariableDeclaration;
				IVariableBinding variableBinding = this.getVariableBinding(fieldDeclaration.binding);
				if (variableBinding == null) {
					return null;
				}
				this.bindingsToAstNodes.put(variableBinding, variable);
				String key = variableBinding.getKey();
				if (key != null) {
					this.bindingKeysToAstNodes.put(key, variable);				
				}
				return variableBinding;
			}
			IVariableBinding variableBinding = this.getVariableBinding(((LocalDeclaration) abstractVariableDeclaration).binding);
			if (variableBinding == null) {
				return null;
			}
			this.bindingsToAstNodes.put(variableBinding, variable);
			String key = variableBinding.getKey();
			if (key != null) {
				this.bindingKeysToAstNodes.put(key, variable);				
			}
			return variableBinding;
		}
		return null;
	}
	/*
	 * Method declared on BindingResolver.
	 */
	IVariableBinding resolveVariable(FieldDeclaration variable) {
		final Object node = this.newAstToOldAst.get(variable);
		if (node instanceof org.eclipse.jdt.internal.compiler.ast.FieldDeclaration) {
			org.eclipse.jdt.internal.compiler.ast.FieldDeclaration fieldDeclaration = (org.eclipse.jdt.internal.compiler.ast.FieldDeclaration) node;
			IVariableBinding variableBinding = this.getVariableBinding(fieldDeclaration.binding);
			if (variableBinding == null) {
				return null;
			}
			this.bindingsToAstNodes.put(variableBinding, variable);
			String key = variableBinding.getKey();
			if (key != null) {
				this.bindingKeysToAstNodes.put(key, variable);				
			}
			return variableBinding;
		}
		return null;
	}
	/*
	 * Method declared on BindingResolver.
	 */
	ITypeBinding resolveExpressionType(Expression expression) {
		if (expression instanceof ClassInstanceCreation) {
			AstNode astNode = (AstNode) this.newAstToOldAst.get(expression);
			if (astNode instanceof org.eclipse.jdt.internal.compiler.ast.TypeDeclaration) {
				org.eclipse.jdt.internal.compiler.ast.TypeDeclaration typeDeclaration = (org.eclipse.jdt.internal.compiler.ast.TypeDeclaration) astNode;
				if (typeDeclaration != null) {
					ITypeBinding typeBinding = this.getTypeBinding(typeDeclaration.binding);
					if (typeBinding == null) {
						return null;
					}
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
				return null;
			}
			return this.getTypeBinding(oldAst.binding);
		} else if (expression instanceof ArrayCreation) {
			ArrayAllocationExpression arrayAllocationExpression = (ArrayAllocationExpression) this.newAstToOldAst.get(expression);
			return this.getTypeBinding(arrayAllocationExpression.resolvedType);
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
			return this.getTypeBinding(castExpression.resolvedType);
		} else if (expression instanceof StringLiteral) {
			return this.getTypeBinding(this.scope.getJavaLangString());
		} else if (expression instanceof TypeLiteral) {
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
			return this.getTypeBinding(operatorExpression.resolvedType);
		} else if (expression instanceof InstanceofExpression) {
			org.eclipse.jdt.internal.compiler.ast.InstanceOfExpression instanceOfExpression = (org.eclipse.jdt.internal.compiler.ast.InstanceOfExpression) this.newAstToOldAst.get(expression);
			return this.getTypeBinding(instanceOfExpression.resolvedType);
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
			return this.getTypeBinding(arrayReference.resolvedType);
		} else if (expression instanceof ThisExpression) {
			ThisReference thisReference = (ThisReference) this.newAstToOldAst.get(expression);
			BlockScope blockScope = (BlockScope) this.astNodesToBlockScope.get(expression);
			if (blockScope == null) {
				return null;
			}
			return this.getTypeBinding(thisReference.resolveType(blockScope));
		} else if (expression instanceof MethodInvocation
				|| expression instanceof SuperMethodInvocation) {
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
		} else if (expression instanceof ConditionalExpression) {
			org.eclipse.jdt.internal.compiler.ast.ConditionalExpression conditionalExpression = (org.eclipse.jdt.internal.compiler.ast.ConditionalExpression) this.newAstToOldAst.get(expression);
			return this.getTypeBinding(conditionalExpression.resolvedType);
		} else if (expression instanceof VariableDeclarationExpression) {
			VariableDeclarationExpression variableDeclarationExpression = (VariableDeclarationExpression) expression;
			Type type = variableDeclarationExpression.getType();
			if (type != null) {
				return type.resolveBinding();
			}
		}
		return null;
	}

	/*
	 * @see BindingResolver#resolveField(FieldAccess)
	 */
	IVariableBinding resolveField(FieldAccess fieldAccess) {
		Object oldNode = this.newAstToOldAst.get(fieldAccess);
		if (oldNode instanceof FieldReference) {
			FieldReference fieldReference = (FieldReference) oldNode;
			if (fieldReference != null) {
				return this.getVariableBinding(fieldReference.binding);
			}
		}
		return null;
	}

	/*
	 * @see BindingResolver#resolveField(SuperFieldAccess)
	 */
	IVariableBinding resolveField(SuperFieldAccess fieldAccess) {
		Object oldNode = this.newAstToOldAst.get(fieldAccess);
		if (oldNode instanceof FieldReference) {
			FieldReference fieldReference = (FieldReference) oldNode;
			if (fieldReference != null) {
				return this.getVariableBinding(fieldReference.binding);
			}
		}
		return null;
	}

	/*
	 * @see BindingResolver#resolveImport(ImportDeclaration)
	 */
	IBinding resolveImport(ImportDeclaration importDeclaration) {
		AstNode node = (AstNode) this.newAstToOldAst.get(importDeclaration);
		if (node instanceof ImportReference) {
			ImportReference importReference = (ImportReference) node;
			if (importReference.onDemand) {
				Binding binding = this.scope.getTypeOrPackage(CharOperation.subarray(importReference.tokens, 0, importReference.tokens.length));
				if (binding != null) {
					if (binding.bindingType() == BindingIds.PACKAGE) {
						IPackageBinding packageBinding = this.getPackageBinding((org.eclipse.jdt.internal.compiler.lookup.PackageBinding) binding);
						if (packageBinding == null) {
							return null;
						}
						return packageBinding;
					} else {
						// if it is not a package, it has to be a type
						ITypeBinding typeBinding = this.getTypeBinding((org.eclipse.jdt.internal.compiler.lookup.TypeBinding) binding);
						if (typeBinding == null) {
							return null;
						}
						return typeBinding;
					}
				}
			} else {
				Binding binding = this.scope.getTypeOrPackage(importReference.tokens);
				if (binding != null && binding instanceof org.eclipse.jdt.internal.compiler.lookup.TypeBinding) {
					ITypeBinding typeBinding = this.getTypeBinding((org.eclipse.jdt.internal.compiler.lookup.TypeBinding) binding);
					return typeBinding == null ? null : typeBinding;
				}
			}
		}
		return null;
	}

	/*
	 * @see BindingResolver#resolvePackage(PackageDeclaration)
	 */
	IPackageBinding resolvePackage(PackageDeclaration pkg) {
		AstNode node = (AstNode) this.newAstToOldAst.get(pkg);
		if (node instanceof ImportReference) {
			ImportReference importReference = (ImportReference) node;
			Binding binding = this.scope.getTypeOrPackage(CharOperation.subarray(importReference.tokens, 0, importReference.tokens.length));
			if ((binding != null) && (binding.isValidBinding())) {
				IPackageBinding packageBinding = this.getPackageBinding((org.eclipse.jdt.internal.compiler.lookup.PackageBinding) binding);
				if (packageBinding == null) {
					return null;
				}
				this.bindingsToAstNodes.put(packageBinding, pkg);
				String key = packageBinding.getKey();
				if (key != null) {
					this.bindingKeysToAstNodes.put(key, pkg);				
				}
				return packageBinding;
			}
		}
		return null;
	}

	/*
	 * Method declared on BindingResolver.
	 */
	public ASTNode findDeclaringNode(IBinding binding) {
		if (binding == null) {
			return null;
		}
		return (ASTNode) this.bindingsToAstNodes.get(binding);
	}
	
	ASTNode findDeclaringNode(String bindingKey) {
		if (bindingKey == null) {
			return null;
		}
		return (ASTNode) this.bindingKeysToAstNodes.get(bindingKey);
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
	void updateKey(ASTNode node, ASTNode newNode) {
		Object astNode = this.newAstToOldAst.remove(node);
		if (astNode != null) {
			this.newAstToOldAst.put(newNode, astNode);
		}
	}
		
	/*
	 * Method declared on BindingResolver.
	 */
	protected ITypeBinding getTypeBinding(org.eclipse.jdt.internal.compiler.lookup.TypeBinding referenceBinding) {
		if (referenceBinding == null) {
			return null;
		} else if (!referenceBinding.isValidBinding()) {
			switch(referenceBinding.problemId()) {
				case ProblemReasons.NotVisible : 
				case ProblemReasons.NonStaticReferenceInStaticContext :
					if (referenceBinding instanceof ProblemReferenceBinding) {
						ProblemReferenceBinding problemReferenceBinding = (ProblemReferenceBinding) referenceBinding;
						Binding binding2 = problemReferenceBinding.original;
						if (binding2 != null && binding2 instanceof org.eclipse.jdt.internal.compiler.lookup.TypeBinding) {
							TypeBinding binding = (TypeBinding) this.compilerBindingsToASTBindings.get(binding2);
							if (binding != null) {
								return binding;
							}
							binding = new TypeBinding(this, (org.eclipse.jdt.internal.compiler.lookup.TypeBinding) binding2);
							this.compilerBindingsToASTBindings.put(binding2, binding);
							return binding;
						} 
					}
			}
			return null;
		} else {
			TypeBinding binding = (TypeBinding) this.compilerBindingsToASTBindings.get(referenceBinding);
			if (binding != null) {
				return binding;
			}
			binding = new TypeBinding(this, referenceBinding);
			this.compilerBindingsToASTBindings.put(referenceBinding, binding);
			return binding;
		}
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
		binding = new PackageBinding(packageBinding);
		this.compilerBindingsToASTBindings.put(packageBinding, binding);
		return binding;
	}
	/*
	 * Method declared on BindingResolver.
	 */
	protected IVariableBinding getVariableBinding(org.eclipse.jdt.internal.compiler.lookup.VariableBinding variableBinding) {
 		if (variableBinding != null) {
	 		if (variableBinding.isValidBinding()) {
				IVariableBinding binding = (IVariableBinding) this.compilerBindingsToASTBindings.get(variableBinding);
				if (binding != null) {
					return binding;
				}
				binding = new VariableBinding(this, variableBinding);
				this.compilerBindingsToASTBindings.put(variableBinding, binding);
				return binding;
	 		} else {
				/*
				 * http://dev.eclipse.org/bugs/show_bug.cgi?id=24449
				 */
				if (variableBinding instanceof ProblemFieldBinding) {
					ProblemFieldBinding problemFieldBinding = (ProblemFieldBinding) variableBinding;
					switch(problemFieldBinding.problemId()) {
						case ProblemReasons.NotVisible : 
						case ProblemReasons.NonStaticReferenceInStaticContext :
						case ProblemReasons.NonStaticReferenceInConstructorInvocation :
							ReferenceBinding declaringClass = problemFieldBinding.declaringClass;
							FieldBinding exactBinding = declaringClass.getField(problemFieldBinding.name);
							if (exactBinding != null) {
								IVariableBinding variableBinding2 = (IVariableBinding) this.compilerBindingsToASTBindings.get(exactBinding);
								if (variableBinding2 != null) {
									return variableBinding2;
								}
								variableBinding2 = new VariableBinding(this, exactBinding);
								this.compilerBindingsToASTBindings.put(exactBinding, variableBinding2);
								return variableBinding2;
							}
							break;
					}
				}
	 		}
 		}
		return null;
	}
	
	/*
	 * Method declared on BindingResolver.
	 */
	protected IMethodBinding getMethodBinding(org.eclipse.jdt.internal.compiler.lookup.MethodBinding methodBinding) {
		if (methodBinding != null) {
			if (methodBinding.isValidBinding()) {
				IMethodBinding binding = (IMethodBinding) this.compilerBindingsToASTBindings.get(methodBinding);
				if (binding != null) {
					return binding;
				}
				binding = new MethodBinding(this, methodBinding);
				this.compilerBindingsToASTBindings.put(methodBinding, binding);
				return binding;
			} else {
				/*
				 * http://dev.eclipse.org/bugs/show_bug.cgi?id=23597
				 */
				switch(methodBinding.problemId()) {
					case ProblemReasons.NotVisible : 
					case ProblemReasons.NonStaticReferenceInStaticContext :
					case ProblemReasons.NonStaticReferenceInConstructorInvocation :
						ReferenceBinding declaringClass = methodBinding.declaringClass;
						if (declaringClass != null) {
							org.eclipse.jdt.internal.compiler.lookup.MethodBinding exactBinding = declaringClass.getExactMethod(methodBinding.selector, methodBinding.parameters);
							if (exactBinding != null) {
								IMethodBinding binding = (IMethodBinding) this.compilerBindingsToASTBindings.get(exactBinding);
								if (binding != null) {
									return binding;
								}
								binding = new MethodBinding(this, exactBinding);
								this.compilerBindingsToASTBindings.put(exactBinding, binding);
								return binding;
							}
						}
						break;
				}
			}
		}
		return null;
	}
	
	/*
	 * @see BindingResolver#resolveConstructor(ClassInstanceCreation)
	 */
	IMethodBinding resolveConstructor(ClassInstanceCreation expression) {
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
		AstNode node = (AstNode) this.newAstToOldAst.get(expression);
		if (node instanceof ExplicitConstructorCall) {
			ExplicitConstructorCall explicitConstructorCall = (ExplicitConstructorCall) node;
			return this.getMethodBinding(explicitConstructorCall.binding);
		}
		return null;
	}

	/*
	 * @see BindingResolver#resolveConstructor(SuperConstructorInvocation)
	 */
	IMethodBinding resolveConstructor(SuperConstructorInvocation expression) {
		AstNode node = (AstNode) this.newAstToOldAst.get(expression);
		if (node instanceof ExplicitConstructorCall) {
			ExplicitConstructorCall explicitConstructorCall = (ExplicitConstructorCall) node;
			return this.getMethodBinding(explicitConstructorCall.binding);
		}
		return null;
	}
	/*
	 * @see BindingResolver#resolveType(AnonymousClassDeclaration)
	 */
	ITypeBinding resolveType(AnonymousClassDeclaration type) {
		final Object node = this.newAstToOldAst.get(type);
		if (node instanceof org.eclipse.jdt.internal.compiler.ast.AnonymousLocalTypeDeclaration) {
			org.eclipse.jdt.internal.compiler.ast.AnonymousLocalTypeDeclaration anonymousLocalTypeDeclaration = (org.eclipse.jdt.internal.compiler.ast.AnonymousLocalTypeDeclaration) node;
			if (anonymousLocalTypeDeclaration != null) {
				ITypeBinding typeBinding = this.getTypeBinding(anonymousLocalTypeDeclaration.binding);
				if (typeBinding == null) {
					return null;
				}
				this.bindingsToAstNodes.put(typeBinding, type);
				String key = typeBinding.getKey();
				if (key != null) {
					this.bindingKeysToAstNodes.put(key, type);				
				}
				return typeBinding;
			}
		}
		return null;
	}

	AstNode getCorrespondingNode(ASTNode currentNode) {
		return (AstNode) this.newAstToOldAst.get(currentNode);
	} 
	/**
	 * @see org.eclipse.jdt.core.dom.BindingResolver#recordScope(ASTNode, BlockScope)
	 */
	void recordScope(ASTNode astNode, BlockScope blockScope) {
		this.astNodesToBlockScope.put(astNode, blockScope);
	}
}
