package org.eclipse.jdt.internal.codeassist.complete;

import org.eclipse.jdt.internal.compiler.ast.Expression;
import org.eclipse.jdt.internal.compiler.ast.FieldDeclaration;
import org.eclipse.jdt.internal.compiler.lookup.MethodScope;
import org.eclipse.jdt.internal.compiler.util.CharOperation;

public class CompletionOnFieldName extends FieldDeclaration {
	private static final char[] FAKENAMESUFFIX = " ".toCharArray();
	public char[] realName;
	public CompletionOnFieldName(Expression initialization, char[] name, int sourceStart, int sourceEnd) {
		super(initialization, CharOperation.concat(name, FAKENAMESUFFIX), sourceStart, sourceEnd); //$NON-NLS-1$
		this.realName = name;
	}
	
	public void resolve(MethodScope initializationScope) {
		super.resolve(initializationScope);
		
		throw new CompletionNodeFound(this, initializationScope);
	}
	
	public String toString(int tab) {
		String s = tabString(tab);
		s += "<CompleteOnFieldName:"; //$NON-NLS-1$
		if (type != null) s += type.toString() + " "; //$NON-NLS-1$
		s += new String(realName);
		if (initialization != null) s += " = " + initialization.toStringExpression(); //$NON-NLS-1$
		s += ">"; //$NON-NLS-1$
		return s;
	}	
}

