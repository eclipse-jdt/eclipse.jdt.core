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
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.core.*;
import org.eclipse.jdt.core.compiler.CharOperation;
import org.eclipse.jdt.core.search.IJavaSearchResultCollector;
import org.eclipse.jdt.internal.compiler.ast.CompilationUnitDeclaration;
import org.eclipse.jdt.internal.compiler.classfmt.ClassFileReader;
import org.eclipse.jdt.internal.compiler.env.*;
import org.eclipse.jdt.internal.compiler.env.ICompilationUnit;
import org.eclipse.jdt.internal.compiler.lookup.*;
import org.eclipse.jdt.internal.compiler.problem.AbortCompilation;
import org.eclipse.jdt.internal.core.*;

public class PotentialMatch implements ICompilationUnit {

public static final String NO_SOURCE_FILE_NAME = "NO SOURCE FILE NAME"; //$NON-NLS-1$

public IResource resource;
public Openable openable;
public MatchingNodeSet matchingNodeSet;
public char[][] compoundName;
CompilationUnitDeclaration parsedUnit;
private String sourceFileName;

public PotentialMatch(MatchLocator locator, IResource resource, Openable openable) {
	this.resource = resource;
	this.openable = openable;
	this.matchingNodeSet = new MatchingNodeSet(locator);
	char[] qualifiedName = getQualifiedName();
	if (qualifiedName != null)
		this.compoundName = CharOperation.splitOn('.', qualifiedName);
}
public boolean equals(Object obj) {
	if (this.compoundName == null) return super.equals(obj);
	if (!(obj instanceof PotentialMatch)) return false;
	return CharOperation.equals(this.compoundName, ((PotentialMatch) obj).compoundName);
}
public char[] getContents() {
	char[] source = null;
	try {
		if (this.openable instanceof CompilationUnit) {
			if (((CompilationUnit) this.openable).isWorkingCopy()) {
				IBuffer buffer = this.openable.getBuffer();
				if (buffer == null) return null;
				source = buffer.getCharacters();
			} else {
				source = Util.getResourceContentsAsCharArray((IFile) this.resource);
			}
		} else if (this.openable instanceof ClassFile) {
			SourceMapper sourceMapper = this.openable.getSourceMapper();
			if (sourceMapper != null) {
				String fileName = getSourceFileName();
				if (fileName == NO_SOURCE_FILE_NAME) return null;

				IType type = ((ClassFile) this.openable).getType();
				source = sourceMapper.findSource(type, fileName);
			}
		}
	} catch (JavaModelException e) {
	}
	if (source == null) return CharOperation.NO_CHAR;
	return source;
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
			ClassFileReader reader = this.matchingNodeSet.locator.classFileReader(type);
			if (reader != null)
				this.sourceFileName = sourceMapper.findSourceFileName(type, reader);
		}
	} catch (JavaModelException e) {
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
/**
 * Locate declaration in the current class file. This class file is always in a jar.
 */
public void locateMatchesInClassFile() throws CoreException {
	MatchLocator locator = this.matchingNodeSet.locator;
	org.eclipse.jdt.internal.core.ClassFile classFile = (org.eclipse.jdt.internal.core.ClassFile) this.openable;
	IBinaryType info = locator.getBinaryInfo(classFile, this.resource);
	if (info == null) return; // unable to go further

	// check class definition
	SearchPattern pattern = locator.pattern;
	BinaryType binaryType = (BinaryType) classFile.getType();
	if (pattern.matchesBinary(info, null))
		locator.reportBinaryMatch(binaryType, info, IJavaSearchResultCollector.EXACT_MATCH);

	int accuracy = IJavaSearchResultCollector.EXACT_MATCH;
	if (pattern.mustResolve) {
		try {
			BinaryTypeBinding binding = locator.cacheBinaryType(binaryType);
			if (binding != null) {
				// filter out element not in hierarchy scope
				if (!locator.typeInHierarchy(binding)) return;

				MethodBinding[] methods = binding.methods();
				for (int i = 0, l = methods.length; i < l; i++) {
					MethodBinding method = methods[i];
					int level = pattern.matchLevel(method);
					if (level >= SearchPattern.POTENTIAL_MATCH) {
						IMethod methodHandle = binaryType.getMethod(
							new String(method.isConstructor() ? binding.compoundName[binding.compoundName.length-1] : method.selector),
							Signature.getParameterTypes(new String(method.signature()).replace('/', '.')));
						locator.reportBinaryMatch(
							methodHandle, 
							info, 
							level == SearchPattern.ACCURATE_MATCH
								? IJavaSearchResultCollector.EXACT_MATCH
								: IJavaSearchResultCollector.POTENTIAL_MATCH);
					}
				}

				FieldBinding[] fields = binding.fields();
				for (int i = 0, l = fields.length; i < l; i++) {
					FieldBinding field = fields[i];
					int level = pattern.matchLevel(field);
					if (level >= SearchPattern.POTENTIAL_MATCH) {
						IField fieldHandle = binaryType.getField(new String(field.name));
						locator.reportBinaryMatch(
							fieldHandle, 
							info, 
							level == SearchPattern.ACCURATE_MATCH
								? IJavaSearchResultCollector.EXACT_MATCH
								: IJavaSearchResultCollector.POTENTIAL_MATCH);
					}
				}

				// no need to check binary info since resolve was successful
				return;
			}
		} catch (AbortCompilation e) { // if compilation was aborted it is a problem with the class path
		}
		// report as a potential match if binary info matches the pattern		
		accuracy = IJavaSearchResultCollector.POTENTIAL_MATCH;
	}

	IBinaryMethod[] methods = info.getMethods();
	if (methods != null) {
		for (int i = 0, l = methods.length; i < l; i++) {
			IBinaryMethod method = methods[i];
			if (pattern.matchesBinary(method, info)) {
				IMethod methodHandle = binaryType.getMethod(
					new String(method.isConstructor() ? info.getName() : method.getSelector()),
					Signature.getParameterTypes(new String(method.getMethodDescriptor()).replace('/', '.')));
				locator.reportBinaryMatch(methodHandle, info, accuracy);
			}
		}
	}

	IBinaryField[] fields = info.getFields();
	if (fields != null) {
		for (int i = 0, l = fields.length; i < l; i++) {
			IBinaryField field = fields[i];
			if (pattern.matchesBinary(field, info)) {
				IField fieldHandle = binaryType.getField(new String(field.getName()));
				locator.reportBinaryMatch(fieldHandle, info, accuracy);
			}
		}
	}
}
public String toString() {
	return this.openable == null ? "Fake PotentialMatch" : this.openable.toString(); //$NON-NLS-1$
}
}
