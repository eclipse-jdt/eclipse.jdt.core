package org.eclipse.jdt.internal.compiler.ast;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
import org.eclipse.jdt.internal.compiler.IAbstractSyntaxTreeVisitor;
import org.eclipse.jdt.internal.compiler.impl.*;
import org.eclipse.jdt.internal.compiler.lookup.*;
import org.eclipse.jdt.internal.compiler.parser.*;
import org.eclipse.jdt.internal.compiler.problem.*;
import org.eclipse.jdt.internal.compiler.util.*;

public class MethodDeclaration extends AbstractMethodDeclaration {
	public TypeReference returnType;
	/**
	 * MethodDeclaration constructor comment.
	 */
	public MethodDeclaration() {
		super();
	}

	public void checkName() {
		// look if the name of the method is correct
		if (isTypeUseDeprecated(binding.returnType, scope))
			scope.problemReporter().deprecatedType(binding.returnType, returnType);

		if (CharOperation.equals(scope.enclosingSourceType().sourceName, selector))
			scope.problemReporter().methodWithConstructorName(this);

		// by grammatical construction, interface methods are always abstract
		if (scope.enclosingSourceType().isInterface())
			return;

		// if a method has an semicolon body and is not declared as abstract==>error
		// native methods may have a semicolon body 
		if ((modifiers & AccSemicolonBody) != 0) {
			if ((modifiers & AccNative) == 0)
				if ((modifiers & AccAbstract) == 0)
					scope.problemReporter().methodNeedingAbstractModifier(this);
		} else {
			// the method HAS a body --> abstract native modifiers are forbiden
			if (((modifiers & AccNative) != 0) || ((modifiers & AccAbstract) != 0))
				scope.problemReporter().methodNeedingNoBody(this);
		}
	}

	public void parseStatements(Parser parser, CompilationUnitDeclaration unit) {
		//fill up the method body with statement

		if (ignoreFurtherInvestigation)
			return;
		parser.parse(this, unit);

	}

	public void resolve(ClassScope upperScope) {
		if (binding == null) {
			ignoreFurtherInvestigation = true;
			return;
		}
		// ========= abort on fatal error =============
		try {
			if (this.returnType != null) {
				this.returnType.binding = this.binding.returnType;
				// record the return type binding
			}
		} catch (AbortMethod e) {
			this.ignoreFurtherInvestigation = true;
		}
		super.resolve(upperScope);

	}

	public String returnTypeToString(int tab) {
		/*slow code */

		if (returnType == null)
			return "";
		return returnType.toString(tab) + " ";
	}

	public void traverse(
		IAbstractSyntaxTreeVisitor visitor,
		ClassScope classScope) {
		if (visitor.visit(this, classScope)) {
			if (returnType != null)
				returnType.traverse(visitor, scope);
			if (arguments != null) {
				int argumentLength = arguments.length;
				for (int i = 0; i < argumentLength; i++)
					arguments[i].traverse(visitor, scope);
			}
			if (thrownExceptions != null) {
				int thrownExceptionsLength = thrownExceptions.length;
				for (int i = 0; i < thrownExceptionsLength; i++)
					thrownExceptions[i].traverse(visitor, scope);
			}
			if (statements != null) {
				int statementsLength = statements.length;
				for (int i = 0; i < statementsLength; i++)
					statements[i].traverse(visitor, scope);
			}
		}
		visitor.endVisit(this, classScope);
	}

}
