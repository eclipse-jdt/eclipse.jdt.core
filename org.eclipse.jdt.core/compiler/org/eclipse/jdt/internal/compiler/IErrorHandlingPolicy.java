package org.eclipse.jdt.internal.compiler;

public interface IErrorHandlingPolicy {
	boolean proceedOnErrors();
	boolean stopOnFirstError();
}
