package org.eclipse.jdt.internal.compiler.ast;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
import org.eclipse.jdt.internal.compiler.IAbstractSyntaxTreeVisitor;
import org.eclipse.jdt.internal.compiler.impl.*;
import org.eclipse.jdt.internal.compiler.lookup.*;

public class QualifiedTypeReference extends TypeReference {
	public char[][] tokens;
	public long[] sourcePositions;
public QualifiedTypeReference(char[][] sources , long[] poss) {
	tokens = sources ;
	sourcePositions = poss ;
	sourceStart = (int) (sourcePositions[0]>>>32) ;
	sourceEnd = (int)(sourcePositions[sourcePositions.length-1] & 0x00000000FFFFFFFFL ) ;
}
public QualifiedTypeReference(char[][] sources , TypeBinding tb , long[] poss) {
	this(sources,poss);
	binding = tb;
}
public TypeReference copyDims(int dim){
	//return a type reference copy of me with some dimensions
	//warning : the new type ref has a null binding
	
	return new ArrayQualifiedTypeReference(tokens,null,dim,sourcePositions) ;
}
public TypeBinding getTypeBinding(Scope scope) {
	if (binding != null)
		return binding;
	return scope.getType(tokens);
}
public char[][] getTypeName(){

	return tokens;
}
public String toStringExpression(int tab) {
	StringBuffer buffer = new StringBuffer();
	for (int i = 0; i < tokens.length; i++) {
		buffer.append(tokens[i]);
		if (i < (tokens.length - 1)) {
			buffer.append("."); //$NON-NLS-1$
		}
	}
	return buffer.toString();
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
