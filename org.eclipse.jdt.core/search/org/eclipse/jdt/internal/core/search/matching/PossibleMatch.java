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
package org.eclipse.jdt.internal.core.search.matching;

import org.eclipse.core.resources.IResource;
import org.eclipse.jdt.core.*;
import org.eclipse.jdt.core.compiler.CharOperation;
import org.eclipse.jdt.core.search.SearchDocument;
import org.eclipse.jdt.internal.compiler.ast.CompilationUnitDeclaration;
import org.eclipse.jdt.internal.compiler.classfmt.ClassFileReader;
import org.eclipse.jdt.internal.compiler.env.ICompilationUnit;
import org.eclipse.jdt.internal.core.*;

public class PossibleMatch implements ICompilationUnit {

public static final String NO_SOURCE_FILE_NAME = "NO SOURCE FILE NAME"; //$NON-NLS-1$

public IResource resource;
public Openable openable;
public MatchingNodeSet nodeSet;
public char[][] compoundName;
CompilationUnitDeclaration parsedUnit;
public SearchDocument document;
private String sourceFileName;
private char[] source;

public PossibleMatch(MatchLocator locator, IResource resource, Openable openable, SearchDocument document) {
	this.resource = resource;
	this.openable = openable;
	this.document = document;
	this.nodeSet = new MatchingNodeSet();
	char[] qualifiedName = getQualifiedName();
	if (qualifiedName != null)
		this.compoundName = CharOperation.splitOn('.', qualifiedName);
}
public void cleanUp() {
	this.source = null;
}
public boolean equals(Object obj) {
	if (this.compoundName == null) return super.equals(obj);
	if (!(obj instanceof PossibleMatch)) return false;

	// By using the compoundName of the source file, multiple .class files (A, A$M...) are considered equal
	// Even .class files for secondary types and their nested types
	return CharOperation.equals(this.compoundName, ((PossibleMatch) obj).compoundName);
}
public char[] getContents() {
	if (this.source != null) return this.source;

	if (this.openable instanceof ClassFile) {
		String fileName = getSourceFileName();
		if (fileName == NO_SOURCE_FILE_NAME) return null;

		SourceMapper sourceMapper = this.openable.getSourceMapper();
		IType type = ((ClassFile) this.openable).getType();
		return this.source = sourceMapper.findSource(type, fileName);
	}
	return this.source = this.document.getCharContents();
}
/**
 * The exact openable file name. In particular, will be the originating .class file for binary openable with attached
 * source.
 * @see PackageReferenceLocator#isDeclaringPackageFragment(IPackageFragment, org.eclipse.jdt.internal.compiler.lookup.ReferenceBinding)
 */
public char[] getFileName() {
	return this.openable.getElementName().toCharArray();
}
public char[] getMainTypeName() {
	// The file is no longer opened to get its name => remove fix for bug 32182
	return this.compoundName[this.compoundName.length-1];
}
public char[][] getPackageName() {
	int length = this.compoundName.length;
	if (length <= 1) return CharOperation.NO_CHAR_CHAR;
	return CharOperation.subarray(this.compoundName, 0, length - 1);
}
/*
 * Returns the fully qualified name of the main type of the compilation unit
 * or the main type of the .java file that defined the class file.
 */
private char[] getQualifiedName() {
	if (this.openable instanceof CompilationUnit) {
		// get file name
		String fileName = this.resource.getFullPath().lastSegment();
		// get main type name
		char[] mainTypeName = fileName.substring(0, fileName.length()-5).toCharArray(); 
		CompilationUnit cu = (CompilationUnit) this.openable;
		return cu.getType(new String(mainTypeName)).getFullyQualifiedName().toCharArray();
	} else if (this.openable instanceof ClassFile) {
		String fileName = getSourceFileName();
		if (fileName == NO_SOURCE_FILE_NAME)
			return ((ClassFile) this.openable).getType().getFullyQualifiedName('.').toCharArray();

		String simpleName = fileName.substring(0, fileName.length() - 5); // length-".java".length()
		String pkgName = this.openable.getParent().getElementName();
		if (pkgName.length() == 0)
			return simpleName.toCharArray();
		return (pkgName + '.' + simpleName).toCharArray();
	}
	return null;
}
/*
 * Returns the source file name of the class file.
 * Returns NO_SOURCE_FILE_NAME if not found.
 */
private String getSourceFileName() {
	if (this.sourceFileName != null) return this.sourceFileName;

	this.sourceFileName = NO_SOURCE_FILE_NAME; 
	SourceMapper sourceMapper = this.openable.getSourceMapper();
	if (sourceMapper != null) {
		IType type = ((ClassFile) this.openable).getType();
		ClassFileReader reader = MatchLocator.classFileReader(type);
		if (reader != null)
			this.sourceFileName = sourceMapper.findSourceFileName(type, reader);
	}
	return this.sourceFileName;
}	
public int hashCode() {
	if (this.compoundName == null) return super.hashCode();

	int hashCode = 0;
	for (int i = 0, length = this.compoundName.length; i < length; i++)
		hashCode += CharOperation.hashCode(this.compoundName[i]);
	return hashCode;
}
public String toString() {
	return this.openable == null ? "Fake PossibleMatch" : this.openable.toString(); //$NON-NLS-1$
}
}
