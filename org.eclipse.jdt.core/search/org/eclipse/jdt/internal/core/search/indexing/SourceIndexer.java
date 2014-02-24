/*******************************************************************************
 * Copyright (c) 2000, 2014 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * This is an implementation of an early-draft specification developed under the Java
 * Community Process (JCP) and is made available for testing and evaluation purposes
 * only. The code is not compatible with any specification of the JCP.
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jdt.internal.core.search.indexing;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.search.SearchDocument;
import org.eclipse.jdt.internal.compiler.ASTVisitor;
import org.eclipse.jdt.internal.compiler.CompilationResult;
import org.eclipse.jdt.internal.compiler.DefaultErrorHandlingPolicies;
import org.eclipse.jdt.internal.compiler.ISourceElementRequestor;
import org.eclipse.jdt.internal.compiler.SourceElementParser;
import org.eclipse.jdt.internal.compiler.ast.CompilationUnitDeclaration;
import org.eclipse.jdt.internal.compiler.ast.LambdaExpression;
import org.eclipse.jdt.internal.compiler.ast.ReferenceExpression;
import org.eclipse.jdt.internal.compiler.env.AccessRestriction;
import org.eclipse.jdt.internal.compiler.env.IBinaryType;
import org.eclipse.jdt.internal.compiler.env.ICompilationUnit;
import org.eclipse.jdt.internal.compiler.env.ISourceType;
import org.eclipse.jdt.internal.compiler.impl.CompilerOptions;
import org.eclipse.jdt.internal.compiler.impl.ITypeRequestor;
import org.eclipse.jdt.internal.compiler.lookup.BlockScope;
import org.eclipse.jdt.internal.compiler.lookup.LookupEnvironment;
import org.eclipse.jdt.internal.compiler.lookup.MethodBinding;
import org.eclipse.jdt.internal.compiler.lookup.PackageBinding;
import org.eclipse.jdt.internal.compiler.parser.Parser;
import org.eclipse.jdt.internal.compiler.parser.SourceTypeConverter;
import org.eclipse.jdt.internal.compiler.problem.DefaultProblemFactory;
import org.eclipse.jdt.internal.compiler.problem.ProblemReporter;
import org.eclipse.jdt.internal.compiler.util.SuffixConstants;
import org.eclipse.jdt.internal.core.CancelableNameEnvironment;
import org.eclipse.jdt.internal.core.DefaultWorkingCopyOwner;
import org.eclipse.jdt.internal.core.JavaModel;
import org.eclipse.jdt.internal.core.JavaModelManager;
import org.eclipse.jdt.internal.core.JavaProject;
import org.eclipse.jdt.internal.core.SourceTypeElementInfo;
import org.eclipse.jdt.internal.core.jdom.CompilationUnit;
import org.eclipse.jdt.internal.core.search.matching.MethodPattern;
import org.eclipse.jdt.internal.core.search.processing.JobManager;

/**
 * A SourceIndexer indexes java files using a java parser. The following items are indexed:
 * Declarations of:<br>
 * - Classes<br>
 * - Interfaces;<br>
 * - Methods;<br>
 * - Fields;<br>
 * References to:<br>
 * - Methods (with number of arguments); <br>
 * - Fields;<br>
 * - Types;<br>
 * - Constructors.
 */
public class SourceIndexer extends AbstractIndexer implements ITypeRequestor, SuffixConstants {

	private LookupEnvironment lookupEnvironment;
	private CompilerOptions options;
	private CompilationUnitDeclaration cu;
	public ISourceElementRequestor requestor;
	private Parser basicParser;
	private ProblemReporter problemReporter;
	
	public SourceIndexer(SearchDocument document) {
		super(document);
		this.requestor = new SourceIndexerRequestor(this);
	}
	public void indexDocument() {
		// Create a new Parser
		String documentPath = this.document.getPath();
		SourceElementParser parser = this.document.getParser();
		if (parser == null) {
			IPath path = new Path(documentPath);
			IProject project = ResourcesPlugin.getWorkspace().getRoot().getProject(path.segment(0));
			parser = JavaModelManager.getJavaModelManager().indexManager.getSourceElementParser(JavaCore.create(project), this.requestor);
		} else {
			parser.setRequestor(this.requestor);
		}

		// Launch the parser
		char[] source = null;
		char[] name = null;
		try {
			source = this.document.getCharContents();
			name = documentPath.toCharArray();
		} catch(Exception e){
			// ignore
		}
		if (source == null || name == null) return; // could not retrieve document info (e.g. resource was discarded)
		CompilationUnit compilationUnit = new CompilationUnit(source, name);
		try {
			this.cu = parser.parseCompilationUnit(compilationUnit, true/*full parse*/, null/*no progress*/);
			// this.document.shouldIndexResolvedDocument = this.cu.hasFunctionalTypes();
		} catch (Exception e) {
			if (JobManager.VERBOSE) {
				e.printStackTrace();
			}
		}
	}

