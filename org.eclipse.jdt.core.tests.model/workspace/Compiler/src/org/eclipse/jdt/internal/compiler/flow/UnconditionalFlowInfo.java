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
import org.eclipse.jdt.internal.compiler.lookup.ReferenceBinding;

/**
 * Record initialization status during definite assignment analysis
 *
 * No caching of pre-allocated instances.
 */
public class UnconditionalFlowInfo extends FlowInfo {

	
	public long definiteInits;
	public long potentialInits;
	public long extraDefiniteInits[];
	public long extraPotentialInits[];
	
	public int reachMode; // by default

	public int maxFieldCount;
	
	// Constants
	public static final int BitCacheSize = 64; // 64 bits in a long.

	UnconditionalFlowInfo() {
		this.reachMode = REACHABLE;
	}

	// unions of both sets of initialization - used for try/finally
	public FlowInfo addInitializationsFrom(FlowInfo inits) {

		if (this == DEAD_END)
			return this;

		UnconditionalFlowInfo otherInits = inits.unconditionalInits();	
		if (otherInits == DEAD_END)
			return this;
			
		// union of definitely assigned variables, 
		definiteInits |= otherInits.definiteInits;
		// union of potentially set ones
		potentialInits |= otherInits.potentialInits;
	
		// treating extra storage
		if (extraDefiniteInits != null) {
			if (otherInits.extraDefiniteInits != null) {
				// both sides have extra storage
				int i = 0, length, otherLength;
				if ((length = extraDefiniteInits.length) < (otherLength = otherInits.extraDefiniteInits.length)) {
					// current storage is shorter -> grow current (could maybe reuse otherInits extra storage?)
					System.arraycopy(extraDefiniteInits, 0, (extraDefiniteInits = new long[otherLength]), 0, length);
					System.arraycopy(extraPotentialInits, 0, (extraPotentialInits = new long[otherLength]), 0, length);
					while (i < length) {
						extraDefiniteInits[i] |= otherInits.extraDefiniteInits[i];
						extraPotentialInits[i] |= otherInits.extraPotentialInits[i++];
					}
					while (i < otherLength) {
						extraPotentialInits[i] = otherInits.extraPotentialInits[i++];
					}
				} else {
					// current storage is longer
					while (i < otherLength) {
						extraDefiniteInits[i] |= otherInits.extraDefiniteInits[i];
						extraPotentialInits[i] |= otherInits.extraPotentialInits[i++];
					}
					while (i < length)
						extraDefiniteInits[i++] = 0;
				}
			} else {
				// no extra storage on otherInits
			}
		} else
			if (otherInits.extraDefiniteInits != null) {
				// no storage here, but other has extra storage.
				int otherLength;
				System.arraycopy(otherInits.extraDefiniteInits, 0, (extraDefiniteInits = new long[otherLength = otherInits.extraDefiniteInits.length]), 0, otherLength);			
				System.arraycopy(otherInits.extraPotentialInits, 0, (extraPotentialInits = new long[otherLength]), 0, otherLength);
			}
		return this;
	}

	// unions of both sets of initialization - used for try/finally
	public FlowInfo addPotentialInitializationsFrom(FlowInfo inits) {
	
		if (this == DEAD_END){
			return this;
		}

		UnconditionalFlowInfo otherInits = inits.unconditionalInits();
		if (otherInits == DEAD_END){
			return this;
		}
		// union of potentially set ones
		potentialInits |= otherInits.potentialInits;
	
		// treating extra storage
		if (extraDefiniteInits != null) {
			if (otherInits.extraDefiniteInits != null) {
				// both sides have extra storage
				int i = 0, length, otherLength;
				if ((length = extraDefiniteInits.length) < (otherLength = otherInits.extraDefiniteInits.length)) {
					// current storage is shorter -> grow current (could maybe reuse otherInits extra storage?)
					System.arraycopy(extraDefiniteInits, 0, (extraDefiniteInits = new long[otherLength]), 0, length);
					System.arraycopy(extraPotentialInits, 0, (extraPotentialInits = new long[otherLength]), 0, length);
					while (i < length) {
						extraPotentialInits[i] |= otherInits.extraPotentialInits[i++];
					}
					while (i < otherLength) {
						extraPotentialInits[i] = otherInits.extraPotentialInits[i++];
					}
				} else {
					// current storage is longer
					while (i < otherLength) {
						extraPotentialInits[i] |= otherInits.extraPotentialInits[i++];
					}
				}
			}
		} else
			if (otherInits.extraDefiniteInits != null) {
				// no storage here, but other has extra storage.
				int otherLength;
				extraDefiniteInits = new long[otherLength = otherInits.extraDefiniteInits.length];			
				System.arraycopy(otherInits.extraPotentialInits, 0, (extraPotentialInits = new long[otherLength]), 0, otherLength);
			}
		return this;
	}

