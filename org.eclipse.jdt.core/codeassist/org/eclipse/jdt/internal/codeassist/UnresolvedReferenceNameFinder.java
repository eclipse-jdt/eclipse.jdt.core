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
import org.eclipse.jdt.internal.compiler.util.SimpleSetOfCharArray;

public class UnresolvedReferenceNameFinder extends ASTVisitor {
	private static final int MAX_LINE_COUNT = 100;
	
	public static interface UnresolvedReferenceNameRequestor {
		public void acceptName(char[] name);
	}
	
	private UnresolvedReferenceNameRequestor requestor;
	
	private CompletionParser parser;
	private CompletionScanner completionScanner;
	
	private int parentsPtr;
	private ASTNode[] parents;
	
	private int potentialVariableNamesPtr;
	private char[][] potentialVariableNames;
	private int[] potentialVariableNameStarts;
	
	private SimpleSetOfCharArray acceptedNames = new SimpleSetOfCharArray();
	
	public UnresolvedReferenceNameFinder(CompletionEngine completionEngine) {
		this.parser = completionEngine.parser;
		this.completionScanner = (CompletionScanner) parser.scanner;
	} 
	
	private void acceptName(char[] name) {
		// the null check is added to fix bug https://bugs.eclipse.org/bugs/show_bug.cgi?id=166570
		if (name == null) return;
		
		if (acceptedNames.includes(name)) return;
		
		this.acceptedNames.add(name);
		
		// accept result
		this.requestor.acceptName(name);
	}
	
	public void find(char[] startWith, Initializer initializer, ClassScope scope, int from, UnresolvedReferenceNameRequestor nameRequestor) {
		MethodDeclaration fakeMethod = this.find(startWith, scope, from, initializer.bodyEnd, nameRequestor);
		if (fakeMethod != null) fakeMethod.traverse(this, scope);
	}
	
	public void find(char[] startWith, AbstractMethodDeclaration methodDeclaration, int from, UnresolvedReferenceNameRequestor nameRequestor) {
		MethodDeclaration fakeMethod = this.find(startWith, methodDeclaration.scope, from, methodDeclaration.bodyEnd, nameRequestor);
		if (fakeMethod != null) fakeMethod.traverse(this, methodDeclaration.scope.classScope());
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
		
		int blockEnd = this.completionScanner.startPosition - 1;
		
		int maxEnd =
			this.completionScanner.getLineEnd(
					this.completionScanner.getLineNumber(from) + MAX_LINE_COUNT);
		
		int end;
		if (maxEnd < 0) {
			end = blockEnd;
		} else {
			end = maxEnd < blockEnd ? maxEnd : blockEnd;
		}
		
		this.completionScanner.startRecordingIdentifiers();
		
		MethodDeclaration fakeMethod = this.parser.parseStatementsAfterCompletion(
				from,
				end,
				s.compilationUnitScope().referenceContext);
		
		this.completionScanner.stopRecordingIdentifiers();
		
		if(!this.initPotentialNamesTables()) return null;
		
		this.parentsPtr = -1;
		this.parents = new ASTNode[10];
		
		return fakeMethod;
	}
	
	private boolean initPotentialNamesTables() {
		char[][] pvns = this.completionScanner.potentialVariableNames;
		int[] pvnss = this.completionScanner.potentialVariableNameStarts;
		int pvnsPtr = this.completionScanner.potentialVariableNamesPtr;
		
		if (pvnsPtr < 0) return false; // there is no potential names
		
		// remove null
		int j = -1;
		for (int i = 0; i <= pvnsPtr; i++) {
			if (pvns[i] != null) {
				char[] temp = pvns[i];
				pvns[i] = null;
				pvns[++j] = temp;
				pvnss[j] = pvnss[i];
				
			}
		}
		pvnsPtr = j;
		
		if (pvnsPtr < 0) return false; // there is no potential names
		
		if (pvnsPtr > 0) {
			// sort by position
			quickSort(pvnss, pvns, 0, pvnsPtr);
			
			// remove double
			j = 0;
			for (int i = 1; i <= pvnsPtr; i++) {
				if (pvnss[i] != pvnss[j]) {
					char[] temp = pvns[i];
					pvns[i] = null;
					pvns[++j] = temp;
					pvnss[j] = pvnss[i];
				} else {
					pvns[i] = null;
				}
			}
			
			pvnsPtr = j;
		}
		
		this.potentialVariableNames = pvns;
		this.potentialVariableNameStarts = pvnss;
		this.potentialVariableNamesPtr = pvnsPtr;
		
		return true;
	}
	
	private static void quickSort(int[] list1, char[][] list2, int left, int right) {
		int original_left= left;
		int original_right= right;
		int mid= list1[(left + right) / 2];
		do {
			while (list1[left] < mid) {
				left++;
			}
			while (mid < list1[right]) {
				right--;
			}
			if (left <= right) {
				int tmp1= list1[left];
				list1[left]= list1[right];
				list1[right]= tmp1;
				
				char[] tmp2= list2[left];
				list2[left]= list2[right];
				list2[right]= tmp2;
				
				left++;
				right--;
			}
		} while (left <= right);
		if (original_left < right) {
			quickSort(list1, list2, original_left, right);
		}
		if (left < original_right) {
			quickSort(list1, list2, left, original_right);
		}
	}
	
