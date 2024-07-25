/*******************************************************************************
* Copyright (c) 2024 Microsoft Corporation and others.
* All rights reserved. This program and the accompanying materials
* are made available under the terms of the Eclipse Public License 2.0
* which accompanies this distribution, and is available at
* https://www.eclipse.org/legal/epl-2.0/
*
* SPDX-License-Identifier: EPL-2.0
*
* Contributors:
*     Microsoft Corporation - initial API and implementation
*******************************************************************************/

package org.eclipse.jdt.internal.javac;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.jdt.core.compiler.CategorizedProblem;

import com.sun.source.tree.ClassTree;
import com.sun.source.tree.CompilationUnitTree;
import com.sun.source.tree.IdentifierTree;
import com.sun.source.tree.ImportTree;
import com.sun.source.tree.MemberReferenceTree;
import com.sun.source.tree.MemberSelectTree;
import com.sun.source.tree.MethodTree;
import com.sun.source.tree.NewClassTree;
import com.sun.source.tree.Tree;
import com.sun.source.tree.VariableTree;
import com.sun.source.util.TreeScanner;
import com.sun.tools.javac.code.Flags;
import com.sun.tools.javac.code.Symbol;
import com.sun.tools.javac.code.Symbol.ClassSymbol;
import com.sun.tools.javac.code.Symbol.MethodSymbol;
import com.sun.tools.javac.code.Symbol.VarSymbol;
import com.sun.tools.javac.tree.JCTree.JCClassDecl;
import com.sun.tools.javac.tree.JCTree.JCFieldAccess;
import com.sun.tools.javac.tree.JCTree.JCIdent;
import com.sun.tools.javac.tree.JCTree.JCImport;
import com.sun.tools.javac.tree.JCTree.JCMemberReference;
import com.sun.tools.javac.tree.JCTree.JCMethodDecl;
import com.sun.tools.javac.tree.JCTree.JCNewClass;
import com.sun.tools.javac.tree.JCTree.JCVariableDecl;

public class UnusedTreeScanner<R, P> extends TreeScanner<R, P> {
	final Set<Tree> privateDecls = new LinkedHashSet<>();
	final Set<Symbol> usedElements = new HashSet<>();
	final Map<String, JCImport> unusedImports = new LinkedHashMap<>();
	private CompilationUnitTree unit = null;

	@Override
	public R visitCompilationUnit(CompilationUnitTree node, P p) {
		this.unit = node;
		return super.visitCompilationUnit(node, p);
	}

	@Override
	public R visitImport(ImportTree node, P p) {
		if (node instanceof JCImport jcImport) {
			String importClass = jcImport.qualid.toString();
			this.unusedImports.put(importClass, jcImport);
		}

		return super.visitImport(node, p);
	}

	@Override
	public R visitClass(ClassTree node, P p) {
		if (node instanceof JCClassDecl classDecl && this.isPrivateDeclaration(classDecl)) {
			this.privateDecls.add(classDecl);
		}

		return super.visitClass(node, p);
	}

	@Override
	public R visitIdentifier(IdentifierTree node, P p) {
		if (node instanceof JCIdent id && isPrivateSymbol(id.sym)) {
			this.usedElements.add(id.sym);
		}

		if (node instanceof JCIdent id && isMemberSymbol(id.sym)) {
			String name = id.toString();
			String ownerName = id.sym.owner.toString();
			if (!ownerName.isBlank()) {
				String starImport = ownerName + ".*";
				String usualImport = ownerName + "." + name;
				if (this.unusedImports.containsKey(starImport)) {
					this.unusedImports.remove(starImport);
				} else if (this.unusedImports.containsKey(usualImport)) {
					this.unusedImports.remove(usualImport);
				}
			}
		}

		return super.visitIdentifier(node, p);
	}

	@Override
	public R visitMemberSelect(MemberSelectTree node, P p) {
		if (node instanceof JCFieldAccess field) {
			if (isPrivateSymbol(field.sym)) {
				this.usedElements.add(field.sym);
			}
		}

		return super.visitMemberSelect(node, p);
	}

