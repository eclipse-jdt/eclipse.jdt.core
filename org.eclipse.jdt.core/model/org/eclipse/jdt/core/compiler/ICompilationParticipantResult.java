/*******************************************************************************
 * Copyright (c) 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *    
 *******************************************************************************/
package org.eclipse.jdt.core.compiler;

import org.eclipse.core.resources.IFile;

public interface ICompilationParticipantResult {

/**
 * Returns the contents of the compilation unit.
 */
char[] getContents();

/**
 * Returns the IFile representing the compilation unit.
 */
IFile getFile();

/**
 * Record the added/changed generated files that need to be compiled.
 */
void recordAddedGeneratedFiles(IFile[] addedGeneratedFiles);

/**
 * Record the generated files that need to be deleted.
 */
void recordDeletedGeneratedFiles(IFile[] deletedGeneratedFiles);

/**
 * Record new problems to report against this compilationUnit.
 */
void recordNewProblems(IProblem[] newProblems);

/**
 * Record the fully-qualified type names of any new dependencies, each name is of the form 'p1.p2.A.B'.
 */
void recordDependencies(String[] typeNameDependencies);
}
