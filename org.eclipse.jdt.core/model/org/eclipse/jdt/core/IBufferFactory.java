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
 * @since 2.0
 */
public interface IBufferFactory {

	/**
	 * Creates a buffer for the given owner.
	 * The new buffer will be initialized with the contents
	 * of the owner (see ISourceReference#getSource()).
	 * 
	 * @param owner the owner of the buffer
	 */
	IBuffer createBuffer(IOpenable owner);
}

