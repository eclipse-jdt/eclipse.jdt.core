package org.eclipse.jdt.internal.core.nd;

/**
 * Holds a reference to a database entity that may be retained across read locks. In normal circumstances, it
 * is unsafe to retain a database address after a read lock is released since the object pointed to at that
 * address may have been deleted in the meantime. This class addresses this problem by remembering both the
 * address itself and enough information to determine whether that address is invalid and search for an
 * equivalent object if the original is lost.
 */
public class DatabaseRef<T extends NdNode> implements Supplier<T> {
	private final Nd nd;
	private T lastResult;
	private long writeCounter;
	private final Supplier<T> searchFunction;

	/**
	 * Constructs a new {@link DatabaseRef} that will search for its target using the given search function.
	 */
	public DatabaseRef(Nd nd, Supplier<T> searchFunction) {
		this.nd = nd;
		this.searchFunction = searchFunction;
		this.writeCounter = -1;
	}

	/**
	 * Constructs a new {@link DatabaseRef} that will search for its target using the given search function.
	 */
	public DatabaseRef(Nd nd, Supplier<T> searchFunction, T initialResult) {
		this.nd = nd;
		this.searchFunction = searchFunction;
		this.lastResult = initialResult;
		this.writeCounter = this.nd.getWriteNumber();
	}

	public T get() {
		long ndWriteNumber = this.nd.getWriteNumber();
		if (this.writeCounter == ndWriteNumber) {
			return this.lastResult;
		}

		T result = this.searchFunction.get();
		this.writeCounter = ndWriteNumber;
		this.lastResult = result;
		return result;
	}
}
