package org.eclipse.jdt.internal.compiler.problem;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
import org.eclipse.jdt.internal.compiler.*;

/*
 * Special unchecked exception type used 
 * to abort from the compilation process
 *
 * should only be thrown from within problem handlers.
 */
public class AbortMethod extends AbortType {
public AbortMethod(CompilationResult compilationResult) {
	super(compilationResult);
}
}