	public void resolveDocument() {
		IPath path = new Path(this.document.getPath());
		IProject project = ResourcesPlugin.getWorkspace().getRoot().getProject(path.segment(0));
		JavaModel model = JavaModelManager.getJavaModelManager().getJavaModel();
		JavaProject javaProject = (JavaProject) model.getJavaProject(project);
		try {
			CancelableNameEnvironment nameEnvironment;
			nameEnvironment = new CancelableNameEnvironment(javaProject, DefaultWorkingCopyOwner.PRIMARY, null);
			this.options = new CompilerOptions(javaProject.getOptions(true));
			this.problemReporter =
					new ProblemReporter(
						DefaultErrorHandlingPolicies.proceedWithAllProblems(),
						this.options,
						new DefaultProblemFactory());
			this.lookupEnvironment = new LookupEnvironment(this, this.options, this.problemReporter, nameEnvironment);
		} catch (JavaModelException e) {
			if (JobManager.VERBOSE) {
				e.printStackTrace();
			}
			this.cu = null;
			return;
		}
		this.lookupEnvironment.buildTypeBindings(this.cu, null);
		this.lookupEnvironment.completeTypeBindings();
		if (this.cu.scope != null) {
			this.cu.scope.faultInTypes();
			this.cu.resolve();
		}
	}
	
	public void accept(IBinaryType binaryType, PackageBinding packageBinding, AccessRestriction accessRestriction) {
		this.lookupEnvironment.createBinaryTypeFrom(binaryType, packageBinding, accessRestriction);
	}

	public void accept(ICompilationUnit unit, AccessRestriction accessRestriction) {
		CompilationResult unitResult = new CompilationResult(unit, 1, 1, this.options.maxProblemsPerUnit);
		CompilationUnitDeclaration parsedUnit = basicParser().dietParse(unit, unitResult);
		this.lookupEnvironment.buildTypeBindings(parsedUnit, accessRestriction);
		this.lookupEnvironment.completeTypeBindings(parsedUnit, true);
	}

	public void accept(ISourceType[] sourceTypes, PackageBinding packageBinding, AccessRestriction accessRestriction) {
		// ensure to jump back to toplevel type for first one (could be a member)
		while (sourceTypes[0].getEnclosingType() != null) {
			sourceTypes[0] = sourceTypes[0].getEnclosingType();
		}

		CompilationResult result =
			new CompilationResult(sourceTypes[0].getFileName(), 1, 1, this.options.maxProblemsPerUnit);
		
		// https://bugs.eclipse.org/bugs/show_bug.cgi?id=305259, build the compilation unit in its own sand box.
		final long savedComplianceLevel = this.options.complianceLevel;
		final long savedSourceLevel = this.options.sourceLevel;
		
		try {
			IJavaProject project = ((SourceTypeElementInfo) sourceTypes[0]).getHandle().getJavaProject();
			this.options.complianceLevel = CompilerOptions.versionToJdkLevel(project.getOption(JavaCore.COMPILER_COMPLIANCE, true));
			this.options.sourceLevel = CompilerOptions.versionToJdkLevel(project.getOption(JavaCore.COMPILER_SOURCE, true));

			// need to hold onto this
			CompilationUnitDeclaration unit =
				SourceTypeConverter.buildCompilationUnit(
						sourceTypes,//sourceTypes[0] is always toplevel here
						SourceTypeConverter.FIELD_AND_METHOD // need field and methods
						| SourceTypeConverter.MEMBER_TYPE // need member types
						| SourceTypeConverter.FIELD_INITIALIZATION // need field initialization
						| SourceTypeConverter.LOCAL_TYPE, // need local type
						this.lookupEnvironment.problemReporter,
						result);

			if (unit != null) {
				this.lookupEnvironment.buildTypeBindings(unit, accessRestriction);
				this.lookupEnvironment.completeTypeBindings(unit);
			}
		} finally {
			this.options.complianceLevel = savedComplianceLevel;
			this.options.sourceLevel = savedSourceLevel;
		}
	}
	
	protected Parser basicParser() {
		if (this.basicParser == null) {
			this.basicParser = new Parser(this.problemReporter, false);
			this.basicParser.reportOnlyOneSyntaxError = true;
		}
		return this.basicParser;
	}


public void indexResolvedDocument() {
	if (this.cu != null && this.cu.scope != null) {
		final ASTVisitor visitor = new ASTVisitor() {
				public boolean visit(LambdaExpression lambdaExpression, BlockScope blockScope) {
					if (lambdaExpression.binding != null && lambdaExpression.binding.isValidBinding()) {
						SourceIndexer.this.addIndexEntry(IIndexConstants.METHOD_DECL, MethodPattern.createIndexKey(lambdaExpression.descriptor.selector, lambdaExpression.descriptor.parameters.length));
					}
					return true;
				}
				public boolean visit(ReferenceExpression referenceExpression, BlockScope blockScope) {
					if (referenceExpression.isArrayConstructorReference())
						return true;
					MethodBinding binding = referenceExpression.getMethodBinding();
					if (binding != null && binding.isValidBinding()) {
						if (referenceExpression.isMethodReference())
							SourceIndexer.this.addMethodReference(binding.selector, binding.parameters.length);
						else
							SourceIndexer.this.addConstructorReference(binding.declaringClass.sourceName(), binding.parameters.length);
					}
					return true;
				}
			};
		this.cu.traverse(visitor , this.cu.scope, false);
	}
}
}
