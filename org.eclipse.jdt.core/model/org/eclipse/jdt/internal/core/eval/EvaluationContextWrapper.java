package org.eclipse.jdt.internal.core.eval;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
import org.eclipse.core.resources.*;
import org.eclipse.core.runtime.*;

import org.eclipse.jdt.internal.codeassist.ISelectionRequestor;
import org.eclipse.jdt.internal.codeassist.ICompletionRequestor;
import org.eclipse.jdt.internal.compiler.*;
import org.eclipse.jdt.internal.compiler.env.INameEnvironment;
import org.eclipse.jdt.core.*;
import org.eclipse.jdt.core.eval.*;
import org.eclipse.jdt.internal.compiler.util.CharOperation;
import org.eclipse.jdt.internal.core.*;
import org.eclipse.jdt.internal.core.builder.impl.JavaBuilder;
import org.eclipse.jdt.internal.core.builder.impl.ProblemFactory;
import org.eclipse.jdt.internal.eval.*;

import java.util.Locale;

/**
 * A wrapper around the infrastructure evaluation context. 
 */
public class EvaluationContextWrapper implements IEvaluationContext {
	protected EvaluationContext context;
	protected JavaProject project;
	/**
	 * Creates a new wrapper around the given infrastructure evaluation context
	 * and project.
	 */
	public EvaluationContextWrapper(
		EvaluationContext context,
		JavaProject project) {
		this.context = context;
		this.project = project;
	}

	/**
	 * @see org.eclipse.jdt.core.eval.IEvaluationContext#allVariables
	 */
	public IGlobalVariable[] allVariables() {
		GlobalVariable[] vars = this.context.allVariables();
		int length = vars.length;
		GlobalVariableWrapper[] result = new GlobalVariableWrapper[length];
		for (int i = 0; i < length; i++) {
			result[i] = new GlobalVariableWrapper(vars[i]);
		}
		return result;
	}

	/**
	 * Checks to ensure that there is a previously built state.
	 */
	protected void checkBuilderState() throws JavaModelException {
		if (!getProject().hasBuildState()) {
			throw new JavaModelException(
				new JavaModelStatus(
					IJavaModelStatusConstants.EVALUATION_ERROR,
					"Cannot evaluate if the project has not been built once"));
		}
	}

	/**
	 * @see org.eclipse.jdt.core.eval.IEvaluationContext#codeComplete.
	 */
	public void codeComplete(
		String codeSnippet,
		int position,
		ICodeCompletionRequestor requestor)
		throws JavaModelException {
		this.context.complete(
			codeSnippet.toCharArray(),
			position,
			this.project.getSearchableNameEnvironment(),
			new CompletionRequestorWrapper(requestor),
			JavaModelManager.convertConfigurableOptions(JavaCore.getOptions()));
	}

	/**
	 * @see org.eclipse.jdt.core.eval.IEvaluationContext#codeSelect.
	 */
	public IJavaElement[] codeSelect(String codeSnippet, int offset, int length)
		throws JavaModelException {
		SelectionRequestor requestor =
			new SelectionRequestor(this.project.getNameLookup(), null);
		// null because there is no need to look inside the code snippet itself
		this.context.select(
			codeSnippet.toCharArray(),
			offset,
			offset + length,
			this.project.getSearchableNameEnvironment(),
			requestor,
			JavaModelManager.convertConfigurableOptions(JavaCore.getOptions()));
		return requestor.getElements();
	}

	/**
	 * @see org.eclipse.jdt.core.eval.IEvaluationContext#deleteVariable.
	 */
	public void deleteVariable(IGlobalVariable variable) {
		if (variable instanceof GlobalVariableWrapper) {
			GlobalVariableWrapper wrapper = (GlobalVariableWrapper) variable;
			this.context.deleteVariable(wrapper.variable);
		} else {
			throw new Error("Unknown implementation of IGlobalVariable");
		}
	}

	/**
	 * @see IEvaluationContext#evaluateCodeSnippet
	 */
	public void evaluateCodeSnippet(
		String codeSnippet,
		String[] localVariableTypeNames,
		String[] localVariableNames,
		int[] localVariableModifiers,
		IType declaringType,
		boolean isStatic,
		boolean isConstructorCall,
		ICodeSnippetRequestor requestor,
		IProgressMonitor progressMonitor)
		throws org.eclipse.jdt.core.JavaModelException {

		checkBuilderState();

		int length = localVariableTypeNames.length;
		char[][] varTypeNames = new char[length][];
		for (int i = 0; i < length; i++) {
			varTypeNames[i] = localVariableTypeNames[i].toCharArray();
		}

		length = localVariableNames.length;
		char[][] varNames = new char[length][];
		for (int i = 0; i < length; i++) {
			varNames[i] = localVariableNames[i].toCharArray();
		}

		// transfer the imports of the IType to the evaluation context
		if (declaringType != null) {
			// retrieves the package statement 
			this.context.setPackageName(
				declaringType.getPackageFragment().getElementName().toCharArray());
			ICompilationUnit compilationUnit = declaringType.getCompilationUnit();
			if (compilationUnit != null) {
				// retrieves the import statement
				IImportDeclaration[] imports = compilationUnit.getImports();
				int importsLength = imports.length;
				if (importsLength != 0) {
					char[][] importsNames = new char[importsLength][];
					for (int i = 0; i < importsLength; i++) {
						importsNames[i] = imports[i].getElementName().toCharArray();
					}
					this.context.setImports(importsNames);
				}
			}
		}
		try {
			this.context.evaluate(
				codeSnippet.toCharArray(),
				varTypeNames,
				varNames,
				localVariableModifiers,
				declaringType == null
					? null
					: declaringType.getFullyQualifiedName().toCharArray(),
				isStatic,
				isConstructorCall,
				getBuildNameEnvironment(),
				getCompilerOptions(),
				getInfrastructureEvaluationRequestor(requestor),
				getProblemFactory());
		} catch (InstallException e) {
			handleInstallException(e);
		}
	}

