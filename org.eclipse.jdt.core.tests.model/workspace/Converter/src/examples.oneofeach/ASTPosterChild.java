/*******************************************************************************
 * Copyright (c) 2002 IBM Corp. and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0 
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial implementation
 ******************************************************************************/
/*
 * Syntactically correct Java program containing at least an instance of 
 * each different kind of construct in the Java language (1.4).
 */
// PackageDeclaration
package examples.oneofeach;

// ImportDeclaration (single-type)
import java.io.Serializable;
// ImportDeclaration (on-demand)
import java.util.*;

/**
 * TypeDeclaration (class)
 */
public final class ASTPosterChild extends java.lang.Object 
	implements Serializable, ASTPosterInterface {

	/**
	 * FieldDeclaration
	 */
	public transient boolean[] f1 = {true, false};
	
	// Initializer
	{
		f1 = null;
	}	
	
	/**
	 * MethodDeclaration (constructor)
	 */
	ASTPosterChild() {
		// ConstructorInvocation
		this(true);
	}	

	/**
	 * MethodDeclaration (constructor)
	 */
	ASTPosterChild(boolean flag) {
		// SuperConstructorInvocation
		super();
	}	

	/**
	 * MethodDeclaration (method)
	 */
	public void op() {

		// PrimitiveType, NumberLiteral
		byte a = 0;
		// BooleanLiteral
		boolean b = true;
		// CharacterLiteral
		char c = 'x';
		short s = 1;
		long l = 1L;
		float f = 1.0f;
		double d = 1e6;
		// SimpleType, StringLiteral
		String z = "hello";//$NON-NLS-1$
		// NullLiteral
		Vector o1 = null;
		// ArrayType, ArrayInitializer
		int[] t1 = {1};
		int[][] t2 = {{1,2}, {3,4}};
		int[][][] t3 = null;
		// ClassInstanceCreation
		java.lang.Object o = new Object();
		// ArrayCreation (initializer)
		Object[] o2 = new Object[]{"0", "1"};//$NON-NLS-1$//$NON-NLS-2$
		// ArrayCreation (dimensions)
		Object[][] o3 = new Object[3][];
		// Assigment
		o3 = null;
		// ArrayAccess
		Object x1 = o3[0];
		// CastExpression
		Object x2 = (Object) o3;
		// ConditionalExpression				
		int x3 = b ? 1 : 2;
		// FieldAccess
		Object x4 = System.out;
		// InfixExpression
		String x5 = "a" + "b";//$NON-NLS-1$//$NON-NLS-2$
		// InfixExpression (extended operands)
		String x6 = "a" + "b" + "c" + "d";//$NON-NLS-1$//$NON-NLS-2$//$NON-NLS-3$//$NON-NLS-4$
		// MethodInvocation
		notifyAll();
		// ParenthesizedExpression
		Object x7 = ((o));
		// PostfixExpression
		l++;
		// PrefixExpression
		--l;
		// ThisExpression
		Object x8 = ASTPosterChild.this;
		// TypeLiteral
		Object x9 = void.class;

		// AssertStatement
		// assert true: "unhappy";//$NON-NLS-1$
		// Block
		{}
		// DoStatement
		do {
		} while (false);
		// VariableDeclarationStatement
		int i;
		// ExpressionStatement
		i = 1;
		// LabeledStatement
		looping:
			// ForStatement, VariableDeclarationExpression
			for (int j=0; j<10; j++) {
				// ContinueStatement
				continue looping;
			}
		// IfStatement
		if (false) 
			System.exit(0);
		else 
			i++;
		// SwitchStatement
		switch (2) {
			// SwitchCase
			case 2:
				// BreakStatement
				break;
			default:
				// EmptyStatement
				;
		}
		// SynchronizedStatement
		synchronized (this) {
		}
		// TryStatement
		try {
			// ThrowStatement
			throw new RuntimeException();
		} catch (Exception e) // CatchClause
		{
		} finally {
		}

		// TypeDeclarationStatement
		abstract class X {
			int f;
			void m() {
			}
		}
		// ClassInstanceCreation (with body declarations)
		Object anon = new X() {
			void m() {
				// SuperMethodInvocation
				super.m();
				// SuperFieldAccess
				super.f = 1;
			}
		};
		// WhileStatement
		boolean b1 = true;
		while (b1);
		// ReturnStatement;
		return;
	}	

}

/**
 * TypeDeclaration (interface)
 */
interface ASTPosterInterface {

	String FOO = "hi";//$NON-NLS-1$
	void op();

}