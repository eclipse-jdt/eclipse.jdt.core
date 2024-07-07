/*******************************************************************************
 * Copyright (c) 2020, 2024 IBM Corporation and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * This is an implementation of an early-draft specification developed under the Java
 * Community Process (JCP) and is made available for testing and evaluation purposes
 * only. The code is not compatible with any specification of the JCP.
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jdt.internal.compiler.lookup;

import org.eclipse.jdt.internal.compiler.ast.ASTNode;

public interface ExtendedTagBits {

	int AreRecordComponentsComplete = ASTNode.Bit1; // type
	int HasUnresolvedPermittedSubtypes = ASTNode.Bit2;

	/** From Java 16
	 *  Flag used to identify the annotation jdk.internal.ValueBased
	 */
	int AnnotationValueBased = ASTNode.Bit3;

	// Java 16 Records
	int IsCanonicalConstructor = ASTNode.Bit4; // constructor
	int isImplicit  = ASTNode.Bit5; // constructor and method

	// @Owning / closing
	int IsClosingMethod = ASTNode.Bit1; // method

	// early construction context - local type (named or anonymous)
	int IsInEarlyConstructionContext = ASTNode.Bit6;

}
