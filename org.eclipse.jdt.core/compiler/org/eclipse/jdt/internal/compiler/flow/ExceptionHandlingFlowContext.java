package org.eclipse.jdt.internal.compiler.flow;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
import org.eclipse.jdt.internal.compiler.*;
import org.eclipse.jdt.internal.compiler.ast.*;
import org.eclipse.jdt.internal.compiler.lookup.*;
import org.eclipse.jdt.internal.compiler.problem.*;
import org.eclipse.jdt.internal.compiler.codegen.*;

/**
 * Reflects the context of code analysis, keeping track of enclosing
 *	try statements, exception handlers, etc...
 */
public class ExceptionHandlingFlowContext extends FlowContext {
	
	ReferenceBinding[] handledExceptions;
	public final static int BitCacheSize = 32; // 32 bits per int
	int[] isReached;
	int[] isNeeded;
	UnconditionalFlowInfo[] initsOnExceptions;
	ObjectCache indexes = new ObjectCache();
	boolean isMethodContext;

	public UnconditionalFlowInfo initsOnReturn;
	
	public ExceptionHandlingFlowContext(
		FlowContext parent,
		AstNode associatedNode,
		ReferenceBinding[] handledExceptions,
		BlockScope scope,
		UnconditionalFlowInfo flowInfo) {

		super(parent, associatedNode);
		isMethodContext = scope == scope.methodScope();
		this.handledExceptions = handledExceptions;
		int count = handledExceptions.length, cacheSize = (count / BitCacheSize) + 1;
		this.isReached = new int[cacheSize]; // none is reached by default
		this.isNeeded = new int[cacheSize]; // none is needed by default
		this.initsOnExceptions = new UnconditionalFlowInfo[count];
		for (int i = 0; i < count; i++) {
			this.indexes.put(handledExceptions[i], i); // key type  -> value index
			boolean isUnchecked =
				(scope.compareUncheckedException(handledExceptions[i]) != NotRelated);
			int cacheIndex = i / BitCacheSize, bitMask = 1 << (i % BitCacheSize);
			if (isUnchecked) {
				isReached[cacheIndex] |= bitMask;
				this.initsOnExceptions[i] = flowInfo.copy().unconditionalInits();
			} else {
				this.initsOnExceptions[i] = FlowInfo.DeadEnd;
			}
		}
		System.arraycopy(this.isReached, 0, this.isNeeded, 0, cacheSize);
		this.initsOnReturn = FlowInfo.DeadEnd;	
	}

	public void complainIfUnusedExceptionHandlers(
		AstNode[] exceptionHandlers,
		BlockScope scope,
		TryStatement tryStatement) {
		// report errors for unreachable exception handlers
		for (int i = 0, count = handledExceptions.length; i < count; i++) {
			int index = indexes.get(handledExceptions[i]);
			int cacheIndex = index / BitCacheSize;
			int bitMask = 1 << (index % BitCacheSize);
			if ((isReached[cacheIndex] & bitMask) == 0) {
				scope.problemReporter().unreachableExceptionHandler(
					handledExceptions[index],
					exceptionHandlers[index]);
			} else {
				if ((isNeeded[cacheIndex] & bitMask) == 0) {
					scope.problemReporter().maskedExceptionHandler(
						handledExceptions[index],
						exceptionHandlers[index]);
				}
			}
		}
		// will optimized out unnecessary catch block during code gen
		tryStatement.preserveExceptionHandler = isNeeded;
	}

	public String individualToString() {
		StringBuffer buffer = new StringBuffer("Exception flow context"); //$NON-NLS-1$
		int length = handledExceptions.length;
		for (int i = 0; i < length; i++) {
			int cacheIndex = i / BitCacheSize;
			int bitMask = 1 << (i % BitCacheSize);
			buffer.append('[').append(handledExceptions[i].readableName());
			if ((isReached[cacheIndex] & bitMask) != 0) {
				if ((isNeeded[cacheIndex] & bitMask) == 0) {
					buffer.append("-masked"); //$NON-NLS-1$
				} else {
					buffer.append("-reached"); //$NON-NLS-1$
				}
			} else {
				buffer.append("-not reached"); //$NON-NLS-1$
			}
			buffer.append('-').append(initsOnExceptions[i].toString()).append(']');
		}
		return buffer.toString();
	}

	public UnconditionalFlowInfo initsOnException(ReferenceBinding exceptionType) {
		int index;
		if ((index = indexes.get(exceptionType)) < 0) {
			return FlowInfo.DeadEnd;
		}
		return initsOnExceptions[index];
	}

	public void recordHandlingException(
		ReferenceBinding exceptionType,
		UnconditionalFlowInfo flowInfo,
		TypeBinding raisedException,
		AstNode invocationSite,
		boolean wasAlreadyDefinitelyCaught) {
		int index = indexes.get(exceptionType);
		// if already flagged as being reached (unchecked exception handler)
		int cacheIndex = index / BitCacheSize;
		int bitMask = 1 << (index % BitCacheSize);
		if (!wasAlreadyDefinitelyCaught) {
			this.isNeeded[cacheIndex] |= bitMask;
		}
		this.isReached[cacheIndex] |= bitMask;
		initsOnExceptions[index] =
			initsOnExceptions[index] == FlowInfo.DeadEnd
				? flowInfo.copy().unconditionalInits()
				: initsOnExceptions[index].mergedWith(flowInfo);
	}
	
	public void recordReturnFrom(UnconditionalFlowInfo flowInfo) {
		// record initializations which were performed at the return point
		initsOnReturn = initsOnReturn.mergedWith(flowInfo);
	}
}