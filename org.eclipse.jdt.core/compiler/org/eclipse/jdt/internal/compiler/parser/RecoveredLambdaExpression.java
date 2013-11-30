/*******************************************************************************
 * Copyright (c) 2013 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * This is an implementation of an early-draft specification developed under the Java
 * Community Process (JCP) and is made available for testing and evaluation purposes
 * only. The code is not compatible with any specification of the JCP.
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/

package org.eclipse.jdt.internal.compiler.parser;

import java.util.HashSet;
import java.util.Set;

import org.eclipse.jdt.internal.compiler.ast.ASTNode;
import org.eclipse.jdt.internal.compiler.ast.Block;
import org.eclipse.jdt.internal.compiler.ast.LambdaExpression;
import org.eclipse.jdt.internal.compiler.ast.Statement;

public class RecoveredLambdaExpression extends RecoveredBlock {

	private LambdaExpression expression;
	private boolean haveBlockBody = false;
	private boolean haveExpressionBody = false;
	private RecoveredStatement bodyExpression;
	
	public RecoveredLambdaExpression(LambdaExpression expression, RecoveredElement parent, int bracketBalance){
		super(new Block(0), parent, bracketBalance); // don't have a block yet. May never have, in that event will course correct.
		this.expression = expression;
		this.expression.setBody(this.blockDeclaration);
	}
	
	/*
	 * Record a nested block declaration
	 */
	public RecoveredElement add(Block block, int bracketBalanceValue) {
		if (!this.haveBlockBody && !this.haveExpressionBody) {
			this.haveBlockBody = true;
			this.haveExpressionBody = false;
			this.blockDeclaration = block;
			return this; 
		}
		return super.add(block, bracketBalanceValue);
	}

	/*
	 * Record a nested block declaration
	 */
	public RecoveredElement add(LambdaExpression lambda, int bracketBalanceValue) {
		if (!this.haveBlockBody && !this.haveExpressionBody) {
			this.haveBlockBody = false;
			this.haveExpressionBody = true;
			this.bodyExpression = new RecoveredLambdaExpression(lambda, this, bracketBalanceValue);
			this.expression.setBody(lambda);
			return this.bodyExpression;
		}
		return super.add(lambda, bracketBalanceValue);
	}
	
	/*
	 * Record a statement declaration
	 */
	public RecoveredElement add(Statement stmt, int bracketBalanceValue) {
		return this.add(stmt, bracketBalanceValue, false);
	}

	/*
	 * Record a statement declaration
	 */
	public RecoveredElement add(Statement stmt, int bracketBalanceValue, boolean delegatedByParent) {
		if (!this.haveBlockBody && !this.haveExpressionBody) {
			this.haveBlockBody = false;
			this.haveExpressionBody = true;
			this.bodyExpression = new RecoveredStatement(stmt, this, bracketBalanceValue);
			this.expression.setBody(stmt);
			return this.bodyExpression;
		}
		return super.add(stmt, bracketBalanceValue, delegatedByParent);
	}
	
	/*
	 * Answer the associated parsed structure
	 */
	public ASTNode parseTree(){
		return updatedLambdaExpression(0, new HashSet());
	}

	public LambdaExpression updatedLambdaExpression(int depth, Set knownTypes) {
		if (this.haveBlockBody)
			this.expression.setBody(super.updatedStatement(depth, knownTypes));
		else if (this.bodyExpression != null)
			this.expression.setBody(this.bodyExpression.updatedStatement(depth, knownTypes));
		return this.expression;
	}
	/*
	 * Rebuild a statement from the nested structure which is in scope
	 */
	public Statement updatedStatement(int depth, Set knownTypes){
		return updatedLambdaExpression(depth, knownTypes);
	}
	/*
	 * Final update the corresponding parse node
	 */
	public void updateParseTree(){
		updatedLambdaExpression(0, new HashSet());
	}
	/*
	 * Rebuild a flattened block from the nested structure which is in scope
	 */
	public Statement updateStatement(int depth, Set knownTypes){
		return updatedLambdaExpression(depth, knownTypes);
	}
	
	public String toString(int tab) {
		StringBuffer result = new StringBuffer(tabString(tab));
		result.append("Recovered Lambda Expression:\n"); //$NON-NLS-1$
		this.expression.print(tab + 1, result);
		return result.toString();
	}
}