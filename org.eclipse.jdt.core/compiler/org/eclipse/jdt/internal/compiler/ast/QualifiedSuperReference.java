package org.eclipse.jdt.internal.compiler.ast;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
import org.eclipse.jdt.internal.compiler.IAbstractSyntaxTreeVisitor;
import org.eclipse.jdt.internal.compiler.impl.*;
import org.eclipse.jdt.internal.compiler.codegen.*;
import org.eclipse.jdt.internal.compiler.flow.*;
import org.eclipse.jdt.internal.compiler.lookup.*;

public class QualifiedSuperReference extends QualifiedThisReference {
public QualifiedSuperReference(TypeReference name, int pos, int sourceEnd) {
	super(name, pos, sourceEnd);
}
public boolean isSuper() {
	
	return true;
}
public boolean isThis() {
	
	return false ;
}
public TypeBinding resolveType(BlockScope scope) {

	super.resolveType(scope);
	if (currentCompatibleType == null) return null; // error case
	
	if (scope.isJavaLangObject(currentCompatibleType)) {
		scope.problemReporter().cannotUseSuperInJavaLangObject(this);
		return null;
	}
	return currentCompatibleType.superclass();	
}
public String toStringExpression(){
	/* slow code */
	
	return qualification.toString(0)+".super" ;
}
public void traverse(IAbstractSyntaxTreeVisitor visitor, BlockScope blockScope) {
	if (visitor.visit(this, blockScope)) {
		qualification.traverse(visitor, blockScope);
	}
	visitor.endVisit(this, blockScope);
}
}
