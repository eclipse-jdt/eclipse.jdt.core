package org.eclipse.jdt.internal.eval;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
import org.eclipse.jdt.internal.codeassist.*;
import org.eclipse.jdt.internal.compiler.IProblem;
import org.eclipse.jdt.internal.compiler.env.IConstants;
import org.eclipse.jdt.internal.compiler.util.CharOperation;
import org.eclipse.jdt.internal.core.JavaModelManager;

/**
 * Maps back and forth a code snippet to a compilation unit.
 * The structure of the compilation unit is as follows:
 * [package <package name>;]
 * [import <import name>;]*
 * public class <code snippet class name> extends <global variable class name> {
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
	private char[] codeSnippet;
	private char[] packageName;
	private char[][] imports;
	char[] className; // NB: Make it package default visibility to optimize access from inner classes
	private char[] varClassName;

	// Mapping of external local variables
	private char[][] localVarNames;
	private char[][] localVarTypeNames;
	private int[] localVarModifiers;
	private char[] declaringTypeName;

/**
 * Rebuild source in presence of external local variables
 */
 public CodeSnippetToCuMapper(char[] codeSnippet, char[] packageName, char[][] imports, char[] className, char[] varClassName, char[][] localVarNames, char[][] localVarTypeNames, int[] localVarModifiers, char[] declaringTypeName) {
	this.codeSnippet = codeSnippet;
	this.packageName = packageName;
	this.imports = imports;
	this.className = className;
	this.varClassName = varClassName;
	this.localVarNames = localVarNames;
	this.localVarTypeNames = localVarTypeNames;
	this.localVarModifiers = localVarModifiers;
	this.declaringTypeName = declaringTypeName;
	this.buildCUSource();
}
private void buildCUSource() {
	StringBuffer buffer = new StringBuffer();

	// package declaration
	if (this.packageName != null && this.packageName.length != 0) {
		buffer.append("package "); //$NON-NLS-1$
		buffer.append(this.packageName);
		buffer.append(";").append(JavaModelManager.LINE_SEPARATOR); //$NON-NLS-1$
		this.lineNumberOffset++;
	}

	// import declarations
	char[][] imports = this.imports;
	for (int i = 0; i < imports.length; i++) {
		buffer.append("import "); //$NON-NLS-1$
		buffer.append(imports[i]);
		buffer.append(';').append(JavaModelManager.LINE_SEPARATOR);
		this.lineNumberOffset++;
	}

	// class declaration
	buffer.append("public class "); //$NON-NLS-1$
	buffer.append(this.className);

	// super class is either a global variable class or the CodeSnippet class
	if (this.varClassName != null) {
		buffer.append(" extends "); //$NON-NLS-1$
		buffer.append(this.varClassName);
	} else {
		buffer.append(" extends "); //$NON-NLS-1$
		buffer.append(PACKAGE_NAME);
		buffer.append("."); //$NON-NLS-1$
		buffer.append(ROOT_CLASS_NAME);
	}
	buffer.append(" {").append(JavaModelManager.LINE_SEPARATOR); //$NON-NLS-1$
	this.lineNumberOffset++;

	if (this.declaringTypeName != null){
		buffer.append("  "); //$NON-NLS-1$
		buffer.append(this.declaringTypeName);
		buffer.append(" "); //$NON-NLS-1$
		buffer.append(DELEGATE_THIS); // val$this
		buffer.append(';').append(JavaModelManager.LINE_SEPARATOR);
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
			buffer.append(';').append(JavaModelManager.LINE_SEPARATOR);
			this.lineNumberOffset++;
		}
	}
	// run() method declaration
	buffer.append("public void run() throws Throwable {").append(JavaModelManager.LINE_SEPARATOR); //$NON-NLS-1$
	this.lineNumberOffset++;
	startPosOffset = buffer.length();
	buffer.append(codeSnippet);
	buffer.append('}').append(JavaModelManager.LINE_SEPARATOR);

	// end of class declaration
	buffer.append('}').append(JavaModelManager.LINE_SEPARATOR);

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
	final int startPosOffset = this.startPosOffset;
	final int lineNumberOffset = this.lineNumberOffset;
	return new ICompletionRequestor() {
		public void acceptClass(char[] packageName, char[] className, char[] completionName, int modifiers, int completionStart, int completionEnd) {
			// Remove completion on generated class name or generated global variable class name
			if (CharOperation.equals(packageName, CodeSnippetToCuMapper.this.packageName) 
					&& (CharOperation.equals(className, CodeSnippetToCuMapper.this.className)
						|| CharOperation.equals(className, CodeSnippetToCuMapper.this.varClassName))) return;
			originalRequestor.acceptClass(packageName, className, completionName, modifiers, completionStart - startPosOffset, completionEnd - startPosOffset);
		}
		public void acceptError(IProblem error) {
			error.setSourceLineNumber(error.getSourceLineNumber() - lineNumberOffset);
			error.setSourceStart(error.getSourceStart() - startPosOffset);
			error.setSourceEnd(error.getSourceEnd() - startPosOffset);
			originalRequestor.acceptError(error);
		}
		public void acceptField(char[] declaringTypePackageName, char[] declaringTypeName, char[] name, char[] typePackageName, char[] typeName, char[] completionName, int modifiers, int completionStart, int completionEnd) {
			originalRequestor.acceptField(declaringTypePackageName, declaringTypeName, name, typePackageName, typeName, completionName, modifiers, completionStart - startPosOffset, completionEnd - startPosOffset);
		}
		public void acceptInterface(char[] packageName, char[] interfaceName, char[] completionName, int modifiers, int completionStart, int completionEnd) {
			originalRequestor.acceptInterface(packageName, interfaceName, completionName, modifiers, completionStart - startPosOffset, completionEnd - startPosOffset);
		}
		public void acceptKeyword(char[] keywordName, int completionStart, int completionEnd) {
			originalRequestor.acceptKeyword(keywordName, completionStart - startPosOffset, completionEnd - startPosOffset);
		}
		public void acceptLabel(char[] labelName, int completionStart, int completionEnd) {
			originalRequestor.acceptLabel(labelName, completionStart - startPosOffset, completionEnd - startPosOffset);
		}
		public void acceptLocalVariable(char[] name, char[] typePackageName, char[] typeName, int modifiers, int completionStart, int completionEnd) {
			originalRequestor.acceptLocalVariable(name, typePackageName, typeName, modifiers, completionStart - startPosOffset, completionEnd - startPosOffset);
		}
		public void acceptMethod(char[] declaringTypePackageName, char[] declaringTypeName, char[] selector, char[][] parameterPackageNames, char[][] parameterTypeNames, char[] returnTypePackageName, char[] returnTypeName, char[] completionName, int modifiers, int completionStart, int completionEnd) {
			// Remove completion on generated method
			if (CharOperation.equals(declaringTypePackageName, CodeSnippetToCuMapper.this.packageName) 
					&& CharOperation.equals(declaringTypeName, CodeSnippetToCuMapper.this.className)
					&& CharOperation.equals(selector, "run".toCharArray())) return; //$NON-NLS-1$
			originalRequestor.acceptMethod(declaringTypePackageName, declaringTypeName, selector, parameterPackageNames, parameterTypeNames, returnTypePackageName, returnTypeName, completionName, modifiers, completionStart - startPosOffset, completionEnd - startPosOffset);
		}
		public void acceptModifier(char[] modifierName, int completionStart, int completionEnd) {
			originalRequestor.acceptModifier(modifierName, completionStart - startPosOffset, completionEnd - startPosOffset);
		}
		public void acceptPackage(char[] packageName, char[] completionName, int completionStart, int completionEnd) {
			originalRequestor.acceptPackage(packageName, completionName, completionStart - startPosOffset, completionEnd - startPosOffset);
		}
		public void acceptType(char[] packageName, char[] typeName, char[] completionName, int completionStart, int completionEnd) {
			// Remove completion on generated class name or generated global variable class name
			if (CharOperation.equals(packageName, CodeSnippetToCuMapper.this.packageName) 
					&& (CharOperation.equals(className, CodeSnippetToCuMapper.this.className)
						|| CharOperation.equals(className, CodeSnippetToCuMapper.this.varClassName))) return;
			originalRequestor.acceptType(packageName, typeName, completionName, completionStart - startPosOffset, completionEnd - startPosOffset);
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
	if (this.packageName != null && this.packageName.length != 0) {
		if (lineNumber == 1) {
			return EvaluationResult.T_PACKAGE;
		}
		currentLine++;
	}

	// check imports
	char[][] imports = this.imports;
	if ((currentLine <= lineNumber) && (lineNumber < (currentLine + imports.length))) {
		return EvaluationResult.T_IMPORT;
	}
	currentLine += imports.length + 1; // + 1 to skip the class declaration line

	// check generated fields
	currentLine +=
		(this.declaringTypeName == null ? 0 : 1) 
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
	int importStartLine = this.lineNumberOffset - 2 - this.imports.length;
	return this.imports[lineNumber - importStartLine];
}
/**
 * Returns a selection requestor that wraps the given requestor and shift the problems
 * according to the start offset and line number offset of the code snippet in the generated compilation unit. 
 */
public ISelectionRequestor getSelectionRequestor(final ISelectionRequestor originalRequestor) {
	final int startPosOffset = this.startPosOffset;
	final int lineNumberOffset = this.lineNumberOffset;
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
		public void acceptMethod(char[] declaringTypePackageName, char[] declaringTypeName, char[] selector, char[][] parameterPackageNames, char[][] parameterTypeNames) {
			originalRequestor.acceptMethod(declaringTypePackageName, declaringTypeName, selector, parameterPackageNames, parameterTypeNames);
		}
		public void acceptPackage(char[] packageName) {
			originalRequestor.acceptPackage(packageName);
		}
	};
}
}
