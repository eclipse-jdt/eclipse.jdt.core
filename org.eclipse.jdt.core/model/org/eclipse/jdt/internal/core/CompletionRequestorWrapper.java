package org.eclipse.jdt.internal.core;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */

import org.eclipse.core.resources.*;
import org.eclipse.core.runtime.*;
import org.eclipse.jdt.internal.codeassist.ICompletionRequestor;
import org.eclipse.jdt.internal.compiler.IProblem;
import org.eclipse.jdt.core.*;

public class CompletionRequestorWrapper implements ICompletionRequestor {
	ICodeCompletionRequestor clientRequestor;
	public CompletionRequestorWrapper(ICodeCompletionRequestor clientRequestor) {
		this.clientRequestor = clientRequestor;
	}

	/**
	 * See ICompletionRequestor
	 */
	public void acceptClass(
		char[] packageName,
		char[] className,
		char[] completionName,
		int modifiers,
		int completionStart,
		int completionEnd) {

		this.clientRequestor.acceptClass(
			packageName,
			className,
			completionName,
			modifiers,
			completionStart,
			completionEnd);
	}

	/**
	 * See ICompletionRequestor
	 */
	public void acceptError(IProblem error) {

		if (true)
			return; // work-around PR 1GD9RLP: ITPJCORE:WIN2000 - Code assist is slow
		try {
			IMarker marker =
				ResourcesPlugin.getWorkspace().getRoot().createMarker(
					IJavaModelMarker.TRANSIENT_PROBLEM);
			marker.setAttribute(IJavaModelMarker.ID, error.getID());
			marker.setAttribute(IMarker.CHAR_START, error.getSourceStart());
			marker.setAttribute(IMarker.CHAR_END, error.getSourceEnd() + 1);
			marker.setAttribute(IMarker.LINE_NUMBER, error.getSourceLineNumber());
			//marker.setAttribute(IMarker.LOCATION, "#" + error.getSourceLineNumber());
			marker.setAttribute(IMarker.MESSAGE, error.getMessage());
			marker.setAttribute(IMarker.SEVERITY, IMarker.SEVERITY_ERROR);

			this.clientRequestor.acceptError(marker);

		} catch (CoreException e) {
		}
	}

	/**
	 * See ICompletionRequestor
	 */
	public void acceptField(
		char[] declaringTypePackageName,
		char[] declaringTypeName,
		char[] name,
		char[] typePackageName,
		char[] typeName,
		char[] completionName,
		int modifiers,
		int completionStart,
		int completionEnd) {
		this.clientRequestor.acceptField(
			declaringTypePackageName,
			declaringTypeName,
			name,
			typePackageName,
			typeName,
			completionName,
			modifiers,
			completionStart,
			completionEnd);
	}

	/**
	 * See ICompletionRequestor
	 */
	public void acceptInterface(
		char[] packageName,
		char[] interfaceName,
		char[] completionName,
		int modifiers,
		int completionStart,
		int completionEnd) {
		this.clientRequestor.acceptInterface(
			packageName,
			interfaceName,
			completionName,
			modifiers,
			completionStart,
			completionEnd);
	}

	/**
	 * See ICompletionRequestor
	 */
	public void acceptKeyword(
		char[] keywordName,
		int completionStart,
		int completionEnd) {
		this.clientRequestor.acceptKeyword(keywordName, completionStart, completionEnd);
	}

	/**
	 * See ICompletionRequestor
	 */
	public void acceptLabel(
		char[] labelName,
		int completionStart,
		int completionEnd) {
		this.clientRequestor.acceptLabel(labelName, completionStart, completionEnd);
	}

	/**
	 * See ICompletionRequestor
	 */
	public void acceptLocalVariable(
		char[] name,
		char[] typePackageName,
		char[] typeName,
		int modifiers,
		int completionStart,
		int completionEnd) {
		this.clientRequestor.acceptLocalVariable(
			name,
			typePackageName,
			typeName,
			modifiers,
			completionStart,
			completionEnd);
	}

	/**
	 * See ICompletionRequestor
	 */
	public void acceptMethod(
		char[] declaringTypePackageName,
		char[] declaringTypeName,
		char[] selector,
		char[][] parameterPackageNames,
		char[][] parameterTypeNames,
		char[] returnTypePackageName,
		char[] returnTypeName,
		char[] completionName,
		int modifiers,
		int completionStart,
		int completionEnd) {
		this.clientRequestor.acceptMethod(
			declaringTypePackageName,
			declaringTypeName,
			selector,
			parameterPackageNames,
			parameterTypeNames,
			returnTypePackageName,
			returnTypeName,
			completionName,
			modifiers,
			completionStart,
			completionEnd);
	}

	/**
	 * See ICompletionRequestor
	 */
	public void acceptModifier(
		char[] modifierName,
		int completionStart,
		int completionEnd) {
		this.clientRequestor.acceptModifier(
			modifierName,
			completionStart,
			completionEnd);
	}

	/**
	 * See ICompletionRequestor
	 */
	public void acceptPackage(
		char[] packageName,
		char[] completionName,
		int completionStart,
		int completionEnd) {
		this.clientRequestor.acceptPackage(
			packageName,
			completionName,
			completionStart,
			completionEnd);
	}

	/**
	 * See ICompletionRequestor
	 */
	public void acceptType(
		char[] packageName,
		char[] typeName,
		char[] completionName,
		int completionStart,
		int completionEnd) {
		this.clientRequestor.acceptType(
			packageName,
			typeName,
			completionName,
			completionStart,
			completionEnd);
	}

}
