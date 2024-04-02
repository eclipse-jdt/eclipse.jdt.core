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
import org.eclipse.jdt.core.dom.PackageDeclaration;

public class FindNextJavadocableSibling extends ASTVisitor {
	public ASTNode nextNode = null;
	private int javadocStart;
	private int javadocLength;
	private boolean done = false;
	public FindNextJavadocableSibling(int javadocStart, int javadocLength) {
		this.javadocStart = javadocStart;
		this.javadocLength = javadocLength;
	}
	public boolean preVisit2(ASTNode node) {
		if( done ) 
			return false;
		
		preVisit(node);
		return true;
	}

	@Override
	public void preVisit(ASTNode node) {
		// If there's any overlap, abort. 
		//int nodeEnd = node.getStartPosition() + node.getLength();
		int jdocEnd = this.javadocStart + this.javadocLength;
		
		if( isJavadocAble(node)) {
			if( node.getStartPosition() == this.javadocStart ) {
				this.nextNode = node;
				done = true;
				return;
			}
			if (node.getStartPosition() > jdocEnd &&
				(this.nextNode == null || this.nextNode.getStartPosition() > node.getStartPosition())) {
					this.nextNode = node;
				}
		}
	}

	private static boolean isJavadocAble(ASTNode node) {
		return node instanceof PackageDeclaration || 
				node instanceof AbstractTypeDeclaration ||
			node instanceof FieldDeclaration ||
			node instanceof MethodDeclaration;
	}
}
