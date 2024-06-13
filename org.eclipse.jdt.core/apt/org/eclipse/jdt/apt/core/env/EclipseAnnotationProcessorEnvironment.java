/*******************************************************************************
 * Copyright (c) 2005, 2007 BEA Systems, Inc.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
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

	@Override
	EclipseMessager getMessager();

	/**
	 * Indicate whether the processor is being called during a build or during editing
	 * (that is, during reconcile).
	 * <p>
	 * Note that processors that behave differently depending on phase may cause
	 * inconsistent results, such as problems showing up in the Problems view but not in
	 * the editor window. If the goal is to improve edit-time performance by skipping
	 * processing during reconcile, it is recommended to use the
	 * {@link org.eclipse.jdt.apt.core.util.AptPreferenceConstants#PROCESSING_IN_EDITOR_DISABLED_OPTION
	 * PROCESSING_IN_EDITOR_DISABLED} option instead.
	 *
	 * @return the current processing phase: either {@link Phase#RECONCILE} or
	 *         {@link Phase#BUILD}
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
