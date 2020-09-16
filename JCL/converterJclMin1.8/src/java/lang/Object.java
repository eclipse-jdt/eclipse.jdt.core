/*******************************************************************************
 * Copyright (c) 2000, 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package java.lang;

public class Object {

	public Object() {
	}
	protected Object clone() throws CloneNotSupportedException {
		return null;
	}
	public boolean equals(Object obj) {
		return false;
	}
	protected void finalize() throws Throwable {
	}
    public final native Class<? extends Object> getClass();
	public int hashCode() {
		return -1;
	}
	public final void notify() throws IllegalMonitorStateException {
	}
	public final void notifyAll() throws IllegalMonitorStateException {
	}
	public String toString() {
		return null;
	}
	public final void wait() throws IllegalMonitorStateException,
			InterruptedException {
	}
	public final void wait(long millis) throws IllegalMonitorStateException,
			InterruptedException {
	}
	public final void wait(long millis, int nanos)
			throws IllegalMonitorStateException, InterruptedException {
	}
}