	/**
	 * Answers a copy of the current instance
	 */
	public FlowInfo copy() {
		
		// do not clone the DeadEnd
		if (this == DEAD_END)
			return this;
	
		// look for an unused preallocated object
		UnconditionalFlowInfo copy = new UnconditionalFlowInfo();
	
		// copy slots
		copy.definiteInits = this.definiteInits;
		copy.potentialInits = this.potentialInits;
		copy.reachMode = this.reachMode;
		copy.maxFieldCount = this.maxFieldCount;
		
		if (this.extraDefiniteInits != null) {
			int length;
			System.arraycopy(this.extraDefiniteInits, 0, (copy.extraDefiniteInits = new long[ (length = extraDefiniteInits.length)]), 0, length);
			System.arraycopy(this.extraPotentialInits, 0, (copy.extraPotentialInits = new long[length]), 0, length);
		}
		return copy;
	}
	
	public UnconditionalFlowInfo discardFieldInitializations(){
		
		int limit = this.maxFieldCount;
		
		if (limit < BitCacheSize) {
			long mask = (1L << limit)-1;
			this.definiteInits &= ~mask;
			this.potentialInits &= ~mask;
			return this;
		} 

		this.definiteInits = 0;
		this.potentialInits = 0;

		// use extra vector
		if (extraDefiniteInits == null) {
			return this; // if vector not yet allocated, then not initialized
		}
		int vectorIndex, length = this.extraDefiniteInits.length;
		if ((vectorIndex = (limit / BitCacheSize) - 1) >= length) {
			return this; // not enough room yet
		}
		for (int i = 0; i < vectorIndex; i++) {
			this.extraDefiniteInits[i] = 0L;
			this.extraPotentialInits[i] = 0L;
		}
		long mask = (1L << (limit % BitCacheSize))-1;
		this.extraDefiniteInits[vectorIndex] &= ~mask;
		this.extraPotentialInits[vectorIndex] &= ~mask;
		return this;
	}

	public UnconditionalFlowInfo discardNonFieldInitializations(){
		
		int limit = this.maxFieldCount;
		
		if (limit < BitCacheSize) {
			long mask = (1L << limit)-1;
			this.definiteInits &= mask;
			this.potentialInits &= mask;
			return this;
		} 
		// use extra vector
		if (extraDefiniteInits == null) {
			return this; // if vector not yet allocated, then not initialized
		}
		int vectorIndex, length = this.extraDefiniteInits.length;
		if ((vectorIndex = (limit / BitCacheSize) - 1) >= length) {
			return this; // not enough room yet
		}
		long mask = (1L << (limit % BitCacheSize))-1;
		this.extraDefiniteInits[vectorIndex] &= mask;
		this.extraPotentialInits[vectorIndex] &= mask;
		for (int i = vectorIndex+1; i < length; i++) {
			this.extraDefiniteInits[i] = 0L;
			this.extraPotentialInits[i] = 0L;
		}
		return this;
	}
	
	public FlowInfo initsWhenFalse() {
		
		return this;
	}
	
	public FlowInfo initsWhenTrue() {
		
		return this;
	}
	
	/**
	 * Check status of definite assignment at a given position.
	 * It deals with the dual representation of the InitializationInfo2:
	 * bits for the first 64 entries, then an array of booleans.
	 */
	final private boolean isDefinitelyAssigned(int position) {
		
		// Dependant of CodeStream.isDefinitelyAssigned(..)
		// id is zero-based
		if (position < BitCacheSize) {
			return (definiteInits & (1L << position)) != 0; // use bits
		}
		// use extra vector
		if (extraDefiniteInits == null)
			return false; // if vector not yet allocated, then not initialized
		int vectorIndex;
		if ((vectorIndex = (position / BitCacheSize) - 1) >= extraDefiniteInits.length)
			return false; // if not enough room in vector, then not initialized 
		return ((extraDefiniteInits[vectorIndex]) & (1L << (position % BitCacheSize))) != 0;
	}
	
	/**
	 * Check status of definite assignment for a field.
	 */
	final public boolean isDefinitelyAssigned(FieldBinding field) {
		
		// Dependant of CodeStream.isDefinitelyAssigned(..)
		// We do not want to complain in unreachable code
		if ((this.reachMode & UNREACHABLE) != 0)  
			return true;
		return isDefinitelyAssigned(field.id); 
	}
	
