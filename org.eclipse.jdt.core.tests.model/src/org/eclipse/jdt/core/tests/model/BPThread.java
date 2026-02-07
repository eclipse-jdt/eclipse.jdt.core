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

import org.eclipse.jdt.core.tests.model.Semaphore.TimeOutException;

public class BPThread {
	public static int TIMEOUT = 1000;
	private final String name;
	private Thread thread;
	private final Semaphore sem = new Semaphore();
	private volatile int breakppoint = -1;

	public BPThread(String name) {
		this.name = name;
	}
	public boolean isSuspended() {
		return this.sem.getCurrentPemissions() <= 0;
	}
	public void runToEnd() {
		resume();
		try {
			this.thread.join(TIMEOUT);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}
	void suspend(int bp) {
		if (this.thread != Thread.currentThread())
			return;
		this.breakppoint = bp;
		try {
			this.sem.acquire(TIMEOUT);
		} catch (TimeOutException e) {
			throw new RuntimeException(e);
		}
	}
	public void resume() {
		this.breakppoint = -1;
		this.sem.release();
	}
	public void runToBP(int bp) {
		if (isSuspended()) {
			resume();
		}
		long timeoutNanos = System.nanoTime() + TIMEOUT * 1_000_000L;
		while (this.breakppoint != bp) {
			try {
				Thread.sleep(1);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			if (System.nanoTime() > timeoutNanos) {
				throw new RuntimeException(new TimeOutException());
			}
		}
	}
	public void start(Runnable runnable) {
		this.thread = new Thread(runnable, this.name);
		this.thread.start();
	}
	@Override
	public String toString() {
		return this.name;
	}
}
