package org.eclipse.jdt.core.tests.runtime;

/*
 * Licensed Materials - Property of IBM,
 * WebSphere Studio Workbench
 * (c) Copyright IBM Corp 2000
 */
import java.util.Vector;

/**
 * An abstract reader that continuously reads.
 */

abstract public class AbstractReader {
	protected String name;
	protected Thread readerThread;
	protected boolean isStopping= false;
/*
 * Creates a new reader with the given name.
 */
public AbstractReader(String name) {
	this.name = name;
}
/**
 * Continuously reads. Note that if the read involves waiting
 * it can be interrupted and a InterruptedException will be thrown.
 */
abstract protected void readerLoop();
/**
 * Start the thread that reads events.
 * 
 */
public void start() {
	this.readerThread = new Thread(
		new Runnable() {
			public void run () {
				readerLoop();
			}
		},
		AbstractReader.this.name);
	this.readerThread.start();
}
/**
 * Tells the reader loop that it should stop.
 */
public void stop() {
	this.isStopping= true;
	if (this.readerThread != null)
		this.readerThread.interrupt();
}
}
