package org.eclipse.jdt.internal.core.search;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
import org.eclipse.core.resources.*;
import org.eclipse.core.runtime.*;

import java.util.*;

/**
 * Collects the resource paths reported by a client to this search requestor.
 */
public class PathCollector implements IIndexSearchRequestor {

	/* a set of resource paths */
	public Hashtable paths = new Hashtable(5);
	/**
	 * @see IIndexSearchRequestor
	 */
	public void acceptClassDeclaration(
		String resourcePath,
		char[] simpleTypeName,
		char[][] enclosingTypeNames,
		char[] packageName) {
		this.paths.put(resourcePath, resourcePath);
	}

	/**
	 * @see IIndexSearchRequestor
	 */
	public void acceptConstructorDeclaration(
		String resourcePath,
		char[] typeName,
		int parameterCount) {

		this.paths.put(resourcePath, resourcePath);
	}

	/**
	 * @see IIndexSearchRequestor
	 */
	public void acceptConstructorReference(
		String resourcePath,
		char[] typeName,
		int parameterCount) {

		this.paths.put(resourcePath, resourcePath);
	}

	/**
	 * @see IIndexSearchRequestor
	 */
	public void acceptFieldDeclaration(String resourcePath, char[] fieldName) {
		this.paths.put(resourcePath, resourcePath);
	}

	/**
	 * @see IIndexSearchRequestor
	 */
	public void acceptFieldReference(String resourcePath, char[] fieldName) {

		this.paths.put(resourcePath, resourcePath);
	}

	/**
	 * @see IIndexSearchRequestor
	 */
	public void acceptInterfaceDeclaration(
		String resourcePath,
		char[] simpleTypeName,
		char[][] enclosingTypeNames,
		char[] packageName) {

		this.paths.put(resourcePath, resourcePath);
	}

	/**
	 * @see IIndexSearchRequestor
	 */
	public void acceptMethodDeclaration(
		String resourcePath,
		char[] methodName,
		int parameterCount) {

		this.paths.put(resourcePath, resourcePath);
	}

	/**
	 * @see IIndexSearchRequestor
	 */
	public void acceptMethodReference(
		String resourcePath,
		char[] methodName,
		int parameterCount) {

		this.paths.put(resourcePath, resourcePath);
	}

	/**
	 * @see IIndexSearchRequestor
	 */
	public void acceptPackageReference(String resourcePath, char[] packageName) {

		this.paths.put(resourcePath, resourcePath);
	}

	/**
	 * @see IIndexSearchRequestor
	 */
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
		this.paths.put(resourcePath, resourcePath);
	}

	/**
	 * @see IIndexSearchRequestor
	 */
	public void acceptSuperTypeReference(
		String resourcePath,
		char[] qualification,
		char[] typeName,
		char classOrInterface,
		char[] superQualification,
		char[] superTypeName,
		char superClassOrInterface,
		int modifiers) {
		this.paths.put(resourcePath, resourcePath);
	}

	/**
	 * @see IIndexSearchRequestor
	 */
	public void acceptTypeReference(String resourcePath, char[] typeName) {
		this.paths.put(resourcePath, resourcePath);
	}

	/**
	 * Returns the files that correspond to the paths that have been collected.
	 */
	public IFile[] getFiles(IWorkspace workspace) {
		IFile[] result = new IFile[this.paths.size()];
		int i = 0;
		for (Enumeration enum = this.paths.elements(); enum.hasMoreElements();) {
			String resourcePath = (String) enum.nextElement();
			IPath path = new Path(resourcePath);
			result[i++] = workspace.getRoot().getFile(path);
		}
		return result;
	}

	/**
	 * Returns the paths that have been collected.
	 */
	public String[] getPaths() {
		String[] result = new String[this.paths.size()];
		int i = 0;
		for (Enumeration enum = this.paths.elements(); enum.hasMoreElements();) {
			result[i++] = (String) enum.nextElement();
		}
		return result;
	}

}
