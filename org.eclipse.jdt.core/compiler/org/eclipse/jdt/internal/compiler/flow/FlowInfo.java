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

import org.eclipse.jdt.internal.compiler.lookup.FieldBinding;
import org.eclipse.jdt.internal.compiler.lookup.LocalVariableBinding;

public abstract class FlowInfo {

	public final static int REACHABLE = 0;
	public final static int UNREACHABLE = 1; 
	
	public static final UnconditionalFlowInfo DEAD_END; // Represents a dead branch status of initialization
	static {
		DEAD_END = new UnconditionalFlowInfo();
		DEAD_END.reachMode = UNREACHABLE;
	}
	abstract public FlowInfo addInitializationsFrom(FlowInfo otherInits);

	abstract public FlowInfo addPotentialInitializationsFrom(FlowInfo otherInits);

	public FlowInfo asNegatedCondition() {

		return this;
	}

	public static FlowInfo conditional(FlowInfo initsWhenTrue, FlowInfo initsWhenFalse){

		// if (initsWhenTrue.equals(initsWhenFalse)) return initsWhenTrue; -- could optimize if #equals is defined
		return new ConditionalFlowInfo(initsWhenTrue, initsWhenFalse);
	}

	abstract public FlowInfo copy();

	public static UnconditionalFlowInfo initial(int maxFieldCount) {
		UnconditionalFlowInfo info = new UnconditionalFlowInfo();
		info.maxFieldCount = maxFieldCount;
		return info;
	}

	abstract public FlowInfo initsWhenFalse();

	abstract public FlowInfo initsWhenTrue();

	/**
	 * Check status of definite assignment for a field.
	 */
	 abstract public boolean isDefinitelyAssigned(FieldBinding field);   

	/**
	 * Check status of definite assignment for a local.
	 */
	public abstract boolean isDefinitelyAssigned(LocalVariableBinding local);

	//abstract public int reachMode(); 

	/**
	 * Check status of potential assignment for a field.
	 */
	 abstract public boolean isPotentiallyAssigned(FieldBinding field);   

	/**
	 * Check status of potential assignment for a local variable.
	 */

	 abstract public boolean isPotentiallyAssigned(LocalVariableBinding field);   

	abstract public boolean isReachable();
	
	/**
	 * Record a field got definitely assigned.
	 */
	abstract public void markAsDefinitelyAssigned(FieldBinding field);

	/**
	 * Record a local got definitely assigned.
	 */
	abstract public void markAsDefinitelyAssigned(LocalVariableBinding local);

	/**
	 * Clear the initialization info for a field
	 */
	abstract public void markAsDefinitelyNotAssigned(FieldBinding field);

	/**
	 * Clear the initialization info for a local variable
	 */
	abstract public void markAsDefinitelyNotAssigned(LocalVariableBinding local);

	/**
	 * Merge branches using optimized boolean conditions
	 */
	public static FlowInfo mergedOptimizedBranches(FlowInfo initsWhenTrue, boolean isOptimizedTrue, FlowInfo initsWhenFalse, boolean isOptimizedFalse, boolean allowFakeDeadBranch) {
		FlowInfo mergedInfo;
		if (isOptimizedTrue){
			if (initsWhenTrue == FlowInfo.DEAD_END && allowFakeDeadBranch) {
				mergedInfo = initsWhenFalse.setReachMode(FlowInfo.UNREACHABLE);
			} else {
				mergedInfo = initsWhenTrue.addPotentialInitializationsFrom(initsWhenFalse);
			}

		} else if (isOptimizedFalse) {
			if (initsWhenFalse == FlowInfo.DEAD_END && allowFakeDeadBranch) {
				mergedInfo = initsWhenTrue.setReachMode(FlowInfo.UNREACHABLE);
			} else {
				mergedInfo = initsWhenFalse.addPotentialInitializationsFrom(initsWhenTrue);
			}

		} else {
			mergedInfo = initsWhenTrue.unconditionalInits().mergedWith(initsWhenFalse.unconditionalInits());
		}
		return mergedInfo;
	}
	
	abstract public int reachMode();

	abstract public FlowInfo setReachMode(int reachMode);

	/**
	 * Returns the receiver updated in the following way: <ul>
	 * <li> intersection of definitely assigned variables, 
	 * <li> union of potentially assigned variables.
	 * </ul>
	 */
	abstract public UnconditionalFlowInfo mergedWith(UnconditionalFlowInfo otherInits);

	public String toString(){

		if (this == DEAD_END){
			return "FlowInfo.DEAD_END"; //$NON-NLS-1$
		}
		return super.toString();
	}

	abstract public UnconditionalFlowInfo unconditionalInits();
}
