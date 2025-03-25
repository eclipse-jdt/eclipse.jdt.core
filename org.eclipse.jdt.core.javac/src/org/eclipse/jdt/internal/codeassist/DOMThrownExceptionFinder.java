/*******************************************************************************
 * Copyright (c) 2025 Red Hat, Inc. and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *******************************************************************************/
package org.eclipse.jdt.internal.codeassist;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.Stack;

import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.AnnotationTypeDeclaration;
import org.eclipse.jdt.core.dom.AnonymousClassDeclaration;
import org.eclipse.jdt.core.dom.CatchClause;
import org.eclipse.jdt.core.dom.ClassInstanceCreation;
import org.eclipse.jdt.core.dom.EnumDeclaration;
import org.eclipse.jdt.core.dom.IMethodBinding;
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.core.dom.MethodInvocation;
import org.eclipse.jdt.core.dom.RecordDeclaration;
import org.eclipse.jdt.core.dom.ThrowStatement;
import org.eclipse.jdt.core.dom.TryStatement;
import org.eclipse.jdt.core.dom.Type;
import org.eclipse.jdt.core.dom.TypeDeclaration;
import org.eclipse.jdt.core.dom.UnionType;

/**
 * Rewrite of {@link org.eclipse.jdt.internal.codeassist.ThrownExceptionFinder}
 * to work with API AST and API bindings.
 */
public class DOMThrownExceptionFinder extends ASTVisitor {

	private Set<ITypeBinding> thrownExceptions;
	private Stack<Set<ITypeBinding>> exceptionsStack;
	private Set<ITypeBinding> caughtExceptions;
	private Set<ITypeBinding> discouragedExceptions;

	/**
	 * Finds the thrown exceptions minus the ones that are already caught in
	 * previous catch blocks. Exception is already caught even if its super type is
	 * being caught. Also computes, separately, a list comprising of (a)those
	 * exceptions that have been caught already and (b)those exceptions that are
	 * thrown by the method and whose super type has been caught already.
	 */
	public void processThrownExceptions(TryStatement tryStatement) {
		this.thrownExceptions = new LinkedHashSet<>();
		this.exceptionsStack = new Stack<>();
		this.caughtExceptions = new LinkedHashSet<>();
		this.discouragedExceptions = new LinkedHashSet<>();
		tryStatement.accept(this);
		removeCaughtExceptions(tryStatement, true /* remove unchecked exceptions this time */);
	}

	private void acceptException(ITypeBinding binding) {
		if (binding != null && !binding.isRecovered()) {
			this.thrownExceptions.add(binding);
		}
	}

	@Override
	public void endVisit(MethodInvocation node) {
		endVisitMethodInvocation(node.resolveMethodBinding());
		super.visit(node);
	}

	@Override
	public void endVisit(ClassInstanceCreation node) {
		endVisitMethodInvocation(node.resolveConstructorBinding());
		super.visit(node);
	}

	@Override
	public void endVisit(ThrowStatement node) {
		acceptException(node.getExpression().resolveTypeBinding());
		super.visit(node);
	}

	private void endVisitMethodInvocation(IMethodBinding methodBinding) {
		if (methodBinding == null) {
			return;
		}
		for (ITypeBinding thrownException : methodBinding.getExceptionTypes()) {
			acceptException(thrownException);
		}
	}

	public ITypeBinding[] getAlreadyCaughtExceptions() {
		return this.caughtExceptions.toArray(ITypeBinding[]::new);
	}

	public ITypeBinding[] getThrownUncaughtExceptions() {
		return this.thrownExceptions.toArray(ITypeBinding[]::new);
	}

	/**
	 * Returns all exceptions that are discouraged to use because (a) they are
	 * already caught in some inner try-catch, or (b) their super exception has
	 * already been caught.
	 * 
	 * @return all discouraged exceptions
	 */
	public ITypeBinding[] getDiscouragedExceptions() {
		return this.discouragedExceptions.toArray(ITypeBinding[]::new);
	}

	@Override
	public boolean visit(TypeDeclaration typeDeclaration) {
		return false;
	}

