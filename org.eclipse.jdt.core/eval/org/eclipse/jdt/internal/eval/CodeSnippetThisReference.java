package org.eclipse.jdt.internal.eval;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
import org.eclipse.jdt.internal.compiler.ast.*;
import org.eclipse.jdt.internal.compiler.codegen.*;
import org.eclipse.jdt.internal.compiler.lookup.*;

/**
 * A this reference inside a code snippet denotes a remote
 * receiver object (i.e. the one of the context in the stack
 * frame)
 */
public class CodeSnippetThisReference extends ThisReference implements EvaluationConstants, InvocationSite {
	EvaluationContext evaluationContext;
	FieldBinding delegateThis;
	boolean isImplicit;
/**
 * CodeSnippetThisReference constructor comment.
 * @param s int
 * @param sourceEnd int
 */
public CodeSnippetThisReference(int s, int sourceEnd, EvaluationContext evaluationContext, boolean isImplicit) {
	super(s, sourceEnd);
	this.evaluationContext = evaluationContext;
	this.isImplicit = isImplicit;
}
protected boolean checkAccess(MethodScope methodScope) {
	// this/super cannot be used in constructor call
	if (evaluationContext.isConstructorCall) {
		methodScope.problemReporter().fieldsOrThisBeforeConstructorInvocation(this);
		return false;
	}

	// static may not refer to this/super
	if (this.evaluationContext.declaringTypeName == null || evaluationContext.isStatic) {
		methodScope.problemReporter().errorThisSuperInStatic(this);
		return false;
	}
	return true;
}
public void generateCode(BlockScope currentScope, CodeStream codeStream, boolean valueRequired) {
	int pc = codeStream.position;
	if (valueRequired) {
		codeStream.aload_0();
		codeStream.getfield(delegateThis);
	}
	codeStream.recordPositionsFrom(pc, this);
}
public boolean isSuperAccess(){
	return false;
}
public boolean isTypeAccess(){
	return false;
}
public TypeBinding resolveType(BlockScope scope) {

	// implicit this
	constant = NotAConstant;
	TypeBinding snippetType = null;
	if (this.isImplicit || checkAccess(scope.methodScope())){
		snippetType = scope.enclosingSourceType();
	}
	if (snippetType == null) return null;
	
	delegateThis = scope.getField(snippetType, DELEGATE_THIS, this);
	if (delegateThis == null) return null; // internal error, field should have been found
	return delegateThis.type;
}
public void setDepth(int depth){
}
public void setFieldIndex(int index){
}
public String toStringExpression(){
	char[] declaringType = this.evaluationContext.declaringTypeName;
	return "("+ (declaringType == null ? "<NO DECLARING TYPE>" : new String(declaringType)) + ")this";
}
}
