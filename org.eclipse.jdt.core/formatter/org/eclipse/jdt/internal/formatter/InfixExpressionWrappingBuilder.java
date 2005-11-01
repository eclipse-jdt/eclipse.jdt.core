package org.eclipse.jdt.internal.formatter;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.InfixExpression;

public class InfixExpressionWrappingBuilder extends ASTVisitor {
	private final static int DEFAULT_SIZE = 10;
	ArrayList fragments = new ArrayList();
	int[] operators = new int[DEFAULT_SIZE];
	int operatorCounter = 0;
	
	public int getFragmentsCounter() {
		return this.fragments.size();
	}
	
	public int[] getOperators() {
		if (operators.length != operatorCounter) {
			// resize
			System.arraycopy(this.operators, 0, (this.operators = new int[this.operatorCounter]), 0, this.operatorCounter);
		}
		return this.operators;
	}
	private boolean isSplittable(Expression expression) {
//		if (expression instanceof InfixExpression) {
//			InfixExpression infixExpression = (InfixExpression) expression;
//			return infixExpression.getLeftOperand() instanceof InfixExpression
//					||  infixExpression.getRightOperand() instanceof InfixExpression;
//		}
//		return false;
		return true;
	}
	public boolean visit(InfixExpression node) {
		final Expression leftOperand = node.getLeftOperand();
		final Expression rightOperand = node.getRightOperand();
		if (leftOperand.getNodeType() == ASTNode.INFIX_EXPRESSION && isSplittable(leftOperand)) {
			leftOperand.accept(this);
		} else {
			this.fragments.add(leftOperand);
		}
		if (operatorCounter == this.operators.length) {
			// need to resize
			System.arraycopy(this.operators, 0, (this.operators = new int[this.operatorCounter * 2]), 0, this.operatorCounter);
		}
		final int extractInfixExpressionOperator = CodeFormatterVisitor2.extractInfixExpressionOperator(node);
		this.operators[this.operatorCounter++] = extractInfixExpressionOperator;
		if (rightOperand.getNodeType() == ASTNode.INFIX_EXPRESSION && isSplittable(leftOperand)) {
			rightOperand.accept(this);
		} else {
			this.fragments.add(rightOperand);
		}
		final List extendedOperands = node.extendedOperands();
		final int extendedOperandsLength = extendedOperands.size();
		if (extendedOperandsLength != 0) {
			for (int i = 0; i < extendedOperandsLength; i++) {
				if (operatorCounter == this.operators.length) {
					// need to resize
					System.arraycopy(this.operators, 0, (this.operators = new int[this.operatorCounter * 2]), 0, this.operatorCounter);
				}
				this.operators[this.operatorCounter++] = extractInfixExpressionOperator;
				final Expression extendedOperand = ((Expression) extendedOperands.get(i));
				if (extendedOperand instanceof InfixExpression) {
					extendedOperand.accept(this);
				} else {
					this.fragments.add(extendedOperand);
				}
			}
		}
		return false;
	}

}
