package org.eclipse.jdt.internal.compiler.ast;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
import org.eclipse.jdt.internal.compiler.IAbstractSyntaxTreeVisitor;
import org.eclipse.jdt.internal.compiler.impl.*;
import org.eclipse.jdt.internal.compiler.codegen.*;
import org.eclipse.jdt.internal.compiler.lookup.*;

public class NullLiteral extends MagicLiteral {
	static final char[] source = {'n' , 'u' , 'l' , 'l'};
public NullLiteral(int s , int e) {
	super(s,e);
}
public void computeConstant() {

	constant = Constant.fromValue(null);}
/**
 * Code generation for the null literal
 *
 * @param currentScope org.eclipse.jdt.internal.compiler.lookup.BlockScope
 * @param codeStream org.eclipse.jdt.internal.compiler.codegen.CodeStream
 * @param valueRequired boolean
 */ 
public void generateCode(BlockScope currentScope, CodeStream codeStream, boolean valueRequired) {
	int pc = codeStream.position;
	if (valueRequired)
		codeStream.aconst_null();
	codeStream.recordPositionsFrom(pc, this);
}
public TypeBinding literalType(BlockScope scope) {
	return NullBinding;
}
/**
 * 
 */
public char[] source() {
	return source;
}
public void traverse(IAbstractSyntaxTreeVisitor visitor, BlockScope scope) {
	visitor.visit(this, scope);
	visitor.endVisit(this, scope);
}
}
