package org.eclipse.jdt.internal.compiler.ast;

import org.eclipse.jdt.internal.compiler.ASTVisitor;
import org.eclipse.jdt.internal.compiler.impl.Constant;
import org.eclipse.jdt.internal.compiler.lookup.BlockScope;
import org.eclipse.jdt.internal.compiler.lookup.TypeBinding;

public class SwitchExprArm extends Expression implements IPolyExpression {

	public enum EXPR_KIND {
		BLOCK,
		EXPR,
		BREAK,
		THROW,
		DEFAULT_EXPR,
		DEFAULT_THROW
	}
	private EXPR_KIND kind;
	private CaseStatement lhs;
	private Statement rhs; 
	/* package */ TypeBinding expectedType;

	public SwitchExprArm(EXPR_KIND kind, CaseStatement lhs, Statement rhs, int start, int end) {
		this.setKind(kind); 
		this.setLhs(lhs);
		this.setRhs(rhs);
		this.sourceStart = start;
		this.sourceEnd = end;
	}

	public CaseStatement getLhs() {
		return this.lhs;
	}

	public void setLhs(CaseStatement lhs) {
		this.lhs = lhs;
	}

	public Statement getRhs() {
		return this.rhs;
	}

	public void setRhs(Statement rhs) {
		this.rhs = rhs;
	}

	public EXPR_KIND getKind() {
		return this.kind;
	}

	public void setKind(EXPR_KIND kind) {
		this.kind = kind;
	}

	@Override
	public TypeBinding resolveType(BlockScope scope) {
		// TODO
		return null;
	}
	public Constant resolveCase(BlockScope scope, TypeBinding switchExpressionExpressionType, 
			SwitchExpression switchExpression) {
		return this.lhs.resolveCase(scope, switchExpressionExpressionType, switchExpression);
	}
	@Override
	public StringBuffer print(int indent, StringBuffer output) {
		if (this.getLhs() != null) {
			output.append('\n');
			this.getLhs().printStatement(indent+2, output);
			this.getRhs().printStatement(indent+2, output);
		}
		return output;
	}
	@Override
	public void traverse(ASTVisitor visitor, BlockScope blockScope) {
		if (visitor.visit(this, blockScope)) {
			this.lhs.traverse(visitor, blockScope);
			this.rhs.traverse(visitor, blockScope);
		}
		visitor.endVisit(this, blockScope);
	}

	@Override
	public StringBuffer printExpression(int indent, StringBuffer output) {
		return print(indent, output);
	}
}