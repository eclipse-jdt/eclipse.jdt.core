/*******************************************************************************
 * Copyright (c) 2000, 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jdt.internal.compiler.problem;

import org.eclipse.jdt.core.compiler.IProblem;
import org.eclipse.jdt.internal.compiler.CompilationResult;

/*
 * Special unchecked exception type used 
 * to abort from the compilation process
 *
 * should only be thrown from within problem handlers.
 */
public class AbortMethod extends AbortType {
	
	private static final long serialVersionUID = -1480267398969840003L; // backward compatible	
	
public AbortMethod(CompilationResult compilationResult, IProblem problem) {
	super(compilationResult, problem);
}
}