	/**
	 * Check status of definite assignment for a local.
	 */
	final public boolean isDefinitelyAssigned(LocalVariableBinding local) {
		
		// Dependant of CodeStream.isDefinitelyAssigned(..)
		// We do not want to complain in unreachable code
		if ((this.reachMode & UNREACHABLE) != 0)
			return true;
		if (local.isArgument) {
			return true;
		}
		// final constants are inlined, and thus considered as always initialized
		if (local.isConstantValue()) {
			return true;
		}
		return isDefinitelyAssigned(local.id + maxFieldCount);
	}
	
	public boolean isReachable() {
		
		return this.reachMode == REACHABLE;
	}
	
	/**
	 * Check status of potential assignment at a given position.
	 * It deals with the dual representation of the InitializationInfo3:
	 * bits for the first 64 entries, then an array of booleans.
	 */
	final private boolean isPotentiallyAssigned(int position) {
		
		// id is zero-based
		if (position < BitCacheSize) {
			// use bits
			return (potentialInits & (1L << position)) != 0;
		}
		// use extra vector
		if (extraPotentialInits == null)
			return false; // if vector not yet allocated, then not initialized
		int vectorIndex;
		if ((vectorIndex = (position / BitCacheSize) - 1) >= extraPotentialInits.length)
			return false; // if not enough room in vector, then not initialized 
		return ((extraPotentialInits[vectorIndex]) & (1L << (position % BitCacheSize))) != 0;
	}
	
	/**
	 * Check status of definite assignment for a field.
	 */
	final public boolean isPotentiallyAssigned(FieldBinding field) {
		
		return isPotentiallyAssigned(field.id); 
	}
	
	/**
	 * Check status of potential assignment for a local.
	 */
	final public boolean isPotentiallyAssigned(LocalVariableBinding local) {
		
		if (local.isArgument) {
			return true;
		}
		// final constants are inlined, and thus considered as always initialized
		if (local.isConstantValue()) {
			return true;
		}
		return isPotentiallyAssigned(local.id + maxFieldCount);
	}
	
	/**
	 * Record a definite assignment at a given position.
	 * It deals with the dual representation of the InitializationInfo2:
	 * bits for the first 64 entries, then an array of booleans.
	 */
	final private void markAsDefinitelyAssigned(int position) {
		
		if (this != DEAD_END) {
	
			// position is zero-based
			if (position < BitCacheSize) {
				// use bits
				long mask;
				definiteInits |= (mask = 1L << position);
				potentialInits |= mask;
			} else {
				// use extra vector
				int vectorIndex = (position / BitCacheSize) - 1;
				if (extraDefiniteInits == null) {
					int length;
					extraDefiniteInits = new long[length = vectorIndex + 1];
					extraPotentialInits = new long[length];
				} else {
					int oldLength; // might need to grow the arrays
					if (vectorIndex >= (oldLength = extraDefiniteInits.length)) {
						System.arraycopy(extraDefiniteInits, 0, (extraDefiniteInits = new long[vectorIndex + 1]), 0, oldLength);
						System.arraycopy(extraPotentialInits, 0, (extraPotentialInits = new long[vectorIndex + 1]), 0, oldLength);
					}
				}
				long mask;
				extraDefiniteInits[vectorIndex] |= (mask = 1L << (position % BitCacheSize));
				extraPotentialInits[vectorIndex] |= mask;
			}
		}
	}
	
	/**
	 * Record a field got definitely assigned.
	 */
	public void markAsDefinitelyAssigned(FieldBinding field) {
		if (this != DEAD_END)
			markAsDefinitelyAssigned(field.id);
	}
	
	/**
	 * Record a local got definitely assigned.
	 */
	public void markAsDefinitelyAssigned(LocalVariableBinding local) {
		if (this != DEAD_END)
			markAsDefinitelyAssigned(local.id + maxFieldCount);
	}
	
	/**
	 * Clear initialization information at a given position.
	 * It deals with the dual representation of the InitializationInfo2:
	 * bits for the first 64 entries, then an array of booleans.
	 */
	final private void markAsDefinitelyNotAssigned(int position) {
		if (this != DEAD_END) {
	
			// position is zero-based
			if (position < BitCacheSize) {
				// use bits
				long mask;
				definiteInits &= ~(mask = 1L << position);
				potentialInits &= ~mask;
			} else {
				// use extra vector
				int vectorIndex = (position / BitCacheSize) - 1;
				if (extraDefiniteInits == null) {
					return; // nothing to do, it was not yet set 
				}
				// might need to grow the arrays
				if (vectorIndex >= extraDefiniteInits.length) {
					return; // nothing to do, it was not yet set 
				}
				long mask;
				extraDefiniteInits[vectorIndex] &= ~(mask = 1L << (position % BitCacheSize));
				extraPotentialInits[vectorIndex] &= ~mask;
			}
		}
	}
	
	/**
	 * Clear the initialization info for a field
	 */
	public void markAsDefinitelyNotAssigned(FieldBinding field) {
		
		if (this != DEAD_END)
			markAsDefinitelyNotAssigned(field.id);
	}
	
