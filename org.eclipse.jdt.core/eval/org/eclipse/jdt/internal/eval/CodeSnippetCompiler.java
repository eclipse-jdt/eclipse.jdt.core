package org.eclipse.jdt.internal.eval;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
import org.eclipse.jdt.internal.compiler.Compiler;
import org.eclipse.jdt.internal.compiler.*;
import org.eclipse.jdt.internal.compiler.env.INameEnvironment;

/**
 * A compiler that compiles code snippets. 
 */
public class CodeSnippetCompiler extends Compiler {
/**
 * Creates a new code snippet compiler initialized with a code snippet parser.
 */
public CodeSnippetCompiler(
		INameEnvironment environment, 
		IErrorHandlingPolicy policy, 
		ConfigurableOption[] settings, 
		ICompilerRequestor requestor, 
		IProblemFactory problemFactory,
		EvaluationContext evaluationContext,
		int codeSnippetStart,
		int codeSnippetEnd) {
	super(environment, policy, settings, requestor, problemFactory);
	this.parser = 
		new CodeSnippetParser(problemReporter, evaluationContext, this.options.parseLiteralExpressionsAsConstants, codeSnippetStart, codeSnippetEnd);
	this.parseThreshold = 1; // fully parse only the code snippet compilation unit
}
}
