package org.eclipse.jdt.internal.compiler.ast;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
import org.eclipse.jdt.internal.compiler.IAbstractSyntaxTreeVisitor;
import org.eclipse.jdt.internal.compiler.impl.*;
import org.eclipse.jdt.internal.compiler.lookup.*;

public class PrefixExpression extends CompoundAssignment {
	/**
	 * PrefixExpression constructor comment.
	 * @param l org.eclipse.jdt.internal.compiler.ast.Expression
	 * @param r org.eclipse.jdt.internal.compiler.ast.Expression
	 * @param op int
	 */
	public PrefixExpression(Expression l, Expression e, int op, int pos) {
		super(l, e, op);
		sourceStart = pos;
		sourceEnd = l.sourceEnd;
	}

	public String operatorToString() {
		switch (operator) {
			case PLUS :
				return "++";
			case MINUS :
				return "--";
		}
		return "unknown operator";
	}

	public boolean restrainUsageToNumericTypes() {
		return true;
	}

	public String toStringExpressionNoParenthesis() {
		/* slow code */

		return operatorToString() + " " + lhs.toStringExpression();

	}

	public void traverse(IAbstractSyntaxTreeVisitor visitor, BlockScope scope) {
		if (visitor.visit(this, scope)) {
			lhs.traverse(visitor, scope);
		}
		visitor.endVisit(this, scope);
	}

}
