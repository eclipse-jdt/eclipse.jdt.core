package org.eclipse.jdt.internal.core.builder;

public interface IDeltaKey {
	/**
	 * Returns a new key describing the key's child with the given local name.
	 * This replaces the "/" syntax in the Smalltalk implementation
	 */
	DeltaKey add(Object localName);
	/**
	 * Returns the local name at the given index in the receiver
	 */
	Object at(int index);
	/**
	 * Returns the receiver's local name (the name of the last child).  If the
	 * receiver is a root key, answer null
	 */
	Object getLocalName();
	/**
	 * Returns true if the receiver is a prefix of the given key, false otherwise.
	 * Keys which are equal are considered to be prefixes of each other (so
	 * true is answered in this case).
	 */
	boolean isPrefixOf(DeltaKey key);
	/**
	 * Returns true if the receiver is the root key, false otherwise
	 */
	boolean isRoot();
	/**
	 * Returns the key describing the receiver's parent, or null if the receiver
	 * is the root key.
	 */
	DeltaKey parent();
	/**
	 * Returns a new key containing the first few local names in the
	 * receiver.
	 *
	 * @param count
	 *	number of local names to copy (length of prefix)
	 * @exception IllegalArgumentException
	 *	receiver contains fewer than count local names, or count is
	 *	negative."
	 */
	DeltaKey prefix(int count);
	/**
	 * Returns the branch length of the key
	 */
	int size();
	/**
	 * Returns a new key containing everything but the first few local
	 * names in the receiver.
	 *
	 * @param count
	 *	number of local names in the key to leave out (length of prefix to cut)
	 * @exception IllegalArgumentException
	 *	receiver contains fewer than count local names, or count is
	 *	negative.
	 */
	DeltaKey withoutPrefix(int count);
}
