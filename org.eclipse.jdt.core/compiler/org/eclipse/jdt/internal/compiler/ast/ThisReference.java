package org.eclipse.jdt.internal.compiler.ast;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
import org.eclipse.jdt.internal.compiler.IAbstractSyntaxTreeVisitor;
import org.eclipse.jdt.internal.compiler.codegen.*;
import org.eclipse.jdt.internal.compiler.lookup.*;

public class ThisReference extends Reference {

	public static final ThisReference ThisImplicit = new ThisReference();

	/**
	 * ThisReference constructor comment.
	 */
	public ThisReference() {
		super();
	}

	public ThisReference(int s, int sourceEnd) {
		this();
		this.sourceStart = s;
		this.sourceEnd = sourceEnd;
	}

	protected boolean checkAccess(MethodScope methodScope) {
		// this/super cannot be used in constructor call
		if (methodScope.isConstructorCall) {
			methodScope.problemReporter().fieldsOrThisBeforeConstructorInvocation(this);
			return false;
		}

		// static may not refer to this/super
		if (methodScope.isStatic) {
			methodScope.problemReporter().errorThisSuperInStatic(this);
			return false;
		}
		return true;
	}

	public void generateCode(
		BlockScope currentScope,
		CodeStream codeStream,
		boolean valueRequired) {
		int pc = codeStream.position;
		if (valueRequired)
			codeStream.aload_0();
		codeStream.recordPositionsFrom(pc, this);
	}

	public boolean isThis() {

		return true;
	}

	public TypeBinding resolveType(BlockScope scope) {
		// implicit this
		constant = NotAConstant;
		if (this != ThisImplicit && !checkAccess(scope.methodScope()))
			return null;
		return scope.enclosingSourceType();
	}

	public String toStringExpression() {

		if (this == ThisImplicit)
			return "";
		return "this";
	}

	public void traverse(
		IAbstractSyntaxTreeVisitor visitor,
		BlockScope blockScope) {
		visitor.visit(this, blockScope);
		visitor.endVisit(this, blockScope);
	}

}
