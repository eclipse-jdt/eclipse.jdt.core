package org.eclipse.jdt.internal.compiler.ast;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
import org.eclipse.jdt.internal.compiler.IAbstractSyntaxTreeVisitor;
import org.eclipse.jdt.internal.compiler.impl.*;
import org.eclipse.jdt.internal.compiler.codegen.*;
import org.eclipse.jdt.internal.compiler.flow.*;
import org.eclipse.jdt.internal.compiler.lookup.*;
import org.eclipse.jdt.internal.compiler.problem.*;

public class TryStatement extends Statement {
	public Block tryBlock;
	public Block[] catchBlocks;
	public Argument[] catchArguments;
	public Block finallyBlock;

	BlockScope scope;
	
	public boolean subRoutineCannotReturn = true; // should rename into subRoutineComplete to be set to false by default

	ReferenceBinding[] caughtExceptionTypes;
	boolean tryBlockExit;
	boolean[] catchExits;
	public int[] preserveExceptionHandler;
	
	Label subRoutineStartLabel;
	LocalVariableBinding anyExceptionVariable, returnAddressVariable;

	final static char[] SecretReturnName = " returnAddress"/*nonNLS*/.toCharArray() ;
	final static char[] SecretAnyHandlerName = " anyExceptionHandler"/*nonNLS*/.toCharArray();

