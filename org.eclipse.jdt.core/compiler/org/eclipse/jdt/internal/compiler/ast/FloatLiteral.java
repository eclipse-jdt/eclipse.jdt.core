/*******************************************************************************
 * Copyright (c) 2000, 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jdt.internal.compiler.ast;

import org.eclipse.jdt.internal.compiler.ASTVisitor;
import org.eclipse.jdt.internal.compiler.codegen.CodeStream;
import org.eclipse.jdt.internal.compiler.impl.Constant;
import org.eclipse.jdt.internal.compiler.lookup.BlockScope;
import org.eclipse.jdt.internal.compiler.lookup.TypeBinding;
import org.eclipse.jdt.internal.compiler.util.Util;

public class FloatLiteral extends NumberLiteral {
	float value;
	final static float Float_MIN_VALUE = Float.intBitsToFloat(1); // work-around VAJ problem 1F6IGUU
	public FloatLiteral(char[] token, int s, int e) {
		super(token, s, e);
	}
	public void computeConstant() {
		//the source is correctly formated so the exception should never occurs
		Float computedValue;
		try {
			computedValue = Float.valueOf(String.valueOf(source));
		} catch (NumberFormatException e) {
			/*
			 * this can happen if this is an hexadecimal floating-point literal and the libraries used 
			 * are < 1.5
			 */
			computedValue = new Float(Util.getFloatingPoint(source));
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
					case 'x' :
					case 'X' :
						break;
					case 'e' :
					case 'E' :
					case 'p' :
					case 'P' :
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
	public void generateCode(BlockScope currentScope, CodeStream codeStream, boolean valueRequired) {
		int pc = codeStream.position;
		if (valueRequired)
			if ((implicitConversion >> 4) == T_float)
				codeStream.generateInlinedValue(value);
			else
				codeStream.generateConstant(constant, implicitConversion);
		codeStream.recordPositionsFrom(pc, this.sourceStart);
	}
	public TypeBinding literalType(BlockScope scope) {
		return FloatBinding;
	}
	public void traverse(ASTVisitor visitor, BlockScope blockScope) {
		visitor.visit(this, blockScope);
		visitor.endVisit(this, blockScope);
	}
}
