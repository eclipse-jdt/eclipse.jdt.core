package org.eclipse.jdt.internal.core.builder;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
import org.eclipse.jdt.internal.core.Assert;
import java.util.*;

/**
 * A <code>MemoryBinaryBroker</code> caches binaries in memory only.
 * It is complete, in that it never forgets binaries which it has been given.
 */
public class MemoryBinaryBroker implements IBinaryBroker {

	Hashtable fTable = new Hashtable();
/**
 * @see IBinaryBroker
 */
public synchronized void close() {
	// NOP
}
/**
 * @see IBinaryBroker
 */
public synchronized void garbageCollect(Hashtable keysInUse) {
	Vector toRemove = new Vector();
	for (Enumeration e = fTable.keys(); e.hasMoreElements();) {
		BinaryBrokerKey key = (BinaryBrokerKey) e.nextElement();
		if (!keysInUse.containsKey(key)) {
			toRemove.addElement(key);
		}
	}
	for (Enumeration e = toRemove.elements(); e.hasMoreElements();) {
		BinaryBrokerKey key = (BinaryBrokerKey) e.nextElement();
		fTable.remove(key);
	}
}
/**
 * @see IBinaryBroker
 */
public synchronized byte[] getBinary(BinaryBrokerKey key) {
	return (byte[]) fTable.get(key);
}
/**
 * @see IBinaryBroker
 */
public synchronized void putBinary(BinaryBrokerKey key, byte[] bytes) {
	byte[] old = (byte[]) fTable.get(key);
	/* Sanity check that old is same as new */
	if (old != null) {
		Assert.isTrue(old.length == bytes.length);
		for (int i = old.length; --i >= 0;) {
			if (old[i] != bytes[i]) {
				Assert.isTrue(false);
			}
		}
	}
	fTable.put(key, bytes);
}
}
