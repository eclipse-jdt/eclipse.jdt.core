/*******************************************************************************
 * Copyright (c) 2002 IBM Corp. and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0 
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 ******************************************************************************/
package org.eclipse.jdt.core.tests.model;

import java.text.Collator;
import java.util.Comparator;
import java.util.List;

import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.BodyDeclaration;
import org.eclipse.jdt.core.dom.FieldDeclaration;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.SingleVariableDeclaration;
import org.eclipse.jdt.core.dom.TypeDeclaration;
import org.eclipse.jdt.core.dom.VariableDeclarationFragment;
import org.eclipse.jdt.core.util.JavaCompilationUnitSorter;

/**
 * 
 * @since 2.1
 */
public class DefaultJavaElementComparator implements Comparator {

	Collator collator;
	
	public DefaultJavaElementComparator() {
		this.collator = Collator.getInstance();
	}

	/**
	 * @see java.util.Comparator#compare(java.lang.Object, java.lang.Object)
	 */
	public int compare(Object o1, Object o2) {
		if (!(o1 instanceof BodyDeclaration) && !(o2 instanceof BodyDeclaration)) {
			throw new ClassCastException();
		}
		BodyDeclaration node1 = (BodyDeclaration) o1;
		BodyDeclaration node2 = (BodyDeclaration) o2;
		int category1 = JavaCompilationUnitSorter.getCategory(node1, JavaCore.getOptions());
		int category2 = JavaCompilationUnitSorter.getCategory(node2, JavaCore.getOptions());
		
		if (category1 != category2) {
			return category1 - category2;
		}
		if (o1 == o2) {
			return 0;
		}
		switch(node1.getNodeType()) {
			case ASTNode.METHOD_DECLARATION :
				MethodDeclaration method1 = (MethodDeclaration) node1;
				MethodDeclaration method2 = (MethodDeclaration) node2;
				
				if (method1.isConstructor()) {
					return compareParams(method1, method2);
				}
				int compare = this.collator.compare(method1.getName().getIdentifier(), method2.getName().getIdentifier());
				if (compare != 0) {
					return compare;
				}
				return compareParams(method1, method2);
			case ASTNode.FIELD_DECLARATION :
				FieldDeclaration fieldDeclaration1 = (FieldDeclaration) node1;
				FieldDeclaration fieldDeclaration2 = (FieldDeclaration) node2;
				VariableDeclarationFragment fragment1 = (VariableDeclarationFragment) fieldDeclaration1.fragments().get(0);
				VariableDeclarationFragment fragment2 = (VariableDeclarationFragment) fieldDeclaration2.fragments().get(0);
				return this.collator.compare(fragment1.getName().getIdentifier(), fragment2.getName().getIdentifier());
			case ASTNode.INITIALIZER :
				return ((Integer) node1.getProperty(JavaCompilationUnitSorter.SOURCE_START)).intValue() - ((Integer) node2.getProperty(JavaCompilationUnitSorter.SOURCE_START)).intValue();
			case ASTNode.TYPE_DECLARATION :
				TypeDeclaration typeDeclaration1 = (TypeDeclaration) node1;
				TypeDeclaration typeDeclaration2 = (TypeDeclaration) node2;
				return this.collator.compare(typeDeclaration1.getName().getIdentifier(), typeDeclaration2.getName().getIdentifier());
		}
		return 0;
	}

	int compareParams(
		MethodDeclaration method1,
		MethodDeclaration method2) {
		int compare;
		final List parameters1 = method1.parameters();
		final List parameters2 = method2.parameters();
		int length1 = parameters1.size();
		int length2 = parameters2.size();
		int len= Math.min(length1, length2);
		for (int i = 0; i < len; i++) {
			compare = this.collator.compare(((SingleVariableDeclaration) parameters1.get(i)).getName().getIdentifier(), ((SingleVariableDeclaration) parameters2.get(i)).getName().getIdentifier());
			if (compare != 0) {
				return compare;
			}
		}
		return length1 - length2;
	}
}
