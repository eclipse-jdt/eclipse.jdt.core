package org.eclipse.jdt.internal.compiler.ast;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
import org.eclipse.jdt.internal.compiler.IAbstractSyntaxTreeVisitor;
import org.eclipse.jdt.internal.compiler.impl.*;
import org.eclipse.jdt.internal.compiler.lookup.*;

public class Argument extends LocalDeclaration {
	
	public Argument(char[] name, long posNom, TypeReference tr, int modifiers) {

		super(null, name, (int) (posNom >>> 32), (int) posNom);
		this.modifiers = modifiers;
		type = tr;
		this.bits |= IsLocalDeclarationReachableMASK;
	}

	public void bind(MethodScope scope, TypeBinding typeBinding, boolean used) {

		if (this.type != null)
			this.type.binding = typeBinding;
		// record the resolved type into the type reference
		int modifierFlag = this.modifiers;
		if ((this.binding = scope.duplicateName(this.name)) != null) {
			//the name already exist....may carry on with the first binding ....
			scope.problemReporter().redefineArgument(this);
		} else {
			scope.addLocalVariable(
				this.binding =
					new LocalVariableBinding(this, typeBinding, modifierFlag, true));
			//true stand for argument instead of just local
			if (typeBinding != null && isTypeUseDeprecated(typeBinding, scope))
				scope.problemReporter().deprecatedType(typeBinding, this.type);
			this.binding.declaration = this;
			this.binding.used = used;
		}
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
		binding = new LocalVariableBinding(this, tb, modifiers, true);
		scope.addLocalVariable(binding);
		binding.constant = NotAConstant;
		return tb;
	}

	public String toString(int tab) {

		String s = ""; //$NON-NLS-1$
		if (modifiers != AccDefault) {
			s += modifiersString(modifiers);
		}
		if (type == null) {
			s += "<no type> "; //$NON-NLS-1$
		} else {
			s += type.toString(tab) + " "; //$NON-NLS-1$
		}
		s += new String(name);
		return s;
	}

	public void traverse(IAbstractSyntaxTreeVisitor visitor, BlockScope scope) {
		
		if (visitor.visit(this, scope)) {
			if (type != null)
				type.traverse(visitor, scope);
			if (initialization != null)
				initialization.traverse(visitor, scope);
		}
		visitor.endVisit(this, scope);
	}
}