	// for local variables table attributes
	int preTryInitStateIndex = -1;
	int mergedInitStateIndex = -1;
/**
 * TryStatement constructor comment.
 */
public TryStatement() {
	super();
}
public FlowInfo analyseCode(BlockScope currentScope, FlowContext flowContext, FlowInfo flowInfo) {

	// Consider the try block and catch block so as to compute the intersection of initializations and	
	// the minimum exit relative depth amongst all of them. Then consider the subroutine, and append its
	// initialization to the try/catch ones, if the subroutine completes normally. If the subroutine does not
	// complete, then only keep this result for the rest of the analysis

	// process the finally block (subroutine) - create a context for the subroutine

	preTryInitStateIndex = currentScope.methodScope().recordInitializationStates(flowInfo);
	
	if (anyExceptionVariable != null) {
		anyExceptionVariable.used = true;
	}
	if (returnAddressVariable != null) {
		returnAddressVariable.used = true;
	}
	FlowContext insideSubContext;
	FinallyFlowContext finallyContext;
	UnconditionalFlowInfo subInfo;
	if (subRoutineStartLabel == null) {
		insideSubContext = flowContext;
		finallyContext = null;
		subInfo = null;
	} else {
		insideSubContext = new InsideSubRoutineFlowContext(flowContext, this);
		subInfo = finallyBlock.analyseCode(
			currentScope,
			finallyContext = new FinallyFlowContext(flowContext, finallyBlock),
			flowInfo.copy()).
				unconditionalInits();
		if (!((subInfo == FlowInfo.DeadEnd) || subInfo.isFakeReachable())) {
			subRoutineCannotReturn = false;
		}
	}

	// process the try block in a context handling the local exceptions.
	ExceptionHandlingFlowContext handlingContext =
		new ExceptionHandlingFlowContext(insideSubContext, tryBlock, caughtExceptionTypes, scope, flowInfo.unconditionalInits());
	FlowInfo tryInfo;
	if (tryBlock.statements == null) {
		tryInfo = flowInfo;
		tryBlockExit = false;
	} else {
		tryInfo = tryBlock.analyseCode(currentScope, handlingContext, flowInfo.copy());
		tryBlockExit = (tryInfo == FlowInfo.DeadEnd) || tryInfo.isFakeReachable();
	}

	// check unreachable catch blocks
	handlingContext.complainIfUnusedExceptionHandlers(catchBlocks, scope, this);

	// process the catch blocks - computing the minimal exit depth amongst try/catch
	if (catchArguments != null) {
		int catchCount;
		catchExits = new boolean[catchCount = catchBlocks.length];
		for (int i = 0; i < catchCount; i++) {
			// keep track of the inits that could potentially have led to this exception handler (for final assignments diagnosis)
			///*
			FlowInfo catchInfo = flowInfo.copy().unconditionalInits()
									.addPotentialInitializationsFrom(handlingContext.initsOnException(caughtExceptionTypes[i]).unconditionalInits())
									.addPotentialInitializationsFrom(tryInfo.unconditionalInits());
			//*/
			// SMART ANALYSIS (see 1FBPLCY)
			//FlowInfo catchInfo = handlingContext.initsOnException(caughtExceptionTypes[i]);
			
			// catch var is always set
			catchInfo.markAsDefinitelyAssigned(catchArguments[i].binding);
			/*
			"If we are about to consider an unchecked exception handler, potential inits may have occured inside
			the try block that need to be detected , e.g. 
			try { x = 1; throwSomething();} catch(Exception e){ x = 2} "
			"(uncheckedExceptionTypes notNil and: [uncheckedExceptionTypes at: index])
			ifTrue: [catchInits addPotentialInitializationsFrom: tryInits]."
			*/
			if (tryBlock.statements == null) {
				catchInfo.markAsFakeReachable(true);
			}
			catchInfo = catchBlocks[i].analyseCode(currentScope, insideSubContext, catchInfo);
			catchExits[i] = ((catchInfo == FlowInfo.DeadEnd) || catchInfo.isFakeReachable());
			tryInfo = tryInfo.mergedWith(catchInfo.unconditionalInits());
		}
	}
	if (subRoutineStartLabel == null) {
		mergedInitStateIndex = currentScope.methodScope().recordInitializationStates(tryInfo);
		return tryInfo;
	}

	// we also need to check potential multiple assignments of final variables inside the finally block
	finallyContext.complainOnRedundantFinalAssignments(tryInfo, currentScope);
	if (subInfo == FlowInfo.DeadEnd) {
		mergedInitStateIndex = currentScope.methodScope().recordInitializationStates(subInfo);
		return subInfo;
	} else {
		FlowInfo mergedInfo = tryInfo.addInitializationsFrom(subInfo);
		mergedInitStateIndex = currentScope.methodScope().recordInitializationStates(mergedInfo);
		return mergedInfo;
	}
}
public boolean cannotReturn(){
	return subRoutineCannotReturn;
}
/**
 * Try statement code generation
 *
 */
public void generateCode(BlockScope currentScope, CodeStream codeStream) {
	if ((bits & IsReachableMASK) == 0) {
		return;
	}
	if (tryBlock.isEmptyBlock()) {
		if (subRoutineStartLabel != null) {
			// since not passing the finallyScope, the block generation will exitUserScope(finallyScope)
			finallyBlock.generateCode(scope, codeStream); 
		}
		// May loose some local variable initializations : affecting the local variable attributes
		if (mergedInitStateIndex != -1) {
			codeStream.removeNotDefinitelyAssignedVariables(currentScope, mergedInitStateIndex);
		}
		// no local bytecode produced so no need for position remembering
		return;
	}
	int pc = codeStream.position;
	Label endLabel = new Label(codeStream);
	boolean requiresNaturalJsr = false;

	// preparing exception labels
	int maxCatches;
	ExceptionLabel[] exceptionLabels = new ExceptionLabel[maxCatches = catchArguments == null ? 0 : catchArguments.length];
	for (int i = 0; i < maxCatches; i++) {
		boolean preserveCurrentHandler = 
			(preserveExceptionHandler[i / ExceptionHandlingFlowContext.BitCacheSize] & (1 << (i % ExceptionHandlingFlowContext.BitCacheSize))) != 0;
		if (preserveCurrentHandler) {
			exceptionLabels[i] = new ExceptionLabel(codeStream, (ReferenceBinding) catchArguments[i].binding.type);
		}
	}
	ExceptionLabel anyExceptionLabel = null;
	if (subRoutineStartLabel != null) {
		subRoutineStartLabel.codeStream = codeStream;
		anyExceptionLabel = new ExceptionLabel(codeStream, null);
	}
	// generate the try block
	tryBlock.generateCode(scope, codeStream);
	boolean tryBlockHasSomeCode = codeStream.position != pc; // flag telling if some bytecodes were issued inside the try block

	// natural exit: only if necessary
	boolean nonReturningSubRoutine = (subRoutineStartLabel != null) && subRoutineCannotReturn;
	if ((!tryBlockExit) && tryBlockHasSomeCode) {
		int position = codeStream.position;
		if (nonReturningSubRoutine) {
			codeStream.goto_(subRoutineStartLabel);
		} else {
			requiresNaturalJsr = true;
			codeStream.goto_(endLabel);
		}
		codeStream.updateLastRecordedEndPC(position); //goto is tagged as part of the try block
	}
	// place end positions of user-defined exception labels
	if (tryBlockHasSomeCode) {
		for (int i = 0; i < maxCatches; i++) {
			boolean preserveCurrentHandler = 
				(preserveExceptionHandler[i / ExceptionHandlingFlowContext.BitCacheSize] & (1 << (i % ExceptionHandlingFlowContext.BitCacheSize))) != 0;
			if (preserveCurrentHandler) {
				exceptionLabels[i].placeEnd();
			}
		}
		/* generate sequence of handler, all starting by storing the TOS (exception
		thrown) into their own catch variables, the one specified in the source
		that must denote the handled exception.
		*/
		if (catchArguments == null) {
			if (anyExceptionLabel != null) {
				anyExceptionLabel.placeEnd();
			}
		} else {
			for (int i = 0; i < maxCatches; i++) {
				boolean preserveCurrentHandler = 
					(preserveExceptionHandler[i / ExceptionHandlingFlowContext.BitCacheSize] & (1 << (i % ExceptionHandlingFlowContext.BitCacheSize))) != 0;
				if (preserveCurrentHandler) {
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
					codeStream.recordPositionsFrom(varPC, catchArguments[i]);
					// Keep track of the pcs at diverging point for computing the local attribute
					// since not passing the catchScope, the block generation will exitUserScope(catchScope)
					catchBlocks[i].generateCode(scope, codeStream);	
				}
				if (i == maxCatches - 1) {
					if (anyExceptionLabel != null) {
						anyExceptionLabel.placeEnd();
					}
					if (subRoutineStartLabel != null) {
						if (!catchExits[i] && preserveCurrentHandler) {
							requiresNaturalJsr = true;
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
		// addition of a special handler so as to ensure that any uncaught exception (or exception thrown
		// inside catch blocks) will run the finally block
		int finallySequenceStartPC = codeStream.position;
		if (subRoutineStartLabel != null) {
			// the additional handler is doing: jsr finallyBlock and rethrow TOS-exception
			anyExceptionLabel.place();

			if (preTryInitStateIndex != -1) { // reset initialization state, as for a normal catch block
				codeStream.removeNotDefinitelyAssignedVariables(currentScope, preTryInitStateIndex);
			}						

			codeStream.incrStackSize(1);
			if (nonReturningSubRoutine) {
				codeStream.pop(); // "if subroutine cannot return, no need to jsr/jump to subroutine since it will be entered in sequence
			} else {
				codeStream.store(anyExceptionVariable, false);
				codeStream.jsr(subRoutineStartLabel);
				codeStream.load(anyExceptionVariable);
				codeStream.athrow();
			}
		}
		// end of catch sequence, place label that will correspond to the finally block beginning, or end of statement
		endLabel.place();
		if (subRoutineStartLabel != null) {
			if (nonReturningSubRoutine) {
				requiresNaturalJsr = false;
			}
			Label veryEndLabel = new Label(codeStream);
			if (requiresNaturalJsr) {
				codeStream.jsr(subRoutineStartLabel);
				codeStream.goto_(veryEndLabel);
			}
			subRoutineStartLabel.place();
			if (!nonReturningSubRoutine) {
				codeStream.incrStackSize(1);
				codeStream.store(returnAddressVariable, false);
			}
			codeStream.recordPositionsFrom(finallySequenceStartPC, finallyBlock); // entire sequence for finally is associated to finally block
			finallyBlock.generateCode(scope, codeStream);
			if (!nonReturningSubRoutine) {
				int position = codeStream.position;
				codeStream.ret(returnAddressVariable.resolvedPosition);
				codeStream.updateLastRecordedEndPC(position); // the ret bytecode is part of the subroutine
			}
			if (requiresNaturalJsr) {
				veryEndLabel.place();
			}
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
	codeStream.recordPositionsFrom(pc, this);
}
public void resolve(BlockScope upperScope) {

	// special scope for secret locals optimization.	
	scope = new BlockScope(upperScope);
	if (finallyBlock != null && finallyBlock.statements != null) { // provision for returning and forcing the finally block to run
		returnAddressVariable = new LocalVariableBinding(SecretReturnName, upperScope.getJavaLangObject(), 0); // the type does not matter as long as its not a normal base type
		scope.addLocalVariable(returnAddressVariable);
		returnAddressVariable.constant = NotAConstant; // not inlinable
		subRoutineStartLabel = new Label();

		BlockScope finallyScope = new BlockScope(scope);
		anyExceptionVariable = new LocalVariableBinding(SecretAnyHandlerName, scope.getJavaLangThrowable(), 0);
		finallyScope.addLocalVariable(anyExceptionVariable);
		anyExceptionVariable.constant = NotAConstant; // not inlinable
		finallyBlock.resolveUsing(finallyScope);
	}
	tryBlock.resolve(scope);

	// arguments type are checked against JavaLangThrowable in resolveForCatch(..)
	if (catchBlocks != null) {
		int length = catchArguments.length;
		TypeBinding[] argumentTypes = new TypeBinding[length];
		for (int i = 0; i < length; i++) {
			BlockScope catchScope = new BlockScope(scope);
			// side effect on catchScope in resolveForCatch(..)
			if ((argumentTypes[i] = catchArguments[i].resolveForCatch(catchScope)) == null)
				return;
			catchBlocks[i].resolveUsing(catchScope);
		}

		// Verify that the catch clause are ordered in the right way:
		// more specialized first.
		caughtExceptionTypes = new ReferenceBinding[length];
		for (int i = 0; i < length; i++) {
			caughtExceptionTypes[i] = (ReferenceBinding) argumentTypes[i];
			for (int j = 0; j < i; j++) {
				if (scope.areTypesCompatible(caughtExceptionTypes[i], argumentTypes[j])) {
					scope.problemReporter().wrongSequenceOfExceptionTypesError(this, i, j);
					return;
				}
			}
		}
	} else {
		caughtExceptionTypes = new ReferenceBinding[0];
	}
}
public String toString(int tab){
	/* slow code */

	String s = tabString(tab) ;
	//try
	s = s + "try "/*nonNLS*/ ;
	if (tryBlock == Block.None)
		s =s + "{}"/*nonNLS*/ ;
	else
		s = s + "\n"/*nonNLS*/ + tryBlock.toString(tab+1) ;
	
	//catches
	if (catchBlocks != null)
		for (int i = 0; i < catchBlocks.length ; i++)
			s = s 	+ "\n"/*nonNLS*/ + tabString(tab) + "catch ("/*nonNLS*/ 
					+ catchArguments[i].toString(0) + ") "/*nonNLS*/
					+ catchBlocks[i].toString(tab+1) ;
	//finally
	if (finallyBlock != null)
	{	if (finallyBlock == Block.None) 
			s = s + "\n"/*nonNLS*/ + tabString(tab)+ "finally {}"/*nonNLS*/ ;
		else
			s = s + "\n"/*nonNLS*/ + tabString(tab)+ "finally\n"/*nonNLS*/ + 
				finallyBlock.toString(tab+1) ;}
	
	return s ;}
public void traverse(IAbstractSyntaxTreeVisitor visitor, BlockScope blockScope) {
	if (visitor.visit(this, blockScope)) {
		tryBlock.traverse(visitor, scope);
		if (catchArguments != null) {
			for (int i = 0, max = catchBlocks.length; i < max; i++) {
				catchArguments[i].traverse(visitor, scope);
				catchBlocks[i].traverse(visitor, scope);
			}
		}
		if (finallyBlock != null) finallyBlock.traverse(visitor, scope);
	}
	visitor.endVisit(this, blockScope);
}
}
