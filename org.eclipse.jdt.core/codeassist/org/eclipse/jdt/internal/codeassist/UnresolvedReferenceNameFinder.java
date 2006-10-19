/*******************************************************************************
 * Copyright (c) 2006 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jdt.internal.codeassist;

import org.eclipse.jdt.core.compiler.CharOperation;
import org.eclipse.jdt.internal.codeassist.complete.CompletionParser;
import org.eclipse.jdt.internal.codeassist.complete.CompletionScanner;
import org.eclipse.jdt.internal.compiler.ASTVisitor;
import org.eclipse.jdt.internal.compiler.ast.ASTNode;
import org.eclipse.jdt.internal.compiler.ast.AbstractMethodDeclaration;
import org.eclipse.jdt.internal.compiler.ast.Argument;
import org.eclipse.jdt.internal.compiler.ast.Block;
import org.eclipse.jdt.internal.compiler.ast.ConstructorDeclaration;
import org.eclipse.jdt.internal.compiler.ast.FieldDeclaration;
import org.eclipse.jdt.internal.compiler.ast.Initializer;
import org.eclipse.jdt.internal.compiler.ast.LocalDeclaration;
import org.eclipse.jdt.internal.compiler.ast.MethodDeclaration;
import org.eclipse.jdt.internal.compiler.ast.Statement;
import org.eclipse.jdt.internal.compiler.ast.TypeDeclaration;
import org.eclipse.jdt.internal.compiler.lookup.BlockScope;
import org.eclipse.jdt.internal.compiler.lookup.ClassScope;
import org.eclipse.jdt.internal.compiler.lookup.MethodScope;
import org.eclipse.jdt.internal.compiler.lookup.Scope;
import org.eclipse.jdt.internal.compiler.util.SimpleSet;

public class UnresolvedReferenceNameFinder extends ASTVisitor {
	public static interface UnresolvedReferenceNameRequestor {
		public void acceptName(char[] name);
	}
	
	private UnresolvedReferenceNameRequestor requestor;
	
	private CompletionParser parser;
	private CompletionScanner completionScanner;
	
	private int parentsPtr;
	private ASTNode[] parentsz;
	
	private int potentialVariableNamesPtr;
	private char[][] potentialVariableNames;
	private int[] potentialVariableNameStarts;
	
	private SimpleSet acceptedNames = new SimpleSet();
	
	public UnresolvedReferenceNameFinder(CompletionEngine completionEngine) {
		this.parser = completionEngine.parser;
		this.completionScanner = (CompletionScanner) parser.scanner;
	} 
	
	private void acceptName(char[] name) {
		if (acceptedNames.includes(name)) return;
		
		this.acceptedNames.add(name);
		
		// accept result
		this.requestor.acceptName(name);
	}
	
	public void find(char[] startWith, Initializer initializer, ClassScope scope, int from, UnresolvedReferenceNameRequestor nameRequestor) {
		MethodDeclaration fakeMethod = this.find(startWith, scope, from, initializer.bodyEnd, nameRequestor);
		fakeMethod.traverse(this, scope);
	}
	
	public void find(char[] startWith, AbstractMethodDeclaration methodDeclaration, int from, UnresolvedReferenceNameRequestor nameRequestor) {
		MethodDeclaration fakeMethod = this.find(startWith, methodDeclaration.scope, from, methodDeclaration.bodyEnd, nameRequestor);
		fakeMethod.traverse(this, methodDeclaration.scope.classScope());
	}
	
	private MethodDeclaration find(char[] startWith, Scope s, int from, int to, UnresolvedReferenceNameRequestor nameRequestor) {
		this.requestor = nameRequestor;
		
		// reinitialize completion scanner to be usable as a normal scanner
		this.completionScanner.cursorLocation = 0;
		
		// reinitialize completionIdentifier
		this.completionScanner.prefix = startWith;
		
		// compute location of the end of the current block
		this.completionScanner.resetTo(from + 1, to);
		this.completionScanner.jumpOverBlock();
		
		this.completionScanner.startRecordingIdentifiers();
		
		MethodDeclaration fakeMethod = this.parser.parseStatementsAfterCompletion(
				from,
				this.completionScanner.startPosition - 1,
				s.compilationUnitScope().referenceContext);
		
		this.completionScanner.stopRecordingIdentifiers();
		
		this.potentialVariableNames = this.completionScanner.potentialVariableNames;
		this.potentialVariableNameStarts = this.completionScanner.potentialVariableNameStarts;
		this.potentialVariableNamesPtr = this.completionScanner.potentialVariableNamesPtr;
		
		this.parentsPtr = -1;
		this.parentsz = new ASTNode[10];
		
		return fakeMethod;
	}
	
	private void popParent() {
		this.parentsPtr--;
	}
	private void pushParent(ASTNode parent) {
		int length = this.parentsz.length;
		if (this.parentsPtr >= length - 1) {
			System.arraycopy(this.parentsz, 0, this.parentsz = new ASTNode[length * 2], 0, length);
		}
		this.parentsz[++this.parentsPtr] = parent;
	}
	
	private ASTNode getEnclosingDeclaration() {
		int i = this.parentsPtr;
		while (i > -1) {
			ASTNode parent = parentsz[i];
			if (parent instanceof AbstractMethodDeclaration) {
				return parent;
			} else if (parent instanceof Initializer) {
				return parent;
			} else if (parent instanceof FieldDeclaration) {
				return parent;
			} else if (parent instanceof TypeDeclaration) {
				return parent;
			}
			i--;
		}
		return null;
	} 
	
	public boolean visit(Block block, BlockScope blockScope) {
		pushParent(block);
		return true;
	}
	
	public boolean visit(ConstructorDeclaration constructorDeclaration, ClassScope classScope) {
		pushParent(constructorDeclaration);
		return true;
	}
	
	public boolean visit(FieldDeclaration fieldDeclaration, MethodScope methodScope) {
		pushParent(fieldDeclaration);
		return true;
	}
	
	public boolean visit(Initializer initializer, MethodScope methodScope) {
		pushParent(initializer);
		return true;
	}
	
	public boolean visit(MethodDeclaration methodDeclaration, ClassScope classScope) {
		pushParent(methodDeclaration);
		return true;
	}
	
	public boolean visit(TypeDeclaration localTypeDeclaration, BlockScope blockScope) {
		removeFields(localTypeDeclaration);
		pushParent(localTypeDeclaration);
		return true;
	}
	
	public boolean visit(TypeDeclaration memberTypeDeclaration, ClassScope classScope) {
		removeFields(memberTypeDeclaration);
		pushParent(memberTypeDeclaration);
		return true;
	}
	
	public void endVisit(Block block, BlockScope blockScope) {
		ASTNode enclosingDeclaration = getEnclosingDeclaration();
		removeLocals(block.statements, enclosingDeclaration.sourceStart, block.sourceEnd);
		popParent();
	}
	
	public void endVisit(Argument argument, BlockScope blockScope) {
		endVisitRemoved(argument.declarationSourceStart, argument.sourceEnd);
	}
	
	public void endVisit(Argument argument, ClassScope classScope) {
		endVisitRemoved(argument.declarationSourceStart, argument.sourceEnd);
	}
	
	public void endVisit(ConstructorDeclaration constructorDeclaration, ClassScope classScope) {
		if (!constructorDeclaration.isDefaultConstructor && !constructorDeclaration.isClinit()) {
			endVisitPreserved(constructorDeclaration.bodyStart, constructorDeclaration.bodyEnd);
		}
		popParent();
	}
	
	public void endVisit(FieldDeclaration fieldDeclaration, MethodScope methodScope) {
		endVisitRemoved(fieldDeclaration.declarationSourceStart, fieldDeclaration.sourceEnd);
		endVisitPreserved(fieldDeclaration.sourceEnd, fieldDeclaration.declarationEnd);
		popParent();
	}
	
	public void endVisit(Initializer initializer, MethodScope methodScope) {
		endVisitPreserved(initializer.bodyStart, initializer.bodyEnd);
		popParent();
	}
	
	public void endVisit(LocalDeclaration localDeclaration, BlockScope blockScope) {
		endVisitRemoved(localDeclaration.declarationSourceStart, localDeclaration.sourceEnd);
	}
	
	public void endVisit(MethodDeclaration methodDeclaration, ClassScope classScope) {
		removeLocals(
				methodDeclaration.arguments,
				methodDeclaration.declarationSourceStart,
				methodDeclaration.declarationSourceEnd);
		removeLocals(
				methodDeclaration.statements,
				methodDeclaration.declarationSourceStart,
				methodDeclaration.declarationSourceEnd);
		endVisitPreserved(
				methodDeclaration.bodyStart,
				methodDeclaration.bodyEnd);
		popParent();
	}
	
	public void endVisit(TypeDeclaration typeDeclaration, BlockScope blockScope) {
		removeFields(typeDeclaration);
		endVisitRemoved(typeDeclaration.sourceStart, typeDeclaration.declarationSourceEnd);
		popParent();
	}
	
	public void endVisit(TypeDeclaration typeDeclaration, ClassScope classScope) {
		endVisitRemoved(typeDeclaration.sourceStart, typeDeclaration.declarationSourceEnd);
		popParent();
	}
	
	private void endVisitPreserved(int start, int end) {
		for (int i = 0; i <= this.potentialVariableNamesPtr; i++) {
			char[] name = this.potentialVariableNames[i];
			if (name != null) {
				int nameStart = this.potentialVariableNameStarts[i];
				if (start < nameStart && nameStart < end) {
					this.potentialVariableNames[i] = null;
					this.acceptName(name);
				}
			}
		}
	}
	
	private void endVisitRemoved(int start, int end) {
		for (int i = 0; i <= this.potentialVariableNamesPtr; i++) {
			if (this.potentialVariableNames[i] != null) {
				int nameStart = this.potentialVariableNameStarts[i];
				if (start < nameStart && nameStart < end) {
					this.potentialVariableNames[i] = null;
				}
			}
		}
	}
	
	private void removeLocals(Statement[] statements, int start, int end) {
		if (statements != null) {
			for (int i = 0; i < statements.length; i++) {
				if (statements[i] instanceof LocalDeclaration) {
					LocalDeclaration localDeclaration = (LocalDeclaration) statements[i];
					for (int j = 0; j <= this.potentialVariableNamesPtr; j++) {
						char[] name = this.potentialVariableNames[j];
						if (name != null) {
							int nameStart = this.potentialVariableNameStarts[j];
							if(start <= nameStart && nameStart <= end) {
								if (CharOperation.equals(name, localDeclaration.name, false)) {
									this.potentialVariableNames[j] = null;
								}
							}
						}
					}
				}
			}
			
		}
	}
	
	private void removeFields(TypeDeclaration typeDeclaration) {
		FieldDeclaration[] fieldDeclarations = typeDeclaration.fields;
		if (fieldDeclarations != null) {
			for (int i = 0; i < fieldDeclarations.length; i++) {
				for (int j = 0; j <= this.potentialVariableNamesPtr; j++) {
					char[] name = this.potentialVariableNames[j];
					if (name != null) {
						int nameStart = this.potentialVariableNameStarts[j];
						if(typeDeclaration.declarationSourceStart <= nameStart &&
								nameStart <= typeDeclaration.declarationSourceEnd) {
							if (CharOperation.equals(name, fieldDeclarations[i].name, false)) {
								this.potentialVariableNames[j] = null;
							}
						}
					}
				}
			}
		}
	}
}
