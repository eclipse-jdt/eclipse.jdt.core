package org.eclipse.jdt.internal.compiler.ast;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
import org.eclipse.jdt.internal.compiler.IAbstractSyntaxTreeVisitor;
import org.eclipse.jdt.internal.compiler.codegen.*;
import org.eclipse.jdt.internal.compiler.flow.*;
import org.eclipse.jdt.internal.compiler.lookup.*;
import org.eclipse.jdt.internal.compiler.problem.*;
import org.eclipse.jdt.internal.compiler.util.Util;

public abstract class Reference extends Expression  {
/**
 * BaseLevelReference constructor comment.
 */
public Reference() {
	super();
}
public FlowInfo analyseAssignment(BlockScope currentScope, FlowContext flowContext, FlowInfo flowInfo, Assignment assignment, boolean isCompound) {
	throw new ShouldNotImplement(Util.bind("ast.variableShouldProvide"/*nonNLS*/));
}
public FlowInfo analyseCode(BlockScope currentScope, FlowContext flowContext, FlowInfo flowInfo) {
	return flowInfo;
}
public FieldBinding fieldBinding() {
	//this method should be sent ONLY after a check against isFieldReference()
	//check its use doing senders.........

	return null ;
}
public void fieldStore(CodeStream codeStream, FieldBinding fieldBinding, MethodBinding syntheticWriteAccessor, boolean valueRequired) {

	if (fieldBinding.isStatic()) {
		if (valueRequired) {
			if ((fieldBinding.type == LongBinding) || (fieldBinding.type == DoubleBinding)) {
				codeStream.dup2();
			} else {
				codeStream.dup();
			}
		}
		if (syntheticWriteAccessor == null) {
			codeStream.putstatic(fieldBinding);
		} else {
			codeStream.invokestatic(syntheticWriteAccessor);
		}
	} else { // Stack:  [owner][new field value]  ---> [new field value][owner][new field value]
		if (valueRequired) {
			if ((fieldBinding.type == LongBinding) || (fieldBinding.type == DoubleBinding)) {
				codeStream.dup2_x1();
			} else {
				codeStream.dup_x1();
			}
		}
		if (syntheticWriteAccessor == null) {
			codeStream.putfield(fieldBinding);
		} else {
			codeStream.invokestatic(syntheticWriteAccessor);
		}
	}
}
public void generateAssignment(BlockScope currentScope, CodeStream codeStream, Assignment assignment, boolean valueRequired) {
	throw new ShouldNotImplement(Util.bind("ast.compoundPreShouldProvide"/*nonNLS*/));
}
public void generateCompoundAssignment(BlockScope currentScope, CodeStream codeStream, Expression expression, int operator, int assignmentImplicitConversion, boolean valueRequired) {
	throw new ShouldNotImplement(Util.bind("ast.compoundVariableShouldProvide"/*nonNLS*/));
}
public void generatePostIncrement(BlockScope currentScope, CodeStream codeStream, CompoundAssignment postIncrement, boolean valueRequired) {
	throw new ShouldNotImplement(Util.bind("ast.postIncrShouldProvide"/*nonNLS*/));
}
public boolean isFieldReference() {

	return false ;
}
}
