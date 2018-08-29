/*******************************************************************************
 * Copyright (c) 2000, 2009 IBM Corporation and others.
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

public class TestProgressMonitor implements IProgressMonitor {

	private static TestProgressMonitor singleton = new TestProgressMonitor();
	public int isCanceledCounter;

/**
 * @see IProgressMonitor#beginTask
 */
public void beginTask(String name, int totalWork) {
}
/**
 * @see IProgressMonitor#done
 */
public void done() {
}
public static TestProgressMonitor getInstance() {
	return singleton;
}
public void internalWorked(double work) {
}
/**
 * @see IProgressMonitor#isCanceled
 */
public boolean isCanceled() {
	return --this.isCanceledCounter < 0;
}
public void reset() {
}
/**
 * @see IProgressMonitor#setCanceled
 */
public void setCanceled(boolean b) {
}
/*
 * Sets the number of time isCanceled() can be called before it returns true.
 */
public void setCancelledCounter(int counter) {
	this.isCanceledCounter = counter;
}
/**
 * @see IProgressMonitor#setTaskName
 */
public void setTaskName(String name) {
}
/**
 * @see IProgressMonitor#subTask
 */
public void subTask(String name) {
}
/**
 * @see IProgressMonitor#worked
 */
public void worked(int work) {
}
}
