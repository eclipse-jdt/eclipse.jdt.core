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

import org.eclipse.jdt.internal.compiler.ASTVisitor;
import org.eclipse.jdt.internal.compiler.impl.*;
import org.eclipse.jdt.internal.compiler.codegen.*;
import org.eclipse.jdt.internal.compiler.flow.*;
import org.eclipse.jdt.internal.compiler.lookup.*;

public class LocalDeclaration extends AbstractVariableDeclaration {

	public LocalVariableBinding binding;
	
	public LocalDeclaration(
		char[] name,
		int sourceStart,
		int sourceEnd) {

		this.name = name;
		this.sourceStart = sourceStart;
		this.sourceEnd = sourceEnd;
		this.declarationEnd = sourceEnd;
	}

	public FlowInfo analyseCode(
		BlockScope currentScope,
		FlowContext flowContext,
		FlowInfo flowInfo) {

		// record variable initialization if any
		if (flowInfo.isReachable()) {
			bits |= IsLocalDeclarationReachableMASK; // only set if actually reached
		}
		if (initialization == null) 
			return flowInfo;
			
		flowInfo =
			initialization
				.analyseCode(currentScope, flowContext, flowInfo)
				.unconditionalInits();

		// final int i = (i = 0);
		// no need to complain since (i = 0) part will get the blame
		//if (binding.isFinal() && flowInfo.isPotentiallyAssigned(binding)) {
		//	currentScope.problemReporter().duplicateInitializationOfFinalLocal(binding, this);
		//}
				
		flowInfo.markAsDefinitelyAssigned(binding);
		return flowInfo;
	}

	public void checkModifiers() {

		//only potential valid modifier is <<final>>
		if (((modifiers & AccJustFlag) & ~AccFinal) != 0)
			//AccModifierProblem -> other (non-visibility problem)
			//AccAlternateModifierProblem -> duplicate modifier
			//AccModifierProblem | AccAlternateModifierProblem -> visibility problem"

			modifiers = (modifiers & ~AccAlternateModifierProblem) | AccModifierProblem;
	}

	/**
	 * Code generation for a local declaration:
	 *	i.e.&nbsp;normal assignment to a local variable + unused variable handling 
	 */
	public void generateCode(BlockScope currentScope, CodeStream codeStream) {

		// even if not reachable, variable must be added to visible if allocated (28298)
		if (binding.resolvedPosition != -1) {
			codeStream.addVisibleLocalVariable(binding);
		}
		if ((bits & IsReachableMASK) == 0) {
			return;
		}
		int pc = codeStream.position;
		Constant inlinedValue;

		// something to initialize?
		if (initialization != null) {
			// initialize to constant value?
			if ((inlinedValue = initialization.constant) != NotAConstant) {
				// forget initializing unused or final locals set to constant value (final ones are inlined)
				if (binding.resolvedPosition != -1) { // may need to preserve variable
					int initPC = codeStream.position;
					codeStream.generateConstant(inlinedValue, initialization.implicitConversion);
					codeStream.recordPositionsFrom(initPC, initialization.sourceStart);
					codeStream.store(binding, false);
					binding.recordInitializationStartPC(codeStream.position);
					//				codeStream.lastInitStateIndexWhenRemovingInits = -2; // reinitialize remove index 
					//				codeStream.lastInitStateIndexWhenAddingInits = -2; // reinitialize add index		
				}
			} else { // initializing to non-constant value
				initialization.generateCode(currentScope, codeStream, true);
				// if binding unused generate then discard the value
				if (binding.resolvedPosition != -1) {
					// 26903, need extra cast to store null in array local var	
					if (binding.type.isArrayType() 
						&& (initialization.resolvedType == NullBinding	// arrayLoc = null
							|| ((initialization instanceof CastExpression)	// arrayLoc = (type[])null
								&& (((CastExpression)initialization).innermostCastedExpression().resolvedType == NullBinding)))){
						codeStream.checkcast(binding.type); 
					}					
					codeStream.store(binding, false);
					if (binding.initializationCount == 0) {
						/* Variable may have been initialized during the code initializing it
							e.g. int i = (i = 1);
						*/
						binding.recordInitializationStartPC(codeStream.position);
						//					codeStream.lastInitStateIndexWhenRemovingInits = -2; // reinitialize remove index 
						//					codeStream.lastInitStateIndexWhenAddingInits = -2; // reinitialize add index 
					}
				} else {
					if ((binding.type == LongBinding) || (binding.type == DoubleBinding)) {
						codeStream.pop2();
					} else {
						codeStream.pop();
					}
				}
			}
		}
		codeStream.recordPositionsFrom(pc, this.sourceStart);
	}

