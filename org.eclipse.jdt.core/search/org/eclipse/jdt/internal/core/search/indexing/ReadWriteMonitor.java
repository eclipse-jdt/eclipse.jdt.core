package org.eclipse.jdt.internal.core.search.indexing;

public class ReadWriteMonitor {

	/**
	 * <0 : writing (cannot go beyond -1, i.e one concurrent writer)
	 * =0 : idle
	 * >0 : reading (number of concurrent readers)
	 */
	private int status = 0;
/**
 * Concurrent reading is allowed
 * Blocking only when already writing.
 */
public synchronized void enterRead() {

	while (status < 0){
		try {
			wait();
		} catch(InterruptedException e){
		}
	}
	status++;
}
/**
 * Only one writer at a time is allowed to perform
 * Blocking only when already writing or reading.
 */
public synchronized void enterWrite() {

	while (status != 0){
		try {
			wait();
		} catch(InterruptedException e){
		}
	}
	status--;
}
/**
 * Only notify waiting writer(s) if last reader
 */
public synchronized void exitRead() {

	if (--status == 0) notifyAll();
}
/**
 * When writing is over, all readers and possible
 * writers are granted permission to restart concurrently
 */
public synchronized void exitWrite() {

	if (++status == 0) notifyAll();
}
}
