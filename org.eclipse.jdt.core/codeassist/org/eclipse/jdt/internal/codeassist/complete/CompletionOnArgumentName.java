package org.eclipse.jdt.internal.codeassist.complete;

import org.eclipse.jdt.internal.compiler.ast.Argument;
import org.eclipse.jdt.internal.compiler.ast.TypeReference;
import org.eclipse.jdt.internal.compiler.lookup.BlockScope;
import org.eclipse.jdt.internal.compiler.lookup.MethodScope;
import org.eclipse.jdt.internal.compiler.lookup.TypeBinding;


public class CompletionOnArgumentName extends Argument {
	public CompletionOnArgumentName(char[] name , long posNom , TypeReference tr , int modifiers){
		super(name, posNom, tr, modifiers);
	}
	
	public void resolve(BlockScope scope) {
		super.resolve(scope);
		throw new CompletionNodeFound(this, scope);
	}
	
	public void bind(MethodScope scope, TypeBinding typeBinding, boolean used) {
		super.bind(scope, typeBinding, used);
		
		throw new CompletionNodeFound(this, scope);
	}
	
	public String toString(int tab) {
		String s = tabString(tab);
		s += "<CompleteOnArgumentName:"; //$NON-NLS-1$
		if (type != null) s += type.toString() + " "; //$NON-NLS-1$
		s += new String(name);
		if (initialization != null) s += " = " + initialization.toStringExpression();
		s += ">"; //$NON-NLS-1$
		return s;
	}	
}

