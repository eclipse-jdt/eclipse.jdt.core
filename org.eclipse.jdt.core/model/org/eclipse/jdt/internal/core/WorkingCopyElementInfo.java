/*******************************************************************************
 * Copyright (c) 2000, 2003 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jdt.internal.core;

import java.util.ArrayList;

import org.eclipse.jdt.core.IProblemRequestor;
import org.eclipse.jdt.core.compiler.IProblem;

public class WorkingCopyElementInfo extends CompilationUnitElementInfo implements IProblemRequestor {
	/* Number of time the compilation unit has been opened for working copy */
	private int useCount;
	ArrayList problems;
	public WorkingCopyElementInfo() {
		this.useCount = 1;
	}
	public WorkingCopyElementInfo(int useCount) {
		this.useCount = useCount;
	}
	public void endReporting() {
	}
	public void acceptProblem(IProblem problem) {
		if (this.problems != null) {
			this.problems.add(problem);
		}
	}
	public void beginReporting() {
	}
	public int decrementUseCount() {
		if (this.useCount > 0) {
			return --this.useCount;
		} else {
			return -(++this.useCount);
		}
	}
	public int incrementUseCount() {
		if (this.useCount > 0) {
			return ++this.useCount;
		} else {
			return -(--this.useCount);
		}
	}
	public boolean isActive() {
		return true;
	}
	protected boolean isOpen() {
		return this.useCount > 0;
	}
	public int useCount() {
		if (this.useCount >= 0) {
			return this.useCount;
		} else {
			return -this.useCount;
		}
	}
	public String toString() {
		return "Working copy info (useCount=" + this.useCount + ")"; //$NON-NLS-1$ //$NON-NLS-2$
	}
}
