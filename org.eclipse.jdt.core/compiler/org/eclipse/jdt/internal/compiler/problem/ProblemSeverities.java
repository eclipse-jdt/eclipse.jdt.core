package org.eclipse.jdt.internal.compiler.problem;

public interface ProblemSeverities {
	final int Ignore = -1; // during handling only
	final int Warning = 0; // during handling only

	final int Error = 1;
	// when bit is set: problem is error, if not it is a warning
	final int AbortCompilation = 2;
	final int AbortCompilationUnit = 4;
	final int AbortType = 8;
	final int AbortMethod = 16;
	final int Abort = 30; // 2r11110
	final int SecondaryError = 64;
}
