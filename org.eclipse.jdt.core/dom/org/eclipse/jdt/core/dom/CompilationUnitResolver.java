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
import org.eclipse.jdt.core.compiler.IProblem;
import org.eclipse.jdt.internal.core.*;
import org.eclipse.jdt.internal.compiler.impl.*;
import org.eclipse.jdt.internal.compiler.ast.*;
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
	 *      @see org.eclipse.jdt.internal.compiler.api.problem.DefaultErrorHandlingPolicies
	 * 
	 *	@param settings The settings to use for the resolution.
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
				true, // need field and methods
				true, // need member types
				false, // no need for field initialization
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
			public void checkAnnotation() {

				if (this.currentElement != null && this.scanner.commentPtr >= 0) {
					flushAnnotationsDefinedPriorTo(endStatementPosition); // discard obsolete comments
				}
				boolean deprecated = false;
				boolean checkDeprecated = false;
				int lastAnnotationIndex = -1;
			
				//since jdk1.2 look only in the last java doc comment...
				nextComment : for (lastAnnotationIndex = scanner.commentPtr; lastAnnotationIndex >= 0; lastAnnotationIndex--){
					//look for @deprecated into the first javadoc comment preceeding the declaration
					int commentSourceStart = scanner.commentStarts[lastAnnotationIndex];
					// javadoc only (non javadoc comment have negative end positions.)
					if (modifiersSourceStart != -1 && modifiersSourceStart < commentSourceStart) {
						continue nextComment;
					}
					if (scanner.commentStops[lastAnnotationIndex] < 0) {
						continue nextComment;
					}
					checkDeprecated = true;
					int commentSourceEnd = scanner.commentStops[lastAnnotationIndex] - 1; //stop is one over
					char[] comment = scanner.source;
			
					deprecated =
						checkDeprecation(
							commentSourceStart,
							commentSourceEnd,
							comment);
					break nextComment;
				}
				if (deprecated) {
					checkAndSetModifiers(AccDeprecated);
				}
				// modify the modifier source start to point at the first comment
				if (lastAnnotationIndex >= 0 && checkDeprecated) {
					modifiersSourceStart = scanner.commentStarts[lastAnnotationIndex]; 
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
			}
		};
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jdt.internal.compiler.Compiler#initializeParser()
	 */
	public void initializeParser() {
		// TODO Auto-generated method stub
		this.parser = createDomParser(this.problemReporter);
	}
	public static CompilationUnitDeclaration resolve(
		ICompilationUnit unitElement,
		IAbstractSyntaxTreeVisitor visitor)
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
						unitElement.getSource().toCharArray(),
						expectedPackageName,
						new String(fileName),
						encoding),
					true, // method verification
					true, // analyze code
					true); // generate code
			return unit;
		} finally {
			if (unit != null) {
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

	private static void reportProblems(CompilationUnitDeclaration unit, IAbstractSyntaxTreeVisitor visitor) {
		CompilationResult unitResult = unit.compilationResult;
		IProblem[] problems = unitResult.getAllProblems();
		for (int i = 0, problemLength = problems == null ? 0 : problems.length; i < problemLength; i++) {
			visitor.acceptProblem(problems[i]);				
		}	
	}
	
	public static CompilationUnitDeclaration resolve(
		char[] source,
		String unitName,
		IJavaProject javaProject,
		IAbstractSyntaxTreeVisitor visitor)
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
			reportProblems(unit, visitor);
			return unit;
		} finally {
			if (unit != null) {
				unit.cleanUp();
			}
		}
	}

	public static CompilationUnitDeclaration resolve(
		char[] source,
		char[][] packageName,
		String unitName,
		IJavaProject javaProject,
		IAbstractSyntaxTreeVisitor visitor)
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
			reportProblems(unit, visitor);					
			return unit;
		} finally {
			if (unit != null) {
				unit.cleanUp();
			}
		}
	}	
	
}
