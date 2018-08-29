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

/**
 * A semaphore implementation with timeout.
 */
public class Semaphore {

	private String name = null;
	private int permissions = 0;

	public static class TimeOutException extends Exception {
		private static final long serialVersionUID = 1L;
	}
	public Semaphore() {
		this(null, 0);
	}
	public Semaphore(int initialPermisions) {
		this(null, initialPermisions);
	}
	public Semaphore(String name, int initialPermissions) {
		this.name = name;
		this.permissions = initialPermissions;
	}
	public void acquire() {
		try {
			acquire(Long.MAX_VALUE);
		} catch (TimeOutException e) {
			e.printStackTrace();
		}
	}
	public synchronized void acquire(long timeout) throws TimeOutException {
		long start = System.currentTimeMillis();
		while (this.permissions <= 0 && timeout > 0) {
			try {
				if (this.name != null) System.out.println(Thread.currentThread() + " - waiting to acquire: " + this.name); //$NON-NLS-1$
				if (timeout == Long.MAX_VALUE) {
					wait();
				} else {
					wait(timeout);
				}
			} catch(InterruptedException e){
			}
			timeout -= System.currentTimeMillis() - start;
		}
		if (timeout <= 0) {
			throw new TimeOutException();
		}
		this.permissions--;
		if (this.name != null) System.out.println(Thread.currentThread() + " - acquired: " + this.name); //$NON-NLS-1$
	}
	public int getCurrentPemissions() {
		return this.permissions;
	}
	public synchronized void release() {
		if (this.name != null) System.out.println(Thread.currentThread() + " - releasing: " + this.name); //$NON-NLS-1$
		if (++this.permissions > 0) notifyAll();
	}
}
