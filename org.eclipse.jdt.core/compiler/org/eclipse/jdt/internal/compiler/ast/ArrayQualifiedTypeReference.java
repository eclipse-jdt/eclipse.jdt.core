package org.eclipse.jdt.internal.compiler.ast;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
import org.eclipse.jdt.internal.compiler.IAbstractSyntaxTreeVisitor;
import org.eclipse.jdt.internal.compiler.impl.*;
import org.eclipse.jdt.internal.compiler.lookup.*;

public class ArrayQualifiedTypeReference extends QualifiedTypeReference {
	int dimensions;
public ArrayQualifiedTypeReference(char[][] sources , int dim, long[] poss) {
	super( sources , poss);
	dimensions = dim ;
}
public ArrayQualifiedTypeReference(char[][] sources , TypeBinding tb, int dim, long[] poss) {
	super( sources , tb, poss);
	dimensions = dim ;
}
public int dimensions() {
	return dimensions;
}
public TypeBinding getTypeBinding(Scope scope) {
	if (binding != null)
		return binding;
	return scope.createArray(scope.getType(tokens), dimensions);
}
public String toStringExpression(int tab){
	/* slow speed */

	String s = super.toStringExpression(tab)  ;
	if (dimensions == 1 ) return s + "[]" ;
	for (int i=1 ; i <= dimensions ; i++)
		s = s + "[]" ;
	return s ;
}
public void traverse(IAbstractSyntaxTreeVisitor visitor, BlockScope scope) {
	visitor.visit(this, scope);
	visitor.endVisit(this, scope);
}
public void traverse(IAbstractSyntaxTreeVisitor visitor, ClassScope scope) {
	visitor.visit(this, scope);
	visitor.endVisit(this, scope);
}
}
