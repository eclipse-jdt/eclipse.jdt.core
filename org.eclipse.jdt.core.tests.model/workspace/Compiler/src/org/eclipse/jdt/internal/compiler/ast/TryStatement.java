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
		if (returnAddressVariable != null) { // TODO (philippe) if subroutine is escaping, unused
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
			if (subInfo == FlowInfo.DEAD_END) {
				isSubRoutineEscaping = true;
				scope.problemReporter().finallyMustCompleteNormally(finallyBlock);
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
		if (tryBlock.isEmptyBlock()) {
			tryInfo = flowInfo;
			tryBlockExit = false;
		} else {
			tryInfo = tryBlock.analyseCode(currentScope, handlingContext, flowInfo.copy());
			tryBlockExit = !tryInfo.isReachable();
		}

		// check unreachable catch blocks
		handlingContext.complainIfUnusedExceptionHandlers(scope, this);

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
				// TODO (philippe) should only tag as unreachable if the catchblock cannot be reached?
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
	 * Try statement code generation with or without jsr bytecode use
	 *	post 1.5 target level, cannot use jsr bytecode, must instead inline finally block
	 * returnAddress is only allocated if jsr is allowed
	 */
	public void generateCode(BlockScope currentScope, CodeStream codeStream) {
		if ((bits & IsReachableMASK) == 0) {
			return;
		}
		// in case the labels needs to be reinitialized
		// when the code generation is restarted in wide mode
		if (this.anyExceptionLabelsCount > 0) {
			this.anyExceptionLabels = NO_EXCEPTION_HANDLER;
			this.anyExceptionLabelsCount = 0;
		}
		int pc = codeStream.position;
		final int NO_FINALLY = 0;									// no finally block
		final int FINALLY_SUBROUTINE = 1; 					// finally is generated as a subroutine (using jsr/ret bytecodes)
		final int FINALLY_DOES_NOT_COMPLETE = 2;	// non returning finally is optimized with only one instance of finally block
		final int FINALLY_MUST_BE_INLINED = 3;			// finally block must be inlined since cannot use jsr/ret bytecodes >1.5
		int finallyMode;
		if (subRoutineStartLabel == null) { 
			finallyMode = NO_FINALLY;
		} else {
			if (this.isSubRoutineEscaping) {
				finallyMode = FINALLY_DOES_NOT_COMPLETE;
			} else if (scope.environment().options.inlineJsrBytecode) {
				finallyMode = FINALLY_MUST_BE_INLINED;
			} else {
				finallyMode = FINALLY_SUBROUTINE;
			}
		}
		boolean requiresNaturalExit = false;
		// preparing exception labels
		int maxCatches;
		ExceptionLabel[] exceptionLabels =
			new ExceptionLabel[maxCatches =
				catchArguments == null ? 0 : catchArguments.length];
		for (int i = 0; i < maxCatches; i++) {
			exceptionLabels[i] = new ExceptionLabel(codeStream, catchArguments[i].binding.type);
		}
		if (subRoutineStartLabel != null) {
			subRoutineStartLabel.initialize(codeStream);
			this.enterAnyExceptionHandler(codeStream);
		}
		// generate the try block
		tryBlock.generateCode(scope, codeStream);
		boolean tryBlockHasSomeCode = codeStream.position != pc;
		// flag telling if some bytecodes were issued inside the try block

		// place end positions of user-defined exception labels
		if (tryBlockHasSomeCode) {
			// natural exit may require subroutine invocation (if finally != null)
			Label naturalExitLabel = new Label(codeStream);
			if (!tryBlockExit) {
				int position = codeStream.position;
				switch(finallyMode) {
					case FINALLY_SUBROUTINE :
					case FINALLY_MUST_BE_INLINED :
						requiresNaturalExit = true;
						// fall through
					case NO_FINALLY :
						codeStream.goto_(naturalExitLabel);
						break;
					case FINALLY_DOES_NOT_COMPLETE :
						codeStream.goto_(subRoutineStartLabel);
						break;
				}
				codeStream.updateLastRecordedEndPC(position);
				//goto is tagged as part of the try block
			}
			for (int i = 0; i < maxCatches; i++) {
				exceptionLabels[i].placeEnd();
			}
			/* generate sequence of handler, all starting by storing the TOS (exception
			thrown) into their own catch variables, the one specified in the source
			that must denote the handled exception.
			*/
			if (catchArguments != null) {
				for (int i = 0; i < maxCatches; i++) {
					// May loose some local variable initializations : affecting the local variable attributes
					if (preTryInitStateIndex != -1) {
						codeStream.removeNotDefinitelyAssignedVariables(currentScope, preTryInitStateIndex);
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
					if (!catchExits[i]) {
						switch(finallyMode) {
							case FINALLY_SUBROUTINE :
							case FINALLY_MUST_BE_INLINED :
								requiresNaturalExit = true;
								// fall through
							case NO_FINALLY :
								codeStream.goto_(naturalExitLabel);
								break;
							case FINALLY_DOES_NOT_COMPLETE :
								codeStream.goto_(subRoutineStartLabel);
								break;
						}
					}
				}
			}
			this.exitAnyExceptionHandler();
			// extra handler for trailing natural exit (will be fixed up later on when natural exit is generated below)
			ExceptionLabel naturalExitExceptionHandler = 
				finallyMode == FINALLY_SUBROUTINE && requiresNaturalExit ? new ExceptionLabel(codeStream, null) : null;

			// addition of a special handler so as to ensure that any uncaught exception (or exception thrown
			// inside catch blocks) will run the finally block
			int finallySequenceStartPC = codeStream.position;
			if (subRoutineStartLabel != null) {
				this.placeAllAnyExceptionHandlers();
				if (naturalExitExceptionHandler != null) naturalExitExceptionHandler.place();
				
				if (preTryInitStateIndex != -1) {
					// reset initialization state, as for a normal catch block
					codeStream.removeNotDefinitelyAssignedVariables(currentScope, preTryInitStateIndex);
				}

				codeStream.incrStackSize(1);
				switch(finallyMode) {
					case FINALLY_SUBROUTINE :
						codeStream.store(anyExceptionVariable, false);
						codeStream.jsr(subRoutineStartLabel);
						codeStream.recordPositionsFrom(finallySequenceStartPC, finallyBlock.sourceStart);
						int position = codeStream.position;						
						codeStream.load(anyExceptionVariable);
						codeStream.athrow();
						codeStream.recordPositionsFrom(position, finallyBlock.sourceEnd);
						subRoutineStartLabel.place();
						codeStream.incrStackSize(1);
						position = codeStream.position;	
						codeStream.store(returnAddressVariable, false);
						codeStream.recordPositionsFrom(position, finallyBlock.sourceStart);
						finallyBlock.generateCode(scope, codeStream);
						position = codeStream.position;
						codeStream.ret(returnAddressVariable.resolvedPosition);
//						codeStream.updateLastRecordedEndPC(position);
						codeStream.recordPositionsFrom(
							position,
							finallyBlock.sourceEnd);
						// the ret bytecode is part of the subroutine
						break;
					case FINALLY_MUST_BE_INLINED :
						codeStream.store(anyExceptionVariable, false);
						codeStream.recordPositionsFrom(finallySequenceStartPC, finallyBlock.sourceStart);
						this.finallyBlock.generateCode(currentScope, codeStream);
						position = codeStream.position;
						codeStream.load(anyExceptionVariable);
						codeStream.athrow();
						subRoutineStartLabel.place();
						codeStream.recordPositionsFrom(position, finallyBlock.sourceEnd);
						break;
					case FINALLY_DOES_NOT_COMPLETE :
						codeStream.pop();
						subRoutineStartLabel.place();
						codeStream.recordPositionsFrom(finallySequenceStartPC, finallyBlock.sourceStart);
						finallyBlock.generateCode(scope, codeStream);
						break;
				}
				// will naturally fall into subsequent code after subroutine invocation
				naturalExitLabel.place();
				if (requiresNaturalExit) {
					switch(finallyMode) {
						case FINALLY_SUBROUTINE :
							int position = codeStream.position;
							// fix up natural exit handler
							naturalExitExceptionHandler.placeStart();
							codeStream.jsr(subRoutineStartLabel);
							naturalExitExceptionHandler.placeEnd();
							codeStream.recordPositionsFrom(
								position,
								finallyBlock.sourceEnd);	
							break;
						case FINALLY_MUST_BE_INLINED :
							// May loose some local variable initializations : affecting the local variable attributes
							// needed since any exception handler got inlined subroutine
							if (preTryInitStateIndex != -1) {
								codeStream.removeNotDefinitelyAssignedVariables(currentScope, preTryInitStateIndex);
							}
							// entire sequence for finally is associated to finally block
							finallyBlock.generateCode(scope, codeStream);
							break;
						case FINALLY_DOES_NOT_COMPLETE :
							break;
					}
				}
			} else {
				// no subroutine, simply position end label (natural exit == end)
				naturalExitLabel.place();
			}
		} else {
			// try block had no effect, only generate the body of the finally block if any
			if (subRoutineStartLabel != null) {
				finallyBlock.generateCode(scope, codeStream);
			}
		}
		// May loose some local variable initializations : affecting the local variable attributes
		if (mergedInitStateIndex != -1) {
			codeStream.removeNotDefinitelyAssignedVariables(currentScope, mergedInitStateIndex);
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
			if (currentScope.environment().options.inlineJsrBytecode) { 
				// cannot use jsr bytecode, then simply inline the subroutine
				this.finallyBlock.generateCode(currentScope, codeStream);
			} else {
				// classic subroutine invocation, distinguish case of non-returning subroutine
				codeStream.jsr(this.subRoutineStartLabel);
			}
		}
	}

	public StringBuffer printStatement(int indent, StringBuffer output) {
		printIndent(indent, output).append("try \n"); //$NON-NLS-1$
		tryBlock.printStatement(indent + 1, output); //$NON-NLS-1$

		//catches
		if (catchBlocks != null)
			for (int i = 0; i < catchBlocks.length; i++) {
					output.append('\n');
					printIndent(indent, output).append("catch ("); //$NON-NLS-1$
					catchArguments[i].print(0, output).append(") "); //$NON-NLS-1$
					catchBlocks[i].printStatement(indent + 1, output);
			}
		//finally
		if (finallyBlock != null) {
			output.append('\n');
			printIndent(indent, output).append("finally\n"); //$NON-NLS-1$
			finallyBlock.printStatement(indent + 1, output);
		}

		return output;
	}

	public void resolve(BlockScope upperScope) {

		// special scope for secret locals optimization.	
		this.scope = new BlockScope(upperScope);

		BlockScope tryScope = new BlockScope(scope);
		BlockScope finallyScope = null;
		
		if (finallyBlock != null) {
			if (finallyBlock.isEmptyBlock()) {
				if ((finallyBlock.bits & UndocumentedEmptyBlockMASK) != 0) {
					scope.problemReporter().undocumentedEmptyBlock(finallyBlock.sourceStart, finallyBlock.sourceEnd);
				}
			} else {
				finallyScope = new BlockScope(scope, false); // don't add it yet to parent scope
	
				// provision for returning and forcing the finally block to run
				MethodScope methodScope = scope.methodScope();
	
				// the type does not matter as long as it is not a base type
				if (!upperScope.environment().options.inlineJsrBytecode) {
					this.returnAddressVariable =
						new LocalVariableBinding(SecretReturnName, upperScope.getJavaLangObject(), AccDefault, false);
					finallyScope.addLocalVariable(returnAddressVariable);
					this.returnAddressVariable.setConstant(NotAConstant); // not inlinable
				}
				this.subRoutineStartLabel = new Label();
	
				this.anyExceptionVariable =
					new LocalVariableBinding(SecretAnyHandlerName, scope.getJavaLangThrowable(), AccDefault, false);
				finallyScope.addLocalVariable(this.anyExceptionVariable);
				this.anyExceptionVariable.setConstant(NotAConstant); // not inlinable
	
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
							this.secretReturnValue.setConstant(NotAConstant); // not inlinable
						}
					}
				}
				finallyBlock.resolveUsing(finallyScope);
				// force the finally scope to have variable positions shifted after its try scope and catch ones
				finallyScope.shiftScopes = new BlockScope[catchArguments == null ? 1 : catchArguments.length+1];
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
				BlockScope catchScope = new BlockScope(scope);
				if (finallyScope != null){
					finallyScope.shiftScopes[i+1] = catchScope;
				}
				// side effect on catchScope in resolveForCatch(..)
				if ((argumentTypes[i] = catchArguments[i].resolveForCatch(catchScope)) == null) {
					catchHasError = true;
				}
				catchBlocks[i].resolveUsing(catchScope);
			}
			if (catchHasError) {
				return;
			}
			// Verify that the catch clause are ordered in the right way:
			// more specialized first.
			this.caughtExceptionTypes = new ReferenceBinding[length];
			for (int i = 0; i < length; i++) {
				caughtExceptionTypes[i] = (ReferenceBinding) argumentTypes[i];
				for (int j = 0; j < i; j++) {
					if (caughtExceptionTypes[i].isCompatibleWith(argumentTypes[j])) {
						scope.problemReporter().wrongSequenceOfExceptionTypesError(this, caughtExceptionTypes[i], i, argumentTypes[j]);
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

	public void traverse(
		ASTVisitor visitor,
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
