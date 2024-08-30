/*******************************************************************************
 * Copyright (c) 2024, Red Hat, Inc. and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *******************************************************************************/
package org.eclipse.jdt.internal.javac.dom;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;

import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.Signature;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.FieldDeclaration;
import org.eclipse.jdt.core.dom.IBinding;
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.core.dom.LambdaExpression;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.Modifier;
import org.eclipse.jdt.core.dom.VariableDeclarationFragment;
import org.eclipse.jdt.internal.core.JavaElement;
import org.eclipse.jdt.internal.core.LambdaFactory;

public class JavacLambdaBinding extends JavacMethodBinding {

	private LambdaExpression declaration;

	public JavacLambdaBinding(JavacMethodBinding methodBinding, LambdaExpression declaration) {
		super(methodBinding.methodType, methodBinding.methodSymbol, methodBinding.parentType, methodBinding.resolver);
		this.declaration = declaration;
	}

	@Override
	public boolean equals(Object obj) {
		return super.equals(obj) && obj instanceof JavacLambdaBinding other && Objects.equals(other.declaration, this.declaration);
	}

	@Override
	public int hashCode() {
		return super.hashCode() ^ declaration.hashCode();
	}

	@Override
	public int getModifiers() {
		return super.getModifiers() & ~Modifier.ABSTRACT;
	}

	@Override
	public IBinding getDeclaringMember() {
		if (this.declaration.getParent() instanceof VariableDeclarationFragment fragment &&
				fragment.getParent() instanceof FieldDeclaration) {
			return fragment.resolveBinding();
		}
		ASTNode parent = this.declaration.getParent();
		while (parent != null) {
			if (parent instanceof MethodDeclaration method) {
				return method.resolveBinding();
			}
			parent = parent.getParent();
		};
		return null;
	}

	@Override
	public IJavaElement getJavaElement() {
		var member = getDeclaringMember();
		if (member != null && member.getJavaElement() instanceof JavaElement parent) {
			int arrowIndex = ((List<ASTNode>)this.declaration.parameters()).stream().mapToInt(param -> param.getStartPosition() + param.getLength()).max().orElse(this.declaration.getStartPosition());
			org.eclipse.jdt.internal.core.LambdaExpression expr = LambdaFactory.createLambdaExpression(parent, Signature.createTypeSignature(getMethodDeclaration().getDeclaringClass().getQualifiedName(), true), this.declaration.getStartPosition(), this.declaration.getStartPosition() + this.declaration.getLength() - 1, arrowIndex);
			return LambdaFactory.createLambdaMethod(expr, this.methodSymbol.name.toString(), getKey(), this.declaration.getStartPosition(), this.declaration.getStartPosition() + this.declaration.getLength() - 1, arrowIndex, Arrays.stream(getParameterTypes()).map(ITypeBinding::getName).toArray(String[]::new), getParameterNames(), Signature.createTypeSignature(getReturnType().getName(), true));
		}
		return super.getJavaElement();
	}

}
