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
package org.eclipse.jdt.internal.eval;

import org.eclipse.jdt.core.ICompletionRequestor;
import org.eclipse.jdt.core.compiler.*;
import org.eclipse.jdt.core.compiler.IProblem;
import org.eclipse.jdt.internal.codeassist.ISelectionRequestor;
import org.eclipse.jdt.internal.compiler.util.Util;

/**
 * Maps back and forth a code snippet to a compilation unit.
 * The structure of the compilation unit is as follows:
 * [package <package name>;]
 * [import <import name>;]*
 * public class <code snippet class name> extends <global variable class name> {
 *   [<declaring type> val$this;]
 *   public void run() {
 *     <code snippet>
 *   }
 * }
 */
class CodeSnippetToCuMapper implements EvaluationConstants {
	/**
	 * The generated compilation unit.
	 */
	public char[] cuSource;
	
	/**
	 * Where the code snippet starts in the generated compilation unit.
	 */
	public int lineNumberOffset = 0;
	public int startPosOffset = 0;

	// Internal fields
	char[] codeSnippet;
	char[] snippetPackageName;
	char[][] snippetImports;
	char[] snippetClassName; 
	char[] snippetVarClassName;
	char[] snippetDeclaringTypeName;

	// Mapping of external local variables
	char[][] localVarNames;
	char[][] localVarTypeNames;
	int[] localVarModifiers;
	
/**
 * Rebuild source in presence of external local variables
 */
 public CodeSnippetToCuMapper(char[] codeSnippet, char[] packageName, char[][] imports, char[] className, char[] varClassName, char[][] localVarNames, char[][] localVarTypeNames, int[] localVarModifiers, char[] declaringTypeName) {
	this.codeSnippet = codeSnippet;
	this.snippetPackageName = packageName;
	this.snippetImports = imports;
	this.snippetClassName = className;
	this.snippetVarClassName = varClassName;
	this.localVarNames = localVarNames;
	this.localVarTypeNames = localVarTypeNames;
	this.localVarModifiers = localVarModifiers;
	this.snippetDeclaringTypeName = declaringTypeName;
	this.buildCUSource();
}
private void buildCUSource() {
	StringBuffer buffer = new StringBuffer();

	// package declaration
	if (this.snippetPackageName != null && this.snippetPackageName.length != 0) {
		buffer.append("package "); //$NON-NLS-1$
		buffer.append(this.snippetPackageName);
		buffer.append(";").append(Util.LINE_SEPARATOR); //$NON-NLS-1$
		this.lineNumberOffset++;
	}

	// import declarations
	char[][] imports = this.snippetImports;
	for (int i = 0; i < imports.length; i++) {
		buffer.append("import "); //$NON-NLS-1$
		buffer.append(imports[i]);
		buffer.append(';').append(Util.LINE_SEPARATOR);
		this.lineNumberOffset++;
	}

	// class declaration
	buffer.append("public class "); //$NON-NLS-1$
	buffer.append(this.snippetClassName);

	// super class is either a global variable class or the CodeSnippet class
	if (this.snippetVarClassName != null) {
		buffer.append(" extends "); //$NON-NLS-1$
		buffer.append(this.snippetVarClassName);
	} else {
		buffer.append(" extends "); //$NON-NLS-1$
		buffer.append(PACKAGE_NAME);
		buffer.append("."); //$NON-NLS-1$
		buffer.append(ROOT_CLASS_NAME);
	}
	buffer.append(" {").append(Util.LINE_SEPARATOR); //$NON-NLS-1$
	this.lineNumberOffset++;

	if (this.snippetDeclaringTypeName != null){
		buffer.append("  "); //$NON-NLS-1$
		buffer.append(this.snippetDeclaringTypeName);
		buffer.append(" "); //$NON-NLS-1$
		buffer.append(DELEGATE_THIS); // val$this
		buffer.append(';').append(Util.LINE_SEPARATOR);
		this.lineNumberOffset++;
	}
	// add some storage location for local variable persisted state
	if (localVarNames != null) {
		for (int i = 0, max = localVarNames.length; i < max; i++) {
			buffer.append("    "); //$NON-NLS-1$
			buffer.append(localVarTypeNames[i]);
			buffer.append(" "); //$NON-NLS-1$
			buffer.append(LOCAL_VAR_PREFIX); // val$...
			buffer.append(localVarNames[i]);
			buffer.append(';').append(Util.LINE_SEPARATOR);
			this.lineNumberOffset++;
		}
	}
	// run() method declaration
	buffer.append("public void run() throws Throwable {").append(Util.LINE_SEPARATOR); //$NON-NLS-1$
	this.lineNumberOffset++;
	startPosOffset = buffer.length();
	buffer.append(codeSnippet);
	// a line separator is required after the code snippet source code
	// in case the code snippet source code ends with a line comment
	// http://dev.eclipse.org/bugs/show_bug.cgi?id=14838
	buffer.append(Util.LINE_SEPARATOR).append('}').append(Util.LINE_SEPARATOR);

	// end of class declaration
	buffer.append('}').append(Util.LINE_SEPARATOR);

	// store result
	int length = buffer.length();
	this.cuSource = new char[length];
	buffer.getChars(0, length, this.cuSource, 0);
}
/**
 * Returns a completion requestor that wraps the given requestor and shift the results
 * according to the start offset and line number offset of the code snippet in the generated compilation unit. 
 */
public ICompletionRequestor getCompletionRequestor(final ICompletionRequestor originalRequestor) {
	return new ICompletionRequestor() {
		public void acceptAnonymousType(char[] superTypePackageName,char[] superTypeName,char[][] parameterPackageNames,char[][] parameterTypeNames,char[][] parameterNames,char[] completionName,int modifiers,int completionStart,int completionEnd, int relevance){
			originalRequestor.acceptAnonymousType(superTypePackageName, superTypeName, parameterPackageNames, parameterTypeNames, parameterNames, completionName, modifiers, completionStart - startPosOffset, completionEnd - startPosOffset, relevance);
		}
		
		public void acceptClass(char[] packageName, char[] className, char[] completionName, int modifiers, int completionStart, int completionEnd, int relevance) {
			// Remove completion on generated class name or generated global variable class name
			if (CharOperation.equals(packageName, CodeSnippetToCuMapper.this.snippetPackageName) 
					&& (CharOperation.equals(className, CodeSnippetToCuMapper.this.snippetClassName)
						|| CharOperation.equals(className, CodeSnippetToCuMapper.this.snippetVarClassName))) return;
			originalRequestor.acceptClass(packageName, className, completionName, modifiers, completionStart - startPosOffset, completionEnd - startPosOffset, relevance);
		}
		public void acceptError(IProblem error) {

			error.setSourceStart(error.getSourceStart() - startPosOffset);
			error.setSourceEnd(error.getSourceEnd() - startPosOffset);
			error.setSourceLineNumber(error.getSourceLineNumber() - lineNumberOffset);
			originalRequestor.acceptError(error);
		}
		public void acceptField(char[] declaringTypePackageName, char[] declaringTypeName, char[] name, char[] typePackageName, char[] typeName, char[] completionName, int modifiers, int completionStart, int completionEnd, int relevance) {
			originalRequestor.acceptField(declaringTypePackageName, declaringTypeName, name, typePackageName, typeName, completionName, modifiers, completionStart - startPosOffset, completionEnd - startPosOffset, relevance);
		}
		public void acceptInterface(char[] packageName, char[] interfaceName, char[] completionName, int modifiers, int completionStart, int completionEnd, int relevance) {
			originalRequestor.acceptInterface(packageName, interfaceName, completionName, modifiers, completionStart - startPosOffset, completionEnd - startPosOffset, relevance);
		}
		public void acceptKeyword(char[] keywordName, int completionStart, int completionEnd, int relevance) {
			originalRequestor.acceptKeyword(keywordName, completionStart - startPosOffset, completionEnd - startPosOffset, relevance);
		}
		public void acceptLabel(char[] labelName, int completionStart, int completionEnd, int relevance) {
			originalRequestor.acceptLabel(labelName, completionStart - startPosOffset, completionEnd - startPosOffset, relevance);
		}
		public void acceptLocalVariable(char[] name, char[] typePackageName, char[] typeName, int modifiers, int completionStart, int completionEnd, int relevance) {
			originalRequestor.acceptLocalVariable(name, typePackageName, typeName, modifiers, completionStart - startPosOffset, completionEnd - startPosOffset, relevance);
		}
		public void acceptMethod(char[] declaringTypePackageName, char[] declaringTypeName, char[] selector, char[][] parameterPackageNames, char[][] parameterTypeNames, char[][] parameterNames, char[] returnTypePackageName, char[] returnTypeName, char[] completionName, int modifiers, int completionStart, int completionEnd, int relevance) {
			// Remove completion on generated method
			if (CharOperation.equals(declaringTypePackageName, CodeSnippetToCuMapper.this.snippetPackageName) 
					&& CharOperation.equals(declaringTypeName, CodeSnippetToCuMapper.this.snippetClassName)
					&& CharOperation.equals(selector, "run".toCharArray())) return; //$NON-NLS-1$
			originalRequestor.acceptMethod(declaringTypePackageName, declaringTypeName, selector, parameterPackageNames, parameterTypeNames, parameterNames, returnTypePackageName, returnTypeName, completionName, modifiers, completionStart - startPosOffset, completionEnd - startPosOffset, relevance);
		}
		public void acceptMethodDeclaration(char[] declaringTypePackageName, char[] declaringTypeName, char[] selector, char[][] parameterPackageNames, char[][] parameterTypeNames, char[][] parameterNames, char[] returnTypePackageName, char[] returnTypeName, char[] completionName, int modifiers, int completionStart, int completionEnd, int relevance) {
			// Remove completion on generated method
			if (CharOperation.equals(declaringTypePackageName, CodeSnippetToCuMapper.this.snippetPackageName) 
					&& CharOperation.equals(declaringTypeName, CodeSnippetToCuMapper.this.snippetClassName)
					&& CharOperation.equals(selector, "run".toCharArray())) return;//$NON-NLS-1$
			originalRequestor.acceptMethodDeclaration(declaringTypePackageName, declaringTypeName, selector, parameterPackageNames, parameterTypeNames, parameterNames, returnTypePackageName, returnTypeName, completionName, modifiers, completionStart - startPosOffset, completionEnd - startPosOffset, relevance);
		}
		public void acceptModifier(char[] modifierName, int completionStart, int completionEnd, int relevance) {
			originalRequestor.acceptModifier(modifierName, completionStart - startPosOffset, completionEnd - startPosOffset, relevance);
		}
		public void acceptPackage(char[] packageName, char[] completionName, int completionStart, int completionEnd, int relevance) {
			originalRequestor.acceptPackage(packageName, completionName, completionStart - startPosOffset, completionEnd - startPosOffset, relevance);
		}
		public void acceptType(char[] packageName, char[] typeName, char[] completionName, int completionStart, int completionEnd, int relevance) {
			// Remove completion on generated class name or generated global variable class name
			if (CharOperation.equals(packageName, CodeSnippetToCuMapper.this.snippetPackageName) 
					&& (CharOperation.equals(snippetClassName, CodeSnippetToCuMapper.this.snippetClassName)
						|| CharOperation.equals(snippetClassName, CodeSnippetToCuMapper.this.snippetVarClassName))) return;
			originalRequestor.acceptType(packageName, typeName, completionName, completionStart - startPosOffset, completionEnd - startPosOffset, relevance);
		}
		public void acceptVariableName(char[] typePackageName, char[] typeName, char[] name, char[] completionName, int completionStart, int completionEnd, int relevance){
			originalRequestor.acceptVariableName(typePackageName, typeName, name, completionName, completionStart, completionEnd, relevance);
		}
	};
}
public char[] getCUSource() {
	if (this.cuSource == null) {
		buildCUSource();
	}
	return this.cuSource;
}
/**
 * Returns the type of evaluation that corresponds to the given line number in the generated compilation unit.
 */
public int getEvaluationType(int lineNumber) {
	int currentLine = 1;

	// check package declaration	
	if (this.snippetPackageName != null && this.snippetPackageName.length != 0) {
		if (lineNumber == 1) {
			return EvaluationResult.T_PACKAGE;
		}
		currentLine++;
	}

	// check imports
	char[][] imports = this.snippetImports;
	if ((currentLine <= lineNumber) && (lineNumber < (currentLine + imports.length))) {
		return EvaluationResult.T_IMPORT;
	}
	currentLine += imports.length + 1; // + 1 to skip the class declaration line

	// check generated fields
	currentLine +=
		(this.snippetDeclaringTypeName == null ? 0 : 1) 
		+ (this.localVarNames == null ? 0 : this.localVarNames.length);
	if (currentLine > lineNumber) {
		return EvaluationResult.T_INTERNAL;
	}
	currentLine ++; // + 1 to skip the method declaration line

	// check code snippet
	if (currentLine >= this.lineNumberOffset) {
		return EvaluationResult.T_CODE_SNIPPET;
	}

	// default
	return EvaluationResult.T_INTERNAL;
}
/**
 * Returns the import defined at the given line number. 
 */
public char[] getImport(int lineNumber) {
	int importStartLine = this.lineNumberOffset - 1 - this.snippetImports.length;
	return this.snippetImports[lineNumber - importStartLine];
}
/**
 * Returns a selection requestor that wraps the given requestor and shift the problems
 * according to the start offset and line number offset of the code snippet in the generated compilation unit. 
 */
public ISelectionRequestor getSelectionRequestor(final ISelectionRequestor originalRequestor) {
	return new ISelectionRequestor() {
		public void acceptClass(char[] packageName, char[] className, boolean needQualification) {
			originalRequestor.acceptClass(packageName, className, needQualification);
		}
		public void acceptError(IProblem error) {
			error.setSourceLineNumber(error.getSourceLineNumber() - lineNumberOffset);
			error.setSourceStart(error.getSourceStart() - startPosOffset);
			error.setSourceEnd(error.getSourceEnd() - startPosOffset);
			originalRequestor.acceptError(error);
		}
		public void acceptField(char[] declaringTypePackageName, char[] declaringTypeName, char[] name) {
			originalRequestor.acceptField(declaringTypePackageName, declaringTypeName, name);
		}
		public void acceptInterface(char[] packageName, char[] interfaceName, boolean needQualification) {
			originalRequestor.acceptInterface(packageName, interfaceName, needQualification);
		}
		public void acceptMethod(char[] declaringTypePackageName, char[] declaringTypeName, char[] selector, char[][] parameterPackageNames, char[][] parameterTypeNames, boolean isConstructor) {
			originalRequestor.acceptMethod(declaringTypePackageName, declaringTypeName, selector, parameterPackageNames, parameterTypeNames, isConstructor);
		}
		public void acceptPackage(char[] packageName) {
			originalRequestor.acceptPackage(packageName);
		}
	};
}
}
