package org.eclipse.jdt.internal.core.builder.impl;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
import org.eclipse.jdt.internal.core.builder.IHandle;
import org.eclipse.jdt.internal.core.builder.IState;
import org.eclipse.jdt.internal.core.builder.IDevelopmentContext;

/**  
 * The root of the State Specific Handle implementation hierarchy.
 */
public abstract class StateSpecificHandleImpl implements IHandle {
	StateImpl fState; // The state
	//NonStateSpecificHandleImpl fHandle; 	//Put this in the subclasses
	/**
	 * Returns whether the receiver and anObject have the same handle and
	 * same wrapped object; i.e., same states and objects.
	 */
	public boolean equals(Object o) {
		if (this == o)
			return true;
		if (!(o instanceof StateSpecificHandleImpl))
			return false;

		StateSpecificHandleImpl ssh = (StateSpecificHandleImpl) o;
		return nonStateSpecific().equals(ssh.nonStateSpecific())
			&& fState.equals(ssh.fState);
	}

	public IDevelopmentContext getDevelopmentContext() {
		return fState.getDevelopmentContext();
	}

	public IState getState() {
		return fState;
	}

	public int hashCode() {
		return nonStateSpecific().hashCode();
	}

	public IHandle inState(IState state) {
		throw new org.eclipse.jdt.internal.core.builder.StateSpecificException();
	}

	public boolean isFictional() {
		return false;
	}

	public abstract boolean isPresent();
	public boolean isStateSpecific() {
		return true;
	}

	public int kind() {
		return nonStateSpecific().kind();
	}

	/**
	 * Return the non state specific handle associated with this handle
	 */
	public abstract IHandle nonStateSpecific();
	public String toString() {
		return nonStateSpecific().toString();
	}

}
