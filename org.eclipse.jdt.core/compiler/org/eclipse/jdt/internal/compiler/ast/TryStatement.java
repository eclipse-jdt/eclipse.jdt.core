/*******************************************************************************
 * Copyright (c) 2000, 2003 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jdt.internal.compiler.ast;

import org.eclipse.jdt.internal.compiler.IAbstractSyntaxTreeVisitor;
import org.eclipse.jdt.internal.compiler.codegen.*;
import org.eclipse.jdt.internal.compiler.flow.*;
import org.eclipse.jdt.internal.compiler.lookup.*;

public class TryStatement extends SubRoutineStatement {
	
	public Block tryBlock;
	public Block[] catchBlocks;
	public Argument[] catchArguments;
	public Block finallyBlock;
	BlockScope scope;

	private boolean isSubRoutineEscaping = false;
	public UnconditionalFlowInfo subRoutineInits;
	
	// should rename into subRoutineComplete to be set to false by default

	ReferenceBinding[] caughtExceptionTypes;
	boolean tryBlockExit;
	boolean[] catchExits;
	public int[] preserveExceptionHandler;

	Label subRoutineStartLabel;
	public LocalVariableBinding anyExceptionVariable,
		returnAddressVariable,
		secretReturnValue;

	public final static char[] SecretReturnName = " returnAddress".toCharArray(); //$NON-NLS-1$
	public final static char[] SecretAnyHandlerName = " anyExceptionHandler".toCharArray(); //$NON-NLS-1$
	public static final char[] SecretLocalDeclarationName = " returnValue".toCharArray(); //$NON-NLS-1$

	// for local variables table attributes
	int preTryInitStateIndex = -1;
	int mergedInitStateIndex = -1;

	public FlowInfo analyseCode(
		BlockScope currentScope,
		FlowContext flowContext,
		FlowInfo flowInfo) {

		// Consider the try block and catch block so as to compute the intersection of initializations and	
		// the minimum exit relative depth amongst all of them. Then consider the subroutine, and append its
		// initialization to the try/catch ones, if the subroutine completes normally. If the subroutine does not
		// complete, then only keep this result for the rest of the analysis

		// process the finally block (subroutine) - create a context for the subroutine

		preTryInitStateIndex =
			currentScope.methodScope().recordInitializationStates(flowInfo);

		if (anyExceptionVariable != null) {
			anyExceptionVariable.useFlag = LocalVariableBinding.USED;
		}
		if (returnAddressVariable != null) {
			returnAddressVariable.useFlag = LocalVariableBinding.USED;
		}
		InsideSubRoutineFlowContext insideSubContext;
		FinallyFlowContext finallyContext;
		UnconditionalFlowInfo subInfo;
		if (subRoutineStartLabel == null) {
			// no finally block
			insideSubContext = null;
			finallyContext = null;
			subInfo = null;
		} else {
			// analyse finally block first
			insideSubContext = new InsideSubRoutineFlowContext(flowContext, this);
			subInfo = 
				finallyBlock
					.analyseCode(
						currentScope,
						finallyContext = new FinallyFlowContext(flowContext, finallyBlock),
						flowInfo.copy())
					.unconditionalInits();
			if (!subInfo.isReachable()) {
				isSubRoutineEscaping = true;
			}
			this.subRoutineInits = subInfo;
		}
		// process the try block in a context handling the local exceptions.
		ExceptionHandlingFlowContext handlingContext =
			new ExceptionHandlingFlowContext(
				insideSubContext == null ? flowContext : insideSubContext,
				tryBlock,
				caughtExceptionTypes,
				scope,
				flowInfo.unconditionalInits());

		FlowInfo tryInfo;
		if (tryBlock.statements == null) {
			tryInfo = flowInfo;
			tryBlockExit = false;
		} else {
			tryInfo = tryBlock.analyseCode(currentScope, handlingContext, flowInfo.copy());
			tryBlockExit = !tryInfo.isReachable();
		}

		// check unreachable catch blocks
		handlingContext.complainIfUnusedExceptionHandlers(catchBlocks, scope, this);

		// process the catch blocks - computing the minimal exit depth amongst try/catch
		if (catchArguments != null) {
			int catchCount;
			catchExits = new boolean[catchCount = catchBlocks.length];
			for (int i = 0; i < catchCount; i++) {
				// keep track of the inits that could potentially have led to this exception handler (for final assignments diagnosis)
				FlowInfo catchInfo =
					flowInfo
						.copy()
						.unconditionalInits()
						.addPotentialInitializationsFrom(
							handlingContext.initsOnException(caughtExceptionTypes[i]).unconditionalInits())
						.addPotentialInitializationsFrom(tryInfo.unconditionalInits())
						.addPotentialInitializationsFrom(handlingContext.initsOnReturn);

				// catch var is always set
				catchInfo.markAsDefinitelyAssigned(catchArguments[i].binding);
				/*
				"If we are about to consider an unchecked exception handler, potential inits may have occured inside
				the try block that need to be detected , e.g. 
				try { x = 1; throwSomething();} catch(Exception e){ x = 2} "
				"(uncheckedExceptionTypes notNil and: [uncheckedExceptionTypes at: index])
				ifTrue: [catchInits addPotentialInitializationsFrom: tryInits]."
				*/
				// TODO: (philippe) should only tag as unreachable if the catchblock cannot be reached?
				//??? if (!handlingContext.initsOnException(caughtExceptionTypes[i]).isReachable()){
				if (tryBlock.statements == null) {
					catchInfo.setReachMode(FlowInfo.UNREACHABLE);
				}
				catchInfo =
					catchBlocks[i].analyseCode(
						currentScope,
						insideSubContext == null ? flowContext : insideSubContext,
						catchInfo);
				catchExits[i] = !catchInfo.isReachable();
				tryInfo = tryInfo.mergedWith(catchInfo.unconditionalInits());
			}
		}
		if (subRoutineStartLabel == null) {
			mergedInitStateIndex =
				currentScope.methodScope().recordInitializationStates(tryInfo);
			return tryInfo;
		}


		// we also need to check potential multiple assignments of final variables inside the finally block
		// need to include potential inits from returns inside the try/catch parts - 1GK2AOF
		finallyContext.complainOnRedundantFinalAssignments(
			tryInfo.isReachable() 
				? (tryInfo.addPotentialInitializationsFrom(insideSubContext.initsOnReturn))
				: insideSubContext.initsOnReturn, 
			currentScope);
		if (subInfo == FlowInfo.DEAD_END) {
			mergedInitStateIndex =
				currentScope.methodScope().recordInitializationStates(subInfo);
			return subInfo;
		} else {
			FlowInfo mergedInfo = tryInfo.addInitializationsFrom(subInfo);
			mergedInitStateIndex =
				currentScope.methodScope().recordInitializationStates(mergedInfo);
			return mergedInfo;
		}
	}

	public boolean isSubRoutineEscaping() {

		return isSubRoutineEscaping;
	}

	/**
	 * Try statement code generation
	 *
	 */
	public void generateCode(BlockScope currentScope, CodeStream codeStream) {

		if ((bits & IsReachableMASK) == 0) {
			return;
		}
		this.resetAnyExceptionHandlers(); // could reenter if redoing codegen in wide-mode

		if (tryBlock.isEmptyBlock()) {
			if (subRoutineStartLabel != null) {
				// since not passing the finallyScope, the block generation will exitUserScope(finallyScope)
				finallyBlock.generateCode(scope, codeStream);
			}
			// May loose some local variable initializations : affecting the local variable attributes
			if (mergedInitStateIndex != -1) {
				codeStream.removeNotDefinitelyAssignedVariables(
					currentScope,
					mergedInitStateIndex);
			}
			// no local bytecode produced so no need for position remembering
			return;
		}
		int pc = codeStream.position;
		Label endLabel = new Label(codeStream);
		boolean requiresNaturalJsr = false;

		// preparing exception labels
		int maxCatches;
		ExceptionLabel[] exceptionLabels =
			new ExceptionLabel[maxCatches =
				catchArguments == null ? 0 : catchArguments.length];
		for (int i = 0; i < maxCatches; i++) {
			boolean preserveCurrentHandler =
				(preserveExceptionHandler[i
					/ ExceptionHandlingFlowContext.BitCacheSize]
						& (1 << (i % ExceptionHandlingFlowContext.BitCacheSize)))
					!= 0;
			if (preserveCurrentHandler) {
				exceptionLabels[i] =
					new ExceptionLabel(
						codeStream,
						(ReferenceBinding) catchArguments[i].binding.type);
			}
		}
		if (subRoutineStartLabel != null) {
			subRoutineStartLabel.codeStream = codeStream;
			this.enterAnyExceptionHandler(codeStream);
		}
		// generate the try block
		tryBlock.generateCode(scope, codeStream);
		boolean tryBlockHasSomeCode = codeStream.position != pc;
		// flag telling if some bytecodes were issued inside the try block

		// natural exit: only if necessary
		boolean nonReturningSubRoutine = subRoutineStartLabel != null && isSubRoutineEscaping; // TODO: (philippe) simplify
		if ((!tryBlockExit) && tryBlockHasSomeCode) {
			int position = codeStream.position;
			if (nonReturningSubRoutine) {
				codeStream.goto_(subRoutineStartLabel);
			} else {
				requiresNaturalJsr = true;
				codeStream.goto_(endLabel);
			}
			codeStream.updateLastRecordedEndPC(position);
			//goto is tagged as part of the try block
		}
		// place end positions of user-defined exception labels
		if (tryBlockHasSomeCode) {
			for (int i = 0; i < maxCatches; i++) {
				boolean preserveCurrentHandler =
					(preserveExceptionHandler[i / ExceptionHandlingFlowContext.BitCacheSize]
							& (1 << (i % ExceptionHandlingFlowContext.BitCacheSize))) != 0;
				if (preserveCurrentHandler) {
					exceptionLabels[i].placeEnd();
				}
			}
			/* generate sequence of handler, all starting by storing the TOS (exception
			thrown) into their own catch variables, the one specified in the source
			that must denote the handled exception.
			*/
			if (catchArguments == null) {
				this.exitAnyExceptionHandler();
			} else {
				for (int i = 0; i < maxCatches; i++) {
					boolean preserveCurrentHandler =
						(preserveExceptionHandler[i / ExceptionHandlingFlowContext.BitCacheSize]
								& (1 << (i % ExceptionHandlingFlowContext.BitCacheSize))) != 0;
					if (preserveCurrentHandler) {
						// May loose some local variable initializations : affecting the local variable attributes
						if (preTryInitStateIndex != -1) {
							codeStream.removeNotDefinitelyAssignedVariables(
								currentScope,
								preTryInitStateIndex);
						}
						exceptionLabels[i].place();
						codeStream.incrStackSize(1);
						// optimizing the case where the exception variable is not actually used
						LocalVariableBinding catchVar;
						int varPC = codeStream.position;
						if ((catchVar = catchArguments[i].binding).resolvedPosition != -1) {
							codeStream.store(catchVar, false);
							catchVar.recordInitializationStartPC(codeStream.position);
							codeStream.addVisibleLocalVariable(catchVar);
						} else {
							codeStream.pop();
						}
						codeStream.recordPositionsFrom(varPC, catchArguments[i].sourceStart);
						// Keep track of the pcs at diverging point for computing the local attribute
						// since not passing the catchScope, the block generation will exitUserScope(catchScope)
						catchBlocks[i].generateCode(scope, codeStream);
					}
					if (i == maxCatches - 1) {
						this.exitAnyExceptionHandler();
						if (subRoutineStartLabel != null) {
							if (!catchExits[i] && preserveCurrentHandler) {
								requiresNaturalJsr = !nonReturningSubRoutine;
								codeStream.goto_(endLabel);
							}
						}
					} else {
						if (!catchExits[i] && preserveCurrentHandler) {
							if (nonReturningSubRoutine) {
								codeStream.goto_(subRoutineStartLabel);
							} else {
								requiresNaturalJsr = true;
								codeStream.goto_(endLabel);
							}
						}
					}
				}
			}
			// extra handler for trailing natural exit (will be fixed up later on when natural exit is generated below)
			ExceptionLabel naturalExitExceptionHandler = null;
			if (requiresNaturalJsr) {
				naturalExitExceptionHandler = this.enterAnyExceptionHandler(codeStream);
			}
						
			// addition of a special handler so as to ensure that any uncaught exception (or exception thrown
			// inside catch blocks) will run the finally block
			int finallySequenceStartPC = codeStream.position;
			if (subRoutineStartLabel != null) {
				// the additional handler is doing: jsr finallyBlock and rethrow TOS-exception
				this.placeAllAnyExceptionHandlers();

				if (preTryInitStateIndex != -1) {
					// reset initialization state, as for a normal catch block
					codeStream.removeNotDefinitelyAssignedVariables(
						currentScope,
						preTryInitStateIndex);
				}

				codeStream.incrStackSize(1);
				if (nonReturningSubRoutine) {
					codeStream.pop();
					// "if subroutine cannot return, no need to jsr/jump to subroutine since it will be entered in sequence
				} else {
					codeStream.store(anyExceptionVariable, false);
					codeStream.jsr(subRoutineStartLabel);
					codeStream.load(anyExceptionVariable);
					codeStream.athrow();
				}
				// end of catch sequence, place label that will correspond to the finally block beginning, or end of statement	
				subRoutineStartLabel.place();
				if (!nonReturningSubRoutine) {
					codeStream.incrStackSize(1);
					codeStream.store(returnAddressVariable, false);
				}
				codeStream.recordPositionsFrom(finallySequenceStartPC, finallyBlock.sourceStart);
				// entire sequence for finally is associated to finally block
				finallyBlock.generateCode(scope, codeStream);
				if (!nonReturningSubRoutine) {
					int position = codeStream.position;
					codeStream.ret(returnAddressVariable.resolvedPosition);
					codeStream.updateLastRecordedEndPC(position);
					codeStream.recordPositionsFrom(
						position,
						finallyBlock.sourceEnd);
					// the ret bytecode is part of the subroutine
				}
				// will naturally fall into subsequent code after subroutine invocation
				endLabel.place();
				if (naturalExitExceptionHandler != null) {
					int position = codeStream.position;					
					// fix up natural exit handler
					naturalExitExceptionHandler.placeStart();
					codeStream.jsr(subRoutineStartLabel);
					naturalExitExceptionHandler.placeEnd();
					codeStream.recordPositionsFrom(
						position,
						finallyBlock.sourceStart);					
				}
			} else {
				// no subroutine, simply position end label
				endLabel.place();
			}
		} else {
			// try block had no effect, only generate the body of the finally block if any
			endLabel.place();
			if (subRoutineStartLabel != null) {
				finallyBlock.generateCode(scope, codeStream);
			}
		}
		// May loose some local variable initializations : affecting the local variable attributes
		if (mergedInitStateIndex != -1) {
			codeStream.removeNotDefinitelyAssignedVariables(
				currentScope,
				mergedInitStateIndex);
			codeStream.addDefinitelyAssignedVariables(currentScope, mergedInitStateIndex);
		}
		codeStream.recordPositionsFrom(pc, this.sourceStart);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jdt.internal.compiler.ast.SubRoutineStatement#generateSubRoutineInvocation(org.eclipse.jdt.internal.compiler.lookup.BlockScope, org.eclipse.jdt.internal.compiler.codegen.CodeStream)
	 */
	public void generateSubRoutineInvocation(
			BlockScope currentScope,
			CodeStream codeStream) {

		if (this.isSubRoutineEscaping) {
			codeStream.goto_(this.subRoutineStartLabel);
		} else {
			codeStream.jsr(this.subRoutineStartLabel);
		}
	}

	public void resetStateForCodeGeneration() {
		if (this.subRoutineStartLabel != null) {
			this.subRoutineStartLabel.resetStateForCodeGeneration();
		}
	}	

	public void resolve(BlockScope upperScope) {

		// special scope for secret locals optimization.	
		this.scope = new BlockScope(upperScope);

		BlockScope tryScope = new BlockScope(scope);
		BlockScope finallyScope = null;
		
		if (finallyBlock != null
			&& finallyBlock.statements != null) {

			finallyScope = new BlockScope(scope, false); // don't add it yet to parent scope

			// provision for returning and forcing the finally block to run
			MethodScope methodScope = scope.methodScope();

			// the type does not matter as long as it is not a base type
			this.returnAddressVariable =
				new LocalVariableBinding(SecretReturnName, upperScope.getJavaLangObject(), AccDefault, false);
			finallyScope.addLocalVariable(returnAddressVariable);
			this.returnAddressVariable.constant = NotAConstant; // not inlinable
			this.subRoutineStartLabel = new Label();

			this.anyExceptionVariable =
				new LocalVariableBinding(SecretAnyHandlerName, scope.getJavaLangThrowable(), AccDefault, false);
			finallyScope.addLocalVariable(this.anyExceptionVariable);
			this.anyExceptionVariable.constant = NotAConstant; // not inlinable

			if (!methodScope.isInsideInitializer()) {
				MethodBinding methodBinding =
					((AbstractMethodDeclaration) methodScope.referenceContext).binding;
				if (methodBinding != null) {
					TypeBinding methodReturnType = methodBinding.returnType;
					if (methodReturnType.id != T_void) {
						this.secretReturnValue =
							new LocalVariableBinding(
								SecretLocalDeclarationName,
								methodReturnType,
								AccDefault,
								false);
						finallyScope.addLocalVariable(this.secretReturnValue);
						this.secretReturnValue.constant = NotAConstant; // not inlinable
					}
				}
			}
			finallyBlock.resolveUsing(finallyScope);
			// force the finally scope to have variable positions shifted after its try scope and catch ones
			finallyScope.shiftScopes = new BlockScope[catchArguments == null ? 1 : catchArguments.length+1];
			finallyScope.shiftScopes[0] = tryScope;
		}
		this.tryBlock.resolveUsing(tryScope);

		// arguments type are checked against JavaLangThrowable in resolveForCatch(..)
		if (this.catchBlocks != null) {
			int length = this.catchArguments.length;
			TypeBinding[] argumentTypes = new TypeBinding[length];
			for (int i = 0; i < length; i++) {
				BlockScope catchScope = new BlockScope(scope);
				if (finallyScope != null){
					finallyScope.shiftScopes[i+1] = catchScope;
				}
				// side effect on catchScope in resolveForCatch(..)
				if ((argumentTypes[i] = catchArguments[i].resolveForCatch(catchScope)) == null)
					return;
				catchBlocks[i].resolveUsing(catchScope);
			}

			// Verify that the catch clause are ordered in the right way:
			// more specialized first.
			this.caughtExceptionTypes = new ReferenceBinding[length];
			for (int i = 0; i < length; i++) {
				caughtExceptionTypes[i] = (ReferenceBinding) argumentTypes[i];
				for (int j = 0; j < i; j++) {
					if (caughtExceptionTypes[i].isCompatibleWith(argumentTypes[j])) {
						scope.problemReporter().wrongSequenceOfExceptionTypesError(this, i, j);
						// cannot return - since may still proceed if unreachable code is ignored (21203)
					}
				}
			}
		} else {
			caughtExceptionTypes = new ReferenceBinding[0];
		}
		
		if (finallyScope != null){
			// add finallyScope as last subscope, so it can be shifted behind try/catch subscopes.
			// the shifting is necessary to achieve no overlay in between the finally scope and its
			// sibling in term of local variable positions.
			this.scope.addSubscope(finallyScope);
		}
	}

	public String toString(int tab) {
		String s = tabString(tab);
		//try
		s = s + "try "; //$NON-NLS-1$
		if (tryBlock == Block.None)
			s = s + "{}"; //$NON-NLS-1$
		else
			s = s + "\n" + tryBlock.toString(tab + 1); //$NON-NLS-1$

		//catches
		if (catchBlocks != null)
			for (int i = 0; i < catchBlocks.length; i++)
					s = s + "\n" + tabString(tab) + "catch (" //$NON-NLS-2$ //$NON-NLS-1$
						+catchArguments[i].toString(0) + ") " //$NON-NLS-1$
						+catchBlocks[i].toString(tab + 1);
		//finally
		if (finallyBlock != null) {
			if (finallyBlock == Block.None)
				s = s + "\n" + tabString(tab) + "finally {}"; //$NON-NLS-2$ //$NON-NLS-1$
			else
					s = s + "\n" + tabString(tab) + "finally\n" + //$NON-NLS-2$ //$NON-NLS-1$
			finallyBlock.toString(tab + 1);
		}

		return s;
	}

	public void traverse(
		IAbstractSyntaxTreeVisitor visitor,
		BlockScope blockScope) {

		if (visitor.visit(this, blockScope)) {
			tryBlock.traverse(visitor, scope);
			if (catchArguments != null) {
				for (int i = 0, max = catchBlocks.length; i < max; i++) {
					catchArguments[i].traverse(visitor, scope);
					catchBlocks[i].traverse(visitor, scope);
				}
			}
			if (finallyBlock != null)
				finallyBlock.traverse(visitor, scope);
		}
		visitor.endVisit(this, blockScope);
	}
}
