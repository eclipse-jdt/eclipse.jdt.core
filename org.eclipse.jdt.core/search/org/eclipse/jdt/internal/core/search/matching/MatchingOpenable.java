/*******************************************************************************
 * Copyright (c) 2000, 2001, 2002 International Business Machines Corp. and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v0.5 
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v05.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 ******************************************************************************/
package org.eclipse.jdt.internal.core.search.matching;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.core.*;
import org.eclipse.jdt.core.compiler.IProblem;
import org.eclipse.jdt.core.search.IJavaSearchResultCollector;
import org.eclipse.jdt.internal.compiler.CompilationResult;
import org.eclipse.jdt.internal.compiler.ast.CompilationUnitDeclaration;
import org.eclipse.jdt.internal.compiler.env.IBinaryField;
import org.eclipse.jdt.internal.compiler.env.IBinaryMethod;
import org.eclipse.jdt.internal.compiler.env.IBinaryType;
import org.eclipse.jdt.internal.compiler.lookup.*;
import org.eclipse.jdt.internal.compiler.problem.AbortCompilation;
import org.eclipse.jdt.internal.compiler.problem.AbortCompilationUnit;
import org.eclipse.jdt.internal.core.*;
 
public class MatchingOpenable {
	static final CompilationUnitDeclaration ALREADY_RESOLVED = new CompilationUnitDeclaration(null, null, 0);
	private MatchLocator locator;
	public IResource resource;
	public Openable openable;
	private CompilationUnitDeclaration parsedUnit;
	private char[] source;
	private MatchSet matchSet;

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
public static IType getTopLevelType(IType binaryType) {
	
	// ensure it is not a local or anoymous type (see bug 28752  J Search resports non-existent Java element)
	String typeName = binaryType.getElementName();
	int lastDollar = typeName.lastIndexOf('$');
	int length = typeName.length();
	if (lastDollar != -1 && lastDollar < length-1) {
		if (Character.isDigit(typeName.charAt(lastDollar+1))) {
			// local or anonymous type
			typeName = typeName.substring(0, lastDollar);
			IClassFile classFile = binaryType.getPackageFragment().getClassFile(typeName+".class"); //$NON-NLS-1$
			try {
				binaryType = classFile.getType();
			} catch (JavaModelException e) {
				// ignore as implementation of getType() cannot throw this exception
			}
		}
	}
	
	// ensure it is a top level type
	IType declaringType = binaryType.getDeclaringType();
	while (declaringType != null) {
		binaryType = declaringType;
		declaringType = binaryType.getDeclaringType();
	}
	return binaryType;
}
public void buildTypeBindings() {
	
	if (this.parsedUnit == null) {
		char[] source = this.getSource();
		if (source == null) return;
		this.buildTypeBindings(source);
	} else {
		// if a parsed unit's scope is set, its bindings have already been built
		if (this.parsedUnit.scope != null) return;
		
		this.locator.lookupEnvironment.buildTypeBindings(this.parsedUnit);
	}
}
private void buildTypeBindings(final char[] source) {
	// get qualified name
	char[] qualifiedName = this.getQualifiedName();
	if (qualifiedName == null) return;

	// create match set	
	this.matchSet = new MatchSet(this.locator);
	
	try {
		this.locator.parser.matchSet = this.matchSet;

		this.parsedUnit = (CompilationUnitDeclaration)this.locator.parsedUnits.get(qualifiedName);
		if (this.parsedUnit == null) {
			// diet parse
			this.parsedUnit = this.locator.dietParse(source);
			
			// initial type binding creation
			this.locator.lookupEnvironment.buildTypeBindings(this.parsedUnit);
		} 

		// free memory and remember that this unit as already been resolved 
		// (case of 2 matching openables on a binary type and its member type) 
		this.locator.parsedUnits.put(qualifiedName, ALREADY_RESOLVED);

	} finally {
		this.locator.parser.matchSet = null;
	}
}
public boolean equals(Object obj) {
	if (!(obj instanceof MatchingOpenable)) return false;
	return this.openable.equals(((MatchingOpenable)obj).openable);
}
private char[] getQualifiedName() {
	if (this.openable instanceof CompilationUnit) {
		// get file name
		String fileName = this.resource.getFullPath().lastSegment();
		// get main type name
		char[] mainTypeName = fileName.substring(0, fileName.length()-5).toCharArray(); 
		CompilationUnit cu = (CompilationUnit)this.openable;
		return cu.getType(new String(mainTypeName)).getFullyQualifiedName().toCharArray();
	} else {
		org.eclipse.jdt.internal.core.ClassFile classFile = (org.eclipse.jdt.internal.core.ClassFile)this.openable;
		try {
			IType type = getTopLevelType(classFile.getType());
			return type.getFullyQualifiedName().toCharArray();
		} catch (JavaModelException e) {
			return null; // nothing we can do here
		}
	}
}
public char[] getSource() {
	if (this.source != null) return source;
	try {
		if (this.openable instanceof WorkingCopy) {
			IBuffer buffer = this.openable.getBuffer();
			if (buffer == null) return null;
			this.source = buffer.getCharacters();
		} else if (this.openable instanceof CompilationUnit) {
			this.source = Util.getResourceContentsAsCharArray((IFile)this.resource);
		} else if (this.openable instanceof org.eclipse.jdt.internal.core.ClassFile) {
			org.eclipse.jdt.internal.core.ClassFile classFile = (org.eclipse.jdt.internal.core.ClassFile)this.openable;
			this.source = this.locator.findSource(classFile);
		}
	} catch (JavaModelException e) {
	}
	return this.source;
}
public int hashCode() {
	return this.openable.hashCode();
}
public boolean hasAlreadyDefinedType() {
	if (this.parsedUnit == null) return false;
	CompilationResult result = this.parsedUnit.compilationResult;
	if (result == null) return false;
	for (int i = 0; i < result.problemCount; i++) {
		IProblem problem = result.problems[i];
		if (problem.getID() == IProblem.DuplicateTypes) {
			return true;
		}
	}
	return false;
}

public void locateMatches() throws CoreException {
	char[] source = this.getSource();
	if (source == null) {
		if (this.openable instanceof org.eclipse.jdt.internal.core.ClassFile) {
			this.locateMatchesInClassFile();
		}
	} else {
		this.locateMatchesInCompilationUnit(source);
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
private void locateMatchesInCompilationUnit(char[] source) throws CoreException {
	if (this.parsedUnit == null) { // case where no binding resolution is needed
		// create match set	
		this.matchSet = new MatchSet(this.locator);
		this.locator.parser.matchSet = this.matchSet;
		
		// diet parse
		char[] qualifiedName = this.getQualifiedName();
		if (qualifiedName == null || (this.parsedUnit = (CompilationUnitDeclaration)this.locator.parsedUnits.get(qualifiedName)) == null) {
			this.parsedUnit = this.locator.dietParse(source);
		}
	}
	if (this.parsedUnit != null && this.parsedUnit != ALREADY_RESOLVED) {
		try {
			this.locator.parser.matchSet = this.matchSet;
			this.locator.parser.scanner.setSource(source);
			this.locator.parser.parseBodies(this.parsedUnit);
			// report matches that don't need resolve
			this.matchSet.cuHasBeenResolved = false;
			this.matchSet.reportMatching(parsedUnit);
			
			// resolve if needed
			if (this.matchSet.needsResolve() && this.parsedUnit.types != null) {
				if (!this.locator.compilationAborted) {
					try {
						if (this.parsedUnit.scope == null) {
							// bindings were not created (case of a FieldReferencePattern that doesn't need resolve, 
							// but we need to resolve because of a SingleNameReference being a potential match)
							MatchingOpenable[] openables = this.locator.matchingOpenables.getMatchingOpenables(this.openable.getJavaProject().getPackageFragmentRoots());
							this.locator.createAndResolveBindings(openables);
						}
						if (this.parsedUnit.scope != null) {
							this.parsedUnit.scope.faultInTypes();
							this.parsedUnit.resolve();
						}
						// report matches that needed resolve
						this.matchSet.cuHasBeenResolved = true;
						this.matchSet.reportMatching(this.parsedUnit);
					} catch (AbortCompilation e) {
						// could not resolve: report innacurate matches
						this.matchSet.cuHasBeenResolved = true;
						this.matchSet.reportMatching(this.parsedUnit);
						if (!(e instanceof AbortCompilationUnit)) {
							// problem with class path
							throw e;
						}
					}
				} else {
					// problem ocured while completing the bindings for the base classes
					// -> report innacurate matches
					this.matchSet.cuHasBeenResolved = true;
					this.matchSet.reportMatching(this.parsedUnit);
				}
			}
		} finally {
			this.locator.parser.matchSet = null;
		}
	}
}
/**
 * Free memory.
 */
public void reset() {
	this.locator.parsedUnits.removeKey(this.getQualifiedName());
	this.parsedUnit = null;
	this.matchSet = null;
}
public String toString() {
	return this.openable.toString();
}
}
