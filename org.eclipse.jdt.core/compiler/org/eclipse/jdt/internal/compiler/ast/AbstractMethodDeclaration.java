/*******************************************************************************
 * Copyright (c) 2000, 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jdt.internal.compiler.ast;

import org.eclipse.jdt.core.compiler.*;
import org.eclipse.jdt.internal.compiler.*;
import org.eclipse.jdt.internal.compiler.flow.FlowInfo;
import org.eclipse.jdt.internal.compiler.flow.InitializationFlowContext;
import org.eclipse.jdt.internal.compiler.impl.*;
import org.eclipse.jdt.internal.compiler.codegen.*;
import org.eclipse.jdt.internal.compiler.lookup.*;
import org.eclipse.jdt.internal.compiler.problem.*;
import org.eclipse.jdt.internal.compiler.parser.*;

public abstract class AbstractMethodDeclaration
	extends ASTNode
	implements ProblemSeverities, ReferenceContext {
		
	public MethodScope scope;
	//it is not relevent for constructor but it helps to have the name of the constructor here 
	//which is always the name of the class.....parsing do extra work to fill it up while it do not have to....
	public char[] selector;
	public int declarationSourceStart;
	public int declarationSourceEnd;
	public int modifiers;
	public int modifiersSourceStart;
	public Annotation[] annotations;
	public Argument[] arguments;
	public TypeReference[] thrownExceptions;
	public Statement[] statements;
	public int explicitDeclarations;
	public MethodBinding binding;
	public boolean ignoreFurtherInvestigation = false;
	public boolean needFreeReturn = false;
	
	public Javadoc javadoc;
	
	public int bodyStart;
	public int bodyEnd = -1;
	public CompilationResult compilationResult;
	
	public boolean errorInSignature = false; 
	
	AbstractMethodDeclaration(CompilationResult compilationResult){
		this.compilationResult = compilationResult;
	}
	
	/*
	 *	We cause the compilation task to abort to a given extent.
	 */
	public void abort(int abortLevel, IProblem problem) {

		switch (abortLevel) {
			case AbortCompilation :
				throw new AbortCompilation(this.compilationResult, problem);
			case AbortCompilationUnit :
				throw new AbortCompilationUnit(this.compilationResult, problem);
			case AbortType :
				throw new AbortType(this.compilationResult, problem);
			default :
				throw new AbortMethod(this.compilationResult, problem);
		}
	}

	public abstract void analyseCode(ClassScope classScope, InitializationFlowContext initializationContext, FlowInfo info);

		/**
	 * Bind and add argument's binding into the scope of the method
	 */
	public void bindArguments() {

		if (this.arguments != null) {
			// by default arguments in abstract/native methods are considered to be used (no complaint is expected)
			boolean used = this.binding == null || this.binding.isAbstract() || this.binding.isNative();

			int length = this.arguments.length;
			for (int i = 0; i < length; i++) {
				TypeBinding argType = this.binding == null ? null : this.binding.parameters[i];
				this.arguments[i].bind(this.scope, argType, used);
			}
		}
	}

	/**
	 * Record the thrown exception type bindings in the corresponding type references.
	 */
	public void bindThrownExceptions() {

		if (this.thrownExceptions != null
			&& this.binding != null
			&& this.binding.thrownExceptions != null) {
			int thrownExceptionLength = this.thrownExceptions.length;
			int length = this.binding.thrownExceptions.length;
			if (length == thrownExceptionLength) {
				for (int i = 0; i < length; i++) {
					this.thrownExceptions[i].resolvedType = this.binding.thrownExceptions[i];
				}
			} else {
				int bindingIndex = 0;
				for (int i = 0; i < thrownExceptionLength && bindingIndex < length; i++) {
					TypeReference thrownException = this.thrownExceptions[i];
					ReferenceBinding thrownExceptionBinding = this.binding.thrownExceptions[bindingIndex];
					char[][] bindingCompoundName = thrownExceptionBinding.compoundName;
					if (thrownException instanceof SingleTypeReference) {
						// single type reference
						int lengthName = bindingCompoundName.length;
						char[] thrownExceptionTypeName = thrownException.getTypeName()[0];
						if (CharOperation.equals(thrownExceptionTypeName, bindingCompoundName[lengthName - 1])) {
							thrownException.resolvedType = thrownExceptionBinding;
							bindingIndex++;
						}
					} else {
						// qualified type reference
						if (CharOperation.equals(thrownException.getTypeName(), bindingCompoundName)) {
							thrownException.resolvedType = thrownExceptionBinding;
							bindingIndex++;
						}						
					}
				}
			}
		}
	}

	public CompilationResult compilationResult() {
		
		return this.compilationResult;
	}
	
	/**
	 * Bytecode generation for a method
	 * @param classScope
	 * @param classFile
	 */
	public void generateCode(ClassScope classScope, ClassFile classFile) {
		
		int problemResetPC = 0;
		classFile.codeStream.wideMode = false; // reset wideMode to false
		if (this.ignoreFurtherInvestigation) {
			// method is known to have errors, dump a problem method
			if (this.binding == null)
				return; // handle methods with invalid signature or duplicates
			int problemsLength;
			IProblem[] problems =
				this.scope.referenceCompilationUnit().compilationResult.getProblems();
			IProblem[] problemsCopy = new IProblem[problemsLength = problems.length];
			System.arraycopy(problems, 0, problemsCopy, 0, problemsLength);
			classFile.addProblemMethod(this, this.binding, problemsCopy);
			return;
		}
		// regular code generation
		try {
			problemResetPC = classFile.contentsOffset;
			this.generateCode(classFile);
		} catch (AbortMethod e) {
			// a fatal error was detected during code generation, need to restart code gen if possible
			if (e.compilationResult == CodeStream.RESTART_IN_WIDE_MODE) {
				// a branch target required a goto_w, restart code gen in wide mode.
				try {
					classFile.contentsOffset = problemResetPC;
					classFile.methodCount--;
					classFile.codeStream.wideMode = true; // request wide mode 
					this.generateCode(classFile); // restart method generation
				} catch (AbortMethod e2) {
					int problemsLength;
					IProblem[] problems =
						this.scope.referenceCompilationUnit().compilationResult.getAllProblems();
					IProblem[] problemsCopy = new IProblem[problemsLength = problems.length];
					System.arraycopy(problems, 0, problemsCopy, 0, problemsLength);
					classFile.addProblemMethod(this, this.binding, problemsCopy, problemResetPC);
				}
			} else {
				// produce a problem method accounting for this fatal error
				int problemsLength;
				IProblem[] problems =
					this.scope.referenceCompilationUnit().compilationResult.getAllProblems();
				IProblem[] problemsCopy = new IProblem[problemsLength = problems.length];
				System.arraycopy(problems, 0, problemsCopy, 0, problemsLength);
				classFile.addProblemMethod(this, this.binding, problemsCopy, problemResetPC);
			}
		}
	}

	private void generateCode(ClassFile classFile) {

		classFile.generateMethodInfoHeader(this.binding);
		int methodAttributeOffset = classFile.contentsOffset;
		int attributeNumber = classFile.generateMethodInfoAttribute(this.binding);
		if ((!this.binding.isNative()) && (!this.binding.isAbstract())) {
			int codeAttributeOffset = classFile.contentsOffset;
			classFile.generateCodeAttributeHeader();
			CodeStream codeStream = classFile.codeStream;
			codeStream.reset(this, classFile);
			// initialize local positions
			this.scope.computeLocalVariablePositions(this.binding.isStatic() ? 0 : 1, codeStream);

			// arguments initialization for local variable debug attributes
			if (this.arguments != null) {
				for (int i = 0, max = this.arguments.length; i < max; i++) {
					LocalVariableBinding argBinding;
					codeStream.addVisibleLocalVariable(argBinding = this.arguments[i].binding);
					argBinding.recordInitializationStartPC(0);
				}
			}
			if (this.statements != null) {
				for (int i = 0, max = this.statements.length; i < max; i++)
					this.statements[i].generateCode(this.scope, codeStream);
			}
			if (this.needFreeReturn) {
				codeStream.return_();
			}
			// local variable attributes
			codeStream.exitUserScope(this.scope);
			codeStream.recordPositionsFrom(0, this.declarationSourceEnd);
			classFile.completeCodeAttribute(codeAttributeOffset);
			attributeNumber++;
		} else {
			checkArgumentsSize();
		}
		classFile.completeMethodInfo(methodAttributeOffset, attributeNumber);

		// if a problem got reported during code gen, then trigger problem method creation
		if (this.ignoreFurtherInvestigation) {
			throw new AbortMethod(this.scope.referenceCompilationUnit().compilationResult, null);
		}
	}

	private void checkArgumentsSize() {
		TypeBinding[] parameters = this.binding.parameters;
		int size = 1; // an abstact method or a native method cannot be static
		for (int i = 0, max = parameters.length; i < max; i++) {
			TypeBinding parameter = parameters[i];
			if (parameter == LongBinding || parameter == DoubleBinding) {
				size += 2;
			} else {
				size++;
			}
			if (size > 0xFF) {
				this.scope.problemReporter().noMoreAvailableSpaceForArgument(this.scope.locals[i], this.scope.locals[i].declaration);
			}
		}
	}
	
	public boolean hasErrors() {
		return this.ignoreFurtherInvestigation;
	}

	public boolean isAbstract() {

		if (this.binding != null)
			return this.binding.isAbstract();
		return (this.modifiers & AccAbstract) != 0;
	}

	public boolean isClinit() {

		return false;
	}

	public boolean isConstructor() {

		return false;
	}

	public boolean isDefaultConstructor() {

		return false;
	}

	public boolean isInitializationMethod() {

		return false;
	}

	public boolean isMethod() {

		return false;
	}

	public boolean isNative() {

		if (this.binding != null)
			return this.binding.isNative();
		return (this.modifiers & AccNative) != 0;
	}

	public boolean isStatic() {

		if (this.binding != null)
			return this.binding.isStatic();
		return (this.modifiers & AccStatic) != 0;
	}

	/**
	 * Fill up the method body with statement
	 * @param parser
	 * @param unit
	 */
	public abstract void parseStatements(
		Parser parser,
		CompilationUnitDeclaration unit);

	public StringBuffer print(int tab, StringBuffer output) {

		printIndent(tab, output);
		printModifiers(this.modifiers, output);
		
		TypeParameter[] typeParams = typeParameters();
		if (typeParams != null) {
			output.append('<');//$NON-NLS-1$
			int max = typeParams.length - 1;
			for (int j = 0; j < max; j++) {
				typeParams[j].print(0, output);
				output.append(", ");//$NON-NLS-1$
			}
			typeParams[max].print(0, output);
			output.append('>');
		}
		
		printReturnType(0, output).append(this.selector).append('(');
		if (this.arguments != null) {
			for (int i = 0; i < this.arguments.length; i++) {
				if (i > 0) output.append(", "); //$NON-NLS-1$
				this.arguments[i].print(0, output);
			}
		}
		output.append(')');
		if (this.thrownExceptions != null) {
			output.append(" throws "); //$NON-NLS-1$
			for (int i = 0; i < this.thrownExceptions.length; i++) {
				if (i > 0) output.append(", "); //$NON-NLS-1$
				this.thrownExceptions[i].print(0, output);
			}
		}
		printBody(tab + 1, output);
		return output;
	}

	public StringBuffer printBody(int indent, StringBuffer output) {

		if (isAbstract() || (this.modifiers & AccSemicolonBody) != 0) 
			return output.append(';');

		output.append(" {"); //$NON-NLS-1$
		if (this.statements != null) {
			for (int i = 0; i < this.statements.length; i++) {
				output.append('\n');
				this.statements[i].printStatement(indent, output); 
			}
		}
		output.append('\n'); //$NON-NLS-1$
		printIndent(indent == 0 ? 0 : indent - 1, output).append('}');
		return output;
	}

	public StringBuffer printReturnType(int indent, StringBuffer output) {
		
		return output;
	}

	public void resolve(ClassScope upperScope) {

		if (this.binding == null) {
			this.ignoreFurtherInvestigation = true;
		}

		try {
			bindArguments(); 
			bindThrownExceptions();
			resolveJavadoc();
			resolveStatements();
		} catch (AbortMethod e) {	// ========= abort on fatal error =============
			this.ignoreFurtherInvestigation = true;
		} 
	}

	public void resolveJavadoc() {
		
		if (this.binding == null) return;
		if (this.javadoc != null) {
			this.javadoc.resolve(this.scope);
			return;
		}
		if (this.binding.declaringClass != null && !this.binding.declaringClass.isLocalType()) {
			this.scope.problemReporter().javadocMissing(this.sourceStart, this.sourceEnd, this.binding.modifiers);
		}
	}

	public void resolveStatements() {

		if (this.statements != null) {
			for (int i = 0, length = this.statements.length; i < length; i++) {
				this.statements[i].resolve(this.scope);
			}
		} else if ((this.bits & UndocumentedEmptyBlockMASK) != 0) {
			this.scope.problemReporter().undocumentedEmptyBlock(this.bodyStart-1, this.bodyEnd+1);
		}
	}

	public void tagAsHavingErrors() {

		this.ignoreFurtherInvestigation = true;
	}

	public void traverse(
		ASTVisitor visitor,
		ClassScope classScope) {
		// default implementation: subclass will define it
	}
	
	public TypeParameter[] typeParameters() {
	    return null;
	}
}
