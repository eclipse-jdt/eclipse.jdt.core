/*******************************************************************************
 * Copyright (c) 2000, 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jdt.internal.compiler;

import org.eclipse.jdt.core.compiler.*;
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
	public ICompilerRequestor requestor;
	public CompilerOptions options;
	public ProblemReporter problemReporter;

	// management of unit to be processed
	//public CompilationUnitResult currentCompilationUnitResult;
	public CompilationUnitDeclaration[] unitsToProcess;
	public int totalUnits; // (totalUnits-1) gives the last unit in unitToProcess

	// name lookup
	public LookupEnvironment lookupEnvironment;

	// ONCE STABILIZED, THESE SHOULD RETURN TO A FINAL FIELD
	public static boolean DEBUG = false;
	public int parseThreshold = -1;
	// number of initial units parsed at once (-1: none)

	/*
	 * Static requestor reserved to listening compilation results in debug mode,
	 * so as for example to monitor compiler activity independantly from a particular
	 * builder implementation. It is reset at the end of compilation, and should not 
	 * persist any information after having been reset.
	 */
	public static IDebugRequestor DebugRequestor = null;

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
	 *      @see org.eclipse.jdt.internal.compiler.DefaultErrorHandlingPolicies
	 *      
	 *  @param requestor org.eclipse.jdt.internal.compiler.api.ICompilerRequestor
	 *      Component which will receive and persist all compilation results and is intended
	 *      to consume them as they are produced. Typically, in a batch compiler, it is 
	 *      responsible for writing out the actual .class files to the file system.
	 *      @see org.eclipse.jdt.internal.compiler.CompilationResult
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
		Map settings,
		final ICompilerRequestor requestor,
		IProblemFactory problemFactory) {

		// create a problem handler given a handling policy
		this.options = new CompilerOptions(settings);
		
		// wrap requestor in DebugRequestor if one is specified
		if(DebugRequestor == null) {
			this.requestor = requestor;
		} else {
			this.requestor = new ICompilerRequestor(){
				public void acceptResult(CompilationResult result){
					if (DebugRequestor.isActive()){
						DebugRequestor.acceptDebugResult(result);
					}
					requestor.acceptResult(result);
				}
			};
		}
		this.problemReporter =
			new ProblemReporter(policy, this.options, problemFactory);
		this.lookupEnvironment =
			new LookupEnvironment(this, options, problemReporter, environment);
		initializeParser();
	}
	
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
	 *      @see org.eclipse.jdt.internal.compiler.DefaultErrorHandlingPolicies
	 *      
	 *  @param requestor org.eclipse.jdt.internal.compiler.api.ICompilerRequestor
	 *      Component which will receive and persist all compilation results and is intended
	 *      to consume them as they are produced. Typically, in a batch compiler, it is 
	 *      responsible for writing out the actual .class files to the file system.
	 *      @see org.eclipse.jdt.internal.compiler.CompilationResult
	 *
	 *  @param problemFactory org.eclipse.jdt.internal.compiler.api.problem.IProblemFactory
	 *      Factory used inside the compiler to create problem descriptors. It allows the
	 *      compiler client to supply its own representation of compilation problems in
	 *      order to avoid object conversions. Note that the factory is not supposed
	 *      to accumulate the created problems, the compiler will gather them all and hand
	 *      them back as part of the compilation unit result.
	 *	@param parseLiteralExpressionsAsConstants <code>boolean</code>
	 *		This parameter is used to optimize the literals or leave them as they are in the source.
	 * 		If you put true, "Hello" + " world" will be converted to "Hello world".
	 */
	public Compiler(
		INameEnvironment environment,
		IErrorHandlingPolicy policy,
		Map settings,
		final ICompilerRequestor requestor,
		IProblemFactory problemFactory,
		boolean parseLiteralExpressionsAsConstants) {

		// create a problem handler given a handling policy
		this.options = new CompilerOptions(settings);
		
		// wrap requestor in DebugRequestor if one is specified
		if(DebugRequestor == null) {
			this.requestor = requestor;
		} else {
			this.requestor = new ICompilerRequestor(){
				public void acceptResult(CompilationResult result){
					if (DebugRequestor.isActive()){
						DebugRequestor.acceptDebugResult(result);
					}
					requestor.acceptResult(result);
				}
			};
		}
		this.problemReporter = new ProblemReporter(policy, this.options, problemFactory);
		this.lookupEnvironment = new LookupEnvironment(this, options, problemReporter, environment);
		initializeParser();
	}
	
	/**
	 * Add an additional binary type
	 */
	public void accept(IBinaryType binaryType, PackageBinding packageBinding) {
		if (options.verbose) {
			System.out.println(
				Util.bind(
					"compilation.loadBinary" , //$NON-NLS-1$
					new String[] {
						new String(binaryType.getName())}));
//			new Exception("TRACE BINARY").printStackTrace(System.out);
//		    System.out.println();
		}
		lookupEnvironment.createBinaryTypeFrom(binaryType, packageBinding);
	}

	/**
	 * Add an additional compilation unit into the loop
	 *  ->  build compilation unit declarations, their bindings and record their results.
	 */
	public void accept(ICompilationUnit sourceUnit) {
		// Switch the current policy and compilation result for this unit to the requested one.
		CompilationResult unitResult =
			new CompilationResult(sourceUnit, totalUnits, totalUnits, this.options.maxProblemsPerUnit);
		try {
			if (options.verbose) {
				String count = String.valueOf(totalUnits + 1);
				System.out.println(
					Util.bind(
						"compilation.request" , //$NON-NLS-1$
						new String[] {
							count,
							count,
							new String(sourceUnit.getFileName())}));
			}
			// diet parsing for large collection of unit
			CompilationUnitDeclaration parsedUnit;
			if (totalUnits < parseThreshold) {
				parsedUnit = parser.parse(sourceUnit, unitResult);
			} else {
				parsedUnit = parser.dietParse(sourceUnit, unitResult);
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
	 * Add additional source types
	 */
	public void accept(ISourceType[] sourceTypes, PackageBinding packageBinding) {
		problemReporter.abortDueToInternalError(
			Util.bind(
				"abort.againstSourceModel" , //$NON-NLS-1$
				String.valueOf(sourceTypes[0].getName()),
				String.valueOf(sourceTypes[0].getFileName())));
	}

	protected void addCompilationUnit(
		ICompilationUnit sourceUnit,
		CompilationUnitDeclaration parsedUnit) {

		// append the unit to the list of ones to process later on
		int size = unitsToProcess.length;
		if (totalUnits == size)
			// when growing reposition units starting at position 0
			System.arraycopy(
				unitsToProcess,
				0,
				(unitsToProcess = new CompilationUnitDeclaration[size * 2]),
				0,
				totalUnits);
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
			CompilationResult unitResult =
				new CompilationResult(sourceUnits[i], i, maxUnits, this.options.maxProblemsPerUnit);
			try {
				if (options.verbose) {
					System.out.println(
						Util.bind(
							"compilation.request" , //$NON-NLS-1$
							new String[] {
								String.valueOf(i + 1),
								String.valueOf(maxUnits),
								new String(sourceUnits[i].getFileName())}));
				}
				// diet parsing for large collection of units
				if (totalUnits < parseThreshold) {
					parsedUnit = parser.parse(sourceUnits[i], unitResult);
				} else {
					parsedUnit = parser.dietParse(sourceUnits[i], unitResult);
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
					if (options.verbose)
						System.out.println(
							Util.bind(
								"compilation.process" , //$NON-NLS-1$
								new String[] {
									String.valueOf(i + 1),
									String.valueOf(totalUnits),
									new String(unitsToProcess[i].getFileName())}));
					process(unit, i);
				} finally {
					// cleanup compilation unit result
					unit.cleanUp();
				}
				unitsToProcess[i] = null; // release reference to processed unit declaration
				requestor.acceptResult(unit.compilationResult.tagAsAccepted());
				if (options.verbose)
					System.out.println(Util.bind("compilation.done", //$NON-NLS-1$
				new String[] {
					String.valueOf(i + 1),
					String.valueOf(totalUnits),
					new String(unit.getFileName())}));
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
				System.out.println(
					Util.bind("compilation.units" , String.valueOf(totalUnits))); //$NON-NLS-1$
			} else {
				System.out.println(
					Util.bind("compilation.unit" , String.valueOf(totalUnits))); //$NON-NLS-1$
			}
		}
	}

	/*
	 * Compiler crash recovery in case of unexpected runtime exceptions
	 */
	protected void handleInternalException(
		Throwable internalException,
		CompilationUnitDeclaration unit,
		CompilationResult result) {

		/* find a compilation result */
		if ((unit != null)) // basing result upon the current unit if available
			result = unit.compilationResult; // current unit being processed ?
		if ((result == null) && (unitsToProcess != null) && (totalUnits > 0))
			result = unitsToProcess[totalUnits - 1].compilationResult;
		// last unit in beginToCompile ?

		boolean needToPrint = true;
		if (result != null) {
			/* create and record a compilation problem */
			StringWriter stringWriter = new StringWriter();
			PrintWriter writer = new PrintWriter(stringWriter);
			internalException.printStackTrace(writer);
			StringBuffer buffer = stringWriter.getBuffer();

			String[] pbArguments = new String[] {
				Util.bind("compilation.internalError" ) //$NON-NLS-1$
					+ "\n"  //$NON-NLS-1$
					+ buffer.toString()};

			result
				.record(
					problemReporter
					.createProblem(
						result.getFileName(),
						IProblem.Unclassified,
						pbArguments,
						pbArguments,
						Error, // severity
						0, // source start
						0, // source end
						0), // line number		
					unit);

			/* hand back the compilation result */
			if (!result.hasBeenAccepted) {
				requestor.acceptResult(result.tagAsAccepted());
				needToPrint = false;
			}
		}
		if (needToPrint) {
			/* dump a stack trace to the console */
			internalException.printStackTrace();
		}
	}

	/*
	 * Compiler recovery in case of internal AbortCompilation event
	 */
	protected void handleInternalException(
		AbortCompilation abortException,
		CompilationUnitDeclaration unit) {

		/* special treatment for SilentAbort: silently cancelling the compilation process */
		if (abortException.isSilent) {
			if (abortException.silentException == null) {
				return;
			}
			throw abortException.silentException;
		}

		/* uncomment following line to see where the abort came from */
		// abortException.printStackTrace(); 

		// Exception may tell which compilation result it is related, and which problem caused it
		CompilationResult result = abortException.compilationResult;
		if ((result == null) && (unit != null)) {
			result = unit.compilationResult; // current unit being processed ?
		}
		// Lookup environment may be in middle of connecting types
		if ((result == null) && lookupEnvironment.unitBeingCompleted != null) {
		    result = lookupEnvironment.unitBeingCompleted.compilationResult;
		}
		if ((result == null) && (unitsToProcess != null) && (totalUnits > 0))
			result = unitsToProcess[totalUnits - 1].compilationResult;
		// last unit in beginToCompile ?
		if (result != null && !result.hasBeenAccepted) {
			/* distant problem which could not be reported back there? */
			if (abortException.problem != null) {
				recordDistantProblem: {
					IProblem distantProblem = abortException.problem;
					IProblem[] knownProblems = result.problems;
					for (int i = 0; i < result.problemCount; i++) {
						if (knownProblems[i] == distantProblem) { // already recorded
							break recordDistantProblem;
						}
					}
					if (distantProblem instanceof DefaultProblem) { // fixup filename TODO (philippe) should improve API to make this official
						((DefaultProblem) distantProblem).setOriginatingFileName(result.getFileName());
					}
					result	.record(distantProblem, unit);
				}
			} else {
				/* distant internal exception which could not be reported back there */
				if (abortException.exception != null) {
					this.handleInternalException(abortException.exception, null, result);
					return;
				}
			}
			/* hand back the compilation result */
			if (!result.hasBeenAccepted) {
				requestor.acceptResult(result.tagAsAccepted());
			}
		} else {
			abortException.printStackTrace();
		}
	}

	public void initializeParser() {

		this.parser = new Parser(this.problemReporter, this.options.parseLiteralExpressionsAsConstants);
	}
	
	/**
	 * Process a compilation unit already parsed and build.
	 */
	public void process(CompilationUnitDeclaration unit, int i) {

		this.parser.getMethodBodies(unit);

		// fault in fields & methods
		if (unit.scope != null)
			unit.scope.faultInTypes();

		// verify inherited methods
		if (unit.scope != null)
			unit.scope.verifyMethods(lookupEnvironment.methodVerifier());

		// type checking
		unit.resolve();

		// flow analysis
		unit.analyseCode();

		// code generation
		unit.generateCode();

		// reference info
		if (options.produceReferenceInfo && unit.scope != null)
			unit.scope.storeDependencyInfo();

		// refresh the total number of units known at this stage
		unit.compilationResult.totalUnitsKnown = totalUnits;
	}
	public void reset() {
		lookupEnvironment.reset();
		parser.scanner.source = null;
		unitsToProcess = null;
		if (DebugRequestor != null) DebugRequestor.reset();
	}

	/**
	 * Internal API used to resolve a given compilation unit. Can run a subset of the compilation process
	 */
	public CompilationUnitDeclaration resolve(
			CompilationUnitDeclaration unit, 
			ICompilationUnit sourceUnit, 
			boolean verifyMethods,
			boolean analyzeCode,
			boolean generateCode) {
				
		try {
			if (unit == null) {
				// build and record parsed units
				parseThreshold = 0; // will request a full parse
				beginToCompile(new ICompilationUnit[] { sourceUnit });
				// process all units (some more could be injected in the loop by the lookup environment)
				unit = unitsToProcess[0];
			} else {
				// initial type binding creation
				lookupEnvironment.buildTypeBindings(unit);

				// binding resolution
				lookupEnvironment.completeTypeBindings();
			}
			this.parser.getMethodBodies(unit);
			if (unit.scope != null) {
				// fault in fields & methods
				unit.scope.faultInTypes();
				if (unit.scope != null && verifyMethods) {
					// http://dev.eclipse.org/bugs/show_bug.cgi?id=23117
 					// verify inherited methods
					unit.scope.verifyMethods(lookupEnvironment.methodVerifier());
				}
				// type checking
				unit.resolve();		

				// flow analysis
				if (analyzeCode) unit.analyseCode();
		
				// code generation
				if (generateCode) unit.generateCode();
			}
			if (unitsToProcess != null) unitsToProcess[0] = null; // release reference to processed unit declaration
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
	/**
	 * Internal API used to resolve a given compilation unit. Can run a subset of the compilation process
	 */
	public CompilationUnitDeclaration resolve(
			ICompilationUnit sourceUnit, 
			boolean verifyMethods,
			boolean analyzeCode,
			boolean generateCode) {
				
		return resolve(
			null,
			sourceUnit,
			verifyMethods,
			analyzeCode,
			generateCode);
	}
}
