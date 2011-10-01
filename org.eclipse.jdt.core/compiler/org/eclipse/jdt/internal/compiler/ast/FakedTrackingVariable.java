/*******************************************************************************
 * Copyright (c) 2011 GK Software AG and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Stephan Herrmann - initial API and implementation
 *******************************************************************************/
package org.eclipse.jdt.internal.compiler.ast;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

import org.eclipse.jdt.core.compiler.CharOperation;
import org.eclipse.jdt.internal.compiler.codegen.CodeStream;
import org.eclipse.jdt.internal.compiler.flow.FlowContext;
import org.eclipse.jdt.internal.compiler.flow.FlowInfo;
import org.eclipse.jdt.internal.compiler.impl.Constant;
import org.eclipse.jdt.internal.compiler.lookup.Binding;
import org.eclipse.jdt.internal.compiler.lookup.BlockScope;
import org.eclipse.jdt.internal.compiler.lookup.LocalVariableBinding;
import org.eclipse.jdt.internal.compiler.lookup.MethodScope;
import org.eclipse.jdt.internal.compiler.lookup.ReferenceBinding;
import org.eclipse.jdt.internal.compiler.lookup.Scope;
import org.eclipse.jdt.internal.compiler.lookup.TypeBinding;
import org.eclipse.jdt.internal.compiler.lookup.TypeConstants;
import org.eclipse.jdt.internal.compiler.lookup.TypeIds;
import org.eclipse.jdt.internal.compiler.problem.ProblemReporter;

/**
 * A faked local variable declaration used for keeping track of data flows of a
 * special variable. Certain events will be recorded by changing the null info
 * for this variable.
 * 
 * See bug 349326 - [1.7] new warning for missing try-with-resources
 */
public class FakedTrackingVariable extends LocalDeclaration {

	// a call to close() was seen at least on one path:
	private static final int CLOSE_SEEN = 1;
	// the resource was passed to outside code (arg in method/ctor call or as a return value from this method):
	private static final int PASSED_TO_OUTSIDE = 2;
	// If close() is invoked from a nested method (inside a local type) report remaining problems only as potential:
	private static final int CLOSED_IN_NESTED_METHOD = 4;
	// a location independent issue has been reported already against this resource:
	private static final int REPORTED = 8;
	
	/**
	 * Bitset of {@link #CLOSE_SEEN}, {@link #PASSED_TO_OUTSIDE}, {@link #CLOSED_IN_NESTED_METHOD} and {@link #REPORTED}.
	 */
	private int globalClosingState = 0;

	MethodScope methodScope; // designates the method declaring this variable
	
	public LocalVariableBinding originalBinding; // the real local being tracked
	
	HashMap recordedLocations; // initially null, ASTNode -> Integer 


	public FakedTrackingVariable(LocalVariableBinding original, Statement location) {
		super(original.name, location.sourceStart, location.sourceEnd);
		this.type = new SingleTypeReference(
				TypeConstants.OBJECT,
				((long)this.sourceStart <<32)+this.sourceEnd);
		this.methodScope = original.declaringScope.methodScope();
		this.originalBinding = original;
		resolve(original.declaringScope);
	}
	
	public void generateCode(BlockScope currentScope, CodeStream codeStream)
	{ /* NOP - this variable is completely dummy, ie. for analysis only. */ }

	public void resolve (BlockScope scope) {
		// only need the binding, which is used as reference in FlowInfo methods.
		this.binding = new LocalVariableBinding(
				this.name,
				scope.getJavaLangObject(),  // dummy, just needs to be a reference type
				0,
				false);
		this.binding.setConstant(Constant.NotAConstant);
		this.binding.useFlag = LocalVariableBinding.USED;
		// use a free slot without assigning it:
		this.binding.id = scope.registerTrackingVariable(this);
	}

	/**
	 * If expression resolves to a local variable binding of type AutoCloseable,
	 * answer the variable that tracks closing of that local, creating it if needed.
	 * @param expression
	 * @return a new {@link FakedTrackingVariable} or null.
	 */
	public static FakedTrackingVariable getCloseTrackingVariable(Expression expression) {
		if (expression instanceof SingleNameReference) {
			SingleNameReference name = (SingleNameReference) expression;
			if (name.binding instanceof LocalVariableBinding) {
				LocalVariableBinding local = (LocalVariableBinding)name.binding;
				if (local.closeTracker != null)
					return local.closeTracker;
				if (local.isParameter() || !isAutoCloseable(expression.resolvedType))
					return null;
				// tracking var doesn't yet exist. This happens in finally block
				// which is analyzed before the corresponding try block
				Statement location = local.declaration;
				return local.closeTracker = new FakedTrackingVariable(local, location);
			}
		}
		return null;
	}

	/** if 'invocationSite' is a call to close() that has a registered tracking variable, answer that variable's binding. */
	public static LocalVariableBinding getTrackerForCloseCall(ASTNode invocationSite) {
		if (invocationSite instanceof MessageSend) {
			MessageSend send = (MessageSend) invocationSite;
			if (CharOperation.equals(TypeConstants.CLOSE, send.selector) && send.receiver instanceof SingleNameReference) {
				Binding receiverBinding = ((SingleNameReference)send.receiver).binding;
				if (receiverBinding instanceof LocalVariableBinding) {
					FakedTrackingVariable trackingVariable = ((LocalVariableBinding)receiverBinding).closeTracker;
					if (trackingVariable != null)
						return trackingVariable.binding;
				}
			}
		}
		return null;
	}

