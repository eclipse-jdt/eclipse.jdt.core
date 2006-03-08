/*******************************************************************************
 * Copyright (c) 2000, 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jdt.internal.compiler.flow;

import org.eclipse.jdt.internal.compiler.impl.Constant;
import org.eclipse.jdt.internal.compiler.lookup.FieldBinding;
import org.eclipse.jdt.internal.compiler.lookup.LocalVariableBinding;
import org.eclipse.jdt.internal.compiler.lookup.ReferenceBinding;
import org.eclipse.jdt.internal.compiler.lookup.TagBits;

/**
 * Record initialization status during definite assignment analysis
 *
 * No caching of pre-allocated instances.
 */
public class UnconditionalFlowInfo extends FlowInfo {
	// Coverage tests
	/**
	 * Exception raised when unexpected behavior is detected during coverage
	 * tests. 
	 */
	public static class AssertionFailedException extends RuntimeException {
		private static final long serialVersionUID = 1827352841030089703L;
		
	public AssertionFailedException(String message) {
		super(message);
	}
	}
	
	// Coverage tests need that the code be instrumented. The following flag
	// controls whether the instrumented code is compiled in or not, and whether
	// the coverage tests methods run or not.
	public final static boolean coverageTestFlag = false;
	// never release with the coverageTestFlag set to true
	public static int coverageTestId;

	public long definiteInits;
	public long potentialInits;
	
	public long nullAssignmentStatusBit1;
	public long nullAssignmentStatusBit2;
	// 0 0 is potential (bit 1 is leftmost here)
	// 1 0 is assigned
	// 0 1 is protected null (aka if (o == null) { // here o protected null...)
	// 1 1 is protected non null
	public long nullAssignmentValueBit1;
	public long nullAssignmentValueBit2;
	// information only relevant for potential and assigned
	// 0 0 is start -- nothing known at all
	// 0 1 is assigned non null or potential anything but null
	// 1 0 is assigned null or potential null
	// 1 1 is potential null and potential anything but null or definite unknown
	// consider reintroducing the difference between potential non null and potential
	// unknown; if this is done, rename to nullAssignmentBit[1-4] since the semantics
	// would be ever less clear
	// went public in order to grant access to tests; do not like it...

	public static final int extraLength = 6;
	public long extra[][];
		// extra bit fields for larger numbers of fields/variables
		// extra[0] holds definiteInits values, extra[1] potentialInits, etc.
		// lifecycle is extra == null or else all extra[]'s are allocated
		// arrays which have the same size

	public int maxFieldCount; // limit between fields and locals
	
