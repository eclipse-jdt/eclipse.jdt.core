package org.eclipse.jdt.internal.compiler;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
/*
 * Factory used from inside the compiler to build the actual problems
 * which are handed back in the compilation result.
 *
 * This allows sharing the internal problem representation with the environment.
 *
 * Note: The factory is responsible for computing and storing a localized error message.
 */
import java.util.*;

public interface IProblemFactory {
	IProblem createProblem(
		char[] originatingFileName,
		int problemId,
		String[] arguments,
		int severity,
		int startPosition,
		int endPosition,
		int lineNumber);
	Locale getLocale();
	String getLocalizedMessage(int problemId, String[] problemArguments);
}
