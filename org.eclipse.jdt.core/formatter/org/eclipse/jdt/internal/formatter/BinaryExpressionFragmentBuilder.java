/*******************************************************************************
 * Copyright (c) 2002, 2003 International Business Machines Corp. and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0 
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 ******************************************************************************/
package org.eclipse.jdt.internal.formatter;

import java.util.ArrayList;

import org.eclipse.jdt.internal.compiler.AbstractSyntaxTreeVisitorAdapter;
import org.eclipse.jdt.internal.compiler.ast.AND_AND_Expression;
import org.eclipse.jdt.internal.compiler.ast.AllocationExpression;
import org.eclipse.jdt.internal.compiler.ast.ArrayAllocationExpression;
import org.eclipse.jdt.internal.compiler.ast.ArrayInitializer;
import org.eclipse.jdt.internal.compiler.ast.ArrayQualifiedTypeReference;
import org.eclipse.jdt.internal.compiler.ast.ArrayReference;
import org.eclipse.jdt.internal.compiler.ast.ArrayTypeReference;
import org.eclipse.jdt.internal.compiler.ast.Assignment;
import org.eclipse.jdt.internal.compiler.ast.AstNode;
import org.eclipse.jdt.internal.compiler.ast.BinaryExpression;
import org.eclipse.jdt.internal.compiler.ast.CastExpression;
import org.eclipse.jdt.internal.compiler.ast.CharLiteral;
import org.eclipse.jdt.internal.compiler.ast.CompoundAssignment;
import org.eclipse.jdt.internal.compiler.ast.ConditionalExpression;
import org.eclipse.jdt.internal.compiler.ast.DoubleLiteral;
import org.eclipse.jdt.internal.compiler.ast.EqualExpression;
import org.eclipse.jdt.internal.compiler.ast.Expression;
import org.eclipse.jdt.internal.compiler.ast.ExtendedStringLiteral;
import org.eclipse.jdt.internal.compiler.ast.FalseLiteral;
import org.eclipse.jdt.internal.compiler.ast.FieldReference;
import org.eclipse.jdt.internal.compiler.ast.FloatLiteral;
import org.eclipse.jdt.internal.compiler.ast.InstanceOfExpression;
import org.eclipse.jdt.internal.compiler.ast.IntLiteral;
import org.eclipse.jdt.internal.compiler.ast.LongLiteral;
import org.eclipse.jdt.internal.compiler.ast.MessageSend;
import org.eclipse.jdt.internal.compiler.ast.NullLiteral;
import org.eclipse.jdt.internal.compiler.ast.OR_OR_Expression;
import org.eclipse.jdt.internal.compiler.ast.OperatorIds;
import org.eclipse.jdt.internal.compiler.ast.PostfixExpression;
import org.eclipse.jdt.internal.compiler.ast.PrefixExpression;
import org.eclipse.jdt.internal.compiler.ast.QualifiedAllocationExpression;
import org.eclipse.jdt.internal.compiler.ast.QualifiedNameReference;
import org.eclipse.jdt.internal.compiler.ast.QualifiedSuperReference;
import org.eclipse.jdt.internal.compiler.ast.QualifiedThisReference;
import org.eclipse.jdt.internal.compiler.ast.SingleNameReference;
import org.eclipse.jdt.internal.compiler.ast.StringLiteral;
import org.eclipse.jdt.internal.compiler.ast.SuperReference;
import org.eclipse.jdt.internal.compiler.ast.ThisReference;
import org.eclipse.jdt.internal.compiler.ast.TrueLiteral;
import org.eclipse.jdt.internal.compiler.ast.UnaryExpression;
import org.eclipse.jdt.internal.compiler.lookup.BlockScope;
import org.eclipse.jdt.internal.compiler.lookup.ClassScope;
import org.eclipse.jdt.internal.compiler.parser.TerminalTokens;