	// Constants
	public static final int BitCacheSize = 64; // 64 bits in a long.

public FlowInfo addInitializationsFrom(FlowInfo inits) {
	if (this == DEAD_END)
		return this;
	if (inits == DEAD_END)
		return this;
	UnconditionalFlowInfo otherInits = inits.unconditionalInits();		

	// union of definitely assigned variables, 
	this.definiteInits |= otherInits.definiteInits;
	// union of potentially set ones
	this.potentialInits |= otherInits.potentialInits;
	// combine null information
	// note: we may have both forms of protection (null and non null) 
	// coming with otherInits, because of loops
	boolean considerNulls = (otherInits.tagBits & NULL_FLAG_MASK) != 0;
	long a1, na1, a2, na2, a3, a4, na4, b1, b2, nb2, b3, nb3, b4, nb4;
	if (considerNulls) {
		if ((this.tagBits & NULL_FLAG_MASK) == 0) {
			this.nullAssignmentStatusBit1 = otherInits.nullAssignmentStatusBit1;
			this.nullAssignmentStatusBit2 = otherInits.nullAssignmentStatusBit2;
			this.nullAssignmentValueBit1 = otherInits.nullAssignmentValueBit1;
			this.nullAssignmentValueBit2 = otherInits.nullAssignmentValueBit2;
			if (coverageTestFlag && coverageTestId == 1) {
				this.nullAssignmentValueBit2 = ~0;
			}
		}
		else {
			this.nullAssignmentStatusBit1 =
				(b1 = otherInits.nullAssignmentStatusBit1) 
					| ((a1 = this.nullAssignmentStatusBit1) 
						& (((nb2 = ~(b2 = otherInits.nullAssignmentStatusBit2)) 
								& (nb3 = ~(b3 = otherInits.nullAssignmentValueBit1)) 
								& ((nb4 = ~(b4 = otherInits.nullAssignmentValueBit2)) 
									| ((a2 = this.nullAssignmentStatusBit2) 
										^ (a4 = this.nullAssignmentValueBit2)))) 
							| nb4 &	(na2 = ~a2)	& (na4 = ~a4)));
			this.nullAssignmentStatusBit2 =
				(b1 & b2) 
					| (~b1 
						& ((((na1 = ~a1) | a4) & b2) 
							| (a2 
								& (b2 
									| (a1 & (na4 = ~a4) & nb2 & nb3) 
									| ((~(a3 = this.nullAssignmentValueBit1) & nb3) 
											| (na1 & na4)) 
										& nb4))));
			this.nullAssignmentValueBit1 = 
				nb2 & b3 |
				~b1 & ((a1 & na2 & na4 | na1 & a3) & (nb2 | nb4) |
						a1 & na2 & a3 & nb2 |
						(a1 | a2 | na4) & b3);
			this.nullAssignmentValueBit2 =
				b4 |
				a4 & (nb2 & nb3 | ~(b1 ^ b2));
			if (coverageTestFlag && coverageTestId == 2) {
				this.nullAssignmentValueBit2 = ~0;
			}
		}
		this.tagBits |= NULL_FLAG_MASK; // in all cases - avoid forgetting extras
	}
	// treating extra storage
	if (this.extra != null || otherInits.extra != null) {
		int mergeLimit = 0, copyLimit = 0;
		if (this.extra != null) {
			if (otherInits.extra != null) {
				// both sides have extra storage
				int length, otherLength;
				if ((length = this.extra[0].length) < 
						(otherLength = otherInits.extra[0].length)) {
					if (coverageTestFlag && coverageTestId == 3) {
						throw new AssertionFailedException("COVERAGE 3"); //$NON-NLS-1$
					}
					// current storage is shorter -> grow current
					for (int j = 0; j < extraLength; j++) {
						System.arraycopy(this.extra[j], 0, 
							(this.extra[j] = new long[otherLength]), 0, length);
					}
					mergeLimit = length;
					copyLimit = otherLength;
				} else {
					if (coverageTestFlag && coverageTestId == 4) {
						throw new AssertionFailedException("COVERAGE 4"); //$NON-NLS-1$
					}
					// current storage is longer
					mergeLimit = otherLength;
				}
			} 
		} 
		else if (otherInits.extra != null) {
			// no storage here, but other has extra storage.
			// shortcut regular copy because array copy is better
			int otherLength;
			this.extra = new long[extraLength][];
			System.arraycopy(otherInits.extra[0], 0, 
				(this.extra[0] = new long[otherLength = 
					otherInits.extra[0].length]), 0, otherLength);			
			System.arraycopy(otherInits.extra[1], 0, 
				(this.extra[1] = new long[otherLength]), 0, otherLength);
			if (considerNulls) {
				for (int j = 2; j < extraLength; j++) {
					System.arraycopy(otherInits.extra[j], 0, 
						(this.extra[j] = new long[otherLength]), 0, otherLength);
				}
				if (coverageTestFlag && coverageTestId == 5) {
					this.extra[5][otherLength - 1] = ~0;
				}
			}
			else {
				for (int j = 2; j < extraLength; j++) {
					this.extra[j] = new long[otherLength];			
				}
				if (coverageTestFlag && coverageTestId == 6) {
					this.extra[5][otherLength - 1] = ~0;
				}
			}
		}
		int i = 0;
		for (; i < mergeLimit; i++) {
			this.extra[0][i] |= otherInits.extra[0][i];
			this.extra[1][i] |= otherInits.extra[1][i];
			if (considerNulls) { // could consider pushing the test outside the loop
				if (this.extra[2][i] == 0 &&
						this.extra[3][i] == 0 &&
						this.extra[4][i] == 0 &&
						this.extra[5][i] == 0) {
					for (int j = 2; j < extraLength; j++) {
						this.extra[j][i] = otherInits.extra[j][i];
					}
					if (coverageTestFlag && coverageTestId == 7) {
						this.extra[5][i] = ~0;
					}
				}
				else {
					this.extra[2][i] =
						(b1 = otherInits.extra[2][i]) |
						(a1	 = this.extra[2][i]) & 
							((nb2 = ~(b2 = otherInits.extra[3][i])) &
								(nb3 = ~(b3 = otherInits.extra[4][i])) &
								((nb4 = ~(b4 = otherInits.extra[5][i])) |
									((a2 = this.extra[3][i]) ^ 
										(a4 = this.extra[5][i]))) | 
							nb4 & (na2 = ~a2) & (na4 = ~a4));
					this.extra[3][i] =
						b1 & b2 |
						~b1 & (((na1 = ~a1) | a4) & b2 |
								a2 & (b2 |
									a1 & (na4 = ~a4) & nb2 & nb3 |
									(~(a3 = this.extra[4][i]) & nb3 | na1 & na4) & nb4));
					this.extra[4][i] = 
						nb2 & b3 |
						~b1 & ((a1 & na2 & na4 | na1 & a3) & (nb2 | nb4) |
								a1 & na2 & a3 & nb2 |
								(a1 | a2 | na4) & b3);
					this.extra[5][i] =
						b4 |
						a4 & (nb2 & nb3 | ~(b1 ^ b2));
						if (coverageTestFlag && coverageTestId == 8) {
							this.extra[5][i] = ~0;
						}
				}
			}
		}
		for (; i < copyLimit; i++) {
			this.extra[0][i] = otherInits.extra[0][i];
			this.extra[1][i] = otherInits.extra[1][i];
			if (considerNulls) {
				for (int j = 2; j < extraLength; j++) {
					this.extra[j][i] = otherInits.extra[j][i];
				}
				if (coverageTestFlag && coverageTestId == 9) {
					this.extra[5][i] = ~0;
				}
			}
		}
	}
	return this;
}

public FlowInfo addPotentialInitializationsFrom(FlowInfo inits) {
	if (this == DEAD_END){
		return this;
	}
	if (inits == DEAD_END){
		return this;
	}
	UnconditionalFlowInfo otherInits = inits.unconditionalInits();
	// union of potentially set ones
	this.potentialInits |= otherInits.potentialInits;
	// treating extra storage
	if (this.extra != null) {
		if (otherInits.extra != null) {
			// both sides have extra storage
			int i = 0, length, otherLength;
			if ((length = this.extra[0].length) < (otherLength = otherInits.extra[0].length)) {
				// current storage is shorter -> grow current
				for (int j = 0; j < extraLength; j++) {
					System.arraycopy(this.extra[j], 0, 
						(this.extra[j] = new long[otherLength]), 0, length);
				}
				for (; i < length; i++) {
					this.extra[1][i] |= otherInits.extra[1][i];
				}
				for (; i < otherLength; i++) {
					this.extra[1][i] = otherInits.extra[1][i];
				}
			} 
			else {
				// current storage is longer
				for (; i < otherLength; i++) {
					this.extra[1][i] |= otherInits.extra[1][i];
				}
			}
		}
	} 
	else if (otherInits.extra != null) {
		// no storage here, but other has extra storage.
		int otherLength = otherInits.extra[0].length;
		this.extra = new long[extraLength][];
		for (int j = 0; j < extraLength; j++) {
			this.extra[j] = new long[otherLength];			
		}
		System.arraycopy(otherInits.extra[1], 0, this.extra[1], 0, 
			otherLength);
	}
	this.addPotentialNullInfoFrom(otherInits);
	return this;
}

/**
 * Compose other inits over this flow info, then return this. The operation
 * semantics are to wave into this flow info the consequences upon null 
 * information of a possible path into the operations that resulted into 
 * otherInits. The fact that this path may be left unexecuted under peculiar 
 * conditions results into less specific results than 
 * {@link #addInitializationsFrom(FlowInfo) addInitializationsFrom}; moreover,
 * only the null information is affected.
 * @param otherInits other null inits to compose over this
 * @return this, modified according to otherInits information
 */
public UnconditionalFlowInfo addPotentialNullInfoFrom(
		UnconditionalFlowInfo otherInits) {
	if ((this.tagBits & UNREACHABLE) != 0 ||
			(otherInits.tagBits & UNREACHABLE) != 0 ||
			(otherInits.tagBits & NULL_FLAG_MASK) == 0) {
		return this;
	}
	// if we get here, otherInits has some null info
	boolean thisHasNulls = (this.tagBits & NULL_FLAG_MASK) != 0;
	if (thisHasNulls) {
		long a1, a2, na2, a3, na3, a4, na4, b1, nb1, b2, nb2, b3, nb3, b4, nb4;
		this.nullAssignmentStatusBit1 =
			((a1 = this.nullAssignmentStatusBit1) &
					(na4 = ~(a4 = this.nullAssignmentValueBit2)) &	
					((na3 = ~(a3 = this.nullAssignmentValueBit1)) | 
							(a2 = this.nullAssignmentStatusBit2)) | 
							a2 & na3 &	a4) & 
					(nb3 = ~(b3 = otherInits.nullAssignmentValueBit1)) &
					((b2 = otherInits.nullAssignmentStatusBit2) | 
					(nb4 = ~(b4 = otherInits.nullAssignmentValueBit2))) |
			a1 & (na2 = ~a2) & 
				(a4 & ((nb1 = ~(b1 = otherInits.nullAssignmentStatusBit1)) & 
						nb3 | b1 &
						(b4 | b2)) |
				na4 & (nb1 & (((nb2 = ~b2) & nb4 | b2) & nb3 | b3 & nb4) | 
						b1 & nb4 & (nb2 | nb3)));
		this.nullAssignmentStatusBit2 =
			a2 & (~a1 & na4 & nb4 |
					a1 & na3 & nb3 & (nb1 & (nb2 & nb4 | b2) |
										b1 & (nb4 |b2 & b4)));
		this.nullAssignmentValueBit1 =
			a3 |
			b1 & nb2 & nb4 |
			nb1 & b3 |
			a1 & na2 & (b1 & b3 | nb1 & b4);
//			b1 & (~b2 & ~b4 | a1 & ~a2 & b3) |
//			~b1 & (b3 | a1 & ~a2 & b4); -- same op nb
		this.nullAssignmentValueBit2 =
			a4 & (na2 | a2 & na3) |
			b4 & (nb2 | b2 & nb3);
		if (coverageTestFlag && coverageTestId == 15) {
			this.nullAssignmentValueBit2 = ~0;
		}
		// extra storage management
		if (otherInits.extra != null) {
			int mergeLimit = 0, copyLimit = 0;
			int otherLength = otherInits.extra[0].length;
			if (this.extra == null) {
				this.extra = new long[extraLength][];
				for (int j = 0; j < extraLength; j++) {
					this.extra[j] = new long[otherLength];
				}
				copyLimit = otherLength;
				if (coverageTestFlag && coverageTestId == 16) {
					this.extra[2][0] = ~0; thisHasNulls = true;
				}
			}
			else {
				mergeLimit = otherLength;
				if (mergeLimit > this.extra[0].length) {
					copyLimit = mergeLimit;
					mergeLimit = this.extra[0].length;
					for (int j = 0; j < extraLength; j++) {
						System.arraycopy(this.extra[j], 0,
								this.extra[j] = new long[otherLength], 0,
								mergeLimit);
					}
				}
				int i;
				for (i = 0; i < mergeLimit; i++) {
					this.extra[2][i] =
						((a1 = this.extra[2][i]) &
								(na4 = ~(a4 = this.extra[5][i])) &	
								((na3 = ~(a3 = this.extra[4][i])) | 
										(a2 = this.extra[3][i])) | 
										a2 & na3 &	a4) & 
								(nb3 = ~(b3 = otherInits.extra[4][i])) &
								((b2 = otherInits.extra[3][i]) | 
								(nb4 = ~(b4 = otherInits.extra[5][i]))) |
						a1 & (na2 = ~a2) & 
							(a4 & ((nb1 = ~(b1 = otherInits.extra[2][i])) & 
									nb3 | b1 &
									(b4 | b2)) |
							na4 & (nb1 & (((nb2 = ~b2) & nb4 | b2) & nb3 | b3 & nb4) | 
									b1 & nb4 & (nb2 | nb3)));
					this.extra[3][i] =
						a2 & (~a1 & na4 & nb4 |
								a1 & na3 & nb3 & (nb1 & (nb2 & nb4 | b2) |
													b1 & (nb4 |b2 & b4)));
					this.extra[4][i] =
						a3 |
						b1 & nb2 & nb4 |
						nb1 & b3 |
						a1 & na2 & (b1 & b3 | nb1 & b4);
					this.extra[5][i] =
						a4 & (na2 | a2 & na3) |
						b4 & (nb2 | b2 & nb3);
					if (coverageTestFlag && coverageTestId == 17) {
						this.nullAssignmentValueBit2 = ~0;
					}
				}
				for (; i < copyLimit; i++) {
					if (otherInits.extra[4][i] != 0 ||
						otherInits.extra[5][i] != 0) {
						this.tagBits |= NULL_FLAG_MASK; 
						this.extra[4][i] = 
							otherInits.extra[4][i] &
							~(otherInits.extra[2][i] &
							  ~otherInits.extra[3][i] &
							  otherInits.extra[5][i]);
						this.extra[5][i] = 
							otherInits.extra[5][i];
						if (coverageTestFlag && coverageTestId == 18) {
							this.extra[5][i] = ~0;
						}
					}
				}
			}
		}
	}
	else {
		if (otherInits.nullAssignmentValueBit1 != 0 ||
			otherInits.nullAssignmentValueBit2 != 0) {
			// add potential values
			this.nullAssignmentValueBit1 = 
				otherInits.nullAssignmentValueBit1 & 
					~(otherInits.nullAssignmentStatusBit1 &
					  ~otherInits.nullAssignmentStatusBit2 &
					  otherInits.nullAssignmentValueBit2); // exclude assigned unknown
			this.nullAssignmentValueBit2 = 
				otherInits.nullAssignmentValueBit2;
			thisHasNulls = 
				this.nullAssignmentValueBit1 != 0 ||
				this.nullAssignmentValueBit2 != 0;
			if (coverageTestFlag && coverageTestId == 10) {
				this.nullAssignmentValueBit2 = ~0;
			}
		}
		// extra storage management
		if (otherInits.extra != null) {
			int mergeLimit = 0, copyLimit = 0;
			int otherLength = otherInits.extra[0].length;
			if (this.extra == null) {
				copyLimit = otherLength; 
					// cannot happen when called from addPotentialInitializationsFrom
				this.extra = new long[extraLength][];
				for (int j = 0; j < extraLength; j++) {
					this.extra[j] = new long[otherLength];
				}
				if (coverageTestFlag && coverageTestId == 11) {
					this.extra[5][0] = ~0; this.tagBits |= NULL_FLAG_MASK;
				}
			}
			else {
				mergeLimit = otherLength;
				if (mergeLimit > this.extra[0].length) {
					copyLimit = mergeLimit;
					mergeLimit = this.extra[0].length;
					System.arraycopy(this.extra[0], 0,
							this.extra[0] = new long[otherLength], 0,
							mergeLimit);
					System.arraycopy(this.extra[1], 0,
							this.extra[1] = new long[otherLength], 0,
							mergeLimit);
					for (int j = 2; j < extraLength; j++) {
						this.extra[j] = new long[otherLength];
					}
					if (coverageTestFlag && coverageTestId == 12) {
						throw new AssertionFailedException("COVERAGE 12"); //$NON-NLS-1$
					}
				}
			}
			int i;
			for (i = 0; i < mergeLimit; i++) {
				if (otherInits.extra[4][i] != 0 ||
					otherInits.extra[5][i] != 0) {
					this.extra[4][i] |= 
						otherInits.extra[4][i] &
						~(otherInits.extra[2][i] &
						  ~otherInits.extra[3][i] &
						  otherInits.extra[5][i]);
					this.extra[5][i] |= 
						otherInits.extra[5][i];
					thisHasNulls = thisHasNulls ||
						this.extra[4][i] != 0 ||
						this.extra[5][i] != 0;
					if (coverageTestFlag && coverageTestId == 13) {
						this.extra[5][i] = ~0;
					}
				}
			}
			for (; i < copyLimit; i++) {
				if (otherInits.extra[4][i] != 0 ||
					otherInits.extra[5][i] != 0) {
					this.extra[4][i] = 
						otherInits.extra[4][i] &
						~(otherInits.extra[2][i] &
						  ~otherInits.extra[3][i] &
						  otherInits.extra[5][i]);
					this.extra[5][i] = 
						otherInits.extra[5][i];
					thisHasNulls = thisHasNulls ||
						this.extra[4][i] != 0 ||
						this.extra[5][i] != 0;
					if (coverageTestFlag && coverageTestId == 14) {
						this.extra[5][i] = ~0;
					}
				}
			}
		}
	}
	if (thisHasNulls) {
		this.tagBits |= NULL_FLAG_MASK; 
	}
	else {
		this.tagBits &= NULL_FLAG_MASK; 
	}
	return this;
}

public FlowInfo copy() {
	// do not clone the DeadEnd
	if (this == DEAD_END) {
		return this;
	}
	UnconditionalFlowInfo copy = new UnconditionalFlowInfo();
	// copy slots
	copy.definiteInits = this.definiteInits;
	copy.potentialInits = this.potentialInits;
	boolean hasNullInfo = (this.tagBits & NULL_FLAG_MASK) != 0;
	if (hasNullInfo) { 
		copy.nullAssignmentStatusBit1 = this.nullAssignmentStatusBit1;
		copy.nullAssignmentStatusBit2 = this.nullAssignmentStatusBit2;
		copy.nullAssignmentValueBit1 = this.nullAssignmentValueBit1;
		copy.nullAssignmentValueBit2 = this.nullAssignmentValueBit2;
	}
	copy.tagBits = this.tagBits;
	copy.maxFieldCount = this.maxFieldCount;
	if (this.extra != null) {
		int length;
		copy.extra = new long[extraLength][];
		System.arraycopy(this.extra[0], 0, 
			(copy.extra[0] = new long[length = this.extra[0].length]), 0, 
			length);
		System.arraycopy(this.extra[1], 0, 
			(copy.extra[1] = new long[length]), 0, length);
		if (hasNullInfo) {
			for (int j = 2; j < extraLength; j++) {
				System.arraycopy(this.extra[j], 0, 
					(copy.extra[j] = new long[length]), 0, length);
			}
		}
		else {
			for (int j = 2; j < extraLength; j++) {
				copy.extra[j] = new long[length];
			}
		}
	}
	return copy;
}

/**
 * Discard definite inits and potential inits from this, then return this.
 * The returned flow info only holds null related information. 
 * @return this flow info, minus definite inits and potential inits
 */
public UnconditionalFlowInfo discardInitializationInfo() {
	if (this == DEAD_END) {
		return this;
	}
	this.definiteInits =
		this.potentialInits = 0;
	if (this.extra != null) {
		for (int i = 0, length = this.extra[0].length; i < length; i++) {
			this.extra[0][i] = this.extra[1][i] = 0;
		}
	}
	return this;
}

/**
 * Remove local variables information from this flow info and return this.
 * @return this, deprived from any local variable information
 */
public UnconditionalFlowInfo discardNonFieldInitializations() {
	int limit = this.maxFieldCount;
	if (limit < BitCacheSize) {
		long mask = (1L << limit)-1;
		this.definiteInits &= mask;
		this.potentialInits &= mask;
		this.nullAssignmentStatusBit1 &= mask;
		this.nullAssignmentStatusBit2 &= mask;
		this.nullAssignmentValueBit1 &= mask;
		this.nullAssignmentValueBit2 &= mask;
	} 
	// use extra vector
	if (this.extra == null) {
		return this; // if vector not yet allocated, then not initialized
	}
	int vectorIndex, length = this.extra[0].length;
	if ((vectorIndex = (limit / BitCacheSize) - 1) >= length) {
		return this; // not enough room yet
	}
	if (vectorIndex >= 0) { 
		// else we only have complete non field array items left
		long mask = (1L << (limit % BitCacheSize))-1;
		for (int j = 0; j < extraLength; j++) {
			this.extra[j][vectorIndex] &= mask;
		}
	}
	for (int i = vectorIndex + 1; i < length; i++) {
		for (int j = 0; j < extraLength; j++) {
			this.extra[j][i] = 0;
		}
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
	if (position < BitCacheSize) {
		// use bits
		return (this.definiteInits & (1L << position)) != 0; 
	}
	// use extra vector
	if (this.extra == null)
		return false; // if vector not yet allocated, then not initialized
	int vectorIndex;
	if ((vectorIndex = (position / BitCacheSize) - 1) 
			>= this.extra[0].length) {
		return false; // if not enough room in vector, then not initialized
	}
	return ((this.extra[0][vectorIndex]) & 
				(1L << (position % BitCacheSize))) != 0;
}

final public boolean isDefinitelyAssigned(FieldBinding field) {
	// Mirrored in CodeStream.isDefinitelyAssigned(..) 
	// do not want to complain in unreachable code
	if ((this.tagBits & UNREACHABLE) != 0) { 
		return true;
	}
	return isDefinitelyAssigned(field.id); 
}

final public boolean isDefinitelyAssigned(LocalVariableBinding local) {
	// do not want to complain in unreachable code
	if ((this.tagBits & UNREACHABLE) != 0) {
		return true;
	}
	// final constants are inlined, and thus considered as always initialized
	if (local.constant() != Constant.NotAConstant) {
		return true;
	}
	return isDefinitelyAssigned(local.id + this.maxFieldCount);
}

final public boolean isDefinitelyNonNull(LocalVariableBinding local) {
	// do not want to complain in unreachable code
	if ((this.tagBits & UNREACHABLE) != 0 || 
			(this.tagBits & NULL_FLAG_MASK) == 0) {
		return false;
	}
	if ((local.type.tagBits & TagBits.IsBaseType) != 0 || 
			local.constant() != Constant.NotAConstant) { // String instances
		return true;
	}
	int position = local.id + this.maxFieldCount;
	long mask;
	if (position < BitCacheSize) { // use bits
		return 
			(this.nullAssignmentStatusBit2 & 
				(mask = 1L << position)) != 0 ?
			(this.nullAssignmentStatusBit1 & mask) != 0 :
			(this.nullAssignmentStatusBit1 & 
				this.nullAssignmentValueBit2 & mask) != 0 &&
			(this.nullAssignmentValueBit1 & mask) == 0; 
	}
	// use extra vector
	if (this.extra == null) {
		return false; // if vector not yet allocated, then not initialized
	}
	int vectorIndex;
	if ((vectorIndex = (position / BitCacheSize) - 1)  
			>= this.extra[0].length) {
		return false; // if not enough room in vector, then not initialized
	}
	return 
		(this.extra[3][vectorIndex] & 
			(mask = 1L << (position % BitCacheSize))) != 0 ?
		(this.extra[2][vectorIndex] & mask) != 0 :
		(this.extra[2][vectorIndex] & 
			this.extra[5][vectorIndex] & mask) != 0 &&
		(this.extra[4][vectorIndex] & mask) == 0;
}

final public boolean isDefinitelyNull(LocalVariableBinding local) {
	// do not want to complain in unreachable code
	if ((this.tagBits & UNREACHABLE) != 0 || 
			(this.tagBits & NULL_FLAG_MASK) == 0 || 
			(local.type.tagBits & TagBits.IsBaseType) != 0) {
		return false;
	}
	int position = local.id + this.maxFieldCount;
	long mask;
	if (position < BitCacheSize) { // use bits
		return 
			(this.nullAssignmentStatusBit2 & (mask = 1L << position)) != 0 ?
			(this.nullAssignmentStatusBit1 & mask) == 0 :
			(this.nullAssignmentStatusBit1 & 
				this.nullAssignmentValueBit1 & mask) != 0 &&
			(this.nullAssignmentValueBit2 & mask) == 0; 
	}
	// use extra vector
	if (this.extra == null) {
		return false; // if vector not yet allocated, then not initialized
	}
	int vectorIndex;
	if ((vectorIndex = (position / BitCacheSize) - 1) >= 
			this.extra[0].length) {
		return false; // if not enough room in vector, then not initialized
	}
	return
		(this.extra[3][vectorIndex] & 
			(mask = 1L << (position % BitCacheSize))) != 0 ?
		(this.extra[2][vectorIndex] & mask) == 0 :
		(this.extra[2][vectorIndex] & 
			this.extra[4][vectorIndex] & mask) != 0 &&
		(this.extra[5][vectorIndex] & mask) == 0;
}

final public boolean isDefinitelyUnknown(LocalVariableBinding local) {
	// do not want to complain in unreachable code
	if ((this.tagBits & UNREACHABLE) != 0 || 
			(this.tagBits & NULL_FLAG_MASK) == 0) {
		return false;
	}
	int position = local.id + this.maxFieldCount;
	long mask;
	if (position < BitCacheSize) { // use bits
		return 
			(this.nullAssignmentStatusBit2 & (mask = 1L << position)) != 0 ?
			false :
			(this.nullAssignmentStatusBit1 & 
				this.nullAssignmentValueBit1 & 
				this.nullAssignmentValueBit2 & mask) != 0; 
	}
	// use extra vector
	if (this.extra == null) {
		return false; // if vector not yet allocated, then not initialized
	}
	int vectorIndex;
	if ((vectorIndex = (position / BitCacheSize) - 1) >= 
			this.extra[0].length) {
		return false; // if not enough room in vector, then not initialized
	}
	return
		(this.extra[3][vectorIndex] & 
			(mask = 1L << (position % BitCacheSize))) != 0 ?
		false :
		(this.extra[2][vectorIndex] & 
			this.extra[4][vectorIndex] &
			this.extra[5][vectorIndex] &
			mask) != 0;
}

/**
 * Check status of potential assignment at a given position.
 */
final private boolean isPotentiallyAssigned(int position) {
	// id is zero-based
	if (position < BitCacheSize) {
		// use bits
		return (this.potentialInits & (1L << position)) != 0;
	}
	// use extra vector
	if (this.extra == null) {
		return false; // if vector not yet allocated, then not initialized
	}
	int vectorIndex;
	if ((vectorIndex = (position / BitCacheSize) - 1) 
			>= this.extra[0].length) {
		return false; // if not enough room in vector, then not initialized
	}
	return ((this.extra[1][vectorIndex]) & 
			(1L << (position % BitCacheSize))) != 0;
}

final public boolean isPotentiallyAssigned(FieldBinding field) {
	return isPotentiallyAssigned(field.id); 
}

final public boolean isPotentiallyAssigned(LocalVariableBinding local) {
	// final constants are inlined, and thus considered as always initialized
	if (local.constant() != Constant.NotAConstant) {
		return true;
	}
	return isPotentiallyAssigned(local.id + this.maxFieldCount);
}

final public boolean isPotentiallyNull(LocalVariableBinding local) {
	if ((this.tagBits & NULL_FLAG_MASK) == 0 || 
			(local.type.tagBits & TagBits.IsBaseType) != 0) {
		return false;
	}
	int position;
	long mask;
	if ((position = local.id + this.maxFieldCount) < BitCacheSize) {
		// use bits
		return
			(this.nullAssignmentStatusBit2 & (mask = 1L << position)) != 0 ?
			(this.nullAssignmentStatusBit1 & mask) == 0 : // protected null
			(this.nullAssignmentValueBit1 & mask) != 0 && // null bit set and
				((this.nullAssignmentStatusBit1 & mask) == 0 || // (potential or
				 (this.nullAssignmentValueBit2 & mask) == 0); 
											// assigned, but not unknown)
	}
	// use extra vector
	if (this.extra == null) {
		return false; // if vector not yet allocated, then not initialized
	}
	int vectorIndex;
	if ((vectorIndex = (position / BitCacheSize) - 1) >= 
			this.extra[0].length) {
		return false; // if not enough room in vector, then not initialized
	}
	return 
		(this.extra[3][vectorIndex] & 
			(mask = 1L << (position % BitCacheSize))) != 0 ?
		(this.extra[2][vectorIndex] & mask) == 0 :
		(this.extra[4][vectorIndex] & mask) != 0 && 
			((this.extra[2][vectorIndex] & mask) == 0 || 
			 (this.extra[5][vectorIndex] & mask) == 0); 
}

final public boolean isPotentiallyUnknown(LocalVariableBinding local) {
	// do not want to complain in unreachable code
	if ((this.tagBits & UNREACHABLE) != 0 || 
			(this.tagBits & NULL_FLAG_MASK) == 0) {
		return false;
	}
	int position = local.id + this.maxFieldCount;
	long mask;
	if (position < BitCacheSize) { // use bits
		return 
			(this.nullAssignmentStatusBit2 & (mask = 1L << position)) != 0 ?
			false :
			((this.nullAssignmentStatusBit1 & 
				this.nullAssignmentValueBit1 |
			 ~this.nullAssignmentStatusBit1 &
				~this.nullAssignmentValueBit1) & 
				this.nullAssignmentValueBit2 & mask) != 0; 
	}
	// use extra vector
	if (this.extra == null) {
		return false; // if vector not yet allocated, then not initialized
	}
	int vectorIndex;
	if ((vectorIndex = (position / BitCacheSize) - 1) >= 
			this.extra[0].length) {
		return false; // if not enough room in vector, then not initialized
	}
	return
		(this.extra[3][vectorIndex] & 
			(mask = 1L << (position % BitCacheSize))) != 0 ?
		false :
		((this.extra[2][vectorIndex] & 
			this.extra[4][vectorIndex] |
		  ~this.extra[2][vectorIndex] &
			~this.extra[4][vectorIndex]) &
			this.extra[5][vectorIndex] &
			mask) != 0;
}

final public boolean isProtectedNonNull(LocalVariableBinding local) {
	if ((this.tagBits & NULL_FLAG_MASK) == 0 ||
			(local.type.tagBits & TagBits.IsBaseType) != 0) {
		return false;
	}
	int position;
	if ((position = local.id + this.maxFieldCount) < BitCacheSize) {
		// use bits
		return (this.nullAssignmentStatusBit1 &
				this.nullAssignmentStatusBit2 & (1L << position)) != 0;
	}
	// use extra vector
	if (this.extra == null) {
		return false; // if vector not yet allocated, then not initialized
	}
	int vectorIndex;
	if ((vectorIndex = (position / BitCacheSize) - 1) >= 
		this.extra[0].length) {
		return false; // if not enough room in vector, then not initialized
	}
	return (this.extra[2][vectorIndex] & 
			this.extra[3][vectorIndex] & 
			(1L << (position % BitCacheSize))) != 0;
}

final public boolean isProtectedNull(LocalVariableBinding local) {
	if ((this.tagBits & NULL_FLAG_MASK) == 0 || 
			(local.type.tagBits & TagBits.IsBaseType) != 0) {
		return false;
	}
	int position;
	if ((position = local.id + this.maxFieldCount) < BitCacheSize) {
		// use bits
		return (~this.nullAssignmentStatusBit1 &
				this.nullAssignmentStatusBit2 & (1L << position)) != 0;
	}
	// use extra vector
	if (this.extra == null) {
		return false; // if vector not yet allocated, then not initialized
	}
	int vectorIndex;
	if ((vectorIndex = (position / BitCacheSize) - 1) >= 
			this.extra[0].length) {
		return false; // if not enough room in vector, then not initialized
	}
	return (~this.extra[2][vectorIndex] & 
			this.extra[3][vectorIndex] &
			(1L << (position % BitCacheSize))) != 0;
}

public void markAsComparedEqualToNonNull(LocalVariableBinding local) {
	// protected from non-object locals in calling methods
	if (this != DEAD_END) {
		this.tagBits |= NULL_FLAG_MASK;
		int position;
		long mask;
		// position is zero-based
		if ((position = local.id + this.maxFieldCount) < BitCacheSize) {
			// use bits
			if (((mask = 1L << position) & // leave assigned non null unchanged 
					this.nullAssignmentStatusBit1 &
					~this.nullAssignmentStatusBit2 &
					~this.nullAssignmentValueBit1 &
					this.nullAssignmentValueBit2) == 0) {
				// set protected non null
				this.nullAssignmentStatusBit1 |= mask;
				this.nullAssignmentStatusBit2 |= mask;
				 // clear potential null
				this.nullAssignmentValueBit1 &= ~mask;
				if (coverageTestFlag && coverageTestId == 19) {
					this.nullAssignmentValueBit2 = ~0;
				}
			}
			if (coverageTestFlag && coverageTestId == 20) {
				this.nullAssignmentValueBit2 = ~0;
			}
		} 
		else {
			// use extra vector
			int vectorIndex = (position / BitCacheSize) - 1;
			if (this.extra == null) {
				int length = vectorIndex + 1;
				this.extra = new long[extraLength][];
				for (int j = 0; j < extraLength; j++) {
					this.extra[j] = new long[length];
				}
				if (coverageTestFlag && coverageTestId == 21) {
					throw new AssertionFailedException("COVERAGE 21"); //$NON-NLS-1$
				}
			}
			else {
				int oldLength;
				if (vectorIndex >= (oldLength = this.extra[0].length)) {
					int newLength = vectorIndex + 1;
					for (int j = 0; j < extraLength; j++) {
						System.arraycopy(this.extra[j], 0, 
							(this.extra[j] = new long[newLength]), 0, 
							oldLength);
					}
					if (coverageTestFlag && coverageTestId == 22) {
						throw new AssertionFailedException("COVERAGE 22"); //$NON-NLS-1$
					}
				}
			}
			if (((mask = 1L << (position % BitCacheSize)) & 
					this.extra[2][vectorIndex] &
					~this.extra[3][vectorIndex] &
					~this.extra[4][vectorIndex] &
					this.extra[5][vectorIndex]) == 0) {
				this.extra[2][vectorIndex] |= mask;
				this.extra[3][vectorIndex] |= mask;
				this.extra[4][vectorIndex] &= ~mask;
				if (coverageTestFlag && coverageTestId == 23) {
					this.extra[5][vectorIndex] = ~0;
				}
			}
		}
	}
}

public void markAsComparedEqualToNull(LocalVariableBinding local) {
	// protected from non-object locals in calling methods
	if (this != DEAD_END) {
		this.tagBits |= NULL_FLAG_MASK;
		int position;
		long mask, unknownAssigned;
		// position is zero-based
		if ((position = local.id + this.maxFieldCount) < BitCacheSize) {
			// use bits
			mask = 1L << position;
			if ((mask & // leave assigned null unchanged
					this.nullAssignmentStatusBit1 &
					~this.nullAssignmentStatusBit2 &
					this.nullAssignmentValueBit1 &
					~this.nullAssignmentValueBit2) == 0) {
				unknownAssigned = this.nullAssignmentStatusBit1 &
					~this.nullAssignmentStatusBit2 &
					this.nullAssignmentValueBit1 &
					this.nullAssignmentValueBit2;
				// set protected
				this.nullAssignmentStatusBit2 |= mask;
				this.nullAssignmentStatusBit1 &= (mask = ~mask);
				// protected is null
				this.nullAssignmentValueBit1 &= mask | ~unknownAssigned;
				this.nullAssignmentValueBit2 &= mask;
				// clear potential anything but null
				if (coverageTestFlag && coverageTestId == 24) {
					this.nullAssignmentValueBit2 = ~0;
				}
			}
			if (coverageTestFlag && coverageTestId == 25) {
				this.nullAssignmentValueBit2 = ~0;
			}
		} 
		else {
			// use extra vector
			int vectorIndex = (position / BitCacheSize) - 1;
			mask = 1L << (position % BitCacheSize);
			if (this.extra == null) {
				int length = vectorIndex + 1;
				this.extra = new long[extraLength][];
				for (int j = 0; j < extraLength; j++) {
					this.extra[j] = new long[length ];
				}
				if (coverageTestFlag && coverageTestId == 26) {
					throw new AssertionFailedException("COVERAGE 26"); //$NON-NLS-1$
				}
			}
			else {
				int oldLength;
				if (vectorIndex >= (oldLength = this.extra[0].length)) {
					int newLength = vectorIndex + 1;
					for (int j = 0; j < extraLength; j++) {
						System.arraycopy(this.extra[j], 0, 
							(this.extra[j] = new long[newLength]), 0,
							oldLength);
					}
					if (coverageTestFlag && coverageTestId == 27) {
						throw new AssertionFailedException("COVERAGE 27"); //$NON-NLS-1$
					}
				}
			}
			if ((mask &
					this.extra[2][vectorIndex] &
					~this.extra[3][vectorIndex] &
					this.extra[4][vectorIndex] &
					~this.extra[5][vectorIndex]) == 0) {
				unknownAssigned = this.extra[2][vectorIndex] &
					~this.extra[3][vectorIndex] &
					this.extra[4][vectorIndex] &
					this.extra[5][vectorIndex];
				this.extra[3][vectorIndex]	 |= mask;
				this.extra[2][vectorIndex] &= (mask = ~mask);
				this.extra[4][vectorIndex] &= mask | ~unknownAssigned;
				this.extra[5][vectorIndex]	&= mask;
				if (coverageTestFlag && coverageTestId == 28) {
					this.extra[5][vectorIndex] = ~0;
				}
			}
		}
	}
}

/**
 * Record a definite assignment at a given position.
 */
final private void markAsDefinitelyAssigned(int position) {
	
	if (this != DEAD_END) {
		// position is zero-based
		if (position < BitCacheSize) {
			// use bits
			long mask;
			this.definiteInits |= (mask = 1L << position);
			this.potentialInits |= mask;
		} 
		else {
			// use extra vector
			int vectorIndex = (position / BitCacheSize) - 1;
			if (this.extra == null) {
				int length = vectorIndex + 1;
				this.extra = new long[extraLength][];
				for (int j = 0; j < extraLength; j++) {
					this.extra[j] = new long[length];
				}
			} 
			else {
				int oldLength; // might need to grow the arrays
				if (vectorIndex >= (oldLength = this.extra[0].length)) {
					for (int j = 0; j < extraLength; j++) {
						System.arraycopy(this.extra[j], 0, 
							(this.extra[j] = new long[vectorIndex + 1]), 0, 
							oldLength);
					}
				}
			}
			long mask;
			this.extra[0][vectorIndex] |= 
				(mask = 1L << (position % BitCacheSize));
			this.extra[1][vectorIndex] |= mask;
		}
	}
}

public void markAsDefinitelyAssigned(FieldBinding field) {
	if (this != DEAD_END)
		markAsDefinitelyAssigned(field.id);
}

public void markAsDefinitelyAssigned(LocalVariableBinding local) {
	if (this != DEAD_END)
		markAsDefinitelyAssigned(local.id + this.maxFieldCount);
}

/**
 * Record a definite non-null assignment at a given position.
 */
final private void markAsDefinitelyNonNull(int position) {
	// DEAD_END guarded above
	this.tagBits |= NULL_FLAG_MASK;
	long mask;
	// position is zero-based
	if (position < BitCacheSize) {
		// use bits
		this.nullAssignmentStatusBit1 |= (mask = 1L << position);
		this.nullAssignmentValueBit2 |= mask; // set non null
		this.nullAssignmentStatusBit2 &= ~mask; // clear protection
		this.nullAssignmentValueBit1 &= ~mask; // clear null
		if (coverageTestFlag && coverageTestId == 29) {
			this.nullAssignmentStatusBit1 = 0;
		}
	} 
	else {
		// use extra vector
		int vectorIndex = (position / BitCacheSize) - 1;
		this.extra[2][vectorIndex] |= 
			(mask = 1L << (position % BitCacheSize));
		this.extra[5][vectorIndex] |= mask;
		this.extra[3][vectorIndex] &= ~mask;
		this.extra[4][vectorIndex] &= ~mask;
		if (coverageTestFlag && coverageTestId == 30) {
			this.extra[5][vectorIndex] = ~0;
		}
	}
}

public void markAsDefinitelyNonNull(FieldBinding field) {
	if (this != DEAD_END) {
		markAsDefinitelyNonNull(field.id);
	}
}

public void markAsDefinitelyNonNull(LocalVariableBinding local) {
	// protected from non-object locals in calling methods
	if (this != DEAD_END) {
		markAsDefinitelyNonNull(local.id + this.maxFieldCount);
	}
}

/**
 * Record a definite null assignment at a given position.
 */
final private void markAsDefinitelyNull(int position) {
	// DEAD_END guarded above
	this.tagBits |= NULL_FLAG_MASK;
	long mask;
	if (position < BitCacheSize) {
		// use bits
		this.nullAssignmentStatusBit1 |= (mask = 1L << position); // set assignment
		this.nullAssignmentStatusBit2 &= ~mask; // clear protection
		this.nullAssignmentValueBit1 |= mask; // set null
		this.nullAssignmentValueBit2 &= ~mask; // clear non null
		if (coverageTestFlag && coverageTestId == 31) {
			this.nullAssignmentValueBit2 = ~0;
		}
	} 
	else {
		// use extra vector
		int vectorIndex = (position / BitCacheSize) - 1;
		this.extra[2][vectorIndex] |= 
			(mask = 1L << (position % BitCacheSize));
		this.extra[3][vectorIndex] &= ~mask;
		this.extra[4][vectorIndex] |= mask;
		this.extra[5][vectorIndex] &= ~mask;
		if (coverageTestFlag && coverageTestId == 32) {
			this.extra[5][vectorIndex] = ~0;
		}
	}
}

public void markAsDefinitelyNull(FieldBinding field) {
	if (this != DEAD_END) {
		markAsDefinitelyNull(field.id);
	}
}

public void markAsDefinitelyNull(LocalVariableBinding local) {
	// protected from non-object locals in calling methods
	if (this != DEAD_END) {
		markAsDefinitelyNull(local.id + this.maxFieldCount);
	}
}

/**
 * Mark a local as having been assigned to an unknown value.
 * @param local the local to mark
 */
// PREMATURE may try to get closer to markAsDefinitelyAssigned, but not
//			 obvious
public void markAsDefinitelyUnknown(LocalVariableBinding local) {
	// protected from non-object locals in calling methods
	if (this != DEAD_END) {
		this.tagBits |= NULL_FLAG_MASK;
		long mask;
		int position;
		// position is zero-based
		if ((position = local.id + this.maxFieldCount) < BitCacheSize) {
			// use bits
			this.nullAssignmentValueBit1 |= (mask = 1L << position);
			this.nullAssignmentValueBit2 |= mask;
			// set unknown
			this.nullAssignmentStatusBit1 |= mask;
			// set assignment
			this.nullAssignmentStatusBit2 &= ~mask;
			// clear protection
			if (coverageTestFlag && coverageTestId == 33) {
				this.nullAssignmentValueBit2 = ~0;
			}
		} 
		else {
			// use extra vector
			int vectorIndex = (position / BitCacheSize) - 1;
			this.extra[4][vectorIndex] |=
				(mask = 1L << (position % BitCacheSize));
			this.extra[5][vectorIndex] |= mask;
			this.extra[2][vectorIndex] |= mask;
			this.extra[3][vectorIndex] &= ~mask;
			if (coverageTestFlag && coverageTestId == 34) {
				this.extra[5][vectorIndex] = ~0;
			}
		}
	}
}

public UnconditionalFlowInfo mergedWith(UnconditionalFlowInfo otherInits) {
	if ((otherInits.tagBits & UNREACHABLE) != 0 && this != DEAD_END) {
		if (coverageTestFlag && coverageTestId == 35) {
			throw new AssertionFailedException("COVERAGE 35"); //$NON-NLS-1$
		}
		// DEAD_END + unreachable other -> other
		return this;
	}
	if ((this.tagBits & UNREACHABLE) != 0) {
		if (coverageTestFlag && coverageTestId == 36) {
			throw new AssertionFailedException("COVERAGE 36"); //$NON-NLS-1$
		}
		return (UnconditionalFlowInfo) otherInits.copy(); // make sure otherInits won't be affected
	} 
	
	// intersection of definitely assigned variables, 
	this.definiteInits &= otherInits.definiteInits;
	// union of potentially set ones
	this.potentialInits |= otherInits.potentialInits;

	// null combinations
	boolean otherHasNulls = (otherInits.tagBits & NULL_FLAG_MASK) != 0,
		thisHasNulls = false;
	long a1, a2, na2, a3, na3, a4, na4, b1, nb1, b2, nb2, b3, nb3, b4, nb4;
	if (otherHasNulls) {
		this.nullAssignmentStatusBit1 =
			(a1 = this.nullAssignmentStatusBit1) & 
			(b1 = otherInits.nullAssignmentStatusBit1) & (
				(nb4 = ~(b4 = otherInits.nullAssignmentValueBit2)) & 
				((b2 = otherInits.nullAssignmentStatusBit2) & 
						(nb3 = ~(b3 = otherInits.nullAssignmentValueBit1)) & 
						(na3 = ~(a3 = this.nullAssignmentValueBit1)) & 
						((a2 = this.nullAssignmentStatusBit2) & 
							(na4 = ~(a4 = this.nullAssignmentValueBit2)) | a4) |
						(na2 = ~a2) & a3 & na4 & (nb2 = ~b2) & b3 ) |
				b4 & (na3 & nb3 & (na4 & a2 | a4) |
						na2 & a4 & nb2));
		this.nullAssignmentStatusBit2 =
			a2 & b2 & ~(a1 ^ b1) & (na3 & nb3 | na4 & nb4) |
			a1 & b1 & (a2 ^ b2) & na3 & nb3 |
			(a1 & na2 & (nb1 = ~b1) & b2 | ~a1 & a2 & b1 & nb2) & na4 & nb4;
		this.nullAssignmentValueBit1 =
			b1 & nb2 & nb4 |
			~a1 & (a3 |
					a2 & na3 & (b1 | nb2)) |
			(a1 | na2) & nb1 & b2 & nb3 |
			nb1 & b3 |
			a1 & na2 & (na4 |
						b1 & nb2 & (a3 | b3));
		this.nullAssignmentValueBit2 =
			a4 | b4;
		
		// WORK recode if tests succeed
		this.nullAssignmentValueBit1 &= 
			~(a1 & na2 & na3 & a4 & nb1 & b2 & nb3 & nb4
					| ~a1 & a2 & na3 & na4 & b1 & nb2 & nb3 & b4);
		
		if (coverageTestFlag && coverageTestId == 37) {
			this.nullAssignmentValueBit2 = ~0;
		}
	}
	else {
		// tune potentials
		this.nullAssignmentValueBit1 =
			~(~this.nullAssignmentStatusBit1 &
					~this.nullAssignmentStatusBit2 &
					~this.nullAssignmentValueBit1) &
			~(this.nullAssignmentStatusBit1 & 
					(this.nullAssignmentStatusBit2 | this.nullAssignmentValueBit2));
		// reset assignment and protected
		this.nullAssignmentStatusBit1 = 
		this.nullAssignmentStatusBit2 = 0;
		if (coverageTestFlag && coverageTestId == 38) {
			this.nullAssignmentValueBit2 = ~0;
		}
	}
	thisHasNulls = this.nullAssignmentStatusBit1 != 0 || 
		this.nullAssignmentStatusBit2 != 0 ||
		this.nullAssignmentValueBit1 != 0 ||
		this.nullAssignmentValueBit2 != 0;

	// treating extra storage
	if (this.extra != null || otherInits.extra != null) {
		int mergeLimit = 0, copyLimit = 0, resetLimit = 0;
		if (this.extra != null) {
			if (otherInits.extra != null) {
				// both sides have extra storage
				int length, otherLength;
				if ((length = this.extra[0].length) < 
						(otherLength = otherInits.extra[0].length)) {
					// current storage is shorter -> grow current 
					for (int j = 0; j < extraLength; j++) {
						System.arraycopy(this.extra[j], 0, 
							(this.extra[j] = new long[otherLength]), 0, length);
					}
					mergeLimit = length;
					copyLimit = otherLength;
					if (coverageTestFlag && coverageTestId == 39) {
						throw new AssertionFailedException("COVERAGE 39"); //$NON-NLS-1$
					}
				} 
				else {
					// current storage is longer
					mergeLimit = otherLength;
					resetLimit = length;
					if (coverageTestFlag && coverageTestId == 40) {
						throw new AssertionFailedException("COVERAGE 40"); //$NON-NLS-1$
					}
				}
			} 
			else {
				resetLimit = this.extra[0].length;
				if (coverageTestFlag && coverageTestId == 41) {
					throw new AssertionFailedException("COVERAGE 41"); //$NON-NLS-1$
				}
			}
		} 
		else if (otherInits.extra != null) {
			// no storage here, but other has extra storage.
			int otherLength = otherInits.extra[0].length;
			this.extra = new long[extraLength][];
			for (int j = 0; j < extraLength; j++) {
				this.extra[j] = new long[otherLength];
			}
			System.arraycopy(otherInits.extra[1], 0, 
				this.extra[1], 0, otherLength);
			copyLimit = otherLength;
			if (coverageTestFlag && coverageTestId == 42) {
				throw new AssertionFailedException("COVERAGE 42"); //$NON-NLS-1$
			}
		}
		int i;
		if (otherHasNulls) {
			for (i = 0; i < mergeLimit; i++) {
				this.extra[2][i] =
					(a1 = this.extra[2][i]) & 
					(b1 = otherInits.extra[2][i]) & (
						(nb4 = ~(b4 = otherInits.extra[5][i])) & 
						((b2 = otherInits.extra[3][i]) & 
								(nb3 = ~(b3 = otherInits.extra[4][i])) & 
								(na3 = ~(a3 = this.extra[4][i])) & 
								((a2 = this.extra[3][i]) & 
									(na4 = ~(a4 = this.extra[5][i])) | a4) |
								(na2 = ~a2) & a3 & na4 & (nb2 = ~b2) & b3 ) |
						b4 & (na3 & nb3 & (na4 & a2 | a4) |
								na2 & a4 & nb2));
				this.extra[3][i] =
					a2 & b2 & ~(a1 ^ b1) & (na3 & nb3 | na4 & nb4) |
					a1 & b1 & (a2 ^ b2) & na3 & nb3 |
					(a1 & na2 & (nb1 = ~b1) & b2 | ~a1 & a2 & b1 & nb2) & na4 & nb4;
				this.extra[4][i] =
					b1 & nb2 & nb4 |
					~a1 & (a3 |
							a2 & na3 & (b1 | nb2)) |
					(a1 | na2) & nb1 & b2 & nb3 |
					nb1 & b3 |
					a1 & na2 & (na4 |
								b1 & nb2 & (a3 | b3));
				this.extra[5][i] =
					a4 | b4;

				// WORK recode if tests succeed
				this.extra[4][i] &= 
					~(a1 & na2 & na3 & a4 & nb1 & b2 & nb3 & nb4
							| ~a1 & a2 & na3 & na4 & b1 & nb2 & nb3 & b4);
		
				thisHasNulls = thisHasNulls ||
					this.extra[5][i] != 0 ||
					this.extra[2][i] != 0 ||
					this.extra[3][i] != 0 ||
					this.extra[4][i] != 0;
				if (coverageTestFlag && coverageTestId == 43) {
					this.extra[5][i] = ~0;
				}
			}
		}
		else {
			for (i = 0; i < mergeLimit; i++) {
				this.extra[0][i] &= 
					otherInits.extra[0][i];
				this.extra[1][i] |= 
					otherInits.extra[1][i];
				this.extra[4][i] =
					~(~this.extra[2][i] &
							~this.extra[3][i] &
							~this.extra[4][i]) &
					~(this.extra[2][i] & 
							(this.extra[3][i] | 
							this.extra[5][i]));
				this.extra[2][i] = 
				this.extra[3][i] = 0;
				thisHasNulls = thisHasNulls ||
					this.extra[4][i] != 0 ||
					this.extra[5][i] != 0;
				if (coverageTestFlag && coverageTestId == 44) {
					this.extra[5][i] = ~0;
				}
			}
		}
		for (; i < copyLimit; i++) {
			this.extra[1][i] = otherInits.extra[1][i];
			this.extra[4][i] =
				~(~otherInits.extra[2][i] &
					~otherInits.extra[3][i] &
					~otherInits.extra[4][i]) &
				~(otherInits.extra[2][i] & 
					(otherInits.extra[3][i] |
					otherInits.extra[5][i]));
			this.extra[5][i] = otherInits.extra[5][i];
			thisHasNulls = thisHasNulls ||
				this.extra[4][i] != 0 ||
				this.extra[5][i] != 0;
			if (coverageTestFlag && coverageTestId == 45) {
				this.extra[5][i] = ~0;
			}
		}
		for (; i < resetLimit; i++) {
			this.extra[4][i] =
				~(~this.extra[2][i] &
						~this.extra[3][i] &
						~this.extra[4][i]) &
				~(this.extra[2][i] & 
						(this.extra[3][i] | 
						this.extra[5][i]));
			this.extra[0][i] = 
			this.extra[2][i] = 
			this.extra[3][i] = 0;
			thisHasNulls = thisHasNulls ||
				this.extra[4][i] != 0 ||
				this.extra[5][i] != 0;
			if (coverageTestFlag && coverageTestId == 46) {
				this.extra[5][i] = ~0;
			}
		}
	}
	if (thisHasNulls) {
		this.tagBits |= NULL_FLAG_MASK;
	}
	else {
		this.tagBits &= ~NULL_FLAG_MASK;
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

public UnconditionalFlowInfo nullInfoLessUnconditionalCopy() {
	if (this == DEAD_END) {
		return this;
	}
	UnconditionalFlowInfo copy = new UnconditionalFlowInfo();
	copy.definiteInits = this.definiteInits;
	copy.potentialInits = this.potentialInits;
	copy.tagBits = this.tagBits & ~NULL_FLAG_MASK;
	copy.maxFieldCount = this.maxFieldCount;
	if (this.extra != null) {
		int length;
		copy.extra = new long[extraLength][];
		System.arraycopy(this.extra[0], 0, 
			(copy.extra[0] = 
				new long[length = this.extra[0].length]), 0, length);
		System.arraycopy(this.extra[1], 0, 
			(copy.extra[1] = new long[length]), 0, length);
		for (int j = 2; j < extraLength; j++) {
			copy.extra[j] = new long[length];
		}
	}
	return copy;
}

public FlowInfo safeInitsWhenTrue() {
	return copy();
}

public FlowInfo setReachMode(int reachMode) {
	if (reachMode == REACHABLE && this != DEAD_END) { // cannot modify DEAD_END
		this.tagBits &= ~UNREACHABLE;
	}
	else {
		if ((this.tagBits & UNREACHABLE) == 0) {
			// reset optional inits when becoming unreachable
			// see InitializationTest#test090 (and others)
			this.potentialInits = 0;
			if (this.extra != null) {
				for (int i = 0, length = this.extra[0].length; 
						i < length; i++) {
					this.extra[1][i] = 0;
				}
			}
		}				
		this.tagBits |= UNREACHABLE;
	}
	return this;
}

public String toString(){
	// PREMATURE consider printing bit fields as 0001 0001 1000 0001...
	if (this == DEAD_END){
		return "FlowInfo.DEAD_END"; //$NON-NLS-1$
	}
	if ((this.tagBits & NULL_FLAG_MASK) != 0) {
		if (this.extra == null) {
			return "FlowInfo<def: " + this.definiteInits //$NON-NLS-1$
				+", pot: " + this.potentialInits  //$NON-NLS-1$
				+ ", reachable:" + ((this.tagBits & UNREACHABLE) == 0) //$NON-NLS-1$
				+", nullS1: " + this.nullAssignmentStatusBit1 //$NON-NLS-1$
				+", nullS2: " + this.nullAssignmentStatusBit2 //$NON-NLS-1$
				+", nullV1: " + this.nullAssignmentValueBit1 //$NON-NLS-1$
				+", nullV2: " + this.nullAssignmentValueBit2 //$NON-NLS-1$
				+">"; //$NON-NLS-1$
		}
		else {
			String def = "FlowInfo<def:[" + this.definiteInits, //$NON-NLS-1$
				pot = "], pot:[" + this.potentialInits, //$NON-NLS-1$
				nullS1 = ", nullS1:[" + this.nullAssignmentStatusBit1, //$NON-NLS-1$
				nullS2 = "], nullS2:[" + this.nullAssignmentStatusBit2, //$NON-NLS-1$
				nullV1 = "], nullV1:[" + this.nullAssignmentValueBit1, //$NON-NLS-1$
				nullV2 = "], nullV2:[" + this.nullAssignmentValueBit2; //$NON-NLS-1$
			int i, ceil;
			for (i = 0, ceil = this.extra[0].length > 3 ? 
								3 : 
								this.extra[0].length;
				i < ceil; i++) {
				def += "," + this.extra[0][i]; //$NON-NLS-1$
				pot += "," + this.extra[1][i]; //$NON-NLS-1$
				nullS1 += "," + this.extra[2][i]; //$NON-NLS-1$
				nullS2 += "," + this.extra[3][i]; //$NON-NLS-1$
				nullV1 += "," + this.extra[4][i]; //$NON-NLS-1$
				nullV2 += "," + this.extra[5][i]; //$NON-NLS-1$
			}
			if (ceil < this.extra[0].length) {
				def += ",..."; //$NON-NLS-1$
				pot += ",..."; //$NON-NLS-1$
				nullS1 += ",..."; //$NON-NLS-1$
				nullS2 += ",..."; //$NON-NLS-1$
				nullV1 += ",..."; //$NON-NLS-1$
				nullV2 += ",..."; //$NON-NLS-1$
			}
			return def + pot 
				+ "], reachable:" + ((this.tagBits & UNREACHABLE) == 0) //$NON-NLS-1$
				+ nullS1 + nullS2 + nullV1 + nullV2
				+ "]>"; //$NON-NLS-1$
		}
	}
	else {
		if (this.extra == null) {
			return "FlowInfo<def: " + this.definiteInits //$NON-NLS-1$
				+", pot: " + this.potentialInits  //$NON-NLS-1$
				+ ", reachable:" + ((this.tagBits & UNREACHABLE) == 0) //$NON-NLS-1$
				+", no null info>"; //$NON-NLS-1$
		}
		else {
			String def = "FlowInfo<def:[" + this.definiteInits, //$NON-NLS-1$
				pot = "], pot:[" + this.potentialInits; //$NON-NLS-1$
			int i, ceil;
			for (i = 0, ceil = this.extra[0].length > 3 ? 
								3 : 
								this.extra[0].length;
				i < ceil; i++) {
				def += "," + this.extra[0][i]; //$NON-NLS-1$
				pot += "," + this.extra[1][i]; //$NON-NLS-1$
			}
			if (ceil < this.extra[0].length) {
				def += ",..."; //$NON-NLS-1$
				pot += ",..."; //$NON-NLS-1$
			}
			return def + pot 
				+ "], reachable:" + ((this.tagBits & UNREACHABLE) == 0) //$NON-NLS-1$
				+ ", no null info>"; //$NON-NLS-1$
		}
	}
}

public UnconditionalFlowInfo unconditionalCopy() {
	return (UnconditionalFlowInfo) copy();
}
	
public UnconditionalFlowInfo unconditionalFieldLessCopy() {
	// TODO (maxime) may consider leveraging null contribution verification as it is done in copy
	UnconditionalFlowInfo copy = new UnconditionalFlowInfo();
	copy.tagBits = this.tagBits;
	copy.maxFieldCount = this.maxFieldCount;
	int limit = this.maxFieldCount;
	if (limit < BitCacheSize) {
		long mask;
		copy.definiteInits = this.definiteInits & (mask = ~((1L << limit)-1));
		copy.potentialInits = this.potentialInits & mask;
		copy.nullAssignmentStatusBit1 = this.nullAssignmentStatusBit1 & mask;
		copy.nullAssignmentStatusBit2 = this.nullAssignmentStatusBit2 & mask;
		copy.nullAssignmentValueBit1 = this.nullAssignmentValueBit1 & mask;
		copy.nullAssignmentValueBit2 = this.nullAssignmentValueBit2 & mask;
	} 
	// use extra vector
	if (this.extra == null) {
		return copy; // if vector not yet allocated, then not initialized
	}
	int vectorIndex, length, copyStart;
	if ((vectorIndex = (limit / BitCacheSize) - 1) >= 
			(length = this.extra[0].length)) {
		return copy; // not enough room yet
	}
	long mask;
	copy.extra = new long[extraLength][];
	if ((copyStart = vectorIndex + 1) < length) {
		int copyLength = length - copyStart;
		for (int j = 0; j < extraLength; j++) {
			System.arraycopy(this.extra[j], copyStart, 
				(copy.extra[j] = new long[length]), copyStart, 
				copyLength);
		}
	}
	else if (vectorIndex >= 0) {
		for (int j = 0; j < extraLength; j++) {
			copy.extra[j] = new long[length];
		}
	}
	if (vectorIndex >= 0) {
		mask = ~((1L << (limit % BitCacheSize))-1);
		for (int j = 0; j < extraLength; j++) {
			copy.extra[j][vectorIndex] = 
				this.extra[j][vectorIndex] & mask;
		}
	}
	return copy;
}

public UnconditionalFlowInfo unconditionalInits() {
	// also see conditional inits, where it requests them to merge
	return this;
}

public UnconditionalFlowInfo unconditionalInitsWithoutSideEffect() {
	return this;
}
}

