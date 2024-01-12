/*******************************************************************************
 * Copyright (c) 2023 Red Hat, Inc. and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *******************************************************************************/
package org.eclipse.jdt.internal.core;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.stream.Stream;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jdt.core.CompletionContext;
import org.eclipse.jdt.core.CompletionProposal;
import org.eclipse.jdt.core.CompletionRequestor;
import org.eclipse.jdt.core.Signature;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.Block;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.FieldAccess;
import org.eclipse.jdt.core.dom.IBinding;
import org.eclipse.jdt.core.dom.IMethodBinding;
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.core.dom.IVariableBinding;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.NodeFinder;
import org.eclipse.jdt.core.dom.Statement;
import org.eclipse.jdt.core.dom.TypeDeclaration;
import org.eclipse.jdt.core.dom.VariableDeclarationFragment;
import org.eclipse.jdt.core.dom.VariableDeclarationStatement;

class DOMCompletionEngine implements Runnable {

	private final int offset;
	private final CompilationUnit unit;
	private CompletionRequestor requestor;

	DOMCompletionEngine(int offset, CompilationUnit unit, CompletionRequestor requestor, IProgressMonitor monitor) {
		this.offset = offset;
		this.unit = unit;
		this.requestor = requestor;
	}

	private static Collection<? extends IBinding> visibleBindings(ASTNode node, int offset) {
		if (node instanceof Block block) {
			return ((List<Statement>)block.statements()).stream()
				.filter(statement -> statement.getStartPosition() < offset)
				.filter(VariableDeclarationStatement.class::isInstance)
				.map(VariableDeclarationStatement.class::cast)
				.flatMap(decl -> ((List<VariableDeclarationFragment>)decl.fragments()).stream())
				.map(VariableDeclarationFragment::resolveBinding)
				.toList();
		} else if (node instanceof MethodDeclaration method) {
			return Stream.of((List<ASTNode>)method.parameters(), (List<ASTNode>)method.typeParameters())
				.flatMap(List::stream)
				.map(DOMCodeSelector::resolveBinding)
				.filter(Objects::nonNull)
				.toList();
		} else if (node instanceof TypeDeclaration type) {
			VariableDeclarationFragment[] fields = Arrays.stream(type.getFields())
				.map(decl -> (List<VariableDeclarationFragment>)decl.fragments())
				.flatMap(List::stream)
				.toArray(VariableDeclarationFragment[]::new);
			return Stream.of(fields, type.getMethods(), type.getTypes())
				.flatMap(Arrays::stream)
				.map(DOMCodeSelector::resolveBinding)
				.filter(Objects::nonNull)
				.toList();
		}
		return List.of();
	}

	@Override
	public void run() {
		this.requestor.beginReporting();
		this.requestor.acceptContext(new CompletionContext());
		ASTNode toComplete = NodeFinder.perform(this.unit, this.offset, 0);
		if (toComplete instanceof FieldAccess fieldAccess) {
			processMembers(fieldAccess.resolveTypeBinding());
		} else if (toComplete.getParent() instanceof FieldAccess fieldAccess) {
			processMembers(fieldAccess.getExpression().resolveTypeBinding());
		}
		Collection<IBinding> scope = new HashSet<>();
		ASTNode current = toComplete;
		while (current != null) {
			scope.addAll(visibleBindings(current, this.offset));
			current = current.getParent();
		}
		// TODO also include other visible content: classpath, static methods...
		scope.stream().map(this::toProposal).forEach(this.requestor::accept);
		this.requestor.endReporting();
	}

	private void processMembers(ITypeBinding typeBinding) {
		if (typeBinding == null) {
			return;
		}
		Arrays.stream(typeBinding.getDeclaredFields()).map(this::toProposal).forEach(this.requestor::accept);
		Arrays.stream(typeBinding.getDeclaredMethods()).map(this::toProposal).forEach(this.requestor::accept);
		if (typeBinding.getInterfaces() != null) {
			Arrays.stream(typeBinding.getInterfaces()).forEach(this::processMembers);
		}
		processMembers(typeBinding.getSuperclass());
	}

	private CompletionProposal toProposal(IBinding binding) {
		int kind = 
			binding instanceof ITypeBinding ? CompletionProposal.TYPE_REF :
			binding instanceof IMethodBinding ? CompletionProposal.METHOD_REF :
			binding instanceof IVariableBinding variableBinding ? CompletionProposal.LOCAL_VARIABLE_REF :
			-1;
		CompletionProposal res = new CompletionProposal() {
			@Override
			public int getKind() {
				return kind;
			}
			@Override
			public char[] getName() {
				return binding.getName().toCharArray();
			}
			@Override
			public char[] getCompletion() {
				return binding.getName().toCharArray();
			}
			@Override
			public char[] getSignature() {
				if (binding instanceof IMethodBinding methodBinding) {
					return Signature.createMethodSignature(
							Arrays.stream(methodBinding.getParameterTypes())
								.map(ITypeBinding::getName)
								.map(String::toCharArray)
								.map(type -> Signature.createTypeSignature(type, true).toCharArray())
								.toArray(char[][]::new),
							Signature.createTypeSignature(methodBinding.getReturnType().getQualifiedName().toCharArray(), true).toCharArray());
				}
				if (binding instanceof IVariableBinding variableBinding) {
					return Signature.createTypeSignature(variableBinding.getType().getQualifiedName().toCharArray(), true).toCharArray();
				}
				if (binding instanceof ITypeBinding typeBinding) {
					return Signature.createTypeSignature(typeBinding.getQualifiedName().toCharArray(), true).toCharArray();
				}
				return new char[] {};
			}
			@Override
			public int getReplaceStart() {
				return DOMCompletionEngine.this.offset;
			}
			@Override
			public int getReplaceEnd() {
				return getReplaceStart();
			}
			@Override
			public int getFlags() {
				return 0; //TODO
			}
			@Override
			public char[] getReceiverSignature() {
				if (binding instanceof IMethodBinding method) {
					return Signature.createTypeSignature(method.getDeclaredReceiverType().getQualifiedName().toCharArray(), true).toCharArray();
				}
				if (binding instanceof IVariableBinding variable && variable.isField()) {
					return Signature.createTypeSignature(variable.getDeclaringClass().getQualifiedName().toCharArray(), true).toCharArray();
				}
				return new char[]{};
			}
			@Override
			public char[] getDeclarationSignature() {
				if (binding instanceof IMethodBinding method) {
					return Signature.createTypeSignature(method.getDeclaringClass().getQualifiedName().toCharArray(), true).toCharArray();
				}
				if (binding instanceof IVariableBinding variable && variable.isField()) {
					return Signature.createTypeSignature(variable.getDeclaringClass().getQualifiedName().toCharArray(), true).toCharArray();
				}
				return new char[]{};
			}
		};
		return res;
	}

}
