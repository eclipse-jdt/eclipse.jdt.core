/*******************************************************************************
 * Copyright (c) 2003 International Business Machines Corp. and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0 
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 ******************************************************************************/
package org.eclipse.jdt.internal.core.search.matching;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.core.*;
import org.eclipse.jdt.core.compiler.CharOperation;
import org.eclipse.jdt.core.search.IJavaSearchResultCollector;
import org.eclipse.jdt.internal.compiler.classfmt.ClassFileReader;
import org.eclipse.jdt.internal.compiler.env.*;
import org.eclipse.jdt.internal.compiler.env.ICompilationUnit;
import org.eclipse.jdt.internal.compiler.lookup.BinaryTypeBinding;
import org.eclipse.jdt.internal.compiler.lookup.FieldBinding;
import org.eclipse.jdt.internal.compiler.lookup.MethodBinding;
import org.eclipse.jdt.internal.compiler.problem.AbortCompilation;
import org.eclipse.jdt.internal.core.*;

public class PotentialMatch implements ICompilationUnit {
	public static final String NO_SOURCE_FILE_NAME = "NO SOURCE FILE NAME"; //$NON-NLS-1$

	private MatchLocator2 locator;
	public IResource resource;
	public Openable openable;
	public char[][] compoundName;
	MatchingNodeSet matchingNodeSet;
	private String sourceFileName;

	public PotentialMatch(
			MatchLocator2 locator, 
			IResource resource, 
			Openable openable) {
		this.locator = locator;
		this.resource = resource;
		this.openable = openable;
		this.matchingNodeSet = new MatchingNodeSet(locator);
		char[] qualifiedName = getQualifiedName();
		if (qualifiedName != null) {
			this.compoundName = CharOperation.splitOn('.', qualifiedName);
		}
	}
	public boolean equals(Object obj) {
		if (this.compoundName == null) return super.equals(obj);
		if (!(obj instanceof PotentialMatch)) return false;
		return CharOperation.equals(this.compoundName, ((PotentialMatch)obj).compoundName);
	}

