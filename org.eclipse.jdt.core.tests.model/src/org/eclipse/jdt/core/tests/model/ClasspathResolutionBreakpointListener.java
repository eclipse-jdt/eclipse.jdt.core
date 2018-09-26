/*******************************************************************************
 * Copyright (c) 2000, 2008 IBM Corporation and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jdt.core.tests.model;

import org.eclipse.jdt.internal.core.JavaProject;

public class ClasspathResolutionBreakpointListener extends JavaProject.ClasspathResolutionBreakpointListener {
	BPThread[] threads;
	public ClasspathResolutionBreakpointListener(BPThread[] threads) {
		this.threads = threads;
	}
	public void breakpoint(int bp) {
		for (int i = 0, length = this.threads.length; i < length; i++) {
			this.threads[i].suspend(bp); // no-op if not current thread
		}
	}
}
