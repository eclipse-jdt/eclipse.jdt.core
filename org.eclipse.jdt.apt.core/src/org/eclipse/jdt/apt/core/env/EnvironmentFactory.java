/*******************************************************************************
 * Copyright (c) 2005, 2018 BEA Systems, Inc. and others
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *    mkaufman@bea.com - initial API and implementation
 *
 *******************************************************************************/

package org.eclipse.jdt.apt.core.env;

import com.sun.mirror.apt.AnnotationProcessorEnvironment;
import org.eclipse.core.resources.IFile;
import org.eclipse.jdt.apt.core.internal.env.BaseProcessorEnv;
import org.eclipse.jdt.apt.core.internal.util.TestCodeUtil;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.dom.CompilationUnit;

public class EnvironmentFactory {

	/**
	 * Return a processor environment for use outside of building or reconciling.
	 * Note that this environment does <b>NOT</b> support the Filer or Messager API,
	 * as it is to be used to perform type system navigation, not building.<p>
	 *
	 * If either getFiler() or getMessager() are called, this environment
	 * will throw an UnsupportedOperationException.
	 *
	 * @param compilationUnit the working copy for which the Environment object is to be created
	 * @param javaProject the java project that the working copy is in.
	 * @return the created environment.
	 */
	public static AnnotationProcessorEnvironment getEnvironment(ICompilationUnit compilationUnit, IJavaProject javaProject )
	{
		CompilationUnit node = BaseProcessorEnv.createAST( javaProject, compilationUnit);
       	BaseProcessorEnv env = new BaseProcessorEnv(
       			node,
       			(IFile)compilationUnit.getResource(),
       			javaProject,
       			Phase.OTHER,
       			TestCodeUtil.isTestCode(compilationUnit)
       			);
       	return env;
	}
}
