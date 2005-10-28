/*******************************************************************************
 * Copyright (c) 2000, 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jdt.internal.compiler.ast;

import java.util.Arrays;
import java.util.Comparator;

import org.eclipse.jdt.core.compiler.CharOperation;
import org.eclipse.jdt.core.compiler.IProblem;
import org.eclipse.jdt.internal.compiler.ASTVisitor;
import org.eclipse.jdt.internal.compiler.ClassFile;
import org.eclipse.jdt.internal.compiler.CompilationResult;
import org.eclipse.jdt.internal.compiler.impl.ReferenceContext;
import org.eclipse.jdt.internal.compiler.lookup.CompilationUnitScope;
import org.eclipse.jdt.internal.compiler.lookup.ImportBinding;
import org.eclipse.jdt.internal.compiler.lookup.LocalTypeBinding;
import org.eclipse.jdt.internal.compiler.lookup.TypeConstants;
import org.eclipse.jdt.internal.compiler.parser.NLSTag;
import org.eclipse.jdt.internal.compiler.problem.AbortCompilationUnit;
import org.eclipse.jdt.internal.compiler.problem.AbortMethod;
import org.eclipse.jdt.internal.compiler.problem.AbortType;
import org.eclipse.jdt.internal.compiler.problem.ProblemReporter;
import org.eclipse.jdt.internal.compiler.problem.ProblemSeverities;

public class CompilationUnitDeclaration
	extends ASTNode
	implements ProblemSeverities, ReferenceContext {
	
	private static final Comparator STRING_LITERAL_COMPARATOR = new Comparator() {
		public int compare(Object o1, Object o2) {
			StringLiteral literal1 = (StringLiteral) o1;
			StringLiteral literal2 = (StringLiteral) o2;
			return literal1.sourceStart - literal2.sourceStart;
		}
	};
	private static final int STRING_LITERALS_INCREMENT = 10;

	public ImportReference currentPackage;
	public ImportReference[] imports;
	public TypeDeclaration[] types;
	public int[][] comments;

	public boolean ignoreFurtherInvestigation = false;	// once pointless to investigate due to errors
	public boolean ignoreMethodBodies = false;
	public CompilationUnitScope scope;
	public ProblemReporter problemReporter;
	public CompilationResult compilationResult;

	public LocalTypeBinding[] localTypes;
	public int localTypeCount = 0;
	
	public boolean isPropagatingInnerClassEmulation;

	public Javadoc javadoc; // 1.5 addition for package-info.java
	
	public NLSTag[] nlsTags;
	private StringLiteral[] stringLiterals;
	private int stringLiteralsPtr;
	
	public CompilationUnitDeclaration(
		ProblemReporter problemReporter,
		CompilationResult compilationResult,
		int sourceLength) {

		this.problemReporter = problemReporter;
		this.compilationResult = compilationResult;

		//by definition of a compilation unit....
		sourceStart = 0;
		sourceEnd = sourceLength - 1;
	}

	/*
	 *	We cause the compilation task to abort to a given extent.
	 */
	public void abort(int abortLevel, IProblem problem) {

		switch (abortLevel) {
			case AbortType :
				throw new AbortType(this.compilationResult, problem);
			case AbortMethod :
				throw new AbortMethod(this.compilationResult, problem);
			default :
				throw new AbortCompilationUnit(this.compilationResult, problem);
		}
	}

	/*
	 * Dispatch code analysis AND request saturation of inner emulation
	 */
	public void analyseCode() {

		if (ignoreFurtherInvestigation)
			return;
		try {
			if (types != null) {
				for (int i = 0, count = types.length; i < count; i++) {
					types[i].analyseCode(scope);
				}
			}
			// request inner emulation propagation
			propagateInnerEmulationForAllLocalTypes();
		} catch (AbortCompilationUnit e) {
			this.ignoreFurtherInvestigation = true;
			return;
		}
	}

	/*
	 * When unit result is about to be accepted, removed back pointers
	 * to compiler structures.
	 */
	public void cleanUp() {
		if (this.types != null) {
			for (int i = 0, max = this.types.length; i < max; i++) {
				cleanUp(this.types[i]);
			}
			for (int i = 0, max = this.localTypeCount; i < max; i++) {
			    LocalTypeBinding localType = localTypes[i];
				// null out the type's scope backpointers
				localType.scope = null; // local members are already in the list
				localType.enclosingCase = null;
			}
		}
		ClassFile[] classFiles = compilationResult.getClassFiles();
		for (int i = 0, max = classFiles.length; i < max; i++) {
			// clear the classFile back pointer to the bindings
			ClassFile classFile = classFiles[i];
			// null out the classfile backpointer to a type binding
			classFile.referenceBinding = null;
			classFile.codeStream = null; // codeStream holds onto ast and scopes
			classFile.innerClassesBindings = null;
		}
	}
	private void cleanUp(TypeDeclaration type) {
		if (type.memberTypes != null) {
			for (int i = 0, max = type.memberTypes.length; i < max; i++){
				cleanUp(type.memberTypes[i]);
			}
		}
		if (type.binding != null) {
			// null out the type's scope backpointers
			type.binding.scope = null;
		}
	}

	public void checkUnusedImports(){
		
		if (this.scope.imports != null){
			for (int i = 0, max = this.scope.imports.length; i < max; i++){
				ImportBinding importBinding = this.scope.imports[i];
				ImportReference importReference = importBinding.reference;
				if (importReference != null && !importReference.used){
					scope.problemReporter().unusedImport(importReference);
				}
			}
		}
	}
	
	public CompilationResult compilationResult() {
		return this.compilationResult;
	}

	/*
	 * Finds the matching type amoung this compilation unit types.
	 * Returns null if no type with this name is found.
	 * The type name is a compound name
	 * eg. if we're looking for X.A.B then a type name would be {X, A, B}
	 */
	public TypeDeclaration declarationOfType(char[][] typeName) {

		for (int i = 0; i < this.types.length; i++) {
			TypeDeclaration typeDecl = this.types[i].declarationOfType(typeName);
			if (typeDecl != null) {
				return typeDecl;
			}
		}
		return null;
	}

	/**
	 * Bytecode generation
	 */
	public void generateCode() {

		if (ignoreFurtherInvestigation) {
			if (types != null) {
				for (int i = 0, count = types.length; i < count; i++) {
					types[i].ignoreFurtherInvestigation = true;
					// propagate the flag to request problem type creation
					types[i].generateCode(scope);
				}
			}
			return;
		}
		if (this.isPackageInfo() && this.types != null && this.currentPackage.annotations != null) {
			types[0].annotations = this.currentPackage.annotations;
		}
		try {
			if (types != null) {
				for (int i = 0, count = types.length; i < count; i++)
					types[i].generateCode(scope);
			}
		} catch (AbortCompilationUnit e) {
			// ignore
		}
	}

	public char[] getFileName() {

		return compilationResult.getFileName();
	}

	public char[] getMainTypeName() {

		if (compilationResult.compilationUnit == null) {
			char[] fileName = compilationResult.getFileName();

			int start = CharOperation.lastIndexOf('/', fileName) + 1;
			if (start == 0 || start < CharOperation.lastIndexOf('\\', fileName))
				start = CharOperation.lastIndexOf('\\', fileName) + 1;

			int end = CharOperation.lastIndexOf('.', fileName);
			if (end == -1)
				end = fileName.length;

			return CharOperation.subarray(fileName, start, end);
		} else {
			return compilationResult.compilationUnit.getMainTypeName();
		}
	}

	public boolean isEmpty() {

		return (currentPackage == null) && (imports == null) && (types == null);
	}

	public boolean isPackageInfo() {
		return CharOperation.equals(this.getMainTypeName(), TypeConstants.PACKAGE_INFO_NAME)
			&& this.currentPackage != null
			&& (this.currentPackage.annotations != null || this.javadoc != null);
	}
	
	public boolean hasErrors() {
		return this.ignoreFurtherInvestigation;
	}

	public StringBuffer print(int indent, StringBuffer output) {

		if (currentPackage != null) {
			printIndent(indent, output).append("package "); //$NON-NLS-1$
			currentPackage.print(0, output, false).append(";\n"); //$NON-NLS-1$
		}
		if (imports != null)
			for (int i = 0; i < imports.length; i++) {
				printIndent(indent, output).append("import "); //$NON-NLS-1$
				imports[i].print(0, output).append(";\n"); //$NON-NLS-1$ 
			}

		if (types != null) {
			for (int i = 0; i < types.length; i++) {
				types[i].print(indent, output).append("\n"); //$NON-NLS-1$
			}
		}
		return output;
	}
	
	/*
	 * Force inner local types to update their innerclass emulation
	 */
	public void propagateInnerEmulationForAllLocalTypes() {

		isPropagatingInnerClassEmulation = true;
		for (int i = 0, max = this.localTypeCount; i < max; i++) {
				
			LocalTypeBinding localType = localTypes[i];
			// only propagate for reachable local types
			if ((localType.scope.referenceType().bits & IsReachable) != 0) {
				localType.updateInnerEmulationDependents();
			}
		}
	}

	public void recordStringLiteral(StringLiteral literal) {
		if (this.stringLiterals == null) {
			this.stringLiterals = new StringLiteral[STRING_LITERALS_INCREMENT];
			this.stringLiteralsPtr = 0;
		} else {
			int stackLength = this.stringLiterals.length;
			if (this.stringLiteralsPtr == stackLength) {
				System.arraycopy(
					this.stringLiterals,
					0,
					this.stringLiterals = new StringLiteral[stackLength + STRING_LITERALS_INCREMENT],
					0,
					stackLength);
			}
		}
		this.stringLiterals[this.stringLiteralsPtr++] = literal;		
	}

	/*
	 * Keep track of all local types, so as to update their innerclass
	 * emulation later on.
	 */
	public void record(LocalTypeBinding localType) {

		if (this.localTypeCount == 0) {
			this.localTypes = new LocalTypeBinding[5];
		} else if (this.localTypeCount == this.localTypes.length) {
			System.arraycopy(this.localTypes, 0, (this.localTypes = new LocalTypeBinding[this.localTypeCount * 2]), 0, this.localTypeCount);
		}
		this.localTypes[this.localTypeCount++] = localType;
	}

	public void resolve() {
		int startingTypeIndex = 0;
		boolean isPackageInfo = isPackageInfo();
		if (this.types != null && isPackageInfo) {
            // resolve synthetic type declaration
			final TypeDeclaration syntheticTypeDeclaration = types[0];
			// set empty javadoc to avoid missing warning (see bug https://bugs.eclipse.org/bugs/show_bug.cgi?id=95286)
			syntheticTypeDeclaration.javadoc = new Javadoc(syntheticTypeDeclaration.declarationSourceStart, syntheticTypeDeclaration.declarationSourceStart);
			syntheticTypeDeclaration.resolve(this.scope);
			// resolve annotations if any
			if (this.currentPackage.annotations != null) {
				resolveAnnotations(syntheticTypeDeclaration.staticInitializerScope, this.currentPackage.annotations, this.scope.fPackage);
			}
			// resolve javadoc package if any
			if (this.javadoc != null) {
				this.javadoc.resolve(syntheticTypeDeclaration.staticInitializerScope);
    		}
			startingTypeIndex = 1;
		}
		if (this.currentPackage != null && this.currentPackage.annotations != null && !isPackageInfo) {
			scope.problemReporter().invalidFileNameForPackageAnnotations(this.currentPackage.annotations[0]);
		}
		try {
			if (types != null) {
				for (int i = startingTypeIndex, count = types.length; i < count; i++) {
					types[i].resolve(scope);
				}
			}
			if (!this.compilationResult.hasErrors()) checkUnusedImports();
			if (this.nlsTags != null || this.stringLiterals != null) {
				final int stringLiteralsLength = this.stringLiteralsPtr;
				final int nlsTagsLength = this.nlsTags == null ? 0 : this.nlsTags.length;
				if (stringLiteralsLength == 0) {
					if (nlsTagsLength != 0) {
						for (int i = 0; i < nlsTagsLength; i++) {
							NLSTag tag = this.nlsTags[i];
							if (tag != null) {
								scope.problemReporter().unnecessaryNLSTags(tag.start, tag.end);
							}
						}
					}
				} else if (nlsTagsLength == 0) {
					// resize string literals
					if (this.stringLiterals.length != stringLiteralsLength) {
						System.arraycopy(this.stringLiterals, 0, (stringLiterals = new StringLiteral[stringLiteralsLength]), 0, stringLiteralsLength);
					}
					Arrays.sort(this.stringLiterals, STRING_LITERAL_COMPARATOR);
					for (int i = 0; i < stringLiteralsLength; i++) {
						scope.problemReporter().nonExternalizedStringLiteral(this.stringLiterals[i]);
					}
				} else {
					// need to iterate both arrays to find non matching elements
					if (this.stringLiterals.length != stringLiteralsLength) {
						System.arraycopy(this.stringLiterals, 0, (stringLiterals = new StringLiteral[stringLiteralsLength]), 0, stringLiteralsLength);
					}
					Arrays.sort(this.stringLiterals, STRING_LITERAL_COMPARATOR);
					int indexInLine = 1;
					int lastLineNumber = -1;
					StringLiteral literal = null;
					int index = 0;
					int i = 0;
					stringLiteralsLoop: for (; i < stringLiteralsLength; i++) {
						literal = this.stringLiterals[i];
						final int literalLineNumber = literal.lineNumber;
						if (lastLineNumber != literalLineNumber) {
							indexInLine = 1;
							lastLineNumber = literalLineNumber;
						} else {
							indexInLine++;
						}
						if (index < nlsTagsLength) {
							nlsTagsLoop: for (; index < nlsTagsLength; index++) {
								NLSTag tag = this.nlsTags[index];
								if (tag == null) continue nlsTagsLoop;
								int tagLineNumber = tag.lineNumber;
								if (literalLineNumber < tagLineNumber) {
									scope.problemReporter().nonExternalizedStringLiteral(literal);
									continue stringLiteralsLoop;
								} else if (literalLineNumber == tagLineNumber) {
									if (tag.index == indexInLine) {
										this.nlsTags[index] = null;
										index++;
										continue stringLiteralsLoop;
									} else {
										nlsTagsLoop2: for (int index2 = index + 1; index2 < nlsTagsLength; index2++) {
											NLSTag tag2 = this.nlsTags[index2];
											if (tag2 == null) continue nlsTagsLoop2;
											int tagLineNumber2 = tag2.lineNumber;
											if (literalLineNumber == tagLineNumber2) {
												if (tag2.index == indexInLine) {
													this.nlsTags[index2] = null;
													continue stringLiteralsLoop;
												} else {
													continue nlsTagsLoop2;
												}
											} else {
												scope.problemReporter().nonExternalizedStringLiteral(literal);
												continue stringLiteralsLoop;
											}
										}
										scope.problemReporter().nonExternalizedStringLiteral(literal);
										continue stringLiteralsLoop;
									}
								} else {
									scope.problemReporter().unnecessaryNLSTags(tag.start, tag.end);
									continue nlsTagsLoop;
								}
							}
						}
						// all nls tags have been processed, so remaining string literals are not externalized
						break stringLiteralsLoop;
					}
					for (; i < stringLiteralsLength; i++) {
						scope.problemReporter().nonExternalizedStringLiteral(this.stringLiterals[i]);
					}
					if (index < nlsTagsLength) {
						for (; index < nlsTagsLength; index++) {
							NLSTag tag = this.nlsTags[index];
							if (tag != null) {
								scope.problemReporter().unnecessaryNLSTags(tag.start, tag.end);
							}
						}						
					}
				}
			}
		} catch (AbortCompilationUnit e) {
			this.ignoreFurtherInvestigation = true;
			return;
		}
	}

	public void tagAsHavingErrors() {
		ignoreFurtherInvestigation = true;
	}

	public void traverse(
		ASTVisitor visitor,
		CompilationUnitScope unitScope) {

		if (ignoreFurtherInvestigation)
			return;
		try {
			if (visitor.visit(this, this.scope)) {
				if (currentPackage != null) {
					currentPackage.traverse(visitor, this.scope);
				}
				if (imports != null) {
					int importLength = imports.length;
					for (int i = 0; i < importLength; i++) {
						imports[i].traverse(visitor, this.scope);
					}
				}
				if (types != null) {
					int typesLength = types.length;
					for (int i = 0; i < typesLength; i++) {
						types[i].traverse(visitor, this.scope);
					}
				}
			}
			visitor.endVisit(this, this.scope);
		} catch (AbortCompilationUnit e) {
			// ignore
		}
	}
}
