/*******************************************************************************
 * Copyright (c) 2000, 2008 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jdt.core.tests.model;

import org.eclipse.jdt.core.tests.model.Semaphore.TimeOutException;

public class BPThread {
	public static int TIMEOUT = 1000;
	private String name;
	private Thread thread;
	private Semaphore sem = new Semaphore();
	private int breakppoint = -1;

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
		if (isSuspended())
			resume();
		long start = System.currentTimeMillis();
		while (this.breakppoint != bp) {
			try {
				Thread.sleep(100);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			if ((System.currentTimeMillis() - start) > TIMEOUT)
				throw new RuntimeException(new TimeOutException());
		}
	}
	public void start(Runnable runnable) {
		this.thread = new Thread(runnable, this.name);
		this.thread.start();
	}
	public String toString() {
		return this.name;
	}
}
