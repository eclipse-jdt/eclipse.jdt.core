package org.eclipse.jdt.internal.core.builder.impl;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
import org.eclipse.jdt.internal.core.builder.BinaryBrokerKey;
import org.eclipse.jdt.internal.core.builder.IBinaryBroker;
import org.eclipse.jdt.internal.core.builder.IState;
import org.eclipse.jdt.internal.core.builder.IType;

import java.util.Hashtable;

/**
 * A <BinaryBrokerOutput> is a <BinaryOutput> that stores the
 * binaries using a binary broker.
 */
public class BinaryBrokerOutput extends BinaryOutput {
	IBinaryBroker fBinaryBroker;
	/**
	 * Creates a new BinaryBrokerOutput for the given binary broker.
	 */
	public BinaryBrokerOutput(IBinaryBroker broker) {
		fBinaryBroker = broker;
	}

	/**
	 * Stores the binary in a manner specific to this BinaryOutput.
	 */
	protected void basicPutBinary(
		TypeStructureEntry tsEntry,
		byte[] binary,
		int crc) {
		BinaryBrokerKey key = new BinaryBrokerKey(tsEntry.getType(), crc);
		fBinaryBroker.putBinary(key, binary);
	}

	/**
	 * @see BinaryOutput
	 */
	public void deleteBinary(IType type) {
		// Cannot delete a binary from a binary broker
	}

	/**
	 * @see BinaryOutput
	 *
	 * This releases any binaries in the binary broker
	 * which aren't needed for the given states.
	 */
	public void garbageCollect(IState[] statesInUse) {
		// estimate size of keysInUse set
		int size = 0;
		for (int i = 0; i < statesInUse.length; ++i) {
			StateImpl state = (StateImpl) statesInUse[i];
			size += state.getPrincipalStructureTable().size();
		}

		// collect keysInUse
		Hashtable keysInUse = new Hashtable(size * 2 + 1);
		for (int i = 0; i < statesInUse.length; ++i) {
			StateImpl state = (StateImpl) statesInUse[i];
			state.collectBinaryBrokerKeys(keysInUse);
		}

		// tell the broker to GC
		fBinaryBroker.garbageCollect(keysInUse);
	}

	/**
	 * @see BinaryOutput
	 */
	public byte[] getBinary(TypeStructureEntry tsEntry, IType type) {
		/* get cached CRC value */
		int crc = tsEntry.getCRC32();
		if (crc == 0) {
			// Never had the binary.
			return null;
		}
		BinaryBrokerKey key = new BinaryBrokerKey(type, crc);
		return fBinaryBroker.getBinary(key);
	}

	/**
	 * Returns the binary broker for this binary broker output.
	 */
	public IBinaryBroker getBinaryBroker() {
		return fBinaryBroker;
	}

	/**
	 * @see BinaryOutput
	 */
	public void scrubOutput() {
		// Cannot delete anything from a binary broker
	}

}
