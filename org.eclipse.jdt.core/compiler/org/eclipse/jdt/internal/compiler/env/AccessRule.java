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
import org.eclipse.jdt.core.compiler.IProblem;

public class AccessRule {
	
	public char[] pattern;
	public int problemId;
	
	public AccessRule(char[] pattern, int problemId) {
		this.pattern = pattern;
		this.problemId = problemId;
	}
	
	public int hashCode() {
		return this.problemId * 17 + CharOperation.hashCode(this.pattern);
	}
	
	public boolean equals(Object obj) {
		if (!(obj instanceof AccessRule)) return false;
		AccessRule other = (AccessRule) obj;
		if (this.problemId != other.problemId) return false;
		return CharOperation.equals(this.pattern, other.pattern);
	}

	public String toString() {
		StringBuffer buffer = new StringBuffer();
		buffer.append("pattern="); //$NON-NLS-1$
		buffer.append(this.pattern);
		switch (this.problemId) {
			case IProblem.ForbiddenReference:
				buffer.append(" (NON ACCESSIBLE)"); //$NON-NLS-1$
				break;
			case IProblem.DiscouragedReference:
				buffer.append(" (DISCOURAGED)"); //$NON-NLS-1$
				break;
			default:
				buffer.append(" (ACCESSIBLE)"); //$NON-NLS-1$
				break;
		}
		return buffer.toString();
	}
}