	public void resolve(BlockScope scope) {

		// create a binding and add it to the scope
		TypeBinding variableType = type.resolveType(scope);

		checkModifiers();

		if (variableType != null) {
			if (variableType == VoidBinding) {
				scope.problemReporter().variableTypeCannotBeVoid(this);
				return;
			}
			if (variableType.isArrayType() && ((ArrayBinding) variableType).leafComponentType == VoidBinding) {
				scope.problemReporter().variableTypeCannotBeVoidArray(this);
				return;
			}
		}
		
		Binding existingVariable = scope.getBinding(name, BindingIds.VARIABLE, this, false /*do not resolve hidden field*/);
		boolean shouldInsertInScope = true;
		if (existingVariable != null && existingVariable.isValidBinding()){
			if (existingVariable instanceof LocalVariableBinding && this.hiddenVariableDepth == 0) {
				shouldInsertInScope = false;
				scope.problemReporter().redefineLocal(this);
			} else {
				scope.problemReporter().localVariableHiding(this, existingVariable, false);
			}
		}
				
		if (shouldInsertInScope) {
			if ((modifiers & AccFinal)!= 0 && this.initialization == null) {
				modifiers |= AccBlankFinal;
			}
			binding = new LocalVariableBinding(this, variableType, modifiers, false);
			scope.addLocalVariable(binding);
			binding.setConstant(NotAConstant);
			// allow to recursivelly target the binding....
			// the correct constant is harmed if correctly computed at the end of this method
		}

		if (variableType == null) {
			if (initialization != null)
				initialization.resolveType(scope); // want to report all possible errors
			return;
		}

		// store the constant for final locals 	
		if (initialization != null) {
			if (initialization instanceof ArrayInitializer) {
				TypeBinding initializationType = initialization.resolveTypeExpecting(scope, variableType);
				if (initializationType != null) {
					((ArrayInitializer) initialization).binding = (ArrayBinding) initializationType;
					initialization.computeConversion(scope, variableType, initializationType);
				}
			} else {
			    this.initialization.setExpectedType(variableType);
				TypeBinding initializationType = this.initialization.resolveType(scope);
				if (initializationType != null) {
					if (initialization.isConstantValueOfTypeAssignableToType(initializationType, variableType)
						|| (variableType.isBaseType() && BaseTypeBinding.isWidening(variableType.id, initializationType.id))
						|| initializationType.isCompatibleWith(variableType)) {
						this.initialization.computeConversion(scope, variableType, initializationType);
						if (initializationType.isRawType() && (variableType.isBoundParameterizedType() || variableType.isGenericType())) {
							    scope.problemReporter().unsafeRawConversion(this.initialization, initializationType, variableType);
						}						
					} else {
						scope.problemReporter().typeMismatchError(initializationType, variableType, this);
					}
				}
			}

			// change the constant in the binding when it is final
			// (the optimization of the constant propagation will be done later on)
			// cast from constant actual type to variable type
			if (binding != null) {
				binding.setConstant(
					binding.isFinal()
						? initialization.constant.castTo((variableType.id << 4) + initialization.constant.typeID())
						: NotAConstant);
			}
		}
	}

	public void traverse(ASTVisitor visitor, BlockScope scope) {

		if (visitor.visit(this, scope)) {
			type.traverse(visitor, scope);
			if (initialization != null)
				initialization.traverse(visitor, scope);
		}
		visitor.endVisit(this, scope);
	}
}
