package org.eclipse.jdt.internal.compiler;

public interface ICompilerRequestor {
	/**
	 * Accept a compilation result.
	 */
	public void acceptResult(CompilationResult result);
}
