package org.eclipse.jdt.internal.core.index.impl;

public class MemoryCheckThread extends Thread {
	Runtime rt = Runtime.getRuntime();
	int timeToSleep;
	/**
	 * MemoryCheckThread constructor comment.
	 */
	public MemoryCheckThread() {
		super();
		setDaemon(true);
		setPriority(Thread.MAX_PRIORITY);
	}

	/**
	 * MemoryCheckThread constructor comment.
	 */
	public MemoryCheckThread(int time) {
		super();
		timeToSleep = time;
		setDaemon(true);
		setPriority(Thread.MAX_PRIORITY);
	}

	public long evaluateMemory() {
		return rt.totalMemory() - rt.freeMemory();
	}

	/**
	 * Insert the method's description here.
	 * Creation date: (6/27/00 9:14:35 AM)
	 */
	public void run() {
		while (true) {
			try {
				sleep(timeToSleep);
			} catch (Exception e) {
			}
			System.gc();
			System.out.println(evaluateMemory());
		}
	}

}