	private void popParent() {
		this.parentsPtr--;
	}
	private void pushParent(ASTNode parent) {
		int length = this.parents.length;
		if (this.parentsPtr >= length - 1) {
			System.arraycopy(this.parents, 0, this.parents = new ASTNode[length * 2], 0, length);
		}
		this.parents[++this.parentsPtr] = parent;
	}
	
	private ASTNode getEnclosingDeclaration() {
		int i = this.parentsPtr;
		while (i > -1) {
			ASTNode parent = parents[i];
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
	
	private int indexOfFisrtNameAfter(int position) {
		int left = 0;
		int right = this.potentialVariableNamesPtr;
		
		next : while (true) {
			if (right < left) return -1;
			
			int mid = (left + right) / 2;
			int midPosition = this.potentialVariableNameStarts[mid];
			if (midPosition < 0) {
				int nextMid = indexOfNextName(mid);
				if (nextMid < 0 || right < nextMid) { // no next index or next index is after 'right'
					right = mid - 1;
					continue next;
				}
				mid = nextMid;
				midPosition = this.potentialVariableNameStarts[nextMid];
				
				if (mid == right) { // mid and right are at the same index, we must move 'left'
					int leftPosition = this.potentialVariableNameStarts[left];
					if (leftPosition < 0 || leftPosition < position) { // 'left' is empty or 'left' is before the position
						int nextLeft = indexOfNextName(left);
						if (nextLeft < 0) return - 1;
						
						left = nextLeft;
						continue next;
					}
					
					return left;
				}
			}
			
			if (left != right) {
				if (midPosition < position) {
					left = mid + 1;
				} else {
					right = mid;
				}
			} else {
				if (midPosition < position) {
					return -1;
				}
				return mid;
			}
		}
	}
	
	private int indexOfNextName(int index) {
		int nextIndex = index + 1;
		while (nextIndex <= this.potentialVariableNamesPtr &&
				this.potentialVariableNames[nextIndex] == null) {
			int jumpIndex = -this.potentialVariableNameStarts[nextIndex];
			if (jumpIndex > 0) {
				nextIndex = jumpIndex;
			} else {
				nextIndex++;
			}
		}
		
		if (this.potentialVariableNamesPtr < nextIndex) {
			this.potentialVariableNamesPtr = index;
			return -1;
		}
		if (index + 1 < nextIndex) {
			this.potentialVariableNameStarts[index + 1] = -nextIndex;
		}
		return nextIndex;
	}
	
	private void removeNameAt(int index) {
		this.potentialVariableNames[index] = null;
		int nextIndex = indexOfNextName(index);
		if (nextIndex != -1) {
			this.potentialVariableNameStarts[index] = -nextIndex;
		} else {
			this.potentialVariableNamesPtr = index - 1;
		}
	}
	
	private void endVisitPreserved(int start, int end) {
		int i = indexOfFisrtNameAfter(start);
		done : while (i != -1) {
			int nameStart = this.potentialVariableNameStarts[i];
			// the null check is added to fix bug https://bugs.eclipse.org/bugs/show_bug.cgi?id=166570
			if (start < nameStart && nameStart < end) {
				this.acceptName(this.potentialVariableNames[i]);
				this.removeNameAt(i);
			}
			
			if (end < nameStart) break done;
			i = indexOfNextName(i);
		}
	}

	private void endVisitRemoved(int start, int end) {
		int i = indexOfFisrtNameAfter(start);
		done : while (i != -1) {
			int nameStart = this.potentialVariableNameStarts[i];
			if (start < nameStart && nameStart < end) {
				this.removeNameAt(i);
			}
			
			if (end < nameStart) break done;
			i = indexOfNextName(i);
		}
	}
	
	private void removeLocals(Statement[] statements, int start, int end) {
		if (statements != null) {
			for (int i = 0; i < statements.length; i++) {
				if (statements[i] instanceof LocalDeclaration) {
					LocalDeclaration localDeclaration = (LocalDeclaration) statements[i];					
					int j = indexOfFisrtNameAfter(start);
					done : while (j != -1) {
						int nameStart = this.potentialVariableNameStarts[j];
						if (start <= nameStart && nameStart <= end) {
							if (CharOperation.equals(this.potentialVariableNames[j], localDeclaration.name, false)) {
								this.removeNameAt(j);
							}
						}
						
						if (end < nameStart) break done;
						j = indexOfNextName(j);
					}
				}
			}
			
		}
	}
	
	private void removeFields(TypeDeclaration typeDeclaration) {
		int start = typeDeclaration.declarationSourceStart;
		int end = typeDeclaration.declarationSourceEnd;
		
		FieldDeclaration[] fieldDeclarations = typeDeclaration.fields;
		if (fieldDeclarations != null) {
			for (int i = 0; i < fieldDeclarations.length; i++) {
				int j = indexOfFisrtNameAfter(start);
				done : while (j != -1) {
					int nameStart = this.potentialVariableNameStarts[j];
					if (start <= nameStart && nameStart <= end) {
						if (CharOperation.equals(this.potentialVariableNames[j], fieldDeclarations[i].name, false)) {
							this.removeNameAt(j);
						}
					}
					
					if (end < nameStart) break done;
					j = indexOfNextName(j);
				}
			}
		}
	}
}
