package org.eclipse.jdt.internal.compiler;
/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
import org.eclipse.jdt.internal.compiler.Compiler;

/**
 * A callback interface for receiving compilation results.
 */
public interface ICompilerRequestor {
	/**
	 * Accept a compilation result.
	 */
	public void acceptResult(CompilationResult result);
}
