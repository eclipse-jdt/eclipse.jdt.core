/*******************************************************************************
 * Copyright (c) 2000, 2001, 2002 International Business Machines Corp. and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v0.5 
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v05.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 ******************************************************************************/
package org.eclipse.jdt.internal.compiler.ast;

import org.eclipse.jdt.internal.compiler.IAbstractSyntaxTreeVisitor;
import org.eclipse.jdt.internal.compiler.codegen.*;
import org.eclipse.jdt.internal.compiler.lookup.*;

public class ThisReference extends Reference {
	
private final static int ImplicitThisStart = -1;
	
public static ThisReference implicitThis(){
	return new ThisReference(ImplicitThisStart, 0);
}
	
public ThisReference(int sourceStart, int sourceEnd) {
	this.sourceStart = sourceStart;
	this.sourceEnd = sourceEnd;
}
protected boolean checkAccess(MethodScope methodScope) {
	// this/super cannot be used in constructor call
	if (methodScope.isConstructorCall) {
		methodScope.problemReporter().fieldsOrThisBeforeConstructorInvocation(this);
		return false;
	}

	// static may not refer to this/super
	if (methodScope.isStatic) {
		methodScope.problemReporter().errorThisSuperInStatic(this);
		return false;
	}
	return true;
}
public void generateCode(BlockScope currentScope, CodeStream codeStream, boolean valueRequired) {
	int pc = codeStream.position;
	if (valueRequired)
		codeStream.aload_0();
	codeStream.recordPositionsFrom(pc, this.sourceStart);
}
public boolean isImplicitThis() {
	
	return this.sourceStart == ImplicitThisStart;
}
public boolean isThis() {
	
	return true ;
}
public TypeBinding resolveType(BlockScope scope) {

	constant = NotAConstant;
	if (!this.isImplicitThis() && !checkAccess(scope.methodScope()))
		return null;
	return this.resolvedType = scope.enclosingSourceType();
}
public String toStringExpression(){

	if (this.isImplicitThis()) return "" ; //$NON-NLS-1$
	return "this"; //$NON-NLS-1$
}
public void traverse(IAbstractSyntaxTreeVisitor visitor, BlockScope blockScope) {
	visitor.visit(this, blockScope);
	visitor.endVisit(this, blockScope);
}
}
