package org.eclipse.jdt.internal.core.builder.impl;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
import org.eclipse.jdt.internal.core.builder.IState;
import org.eclipse.jdt.internal.core.builder.IType;

/**
 * A <BinaryOutput> is the place where binaries coming from the image
 * builder are stored. 
 */
public abstract class BinaryOutput {

	private static final boolean COMPUTE_CRC = false; // disabled since unused	
/**
 * Stores the binary in a manner specific to this BinaryOutput.
 */
abstract protected void basicPutBinary(TypeStructureEntry tsEntry, byte[] binary, int crc);
/**
 * Computes a 32-bit CRC on the given binary.
 */
int crc32(byte[] binary) {
	java.util.zip.CRC32 crc = new java.util.zip.CRC32();
	crc.update(binary);
	return (int) crc.getValue();
}
/**
 * Deletes the binary previously produced for a type.
 */
public abstract void deleteBinary(IType type);
/**
 * Garbage collect any resources maintained by this binary
 * output which are no longer needed, given the states which
 * are still in use.
 */
public abstract void garbageCollect(IState[] statesInUse);
/**
 * Returns the binary previously produced for a type.
 * Returns null if the binary could not be found.
 */
public abstract byte[] getBinary(TypeStructureEntry tsEntry, IType type);
/**
 * Stores the binary produced by compiling a type.
 */
public void putBinary(TypeStructureEntry tsEntry, byte[] binary) {
	/* store crc in type structure entry */
	int crc;
	
	if (COMPUTE_CRC){
		crc = crc32(binary);
		tsEntry.setCRC32(crc);
	} else {
		crc = 0;
	}
	
	/* store binary */
	basicPutBinary(tsEntry, binary, crc);
}
/**
 * Deletes everything in this binary output.
 */
public abstract void scrubOutput();
}
