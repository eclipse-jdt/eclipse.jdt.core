package org.eclipse.jdt.internal.compiler.ast;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
import org.eclipse.jdt.internal.compiler.impl.*;
import org.eclipse.jdt.internal.compiler.flow.*;
import org.eclipse.jdt.internal.compiler.lookup.*;

public abstract class Literal extends Expression {

	public Literal(int s, int e) {
		sourceStart = s;
		sourceEnd = e;
	}

	public abstract void computeConstant();
	//ON ERROR constant STAYS NULL
	public abstract TypeBinding literalType(BlockScope scope);
	public TypeBinding resolveType(BlockScope scope) {
		// compute the real value, which must range its type's range

		computeConstant();
		if (constant == null) {
			scope.problemReporter().constantOutOfRange(this);
			constant = Constant.NotAConstant;
			return null;
		}
		return literalType(scope);
	}

	public abstract char[] source();
}
