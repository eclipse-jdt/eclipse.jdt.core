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

public class DefaultCase extends Statement {

	public CaseLabel targetLabel;
/**
 * DefautCase constructor comment.
 */
public DefaultCase(int startPosition) {
	this.sourceStart = startPosition ;
	this.sourceEnd = startPosition+6; //default.length-1  (good approximation)
}
public FlowInfo analyseCode(BlockScope currentScope, FlowContext flowContext, FlowInfo flowInfo) {
	return flowInfo;
}
/**
 * Default case code generation
 *
 * @param currentScope org.eclipse.jdt.internal.compiler.lookup.BlockScope
 * @param codeStream org.eclipse.jdt.internal.compiler.codegen.CodeStream
 */
public void generateCode(BlockScope currentScope, CodeStream codeStream) {

	if ((bits & IsReachableMASK) == 0) {
		return;
	}
	int pc = codeStream.position;
	targetLabel.place();
	codeStream.recordPositionsFrom(pc, this);
	
}
public Constant resolveCase(BlockScope scope, TypeBinding testType, SwitchStatement switchStatement) {
	// remember the default case into the associated switch statement
	if (switchStatement.defaultCase != null)
		scope.problemReporter().duplicateDefaultCase(this);

	// on error the last default will be the selected one .... (why not) ....	
	switchStatement.defaultCase = this;
	resolve(scope);
	return null;
}
public String toString(int tab){
	/* slow code */

	String s = tabString(tab);
	s = s + "default : " ; //$NON-NLS-1$
	return s;}
public void traverse(IAbstractSyntaxTreeVisitor visitor, BlockScope blockScope) {
	visitor.visit(this, blockScope);
	visitor.endVisit(this, blockScope);
}
}
