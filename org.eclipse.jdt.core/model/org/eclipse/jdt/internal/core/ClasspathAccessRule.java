/*******************************************************************************
 * Copyright (c) 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jdt.internal.core;

import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.jdt.core.IAccessRule;
import org.eclipse.jdt.internal.compiler.env.AccessRule;
import org.eclipse.jdt.internal.compiler.problem.ProblemSeverities;

public class ClasspathAccessRule extends AccessRule implements IAccessRule {
	
	public ClasspathAccessRule(IPath pattern, int kind) {
		this(pattern.toString().toCharArray(), toSeverity(kind));
	}
	
	public ClasspathAccessRule(char[] pattern, int severity) {
		super(pattern, severity);
	}
	
	private static int toSeverity(int kind) {
		switch (kind) {
			case K_NON_ACCESSIBLE:
				return ProblemSeverities.Error;
			case K_DISCOURAGED:
				return ProblemSeverities.Warning;
			default:
				return -1;
		}
	}

	public IPath getPattern() {
		return new Path(new String(this.pattern));
	}

	public int getKind() {
		switch (this.severity) {
			case ProblemSeverities.Error:
				return K_NON_ACCESSIBLE;
			case ProblemSeverities.Warning:
				return K_DISCOURAGED;
			default:
				return K_ACCESSIBLE;
		}
	}

}
