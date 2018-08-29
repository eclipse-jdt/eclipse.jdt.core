/*******************************************************************************
 * Copyright (c) 2004, 2009 IBM Corporation and others.
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

import org.eclipse.core.runtime.IProgressMonitor;

/*
 * Cancels the operation on the count time isCanceled() is called.
 */
public class Canceler implements IProgressMonitor {
	int count;
	public Canceler(int count) {
		this.count = count;
	}
	public void beginTask(String name, int totalWork) {}
	public void done() {}
	public void internalWorked(double work) {}
	public boolean isCanceled() {
		return --this.count < 0;
	}
	public void setCanceled(boolean value) {}
	public void setTaskName(String name) {}
	public void subTask(String name) {}
	public void worked(int work) {}
}