	/**
	 * Clear the initialization info for a local variable
	 */
	
	public void markAsDefinitelyNotAssigned(LocalVariableBinding local) {
		
		if (this != DEAD_END)
			markAsDefinitelyNotAssigned(local.id + maxFieldCount);
	}
		
	/**
	 * Returns the receiver updated in the following way: <ul>
	 * <li> intersection of definitely assigned variables, 
	 * <li> union of potentially assigned variables.
	 * </ul>
	 */
	public UnconditionalFlowInfo mergedWith(UnconditionalFlowInfo otherInits) {
	
		if (this == DEAD_END) return otherInits;
		if (otherInits == DEAD_END) return this;
	
		if ((this.reachMode & UNREACHABLE) != (otherInits.reachMode & UNREACHABLE)){
			if ((this.reachMode & UNREACHABLE) != 0){
				return otherInits;
			} 
			return this;
		}
		
		// if one branch is not fake reachable, then the merged one is reachable
		this.reachMode &= otherInits.reachMode;
	
		// intersection of definitely assigned variables, 
		this.definiteInits &= otherInits.definiteInits;
		// union of potentially set ones
		this.potentialInits |= otherInits.potentialInits;
	
		// treating extra storage
		if (this.extraDefiniteInits != null) {
			if (otherInits.extraDefiniteInits != null) {
				// both sides have extra storage
				int i = 0, length, otherLength;
				if ((length = this.extraDefiniteInits.length) < (otherLength = otherInits.extraDefiniteInits.length)) {
					// current storage is shorter -> grow current (could maybe reuse otherInits extra storage?)
					System.arraycopy(this.extraDefiniteInits, 0, (this.extraDefiniteInits = new long[otherLength]), 0, length);
					System.arraycopy(this.extraPotentialInits, 0, (this.extraPotentialInits = new long[otherLength]), 0, length);
					while (i < length) {
						this.extraDefiniteInits[i] &= otherInits.extraDefiniteInits[i];
						this.extraPotentialInits[i] |= otherInits.extraPotentialInits[i++];
					}
					while (i < otherLength) {
						this.extraPotentialInits[i] = otherInits.extraPotentialInits[i++];
					}
				} else {
					// current storage is longer
					while (i < otherLength) {
						this.extraDefiniteInits[i] &= otherInits.extraDefiniteInits[i];
						this.extraPotentialInits[i] |= otherInits.extraPotentialInits[i++];
					}
					while (i < length)
						this.extraDefiniteInits[i++] = 0;
				}
			} else {
				// no extra storage on otherInits
				int i = 0, length = this.extraDefiniteInits.length;
				while (i < length)
					this.extraDefiniteInits[i++] = 0;
			}
		} else
			if (otherInits.extraDefiniteInits != null) {
				// no storage here, but other has extra storage.
				int otherLength;
				this.extraDefiniteInits = new long[otherLength = otherInits.extraDefiniteInits.length];
				System.arraycopy(otherInits.extraPotentialInits, 0, (this.extraPotentialInits = new long[otherLength]), 0, otherLength);
			}
		return this;
	}
	
	/*
	 * Answer the total number of fields in enclosing types of a given type
	 */
	static int numberOfEnclosingFields(ReferenceBinding type){
		
		int count = 0;
		type = type.enclosingType();
		while(type != null) {
			count += type.fieldCount();
			type = type.enclosingType();
		}
		return count;
	}
	
	public int reachMode(){
		return this.reachMode;
	}
	
	public FlowInfo setReachMode(int reachMode) {
		
		if (this == DEAD_END) return this; // cannot modify DEAD_END
	
		// reset optional inits when becoming unreachable
		if ((this.reachMode & UNREACHABLE) == 0 && (reachMode & UNREACHABLE) != 0) {
			this.potentialInits = 0;
			if (this.extraPotentialInits != null){
				for (int i = 0, length = this.extraPotentialInits.length; i < length; i++){
					this.extraPotentialInits[i] = 0;
				}
			}
		}				
		this.reachMode = reachMode;
	
		return this;
	}

	public String toString(){
		
		if (this == DEAD_END){
			return "FlowInfo.DEAD_END"; //$NON-NLS-1$
		}
		return "FlowInfo<def: "+ this.definiteInits //$NON-NLS-1$
			+", pot: " + this.potentialInits  //$NON-NLS-1$
			+ ", reachable:" + ((this.reachMode & UNREACHABLE) == 0) //$NON-NLS-1$
			+">"; //$NON-NLS-1$
	}
	
	public UnconditionalFlowInfo unconditionalInits() {
		
		// also see conditional inits, where it requests them to merge
		return this;
	}
}
