package org.eclipse.jdt.internal.core;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
import org.eclipse.jdt.internal.compiler.Compiler;
import org.eclipse.jdt.internal.compiler.*;
import org.eclipse.jdt.internal.compiler.env.*;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.*;
import org.eclipse.jdt.internal.compiler.impl.*;
import org.eclipse.jdt.internal.compiler.ast.*;
import org.eclipse.jdt.internal.compiler.lookup.PackageBinding;
import org.eclipse.jdt.internal.compiler.parser.SourceTypeConverter;
import org.eclipse.jdt.internal.compiler.problem.*;

import java.io.*;
import java.util.*;

public class CompilationUnitVisitor extends Compiler {
/**
 * Answer a new CompilationUnitVisitor using the given name environment and compiler options.
 * The environment and options will be in effect for the lifetime of the compiler.
 * When the compiler is run, compilation results are sent to the given requestor.
 *
 *  @param environment org.eclipse.jdt.internal.compiler.api.env.INameEnvironment
 *      Environment used by the compiler in order to resolve type and package
 *      names. The name environment implements the actual connection of the compiler
 *      to the outside world (e.g. in batch mode the name environment is performing
 *      pure file accesses, reuse previous build state or connection to repositories).
 *      Note: the name environment is responsible for implementing the actual classpath
 *            rules.
 *
 *  @param policy org.eclipse.jdt.internal.compiler.api.problem.IErrorHandlingPolicy
 *      Configurable part for problem handling, allowing the compiler client to
 *      specify the rules for handling problems (stop on first error or accumulate
 *      them all) and at the same time perform some actions such as opening a dialog
 *      in UI when compiling interactively.
 *      @see org.eclipse.jdt.internal.compiler.api.problem.DefaultErrorHandlingPolicies
 *      
 *  @param requestor org.eclipse.jdt.internal.compiler.api.ICompilerRequestor
 *      Component which will receive and persist all compilation results and is intended
 *      to consume them as they are produced. Typically, in a batch compiler, it is 
 *      responsible for writing out the actual .class files to the file system.
 *      @see org.eclipse.jdt.internal.compiler.api.CompilationResult
 *
 *  @param problemFactory org.eclipse.jdt.internal.compiler.api.problem.IProblemFactory
 *      Factory used inside the compiler to create problem descriptors. It allows the
 *      compiler client to supply its own representation of compilation problems in
 *      order to avoid object conversions. Note that the factory is not supposed
 *      to accumulate the created problems, the compiler will gather them all and hand
 *      them back as part of the compilation unit result.
 */
public CompilationUnitVisitor(
	INameEnvironment environment, 
	IErrorHandlingPolicy policy, 
	ConfigurableOption[] settings, 
	ICompilerRequestor requestor, 
	IProblemFactory problemFactory) {
		
	super(environment, policy, settings, requestor, problemFactory);
}
/**
 * Add an additional source type
 */
public void accept(ISourceType sourceType, PackageBinding packageBinding) {
	CompilationResult result = new CompilationResult(sourceType.getFileName(), 1, 1); // need to hold onto this
	CompilationUnitDeclaration unit =
		SourceTypeConverter.buildCompilationUnit(sourceType, true, true, lookupEnvironment.problemReporter, result);

	if (unit != null) {
		this.lookupEnvironment.buildTypeBindings(unit);
		this.lookupEnvironment.completeTypeBindings(unit, true);
	}
}
/*
 *  Low-level API performing the actual compilation
 */
protected static IErrorHandlingPolicy getHandlingPolicy() {

	// passes the initial set of files to the batch oracle (to avoid finding more than once the same units when case insensitive match)	
	return new IErrorHandlingPolicy() {
		public boolean stopOnFirstError() {
			return true;
		}
		public boolean proceedOnErrors() {
			return false; // stop if there are some errors 
		}
	};
}
protected static INameEnvironment getNameEnvironment(ICompilationUnit sourceUnit) throws JavaModelException {
	return (SearchableEnvironment) ((JavaProject) sourceUnit.getJavaProject())
				.getSearchableNameEnvironment();
}
/*
 *  Low-level API performing the actual compilation
 */
protected static ConfigurableOption[] getOptions() {
	CompilerOptions options = new CompilerOptions();
	return options.getConfigurableOptions(Locale.getDefault());
}
protected static IProblemFactory getProblemFactory(final IAbstractSyntaxTreeVisitor visitor) {

	return new DefaultProblemFactory(Locale.getDefault()){
		public IProblem createProblem(char[] originatingFileName, int problemId, String[] arguments, int severity, int startPosition, int endPosition, int lineNumber) {

			IProblem problem = super.createProblem(
				originatingFileName,
				problemId,
				arguments,
				severity,
				startPosition,
				endPosition,
				lineNumber);
			visitor.acceptProblem(problem);
			return problem;
		}
	};
}
/*
 * Answer the component to which will be handed back compilation results from the compiler
 */
protected static ICompilerRequestor getRequestor() {
	return new ICompilerRequestor() {
		public void acceptResult(CompilationResult compilationResult) {
		}
	};
}
public static void visit(ICompilationUnit unitElement, IAbstractSyntaxTreeVisitor visitor) throws JavaModelException {
	
	CompilationUnitVisitor compilationUnitVisitor = new CompilationUnitVisitor(
		getNameEnvironment(unitElement),
		getHandlingPolicy(),
		getOptions(),
		getRequestor(),
		getProblemFactory(visitor));

	
	CompilationUnitDeclaration unit = null;
	try {
		unit = compilationUnitVisitor.resolve(
				new BasicCompilationUnit(
					unitElement.getSource().toCharArray(), 
					unitElement.getElementName()));
		if (unit != null) {
			unit.traverse(visitor, unit.scope);
		}		
	} finally {
		if (unit != null) {
			unit.cleanUp();
		}
	}
}
}