class BinaryExpressionFragmentBuilder
	extends AbstractSyntaxTreeVisitorAdapter {
		
	ArrayList fragmentsList;
	ArrayList operatorsList;

	BinaryExpressionFragmentBuilder() {
		this.fragmentsList = new ArrayList();
		this.operatorsList = new ArrayList();
	}

	private boolean buildFragments(Expression expression) {
		if (((expression.bits & AstNode.ParenthesizedMASK) >> AstNode.ParenthesizedSHIFT) != 0) {
			this.fragmentsList.add(expression);
			return false;
		} else {
			return true;
		}
	}

	public AstNode[] fragments() {
		AstNode[] fragments = new AstNode[this.fragmentsList.size()];
		this.fragmentsList.toArray(fragments);
		return fragments;
	}
	
	public int[] operators() {
		int length = operatorsList.size();
		int[] tab = new int[length];
		for (int i = 0; i < length; i++) {
			tab[i] = ((Integer)operatorsList.get(i)).intValue();
		}
		return tab;
	}

	public int size() {
		return this.fragmentsList.size();
	}
	
	public boolean visit(
		AllocationExpression allocationExpression,
		BlockScope scope) {
			this.fragmentsList.add(allocationExpression);
			return false;
	}

	public boolean visit(
		AND_AND_Expression and_and_Expression,
		BlockScope scope) {
			if (buildFragments(and_and_Expression)) {
				this.operatorsList.add(new Integer(TerminalTokens.TokenNameAND_AND));
				return true;
			} else {
				return false;
			}
	}

	public boolean visit(
		ArrayAllocationExpression arrayAllocationExpression,
		BlockScope scope) {
			this.fragmentsList.add(arrayAllocationExpression);
			return false;
	}

	public boolean visit(ArrayInitializer arrayInitializer, BlockScope scope) {
		this.fragmentsList.add(arrayInitializer);
		return false;
	}
	
	public boolean visit(
		ArrayQualifiedTypeReference arrayQualifiedTypeReference,
		BlockScope scope) {
			this.fragmentsList.add(arrayQualifiedTypeReference);
			return false;
	}

	public boolean visit(
		ArrayQualifiedTypeReference arrayQualifiedTypeReference,
		ClassScope scope) {
			this.fragmentsList.add(arrayQualifiedTypeReference);
			return false;
	}

	public boolean visit(ArrayReference arrayReference, BlockScope scope) {
		this.fragmentsList.add(arrayReference);
		return false;
	}

	public boolean visit(
		ArrayTypeReference arrayTypeReference,
		BlockScope scope) {
			this.fragmentsList.add(arrayTypeReference);
			return false;
	}

	public boolean visit(
		ArrayTypeReference arrayTypeReference,
		ClassScope scope) {
			this.fragmentsList.add(arrayTypeReference);
			return false;
	}

	public boolean visit(Assignment assignment, BlockScope scope) {
		this.fragmentsList.add(assignment);
		return false;
	}

	public boolean visit(BinaryExpression binaryExpression, BlockScope scope) {
		final int numberOfParens = (binaryExpression.bits & AstNode.ParenthesizedMASK) >> AstNode.ParenthesizedSHIFT;
		if (numberOfParens > 0) {
			this.fragmentsList.add(binaryExpression);
		} else {
			switch((binaryExpression.bits & AstNode.OperatorMASK) >> AstNode.OperatorSHIFT) {
				case OperatorIds.PLUS :
					if (buildFragments(binaryExpression)) {
						this.operatorsList.add(new Integer(TerminalTokens.TokenNamePLUS));
						return true;
					} else {
						return false;
					}
				case OperatorIds.MINUS :
					if (buildFragments(binaryExpression)) {
						this.operatorsList.add(new Integer(TerminalTokens.TokenNameMINUS));
						return true;
					} else {
						return false;
					}
				case OperatorIds.OR :
					if (buildFragments(binaryExpression)) {
						this.operatorsList.add(new Integer(TerminalTokens.TokenNameOR));
						return true;
					} else {
						return false;
					}
				case OperatorIds.AND :
					if (buildFragments(binaryExpression)) {
						this.operatorsList.add(new Integer(TerminalTokens.TokenNameAND));
						return true;
					} else {
						return false;
					}
				default:
					this.fragmentsList.add(binaryExpression);
			}
		}
		return false;
	}

	public boolean visit(CastExpression castExpression, BlockScope scope) {
		this.fragmentsList.add(castExpression);
		return false;
	}

	public boolean visit(CharLiteral charLiteral, BlockScope scope) {
		this.fragmentsList.add(charLiteral);
		return false;
	}

	public boolean visit(
		CompoundAssignment compoundAssignment,
		BlockScope scope) {
			this.fragmentsList.add(compoundAssignment);
			return false;
	}

	public boolean visit(
		ConditionalExpression conditionalExpression,
		BlockScope scope) {
			this.fragmentsList.add(conditionalExpression);
			return false;
	}

	public boolean visit(DoubleLiteral doubleLiteral, BlockScope scope) {
		this.fragmentsList.add(doubleLiteral);
		return false;
	}

	public boolean visit(EqualExpression equalExpression, BlockScope scope) {
		this.fragmentsList.add(equalExpression);
		return false;
	}

	public boolean visit(
		ExtendedStringLiteral extendedStringLiteral,
		BlockScope scope) {
			this.fragmentsList.add(extendedStringLiteral);
			return false;
	}

	public boolean visit(FalseLiteral falseLiteral, BlockScope scope) {
		this.fragmentsList.add(falseLiteral);
		return false;
	}

	public boolean visit(FieldReference fieldReference, BlockScope scope) {
		this.fragmentsList.add(fieldReference);
		return false;
	}

	public boolean visit(FloatLiteral floatLiteral, BlockScope scope) {
		this.fragmentsList.add(floatLiteral);
		return false;
	}

	public boolean visit(
		InstanceOfExpression instanceOfExpression,
		BlockScope scope) {
			this.fragmentsList.add(instanceOfExpression);
			return false;
	}

	public boolean visit(IntLiteral intLiteral, BlockScope scope) {
		this.fragmentsList.add(intLiteral);
		return false;
	}

	public boolean visit(LongLiteral longLiteral, BlockScope scope) {
		this.fragmentsList.add(longLiteral);
		return false;
	}

	public boolean visit(MessageSend messageSend, BlockScope scope) {
		this.fragmentsList.add(messageSend);
		return false;
	}

	public boolean visit(NullLiteral nullLiteral, BlockScope scope) {
		this.fragmentsList.add(nullLiteral);
		return false;
	}

	public boolean visit(OR_OR_Expression or_or_Expression, BlockScope scope) {
		if (buildFragments(or_or_Expression)) {
			this.operatorsList.add(new Integer(TerminalTokens.TokenNameOR_OR));
			return true;
		} else {
			return false;
		}
	}

	public boolean visit(
		PostfixExpression postfixExpression,
		BlockScope scope) {
			this.fragmentsList.add(postfixExpression);
			return false;
	}

	public boolean visit(PrefixExpression prefixExpression, BlockScope scope) {
		this.fragmentsList.add(prefixExpression);
		return false;
	}

	public boolean visit(
		QualifiedAllocationExpression qualifiedAllocationExpression,
		BlockScope scope) {
			this.fragmentsList.add(qualifiedAllocationExpression);
			return false;
	}
	public boolean visit(
		QualifiedNameReference qualifiedNameReference,
		BlockScope scope) {
			this.fragmentsList.add(qualifiedNameReference);
			return false;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jdt.internal.compiler.IAbstractSyntaxTreeVisitor#visit(org.eclipse.jdt.internal.compiler.ast.QualifiedSuperReference, org.eclipse.jdt.internal.compiler.lookup.BlockScope)
	 */
	public boolean visit(
			QualifiedSuperReference qualifiedSuperReference,
			BlockScope scope) {
		this.fragmentsList.add(qualifiedSuperReference);
		return false;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jdt.internal.compiler.IAbstractSyntaxTreeVisitor#visit(org.eclipse.jdt.internal.compiler.ast.QualifiedThisReference, org.eclipse.jdt.internal.compiler.lookup.BlockScope)
	 */
	public boolean visit(
			QualifiedThisReference qualifiedThisReference,
			BlockScope scope) {
		this.fragmentsList.add(qualifiedThisReference);
		return false;
	}

	public boolean visit(
		SingleNameReference singleNameReference,
		BlockScope scope) {
			this.fragmentsList.add(singleNameReference);
			return false;
	}

	public boolean visit(StringLiteral stringLiteral, BlockScope scope) {
		this.fragmentsList.add(stringLiteral);
		return false;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jdt.internal.compiler.IAbstractSyntaxTreeVisitor#visit(org.eclipse.jdt.internal.compiler.ast.SuperReference, org.eclipse.jdt.internal.compiler.lookup.BlockScope)
	 */
	public boolean visit(SuperReference superReference, BlockScope scope) {
		this.fragmentsList.add(superReference);
		return false;
	}

	public boolean visit(ThisReference thisReference, BlockScope scope) {
		this.fragmentsList.add(thisReference);
		return false;
	}

	public boolean visit(TrueLiteral trueLiteral, BlockScope scope) {
		this.fragmentsList.add(trueLiteral);
		return false;
	}

	public boolean visit(UnaryExpression unaryExpression, BlockScope scope) {
		this.fragmentsList.add(unaryExpression);
		return false;
	}

}
