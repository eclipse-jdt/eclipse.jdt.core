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
import org.eclipse.jdt.internal.compiler.ast.TypeDeclaration;
import org.eclipse.jdt.internal.compiler.env.ICompilationUnit;
import org.eclipse.jdt.internal.compiler.env.*;
import org.eclipse.jdt.internal.compiler.lookup.*;
import org.eclipse.jdt.internal.compiler.problem.AbortCompilation;
import org.eclipse.jdt.internal.compiler.problem.ProblemIrritants;
import org.eclipse.jdt.internal.compiler.util.CharOperation;
import org.eclipse.jdt.internal.core.*;

import java.io.*;
import java.util.zip.ZipFile;

public class MatchingOpenable {
	private MatchLocator locator;
	public IResource resource;
	public Openable openable;
	private CompilationUnitDeclaration parsedUnit;
	private MatchSet matchSet;
	public boolean shouldResolve = true;
public MatchingOpenable(MatchLocator locator, IResource resource, Openable openable) {
	this.locator = locator;
	this.resource = resource;
	this.openable = openable;
}
public MatchingOpenable(
		MatchLocator locator, 
		IResource resource, 
		Openable openable,
		CompilationUnitDeclaration parsedUnit,
		MatchSet matchSet) {
	this.locator = locator;
	this.resource = resource;
	this.openable = openable;
	this.parsedUnit = parsedUnit;
	this.matchSet = matchSet;
}
public void buildTypeBindings() {
	
	// if a parsed unit exits, its bindings have already been built
	if (this.parsedUnit != null) return;
	
	if (openable instanceof CompilationUnit) {
		this.buildTypeBindings(this.getSource());
	} else if (openable instanceof org.eclipse.jdt.internal.core.ClassFile) {
		char[] source = this.locator.findSource((org.eclipse.jdt.internal.core.ClassFile)openable); 
		if (source != null) {
			this.buildTypeBindings(source);
			
			// try to use the main type's class file as the openable
			TypeDeclaration[] types = this.parsedUnit.types;
			if (types != null && types.length > 0) {
				String simpleTypeName = new String(types[0].name);
				IPackageFragment parent = (IPackageFragment)openable.getParent();
				org.eclipse.jdt.core.IClassFile mainTypeClassFile = 
					parent.getClassFile(simpleTypeName + ".class"); //$NON-NLS-1$
				if (mainTypeClassFile.exists()) {
					this.openable = (Openable)mainTypeClassFile;
				} 
			}
		}
	}
}
private void buildTypeBindings(final char[] source) {
	// get qualified name
	char[] qualifiedName;
	if (this.openable instanceof CompilationUnit) {
		// get file name
		String fileName = this.resource.getFullPath().lastSegment();
		// get main type name
		char[] mainTypeName = fileName.substring(0, fileName.length()-5).toCharArray(); 
		CompilationUnit cu = (CompilationUnit)this.openable;
		qualifiedName = cu.getType(new String(mainTypeName)).getFullyQualifiedName().toCharArray();
	} else {
		org.eclipse.jdt.internal.core.ClassFile classFile = (org.eclipse.jdt.internal.core.ClassFile)this.openable;
		try {
			qualifiedName = classFile.getType().getFullyQualifiedName().toCharArray();
		} catch (JavaModelException e) {
			return; // nothing we can do here
		}
	}

	// create match set	
	this.matchSet = new MatchSet(this.locator);
	this.locator.parser.matchSet = this.matchSet;

	this.parsedUnit = (CompilationUnitDeclaration)this.locator.parsedUnits.get(qualifiedName);
	if (this.parsedUnit == null) {
		// diet parse
		this.parsedUnit = this.locator.dietParse(source);
		
		// initial type binding creation
		this.locator.lookupEnvironment.buildTypeBindings(this.parsedUnit);
	} else {
		// free memory
		this.locator.parsedUnits.put(qualifiedName, null);
	}
}
public char[] getSource() {
	try {
		if (this.openable instanceof CompilationUnit) {
			return Util.getResourceContentsAsCharArray((IFile)this.resource);
		} else if (this.openable instanceof org.eclipse.jdt.internal.core.ClassFile) {
			org.eclipse.jdt.internal.core.ClassFile classFile = (org.eclipse.jdt.internal.core.ClassFile)this.openable;
			return classFile.getSource().toCharArray();
		} else {
			return null;
		}
	} catch (JavaModelException e) {
		return null;
	}
}
public boolean hasAlreadyDefinedType() {
	if (this.parsedUnit == null) return false;
	CompilationResult result = this.parsedUnit.compilationResult;
	if (result == null) return false;
	for (int i = 0; i < result.problemCount; i++) {
		IProblem problem = result.problems[i];
		if (problem.getID() == ProblemIrritants.DuplicateTypes) {
			return true;
		}
	}
	return false;
}

public void locateMatches() throws CoreException {
	if (this.openable instanceof CompilationUnit) {
		this.locateMatchesInCompilationUnit(this.getSource());
	} else if (this.openable instanceof org.eclipse.jdt.internal.core.ClassFile) {
		org.eclipse.jdt.internal.core.ClassFile classFile = (org.eclipse.jdt.internal.core.ClassFile)this.openable;
		String source = classFile.getSource();
		if (source != null) {
			this.locateMatchesInCompilationUnit(source.toCharArray());
		} else {
			this.locateMatchesInClassFile();
		}
	}
}
/**
 * Locate declaration in the current class file. This class file is always in a jar.
 */
private void locateMatchesInClassFile() throws CoreException, JavaModelException {
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
			}
		
			// check fields
			if (binding != null) {
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
private void locateMatchesInCompilationUnit(char[] source) throws CoreException {
	if (this.parsedUnit != null) {
		this.locator.parser.matchSet = this.matchSet;
		this.locator.parser.scanner.setSourceBuffer(source);
		this.locator.parser.parseBodies(this.parsedUnit);
		// report matches that don't need resolve
		this.matchSet.cuHasBeenResolved = false;
		this.matchSet.reportMatching(parsedUnit);
		
		// resolve if needed
		if (this.matchSet.needsResolve()) {
			if (this.parsedUnit.types != null) {
				if (this.shouldResolve) {
					try {
						if (this.parsedUnit.scope != null) {
							this.parsedUnit.scope.faultInTypes();
							this.parsedUnit.resolve();
						}
						// report matches that needed resolve
						this.matchSet.cuHasBeenResolved = true;
						this.matchSet.reportMatching(this.parsedUnit);
					} catch (AbortCompilation e) {
						// could not resolve (reasons include "could not find library class") 
						// -> ignore and report innacurate matches
						this.matchSet.cuHasBeenResolved = true;
						this.matchSet.reportMatching(this.parsedUnit);
					}
				} else {
					// problem ocured while completing the bindings for the base classes
					// -> report innacurate matches
					this.matchSet.cuHasBeenResolved = true;
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
