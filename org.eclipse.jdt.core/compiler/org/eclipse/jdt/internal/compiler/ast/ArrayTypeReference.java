package org.eclipse.jdt.internal.compiler.ast;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
import org.eclipse.jdt.internal.compiler.IAbstractSyntaxTreeVisitor;
import org.eclipse.jdt.internal.compiler.impl.*;
import org.eclipse.jdt.internal.compiler.lookup.*;

public class ArrayTypeReference extends SingleTypeReference {
	public int dimensions;
/**
 * ArrayTypeReference constructor comment.
 * @param source char[]
 * @param dim int
 * @param pos int
 */
public ArrayTypeReference(char[] source, int dim, long pos) {
	super(source, pos);
	dimensions = dim ;
}
public ArrayTypeReference(char[] source, TypeBinding tb, int dim, long pos) {
	super(source, tb, pos);
	dimensions = dim ;}
public int dimensions() {
	return dimensions;
}
public TypeBinding getTypeBinding(Scope scope) {
	if (binding != null)
		return binding;
	return scope.createArray(scope.getType(token), dimensions);
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
}
