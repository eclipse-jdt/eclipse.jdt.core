package org.eclipse.jdt.internal.compiler.ast;

public class SwitchExprArm extends ASTNode {

	public enum EXPR_KIND {
		BLOCK,
		EXPR,
		BREAK,
		THROW,
		DEFAULT_EXPR,
		DEFAULT_THROW
	}
	EXPR_KIND kind;
	public CaseStatement lhs;
	public Statement rhs; 

	public SwitchExprArm(EXPR_KIND kind, CaseStatement lhs, Statement rhs, int start, int end) {
		this.kind = kind; 
		this.lhs = lhs;
		this.rhs = rhs;
		this.sourceStart = start;
		this.sourceEnd = end;
	}

	@Override
	public StringBuffer print(int indent, StringBuffer output) {
		if (this.lhs != null) {
			output.append('\n');
			this.lhs.printStatement(indent+2, output);
			this.rhs.printStatement(indent+2, output);
		}
		return output;
	}
}