/*******************************************************************************
 * Copyright (c) 2004, 2009 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jdt.core.tests.model;

import org.eclipse.core.runtime.IProgressMonitor;

/*
 * Counts the number of time isCancelled() is called.
 */
public class CancelCounter implements IProgressMonitor {
	public int count = 0;
	public void beginTask(String name, int totalWork) {}
	public void done() {}
	public void internalWorked(double work) {}
	public boolean isCanceled() {
		this.count++;
		return false;
	}
	public void setCanceled(boolean value) {}
	public void setTaskName(String name) {}
	public void subTask(String name) {}
	public void worked(int work) {}
}
