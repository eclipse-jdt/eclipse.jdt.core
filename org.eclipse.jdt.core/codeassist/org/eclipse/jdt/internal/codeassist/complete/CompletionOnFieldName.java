package org.eclipse.jdt.internal.codeassist.complete;

import org.eclipse.jdt.internal.compiler.ast.Expression;
import org.eclipse.jdt.internal.compiler.ast.FieldDeclaration;
import org.eclipse.jdt.internal.compiler.lookup.MethodScope;

public class CompletionOnFieldName extends FieldDeclaration {
	public CompletionOnFieldName(Expression initialization, char[] name, int sourceStart, int sourceEnd) {
		super(initialization, name, sourceStart, sourceEnd);
	}
	
	public void resolve(MethodScope initializationScope) {
		super.resolve(initializationScope);
		
		throw new CompletionNodeFound(this, initializationScope);
	}
	
	public String toString(int tab) {
		String s = tabString(tab);
		s += "<CompleteOnFieldName:"; //$NON-NLS-1$
		if (type != null) s += type.toString() + " "; //$NON-NLS-1$
		s += new String(name);
		if (initialization != null) s += " = " + initialization.toStringExpression();
		s += ">"; //$NON-NLS-1$
		return s;
	}	
}

