/*
 * Created on May 12, 2003
 *
 * To change this generated comment go to 
 * Window>Preferences>Java>Code Generation>Code Template
 */
package org.eclipse.jdt.internal.formatter;

import java.util.ArrayList;

import org.eclipse.jdt.core.compiler.ITerminalSymbols;
import org.eclipse.jdt.internal.compiler.AbstractSyntaxTreeVisitorAdapter;
import org.eclipse.jdt.internal.compiler.ast.AND_AND_Expression;
import org.eclipse.jdt.internal.compiler.ast.AllocationExpression;
import org.eclipse.jdt.internal.compiler.ast.ArrayAllocationExpression;
import org.eclipse.jdt.internal.compiler.ast.ArrayInitializer;
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
import org.eclipse.jdt.internal.compiler.ast.FloatLiteral;
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
import org.eclipse.jdt.internal.compiler.ast.SingleNameReference;
import org.eclipse.jdt.internal.compiler.ast.StringLiteral;
import org.eclipse.jdt.internal.compiler.ast.TrueLiteral;
import org.eclipse.jdt.internal.compiler.ast.UnaryExpression;
import org.eclipse.jdt.internal.compiler.lookup.BlockScope;

public class BinaryExpressionFragmentBuilder
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
			return buildFragments(allocationExpression);
	}

	public boolean visit(
		AND_AND_Expression and_and_Expression,
		BlockScope scope) {
			if (buildFragments(and_and_Expression)) {
				this.operatorsList.add(new Integer(ITerminalSymbols.TokenNameAND_AND));
				return true;
			} else {
				return false;
			}
	}

	public boolean visit(
		ArrayAllocationExpression arrayAllocationExpression,
		BlockScope scope) {
			return buildFragments(arrayAllocationExpression);
	}

	public boolean visit(ArrayInitializer arrayInitializer, BlockScope scope) {
		return buildFragments(arrayInitializer);
	}

	public boolean visit(Assignment assignment, BlockScope scope) {
		return buildFragments(assignment);
	}

	public boolean visit(BinaryExpression binaryExpression, BlockScope scope) {
		if (buildFragments(binaryExpression)) {
			switch((binaryExpression.bits & EqualExpression.OperatorMASK) >> EqualExpression.OperatorSHIFT) {
				case OperatorIds.AND :
					this.operatorsList.add(new Integer(ITerminalSymbols.TokenNameAND));
					break;
				case OperatorIds.DIVIDE :
					this.operatorsList.add(new Integer(ITerminalSymbols.TokenNameDIVIDE));
					break;
				case OperatorIds.GREATER :
					this.operatorsList.add(new Integer(ITerminalSymbols.TokenNameGREATER));
					break;
				case OperatorIds.GREATER_EQUAL :
					this.operatorsList.add(new Integer(ITerminalSymbols.TokenNameGREATER_EQUAL));
					break;
				case OperatorIds.LEFT_SHIFT :
					this.operatorsList.add(new Integer(ITerminalSymbols.TokenNameLEFT_SHIFT));
					break;
				case OperatorIds.LESS :
					this.operatorsList.add(new Integer(ITerminalSymbols.TokenNameLESS));
					break;
				case OperatorIds.LESS_EQUAL :
					this.operatorsList.add(new Integer(ITerminalSymbols.TokenNameLESS_EQUAL));
					break;
				case OperatorIds.MINUS :
					this.operatorsList.add(new Integer(ITerminalSymbols.TokenNameMINUS));
					break;
				case OperatorIds.MULTIPLY :
					this.operatorsList.add(new Integer(ITerminalSymbols.TokenNameMULTIPLY));
					break;
				case OperatorIds.OR :
					this.operatorsList.add(new Integer(ITerminalSymbols.TokenNameOR));
					break;
				case OperatorIds.PLUS :
					this.operatorsList.add(new Integer(ITerminalSymbols.TokenNamePLUS));
					break;
				case OperatorIds.REMAINDER :
					this.operatorsList.add(new Integer(ITerminalSymbols.TokenNameREMAINDER));
					break;
				case OperatorIds.RIGHT_SHIFT :
					this.operatorsList.add(new Integer(ITerminalSymbols.TokenNameRIGHT_SHIFT));
					break;
				case OperatorIds.UNSIGNED_RIGHT_SHIFT :
					this.operatorsList.add(new Integer(ITerminalSymbols.TokenNameUNSIGNED_RIGHT_SHIFT));
					break;
				case OperatorIds.XOR :
					this.operatorsList.add(new Integer(ITerminalSymbols.TokenNameXOR));
			}
			return true;
		} else {
			return false;
		}
	}

	public boolean visit(CastExpression castExpression, BlockScope scope) {
		return buildFragments(castExpression);
	}

	public boolean visit(CharLiteral charLiteral, BlockScope scope) {
		return buildFragments(charLiteral);
	}

	public boolean visit(
		CompoundAssignment compoundAssignment,
		BlockScope scope) {
			return buildFragments(compoundAssignment);
	}

	public boolean visit(
		ConditionalExpression conditionalExpression,
		BlockScope scope) {
			return buildFragments(conditionalExpression);
	}

	public boolean visit(DoubleLiteral doubleLiteral, BlockScope scope) {
		return buildFragments(doubleLiteral);
	}

	public boolean visit(EqualExpression equalExpression, BlockScope scope) {
		if (buildFragments(equalExpression)) {
			switch((equalExpression.bits & EqualExpression.OperatorMASK) >> EqualExpression.OperatorSHIFT) {
				case OperatorIds.EQUAL_EQUAL :
					this.operatorsList.add(new Integer(ITerminalSymbols.TokenNameEQUAL_EQUAL));
					break;
				case OperatorIds.NOT_EQUAL :
					this.operatorsList.add(new Integer(ITerminalSymbols.TokenNameNOT_EQUAL));
					break;
			}
			return true;
		} else {
			return false;
		}
	}

	public boolean visit(
		ExtendedStringLiteral extendedStringLiteral,
		BlockScope scope) {
			return buildFragments(extendedStringLiteral);
	}

	public boolean visit(FalseLiteral falseLiteral, BlockScope scope) {
		return buildFragments(falseLiteral);
	}

	public boolean visit(FloatLiteral floatLiteral, BlockScope scope) {
		return buildFragments(floatLiteral);
	}

	public boolean visit(IntLiteral intLiteral, BlockScope scope) {
		return buildFragments(intLiteral);
	}

	public boolean visit(LongLiteral longLiteral, BlockScope scope) {
		return buildFragments(longLiteral);
	}

	public boolean visit(MessageSend messageSend, BlockScope scope) {
		return buildFragments(messageSend);
	}

	public boolean visit(NullLiteral nullLiteral, BlockScope scope) {
		return buildFragments(nullLiteral);
	}

	public boolean visit(OR_OR_Expression or_or_Expression, BlockScope scope) {
		if (buildFragments(or_or_Expression)) {
			this.operatorsList.add(new Integer(ITerminalSymbols.TokenNameOR_OR));
			return true;
		} else {
			return false;
		}
	}

	public boolean visit(
		PostfixExpression postfixExpression,
		BlockScope scope) {
			return buildFragments(postfixExpression);
	}

	public boolean visit(PrefixExpression prefixExpression, BlockScope scope) {
		return buildFragments(prefixExpression);
	}

	public boolean visit(
		QualifiedAllocationExpression qualifiedAllocationExpression,
		BlockScope scope) {
			return buildFragments(qualifiedAllocationExpression);
	}

	public boolean visit(StringLiteral stringLiteral, BlockScope scope) {
		return buildFragments(stringLiteral);
	}

	public boolean visit(TrueLiteral trueLiteral, BlockScope scope) {
		return buildFragments(trueLiteral);
	}

	public boolean visit(UnaryExpression unaryExpression, BlockScope scope) {
		return buildFragments(unaryExpression);
	}
	public boolean visit(
		QualifiedNameReference qualifiedNameReference,
		BlockScope scope) {
			return buildFragments(qualifiedNameReference);
	}

	public boolean visit(
		SingleNameReference singleNameReference,
		BlockScope scope) {
			return buildFragments(singleNameReference);
	}

}
