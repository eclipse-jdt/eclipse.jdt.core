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
import org.eclipse.jdt.core.IBuffer;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.WorkingCopyOwner;

/**
 * A working copy owner that creates internal buffers.
 * It also defines the PRIMARY working copy owner that is used by JDT/Core.
 */
public class DefaultWorkingCopyOwner extends WorkingCopyOwner {

	/**
	 * Note this field is temporary public so that JDT/UI can reach in and change the factory. It will disapear before 3.0.
	 */
	public IBufferFactory factory; // TODO: remove before 3.0
		
	public static final  DefaultWorkingCopyOwner PRIMARY = 
		new DefaultWorkingCopyOwner() {
			public IBuffer createBuffer(ICompilationUnit workingCopy) {
				if (this.factory == null) {
					return BufferManager.getDefaultBufferManager().createBuffer(workingCopy);
				} else {
					// TODO: change to use a org.eclipse.text buffer
					return this.factory.createBuffer(workingCopy);
				}
			}			
			public String toString() {
				return "Primary owner"; //$NON-NLS-1$
			}
		};

	public DefaultWorkingCopyOwner() {
	}
}