	/*
	 * Finds the source of this class file.
	 * Returns null if not found.
	 */
	private char[] findClassFileSource() {
		String sourceFileName = getSourceFileName();
		if (sourceFileName == NO_SOURCE_FILE_NAME) return null;
		char[] source = null; 
		try {
			SourceMapper sourceMapper = this.openable.getSourceMapper();
			if (sourceMapper != null) {
				IType type = ((ClassFile)this.openable).getType();
				source = sourceMapper.findSource(type, sourceFileName);
			}
		} catch (JavaModelException e) {
		}
		return source;
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
				IType type = ((ClassFile)this.openable).getType();
				ClassFileReader reader = this.locator.classFileReader(type);
				if (reader != null) {
					this.sourceFileName = sourceMapper.findSourceFileName(type, reader);
				}
			}
		} catch (JavaModelException e) {
		}
		return this.sourceFileName;
	}	
	public char[] getContents() {
		char[] source = null;
		try {
			if (this.openable instanceof WorkingCopy) {
				IBuffer buffer = this.openable.getBuffer();
				if (buffer == null) return null;
				source = buffer.getCharacters();
			} else if (this.openable instanceof CompilationUnit) {
				source = Util.getResourceContentsAsCharArray((IFile)this.resource);
			} else if (this.openable instanceof ClassFile) {
				source = findClassFileSource();
			}
		} catch (JavaModelException e) {
		}
		if (source == null) return CharOperation.NO_CHAR;
		return source;
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
			CompilationUnit cu = (CompilationUnit)this.openable;
			return cu.getType(new String(mainTypeName)).getFullyQualifiedName().toCharArray();
		} else if (this.openable instanceof ClassFile) {
			String sourceFileName = getSourceFileName();
			if (sourceFileName == NO_SOURCE_FILE_NAME) {
				try {
					return ((ClassFile)this.openable).getType().getFullyQualifiedName('.').toCharArray();
				} catch (JavaModelException e) {
					return null;
				}
			}
			String simpleName = sourceFileName.substring(0, sourceFileName.length()-5); // length-".java".length()
			String pkgName = this.openable.getParent().getElementName();
			if (pkgName.length() == 0) {
				return simpleName.toCharArray();
			} else {
				return (pkgName + '.' + simpleName).toCharArray();
			}
		} else {
			return null;
		}
	}
	public int hashCode() {
		if (this.compoundName == null) return super.hashCode();
		int hashCode = 0;
		for (int i = 0, length = this.compoundName.length; i < length; i++) {
			hashCode += CharOperation.hashCode(this.compoundName[i]);
		}
		return hashCode;
	}

	public char[] getMainTypeName() {
		return null; // cannot know the main type name without opening .java or .class file
		                  // see http://bugs.eclipse.org/bugs/show_bug.cgi?id=32182
	}
	public char[][] getPackageName() {
		int length;
		if ((length = this.compoundName.length) > 1) {
			return CharOperation.subarray(this.compoundName, 0, length-1);
		} else {
			return CharOperation.NO_CHAR_CHAR;
		}
	}
	/**
	 * Locate declaration in the current class file. This class file is always in a jar.
	 */
	public void locateMatchesInClassFile() throws CoreException {
		org.eclipse.jdt.internal.core.ClassFile classFile = (org.eclipse.jdt.internal.core.ClassFile)this.openable;
		IBinaryType info = this.locator.getBinaryInfo(classFile, this.resource);
		if (info == null) 
			return; // unable to go further
	
		// check class definition
		BinaryType binaryType = (BinaryType)classFile.getType();
		if (this.locator.pattern.matchesBinary(info, null)) {
			this.locator.reportBinaryMatch(binaryType, info, IJavaSearchResultCollector.EXACT_MATCH);
		}
	
		boolean compilationAborted = false;
		if (this.locator.pattern.needsResolve) {
			// resolve
			BinaryTypeBinding binding = null;
			try {
				binding = this.locator.cacheBinaryType(binaryType);
				if (binding != null) {
					// filter out element not in hierarchy scope
					if (this.locator.hierarchyResolver != null 
							&& !this.locator.hierarchyResolver.subOrSuperOfFocus(binding)) {
						return;
					}
		
					// check methods
					MethodBinding[] methods = binding.methods();
					for (int i = 0; i < methods.length; i++) {
						MethodBinding method = methods[i];
						int level = this.locator.pattern.matchLevel(method);
						switch (level) {
							case SearchPattern.IMPOSSIBLE_MATCH:
							case SearchPattern.INACCURATE_MATCH:
								break;
							default:
								IMethod methodHandle = 
									binaryType.getMethod(
										new String(method.isConstructor() ? binding.compoundName[binding.compoundName.length-1] : method.selector),
										Signature.getParameterTypes(new String(method.signature()).replace('/', '.'))
									);
								this.locator.reportBinaryMatch(
									methodHandle, 
									info, 
									level == SearchPattern.ACCURATE_MATCH ? 
										IJavaSearchResultCollector.EXACT_MATCH : 
										IJavaSearchResultCollector.POTENTIAL_MATCH);
						}
					}
			
					// check fields
					FieldBinding[] fields = binding.fields();
					for (int i = 0; i < fields.length; i++) {
						FieldBinding field = fields[i];
						int level = this.locator.pattern.matchLevel(field);
						switch (level) {
							case SearchPattern.IMPOSSIBLE_MATCH:
							case SearchPattern.INACCURATE_MATCH:
								break;
							default:
								IField fieldHandle = binaryType.getField(new String(field.name));
								this.locator.reportBinaryMatch(
									fieldHandle, 
									info, 
									level == SearchPattern.ACCURATE_MATCH ? 
										IJavaSearchResultCollector.EXACT_MATCH : 
										IJavaSearchResultCollector.POTENTIAL_MATCH);
						}
					}
				}
			} catch (AbortCompilation e) {
				binding = null;
			}
	
			// no need to check binary info if resolve was successful
			compilationAborted = binding == null;
			if (!compilationAborted) return;
		}
	
		// if compilation was aborted it is a problem with the class path: 
		// report as a potential match if binary info matches the pattern
		int accuracy = compilationAborted ? IJavaSearchResultCollector.POTENTIAL_MATCH : IJavaSearchResultCollector.EXACT_MATCH;
		
		// check methods
		IBinaryMethod[] methods = info.getMethods();
		int length = methods == null ? 0 : methods.length;
		for (int i = 0; i < length; i++) {
			IBinaryMethod method = methods[i];
			if (this.locator.pattern.matchesBinary(method, info)) {
				IMethod methodHandle = 
					binaryType.getMethod(
						new String(method.isConstructor() ? info.getName() : method.getSelector()),
						Signature.getParameterTypes(new String(method.getMethodDescriptor()).replace('/', '.'))
					);
				this.locator.reportBinaryMatch(methodHandle, info, accuracy);
			}
		}
	
		// check fields
		IBinaryField[] fields = info.getFields();
		length = fields == null ? 0 : fields.length;
		for (int i = 0; i < length; i++) {
			IBinaryField field = fields[i];
			if (this.locator.pattern.matchesBinary(field, info)) {
				IField fieldHandle = binaryType.getField(new String(field.getName()));
				this.locator.reportBinaryMatch(fieldHandle, info, accuracy);
			}
		}
	}
	public String toString() {
		return this.openable == null ? "Fake PotentialMatch" : this.openable.toString(); //$NON-NLS-1$
	}

	public char[] getFileName() {
		return this.openable.getPath().toString().toCharArray();
	}

}
