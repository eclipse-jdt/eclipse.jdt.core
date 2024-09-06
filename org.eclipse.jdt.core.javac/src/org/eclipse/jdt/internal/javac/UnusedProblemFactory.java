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

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.lang.model.element.ElementKind;
import javax.tools.JavaFileObject;

import org.eclipse.jdt.core.compiler.CategorizedProblem;
import org.eclipse.jdt.core.compiler.IProblem;
import org.eclipse.jdt.internal.compiler.IProblemFactory;
import org.eclipse.jdt.internal.compiler.impl.CompilerOptions;
import org.eclipse.jdt.internal.compiler.problem.ProblemReporter;
import org.eclipse.jdt.internal.compiler.problem.ProblemSeverities;

import com.sun.source.tree.CompilationUnitTree;
import com.sun.source.tree.Tree;
import com.sun.tools.javac.code.Symbol.VarSymbol;
import com.sun.tools.javac.tree.JCTree.JCClassDecl;
import com.sun.tools.javac.tree.JCTree.JCImport;
import com.sun.tools.javac.tree.JCTree.JCMethodDecl;
import com.sun.tools.javac.tree.JCTree.JCVariableDecl;

public class UnusedProblemFactory {
	private Map<JavaFileObject, Map<String, CategorizedProblem>> filesToUnusedImports = new HashMap<>();
	private IProblemFactory problemFactory;
	private CompilerOptions compilerOptions;

	public UnusedProblemFactory(IProblemFactory problemFactory, CompilerOptions compilerOptions) {
		this.problemFactory = problemFactory;
		this.compilerOptions = compilerOptions;
	}

	public UnusedProblemFactory(IProblemFactory problemFactory, Map<String, String> compilerOptions) {
		this.problemFactory = problemFactory;
		this.compilerOptions = new CompilerOptions(compilerOptions);
	}

	public List<CategorizedProblem> addUnusedImports(CompilationUnitTree unit, Map<String, JCImport> unusedImports) {
		int severity = this.toSeverity(IProblem.UnusedImport);
		if (severity == ProblemSeverities.Ignore || severity == ProblemSeverities.Optional) {
			return null;
		}

		Map<String, CategorizedProblem> unusedWarning = new LinkedHashMap<>();
		final char[] fileName = unit.getSourceFile().getName().toCharArray();
		for (Entry<String, JCImport> unusedImport : unusedImports.entrySet()) {
			String importName = unusedImport.getKey();
			JCImport importNode = unusedImport.getValue();
			int pos = importNode.qualid.getStartPosition();
			int endPos = pos + importName.length() - 1;
			if (importName.endsWith(".*")) {
				/**
				 * For the unused star imports (e.g., java.util.*), display the
				 * diagnostic on the java.util part instead of java.util.* to
				 * be compatible with 'remove unused import' quickfix in JDT.
				 */
				importName = importName.substring(0, importName.length() - 2);
				endPos = endPos - 2;
			}
			int line = (int) unit.getLineMap().getLineNumber(pos);
			int column = (int) unit.getLineMap().getColumnNumber(pos);
			String[] arguments = new String[] { importName };
			CategorizedProblem problem = problemFactory.createProblem(fileName,
						IProblem.UnusedImport,
						arguments,
						arguments,
						severity, pos, endPos, line, column);
			unusedWarning.put(importName, problem);
		}

		JavaFileObject file = unit.getSourceFile();
		Map<String, CategorizedProblem> newUnusedImports = mergeUnusedImports(filesToUnusedImports.get(file), unusedWarning);
		filesToUnusedImports.put(file, newUnusedImports);
		return new ArrayList<>(newUnusedImports.values());
	}

