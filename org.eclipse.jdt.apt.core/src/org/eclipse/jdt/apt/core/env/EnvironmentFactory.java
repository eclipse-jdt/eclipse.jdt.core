/*******************************************************************************
 * Copyright (c) 2005 BEA Systems, Inc. 
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    mkaufman@bea.com - initial API and implementation
 *    
 *******************************************************************************/

package org.eclipse.jdt.apt.core.env;

import org.eclipse.jdt.apt.core.internal.env.ProcessorEnvImpl;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaProject;

import com.sun.mirror.apt.AnnotationProcessorEnvironment;

public class EnvironmentFactory {

	/**
	 *  @param compilationUnit the working copy for which the Environment object is to be created
	 *  @param javaProject the java project that the working copy is in. 
	 *  @return the created environment.
	 */

	public static AnnotationProcessorEnvironment getEnvironment(ICompilationUnit compilationUnit, IJavaProject javaProject )
	{
		return ProcessorEnvImpl.newProcessorEnvironmentForReconcile( compilationUnit, javaProject );
	}
}
