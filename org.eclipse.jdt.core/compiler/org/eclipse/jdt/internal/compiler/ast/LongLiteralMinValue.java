package org.eclipse.jdt.internal.compiler.ast;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
import org.eclipse.jdt.internal.compiler.impl.*;
import org.eclipse.jdt.internal.compiler.lookup.*;

public class LongLiteralMinValue extends LongLiteral {

	final static char[] CharValue =
		new char[] {
			'-',
			'9',
			'2',
			'2',
			'3',
			'3',
			'7',
			'2',
			'0',
			'3',
			'6',
			'8',
			'5',
			'4',
			'7',
			'7',
			'5',
			'8',
			'0',
			'8',
			'L' };
	final static Constant MIN_VALUE = Constant.fromValue(Long.MIN_VALUE);

	public LongLiteralMinValue() {
		super(CharValue, 0, 0, Long.MIN_VALUE);
		constant = MIN_VALUE;
	}

	public void computeConstant() {

		/*precomputed at creation time*/
	}

}