	/**
	 * @see IEvaluationContext#evaluateCodeSnippet
	 */
	public void evaluateCodeSnippet(
		String codeSnippet,
		ICodeSnippetRequestor requestor,
		IProgressMonitor progressMonitor)
		throws JavaModelException {
		checkBuilderState();
		try {
			this.context.evaluate(
				codeSnippet.toCharArray(),
				getBuildNameEnvironment(),
				getCompilerOptions(),
				getInfrastructureEvaluationRequestor(requestor),
				getProblemFactory());
		} catch (InstallException e) {
			handleInstallException(e);
		}
	}

	/**
	 * @see IEvaluationContext#evaluateVariable
	 */
	public void evaluateVariable(
		IGlobalVariable variable,
		ICodeSnippetRequestor requestor,
		IProgressMonitor progressMonitor)
		throws JavaModelException {
		checkBuilderState();
		try {
			this.context.evaluateVariable(
				((GlobalVariableWrapper) variable).variable,
				getBuildNameEnvironment(),
				getCompilerOptions(),
				getInfrastructureEvaluationRequestor(requestor),
				getProblemFactory());
		} catch (InstallException e) {
			handleInstallException(e);
		}
	}

	/**
	 * Returns a name environment for the last built state.
	 */
	protected INameEnvironment getBuildNameEnvironment()
		throws JavaModelException {
		return JavaModelManager.getJavaModelManager().getNameEnvironment(
			getProject().getProject());
	}

	/**
	 * Returns the compiler's configurable options.
	 */
	protected ConfigurableOption[] getCompilerOptions() throws JavaModelException {
		return JavaModelManager.convertConfigurableOptions(JavaCore.getOptions());
	}

	/**
	 * @see org.eclipse.jdt.core.eval.IEvaluationContext#getImports
	 */
	public String[] getImports() {
		char[][] imports = this.context.getImports();
		int length = imports.length;
		String[] result = new String[length];
		for (int i = 0; i < length; i++) {
			result[i] = new String(imports[i]);
		}
		return result;
	}

	/**
	 * Returns the infrastructure evaluation context.
	 */
	public EvaluationContext getInfrastructureEvaluationContext() {
		return this.context;
	}

	/**
	 * Returns a new infrastructure evaluation requestor instance.
	 */
	protected IRequestor getInfrastructureEvaluationRequestor(ICodeSnippetRequestor requestor) {
		return new RequestorWrapper(requestor);
	}

	/**
	 * @see org.eclipse.jdt.core.eval.IEvaluationContext#getPackageName
	 */
	public String getPackageName() {
		return new String(this.context.getPackageName());
	}

	/**
	 * Returns the problem factory to be used during evaluation.
	 */
	protected IProblemFactory getProblemFactory() throws JavaModelException {
		return ProblemFactory.getProblemFactory(Locale.getDefault());
	}

	/**
	 * @see org.eclipse.jdt.core.eval.IEvaluationContext#getProject
	 */
	public IJavaProject getProject() {
		return this.project;
	}

	/**
	 * Handles an install exception by throwing a Java Model exception.
	 */
	protected void handleInstallException(InstallException e)
		throws JavaModelException {
		throw new JavaModelException(
			new JavaModelStatus(IJavaModelStatusConstants.EVALUATION_ERROR, e.toString()));
	}

	/**
	 * @see org.eclipse.jdt.core.eval.IEvaluationContext#newVariable
	 */
	public IGlobalVariable newVariable(
		String typeName,
		String name,
		String initializer) {
		GlobalVariable newVar =
			this.context.newVariable(
				typeName.toCharArray(),
				name.toCharArray(),
				(initializer == null) ? null : initializer.toCharArray());
		return new GlobalVariableWrapper(newVar);
	}

	/**
	 * @see org.eclipse.jdt.core.eval.IEvaluationContext#setImports
	 */
	public void setImports(String[] imports) {
		int length = imports.length;
		char[][] result = new char[length][];
		for (int i = 0; i < length; i++) {
			result[i] = imports[i].toCharArray();
		}
		this.context.setImports(result);
	}

	/**
	 * @see org.eclipse.jdt.core.eval.IEvaluationContext#setPackageName
	 */
	public void setPackageName(String packageName) {
		this.context.setPackageName(packageName.toCharArray());
	}

	/**
	 * @see IEvaluationContext#validateImports
	 */
	public void validateImports(ICodeSnippetRequestor requestor)
		throws JavaModelException {
		checkBuilderState();
		this.context.evaluateImports(
			getBuildNameEnvironment(),
			getInfrastructureEvaluationRequestor(requestor),
			getProblemFactory());
	}

}