	@Override
	public boolean visit(EnumDeclaration enumDeclaration) {
		return false;
	}

	@Override
	public boolean visit(RecordDeclaration recordDeclaration) {
		return false;
	}

	@Override
	public boolean visit(AnnotationTypeDeclaration annotationTypeDeclaration) {
		return false;
	}

	@Override
	public boolean visit(AnonymousClassDeclaration anonymousClassDeclaration) {
		return false;
	}

	@Override
	public boolean visit(TryStatement tryStatement) {
		this.exceptionsStack.push(this.thrownExceptions);
		Set<ITypeBinding> exceptionSet = new LinkedHashSet<>();
		this.thrownExceptions = exceptionSet;
		tryStatement.getBody().accept(this);

		removeCaughtExceptions(tryStatement, false);

		this.thrownExceptions = this.exceptionsStack.pop();

		for (ITypeBinding value : exceptionSet) {
			this.thrownExceptions.add(value);
		}

		for (CatchClause catchClause : (List<CatchClause>) tryStatement.catchClauses()) {
			catchClause.getBody().accept(this);
		}
		return false;
	}

	private void removeCaughtExceptions(TryStatement tryStatement, boolean recordUncheckedCaughtExceptions) {
		for (CatchClause catchClause : (List<CatchClause>) tryStatement.catchClauses()) {
			Type exceptionType = catchClause.getException().getType();
			if (exceptionType instanceof UnionType unionType) {
				for (Type unionMemberType : (List<Type>) unionType.types()) {
					ITypeBinding unionMemberTypeBinding = unionMemberType.resolveBinding();
					// if it's recovered, it may be the node being completed
					if (!unionMemberTypeBinding.isRecovered()) {
						if (recordUncheckedCaughtExceptions) {
							// is in outermost try-catch. Remove all caught exceptions, unchecked or checked
							removeCaughtException(unionMemberTypeBinding);
							this.caughtExceptions.add(unionMemberTypeBinding);
						} else {
							// is in some inner try-catch. Discourage already caught checked exceptions
							// from being proposed in an outer catch.
							if (!isUncheckedException(unionMemberTypeBinding)) {
								this.discouragedExceptions.add(unionMemberTypeBinding);
							}
						}
					}
				}
			} else {
				ITypeBinding exception = catchClause.getException().getType().resolveBinding();
				if (!exception.isRecovered()) {
					if (recordUncheckedCaughtExceptions) {
						// is in outermost try-catch. Remove all caught exceptions, unchecked or checked
						removeCaughtException(exception);
						this.caughtExceptions.add(exception);
					} else {
						// is in some inner try-catch. Discourage already caught checked exceptions
						// from being proposed in an outer catch
						if (!isUncheckedException(exception)) {
							this.discouragedExceptions.add(exception);
						}
					}
				}
			}
		}
	}

	private void removeCaughtException(ITypeBinding caughtException) {
		List<ITypeBinding> toRemoves = new ArrayList<>();
		for (ITypeBinding exception : this.thrownExceptions) {
			if (caughtException.getKey().equals(exception.getKey())) {
				toRemoves.add(exception);
			} else if (exception.isSubTypeCompatible(caughtException)) {
				// catching the sub-exception when super has been caught already will give an
				// error
				// so remove it from thrown list and lower the relevance for cases when it is
				// found
				// from searchAllTypes(..)
				toRemoves.add(exception);
				this.discouragedExceptions.add(exception);
			}
		}
		for (ITypeBinding toRemove : toRemoves) {
			this.thrownExceptions.remove(toRemove);
		}
	}

	private boolean isUncheckedException(ITypeBinding binding) {
		if (binding.isArray()) {
			return false;
		}
		ITypeBinding cursor = binding;
		while (cursor != null) {
			if ("Ljava/lang/RuntimeException;".equals(cursor.getKey())) {
				return true;
			} else if ("Ljava/lang/Error;".equals(cursor.getKey())) {
				return true;
			}
			cursor = cursor.getSuperclass();
		}
		return false;
	}
}
