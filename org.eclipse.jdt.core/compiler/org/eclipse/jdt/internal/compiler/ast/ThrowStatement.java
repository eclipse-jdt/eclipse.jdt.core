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

public class ThrowStatement extends Statement {
	public Expression exception;
	public TypeBinding exceptionType;
public ThrowStatement(Expression exception,int startPosition) {
	this.exception = exception;
	this.sourceStart = startPosition ;
	this.sourceEnd = exception.sourceEnd ;
}
public FlowInfo analyseCode(BlockScope currentScope, FlowContext flowContext, FlowInfo flowInfo) {

	// need to check that exception thrown is actually caught somewhere

	exception.analyseCode(currentScope, flowContext, flowInfo);
	flowContext.checkExceptionHandlers(exceptionType, this, flowInfo, currentScope);
	return FlowInfo.DeadEnd;
}
/**
 * Throw code generation
 *
 * @param currentScope org.eclipse.jdt.internal.compiler.lookup.BlockScope
 * @param codeStream org.eclipse.jdt.internal.compiler.codegen.CodeStream
 */ 
public void generateCode(BlockScope currentScope, CodeStream codeStream) {

	if ((bits & IsReachableMASK) == 0) {
		return;
	}
	int pc = codeStream.position;
	exception.generateCode(currentScope, codeStream, true);
	codeStream.athrow();
	codeStream.recordPositionsFrom(pc, this);
	
}
public void resolve(BlockScope scope) {
	exceptionType = exception.resolveTypeExpecting(scope, scope.getJavaLangThrowable());
	if (exceptionType == NullBinding)
		scope.problemReporter().cannotThrowNull(this);
	exception.implicitWidening(exceptionType, exceptionType);
}
/* SHOULDN'T IT RATHER DO -
scope.checkThrowable(exceptionType = expression.resolveType(scope)); 
*/
public String toString(int tab){
	/* slow code */

	String s = tabString(tab) ;
	s = s + "throw ";
	s = s + exception.toStringExpression() ;
	return s;
}
public void traverse(IAbstractSyntaxTreeVisitor visitor, BlockScope blockScope) {
	if (visitor.visit(this, blockScope)) {
		exception.traverse(visitor, blockScope);
	}
	visitor.endVisit(this, blockScope);
}
}
