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
package org.eclipse.jdt.internal.compiler.flow;

import java.util.ArrayList;

import org.eclipse.jdt.internal.compiler.ast.AbstractMethodDeclaration;
import org.eclipse.jdt.internal.compiler.ast.ASTNode;
import org.eclipse.jdt.internal.compiler.ast.TryStatement;
import org.eclipse.jdt.internal.compiler.codegen.ObjectCache;
import org.eclipse.jdt.internal.compiler.lookup.BlockScope;
import org.eclipse.jdt.internal.compiler.lookup.CompilerModifiers;
import org.eclipse.jdt.internal.compiler.lookup.MethodScope;
import org.eclipse.jdt.internal.compiler.lookup.ReferenceBinding;
import org.eclipse.jdt.internal.compiler.lookup.Scope;
import org.eclipse.jdt.internal.compiler.lookup.TypeBinding;

/**
 * Reflects the context of code analysis, keeping track of enclosing
 *	try statements, exception handlers, etc...
 */
public class ExceptionHandlingFlowContext extends FlowContext {
	
	public ReferenceBinding[] handledExceptions;
	
	public final static int BitCacheSize = 32; // 32 bits per int
	int[] isReached;
	int[] isNeeded;
	UnconditionalFlowInfo[] initsOnExceptions;
	ObjectCache indexes = new ObjectCache();
	boolean isMethodContext;

	public UnconditionalFlowInfo initsOnReturn;

	// for dealing with anonymous constructor thrown exceptions
	public ArrayList extendedExceptions;
	
	public ExceptionHandlingFlowContext(
		FlowContext parent,
		ASTNode associatedNode,
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
				this.initsOnExceptions[i] = FlowInfo.DEAD_END;
			}
		}
		System.arraycopy(this.isReached, 0, this.isNeeded, 0, cacheSize);
		this.initsOnReturn = FlowInfo.DEAD_END;	
	}

	public void complainIfUnusedExceptionHandlers(AbstractMethodDeclaration method) {
		MethodScope scope = method.scope;
		// can optionally skip overriding methods
		if ((method.binding.modifiers & (CompilerModifiers.AccOverriding | CompilerModifiers.AccImplementing)) != 0
		        && !scope.environment().options.reportUnusedDeclaredThrownExceptionWhenOverriding) {
		    return;
		}
		    
		// report errors for unreachable exception handlers
		for (int i = 0, count = handledExceptions.length; i < count; i++) {
			int index = indexes.get(handledExceptions[i]);
			int cacheIndex = index / BitCacheSize;
			int bitMask = 1 << (index % BitCacheSize);
			if ((isReached[cacheIndex] & bitMask) == 0) {
				scope.problemReporter().unusedDeclaredThrownException(
					handledExceptions[index],
					method,
					method.thrownExceptions[index]);
			}
		}
	}
	
	public void complainIfUnusedExceptionHandlers(
		BlockScope scope,
		TryStatement tryStatement) {
		// report errors for unreachable exception handlers
		for (int i = 0, count = handledExceptions.length; i < count; i++) {
			int index = indexes.get(handledExceptions[i]);
			int cacheIndex = index / BitCacheSize;
			int bitMask = 1 << (index % BitCacheSize);
			if ((isReached[cacheIndex] & bitMask) == 0) {
				scope.problemReporter().unreachableCatchBlock(
					handledExceptions[index],
					tryStatement.catchArguments[index].type);
			} else {
				if ((isNeeded[cacheIndex] & bitMask) == 0) {
					scope.problemReporter().hiddenCatchBlock(
						handledExceptions[index],
						tryStatement.catchArguments[index].type);
				}
			}
		}
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
		buffer.append("[initsOnReturn -").append(initsOnReturn.toString()).append(']'); //$NON-NLS-1$
		return buffer.toString();
	}

	public UnconditionalFlowInfo initsOnException(ReferenceBinding exceptionType) {
		
		int index;
		if ((index = indexes.get(exceptionType)) < 0) {
			return FlowInfo.DEAD_END;
		}
		return initsOnExceptions[index];
	}

	public UnconditionalFlowInfo initsOnReturn(){
		return this.initsOnReturn;
	}
	
	public void recordHandlingException(
		ReferenceBinding exceptionType,
		UnconditionalFlowInfo flowInfo,
		TypeBinding raisedException,
		ASTNode invocationSite,
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
			initsOnExceptions[index] == FlowInfo.DEAD_END
				? flowInfo.copy().unconditionalInits()
				: initsOnExceptions[index].mergedWith(flowInfo.copy().unconditionalInits());
	}
	
	public void recordReturnFrom(FlowInfo flowInfo) {

		if (!flowInfo.isReachable()) return; 
		if (initsOnReturn == FlowInfo.DEAD_END) {
			initsOnReturn = flowInfo.copy().unconditionalInits();
		} else {
			initsOnReturn = initsOnReturn.mergedWith(flowInfo.copy().unconditionalInits());
		}
	}
	
	/*
	 * Compute a merged list of unhandled exception types (keeping only the most generic ones).
	 * This is necessary to add synthetic thrown exceptions for anonymous type constructors (JLS 8.6).
	 */
	public void mergeUnhandledException(TypeBinding newException){
		
		if (this.extendedExceptions == null){
			this.extendedExceptions = new ArrayList(5);
			for (int i = 0; i < this.handledExceptions.length; i++){
				this.extendedExceptions.add(this.handledExceptions[i]);
			}
		}
		
		boolean isRedundant = false;
		
		for(int i = this.extendedExceptions.size()-1; i >= 0; i--){
			switch(Scope.compareTypes(newException, (TypeBinding)this.extendedExceptions.get(i))){
				case MoreGeneric :
					this.extendedExceptions.remove(i);
					break;
				case EqualOrMoreSpecific :
					isRedundant = true;
					break;
				case NotRelated :
					break;
			}
		}
		if (!isRedundant){
			this.extendedExceptions.add(newException);
		}
	}
}
