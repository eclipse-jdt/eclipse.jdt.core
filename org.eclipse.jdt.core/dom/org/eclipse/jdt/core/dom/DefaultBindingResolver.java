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

import java.util.HashMap;
import java.util.Map;

import org.eclipse.jdt.core.compiler.CharOperation;
import org.eclipse.jdt.internal.compiler.ast.AbstractMethodDeclaration;
import org.eclipse.jdt.internal.compiler.ast.AbstractVariableDeclaration;
import org.eclipse.jdt.internal.compiler.ast.AllocationExpression;
import org.eclipse.jdt.internal.compiler.ast.ArrayAllocationExpression;
import org.eclipse.jdt.internal.compiler.ast.ArrayReference;
import org.eclipse.jdt.internal.compiler.ast.CharLiteral;
import org.eclipse.jdt.internal.compiler.ast.CompilationUnitDeclaration;
import org.eclipse.jdt.internal.compiler.ast.ExplicitConstructorCall;
import org.eclipse.jdt.internal.compiler.ast.FalseLiteral;
import org.eclipse.jdt.internal.compiler.ast.FieldReference;
import org.eclipse.jdt.internal.compiler.ast.ImplicitDocTypeReference;
import org.eclipse.jdt.internal.compiler.ast.ImportReference;
import org.eclipse.jdt.internal.compiler.ast.JavadocAllocationExpression;
import org.eclipse.jdt.internal.compiler.ast.JavadocFieldReference;
import org.eclipse.jdt.internal.compiler.ast.JavadocMessageSend;
import org.eclipse.jdt.internal.compiler.ast.JavadocQualifiedTypeReference;
import org.eclipse.jdt.internal.compiler.ast.JavadocSingleTypeReference;
import org.eclipse.jdt.internal.compiler.ast.Literal;
import org.eclipse.jdt.internal.compiler.ast.LocalDeclaration;
import org.eclipse.jdt.internal.compiler.ast.MessageSend;
import org.eclipse.jdt.internal.compiler.ast.OperatorExpression;
import org.eclipse.jdt.internal.compiler.ast.QualifiedNameReference;
import org.eclipse.jdt.internal.compiler.ast.QualifiedSuperReference;
import org.eclipse.jdt.internal.compiler.ast.QualifiedTypeReference;
import org.eclipse.jdt.internal.compiler.ast.SingleNameReference;
import org.eclipse.jdt.internal.compiler.ast.SingleTypeReference;
import org.eclipse.jdt.internal.compiler.ast.StringLiteralConcatenation;
import org.eclipse.jdt.internal.compiler.ast.ThisReference;
import org.eclipse.jdt.internal.compiler.ast.TrueLiteral;
import org.eclipse.jdt.internal.compiler.ast.TypeReference;
import org.eclipse.jdt.internal.compiler.lookup.ArrayBinding;
import org.eclipse.jdt.internal.compiler.lookup.BaseTypes;
import org.eclipse.jdt.internal.compiler.lookup.Binding;
import org.eclipse.jdt.internal.compiler.lookup.BindingIds;
import org.eclipse.jdt.internal.compiler.lookup.BlockScope;
import org.eclipse.jdt.internal.compiler.lookup.CompilationUnitScope;
import org.eclipse.jdt.internal.compiler.lookup.FieldBinding;
import org.eclipse.jdt.internal.compiler.lookup.ProblemFieldBinding;
import org.eclipse.jdt.internal.compiler.lookup.ProblemReasons;
import org.eclipse.jdt.internal.compiler.lookup.ProblemReferenceBinding;
import org.eclipse.jdt.internal.compiler.lookup.ReferenceBinding;

