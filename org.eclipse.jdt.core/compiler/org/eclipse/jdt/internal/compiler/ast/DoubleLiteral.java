package org.eclipse.jdt.internal.compiler.ast;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
import org.eclipse.jdt.internal.compiler.IAbstractSyntaxTreeVisitor;
import org.eclipse.jdt.internal.compiler.impl.*;
import org.eclipse.jdt.internal.compiler.codegen.*;
import org.eclipse.jdt.internal.compiler.lookup.*;

public class DoubleLiteral extends NumberLiteral {
	double value;
	public DoubleLiteral(char[] token, int s, int e) {
		super(token, s, e);
	}

	public void computeConstant() {

		//the source is correctly formated so the exception should never occurs

		Double computedValue;
		try {
			computedValue = Double.valueOf(String.valueOf(source));
		} catch (NumberFormatException e) {
			return;
		} //how can it happen ????

		if (computedValue.doubleValue() > Double.MAX_VALUE)
			return; //may be Infinity
		if (computedValue.doubleValue() < Double.MIN_VALUE) {
			//only a true 0 can be made of zeros :-)
			//2.00000000000000000e-324 is illegal .... 
			label : for (
				int i = 0;
					i < source.length;
					i++) { //it is welled formated so just test against '0' and potential . D d  
				switch (source[i]) {
					case '0' :
					case '.' :
					case 'd' :
					case 'D' :
						break;
					case 'e' :
					case 'E' :
						break label; //exposant are valid....!
					default :
						return;
				}
			}
		} //error

		constant = Constant.fromValue(value = computedValue.doubleValue());
	}

	/**
	 * Code generation for the double literak
	 *
	 * @param currentScope org.eclipse.jdt.internal.compiler.lookup.BlockScope
	 * @param codeStream org.eclipse.jdt.internal.compiler.codegen.CodeStream
	 * @param valueRequired boolean
	 */
	public void generateCode(
		BlockScope currentScope,
		CodeStream codeStream,
		boolean valueRequired) {
		int pc = codeStream.position;
		if (valueRequired)
			if ((implicitConversion >> 4) == T_double)
				codeStream.generateInlinedValue(value);
			else
				codeStream.generateConstant(constant, implicitConversion);
		codeStream.recordPositionsFrom(pc, this);
	}

	public TypeBinding literalType(BlockScope scope) {
		return DoubleBinding;
	}

	public void traverse(
		IAbstractSyntaxTreeVisitor visitor,
		BlockScope blockScope) {
		visitor.visit(this, blockScope);
		visitor.endVisit(this, blockScope);
	}

}
