package org.eclipse.jdt.internal.core.builder;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
import java.util.Hashtable;

/**
 * A binary broker is responsible for maintaining the binaries produced by the image builder.
 * The image builder itself does not store or otherwise hang on to binaries it produces, but
 * delegates this responsibility to a binary broker.
 * <p>
 * The binary broker provides methods for storing and retrieving the binary for a given type.
 * The put and get methods are passed a key consisting of a non-state-specific type handle 
 * and a 32-bit CRC.
 * <p>
 * The CRC is used to identify the binary independently of the state which produced it,
 * and is remembered by the image builder.
 * <p>
 * The broker is not required to remember all binaries for all time, but should make a good effort
 * to remember many for as long as possible.  If the broker cannot supply the binary back to the
 * image builder, the builder must recompile the compilation unit which produced the type, which is
 * expensive.
 * <p>
 * Note that, due to lazy builds, the broker may receive put messages for some state after the build 
 * for that state has ended.
 * <p>
 * Although not included in this interface, the broker may provide methods for 
 * the environment to notify it when it is changing in interesting ways.  For example, new states
 * being created, states being dropped, etc.  It is of limited value to add these to the interface
 * between the broker and the builder since the builder cannot provide sufficient information
 * for the broker to manage its storage more effectively than a simple LRU cache.  
 * Only higher level components know when a state is being dropped.
 * Even with such notifications, storage management is not trivial, however, 
 * since the binaries for types from a dropped state may still be needed for other states 
 * derived from it.  It may be that a simple LRU cache is the best as well as the simplest strategy.
 *
 * @see IDevelopmentContext#setBinaryBroker()
 * @see IType#getBinary()
 * @see IHandle#nonStateSpecific()
 */
public interface IBinaryBroker {
	/**
	 * Ensures that all state kept by the binary broker has been saved.
	 * Subsequent operations are permitted after a close.
	 */
	void close();
	/**
	 * Removes unused binaries from the binary broker, given a set
	 * of BinaryBrokerKeys that are still in use.
	 *
	 * @param keysInUse <Hashtable(<BinaryBrokerKey> -> <BinaryBrokerKey>)>
	 */
	void garbageCollect(Hashtable keysInUse);
	/**
	 * Retrieve the binary for the given key (type and CRC).
	 * Return the binary if successful, or null if the binary could
	 * not be retrieved.
	 */
	byte[] getBinary(BinaryBrokerKey key);
	/**
	 * Remember the binary for the given key (type and CRC). 
	 */
	void putBinary(BinaryBrokerKey key, byte[] bytes);
}
