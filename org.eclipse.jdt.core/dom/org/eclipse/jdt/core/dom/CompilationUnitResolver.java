/*******************************************************************************
 * Copyright (c) 2000, 2003 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/

package org.eclipse.jdt.core.dom;

import org.eclipse.jdt.internal.compiler.Compiler;
import org.eclipse.jdt.internal.compiler.*;
import org.eclipse.jdt.internal.compiler.env.*;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.*;
import org.eclipse.jdt.core.compiler.*;
import org.eclipse.jdt.internal.core.*;
import org.eclipse.jdt.internal.compiler.impl.*;
import org.eclipse.jdt.internal.compiler.ast.*;
import org.eclipse.jdt.internal.compiler.lookup.CompilerModifiers;
import org.eclipse.jdt.internal.compiler.lookup.PackageBinding;
import org.eclipse.jdt.internal.compiler.parser.Parser;
import org.eclipse.jdt.internal.compiler.parser.SourceTypeConverter;
import org.eclipse.jdt.internal.compiler.problem.*;

import java.util.*;

class CompilationUnitResolver extends Compiler {
	
	/**
	 * Answer a new CompilationUnitVisitor using the given name environment and compiler options.
	 * The environment and options will be in effect for the lifetime of the compiler.
	 * When the compiler is run, compilation results are sent to the given requestor.
	 *
	 *  @param environment org.eclipse.jdt.internal.compiler.api.env.INameEnvironment
	 *      Environment used by the compiler in order to resolve type and package
	 *      names. The name environment implements the actual connection of the compiler
	 *      to the outside world (for example, in batch mode the name environment is performing
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
	 *	@param settings The settings to use for the resolution.
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
	public CompilationUnitResolver(
		INameEnvironment environment,
		IErrorHandlingPolicy policy,
		Map settings,
		ICompilerRequestor requestor,
		IProblemFactory problemFactory) {

		super(environment, policy, settings, requestor, problemFactory, false);
	}
	
	/**
	 * Add additional source types
	 */
	public void accept(ISourceType[] sourceTypes, PackageBinding packageBinding) {
		CompilationResult result =
			new CompilationResult(sourceTypes[0].getFileName(), 1, 1, this.options.maxProblemsPerUnit);
		// need to hold onto this
		CompilationUnitDeclaration unit =
			SourceTypeConverter.buildCompilationUnit(
				sourceTypes,//sourceTypes[0] is always toplevel here
				SourceTypeConverter.FIELD_AND_METHOD // need field and methods
				| SourceTypeConverter.MEMBER_TYPE // need member types
				| SourceTypeConverter.FIELD_INITIALIZATION, // need field initialization: see bug 40476
				lookupEnvironment.problemReporter,
				result);

		if (unit != null) {
			this.lookupEnvironment.buildTypeBindings(unit);
			this.lookupEnvironment.completeTypeBindings(unit, true);
		}
	}

	private static Parser createDomParser(ProblemReporter problemReporter) {
		
		return new Parser(problemReporter, false) {
			// old annotation style check which doesn't include all leading comments into declaration
			// for backward compatibility with 2.1 DOM 
			public void checkComment() {

				if (this.currentElement != null && this.scanner.commentPtr >= 0) {
					flushCommentsDefinedPriorTo(endStatementPosition); // discard obsolete comments
				}
				boolean deprecated = false;
				boolean checkDeprecated = false;
				int lastCommentIndex = -1;
			
				//since jdk1.2 look only in the last java doc comment...
				nextComment : for (lastCommentIndex = scanner.commentPtr; lastCommentIndex >= 0; lastCommentIndex--){
					//look for @deprecated into the first javadoc comment preceeding the declaration
					int commentSourceStart = scanner.commentStarts[lastCommentIndex];
					// javadoc only (non javadoc comment have negative end positions.)
					if (modifiersSourceStart != -1 && modifiersSourceStart < commentSourceStart) {
						continue nextComment;
					}
					if (scanner.commentStops[lastCommentIndex] < 0) {
						continue nextComment;
					}
					checkDeprecated = true;
					int commentSourceEnd = scanner.commentStops[lastCommentIndex] - 1; //stop is one over
			
					deprecated =
						this.javadocParser.checkDeprecation(commentSourceStart, commentSourceEnd);
					this.javadoc = this.javadocParser.javadoc;
					break nextComment;
				}
				if (deprecated) {
					checkAndSetModifiers(AccDeprecated);
				}
				// modify the modifier source start to point at the first comment
				if (lastCommentIndex >= 0 && checkDeprecated) {
					modifiersSourceStart = scanner.commentStarts[lastCommentIndex]; 
				}

			}
		};
	}

	/*
	 *  Low-level API performing the actual compilation
	 */
	protected static IErrorHandlingPolicy getHandlingPolicy() {

		// passes the initial set of files to the batch oracle (to avoid finding more than once the same units when case insensitive match)	
		return new IErrorHandlingPolicy() {
			public boolean stopOnFirstError() {
				return false;
			}
			public boolean proceedOnErrors() {
				return false; // stop if there are some errors 
			}
		};
	}

	protected static INameEnvironment getNameEnvironment(ICompilationUnit sourceUnit)
		throws JavaModelException {
		return (SearchableEnvironment) ((JavaProject) sourceUnit.getJavaProject())
			.getSearchableNameEnvironment();
	}

	protected static INameEnvironment getNameEnvironment(IJavaProject javaProject)
		throws JavaModelException {
		return (SearchableEnvironment) ((JavaProject) javaProject)
			.getSearchableNameEnvironment();
	}
	
	/*
	 * Answer the component to which will be handed back compilation results from the compiler
	 */
	protected static ICompilerRequestor getRequestor() {
		return new ICompilerRequestor() {
			public void acceptResult(CompilationResult compilationResult) {
				// do nothing
			}
		};
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jdt.internal.compiler.Compiler#initializeParser()
	 */
	public void initializeParser() {
		this.parser = createDomParser(this.problemReporter);
	}
	/*
	 * Compiler crash recovery in case of unexpected runtime exceptions
	 */
	protected void handleInternalException(
			Throwable internalException,
			CompilationUnitDeclaration unit,
			CompilationResult result) {
		super.handleInternalException(internalException, unit, result);
		if (unit != null) {
			removeUnresolvedBindings(unit);
		}
	}
	
	/*
	 * Compiler recovery in case of internal AbortCompilation event
	 */
	protected void handleInternalException(
			AbortCompilation abortException,
			CompilationUnitDeclaration unit) {
		super.handleInternalException(abortException, unit);
		if (unit != null) {
			removeUnresolvedBindings(unit);
		}
	}	
	public static CompilationUnitDeclaration resolve(
		ICompilationUnit unitElement,
		boolean cleanUp,
		char[] source)
		throws JavaModelException {

		char[] fileName = unitElement.getElementName().toCharArray();
		IJavaProject project = unitElement.getJavaProject();
		CompilationUnitResolver compilationUnitVisitor =
			new CompilationUnitResolver(
				getNameEnvironment(unitElement),
				getHandlingPolicy(),
				project.getOptions(true),
				getRequestor(),
				new DefaultProblemFactory());

		CompilationUnitDeclaration unit = null;
		try {
			String encoding = project.getOption(JavaCore.CORE_ENCODING, true);

			IPackageFragment packageFragment = (IPackageFragment)unitElement.getAncestor(IJavaElement.PACKAGE_FRAGMENT);
			char[][] expectedPackageName = null;
			if (packageFragment != null){
				expectedPackageName = CharOperation.splitOn('.', packageFragment.getElementName().toCharArray());
			}
			
			unit =
				compilationUnitVisitor.resolve(
					new BasicCompilationUnit(
						source,
						expectedPackageName,
						new String(fileName),
						encoding),
					true, // method verification
					true, // analyze code
					true); // generate code
			return unit;
		} finally {
			if (cleanUp && unit != null) {
				unit.cleanUp();
			}
		}
	}
	
	public static CompilationUnitDeclaration parse(char[] source, Map settings) {
		if (source == null) {
			throw new IllegalArgumentException();
		}
		CompilerOptions compilerOptions = new CompilerOptions(settings);
		Parser parser = createDomParser(
			new ProblemReporter(
					DefaultErrorHandlingPolicies.proceedWithAllProblems(), 
					compilerOptions, 
					new DefaultProblemFactory()));
		org.eclipse.jdt.internal.compiler.env.ICompilationUnit sourceUnit = 
			new org.eclipse.jdt.internal.compiler.batch.CompilationUnit(
				source, 
				"", //$NON-NLS-1$
				compilerOptions.defaultEncoding);
		CompilationUnitDeclaration compilationUnitDeclaration = parser.dietParse(sourceUnit, new CompilationResult(sourceUnit, 0, 0, compilerOptions.maxProblemsPerUnit));
		
		if (compilationUnitDeclaration.ignoreMethodBodies) {
			compilationUnitDeclaration.ignoreFurtherInvestigation = true;
			// if initial diet parse did not work, no need to dig into method bodies.
			return compilationUnitDeclaration; 
		}
		
		//fill the methods bodies in order for the code to be generated
		//real parse of the method....
		parser.scanner.setSource(source);
		org.eclipse.jdt.internal.compiler.ast.TypeDeclaration[] types = compilationUnitDeclaration.types;
		if (types != null) {
			for (int i = types.length; --i >= 0;)
				types[i].parseMethod(parser, compilationUnitDeclaration);
		}
		return compilationUnitDeclaration;
	}

	public static CompilationUnitDeclaration parse(char[] source, NodeSearcher nodeSearcher, Map settings) {
		if (source == null) {
			throw new IllegalArgumentException();
		}
		CompilerOptions compilerOptions = new CompilerOptions(settings);
		Parser parser = createDomParser(
			new ProblemReporter(
					DefaultErrorHandlingPolicies.proceedWithAllProblems(), 
					compilerOptions, 
					new DefaultProblemFactory()));
		org.eclipse.jdt.internal.compiler.env.ICompilationUnit sourceUnit = 
			new org.eclipse.jdt.internal.compiler.batch.CompilationUnit(
				source, 
				"", //$NON-NLS-1$
				compilerOptions.defaultEncoding);
		CompilationUnitDeclaration compilationUnitDeclaration = parser.dietParse(sourceUnit, new CompilationResult(sourceUnit, 0, 0, compilerOptions.maxProblemsPerUnit));
		
		if (compilationUnitDeclaration.ignoreMethodBodies) {
			compilationUnitDeclaration.ignoreFurtherInvestigation = true;
			// if initial diet parse did not work, no need to dig into method bodies.
			return null; 
		}
		
		int searchPosition = nodeSearcher.position;
		if (searchPosition < 0 || searchPosition > source.length) {
			// the position is out of range. There is no need to search for a node.
 			return compilationUnitDeclaration;
		}
	
		compilationUnitDeclaration.traverse(nodeSearcher, compilationUnitDeclaration.scope);
		
		org.eclipse.jdt.internal.compiler.ast.ASTNode node = nodeSearcher.found;
 		if (node == null) {
 			return compilationUnitDeclaration;
 		}
 		
 		org.eclipse.jdt.internal.compiler.ast.TypeDeclaration enclosingTypeDeclaration = nodeSearcher.enclosingType;
 		
		if (node instanceof AbstractMethodDeclaration) {
			((AbstractMethodDeclaration)node).parseStatements(parser, compilationUnitDeclaration);
		} else if (enclosingTypeDeclaration != null) {
			if (node instanceof org.eclipse.jdt.internal.compiler.ast.Initializer) {
				((org.eclipse.jdt.internal.compiler.ast.Initializer) node).parseStatements(parser, enclosingTypeDeclaration, compilationUnitDeclaration);
			} else {  					
				((org.eclipse.jdt.internal.compiler.ast.TypeDeclaration)node).parseMethod(parser, compilationUnitDeclaration);
			} 				
		}
		
		return compilationUnitDeclaration;
	}
	public static CompilationUnitDeclaration resolve(
		char[] source,
		String unitName,
		IJavaProject javaProject,
		boolean cleanUp)
		throws JavaModelException {
	
		CompilationUnitResolver compilationUnitVisitor =
			new CompilationUnitResolver(
				getNameEnvironment(javaProject),
				getHandlingPolicy(),
				javaProject.getOptions(true),
				getRequestor(),
				new DefaultProblemFactory());
	
		CompilationUnitDeclaration unit = null;
		try {
			String encoding = javaProject.getOption(JavaCore.CORE_ENCODING, true);

			unit =
				compilationUnitVisitor.resolve(
					new BasicCompilationUnit(
						source,
						null,
						unitName,
						encoding),
					true, // method verification
					true, // analyze code
					true); // generate code
			return unit;
		} finally {
			if (cleanUp && unit != null) {
				unit.cleanUp();
			}
		}
	}

	public static CompilationUnitDeclaration resolve(
		ICompilationUnit unitElement,
		NodeSearcher nodeSearcher,
		boolean cleanUp,
		char[] source)
		throws JavaModelException {

		CompilationUnitDeclaration unit = null;
		try {
			char[] fileName = unitElement.getElementName().toCharArray();
			IJavaProject project = unitElement.getJavaProject();
			CompilationUnitResolver compilationUnitVisitor =
				new CompilationUnitResolver(
					getNameEnvironment(unitElement),
					getHandlingPolicy(),
					project.getOptions(true),
					getRequestor(),
					new DefaultProblemFactory());
	
			String encoding = project.getOption(JavaCore.CORE_ENCODING, true);
	
			IPackageFragment packageFragment = (IPackageFragment)unitElement.getAncestor(IJavaElement.PACKAGE_FRAGMENT);
			char[][] expectedPackageName = null;
			if (packageFragment != null){
				expectedPackageName = CharOperation.splitOn('.', packageFragment.getElementName().toCharArray());
			}
		
			unit = compilationUnitVisitor.resolve(
				new BasicCompilationUnit(
					source,
					expectedPackageName,
					new String(fileName),
					encoding),
				nodeSearcher,
				true, // method verification
				true, // analyze code
				true); // generate code
			return unit;
		} finally {
			if (cleanUp && unit != null) {
				unit.cleanUp();
			}
		}
	}

	public static CompilationUnitDeclaration resolve(
		char[] source,
		char[][] packageName,
		String unitName,
		IJavaProject javaProject,
		boolean cleanUp)
		throws JavaModelException {
	
		CompilationUnitResolver compilationUnitVisitor =
			new CompilationUnitResolver(
				getNameEnvironment(javaProject),
				getHandlingPolicy(),
				javaProject.getOptions(true),
				getRequestor(),
				new DefaultProblemFactory());
	
		CompilationUnitDeclaration unit = null;
		try {
			String encoding = javaProject.getOption(JavaCore.CORE_ENCODING, true);

			unit =
				compilationUnitVisitor.resolve(
					new BasicCompilationUnit(
						source,
						packageName,
						unitName,
						encoding),
					true, // method verification
					true, // analyze code
					true); // generate code					
			return unit;
		} finally {
			if (cleanUp && unit != null) {
				unit.cleanUp();
			}
		}
	}
	/*
	 * When unit result is about to be accepted, removed back pointers
	 * to unresolved bindings
	 */
	public void removeUnresolvedBindings(CompilationUnitDeclaration compilationUnitDeclaration) {
		final org.eclipse.jdt.internal.compiler.ast.TypeDeclaration[] types = compilationUnitDeclaration.types;
		if (types != null) {
			for (int i = 0, max = types.length; i < max; i++) {
				removeUnresolvedBindings(types[i]);
			}
		}
	}
	private void removeUnresolvedBindings(org.eclipse.jdt.internal.compiler.ast.TypeDeclaration type) {
		final org.eclipse.jdt.internal.compiler.ast.TypeDeclaration[] memberTypes = type.memberTypes;
		if (memberTypes != null) {
			for (int i = 0, max = memberTypes.length; i < max; i++){
				removeUnresolvedBindings(memberTypes[i]);
			}
		}
		if (type.binding != null && (type.binding.modifiers & CompilerModifiers.AccUnresolved) != 0) {
			type.binding = null;
		}
		
		final org.eclipse.jdt.internal.compiler.ast.FieldDeclaration[] fields = type.fields;
		if (fields != null) {
			for (int i = 0, max = fields.length; i < max; i++){
				if (fields[i].binding != null && (fields[i].binding.modifiers & CompilerModifiers.AccUnresolved) != 0) {
					fields[i].binding = null;
				}
			}
		}
	
		final AbstractMethodDeclaration[] methods = type.methods;
		if (methods != null) {
			for (int i = 0, max = methods.length; i < max; i++){
				if (methods[i].binding !=  null && (methods[i].binding.modifiers & CompilerModifiers.AccUnresolved) != 0) {
					methods[i].binding = null;
				}
			}
		}
	}

	/**
	 * Internal API used to resolve a given compilation unit. Can run a subset of the compilation process
	 */
	public CompilationUnitDeclaration resolve(
			org.eclipse.jdt.internal.compiler.env.ICompilationUnit compilationUnit,
			NodeSearcher nodeSearcher,
			boolean verifyMethods,
			boolean analyzeCode,
			boolean generateCode) {

		CompilationUnitDeclaration unit = null;
		try {

			parseThreshold = 0; // will request a diet parse
			beginToCompile(new org.eclipse.jdt.internal.compiler.env.ICompilationUnit[] { compilationUnit});
			// process all units (some more could be injected in the loop by the lookup environment)
			unit = unitsToProcess[0];

			int searchPosition = nodeSearcher.position;
			if (searchPosition >= 0 && searchPosition <= compilationUnit.getContents().length) {
				unit.traverse(nodeSearcher, unit.scope);
				
				org.eclipse.jdt.internal.compiler.ast.ASTNode node = nodeSearcher.found;
				
	 			if (node != null) {
					org.eclipse.jdt.internal.compiler.ast.TypeDeclaration enclosingTypeDeclaration = nodeSearcher.enclosingType;
	  				if (node instanceof AbstractMethodDeclaration) {
						((AbstractMethodDeclaration)node).parseStatements(parser, unit);
	 				} else if (enclosingTypeDeclaration != null) {
						if (node instanceof org.eclipse.jdt.internal.compiler.ast.Initializer) {
		 					((org.eclipse.jdt.internal.compiler.ast.Initializer) node).parseStatements(parser, enclosingTypeDeclaration, unit);
	 					} else if (node instanceof org.eclipse.jdt.internal.compiler.ast.TypeDeclaration) {  					
							((org.eclipse.jdt.internal.compiler.ast.TypeDeclaration)node).parseMethod(parser, unit);
						} 				
	 				}
	 			}
			}
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
			return null;
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
			org.eclipse.jdt.internal.compiler.env.ICompilationUnit sourceUnit, 
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

	/**
	 * Internal API used to resolve a given compilation unit. Can run a subset of the compilation process
	 */
	public CompilationUnitDeclaration resolve(
			CompilationUnitDeclaration unit, 
			org.eclipse.jdt.internal.compiler.env.ICompilationUnit sourceUnit, 
			boolean verifyMethods,
			boolean analyzeCode,
			boolean generateCode) {
				
		try {
			if (unit == null) {
				// build and record parsed units
				parseThreshold = 0; // will request a full parse
				beginToCompile(new org.eclipse.jdt.internal.compiler.env.ICompilationUnit[] { sourceUnit });
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
}
