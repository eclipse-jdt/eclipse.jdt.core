/*******************************************************************************
 * Copyright (c) 2011 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * This is an implementation of an early-draft specification developed under the Java
 * Community Process (JCP) and is made available for testing and evaluation purposes
 * only. The code is not compatible with any specification of the JCP.
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jdt.internal.compiler.ast;

import org.eclipse.jdt.internal.compiler.ASTVisitor;
import org.eclipse.jdt.internal.compiler.classfmt.ClassFileConstants;
import org.eclipse.jdt.internal.compiler.codegen.BranchLabel;
import org.eclipse.jdt.internal.compiler.impl.Constant;
import org.eclipse.jdt.internal.compiler.lookup.BlockScope;
import org.eclipse.jdt.internal.compiler.lookup.LocalVariableBinding;
import org.eclipse.jdt.internal.compiler.lookup.MethodBinding;
import org.eclipse.jdt.internal.compiler.lookup.MethodScope;
import org.eclipse.jdt.internal.compiler.lookup.ReferenceBinding;
import org.eclipse.jdt.internal.compiler.lookup.TypeBinding;
import org.eclipse.jdt.internal.compiler.lookup.TypeIds;

public class TryStatementWithResources extends TryStatement {

	public LocalDeclaration[] resources;

	public StringBuffer printStatement(int indent, StringBuffer output) {
		printIndent(indent, output).append("try ("); //$NON-NLS-1$
		int length = this.resources.length;
		for (int i = 0; i < length; i++) {
			this.resources[i].printAsExpression(0, output);
			if (i != length - 1) {
				output.append(";\n"); //$NON-NLS-1$
				printIndent(indent + 2, output);
			}
		}
		output.append(")\n"); //$NON-NLS-1$
		this.tryBlock.printStatement(indent + 1, output);

		// catches
		if (this.catchBlocks != null)
			for (int i = 0; i < this.catchBlocks.length; i++) {
				output.append('\n');
				printIndent(indent, output).append("catch ("); //$NON-NLS-1$
				this.catchArguments[i].print(0, output).append(")\n"); //$NON-NLS-1$
				this.catchBlocks[i].printStatement(indent + 1, output);
			}
		// finally
		if (this.finallyBlock != null) {
			output.append('\n');
			printIndent(indent, output).append("finally\n"); //$NON-NLS-1$
			this.finallyBlock.printStatement(indent + 1, output);
		}
		return output;
	}

	public void resolve(BlockScope upperScope) {
		// special scope for secret locals optimization.
		this.scope = new BlockScope(upperScope);

		BlockScope finallyScope = null;

		// resolve all resources and inject them into separate scopes
		BlockScope localScope = new BlockScope(this.scope);
		for (int i = 0, max = this.resources.length; i < max; i++) {
			this.resources[i].resolve(localScope);
			LocalVariableBinding localVariableBinding = this.resources[i].binding;
			if (localVariableBinding != null && localVariableBinding.isValidBinding()) {
				TypeBinding resourceType = localVariableBinding.type;
				if (resourceType.isClass() || resourceType.isInterface()) {
					if (resourceType.findSuperTypeOriginatingFrom(TypeIds.T_JavaLangAutoCloseable, false /*AutoCloseable is not a class*/) == null && resourceType.isValidBinding()) {
						upperScope.problemReporter().resourceHasToBeAutoCloseable(resourceType, this.resources[i].type);
					}
				} else { 
					upperScope.problemReporter().resourceHasToBeAutoCloseable(resourceType, this.resources[i].type);
				}
			}
			localScope = new BlockScope(localScope, 1);
		}

		BlockScope tryScope = new BlockScope(localScope);

		if (this.finallyBlock != null) {
			if (this.finallyBlock.isEmptyBlock()) {
				if ((this.finallyBlock.bits & ASTNode.UndocumentedEmptyBlock) != 0) {
					this.scope.problemReporter().undocumentedEmptyBlock(this.finallyBlock.sourceStart,
							this.finallyBlock.sourceEnd);
				}
			} else {
				finallyScope = new BlockScope(this.scope, false); // don't add it yet to parent scope

				// provision for returning and forcing the finally block to run
				MethodScope methodScope = this.scope.methodScope();

				// the type does not matter as long as it is not a base type
				if (!upperScope.compilerOptions().inlineJsrBytecode) {
					this.returnAddressVariable = new LocalVariableBinding(TryStatement.SECRET_RETURN_ADDRESS_NAME,
							upperScope.getJavaLangObject(), ClassFileConstants.AccDefault, false);
					finallyScope.addLocalVariable(this.returnAddressVariable);
					this.returnAddressVariable.setConstant(Constant.NotAConstant); // not inlinable
				}
				this.subRoutineStartLabel = new BranchLabel();

				this.anyExceptionVariable = new LocalVariableBinding(TryStatement.SECRET_ANY_HANDLER_NAME,
						this.scope.getJavaLangThrowable(), ClassFileConstants.AccDefault, false);
				finallyScope.addLocalVariable(this.anyExceptionVariable);
				this.anyExceptionVariable.setConstant(Constant.NotAConstant); // not inlinable

				if (!methodScope.isInsideInitializer()) {
					MethodBinding methodBinding = ((AbstractMethodDeclaration) methodScope.referenceContext).binding;
					if (methodBinding != null) {
						TypeBinding methodReturnType = methodBinding.returnType;
						if (methodReturnType.id != TypeIds.T_void) {
							this.secretReturnValue = new LocalVariableBinding(TryStatement.SECRET_RETURN_VALUE_NAME,
									methodReturnType, ClassFileConstants.AccDefault, false);
							finallyScope.addLocalVariable(this.secretReturnValue);
							this.secretReturnValue.setConstant(Constant.NotAConstant); // not inlinable
						}
					}
				}
				this.finallyBlock.resolveUsing(finallyScope);
				// force the finally scope to have variable positions shifted after its try scope and catch ones
				int shiftScopesLength = this.catchArguments == null ? 1 : this.catchArguments.length + 1;
				shiftScopesLength += this.resources.length;
				finallyScope.shiftScopes = new BlockScope[shiftScopesLength];
				finallyScope.shiftScopes[0] = tryScope;
			}
		}

		this.tryBlock.resolveUsing(tryScope);

		// arguments type are checked against JavaLangThrowable in resolveForCatch(..)
		if (this.catchBlocks != null) {
			int length = this.catchArguments.length;
			TypeBinding[] argumentTypes = new TypeBinding[length];
			boolean catchHasError = false;
			for (int i = 0; i < length; i++) {
				BlockScope catchScope = new BlockScope(this.scope);
				if (finallyScope != null) {
					finallyScope.shiftScopes[i + 1] = catchScope;
				}
				// side effect on catchScope in resolveForCatch(..)
				if ((argumentTypes[i] = this.catchArguments[i].resolveForCatch(catchScope)) == null) {
					catchHasError = true;
				}
				this.catchBlocks[i].resolveUsing(catchScope);
			}
			if (catchHasError) {
				return;
			}
			// Verify that the catch clause are ordered in the right way:
			// more specialized first.
			this.caughtExceptionTypes = new ReferenceBinding[length];
			for (int i = 0; i < length; i++) {
				this.caughtExceptionTypes[i] = (ReferenceBinding) argumentTypes[i];
				for (int j = 0; j < i; j++) {
					if (this.caughtExceptionTypes[i].isCompatibleWith(argumentTypes[j])) {
						this.scope.problemReporter().wrongSequenceOfExceptionTypesError(this,
								this.caughtExceptionTypes[i], i, argumentTypes[j]);
					}
				}
			}
		} else {
			this.caughtExceptionTypes = new ReferenceBinding[0];
		}

		if (finallyScope != null) {
			// add finallyScope as last subscope, so it can be shifted behind try/catch subscopes.
			// the shifting is necessary to achieve no overlay in between the finally scope and its
			// sibling in term of local variable positions.
			this.scope.addSubscope(finallyScope);
		}
	}

	public void traverse(ASTVisitor visitor, BlockScope blockScope) {
		if (visitor.visit(this, blockScope)) {
			LocalDeclaration[] localDeclarations = this.resources;
			for (int i = 0, max = localDeclarations.length; i < max; i++) {
				localDeclarations[i].traverse(visitor, this.scope);
			}
			this.tryBlock.traverse(visitor, this.scope);
			if (this.catchArguments != null) {
				for (int i = 0, max = this.catchBlocks.length; i < max; i++) {
					this.catchArguments[i].traverse(visitor, this.scope);
					this.catchBlocks[i].traverse(visitor, this.scope);
				}
			}
			if (this.finallyBlock != null)
				this.finallyBlock.traverse(visitor, this.scope);
		}
		visitor.endVisit(this, blockScope);
	}
}
