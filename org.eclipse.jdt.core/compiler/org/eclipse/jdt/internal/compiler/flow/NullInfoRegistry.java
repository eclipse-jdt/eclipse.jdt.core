/*******************************************************************************
 * Copyright (c) 2006 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jdt.internal.compiler.flow;

import org.eclipse.jdt.internal.compiler.lookup.LocalVariableBinding;

/**
 * A degenerate form of UnconditionalFlowInfo explicitly meant to capture
 * the effects of null related operations within try blocks. Given the fact
 * that a try block might exit at any time, a null related operation that
 * occurs within such a block mitigates whatever we know about the previous
 * null status of involved variables. NullInfoRegistry handles that
 * by negating upstream definite information that clashes with what a given
 * statement contends about the same variable. It also implements 
 * {@link #mitigateNullInfoOf(FlowInfo) mitigateNullInfo} so as to elaborate the
 * flow info presented in input of finally blocks.
 */
public class NullInfoRegistry extends UnconditionalFlowInfo {
	// encoding of null status at this level:
//	public long nullAssignmentStatusBit1;
	// assigned null
//	public long nullAssignmentStatusBit2;
	// assigned non null
//	public long nullAssignmentValueBit1;
	// assigned unknown
//	public long nullAssignmentValueBit2;
	// message send (no NPE)

// PREMATURE implement coverage and low level tests

/**
 * Make a new null info registry, using an upstream flow info. All definite
 * assignments of the upstream are carried forward, since a try block may
 * exit before its first statement.
 * @param upstream - UnconditionalFlowInfo: the flow info before we enter the
 * 		try block; only definite assignments are considered; this parameter is
 *  	not modified by this constructor
 */
public NullInfoRegistry(UnconditionalFlowInfo upstream) {
	if ((upstream.tagBits & NULL_FLAG_MASK) != 0) {
		long a1, a2, a3, b1nb2, b3, b4;
		a1 = this.nullAssignmentStatusBit1 = 
			(b1nb2 = upstream.nullAssignmentStatusBit1 
					&  ~upstream.nullAssignmentStatusBit2)
				& (b3 = upstream.nullAssignmentValueBit1)
				& ~(b4 = upstream.nullAssignmentValueBit2);
		a2 = this.nullAssignmentStatusBit2 =
			b1nb2 & ~b3 & b4;
		a3 = this.nullAssignmentValueBit1 =
			b1nb2 & b3 & b4;
		if ((a1 | a2 | a3) != 0) {
			this.tagBits |= NULL_FLAG_MASK;
		}
		if (upstream.extra != null) {
			this.extra = new long[extraLength][];
			int length= upstream.extra[2].length;
			for (int i = 2; i < extraLength; i++) {
				this.extra[i] = new long[length];
			}
			for (int i = 0; i < length; i++) {
				a1 = this.extra[2][i] = 
					(b1nb2 = upstream.extra[2][i] 
							& ~upstream.extra[3][i])
						& (b3 = upstream.extra[4][i])
						& ~(b4 = upstream.extra[5][i]);
				a2 = this.extra[3][i] =
					b1nb2 & ~b3 & b4;
				a3 = this.extra[4][i] =
					b1nb2 & b3 & b4;
				if ((a1 | a2 | a3) != 0) {
					this.tagBits |= NULL_FLAG_MASK;
				}
			}
		}
	}
}

/**
 * Add the information held by another NullInfoRegistry instance to this,
 * then return this.
 * @param other - NullInfoRegistry: the information to add to this
 * @return this, modified to carry the information held by other
 */
public NullInfoRegistry add(NullInfoRegistry other) {
	if ((other.tagBits & NULL_FLAG_MASK) == 0) {
		return this;
	}
	this.tagBits |= NULL_FLAG_MASK;
	this.nullAssignmentStatusBit1 |= other.nullAssignmentStatusBit1;
	this.nullAssignmentStatusBit2 |= other.nullAssignmentStatusBit2;
	this.nullAssignmentValueBit1 |= other.nullAssignmentValueBit1;
	this.nullAssignmentValueBit2 |= other.nullAssignmentValueBit2;
	if (other.extra != null) {
		if (this.extra == null) {
			this.extra = new long[extraLength][];
			for (int i = 2, length = other.extra[2].length; i < extraLength; i++) {
				System.arraycopy(other.extra[i], 0, 
					(this.extra[i] = new long[length]), 0, length);
			}
		} else {
			int length = this.extra[2].length, otherLength = other.extra[2].length;
			if (otherLength > length) {
				for (int i = 2; i < extraLength; i++) {
					System.arraycopy(this.extra[i], 0, 
						(this.extra[i] = new long[otherLength]), 0, length);
					System.arraycopy(other.extra[i], length, 
						this.extra[i], length, otherLength - length);
				}
			} else if (otherLength < length) {
				length = otherLength;
			}
			for (int i = 2; i < extraLength; i++) {
				for (int j = 0; j < length; j++) {
					this.extra[i][j] |= other.extra[i][j];
				}
			}
		}
	}
	return this;
}

public void markAsComparedEqualToNonNull(LocalVariableBinding local) {
	this.tagBits |= NULL_FLAG_MASK;
	int position;
	// position is zero-based
	if ((position = local.id + this.maxFieldCount) < BitCacheSize) {
		// use bits
		this.nullAssignmentValueBit2 |= (1L << position);
	} 
	else {
		// use extra vector
		int vectorIndex = (position / BitCacheSize) - 1;
		if (this.extra == null) {
			int length = vectorIndex + 1;
			this.extra = new long[extraLength][];
			for (int j = 2 /* do not care about non null info */;
					j < extraLength; j++) {
				this.extra[j] = new long[length];
			}
		}
		else {
			int oldLength;
			if (vectorIndex >= (oldLength = this.extra[0].length)) {
				int newLength = vectorIndex + 1;
				for (int j = 2 /* do not care about non null info */; 
						j < extraLength; j++) {
					System.arraycopy(this.extra[j], 0, 
						(this.extra[j] = new long[newLength]), 0, 
						oldLength);
				}
			}
		}
		this.extra[5][vectorIndex] |= (1L << (position % BitCacheSize));
	}
}

public void markAsDefinitelyNonNull(LocalVariableBinding local) {
	this.tagBits |= NULL_FLAG_MASK;
	int position;
	// position is zero-based
	if ((position = local.id + this.maxFieldCount) < BitCacheSize) {
		// use bits
		this.nullAssignmentStatusBit2 |= (1L << position);
	} 
	else {
		// use extra vector
		int vectorIndex = (position / BitCacheSize) - 1;
		if (this.extra == null) {
			int length = vectorIndex + 1;
			this.extra = new long[extraLength][];
			for (int j = 2 /* do not care about non null info */; 
					j < extraLength; j++) {
				this.extra[j] = new long[length];
			}
		}
		else {
			int oldLength;
			if (vectorIndex >= (oldLength = this.extra[0].length)) {
				int newLength = vectorIndex + 1;
				for (int j = 2 /* do not care about non null info */; 
						j < extraLength; j++) {
					System.arraycopy(this.extra[j], 0, 
						(this.extra[j] = new long[newLength]), 0, 
						oldLength);
				}
			}
		}
		this.extra[3][vectorIndex] |= (1L << (position % BitCacheSize));
	}
}

public void markAsDefinitelyNull(LocalVariableBinding local) {
	this.tagBits |= NULL_FLAG_MASK;
	int position;
	// position is zero-based
	if ((position = local.id + this.maxFieldCount) < BitCacheSize) {
		// use bits
		this.nullAssignmentStatusBit1 |= (1L << position);
	} 
	else {
		// use extra vector
		int vectorIndex = (position / BitCacheSize) - 1;
		if (this.extra == null) {
			int length = vectorIndex + 1;
			this.extra = new long[extraLength][];
			for (int j = 2 /* do not care about non null info */;
					j < extraLength; j++) {
				this.extra[j] = new long[length];
			}
		}
		else {
			int oldLength;
			if (vectorIndex >= (oldLength = this.extra[0].length)) {
				int newLength = vectorIndex + 1;
				for (int j = 2 /* do not care about non null info */; 
						j < extraLength; j++) {
					System.arraycopy(this.extra[j], 0, 
						(this.extra[j] = new long[newLength]), 0, 
						oldLength);
				}
			}
		}
		this.extra[2][vectorIndex] |= (1L << (position % BitCacheSize));
	}
}

public void markAsDefinitelyUnknown(LocalVariableBinding local) {
	this.tagBits |= NULL_FLAG_MASK;
	int position;
	// position is zero-based
	if ((position = local.id + this.maxFieldCount) < BitCacheSize) {
		// use bits
		this.nullAssignmentValueBit1 |= (1L << position);
	} 
	else {
		// use extra vector
		int vectorIndex = (position / BitCacheSize) - 1;
		if (this.extra == null) {
			int length = vectorIndex + 1;
			this.extra = new long[extraLength][];
			for (int j = 2 /* do not care about non null info */;
					j < extraLength; j++) {
				this.extra[j] = new long[length];
			}
		}
		else {
			int oldLength;
			if (vectorIndex >= (oldLength = this.extra[0].length)) {
				int newLength = vectorIndex + 1;
				for (int j = 2 /* do not care about non null info */; 
						j < extraLength; j++) {
					System.arraycopy(this.extra[j], 0, 
						(this.extra[j] = new long[newLength]), 0, 
						oldLength);
				}
			}
		}
		this.extra[4][vectorIndex] |= (1L << (position % BitCacheSize));
	}
}

/**
 * Mitigate the definite and protected info of flowInfo, depending on what 
 * this null info registry knows about potential assignments and messages
 * sends involving locals. May return flowInfo unchanged, or a modified,
 * fresh copy of flowInfo.
 * @param flowInfo - FlowInfo: the flow information that this null info
 * 		registry may mitigate
 * @return a copy of flowInfo carrying mitigated information, or else
 * 		flowInfo unchanged
 */
public UnconditionalFlowInfo mitigateNullInfoOf(FlowInfo flowInfo) {
	if ((this.tagBits & NULL_FLAG_MASK) == 0) {
		return flowInfo.unconditionalInits();
	}
//	// Reference implementation
//	UnconditionalFlowInfo source = flowInfo.unconditionalCopy();
//	long mask;
//	// clear uncompatible protections
//	mask = source.nullAssignmentStatusBit1 & source.nullAssignmentStatusBit2
//			// prot. non null
//		& (this.nullAssignmentStatusBit1 | this.nullAssignmentValueBit1);
//			// null or unknown
//	source.nullAssignmentStatusBit1 &= ~mask;
//	source.nullAssignmentStatusBit2 &= ~mask;
//	mask = ~source.nullAssignmentStatusBit1 & source.nullAssignmentStatusBit2
//			// prot. null
//		& (this.nullAssignmentStatusBit2 | this.nullAssignmentValueBit1
//				| this.nullAssignmentValueBit2);
//			// non null or unknown
//	source.nullAssignmentStatusBit2 &= ~mask;
//	// clear uncompatible assignments
//	mask = source.nullAssignmentStatusBit1 & ~source.nullAssignmentStatusBit2
//		& (source.nullAssignmentValueBit1 & ~source.nullAssignmentValueBit2 
//				& (this.nullAssignmentStatusBit2 | this.nullAssignmentValueBit1
//						| this.nullAssignmentValueBit2)
//			| ~source.nullAssignmentValueBit1 & source.nullAssignmentValueBit2
//				& (this.nullAssignmentStatusBit1 | this.nullAssignmentValueBit1)
//			| source.nullAssignmentValueBit1 & source.nullAssignmentValueBit2
//				& (this.nullAssignmentStatusBit1));
//	source.nullAssignmentStatusBit1 &= ~mask;
	long m1, m2, m3, a1, a2, a3, a4, s1, s2, s3, s4;
	boolean newCopy = false;
	UnconditionalFlowInfo source = flowInfo.unconditionalInits();
	// clear uncompatible protections
	m1 = (s1 = source.nullAssignmentStatusBit1) 
			& (s2 = source.nullAssignmentStatusBit2)
			// prot. non null
		& ((a1 = this.nullAssignmentStatusBit1)
				| (a3 = this.nullAssignmentValueBit1));
			// null or unknown
	m2 = ~s1 & s2
			// prot. null
		& ((a2 = this.nullAssignmentStatusBit2) | a3
				| (a4 = this.nullAssignmentValueBit2));
			// non null or unknown
	// clear uncompatible assignments
	m3 = s1 & ~s2
		& ((s3 = source.nullAssignmentValueBit1) 
				& ~(s4 = source.nullAssignmentValueBit2) 
				& (a2 | a3 | a4)
					| s4 & (~s3 & a3 | a1));
	if ((m1 | m2 | m3) != 0) {
		newCopy = true;
		source = source.unconditionalCopy();
		source.nullAssignmentStatusBit1 &= ~(m1 | m3);
		source.nullAssignmentStatusBit2 &= ~(m1 | m2);
	}
	if (this.extra != null && source.extra != null) {
		int length = this.extra[2].length, sourceLength = source.extra[0].length;
		if (sourceLength < length) {
			length = sourceLength;
		}
		for (int i = 0; i < length; i++) {
			// clear uncompatible protections
			m1 = (s1 = source.extra[2][i]) & (s2 = source.extra[3][i])
					// prot. non null
				& ((a1 = this.extra[2][i]) | (a3 = this.extra[4][i]));
					// null or unknown
			m2 = ~s1 & s2
					// prot. null
				& ((a2 = this.extra[3][i]) | a3
						| (a4 = this.extra[5][i]));
					// non null or unknown
			// clear uncompatible assignments
			m3 = s1 & ~s2
				& ((s3 = source.extra[4][i]) & ~(s4 = source.extra[5][i]) 
						& (a2 | a3 | a4)
					| s4 & (~s3 & a3 | a1));
			if ((m1 | m2 | m3) != 0) {
				if (!newCopy) {
					newCopy = true;
					source = source.unconditionalCopy();
				}
				source.extra[2][i] &= ~(m1 | m3);
				source.extra[3][i] &= ~(m1 | m2);
			}
		}
	}
	return source;
}

public String toString(){
	if (this.extra == null) {
		return "NullInfoRegistry<nullS1: " + this.nullAssignmentStatusBit1 //$NON-NLS-1$
			+", nullS2: " + this.nullAssignmentStatusBit2 //$NON-NLS-1$
			+", nullV1: " + this.nullAssignmentValueBit1 //$NON-NLS-1$
			+", nullV2: " + this.nullAssignmentValueBit2 //$NON-NLS-1$
			+">"; //$NON-NLS-1$
	}
	else {
		String nullS1 = "NullInfoRegistry<nullS1:[" + this.nullAssignmentStatusBit1, //$NON-NLS-1$
			nullS2 = "], nullS2:[" + this.nullAssignmentStatusBit2, //$NON-NLS-1$
			nullV1 = "], nullV1:[" + this.nullAssignmentValueBit1, //$NON-NLS-1$
			nullV2 = "], nullV2:[" + this.nullAssignmentValueBit2; //$NON-NLS-1$
		int i, ceil;
		for (i = 0, ceil = this.extra[0].length > 3 ? 
							3 : 
							this.extra[0].length;
			i < ceil; i++) {
			nullS1 += "," + this.extra[2][i]; //$NON-NLS-1$
			nullS2 += "," + this.extra[3][i]; //$NON-NLS-1$
			nullV1 += "," + this.extra[4][i]; //$NON-NLS-1$
			nullV2 += "," + this.extra[5][i]; //$NON-NLS-1$
		}
		if (ceil < this.extra[0].length) {
			nullS1 += ",..."; //$NON-NLS-1$
			nullS2 += ",..."; //$NON-NLS-1$
			nullV1 += ",..."; //$NON-NLS-1$
			nullV2 += ",..."; //$NON-NLS-1$
		}
		return nullS1 + nullS2 + nullV1 + nullV2
			+ "]>"; //$NON-NLS-1$
	}
}
}

