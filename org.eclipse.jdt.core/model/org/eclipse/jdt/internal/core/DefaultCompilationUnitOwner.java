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

import org.eclipse.jdt.core.*;
import org.eclipse.jdt.core.CompilationUnitOwner;

/**
 * TODO: comment
 */
public class DefaultCompilationUnitOwner extends CompilationUnitOwner {
	public static final DefaultCompilationUnitOwner PRIMARY = new DefaultCompilationUnitOwner();

	private IBufferFactory factory;
	private IProblemRequestor problemRequestor;
	public DefaultCompilationUnitOwner() {
	}
	public DefaultCompilationUnitOwner(IBufferFactory factory, IProblemRequestor problemRequestor) {
		this.factory = factory;
		this.problemRequestor = problemRequestor;
	}
	public IBuffer createBuffer(ICompilationUnit compilationUnit) {
		if (this.factory != null) {
			return this.factory.createBuffer(compilationUnit);
		} else {
			return BufferManager.getDefaultBufferManager().createBuffer(compilationUnit);
		}
	}
	public boolean equals(Object obj) {
		if (!(obj instanceof DefaultCompilationUnitOwner)) return false;
		DefaultCompilationUnitOwner other = (DefaultCompilationUnitOwner)obj;
		if (this.factory == null) return this == other;
		return this.factory.equals(other.factory);
	}
	public IProblemRequestor getProblemRequestor() {
		return this.problemRequestor;
	}
	public int hashCode() {
		if (this.factory == null) return super.hashCode();
		return this.factory.hashCode();
	}
	public String toString() {
		if (this == PRIMARY) {
			return "Primary owner"; //$NON-NLS-1$
		}
		StringBuffer buffer = new StringBuffer("CU owner for\n"); //$NON-NLS-1$
		buffer.append("-factory: ").append(this.factory).append('\n'); //$NON-NLS-1$
		buffer.append("-problemRequestor: ").append(this.problemRequestor); //$NON-NLS-1$
		return buffer.toString();
	}

}
