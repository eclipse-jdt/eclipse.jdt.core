package org.eclipse.jdt.internal.compiler.ast;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
import org.eclipse.jdt.internal.compiler.IAbstractSyntaxTreeVisitor;
import org.eclipse.jdt.internal.compiler.impl.*;
import org.eclipse.jdt.internal.compiler.codegen.*;
import org.eclipse.jdt.internal.compiler.lookup.*;

public class FloatLiteral extends NumberLiteral {
	float value;
	final static float Float_MIN_VALUE = Float.intBitsToFloat(1);
	// work-around VAJ problem 1F6IGUU
	public FloatLiteral(char[] token, int s, int e) {
		super(token, s, e);
	}

	public void computeConstant() {

		//the source is correctly formated so the exception should never occurs

		Float computedValue;
		try {
			computedValue = Float.valueOf(String.valueOf(source));
		} catch (NumberFormatException e) {
			return;
		}

		if (computedValue.doubleValue() > Float.MAX_VALUE) {
			return; //may be Infinity
		}
		if (computedValue.floatValue() < Float_MIN_VALUE) {
			// see 1F6IGUU
			//only a true 0 can be made of zeros
			//1.00000000e-46f is illegal ....
			label : for (int i = 0; i < source.length; i++) {
				switch (source[i]) {
					case '.' :
					case 'f' :
					case 'F' :
					case '0' :
						break;
					case 'e' :
					case 'E' :
						break label; //exposant are valid !....
					default :
						return; //error

				}
			}
		}
		constant = Constant.fromValue(value = computedValue.floatValue());
	}

	/**
	 * Code generation for float literal
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
			if ((implicitConversion >> 4) == T_float)
				codeStream.generateInlinedValue(value);
			else
				codeStream.generateConstant(constant, implicitConversion);
		codeStream.recordPositionsFrom(pc, this);
	}

	public TypeBinding literalType(BlockScope scope) {
		return FloatBinding;
	}

	public void traverse(
		IAbstractSyntaxTreeVisitor visitor,
		BlockScope blockScope) {
		visitor.visit(this, blockScope);
		visitor.endVisit(this, blockScope);
	}

}