/**
 * Internal class for resolving bindings using old ASTs.
 * <p>
 * IMPORTANT: The methods on this class are synchronized. This is required
 * because there may be multiple clients in separate threads concurrently
 * reading an AST and asking for bindings for its nodes. These requests all
 * end up invoking instance methods on this class. There are various internal
 * tables and caches which are built and maintained in the course of looking
 * up bindings. To ensure that they remain coherent in the presence of multiple
 * threads, the methods are synchronized on the DefaultBindingResolver instance.
 * </p>
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
	synchronized IBinding resolveName(Name name) {
		org.eclipse.jdt.internal.compiler.ast.ASTNode node = (org.eclipse.jdt.internal.compiler.ast.ASTNode) this.newAstToOldAst.get(name);
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
				try {
					if (internalScope == null) {
						binding = this.scope.getTypeOrPackage(CharOperation.subarray(tokens, 0, indexInQualifiedName));
					} else {
						binding = internalScope.getTypeOrPackage(CharOperation.subarray(tokens, 0, indexInQualifiedName));
					}
				} catch (RuntimeException e) {
					// see https://bugs.eclipse.org/bugs/show_bug.cgi?id=53357
					// see https://bugs.eclipse.org/bugs/show_bug.cgi?id=63550
					// see https://bugs.eclipse.org/bugs/show_bug.cgi?id=64299
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
											FieldBinding exactBinding = declaringClass.getField(tokens[tokens.length - 1], true /*resolve*/);
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
				if (!qualifiedTypeReference.resolvedType.isValidBinding() && qualifiedTypeReference instanceof JavadocQualifiedTypeReference) {
					JavadocQualifiedTypeReference typeRef = (JavadocQualifiedTypeReference) node;
					if (typeRef.packageBinding != null) {
						return getPackageBinding(typeRef.packageBinding);
					}
				}
				return this.getTypeBinding(qualifiedTypeReference.resolvedType.leafComponentType());
			} else {
				int qualifiedTypeLength = qualifiedTypeReference.tokens.length;
				int indexInQualifiedName = qualifiedTypeLength - index; // one-based
				if (indexInQualifiedName >= 0) {
					BlockScope internalScope = (BlockScope) this.astNodesToBlockScope.get(name);
					Binding binding = null;
					try {
						if (internalScope == null) {
							binding = this.scope.getTypeOrPackage(CharOperation.subarray(qualifiedTypeReference.tokens, 0, indexInQualifiedName));
						} else {
							binding = internalScope.getTypeOrPackage(CharOperation.subarray(qualifiedTypeReference.tokens, 0, indexInQualifiedName));
						}
					} catch (RuntimeException e) {
						// see https://bugs.eclipse.org/bugs/show_bug.cgi?id=53357
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
				Binding binding = null;
				try {
					binding = this.scope.getTypeOrPackage(CharOperation.subarray(importReference.tokens, 0, indexInImportReference));
				} catch (RuntimeException e) {
					// see https://bugs.eclipse.org/bugs/show_bug.cgi?id=53357
				}
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
									FieldBinding exactBinding = declaringClass.getField(problemFieldBinding.name, true /*resolve*/);
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
			if (binding != null) {
				if (!binding.isValidBinding() && node instanceof JavadocSingleTypeReference) {
					JavadocSingleTypeReference typeRef = (JavadocSingleTypeReference) node;
					if (typeRef.packageBinding != null) {
						return getPackageBinding(typeRef.packageBinding);
					}
				}
				return this.getTypeBinding(binding.leafComponentType());
			}
		} else if (node instanceof org.eclipse.jdt.internal.compiler.ast.FieldDeclaration) {
			org.eclipse.jdt.internal.compiler.ast.FieldDeclaration fieldDeclaration = (org.eclipse.jdt.internal.compiler.ast.FieldDeclaration) node;
			return this.getVariableBinding(fieldDeclaration.binding);
		} else if (node instanceof MessageSend) {
			MessageSend messageSend = (MessageSend) node;
			return getMethodBinding(messageSend.binding);
		} else if (node instanceof AllocationExpression) {
			AllocationExpression allocation = (AllocationExpression) node;
			return getMethodBinding(allocation.binding);
		} else if (node instanceof ImplicitDocTypeReference) {
			ImplicitDocTypeReference implicitRef = (ImplicitDocTypeReference) node;
			return getTypeBinding(implicitRef.resolvedType);
		}
		return null;
	}

	/*
	 * Method declared on BindingResolver.
	 */
	synchronized ITypeBinding resolveType(Type type) {
		// retrieve the old ast node
		org.eclipse.jdt.internal.compiler.ast.ASTNode node = (org.eclipse.jdt.internal.compiler.ast.ASTNode) this.newAstToOldAst.get(type);
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
						return getTypeBinding(this.scope.createArrayType(arrayBinding.leafComponentType, arrayType.getDimensions()));
					} else {
						return getTypeBinding(this.scope.createArrayType(binding, arrayType.getDimensions()));
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
			/* Handle the void primitive type returned by getReturnType for a method declaration 
			 * that is a constructor declaration. It prevents null from being returned
			 */
			if (((PrimitiveType) type).getPrimitiveTypeCode() == PrimitiveType.VOID) {
				return this.getTypeBinding(BaseTypes.VoidBinding);
			}
		}
		return null;
	}
	
	/*
	 * Method declared on BindingResolver.
	 */
	synchronized ITypeBinding resolveWellKnownType(String name) {
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
			return this.getTypeBinding(this.scope.getType(JAVA_LANG_STRINGBUFFER, 3));
		} else if ("java.lang.Throwable".equals(name)) {//$NON-NLS-1$
			return this.getTypeBinding(this.scope.getJavaLangThrowable());
		} else if ("java.lang.Exception".equals(name)) {//$NON-NLS-1$
			return this.getTypeBinding(this.scope.getType(JAVA_LANG_EXCEPTION, 3));
		} else if ("java.lang.RuntimeException".equals(name)) {//$NON-NLS-1$
			return this.getTypeBinding(this.scope.getJavaLangRuntimeException());
		} else if ("java.lang.Error".equals(name)) {//$NON-NLS-1$
			return this.getTypeBinding(this.scope.getJavaLangError());
		} else if ("java.lang.Class".equals(name)) {//$NON-NLS-1$ 
			return this.getTypeBinding(this.scope.getJavaLangClass());
	    } else if ("java.lang.Cloneable".equals(name)) {//$NON-NLS-1$ 
			return this.getTypeBinding(this.scope.getJavaLangCloneable());
		} else if ("java.io.Serializable".equals(name)) {//$NON-NLS-1$ 
			return this.getTypeBinding(this.scope.getJavaIoSerializable());
		} else {
			return null;
		}
	}
	/*
	 * Method declared on BindingResolver.
	 */
	synchronized ITypeBinding resolveType(TypeDeclaration type) {
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
	synchronized IMethodBinding resolveMethod(MethodDeclaration method) {
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
	synchronized IMethodBinding resolveMethod(MethodInvocation method) {
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
	synchronized IMethodBinding resolveMethod(SuperMethodInvocation method) {
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
	synchronized IVariableBinding resolveVariable(VariableDeclaration variable) {
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
	synchronized ITypeBinding resolveExpressionType(Expression expression) {
		if (expression instanceof ClassInstanceCreation) {
			org.eclipse.jdt.internal.compiler.ast.ASTNode astNode = (org.eclipse.jdt.internal.compiler.ast.ASTNode) this.newAstToOldAst.get(expression);
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
			Object node = this.newAstToOldAst.get(expression);
			if (node instanceof OperatorExpression) {
				OperatorExpression operatorExpression = (OperatorExpression) node;
				return this.getTypeBinding(operatorExpression.resolvedType);
			} else if (node instanceof StringLiteralConcatenation) {
				StringLiteralConcatenation stringLiteralConcatenation = (StringLiteralConcatenation) node;
				return this.getTypeBinding(stringLiteralConcatenation.resolvedType);
			}
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
	synchronized IVariableBinding resolveField(FieldAccess fieldAccess) {
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
	synchronized IVariableBinding resolveField(SuperFieldAccess fieldAccess) {
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
	synchronized IBinding resolveImport(ImportDeclaration importDeclaration) {
		try {
			org.eclipse.jdt.internal.compiler.ast.ASTNode node = (org.eclipse.jdt.internal.compiler.ast.ASTNode) this.newAstToOldAst.get(importDeclaration);
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
		} catch(RuntimeException e) {
			// see https://bugs.eclipse.org/bugs/show_bug.cgi?id=53357
			// see https://bugs.eclipse.org/bugs/show_bug.cgi?id=63550
			// see https://bugs.eclipse.org/bugs/show_bug.cgi?id=64299
		}
		return null;
	}

	/*
	 * @see BindingResolver#resolvePackage(PackageDeclaration)
	 */
	synchronized IPackageBinding resolvePackage(PackageDeclaration pkg) {
		try {
			org.eclipse.jdt.internal.compiler.ast.ASTNode node = (org.eclipse.jdt.internal.compiler.ast.ASTNode) this.newAstToOldAst.get(pkg);
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
		} catch (RuntimeException e) {
			// see https://bugs.eclipse.org/bugs/show_bug.cgi?id=53357
			// see https://bugs.eclipse.org/bugs/show_bug.cgi?id=63550
			// see https://bugs.eclipse.org/bugs/show_bug.cgi?id=64299
		}
		return null;
	}

	/*
	 * Method declared on BindingResolver.
	 */
	synchronized ASTNode findDeclaringNode(IBinding binding) {
		if (binding == null) {
			return null;
		}
		return (ASTNode) this.bindingsToAstNodes.get(binding);
	}
	
	synchronized ASTNode findDeclaringNode(String bindingKey) {
		if (bindingKey == null) {
			return null;
		}
		return (ASTNode) this.bindingKeysToAstNodes.get(bindingKey);
	}
		
	/*
	 * Method declared on BindingResolver.
	 */
	synchronized void store(ASTNode node, org.eclipse.jdt.internal.compiler.ast.ASTNode oldASTNode) {
		this.newAstToOldAst.put(node, oldASTNode);
	}
	
	/*
	 * Method declared on BindingResolver.
	 */
	synchronized void updateKey(ASTNode node, ASTNode newNode) {
		Object astNode = this.newAstToOldAst.remove(node);
		if (astNode != null) {
			this.newAstToOldAst.put(newNode, astNode);
		}
	}
		
	/*
	 * Method declared on BindingResolver.
	 */
	synchronized ITypeBinding getTypeBinding(org.eclipse.jdt.internal.compiler.lookup.TypeBinding referenceBinding) {
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
	synchronized IPackageBinding getPackageBinding(org.eclipse.jdt.internal.compiler.lookup.PackageBinding packageBinding) {
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
	synchronized IVariableBinding getVariableBinding(org.eclipse.jdt.internal.compiler.lookup.VariableBinding variableBinding) {
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
							FieldBinding exactBinding = declaringClass.getField(problemFieldBinding.name, true /*resolve*/);
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
	synchronized IMethodBinding getMethodBinding(org.eclipse.jdt.internal.compiler.lookup.MethodBinding methodBinding) {
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
	synchronized IMethodBinding resolveConstructor(ClassInstanceCreation expression) {
		org.eclipse.jdt.internal.compiler.ast.ASTNode node = (org.eclipse.jdt.internal.compiler.ast.ASTNode) this.newAstToOldAst.get(expression);
		if (node != null && (node.bits & org.eclipse.jdt.internal.compiler.ast.ASTNode.IsAnonymousTypeMASK) != 0) {
			org.eclipse.jdt.internal.compiler.ast.TypeDeclaration anonymousLocalTypeDeclaration = (org.eclipse.jdt.internal.compiler.ast.TypeDeclaration) node;
			return this.getMethodBinding(anonymousLocalTypeDeclaration.allocation.binding);
		} else if (node instanceof AllocationExpression) {
			return this.getMethodBinding(((AllocationExpression)node).binding);
		}
		return null;
	}

	/*
	 * @see BindingResolver#resolveConstructor(ConstructorInvocation)
	 */
	synchronized IMethodBinding resolveConstructor(ConstructorInvocation expression) {
		org.eclipse.jdt.internal.compiler.ast.ASTNode node = (org.eclipse.jdt.internal.compiler.ast.ASTNode) this.newAstToOldAst.get(expression);
		if (node instanceof ExplicitConstructorCall) {
			ExplicitConstructorCall explicitConstructorCall = (ExplicitConstructorCall) node;
			return this.getMethodBinding(explicitConstructorCall.binding);
		}
		return null;
	}

	/*
	 * @see BindingResolver#resolveConstructor(SuperConstructorInvocation)
	 */
	synchronized IMethodBinding resolveConstructor(SuperConstructorInvocation expression) {
		org.eclipse.jdt.internal.compiler.ast.ASTNode node = (org.eclipse.jdt.internal.compiler.ast.ASTNode) this.newAstToOldAst.get(expression);
		if (node instanceof ExplicitConstructorCall) {
			ExplicitConstructorCall explicitConstructorCall = (ExplicitConstructorCall) node;
			return this.getMethodBinding(explicitConstructorCall.binding);
		}
		return null;
	}
	/*
	 * @see BindingResolver#resolveType(AnonymousClassDeclaration)
	 */
	synchronized ITypeBinding resolveType(AnonymousClassDeclaration type) {
		org.eclipse.jdt.internal.compiler.ast.ASTNode node = (org.eclipse.jdt.internal.compiler.ast.ASTNode) this.newAstToOldAst.get(type);
		if (node != null && (node.bits & org.eclipse.jdt.internal.compiler.ast.ASTNode.IsAnonymousTypeMASK) != 0) {
			org.eclipse.jdt.internal.compiler.ast.TypeDeclaration anonymousLocalTypeDeclaration = (org.eclipse.jdt.internal.compiler.ast.TypeDeclaration) node;
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

	synchronized org.eclipse.jdt.internal.compiler.ast.ASTNode getCorrespondingNode(ASTNode currentNode) {
		return (org.eclipse.jdt.internal.compiler.ast.ASTNode) this.newAstToOldAst.get(currentNode);
	} 
	/**
	 * @see org.eclipse.jdt.core.dom.BindingResolver#recordScope(ASTNode, BlockScope)
	 */
	synchronized void recordScope(ASTNode astNode, BlockScope blockScope) {
		this.astNodesToBlockScope.put(astNode, blockScope);
	}
	
	/* (non-Javadoc)
	 * @see BindingResolver#resolveReference(MemberRef)
     * @since 3.0
	 */
	synchronized IBinding resolveReference(MemberRef ref) {
		/*
		if (ref.getParent() != null) {
			Javadoc docComment = ref.getJavadoc();
			if (docComment != null) {
				org.eclipse.jdt.internal.compiler.ast.Javadoc javadoc = (org.eclipse.jdt.internal.compiler.ast.Javadoc) this.newAstToOldAst.get(docComment);
				if (javadoc != null) {
					int start = ref.getStartPosition();
					// search for compiler ast nodes with same position
					if (ref.getName() == null) {
						for (int i=0; i<javadoc.thrownExceptions.length; i++) {
							TypeReference typeRef = javadoc.thrownExceptions[i];
							if (typeRef.sourceStart==start) {
								return getTypeBinding(typeRef.resolvedType);
							}
						}
					}
					for (int i=0; i<javadoc.references.length; i++) {
						org.eclipse.jdt.internal.compiler.ast.Expression expression = javadoc.references[i];
						if (expression.sourceStart==start) {
							if (expression instanceof TypeReference) {
								return getTypeBinding(expression.resolvedType);
							}
							else if (expression instanceof JavadocFieldReference) {
								return getVariableBinding(((JavadocFieldReference)expression).binding);
							}
						}
					}
				}
			}
		}
		*/
		org.eclipse.jdt.internal.compiler.ast.Expression expression = (org.eclipse.jdt.internal.compiler.ast.Expression) this.newAstToOldAst.get(ref);
		if (expression instanceof TypeReference) {
			return getTypeBinding(expression.resolvedType);
		}
		else if (expression instanceof JavadocFieldReference) {
			return getVariableBinding(((JavadocFieldReference)expression).binding);
		}
		return null;
	}

	/* (non-Javadoc)
	 * @see BindingResolver#resolveReference(MethodRef)
     * @since 3.0
	 */
	synchronized IBinding resolveReference(MethodRef ref) {
		/*
		if (ref.getParent() != null) {
			Javadoc docComment = ref.getJavadoc();
			if (docComment != null) {
				org.eclipse.jdt.internal.compiler.ast.Javadoc javadoc = (org.eclipse.jdt.internal.compiler.ast.Javadoc) this.newAstToOldAst.get(docComment);
				if (javadoc != null) {
					int start = ref.getStartPosition();
					// search for compiler ast nodes with same position
					org.eclipse.jdt.internal.compiler.lookup.MethodBinding binding = null;
					for (int i=0; binding==null && i<javadoc.references.length; i++) {
						org.eclipse.jdt.internal.compiler.ast.Expression expression = javadoc.references[i];
						if (expression.sourceStart==start) {
							if (expression instanceof JavadocMessageSend) {
								return this.getMethodBinding(((JavadocMessageSend)expression).binding);
							}
							else if (expression instanceof JavadocAllocationExpression) {
								return this.getMethodBinding(((JavadocAllocationExpression)expression).binding);
							}
						}
					}
				}
			}
		}
		*/
		org.eclipse.jdt.internal.compiler.ast.Expression expression = (org.eclipse.jdt.internal.compiler.ast.Expression) this.newAstToOldAst.get(ref);
		if (expression instanceof JavadocMessageSend) {
			return this.getMethodBinding(((JavadocMessageSend)expression).binding);
		}
		else if (expression instanceof JavadocAllocationExpression) {
			return this.getMethodBinding(((JavadocAllocationExpression)expression).binding);
		}
		return null;
	}

}