	/** 
	 * Check if the rhs of an assignment or local declaration is an (Auto)Closeable.
	 * If so create or re-use a tracking variable, and wire and initialize everything. 
	 */
	public static void handleResourceAssignment(FlowInfo flowInfo, Statement location, Expression rhs, LocalVariableBinding local,
				LocalVariableBinding previousTrackerBinding) 
	{
		if (isAutoCloseable(rhs.resolvedType)) {
			// new value is AutoCloseable, start tracking, possibly re-using existing tracker var:
	
			FakedTrackingVariable rhsTrackVar = getCloseTrackingVariable(rhs);
			if (rhsTrackVar != null) {								// 1. share tracking variable with RHS?
				local.closeTracker = rhsTrackVar;
				// keep null-status unchanged across this assignment
			} else if (previousTrackerBinding != null) {			// 2. re-use tracking variable from the LHS?
				// re-assigning from a fresh, mark as not-closed again:
				flowInfo.markAsDefinitelyNull(previousTrackerBinding);
			} else {												// 3. no re-use, create a fresh tracking variable:
				local.closeTracker = new FakedTrackingVariable(local, location);
				// a fresh resource, mark as not-closed:
				flowInfo.markAsDefinitelyNull(local.closeTracker.binding);
// TODO(stephan): this might be useful, but I could not find a test case for it: 
//				if (flowContext.initsOnFinally != null)
//					flowContext.initsOnFinally.markAsDefinitelyNonNull(trackerBinding);
			}
		}
	}

	/** Answer wither the given type binding is a subtype of java.lang.AutoCloseable. */
	public static boolean isAutoCloseable(TypeBinding typeBinding) {
		return typeBinding instanceof ReferenceBinding
			&& ((ReferenceBinding)typeBinding).hasTypeBit(TypeIds.BitAutoCloseable|TypeIds.BitCloseable);
	}

	/** Mark that this resource is closed locally. */
	public void markClose(FlowInfo flowInfo, FlowContext flowContext) {
		flowInfo.markAsDefinitelyNonNull(this.binding);
		this.globalClosingState |= CLOSE_SEEN;
//TODO(stephan): this might be useful, but I could not find a test case for it: 
//		if (flowContext.initsOnFinally != null)
//			flowContext.initsOnFinally.markAsDefinitelyNonNull(this.binding);
	}

	/** Mark that this resource is closed from a nested method (inside a local class). */
	public void markClosedInNestedMethod() {
		this.globalClosingState |= CLOSED_IN_NESTED_METHOD;
	}

	/** 
	 * Mark that this resource is passed to some outside code
	 * (as argument to a method/ctor call or as a return value from the current method), 
	 * and thus should be considered as potentially closed.
	 */
	public static FlowInfo markPassedToOutside(BlockScope scope, Expression expression, FlowInfo flowInfo) {
		FakedTrackingVariable trackVar = getCloseTrackingVariable(expression);
		if (trackVar != null) {
			trackVar.globalClosingState |= PASSED_TO_OUTSIDE;
			if (scope.methodScope() != trackVar.methodScope)
				trackVar.globalClosingState |= CLOSED_IN_NESTED_METHOD;
			// insert info that the tracked resource *may* be closed (by the target method, i.e.)
			FlowInfo infoResourceIsClosed = flowInfo.copy();
			infoResourceIsClosed.markAsDefinitelyNonNull(trackVar.binding);
			return FlowInfo.conditional(flowInfo, infoResourceIsClosed);
		}
		return flowInfo;
	}
	
	public void recordErrorLocation(ASTNode location, int nullStatus) {
		if (this.recordedLocations == null)
			this.recordedLocations = new HashMap();
		this.recordedLocations.put(location, new Integer(nullStatus));
	}

	public boolean reportRecordedErrors(Scope scope) {
		if (this.globalClosingState == 0) {
			reportError(scope.problemReporter(), null, FlowInfo.NULL);
			return true;
		}
		boolean hasReported = false;
		if (this.recordedLocations != null) {
			Iterator locations = this.recordedLocations.entrySet().iterator();
			while (locations.hasNext()) {
				Map.Entry entry = (Entry) locations.next();
				reportError(scope.problemReporter(), (ASTNode)entry.getKey(), ((Integer)entry.getValue()).intValue());
				hasReported = true;
			}
		}
		return hasReported;
	}
	
	public void reportError(ProblemReporter problemReporter, ASTNode location, int nullStatus) {
		if (nullStatus == FlowInfo.NULL) {
			if ((this.globalClosingState & CLOSED_IN_NESTED_METHOD) != 0)
				problemReporter.potentiallyUnclosedCloseable(this, location);
			else
				problemReporter.unclosedCloseable(this, location);
		} else if (nullStatus == FlowInfo.POTENTIALLY_NULL) {
			problemReporter.potentiallyUnclosedCloseable(this, location);
		}		
	}

	public void reportExplicitClosing(ProblemReporter problemReporter) {
		if ((this.globalClosingState & REPORTED) == 0) {
			this.globalClosingState |= REPORTED;
			problemReporter.explicitlyClosedAutoCloseable(this);
		}
	}
}
