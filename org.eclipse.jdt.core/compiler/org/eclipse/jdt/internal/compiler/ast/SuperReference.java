package org.eclipse.jdt.internal.compiler.ast;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
import org.eclipse.jdt.internal.compiler.IAbstractSyntaxTreeVisitor;
import org.eclipse.jdt.internal.compiler.codegen.*;
import org.eclipse.jdt.internal.compiler.lookup.*;

public class SuperReference extends ThisReference {
	public static final SuperReference Super = new SuperReference();

	/**
	 * SuperReference constructor comment.
	 */
	public SuperReference() {
		super();
	}

	public SuperReference(int pos, int sourceEnd) {
		super();
		sourceStart = pos;
		this.sourceEnd = sourceEnd;
	}

	public static ExplicitConstructorCall implicitSuperConstructorCall() {
		return new ExplicitConstructorCall(ExplicitConstructorCall.ImplicitSuper);
	}

	public boolean isSuper() {

		return true;
	}

	public boolean isThis() {

		return false;
	}

	public TypeBinding resolveType(BlockScope scope) {
		constant = NotAConstant;
		if (!checkAccess(scope.methodScope()))
			return null;
		SourceTypeBinding enclosingTb = scope.enclosingSourceType();
		if (scope.isJavaLangObject(enclosingTb)) {
			scope.problemReporter().cannotUseSuperInJavaLangObject(this);
			return null;
		}
		return enclosingTb.superclass;
	}

	public String toStringExpression() {

		return "super";

	}

	public void traverse(
		IAbstractSyntaxTreeVisitor visitor,
		BlockScope blockScope) {
		visitor.visit(this, blockScope);
		visitor.endVisit(this, blockScope);
	}

}
