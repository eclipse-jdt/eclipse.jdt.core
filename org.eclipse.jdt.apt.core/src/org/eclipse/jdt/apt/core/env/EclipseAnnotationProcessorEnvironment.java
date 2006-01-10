/*******************************************************************************
 * Copyright (c) 2005 BEA Systems, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    tyeung@bea.com - initial API    
 *******************************************************************************/
package org.eclipse.jdt.apt.core.env;

import org.eclipse.jdt.apt.core.util.EclipseMessager;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.dom.CompilationUnit;

import com.sun.mirror.apt.AnnotationProcessorEnvironment;

/**
 * Extended the APT {@link AnnotationProcessorEnvironment} to expose
 * extra API.
 */
public interface EclipseAnnotationProcessorEnvironment extends
		AnnotationProcessorEnvironment 
{	
	/**
	 * Return the AST of the file currently being processed. 
	 * @return the root of the fully flushed out DOM/AST of the file that is currently being processed.	 
	 *         This AST will contain binding information.
	 *         Return <code>null</code> for if called by a batch processor. 
	 */	
	CompilationUnit getAST();
	
	/**
	 * @return a messager for registering diagnostics.
	 */
	EclipseMessager getMessager();
	
	/**	
	 * @return the current processing phase. Either
	 * @link{Phase.RECONCILE} or @link{Phase.BUILD}
	 */
	Phase getPhase();
	
	/**
	 * @return the java project associated with the current processing phase
	 */
	IJavaProject getJavaProject();
	
	/**
	 * Add a type dependency on the type named <code>fullyQualifiedTypeName</code>
	 * @param fullyQualifiedTypeName the fully qualified (dot-separated) name of a type.
	 * @throws IllegalArgumentException if <code>fullyQualifiedTypeName</code> cannot be resolved to a type.
	 */
	void addTypeDependency(final String fullyQualifiedTypeName);
}
