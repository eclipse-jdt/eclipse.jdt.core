package org.eclipse.jdt.internal.compiler.ast;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
import org.eclipse.jdt.internal.compiler.IAbstractSyntaxTreeVisitor;
import org.eclipse.jdt.internal.compiler.impl.*;
import org.eclipse.jdt.internal.compiler.codegen.*;
import org.eclipse.jdt.internal.compiler.lookup.*;

public class TrueLiteral extends MagicLiteral {
	static final char[] source = {'t' , 'r' , 'u' , 'e'};
public TrueLiteral(int s , int e) {
	super(s,e);
}
public void computeConstant() {

	constant = Constant.fromValue(true);}
/**
 * Code generation for the true literal
 *
 * @param currentScope org.eclipse.jdt.internal.compiler.lookup.BlockScope
 * @param codeStream org.eclipse.jdt.internal.compiler.codegen.CodeStream
 * @param valueRequired boolean
 */ 
public void generateCode(BlockScope currentScope, CodeStream codeStream, boolean valueRequired) {
	int pc = codeStream.position;
	if (valueRequired)
		codeStream.iconst_1();
	codeStream.recordPositionsFrom(pc, this);
}
public void generateOptimizedBoolean(BlockScope currentScope, CodeStream codeStream, Label trueLabel, Label falseLabel, boolean valueRequired) {

	// trueLabel being not nil means that we will not fall through into the TRUE case

	int pc = codeStream.position;
	// constant == true
	if (valueRequired) {
		if (falseLabel == null) {
			// implicit falling through the FALSE case
			if (trueLabel != null) {
				codeStream.goto_(trueLabel);
			}
		}
	}
	codeStream.recordPositionsFrom(pc, this);
}
public TypeBinding literalType(BlockScope scope) {
	return BooleanBinding;
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
