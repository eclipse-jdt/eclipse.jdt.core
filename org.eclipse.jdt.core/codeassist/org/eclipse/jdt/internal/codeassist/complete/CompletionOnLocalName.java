package org.eclipse.jdt.internal.codeassist.complete;

import org.eclipse.jdt.internal.compiler.ast.Expression;
import org.eclipse.jdt.internal.compiler.ast.LocalDeclaration;
import org.eclipse.jdt.internal.compiler.lookup.BlockScope;


public class CompletionOnLocalName extends LocalDeclaration {
	public CompletionOnLocalName(Expression expr,char[] name, int sourceStart, int sourceEnd){
		super(expr, name, sourceStart, sourceEnd);
	}
	
	public void resolve(BlockScope scope) {
		super.resolve(scope);
		
		throw new CompletionNodeFound(this, scope);
	}
	public String toString(int tab) {
		String s = tabString(tab);
		s += "<CompleteOnLocalName:"; //$NON-NLS-1$
		if (type != null) s += type.toString() + " "; //$NON-NLS-1$
		s += new String(name);
		if (initialization != null) s += " = " + initialization.toStringExpression(); //$NON-NLS-1$
		s += ">"; //$NON-NLS-1$
		return s;
	}	
}

