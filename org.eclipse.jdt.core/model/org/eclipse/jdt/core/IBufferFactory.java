package org.eclipse.jdt.core;
/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
import org.eclipse.jdt.internal.core.*; 

/**
 * A factory that creates <code>IBuffer</code>s for openables.
 * <p>
 * This interface may be implemented by clients.
 * </p>
 */
public interface IBufferFactory {

	/**
	 * Creates a buffer for the given owner.
	 * 
	 * @param owner the owner of the buffer
	 */
	IBuffer createBuffer(IOpenable owner);
}

