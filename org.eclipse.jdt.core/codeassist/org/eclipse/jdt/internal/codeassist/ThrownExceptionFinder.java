/*******************************************************************************
 * Copyright (c) 2007, 2011 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jdt.internal.codeassist;

import java.util.Stack;

import org.eclipse.jdt.internal.compiler.ASTVisitor;
import org.eclipse.jdt.internal.compiler.ast.AllocationExpression;
import org.eclipse.jdt.internal.compiler.ast.Argument;
import org.eclipse.jdt.internal.compiler.ast.Block;
import org.eclipse.jdt.internal.compiler.ast.MessageSend;
import org.eclipse.jdt.internal.compiler.ast.ThrowStatement;
import org.eclipse.jdt.internal.compiler.ast.TryStatement;
import org.eclipse.jdt.internal.compiler.ast.TypeDeclaration;
import org.eclipse.jdt.internal.compiler.ast.UnionTypeReference;
import org.eclipse.jdt.internal.compiler.lookup.BlockScope;
import org.eclipse.jdt.internal.compiler.lookup.ClassScope;
import org.eclipse.jdt.internal.compiler.lookup.CompilationUnitScope;
import org.eclipse.jdt.internal.compiler.lookup.MethodBinding;
import org.eclipse.jdt.internal.compiler.lookup.ReferenceBinding;
import org.eclipse.jdt.internal.compiler.lookup.TypeBinding;
import org.eclipse.jdt.internal.compiler.util.SimpleSet;

public class ThrownExceptionFinder extends ASTVisitor {

	private SimpleSet thrownExceptions;
	private Stack exceptionsStack;
	private SimpleSet caughtExceptions;

	/**
	 * Finds the thrown exceptions minus the ones that are already caught in previous catch blocks.
	 * Exception is already caught even if its super type is being caught. Also computes, separately,
	 * a list comprising of (a)those exceptions that have been caught already and (b)those exceptions that are thrown
	 * by the method and whose super type has been caught already. 
	 * @param tryStatement
	 * @param scope
	 */
	public void processThrownExceptions(TryStatement tryStatement, BlockScope scope) {
		this.thrownExceptions = new SimpleSet();
		this.exceptionsStack = new Stack();
		this.caughtExceptions = new SimpleSet();
		tryStatement.traverse(this, scope);
		removeCaughtExceptions(tryStatement, true /*remove unchecked exceptions this time*/);
	}

	private void acceptException(ReferenceBinding binding) {
		if (binding != null && binding.isValidBinding()) {
			this.thrownExceptions.add(binding);
		}
	}

	public void endVisit(MessageSend messageSend, BlockScope scope) {
		if (messageSend.binding != null) {
			endVisitMethodInvocation(messageSend.binding);
		}
		super.endVisit(messageSend, scope);
	}

	public void endVisit(AllocationExpression allocationExpression, BlockScope scope) {
		if (allocationExpression.binding != null) {
			endVisitMethodInvocation(allocationExpression.binding);
		}
		super.endVisit(allocationExpression, scope);
	}

	public void endVisit(ThrowStatement throwStatement, BlockScope scope) {
		acceptException((ReferenceBinding)throwStatement.exception.resolvedType);
		super.endVisit(throwStatement, scope);
	}


	private void endVisitMethodInvocation(MethodBinding methodBinding) {
		ReferenceBinding[] thrownExceptionBindings = methodBinding.thrownExceptions;
		int length = thrownExceptionBindings == null ? 0 : thrownExceptionBindings.length;
		for (int i = 0; i < length; i++) {
			acceptException(thrownExceptionBindings[i]);
		}
	}


	/**
	 * Returns all the already caught exceptions in catch blocks, found by the call to
	 * {@link ThrownExceptionFinder#processThrownExceptions(TryStatement, BlockScope)}
	 * @return Returns an array of (a)those exceptions that have been caught already and 
	 * (b)those exceptions that are thrown by the method and whose super type has been 
	 * caught already.
	 */
	public ReferenceBinding[] getAlreadyCaughtExceptions() {
		ReferenceBinding[] allCaughtExceptions = new ReferenceBinding[this.caughtExceptions.elementSize];
		this.caughtExceptions.asArray(allCaughtExceptions);
		return allCaughtExceptions;
	}
	
	/**
	 * Returns all the thrown exceptions minus the ones that are already caught in previous catch blocks,
	 * found by the call to {@link ThrownExceptionFinder#processThrownExceptions(TryStatement, BlockScope)}.
	 * Exception is already caught even if its super type is being caught
	 * @return Returns an array of thrown exceptions that are still not caught in any catch block.
	 */
	public ReferenceBinding[] getThrownUncaughtExceptions() {
		ReferenceBinding[] result = new ReferenceBinding[this.thrownExceptions.elementSize];
		this.thrownExceptions.asArray(result);
		return result;
	}
	public boolean visit(TypeDeclaration typeDeclaration, CompilationUnitScope scope) {
		return visitType(typeDeclaration);
	}

	public boolean visit(TypeDeclaration memberTypeDeclaration, ClassScope scope) {
		return visitType(memberTypeDeclaration);
	}

	public boolean visit(TypeDeclaration localTypeDeclaration, BlockScope scope) {
		return visitType(localTypeDeclaration);
	}

	private boolean visitType(TypeDeclaration typeDeclaration) {
		return false;
	}

	public boolean visit(TryStatement tryStatement, BlockScope scope) {
		this.exceptionsStack.push(this.thrownExceptions);
		SimpleSet exceptionSet = new SimpleSet();
		this.thrownExceptions = exceptionSet;
		tryStatement.tryBlock.traverse(this, scope);

		removeCaughtExceptions(tryStatement, false);

		this.thrownExceptions = (SimpleSet)this.exceptionsStack.pop();

		Object[] values = exceptionSet.values;
		for (int i = 0; i < values.length; i++) {
			if (values[i] != null) {
				this.thrownExceptions.add(values[i]);
			}
		}

		Block[] catchBlocks = tryStatement.catchBlocks;
		int length = catchBlocks == null ? 0 : catchBlocks.length;
		for (int i = 0; i < length; i++) {
			catchBlocks[i].traverse(this, scope);
		}
		return false;
	}
	
	private void removeCaughtExceptions(TryStatement tryStatement, boolean recordUncheckedCaughtExceptions) {
		Argument[] catchArguments = tryStatement.catchArguments;
		int length = catchArguments == null ? 0 : catchArguments.length;
		for (int i = 0; i < length; i++) {
			if (catchArguments[i].type instanceof UnionTypeReference) {
				UnionTypeReference unionTypeReference = (UnionTypeReference) catchArguments[i].type;
				TypeBinding caughtException;
				for (int j = 0; j < unionTypeReference.typeReferences.length; j++) {
					caughtException = unionTypeReference.typeReferences[j].resolvedType;
					if (caughtException != null && caughtException.isValidBinding()) {	// might be null when its the completion node
						if (!caughtException.isUncheckedException(true) || recordUncheckedCaughtExceptions) {
							removeCaughtException((ReferenceBinding)caughtException);
							this.caughtExceptions.add(caughtException);
						}
					}
				}
			} else {
				TypeBinding exception = catchArguments[i].type.resolvedType;
				if (exception != null && exception.isValidBinding()) {
					if (!exception.isUncheckedException(true) || recordUncheckedCaughtExceptions) {
						removeCaughtException((ReferenceBinding)exception);
						this.caughtExceptions.add(exception);
					}
				}
			}
		}
	}

	private void removeCaughtException(ReferenceBinding caughtException) {
		Object[] exceptions = this.thrownExceptions.values;
		for (int i = 0; i < exceptions.length; i++) {
			ReferenceBinding exception = (ReferenceBinding)exceptions[i];
			if (exception != null) {
				if (exception == caughtException) {
					this.thrownExceptions.remove(exception);
				} else if (caughtException.isSuperclassOf(exception)) {
					this.thrownExceptions.remove(exception);
					this.caughtExceptions.add(exception);
				}
			}
		}
	}
}
