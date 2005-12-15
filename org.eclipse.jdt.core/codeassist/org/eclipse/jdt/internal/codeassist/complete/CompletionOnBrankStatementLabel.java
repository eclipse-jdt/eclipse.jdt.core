package org.eclipse.jdt.internal.codeassist.complete;

import org.eclipse.jdt.internal.compiler.ast.BranchStatement;
import org.eclipse.jdt.internal.compiler.flow.FlowContext;
import org.eclipse.jdt.internal.compiler.flow.FlowInfo;
import org.eclipse.jdt.internal.compiler.lookup.BlockScope;

public class CompletionOnBrankStatementLabel extends BranchStatement {
	public static final int BREAK = 1;
	public static final int CONTINUE = 2;
	
	private int kind;
	public char[][] possibleLabels;
	
	public CompletionOnBrankStatementLabel(int kind, char[] l, int s, int e, char[][] possibleLabels) {
		super(l, s, e);
		this.kind = kind;
		this.possibleLabels = possibleLabels;
	}

	public FlowInfo analyseCode(BlockScope currentScope,
			FlowContext flowContext, FlowInfo flowInfo) {
		// Is never called
		return null;
	}

	public void resolve(BlockScope scope) {
		throw new CompletionNodeFound(this, scope);
	}
	public StringBuffer printStatement(int indent, StringBuffer output) {
		printIndent(indent, output);
		if(kind == CONTINUE) {
			output.append("continue "); //$NON-NLS-1$
		} else {
			output.append("break "); //$NON-NLS-1$
		}
		output.append("<CompleteOnLabel:"); //$NON-NLS-1$
		output.append(label);
		return output.append(">;"); //$NON-NLS-1$
	}

}
