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
package org.eclipse.jdt.internal.compiler.env;

import org.eclipse.jdt.core.compiler.CharOperation;
import org.eclipse.jdt.internal.compiler.problem.ProblemSeverities;

public class AccessRule {
	
	public char[] pattern;
	public int severity;
	
	public AccessRule(char[] pattern, int severity) {
		this.pattern = pattern;
		this.severity = severity;
	}
	
	public int hashCode() {
		return this.severity * 17 + CharOperation.hashCode(this.pattern);
	}
	
	public boolean equals(Object obj) {
		if (!(obj instanceof AccessRule)) return false;
		AccessRule other = (AccessRule) obj;
		if (this.severity != other.severity) return false;
		return CharOperation.equals(this.pattern, other.pattern);
	}

	public String toString() {
		StringBuffer buffer = new StringBuffer();
		buffer.append("pattern="); //$NON-NLS-1$
		buffer.append(this.pattern);
		switch (this.severity) {
			case ProblemSeverities.Error:
				buffer.append(" (NON ACCESSIBLE)"); //$NON-NLS-1$
				break;
			case ProblemSeverities.Warning:
				buffer.append(" (DISCOURAGED)"); //$NON-NLS-1$
				break;
			default:
				buffer.append(" (ACCESSIBLE)"); //$NON-NLS-1$
				break;
		}
		return buffer.toString();
	}
}
