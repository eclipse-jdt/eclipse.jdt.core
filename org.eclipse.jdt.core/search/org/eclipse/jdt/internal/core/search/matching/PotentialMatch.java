package org.eclipse.jdt.internal.core.search.matching;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
import org.eclipse.core.resources.*;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.core.*;
import org.eclipse.jdt.core.search.IJavaSearchResultCollector;
import org.eclipse.jdt.internal.compiler.*;
import org.eclipse.jdt.internal.compiler.ast.CompilationUnitDeclaration;
import org.eclipse.jdt.internal.compiler.env.ICompilationUnit;
import org.eclipse.jdt.internal.compiler.env.*;
import org.eclipse.jdt.internal.compiler.lookup.*;
import org.eclipse.jdt.internal.compiler.problem.AbortCompilation;
import org.eclipse.jdt.internal.compiler.util.CharOperation;
import org.eclipse.jdt.internal.core.*;

import java.io.*;
import java.util.zip.ZipFile;

public class PotentialMatch {
	private MatchLocator locator;
	public IResource resource;
	public Openable openable;
	private CompilationUnitDeclaration parsedUnit;
	private MatchSet matchSet;
public PotentialMatch(MatchLocator locator, IResource resource, Openable openable) {
	this.locator = locator;
	this.resource = resource;
	this.openable = openable;
	if (openable instanceof CompilationUnit) {
		this.buildTypeBindings();
	}
}
private void buildTypeBindings() {
	// get main type name
	String fileName = this.resource.getFullPath().lastSegment();
	// remove extension ".java"
	final char[] mainTypeName = fileName.substring(0, fileName.length()-5).toCharArray(); 

	// get qualified name
	CompilationUnit cu = (CompilationUnit)this.openable;
	char[] qualifiedName = cu.getType(new String(mainTypeName)).getFullyQualifiedName().toCharArray();

	// create match set	
	this.matchSet = new MatchSet(this.locator);
	this.locator.parser.matchSet = this.matchSet;

	this.parsedUnit = (CompilationUnitDeclaration)this.locator.parsedUnits.get(qualifiedName);
	if (this.parsedUnit == null) {
		// get source
		final char[] source = this.getSource();

		// source unit
		ICompilationUnit sourceUnit = new ICompilationUnit() {
			public char[] getContents() {
				return source;
			}
			public char[] getFileName() {
				return PotentialMatch.this.resource.getName().toCharArray();
			}
			public char[] getMainTypeName() {
				return mainTypeName;
			}
		};
		
		// diet parse
		CompilationResult compilationResult = new CompilationResult(sourceUnit, 0, 0);  
		this.parsedUnit = this.locator.parser.dietParse(sourceUnit, compilationResult);

		// initial type binding creation
		this.locator.lookupEnvironment.buildTypeBindings(this.parsedUnit);
	} else {
		// free memory
		this.locator.parsedUnits.put(qualifiedName, null);
	}
}
public static char[] getContents(IFile file) {
	BufferedInputStream input = null;
	try {
		input = new BufferedInputStream(file.getContents(true));
		StringBuffer buffer= new StringBuffer();
		int nextChar = input.read();
		while (nextChar != -1) {
			buffer.append( (char)nextChar );
			nextChar = input.read();
		}
		int length = buffer.length();
		char[] result = new char[length];
		buffer.getChars(0, length, result, 0);
		return result;
	} catch (IOException e) {
		return null;
	} catch (CoreException e) {
		return null;
	} finally {
		if (input != null) {
			try {
				input.close();
			} catch (IOException e) {
				// nothing can be done if the file cannot be closed
			}
		}
	}
}
public char[] getSource() {
	return getContents((IFile)this.resource);
}
public void locateMatches() throws CoreException {
	if (this.openable instanceof CompilationUnit) {
		this.locateMatchesInCompilationUnit();
	} else if (this.openable instanceof org.eclipse.jdt.internal.core.ClassFile) {
		this.locateMatchesInClassFile();
	}
}
/**
 * Locate declaration in the current class file. This class file is always in a jar.
 */
private void locateMatchesInClassFile() throws CoreException, JavaModelException {
	org.eclipse.jdt.internal.core.ClassFile classFile = (org.eclipse.jdt.internal.core.ClassFile)this.openable;
	BinaryType binaryType = (BinaryType)classFile.getType();
	IBinaryType info;
	if (classFile.isOpen()) {
		// reuse the info from the java model cache
		info = (IBinaryType)binaryType.getRawInfo();
	} else {
		// create a temporary info
		try {
			IJavaElement pkg = classFile.getParent();
			PackageFragmentRoot root = (PackageFragmentRoot)pkg.getParent();
			if (root.isArchive()) {
				// class file in a jar
				String pkgPath = pkg.getElementName().replace('.', '/');
				String classFilePath = 
					(pkgPath.length() > 0) ?
						pkgPath + "/"/*nonNLS*/ + classFile.getElementName() :
						classFile.getElementName();
				ZipFile zipFile = null;
				try {
					zipFile = ((JarPackageFragmentRoot)root).getJar();
					info = org.eclipse.jdt.internal.compiler.classfmt.ClassFileReader.read(
						zipFile,
						classFilePath);
				} finally {
					if (zipFile != null) {
						try {
							zipFile.close();
						} catch (IOException e) {
							// ignore 
						}
					}
				}
			} else {
				// class file in a directory
				String osPath = this.resource.getFullPath().toOSString();
				info = org.eclipse.jdt.internal.compiler.classfmt.ClassFileReader.read(osPath);
			}
		} catch (org.eclipse.jdt.internal.compiler.classfmt.ClassFormatException e) {
			e.printStackTrace();
			return;
		} catch (java.io.IOException e) {
			throw new JavaModelException(e, IJavaModelStatusConstants.IO_EXCEPTION);
		}
		
	}

	// check class definition
	if (this.locator.pattern.matchesBinary(info, null)) {
		this.locator.reportBinaryMatch(binaryType, info, IJavaSearchResultCollector.EXACT_MATCH);
	}

	boolean compilationAborted = false;
	if (this.locator.pattern.needsResolve) {
		// resolve
		BinaryTypeBinding binding = null;
		try {
			binding = this.locator.lookupEnvironment.cacheBinaryType(info);
			if (binding == null) { // it was already cached as a result of a previous query
				char[][] compoundName = CharOperation.splitOn('.', binaryType.getFullyQualifiedName().toCharArray());
				ReferenceBinding referenceBinding = this.locator.lookupEnvironment.getCachedType(compoundName);
				if (referenceBinding != null && (referenceBinding instanceof BinaryTypeBinding)) {
					// if the binding could be found and if it comes from a source type,
					binding = (BinaryTypeBinding)referenceBinding;
				}
			}

			// check methods
			if (binding != null) {
				MethodBinding[] methods = binding.methods();
				for (int i = 0; i < methods.length; i++) {
					MethodBinding method = methods[i];
					int level = this.locator.pattern.matchLevel(method);
					if (level != SearchPattern.IMPOSSIBLE_MATCH) {
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
			}
		
			// check fields
			if (binding != null) {
				FieldBinding[] fields = binding.fields();
				for (int i = 0; i < fields.length; i++) {
					FieldBinding field = fields[i];
					int level = this.locator.pattern.matchLevel(field);
					if (level != SearchPattern.IMPOSSIBLE_MATCH) {
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
private void locateMatchesInCompilationUnit() throws CoreException {
	if (this.parsedUnit != null) {
		this.locator.parser.matchSet = this.matchSet;
		this.locator.parser.scanner.setSourceBuffer(this.getSource());
		this.locator.parser.parseBodies(this.parsedUnit);
		// report matches that don't need resolve
		this.matchSet.cuHasBeenResolved = false;
		this.matchSet.accuracy = IJavaSearchResultCollector.EXACT_MATCH;
		this.matchSet.reportMatching(parsedUnit);
		
		// resolve if needed
		if (this.matchSet.needsResolve()) {
			if (this.parsedUnit.types != null) {
				try {
					if (this.parsedUnit.scope != null) {
						this.parsedUnit.scope.faultInTypes();
						this.parsedUnit.resolve();
					}
					// report matches that needed resolve
					this.matchSet.cuHasBeenResolved = true;
					this.matchSet.accuracy = IJavaSearchResultCollector.EXACT_MATCH;
					this.matchSet.reportMatching(this.parsedUnit);
				} catch (AbortCompilation e) {
					// could not resolve (reasons include "could not find library class") -> ignore and report the unresolved nodes
					this.matchSet.cuHasBeenResolved = false;
					this.matchSet.accuracy = IJavaSearchResultCollector.POTENTIAL_MATCH;
					this.matchSet.reportMatching(this.parsedUnit);
				}
			}
		}
	}
}
/**
 * Free memory.
 */
public void reset() {
	this.parsedUnit = null;
	this.matchSet = null;
}
public String toString() {
	return this.openable.toString();
}
}
