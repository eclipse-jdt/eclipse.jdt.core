package org.eclipse.jdt.internal.compiler;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
import org.eclipse.jdt.internal.compiler.*;
import org.eclipse.jdt.internal.compiler.env.*;

import org.eclipse.jdt.internal.compiler.impl.*;
import org.eclipse.jdt.internal.compiler.ast.*;
import org.eclipse.jdt.internal.compiler.lookup.*;
import org.eclipse.jdt.internal.compiler.parser.*;
import org.eclipse.jdt.internal.compiler.problem.*;
import org.eclipse.jdt.internal.compiler.util.*;

import java.io.*;
import java.util.*;

public class Compiler implements ITypeRequestor, ProblemSeverities {
	public Parser parser;	
	ICompilerRequestor requestor;
	public CompilerOptions options;
	public ProblemReporter problemReporter;
	
	// management of unit to be processed
	//public CompilationUnitResult currentCompilationUnitResult;
	CompilationUnitDeclaration[] unitsToProcess;
	int totalUnits; // (totalUnits-1) gives the last unit in unitToProcess

	// name lookup
	public LookupEnvironment lookupEnvironment;

	// ONCE STABILIZED, THESE SHOULD RETURN TO A FINAL FIELD
	public static final boolean DEBUG = false; 
	public int parseThreshold = -1; // number of initial units parsed at once (-1: none)
/**
 * Answer a new compiler using the given name environment and compiler options.
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
public Compiler(
	INameEnvironment environment, 
	IErrorHandlingPolicy policy, 
	ConfigurableOption[] settings, 
	ICompilerRequestor requestor, 
	IProblemFactory problemFactory) {

	// create a problem handler given a handling policy
	this.options = new CompilerOptions(settings);
	this.requestor = requestor;
	this.problemReporter = 
		new ProblemReporter(
			policy, 
			this.options, 
			problemFactory);
	this.lookupEnvironment = new LookupEnvironment(this, options, problemReporter, environment);
	this.parser = 
		new Parser(problemReporter, this.options.parseLiteralExpressionsAsConstants, this.options.getAssertMode()); 
}
/**
 * Add an additional binary type
 */

public void accept(IBinaryType binaryType, PackageBinding packageBinding) {
	lookupEnvironment.createBinaryTypeFrom(binaryType, packageBinding);
}
/**
 * Add an additional compilation unit into the loop
 *  ->  build compilation unit declarations, their bindings and record their results.
 */

public void accept(ICompilationUnit sourceUnit) {
	// Switch the current policy and compilation result for this unit to the requested one.
	CompilationResult unitResult = 
		new CompilationResult(sourceUnit, totalUnits, totalUnits); 
	try {
		// diet parsing for large collection of unit
		CompilationUnitDeclaration parsedUnit;
		if (totalUnits < parseThreshold) {
			parsedUnit = parser.parse(sourceUnit, unitResult);
		} else {
			parsedUnit = parser.dietParse(sourceUnit, unitResult);
		}

		if (options.verbose) {
			System.out.println(Util.bind("compilation.request"/*nonNLS*/,new String[]{String.valueOf(totalUnits + 1),String.valueOf(totalUnits + 1),new String(sourceUnit.getFileName())})); 
		}

		// initial type binding creation
		lookupEnvironment.buildTypeBindings(parsedUnit);
		this.addCompilationUnit(sourceUnit, parsedUnit);

		// binding resolution
		lookupEnvironment.completeTypeBindings(parsedUnit);
	} catch (AbortCompilationUnit e) {
		// at this point, currentCompilationUnitResult may not be sourceUnit, but some other
		// one requested further along to resolve sourceUnit.
		if (unitResult.compilationUnit == sourceUnit) { // only report once
			requestor.acceptResult(unitResult.tagAsAccepted());
		} else {
			throw e; // want to abort enclosing request to compile
		}
	}
}
/**
 * Add an additional source type
 */

public void accept(ISourceType sourceType, PackageBinding packageBinding) {
	problemReporter.abortDueToInternalError(Util.bind("abort.againstSourceModel "/*nonNLS*/,String.valueOf(sourceType.getName()),String.valueOf(sourceType.getFileName())));
}
protected void addCompilationUnit(ICompilationUnit sourceUnit, CompilationUnitDeclaration parsedUnit) {

	// append the unit to the list of ones to process later on
	int size = unitsToProcess.length;
	if (totalUnits == size)	// when growing reposition units starting at position 0
		System.arraycopy(unitsToProcess, 0, (unitsToProcess = new CompilationUnitDeclaration[size * 2]), 0, totalUnits);
	unitsToProcess[totalUnits++] = parsedUnit;
}
/**
 * Add the initial set of compilation units into the loop
 *  ->  build compilation unit declarations, their bindings and record their results.
 */
protected void beginToCompile(ICompilationUnit[] sourceUnits) {
	int maxUnits = sourceUnits.length;
	totalUnits = 0; 
	unitsToProcess = new CompilationUnitDeclaration[maxUnits];

	// Switch the current policy and compilation result for this unit to the requested one.
	for (int i = 0; i < maxUnits; i++) {
		CompilationUnitDeclaration parsedUnit;
		CompilationResult unitResult = new CompilationResult(sourceUnits[i], i, maxUnits);
		try {
			// diet parsing for large collection of units
			if (totalUnits < parseThreshold)
			{	parsedUnit = parser.parse(sourceUnits[i], unitResult);}
			else
			{	parsedUnit = parser.dietParse(sourceUnits[i], unitResult);}
			if (options.verbose) {
				System.out.println(Util.bind("compilation.request"/*nonNLS*/,new String[]{String.valueOf(i+1),String.valueOf(maxUnits),new String(sourceUnits[i].getFileName())}));
			}
			// initial type binding creation
			lookupEnvironment.buildTypeBindings(parsedUnit);
			this.addCompilationUnit(sourceUnits[i], parsedUnit);
		//} catch (AbortCompilationUnit e) {
		//	requestor.acceptResult(unitResult.tagAsAccepted());
		} finally {
			sourceUnits[i] = null; // no longer hold onto the unit
		}
	}
	// binding resolution
	lookupEnvironment.completeTypeBindings();
}
/**
 * General API
 * -> compile each of supplied files
 * -> recompile any required types for which we have an incomplete principle structure
 */

public void compile(ICompilationUnit[] sourceUnits) {
	CompilationUnitDeclaration unit = null;
	int i = 0;
	try {
		// build and record parsed units

		beginToCompile(sourceUnits);

		// process all units (some more could be injected in the loop by the lookup environment)
		for (; i < totalUnits; i++) {
			unit = unitsToProcess[i];
			try {
				if (options.verbose) System.out.println(Util.bind("compilation.process"/*nonNLS*/,new String[]{String.valueOf(i + 1),String.valueOf(totalUnits),new String(unitsToProcess[i].getFileName())})); 
				process(unit, i);
			} finally {
				// cleanup compilation unit result
				unit.cleanUp();
				if (options.verbose) 
					System.out.println(Util.bind("compilation.done"/*nonNLS*/,new String[]{String.valueOf(i + 1),String.valueOf(totalUnits),new String(unitsToProcess[i].getFileName())}));  
			}
			unitsToProcess[i] = null; // release reference to processed unit declaration
			requestor.acceptResult(unit.compilationResult.tagAsAccepted());
		}
	} catch (AbortCompilation e) {
		this.handleInternalException(e, unit);
	} catch (Error e) {
		this.handleInternalException(e, unit, null);
		throw e; // rethrow
	} catch (RuntimeException e) {
		this.handleInternalException(e, unit, null);
		throw e; // rethrow
	} finally {
		this.reset();
	}
	if (options.verbose) {
		if (totalUnits > 1) {
			System.out.println(Util.bind("compilation.units"/*nonNLS*/,String.valueOf(totalUnits)));
		} else {
			System.out.println(Util.bind("compilation.unit"/*nonNLS*/,String.valueOf(totalUnits)));
		}
	}
}
/**
 * Answer an array of descriptions for the configurable options.
 * The descriptions may be changed and passed back to a different
 * compiler.
 *
 *  @return ConfigurableOption[] - array of configurable options
 */
public static ConfigurableOption[] getDefaultOptions(Locale locale) {
	return new CompilerOptions().getConfigurableOptions(locale);
}
protected void getMethodBodies(CompilationUnitDeclaration unit, int place) {
	//fill the methods bodies in order for the code to be generated
	
	if (unit.ignoreMethodBodies) {
		unit.ignoreFurtherInvestigation = true;
		return; // if initial diet parse did not work, no need to dig into method bodies.
	}
	
	if (place < parseThreshold)
		return; //work already done ...

	//real parse of the method....
	parser.scanner.setSourceBuffer(unit.compilationResult.compilationUnit.getContents());
	if (unit.types != null) {
		for (int i = unit.types.length; --i >= 0;)
			unit.types[i].parseMethod(parser, unit);
	}
}
/*
 * Compiler crash recovery in case of unexpected runtime exceptions
 */
protected void handleInternalException(Throwable internalException, CompilationUnitDeclaration unit, CompilationResult result) {

	/* dump a stack trace to the console */
	internalException.printStackTrace(); 
		
	/* find a compilation result */
	if ((unit != null)) // basing result upon the current unit if available
		result = unit.compilationResult; // current unit being processed ?
	if ((result == null) && (unitsToProcess != null) && (totalUnits > 0))
		result = unitsToProcess[totalUnits - 1].compilationResult; // last unit in beginToCompile ?
		
	if (result != null) {
		/* create and record a compilation problem */
		StringWriter stringWriter = new StringWriter();
		PrintWriter writer = new PrintWriter(stringWriter);
		internalException.printStackTrace(writer);
		StringBuffer buffer = stringWriter.getBuffer();

		result.record(
			problemReporter.createProblem(
				result.getFileName(), 
				ProblemIrritants.UnclassifiedProblem, 
				new String[] {Util.bind("compilation.internalError"/*nonNLS*/)+"\n"/*nonNLS*/ + buffer.toString()},
				Error, // severity
				0, // source start
				0, // source end
				0)); // line number		

		/* hand back the compilation result */
		if (!result.hasBeenAccepted) {
			requestor.acceptResult(result.tagAsAccepted());
		}
	}
}
/*
 * Compiler recovery in case of internal AbortCompilation event
 */
protected void handleInternalException(AbortCompilation abortException, CompilationUnitDeclaration unit){

	/* special treatment for SilentAbort: silently cancelling the compilation process */
	if (abortException.isSilent){
		if (abortException.silentException == null) {
			return;
		} else {
			throw abortException.silentException;
		}
	}
	
	/* uncomment following line to see where the abort came from */
	// abortException.printStackTrace(); 
		
	// Exception may tell which compilation result it is related, and which problem caused it
	CompilationResult result = abortException.compilationResult; 
	if ((result == null) && (unit != null)) result = unit.compilationResult; // current unit being processed ?
	if ((result == null) && (unitsToProcess != null) && (totalUnits > 0)) result = unitsToProcess[totalUnits - 1].compilationResult; // last unit in beginToCompile ?
	if (result != null && !result.hasBeenAccepted){
		/* distant problem which could not be reported back there */
		if (abortException.problemId != 0){ 
			result.record(
				problemReporter.createProblem(
					result.getFileName(),
					abortException.problemId, 
					abortException.problemArguments, 
					Error, // severity
					0, // source start
					0, // source end
					0)); // line number
		} else {
			/* distant internal exception which could not be reported back there */
			if (abortException.exception != null){
				this.handleInternalException(abortException.exception, 	null, result);
				return;
			}
		}
		/* hand back the compilation result */
		if (!result.hasBeenAccepted) { 
			requestor.acceptResult(result.tagAsAccepted());
		}
	} else {
		/*
		if (abortException.problemId != 0){ 
			IProblem problem =
				problemReporter.createProblem(
					"???".toCharArray(),
					abortException.problemId, 
					abortException.problemArguments, 
					Error, // severity
					0, // source start
					0, // source end
					0); // line number
			System.out.println(problem.getMessage());
		}
		*/
		abortException.printStackTrace();
	}
}
/**
 * Process a compilation unit already parsed and build.
 */
private void process(CompilationUnitDeclaration unit,int i) {

	getMethodBodies(unit,i);

	// fault in fields & methods
	if (unit.scope != null)
		unit.scope.faultInTypes();

	// verify inherited methods
	if (unit.scope != null)
		unit.scope.verifyMethods(lookupEnvironment.methodVerifier());

	// type checking
	long startTime = System.currentTimeMillis();
	unit.resolve();

	// flow analysis
	startTime = System.currentTimeMillis();
	unit.analyseCode();

	// code generation
	startTime = System.currentTimeMillis();
	unit.generateCode();

	// reference info
	if (options.produceReferenceInfo && unit.scope != null)
		unit.scope.storeDependencyInfo();

	// refresh the total number of units known at this stage
	unit.compilationResult.totalUnitsKnown = totalUnits;
}
public void reset(){
	lookupEnvironment.reset();
	parser.scanner.source = null;
	unitsToProcess	 = null;
}
/**
 * Internal API used to resolve a compilation unit minimally for code assist engine
 */

public CompilationUnitDeclaration resolve(ICompilationUnit sourceUnit) {
	CompilationUnitDeclaration unit = null;
	try {
		// build and record parsed units
		parseThreshold = 1; // will request a full parse
		beginToCompile(new ICompilationUnit[] { sourceUnit });
		// process all units (some more could be injected in the loop by the lookup environment)
		unit = unitsToProcess[0];
		//getMethodBodies(unit,i);
		if (unit.scope != null) {
			// fault in fields & methods
			unit.scope.faultInTypes();
			// type checking
			unit.resolve();
		}
		unitsToProcess[0] = null; // release reference to processed unit declaration
		requestor.acceptResult(unit.compilationResult.tagAsAccepted());
		return unit;
	} catch (AbortCompilation e) {
		this.handleInternalException(e, unit);
		return unit == null ? unitsToProcess[0] : unit;
	} catch (Error e) {
		this.handleInternalException(e, unit, null);
		throw e; // rethrow
	} catch (RuntimeException e) {
		this.handleInternalException(e, unit, null);
		throw e; // rethrow
	} finally {
		// No reset is performed there anymore since,
		// within the CodeAssist (or related tools),
		// the compiler may be called *after* a call
		// to this resolve(...) method. And such a call
		// needs to have a compiler with a non-empty
		// environment.
		// this.reset();
	}
}
}
