package org.eclipse.jdt.internal.compiler.ast;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
import org.eclipse.jdt.internal.compiler.*;
import org.eclipse.jdt.internal.compiler.impl.*;
import org.eclipse.jdt.internal.compiler.codegen.*;
import org.eclipse.jdt.internal.compiler.flow.*;
import org.eclipse.jdt.internal.compiler.lookup.*;
import org.eclipse.jdt.internal.compiler.problem.*;
import org.eclipse.jdt.internal.compiler.parser.*;

public abstract class AbstractMethodDeclaration
	extends AstNode
	implements ProblemSeverities, ReferenceContext {
	public MethodScope scope;
	//it is not relevent for constructor but it helps to have the name of the constructor here 
	//which is always the name of the class.....parsing do extra work to fill it up while it do not have to....
	public char[] selector;
	public int declarationSourceStart;
	public int declarationSourceEnd;
	public int modifiers;
	public int modifiersSourceStart;
	public Argument[] arguments;
	public TypeReference[] thrownExceptions;
	public Statement[] statements;
	public int explicitDeclarations;
	public MethodBinding binding;
	public boolean ignoreFurtherInvestigation = false;
	public boolean needFreeReturn = false;
	public LocalVariableBinding secretReturnValue;
	static final char[] SecretLocalDeclarationName = " returnValue".toCharArray();

	public int bodyStart;
	public int bodyEnd = -1;
	/**
	 * AbstractMethodDeclaration constructor comment.
	 */
	public AbstractMethodDeclaration() {
		super();
	}

	/*
	 *	We cause the compilation task to abort to a given extent.
	 */
	public void abort(int abortLevel) {

		if (scope == null) {
			throw new AbortCompilation(); // cannot do better
		}

		CompilationResult compilationResult =
			scope.referenceCompilationUnit().compilationResult;

		switch (abortLevel) {
			case AbortCompilation :
				throw new AbortCompilation(compilationResult);
			case AbortCompilationUnit :
				throw new AbortCompilationUnit(compilationResult);
			case AbortType :
				throw new AbortType(compilationResult);
			default :
				throw new AbortMethod(compilationResult);
		}
	}

	public void analyseCode(
		ClassScope currentScope,
		FlowContext flowContext,
		FlowInfo flowInfo) {

		// starting of the code analysis for methods
		if (ignoreFurtherInvestigation)
			return;
		try {
			if (binding == null)
				return;
			// may be in a non necessary <clinit> for innerclass with static final constant fields
			if (binding.isAbstract() || binding.isNative())
				return;

			ExceptionHandlingFlowContext methodContext =
				new ExceptionHandlingFlowContext(
					flowContext,
					this,
					binding.thrownExceptions,
					scope,
					FlowInfo.DeadEnd);

			// propagate to statements
			if (statements != null) {
				for (int i = 0, count = statements.length; i < count; i++) {
					Statement stat;
					if (!flowInfo.complainIfUnreachable((stat = statements[i]), scope)) {
						flowInfo = stat.analyseCode(scope, methodContext, flowInfo);
					}
				}
			}
			// check for missing returning path
			TypeBinding returnType = binding.returnType;
			if ((returnType == VoidBinding) || isAbstract()) {
				needFreeReturn =
					!((flowInfo == FlowInfo.DeadEnd) || flowInfo.isFakeReachable());
			} else {
				if (flowInfo != FlowInfo.DeadEnd) {
					// special test for empty methods that should return something
					if ((statements == null) && (returnType != VoidBinding)) {
						scope.problemReporter().shouldReturn(returnType, this);
					} else {
						scope.problemReporter().shouldReturn(
							returnType,
							statements[statements.length - 1]);
					}
				}
			}
		} catch (AbortMethod e) {
			this.ignoreFurtherInvestigation = true;
		}
	}

	public void bindArguments() {
		//bind and add argument's binding into the scope of the method

		if (arguments != null) {
			int length = arguments.length;
			for (int i = 0; i < length; i++) {
				Argument argument = arguments[i];
				if (argument.type != null)
					argument.type.binding = binding.parameters[i];
				// record the resolved type into the type reference
				int modifierFlag = argument.modifiers;
				if ((argument.binding = scope.duplicateName(argument.name)) != null) {
					//the name already exist....may carry on with the first binding ....
					scope.problemReporter().redefineArgument(argument);
				} else {
					scope.addLocalVariable(
						argument.binding =
							new LocalVariableBinding(
								argument.name,
								binding.parameters[i],
								modifierFlag,
								true));
					//true stand for argument instead of just local
					if (isTypeUseDeprecated(binding.parameters[i], scope))
						scope.problemReporter().deprecatedType(binding.parameters[i], argument.type);
					argument.binding.declaration = argument;
					argument.binding.used = binding.isAbstract() | binding.isNative();
					// by default arguments in abstract/native methods are considered to be used (no complaint is expected)
				}
			}
		}
	}

	public void checkName() {
		//look if the name of the method is correct
	}

	public CompilationResult compilationResult() {
		return scope.referenceCompilationUnit().compilationResult;
	}

	/**
	 * Bytecode generation for a method
	 */
	public void generateCode(ClassScope classScope, ClassFile classFile) {
		int problemResetPC = 0;
		if (ignoreFurtherInvestigation) {
			if (this.binding == null)
				return; // Handle methods with invalid signature or duplicates
			int problemsLength;
			IProblem[] problems =
				scope.referenceCompilationUnit().compilationResult.getProblems();
			IProblem[] problemsCopy = new IProblem[problemsLength = problems.length];
			System.arraycopy(problems, 0, problemsCopy, 0, problemsLength);
			classFile.addProblemMethod(this, binding, problemsCopy);
			return;
		}
		try {
			problemResetPC = classFile.contentsOffset;
			classFile.generateMethodInfoHeader(binding);
			int methodAttributeOffset = classFile.contentsOffset;
			int attributeNumber = classFile.generateMethodInfoAttribute(binding);
			if ((!binding.isNative()) && (!binding.isAbstract())) {
				int codeAttributeOffset = classFile.contentsOffset;
				classFile.generateCodeAttributeHeader();
				CodeStream codeStream = classFile.codeStream;
				codeStream.reset(this, classFile);
				// initialize local positions
				scope.computeLocalVariablePositions(binding.isStatic() ? 0 : 1, codeStream);

				// arguments initialization for local variable debug attributes
				if (arguments != null) {
					for (int i = 0, max = arguments.length; i < max; i++) {
						LocalVariableBinding argBinding;
						codeStream.addVisibleLocalVariable(argBinding = arguments[i].binding);
						argBinding.recordInitializationStartPC(0);
					}
				}
				if (statements != null) {
					for (int i = 0, max = statements.length; i < max; i++)
						statements[i].generateCode(scope, codeStream);
				}
				if (needFreeReturn) {
					codeStream.return_();
				}
				// local variable attributes
				codeStream.exitUserScope(scope);
				codeStream.recordPositionsFrom(0, this);
				classFile.completeCodeAttribute(codeAttributeOffset);
				attributeNumber++;
			}
			classFile.completeMethodInfo(methodAttributeOffset, attributeNumber);

			// if a problem got reported during code gen, then trigger problem method creation
			if (ignoreFurtherInvestigation) {
				throw new AbortMethod(scope.referenceCompilationUnit().compilationResult);
			}
		} catch (AbortMethod e) {
			int problemsLength;
			IProblem[] problems =
				scope.referenceCompilationUnit().compilationResult.getProblems();
			IProblem[] problemsCopy = new IProblem[problemsLength = problems.length];
			System.arraycopy(problems, 0, problemsCopy, 0, problemsLength);
			classFile.addProblemMethod(this, binding, problemsCopy, problemResetPC);
		}
	}

	public boolean isAbstract() {

		if (binding != null)
			return binding.isAbstract();
		return (modifiers & AccAbstract) != 0;
	}

	public boolean isClinit() {
		return false;
	}

	/**
	 * @return boolean
	 */
	public boolean isConstructor() {
		return false;
	}

	public boolean isDefaultConstructor() {
		return false;
	}

	public boolean isInitializationMethod() {
		return false;
	}

	public boolean isNative() {

		if (binding != null)
			return binding.isNative();
		return (modifiers & AccNative) != 0;
	}

	public boolean isStatic() {
		if (binding != null)
			return binding.isStatic();
		return (modifiers & AccStatic) != 0;
	}

	public abstract void parseStatements(
		Parser parser,
		CompilationUnitDeclaration unit);
	//fill up the method body with statement
	public void resolve(ClassScope upperScope) {
		if (binding == null) {
			ignoreFurtherInvestigation = true;
			return;
		}

		// ========= abort on fatal error =============
		try {
			bindArguments(); //<-- shoud be done at binding/scope creation time
			checkName();

			// create secret value location
			scope.addLocalVariable(
				secretReturnValue =
					new LocalVariableBinding(
						SecretLocalDeclarationName,
						binding.returnType,
						AccDefault));
			secretReturnValue.constant = NotAConstant; // not inlinable

			// and then ....deep jump into statements.....
			if (statements != null) {
				int i = 0, length = statements.length;
				while (i < length)
					statements[i++].resolve(scope);
			}
		} catch (AbortMethod e) {
			this.ignoreFurtherInvestigation = true;
		}
	}

	public String returnTypeToString(int tab) {
		/*slow code */

		return "";
	}

	public void tagAsHavingErrors() {
		ignoreFurtherInvestigation = true;
	}

	public String toString(int tab) {
		/* slow code */

		String s = tabString(tab);
		if (modifiers != AccDefault) {
			s += modifiersString(modifiers);
		}

		s += returnTypeToString(0);

		s += new String(selector) + "(";
		if (arguments != null) {
			for (int i = 0; i < arguments.length; i++) {
				s += arguments[i].toString(0);
				if (i != (arguments.length - 1))
					s = s + ", ";
			};
		};
		s += ")";
		if (thrownExceptions != null) {
			s += " throws ";
			for (int i = 0; i < thrownExceptions.length; i++) {
				s += thrownExceptions[i].toString(0);
				if (i != (thrownExceptions.length - 1))
					s = s + ", ";
			};
		};

		s += toStringStatements(tab + 1);

		return s;
	}

	public String toStringStatements(int tab) {
		/* slow code */

		if (isAbstract() || (this.modifiers & AccSemicolonBody) != 0)
			return ";";

		String s = " {";
		if (statements != null) {
			for (int i = 0; i < statements.length; i++) {
				s = s + "\n" + statements[i].toString(tab);
				if (!(statements[i] instanceof Block)) {
					s += ";";
				}
			}
		}
		s += "\n" + tabString(tab == 0 ? 0 : tab - 1) + "}";
		return s;
	}

	public void traverse(
		IAbstractSyntaxTreeVisitor visitor,
		ClassScope classScope) {
	}

}
