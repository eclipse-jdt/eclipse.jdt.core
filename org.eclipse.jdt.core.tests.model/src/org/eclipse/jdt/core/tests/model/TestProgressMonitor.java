/*******************************************************************************
 * Copyright (c) 2002 IBM Corp. and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0 
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 ******************************************************************************/
package org.eclipse.jdt.core.tests.model;

import org.eclipse.core.runtime.IProgressMonitor;

public class TestProgressMonitor implements IProgressMonitor {

	private static IProgressMonitor singleton = new TestProgressMonitor();
	public boolean cancelled = false;

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
public static IProgressMonitor getInstance() {
	return singleton;
}
public void internalWorked(double work) {
}
/**
 * @see IProgressMonitor#isCanceled
 */
public boolean isCanceled() {
	return this.cancelled;
}
public void reset() {
}
/**
 * @see IProgressMonitor#setCanceled
 */
public void setCanceled(boolean b) {
	this.cancelled= b;
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
public void worked(double work) {
}
/**
 * @see IProgressMonitor#worked
 */
public void worked(int work) {
}
}
