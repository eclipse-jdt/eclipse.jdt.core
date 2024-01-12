/*******************************************************************************
 * Copyright (c) 2023 Red Hat, Inc. and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *******************************************************************************/
package org.eclipse.jdt.core.dom;

import java.util.List;
import java.util.Map;

import org.eclipse.core.runtime.ILog;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.WorkingCopyOwner;
import org.eclipse.jdt.internal.compiler.batch.FileSystem.Classpath;

interface ICompilationUnitResolver {

	void resolve(String[] sourceFilePaths, String[] encodings, String[] bindingKeys, FileASTRequestor requestor,
			int apiLevel, Map<String, String> compilerOptions, List<Classpath> list, int flags,
			IProgressMonitor monitor);

	void parse(ICompilationUnit[] compilationUnits, ASTRequestor requestor, int apiLevel,
			Map<String, String> compilerOptions, int flags, IProgressMonitor monitor);

	void parse(String[] sourceFilePaths, String[] encodings, FileASTRequestor requestor, int apiLevel,
			Map<String, String> compilerOptions, int flags, IProgressMonitor monitor);

	void resolve(ICompilationUnit[] compilationUnits, String[] bindingKeys, ASTRequestor requestor, int apiLevel,
			Map<String, String> compilerOptions, IJavaProject project, WorkingCopyOwner workingCopyOwner, int flags,
			IProgressMonitor monitor);

	IBinding[] resolve(IJavaElement[] elements, int apiLevel, Map<String, String> compilerOptions, IJavaProject project,
			WorkingCopyOwner workingCopyOwner, int flags, IProgressMonitor monitor);

	CompilationUnit toCompilationUnit(org.eclipse.jdt.internal.compiler.env.ICompilationUnit sourceUnit, final boolean initialNeedsToResolveBinding, IJavaProject project, List<Classpath> classpaths, NodeSearcher nodeSearcher,
			int apiLevel, Map<String, String> compilerOptions, WorkingCopyOwner parsedUnitWorkingCopyOwner, WorkingCopyOwner typeRootWorkingCopyOwner, int flags, IProgressMonitor monitor);

	@SuppressWarnings("unchecked")
	static ICompilationUnitResolver getInstance() {
		String compilationUnitResolverClass = System.getProperty(ICompilationUnitResolver.class.getSimpleName());
		if (compilationUnitResolverClass != null) {
			try {
				Class<? extends ICompilationUnitResolver> clazz = (Class<? extends ICompilationUnitResolver>) Class.forName(compilationUnitResolverClass);
				return clazz.getDeclaredConstructor().newInstance();
			} catch (Exception e) {
				ILog.get().error("Could not instantiate ICompilationUnitResolver: " + compilationUnitResolverClass, e); //$NON-NLS-1$
			}
		}
		return CompilationUnitResolver.FACADE;
	}

}
