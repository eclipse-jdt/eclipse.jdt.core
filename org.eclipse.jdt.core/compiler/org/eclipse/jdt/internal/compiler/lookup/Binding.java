/*******************************************************************************
 * Copyright (c) 2000, 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jdt.internal.compiler.lookup;

import org.eclipse.jdt.internal.compiler.ast.ASTNode;

public abstract class Binding implements CompilerModifiers, ProblemReasons {

	// binding kinds
	public static final int FIELD = ASTNode.Bit1;
	public static final int LOCAL = ASTNode.Bit2;
	public static final int VARIABLE = FIELD | LOCAL;
	public static final int TYPE = ASTNode.Bit3;
	public static final int METHOD = ASTNode.Bit4;
	public static final int PACKAGE = ASTNode.Bit5;
	public static final int IMPORT = ASTNode.Bit6;
	public static final int ARRAY_TYPE = TYPE | ASTNode.Bit7;
	public static final int PARAMETERIZED_TYPE = TYPE | ASTNode.Bit8;
	public static final int WILDCARD_TYPE = TYPE | ASTNode.Bit9;
	public static final int RAW_TYPE = TYPE | ASTNode.Bit10;
	public static final int GENERIC_TYPE = TYPE | ASTNode.Bit11;
	public static final int TYPE_PARAMETER = TYPE | ASTNode.Bit12;
	public static final int ANNOTATION_BINDING = TYPE | ASTNode.Bit13; // for annotation refs
	
	// TODO (jerome) change to true to fix https://bugs.eclipse.org/bugs/show_bug.cgi?id=90392
	public static boolean USE_ACCESS_FLAGS_IN_BINDING_KEY = false;

	/* API
	* Answer the receiver's binding type from Binding.BindingID.
	*
	* Note: Do NOT expect this to be used very often... only in switch statements with
	* more than 2 possible choices.
	*/
	public abstract int kind();
	/*
	 * Computes a key that uniquely identifies this binding.
	 * Returns null if binding is not a TypeBinding, a MethodBinding, a FieldBinding or a PackageBinding.
	 */
	public char[] computeUniqueKey() {
		return computeUniqueKey(USE_ACCESS_FLAGS_IN_BINDING_KEY/*without access flags*/);
	}
	/*
	 * Computes a key that uniquely identifies this binding. Optinaly include access flags.
	 * Returns null if binding is not a TypeBinding, a MethodBinding, a FieldBinding or a PackageBinding.
	 */
	public char[] computeUniqueKey(boolean withAccessFlags) {
		return null;
	}
	/**
	 * Compute the tagbits for standard annotations. For source types, these could require
	 * lazily resolving corresponding annotation nodes, in case of forward references.
	 * @see org.eclipse.jdt.internal.compiler.lookup.SourceTypeBinding#getAnnotationTagBits()
	 */
	public long getAnnotationTagBits() {
		return 0;
	}

	/* API
	* Answer true if the receiver is not a problem binding
	*/
	public final boolean isValidBinding() {
		return problemId() == NoError;
	}
	/* API
	* Answer the problem id associated with the receiver.
	* NoError if the receiver is a valid binding.
	*/
	// TODO (philippe) should rename into problemReason()
	public int problemId() {
		return NoError;
	}
	/* Answer a printable representation of the receiver.
	*/
	public abstract char[] readableName();
	/* Shorter printable representation of the receiver (no qualified type)
	 */	
	public char[] shortReadableName(){
		return readableName();
	}
}
