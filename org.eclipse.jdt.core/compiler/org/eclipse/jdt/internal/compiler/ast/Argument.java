package org.eclipse.jdt.internal.compiler.ast;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
import org.eclipse.jdt.internal.compiler.IAbstractSyntaxTreeVisitor;
import org.eclipse.jdt.internal.compiler.impl.*;
import org.eclipse.jdt.internal.compiler.lookup.*;

public class Argument extends LocalDeclaration {
public Argument(char[] name , long posNom , TypeReference tr , int modifiers){
	super(null,name, (int) (posNom >>> 32), (int) (posNom & 0xFFFFFFFFL));
	this.modifiers = modifiers;
	type = tr;
}
public void resolve(BlockScope scope) {
	// an argument may be final ==> cannot be assigned

	super.resolve(scope);
	binding.isArgument = true;
	binding.used = true;
}
public TypeBinding resolveForCatch(BlockScope scope) {
	// resolution on an argument of a catch clause
	// provide the scope with a side effect : insertion of a LOCAL
	// that represents the argument. The type must be from JavaThrowable

	TypeBinding tb = type.resolveTypeExpecting(scope, scope.getJavaLangThrowable());
	if (tb == null)
		return null;
	if ((binding = scope.duplicateName(name)) != null) {
		// the name already exists....may carry on with the first binding ....
		scope.problemReporter().redefineArgument(this);
		return null;
	}
	binding = new LocalVariableBinding(this, tb, modifiers);
	scope.addLocalVariable(binding);
	binding.constant = NotAConstant;
	return tb;
}
public String toString(int tab){
	/* slow code */
	
	String s = ""/*nonNLS*/;
	if (modifiers != AccDefault){
		s += modifiersString(modifiers);
	}
	if (type == null){
		s += "<no type> "/*nonNLS*/;
	} else {
		s += type.toString(tab) + " "/*nonNLS*/;
	}
	s += new String(name);
	return s;
}
public void traverse(IAbstractSyntaxTreeVisitor visitor, BlockScope scope) {
	if (visitor.visit(this, scope)) {
		if (type != null) type.traverse(visitor, scope);
		if (initialization != null) initialization.traverse(visitor, scope);
	}
	visitor.endVisit(this, scope);
}
}
