package org.eclipse.jdt.internal.core.search.matching;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
import org.eclipse.jdt.core.search.*;
import org.eclipse.jdt.internal.core.search.*;

import java.util.*;

public class OrPathCombiner implements IIndexSearchRequestor {

	IIndexSearchRequestor targetRequestor;
	Hashtable acceptedAnswers = new Hashtable(5);
	public OrPathCombiner(IIndexSearchRequestor targetRequestor) {
		this.targetRequestor = targetRequestor;
	}

	public void acceptClassDeclaration(
		String resourcePath,
		char[] simpleTypeName,
		char[][] enclosingTypeNames,
		char[] packageName) {

		if (!this.acceptedAnswers.containsKey(resourcePath)) {
			this.acceptedAnswers.put(resourcePath, resourcePath);
			this.targetRequestor.acceptClassDeclaration(
				resourcePath,
				simpleTypeName,
				enclosingTypeNames,
				packageName);
		}
	}

	public void acceptConstructorDeclaration(
		String resourcePath,
		char[] typeName,
		int parameterCount) {
		if (!this.acceptedAnswers.containsKey(resourcePath)) {
			this.acceptedAnswers.put(resourcePath, resourcePath);
			this.targetRequestor.acceptConstructorDeclaration(
				resourcePath,
				typeName,
				parameterCount);
		}
	}

	public void acceptConstructorReference(
		String resourcePath,
		char[] typeName,
		int parameterCount) {
		if (!this.acceptedAnswers.containsKey(resourcePath)) {
			this.acceptedAnswers.put(resourcePath, resourcePath);
			this.targetRequestor.acceptConstructorReference(
				resourcePath,
				typeName,
				parameterCount);
		}
	}

	public void acceptFieldDeclaration(String resourcePath, char[] fieldName) {
		if (!this.acceptedAnswers.containsKey(resourcePath)) {
			this.acceptedAnswers.put(resourcePath, resourcePath);
			this.targetRequestor.acceptFieldDeclaration(resourcePath, fieldName);
		}
	}

	public void acceptFieldReference(String resourcePath, char[] fieldName) {
		if (!this.acceptedAnswers.containsKey(resourcePath)) {
			this.acceptedAnswers.put(resourcePath, resourcePath);
			this.targetRequestor.acceptFieldReference(resourcePath, fieldName);
		}
	}

	public void acceptInterfaceDeclaration(
		String resourcePath,
		char[] simpleTypeName,
		char[][] enclosingTypeNames,
		char[] packageName) {
		if (!this.acceptedAnswers.containsKey(resourcePath)) {
			this.acceptedAnswers.put(resourcePath, resourcePath);
			this.targetRequestor.acceptInterfaceDeclaration(
				resourcePath,
				simpleTypeName,
				enclosingTypeNames,
				packageName);
		}
	}

	public void acceptMethodDeclaration(
		String resourcePath,
		char[] methodName,
		int parameterCount) {
		if (!this.acceptedAnswers.containsKey(resourcePath)) {
			this.acceptedAnswers.put(resourcePath, resourcePath);
			this.targetRequestor.acceptMethodDeclaration(
				resourcePath,
				methodName,
				parameterCount);
		}
	}

	public void acceptMethodReference(
		String resourcePath,
		char[] methodName,
		int parameterCount) {
		if (!this.acceptedAnswers.containsKey(resourcePath)) {
			this.acceptedAnswers.put(resourcePath, resourcePath);
			this.targetRequestor.acceptMethodReference(
				resourcePath,
				methodName,
				parameterCount);
		}
	}

	public void acceptPackageReference(String resourcePath, char[] packageName) {
		if (!this.acceptedAnswers.containsKey(resourcePath)) {
			this.acceptedAnswers.put(resourcePath, resourcePath);
			this.targetRequestor.acceptPackageReference(resourcePath, packageName);
		}
	}

	public void acceptSuperTypeReference(
		String resourcePath,
		char[] qualification,
		char[] typeName,
		char[] enclosingTypeName,
		char classOrInterface,
		char[] superQualification,
		char[] superTypeName,
		char superClassOrInterface,
		int modifiers) {
		if (!this.acceptedAnswers.containsKey(resourcePath)) {
			this.acceptedAnswers.put(resourcePath, resourcePath);
			this.targetRequestor.acceptSuperTypeReference(
				resourcePath,
				qualification,
				typeName,
				enclosingTypeName,
				classOrInterface,
				superQualification,
				superTypeName,
				superClassOrInterface,
				modifiers);
		}
	}

	public void acceptTypeReference(String resourcePath, char[] typeName) {
		if (!this.acceptedAnswers.containsKey(resourcePath)) {
			this.acceptedAnswers.put(resourcePath, resourcePath);
			this.targetRequestor.acceptTypeReference(resourcePath, typeName);
		}
	}

}
