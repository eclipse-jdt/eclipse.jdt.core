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
package org.eclipse.jdt.internal.core.search.matching;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.jdt.core.*;
import org.eclipse.jdt.core.compiler.CharOperation;
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
private String sourceFileName;
private char[] source;

public PossibleMatch(MatchLocator locator, IResource resource, Openable openable) {
	this.resource = resource;
	this.openable = openable;
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
	return CharOperation.equals(this.compoundName, ((PossibleMatch) obj).compoundName);
}
public char[] getContents() {
	if (this.source != null) return this.source;

	this.source = CharOperation.NO_CHAR;
	try {
		if (this.openable instanceof CompilationUnit) {
			if (((CompilationUnit) this.openable).isWorkingCopy()) {
				IBuffer buffer = this.openable.getBuffer();
				if (buffer == null) return null;
				this.source = buffer.getCharacters();
			} else {
				this.source = Util.getResourceContentsAsCharArray((IFile) this.resource);
			}
		} else if (this.openable instanceof ClassFile) {
			SourceMapper sourceMapper = this.openable.getSourceMapper();
			if (sourceMapper != null) {
				String fileName = getSourceFileName();
				if (fileName == NO_SOURCE_FILE_NAME) return null;

				IType type = ((ClassFile) this.openable).getType();
				this.source = sourceMapper.findSource(type, fileName);
			}
		}
	} catch (JavaModelException e) { // ignored
	}
	return this.source;
}
public char[] getFileName() {
	return this.openable.getPath().toString().toCharArray();
}
public char[] getMainTypeName() {
	return null; // cannot know the main type name without opening .java or .class file
	                  // see http://bugs.eclipse.org/bugs/show_bug.cgi?id=32182
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
		if (fileName == NO_SOURCE_FILE_NAME) {
			try {
				return ((ClassFile) this.openable).getType().getFullyQualifiedName('.').toCharArray();
			} catch (JavaModelException e) {
				return null;
			}
		}
		String simpleName = fileName.substring(0, fileName.length()-5); // length-".java".length()
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
	try {
		SourceMapper sourceMapper = this.openable.getSourceMapper();
		if (sourceMapper != null) {
			IType type = ((ClassFile) this.openable).getType();
			ClassFileReader reader = MatchLocator.classFileReader(type);
			if (reader != null)
				this.sourceFileName = sourceMapper.findSourceFileName(type, reader);
		}
	} catch (JavaModelException e) { // ignored
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
