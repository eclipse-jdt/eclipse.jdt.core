/*******************************************************************************
 * Copyright (c) 2000, 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jdt.internal.compiler.ast;

import org.eclipse.jdt.internal.compiler.ASTVisitor;
import org.eclipse.jdt.internal.compiler.classfmt.ClassFileConstants;
import org.eclipse.jdt.internal.compiler.codegen.*;
import org.eclipse.jdt.internal.compiler.flow.*;
import org.eclipse.jdt.internal.compiler.impl.Constant;
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

		this.preTryInitStateIndex =
			currentScope.methodScope().recordInitializationStates(flowInfo);

		if (this.anyExceptionVariable != null) {
			this.anyExceptionVariable.useFlag = LocalVariableBinding.USED;
		}
		if (this.returnAddressVariable != null) { // TODO (philippe) if subroutine is escaping, unused
			this.returnAddressVariable.useFlag = LocalVariableBinding.USED;
		}
		InsideSubRoutineFlowContext insideSubContext;
		FinallyFlowContext finallyContext;
		UnconditionalFlowInfo subInfo;
		if (this.subRoutineStartLabel == null) {
			// no finally block
			insideSubContext = null;
			finallyContext = null;
			subInfo = null;
		} else {
			// analyse finally block first
			insideSubContext = new InsideSubRoutineFlowContext(flowContext, this);
			subInfo = 
				this.finallyBlock
					.analyseCode(
						currentScope,
						finallyContext = new FinallyFlowContext(flowContext, this.finallyBlock),
						flowInfo.nullInfoLessUnconditionalCopy())
					.unconditionalInits();
			if (subInfo == FlowInfo.DEAD_END) {
				this.isSubRoutineEscaping = true;
				this.scope.problemReporter().finallyMustCompleteNormally(this.finallyBlock);
			}
			this.subRoutineInits = subInfo;
		}
		// process the try block in a context handling the local exceptions.
		ExceptionHandlingFlowContext handlingContext =
			new ExceptionHandlingFlowContext(
				insideSubContext == null ? flowContext : insideSubContext,
				this.tryBlock,
				this.caughtExceptionTypes,
				this.scope,
				flowInfo.unconditionalInits());

		FlowInfo tryInfo;
		if (this.tryBlock.isEmptyBlock()) {
			tryInfo = flowInfo;
			this.tryBlockExit = false;
		} else {
			tryInfo = this.tryBlock.analyseCode(currentScope, handlingContext, flowInfo.copy());
			this.tryBlockExit = (tryInfo.tagBits & FlowInfo.UNREACHABLE) != 0;
		}

		// check unreachable catch blocks
		handlingContext.complainIfUnusedExceptionHandlers(this.scope, this);

		// process the catch blocks - computing the minimal exit depth amongst try/catch
		if (this.catchArguments != null) {
			int catchCount;
			this.catchExits = new boolean[catchCount = this.catchBlocks.length];
			for (int i = 0; i < catchCount; i++) {
				// keep track of the inits that could potentially have led to this exception handler (for final assignments diagnosis)
				FlowInfo catchInfo =
					flowInfo.unconditionalCopy().
						addPotentialInitializationsFrom(
							handlingContext.initsOnException(
								this.caughtExceptionTypes[i]))
						.addPotentialInitializationsFrom(
							tryInfo.nullInfoLessUnconditionalCopy())
							// remove null info to protect point of 
							// exception null info 
						.addPotentialInitializationsFrom(
							handlingContext.initsOnReturn.
								nullInfoLessUnconditionalCopy());

				// catch var is always set
				LocalVariableBinding catchArg = this.catchArguments[i].binding;
				FlowContext catchContext = insideSubContext == null ? flowContext : insideSubContext;
				catchInfo.markAsDefinitelyAssigned(catchArg);
				catchInfo.markAsDefinitelyNonNull(catchArg);
				/*
				"If we are about to consider an unchecked exception handler, potential inits may have occured inside
				the try block that need to be detected , e.g. 
				try { x = 1; throwSomething();} catch(Exception e){ x = 2} "
				"(uncheckedExceptionTypes notNil and: [uncheckedExceptionTypes at: index])
				ifTrue: [catchInits addPotentialInitializationsFrom: tryInits]."
				*/
				if (this.tryBlock.statements == null) {
					catchInfo.setReachMode(FlowInfo.UNREACHABLE);
				}
				catchInfo =
					this.catchBlocks[i].analyseCode(
						currentScope,
						catchContext,
						catchInfo);
				this.catchExits[i] = 
					(catchInfo.tagBits & FlowInfo.UNREACHABLE) != 0;
				tryInfo = tryInfo.mergedWith(catchInfo.unconditionalInits());
			}
		}
		if (this.subRoutineStartLabel == null) {
			this.mergedInitStateIndex =
				currentScope.methodScope().recordInitializationStates(tryInfo);
			return tryInfo;
		}


		// we also need to check potential multiple assignments of final variables inside the finally block
		// need to include potential inits from returns inside the try/catch parts - 1GK2AOF
		finallyContext/* NN null with subRoutineStartLabel, which returns */.complainOnDeferredChecks( 
			(tryInfo.tagBits & FlowInfo.UNREACHABLE) == 0 
				? flowInfo.unconditionalCopy().
					addPotentialInitializationsFrom(tryInfo).
						// lighten the influence of the try block, which may have 
						// exited at any point
					addPotentialInitializationsFrom(
						insideSubContext/* NN null with subRoutineStartLabel, which returns */.
							initsOnReturn)
				: insideSubContext.initsOnReturn,
			currentScope);
		if (subInfo == FlowInfo.DEAD_END) {
			this.mergedInitStateIndex =
				currentScope.methodScope().recordInitializationStates(subInfo);
			return subInfo;
		} else {
			FlowInfo mergedInfo = tryInfo.addInitializationsFrom(subInfo);
			this.mergedInitStateIndex =
				currentScope.methodScope().recordInitializationStates(mergedInfo);
			return mergedInfo;
		}
	}

	public boolean isSubRoutineEscaping() {

		return this.isSubRoutineEscaping;
	}

	/**
	 * Try statement code generation with or without jsr bytecode use
	 *	post 1.5 target level, cannot use jsr bytecode, must instead inline finally block
	 * returnAddress is only allocated if jsr is allowed
	 */
	public void generateCode(BlockScope currentScope, CodeStream codeStream) {
		if ((this.bits & ASTNode.IsReachable) == 0) {
			return;
		}
		// in case the labels needs to be reinitialized
		// when the code generation is restarted in wide mode
		if (this.anyExceptionLabelsCount > 0) {
			this.anyExceptionLabels = SubRoutineStatement.NO_EXCEPTION_HANDLER;
			this.anyExceptionLabelsCount = 0;
		}
		int pc = codeStream.position;
		final int NO_FINALLY = 0;									// no finally block
		final int FINALLY_SUBROUTINE = 1; 					// finally is generated as a subroutine (using jsr/ret bytecodes)
		final int FINALLY_DOES_NOT_COMPLETE = 2;	// non returning finally is optimized with only one instance of finally block
		final int FINALLY_MUST_BE_INLINED = 3;			// finally block must be inlined since cannot use jsr/ret bytecodes >1.5
		int finallyMode;
		if (this.subRoutineStartLabel == null) { 
			finallyMode = NO_FINALLY;
		} else {
			if (this.isSubRoutineEscaping) {
				finallyMode = FINALLY_DOES_NOT_COMPLETE;
			} else if (this.scope.compilerOptions().inlineJsrBytecode) {
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
				this.catchArguments == null ? 0 : this.catchArguments.length];
		for (int i = 0; i < maxCatches; i++) {
			exceptionLabels[i] = new ExceptionLabel(codeStream, this.catchArguments[i].binding.type);
		}
		if (this.subRoutineStartLabel != null) {
			this.subRoutineStartLabel.initialize(codeStream);
			this.enterAnyExceptionHandler(codeStream);
		}
		// generate the try block
		this.tryBlock.generateCode(this.scope, codeStream);
		boolean tryBlockHasSomeCode = codeStream.position != pc;
		// flag telling if some bytecodes were issued inside the try block

		// place end positions of user-defined exception labels
		if (tryBlockHasSomeCode) {
			// natural exit may require subroutine invocation (if finally != null)
			Label naturalExitLabel = new Label(codeStream);
			if (!this.tryBlockExit) {
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
						codeStream.goto_(this.subRoutineStartLabel);
						break;
				}
				codeStream.updateLastRecordedEndPC(this.tryBlock.scope, position);
				//goto is tagged as part of the try block
			}
			for (int i = 0; i < maxCatches; i++) {
				exceptionLabels[i].placeEnd();
			}
			/* generate sequence of handler, all starting by storing the TOS (exception
			thrown) into their own catch variables, the one specified in the source
			that must denote the handled exception.
			*/
			if (this.catchArguments != null) {
				for (int i = 0; i < maxCatches; i++) {
					// May loose some local variable initializations : affecting the local variable attributes
					if (this.preTryInitStateIndex != -1) {
						codeStream.removeNotDefinitelyAssignedVariables(currentScope, this.preTryInitStateIndex);
					}
					codeStream.pushOnStack(exceptionLabels[i].exceptionType);
					exceptionLabels[i].place();
					// optimizing the case where the exception variable is not actually used
					LocalVariableBinding catchVar;
					int varPC = codeStream.position;
					if ((catchVar = this.catchArguments[i].binding).resolvedPosition != -1) {
						codeStream.store(catchVar, false);
						catchVar.recordInitializationStartPC(codeStream.position);
						codeStream.addVisibleLocalVariable(catchVar);
					} else {
						codeStream.pop();
					}
					codeStream.recordPositionsFrom(varPC, this.catchArguments[i].sourceStart);
					// Keep track of the pcs at diverging point for computing the local attribute
					// since not passing the catchScope, the block generation will exitUserScope(catchScope)
					this.catchBlocks[i].generateCode(this.scope, codeStream);
					if (!this.catchExits[i]) {
						switch(finallyMode) {
							case FINALLY_SUBROUTINE :
							case FINALLY_MUST_BE_INLINED :
								requiresNaturalExit = true;
								// fall through
							case NO_FINALLY :
								codeStream.goto_(naturalExitLabel);
								break;
							case FINALLY_DOES_NOT_COMPLETE :
								codeStream.goto_(this.subRoutineStartLabel);
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
			if (this.subRoutineStartLabel != null) {
				codeStream.pushOnStack(this.scope.getJavaLangThrowable());
				if (this.preTryInitStateIndex != -1) {
					// reset initialization state, as for a normal catch block
					codeStream.removeNotDefinitelyAssignedVariables(currentScope, this.preTryInitStateIndex);
				}
				this.placeAllAnyExceptionHandlers();
				if (naturalExitExceptionHandler != null) naturalExitExceptionHandler.place();
				

				switch(finallyMode) {
					case FINALLY_SUBROUTINE :
						codeStream.store(this.anyExceptionVariable, false);
						codeStream.jsr(this.subRoutineStartLabel);
						codeStream.recordPositionsFrom(finallySequenceStartPC, this.finallyBlock.sourceStart);
						int position = codeStream.position;						
						codeStream.throwAnyException(this.anyExceptionVariable);
						codeStream.recordPositionsFrom(position, this.finallyBlock.sourceEnd);
						this.subRoutineStartLabel.place();
						codeStream.pushOnStack(this.scope.getJavaLangThrowable());
						position = codeStream.position;	
						codeStream.store(this.returnAddressVariable, false);
						codeStream.recordPositionsFrom(position, this.finallyBlock.sourceStart);
						this.finallyBlock.generateCode(this.scope, codeStream);
						position = codeStream.position;
						codeStream.ret(this.returnAddressVariable.resolvedPosition);
//						codeStream.updateLastRecordedEndPC(position);
						codeStream.recordPositionsFrom(
							position,
							this.finallyBlock.sourceEnd);
						// the ret bytecode is part of the subroutine
						break;
					case FINALLY_MUST_BE_INLINED :
						codeStream.store(this.anyExceptionVariable, false);
						codeStream.recordPositionsFrom(finallySequenceStartPC, this.finallyBlock.sourceStart);
						this.finallyBlock.generateCode(currentScope, codeStream);
						position = codeStream.position;
						codeStream.throwAnyException(this.anyExceptionVariable);
						this.subRoutineStartLabel.place();
						codeStream.recordPositionsFrom(position, this.finallyBlock.sourceEnd);
						break;
					case FINALLY_DOES_NOT_COMPLETE :
						codeStream.pop();
						this.subRoutineStartLabel.place();
						codeStream.recordPositionsFrom(finallySequenceStartPC, this.finallyBlock.sourceStart);
						this.finallyBlock.generateCode(this.scope, codeStream);
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
							codeStream.jsr(this.subRoutineStartLabel);
							naturalExitExceptionHandler.placeEnd();
							codeStream.recordPositionsFrom(
								position,
								this.finallyBlock.sourceEnd);	
							break;
						case FINALLY_MUST_BE_INLINED :
							// May loose some local variable initializations : affecting the local variable attributes
							// needed since any exception handler got inlined subroutine
							if (this.preTryInitStateIndex != -1) {
								codeStream.removeNotDefinitelyAssignedVariables(currentScope, this.preTryInitStateIndex);
							}
							// entire sequence for finally is associated to finally block
							this.finallyBlock.generateCode(this.scope, codeStream);
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
			if (this.subRoutineStartLabel != null) {
				this.finallyBlock.generateCode(this.scope, codeStream);
			}
		}
		// May loose some local variable initializations : affecting the local variable attributes
		if (this.mergedInitStateIndex != -1) {
			codeStream.removeNotDefinitelyAssignedVariables(currentScope, this.mergedInitStateIndex);
			codeStream.addDefinitelyAssignedVariables(currentScope, this.mergedInitStateIndex);
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
			if (currentScope.compilerOptions().inlineJsrBytecode) {
				// cannot use jsr bytecode, then simply inline the subroutine
				this.exitAnyExceptionHandler();				
				this.finallyBlock.generateCode(currentScope, codeStream);
				this.enterAnyExceptionHandler(codeStream);
			} else {
				// classic subroutine invocation, distinguish case of non-returning subroutine
				codeStream.jsr(this.subRoutineStartLabel);
			}
		}
	}

	public StringBuffer printStatement(int indent, StringBuffer output) {
		printIndent(indent, output).append("try \n"); //$NON-NLS-1$
		this.tryBlock.printStatement(indent + 1, output);

		//catches
		if (this.catchBlocks != null)
			for (int i = 0; i < this.catchBlocks.length; i++) {
					output.append('\n');
					printIndent(indent, output).append("catch ("); //$NON-NLS-1$
					this.catchArguments[i].print(0, output).append(") "); //$NON-NLS-1$
					this.catchBlocks[i].printStatement(indent + 1, output);
			}
		//finally
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

		BlockScope tryScope = new BlockScope(this.scope);
		BlockScope finallyScope = null;
		
		if (this.finallyBlock != null) {
			if (this.finallyBlock.isEmptyBlock()) {
				if ((this.finallyBlock.bits & ASTNode.UndocumentedEmptyBlock) != 0) {
					this.scope.problemReporter().undocumentedEmptyBlock(this.finallyBlock.sourceStart, this.finallyBlock.sourceEnd);
				}
			} else {
				finallyScope = new BlockScope(this.scope, false); // don't add it yet to parent scope
	
				// provision for returning and forcing the finally block to run
				MethodScope methodScope = this.scope.methodScope();
	
				// the type does not matter as long as it is not a base type
				if (!upperScope.compilerOptions().inlineJsrBytecode) {
					this.returnAddressVariable =
						new LocalVariableBinding(TryStatement.SecretReturnName, upperScope.getJavaLangObject(), ClassFileConstants.AccDefault, false);
					finallyScope.addLocalVariable(this.returnAddressVariable);
					this.returnAddressVariable.setConstant(Constant.NotAConstant); // not inlinable
				}
				this.subRoutineStartLabel = new Label();
	
				this.anyExceptionVariable =
					new LocalVariableBinding(TryStatement.SecretAnyHandlerName, this.scope.getJavaLangThrowable(), ClassFileConstants.AccDefault, false);
				finallyScope.addLocalVariable(this.anyExceptionVariable);
				this.anyExceptionVariable.setConstant(Constant.NotAConstant); // not inlinable
	
				if (!methodScope.isInsideInitializer()) {
					MethodBinding methodBinding =
						((AbstractMethodDeclaration) methodScope.referenceContext).binding;
					if (methodBinding != null) {
						TypeBinding methodReturnType = methodBinding.returnType;
						if (methodReturnType.id != TypeIds.T_void) {
							this.secretReturnValue =
								new LocalVariableBinding(
									TryStatement.SecretLocalDeclarationName,
									methodReturnType,
									ClassFileConstants.AccDefault,
									false);
							finallyScope.addLocalVariable(this.secretReturnValue);
							this.secretReturnValue.setConstant(Constant.NotAConstant); // not inlinable
						}
					}
				}
				this.finallyBlock.resolveUsing(finallyScope);
				// force the finally scope to have variable positions shifted after its try scope and catch ones
				finallyScope.shiftScopes = new BlockScope[this.catchArguments == null ? 1 : this.catchArguments.length+1];
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
				if (finallyScope != null){
					finallyScope.shiftScopes[i+1] = catchScope;
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
						this.scope.problemReporter().wrongSequenceOfExceptionTypesError(this, this.caughtExceptionTypes[i], i, argumentTypes[j]);
					}
				}
			}
		} else {
			this.caughtExceptionTypes = new ReferenceBinding[0];
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
