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
package org.eclipse.jdt.internal.compiler.ast;

import org.eclipse.jdt.internal.compiler.IAbstractSyntaxTreeVisitor;
import org.eclipse.jdt.internal.compiler.lookup.BlockScope;
import org.eclipse.jdt.internal.compiler.lookup.SourceTypeBinding;
import org.eclipse.jdt.internal.compiler.lookup.TypeBinding;

public class SuperReference extends ThisReference {
	
public SuperReference(int sourceStart, int sourceEnd) {
	super(sourceStart, sourceEnd);
}
public static ExplicitConstructorCall implicitSuperConstructorCall() {
	return new ExplicitConstructorCall(ExplicitConstructorCall.ImplicitSuper);
}
public boolean isImplicitThis() {
	
	return false;
}
public boolean isSuper() {
	
	return true;
}
public boolean isThis() {
	
	return false ;
}
public TypeBinding resolveType(BlockScope scope) {
	constant = NotAConstant;
	if (!checkAccess(scope.methodScope()))
		return null;
	SourceTypeBinding enclosingTb = scope.enclosingSourceType();
	if (scope.isJavaLangObject(enclosingTb)) {
		scope.problemReporter().cannotUseSuperInJavaLangObject(this);
		return null;
	}
	return this.resolvedType = enclosingTb.superclass;
}
public String toStringExpression(){

	return "super"; //$NON-NLS-1$
	
}
public void traverse(IAbstractSyntaxTreeVisitor visitor, BlockScope blockScope) {
	visitor.visit(this, blockScope);
	visitor.endVisit(this, blockScope);
}
}
