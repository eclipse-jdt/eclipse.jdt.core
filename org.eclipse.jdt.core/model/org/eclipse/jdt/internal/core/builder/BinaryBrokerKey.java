package org.eclipse.jdt.internal.core.builder;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */

import org.eclipse.jdt.internal.core.Assert;

/**
 * A BinaryBrokerKey describes the type and CRC for which
 * binary has been given to the binary broker.
 *
 * @see IBinaryBroker
 */
public class BinaryBrokerKey {
	protected IType fType;
	protected int fCRC;

	/**
	 * Create a binary broker key for the given type and 32-bit CRC.
	 * The type handle must be non-state-specific.
	 */
	public BinaryBrokerKey(IType type, int crc) {
		Assert.isTrue(!type.isStateSpecific());
		fType = type;
		fCRC = crc;
	}
	public boolean equals(Object o) {
		if (this == o) return true;
		if (!(o instanceof BinaryBrokerKey)) return false;
		BinaryBrokerKey k = (BinaryBrokerKey) o;
		return fCRC == k.fCRC && fType.equals(k.fType);
	}
	/**
	 * Returns the CRC.
	 */
	public int getCRC() {
		return fCRC;
	}
	/**
	 * Returns the type.
	 */
	public IType getType() {
		return fType;
	}
	/**
	 * Returns the hash code.
	 */
	public int hashCode() {
		// The CRC should be unique enough for a hash code.
		return fCRC;
	}
}