	public List<CategorizedProblem> addUnusedPrivateMembers(CompilationUnitTree unit, List<Tree> unusedPrivateDecls) {
		if (unit == null) {
			return Collections.emptyList();
		}

		final char[] fileName = unit.getSourceFile().getName().toCharArray();
		List<CategorizedProblem> problems = new ArrayList<>();
		for (Tree decl : unusedPrivateDecls) {
			CategorizedProblem problem = null;
			if (decl instanceof JCClassDecl classDecl) {
				int severity = this.toSeverity(IProblem.UnusedPrivateType);
				if (severity == ProblemSeverities.Ignore || severity == ProblemSeverities.Optional) {
					continue;
				}

				int pos = classDecl.getPreferredPosition();
				int startPos = pos;
				int endPos = pos;
				String shortName = classDecl.name.toString();
				JavaFileObject fileObject = unit.getSourceFile();
				try {
					CharSequence charContent = fileObject.getCharContent(true);
					String content = charContent.toString();
					if (content != null && content.length() > pos) {
						String temp = content.substring(pos);
						int index = temp.indexOf(shortName);
						if (index >= 0) {
							startPos = pos + index;
							endPos = startPos + shortName.length() - 1;
						}
					}
				} catch (IOException e) {
					// ignore
				}

				int line = (int) unit.getLineMap().getLineNumber(startPos);
				int column = (int) unit.getLineMap().getColumnNumber(startPos);
				problem = problemFactory.createProblem(fileName,
						IProblem.UnusedPrivateType, new String[] {
							shortName
						}, new String[] {
							shortName
						},
						severity, startPos, endPos, line, column);
			} else if (decl instanceof JCMethodDecl methodDecl) {
				int problemId = methodDecl.sym.isConstructor() ? IProblem.UnusedPrivateConstructor
						: IProblem.UnusedPrivateMethod;
				int severity = this.toSeverity(problemId);
				if (severity == ProblemSeverities.Ignore || severity == ProblemSeverities.Optional) {
					continue;
				}

				String selector = methodDecl.name.toString();
				String typeName = methodDecl.sym.owner.name.toString();
				String[] params = methodDecl.params.stream().map(variableDecl -> {
					return variableDecl.vartype.toString();
				}).toArray(String[]::new);
				String[] arguments = new String[] {
						typeName, selector, String.join(", ", params)
				};

				int pos = methodDecl.getPreferredPosition();
				int endPos = pos + methodDecl.name.toString().length() - 1;
				int line = (int) unit.getLineMap().getLineNumber(pos);
				int column = (int) unit.getLineMap().getColumnNumber(pos);
				problem = problemFactory.createProblem(fileName,
						problemId, arguments, arguments,
						severity, pos, endPos, line, column);
			} else if (decl instanceof JCVariableDecl variableDecl) {
				int pos = variableDecl.getPreferredPosition();
				int endPos = pos + variableDecl.name.toString().length() - 1;
				int line = (int) unit.getLineMap().getLineNumber(pos);
				int column = (int) unit.getLineMap().getColumnNumber(pos);
				int problemId = IProblem.LocalVariableIsNeverUsed;
				String name = variableDecl.name.toString();
				String[] arguments = new String[] { name };
				VarSymbol varSymbol = variableDecl.sym;
				ElementKind varKind = varSymbol == null ? null : varSymbol.getKind();
				if (varKind == ElementKind.FIELD) {
					problemId = IProblem.UnusedPrivateField;
					String typeName = varSymbol.owner.name.toString();
					arguments = new String[] {
						typeName, name
					};
				} else if (varKind == ElementKind.PARAMETER) {
					problemId = IProblem.ArgumentIsNeverUsed;
				} else if (varKind == ElementKind.EXCEPTION_PARAMETER) {
					problemId = IProblem.ExceptionParameterIsNeverUsed;
				}

				int severity = this.toSeverity(problemId);
				if (severity == ProblemSeverities.Ignore || severity == ProblemSeverities.Optional) {
					continue;
				}

				problem = problemFactory.createProblem(fileName,
						problemId, arguments, arguments,
						severity, pos, endPos, line, column);
			}

			problems.add(problem);
		}

		return problems;
	}

	// Merge the entries that exist in both maps
	private Map<String, CategorizedProblem> mergeUnusedImports(Map<String, CategorizedProblem> map1, Map<String, CategorizedProblem> map2) {
		if (map1 == null) {
			return map2;
		} else if (map2 == null) {
			return map2;
		}

		Map<String, CategorizedProblem> mergedMap = new LinkedHashMap<>();
		for (Entry<String, CategorizedProblem> entry : map1.entrySet()) {
			if (map2.containsKey(entry.getKey())) {
				mergedMap.put(entry.getKey(), entry.getValue());
			}
		}

		return mergedMap;
	}

	private int toSeverity(int jdtProblemId) {
		int irritant = ProblemReporter.getIrritant(jdtProblemId);
		if (irritant != 0) {
			int res = this.compilerOptions.getSeverity(irritant);
			res &= ~ProblemSeverities.Optional; // reject optional flag at this stage
			return res;
		}

		return ProblemSeverities.Warning;
	}
}
