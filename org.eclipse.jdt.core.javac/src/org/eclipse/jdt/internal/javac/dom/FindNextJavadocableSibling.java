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
package org.eclipse.jdt.internal.javac.dom;

import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.AbstractTypeDeclaration;
import org.eclipse.jdt.core.dom.FieldDeclaration;
import org.eclipse.jdt.core.dom.MethodDeclaration;

public class FindNextJavadocableSibling extends ASTVisitor {
	public ASTNode nextNode = null;
	private int javadocOffsetEnd;
	public FindNextJavadocableSibling(int javadocOffsetEnd) {
		this.javadocOffsetEnd = javadocOffsetEnd;
	}
	@Override
	public void preVisit(ASTNode node) {
		if (node.getStartPosition() > this.javadocOffsetEnd &&
			isJavadocAble(node) &&
			(this.nextNode == null || this.nextNode.getStartPosition() > node.getStartPosition())) {
				this.nextNode = node;
			}
	}

	private static boolean isJavadocAble(ASTNode node) {
		return node instanceof AbstractTypeDeclaration ||
			node instanceof FieldDeclaration ||
			node instanceof MethodDeclaration;
	}
}