	@Override
	public R visitMethod(MethodTree node, P p) {
		boolean isPrivateMethod = this.isPrivateDeclaration(node);
		if (isPrivateMethod) {
			this.privateDecls.add(node);
		}

		return super.visitMethod(node, p);
	}

	@Override
	public R visitVariable(VariableTree node, P p) {
		boolean isPrivateVariable = this.isPrivateDeclaration(node);
		if (isPrivateVariable) {
			this.privateDecls.add(node);
		}

		return super.visitVariable(node, p);
	}

	@Override
	public R visitMemberReference(MemberReferenceTree node, P p) {
		if (node instanceof JCMemberReference member && isPrivateSymbol(member.sym)) {
			this.usedElements.add(member.sym);
		}

		return super.visitMemberReference(node, p);
	}

	@Override
	public R visitNewClass(NewClassTree node, P p) {
		if (node instanceof JCNewClass newClass) {
			Symbol targetClass = newClass.def != null ? newClass.def.sym : newClass.type.tsym;
			if (isPrivateSymbol(targetClass)) {
				this.usedElements.add(targetClass);
			}
		}

		return super.visitNewClass(node, p);
	}

	private boolean isPrivateDeclaration(Tree tree) {
		if (tree instanceof JCClassDecl classTree) {
			return (classTree.getModifiers().flags & Flags.PRIVATE) != 0;
		} else if (tree instanceof JCMethodDecl methodTree) {
			boolean isDefaultConstructor = methodTree.getParameters().isEmpty() && methodTree.getReturnType() == null;
			return (methodTree.getModifiers().flags & Flags.PRIVATE) != 0 && !isDefaultConstructor;
		} else if (tree instanceof JCVariableDecl variable) {
			Symbol owner = variable.sym == null ? null : variable.sym.owner;
			if (owner instanceof ClassSymbol) {
				return (variable.getModifiers().flags & Flags.PRIVATE) != 0;
			} else if (owner instanceof MethodSymbol) {
				return true;
			}
		}

		return false;
	}

	private boolean isPrivateSymbol(Symbol symbol) {
		if (symbol instanceof ClassSymbol
				|| symbol instanceof MethodSymbol) {
			return (symbol.flags() & Flags.PRIVATE) != 0;
		} else if (symbol instanceof VarSymbol) {
			if (symbol.owner instanceof ClassSymbol) {
				return (symbol.flags() & Flags.PRIVATE) != 0;
			} else if (symbol.owner instanceof MethodSymbol) {
				return true;
			}
		}

		return false;
	}

	private boolean isMemberSymbol(Symbol symbol) {
		if (symbol instanceof ClassSymbol
				|| symbol instanceof MethodSymbol) {
			return true;
		}

		if (symbol instanceof VarSymbol) {
			return symbol.owner instanceof ClassSymbol;
		}

		return false;
	}

	public List<CategorizedProblem> getUnusedImports(UnusedProblemFactory problemFactory) {
		return problemFactory.addUnusedImports(this.unit, this.unusedImports);
	}

	public List<CategorizedProblem> getUnusedPrivateMembers(UnusedProblemFactory problemFactory) {
		List<Tree> unusedPrivateMembers = new ArrayList<>();
		for (Tree decl : this.privateDecls) {
			if (decl instanceof JCClassDecl classDecl && !this.usedElements.contains(classDecl.sym)) {
				unusedPrivateMembers.add(decl);	
			} else if (decl instanceof JCMethodDecl methodDecl && !this.usedElements.contains(methodDecl.sym)) {
				unusedPrivateMembers.add(decl);
			} else if (decl instanceof JCVariableDecl variableDecl && !this.usedElements.contains(variableDecl.sym)) {
				unusedPrivateMembers.add(decl);
			}
		}

		return problemFactory.addUnusedPrivateMembers(unit, unusedPrivateMembers);
	}
}
