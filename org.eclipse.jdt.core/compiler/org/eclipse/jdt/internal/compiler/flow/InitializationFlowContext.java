package org.eclipse.jdt.internal.compiler.flow;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
import org.eclipse.jdt.internal.compiler.*;
import org.eclipse.jdt.internal.compiler.ast.*;
import org.eclipse.jdt.internal.compiler.lookup.*;
import org.eclipse.jdt.internal.compiler.problem.*;
import org.eclipse.jdt.internal.compiler.util.*;

import java.util.*;

/**
 * Reflects the context of code analysis, keeping track of enclosing
 *	try statements, exception handlers, etc...
 */
public class InitializationFlowContext extends ExceptionHandlingFlowContext {
	public UnconditionalFlowInfo initsOnReturn;

	public int exceptionCount;
	public TypeBinding[] thrownExceptions = new TypeBinding[5];
	public AstNode[] exceptionThrowers = new AstNode[5];
	public InitializationFlowContext(
		FlowContext parent,
		AstNode associatedNode,
		BlockScope scope) {
		super(
			parent,
			associatedNode,
			new ReferenceBinding[] { scope.getJavaLangThrowable()},
		// tolerate any kind of exception, but record them
		scope, FlowInfo.DeadEnd);

		this.initsOnReturn = FlowInfo.DeadEnd;
	}

	public void checkInitializerExceptions(
		BlockScope currentScope,
		FlowContext initializerContext,
		FlowInfo flowInfo) {
		for (int i = 0; i < exceptionCount; i++) {
			initializerContext.checkExceptionHandlers(
				thrownExceptions[i],
				exceptionThrowers[i],
				flowInfo,
				currentScope);
		}
	}

	public void recordHandlingException(
		ReferenceBinding exceptionType,
		UnconditionalFlowInfo flowInfo,
		TypeBinding raisedException,
		AstNode invocationSite,
		boolean wasMasked) {

		int size = thrownExceptions.length;
		if (exceptionCount == size) {
			System.arraycopy(
				thrownExceptions,
				0,
				(thrownExceptions = new TypeBinding[size * 2]),
				0,
				size);
			System.arraycopy(
				exceptionThrowers,
				0,
				(exceptionThrowers = new AstNode[size * 2]),
				0,
				size);
		}
		thrownExceptions[exceptionCount] = raisedException;
		exceptionThrowers[exceptionCount++] = invocationSite;
	}

	public void recordReturnFrom(UnconditionalFlowInfo flowInfo) {

		// record initializations which were performed at the return point
		initsOnReturn = initsOnReturn.mergedWith(flowInfo);
	}

}
