/*******************************************************************************
 * Copyright (c) 2000, 2006 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jdt.internal.compiler.lookup;

import org.eclipse.jdt.core.compiler.CharOperation;
import org.eclipse.jdt.internal.compiler.ast.ASTNode;
import org.eclipse.jdt.internal.compiler.ast.TypeDeclaration;
import org.eclipse.jdt.internal.compiler.classfmt.ClassFileConstants;

public final class MemberTypeBinding extends NestedTypeBinding {
public MemberTypeBinding(char[][] compoundName, ClassScope scope, SourceTypeBinding enclosingType) {
	super(compoundName, scope, enclosingType);
	this.tagBits |= TagBits.MemberTypeMask;
}
void checkSyntheticArgsAndFields() {
	if (this.isStatic()) return;
	if (this.isInterface()) return;
	this.addSyntheticArgumentAndField(this.enclosingType);
}
/* Answer the receiver's constant pool name.
*
* NOTE: This method should only be used during/after code gen.
*/

public char[] constantPoolName() /* java/lang/Object */ {
	if (constantPoolName != null)
		return constantPoolName;

	return constantPoolName = CharOperation.concat(enclosingType().constantPoolName(), sourceName, '$');
}
public void initializeDeprecatedAnnotationTagBits() {
	if ((this.tagBits & (TagBits.AnnotationResolved|TagBits.AnnotationDeprecated)) == 0) {
		ReferenceBinding enclosing = this.enclosingType();
		enclosing.initializeDeprecatedAnnotationTagBits();
		TypeDeclaration typeDecl = this.scope.referenceContext;
		boolean old = typeDecl.staticInitializerScope.insideTypeAnnotation;
		try {
			typeDecl.staticInitializerScope.insideTypeAnnotation = true;
			ASTNode.resolveDeprecatedAnnotations(typeDecl.staticInitializerScope, typeDecl.annotations, this);
		} finally {
			typeDecl.staticInitializerScope.insideTypeAnnotation = old;
		}
		if ((this.tagBits & TagBits.AnnotationDeprecated) != 0) {
			this.modifiers |= ClassFileConstants.AccDeprecated;
		} else if ((enclosing.modifiers & (ClassFileConstants.AccDeprecated |
						ExtraCompilerModifiers.AccDeprecatedImplicitly)) != 0) {
			this.modifiers |= ExtraCompilerModifiers.AccDeprecatedImplicitly;
		}
	}
}
public String toString() {
	return "Member type : " + new String(sourceName()) + " " + super.toString(); //$NON-NLS-2$ //$NON-NLS-1$
}
}
